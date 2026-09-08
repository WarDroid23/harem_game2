package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * Types of special abilities with customized visual FX presets.
 */
enum class CombatAbilityType(
    val title: String,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val shakeIntensityPx: Float,
    val particleCount: Int,
    val defaultShape: ParticleShape
) {
    SLASH(
        title = "Sek zbraní",
        icon = "🗡️",
        primaryColor = Color(0xFFEF5350),
        secondaryColor = Color(0xFFFFFFFF),
        shakeIntensityPx = 10f,
        particleCount = 18,
        defaultShape = ParticleShape.SLASH
    ),
    HEAVY_STRIKE(
        title = "Drtivý úder",
        icon = "💥",
        primaryColor = Color(0xFFFF9100),
        secondaryColor = Color(0xFFFFD700),
        shakeIntensityPx = 22f,
        particleCount = 36,
        defaultShape = ParticleShape.SPARK
    ),
    BLEED_STRIKE(
        title = "Krvavé bodnutí",
        icon = "🩸",
        primaryColor = Color(0xFFD50000),
        secondaryColor = Color(0xFF880E4F),
        shakeIntensityPx = 16f,
        particleCount = 32,
        defaultShape = ParticleShape.SLASH
    ),
    DARK_BURST(
        title = "Temný výboj",
        icon = "🔮",
        primaryColor = Color(0xFFAB47BC),
        secondaryColor = Color(0xFF311B92),
        shakeIntensityPx = 18f,
        particleCount = 38,
        defaultShape = ParticleShape.ORB
    ),
    SHADOW_CURSE(
        title = "Prokletí stínů",
        icon = "👁️",
        primaryColor = Color(0xFF7E57C2),
        secondaryColor = Color(0xFF1A237E),
        shakeIntensityPx = 12f,
        particleCount = 28,
        defaultShape = ParticleShape.DIAMOND
    ),
    SOUL_DRAIN(
        title = "Vysátí duše",
        icon = "🖤",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF9C27B0),
        shakeIntensityPx = 14f,
        particleCount = 34,
        defaultShape = ParticleShape.ORB
    ),
    HAREM_SUPPORT(
        title = "Požehnání harému",
        icon = "💖",
        primaryColor = Color(0xFFFF4081),
        secondaryColor = Color(0xFFFF80AB),
        shakeIntensityPx = 8f,
        particleCount = 30,
        defaultShape = ParticleShape.HEART
    ),
    DEFEND(
        title = "Obranný štít",
        icon = "🛡️",
        primaryColor = Color(0xFF2979FF),
        secondaryColor = Color(0xFF80D8FF),
        shakeIntensityPx = 12f,
        particleCount = 24,
        defaultShape = ParticleShape.DIAMOND
    ),
    CHAR_SPECIAL(
        title = "Speciální technika",
        icon = "✨",
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFFF1744),
        shakeIntensityPx = 24f,
        particleCount = 45,
        defaultShape = ParticleShape.STAR
    ),
    ITEM_HEAL(
        title = "Léčivý balzám",
        icon = "🧪",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFFB9F6CA),
        shakeIntensityPx = 6f,
        particleCount = 22,
        defaultShape = ParticleShape.SPARK
    )
}

enum class ParticleShape {
    CIRCLE, SPARK, SLASH, HEART, DIAMOND, ORB, STAR
}

/**
 * Single dynamic particle.
 */
class VisualParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var maxLife: Float,
    var life: Float,
    val color: Color,
    val shape: ParticleShape,
    var rotation: Float = 0f,
    val vRot: Float = 0f,
    val drag: Float = 0.94f,
    val gravity: Float = 0.15f
) {
    val alpha: Float
        get() = (life / maxLife).coerceIn(0f, 1f)

    fun update(): Boolean {
        life -= 16f
        if (life <= 0f) return false
        vx *= drag
        vy = (vy * drag) + gravity
        x += vx
        y += vy
        rotation += vRot
        return true
    }
}

