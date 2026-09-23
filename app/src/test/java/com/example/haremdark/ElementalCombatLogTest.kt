package com.example.haremdark

import com.example.haremdark.domain.PartyCombatManager
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalBattleSummary
import com.example.haremdark.models.ElementalDamageBreakdown
import com.example.haremdark.models.ElementalMatchupType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementalCombatLogTest {

    @Test
    fun testElementMultipliersAndMatchups() {
        // FIRE vs EARTH -> super effective (+25%)
        val fireVsEarth = PartyCombatManager.getElementMultiplier(Element.FIRE, Element.EARTH)
        assertEquals(1.25f, fireVsEarth, 0.001f)
        assertEquals(ElementalMatchupType.SUPER_EFFECTIVE, PartyCombatManager.getMatchupType(fireVsEarth))

        // FIRE vs WATER -> resisted (-25%)
        val fireVsWater = PartyCombatManager.getElementMultiplier(Element.FIRE, Element.WATER)
        assertEquals(0.75f, fireVsWater, 0.001f)
        assertEquals(ElementalMatchupType.RESISTED, PartyCombatManager.getMatchupType(fireVsWater))

        // DARK vs HOLY -> extreme weakness (+30%)
        val darkVsHoly = PartyCombatManager.getElementMultiplier(Element.DARK, Element.HOLY)
        assertEquals(1.30f, darkVsHoly, 0.001f)
        assertEquals(ElementalMatchupType.EXTREME_WEAKNESS, PartyCombatManager.getMatchupType(darkVsHoly))

        // PHYSICAL vs WATER -> neutral (1.0x)
        val physVsWater = PartyCombatManager.getElementMultiplier(Element.PHYSICAL, Element.WATER)
        assertEquals(1.0f, physVsWater, 0.001f)
        assertEquals(ElementalMatchupType.NEUTRAL, PartyCombatManager.getMatchupType(physVsWater))
    }

    @Test
    fun testElementalBattleSummaryAggregation() {
        val breakdown1 = ElementalDamageBreakdown(
            turn = 1,
            round = 1,
            attackerName = "Královna Sukub",
            attackerElement = Element.DARK,
            defenderName = "Inkvizitor",
            defenderElement = Element.HOLY,
            isPlayerPartyAttacker = true,
            actionName = "Temný šleh",
            basePower = 40,
            affinityMultiplier = 1.30f,
            elementMatchupMultiplier = 1.30f,
            matchupType = ElementalMatchupType.EXTREME_WEAKNESS,
            isCritical = true,
            critMultiplier = 1.65f,
            targetDefense = 10,
            defenseMitigation = 4,
            rawCalculatedDamage = 67,
            finalDamage = 65,
            affinityBonusDamageGained = 15,
            matchupBonusDamageGained = 15,
            formulaDisplay = "[Báze: 40] × [Afinita: 1.30x] × [Živel: 1.30x] - [Obrana: 4] = 65 DMG",
            tacticalNote = "Trénink afinity přidal +15 DMG"
        )

        val breakdown2 = ElementalDamageBreakdown(
            turn = 2,
            round = 1,
            attackerName = "Dračí Dívka",
            attackerElement = Element.FIRE,
            defenderName = "Vodní Elementál",
            defenderElement = Element.WATER,
            isPlayerPartyAttacker = true,
            actionName = "Plamenný dech",
            basePower = 30,
            affinityMultiplier = 1.15f,
            elementMatchupMultiplier = 0.75f,
            matchupType = ElementalMatchupType.RESISTED,
            isCritical = false,
            critMultiplier = 1.0f,
            targetDefense = 15,
            defenseMitigation = 6,
            rawCalculatedDamage = 25,
            finalDamage = 20,
            affinityBonusDamageGained = 3,
            matchupBonusDamageGained = -5,
            formulaDisplay = "[Báze: 30] × [Afinita: 1.15x] × [Živel: 0.75x] - [Obrana: 6] = 20 DMG",
            tacticalNote = "Odolnost vodního elementu"
        )

        val logs = listOf(
            CombatLogEntry(
                turn = 1,
                type = "player_special",
                message = "Zásah",
                actor = "Královna Sukub",
                damageDealt = 65,
                elementalBreakdown = breakdown1
            ),
            CombatLogEntry(
                turn = 2,
                type = "player_attack",
                message = "Zásah",
                actor = "Dračí Dívka",
                damageDealt = 20,
                elementalBreakdown = breakdown2
            )
        )

        val summary = ElementalBattleSummary.fromLogs(logs)

        assertEquals(2, summary.totalAttacksAnalyzed)
        assertEquals(85, summary.totalPartyDamage)
        assertEquals(18, summary.totalAffinityBonusDamageGained)
        assertEquals(1, summary.weaknessHitsTriggered)
        assertEquals(1, summary.resistedHitsEncountered)
        assertEquals(65, summary.highestElementalDamageHit)
        assertEquals("Královna Sukub", summary.bestAffinityContributor)
        assertEquals(1.30f, summary.highestAffinityMultiplierObserved, 0.001f)
    }
}
