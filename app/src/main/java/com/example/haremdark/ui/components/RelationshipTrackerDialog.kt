package com.example.haremdark.ui.components

import android.widget.Toast
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.RelationshipDialogueCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.*

/**
 * Interactive Affection & Relationship Tracker Dialog.
 * Displays character bond stages, unlocks unique dialogue paths, and tracks interaction history.
 */
@Composable
fun RelationshipTrackerDialog(
    character: Character,
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Narrative Paths & Dialogues, 1: Interaction History

    // Active conversation state if player initiated a dialogue path
    var activeConversationPath by remember { mutableStateOf<CharacterNarrativePath?>(null) }
    var selectedDialogueOption by remember { mutableStateOf<DialogueChoiceOption?>(null) }

    val currentAffection = character.attributes.affection
    val activeStage = RelationshipStage.fromAffection(currentAffection)
    val stageColor = Color(activeStage.colorHex)
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)

    val narrativePaths = remember(character, currentAffection) {
        RelationshipDialogueCatalog.getPathsForCharacter(character)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .heightIn(max = 680.dp)
                    .testTag("relationship_tracker_dialog"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF150A1E),
                border = BorderStroke(
                    width = 1.6.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            stageColor,
                            Color(0xFF8E24AA).copy(alpha = 0.6f),
                            Color(0xFF311B92)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // --- 1. HEADER: CHARACTER PROFILE & STAGE GAUGE ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .border(BorderStroke(1.2.dp, stageColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(portraitRes)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = character.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    error = { Text(character.statusIcon, fontSize = 20.sp) }
                                )
                            }

                            Column {
                                Text(
                                    text = character.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Vztah & Osobní Pouto (${character.role.take(18)})",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.LightGray)
                        }
                    }

                    // --- 2. RELATIONSHIP STAGE CARD & AFFECTION GAUGE ---
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1028)),
                        border = BorderStroke(1.dp, stageColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(activeStage.icon, fontSize = 16.sp)
                                    Text(
                                        text = "Fáze ${activeStage.stageRank}: ${activeStage.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = stageColor
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = stageColor.copy(alpha = 0.2f),
                                    border = BorderStroke(0.8.dp, stageColor)
                                ) {
                                    Text(
                                        text = "Náklonnost: $currentAffection / 100",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = stageColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = activeStage.description,
                                fontSize = 10.sp,
                                color = Color.LightGray.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )

                            // Progress Bar to next stage
                            val progressFloat = (currentAffection.toFloat() / 100f).coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                LinearProgressIndicator(
                                    progress = { progressFloat },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = stageColor,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }

                    // --- 3. CONVERSATION DIALOGUE PLAYER (IF ACTIVE) ---
                    if (activeConversationPath != null) {
                        val path = activeConversationPath!!

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF220E32)),
                            border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(path.icon, fontSize = 16.sp)
                                        Text(
                                            text = path.title,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = Color(0xFFFFD700)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            activeConversationPath = null
                                            selectedDialogueOption = null
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Zavřít rozhovor", tint = Color.LightGray)
                                    }
                                }

                                // Character prompt box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF100718),
                                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "💭 ${path.characterInitialThought}",
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                        Text(
                                            text = "\"${path.promptDialogue}\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                if (selectedDialogueOption == null) {
                                    Text(
                                        text = "💬 Jak odpovíš?",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD54F)
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        path.options.forEach { opt ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF190A24),
                                                border = BorderStroke(1.dp, Color(opt.tendency.colorHex).copy(alpha = 0.6f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        HapticManager.vibrateHeavy()
                                                        selectedDialogueOption = opt

                                                        // Apply deltas to character
                                                        character.attributes.affection = (character.attributes.affection + opt.affectionDelta).coerceIn(0, 150)
                                                        character.poslusnost = (character.poslusnost + opt.obedienceDelta).coerceIn(0, 100)
                                                        character.loajalita = (character.loajalita + opt.loyaltyDelta).coerceIn(0, 100)
                                                        character.touha = (character.touha + opt.desireDelta).coerceIn(0, 100)

                                                        // Add to interaction logs
                                                        character.interactionLogs.add(
                                                            InteractionLogEntry(
                                                                day = engine.gameState.value.player.day,
                                                                type = "rozhovor",
                                                                title = path.title,
                                                                description = opt.playerText,
                                                                statChanges = "+${opt.affectionDelta} Náklonnost"
                                                            )
                                                        )

                                                        engine.addLog("💖 Rozhovor s ${character.name}: ${opt.statBonusDescription ?: ""}")
                                                        engine.autoSave()
                                                    }
                                                    .testTag("dialogue_choice_${opt.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(opt.tendency.icon, fontSize = 14.sp)
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = opt.playerText,
                                                            fontSize = 11.sp,
                                                            color = Color.White,
                                                            lineHeight = 14.sp
                                                        )
                                                        if (opt.statBonusDescription != null) {
                                                            Text(
                                                                text = "Důsledek: ${opt.statBonusDescription}",
                                                                fontSize = 9.sp,
                                                                color = Color(opt.tendency.colorHex),
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Response view
                                    val opt = selectedDialogueOption!!
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF100718),
                                        border = BorderStroke(1.dp, Color(0xFF69F0AE).copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
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
                                                Text(
                                                    text = "${character.name} reaguje (${opt.responseEmotion}):",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF69F0AE)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF1B5E20).copy(alpha = 0.6f)
                                                ) {
                                                    Text(
                                                        text = "+${opt.affectionDelta} Náklonnost",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF69F0AE),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = opt.responseText,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                lineHeight = 16.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )

                                            if (path.perkRewardTitle != null) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF7B1FA2).copy(alpha = 0.3f),
                                                    border = BorderStroke(0.8.dp, Color(0xFFFFD700)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text("⭐", fontSize = 12.sp)
                                                        Column {
                                                            Text(
                                                                text = "Odemčen perk: ${path.perkRewardTitle}",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFFFD700)
                                                            )
                                                            Text(
                                                                text = path.perkRewardDescription ?: "",
                                                                fontSize = 9.sp,
                                                                color = Color.LightGray
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    activeConversationPath = null
                                                    selectedDialogueOption = null
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Text("Dokončit rozhovor", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFFD700))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 4. TABS: NARRATIVE PATHS VS INTERACTION HISTORY ---
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF190C24),
                        contentColor = Color(0xFFFFD700),
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = (selectedTab == 0),
                            onClick = { selectedTab = 0 },
                            text = { Text("📖 Osobní Dialogy (${narrativePaths.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = (selectedTab == 1),
                            onClick = { selectedTab = 1 },
                            text = { Text("📜 Kronika Interakcí (${character.interactionLogs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (selectedTab == 0) {
                        // --- UNLOCKABLE DIALOGUES & NARRATIVE PATHS ---
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            narrativePaths.forEach { path ->
                                val isUnlocked = (currentAffection >= path.requiredStage.minAffection)
                                val pathColor = Color(path.requiredStage.colorHex)

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isUnlocked) Color(0xFF1C0D26) else Color(0xFF110716),
                                    border = BorderStroke(
                                        width = if (isUnlocked) 1.2.dp else 0.5.dp,
                                        color = if (isUnlocked) pathColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = isUnlocked) {
                                            HapticManager.vibrateClick()
                                            activeConversationPath = path
                                            selectedDialogueOption = null
                                        }
                                        .testTag("narrative_path_${path.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isUnlocked) pathColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
                                            border = BorderStroke(1.dp, if (isUnlocked) pathColor else Color.Gray),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(if (isUnlocked) path.icon else "🔒", fontSize = 18.sp)
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = path.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isUnlocked) Color.White else Color.Gray
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isUnlocked) pathColor.copy(alpha = 0.25f) else Color.DarkGray
                                                ) {
                                                    Text(
                                                        text = if (isUnlocked) "ODEMČENO" else "Od ${path.requiredStage.minAffection} Náklonnosti",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isUnlocked) pathColor else Color.LightGray,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = path.synopsis,
                                                fontSize = 10.sp,
                                                color = if (isUnlocked) Color.LightGray else Color.DarkGray,
                                                lineHeight = 13.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (isUnlocked) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Zahájit rozhovor",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // --- INTERACTION & EVENT CHRONICLE ---
                        val logs = character.getSafeInteractionLogs(engine.gameState.value.player.day)

                        if (logs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Zatím žádné záznamy o interakcích.", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                logs.takeLast(10).reversed().forEach { log ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF170C20),
                                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Den ${log.day} • ${log.title} (${log.type})",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF80AB)
                                                )
                                                Text(
                                                    text = log.description,
                                                    fontSize = 9.sp,
                                                    color = Color.LightGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (log.statChanges.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF2E7D32).copy(alpha = 0.5f)
                                                ) {
                                                    Text(
                                                        text = log.statChanges,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF69F0AE),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
