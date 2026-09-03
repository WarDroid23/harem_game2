import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

# 1. upgradeBuilding
target1 = """        updateState { state ->
            val p = state.player.copy("""
replacement1 = """        updateState { state ->
            val p = state.player.copy("""

# Wait, it's easier to just append autoSave() before return Pair(...)
target1_end = """        }
        return Pair(true, msg)
    }

    fun upgradeTerritory"""

replacement1_end = """        }
        autoSave()
        return Pair(true, msg)
    }

    fun upgradeTerritory"""

text = text.replace(target1_end, replacement1_end)

# 2. upgradeTerritory
target2_end = """        }
        return Pair(true, msg)
    }

    fun trainEndurance()"""

replacement2_end = """        }
        autoSave()
        return Pair(true, msg)
    }

    fun trainEndurance()"""

text = text.replace(target2_end, replacement2_end)

# 3. runArenaExpedition
target3_end = """            state.copy(
                player = newPlayer,
                characters = updatedGirls
            )
        }
        
        return logs
    }"""

replacement3_end = """            state.copy(
                player = newPlayer,
                characters = updatedGirls
            )
        }
        autoSave()
        return logs
    }"""

text = text.replace(target3_end, replacement3_end)

# 4. endCombat
target4_end = """    fun endCombat() {
        _combatState.value = null
    }"""

replacement4_end = """    fun endCombat() {
        _combatState.value = null
        autoSave()
    }"""

text = text.replace(target4_end, replacement4_end)

# 5. Add coroutine in init
target_init = """    init {
        _currentTheme.value = _gameState.value.currentTheme"""

replacement_init = """    init {
        _currentTheme.value = _gameState.value.currentTheme
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            while (true) {
                kotlinx.coroutines.delay(5 * 60 * 1000L) // 5 minut
                autoSave()
            }
        }"""

text = text.replace(target_init, replacement_init)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
