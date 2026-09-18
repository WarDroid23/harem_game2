package com.example.haremdark.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Types of particle bursts triggered during high-affinity actions.
 */
enum class AffinityBurstType {
    HEARTS,
    SPARKLES,
    LOVE_BURST,
    DEVOTION_GOLD
}

/**
 * Single animated particle descriptor.
 */
private data class AffinityParticle(
    val id: Int,
    val shape: AffinityParticleShape,
    val startXRatio: Float,
    val startYRatio: Float,
    val horizontalDrift: Float,
    val verticalSpeed: Float,
    val size: Float,
    val color: Color,
    val initialRotation: Float,
    val rotationSpeed: Float,
    val swayFrequency: Float,
    val swayAmplitude: Float,
    val startDelayRatio: Float
)

private enum class AffinityParticleShape {
    HEART,
    FOUR_POINT_STAR,
    EIGHT_POINT_STAR,
    SPARKLE_DOT,
    RIPPLE_RING
}

/**
 * A subtle, elegant particle effect overlay designed to sit over character portraits
 * or banners whenever a successful high-affinity interaction occurs.
 */
@Composable
fun AffinityParticleOverlay(
    triggerKey: Long,
    burstType: AffinityBurstType = AffinityBurstType.LOVE_BURST,
    intensity: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    if (triggerKey <= 0L) return

    val progressAnim = remember(triggerKey) { Animatable(0f) }
    var activeParticles by remember(triggerKey) { mutableStateOf<List<AffinityParticle>>(emptyList()) }

    LaunchedEffect(triggerKey) {
        val particleCount = (18 * intensity).toInt().coerceIn(12, 36)
        activeParticles = generateParticles(burstType, particleCount)

        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1800,
                easing = LinearEasing
            )
        )
    }

    if (progressAnim.value > 0f && progressAnim.value < 1f) {
        val currentProgress = progressAnim.value

        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Subtle central ambient glow flash at onset of burst
            if (currentProgress < 0.55f) {
                val glowAlpha = ((1f - currentProgress / 0.55f) * 0.38f).coerceIn(0f, 0.45f)
                val glowRadius = width * (0.35f + currentProgress * 0.45f)
                val glowColor = when (burstType) {
                    AffinityBurstType.HEARTS, AffinityBurstType.LOVE_BURST -> Color(0xFFFF4081)
                    AffinityBurstType.SPARKLES, AffinityBurstType.DEVOTION_GOLD -> Color(0xFFFFD700)
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha),
                            glowColor.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, height * 0.55f),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(width * 0.5f, height * 0.55f)
                )
            }

            // 2. Render each active particle
            activeParticles.forEach { p ->
                val particleLocalProgress = ((currentProgress - p.startDelayRatio) / (1f - p.startDelayRatio)).coerceIn(0f, 1f)

                if (particleLocalProgress > 0f) {
                    // Upward floating physics with slight easing
                    val y = height * p.startYRatio - (particleLocalProgress * height * p.verticalSpeed)

                    // Sinusoidal horizontal sway
                    val sway = sin((particleLocalProgress * p.swayFrequency * 2 * PI).toFloat()) * p.swayAmplitude
                    val x = (width * p.startXRatio + sway + (particleLocalProgress * p.horizontalDrift)).coerceIn(0f, width)

                    // Natural pop-in and fade-out alpha curve
                    val alpha = when {
                        particleLocalProgress < 0.2f -> (particleLocalProgress / 0.2f)
                        particleLocalProgress > 0.65f -> ((1f - particleLocalProgress) / 0.35f)
                        else -> 1f
                    }.coerceIn(0f, 1f)

                    // Scale dynamics (grows quickly on emergence, gently shrinks on fadeout)
                    val scaleFactor = when {
                        particleLocalProgress < 0.25f -> 0.4f + (particleLocalProgress / 0.25f) * 0.6f
                        particleLocalProgress > 0.7f -> 1.0f - ((particleLocalProgress - 0.7f) / 0.3f) * 0.3f
                        else -> 1.0f
                    }

                    val rotation = p.initialRotation + (particleLocalProgress * p.rotationSpeed)
                    val center = Offset(x, y)
                    val particleSize = p.size * scaleFactor
                    val particleColor = p.color.copy(alpha = (p.color.alpha * alpha).coerceIn(0f, 1f))

                    rotate(degrees = rotation, pivot = center) {
                        when (p.shape) {
                            AffinityParticleShape.HEART -> {
                                drawHeartShape(center, particleSize, particleColor)
                            }
                            AffinityParticleShape.FOUR_POINT_STAR -> {
                                drawFourPointStar(center, particleSize, particleColor)
                            }
                            AffinityParticleShape.EIGHT_POINT_STAR -> {
                                drawEightPointStar(center, particleSize * 1.15f, particleColor)
                            }
                            AffinityParticleShape.SPARKLE_DOT -> {
                                drawCircle(
                                    color = particleColor,
                                    radius = particleSize * 0.45f,
                                    center = center
                                )
                            }
                            AffinityParticleShape.RIPPLE_RING -> {
                                drawCircle(
                                    color = particleColor,
                                    radius = particleSize * 0.8f,
                                    center = center,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateParticles(burstType: AffinityBurstType, count: Int): List<AffinityParticle> {
    val heartColors = listOf(
        Color(0xFFFF4081), // Neon Pink
        Color(0xFFFF80AB), // Rose Quartz
        Color(0xFFF50057), // Vivid Crimson
        Color(0xFFFF69B4), // Hot Pink
        Color(0xFFFFC0CB), // Pastel Pink
        Color(0xFFE040FB)  // Purple Bloom
    )

    val sparkleColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFFEE58), // Bright Yellow
        Color(0xFFFFB300), // Amber
        Color(0xFF80D8FF), // Cyan Sparkle
        Color(0xFFFFFFFF), // Pure White Star
        Color(0xFFFF8A80)  // Peach Shimmer
    )

    return List(count) { i ->
        val shape = when (burstType) {
            AffinityBurstType.HEARTS -> {
                if (Random.nextFloat() < 0.75f) AffinityParticleShape.HEART else AffinityParticleShape.SPARKLE_DOT
            }
            AffinityBurstType.SPARKLES -> {
                when (Random.nextInt(3)) {
                    0 -> AffinityParticleShape.FOUR_POINT_STAR
                    1 -> AffinityParticleShape.EIGHT_POINT_STAR
                    else -> AffinityParticleShape.SPARKLE_DOT
                }
            }
            AffinityBurstType.LOVE_BURST -> {
                when (Random.nextInt(5)) {
                    0, 1 -> AffinityParticleShape.HEART
                    2 -> AffinityParticleShape.FOUR_POINT_STAR
                    3 -> AffinityParticleShape.EIGHT_POINT_STAR
                    else -> AffinityParticleShape.SPARKLE_DOT
                }
            }
            AffinityBurstType.DEVOTION_GOLD -> {
                when (Random.nextInt(4)) {
                    0 -> AffinityParticleShape.EIGHT_POINT_STAR
                    1 -> AffinityParticleShape.FOUR_POINT_STAR
                    2 -> AffinityParticleShape.HEART
                    else -> AffinityParticleShape.RIPPLE_RING
                }
            }
        }

        val color = when (burstType) {
            AffinityBurstType.HEARTS -> heartColors.random()
            AffinityBurstType.SPARKLES, AffinityBurstType.DEVOTION_GOLD -> sparkleColors.random()
            AffinityBurstType.LOVE_BURST -> {
                if (shape == AffinityParticleShape.HEART) heartColors.random()
                else sparkleColors.random()
            }
        }

        AffinityParticle(
            id = i,
            shape = shape,
            startXRatio = 0.2f + Random.nextFloat() * 0.6f,
            startYRatio = 0.65f + Random.nextFloat() * 0.28f,
            horizontalDrift = (Random.nextFloat() - 0.5f) * 60f,
            verticalSpeed = 0.55f + Random.nextFloat() * 0.5f,
            size = 10f + Random.nextFloat() * 16f,
            color = color,
            initialRotation = Random.nextFloat() * 360f,
            rotationSpeed = (Random.nextFloat() - 0.5f) * 120f,
            swayFrequency = 1.2f + Random.nextFloat() * 2.0f,
            swayAmplitude = 12f + Random.nextFloat() * 20f,
            startDelayRatio = Random.nextFloat() * 0.25f
        )
    }
}

/**
 * Draws a vectorized heart centered at the specified point.
 */
private fun DrawScope.drawHeartShape(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val width = size * 1.15f
        val height = size * 1.15f
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

/**
 * Draws a 4-point sparkling star.
 */
private fun DrawScope.drawFourPointStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val radius = size * 0.9f
        val innerRadius = size * 0.28f
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

/**
 * Draws an 8-point sparkling star with radiant flair.
 */
private fun DrawScope.drawEightPointStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val radius = size * 1.0f
        val innerRadius = size * 0.32f
        val numPoints = 8
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
