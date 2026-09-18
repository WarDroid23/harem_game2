package com.example.haremdark.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.R
import com.example.haremdark.models.Character
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class MoodCategory {
    ROMANTIC,       // In love, passionate, devoted, high affinity (Level 5+)
    CELESTIAL,      // Soulmate, eternal queen, supreme loyalty (Level 6+)
    PASSIONATE,     // Fiery, high desire, ecstatic, warrior passion
    SERENE,         // Happy, obedient, peaceful, content
    MELANCHOLIC,    // Sad, fearful, crying, longing
    CORRUPTED,      // Broken, mindbreak, high corruption/degradation
    NEUTRAL         // Calm, observant, reserved
}

enum class MoodParticleType {
    HEARTS,
    CELESTIAL_STARS,
    EMBERS,
    RAIN_MIST,
    VOID_WISPS,
    SPARKLES
}

data class CharacterMoodTheme(
    val category: MoodCategory,
    val backgroundDrawableRes: Int,
    val moodTitle: String,
    val moodIcon: String,
    val moodDescription: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val gradientColors: List<Color>,
    val particleType: MoodParticleType,
    val particleColor: Color,
    val ambientGlowColor: Color
)

object MoodThemeResolver {
    fun resolve(character: Character): CharacterMoodTheme {
        val mood = character.nalada.lowercase()
        val affinity = character.affinityLevel
        val morale = character.morale
        val broken = character.broken
        val corruption = character.fazeZkazenosti

        return when {
            // Celestial tier (Level 6 Queen or high affinity soulmate with 80+ loyalty)
            affinity >= 6 || (affinity >= 5 && character.loajalita >= 80) || character.jeManzelkou -> {
                CharacterMoodTheme(
                    category = MoodCategory.CELESTIAL,
                    backgroundDrawableRes = R.drawable.bg_mood_celestial_1789700563969,
                    moodTitle = "Královská oddanost & Záře",
                    moodIcon = "👑",
                    moodDescription = "Její srdce je plně spjato s tvým osudem. V komnatách září posvátné světlo věrnosti.",
                    primaryAccent = Color(0xFFFFD700),
                    secondaryAccent = Color(0xFFBA68C8),
                    gradientColors = listOf(
                        Color(0x66FFD700),
                        Color(0x554A148C),
                        Color(0xE60D0414)
                    ),
                    particleType = MoodParticleType.CELESTIAL_STARS,
                    particleColor = Color(0xFFFFE082),
                    ambientGlowColor = Color(0x44FFD700)
                )
            }
            // Corrupted / Mindbreak / Dark Void
            broken > 50 || corruption >= 4 || mood.contains("zlomen") || mood.contains("temn") -> {
                CharacterMoodTheme(
                    category = MoodCategory.CORRUPTED,
                    backgroundDrawableRes = R.drawable.bg_mood_passionate_1789700590654,
                    moodTitle = "Temné pohlcení & Zkáza",
                    moodIcon = "🔮",
                    moodDescription = "Temná magie a podrobená vůle obklopují komnatu hlubokým magickým vírem.",
                    primaryAccent = Color(0xFFAB47BC),
                    secondaryAccent = Color(0xFFD32F2F),
                    gradientColors = listOf(
                        Color(0x668E24AA),
                        Color(0x661A002C),
                        Color(0xFA09010E)
                    ),
                    particleType = MoodParticleType.VOID_WISPS,
                    particleColor = Color(0xFFE040FB),
                    ambientGlowColor = Color(0x449C27B0)
                )
            }
            // Romantic / In Love / High Affinity (Level 3-5 or romantic moods)
            affinity >= 3 || mood.contains("zamilovan") || mood.contains("lásk") || character.partnerka || character.romanceBody >= 40 -> {
                CharacterMoodTheme(
                    category = MoodCategory.ROMANTIC,
                    backgroundDrawableRes = R.drawable.bg_mood_romantic_1789700551759,
                    moodTitle = "Romantická intimita",
                    moodIcon = "💖",
                    moodDescription = "Jemné plátky růží a hřejivé světlo svící. Dívka dychtí po tvé blízkosti a něze.",
                    primaryAccent = Color(0xFFFF4081),
                    secondaryAccent = Color(0xFFFF80AB),
                    gradientColors = listOf(
                        Color(0x66FF4081),
                        Color(0x553E1029),
                        Color(0xEB130310)
                    ),
                    particleType = MoodParticleType.HEARTS,
                    particleColor = Color(0xFFFF4081),
                    ambientGlowColor = Color(0x44FF4081)
                )
            }
            // Fiery / Passionate / Angry / Combatant
            mood.contains("vášniv") || mood.contains("nadšen") || mood.contains("vztekl") || mood.contains("zuřiv") || character.touha >= 70 -> {
                CharacterMoodTheme(
                    category = MoodCategory.PASSIONATE,
                    backgroundDrawableRes = R.drawable.bg_mood_passionate_1789700590654,
                    moodTitle = "Planoucí vášeň & Žár",
                    moodIcon = "🔥",
                    moodDescription = "Komnata sálá nezkrotnou energií a ohněm touhy, který nelze uhasit.",
                    primaryAccent = Color(0xFFFF5722),
                    secondaryAccent = Color(0xFFFF9800),
                    gradientColors = listOf(
                        Color(0x66FF5722),
                        Color(0x553B1202),
                        Color(0xEB130402)
                    ),
                    particleType = MoodParticleType.EMBERS,
                    particleColor = Color(0xFFFFAB40),
                    ambientGlowColor = Color(0x44FF5722)
                )
            }
            // Melancholic / Sad / Fearful
            morale < 35 || mood.contains("smutn") || mood.contains("vyděšen") || mood.contains("zouf") || character.strach >= 60 -> {
                CharacterMoodTheme(
                    category = MoodCategory.MELANCHOLIC,
                    backgroundDrawableRes = R.drawable.bg_mood_melancholy_1789700577635,
                    moodTitle = "Melancholický stesk & Mlha",
                    moodIcon = "🌧️",
                    moodDescription = "Chladný déšť stéká po hradních oknech. Dívka hledá útěchu a jistotu u svého pána.",
                    primaryAccent = Color(0xFF64B5F6),
                    secondaryAccent = Color(0xFF7E57C2),
                    gradientColors = listOf(
                        Color(0x5542A5F5),
                        Color(0x55102035),
                        Color(0xEB040B14)
                    ),
                    particleType = MoodParticleType.RAIN_MIST,
                    particleColor = Color(0xFF90CAF9),
                    ambientGlowColor = Color(0x332196F3)
                )
            }
            // Serene / Peaceful / Happy
            morale >= 70 || mood.contains("šťastn") || mood.contains("vesel") || mood.contains("spokojen") -> {
                CharacterMoodTheme(
                    category = MoodCategory.SERENE,
                    backgroundDrawableRes = R.drawable.harem_bg_morning,
                    moodTitle = "Harmonický klid & Radost",
                    moodIcon = "🌸",
                    moodDescription = "Klidná a uvolněná atmosféra ranního svítání plná spokojenosti a vděku.",
                    primaryAccent = Color(0xFF81C784),
                    secondaryAccent = Color(0xFFFFB74D),
                    gradientColors = listOf(
                        Color(0x5581C784),
                        Color(0x5519331B),
                        Color(0xEB081409)
                    ),
                    particleType = MoodParticleType.SPARKLES,
                    particleColor = Color(0xFFA5D6A7),
                    ambientGlowColor = Color(0x334CAF50)
                )
            }
            // Default Neutral
            else -> {
                CharacterMoodTheme(
                    category = MoodCategory.NEUTRAL,
                    backgroundDrawableRes = R.drawable.harem_bg_day,
                    moodTitle = "Vyrovnaná atmosféra",
                    moodIcon = "✨",
                    moodDescription = "Běžný den v soukromých komnatách dominia pod dohledem pána.",
                    primaryAccent = Color(0xFF90CAF9),
                    secondaryAccent = Color(0xFFCE93D8),
                    gradientColors = listOf(
                        Color(0x4490CAF9),
                        Color(0x55211530),
                        Color(0xEB0F0619)
                    ),
                    particleType = MoodParticleType.SPARKLES,
                    particleColor = Color(0xFFE1BEE7),
                    ambientGlowColor = Color(0x229C27B0)
                )
            }
        }
    }
}

