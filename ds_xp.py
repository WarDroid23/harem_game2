import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

xp_logic = """
        if (session.deployedCharacterId != null) {
            val updatedCharacters = currentGameState.characters.map { c ->
                if (c.id == session.deployedCharacterId) {
                    var newXp = c.xp + session.boss.rewardXp
                    var newLevel = c.level
                    var newSp = c.skillPoints
                    var newMaxHp = c.maxHp
                    var newHp = newPlayerHp
                    var nextLevelXp = newLevel * 100
                    
                    while (newXp >= nextLevelXp) {
                        newXp -= nextLevelXp
                        newLevel++
                        newSp++
                        newMaxHp += 5
                        newHp = newMaxHp // Full heal on level up
                        nextLevelXp = newLevel * 100
                    }
                    if (newPlayerHp > 0 && newHp < newMaxHp) {
                        newHp = newPlayerHp // Keep current damage if no level up
                    }
                    c.copy(hp = newHp, maxHp = newMaxHp, xp = newXp, level = newLevel, skillPoints = newSp)
                } else c
            }
            updateState { it.copy(characters = updatedCharacters) }
        } else {
            player.hp = newPlayerHp
        }
"""

# Replace the block around 1959
old_block = """        if (session.deployedCharacterId != null) {
            val updatedCharacters = currentGameState.characters.map { c ->
                if (c.id == session.deployedCharacterId) c.copy(hp = newPlayerHp) else c
            }
            updateState { it.copy(characters = updatedCharacters) }
        } else {
            player.hp = newPlayerHp
        }"""

text = text.replace(old_block, xp_logic)

# In victory, we should also log the character XP gain
log_victory_old = """            val itemDropStr = if (droppedItem != null) " • Nalezeno: ${droppedItem.name}" else ""
            lootInfo = "+${session.boss.rewardGold} zlatých • +${session.boss.rewardXp} XP$itemDropStr"
"""
log_victory_new = """            val itemDropStr = if (droppedItem != null) " • Nalezeno: ${droppedItem.name}" else ""
            val charExpStr = if (session.deployedCharacterId != null) " • Dívka +${session.boss.rewardXp} ZK" else ""
            lootInfo = "+${session.boss.rewardGold} zlatých • +${session.boss.rewardXp} XP$charExpStr$itemDropStr"
"""

text = text.replace(log_victory_old, log_victory_new)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)

