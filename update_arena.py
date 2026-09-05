with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        var baseTeamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 }
        val damageBuffs = current.activeBuffs.filter { it.type == "DAMAGE" }.sumOf { it.value }
        if (damageBuffs > 0) {
            baseTeamDmg = (baseTeamDmg * (1.0f + (damageBuffs / 100f))).toInt()
        }
        val teamDmg = baseTeamDmg
        var baseDefense = girlsInTeam.sumOf { it.poslusnost / 15 + 2 }
        val defenseBuffs = current.activeBuffs.filter { it.type == "DEFENSE" }.sumOf { it.value }
        if (defenseBuffs > 0) {
            baseDefense += defenseBuffs
        }"""

replacement = """        var baseTeamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 }
        val damageBuffs = current.activeBuffs.filter { it.type == "DAMAGE" }.sumOf { it.value }
        if (damageBuffs > 0) {
            baseTeamDmg = (baseTeamDmg * (1.0f + (damageBuffs / 100f))).toInt()
        }
        
        var relationshipDmgMultiplier = 1.0f
        var relationshipDefMultiplier = 1.0f
        
        girlsInTeam.forEach { c ->
            val rel = c.getRelationship()
            if (rel == com.example.haremdark.models.RelStatus.BLOOD_SISTER) relationshipDmgMultiplier += rel.buffValue
            if (rel == com.example.haremdark.models.RelStatus.REBELLIOUS) relationshipDmgMultiplier += rel.buffValue
            if (rel == com.example.haremdark.models.RelStatus.BROKEN) relationshipDefMultiplier -= 0.10f
        }
        
        val teamDmg = (baseTeamDmg * relationshipDmgMultiplier).toInt()
        var baseDefense = girlsInTeam.sumOf { it.poslusnost / 15 + 2 }
        val defenseBuffs = current.activeBuffs.filter { it.type == "DEFENSE" }.sumOf { it.value }
        if (defenseBuffs > 0) {
            baseDefense += defenseBuffs
        }
        baseDefense = (baseDefense * relationshipDefMultiplier).toInt()"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