/**
 * Controller holding combat animation states (screen shake, particles, ability banner, flash vignette).
 */
@Stable
class CombatVisualFxState {
    val shakeOffsetX = Animatable(0f)
    val shakeOffsetY = Animatable(0f)
    val flashAlpha = Animatable(0f)
    var flashColor by mutableStateOf(Color.Transparent)

    var activeBanner by mutableStateOf<Pair<CombatAbilityType, String>?>(null)
    val particles = mutableStateListOf<VisualParticle>()

    /**
     * Trigger subtle screen shake and particle explosion for a specific ability.
     */
    fun triggerAbility(
        type: CombatAbilityType,
        customName: String? = null,
        scope: CoroutineScope,
        onImpact: (() -> Unit)? = null
    ) {
        scope.launch {
            val title = customName ?: type.title
            activeBanner = Pair(type, title)
            flashColor = type.primaryColor

            // 1. Particle creation around center of canvas
            val spawnParticles = ArrayList<VisualParticle>(type.particleCount)
            val baseColor = type.primaryColor
            val altColor = type.secondaryColor

            for (i in 0 until type.particleCount) {
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = Random.nextFloat() * 12f + 4f
                val colorMix = if (Random.nextBoolean()) baseColor else altColor
                val lifeMs = Random.nextFloat() * 400f + 450f
                val pSize = Random.nextFloat() * 12f + 8f

                val shape = when (type.defaultShape) {
                    ParticleShape.HEART -> if (Random.nextFloat() < 0.6f) ParticleShape.HEART else ParticleShape.SPARK
                    ParticleShape.SLASH -> if (Random.nextFloat() < 0.5f) ParticleShape.SLASH else ParticleShape.SPARK
                    else -> type.defaultShape
                }

                spawnParticles.add(
                    VisualParticle(
                        x = 0f, // updated to center inside draw scope
                        y = 0f,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed - (if (type == CombatAbilityType.HAREM_SUPPORT) 4f else 1f),
                        size = pSize,
                        maxLife = lifeMs,
                        life = lifeMs,
                        color = colorMix,
                        shape = shape,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 14f,
                        gravity = if (type == CombatAbilityType.HAREM_SUPPORT) -0.1f else 0.22f
                    )
                )
            }

            particles.clear()
            particles.addAll(spawnParticles)

            // 2. Immediate Flash
            launch {
                flashAlpha.snapTo(0.28f)
                flashAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 350, easing = LinearEasing)
                )
            }

            // 3. Screen shake sequence (impact vibration)
            launch {
                val intensity = type.shakeIntensityPx
                val shakePattern = listOf(
                    Pair(intensity * 0.9f, -intensity * 0.7f),
                    Pair(-intensity * 0.8f, intensity * 0.6f),
                    Pair(intensity * 0.6f, -intensity * 0.4f),
                    Pair(-intensity * 0.35f, intensity * 0.25f),
                    Pair(intensity * 0.15f, -intensity * 0.1f),
                    Pair(0f, 0f)
                )
                for (shake in shakePattern) {
                    shakeOffsetX.snapTo(shake.first)
                    shakeOffsetY.snapTo(shake.second)
                    delay(30)
                }
            }

            // 4. Trigger damage and combat resolution right on impact (~40ms)
            delay(40)
            onImpact?.invoke()

            // 5. Run particle simulation loop for ~650ms
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 700L && particles.isNotEmpty()) {
                val iter = particles.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    if (!p.update()) {
                        iter.remove()
                    }
                }
                delay(16)
            }
            particles.clear()

            // 6. Dismiss banner
            delay(150)
            activeBanner = null
        }
    }
}

@Composable
fun rememberCombatVisualFxState(): CombatVisualFxState {
    return remember { CombatVisualFxState() }
}

/**
 * Renders particle effects, screen flashes, and active ability banners over the combat view.
 */
