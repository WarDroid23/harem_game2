package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.models.EnvironmentalHazard
import com.example.haremdark.models.HazardTargetRule
import com.example.haremdark.models.HazardType
import kotlin.random.Random

/**
 * Top banner showing active battle scenario environmental hazard and terrain modifiers
 * with an interactive dialog explaining tactics and countdown to periodic hazard eruptions.
 */
@Composable
fun EnvironmentalHazardBanner(
    hazard: EnvironmentalHazard?,
    countdown: Int,
    lastTriggerMessage: String? = null,
    modifier: Modifier = Modifier
) {
    if (hazard == null) return

    var showDetailDialog by remember { mutableStateOf(false) }

    // Pulsing animation for imminent danger
    val infiniteTransition = rememberInfiniteTransition(label = "hazard_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (countdown <= 1) 400 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hazard_glow_alpha"
    )

    val hazardColors = getHazardThemeColors(hazard.hazardType)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Main Hazard Banner Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = if (countdown <= 1) 1.5.dp else 1.dp,
                    color = if (countdown <= 1) hazardColors.primary.copy(alpha = pulseAlpha) else hazardColors.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { showDetailDialog = true },
            color = Color(0xFF141218).copy(alpha = 0.88f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                hazardColors.primary.copy(alpha = 0.22f),
                                hazardColors.secondary.copy(alpha = 0.08f),
                                Color(0xFF0F0E17).copy(alpha = 0.4f)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hazard Icon & Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(hazardColors.primary.copy(alpha = 0.25f))
                                .border(1.dp, hazardColors.primary.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hazard.icon,
                                fontSize = 18.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = hazard.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = hazardColors.primary.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, hazardColors.primary.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = hazard.dangerLevel,
                                        color = hazardColors.primary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = hazard.terrainModifierDesc,
                                color = Color(0xFFB0BEC5),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Countdown Badge with click hint
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (countdown <= 1) hazardColors.primary.copy(alpha = 0.35f) else Color(0xFF21212B),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (countdown <= 1) hazardColors.primary else Color(0xFF424250)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (countdown <= 1) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Pozor",
                                    tint = hazardColors.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "ERUPCE!",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    text = "⏳ ${countdown} kol",
                                    color = hazardColors.secondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trigger Alert Message Banner if active
        AnimatedVisibility(
            visible = !lastTriggerMessage.isNullOrBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                color = hazardColors.primary.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(1.dp, hazardColors.primary.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 12.sp
                    )
                    Text(
                        text = lastTriggerMessage ?: "",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Detail & Tactical Guide Dialog
    if (showDetailDialog) {
        EnvironmentalHazardDialog(
            hazard = hazard,
            currentCountdown = countdown,
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * Tactical Modal explaining environmental hazards, terrain benefits/debuffs, and target rules.
 */
@Composable
fun EnvironmentalHazardDialog(
    hazard: EnvironmentalHazard,
    currentCountdown: Int,
    onDismiss: () -> Unit
) {
    val hazardColors = getHazardThemeColors(hazard.hazardType)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16151F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, hazardColors.primary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = hazard.icon,
                            fontSize = 28.sp
                        )
                        Column {
                            Text(
                                text = hazard.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = hazard.title,
                                color = hazardColors.secondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Zavřít",
                            tint = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF2C2B3A))

                // Description
                Text(
                    text = hazard.description,
                    color = Color(0xFFCFD8DC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Terrain Modifiers Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = hazardColors.primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, hazardColors.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🌐 Pasivní modifikátory bojiště:",
                            color = hazardColors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = hazard.terrainModifierDesc,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }

                // Hazard Impact Details Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF201F2C))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Riziko hrozby:", color = Color.Gray, fontSize = 11.sp)
                        Text(text = hazard.dangerLevel, color = hazardColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Perioda erupce:", color = Color.Gray, fontSize = 11.sp)
                        Text(text = "Každých ${hazard.triggerIntervalTurns} kola (Zbývá: $currentCountdown)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Zasažené cíle:", color = Color.Gray, fontSize = 11.sp)
                        val targetDesc = when (hazard.targetRule) {
                            HazardTargetRule.RANDOM_ANY_COMBATANT -> "Náhodná jednotka (spojenec i nepřítel)"
                            HazardTargetRule.RANDOM_ALLY_ONLY -> "Pouze tvoji spojenci"
                            HazardTargetRule.RANDOM_ENEMY_ONLY -> "Pouze nepřátelé"
                            HazardTargetRule.LOWEST_HP_COMBATANT -> "Jednotka s nejnižším HP"
                            HazardTargetRule.ALL_COMBATANTS -> "Všichni bojovníci v aréně"
                        }
                        Text(text = targetDesc, color = Color(0xFFFFCC80), fontSize = 11.sp)
                    }
                    hazard.statusEffectToApply?.let { status ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Aplikovaný efekt:", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = "${status.icon} ${status.name} (${status.durationTurns} kola)",
                                color = Color(0xFF80DEEA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tactical Advice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B221E),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF4CAF50).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Taktika",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Taktický tip: Využij příznivých elementárních útoků odpovídajících prostředí k maximalizaci poškození!",
                            color = Color(0xFFA5D6A7),
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = hazardColors.primary)
                ) {
                    Text(text = "Rozumím", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Ambient background floating particles for environmental hazards (embers, spores, ice crystals, sparks).
 */
@Composable
fun EnvironmentalHazardParticles(
    hazardType: HazardType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hazard_particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_loop"
    )

    val colors = getHazardThemeColors(hazardType)

    // Pre-seed pseudo random points
    val particleOffsets = remember {
        List(16) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3f + 1.5f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        particleOffsets.forEach { (initX, initY, radius) ->
            val curY = ((initY - progress * 0.4f) % 1f + 1f) % 1f
            val curX = (initX + kotlin.math.sin((progress + initX) * 6.28f) * 0.05f) % 1f

            val alpha = when {
                curY < 0.15f -> curY / 0.15f
                curY > 0.85f -> (1f - curY) / 0.15f
                else -> 0.7f
            }

            drawCircle(
                color = colors.primary.copy(alpha = alpha * 0.45f),
                radius = radius,
                center = Offset(curX * w, curY * h)
            )
        }
    }
}

data class HazardColorTheme(
    val primary: Color,
    val secondary: Color
)

fun getHazardThemeColors(type: HazardType): HazardColorTheme {
    return when (type) {
        HazardType.LAVA_ERUPTION -> HazardColorTheme(
            primary = Color(0xFFFF5722),
            secondary = Color(0xFFFFAB40)
        )
        HazardType.TOXIC_MIASMA -> HazardColorTheme(
            primary = Color(0xFF8E24AA),
            secondary = Color(0xFF00E676)
        )
        HazardType.THUNDER_SURGE -> HazardColorTheme(
            primary = Color(0xFFFFD600),
            secondary = Color(0xFF40C4FF)
        )
        HazardType.FROSTBITE_TEMPEST -> HazardColorTheme(
            primary = Color(0xFF00E5FF),
            secondary = Color(0xFF80D8FF)
        )
        HazardType.HOLY_RADIANCE -> HazardColorTheme(
            primary = Color(0xFFFFD54F),
            secondary = Color(0xFFFFF59D)
        )
        HazardType.SHADOW_ABYSS -> HazardColorTheme(
            primary = Color(0xFF7C4DFF),
            secondary = Color(0xFFE040FB)
        )
    }
}
