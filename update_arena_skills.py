import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target1 = """        var baseTeamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 }
        val damageBuffs = current.activeBuffs.filter { it.type == "DAMAGE" }.sumOf { it.value }"""

replacement1 = """        var baseTeamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 + ((it.skills["combat"] ?: 0) * 5) }
        val damageBuffs = current.activeBuffs.filter { it.type == "DAMAGE" }.sumOf { it.value }"""
text = text.replace(target1, replacement1)

target2 = """        var baseDefense = girlsInTeam.sumOf { it.poslusnost / 15 + 2 }
        val defenseBuffs = current.activeBuffs.filter { it.type == "DEFENSE" }.sumOf { it.value }"""

replacement2 = """        var baseDefense = girlsInTeam.sumOf { (it.poslusnost / 15) + 2 + ((it.skills["defense"] ?: 0) * 2) }
        val defenseBuffs = current.activeBuffs.filter { it.type == "DEFENSE" }.sumOf { it.value }"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
