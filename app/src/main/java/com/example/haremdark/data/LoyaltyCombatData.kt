package com.example.haremdark.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Data model for loyalty tiers and their direct combat performance impacts.
 */
@Serializable
data class LoyaltyCombatBonus(
    val tierLevel: Int, // 1 to 5
    val title: String,
    val icon: String,
    val colorHex: Long,
    val attackMultiplier: Float,
    val defenseMultiplier: Float,
    val critBonusPercent: Int,
    val hpRegenPerTurn: Int,
    val mpRegenPerTurn: Int,
    val assistStrikeChancePercent: Int,
    val protectLordChancePercent: Int,
    val perkTag: String,
    val perkName: String,
    val perkDescription: String,
    val summaryText: String
) {
    val composeColor: Color get() = Color(colorHex)
}

object LoyaltyCombatData {
    fun getBonusForLoyalty(loyalty: Int): LoyaltyCombatBonus = when {
        loyalty >= 95 -> LoyaltyCombatBonus(
            tierLevel = 5,
            title = "Pouto věčné oddanosti",
            icon = "👑",
            colorHex = 0xFFFFD700,
            attackMultiplier = 1.35f,
            defenseMultiplier = 1.30f,
            critBonusPercent = 15,
            hpRegenPerTurn = 15,
            mpRegenPerTurn = 8,
            assistStrikeChancePercent = 50,
            protectLordChancePercent = 45,
            perkTag = "SOULBOUND_ZEAL",
            perkName = "Pouto Duší Pána",
            perkDescription = "+35% Útok, +30% Obrana, +15% Krit, 50% Asistenční úder a krytí Pána.",
            summaryText = "+35% Útok • +30% Obrana • +15% Krit • Asistence 50%"
        )
        loyalty >= 75 -> LoyaltyCombatBonus(
            tierLevel = 4,
            title = "Oddaná fanatička",
            icon = "💖",
            colorHex = 0xFFFF4081,
            attackMultiplier = 1.25f,
            defenseMultiplier = 1.20f,
            critBonusPercent = 10,
            hpRegenPerTurn = 8,
            mpRegenPerTurn = 5,
            assistStrikeChancePercent = 35,
            protectLordChancePercent = 30,
            perkTag = "GUARDIAN_DEVOTION",
            perkName = "Fanatická Ochrana",
            perkDescription = "+25% Útok, +20% Obrana, +10% Krit, 35% Asistence a štít pro Pána.",
            summaryText = "+25% Útok • +20% Obrana • +10% Krit • Asistence 35%"
        )
        loyalty >= 55 -> LoyaltyCombatBonus(
            tierLevel = 3,
            title = "Věrná společnice",
            icon = "🛡️",
            colorHex = 0xFF29B6F6,
            attackMultiplier = 1.15f,
            defenseMultiplier = 1.10f,
            critBonusPercent = 5,
            hpRegenPerTurn = 0,
            mpRegenPerTurn = 3,
            assistStrikeChancePercent = 25,
            protectLordChancePercent = 0,
            perkTag = "DEVOTION_STRIKE",
            perkName = "Věrný Protiúder",
            perkDescription = "+15% Útok, +10% Obrana, +5% Krit, 25% šance na doprovodný úder.",
            summaryText = "+15% Útok • +10% Obrana • +5% Krit • Asistence 25%"
        )
        loyalty >= 30 -> LoyaltyCombatBonus(
            tierLevel = 2,
            title = "Poslušná válečnice",
            icon = "⚔️",
            colorHex = 0xFFAB47BC,
            attackMultiplier = 1.00f,
            defenseMultiplier = 1.00f,
            critBonusPercent = 0,
            hpRegenPerTurn = 0,
            mpRegenPerTurn = 0,
            assistStrikeChancePercent = 0,
            protectLordChancePercent = 0,
            perkTag = "DISCIPLINED",
            perkName = "Bojová Kázeň",
            perkDescription = "Plní bojové rozkazy se standardní efektivitou bez postihů.",
            summaryText = "Standardní bojový výkon (100%)"
        )
        else -> LoyaltyCombatBonus(
            tierLevel = 1,
            title = "Ostražitá & Vzpurná",
            icon = "⚠️",
            colorHex = 0xFFFF5252,
            attackMultiplier = 0.90f,
            defenseMultiplier = 0.90f,
            critBonusPercent = -5,
            hpRegenPerTurn = 0,
            mpRegenPerTurn = 0,
            assistStrikeChancePercent = 0,
            protectLordChancePercent = 0,
            perkTag = "REBELLIOUS",
            perkName = "Zaváhání z nedůvěry",
            perkDescription = "-10% Útok i Obrana. Nízká loajalita způsobuje váhání v boji. Zlepši její věrnost dary!",
            summaryText = "-10% Útok i Obrana (Nízká loajalita)"
        )
    }

    val ALL_TIERS = listOf(
        getBonusForLoyalty(10),
        getBonusForLoyalty(40),
        getBonusForLoyalty(60),
        getBonusForLoyalty(80),
        getBonusForLoyalty(99)
    )
}
