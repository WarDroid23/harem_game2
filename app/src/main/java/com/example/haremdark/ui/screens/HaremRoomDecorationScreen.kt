package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaremRoomDecorationScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = gameState.player
    val characters = gameState.characters
    val rooms = engine.getHaremRooms()

    var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.id ?: "room_1") }
    var selectedSlotTab by remember { mutableStateOf<RoomDecorationSlot?>(null) } // null = Vše
    var showAssignModal by remember { mutableStateOf(false) }

    val currentRoom = rooms.find { it.id == selectedRoomId } ?: rooms.first()
    val assignedGirl = characters.find { it.id == currentRoom.assignedCharacterId }

    val availableDecorations = remember(selectedSlotTab) {
        if (selectedSlotTab == null) {
            RoomDecorationCatalog.ALL_DECORATIONS
        } else {
            RoomDecorationCatalog.ALL_DECORATIONS.filter { it.slot == selectedSlotTab }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🛏️ Komnaty Harému & Dekorace",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Zařizuj soukromé ložnice pro své dívky a získej trvalé pasivní bonusy",
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
            // --- TOP ROOM SELECTOR CAROUSEL ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E0E32),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)) {
                    Text(
                        "👑 Vyber komnatu k úpravě:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(rooms) { room ->
                            val isSelected = room.id == selectedRoomId
                            val girl = characters.find { it.id == room.assignedCharacterId }

                            Card(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable {
                                        selectedRoomId = room.id
                                        HapticManager.vibrateClick()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF4A148C) else Color(0xFF26123D)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFFFD700) else Color(0xFF381E52)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(room.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            room.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "✨ Komfort: ${room.totalComfort}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF81D4FA)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (girl != null) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color(0xFF424242).copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (girl != null) Color(0xFF81C784) else Color.Gray)
                                    ) {
                                        Text(
                                            text = if (girl != null) "💃 ${girl.name}" else " Prázdná",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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

            // --- MAIN ROOM DETAILS & DECORATION AREA ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Room Header Banner & Active Girl Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B2E)),
                        border = BorderStroke(1.dp, Color(0xFF9C27B0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(currentRoom.icon, fontSize = 36.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        currentRoom.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Téma: ${currentRoom.themeName} • Komfort: ${currentRoom.totalComfort} bodů",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF381E52))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Assigned Harem Member Banner
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF381E52),
                                    border = BorderStroke(1.dp, Color(0xFFFFC107)),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (assignedGirl != null) "💃" else "🕯️", fontSize = 22.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    if (assignedGirl != null) {
                                        Text(
                                            "Ubytována: ${assignedGirl.name}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "Nálada: Šťastná & Hýčkaná v luxusu • Náklonnost: ${assignedGirl.srdce}%",
                                            fontSize = 11.sp,
                                            color = Color(0xFF81C784)
                                        )
                                    } else {
                                        Text(
                                            "Komnata nemá ubytovanou dívku",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.LightGray
                                        )
                                        Text(
                                            "Ubytuj členku harému a aktivuj jí pasivní bojové bonusy!",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        showAssignModal = true
                                        HapticManager.vibrateClick()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                                ) {
                                    Text(if (assignedGirl != null) "Změnit" else "Ubytovat", fontSize = 11.sp)
                                }
                            }

                            // Passive Stat Bonuses Summary
                            val bonus = currentRoom.getCumulativeStatBonus()
                            if (bonus.attackBonus > 0 || bonus.defenseBonus > 0 || bonus.hpBonus > 0 || bonus.moraleBonusPerDay > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF231038),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            "⚡ Pasivní bonusy pro ubytovanou dívku:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (bonus.attackBonus > 0) Text("⚔️ +${bonus.attackBonus} Útok", fontSize = 11.sp, color = Color.White)
                                            if (bonus.defenseBonus > 0) Text("🛡️ +${bonus.defenseBonus} Obrana", fontSize = 11.sp, color = Color.White)
                                            if (bonus.hpBonus > 0) Text("❤️ +${bonus.hpBonus} HP", fontSize = 11.sp, color = Color.White)
                                            if (bonus.moraleBonusPerDay > 0) Text("💖 +${bonus.moraleBonusPerDay} Morálka/den", fontSize = 11.sp, color = Color(0xFFE91E63))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Installed Room Furniture Slots Preview
                item {
                    Text(
                        "🛋️ Instalované Vybavení & Dekory:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoomDecorationSlot.entries.forEach { slot ->
                            val installedItem = currentRoom.installedDecorations[slot.name]

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0E32)),
                                border = BorderStroke(1.dp, if (installedItem != null) Color(0xFFAB47BC) else Color(0xFF381E52))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2A153E),
                                        border = BorderStroke(1.dp, if (installedItem != null) Color(0xFFFFD700) else Color.Gray),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(installedItem?.icon ?: slot.icon, fontSize = 22.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            slot.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Gray
                                        )
                                        if (installedItem != null) {
                                            Text(
                                                installedItem.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                "Komfort: +${installedItem.comfortPoints} • ${installedItem.description}",
                                                fontSize = 10.sp,
                                                color = Color.LightGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                "Prázdný slot — Vyber dekoraci níže",
                                                fontSize = 12.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Decoration Shop & Upgrade Options Title
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "🛍️ Obchod s Nábytkem & Dekoracemi:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }

                // Slot Filter Tabs
                item {
                    ScrollableTabRow(
                        selectedTabIndex = if (selectedSlotTab == null) 0 else selectedSlotTab!!.ordinal + 1,
                        containerColor = Color(0xFF160A24),
                        contentColor = Color(0xFFFFD700),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedSlotTab == null,
                            onClick = {
                                selectedSlotTab = null
                                HapticManager.vibrateClick()
                            },
                            text = { Text("Vše", fontSize = 11.sp) }
                        )
                        RoomDecorationSlot.entries.forEachIndexed { idx, slot ->
                            Tab(
                                selected = selectedSlotTab == slot,
                                onClick = {
                                    selectedSlotTab = slot
                                    HapticManager.vibrateClick()
                                },
                                text = { Text("${slot.icon} ${slot.title.split(" ").first()}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Furniture Catalog List
                items(availableDecorations) { item ->
                    val isAlreadyInstalled = currentRoom.installedDecorations[item.slot.name]?.id == item.id
                    val canAffordGold = player.gold >= item.goldCost
                    val canAffordDark = player.darkEnergy >= item.darkEnergyCost
                    val canAffordMaterials = item.materialCosts.all { (m, req) -> (player.craftingResources[m] ?: 0) >= req }
                    val canBuy = canAffordGold && canAffordDark && canAffordMaterials

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0E32)),
                        border = BorderStroke(1.dp, if (isAlreadyInstalled) Color(0xFF4CAF50) else Color(0xFF381E52))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF2A153E),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(item.icon, fontSize = 24.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF8E24AA).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFF8E24AA))
                                        ) {
                                            Text(
                                                "Úroveň ${item.level}",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                fontSize = 9.sp,
                                                color = Color(0xFFE1BEE7)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(item.description, fontSize = 11.sp, color = Color.LightGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Comfort & Stat Bonuses Preview
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨ +${item.comfortPoints} Komfort", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                if (item.attackBonus > 0) Text("⚔️ +${item.attackBonus} Útok", fontSize = 11.sp, color = Color(0xFF81D4FA))
                                if (item.defenseBonus > 0) Text("🛡️ +${item.defenseBonus} Obrana", fontSize = 11.sp, color = Color(0xFF81D4FA))
                                if (item.hpBonus > 0) Text("❤️ +${item.hpBonus} HP", fontSize = 11.sp, color = Color(0xFF81D4FA))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Cost breakdown
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💰 ${item.goldCost} Zlata", fontSize = 11.sp, color = if (canAffordGold) Color.White else Color(0xFFE57373))
                                Text("🔮 ${item.darkEnergyCost} TE", fontSize = 11.sp, color = if (canAffordDark) Color.White else Color(0xFFE57373))
                                item.materialCosts.forEach { (m, req) ->
                                    val curr = player.craftingResources[m] ?: 0
                                    Text("$m: $curr/$req", fontSize = 10.sp, color = if (curr >= req) Color.Gray else Color(0xFFE57373))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val (success, msg) = engine.installDecorationToRoom(currentRoom.id, item.id)
                                    if (success) {
                                        SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                                        HapticManager.vibrateHeavy()
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                enabled = canBuy && !isAlreadyInstalled,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAlreadyInstalled) Color(0xFF2E7D32) else Color(0xFF8E24AA),
                                    disabledContainerColor = Color(0xFF381E52)
                                )
                            ) {
                                Text(
                                    if (isAlreadyInstalled) "✅ Nainstalováno" else "🛋️ Koupit & Nainstalovat do komnaty",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MODAL: CHARACTER ASSIGNMENT ---
    if (showAssignModal) {
        AlertDialog(
            onDismissRequest = { showAssignModal = false },
            title = {
                Text("👤 Ubytování v komnatě '${currentRoom.name}'", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Vyber dívku, kterou ubytuješ v této luxusní komnatě. Dívka získá pasivní bojové bonusy z nainstalovaných dekorací.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (characters.isEmpty()) {
                        Text("Nemáš v harému žádné aktivní dívky.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                // Option to unassign
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            engine.assignCharacterToRoom(currentRoom.id, null)
                                            showAssignModal = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A153E))
                                ) {
                                    Text("🚫 Nechat komnatu prázdnou", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            items(characters) { girl ->
                                val assignedRoom = rooms.find { it.assignedCharacterId == girl.id }
                                val isCurrent = girl.id == currentRoom.assignedCharacterId

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            engine.assignCharacterToRoom(currentRoom.id, girl.id)
                                            showAssignModal = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrent) Color(0xFF4A148C) else Color(0xFF231038)
                                    ),
                                    border = BorderStroke(1.dp, if (isCurrent) Color(0xFFFFD700) else Color(0xFF6A1B9A))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💃", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(girl.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text(
                                                if (assignedRoom != null) "Aktuálně v: ${assignedRoom.name}" else "Volná k ubytování",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        if (isCurrent) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFFFFD700))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAssignModal = false }) {
                    Text("Zavřít", color = Color(0xFFFFD700))
                }
            },
            containerColor = Color(0xFF1B0B2E)
        )
    }
}
