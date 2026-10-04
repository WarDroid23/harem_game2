package com.example.haremdark.models

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class TimeOfDay(
    val title: String,
    val emoji: String,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val bonusDescription: String
) {
    NIGHT(
        title = "Hluboká Noc",
        emoji = "🌙",
        backgroundColor = 0xFF0A0512,
        surfaceColor = 0xFF1B0B2E,
        bonusDescription = "+15% Temná energie & Stínová síla"
    ),
    DAWN(
        title = "Krvavý Úsvit",
        emoji = "🌅",
        backgroundColor = 0xFF1E0A12,
        surfaceColor = 0xFF381020,
        bonusDescription = "+15% Bojová morálka & Iniciativa"
    ),
    NOON(
        title = "Poledne",
        emoji = "☀️",
        backgroundColor = 0xFF1E160A,
        surfaceColor = 0xFF382B10,
        bonusDescription = "+15% Zlaťáky & Produkce dominia"
    ),
    DUSK(
        title = "Soumrak",
        emoji = "🌆",
        backgroundColor = 0xFF160A1E,
        surfaceColor = 0xFF2E1238,
        bonusDescription = "+15% Afinita & Svádění"
    );

    val resolvedBackground: Color get() = Color(backgroundColor)
    val resolvedSurface: Color get() = Color(surfaceColor)

    companion object {
        fun getForDay(day: Int): TimeOfDay {
            return when (day % 4) {
                0 -> NIGHT
                1 -> DAWN
                2 -> NOON
                else -> DUSK
            }
        }
    }
}
