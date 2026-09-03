import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

text = text.replace('if (slot == 0) "save_slot_autosave" else "save_slot_$slot"', 'when(slot) { 0 -> "save_slot_autosave"; 99 -> "save_slot_quicksave"; else -> "save_slot_$slot" }')
text = text.replace('val str = prefs.getString("save_slot_$slot", null) ?: return false', 'val key = when(slot) { 0 -> "save_slot_autosave"; 99 -> "save_slot_quicksave"; else -> "save_slot_$slot" }\n        val str = prefs.getString(key, null) ?: return false')

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
