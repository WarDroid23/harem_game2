package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.Player

@Composable
fun GameTopBar(
    player: Player,
    onRestClick: () -> Unit,
    onQuickSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Pulsing color for warning/low resources
    val infiniteTransition = rememberInfiniteTransition(label = "top_bar_pulse")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Player Title, Gold, Quick Save, Rest & Expand Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Part: Level Badge, Name and Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Lvl ${player.level}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }

                    Column {
                        var displayName = player.name
                        if (player.activeTitle != null) {
                            val tObj = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == player.activeTitle }
                            if (tObj != null) displayName = "${tObj.badgeIcon} " + displayName
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Detaily",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        // XP Mini Progress Line below name
                        val xpProgress = (player.xp.toFloat() / player.xpNext.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Den ${player.day} • ${player.cityTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            LinearProgressIndicator(
                                progress = { xpProgress },
                                modifier = Modifier
                                    .width(45.dp)
                                    .height(3.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // Right Part: Resources, Rest & Menu Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Gold badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF261D0F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Zlato",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${player.gold}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Quick Save Button with subtle glow
                    FilledTonalIconButton(
                        onClick = onQuickSaveClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Rychlé uložení",
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Next Day Rest Button
                    Button(
                        onClick = onRestClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Nový den",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Nový den", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Row 2: Standard Stat Meters (HP, SE, TE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatMeter(
                    title = "HP",
                    current = player.hp,
                    max = player.maxHp,
                    color = Color(0xFFEF5350),
                    icon = Icons.Default.Favorite,
                    warningAlpha = if (player.hp <= player.maxHp * 0.25f) warningAlpha else 0f,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                )
                StatMeter(
                    title = "SE",
                    current = player.sexEnergy,
                    max = player.maxSexEnergy,
                    color = Color(0xFFEC407A),
                    icon = Icons.Default.FlashOn,
                    warningAlpha = if (player.sexEnergy <= 3) warningAlpha else 0f,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                )
                StatMeter(
                    title = "TE",
                    current = player.darkEnergy,
                    max = player.maxDarkEnergy,
                    color = Color(0xFFAB47BC),
                    icon = Icons.Default.AutoAwesome,
                    warningAlpha = if (player.darkEnergy <= 2) warningAlpha else 0f,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                )
            }

            // Dropdown / Expandable Detailed RPG Energy & Stats Panel
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 Přehled energií a stavu pána",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Zkušenosti: ${player.xp} / ${player.xpNext} XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                        // Stats Grid Breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row A: HP & SE explanation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DetailedStatItem(
                                    title = "❤️ Životní síla (HP)",
                                    desc = "Určuje tvou odolnost v boji. Pokud klesne na nulu, utrpíš porážku a budeš muset odpočívat.",
                                    regenText = "Obnova: Odpočinkem nebo elixíry v inventáři.",
                                    modifier = Modifier.weight(1f)
                                )
                                DetailedStatItem(
                                    title = "⚡ Sexuální Energie (SE)",
                                    desc = "Hlavní platidlo pro intimní interakce s dívkami v komnatách, expedice a klasické i rychlé cestování.",
                                    regenText = "Obnova: Plně se regeneruje restem (Novým dnem).",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row B: TE & Prestige
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DetailedStatItem(
                                    title = "🔮 Temná Energie (TE)",
                                    desc = "Používá se pro rituály stínů, hypnózu, trénování poslušnosti v harému a magické rituály.",
                                    regenText = "Obnova: Pomalu regeneruje časem nebo z darů.",
                                    modifier = Modifier.weight(1f)
                                )
                                DetailedStatItem(
                                    title = "👑 Prestiž a Sláva",
                                    desc = "Aktuální prestiž pána: ${player.prestige} ⭐\nCelkem vyhraných bitev: ${player.battlesWon} ⚔️",
                                    regenText = "Vliv: Vyšší prestiž odemyká vzácná privilegia a tituly.",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Close Panel Button
                        TextButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Zavřít přehled", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatMeter(
    title: String,
    current: Int,
    max: Int,
    color: Color,
    icon: ImageVector,
    warningAlpha: Float,
    modifier: Modifier = Modifier
) {
    val progress = (current.toFloat() / max.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (warningAlpha > 0f) {
                    color.copy(alpha = 0.08f + (warningAlpha * 0.12f))
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
            )
            .border(
                width = 1.dp,
                color = if (warningAlpha > 0f) color.copy(alpha = warningAlpha) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$title $current/$max",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = color,
            trackColor = color.copy(alpha = 0.1f),
        )
    }
}

@Composable
fun DetailedStatItem(
    title: String,
    desc: String,
    regenText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 12.sp
            )
            Text(
                text = regenText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 12.sp
            )
        }
    }
}
