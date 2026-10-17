package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import com.example.haremdark.data.AffinityData
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character

/**
 * Visual themes and animation types for each affinity milestone tier threshold.
 */
enum class MilestoneLottieType(
    val milestoneThreshold: Int,
    val title: String,
    val effectName: String,
    val effectIcon: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    SPARKLES_30(
        milestoneThreshold = 30,
        title = "Pokorná služebná (30 pts)",
        effectName = "Zlaté Třpytky & Hvězdná Záře",
        effectIcon = "✨",
        primaryColorHex = 0xFFFFD700,
        secondaryColorHex = 0xFFFFE082
    ),
    EMERALD_BLOOM_70(
        milestoneThreshold = 70,
        title = "Důvěrnice komnat (70 pts)",
        effectName = "Nefritový Rozkvět & Éterický Kruh",
        effectIcon = "🌿",
        primaryColorHex = 0xFF4CAF50,
        secondaryColorHex = 0xFF81C784
    ),
    SAPPHIRE_DEVOTION_120(
        milestoneThreshold = 120,
        title = "Oddaná milenka (120 pts)",
        effectName = "Safírové Krystaly & Modré Srdce",
        effectIcon = "💎",
        primaryColorHex = 0xFF00BCD4,
        secondaryColorHex = 0xFF80DEEA
    ),
    CRIMSON_PASSION_180(
        milestoneThreshold = 180,
        title = "Paní komnat (180 pts)",
        effectName = "Karmínová Vášeň & Víření Růží",
        effectIcon = "💖",
        primaryColorHex = 0xFFFF4081,
        secondaryColorHex = 0xFFFF80AB
    ),
    AMETHYST_VORTEX_250(
        milestoneThreshold = 250,
        title = "Spřízněná duše (250 pts)",
        effectName = "Ametystový Kosmický Vír & Supernova",
        effectIcon = "🌌",
        primaryColorHex = 0xFFE040FB,
        secondaryColorHex = 0xFFEA80FC
    ),
    INFERNO_FIRE_350(
        milestoneThreshold = 350,
        title = "Věčná královna Dominia (350 pts)",
        effectName = "Pekelný Plamen Absolutního Panství",
        effectIcon = "🔥",
        primaryColorHex = 0xFFFF5722,
        secondaryColorHex = 0xFFFFD700
    );

    companion object {
        fun forThreshold(threshold: Int): MilestoneLottieType {
            return when {
                threshold >= 350 -> INFERNO_FIRE_350
                threshold >= 250 -> AMETHYST_VORTEX_250
                threshold >= 180 -> CRIMSON_PASSION_180
                threshold >= 120 -> SAPPHIRE_DEVOTION_120
                threshold >= 70 -> EMERALD_BLOOM_70
                else -> SPARKLES_30
            }
        }
    }
}

/**
 * Self-contained vector Lottie JSON catalog providing unique milestone animations for each tier.
 */
object MilestoneLottieJsonCatalog {

    fun getJson(type: MilestoneLottieType): String {
        return when (type) {
            MilestoneLottieType.SPARKLES_30 -> SPARKLES_30_JSON
            MilestoneLottieType.EMERALD_BLOOM_70 -> EMERALD_BLOOM_70_JSON
            MilestoneLottieType.SAPPHIRE_DEVOTION_120 -> SAPPHIRE_DEVOTION_120_JSON
            MilestoneLottieType.CRIMSON_PASSION_180 -> CRIMSON_PASSION_180_JSON
            MilestoneLottieType.AMETHYST_VORTEX_250 -> AMETHYST_VORTEX_250_JSON
            MilestoneLottieType.INFERNO_FIRE_350 -> INFERNO_FIRE_350_JSON
        }
    }

