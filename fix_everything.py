import re

# Fix InteractiveDialogs imports
with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()
if "import com.example.haremdark.domain.GameEngine" not in text:
    text = text.replace("import com.example.haremdark.models.Player", "import com.example.haremdark.models.Player\nimport com.example.haremdark.domain.GameEngine")
with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)


# Fix GameEngine
with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

# startBossCombat replacement
import re
text = re.sub(
    r"fun startBossCombat\(boss: Boss\) \{[\s\S]*?log = listOf\(initialEntry\.message\),",
    """fun startBossCombat(boss: Boss, characterId: String? = null) {
        val current = _gameState.value
        val player = current.player
        
        var fighterName = "Pán Dominia"
        var fighterHp = player.hp
        var fighterMaxHp = player.maxHp
        
        if (characterId != null) {
            val char = current.characters.firstOrNull { it.id == characterId }
            if (char != null) {
                val hpBonus = char.equipment.values.filterNotNull().sumOf { it.hpBonus }
                fighterName = char.name
                fighterHp = char.hp
                fighterMaxHp = char.maxHp + hpBonus
            }
        }
        
        val initialEntry = CombatLogEntry(
            turn = 1,
            type = "system",
            message = "⚔️ $fighterName vstupuje do boje proti: ${boss.name} (${boss.phaseName})!"
        )
        
        _combatState.value = CombatSession(
            boss = boss,
            bossHp = boss.hp,
            bossMaxHp = boss.maxHp,
            playerHp = fighterHp,
            playerMaxHp = fighterMaxHp,
            deployedCharacterId = characterId,
            turnCount = 1,
            isDefending = false,
            enemyBleedTurns = 0,
            enemyStunned = false,
            activeBuff = null,
            logEntries = listOf(initialEntry),
            log = listOf(initialEntry.message),""",
    text
)

# executeCombatTurn part 1
text = re.sub(
    r"fun executeCombatTurn\(action: String, itemId: String\? = null\) \{[\s\S]*?val playerMultiplier = 1\.0f \+ \(0\.25f \* level5Count\)",
    """fun executeCombatTurn(action: String, itemId: String? = null) {
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
        val playerMultiplier = 1.0f + (0.25f * level5Count)""",
    text
)

# executeCombatTurn part 2 - Attacks
text = re.sub(
    r'"attack", "slash" -> \{[\s\S]*?"heavy_strike" -> \{[\s\S]*?\}\n            \}',
    """"attack", "slash" -> {
                val isCrit = Random.nextInt(100) < (15 + combatSkill * 2)
                val critMultiplier = if (isCrit) 1.65f else 1.0f
                val rawDmg = weaponDamage + combatSkill * 3 + Random.nextInt(-2, 5)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.35f)) * critMultiplier) * playerMultiplier).toInt().coerceAtLeast(6)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                val critText = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isCrit) "player_attack" else "player_attack",
                    message = "🗡️ Útok pomocí $weaponName udělil $finalDmg poškození!$critText"
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
            }""",
    text
)

# Fix Defense Skill usage in enemy turn (the previous one failed or succeeded, I need to ensure it uses defenseSkill)
text = re.sub(
    r'val defenseReduction = \(player\.skills\["obrana"\] \?: 0\) \* 2\n',
    r'val defenseReduction = defenseSkill * 2.5f\n',
    text
)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
