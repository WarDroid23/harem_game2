import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

old_logic = """            // Passive income from mafia and buildings
            val buildingIncome = current.buildings.sumOf { it.level * 15 }
            val territoryIncome = current.territories.filter { it.level > 0 }.sumOf { it.baseIncome * it.level }
            
            val haremIncomeBase = current.haremLevel * 10
            val haremIncomeBonus = (haremIncomeBase * (0.10f * level2Count)).toInt()
            
            val basePassive = buildingIncome + territoryIncome + haremIncomeBase + haremIncomeBonus
            val globalIncomeMultiplier = 1.0f + (0.50f * level6Count)
            val totalPassive = (basePassive * globalIncomeMultiplier).toInt()"""

new_logic = """            // Domain Resources & Passive income
            val resourceManager = DomainResourceManager()
            val yield = resourceManager.calculateDailyYield(current)
            
            val haremIncomeBase = current.haremLevel * 10
            val haremIncomeBonus = (haremIncomeBase * (0.10f * level2Count)).toInt()
            
            val basePassiveGold = yield.gold + haremIncomeBase + haremIncomeBonus
            val globalIncomeMultiplier = 1.0f + (0.50f * level6Count)
            val totalPassiveGold = (basePassiveGold * globalIncomeMultiplier).toInt()"""

text = text.replace(old_logic, new_logic)

old_gold_update = "p.gold += totalPassive + rentalIncome"
new_gold_update = """            // Apply Domain Resources
            val modifiedYield = yield.copy(gold = totalPassiveGold + rentalIncome)
            resourceManager.applyYield(p, modifiedYield)"""

text = text.replace(old_gold_update, new_gold_update)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
