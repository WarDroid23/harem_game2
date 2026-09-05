import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

new_method = """
    fun togglePin(characterId: String): Pair<Boolean, String> {
        var msg = ""
        var success = false
        updateState { current ->
            val updated = current.characters.map { c ->
                if (c.id == characterId) {
                    val pinned = !c.isPinned
                    msg = if (pinned) "${c.name} byla připnuta na vrch seznamu." else "${c.name} již není připnutá."
                    success = true
                    c.copy(isPinned = pinned)
                } else c
            }
            current.copy(characters = updated)
        }
        if (success) autoSave()
        return Pair(success, msg)
    }
"""

text = text.rstrip()
if text.endswith('}'):
    text = text[:-1] + new_method + "\n}\n"

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