/**
 * Dynamic background container that renders high-fidelity artwork and interactive mood particles.
 */
@Composable
fun DynamicCharacterMoodBackground(
    character: Character,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(CharacterMoodTheme) -> Unit
) {
    val theme = remember(character.nalada, character.affinityLevel, character.morale, character.broken, character.fazeZkazenosti, character.partnerka, character.jeManzelkou) {
        MoodThemeResolver.resolve(character)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Layer 1: Dynamic Background Image with Smooth Crossfade
        Crossfade(
            targetState = theme.backgroundDrawableRes,
            animationSpec = tween(durationMillis = 600),
            label = "MoodBgCrossfade"
        ) { drawableRes ->
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = "Atmosféra: ${theme.moodTitle}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Layer 2: Dynamic Atmospheric Color Tint & Vignette Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = theme.gradientColors
                    )
                )
        )

        // Layer 3: Ambient Radial Glow at top/center
        val infiniteTransition = rememberInfiniteTransition(label = "MoodGlowAnim")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowPulse"
        )
        val particleProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ParticleProgress"
        )

        // Layer 4: Floating Particle Canvas (Hearts, Embers, Stars, Rain, Wisps)
        MoodParticleCanvas(
            particleType = theme.particleType,
            particleColor = theme.particleColor,
            progress = particleProgress,
            pulseScale = pulseScale,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 5: Child Content
        content(theme)
    }
}

/**
 * Custom Canvas Particle System for dynamic mood rendering.
 */
