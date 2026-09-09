package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DialogueChoice
import com.example.haremdark.data.StaticData
import com.example.haremdark.data.TimeLimitedHaremEvent
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character

@Composable
fun HaremSpecialEventDialog(
    event: TimeLimitedHaremEvent,
    character: Character,
    onDismiss: () -> Unit,
    onChoiceSelected: (DialogueChoice) -> Unit
) {
    var selectedChoice by remember { mutableStateOf<DialogueChoice?>(null) }
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
    val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)
    val tierColor = Color(affinityTier.colorHex)

    LaunchedEffect(event.id) {
        SoundEffectManager.playEventTrigger()
    }

    Dialog(
        onDismissRequest = {
            if (selectedChoice == null) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, tierColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16101E)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Hero Image with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x9916101E),
                                        Color(0xFF16101E)
                                    )
                                )
                            )
                    )

                    // Top Bar controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tierColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(affinityTier.icon, fontSize = 12.sp)
                                Text(
                                    text = "Úr. ${affinityTier.level} • ${affinityTier.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Title & Character Name on bottom of banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${character.name} • ${character.archetypeId}",
                            fontSize = 12.sp,
                            color = Color(0xFFCE93D8)
                        )
                    }
                }

                // Main Dialogue Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedChoice == null) {
                        // STEP 1: Encounter Dialogue
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A148C).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = event.dialogueSequence.prologue,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        // Speech bubble
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF23172E),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, tierColor.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("💋", fontSize = 14.sp)
                                    Text(
                                        text = character.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = tierColor
                                    )
                                }
                                Text(
                                    text = event.dialogueSequence.characterMonologue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        // Choices Header
                        Text(
                            text = "Jak na její slova odpovíš?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )

                        // Dialogue Choices
                        event.dialogueSequence.options.forEach { choice ->
                            val choiceBorderColor = when (choice.style) {
                                "dominant" -> Color(0xFFAB47BC)
                                "romantic" -> Color(0xFFFF4081)
                                else -> Color(0xFFFFD700)
                            }
                            val choiceIcon = when (choice.style) {
                                "dominant" -> "⚡"
                                "romantic" -> "💖"
                                else -> "👑"
                            }
                            val styleBadge = when (choice.style) {
                                "dominant" -> "Pánská dominance"
                                "romantic" -> "Temná vášeň"
                                else -> "Královská štědrost"
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SoundEffectManager.playAffinityGain()
                                        selectedChoice = choice
                                    },
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

                                        Text(
                                            text = "+${choice.affinityGain} Náklonnost",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF4081)
                                        )
                                    }

                                    Text(
                                        text = choice.text,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // STEP 2: Outcome & Emotional Reaction
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
                                        text = "Reakce ${character.name}:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = tierColor
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

                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Text(
                                    text = "🎁 Výsledek setkání:",
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
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tierColor
                            )
                        ) {
                            Text(
                                text = "Zpečetit rituál & Přijmout posílení",
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
