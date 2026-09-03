import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """            val basePassiveGold = yield.gold + haremIncomeBase + haremIncomeBonus
            val globalIncomeMultiplier = 1.0f + (0.50f * level6Count)
            val totalPassiveGold = (basePassiveGold * globalIncomeMultiplier).toInt()"""

replacement = """            val basePassiveGold = yield.gold + haremIncomeBase + haremIncomeBonus
            var globalIncomeMultiplier = 1.0f + (0.50f * level6Count)
            
            // Apply RESOURCE_BOOST buffs
            val resourceBuffs = current.activeBuffs.filter { it.type == "RESOURCE_BOOST" }.sumOf { it.value }
            if (resourceBuffs > 0) {
                globalIncomeMultiplier += (resourceBuffs / 100f)
            }
            
            val totalPassiveGold = (basePassiveGold * globalIncomeMultiplier).toInt()"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
