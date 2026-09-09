package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.haremdark.data.PartyCombatCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.CombatRole
import com.example.haremdark.models.GameSave

@Composable
fun PartySelectionDialog(
    gameState: GameSave,
    preselectedEncounter: PartyCombatCatalog.PartyEncounterDefinition?,
    onDismiss: () -> Unit,
    onStartCombat: (selectedGirlIds: List<String>, includePlayer: Boolean, encounter: PartyCombatCatalog.PartyEncounterDefinition) -> Unit
) {
    var selectedEncounter by remember {
        mutableStateOf(preselectedEncounter ?: PartyCombatCatalog.ENCOUNTERS.first())
    }

    // Default select up to 3 healthiest girls
    val availableGirls = remember(gameState.characters) {
        gameState.characters.filter { it.hp > 0 }
    }

    var selectedGirls by remember {
        mutableStateOf(
            availableGirls.sortedByDescending { (it.skills["combat"] ?: 0) + it.affinityPoints }
                .take(3)
                .map { it.id }
                .toList()
        )
    }

    var includePlayer by remember { mutableStateOf(true) }

    val maxGirlsAllowed = if (includePlayer) 3 else 4

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF160D1E),
            border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "⚔️ Sestavení bojové družiny",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            "Vyber hrdinky z harému (${selectedGirls.size}/$maxGirlsAllowed)",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.Gray)
                    }
                }

                // --- 1. ENCOUNTER SELECTOR CAROUSEL ---
                Text("Vyber arénový střet / výpravu:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PartyCombatCatalog.ENCOUNTERS) { encounter ->
                        val isSelected = (encounter.id == selectedEncounter.id)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF4A1435) else Color(0xFF22132A),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.clickable { selectedEncounter = encounter }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(encounter.icon, fontSize = 16.sp)
                                Column {
                                    Text(encounter.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isSelected) Color(0xFFFFD700) else Color.White, maxLines = 1)
                                    Text(encounter.tierName, fontSize = 9.sp, color = Color(0xFFB0BEC5))
                                }
                            }
                        }
                    }
                }

                // --- 2. PLAYER INCLUSION TOGGLE ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (includePlayer) Color(0xFF2E1B3E) else Color(0xFF1E1424)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👑", fontSize = 20.sp)
                            Column {
                                Text("Pán Dominia (Velitel)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Vstoupí osobně do boje s čepelí dominia", fontSize = 10.sp, color = Color(0xFFB39DDB))
                            }
                        }

                        Switch(
                            checked = includePlayer,
                            onCheckedChange = { checked ->
                                includePlayer = checked
                                if (checked && selectedGirls.size > 3) {
                                    selectedGirls = selectedGirls.take(3).toList()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700), checkedTrackColor = Color(0xFF7B1FA2))
                        )
                    }
                }

                // --- SLOT-BASED FORMATION UI ---
                Text("Formace družiny (Pozice poskytují bonusy):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                val slotBonuses = listOf(
                    "Vanguard" to "+15% HP & Obrana",
                    "Flank" to "+10% Útok & Rychlost",
                    "Rearguard" to "+15% Crit & Mana",
                    "Support" to "+5 Rychlost & Obrana"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until maxGirlsAllowed) {
                        val girlId = selectedGirls.getOrNull(i)
                        val char = girlId?.let { id -> availableGirls.find { it.id == id } }
                        val bonusInfo = slotBonuses.getOrNull(if (includePlayer) i + 1 else i) ?: ("Slot ${i+1}" to "")
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).clickable {
                                if (girlId != null) {
                                    selectedGirls = selectedGirls.filter { it != girlId }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (char != null) Color(0xFFFF4081) else Color.Gray, RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2E1B3E)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (char != null) {
                                    val portraitRes = StaticData.getPortraitForArchetype(char.archetype)
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(portraitRes).crossfade(true).build(),
                                        contentDescription = char.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Prázdné", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(bonusInfo.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            Text(bonusInfo.second, fontSize = 8.sp, color = Color(0xFFB39DDB), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // --- 3. ROSTER OF HAREM GIRLS ---
                Text("Dostupné společnice v dominiu:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)

                if (availableGirls.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Všechny tvé dívky jsou zraněné! Ošetři je v komnatách.", color = Color(0xFFFF8A80), fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableGirls) { char ->
                            val isSelected = selectedGirls.contains(char.id)
                            val role = PartyCombatCatalog.getRoleForArchetype(char.archetype)
                            val portraitRes = StaticData.getPortraitForArchetype(char.archetype)
                            val affinityTier = AffinityData.getTierForPoints(char.affinityPoints)
                            val combatSkill = char.skills["combat"] ?: 5

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedGirls = selectedGirls - char.id
                                        } else {
                                            if (selectedGirls.size < maxGirlsAllowed) {
                                                selectedGirls = selectedGirls + char.id
                                            }
                                        }
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF3B152A) else Color(0xFF1E1224)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(portraitRes)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = char.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(char.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF311B92)
                                                ) {
                                                    Text(
                                                        text = "${role.icon} ${role.title}",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFFB388FF),
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("HP: ${char.hp}/${char.maxHp}", fontSize = 10.sp, color = Color(0xFF81C784))
                                                Text("Útok: $combatSkill", fontSize = 10.sp, color = Color(0xFFFFB74D))
                                                Text("💖 ${affinityTier.title}", fontSize = 10.sp, color = Color(0xFFFF80AB))
                                            }
                                        }
                                    }

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked && selectedGirls.size < maxGirlsAllowed) {
                                                selectedGirls = selectedGirls + char.id
                                            } else if (!checked) {
                                                selectedGirls = selectedGirls - char.id
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFFF4081),
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 4. START COMBAT ACTION BUTTON ---
                val canStart = selectedGirls.isNotEmpty() || includePlayer
                Button(
                    onClick = {
                        onStartCombat(selectedGirls.toList(), includePlayer, selectedEncounter)
                    },
                    enabled = canStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC2185B),
                        disabledContainerColor = Color(0xFF4A182E)
                    )
                ) {
                    Icon(Icons.Default.SportsKabaddi, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VYSLAT DRUŽINU DO BOJE (${if (includePlayer) selectedGirls.size + 1 else selectedGirls.size} bojovníků)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = if (canStart) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}
