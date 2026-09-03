import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """            val logEntry = "🌅 Den $newDay svítá. Energie plně obnovena (${p.sexEnergy}/${p.darkEnergy}). Příjem: +${totalPassiveGold + rentalIncome} zlatých."
            val logs = (listOf(logEntry) + current.gameLog).take(30)"""

replacement = """
            // Buff durations
            val newBuffs = current.activeBuffs.map { it.copy(durationDays = it.durationDays - 1) }.filter { it.durationDays > 0 }.toMutableList()
            var bondingLog: String? = null

            // Random Bonding Event
            if (updatedCharacters.size >= 2 && Math.random() < 0.3) {
                val shuffled = updatedCharacters.shuffled()
                val c1 = shuffled[0]
                val c2 = shuffled[1]
                
                val eventType = listOf("COMBAT", "PRODUCTION", "MORALE").random()
                when (eventType) {
                    "COMBAT" -> {
                        bondingLog = "⚔️ Pouto: ${c1.name} a ${c2.name} spolu v noci trénovaly. Celý harém má bonus +10% k poškození na 2 dny!"
                        newBuffs.add(PartyBuff("bond_combat_${newDay}", "Bojové pouto", "Bonus k poškození z nočního tréninku.", 2, "DAMAGE", 10))
                    }
                    "PRODUCTION" -> {
                        bondingLog = "🛠️ Pouto: ${c1.name} a ${c2.name} zorganizovaly výpomoc v dominiu. Zvýšená produkce zlata o 15% na 2 dny!"
                        newBuffs.add(PartyBuff("bond_prod_${newDay}", "Organizační talent", "Bonus k produkci surovin.", 2, "RESOURCE_BOOST", 15))
                    }
                    "MORALE" -> {
                        bondingLog = "💕 Pouto: ${c1.name} a ${c2.name} strávily noc spolu a posílily své pouto. Dočasná odolnost a nadšení!"
                        newBuffs.add(PartyBuff("bond_morale_${newDay}", "Hřejivé pouto", "Pasivní odolnost harému.", 3, "DEFENSE", 10))
                        c1.vlhkost = (c1.vlhkost + 20).coerceAtMost(100)
                        c2.vlhkost = (c2.vlhkost + 20).coerceAtMost(100)
                        c1.loajalita = (c1.loajalita + 5).coerceAtMost(100)
                        c2.loajalita = (c2.loajalita + 5).coerceAtMost(100)
                    }
                }
            }

            val logEntry = "🌅 Den $newDay svítá. Energie plně obnovena (${p.sexEnergy}/${p.darkEnergy}). Příjem: +${totalPassiveGold + rentalIncome} zlatých."
            val logsList = mutableListOf(logEntry)
            if (bondingLog != null) logsList.add(bondingLog!!)
            val logs = (logsList + current.gameLog).take(30)
"""

text = text.replace(target, replacement)

# Now update the current.copy(...) block
copy_target = """            current.copy(
                player = p,
                characters = updatedCharacters,
                gameLog = logs,
                dailyMissions = newMissions,
                lastMissionUpdateDay = newDay
            )"""

copy_replacement = """            current.copy(
                player = p,
                characters = updatedCharacters,
                gameLog = logs,
                dailyMissions = newMissions,
                lastMissionUpdateDay = newDay,
                activeBuffs = newBuffs
            )"""

text = text.replace(copy_target, copy_replacement)

# Oh, we need to import PartyBuff in GameEngine? PartyBuff is in the same package (com.example.haremdark.models). 
# So it doesn't need explicit import if GameSave is imported (which it is, or it is in domain, but it has access to GameModels).

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
