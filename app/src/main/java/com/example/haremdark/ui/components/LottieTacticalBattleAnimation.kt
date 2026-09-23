package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

/**
 * Tactical Combat Animation types covering character skill archetypes and status effect triggers.
 */
enum class TacticalAnimationType(
    val title: String,
    val category: String,
    val icon: String,
    val accentColor: Color,
    val secondaryColor: Color
) {
    // Skill Types
    SLASH_BLADE(
        title = "Sek čepelí",
        category = "Dovednost",
        icon = "🗡️",
        accentColor = Color(0xFFEF5350),
        secondaryColor = Color(0xFFFFD54F)
    ),
    DARK_MAGIC_BURST(
        title = "Temný výboj",
        category = "Magie stínů",
        icon = "🔮",
        accentColor = Color(0xFFAB47BC),
        secondaryColor = Color(0xFF311B92)
    ),
    HOLY_HEAL(
        title = "Svaté léčení",
        category = "Obnova",
        icon = "✨",
        accentColor = Color(0xFF00E676),
        secondaryColor = Color(0xFFB9F6CA)
    ),
    HAREM_DEVOTION(
        title = "Vášeň a podpora",
        category = "Harém",
        icon = "💖",
        accentColor = Color(0xFFFF4081),
        secondaryColor = Color(0xFFFF80AB)
    ),
    CRITICAL_SUPERNOVA(
        title = "Kritická supernova",
        category = "Kritický zásah",
        icon = "💥",
        accentColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFFF3D00)
    ),
    HEAVY_CRUSH(
        title = "Drtivý otřes",
        category = "Těžký úder",
        icon = "⚡",
        accentColor = Color(0xFFFF9100),
        secondaryColor = Color(0xFFFFAB40)
    ),

    // Status Effect Triggers
    BLEED_TRIGGER(
        title = "Krvácení aktivováno",
        category = "Stavový efekt",
        icon = "🩸",
        accentColor = Color(0xFFD50000),
        secondaryColor = Color(0xFF880E4F)
    ),
    STUN_TRIGGER(
        title = "Omráčení / Parolýza",
        category = "Stavový efekt",
        icon = "💫",
        accentColor = Color(0xFFFFD600),
        secondaryColor = Color(0xFFFF6D00)
    ),
    SHADOW_CURSE_TRIGGER(
        title = "Kletba stínů",
        category = "Stavový efekt",
        icon = "👁️",
        accentColor = Color(0xFF7E57C2),
        secondaryColor = Color(0xFF1A237E)
    ),
    SHIELD_BARRIER_TRIGGER(
        title = "Obranná bariéra",
        category = "Obrana",
        icon = "🛡️",
        accentColor = Color(0xFF2979FF),
        secondaryColor = Color(0xFF80D8FF)
    ),
    POISON_TRIGGER(
        title = "Jedové zamoření",
        category = "Stavový efekt",
        icon = "🧪",
        accentColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF1B5E20)
    ),
    FURY_TRIGGER(
        title = "Zuřivost a hněv",
        category = "Posílení",
        icon = "🔥",
        accentColor = Color(0xFFFF3D00),
        secondaryColor = Color(0xFFFF9100)
    ),
    DODGE_EVADE(
        title = "Úskok a vyhnutí",
        category = "Reflex",
        icon = "💨",
        accentColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF80D8FF)
    ),
    DOMAIN_EXPANSION(
        title = "Rozšíření domény",
        category = "Doména",
        icon = "🌌",
        accentColor = Color(0xFF6200EE),
        secondaryColor = Color(0xFF3700B3)
    ),
    SKILL_ACTIVATION(
        title = "Aktivace dovednosti",
        category = "Dovednost",
        icon = "⭐",
        accentColor = Color(0xFFFFEA00),
        secondaryColor = Color(0xFFFF9100)
    );

    companion object {
        fun fromLogEntry(type: String, message: String, actionName: String? = null): TacticalAnimationType {
            val text = (message + " " + (actionName ?: "")).lowercase()
            return when {
                text.contains("úskok") || text.contains("vyhn") || text.contains("dodge") || text.contains("evade") || text.contains("minul") -> DODGE_EVADE
                text.contains("krvácen") || text.contains("bleed") || text.contains("krvavé") -> BLEED_TRIGGER
                text.contains("omráčen") || text.contains("stun") || text.contains("paralýz") -> STUN_TRIGGER
                text.contains("proklet") || text.contains("kletb") || text.contains("curse") || text.contains("stínů") -> SHADOW_CURSE_TRIGGER
                text.contains("štít") || text.contains("kryt") || text.contains("defend") || text.contains("obranný") || text.contains("odražení") -> SHIELD_BARRIER_TRIGGER
                text.contains("jed") || text.contains("poison") || text.contains("tox") -> POISON_TRIGGER
                text.contains("zuřivost") || text.contains("fury") || text.contains("hněv") -> FURY_TRIGGER
                text.contains("krit") || text.contains("supernova") || text.contains("zničující") -> CRITICAL_SUPERNOVA
                text.contains("uzdrav") || text.contains("léč") || text.contains("heal") || text.contains("balzám") || type.contains("heal") -> HOLY_HEAL
                text.contains("harém") || text.contains("požehnání") || text.contains("podpora") || text.contains("vášeň") || type == "player_support" -> HAREM_DEVOTION
                text.contains("temn") || text.contains("výboj") || text.contains("duše") || text.contains("dark") || type.contains("spell") -> DARK_MAGIC_BURST
                text.contains("drtiv") || text.contains("těžký") || text.contains("úder") || text.contains("heavy") -> HEAVY_CRUSH
                text.contains("doména") || text.contains("domain") -> DOMAIN_EXPANSION
                text.contains("dovednost") || text.contains("technika") || text.contains("skill") -> SKILL_ACTIVATION
                else -> SLASH_BLADE
            }
        }
    }
}

