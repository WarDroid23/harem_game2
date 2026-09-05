with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target1 = """            // Add Relationship Buffs
            var relResMultiplier = 0.0f
            current.characters.forEach { c ->
                val rel = c.getRelationship()
                if (rel == com.example.haremdark.models.RelStatus.DEVOTED) relResMultiplier += rel.buffValue
                if (rel == com.example.haremdark.models.RelStatus.OBEDIENT) relResMultiplier += rel.buffValue
            }
            globalIncomeMultiplier += relResMultiplier"""

replacement1 = """            // Add Relationship Buffs
            var relResMultiplier = 0.0f
            var skillResMultiplier = 0.0f
            current.characters.forEach { c ->
                val rel = c.getRelationship()
                if (rel == com.example.haremdark.models.RelStatus.DEVOTED) relResMultiplier += rel.buffValue
                if (rel == com.example.haremdark.models.RelStatus.OBEDIENT) relResMultiplier += rel.buffValue
                skillResMultiplier += (c.skills["production"] ?: 0) * 0.02f
            }
            globalIncomeMultiplier += relResMultiplier + skillResMultiplier"""

text = text.replace(target1, replacement1)

target2 = """                    if (rel == com.example.haremdark.models.RelStatus.BROKEN) {
                        dailyIncome = (dailyIncome * (1.0f + rel.buffValue)).toInt()
                    }"""

replacement2 = """                    if (rel == com.example.haremdark.models.RelStatus.BROKEN) {
                        dailyIncome = (dailyIncome * (1.0f + rel.buffValue)).toInt()
                    }
                    dailyIncome += (copy.skills["rental"] ?: 0) * 15"""

text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
