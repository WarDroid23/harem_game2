package com.example.haremdark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom weather overlay rendering subtle particle effects (Rain, Snow, Mist, Sunny Rays)
 * depending on the current global weather of the explored region.
 */
@Composable
fun CombatWeatherParticles(
    weatherId: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_particles")
    
    // Smooth progress animation that loops continuously
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weather_particle_progress"
    )

    // Generate pseudo-random, persistent properties for particles
    val particleCount = when (weatherId) {
        "blizzard" -> 24 // Snowflakes
        "storm" -> 40    // Raindrops
        "fog" -> 10      // Large mist circles
        "sunny" -> 16    // Warm sun sparks
        else -> 0
    }

    val particles = remember(weatherId) {
        List(particleCount) {
            val randomX = Random.nextFloat()
            val randomY = Random.nextFloat()
            val size = when (weatherId) {
                "blizzard" -> 3f + Random.nextFloat() * 4f
                "storm" -> 1.5f + Random.nextFloat() * 1.5f
                "fog" -> 50f + Random.nextFloat() * 70f
                "sunny" -> 2f + Random.nextFloat() * 4f
                else -> 0f
            }
            val speed = when (weatherId) {
                "blizzard" -> 0.25f + Random.nextFloat() * 0.25f
                "storm" -> 1.2f + Random.nextFloat() * 0.6f
                "fog" -> 0.05f + Random.nextFloat() * 0.05f
                "sunny" -> -0.15f - Random.nextFloat() * 0.25f // Floating upwards
                else -> 0f
            }
            val swayAmplitude = when (weatherId) {
                "blizzard" -> 15f + Random.nextFloat() * 20f
                "fog" -> 30f + Random.nextFloat() * 50f
                "sunny" -> 5f + Random.nextFloat() * 8f
                else -> 0f
            }
            val swayFrequency = 1f + Random.nextFloat() * 2f
            
            WeatherParticleData(
                startX = randomX,
                startY = randomY,
                size = size,
                speed = speed,
                swayAmplitude = swayAmplitude,
                swayFrequency = swayFrequency
            )
        }
    }

    if (particleCount == 0) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        particles.forEach { p ->
            // Update horizontal sway and vertical animation
            val elapsedY = p.startY + (progress * p.speed)
            val y = ((elapsedY % 1f) + 1f) % 1f * h

            val sway = if (p.swayAmplitude > 0) {
                sin((progress + p.startX) * p.swayFrequency * 2 * Math.PI).toFloat() * p.swayAmplitude
            } else 0f

            val x = ((p.startX * w + sway) % w + w) % w

            // Determine opacity based on screen bounds to prevent jarring pop-ins
            val borderFade = when {
                y < h * 0.1f -> y / (h * 0.1f)
                y > h * 0.9f -> (h - y) / (h * 0.1f)
                else -> 1f
            }.coerceIn(0f, 1f)

            when (weatherId) {
                "blizzard" -> {
                    // Soft white fluffy snowflakes
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f * borderFade),
                        radius = p.size,
                        center = Offset(x, y)
                    )
                }
                "storm" -> {
                    // Falling slated raindrop streaks
                    val lineLength = p.size * 12f
                    drawLine(
                        color = Color(0xFF80DEEA).copy(alpha = 0.35f * borderFade),
                        start = Offset(x, y),
                        end = Offset(x - 4f, y + lineLength),
                        strokeWidth = p.size
                    )
                }
                "fog" -> {
                    // Giant slow horizontal mist clouds
                    drawCircle(
                        color = Color(0xFFB0BEC5).copy(alpha = 0.08f * borderFade),
                        radius = p.size,
                        center = Offset(x, y)
                    )
                }
                "sunny" -> {
                    // Warm rising sun motes / gold sparks
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = 0.45f * borderFade),
                        radius = p.size,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

private data class WeatherParticleData(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float
)
