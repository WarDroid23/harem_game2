import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

new_method = """
    fun upgradeCharacterSkill(characterId: String, skillName: String): Pair<Boolean, String> {
        var result = Pair(false, "Chyba při vylepšení dovednosti.")
        updateState { current ->
            val charIndex = current.characters.indexOfFirst { it.id == characterId }
            if (charIndex != -1) {
                val char = current.characters[charIndex]
                if (char.skillPoints > 0) {
                    val updatedSkills = char.skills.toMutableMap()
                    val currentVal = updatedSkills[skillName] ?: 0
                    updatedSkills[skillName] = currentVal + 1
                    
                    val updatedChar = char.copy(
                        skillPoints = char.skillPoints - 1,
                        skills = updatedSkills
                    )
                    
                    val newList = current.characters.toMutableList()
                    newList[charIndex] = updatedChar
                    
                    result = Pair(true, "Dovednost vylepšena!")
                    current.copy(characters = newList)
                } else {
                    result = Pair(false, "Nedostatek dovednostních bodů.")
                    current
                }
            } else {
                current
            }
        }
        if (result.first) autoSave()
        return result
    }"""

text = text.rstrip()
if text.endswith('}'):
    text = text[:-1] + new_method + "\n}\n"

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
