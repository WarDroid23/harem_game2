package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.R
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Boss
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.CombatSession
import com.example.haremdark.models.GameSave

@Composable
fun TurnBasedCombatModule(
    gameState: GameSave,
    session: CombatSession?,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    if (session != null) {
        ActiveCombatView(
            gameState = gameState,
            session = session,
            engine = engine,
            modifier = modifier
        )
    } else {
        EnemyRosterView(
            gameState = gameState,
            engine = engine,
            modifier = modifier
        )
    }
}

@Composable
fun ActiveCombatView(
    gameState: GameSave,
    session: CombatSession,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedActionCategory by remember { mutableIntStateOf(0) }
    var selectedLogFilter by remember { mutableStateOf("all") }
    var showFullHistoryModal by remember { mutableStateOf(false) }
    var selectedStatusTooltip by remember { mutableStateOf<String?>(null) }

    val player = gameState.player
    val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 85.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Combat Arena Mini-Banner & Turn Tracker
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(65.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_arena_battle),
                    contentDescription = "Bojová aréna",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xDD150B13), Color(0x992B1015), Color(0xEE150B13))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD32F2F)
                        ) {
                            Text(
                                text = "KOLO ${session.turnCount}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Full Log Modal Quick Button
                        IconButton(
                            onClick = { showFullHistoryModal = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "Historie",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showFullHistoryModal = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log (${session.logEntries.size})", fontSize = 11.sp)
                        }

                        if (!session.isOver) {
                            OutlinedButton(
                                onClick = { engine.executeCombatTurn("flee") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80))
                            ) {
                                Text("Ústup", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Duel Showcase: Enemy Card vs Player Card with STATUS EFFECT BADGES NEXT TO HP BARS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- ENEMY CARD ---
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1517))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = session.boss.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFFCDD2),
                        maxLines = 1
                    )
                    Text(
                        text = "Fáze: ${session.boss.phaseName}",
                        fontSize = 10.sp,
                        color = Color(0xFFE57373),
                        maxLines = 1
                    )

                    // Enemy HP Bar
                    val bossProgress = (session.bossHp.toFloat() / session.bossMaxHp.toFloat()).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("HP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                            Text("${session.bossHp}/${session.bossMaxHp}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                        }
                        LinearProgressIndicator(
                            progress = { bossProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFE53935),
                            trackColor = Color(0xFF532223)
                        )
                    }

                    // --- ENEMY STATUS EFFECT ICONS NEXT TO HEALTH ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (session.enemyBleedTurns > 0) {
                            StatusEffectBadge(
                                icon = "🩸",
                                label = "Krvácení (${session.enemyBleedTurns})",
                                color = Color(0xFFB71C1C),
                                onClick = { selectedStatusTooltip = "🩸 Krvácení: Způsobuje zranění na začátku každého kola (zbývá ${session.enemyBleedTurns} kol)." }
                            )
                        }
                        if (session.enemyStunned) {
                            StatusEffectBadge(
                                icon = "💫",
                                label = "Omráčen",
                                color = Color(0xFFF57F17),
                                onClick = { selectedStatusTooltip = "💫 Omráčení: Protivník vynechává své další kolo útoků." }
                            )
                        }
                        if (session.activeBuff?.contains("Prokletí") == true) {
                            StatusEffectBadge(
                                icon = "👁️",
                                label = "Prokletí",
                                color = Color(0xFF4A148C),
                                onClick = { selectedStatusTooltip = "👁️ Prokletí stínů: Protivník je oslaben a uděluje o 25% menší poškození." }
                            )
                        }
                        if (session.turnCount % 3 == 0) {
                            StatusEffectBadge(
                                icon = "⚡",
                                label = "Zuřivost",
                                color = Color(0xFFFF6F00),
                                onClick = { selectedStatusTooltip = "⚡ Speciální technika: Protivník nyní připravuje zničující fázový útok!" }
                            )
                        }
                        if (session.enemyBleedTurns == 0 && !session.enemyStunned && session.activeBuff?.contains("Prokletí") != true && session.turnCount % 3 != 0) {
                            Text("Normální stav", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- PLAYER CARD ---
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "${player.name} (Pán)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFC8E6C9),
                        maxLines = 1
                    )
                    Text(
                        text = "Zbraň: ${weapon?.name ?: "Pěsti"}",
                        fontSize = 10.sp,
                        color = Color(0xFF81C784),
                        maxLines = 1
                    )

                    // Player HP Bar
                    val playerProgress = (session.playerHp.toFloat() / session.playerMaxHp.toFloat()).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("HP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Text("${session.playerHp}/${session.playerMaxHp}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                        LinearProgressIndicator(
                            progress = { playerProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF43A047),
                            trackColor = Color(0xFF1B3B26)
                        )
                    }

                    // --- PLAYER STATUS EFFECT ICONS NEXT TO HEALTH ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (session.isDefending) {
                            StatusEffectBadge(
                                icon = "🛡️",
                                label = "Štít -65%",
                                color = Color(0xFF1565C0),
                                onClick = { selectedStatusTooltip = "🛡️ Obranný postoj: Utržené poškození je v tomto kole sníženo o 65% a regeneruje se TE." }
                            )
                        }
                        if (session.activeBuff?.contains("Požehnání") == true) {
                            StatusEffectBadge(
                                icon = "💖",
                                label = "Požehnání",
                                color = Color(0xFFAD1457),
                                onClick = { selectedStatusTooltip = "💖 Požehnání harému: Oblíbenkyně z harému ti poskytuje psychickou sílu a regeneraci." }
                            )
                        }
                        if (player.darkEnergy >= 20) {
                            StatusEffectBadge(
                                icon = "🔮",
                                label = "Rezonance",
                                color = Color(0xFF7B1FA2),
                                onClick = { selectedStatusTooltip = "🔮 Temná rezonance: Tvá stínová magie je plně nabitá pro sesílání mocných kouzel." }
                            )
                        }
                        if (session.playerHp <= (session.playerMaxHp * 0.3f)) {
                            StatusEffectBadge(
                                icon = "⚠️",
                                label = "Kritický",
                                color = Color(0xFFC62828),
                                onClick = { selectedStatusTooltip = "⚠️ Kritický stav: Tvé HP kleslo pod 30%! Použij balzám nebo vysátí duše." }
                            )
                        }
                        if (!session.isDefending && session.activeBuff?.contains("Požehnání") != true && player.darkEnergy < 20 && session.playerHp > (session.playerMaxHp * 0.3f)) {
                            Text("Bojová připravenost", fontSize = 9.sp, color = Color(0xFF81C784))
                        }
                    }
                }
            }
        }

        // Status tooltip dialog
        selectedStatusTooltip?.let { tooltipText ->
            AlertDialog(
                onDismissRequest = { selectedStatusTooltip = null },
                title = { Text("Stavový efekt v souboji", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                text = { Text(tooltipText, fontSize = 13.sp) },
                confirmButton = {
                    TextButton(onClick = { selectedStatusTooltip = null }) {
                        Text("Rozumím")
                    }
                }
            )
        }

        // If Combat Is Over (Victory or Defeat Banner)
        if (session.isOver) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (session.victory) Color(0xFF1B382B) else Color(0xFF3A1A1E)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (session.victory) "🏆 VÍTĚZSTVÍ V SOUBOJI!" else "💀 PORÁŽKA V BOJI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (session.victory) Color(0xFFFFD700) else Color(0xFFEF5350)
                    )
                    if (session.victory && session.lootGained != null) {
                        Text(
                            text = "Získané odměny: ${session.lootGained}",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    } else if (!session.victory) {
                        Text(
                            text = "Byl jsi odnesen zpět do své pevnosti. Odpočiň si a doplň energii.",
                            fontSize = 11.sp,
                            color = Color(0xFFFFCDD2)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showFullHistoryModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Celý log")
                        }

                        Button(
                            onClick = { engine.endCombat() },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (session.victory) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        ) {
                            Text("Zpět do arény", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Action Selection Tabs
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionTabButton("Útoky", Icons.Default.FlashOn, selectedActionCategory == 0) { selectedActionCategory = 0 }
                        ActionTabButton("Temnota", Icons.Default.AutoAwesome, selectedActionCategory == 1) { selectedActionCategory = 1 }
                        ActionTabButton("Obrana", Icons.Default.Shield, selectedActionCategory == 2) { selectedActionCategory = 2 }
                        ActionTabButton("Předměty", Icons.Default.Medication, selectedActionCategory == 3) { selectedActionCategory = 3 }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Actions Content
                    when (selectedActionCategory) {
                        0 -> {
                            // Physical Attacks
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ActionRowButton(
                                    title = "Sek zbraní (${weapon?.name ?: "Zbraň"})",
                                    subtitle = "Přesný úder • Šance na kritický zásah",
                                    icon = Icons.Default.Gavel,
                                    buttonColor = Color(0xFFD32F2F),
                                    onClick = { engine.executeCombatTurn("slash") }
                                )
                                ActionRowButton(
                                    title = "Drtivý těžký úder",
                                    subtitle = "Masivní rozmach za 1.8x poškození • 25% šance na kritický úder",
                                    icon = Icons.Default.Bolt,
                                    buttonColor = Color(0xFFE65100),
                                    onClick = { engine.executeCombatTurn("heavy_strike") }
                                )
                                ActionRowButton(
                                    title = "Krvavé bodnutí",
                                    subtitle = "Otevře krvácející ránu způsobující DoT poškození po 3 kola",
                                    icon = Icons.Default.Bloodtype,
                                    buttonColor = Color(0xFF880E4F),
                                    onClick = { engine.executeCombatTurn("bleed_strike") }
                                )
                            }
                        }
                        1 -> {
                            // Dark Magic Spells
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ActionRowButton(
                                    title = "Temný výboj (10 TE)",
                                    subtitle = "Mocný paprsek stínové energie ignorující obranu",
                                    icon = Icons.Default.AutoAwesome,
                                    buttonColor = Color(0xFF6A1B9A),
                                    enabled = player.darkEnergy >= 10,
                                    onClick = { engine.executeCombatTurn("dark_burst") }
                                )
                                ActionRowButton(
                                    title = "Prokletí stínů (15 TE)",
                                    subtitle = "Uvalí na nepřítele kletbu a oslabí jeho útočnou sílu",
                                    icon = Icons.Default.Visibility,
                                    buttonColor = Color(0xFF4A148C),
                                    enabled = player.darkEnergy >= 15,
                                    onClick = { engine.executeCombatTurn("curse_shadow") }
                                )
                                ActionRowButton(
                                    title = "Vysátí duše (20 TE)",
                                    subtitle = "Vysaje životní sílu cíle a uzdraví pána o 75% poškození",
                                    icon = Icons.Default.Favorite,
                                    buttonColor = Color(0xFF311B92),
                                    enabled = player.darkEnergy >= 20,
                                    onClick = { engine.executeCombatTurn("soul_drain") }
                                )
                            }
                        }
                        2 -> {
                            // Defense & Harem Support
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ActionRowButton(
                                    title = "Obranný postoj & Odražení",
                                    subtitle = "Sníží utržené poškození o 65% v tomto kole a doplní +8 TE",
                                    icon = Icons.Default.Shield,
                                    buttonColor = Color(0xFF1565C0),
                                    onClick = { engine.executeCombatTurn("defend") }
                                )
                                val characters = gameState.characters
                                val favorite = characters.firstOrNull { it.oblibena } ?: characters.firstOrNull { it.jeManzelkou } ?: characters.firstOrNull()
                                ActionRowButton(
                                    title = "Podpora harému (${favorite?.name ?: "Žádná"})",
                                    subtitle = "Oblíbenkyně ti dodá duševní sílu (+28 HP, +15 TE)",
                                    icon = Icons.Default.FavoriteBorder,
                                    buttonColor = Color(0xFFAD1457),
                                    enabled = favorite != null,
                                    onClick = { engine.executeCombatTurn("harem_support") }
                                )
                            }
                        }
                        3 -> {
                            // Consumable Items
                            val potionItems = player.items.filter { it.count > 0 }
                            if (potionItems.isEmpty()) {
                                Text(
                                    "V inventáři nemáš žádné použitelné lektvary ani balzámy.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    potionItems.forEach { item ->
                                        ActionRowButton(
                                            title = "${item.name} (${item.count}x)",
                                            subtitle = item.description,
                                            icon = Icons.Default.Medication,
                                            buttonColor = Color(0xFF2E7D32),
                                            onClick = { engine.executeCombatTurn("item", item.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Battle Logs Panel (Recent Entries)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Bojový záznamník",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        LogFilterChip("Vše", selectedLogFilter == "all") { selectedLogFilter = "all" }
                        LogFilterChip("Útoky", selectedLogFilter == "attacks") { selectedLogFilter = "attacks" }
                        LogFilterChip("Kouzla", selectedLogFilter == "spells") { selectedLogFilter = "spells" }

                        IconButton(
                            onClick = { showFullHistoryModal = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Zvětšit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                val filteredLogs = remember(session.logEntries, selectedLogFilter) {
                    when (selectedLogFilter) {
                        "attacks" -> session.logEntries.filter { it.type.contains("attack") }
                        "spells" -> session.logEntries.filter { it.type.contains("spell") || it.type.contains("heal") }
                        else -> session.logEntries
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs) { entry ->
                        CombatLogItem(entry)
                    }
                }
            }
        }
    }

    // --- FULL COMBAT LOG HISTORY MODAL ---
    if (showFullHistoryModal) {
        FullCombatHistoryDialog(
            session = session,
            onDismiss = { showFullHistoryModal = false }
        )
    }
}

@Composable
fun StatusEffectBadge(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.85f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(icon, fontSize = 9.sp)
            Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FullCombatHistoryDialog(
    session: CombatSession,
    onDismiss: () -> Unit
) {
    var modalFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(session.logEntries, modalFilter, searchQuery) {
        session.logEntries.filter { entry ->
            val matchesFilter = when (modalFilter) {
                "player" -> entry.type.startsWith("player_")
                "enemy" -> entry.type.startsWith("enemy_")
                "spells" -> entry.type.contains("spell") || entry.type.contains("heal") || entry.type.contains("support")
                "results" -> entry.type == "victory" || entry.type == "defeat" || entry.type == "system"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else entry.message.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFFFD700))
                        Column {
                            Text("Kompletní historie souboje", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Protivník: ${session.boss.name} • Celkem ${session.logEntries.size} záznamů", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                // Combat Quick Statistics Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCounter("Kola", "${session.turnCount}")
                        StatCounter("Hráč HP", "${session.playerHp}/${session.playerMaxHp}")
                        StatCounter("Boss HP", "${session.bossHp}/${session.bossMaxHp}")
                        StatCounter("Stav", if (session.isOver) (if (session.victory) "Výhra" else "Prohra") else "Probíhá")
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hledat v záznamech (např. zranění, kouzlo)...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModalFilterChip("Vše (${session.logEntries.size})", modalFilter == "all") { modalFilter = "all" }
                    ModalFilterChip("Hráč", modalFilter == "player") { modalFilter = "player" }
                    ModalFilterChip("Nepřítel", modalFilter == "enemy") { modalFilter = "enemy" }
                    ModalFilterChip("Kouzla & Podpora", modalFilter == "spells") { modalFilter = "spells" }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Scrollable List of Logs
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Žádné záznamy neodpovídají hledání.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList) { entry ->
                            DetailedCombatLogCard(entry)
                        }
                    }
                }

                // Footer Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zpět k boji", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCounter(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ModalFilterChip(title: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(title, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun DetailedCombatLogCard(entry: CombatLogEntry) {
    val bgColor = when (entry.type) {
        "player_attack" -> Color(0xFF1A2733)
        "player_spell" -> Color(0xFF2B1B38)
        "player_heal", "player_support" -> Color(0xFF183020)
        "player_defend" -> Color(0xFF192A40)
        "enemy_attack" -> Color(0xFF381B1D)
        "enemy_special" -> Color(0xFF4C181C)
        "victory" -> Color(0xFF283B19)
        "defeat" -> Color(0xFF451518)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val badgeColor = when (entry.type) {
        "player_attack" -> Color(0xFF42A5F5)
        "player_spell" -> Color(0xFFBA68C8)
        "player_heal", "player_support" -> Color(0xFF66BB6A)
        "player_defend" -> Color(0xFF29B6F6)
        "enemy_attack" -> Color(0xFFEF5350)
        "enemy_special" -> Color(0xFFFF5252)
        "victory" -> Color(0xFFFFD700)
        "defeat" -> Color(0xFFFF1744)
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeColor.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "KOLO ${entry.turn}",
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = entry.message,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CombatLogItem(entry: CombatLogEntry) {
    val bgColor = when (entry.type) {
        "player_attack" -> Color(0xFF1E2833)
        "player_spell" -> Color(0xFF281E33)
        "player_heal", "player_support" -> Color(0xFF1B2E20)
        "player_defend" -> Color(0xFF1C273B)
        "enemy_attack" -> Color(0xFF331E1E)
        "enemy_special" -> Color(0xFF45191B)
        "victory" -> Color(0xFF2E3A1A)
        "defeat" -> Color(0xFF421518)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    val badgeColor = when (entry.type) {
        "player_attack" -> Color(0xFF42A5F5)
        "player_spell" -> Color(0xFFBA68C8)
        "player_heal", "player_support" -> Color(0xFF66BB6A)
        "player_defend" -> Color(0xFF29B6F6)
        "enemy_attack" -> Color(0xFFEF5350)
        "enemy_special" -> Color(0xFFFF5252)
        "victory" -> Color(0xFFFFD700)
        "defeat" -> Color(0xFFFF1744)
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Kolo ${entry.turn}:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = entry.message,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun LogFilterChip(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ActionTabButton(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(title, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun ActionRowButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun EnemyRosterView(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedTierFilter by remember { mutableStateOf("all") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_arena_battle),
                            contentDescription = "Aréna dominia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xDD12080D))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text("Aréna & Tahové souboje dominia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Vyzvi na souboj bandity, pašeráky, inkvizitory i arcidémony!", fontSize = 11.sp, color = Color(0xFFFFD700))
                        }
                    }

                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RosterFilterChip("Všichni", selectedTierFilter == "all") { selectedTierFilter = "all" }
                            RosterFilterChip("Běžní", selectedTierFilter == "skirmish") { selectedTierFilter = "skirmish" }
                            RosterFilterChip("Bossové", selectedTierFilter == "boss") { selectedTierFilter = "boss" }
                            RosterFilterChip("Poražení", selectedTierFilter == "defeated") { selectedTierFilter = "defeated" }
                        }
                    }
                }
            }
        }

        val filteredBosses = GameContent.BOSSES.filter { boss ->
            val isDefeated = gameState.defeatedBosses.contains(boss.id)
            when (selectedTierFilter) {
                "skirmish" -> boss.hp <= 120
                "boss" -> boss.hp > 120
                "defeated" -> isDefeated
                else -> true
            }
        }

        items(filteredBosses) { boss ->
            val defeated = gameState.defeatedBosses.contains(boss.id)
            EnemyCard(
                boss = boss,
                isDefeated = defeated,
                onChallenge = { engine.startBossCombat(boss) }
            )
        }
    }
}

@Composable
fun RosterFilterChip(title: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(title, fontSize = 11.sp) },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun EnemyCard(
    boss: Boss,
    isDefeated: Boolean,
    onChallenge: () -> Unit
) {
    val difficultyColor = when {
        boss.hp < 100 -> Color(0xFF4CAF50)
        boss.hp < 180 -> Color(0xFFFFA000)
        boss.hp < 250 -> Color(0xFFE53935)
        else -> Color(0xFF9C27B0)
    }

    val difficultyTitle = when {
        boss.hp < 100 -> "Nízká"
        boss.hp < 180 -> "Střední"
        boss.hp < 250 -> "Vysoká"
        else -> "Smrtící"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDefeated) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(boss.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("📍 ${boss.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }

                if (isDefeated) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF4CAF50).copy(alpha = 0.2f)) {
                        Text(
                            "✓ Poražen",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(6.dp), color = difficultyColor.copy(alpha = 0.2f)) {
                        Text(
                            "Obtížnost: $difficultyTitle",
                            fontSize = 10.sp,
                            color = difficultyColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = boss.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )

            // Stat pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("❤️ ${boss.hp} HP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("⚔️ Útok ${boss.attack}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA726), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("🛡️ Obrana ${boss.defense}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Text(
                "Fáze: ${boss.phaseName} • Odměna: +${boss.rewardGold} zlata, +${boss.rewardXp} XP",
                fontSize = 11.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.SemiBold
            )

            Button(
                onClick = onChallenge,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.SportsMartialArts, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isDefeated) "Vyzvat znovu k tréninku" else "⚔️ Vyzvat na souboj", fontWeight = FontWeight.Bold)
            }
        }
    }
}
