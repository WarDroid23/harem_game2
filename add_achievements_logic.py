import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

new_method = """
    fun checkAchievements(): List<String> {
        val current = _gameState.value
        val player = current.player
        val newUnlocks = mutableListOf<String>()
        val currentUnlocks = player.unlockedAchievements.toMutableList()

        val allAchs = com.example.haremdark.models.AchievementList.allAchievements
        
        fun award(id: String) {
            if (!currentUnlocks.contains(id)) {
                currentUnlocks.add(id)
                newUnlocks.add(id)
            }
        }

        // Conditions
        if (current.characters.size >= 10) award("ach_harem_10")
        if (current.characters.size >= 20) award("ach_harem_20")
        
        val totalAffinity = current.characters.sumOf { it.affinityPoints }
        if (totalAffinity >= 250) award("ach_affinity_total")
        
        if (current.defeatedBosses.size >= 3) award("ach_boss_slayer")
        
        if (current.characters.any { it.level >= 10 }) award("ach_arena_champion")
        
        if (player.gold >= 10000) award("ach_wealthy")
        
        val fortressLevel = current.buildings.firstOrNull { it.id == "b1" }?.level ?: 1
        if (fortressLevel >= 5) award("ach_domain_max")
        
        if (current.characters.any { it.getRelationship() == com.example.haremdark.models.RelStatus.BLOOD_SISTER }) award("ach_blood_sister")
        
        if (newUnlocks.isNotEmpty()) {
            val updatedPlayer = player.copy(unlockedAchievements = currentUnlocks)
            updateState { it.copy(player = updatedPlayer) }
            autoSave()
        }
        
        return newUnlocks
    }

    fun setActiveTitle(titleId: String?): Boolean {
        var success = false
        updateState { state ->
            if (titleId == null || state.player.unlockedAchievements.contains(titleId)) {
                success = true
                state.copy(player = state.player.copy(activeTitle = titleId))
            } else {
                state
            }
        }
        if (success) autoSave()
        return success
    }
"""

text = text.rstrip()
if text.endswith('}'):
    text = text[:-1] + new_method + "\n}\n"

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
