with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        // Apply damage to individual girls
        val damagePercentage = if (teamHp <= 0) 1.0f else 1.0f - (teamHp.toFloat() / girlsInTeam.sumOf { it.maxHp })
        
        updateState { state ->
            val updatedGirls = state.characters.map { girl ->
                if (girlIds.contains(girl.id)) {
                    val individualDmg = (girl.maxHp * damagePercentage).toInt()
                    girl.copy(hp = (girl.hp - individualDmg).coerceAtLeast(1))
                } else girl
            }"""

replacement = """        // Apply damage to individual girls
        val damagePercentage = if (teamHp <= 0) 1.0f else 1.0f - (teamHp.toFloat() / girlsInTeam.sumOf { it.maxHp })
        
        val xpGain = (currentWave - 1) * 20 + 10
        logs.add("🌟 Každá přeživší dívka v týmu získala +$xpGain ZK!")
        
        updateState { state ->
            val updatedGirls = state.characters.map { girl ->
                if (girlIds.contains(girl.id)) {
                    val individualDmg = (girl.maxHp * damagePercentage).toInt()
                    var newXp = girl.xp + xpGain
                    var newLevel = girl.level
                    var newSp = girl.skillPoints
                    
                    while (newXp >= newLevel * 100) {
                        newXp -= newLevel * 100
                        newLevel++
                        newSp++
                        logs.add("✨ ${girl.name} dosáhla úrovně $newLevel a získala 1 Dovednostní bod!")
                    }
                    
                    girl.copy(
                        hp = (girl.hp - individualDmg).coerceAtLeast(1),
                        xp = newXp,
                        level = newLevel,
                        skillPoints = newSp
                    )
                } else girl
            }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
