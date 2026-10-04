package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.Player
import com.example.haremdark.ui.sound.ComposeSoundManager
import com.example.haremdark.ui.sound.SoundQuickToggleBadge

/**
 * Optimized, streamlined game top bar.
 * Provides a clean, uncluttered, professional dark fantasy header
 * with vital stats, quick actions, and an expandable deep telemetry HUD.
 */
@Composable
fun GameTopBar(
    player: Player,
    onRestClick: () -> Unit,
    onQuickSaveClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    timeOfDayDay: Int? = null,
    soundManager: ComposeSoundManager? = null,
    onOpenSoundSettings: (() -> Unit)? = null,
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Menu Navigation, Player Identity, Key Currencies & Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Group: Drawer Menu Button + Level Badge + Name & Day
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onMenuClick != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onMenuClick()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("topbar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Level Badge & Name (clickable to expand details)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                HapticManager.vibrateClick()
                                isExpanded = !isExpanded
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                        )
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "L${player.level}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }

                        Column {
                            var displayName = player.name
                            if (player.activeTitle != null) {
                                val tObj = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == player.activeTitle }
                                if (tObj != null) displayName = "${tObj.badgeIcon} " + displayName
                            }
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Den ${player.day} • ${player.cityTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Right Group: Gold, Time/Sound Badges, Save, Rest & Expander
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Gold Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF261D0F),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        modifier = Modifier.clickable {
                            HapticManager.vibrateClick()
                            isExpanded = !isExpanded
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Zlato",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${player.gold}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Time of Day mini-badge
                    if (timeOfDayDay != null) {
                        TimeOfDayBadge(
                            currentDay = timeOfDayDay,
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Sound Toggle mini-badge
                    if (soundManager != null) {
                        SoundQuickToggleBadge(
                            soundManager = soundManager,
                            onOpenSoundSettings = onOpenSoundSettings,
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Quick Save Button
                    FilledTonalIconButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            onQuickSaveClick()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("topbar_quick_save")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Rychlé uložení",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Next Day / Rest Button
                    Button(
                        onClick = {
                            HapticManager.vibrateClick()
                            onRestClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("topbar_rest_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Nový den",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Rest", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Toggle Expand Chevron
                    IconButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            isExpanded = !isExpanded
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Podrobnosti přehledu",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Streamlined Vital Meters Strip (HP, SE, TE, ME, INF)
            // Clean, non-intrusive, only ~20dp height!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        HapticManager.vibrateClick()
                        isExpanded = !isExpanded
                    }
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HP Meter
                CompactStatMeter(
                    label = "HP",
                    value = "${player.hp}/${player.maxHp}",
                    progress = (player.hp.toFloat() / player.maxHp.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f),
                    color = Color(0xFFEF5350),
                    icon = Icons.Default.Favorite,
                    warningAlpha = if (player.hp <= player.maxHp * 0.25f) warningAlpha else 0f,
                    modifier = Modifier.weight(1.1f)
                )

                // SE Meter
                CompactStatMeter(
                    label = "SE",
                    value = "${player.sexEnergy}/${player.maxSexEnergy}",
                    progress = (player.sexEnergy.toFloat() / player.maxSexEnergy.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f),
                    color = Color(0xFFEC407A),
                    icon = Icons.Default.FlashOn,
                    warningAlpha = if (player.sexEnergy <= 3) warningAlpha else 0f,
                    modifier = Modifier.weight(1f)
                )

                // TE Meter
                CompactStatMeter(
                    label = "TE",
                    value = "${player.darkEnergy}/${player.maxDarkEnergy}",
                    progress = (player.darkEnergy.toFloat() / player.maxDarkEnergy.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f),
                    color = Color(0xFFAB47BC),
                    icon = Icons.Default.AutoAwesome,
                    warningAlpha = if (player.darkEnergy <= 2) warningAlpha else 0f,
                    modifier = Modifier.weight(1f)
                )

                // Mana Essence (ME) Meter
                CompactStatMeter(
                    label = "ME",
                    value = "${player.manaEssence}",
                    progress = (player.manaEssence % 100).toFloat() / 100f,
                    color = Color(0xFF00E676),
                    icon = Icons.Default.Science,
                    modifier = Modifier.weight(0.9f)
                )

                // Influence (INF) Meter
                CompactStatMeter(
                    label = "INF",
                    value = "${player.influence}/${player.maxInfluence}",
                    progress = (player.influence.toFloat() / player.maxInfluence.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f),
                    color = Color(0xFF03A9F4),
                    icon = Icons.Default.Handshake,
                    modifier = Modifier.weight(1f)
                )
            }

            // Dropdown / Expandable Detailed RPG Energy & Stats Panel
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header info & XP Progress
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
                            val xpProgress = (player.xp.toFloat() / player.xpNext.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                            Text(
                                text = "XP: ${player.xp} / ${player.xpNext} (${(xpProgress * 100).toInt()}%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // XP Progress Bar
                        val xpProg = (player.xp.toFloat() / player.xpNext.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { xpProg },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                        // Active Theme Aura Indicator
                        val currentAura = com.example.haremdark.ui.theme.LocalThemeAura.current
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentAura.accentColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, currentAura.accentColor.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(currentAura.icon, fontSize = 14.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentAura.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentAura.accentColor
                                    )
                                    Text(
                                        text = currentAura.subtitle,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                if (currentAura.isDynamic) {
                                    Text(
                                        text = "AURA",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = currentAura.accentColor
                                    )
                                }
                            }
                        }

                        // Stats Grid Breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Row A: HP & SE explanation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DetailedStatItem(
                                    title = "❤️ Životní síla (HP)",
                                    desc = "Odolnost v boji. Pokud klesne na 0, utrpíš porážku a budeš muset odpočívat.",
                                    regenText = "Obnova: Odpočinkem nebo elixíry v inventáři.",
                                    modifier = Modifier.weight(1f)
                                )
                                DetailedStatItem(
                                    title = "⚡ Sexuální Energie (SE)",
                                    desc = "Platidlo pro intimní interakce s dívkami v komnatách a expedice.",
                                    regenText = "Obnova: Plně se regeneruje restem (Novým dnem).",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row B: TE & Prestige
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DetailedStatItem(
                                    title = "🔮 Temná Energie (TE)",
                                    desc = "Rituály stínů, hypnóza a trénování poslušnosti v harému.",
                                    regenText = "Obnova: Pomalu regeneruje časem nebo z temných rituálů.",
                                    modifier = Modifier.weight(1f)
                                )
                                DetailedStatItem(
                                    title = "👑 Prestiž a Sláva",
                                    desc = "Prestiž pána: ${player.prestige} ⭐ • Vyhraných bitev: ${player.battlesWon} ⚔️",
                                    regenText = "Vliv: Vyšší prestiž odemyká vzácná privilegia.",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row C: Mana Essence & Influence Detailed
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DetailedStatItem(
                                    title = "🧪 Esence Many",
                                    desc = "Magická energie získávaná z bitev a rituálů v Chrámu temnoty.",
                                    regenText = "Využití: Nutná pro pokročilé budovy a rituály.",
                                    modifier = Modifier.weight(1f)
                                )
                                DetailedStatItem(
                                    title = "🤝 Vliv v Dominantě",
                                    desc = "Politický a sociální dosah v podsvětí a mezi otrokyněmi.",
                                    regenText = "Zisk: Získáváš interakcemi a upevňováním moci.",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Close Panel Button
                        TextButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                isExpanded = false
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sbalit přehled", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact, streamlined stat meter for the top bar.
 */
@Composable
fun CompactStatMeter(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    icon: ImageVector,
    warningAlpha: Float = 0f,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (warningAlpha > 0f) {
            color.copy(alpha = 0.12f + (warningAlpha * 0.15f))
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            0.5.dp,
            if (warningAlpha > 0f) color.copy(alpha = warningAlpha) else color.copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = value,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.12f)
            )
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
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 11.sp
            )
            Text(
                text = regenText,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 11.sp
            )
        }
    }
}
