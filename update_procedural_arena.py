import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        var currentWave = 1
        var eHp = 0
        var enemyDmg = 0

        while (teamHp > 0 && currentWave <= 10) {
            logs.add("--- Vlna $currentWave ---")
            eHp = 20 + (currentWave * 25)
            enemyDmg = 8 + (currentWave * 6)

            while (eHp > 0 && teamHp > 0) {
                // Team attacks
                val dmgDealt = (teamDmg + (0..6).random()).coerceAtLeast(1)
                eHp -= dmgDealt
                
                // Enemy attacks
                if (eHp > 0) {
                    val dmgTaken = (enemyDmg - teamDefense + (0..4).random()).coerceAtLeast(1)
                    teamHp -= dmgTaken
                }
            }"""

replacement = """        var currentWave = 1
        var eHp = 0
        var enemyDmg = 0
        var enemyName = ""

        val difficultyScale = 1.0f + (player.level * 0.15f) + (current.haremLevel * 0.1f) + (current.characters.size * 0.05f)
        
        val enemyTypes = listOf(
            Triple("Goblini otrokáři", 20, 8),
            Triple("Žoldnéři Cechu", 35, 12),
            Triple("Zbloudilá Inkvizice", 50, 18),
            Triple("Krvaví kultisté", 70, 14),
            Triple("Divocí vlkodlaci", 80, 22),
            Triple("Stínoví démoni", 100, 25)
        )
        val bossTypes = listOf(
            Triple("Velitel Inkvizice (Boss)", 250, 40),
            Triple("Golemský Ničitel (Boss)", 350, 25),
            Triple("Prastarý Upír (Boss)", 200, 50)
        )

        while (teamHp > 0 && currentWave <= 10) {
            val isBoss = (currentWave % 5 == 0) // Boss on wave 5 and 10
            
            val baseEnemy = if (isBoss) bossTypes.random() else enemyTypes.random()
            enemyName = baseEnemy.first
            
            // Procedural scaling: scales by difficulty factor + wave number
            eHp = ((baseEnemy.second + (currentWave * 20)) * difficultyScale).toInt()
            enemyDmg = ((baseEnemy.third + (currentWave * 4)) * difficultyScale).toInt()
            
            logs.add("--- Vlna $currentWave: $enemyName ---")
            logs.add("⚔️ Nepřítel: Zdraví $eHp, Útok $enemyDmg")

            while (eHp > 0 && teamHp > 0) {
                // Team attacks
                val dmgDealt = (teamDmg + (0..8).random()).coerceAtLeast(1)
                eHp -= dmgDealt
                
                // Enemy attacks
                if (eHp > 0) {
                    val dmgTaken = (enemyDmg - teamDefense + (0..5).random()).coerceAtLeast(1)
                    teamHp -= dmgTaken
                }
            }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
