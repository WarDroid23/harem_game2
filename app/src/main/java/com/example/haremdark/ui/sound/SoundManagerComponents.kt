package com.example.haremdark.ui.sound

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.LocationAmbientSound

/**
 * Visual animated audio waveform equalizer bars
 */
@Composable
fun AnimatedEqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    val barScales = (0 until barCount).map { index ->
        val duration = remember(index) { 400 + (index * 120) }
        val targetScale = remember(index) { 0.3f + ((index % 3) * 0.3f) }
        
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = if (isPlaying) 1.0f else 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val maxHeight = size.height
        val barWidth = (totalWidth / (barCount * 2f)).coerceAtLeast(3f)
        val space = (totalWidth - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)

        for (i in 0 until barCount) {
            val scale = if (isPlaying) barScales[i].value else 0.2f
            val currentBarHeight = (maxHeight * scale).coerceAtLeast(4f)
            val left = i * (barWidth + space)
            val top = (maxHeight - currentBarHeight) / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Material 3 Sound Manager Control Card with ambient tracks, volume sliders, and live SFX soundboard.
 */
@Composable
fun SoundManagerControlCard(
    soundManager: ComposeSoundManager = LocalSoundManager.current,
    modifier: Modifier = Modifier
) {
    val isMuted by soundManager.isMuted.collectAsState()
    val isAmbientPlaying by soundManager.isAmbientPlaying.collectAsState()
    val isAmbientLoopEnabled by soundManager.isAmbientLoopEnabled.collectAsState()
    val currentAmbient by soundManager.currentAmbient.collectAsState()
    val bgmVolume by soundManager.bgmVolume.collectAsState()
    val sfxVolume by soundManager.sfxVolume.collectAsState()
    val voiceVolume by soundManager.voiceVolume.collectAsState()
    val lastTriggeredName by soundManager.lastTriggeredName

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Master Mute Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isMuted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Zvuk ztlumen" else "Zvuk zapnut",
                                tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Správce Zvuku (SoundManager)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isMuted) "Veškerý zvuk je ztlumen" else "Atmosféra & efekty aktivní",
                            fontSize = 11.sp,
                            color = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { soundManager.toggleMute() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isMuted) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isMuted) "ZAPNOUT" else "ZTLUMIT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Last Triggered Sound Badge
            AnimatedVisibility(
                visible = lastTriggeredName != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔊 Poslední zvuk:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = lastTriggeredName ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // 1. Ambient Soundtrack Player Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎶 Atmosférické stopy pozadí", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        AnimatedEqualizerBars(
                            isPlaying = !isMuted && (isAmbientPlaying || isAmbientLoopEnabled),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(width = 24.dp, height = 14.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isAmbientLoopEnabled) "Smyčka ZAP" else "Smyčka VYP",
                            fontSize = 10.sp,
                            color = if (isAmbientLoopEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = isAmbientLoopEnabled,
                            onCheckedChange = { soundManager.toggleAmbientLoop() },
                            modifier = Modifier.scale(0.75f)
                        )
                    }
                }

                // Currently Playing Track Banner
                val activeTrack = currentAmbient ?: LocationAmbientSound.DARK_FOREST
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(activeTrack.icon, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = activeTrack.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = activeTrack.weatherTheme,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isAmbientPlaying || isAmbientLoopEnabled) {
                                    soundManager.stopAmbient()
                                } else {
                                    soundManager.playAmbient(activeTrack)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isAmbientPlaying || isAmbientLoopEnabled) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "Přehrát / Zastavit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Ambient Track Carousel Selector
                Text("Vyber lokaci pro přechod:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocationAmbientSound.entries.forEach { track ->
                        val isSelected = currentAmbient == track
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    soundManager.playAmbient(track, crossfade = true)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(track.icon, fontSize = 14.sp)
                                Text(
                                    text = track.title.split(" ").take(2).joinToString(" "),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // 2. Volume Controls Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🎚️ Hlasitost jednotlivých kanálů", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                // BGM Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎵 Hudba v pozadí (BGM)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(bgmVolume * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = bgmVolume,
                        onValueChange = { soundManager.setBgmVolume(it) },
                        valueRange = 0f..1f
                    )
                }

                // SFX Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔔 Boj & Herní efekty (SFX)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(sfxVolume * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = sfxVolume,
                        onValueChange = { soundManager.setSfxVolume(it) },
                        valueRange = 0f..1f
                    )
                }

                // Voice Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🗣️ Hlasy a hlášky společnic", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(voiceVolume * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = voiceVolume,
                        onValueChange = { soundManager.setVoiceVolume(it) },
                        valueRange = 0f..1f
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // 3. Interactive Soundboard for Combat & Event Triggers
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🎮 Interaktivní spouštěče efektů (Soundboard)", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                // Combat Triggers Sub-Row
                Text("⚔️ Bojové triggery:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val combatTriggers = listOf(
                        CombatSoundTrigger.MeleeSlash(isCrit = false),
                        CombatSoundTrigger.MeleeSlash(isCrit = true),
                        CombatSoundTrigger.DarkSpell,
                        CombatSoundTrigger.ShieldBlock,
                        CombatSoundTrigger.CriticalSupernova,
                        CombatSoundTrigger.HealRestore,
                        CombatSoundTrigger.Dodge,
                        CombatSoundTrigger.Victory,
                        CombatSoundTrigger.Defeat
                    )

                    combatTriggers.forEach { trigger ->
                        AssistChip(
                            onClick = { soundManager.playCombatSound(trigger) },
                            label = { Text("${trigger.icon} ${trigger.displayName}", fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Event Triggers Sub-Row
                Text("📜 Události & Příběhové triggery:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val eventTriggers = listOf(
                        EventSoundTrigger.NarrativeSummon,
                        EventSoundTrigger.ChoiceSelected,
                        EventSoundTrigger.AffinityGain,
                        EventSoundTrigger.LevelUp,
                        EventSoundTrigger.RewardGained,
                        EventSoundTrigger.SkillUnlocked,
                        EventSoundTrigger.CraftingSuccess,
                        EventSoundTrigger.CountdownPulse
                    )

                    eventTriggers.forEach { trigger ->
                        AssistChip(
                            onClick = { soundManager.playEventSound(trigger) },
                            label = { Text("${trigger.icon} ${trigger.displayName}", fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog wrapper for SoundManagerControlCard
 */
@Composable
fun SoundManagerDialog(
    onDismissRequest: () -> Unit,
    soundManager: ComposeSoundManager = LocalSoundManager.current
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                SoundManagerControlCard(soundManager = soundManager)
            }
        }
    }
}

/**
 * Compact quick sound toggle badge for top bars or overlays
 */
@Composable
fun SoundQuickToggleBadge(
    soundManager: ComposeSoundManager = LocalSoundManager.current,
    onOpenSoundSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMuted by soundManager.isMuted.collectAsState()
    val isAmbientPlaying by soundManager.isAmbientPlaying.collectAsState()
    val currentAmbient by soundManager.currentAmbient.collectAsState()

    Surface(
        shape = CircleShape,
        color = if (isMuted) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .height(34.dp)
            .clickable {
                if (onOpenSoundSettings != null) {
                    onOpenSoundSettings()
                } else {
                    soundManager.toggleMute()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isMuted) "Zvuk vypnut" else "Zvuk zapnut",
                tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            if (!isMuted && isAmbientPlaying && currentAmbient != null) {
                Text(
                    text = currentAmbient?.icon ?: "🎵",
                    fontSize = 11.sp
                )
            }
        }
    }
}
