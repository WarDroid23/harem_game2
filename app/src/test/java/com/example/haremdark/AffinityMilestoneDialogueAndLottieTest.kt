package com.example.haremdark

import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.Character
import com.example.haremdark.ui.components.MilestoneLottieJsonCatalog
import com.example.haremdark.ui.components.MilestoneLottieType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AffinityMilestoneDialogueAndLottieTest {

    @Test
    fun testMilestoneLottieTypeResolutionForEveryTier() {
        // Milestone 30: Sparkles
        val type30 = MilestoneLottieType.forThreshold(30)
        assertEquals(MilestoneLottieType.SPARKLES_30, type30)
        assertEquals(30, type30.milestoneThreshold)
        assertTrue(type30.effectName.contains("Třpytky"))

        // Milestone 70: Emerald Bloom
        val type70 = MilestoneLottieType.forThreshold(70)
        assertEquals(MilestoneLottieType.EMERALD_BLOOM_70, type70)
        assertTrue(type70.effectName.contains("Rozkvět") || type70.effectName.contains("Nefrit"))

        // Milestone 120: Sapphire Devotion
        val type120 = MilestoneLottieType.forThreshold(120)
        assertEquals(MilestoneLottieType.SAPPHIRE_DEVOTION_120, type120)
        assertTrue(type120.effectName.contains("Safírové") || type120.effectName.contains("Srdce"))

        // Milestone 180: Crimson Passion
        val type180 = MilestoneLottieType.forThreshold(180)
        assertEquals(MilestoneLottieType.CRIMSON_PASSION_180, type180)
        assertTrue(type180.effectName.contains("Karmínová") || type180.effectName.contains("Vášeň"))

        // Milestone 250: Amethyst Soulmate Vortex
        val type250 = MilestoneLottieType.forThreshold(250)
        assertEquals(MilestoneLottieType.AMETHYST_VORTEX_250, type250)
        assertTrue(type250.effectName.contains("Ametyst") || type250.effectName.contains("Vír"))

        // Milestone 350: Hellfire Inferno
        val type350 = MilestoneLottieType.forThreshold(350)
        assertEquals(MilestoneLottieType.INFERNO_FIRE_350, type350)
        assertTrue(type350.effectName.contains("Plamen") || type350.effectName.contains("Pekelný"))
    }

    @Test
    fun testAllLottieJsonSpecificationsAreValidJson() {
        MilestoneLottieType.values().forEach { type ->
            val jsonStr = MilestoneLottieJsonCatalog.getJson(type)
            assertNotNull("JSON string should not be null for $type", jsonStr)
            assertTrue("JSON string should not be empty for $type", jsonStr.isNotBlank())

            // Validate Lottie structure
            assertTrue(jsonStr.startsWith("{"))
            assertTrue(jsonStr.endsWith("}"))
            assertTrue(jsonStr.contains("\"v\": \"5.7.4\""))
            assertTrue(jsonStr.contains("\"layers\":"))
            assertTrue(jsonStr.contains("\"shapes\":"))
        }
    }

    @Test
    fun testMilestoneFlavorDialoguePoolsUnlocking() {
        val character = Character(
            id = "sukuba_test",
            name = "Lilith",
            archetypeId = "sukuba",
            affinityPoints = 15 // Tier 1 (below 30)
        )

        val unlockedTier1 = AffinityData.getAllUnlockedFlavorDialogues(character)
        assertTrue(unlockedTier1.isNotEmpty())

        // Cross 30 milestone (Tier 2)
        val char30 = character.copy(affinityPoints = 35)
        val unlocked30 = AffinityData.getAllUnlockedFlavorDialogues(char30)
        assertTrue(unlocked30.size >= unlockedTier1.size)

        // Cross 70 milestone (Tier 3)
        val char70 = character.copy(affinityPoints = 75)
        val unlocked70 = AffinityData.getAllUnlockedFlavorDialogues(char70)
        assertTrue(unlocked70.size >= unlocked30.size)

        // Cross 120 milestone (Tier 4)
        val char120 = character.copy(affinityPoints = 125)
        val unlocked120 = AffinityData.getAllUnlockedFlavorDialogues(char120)
        assertTrue(unlocked120.size >= unlocked70.size)

        // Cross 180 milestone (Tier 5)
        val char180 = character.copy(affinityPoints = 185)
        val unlocked180 = AffinityData.getAllUnlockedFlavorDialogues(char180)
        assertTrue(unlocked180.size >= unlocked120.size)

        // Cross 250 & 350 milestone (Tier 6)
        val char350 = character.copy(affinityPoints = 350)
        val unlocked350 = AffinityData.getAllUnlockedFlavorDialogues(char350)
        assertTrue(unlocked350.size >= unlocked180.size)

        // Celebratory line at 350
        val celebratoryLine350 = AffinityData.getCelebratoryMilestoneLine(char350, 350)
        assertNotNull(celebratoryLine350)
        assertTrue(celebratoryLine350.isNotBlank())
    }
}
