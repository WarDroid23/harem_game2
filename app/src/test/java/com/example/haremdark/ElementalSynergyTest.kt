package com.example.haremdark

import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalSynergyManager
import org.junit.Assert.*
import org.junit.Test

class ElementalSynergyTest {

    @Test
    fun testFireAndEarthSynergyActivation() {
        val pairs = listOf(
            "Ignis" to Element.FIRE,
            "Terra" to Element.EARTH
        )
        val synergies = ElementalSynergyManager.evaluateSynergiesFromPairs(pairs)
        assertTrue("Should activate Magmatický krunýř", synergies.any { it.id == "magmatic_bastion" })

        val magmatic = synergies.first { it.id == "magmatic_bastion" }
        assertTrue("Protects against Earth", magmatic.protectsAgainst(Element.EARTH))
        assertTrue("Protects against Fire", magmatic.protectsAgainst(Element.FIRE))
        assertTrue("Protects against Physical", magmatic.protectsAgainst(Element.PHYSICAL))
        assertEquals(0.22f, magmatic.damageResistancePercent, 0.001f)
    }

    @Test
    fun testWaterAndIceSynergyActivation() {
        val pairs = listOf(
            "Marina" to Element.WATER,
            "Kael" to Element.ICE
        )
        val synergies = ElementalSynergyManager.evaluateSynergiesFromPairs(pairs)
        assertTrue("Should activate Glaciální zrcadlo", synergies.any { it.id == "glacial_mirror" })

        val glacial = synergies.first { it.id == "glacial_mirror" }
        assertTrue("Protects against Water", glacial.protectsAgainst(Element.WATER))
        assertTrue("Protects against Ice", glacial.protectsAgainst(Element.ICE))
        assertTrue("Protects against Air", glacial.protectsAgainst(Element.AIR))
        assertEquals(0.25f, glacial.damageResistancePercent, 0.001f)
    }

    @Test
    fun testDarkAndHolySynergyActivation() {
        val pairs = listOf(
            "Lilith" to Element.DARK,
            "Elena" to Element.HOLY
        )
        val synergies = ElementalSynergyManager.evaluateSynergiesFromPairs(pairs)
        assertTrue("Should activate Eclipsní rovnováha", synergies.any { it.id == "twilight_eclipse" })

        val eclipse = synergies.first { it.id == "twilight_eclipse" }
        assertTrue("Protects against Dark", eclipse.protectsAgainst(Element.DARK))
        assertTrue("Protects against Holy", eclipse.protectsAgainst(Element.HOLY))
        assertTrue("Has all element resistance", eclipse.allElementResistancePercent > 0f)
    }

    @Test
    fun testPantheonHarmonyWithFourDistinctElements() {
        val pairs = listOf(
            "Draci" to Element.FIRE,
            "Subka" to Element.WATER,
            "Chladna" to Element.ICE,
            "Vzdorna" to Element.EARTH
        )
        val synergies = ElementalSynergyManager.evaluateSynergiesFromPairs(pairs)
        assertTrue("Should activate Panteon Živlů for 4+ distinct elements", synergies.any { it.id == "elemental_pantheon" })

        val pantheon = synergies.first { it.id == "elemental_pantheon" }
        assertEquals(0.18f, pantheon.damageResistancePercent, 0.001f)
    }

    @Test
    fun testDamageMitigationCalculation() {
        val pairs = listOf(
            "Ignis" to Element.FIRE,
            "Terra" to Element.EARTH
        )
        val synergies = ElementalSynergyManager.evaluateSynergiesFromPairs(pairs)

        // Incoming 100 Earth damage should be mitigated by Magmatic Bastion (-22% + 3% = 25%)
        val result = ElementalSynergyManager.calculateDamageMitigation(
            incomingDamage = 100,
            attackerElement = Element.EARTH,
            activeSynergies = synergies
        )

        assertTrue("Mitigation ratio should be around 25%", result.totalMitigationRatio >= 0.22f)
        assertEquals(25, result.mitigatedDamageAmount)
        assertEquals(75, result.finalDamageAfterSynergy)
        assertTrue("Contributing synergies should contain Magmatický krunýř", result.contributingSynergies.any { it.id == "magmatic_bastion" })
    }

    @Test
    fun testDamageMitigationCapsSafely() {
        val allSynergies = ElementalSynergyManager.DEFINED_SYNERGIES
        val result = ElementalSynergyManager.calculateDamageMitigation(
            incomingDamage = 1000,
            attackerElement = Element.FIRE,
            activeSynergies = allSynergies
        )

        assertTrue("Mitigation ratio should be capped at 50%", result.totalMitigationRatio <= 0.50f)
        assertTrue("Final damage must be >= 1", result.finalDamageAfterSynergy >= 1)
    }
}
