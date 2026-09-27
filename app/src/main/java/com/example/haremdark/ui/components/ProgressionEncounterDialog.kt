package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.ProgressionEncounter
import com.example.haremdark.domain.ProgressionEncounterChoice
import com.example.haremdark.domain.SoundEffectManager

@Composable
fun ProgressionEncounterDialog(
    encounter: ProgressionEncounter,
    onDismiss: () -> Unit,
    onChoiceSelected: (ProgressionEncounterChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChoice by remember { mutableStateOf<ProgressionEncounterChoice?>(null) }

    var scale by remember { mutableStateOf(0.85f) }
    var alpha by remember { mutableStateOf(0f) }

    LaunchedEffect(encounter.id) {
        SoundEffectManager.playEventTrigger()
    }

    LaunchedEffect(Unit) {
        androidx.compose.animation.core.animate(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 350f)
        ) { value, _ ->
            scale = value
        }
    }

    LaunchedEffect(Unit) {
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
        ) { value, _ ->
            alpha = value
        }
    }

    val categoryColor = Color(encounter.category.badgeColorHex)

    Dialog(
        onDismissRequest = {
            if (selectedChoice == null) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha
                )
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, categoryColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .testTag("progression_encounter_dialog"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16101E)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = categoryColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(encounter.category.icon, fontSize = 14.sp)
                            Text(
                                text = encounter.category.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Zavřít",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Title
                Text(
                    text = encounter.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Prologue Setting Description
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A148C).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = encounter.prologue,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (selectedChoice == null) {
                    // STEP 1: Monologue & Interactive Choices
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF23172E),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(encounter.speakerIcon, fontSize = 16.sp)
                                Text(
                                    text = encounter.speakerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = categoryColor
                                )
                            }
                            Text(
                                text = encounter.monologue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Text(
                        text = "Jak rozhodneš o tomto setkání?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )

                    // Choices List
                    encounter.choices.forEach { choice ->
                        val choiceBorderColor = when (choice.style) {
                            "dominant" -> Color(0xFFAB47BC)
                            "romantic" -> Color(0xFFFF4081)
                            "passionate" -> Color(0xFFFF7043)
                            "strategic" -> Color(0xFF00BCD4)
                            else -> Color(0xFFFFD700)
                        }
                        val choiceIcon = when (choice.style) {
                            "dominant" -> "⚡"
                            "romantic" -> "💖"
                            "passionate" -> "🔥"
                            "strategic" -> "⚗️"
                            else -> "👑"
                        }
                        val styleBadge = when (choice.style) {
                            "dominant" -> "Pánská dominance"
                            "romantic" -> "Temná vášeň"
                            "passionate" -> "Plamenná nenasytnost"
                            "strategic" -> "Strategická volba"
                            else -> "Královské rozhodnutí"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundEffectManager.playAffinityGain()
                                    selectedChoice = choice
                                }
                                .testTag("choice_${choice.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, choiceBorderColor.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = choiceBorderColor.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(choiceIcon, fontSize = 10.sp)
                                            Text(
                                                text = styleBadge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = choiceBorderColor
                                            )
                                        }
                                    }

                                    if (choice.affinityGain > 0) {
                                        Text(
                                            text = "+${choice.affinityGain} Náklonnost",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF4081)
                                        )
                                    }
                                }

                                Text(
                                    text = choice.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )

                                Text(
                                    text = "Efekt: ${choice.outcomeSummary}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFE082)
                                )
                            }
                        }
                    }
                } else {
                    // STEP 2: Outcome & Reaction Summary
                    val choice = selectedChoice!!

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF23172E),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF66BB6A))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("✨", fontSize = 16.sp)
                                Text(
                                    text = "Reakce na tvé rozhodnutí:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = categoryColor
                                )
                            }

                            Text(
                                text = choice.reactionText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 20.sp
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Text(
                                text = "🎁 Získané odměny & Dopad na panství:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF120B1B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = choice.outcomeSummary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFE082),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            SoundEffectManager.playLevelUp()
                            onChoiceSelected(choice)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_confirm_encounter_choice"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = categoryColor
                        )
                    ) {
                        Text(
                            text = "Přijmout výsledek & Pokračovat",
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
