with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """            val modifiedYield = yield.copy(gold = totalPassiveGold + rentalIncome)
            resourceManager.applyYield(p, modifiedYield)"""

replacement = """            val modifiedYield = yield.copy(gold = totalPassiveGold + rentalIncome)
            resourceManager.applyYield(p, modifiedYield)
            
            // Record production history
            val newStat = DailyResourceStat(
                day = newDay,
                goldProduced = modifiedYield.gold,
                manaProduced = modifiedYield.mana,
                woodProduced = modifiedYield.wood,
                stoneProduced = modifiedYield.stone,
                ironProduced = modifiedYield.iron
            )
            val newHistory = (current.resourceHistory + newStat).takeLast(14) // Keep last 14 days
"""

text = text.replace(target, replacement)

# Now update the current.copy block
copy_target = """            current.copy(
                player = p,
                characters = updatedCharacters,
                gameLog = logs,
                dailyMissions = newMissions,
                lastMissionUpdateDay = newDay,
                activeBuffs = newBuffs
            )"""

copy_replacement = """            current.copy(
                player = p,
                characters = updatedCharacters,
                gameLog = logs,
                dailyMissions = newMissions,
                lastMissionUpdateDay = newDay,
                activeBuffs = newBuffs,
                resourceHistory = newHistory
            )"""

text = text.replace(copy_target, copy_replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
