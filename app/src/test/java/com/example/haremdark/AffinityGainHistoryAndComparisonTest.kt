package com.example.haremdark

import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.AffinityPointRecord
import com.example.haremdark.models.Character
import com.example.haremdark.models.addAffinityHistory
import com.example.haremdark.models.getSafeAffinityTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AffinityGainHistoryAndComparisonTest {

    @Test
    fun testAffinityPointRecordEffectiveGainAndDate() {
        val record1 = AffinityPointRecord(
            day = 3,
            points = 45,
            source = "Dar: Hedvábný šátek (+15 pts)",
            amountIncreased = 15,
            dateText = "Den 3"
        )
        assertEquals(15, record1.effectiveGain)
        assertEquals("Den 3", record1.displayDate)

        // Fallback extraction when amountIncreased is 0
        val record2 = AffinityPointRecord(
            day = 5,
            points = 60,
            source = "Královská přízeň (+15 pts)"
        )
        assertEquals(15, record2.effectiveGain)
        assertEquals("Den 5", record2.displayDate)
    }

    @Test
    fun testAddAffinityHistoryTracking() {
        val character = Character(
            id = "test_char",
            name = "Aria",
            archetypeId = "subka",
            affinityPoints = 10
        )

        // Add first interaction
        character.affinityPoints = 25
        character.addAffinityHistory(day = 2, source = "Důvěrný rozhovor", amountIncreased = 15)

        assertEquals(1, character.affinityHistory.size)
        val first = character.affinityHistory.first()
        assertEquals(2, first.day)
        assertEquals(25, first.points)
        assertEquals(15, first.amountIncreased)
        assertEquals("Den 2", first.dateText)

        // Add second interaction with auto-calculated gain
        character.affinityPoints = 40
        character.addAffinityHistory(day = 4, source = "Vzácný dárek")

        assertEquals(2, character.affinityHistory.size)
        val second = character.affinityHistory[1]
        assertEquals(4, second.day)
        assertEquals(40, second.points)
        assertEquals(15, second.amountIncreased)
    }

    @Test
    fun testSafeAffinityTrendGeneratesPopulatedHistory() {
        val character = Character(
            id = "test_trend",
            name = "Vespera",
            archetypeId = "sukuba",
            affinityPoints = 150
        )

        val trend = character.getSafeAffinityTrend(currentDay = 10)
        assertTrue("Trend should contain multiple entries", trend.size >= 4)

        trend.forEach { record ->
            assertTrue("Points should be positive", record.points > 0)
            assertTrue("Gain should be recorded", record.amountIncreased > 0 || record.effectiveGain > 0)
            assertTrue("Date text should be populated", record.displayDate.isNotBlank())
        }

        // Final point must match character's current affinity
        assertEquals(150, trend.last().points)
    }

    @Test
    fun testSideBySideMilestonesComparisonBetweenTwoCharacters() {
        // Character A: Higher affinity (crosses through 180 pts)
        val charA = Character(
            id = "char_a",
            name = "Katarina",
            archetypeId = "slechticna",
            affinityPoints = 200
        )

        // Character B: Lower affinity (crosses through 70 pts)
        val charB = Character(
            id = "char_b",
            name = "Morrigan",
            archetypeId = "carodejka",
            affinityPoints = 85
        )

        val milestones = AffinityData.AFFINITY_MILESTONES // [30, 70, 120, 180, 250, 350]

        // Check milestone 30: both unlocked
        assertTrue(charA.affinityPoints >= 30)
        assertTrue(charB.affinityPoints >= 30)
        val quoteA30 = AffinityData.getCelebratoryMilestoneLine(charA, 30)
        val quoteB30 = AffinityData.getCelebratoryMilestoneLine(charB, 30)
        assertNotNull(quoteA30)
        assertNotNull(quoteB30)

        // Check milestone 120: char A unlocked, char B locked
        assertTrue(charA.affinityPoints >= 120)
        assertFalse(charB.affinityPoints >= 120)

        // Check milestone 180: char A unlocked, char B locked
        assertTrue(charA.affinityPoints >= 180)
        assertFalse(charB.affinityPoints >= 180)

        // Check milestone 250: both locked
        assertFalse(charA.affinityPoints >= 250)
        assertFalse(charB.affinityPoints >= 250)

        // Count unlocked
        val countA = milestones.count { charA.affinityPoints >= it }
        val countB = milestones.count { charB.affinityPoints >= it }
        assertEquals(4, countA) // 30, 70, 120, 180
        assertEquals(2, countB) // 30, 70
    }
}