/**
 * Micro Lottie animated badge for embedding directly inside combat log rows & cards.
 */
@Composable
fun TacticalLottieMicroBadge(
    animationType: TacticalAnimationType,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 26.dp,
    showBorder: Boolean = true
) {
    val jsonString = remember(animationType) {
        LottieTacticalJsonCatalog.getJsonForType(animationType)
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(jsonString))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Integer.MAX_VALUE,
        speed = 1.0f
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .then(
                if (showBorder) {
                    Modifier
                        .clip(CircleShape)
                        .background(animationType.accentColor.copy(alpha = 0.18f))
                        .border(1.dp, animationType.accentColor.copy(alpha = 0.6f), CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize().padding(1.dp)
            )
        } else {
            Text(animationType.icon, fontSize = (sizeDp.value * 0.55f).sp)
        }
    }
}

/**
 * Full Tactical Battle Lottie Animation Overlay that plays dynamically on skill casts & status triggers.
 */
@Composable
fun LottieTacticalBattleAnimation(
    triggerKey: Long,
    animationType: TacticalAnimationType,
    customTitle: String? = null,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp,
    onAnimationEnd: (() -> Unit)? = null
) {
    if (triggerKey <= 0L) return

    val jsonString = remember(animationType) {
        LottieTacticalJsonCatalog.getJsonForType(animationType)
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(jsonString))
    val lottieAnimState = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = triggerKey > 0L,
        iterations = 1,
        speed = 1.25f
    )

    val fadeAnim = remember(triggerKey) { Animatable(0f) }
    val scaleAnim = remember(triggerKey) { Animatable(0.7f) }

    LaunchedEffect(triggerKey) {
        fadeAnim.snapTo(0f)
        scaleAnim.snapTo(0.75f)

        // Intro punch
        fadeAnim.animateTo(1f, tween(120, easing = LinearEasing))
        scaleAnim.animateTo(1.05f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        scaleAnim.animateTo(1.0f, tween(100))

        // Hold active
        delay(1100)

        // Outro fade
        fadeAnim.animateTo(0f, tween(250, easing = LinearEasing))
        onAnimationEnd?.invoke()
    }

    if (fadeAnim.value > 0.01f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .alpha(fadeAnim.value),
            contentAlignment = Alignment.Center
        ) {
            // Radial energy glow backdrop
            Box(
                modifier = Modifier
                    .size(sizeDp * 1.5f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animationType.accentColor.copy(alpha = 0.45f * fadeAnim.value),
                                animationType.secondaryColor.copy(alpha = 0.2f * fadeAnim.value),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.scale(scaleAnim.value)
            ) {
                // Tactical Lottie Render
                Box(
                    modifier = Modifier.size(sizeDp),
                    contentAlignment = Alignment.Center
                ) {
                    composition?.let { comp ->
                        LottieAnimation(
                            composition = comp,
                            progress = { lottieAnimState.progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Tactical Banner Callout
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F0814).copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(
                                animationType.accentColor,
                                animationType.secondaryColor,
                                animationType.accentColor
                            )
                        )
                    ),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(animationType.icon, fontSize = 18.sp)
                        Column {
                            Text(
                                text = animationType.category.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = animationType.accentColor,
                                letterSpacing = 1.1.sp
                            )
                            Text(
                                text = customTitle ?: animationType.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Embedded Self-Contained Lottie JSON catalogue for tactical animations.
 * Provides high-performance vector animations without external file dependencies.
 */
object LottieTacticalJsonCatalog {

    fun getJsonForType(type: TacticalAnimationType): String {
        return when (type) {
            TacticalAnimationType.SLASH_BLADE -> SLASH_JSON
            TacticalAnimationType.DARK_MAGIC_BURST -> DARK_MAGIC_JSON
            TacticalAnimationType.HOLY_HEAL -> HEAL_JSON
            TacticalAnimationType.HAREM_DEVOTION -> DEVOTION_JSON
            TacticalAnimationType.CRITICAL_SUPERNOVA -> SUPERNOVA_JSON
            TacticalAnimationType.HEAVY_CRUSH -> HEAVY_CRUSH_JSON
            TacticalAnimationType.BLEED_TRIGGER -> BLEED_JSON
            TacticalAnimationType.STUN_TRIGGER -> STUN_JSON
            TacticalAnimationType.SHADOW_CURSE_TRIGGER -> CURSE_JSON
            TacticalAnimationType.SHIELD_BARRIER_TRIGGER -> SHIELD_JSON
            TacticalAnimationType.POISON_TRIGGER -> POISON_JSON
            TacticalAnimationType.FURY_TRIGGER -> FURY_JSON
            TacticalAnimationType.DODGE_EVADE -> SLASH_JSON
            TacticalAnimationType.DOMAIN_EXPANSION -> DOMAIN_EXPANSION_JSON
            TacticalAnimationType.SKILL_ACTIVATION -> SUPERNOVA_JSON
        }
    }

    val DOMAIN_EXPANSION_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 60, "w": 200, "h": 200, "nm": "Domain Expansion", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Domain Expansion Glow", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 15, "s": [100] }, { "t": 45, "s": [100] }, { "t": 60, "s": [0] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [10, 10] }, { "t": 30, "s": [150, 150] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [80, 80] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.38, 0, 0.93, 1] }, "o": { "a": 0, "k": 60 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val SLASH_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 36, "w": 200, "h": 200, "nm": "Slash Cut", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Slash Blade 1", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 6, "s": [100] }, { "t": 24, "s": [100] }, { "t": 36, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [-45] }, { "t": 36, "s": [45] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 10] }, { "t": 12, "s": [140, 30] }, { "t": 36, "s": [160, 5] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [120, 16] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.25, 0.25, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Core Beam", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 2, "s": [0] }, { "t": 8, "s": [100] }, { "t": 22, "s": [100] }, { "t": 34, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [-35] }, { "t": 36, "s": [55] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 6] }, { "t": 12, "s": [110, 15] }, { "t": 36, "s": [130, 2] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [100, 10] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.95, 0.6, 1] }, "o": { "a": 0, "k": 100 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val DARK_MAGIC_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 40, "w": 200, "h": 200, "nm": "Dark Magic Orb", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Dark Vortex", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [95] }, { "t": 28, "s": [95] }, { "t": 40, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 40, "s": [360] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 16, "s": [120, 120] }, { "t": 40, "s": [145, 145] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [70, 70] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [0.67, 0.28, 0.74, 1] }, "w": { "a": 0, "k": 8 }, "o": { "a": 0, "k": 90 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [45, 45] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.38, 0.08, 0.58, 1] }, "o": { "a": 0, "k": 85 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Inner Void", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 35, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 40, "s": [-180] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [10, 10] }, { "t": 20, "s": [85, 85] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [30, 30] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.1, 0.02, 0.2, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val HEAL_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 38, "w": 200, "h": 200, "nm": "Holy Heal", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Healing Cross", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [100] }, { "t": 26, "s": [100] }, { "t": 38, "s": [0] }] },
            "r": { "a": 0, "k": 0 },
            "p": { "a": 1, "k": [{ "t": 0, "s": [100, 120, 0] }, { "t": 38, "s": [100, 80, 0] }] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 14, "s": [115, 115] }, { "t": 38, "s": [130, 130] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [20, 60] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [60, 20] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                { "ty": "fl", "c": { "a": 0, "k": [0, 0.9, 0.46, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        },
        {
          "ddd": 0, "ind": 2, "ty": 4, "nm": "Radiant Ring", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [85] }, { "t": 38, "s": [0] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 38, "s": [140, 140] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [80, 80] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [0.72, 0.96, 0.79, 1] }, "w": { "a": 0, "k": 4 }, "o": { "a": 0, "k": 80 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val DEVOTION_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 38, "w": 200, "h": 200, "nm": "Devotion Hearts", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Pulse Heart", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [100] }, { "t": 28, "s": [100] }, { "t": 38, "s": [0] }] },
            "r": { "a": 0, "k": 0 },
            "p": { "a": 0, "k": [100, 95, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [35, 35] }, { "t": 14, "s": [120, 120] }, { "t": 38, "s": [135, 135] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [50, 50] }, "p": { "a": 0, "k": [-18, -15] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [50, 50] }, "p": { "a": 0, "k": [18, -15] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.25, 0.5, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val SUPERNOVA_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 40, "w": 200, "h": 200, "nm": "Supernova Crit", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Star Beams", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 6, "s": [100] }, { "t": 25, "s": [100] }, { "t": 40, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 40, "s": [120] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [25, 25] }, { "t": 15, "s": [135, 135] }, { "t": 40, "s": [150, 150] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [8, 90] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 2 } },
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [90, 8] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 2 } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.84, 0, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [40, 40] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.24, 0, 1] }, "o": { "a": 0, "k": 85 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val HEAVY_CRUSH_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 35, "w": 200, "h": 200, "nm": "Heavy Shockwave", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Shockwave", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 5, "s": [100] }, { "t": 20, "s": [80] }, { "t": 35, "s": [0] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 35, "s": [150, 150] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [70, 70] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "st", "c": { "a": 0, "k": [1, 0.57, 0, 1] }, "w": { "a": 0, "k": 8 }, "o": { "a": 0, "k": 90 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val BLEED_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 36, "w": 200, "h": 200, "nm": "Bleed Drops", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Blood Splatter", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 6, "s": [100] }, { "t": 25, "s": [100] }, { "t": 36, "s": [0] }] },
            "p": { "a": 1, "k": [{ "t": 0, "s": [100, 85, 0] }, { "t": 36, "s": [100, 115, 0] }] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [40, 40] }, { "t": 18, "s": [110, 125] }, { "t": 36, "s": [120, 140] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [40, 55] }, "p": { "a": 0, "k": [0, 0] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 28] }, "p": { "a": 0, "k": [-24, 15] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [18, 22] }, "p": { "a": 0, "k": [22, 18] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.83, 0, 0, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val STUN_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 40, "w": 200, "h": 200, "nm": "Stun Stars", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Spinning Stars", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [100] }, { "t": 30, "s": [100] }, { "t": 40, "s": [0] }] },
            "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 40, "s": [360] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 20, "s": [115, 115] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 22] }, "p": { "a": 0, "k": [-35, 0] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 22] }, "p": { "a": 0, "k": [35, 0] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [22, 22] }, "p": { "a": 0, "k": [0, -35] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.84, 0, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val CURSE_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 40, "w": 200, "h": 200, "nm": "Shadow Curse", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Cursed Eye", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [95] }, { "t": 30, "s": [95] }, { "t": 40, "s": [0] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 15] }, { "t": 18, "s": [120, 60] }, { "t": 40, "s": [130, 20] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [70, 70] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.49, 0.34, 0.76, 1] }, "o": { "a": 0, "k": 85 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [25, 25] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [0.1, 0.1, 0.3, 1] }, "o": { "a": 0, "k": 100 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val SHIELD_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 38, "w": 200, "h": 200, "nm": "Shield Barrier", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Barrier Ring", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [90] }, { "t": 28, "s": [90] }, { "t": 38, "s": [0] }] },
            "p": { "a": 0, "k": [100, 100, 0] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [40, 40] }, { "t": 15, "s": [125, 125] }, { "t": 38, "s": [135, 135] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "rc", "s": { "a": 0, "k": [70, 70] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 16 } },
                { "ty": "st", "c": { "a": 0, "k": [0.16, 0.47, 1, 1] }, "w": { "a": 0, "k": 6 }, "o": { "a": 0, "k": 95 } },
                { "ty": "fl", "c": { "a": 0, "k": [0.5, 0.85, 1, 1] }, "o": { "a": 0, "k": 25 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val POISON_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 36, "w": 200, "h": 200, "nm": "Poison Bubbles", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Toxic Fume", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 6, "s": [85] }, { "t": 26, "s": [85] }, { "t": 36, "s": [0] }] },
            "p": { "a": 1, "k": [{ "t": 0, "s": [100, 110, 0] }, { "t": 36, "s": [100, 90, 0] }] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [30, 30] }, { "t": 20, "s": [120, 120] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [35, 35] }, "p": { "a": 0, "k": [-20, 0] } },
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [45, 45] }, "p": { "a": 0, "k": [15, -10] } },
                { "ty": "fl", "c": { "a": 0, "k": [0, 0.9, 0.46, 1] }, "o": { "a": 0, "k": 80 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val FURY_JSON = """
    {
      "v": "5.7.4", "fr": 30, "ip": 0, "op": 36, "w": 200, "h": 200, "nm": "Fury Flame", "ddd": 0, "assets": [],
      "layers": [
        {
          "ddd": 0, "ind": 1, "ty": 4, "nm": "Rage Fire", "sr": 1,
          "ks": {
            "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 6, "s": [95] }, { "t": 25, "s": [95] }, { "t": 36, "s": [0] }] },
            "p": { "a": 1, "k": [{ "t": 0, "s": [100, 110, 0] }, { "t": 36, "s": [100, 85, 0] }] },
            "s": { "a": 1, "k": [{ "t": 0, "s": [40, 40] }, { "t": 18, "s": [125, 135] }] }
          },
          "shapes": [
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [55, 75] }, "p": { "a": 0, "k": [0, 0] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.24, 0, 1] }, "o": { "a": 0, "k": 90 } }
              ]
            },
            {
              "ty": "gr",
              "it": [
                { "d": 1, "ty": "el", "s": { "a": 0, "k": [30, 45] }, "p": { "a": 0, "k": [0, 5] } },
                { "ty": "fl", "c": { "a": 0, "k": [1, 0.84, 0, 1] }, "o": { "a": 0, "k": 95 } }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()
}
