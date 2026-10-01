package com.example.haremdark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.haremdark.R

/**
 * Subtle Lottie Animation overlay that visually distinguishes between:
 * - Buffs: Glowing green expanding aura & sparkle
 * - Debuffs: Pulsing red warning shockwave & heartbeat
 */
@Composable
fun StatusLottieAura(
    isBuff: Boolean,
    modifier: Modifier = Modifier,
    glowScale: Float = 1.4f
) {
    val rawResId = if (isBuff) R.raw.lottie_buff_glow else R.raw.lottie_debuff_pulse
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))
    val composition = compositionResult.value

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = if (isBuff) 0.85f else 1.15f
    )

    // Fallback subtle pulsating ambient glow in case Lottie is parsing or loading
    val infiniteTransition = rememberInfiniteTransition(label = "fallback_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isBuff) 1200 else 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val ambientColor = if (isBuff) Color(0xFF00E676) else Color(0xFFFF1744)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Ambient soft gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(0.85f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ambientColor.copy(alpha = pulseAlpha),
                            ambientColor.copy(alpha = 0f)
                        )
                    )
                )
        )

        // Subtle Lottie Animation
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(glowScale)
            )
        }
    }
}

/**
 * Interactive Status Icon with subtle Lottie animation underneath/around it.
 * Visually distinguishes buffs (glowing green) from debuffs (pulsing red).
 */
@Composable
fun StatusEffectLottieIcon(
    icon: String,
    isBuff: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    iconSize: TextUnit = 11.sp,
    glowScale: Float = 1.35f
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Lottie Aura
        StatusLottieAura(
            isBuff = isBuff,
            modifier = Modifier.fillMaxSize(),
            glowScale = glowScale
        )

        // Foreground status icon emoji/symbol
        Text(
            text = icon,
            fontSize = iconSize,
            textAlign = TextAlign.Center
        )
    }
}
