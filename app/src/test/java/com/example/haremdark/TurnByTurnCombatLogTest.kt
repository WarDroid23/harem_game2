package com.example.haremdark

import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.CombatStatusEffect
import com.example.haremdark.models.Element
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurnByTurnCombatLogTest {

    @Test
    fun testCombatLogEntryCreationWithDamageAndStatusEffects() {
        val bleedEffect = CombatStatusEffect(
            id = "bleed_1",
            name = "Krvácení",
            icon = "🩸",
            type = "BLEED",
            value = 20,
            durationTurns = 2,
            maxDuration = 2,
            description = "Způsobuje zranění na začátku každého kola."
        )

        val entry = CombatLogEntry(
            turn = 1,
            type = "player_special",
            message = "🗡️ Vespera použila 'Krvavý zásek' na Kostlivce za 48 poškození!",
            actor = "Vespera",
            actionName = "Krvavý zásek",
            damageDealt = 48,
            targetName = "Kostlivec",
            isCritical = true,
            statusEffectsApplied = listOf(bleedEffect)
        )

        assertEquals(1, entry.turn)
        assertEquals(48, entry.damageDealt)
        assertEquals("Kostlivec", entry.targetName)
        assertTrue(entry.isCritical)
        assertEquals(1, entry.statusEffectsApplied.size)
        assertEquals("Krvácení", entry.statusEffectsApplied.first().name)
        assertTrue(entry.statusEffectsApplied.first().isDebuff)
        assertFalse(entry.statusEffectsApplied.first().isBuff)
    }

    @Test
    fun testCombatLogEntryBuffEffect() {
        val buffEffect = CombatStatusEffect(
            id = "buff_atk",
            name = "Totální zteč",
            icon = "⚔️",
            type = "ATK_BUFF",
            value = 30,
            durationTurns = 2,
            description = "+30% útočné poškození."
        )

        val entry = CombatLogEntry(
            turn = 2,
            type = "player_special",
            message = "⚔️ Pán zavelel k Totální zteči!",
            actor = "Pán Dominia",
            actionName = "Totální zteč",
            damageDealt = 0,
            targetName = "Celá družina",
            statusEffectsApplied = listOf(buffEffect)
        )

        assertEquals(2, entry.turn)
        assertEquals(0, entry.damageDealt)
        assertTrue(entry.statusEffectsApplied.first().isBuff)
        assertFalse(entry.statusEffectsApplied.first().isDebuff)
        assertEquals(30, entry.statusEffectsApplied.first().value)
    }

    @Test
    fun testTurnGroupingAndDamageAggregation() {
        val logs = listOf(
            CombatLogEntry(turn = 1, type = "player_attack", message = "Útok 1", damageDealt = 30),
            CombatLogEntry(turn = 1, type = "player_special", message = "Útok 2", damageDealt = 50),
            CombatLogEntry(turn = 1, type = "enemy_attack", message = "Útok nepřítele", damageDealt = 25),
            CombatLogEntry(turn = 2, type = "player_attack", message = "Útok 3", damageDealt = 40)
        )

        val grouped = logs.groupBy { it.turn }
        assertEquals(2, grouped.size)

        val turn1Logs = grouped[1]!!
        val turn1AllyDmg = turn1Logs.filter { it.type.startsWith("player") }.sumOf { it.damageDealt }
        val turn1EnemyDmg = turn1Logs.filter { it.type.startsWith("enemy") }.sumOf { it.damageDealt }

        assertEquals(80, turn1AllyDmg)
        assertEquals(25, turn1EnemyDmg)

        val turn2Logs = grouped[2]!!
        val turn2AllyDmg = turn2Logs.filter { it.type.startsWith("player") }.sumOf { it.damageDealt }
        assertEquals(40, turn2AllyDmg)
    }
}
