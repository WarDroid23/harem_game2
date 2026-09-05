import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """    fun startBossCombat(boss: Boss) {
        val player = _gameState.value.player
        val initialEntry = CombatLogEntry(
            turn = 1,
            type = "system",
            message = "⚔️ Vstoupil jsi do arény proti: ${boss.name} (${boss.phaseName})!"
        )
        
        _combatState.value = CombatSession(
            boss = boss,
            bossHp = boss.hp,
            bossMaxHp = boss.maxHp,
            playerHp = player.hp,
            playerMaxHp = player.maxHp,
            turnCount = 1,"""

replacement = """    fun startBossCombat(boss: Boss, characterId: String? = null) {
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
            turnCount = 1,"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
