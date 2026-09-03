import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        var teamHp = girlsInTeam.sumOf { it.hp }
        val teamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 }"""

replacement = """        var teamHp = girlsInTeam.sumOf { it.hp }
        
        var baseTeamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 }
        val damageBuffs = current.activeBuffs.filter { it.type == "DAMAGE" }.sumOf { it.value }
        if (damageBuffs > 0) {
            baseTeamDmg = (baseTeamDmg * (1.0f + (damageBuffs / 100f))).toInt()
        }
        val teamDmg = baseTeamDmg"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
