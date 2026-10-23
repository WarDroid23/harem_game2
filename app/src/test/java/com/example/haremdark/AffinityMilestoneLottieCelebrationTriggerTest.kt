package com.example.haremdark

import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.Character
import com.example.haremdark.ui.components.MilestoneLottieJsonCatalog
import com.example.haremdark.ui.components.MilestoneLottieType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AffinityMilestoneLottieCelebrationTriggerTest {

    @Test
    fun testMilestoneLottieTypeForThresholds() {
        assertEquals(MilestoneLottieType.SPARKLES_30, MilestoneLottieType.forThreshold(30))
        assertEquals(MilestoneLottieType.SPARKLES_30, MilestoneLottieType.forThreshold(50))
        assertEquals(MilestoneLottieType.EMERALD_BLOOM_70, MilestoneLottieType.forThreshold(70))
        assertEquals(MilestoneLottieType.EMERALD_BLOOM_70, MilestoneLottieType.forThreshold(100))
        assertEquals(MilestoneLottieType.SAPPHIRE_DEVOTION_120, MilestoneLottieType.forThreshold(120))
        assertEquals(MilestoneLottieType.SAPPHIRE_DEVOTION_120, MilestoneLottieType.forThreshold(150))
        assertEquals(MilestoneLottieType.CRIMSON_PASSION_180, MilestoneLottieType.forThreshold(180))
        assertEquals(MilestoneLottieType.AMETHYST_VORTEX_250, MilestoneLottieType.forThreshold(250))
        assertEquals(MilestoneLottieType.INFERNO_FIRE_350, MilestoneLottieType.forThreshold(350))
        assertEquals(MilestoneLottieType.INFERNO_FIRE_350, MilestoneLottieType.forThreshold(500))
    }

    @Test
    fun testMilestoneLottieJsonCatalogProducesValidSpecs() {
        for (type in MilestoneLottieType.values()) {
            val json = MilestoneLottieJsonCatalog.getJson(type)
            assertNotNull(json)
            assertTrue("JSON should not be blank for ${type.name}", json.isNotBlank())
            assertTrue("Should contain Lottie version field", json.contains("\"v\":"))
            assertTrue("Should contain layers array", json.contains("\"layers\":"))
            assertTrue("Should contain frame rate", json.contains("\"fr\":"))
        }
    }

    @Test
    fun testCrossingThresholdDetection() {
        val milestones = AffinityData.AFFINITY_MILESTONES // [30, 70, 120, 180, 250, 350]

        // Function mirroring the progress bar's crossing detection logic
        fun detectCrossedMilestone(oldPoints: Int, newPoints: Int, lastAck: Int): Int? {
            return milestones.lastOrNull { threshold ->
                (oldPoints < threshold && newPoints >= threshold) ||
                (newPoints >= threshold && threshold > lastAck)
            }
        }

        // Case 1: Point increase from 20 to 25 -> no milestone crossed
        val noCrossing = detectCrossedMilestone(20, 25, 0)
        assertNull(noCrossing)

        // Case 2: Crossing milestone 30
        val cross30 = detectCrossedMilestone(25, 30, 0)
        assertEquals(30, cross30)

        // Case 3: Crossing milestone 70
        val cross70 = detectCrossedMilestone(65, 75, 30)
        assertEquals(70, cross70)

        // Case 4: Crossing milestone 120
        val cross120 = detectCrossedMilestone(115, 120, 70)
        assertEquals(120, cross120)

        // Case 5: Large leap crossing multiple milestones (e.g. 25 to 135)
        // should detect the highest crossed milestone (120)
        val leapCross = detectCrossedMilestone(25, 135, 0)
        assertEquals(120, leapCross)

        // Case 6: Already acknowledged milestone 120, moving from 125 to 130 -> no new crossing
        val noReTrigger = detectCrossedMilestone(125, 130, 120)
        assertNull(noReTrigger)

        // Case 7: Reaching max milestone 350
        val cross350 = detectCrossedMilestone(340, 350, 250)
        assertEquals(350, cross350)
    }

    @Test
    fun testCharacterAcknowledgmentUpdates() {
        val character = Character(
            id = "char_test",
            name = "Aria",
            archetypeId = "subka",
            affinityPoints = 25,
            lastAcknowledgedAffinityMilestone = 0
        )

        // Simulate gaining affinity across milestone 30
        val newPoints = 35
        val oldPoints = character.affinityPoints
        val crossed = AffinityData.AFFINITY_MILESTONES.lastOrNull { threshold ->
            (oldPoints < threshold && newPoints >= threshold) ||
            (newPoints >= threshold && threshold > character.lastAcknowledgedAffinityMilestone)
        }

        assertNotNull(crossed)
        assertEquals(30, crossed)

        // Update character acknowledgment
        character.lastAcknowledgedAffinityMilestone = crossed!!
        character.affinityPoints = newPoints

        assertEquals(30, character.lastAcknowledgedAffinityMilestone)

        // Next small update should not trigger again
        val nextPoints = 40
        val nextCrossed = AffinityData.AFFINITY_MILESTONES.lastOrNull { threshold ->
            (character.affinityPoints < threshold && nextPoints >= threshold) ||
            (nextPoints >= threshold && threshold > character.lastAcknowledgedAffinityMilestone)
        }
        assertNull(nextCrossed)
    }
}
