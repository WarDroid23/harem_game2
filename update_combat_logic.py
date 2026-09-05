import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """    fun executeCombatTurn(action: String, itemId: String? = null) {
        val session = _combatState.value ?: return
        if (session.isOver) return

        val player = _gameState.value.player
        val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull() ?: Weapon("Pěsti temnoty", "kratka", 10, 0)
        
        var newBossHp = session.bossHp
        var newPlayerHp = session.playerHp
        var newPlayerDark = player.darkEnergy
        
        var newBleedTurns = session.enemyBleedTurns
        var newStunned = session.enemyStunned
        var isDefending = false
        var activeBuff = session.activeBuff
        
        val newLogEntries = session.logEntries.toMutableList()
        val currentTurn = session.turnCount
        
        var isOver = false
        var victory = false
        var lootInfo: String? = null
        
        // 1. Process Player Action
        val level5Count = _gameState.value.characters.count { it.affinityLevel >= 5 }
        val playerMultiplier = 1.0f + (0.25f * level5Count)"""

replacement = """    fun executeCombatTurn(action: String, itemId: String? = null) {
        val session = _combatState.value ?: return
        if (session.isOver) return

        val currentGameState = _gameState.value
        val player = currentGameState.player
        
        var weaponDamage = 10
        var weaponName = "Holé pěsti"
        var combatSkill = player.skills["boj"] ?: 0
        var defenseSkill = player.skills["obrana"] ?: 0
        
        if (session.deployedCharacterId != null) {
            val char = currentGameState.characters.firstOrNull { it.id == session.deployedCharacterId }
            if (char != null) {
                combatSkill = char.skills["combat"] ?: 0
                defenseSkill = char.skills["defense"] ?: 0
                
                val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                val defBonus = char.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                
                combatSkill += combatBonus
                defenseSkill += defBonus
                
                val eqWeapon = char.equipment["weapon"]
                if (eqWeapon != null) {
                    weaponDamage = eqWeapon.combatBonus
                    weaponName = eqWeapon.name
                }
            }
        } else {
            val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull() ?: Weapon("Pěsti temnoty", "kratka", 10, 0)
            weaponDamage = weapon.damage
            weaponName = weapon.name
        }
        
        var newBossHp = session.bossHp
        var newPlayerHp = session.playerHp
        var newPlayerDark = player.darkEnergy
        
        var newBleedTurns = session.enemyBleedTurns
        var newStunned = session.enemyStunned
        var isDefending = false
        var activeBuff = session.activeBuff
        
        val newLogEntries = session.logEntries.toMutableList()
        val currentTurn = session.turnCount
        
        var isOver = false
        var victory = false
        var lootInfo: String? = null
        
        // 1. Process Player Action
        val level5Count = currentGameState.characters.count { it.affinityLevel >= 5 }
        val playerMultiplier = 1.0f + (0.25f * level5Count)"""

text = text.replace(target, replacement)


target2 = """            "attack", "slash" -> {
                val isCrit = Random.nextInt(100) < (15 + (player.skills["boj"] ?: 0) * 2)
                val critMultiplier = if (isCrit) 1.65f else 1.0f
                val rawDmg = weapon.damage + (player.skills["boj"] ?: 0) * 3 + Random.nextInt(-2, 5)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.35f)) * critMultiplier) * playerMultiplier).toInt().coerceAtLeast(6)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                val critText = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isCrit) "player_attack" else "player_attack",
                    message = "🗡️ Sek zbraní ${weapon.name} udělil $finalDmg poškození!$critText"
                ))
            }
            "heavy_strike" -> {
                val isCrit = Random.nextInt(100) < 25
                val multiplier = if (isCrit) 2.2f else 1.5f
                val rawDmg = (weapon.damage * 1.5f) + (player.skills["boj"] ?: 0) * 4 + Random.nextInt(2, 10)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.25f)) * multiplier) * playerMultiplier).toInt().coerceAtLeast(12)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_special",
                    message = "⚔️ Těžký útok ubral ${finalDmg} HP!"
                ))
            }"""

replacement2 = """            "attack", "slash" -> {
                val isCrit = Random.nextInt(100) < (15 + combatSkill * 2)
                val critMultiplier = if (isCrit) 1.65f else 1.0f
                val rawDmg = weaponDamage + combatSkill * 3 + Random.nextInt(-2, 5)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.35f)) * critMultiplier) * playerMultiplier).toInt().coerceAtLeast(6)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                val critText = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isCrit) "player_attack" else "player_attack",
                    message = "🗡️ Útok pomocí ${weaponName} udělil $finalDmg poškození!$critText"
                ))
            }
            "heavy_strike" -> {
                val isCrit = Random.nextInt(100) < 25
                val multiplier = if (isCrit) 2.2f else 1.5f
                val rawDmg = (weaponDamage * 1.5f) + combatSkill * 4 + Random.nextInt(2, 10)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.25f)) * multiplier) * playerMultiplier).toInt().coerceAtLeast(12)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_special",
                    message = "⚔️ Těžký útok ubral ${finalDmg} HP!"
                ))
            }"""

text = text.replace(target2, replacement2)


target3 = """        if (newBossHp > 0 && !isOver) {
            if (newStunned) {
                newLogEntries.add(0, CombatLogEntry(currentTurn, "boss_stunned", "🛑 Boss je omráčený a nemůže útočit!"))
                newStunned = false
            } else {
                val rawBossDmg = session.boss.attack + Random.nextInt(-3, 8)
                var defenseReduction = (player.skills["obrana"] ?: 0) * 2.5f"""

replacement3 = """        if (newBossHp > 0 && !isOver) {
            if (newStunned) {
                newLogEntries.add(0, CombatLogEntry(currentTurn, "boss_stunned", "🛑 Nepřítel je omráčený a nemůže útočit!"))
                newStunned = false
            } else {
                val rawBossDmg = session.boss.attack + Random.nextInt(-3, 8)
                var defenseReduction = defenseSkill * 2.5f"""
text = text.replace(target3, replacement3)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