    /**
     * Tier 30: Golden Sparkles & Twinkling Starlight.
     */
    val SPARKLES_30_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 240, "h": 240, "nm": "Sparkles 30", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Central Golden Star", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 12, "s": [100] }, { "t": 48, "s": [100] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 60, "s": [180] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 20, "s": [130, 130] }, { "t": 45, "s": [110, 110] }, { "t": 60, "s": [140, 140] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [12, 110] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [110, 12] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.85, 0.15, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [8, 80] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [80, 8] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                { "ty": "tr", "p": { "a": 0, "k": [0, 0] }, "a": { "a": 0, "k": [0, 0] }, "s": { "a": 0, "k": [100, 100] }, "r": { "a": 0, "k": 45 }, "o": { "a": 0, "k": 100 } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.95, 0.6, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Sparkle Burst Left", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 5, "s": [0] }, { "t": 18, "s": [100] }, { "t": 45, "s": [100] }, { "t": 58, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 5, "s": [-30] }, { "t": 58, "s": [90] }] },
            "p": { "a": 1, "k": [{ "t": 5, "s": [100, 110, 0] }, { "t": 58, "s": [60, 70, 0] }] },
            "s": { "a": 1, "k": [{ "t": 5, "s": [10, 10] }, { "t": 25, "s": [90, 90] }, { "t": 58, "s": [40, 40] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [6, 45] }, "p": { "a": 0, "k": [0, 0] } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [45, 6] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.92, 0.4, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 3, "ty": 4, "nm": "Sparkle Burst Right", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 10, "s": [0] }, { "t": 24, "s": [100] }, { "t": 50, "s": [100] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 10, "s": [20] }, { "t": 60, "s": [140] }] },
            "p": { "a": 1, "k": [{ "t": 10, "s": [140, 110, 0] }, { "t": 60, "s": [180, 80, 0] }] },
            "s": { "a": 1, "k": [{ "t": 10, "s": [10, 10] }, { "t": 30, "s": [85, 85] }, { "t": 60, "s": [30, 30] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [6, 40] }, "p": { "a": 0, "k": [0, 0] } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [40, 6] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.9, 0.3, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 4, "ty": 4, "nm": "Gold Radiant Halo", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 15, "s": [80] }, { "t": 45, "s": [70] }, { "t": 60, "s": [0] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 40, "s": [145, 145] }, { "t": 60, "s": [160, 160] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [100, 100] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [1, 0.85, 0.2, 1] }, "w": { "a": 0, "k": 4 }, "o": { "a": 0, "k": 80 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    /**
     * Tier 70: Emerald Bloom & Verdant Harmonic Ring.
     */
    val EMERALD_BLOOM_70_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 240, "h": 240, "nm": "Emerald Bloom 70", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Emerald Bloom Core", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 12, "s": [100] }, { "t": 48, "s": [100] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [-45] }, { "t": 60, "s": [45] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [25, 25] }, { "t": 22, "s": [125, 125] }, { "t": 60, "s": [135, 135] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [40, 75] }, "p": { "a": 0, "k": [0, -25] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [40, 75] }, "p": { "a": 0, "k": [0, 25] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [75, 40] }, "p": { "a": 0, "k": [-25, 0] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [75, 40] }, "p": { "a": 0, "k": [25, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.22, 0.78, 0.42, 1] }, "o": { "a": 0, "k": 85 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [35, 35] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.65, 0.98, 0.75, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Jade Ripple", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 15, "s": [90] }, { "t": 45, "s": [60] }, { "t": 60, "s": [0] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 35, "s": [130, 130] }, { "t": 60, "s": [155, 155] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [90, 90] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [0.4, 0.9, 0.55, 1] }, "w": { "a": 0, "k": 5 }, "o": { "a": 0, "k": 85 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    /**
     * Tier 120: Sapphire Crystalline Devotion & Azure Hearts.
     */
    val SAPPHIRE_DEVOTION_120_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 240, "h": 240, "nm": "Sapphire 120", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Sapphire Diamond", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 48, "s": [100] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [-30] }, { "t": 60, "s": [90] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 20, "s": [130, 130] }, { "t": 60, "s": [140, 140] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [70, 70] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 8 } },
                { "ty": "fl", "c": { "a": 0, "k": [0.08, 0.65, 0.92, 1] }, "o": { "a": 0, "k": 85 } },
                { "ty": "st", "c": { "a": 0, "k": [0.65, 0.92, 1.0, 1] }, "w": { "a": 0, "k": 4 }, "o": { "a": 0, "k": 95 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [35, 35] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.9, 0.98, 1.0, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Azure Devotion Heart", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 6, "s": [0] }, { "t": 18, "s": [95] }, { "t": 48, "s": [95] }, { "t": 60, "s": [0] }] },
            "p": { "a": 0, "k": [120, 114, 0] },
            "s": { "a": 1, "k": [{ "t": 6, "s": [30, 30] }, { "t": 25, "s": [115, 115] }, { "t": 60, "s": [130, 130] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [38, 38] }, "p": { "a": 0, "k": [-14, -12] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [38, 38] }, "p": { "a": 0, "k": [14, -12] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.2, 0.8, 1.0, 1] }, "o": { "a": 0, "k": 85 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    /**
     * Tier 180: Crimson Passion Bloom & Whirlwind of Blooming Hearts.
     */
    val CRIMSON_PASSION_180_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 240, "h": 240, "nm": "Crimson Passion 180", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Crimson Heart Core", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 45, "s": [100] }, { "t": 60, "s": [0] }] },
            "p": { "a": 0, "k": [120, 110, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 20, "s": [140, 140] }, { "t": 35, "s": [125, 125] }, { "t": 60, "s": [155, 155] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [54, 54] }, "p": { "a": 0, "k": [-20, -18] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [54, 54] }, "p": { "a": 0, "k": [20, -18] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.18, 0.42, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [26, 26] }, "p": { "a": 0, "k": [-14, -16] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [26, 26] }, "p": { "a": 0, "k": [14, -16] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.65, 0.8, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Passion Whirlwind Ring", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 5, "s": [0] }, { "t": 18, "s": [85] }, { "t": 48, "s": [75] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 60, "s": [220] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 5, "s": [40, 40] }, { "t": 35, "s": [145, 145] }, { "t": 60, "s": [165, 165] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [110, 110] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [1.0, 0.3, 0.55, 1] }, "w": { "a": 0, "k": 5 }, "o": { "a": 0, "k": 80 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    /**
     * Tier 250: Amethyst Soulmate Vortex & Cosmic Starlight Supernova.
     */
    val AMETHYST_VORTEX_250_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 240, "h": 240, "nm": "Amethyst 250", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Cosmic Vortex Swirl", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 12, "s": [100] }, { "t": 50, "s": [100] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 60, "s": [360] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 25, "s": [135, 135] }, { "t": 60, "s": [155, 155] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [90, 90] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [0.85, 0.25, 0.98, 1] }, "w": { "a": 0, "k": 7 }, "o": { "a": 0, "k": 90 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [50, 50] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.55, 0.1, 0.75, 1] }, "o": { "a": 0, "k": 85 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Starlight Starburst", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 8, "s": [0] }, { "t": 20, "s": [100] }, { "t": 48, "s": [100] }, { "t": 60, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 8, "s": [0] }, { "t": 60, "s": [-180] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 8, "s": [15, 15] }, { "t": 30, "s": [120, 120] }, { "t": 60, "s": [140, 140] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [10, 85] }, "p": { "a": 0, "k": [0, 0] } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [85, 10] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.95, 0.75, 1.0, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    /**
     * Tier 350: Hellfire Inferno & Blazing Flames (fire at 350!).
     */
    val INFERNO_FIRE_350_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 240, "h": 240, "nm": "Inferno Fire 350", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Fire Flame Column", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 50, "s": [100] }, { "t": 60, "s": [0] }] },
            "p": { "a": 1, "k": [{ "t": 0, "s": [120, 140, 0] }, { "t": 30, "s": [120, 110, 0] }, { "t": 60, "s": [120, 95, 0] }] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 20] }, { "t": 20, "s": [135, 145] }, { "t": 40, "s": [120, 135] }, { "t": 60, "s": [145, 155] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [65, 110] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.22, 0.05, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [40, 75] }, "p": { "a": 0, "k": [0, 10] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.65, 0.05, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 45] }, "p": { "a": 0, "k": [0, 18] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.95, 0.35, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Fire Ember Left", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 5, "s": [0] }, { "t": 18, "s": [100] }, { "t": 45, "s": [90] }, { "t": 60, "s": [0] }] },
            "p": { "a": 1, "k": [{ "t": 5, "s": [110, 130, 0] }, { "t": 60, "s": [70, 60, 0] }] },
            "s": { "a": 1, "k": [{ "t": 5, "s": [10, 10] }, { "t": 25, "s": [85, 85] }, { "t": 60, "s": [25, 25] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 38] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.45, 0.05, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 3, "ty": 4, "nm": "Fire Ember Right", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 8, "s": [0] }, { "t": 22, "s": [100] }, { "t": 48, "s": [90] }, { "t": 60, "s": [0] }] },
            "p": { "a": 1, "k": [{ "t": 8, "s": [130, 130, 0] }, { "t": 60, "s": [170, 65, 0] }] },
            "s": { "a": 1, "k": [{ "t": 8, "s": [10, 10] }, { "t": 28, "s": [85, 85] }, { "t": 60, "s": [25, 25] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 38] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1.0, 0.55, 0.1, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 4, "ty": 4, "nm": "Blazing Corona Ring", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 15, "s": [85] }, { "t": 45, "s": [85] }, { "t": 60, "s": [0] }] },
            "p": { "a": 0, "k": [120, 120, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 35, "s": [140, 140] }, { "t": 60, "s": [165, 165] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [100, 100] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [1.0, 0.35, 0.05, 1] }, "w": { "a": 0, "k": 6 }, "o": { "a": 0, "k": 85 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()
}

/**
 * Custom Lottie Animation Celebration Overlay displayed when a character crosses an affinity milestone.
 * Features tier-specific Lottie animation effects (sparkles at 30, fire at 350, etc.),
 * dynamic audio fanfare, haptic feedback, celebratory quote, and perks review.
 */
@Composable
fun AffinityMilestoneLottieOverlay(
    milestoneThreshold: Int,
    character: Character,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lottieType = remember(milestoneThreshold) {
        MilestoneLottieType.forThreshold(milestoneThreshold)
    }

    val tierColor = remember(lottieType) { Color(lottieType.primaryColorHex) }
    val secondaryColor = remember(lottieType) { Color(lottieType.secondaryColorHex) }

    val jsonString = remember(lottieType) {
        MilestoneLottieJsonCatalog.getJson(lottieType)
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(jsonString))
    val lottieAnimState = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1.05f
    )

    // Trigger haptic and sound effect once upon mount
    LaunchedEffect(milestoneThreshold) {
        HapticManager.vibrateMilestoneCelebration()
        SoundEffectManager.playMilestoneCelebration(milestoneThreshold)
    }

    // Spring card entry scale and alpha
    val scaleAnim = remember { Animatable(0.75f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(milestoneThreshold) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)
        )
    }
    LaunchedEffect(milestoneThreshold) {
        alphaAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )
    }

    // Infinite breathing glow for the background ring
    val infiniteTransition = rememberInfiniteTransition(label = "milestoneLottieGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val celebratoryLine = remember(character.archetypeId, milestoneThreshold) {
        AffinityData.getCelebratoryMilestoneLine(character, milestoneThreshold)
    }

    val tierLevel = when (milestoneThreshold) {
        30 -> 2
        70 -> 3
        120 -> 4
        180 -> 5
        250 -> 6
        350 -> 6
        else -> 2
    }
    val tierInfo = remember(tierLevel) {
        AffinityData.TIERS.find { it.level == tierLevel } ?: AffinityData.TIERS.first()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xD906020D))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .testTag("affinity_milestone_lottie_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Main Celebration Card
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xF5160924),
                border = BorderStroke(
                    2.dp,
                    Brush.radialGradient(
                        colors = listOf(tierColor, secondaryColor, tierColor)
                    )
                ),
                tonalElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* prevent outside dismiss when clicking inside */ }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header: Milestone achieved badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tierColor.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, tierColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = tierColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "🌟 MILNÍK POUTA ODEMČEN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tierColor
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .testTag("dismiss_milestone_overlay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // --- CUSTOM LOTTIE ANIMATION DISPLAY ---
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing background radial halo
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            tierColor.copy(alpha = 0.35f),
                                            secondaryColor.copy(alpha = 0.12f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Vector Lottie Animation
                        composition?.let { comp ->
                            LottieAnimation(
                                composition = comp,
                                progress = { lottieAnimState.progress },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("milestone_lottie_animation_${milestoneThreshold}")
                            )
                        }
                    }

                    // Milestone Tier Title and Effect Descriptor
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = lottieType.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tierColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(lottieType.effectIcon, fontSize = 12.sp)
                                Text(
                                    text = lottieType.effectName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }
                        }
                    }

                    // Character Flavor Line
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = character.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                            Text(
                                text = if (celebratoryLine.startsWith("„")) celebratoryLine else "„$celebratoryLine“",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = Color(0xFFFCE4EC),
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Unlocked Perks & Combat Bonuses Preview
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🎁 Odemčené odměny & schopnosti:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E102E),
                            border = BorderStroke(1.dp, Color(0xFF4A2B68)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "⚔️ ${tierInfo.combatBonusDescription}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = "✨ ${tierInfo.perkDescription}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }
                    }

                    // Action Button: Dismiss / Accept
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tierColor,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("accept_milestone_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "✨ PŘIJMOUT POUTO",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