@Composable
fun CombatVisualFxOverlay(
    fxState: CombatVisualFxState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Ambient element vignette/flash
        if (fxState.flashAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                fxState.flashColor.copy(alpha = fxState.flashAlpha.value)
                            )
                        )
                    )
            )
        }

        // 2. Dynamic Canvas for particles
        if (fxState.particles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height * 0.38f // centered around duel area

                for (p in fxState.particles) {
                    // Set spawn origin on first frame if not placed yet
                    val px = if (p.x == 0f) centerX else p.x
                    val py = if (p.y == 0f) centerY else p.y
                    p.x = px
                    p.y = py

                    drawVisualParticle(p)
                }
            }
        }

        // 3. Floating Ability Banner
        androidx.compose.animation.AnimatedVisibility(
            visible = fxState.activeBanner != null,
            enter = fadeIn(animationSpec = tween(120)) + scaleIn(initialScale = 0.75f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.9f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
        ) {
            fxState.activeBanner?.let { (type, title) ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    tonalElevation = 10.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(type.primaryColor, type.secondaryColor, type.primaryColor)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = type.icon,
                            fontSize = 20.sp
                        )
                        Column {
                            Text(
                                text = "SPECIÁLNÍ SCHOPNOST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = type.primaryColor,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
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
 * Draws single particle based on shape.
 */
private fun DrawScope.drawVisualParticle(p: VisualParticle) {
    val alpha = p.alpha
    if (alpha <= 0.01f) return
    val color = p.color.copy(alpha = alpha)

    when (p.shape) {
        ParticleShape.CIRCLE, ParticleShape.SPARK -> {
            drawCircle(
                color = color,
                radius = p.size,
                center = Offset(p.x, p.y)
            )
            // soft outer glow
            drawCircle(
                color = color.copy(alpha = alpha * 0.35f),
                radius = p.size * 1.8f,
                center = Offset(p.x, p.y)
            )
        }
        ParticleShape.ORB -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = alpha), color, Color.Transparent),
                    center = Offset(p.x, p.y),
                    radius = p.size * 2f
                ),
                radius = p.size * 2f,
                center = Offset(p.x, p.y)
            )
        }
        ParticleShape.SLASH -> {
            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                drawLine(
                    color = color,
                    start = Offset(p.x - p.size * 1.6f, p.y),
                    end = Offset(p.x + p.size * 1.6f, p.y),
                    strokeWidth = (p.size * 0.5f).coerceAtLeast(2f)
                )
            }
        }
        ParticleShape.DIAMOND -> {
            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                val path = Path().apply {
                    moveTo(p.x, p.y - p.size)
                    lineTo(p.x + p.size * 0.7f, p.y)
                    lineTo(p.x, p.y + p.size)
                    lineTo(p.x - p.size * 0.7f, p.y)
                    close()
                }
                drawPath(path, color = color)
            }
        }
        ParticleShape.STAR -> {
            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                val s = p.size
                drawLine(color = color, start = Offset(p.x - s, p.y), end = Offset(p.x + s, p.y), strokeWidth = 2.5f)
                drawLine(color = color, start = Offset(p.x, p.y - s), end = Offset(p.x, p.y + s), strokeWidth = 2.5f)
                drawCircle(color = Color.White.copy(alpha = alpha), radius = s * 0.3f, center = Offset(p.x, p.y))
            }
        }
        ParticleShape.HEART -> {
            rotate(p.rotation * 0.4f, pivot = Offset(p.x, p.y)) {
                val s = p.size * 0.9f
                val path = Path().apply {
                    moveTo(p.x, p.y + s * 0.6f)
                    cubicTo(p.x - s, p.y, p.x - s, p.y - s * 0.8f, p.x, p.y - s * 0.4f)
                    cubicTo(p.x + s, p.y - s * 0.8f, p.x + s, p.y, p.x, p.y + s * 0.6f)
                    close()
                }
                drawPath(path, color = color)
            }
        }
    }
}
