package com.example.haremdark

import androidx.compose.ui.graphics.Color
import com.example.haremdark.models.Character
import com.example.haremdark.models.InteractionLogEntry
import com.example.haremdark.ui.components.DynamicMoodEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicMoodIndicatorTest {

    @Test
    fun testLowAffinityBaselineMood() {
        val character = Character(
            id = "girl_1",
            name = "Novice Girl",
            affinityPoints = 15,
            moodScore = 50
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Opatrná zdrženlivost", mood.title)
        assertEquals("😐⛓️", mood.icon)
        assertEquals(Color(0xFF90A4AE), mood.primaryColor)
        assertEquals("Železo", mood.affinityRankName)
    }

    @Test
    fun testHighAffinityBaselineMood() {
        val character = Character(
            id = "girl_2",
            name = "Gold Girl",
            affinityPoints = 140,
            moodScore = 90
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Oddaná láska", mood.title)
        assertEquals("😍💖", mood.icon)
        assertEquals(Color(0xFFFF4081), mood.primaryColor)
        assertEquals("Zlato", mood.affinityRankName)
    }

    @Test
    fun testMasterAffinityBaselineMood() {
        val character = Character(
            id = "girl_master",
            name = "Queen Girl",
            affinityPoints = 360,
            moodScore = 100
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Suverénní harmonie", mood.title)
        assertEquals("👑💖", mood.icon)
        assertEquals(Color(0xFFFFD700), mood.primaryColor)
        assertEquals("Věčný Mistr", mood.affinityRankName)
    }

    @Test
    fun testRecentGiftInteractionHighAffinity() {
        val character = Character(
            id = "girl_gift",
            name = "Gift Girl",
            affinityPoints = 150,
            interactionLogs = mutableListOf(
                InteractionLogEntry(
                    day = 5,
                    type = "dar",
                    title = "Kytice rudých růží a perlový náhrdelník",
                    description = "Dívka byla dojata tvou štědrostí."
                )
            )
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Zbožňující extáze", mood.title)
        assertEquals("💖✨", mood.icon)
        assertEquals(Color(0xFFFF4081), mood.primaryColor)
        assertTrue(mood.recentInteractionImpact.contains("Kytice"))
    }

    @Test
    fun testRecentGiftInteractionLowAffinity() {
        val character = Character(
            id = "girl_gift_low",
            name = "Guarded Girl",
            affinityPoints = 10,
            interactionLogs = mutableListOf(
                InteractionLogEntry(
                    day = 2,
                    type = "dar",
                    title = "Stříbrný hřeben",
                    description = "Překvapeně přijala dar."
                )
            )
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Příjemně zaskočená", mood.title)
        assertEquals("😳🎁", mood.icon)
        assertEquals(Color(0xFFFFB74D), mood.primaryColor)
    }

    @Test
    fun testRecentPunishmentLowAffinity() {
        val character = Character(
            id = "girl_punish_low",
            name = "Fearful Girl",
            affinityPoints = 15,
            interactionLogs = mutableListOf(
                InteractionLogEntry(
                    day = 3,
                    type = "trest",
                    title = "Tvrdé pokárání a zákaz vycházení",
                    description = "Byla potrestána za neposlušnost."
                )
            )
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Zastrašená a opatrná", mood.title)
        assertEquals("😨🌧️", mood.icon)
        assertEquals(Color(0xFFFF7043), mood.primaryColor)
    }

    @Test
    fun testRecentPunishmentHighAffinity() {
        val character = Character(
            id = "girl_punish_high",
            name = "Devoted Girl",
            affinityPoints = 160,
            interactionLogs = mutableListOf(
                InteractionLogEntry(
                    day = 4,
                    type = "trest",
                    title = "Rituální spoutání",
                    description = "Přijala trest s pokorou."
                )
            )
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Sladké pokání", mood.title)
        assertEquals("⛓️💜", mood.icon)
        assertEquals(Color(0xFFBA68C8), mood.primaryColor)
    }

    @Test
    fun testRecentIntimacyHighAffinity() {
        val character = Character(
            id = "girl_intimate",
            name = "Passionate Girl",
            affinityPoints = 180,
            interactionLogs = mutableListOf(
                InteractionLogEntry(
                    day = 6,
                    type = "intimita",
                    title = "Vášnivá noc v pánových komnatách",
                    description = "Nezapomenutelná noc plná doteků."
                )
            )
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Nenasytná vášeň", mood.title)
        assertEquals("🔥💋", mood.icon)
        assertEquals(Color(0xFFE91E63), mood.primaryColor)
    }

    @Test
    fun testRecentTrainingHighAffinity() {
        val character = Character(
            id = "girl_combat",
            name = "Warrior Girl",
            affinityPoints = 95,
            interactionLogs = mutableListOf(
                InteractionLogEntry(
                    day = 7,
                    type = "výcvik",
                    title = "Bojový dril s mečem",
                    description = "Plná koordinace v aréně."
                )
            )
        )
        val mood = DynamicMoodEvaluator.evaluate(character)
        assertEquals("Takticky odhodlaná", mood.title)
        assertEquals("⚔️⚡", mood.icon)
        assertEquals(Color(0xFF00E5FF), mood.primaryColor)
    }

    @Test
    fun test24HourSlotsGenerationAndIntegrity() {
        val character = Character(
            id = "girl_24h_test",
            name = "Test Girl",
            affinityPoints = 85,
            interactionLogs = mutableListOf(
                InteractionLogEntry(day = 3, type = "dar", title = "Drahocenný šperk", description = "Radost z daru", hourOffset = 0),
                InteractionLogEntry(day = 3, type = "rozhovor", title = "Večerní povídání", description = "Důvěrná chvíle", hourOffset = 3)
            )
        )
        val trend = com.example.haremdark.ui.components.DynamicMood24hEvaluator.evaluate24Hours(character)
        assertEquals(24, trend.slots.size)
        assertTrue(trend.totalInteractions24h >= 2)
        assertNotNull(trend.dominantEmotionTitle)
        assertNotNull(trend.dominantEmotionIcon)
        assertTrue(trend.averageValenceScore in 0..100)
        assertTrue(trend.emotionDistribution.isNotEmpty())

        val slotNow = trend.slots.first { it.hourOffset == 0 }
        assertEquals("Nyní", slotNow.relativeLabel)
        assertTrue(slotNow.interactionCount >= 1)
        assertTrue(slotNow.valenceScore >= 70)
    }

    @Test
    fun test24HourInteractionMappingWithEmotions() {
        val character = Character(
            id = "girl_intimacy_trend",
            name = "Intimate Girl",
            affinityPoints = 160,
            interactionLogs = mutableListOf(
                InteractionLogEntry(day = 4, type = "intimita", title = "Vášnivá noc", description = "Blízkost", hourOffset = 0)
            )
        )
        val trend = com.example.haremdark.ui.components.DynamicMood24hEvaluator.evaluate24Hours(character)
        val currentSlot = trend.slots.first { it.hourOffset == 0 }
        assertEquals("Spalující vášeň", currentSlot.dominantEmotion)
        assertEquals("🔥", currentSlot.emotionIcon)
        assertEquals(Color(0xFFE91E63), currentSlot.heatColor)
        assertEquals(98, currentSlot.valenceScore)
    }
}

