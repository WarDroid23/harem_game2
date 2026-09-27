package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.models.Character
import com.example.haremdark.models.EquipmentLoadout
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearLoadoutManagementScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val characters = gameState.characters
    val allLoadouts = engine.getAllLoadouts()

    var selectedCharacterId by remember { mutableStateOf<String?>(characters.firstOrNull()?.id) }
    val selectedCharacter = remember(selectedCharacterId, characters) {
        characters.firstOrNull { it.id == selectedCharacterId } ?: characters.firstOrNull()
    }

    var showCreateLoadoutModal by remember { mutableStateOf(false) }
    var newLoadoutName by remember { mutableStateOf("") }
    var newLoadoutTag by remember { mutableStateOf("CUSTOM") }
    var newLoadoutIcon by remember { mutableStateOf("⚔️") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🛡️ Správa Bojových Loadoutů",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Rychlé přepínání výbavy pro taktickou adaptaci v boji",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateLoadoutModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Uložit nový loadout", tint = Color(0xFFFFD700))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160A24)
                )
            )
        },
        containerColor = Color(0xFF0F0618)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- CHARACTER SELECTOR BAR ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E0E32),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "👤 Vyber dívku pro konfiguraci výbavy:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            val isTeamSelected = selectedCharacterId == null
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTeamSelected) Color(0xFF8E24AA) else Color(0xFF2B1545),
                                border = BorderStroke(1.dp, if (isTeamSelected) Color(0xFFFFD700) else Color(0xFF4A148C)),
                                modifier = Modifier.clickable {
                                    selectedCharacterId = null
                                    HapticManager.vibrateClick()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                    Text("Celý Tým", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        items(characters) { char ->
                            val isSelected = selectedCharacterId == char.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF8E24AA) else Color(0xFF2B1545),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF4A148C)),
                                modifier = Modifier.clickable {
                                    selectedCharacterId = char.id
                                    HapticManager.vibrateClick()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("💃", fontSize = 16.sp)
                                    Column {
                                        Text(char.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Lvl ${char.level}", fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- CURRENT EQUIPPED SUMMARY FOR SELECTED CHARACTER ---
            if (selectedCharacter != null) {
                val weapon = selectedCharacter.equipment["weapon"]
                val armor = selectedCharacter.equipment["armor"]
                val accessory = selectedCharacter.equipment["accessory"]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF25103A)),
                    border = BorderStroke(1.dp, Color(0xFFBA68C8))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⚔️ Aktuální výbava: ${selectedCharacter.name}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = {
                                    newLoadoutName = "Set pro ${selectedCharacter.name}"
                                    showCreateLoadoutModal = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Uložit výbavu", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EquipmentSlotSummary("🗡️ Zbraň", weapon?.name ?: "Základní", Color(0xFFFFCC80), Modifier.weight(1f))
                            EquipmentSlotSummary("🛡️ Zbroj", armor?.name ?: "Žádná", Color(0xFF90CAF9), Modifier.weight(1f))
                            EquipmentSlotSummary("💍 Doplněk", accessory?.name ?: "Žádný", Color(0xFFCE93D8), Modifier.weight(1f))
                        }
                    }
                }
            }

            // --- TACTICAL ADVICE BANNER ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B2E))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Bojový tip: Prořídni bossy s vysokým armorem pomocí setu BLEED, nebo zvol TANK pro přežití těžkých úderů.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- SAVED LOADOUTS LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allLoadouts, key = { it.id }) { loadout ->
                    val isActive = loadout.id == gameState.activeLoadoutId

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFF2E1C2B) else Color(0xFF1E0E32)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isActive) Color(0xFFFF80AB) else Color(0xFF381E52)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(loadout.icon, fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = loadout.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isActive) Color(0xFFFFD700) else Color.White
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF9C27B0).copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = "Tag: ${loadout.situationTag}",
                                                color = Color(0xFFFF80AB),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "✅ AKTIVNÍ",
                                            color = Color(0xFF81C784),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = loadout.description,
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Loadout Equipment Slots Breakdown
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF140822),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("🗡️ Zbraň:", fontSize = 11.sp, color = Color.Gray)
                                        Text(loadout.weaponItem?.name ?: "Základní", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCC80))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("🛡️ Zbroj:", fontSize = 11.sp, color = Color.Gray)
                                        Text(loadout.armorItem?.name ?: "Žádná", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90CAF9))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("💍 Doplněk:", fontSize = 11.sp, color = Color.Gray)
                                        Text(loadout.accessoryItem?.name ?: "Žádný", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8))
                                    }
                                }
                            }

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!loadout.isDefaultPreset) {
                                    TextButton(
                                        onClick = {
                                            val removed = engine.deleteCustomLoadout(loadout.id)
                                            if (removed) {
                                                HapticManager.vibrateClick()
                                                Toast.makeText(context, "Set '${loadout.name}' smazán.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Smazat", fontSize = 12.sp, color = Color(0xFFE57373))
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Button(
                                    onClick = {
                                        val targetId = selectedCharacterId
                                        val (success, msg) = engine.applyLoadout(loadout.id, targetId)
                                        if (success) {
                                            SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                                            HapticManager.vibrateClick()
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (selectedCharacter != null) "Aktivovat pro ${selectedCharacter.name}" else "Aktivovat pro Tým",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MODAL: CREATE CUSTOM LOADOUT ---
    if (showCreateLoadoutModal) {
        AlertDialog(
            onDismissRequest = { showCreateLoadoutModal = false },
            title = {
                Text("✏️ Uložit nový loadout", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Uloží aktuálně nasazenou výbavu ${selectedCharacter?.name ?: "hráče"} jako nový znovupoužitelný bojový set.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    OutlinedTextField(
                        value = newLoadoutName,
                        onValueChange = { newLoadoutName = it },
                        label = { Text("Název setu") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFAB47BC),
                            unfocusedBorderColor = Color(0xFF4A148C),
                            focusedLabelColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Bojový tag / Situace:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tags = listOf("DPS", "TANK", "MAGIC", "BLEED", "CUSTOM")
                        tags.forEach { tag ->
                            val isSel = newLoadoutTag == tag
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF8E24AA) else Color(0xFF2B1545),
                                modifier = Modifier.clickable { newLoadoutTag = tag }
                            ) {
                                Text(
                                    tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLoadoutName.isNotBlank()) {
                            engine.saveCurrentLoadout(
                                name = newLoadoutName,
                                situationTag = newLoadoutTag,
                                icon = newLoadoutIcon,
                                characterId = selectedCharacterId
                            )
                            SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                            HapticManager.vibrateClick()
                            Toast.makeText(context, "Loadout '$newLoadoutName' byl uložen!", Toast.LENGTH_SHORT).show()
                            showCreateLoadoutModal = false
                            newLoadoutName = ""
                        } else {
                            Toast.makeText(context, "Zadej název loadoutu!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) {
                    Text("Uložit set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateLoadoutModal = false }) {
                    Text("Zrušit", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1B0B2E)
        )
    }
}

@Composable
private fun EquipmentSlotSummary(label: String, itemName: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A0A2C),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(itemName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
