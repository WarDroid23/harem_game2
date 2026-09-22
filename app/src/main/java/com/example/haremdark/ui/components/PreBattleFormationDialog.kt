package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.FormationPosition
import com.example.haremdark.models.GameSave

@Composable
fun PreBattleFormationDialog(
    gameState: GameSave,
    engine: GameEngine,
    selectedCharacterIds: List<String>,
    includePlayer: Boolean,
    onDismiss: () -> Unit,
    onSaveAndProceed: (Map<String, FormationPosition>) -> Unit
) {
    // Local state mapping character ID -> FormationPosition
    val formationMap = remember {
        mutableStateMapOf<String, FormationPosition>().apply {
            if (includePlayer) {
                val initialPlayerPos = gameState.player.partyFormationMap["player"] ?: "MID"
                put("player", FormationPosition.fromString(initialPlayerPos))
            }
            selectedCharacterIds.forEach { id ->
                val char = gameState.characters.find { it.id == id }
                val initialPos = gameState.player.partyFormationMap[id] ?: char?.preferredFormation ?: "MID"
                put(id, FormationPosition.fromString(initialPos))
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF14091F),
            border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "🛡️ Pokročilá bojová formace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            "Přiřaď hrdinky do linií pro taktické statové bonusy",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.Gray)
                    }
                }

                // Auto-Build Button
                OutlinedButton(
                    onClick = {
                        HapticManager.vibrateClick()
                        val maxGirls = if (includePlayer) 3 else 4
                        val sortedChars = gameState.characters
                            .filter { it.hp > 0 }
                            .sortedByDescending { (it.skills["combat"] ?: 5) * 10 + it.level * 5 + it.affinityPoints }
                            .take(maxGirls)

                        formationMap.clear()
                        if (includePlayer) {
                            formationMap["player"] = FormationPosition.MID_LINE
                        }
                        sortedChars.forEach { char ->
                            val role = com.example.haremdark.data.PartyCombatCatalog.getRoleForArchetype(char.archetype)
                            val optimalPos = when (role) {
                                com.example.haremdark.models.CombatRole.TANK_GUARDIAN -> FormationPosition.FRONT_LINE
                                com.example.haremdark.models.CombatRole.DARK_SORCERESS, com.example.haremdark.models.CombatRole.HEALER_PRIESTESS, com.example.haremdark.models.CombatRole.SIREN_DEBUFFER -> FormationPosition.BACK_LINE
                                else -> FormationPosition.MID_LINE
                            }
                            formationMap[char.id] = optimalPos
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ Auto-Build (Optimalizovat sestavu a pozice)", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                // Summary banner of Formation Lines
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FormationPosition.values().forEach { pos ->
                        val count = formationMap.values.count { it == pos }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF261335),
                            border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(pos.icon, fontSize = 16.sp)
                                Text(pos.shortTag, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFFFFD700))
                                Text("$count hrdinek", fontSize = 8.sp, color = Color(0xFFB39DDB))
                            }
                        }
                    }
                }

                // Character Position Assignment List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Player Leader if included
                    if (includePlayer) {
                        item(key = "player_leader") {
                            val currentPos = formationMap["player"] ?: FormationPosition.MID_LINE
                            PositionAssignmentCard(
                                name = "👑 Pán Dominia (Velitel)",
                                roleTitle = "Vládce armády",
                                portraitRes = android.R.drawable.ic_menu_my_calendar, // fallback or custom icon
                                isPlayer = true,
                                currentPosition = currentPos,
                                onPositionChanged = { newPos ->
                                    HapticManager.vibrateClick()
                                    formationMap["player"] = newPos
                                }
                            )
                        }
                    }

                    // Harem Girls
                    items(selectedCharacterIds, key = { it }) { charId ->
                        val char = gameState.characters.find { it.id == charId }
                        if (char != null) {
                            val currentPos = formationMap[charId] ?: FormationPosition.MID_LINE
                            val portraitRes = StaticData.getPortraitForArchetype(char.archetype)
                            val affinityTier = AffinityData.getTierForPoints(char.affinityPoints)

                            PositionAssignmentCard(
                                name = char.name,
                                roleTitle = "${char.archetype} • Lvl ${char.level}",
                                portraitRes = portraitRes,
                                isPlayer = false,
                                affinityColor = Color(affinityTier.colorHex),
                                currentPosition = currentPos,
                                onPositionChanged = { newPos ->
                                    HapticManager.vibrateClick()
                                    formationMap[charId] = newPos
                                }
                            )
                        }
                    }
                }

                // Confirm / Save Button
                Button(
                    onClick = {
                        HapticManager.vibrateClick()
                        onSaveAndProceed(formationMap.toMap())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC2185B)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Potvrdit formaci a zahájit boj", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PositionAssignmentCard(
    name: String,
    roleTitle: String,
    portraitRes: Any,
    isPlayer: Boolean,
    affinityColor: Color = Color(0xFFFFD700),
    currentPosition: FormationPosition,
    onPositionChanged: (FormationPosition) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1029)),
        border = BorderStroke(1.dp, affinityColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, affinityColor, CircleShape)
                    ) {
                        if (isPlayer) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF4A148C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 18.sp)
                            }
                        } else {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(portraitRes)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, maxLines = 1)
                        Text(roleTitle, fontSize = 9.sp, color = Color(0xFFB39DDB), maxLines = 1)
                    }
                }

                // Current Active Position Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF2E153B),
                    border = BorderStroke(1.dp, Color(0xFFFF4081))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(currentPosition.icon, fontSize = 11.sp)
                        Text(currentPosition.shortTag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                }
            }

            // Position Selector Buttons (Front, Mid, Back)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FormationPosition.values().forEach { pos ->
                    val isSelected = (currentPosition == pos)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF6A1B9A) else Color(0xFF261536),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPositionChanged(pos) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(pos.icon, fontSize = 11.sp)
                                Text(
                                    pos.shortTag,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                pos.statSummary.take(28) + "...",
                                fontSize = 7.sp,
                                color = if (isSelected) Color(0xFFE1BEE7) else Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
