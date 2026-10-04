package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.*

/**
 * Interactive Modal Dialog for Narrative Random Events & Branching Encounters.
 */
@Composable
fun NarrativeEventDialog(
    event: NarrativeEvent,
    playerSkills: Map<String, Int>,
    onSelectChoice: (String) -> Pair<EventOutcome, Boolean>,
    onDismiss: () -> Unit
) {
    var resolvedOutcome by remember { mutableStateOf<Pair<EventOutcome, Boolean>?>(null) }
    val categoryColor = Color(event.category.colorHex)
    val rarityColor = Color(event.rarity.colorHex)

    Dialog(
        onDismissRequest = {
            if (resolvedOutcome != null) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 680.dp)
                    .testTag("narrative_event_dialog"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF150A1C),
                border = BorderStroke(
                    width = 1.8.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            rarityColor,
                            categoryColor.copy(alpha = 0.6f),
                            Color(0xFF4A148C)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (resolvedOutcome == null) {
                        // --- 1. EVENT HEADER ---
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
                                    color = categoryColor.copy(alpha = 0.2f),
                                    border = BorderStroke(1.2.dp, categoryColor),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(event.icon, fontSize = 22.sp)
                                    }
                                }

                                Column {
                                    Text(
                                        text = event.title,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📍 ${event.locationTag}",
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                        Text("•", fontSize = 10.sp, color = Color.Gray)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = rarityColor.copy(alpha = 0.25f),
                                            border = BorderStroke(0.5.dp, rarityColor)
                                        ) {
                                            Text(
                                                text = event.rarity.title,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = rarityColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- 2. NARRATIVE STORY BOX ---
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F0614),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = event.narrativeStory,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFE2DCE8)
                                )

                                if (event.involvedCompanionName != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("💬", fontSize = 12.sp)
                                        Text(
                                            text = "Družina: ${event.involvedCompanionName} sleduje situaci.",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFF80AB)
                                        )
                                    }
                                }
                            }
                        }

                        // --- 3. BRANCHING CHOICES ---
                        Text(
                            text = "🎲 Jak se zachováš?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            event.choices.forEach { choice ->
                                NarrativeChoiceCard(
                                    choice = choice,
                                    playerSkills = playerSkills,
                                    onClick = {
                                        HapticManager.vibrateHeavy()
                                        val result = onSelectChoice(choice.id)
                                        resolvedOutcome = result
                                    }
                                )
                            }
                        }
                    } else {
                        // --- RESOLVED OUTCOME VIEW ---
                        val (outcome, isSuccess) = resolvedOutcome!!

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSuccess) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFFB71C1C).copy(alpha = 0.4f),
                                border = BorderStroke(1.5.dp, if (isSuccess) Color(0xFF69F0AE) else Color(0xFFFF5252)),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(if (isSuccess) "🏆" else "⚠️", fontSize = 28.sp)
                                }
                            }

                            Text(
                                text = outcome.outcomeTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSuccess) Color(0xFFFFD700) else Color(0xFFFF8A80),
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F0614),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = outcome.outcomeNarrative,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            // Rewards & Consequences Cluster
                            Text(
                                text = "🎁 Získané Odměny a Důsledky",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (outcome.goldDelta != 0) {
                                        OutcomeRewardPill(
                                            label = if (outcome.goldDelta > 0) "+${outcome.goldDelta} Zlato" else "${outcome.goldDelta} Zlato",
                                            icon = "💰",
                                            color = Color(0xFFFFD700)
                                        )
                                    }
                                    if (outcome.influenceDelta != 0) {
                                        OutcomeRewardPill(
                                            label = if (outcome.influenceDelta > 0) "+${outcome.influenceDelta} Vliv" else "${outcome.influenceDelta} Vliv",
                                            icon = "👑",
                                            color = Color(0xFFFF4081)
                                        )
                                    }
                                    if (outcome.darkEnergyDelta != 0) {
                                        OutcomeRewardPill(
                                            label = "+${outcome.darkEnergyDelta} Temná Energie",
                                            icon = "🔮",
                                            color = Color(0xFFE040FB)
                                        )
                                    }
                                    if (outcome.itemReward != null) {
                                        OutcomeRewardPill(
                                            label = "Předmět: ${outcome.itemReward.name}",
                                            icon = outcome.itemReward.icon,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                    if (outcome.recruitedGirlName != null) {
                                        OutcomeRewardPill(
                                            label = "Nová dívka: ${outcome.recruitedGirlName}!",
                                            icon = "💖",
                                            color = Color(0xFFFF80AB)
                                        )
                                    }
                                    if (outcome.playerXp > 0 || outcome.haremExp > 0) {
                                        OutcomeRewardPill(
                                            label = "+${outcome.playerXp} XP • +${outcome.haremExp} Harém EXP",
                                            icon = "⭐",
                                            color = Color(0xFF69F0AE)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    HapticManager.vibrateClick()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dismiss_event_outcome_button")
                            ) {
                                Text(
                                    "Pokračovat v cestě",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NarrativeChoiceCard(
    choice: NarrativeEventChoice,
    playerSkills: Map<String, Int>,
    onClick: () -> Unit
) {
    val skillCheck = choice.skillCheck
    val hasSkillCheck = (skillCheck != null)
    val currentSkillLvl = if (hasSkillCheck) playerSkills[skillCheck!!.skillKey] ?: 0 else 0
    val estimatedSuccessPercent = if (hasSkillCheck) {
        (skillCheck!!.baseSuccessPercent + (currentSkillLvl * 10)).coerceIn(10, 95)
    } else 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("event_choice_${choice.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1026)),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(choice.icon, fontSize = 16.sp)
                    Text(
                        text = choice.choiceText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (hasSkillCheck) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (estimatedSuccessPercent >= 75) Color(0xFF2E7D32).copy(alpha = 0.6f) else Color(0xFFE65100).copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${skillCheck!!.skillDisplayName} ($estimatedSuccessPercent%)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = choice.description,
                fontSize = 10.sp,
                color = Color.LightGray.copy(alpha = 0.85f),
                lineHeight = 13.sp
            )

            // Costs badge if any
            if (choice.costGold > 0 || choice.costDarkEnergy > 0 || choice.costSexEnergy > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (choice.costGold > 0) {
                        Text("💰 Cena: ${choice.costGold} zlata", fontSize = 9.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                    }
                    if (choice.costDarkEnergy > 0) {
                        Text("🔮 Cena: ${choice.costDarkEnergy} TE", fontSize = 9.sp, color = Color(0xFFE040FB), fontWeight = FontWeight.Bold)
                    }
                    if (choice.costSexEnergy > 0) {
                        Text("💖 Cena: ${choice.costSexEnergy} SE", fontSize = 9.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeRewardPill(label: String, icon: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 11.sp)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
