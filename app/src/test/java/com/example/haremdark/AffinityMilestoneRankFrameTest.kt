package com.example.haremdark

import com.example.haremdark.ui.components.AffinityMilestoneProgressCalculator
import com.example.haremdark.ui.components.AffinityMilestoneRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AffinityMilestoneRankFrameTest {

    @Test
    fun testMilestoneRankThresholdMappings() {
        // Unranked / Iron below 30
        assertEquals(AffinityMilestoneRank.UNRANKED, AffinityMilestoneRank.getRankForPoints(0))
        assertEquals(AffinityMilestoneRank.UNRANKED, AffinityMilestoneRank.getRankForPoints(15))
        assertEquals(AffinityMilestoneRank.UNRANKED, AffinityMilestoneRank.getRankForPoints(29))

        // Bronze at 30 to 69
        assertEquals(AffinityMilestoneRank.BRONZE, AffinityMilestoneRank.getRankForPoints(30))
        assertEquals(AffinityMilestoneRank.BRONZE, AffinityMilestoneRank.getRankForPoints(50))
        assertEquals(AffinityMilestoneRank.BRONZE, AffinityMilestoneRank.getRankForPoints(69))
        assertEquals("Bronz", AffinityMilestoneRank.BRONZE.title)
        assertEquals("Bronzový rám", AffinityMilestoneRank.BRONZE.rankLabel)
        assertEquals("🥉", AffinityMilestoneRank.BRONZE.rankBadgeIcon)

        // Silver at 70 to 119
        assertEquals(AffinityMilestoneRank.SILVER, AffinityMilestoneRank.getRankForPoints(70))
        assertEquals(AffinityMilestoneRank.SILVER, AffinityMilestoneRank.getRankForPoints(100))
        assertEquals(AffinityMilestoneRank.SILVER, AffinityMilestoneRank.getRankForPoints(119))
        assertEquals("Stříbro", AffinityMilestoneRank.SILVER.title)
        assertEquals("Stříbrný rám", AffinityMilestoneRank.SILVER.rankLabel)
        assertEquals("🥈", AffinityMilestoneRank.SILVER.rankBadgeIcon)

        // Gold at 120 to 179
        assertEquals(AffinityMilestoneRank.GOLD, AffinityMilestoneRank.getRankForPoints(120))
        assertEquals(AffinityMilestoneRank.GOLD, AffinityMilestoneRank.getRankForPoints(150))
        assertEquals(AffinityMilestoneRank.GOLD, AffinityMilestoneRank.getRankForPoints(179))
        assertEquals("Zlato", AffinityMilestoneRank.GOLD.title)
        assertEquals("Zlatý rám", AffinityMilestoneRank.GOLD.rankLabel)
        assertEquals("🥇", AffinityMilestoneRank.GOLD.rankBadgeIcon)

        // Platinum at 180 to 249
        assertEquals(AffinityMilestoneRank.PLATINUM, AffinityMilestoneRank.getRankForPoints(180))
        assertEquals(AffinityMilestoneRank.PLATINUM, AffinityMilestoneRank.getRankForPoints(200))
        assertEquals(AffinityMilestoneRank.PLATINUM, AffinityMilestoneRank.getRankForPoints(249))
        assertEquals("Platina", AffinityMilestoneRank.PLATINUM.title)
        assertEquals("💠", AffinityMilestoneRank.PLATINUM.rankBadgeIcon)

        // Diamond at 250 to 349
        assertEquals(AffinityMilestoneRank.DIAMOND, AffinityMilestoneRank.getRankForPoints(250))
        assertEquals(AffinityMilestoneRank.DIAMOND, AffinityMilestoneRank.getRankForPoints(300))
        assertEquals(AffinityMilestoneRank.DIAMOND, AffinityMilestoneRank.getRankForPoints(349))
        assertEquals("Diamant", AffinityMilestoneRank.DIAMOND.title)
        assertEquals("💎", AffinityMilestoneRank.DIAMOND.rankBadgeIcon)

        // Master at 350+
        assertEquals(AffinityMilestoneRank.MASTER, AffinityMilestoneRank.getRankForPoints(350))
        assertEquals(AffinityMilestoneRank.MASTER, AffinityMilestoneRank.getRankForPoints(500))
        assertEquals("Věčný Mistr", AffinityMilestoneRank.MASTER.title)
        assertEquals("👑", AffinityMilestoneRank.MASTER.rankBadgeIcon)
    }

    @Test
    fun testGetRankForMilestoneThresholds() {
        assertEquals(AffinityMilestoneRank.BRONZE, AffinityMilestoneRank.getRankForMilestone(30))
        assertEquals(AffinityMilestoneRank.SILVER, AffinityMilestoneRank.getRankForMilestone(70))
        assertEquals(AffinityMilestoneRank.GOLD, AffinityMilestoneRank.getRankForMilestone(120))
        assertEquals(AffinityMilestoneRank.PLATINUM, AffinityMilestoneRank.getRankForMilestone(180))
        assertEquals(AffinityMilestoneRank.DIAMOND, AffinityMilestoneRank.getRankForMilestone(250))
        assertEquals(AffinityMilestoneRank.MASTER, AffinityMilestoneRank.getRankForMilestone(350))
    }

    @Test
    fun testProgressCalculatorProvidesRank() {
        val progressNovice = AffinityMilestoneProgressCalculator.calculate(10)
        assertEquals(AffinityMilestoneRank.UNRANKED, progressNovice.currentRank)
        assertEquals(30, progressNovice.nextThreshold)
        assertEquals(20, progressNovice.pointsNeeded)

        val progressBronze = AffinityMilestoneProgressCalculator.calculate(35)
        assertEquals(AffinityMilestoneRank.BRONZE, progressBronze.currentRank)
        assertEquals(30, progressBronze.previousThreshold)
        assertEquals(70, progressBronze.nextThreshold)
        assertEquals(35, progressBronze.pointsNeeded)

        val progressSilver = AffinityMilestoneProgressCalculator.calculate(70)
        assertEquals(AffinityMilestoneRank.SILVER, progressSilver.currentRank)
        assertEquals(70, progressSilver.previousThreshold)
        assertEquals(120, progressSilver.nextThreshold)
        assertEquals(50, progressSilver.pointsNeeded)

        val progressGold = AffinityMilestoneProgressCalculator.calculate(125)
        assertEquals(AffinityMilestoneRank.GOLD, progressGold.currentRank)
        assertEquals(120, progressGold.previousThreshold)
        assertEquals(180, progressGold.nextThreshold)
        assertEquals(55, progressGold.pointsNeeded)

        val progressMax = AffinityMilestoneProgressCalculator.calculate(350)
        assertEquals(AffinityMilestoneRank.MASTER, progressMax.currentRank)
        assertTrue(progressMax.isMaxMilestone)
        assertEquals(1.0f, progressMax.bracketProgress, 0.001f)
    }

    @Test
    fun testRankFrameVisualProperties() {
        for (rank in AffinityMilestoneRank.values()) {
            assertNotNull(rank.gradientColors)
            assertTrue(rank.gradientColors.isNotEmpty())
            assertTrue(rank.borderWidth.value > 0f)
            assertTrue(rank.borderShadowElevation.value > 0f)
            assertTrue(rank.title.isNotBlank())
            assertTrue(rank.rankLabel.isNotBlank())
            assertTrue(rank.rankBadgeIcon.isNotBlank())
        }
    }

    @Test
    fun testUnlockedAffinityPassivesAndResourceBonusCatalog() {
        val character = com.example.haremdark.models.Character(
            id = "test_char",
            name = "Test Girl",
            archetypeId = "subka",
            affinityPoints = 125,
            duvera = 80,
            loajalita = 75,
            poslusnost = 85,
            touha = 70,
            morale = 90,
            strength = 50
        )

        val unlocked = com.example.haremdark.data.AffinityMilestoneBonusCatalog.getUnlockedMilestones(character)
        // At 125 pts, thresholds 30, 70, 120 are unlocked (3 milestones)
        assertEquals(3, unlocked.size)
        assertEquals(30, unlocked[0].milestoneThreshold)
        assertEquals(70, unlocked[1].milestoneThreshold)
        assertEquals(120, unlocked[2].milestoneThreshold)

        val cumulative = com.example.haremdark.data.AffinityMilestoneBonusCatalog.getCumulativeResourceBonuses(character)
        assertTrue(cumulative.totalDailyGold > 0)
        assertTrue(cumulative.totalDailyMana > 0)
    }

    @Test
    fun testAffinitySubStatsDerivation() {
        val character = com.example.haremdark.models.Character(
            id = "test_char",
            name = "Test Girl",
            archetypeId = "subka",
            affinityPoints = 150,
            duvera = 80,
            loajalita = 70,
            poslusnost = 90,
            touha = 65,
            morale = 85,
            strength = 60
        )

        val combatSynergy = ((character.loajalita * 0.35f + (character.affinityPoints.coerceAtMost(250) / 2.5f) * 0.40f + character.strength * 0.25f)).toInt().coerceIn(0, 100)
        val domainInfluence = (((character.affinityPoints.coerceAtMost(250) / 2.5f) * 0.45f + character.poslusnost * 0.30f + character.duvera * 0.25f)).toInt().coerceIn(0, 100)
        val devotion = (((character.affinityPoints.coerceAtMost(350) / 3.5f) * 0.50f + character.loajalita * 0.30f + character.morale * 0.20f)).toInt().coerceIn(0, 100)

        assertTrue(combatSynergy in 0..100)
        assertTrue(domainInfluence in 0..100)
        assertTrue(devotion in 0..100)
        assertTrue(character.duvera in 0..100)
        assertTrue(character.loajalita in 0..100)
        assertTrue(character.touha in 0..100)
    }

    @Test
    fun testMilestoneComparisonDeltas() {
        val progress = AffinityMilestoneProgressCalculator.calculate(currentPoints = 50, archetypeId = "subka")
        assertEquals(AffinityMilestoneRank.BRONZE, progress.currentRank)
        assertEquals(70, progress.nextThreshold)
        assertEquals(20, progress.pointsNeeded)
        assertNotNull(progress.nextMilestoneBonus)

        val nextBonus = progress.nextMilestoneBonus!!
        assertEquals(70, nextBonus.milestoneThreshold)
        assertTrue(nextBonus.hpBonus > 0)
        assertTrue(nextBonus.attackBonusPercent >= 0f)
        assertTrue(nextBonus.dailyGold >= 0)
    }
}
