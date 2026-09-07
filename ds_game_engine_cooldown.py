import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

# 1. Add cooldown check and tracking
old_process_player = """        // 1. Process Player Action
        val level5Count = currentGameState.characters.count { it.affinityLevel >= 5 }
        val playerMultiplier = 1.0f + (0.25f * level5Count)
        when (action) {"""

new_process_player = """        // Check cooldown
        if (session.skillCooldowns.getOrDefault(action, 0) > 0) {
            updateState { current ->
                val logs = (listOf("❌ Dovednost je ještě v cooldownu!") + current.gameLog).take(30)
                current.copy(gameLog = logs)
            }
            return
        }

        var newCooldowns = session.skillCooldowns.toMutableMap()
        if (action == "heavy_strike") newCooldowns["heavy_strike"] = 3
        if (action == "bleed_strike") newCooldowns["bleed_strike"] = 4
        if (action == "dark_burst") newCooldowns["dark_burst"] = 2
        if (action == "curse_shadow") newCooldowns["curse_shadow"] = 3
        if (action == "soul_drain") newCooldowns["soul_drain"] = 5
        if (action == "harem_support") newCooldowns["harem_support"] = 4

        // 1. Process Player Action
        val level5Count = currentGameState.characters.count { it.affinityLevel >= 5 }
        val playerMultiplier = 1.0f + (0.25f * level5Count)
        when (action) {
            "bleed_strike" -> {
                val dmg = weaponDamage + combatSkill * 2
                newBossHp = (newBossHp - dmg).coerceAtLeast(0)
                newBleedTurns = 3
                newLogEntries.add(0, CombatLogEntry(currentTurn, "player_special", "🩸 Krvavé bodnutí způsobilo $dmg poškození a nepřítel bude krvácet!"))
            }
            "dark_burst" -> {
                if (player.darkEnergy >= 10) {
                    player.darkEnergy -= 10
                    newPlayerDark = player.darkEnergy
                    val burstDmg = ((35 + (player.skills["temnota"] ?: 0) * 3) * playerMultiplier).toInt()
                    newBossHp = (newBossHp - burstDmg).coerceAtLeast(0)
                    newLogEntries.add(0, CombatLogEntry(currentTurn, "player_spell", "✨ Temný výboj prorazil obranu a udělil $burstDmg poškození!"))
                } else {
                    newLogEntries.add(0, CombatLogEntry(currentTurn, "system", "❌ Nemáš dostatek temné energie na Výboj (vyžaduje 10)!"))
                    newCooldowns["dark_burst"] = 0
                }
            }"""

text = text.replace(old_process_player, new_process_player)

# Decrease cooldowns at the end
old_copy = """            _combatState.value = session.copy(
                bossHp = newBossHp,
                playerHp = newPlayerHp,"""
new_copy = """            newCooldowns = newCooldowns.mapValues { it.value - 1 }.filterValues { it > 0 }.toMutableMap()
            _combatState.value = session.copy(
                bossHp = newBossHp,
                playerHp = newPlayerHp,
                skillCooldowns = newCooldowns,"""

text = text.replace(old_copy, new_copy)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)

