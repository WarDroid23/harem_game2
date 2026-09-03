import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        val teamDefense = girlsInTeam.sumOf { it.poslusnost / 15 + 2 }"""

replacement = """        var baseDefense = girlsInTeam.sumOf { it.poslusnost / 15 + 2 }
        val defenseBuffs = current.activeBuffs.filter { it.type == "DEFENSE" }.sumOf { it.value }
        if (defenseBuffs > 0) {
            baseDefense += defenseBuffs
        }
        val teamDefense = baseDefense"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
