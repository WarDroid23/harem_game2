package com.example.haremdark.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Supported emotional reaction types for character interactions.
 */
enum class CharacterEmotionType(val emoji: String, val displayName: String) {
    BLUSH("😳", "Červenání"),
    CHEER("🎉", "Jásot"),
    LOVE("💖", "Vroucí Láska"),
    SHY("🙈", "Stydlivost"),
    SPARKLE("✨", "Okouzlení")
}

/**
 * A specialized Lottie & animated overlay that renders over character portraits
 * to display emotional reactions (e.g., blushing, cheering, heart bursts) during dialogues and gifting.
 */
@Composable
fun LottieEmotionOverlay(
    triggerKey: Long,
    emotionType: CharacterEmotionType = CharacterEmotionType.BLUSH,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 160.dp
) {
    if (triggerKey <= 0L) return

    val jsonSpec = remember(emotionType) {
        when (emotionType) {
            CharacterEmotionType.BLUSH -> LottieEmotionJsonCatalog.BLUSH_JSON
            CharacterEmotionType.CHEER -> LottieEmotionJsonCatalog.CHEER_JSON
            CharacterEmotionType.LOVE -> LottieEmotionJsonCatalog.LOVE_JSON
            CharacterEmotionType.SHY -> LottieEmotionJsonCatalog.SHY_JSON
            CharacterEmotionType.SPARKLE -> LottieEmotionJsonCatalog.SPARKLE_JSON
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(jsonSpec))
    val lottieAnimState = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = triggerKey > 0L,
        iterations = 1,
        speed = 1.15f
    )

    val fadeAnim = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        fadeAnim.snapTo(0f)
        fadeAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(150, easing = LinearEasing)
        )
        // Hold visible while Lottie plays
        kotlinx.coroutines.delay(1400)
        fadeAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(350, easing = LinearEasing)
        )
    }

    if (fadeAnim.value > 0f) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Lottie Composition overlay
            composition?.let { comp ->
                LottieAnimation(
                    composition = comp,
                    progress = { lottieAnimState.progress },
                    modifier = Modifier
                        .size(sizeDp)
                        .offset(y = 10.dp)
                        .scale(0.85f + fadeAnim.value * 0.25f)
                        .alpha(fadeAnim.value)
                )
            }

            // Complementary particle burst for maximum visual elegance
            val burstType = when (emotionType) {
                CharacterEmotionType.BLUSH -> AffinityBurstType.HEARTS
                CharacterEmotionType.CHEER -> AffinityBurstType.SPARKLES
                CharacterEmotionType.LOVE -> AffinityBurstType.LOVE_BURST
                CharacterEmotionType.SHY -> AffinityBurstType.HEARTS
                CharacterEmotionType.SPARKLE -> AffinityBurstType.DEVOTION_GOLD
            }

            AffinityParticleOverlay(
                triggerKey = triggerKey,
                burstType = burstType,
                intensity = 1.2f,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Lightweight, self-contained Lottie JSON animation string specifications for emotions.
 */
object LottieEmotionJsonCatalog {

    val BLUSH_JSON = """
    {
      "v": "5.7.4",
      "fr": 30,
      "ip": 0,
      "op": 45,
      "w": 200,
      "h": 200,
      "nm": "Blush Hearts",
      "ddd": 0,
      "assets": [],
      "layers": [
        {
          "ddd": 0,
          "ind": 1,
          "ty": 4,
          "nm": "Heart Pulse",
          "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 35, "s": [100] }, { "t": 45, "s": [0] }] },
            "r": { "a": 0, "k": 0 },
            "p": { "a": 0, "k": [100, 90, 0] },
            "a": { "a": 0, "k": [0, 0, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [40, 40] }, { "t": 20, "s": [115, 115] }, { "t": 45, "s": [130, 130] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                {
                  "d": 1,
                  "ty": "el",
                  "s": { "a": 0, "k": [60, 60] },
                  "p": { "a": 0, "k": [-25, -20] }
                },
                {
                  "d": 1,
                  "ty": "el",
                  "s": { "a": 0, "k": [60, 60] },
                  "p": { "a": 0, "k": [25, -20] }
                },
                {
                  "ty": "fl",
                  "c": { "a": 0, "k": [1, 0.25, 0.5, 1] },
                  "o": { "a": 0, "k": 85 }
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val CHEER_JSON = """
    {
      "v": "5.7.4",
      "fr": 30,
      "ip": 0,
      "op": 45,
      "w": 200,
      "h": 200,
      "nm": "Cheer Burst",
      "ddd": 0,
      "assets": [],
      "layers": [
        {
          "ddd": 0,
          "ind": 1,
          "ty": 4,
          "nm": "Gold Stars",
          "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [100] }, { "t": 35, "s": [100] }, { "t": 45, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 45, "s": [180] }] },
            "p": { "a": 0, "k": [100, 80, 0] },
            "a": { "a": 0, "k": [0, 0, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 20, "s": [120, 120] }, { "t": 45, "s": [140, 140] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                {
                  "d": 1,
                  "ty": "el",
                  "s": { "a": 0, "k": [50, 50] },
                  "p": { "a": 0, "k": [0, 0] }
                },
                {
                  "ty": "fl",
                  "c": { "a": 0, "k": [1, 0.84, 0, 1] },
                  "o": { "a": 0, "k": 90 }
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val LOVE_JSON = """
    {
      "v": "5.7.4",
      "fr": 30,
      "ip": 0,
      "op": 45,
      "w": 200,
      "h": 200,
      "nm": "Love Hearts Ring",
      "ddd": 0,
      "assets": [],
      "layers": [
        {
          "ddd": 0,
          "ind": 1,
          "ty": 4,
          "nm": "Pulsing Ring",
          "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 12, "s": [100] }, { "t": 38, "s": [100] }, { "t": 45, "s": [0] }] },
            "r": { "a": 0, "k": 0 },
            "p": { "a": 0, "k": [100, 100, 0] },
            "a": { "a": 0, "k": [0, 0, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 25, "s": [125, 125] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                {
                  "d": 1,
                  "ty": "el",
                  "s": { "a": 0, "k": [80, 80] },
                  "p": { "a": 0, "k": [0, 0] }
                },
                {
                  "ty": "fl",
                  "c": { "a": 0, "k": [0.95, 0.1, 0.4, 1] },
                  "o": { "a": 0, "k": 75 }
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val SHY_JSON = """
    {
      "v": "5.7.4",
      "fr": 30,
      "ip": 0,
      "op": 45,
      "w": 200,
      "h": 200,
      "nm": "Shy Swirl",
      "ddd": 0,
      "assets": [],
      "layers": [
        {
          "ddd": 0,
          "ind": 1,
          "ty": 4,
          "nm": "Pink Flush",
          "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [90] }, { "t": 35, "s": [90] }, { "t": 45, "s": [0] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [50, 50] }, { "t": 25, "s": [110, 110] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                {
                  "d": 1,
                  "ty": "el",
                  "s": { "a": 0, "k": [70, 70] },
                  "p": { "a": 0, "k": [0, 0] }
                },
                {
                  "ty": "fl",
                  "c": { "a": 0, "k": [1, 0.6, 0.8, 1] },
                  "o": { "a": 0, "k": 80 }
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val SPARKLE_JSON = """
    {
      "v": "5.7.4",
      "fr": 30,
      "ip": 0,
      "op": 45,
      "w": 200,
      "h": 200,
      "nm": "Glitter Sparkles",
      "ddd": 0,
      "assets": [],
      "layers": [
        {
          "ddd": 0,
          "ind": 1,
          "ty": 4,
          "nm": "Fairy Dust",
          "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 35, "s": [100] }, { "t": 45, "s": [0] }] },
            "p": { "a": 0, "k": [100, 85, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 20, "s": [130, 130] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                {
                  "d": 1,
                  "ty": "el",
                  "s": { "a": 0, "k": [55, 55] },
                  "p": { "a": 0, "k": [0, 0] }
                },
                {
                  "ty": "fl",
                  "c": { "a": 0, "k": [0.5, 0.85, 1, 1] },
                  "o": { "a": 0, "k": 85 }
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()
}
