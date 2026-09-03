import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

new_func = """    fun autoSave() {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 0,
            saveDate = "Den ${state.player.day} (Autosave)"
        )
        val str = json.encodeToString(current)
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("save_slot_autosave", str).apply()
    }

    fun quickSave() {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 99,
            saveDate = "Den ${state.player.day} (Quick Save)"
        )
        val str = json.encodeToString(current)
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("save_slot_quicksave", str).apply()
        addLog("⚡ Rychlé uložení dokončeno.")
    }"""

text = text.replace(
"""    fun autoSave() {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 0,
            saveDate = "Den ${state.player.day} (Autosave)"
        )
        val str = json.encodeToString(current)
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("save_slot_autosave", str).apply()
    }""", new_func)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
