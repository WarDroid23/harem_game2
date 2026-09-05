import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

new_method = """
    fun recruitCharacter(type: String): Pair<Boolean, String> {
        var result = Pair(false, "Neznámý typ náboru.")
        updateState { current ->
            val p = current.player
            
            // Define cost based on type
            val costGold: Int
            val costMana: Int
            val minRarity: Int
            val title: String
            
            when (type) {
                "basic" -> { costGold = 250; costMana = 0; minRarity = 1; title = "Běžný otrok" }
                "advanced" -> { costGold = 600; costMana = 20; minRarity = 2; title = "Vzácný zajatec" }
                "elite" -> { costGold = 1500; costMana = 50; minRarity = 3; title = "Exkluzivní trofej" }
                else -> return@updateState current
            }
            
            if (p.gold < costGold || p.mana < costMana) {
                result = Pair(false, "Nedostatek surovin (Potřebuješ $costGold Zlata a $costMana Many).")
                return@updateState current
            }
            
            if (current.characters.size >= p.maxPopulation) {
                result = Pair(false, "Tvůj harém je plný! (Kapacita: ${p.maxPopulation})")
                return@updateState current
            }
            
            // Generate char
            val names = listOf("Lumia", "Sera", "Thalia", "Vex", "Kaelia", "Rina", "Myra", "Nyx", "Elaria", "Zora", "Lyra", "Tess", "Aria", "Morgana", "Lilith", "Carmilla", "Isolde", "Ophelia")
            val randomName = names.random()
            val archetypes = com.example.haremdark.data.StaticData.ARCHETYPES.keys.toList()
            val chosenArchetype = archetypes.random()
            val age = (18..26).random()
            
            // Stats based on type
            val statBoost = minRarity * 15
            
            val newGirl = com.example.haremdark.models.Character(
                id = "c_${java.util.UUID.randomUUID().toString().take(8)}",
                name = randomName,
                age = age,
                archetypeId = chosenArchetype,
                rarity = minRarity,
                hp = 100 + (minRarity * 20),
                maxHp = 100 + (minRarity * 20),
                srdce = 50 + (0..statBoost).random(),
                poslusnost = 20 + (0..statBoost).random(),
                vlhkost = 40 + (0..statBoost).random(),
                submisivita = 30 + (0..statBoost).random(),
                loajalita = 20 + (0..statBoost).random(),
                touha = 40 + (0..statBoost).random(),
                level = minRarity,
                xp = 0,
                skillPoints = minRarity - 1,
                skills = mutableMapOf("combat" to (0..minRarity).random(), "defense" to (0..minRarity).random(), "production" to (0..minRarity).random(), "rental" to (0..minRarity).random())
            )
            
            val newPlayer = p.copy(
                gold = p.gold - costGold,
                mana = p.mana - costMana
            )
            
            val newList = current.characters.toMutableList()
            newList.add(newGirl)
            
            result = Pair(true, "Nábor úspěšný! Získal jsi novou dívku: $randomName.")
            
            current.copy(
                player = newPlayer,
                characters = newList,
                gameLog = current.gameLog + "⛓️ Úspěšný nábor ($title): $randomName se přidává do harému!"
            )
        }
        if (result.first) autoSave()
        return result
    }
"""

text = text.rstrip()
if text.endswith('}'):
    text = text[:-1] + new_method + "\n}\n"

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