@Composable
fun MoodParticleCanvas(
    particleType: MoodParticleType,
    particleColor: Color,
    progress: Float,
    pulseScale: Float,
    modifier: Modifier = Modifier
) {
    // Generate static seed particles
    val particles = remember(particleType) {
        List(24) { i ->
            ParticleSeed(
                baseX = (i * 0.042f + Random.nextFloat() * 0.03f).coerceIn(0.02f, 0.98f),
                baseSpeed = 0.6f + Random.nextFloat() * 0.8f,
                size = 6f + Random.nextFloat() * 12f,
                alpha = 0.3f + Random.nextFloat() * 0.55f,
                swayFrequency = 1.5f + Random.nextFloat() * 2.5f,
                swayAmplitude = 15f + Random.nextFloat() * 25f
            )
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEachIndexed { index, p ->
            val effectiveProg = (progress * p.baseSpeed + (index * 0.0416f)) % 1f
            
            // Move upwards for hearts, embers, stars; downwards for rain
            val yPos = if (particleType == MoodParticleType.RAIN_MIST) {
                effectiveProg * canvasHeight
            } else {
                canvasHeight - (effectiveProg * canvasHeight)
            }

            val sway = sin((progress * p.swayFrequency * 2 * PI + index).toFloat()) * p.swayAmplitude
            val xPos = (p.baseX * canvasWidth + sway).coerceIn(0f, canvasWidth)
            val currentAlpha = (p.alpha * (1f - (effectiveProg - 0.5f) * (effectiveProg - 0.5f) * 4).coerceIn(0f, 1f))

            when (particleType) {
                MoodParticleType.HEARTS -> {
                    drawHeart(
                        center = Offset(xPos, yPos),
                        size = p.size * pulseScale,
                        color = particleColor.copy(alpha = currentAlpha)
                    )
                }
                MoodParticleType.CELESTIAL_STARS -> {
                    drawStar(
                        center = Offset(xPos, yPos),
                        size = p.size * pulseScale * 1.2f,
                        color = particleColor.copy(alpha = currentAlpha)
                    )
                }
                MoodParticleType.EMBERS -> {
                    drawCircle(
                        color = particleColor.copy(alpha = currentAlpha),
                        radius = (p.size / 2.2f) * (1f - effectiveProg * 0.5f),
                        center = Offset(xPos, yPos)
                    )
                }
                MoodParticleType.RAIN_MIST -> {
                    drawLine(
                        color = particleColor.copy(alpha = currentAlpha * 0.7f),
                        start = Offset(xPos, yPos),
                        end = Offset(xPos - 3f, yPos + p.size * 2f),
                        strokeWidth = 1.8f
                    )
                }
                MoodParticleType.VOID_WISPS -> {
                    drawCircle(
                        color = particleColor.copy(alpha = currentAlpha * 0.4f),
                        radius = p.size * 1.5f * pulseScale,
                        center = Offset(xPos, yPos)
                    )
                }
                MoodParticleType.SPARKLES -> {
                    drawStar(
                        center = Offset(xPos, yPos),
                        size = p.size * 0.9f,
                        color = particleColor.copy(alpha = currentAlpha)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val width = size * 1.2f
        val height = size * 1.2f
        val x = center.x - width / 2
        val y = center.y - height / 2

        moveTo(x + width / 2, y + height / 5)
        cubicTo(x + width / 2, y, x, y, x, y + height / 3)
        cubicTo(x, y + height * 2 / 3, x + width / 2, y + height * 0.9f, x + width / 2, y + height)
        cubicTo(x + width / 2, y + height * 0.9f, x + width, y + height * 2 / 3, x + width, y + height / 3)
        cubicTo(x + width, y, x + width / 2, y, x + width / 2, y + height / 5)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val radius = size
        val innerRadius = size * 0.4f
        val numPoints = 4
        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = i * Math.PI / numPoints - Math.PI / 2
            val px = (center.x + r * cos(angle)).toFloat()
            val py = (center.y + r * sin(angle)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path, color)
}

private data class ParticleSeed(
    val baseX: Float,
    val baseSpeed: Float,
    val size: Float,
    val alpha: Float,
    val swayFrequency: Float,
    val swayAmplitude: Float
)

/**
 * Atmosphere Badge UI component displaying real-time character mood resonance.
 */
@Composable
fun MoodAtmosphereBanner(
    character: Character,
    modifier: Modifier = Modifier
) {
    val theme = remember(character.nalada, character.affinityLevel, character.morale, character.broken, character.fazeZkazenosti, character.partnerka, character.jeManzelkou) {
        MoodThemeResolver.resolve(character)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = theme.primaryAccent.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(theme.primaryAccent.copy(alpha = 0.25f), CircleShape)
                    .border(1.dp, theme.primaryAccent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(theme.moodIcon, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = theme.moodTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryAccent
                    )
                    Text(
                        text = "• Úroveň náklonnosti ${character.affinityLevel}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = theme.moodDescription,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}
