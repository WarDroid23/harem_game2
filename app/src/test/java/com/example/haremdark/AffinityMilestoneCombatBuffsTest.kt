package com.example.haremdark

import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.Character
import com.example.haremdark.ui.components.AffinityMilestoneCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AffinityMilestoneCombatBuffsTest {

    @Test
    fun testMilestoneUnlocksAtDifferentAffinityLevels() {
        // Character at 15 points (Tier 1: 0-30 pts)
        val charTier1 = Character(
            id = "char_1",
            name = "Vespera",
            archetypeId = "sukuba",
            affinityPoints = 15
        )

        val milestonesTier1 = AffinityMilestoneCalculator.getMilestonesForCharacter(charTier1)
        assertEquals(6, milestonesTier1.size)
        assertTrue(milestonesTier1[0].isUnlocked) // Tier 1 (0 pts)
        assertFalse(milestonesTier1[1].isUnlocked) // Tier 2 (31 pts)
        assertFalse(milestonesTier1[2].isUnlocked) // Tier 3 (71 pts)
        assertEquals(16, milestonesTier1[1].pointsNeededToUnlock) // 31 - 15 = 16

        // Character at 150 points (Tier 4: 121-180 pts)
        val charTier4 = Character(
            id = "char_2",
            name = "Vespera",
            archetypeId = "sukuba",
            affinityPoints = 150
        )

        val milestonesTier4 = AffinityMilestoneCalculator.getMilestonesForCharacter(charTier4)
        assertTrue(milestonesTier4[0].isUnlocked) // Tier 1
        assertTrue(milestonesTier4[1].isUnlocked) // Tier 2
        assertTrue(milestonesTier4[2].isUnlocked) // Tier 3
        assertTrue(milestonesTier4[3].isUnlocked) // Tier 4
        assertFalse(milestonesTier4[4].isUnlocked) // Tier 5 (181 pts)
        assertEquals(31, milestonesTier4[4].pointsNeededToUnlock) // 181 - 150 = 31
    }

    @Test
    fun testCumulativePassiveBonusesCalculation() {
        val dragonGirl = Character(
            id = "char_dragon",
            name = "Ignis",
            archetypeId = "draci_divka",
            affinityPoints = 85 // Tier 3 (71-120 pts)
        )

        val cumulative = AffinityMilestoneCalculator.getCumulativePassiveBonuses(dragonGirl)

        // Tier 3 tier bonus: hp=25, atk=8%, def=4, crit=10%
        // draci_divka Tier 3 buff: Dračí pancíř (hp=40, def=18, specialEffectTag=ELEMENT_RESIST)
        assertEquals(65, cumulative.totalHp) // 25 + 40
        assertEquals(8, cumulative.totalAttackPercent) // 8% + 0%
        assertEquals(22, cumulative.totalDefense) // 4 + 18
        assertEquals(10, cumulative.totalCritPercent) // 10%
        assertEquals("Dračí pancíř", cumulative.activeBuffName)
        assertEquals("ELEMENT_RESIST", cumulative.specialEffectTag)
    }

    @Test
    fun testArchetypeSpecificUniqueBuffs() {
        val assassin = Character(
            id = "char_assassin",
            name = "Kage",
            archetypeId = "ticha_panenka",
            affinityPoints = 200 // Tier 5
        )

        val milestones = AffinityMilestoneCalculator.getMilestonesForCharacter(assassin)
        val tier5Milestone = milestones[4]

        assertTrue(tier5Milestone.isUnlocked)
        assertEquals("Smrtící balet", tier5Milestone.characterSpecificBuff.name)
        assertEquals("ARMOR_PIERCE", tier5Milestone.characterSpecificBuff.specialEffectTag)
        assertEquals(20, tier5Milestone.characterSpecificBuff.critBonusPercent)
    }

    @Test
    fun testMaxMilestoneLevelUnlocked() {
        val sovereign = Character(
            id = "char_queen",
            name = "Aurelia",
            archetypeId = "slechticna",
            affinityPoints = 300 // Tier 6 (251+ pts)
        )

        val milestones = AffinityMilestoneCalculator.getMilestonesForCharacter(sovereign)
        assertTrue(milestones.all { it.isUnlocked })

        val cumulative = AffinityMilestoneCalculator.getCumulativePassiveBonuses(sovereign)
        assertEquals("Vládkyně krvavého trůnu", cumulative.activeBuffName)
        assertEquals("SUPREME_COMMAND", cumulative.specialEffectTag)
        assertTrue(cumulative.totalAttackPercent >= 80) // 40% tier + 45% buff
    }
}
