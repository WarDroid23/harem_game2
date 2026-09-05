with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

# 1. HP regen IN_LOVE
target_hp = """                } else if (copy.hp > 0) {
                    val bathLevel = current.buildings.firstOrNull { it.type == "lazne" }?.level ?: 0
                    copy.hp = (copy.hp + 10 + bathLevel * 5).coerceAtMost(copy.maxHp)
                }"""

replacement_hp = """                } else if (copy.hp > 0) {
                    val bathLevel = current.buildings.firstOrNull { it.type == "lazne" }?.level ?: 0
                    var healAmount = 10 + bathLevel * 5
                    if (rel == com.example.haremdark.models.RelStatus.IN_LOVE) healAmount += (copy.maxHp * rel.buffValue).toInt()
                    copy.hp = (copy.hp + healAmount).coerceAtMost(copy.maxHp)
                }"""

text = text.replace(target_hp, replacement_hp)

# 2. Increase bonding event chance if IN_LOVE
target_bond = """            // Random Bonding Event
            if (updatedCharacters.size >= 2 && Math.random() < 0.3) {"""

replacement_bond = """            // Random Bonding Event
            val inLoveCount = updatedCharacters.count { it.getRelationship() == com.example.haremdark.models.RelStatus.IN_LOVE }
            val bondingChance = 0.3 + (inLoveCount * 0.10)
            if (updatedCharacters.size >= 2 && Math.random() < bondingChance) {"""

text = text.replace(target_bond, replacement_bond)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
