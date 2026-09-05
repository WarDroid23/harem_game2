with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target1 = """            // Apply RESOURCE_BOOST buffs
            val resourceBuffs = current.activeBuffs.filter { it.type == "RESOURCE_BOOST" }.sumOf { it.value }
            if (resourceBuffs > 0) {
                globalIncomeMultiplier += (resourceBuffs / 100f)
            }
            
            val totalPassiveGold = (basePassiveGold * globalIncomeMultiplier).toInt()"""

replacement1 = """            // Apply RESOURCE_BOOST buffs
            val resourceBuffs = current.activeBuffs.filter { it.type == "RESOURCE_BOOST" }.sumOf { it.value }
            if (resourceBuffs > 0) {
                globalIncomeMultiplier += (resourceBuffs / 100f)
            }
            
            // Add Relationship Buffs
            var relResMultiplier = 0.0f
            current.characters.forEach { c ->
                val rel = c.getRelationship()
                if (rel == com.example.haremdark.models.RelStatus.DEVOTED) relResMultiplier += rel.buffValue
                if (rel == com.example.haremdark.models.RelStatus.OBEDIENT) relResMultiplier += rel.buffValue
            }
            globalIncomeMultiplier += relResMultiplier
            
            val totalPassiveGold = (basePassiveGold * globalIncomeMultiplier).toInt()"""

text = text.replace(target1, replacement1)

target2 = """            var rentalIncome = 0
            val updatedCharacters = current.characters.map { c ->
                val copy = c.copy()
                if (copy.naNajmu) {
                    val dailyIncome = when (copy.klient) {"""

replacement2 = """            var rentalIncome = 0
            val updatedCharacters = current.characters.map { c ->
                val copy = c.copy()
                val rel = copy.getRelationship()
                if (copy.naNajmu) {
                    var dailyIncome = when (copy.klient) {"""

text = text.replace(target2, replacement2)

target3 = """                        else -> 0
                    }
                    if (dmg > 0) {"""

replacement3 = """                        else -> 0
                    }
                    
                    if (rel == com.example.haremdark.models.RelStatus.BROKEN) {
                        dailyIncome = (dailyIncome * (1.0f + rel.buffValue)).toInt()
                    }
                    
                    if (dmg > 0) {"""

text = text.replace(target3, replacement3)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
