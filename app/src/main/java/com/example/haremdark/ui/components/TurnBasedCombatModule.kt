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
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt
import com.example.haremdark.R
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.domain.VoiceTriggerType
import com.example.haremdark.models.Boss
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.CombatSession
import com.example.haremdark.models.CombatStatusEffect
import com.example.haremdark.models.GameSave
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.HapticManager
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

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
    val fxState = rememberCombatVisualFxState()
    val coroutineScope = rememberCoroutineScope()
    val deployedChar = remember(gameState.characters, session.deployedCharacterId) {
        gameState.characters.firstOrNull { it.id == session.deployedCharacterId }
    }

    var selectedActionCategory by remember { mutableIntStateOf(0) }
    var showFullHistoryModal by remember { mutableStateOf(false) }
    var showTacticalOverlayMenu by remember { mutableStateOf(false) }
    var selectedStatusTooltip by remember { mutableStateOf<String?>(null) }
    var selectedStatusForDetailDialog by remember { mutableStateOf<CombatStatusEffect?>(null) }

    val player = gameState.player
    val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 85.dp)
    ) {
        val shakeX = fxState.shakeOffsetX.value
        val shakeY = fxState.shakeOffsetY.value
        val shakeRot = fxState.shakeRotation.value
        val camScale = fxState.cameraScale.value

        // Automatically trigger enhanced screen-shake & particle supernova if a critical hit lands
        LaunchedEffect(session.logEntries.firstOrNull()?.turn, session.logEntries.firstOrNull()?.message) {
            val latest = session.logEntries.firstOrNull() ?: return@LaunchedEffect
            if (latest.message.contains("KRITICKÝ ZÁSAH") || latest.message.contains("KRIT!")) {
                fxState.triggerCriticalExplosion(
                    damageText = "-${latest.damageDealt} HP",
                    scope = coroutineScope
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(camScale)
                .graphicsLayer {
                    rotationZ = shakeRot
                }
                .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) },
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

                        // Active Loadout Badge
                        val currentLoadout = engine.getAllLoadouts().firstOrNull { it.id == gameState.activeLoadoutId }
                            ?: engine.defaultCombatLoadouts.first()
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E1C2B),
                            border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(currentLoadout.icon, fontSize = 11.sp)
                                Text(
                                    text = currentLoadout.situationTag,
                                    color = Color(0xFFFF80AB),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Battle Cry Audio Trigger Button
                        IconButton(
                            onClick = {
                                VoiceManager.playTriggerVoice(VoiceTriggerType.COMBAT_START, deployedChar)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Bojový pokřik",
                                tint = Color(0xFFFF80AB),
                                modifier = Modifier.size(18.dp)
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

                        VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.2f))

                        // Mana Essence Tracker
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔮", fontSize = 12.sp)
                            Column {
                                Text("ESENCE", color = Color.White.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("${player.manaEssence}", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Influence Tracker
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👑", fontSize = 12.sp)
                            Column {
                                Text("VLIV", color = Color.White.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("${player.influence}", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showFullHistoryModal = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log (${session.logEntries.size})", fontSize = 11.sp)
                        }

                        if (!session.isOver) {
                            Button(
                                onClick = {
                                    HapticManager.vibrateClick()
                                    showTacticalOverlayMenu = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                                modifier = Modifier.testTag("arena_tactical_menu_button")
                            ) {
                                Text("🎯 Taktika", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            }

                            OutlinedButton(
                                onClick = {
                                    HapticManager.vibrateClick()
                                    showTacticalOverlayMenu = true
                                },
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

        // Environmental Hazard Banner in 1v1 Arena
        EnvironmentalHazardBanner(
            hazard = session.environmentalHazard,
            countdown = session.hazardCountdown,
            lastTriggerMessage = session.lastHazardTriggerMessage
        )

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

                    // --- ENEMY STATUS EFFECT ICONS NEXT TO HEALTH WITH COUNTDOWNS ---
                    val bossStatusEffects = remember(session.enemyBleedTurns, session.enemyStunned, session.activeBuff, session.turnCount) {
                        buildStatusEffectsFor1v1Boss(session)
                    }
                    if (bossStatusEffects.isNotEmpty()) {
                        CharacterStatusEffectsRow(
                            statusEffects = bossStatusEffects,
                            onSelectEffect = { selectedStatusForDetailDialog = it },
                            maxVisible = 3,
                            compact = true
                        )
                    } else {
                        Text("Normální stav", fontSize = 9.sp, color = Color.Gray)
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
                        text = if (deployedChar != null) "✨ ${deployedChar.name} (${deployedChar.role})" else "${player.name} (Pán)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFC8E6C9),
                        maxLines = 1
                    )
                    Text(
                        text = if (deployedChar != null) "Archetyp: ${deployedChar.archetypeId} • Dívka" else "Zbraň: ${weapon?.name ?: "Pěsti"}",
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

                    // --- PLAYER STATUS EFFECT ICONS NEXT TO HEALTH WITH COUNTDOWNS ---
                    val playerStatusEffects = remember(session.isDefending, session.activeBuff, player.darkEnergy, session.playerHp, session.playerMaxHp) {
                        buildStatusEffectsFor1v1Player(session, player, deployedChar)
                    }
                    if (playerStatusEffects.isNotEmpty()) {
                        CharacterStatusEffectsRow(
                            statusEffects = playerStatusEffects,
                            onSelectEffect = { selectedStatusForDetailDialog = it },
                            maxVisible = 3,
                            compact = true
                        )
                    } else {
                        Text("Bojová připravenost", fontSize = 9.sp, color = Color(0xFF81C784))
                    }
                }
            }
        }

        // Vico Chart for Dual Health Status Monitoring
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1428)),
            border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.25f))
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
                    Text(
                        text = "📊 VICO GRAFICKÉ MONITOROVÁNÍ HP STATUSU",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Kolo ${session.turnCount}",
                        fontSize = 9.sp,
                        color = Color.LightGray
                    )
                }

                val playerHpPercent = if (session.playerMaxHp > 0) (session.playerHp.toFloat() / session.playerMaxHp.toFloat() * 100f).coerceIn(0f, 100f) else 0f
                val enemyHpPercent = if (session.bossMaxHp > 0) (session.bossHp.toFloat() / session.bossMaxHp.toFloat() * 100f).coerceIn(0f, 100f) else 0f

                val playerEntry = FloatEntry(0f, playerHpPercent)
                val enemyEntry = FloatEntry(1f, enemyHpPercent)
                val model = entryModelOf(listOf(playerEntry), listOf(enemyEntry))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFF0C0610), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Chart(
                        chart = columnChart(),
                        model = model,
                        startAxis = rememberStartAxis(title = "% HP"),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                when (value.toInt()) {
                                    0 -> "Můj Stav"
                                    1 -> "Oponent"
                                    else -> ""
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Status effect detail dialog with countdown timer
        val activeSelectedStatus = selectedStatusForDetailDialog
        if (activeSelectedStatus != null) {
            CombatStatusEffectDetailDialog(
                effect = activeSelectedStatus,
                onDismiss = { selectedStatusForDetailDialog = null }
            )
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
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        ActionTabButton("Dovednosti", Icons.Default.AutoFixHigh, selectedActionCategory == 4) { selectedActionCategory = 4 }
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
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.SLASH, "Sek zbraní", coroutineScope) {
                                            engine.executeCombatTurn("slash")
                                        }
                                    }
                                )
                                ActionRowButton(
                                    title = "Drtivý těžký úder",
                                    subtitle = "Masivní rozmach za 1.8x poškození • 25% šance na kritický úder",
                                    icon = Icons.Default.Bolt,
                                    buttonColor = Color(0xFFE65100),
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.HEAVY_STRIKE, "Drtivý těžký úder", coroutineScope) {
                                            engine.executeCombatTurn("heavy_strike")
                                        }
                                    }
                                )
                                ActionRowButton(
                                    title = "Krvavé bodnutí",
                                    subtitle = "Otevře krvácející ránu způsobující DoT poškození po 3 kola",
                                    icon = Icons.Default.Bloodtype,
                                    buttonColor = Color(0xFF880E4F),
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.BLEED_STRIKE, "Krvavé bodnutí", coroutineScope) {
                                            engine.executeCombatTurn("bleed_strike")
                                        }
                                    }
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
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.DARK_BURST, "Temný výboj", coroutineScope) {
                                            engine.executeCombatTurn("dark_burst")
                                        }
                                    }
                                )
                                ActionRowButton(
                                    title = "Prokletí stínů (15 TE)",
                                    subtitle = "Uvalí na nepřítele kletbu a oslabí jeho útočnou sílu",
                                    icon = Icons.Default.Visibility,
                                    buttonColor = Color(0xFF4A148C),
                                    enabled = player.darkEnergy >= 15,
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.SHADOW_CURSE, "Prokletí stínů", coroutineScope) {
                                            engine.executeCombatTurn("curse_shadow")
                                        }
                                    }
                                )
                                ActionRowButton(
                                    title = "Vysátí duše (20 TE)",
                                    subtitle = "Vysaje životní sílu cíle a uzdraví pána o 75% poškození",
                                    icon = Icons.Default.Favorite,
                                    buttonColor = Color(0xFF311B92),
                                    enabled = player.darkEnergy >= 20,
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.SOUL_DRAIN, "Vysátí duše", coroutineScope) {
                                            engine.executeCombatTurn("soul_drain")
                                        }
                                    }
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
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.DEFEND, "Obranný postoj", coroutineScope) {
                                            engine.executeCombatTurn("defend")
                                        }
                                    }
                                )
                                if (deployedChar != null) {
                                    ActionRowButton(
                                        title = "Speciální technika (${deployedChar.name})",
                                        subtitle = "Dívka využije svou bojovou techniku s šancí na omráčení",
                                        icon = Icons.Default.AutoAwesome,
                                        buttonColor = Color(0xFFFF6F00),
                                        onClick = {
                                            fxState.triggerAbility(CombatAbilityType.CHAR_SPECIAL, "Technika: ${deployedChar.name}", coroutineScope) {
                                                engine.executeCombatTurn("char_special")
                                            }
                                        }
                                    )
                                }
                                val characters = gameState.characters
                                val favorite = characters.firstOrNull { it.oblibena } ?: characters.firstOrNull { it.jeManzelkou } ?: characters.firstOrNull()
                                ActionRowButton(
                                    title = "Podpora harému (${favorite?.name ?: "Žádná"})",
                                    subtitle = "Oblíbenkyně ti dodá duševní sílu (+28 HP, +15 TE)",
                                    icon = Icons.Default.FavoriteBorder,
                                    buttonColor = Color(0xFFAD1457),
                                    enabled = favorite != null,
                                    onClick = {
                                        fxState.triggerAbility(CombatAbilityType.HAREM_SUPPORT, "Požehnání: ${favorite?.name ?: "Harém"}", coroutineScope) {
                                            engine.executeCombatTurn("harem_support")
                                        }
                                    }
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
                                            onClick = {
                                                fxState.triggerAbility(CombatAbilityType.ITEM_HEAL, item.name, coroutineScope) {
                                                    engine.executeCombatTurn("item", item.id)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        4 -> {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val stateManager = remember { com.example.haremdark.data.GameStateManager(context) }

                            // Load or initialize default player skills
                            var managerState by remember { mutableStateOf(stateManager.loadState()) }

                            // Initialize default skills if they don't exist yet
                            LaunchedEffect(Unit) {
                                if (managerState.playerSkills.isEmpty()) {
                                    val defaultSkills = mapOf(
                                        "pils_fireball" to com.example.haremdark.data.PlayerSkillState(
                                            id = "pils_fireball",
                                            name = "Ohnivá koule",
                                            description = "Sežehne nepřítele silou pekelných stínů a zapálí ho na 2 kola.",
                                            icon = "🔥",
                                            manaCost = 15,
                                            baseCooldownTurns = 3
                                        ),
                                        "pils_shadow_shield" to com.example.haremdark.data.PlayerSkillState(
                                            id = "pils_shadow_shield",
                                            name = "Stínový štít",
                                            description = "Obklopí tě auru stínů: okamžitě doplní +50 HP a zvýší obranu.",
                                            icon = "🛡️",
                                            manaCost = 10,
                                            baseCooldownTurns = 4
                                        ),
                                        "pils_dark_harvest" to com.example.haremdark.data.PlayerSkillState(
                                            id = "pils_dark_harvest",
                                            name = "Sklizeň duší",
                                            description = "Vysaje život nepřítele a plně tě o tuto hodnotu uzdraví.",
                                            icon = "🌾",
                                            manaCost = 20,
                                            baseCooldownTurns = 5
                                        )
                                    )
                                    val newState = managerState.copy(playerSkills = defaultSkills)
                                    stateManager.saveState(newState)
                                    managerState = newState
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Section A: Harem Companion Skills
                                Text(
                                    text = "✨ AKTIVNÍ DOVEDNOSTI SPROVODKYNĚ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF80AB)
                                )
                                if (deployedChar == null) {
                                    Text(
                                        "Není nasazena žádná dívka, její dovednosti jsou nedostupné.",
                                        fontSize = 11.sp,
                                        color = Color.LightGray.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                } else {
                                    val unlockedSkills = com.example.haremdark.data.CharacterSkillCatalog.getUnlockedActiveSkills(deployedChar)
                                    if (unlockedSkills.isEmpty()) {
                                        Text(
                                            "Tato dívka nemá odemčené žádné aktivní dovednosti.",
                                            fontSize = 11.sp,
                                            color = Color.LightGray.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    } else {
                                        unlockedSkills.forEach { skill ->
                                            val cooldown = session.skillCooldowns[skill.id] ?: 0
                                            val hasMana = deployedChar.mana >= skill.manaCost
                                            val hasEssence = player.manaEssence >= skill.manaEssenceCost

                                            ActionRowButton(
                                                title = "${skill.icon} ${skill.name}" + if (cooldown > 0) " ($cooldown kol)" else "",
                                                subtitle = skill.description + if (skill.manaEssenceCost > 0) "\nNáklady: ${skill.manaEssenceCost} Esence" else "",
                                                icon = if (skill.category == com.example.haremdark.models.SkillCategory.DARK_MAGIC) Icons.Default.AutoAwesome else Icons.Default.Bolt,
                                                buttonColor = when(skill.category) {
                                                    com.example.haremdark.models.SkillCategory.PHYSICAL_ATTACK -> Color(0xFFD32F2F)
                                                    com.example.haremdark.models.SkillCategory.DARK_MAGIC -> Color(0xFF6A1B9A)
                                                    com.example.haremdark.models.SkillCategory.HOLY_HEAL -> Color(0xFF43A047)
                                                    com.example.haremdark.models.SkillCategory.SUPPORT_BUFF -> Color(0xFF1976D2)
                                                    else -> Color(0xFF455A64)
                                                },
                                                enabled = cooldown == 0 && hasMana && hasEssence,
                                                onClick = {
                                                    fxState.triggerAbility(CombatAbilityType.CHAR_SPECIAL, skill.name, coroutineScope) {
                                                        engine.executeCombatTurn("skill", skill.id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                                // Section B: Player skills with persistent cooldowns
                                Text(
                                    text = "🔮 BOJOVÉ UMĚNÍ VLÁDCE (COOLDOWNY)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )

                                managerState.playerSkills.values.forEach { skill ->
                                    // Cooldown turns are persisted inside GameStateManager.skillCooldowns
                                    val currentCooldown = managerState.skillCooldowns[skill.id] ?: 0
                                    val hasTE = player.darkEnergy >= skill.manaCost // TE acts as player mana

                                    ActionRowButton(
                                        title = "${skill.icon} ${skill.name}" + if (currentCooldown > 0) " ($currentCooldown kol)" else "",
                                        subtitle = skill.description + "\nNáklady: ${skill.manaCost} Temné Energie",
                                        icon = Icons.Default.AutoAwesome,
                                        buttonColor = when (skill.id) {
                                            "pils_fireball" -> Color(0xFFE65100)
                                            "pils_shadow_shield" -> Color(0xFF1565C0)
                                            "pils_dark_harvest" -> Color(0xFF4A148C)
                                            else -> Color(0xFF37474F)
                                        },
                                        enabled = currentCooldown == 0 && hasTE && !session.isOver,
                                        onClick = {
                                            // 1. Set the persistent cooldown in GameStateManager
                                            val updatedCooldowns = managerState.skillCooldowns.toMutableMap()
                                            updatedCooldowns[skill.id] = skill.baseCooldownTurns
                                            val newState = managerState.copy(skillCooldowns = updatedCooldowns)
                                            stateManager.saveState(newState)
                                            managerState = newState

                                            // 2. Trigger the custom combat turn
                                            val abilityType = when (skill.id) {
                                                "pils_fireball" -> CombatAbilityType.HEAVY_STRIKE
                                                "pils_shadow_shield" -> CombatAbilityType.DEFEND
                                                "pils_dark_harvest" -> CombatAbilityType.SOUL_DRAIN
                                                else -> CombatAbilityType.SLASH
                                            }
                                            fxState.triggerAbility(abilityType, skill.name, coroutineScope) {
                                                engine.executeCombatTurn("player_skill_${skill.id}")
                                            }
                                        }
                                    )
                                }

                                // Hook into turn ends to reduce player cooldowns!
                                // Each time turn count advances, we decrement persistent cooldowns
                                LaunchedEffect(session.turnCount) {
                                    val updatedCooldowns = managerState.skillCooldowns.mapValues { (_, cooldown) ->
                                        (cooldown - 1).coerceAtLeast(0)
                                    }.filterValues { it > 0 }
                                    val newState = managerState.copy(skillCooldowns = updatedCooldowns)
                                    stateManager.saveState(newState)
                                    managerState = newState
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Combat Log UI Component
        ScrollableCombatLogComponent(
            session = session,
            onOpenFullHistory = { showFullHistoryModal = true },
            modifier = Modifier.weight(1f)
        )
    }

    // Particle effects, impact flashes, and floating ability banner
    CombatVisualFxOverlay(fxState = fxState)
    }

    // --- FULL COMBAT LOG HISTORY MODAL ---
    if (showFullHistoryModal) {
        FullCombatHistoryDialog(
            session = session,
            onDismiss = { showFullHistoryModal = false }
        )
    }

    // --- TACTICAL OVERLAY MENU IN ARENA ---
    if (showTacticalOverlayMenu) {
        ArenaTacticalOverlayMenu(
            bossName = session.boss.name,
            onDismiss = { showTacticalOverlayMenu = false },
            onDefend = {
                showTacticalOverlayMenu = false
                fxState.triggerAbility(CombatAbilityType.DEFEND, "Neprostupný kryt", coroutineScope) {
                    engine.executeCombatTurn("defend")
                }
            },
            onFocusFire = {
                showTacticalOverlayMenu = false
                fxState.triggerAbility(CombatAbilityType.HEAVY_STRIKE, "Soustředěná palba", coroutineScope) {
                    engine.executeCombatTurn("heavy_strike")
                }
            },
            onRetreat = {
                showTacticalOverlayMenu = false
                engine.executeCombatTurn("flee")
            }
        )
    }
}

/**
 * Tactical Overlay Menu for 1v1 Arena Combat.
 * Provides Defend, Focus Fire, and Retreat options.
 */
@Composable
fun ArenaTacticalOverlayMenu(
    bossName: String,
    onDismiss: () -> Unit,
    onDefend: () -> Unit,
    onFocusFire: () -> Unit,
    onRetreat: () -> Unit
) {
    var showRetreatConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 620.dp)
                    .clickable(enabled = false) {}
                    .testTag("arena_tactical_overlay"),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF14081E),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.8f),
                            Color(0xFFFF4081).copy(alpha = 0.5f),
                            Color(0xFF7B1FA2).copy(alpha = 0.8f)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Bar
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
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Taktické Rozkazy",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Soupeř: $bossName",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF80AB),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Tactical Options List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. DEFEND
                        ArenaTacticalCard(
                            title = "Obrana & Kryt (Defend)",
                            subtitle = "Neprostupný kryt a magická bariéra",
                            details = "-65% utrženého zranění v tomto kole a obnova +8 Temné Energie.",
                            icon = Icons.Default.Shield,
                            badgeText = "DEFENZÍVA",
                            badgeColor = Color(0xFF1565C0),
                            accentColor = Color(0xFF42A5F5),
                            testTag = "arena_tactical_defend",
                            onClick = {
                                HapticManager.vibrateClick()
                                onDefend()
                            }
                        )

                        // 2. FOCUS FIRE
                        ArenaTacticalCard(
                            title = "Soustředěná palba (Focus Fire)",
                            subtitle = "Průrazný úder na slabé místo",
                            details = "Koncentrovaný těžký útok za 1.5x až 2.2x poškození s vysokou šancí na kritický zásah.",
                            icon = Icons.Default.GpsFixed,
                            badgeText = "PRIORITNÍ ZÁSAH",
                            badgeColor = Color(0xFFC62828),
                            accentColor = Color(0xFFFF5252),
                            testTag = "arena_tactical_focus_fire",
                            onClick = {
                                HapticManager.vibrateHeavy()
                                onFocusFire()
                            }
                        )

                        // 3. RETREAT
                        ArenaTacticalCard(
                            title = "Taktický ústup (Retreat)",
                            subtitle = "Krycí manévr & bezpečné stažení",
                            details = "Okamžitě opustí bojiště a zachová plné zdraví i získané prostředky.",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            badgeText = "ÚSTUP",
                            badgeColor = Color(0xFF455A64),
                            accentColor = Color(0xFFFF8A80),
                            testTag = "arena_tactical_retreat",
                            onClick = {
                                HapticManager.vibrateClick()
                                showRetreatConfirm = true
                            }
                        )
                    }
                }
            }

            if (showRetreatConfirm) {
                AlertDialog(
                    onDismissRequest = { showRetreatConfirm = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💨", fontSize = 20.sp)
                            Text("Potvrdit ústup z arény?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Text(
                            text = "Opravdu chceš opustit tento souboj a stáhnout se zpět?",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRetreatConfirm = false
                                onRetreat()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            modifier = Modifier.testTag("confirm_arena_retreat_button")
                        ) {
                            Text("Ano, ustoupit", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showRetreatConfirm = false }) {
                            Text("Zůstat v boji")
                        }
                    },
                    containerColor = Color(0xFF1E1022),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ArenaTacticalCard(
    title: String,
    subtitle: String,
    details: String,
    icon: ImageVector,
    badgeText: String,
    badgeColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1228)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = details,
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.85f),
                    lineHeight = 13.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun StatusEffectBadge(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val isBuff = color == Color(0xFF2E7D32) || color == Color(0xFF0288D1) || color == Color(0xFF6A1B9A) ||
        label.contains("buff", ignoreCase = true) || label.contains("štít", ignoreCase = true) || label.contains("posílení", ignoreCase = true)

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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(13.dp)) {
                StatusLottieAura(
                    isBuff = isBuff,
                    modifier = Modifier.fillMaxSize(),
                    glowScale = 1.35f
                )
                Text(icon, fontSize = 9.sp)
            }
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
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFFFFD700))
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
fun ScrollableCombatLogComponent(
    session: CombatSession,
    onOpenFullHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLogFilter by remember { mutableStateOf("all") }
    var narrativeMode by remember { mutableStateOf(true) }
    val isMuted by SoundEffectManager.isMuted.collectAsState()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Title, Sound Mute Button, Mode Switch, Fullscreen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "⚔️ Bojový deník",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Audio Sound Effect Toggle Button
                    IconButton(
                        onClick = { SoundEffectManager.toggleMute() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Zvuk vypnut" else "Zvuk zapnut",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Narrative vs Compact mode switcher
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (narrativeMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (narrativeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { narrativeMode = !narrativeMode }
                    ) {
                        Text(
                            text = if (narrativeMode) "📖 Příběh" else "⚡ Stručně",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (narrativeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenFullHistory,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Zvětšit", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Filters row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogFilterChip("Vše (${session.logEntries.size})", selectedLogFilter == "all") { selectedLogFilter = "all" }
                LogFilterChip("🗡️ Útoky", selectedLogFilter == "attacks") { selectedLogFilter = "attacks" }
                LogFilterChip("🔮 Magie", selectedLogFilter == "spells") { selectedLogFilter = "spells" }
                LogFilterChip("👹 Boss", selectedLogFilter == "enemy") { selectedLogFilter = "enemy" }
            }

            val filteredLogs = remember(session.logEntries, selectedLogFilter) {
                when (selectedLogFilter) {
                    "attacks" -> session.logEntries.filter { it.type.contains("attack") || it.type.contains("special") }
                    "spells" -> session.logEntries.filter { it.type.contains("spell") || it.type.contains("heal") || it.type.contains("support") }
                    "enemy" -> session.logEntries.filter { it.type.startsWith("enemy") }
                    else -> session.logEntries
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredLogs) { entry ->
                    if (narrativeMode) {
                        DetailedCombatLogCard(entry)
                    } else {
                        CombatLogItem(entry)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedCombatLogCard(entry: CombatLogEntry) {
    val tacticalType = remember(entry.type, entry.message, entry.actionName) {
        TacticalAnimationType.fromLogEntry(entry.type, entry.message, entry.actionName)
    }

    val bgColor = when (entry.type) {
        "player_attack" -> Color(0xFF131F2E)
        "player_special" -> Color(0xFF1B2338)
        "player_spell" -> Color(0xFF281836)
        "player_heal", "player_support" -> Color(0xFF152A1C)
        "player_defend" -> Color(0xFF13243A)
        "enemy_attack" -> Color(0xFF2E1517)
        "enemy_special" -> Color(0xFF3D1216)
        "victory" -> Color(0xFF233515)
        "defeat" -> Color(0xFF381215)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val badgeColor = when (entry.type) {
        "player_attack" -> Color(0xFF42A5F5)
        "player_special" -> Color(0xFF64B5F6)
        "player_spell" -> Color(0xFFBA68C8)
        "player_heal", "player_support" -> Color(0xFF66BB6A)
        "player_defend" -> Color(0xFF29B6F6)
        "enemy_attack" -> Color(0xFFEF5350)
        "enemy_special" -> Color(0xFFFF5252)
        "victory" -> Color(0xFFFFD700)
        "defeat" -> Color(0xFFFF1744)
        else -> Color.Gray
    }

    val actorColor = when {
        entry.type.startsWith("enemy") -> Color(0xFFFF8A80)
        entry.type == "player_support" -> Color(0xFFFF80AB)
        entry.type.startsWith("player") -> Color(0xFF80D8FF)
        entry.type == "victory" -> Color(0xFFFFD54F)
        else -> Color.LightGray
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, tacticalType.accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Tactical Micro Lottie Animation Icon
            TacticalLottieMicroBadge(
                animationType = tacticalType,
                sizeDp = 28.dp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header line: Turn pill, Actor & Action, Damage tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "KOLO ${entry.turn}",
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        if (!entry.actor.isNullOrBlank()) {
                            Text(
                                text = entry.actor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = actorColor
                            )
                        }

                        if (!entry.actionName.isNullOrBlank()) {
                            Text(
                                text = "• ${entry.actionName}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    if (entry.damageDealt != null && entry.damageDealt > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (entry.type.startsWith("enemy")) Color(0xFFD32F2F).copy(alpha = 0.3f) else Color(0xFF1976D2).copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "${if (entry.type.startsWith("enemy")) "-" else ""}${entry.damageDealt} DMG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (entry.type.startsWith("enemy")) Color(0xFFFF8A80) else Color(0xFF90CAF9),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Narrative Story Description Box
                if (!entry.narrativeText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.28f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("📜", fontSize = 11.sp)
                            Text(
                                text = entry.narrativeText,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 15.sp,
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                } else {
                    Text(
                        text = entry.message,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Damage Calculation Formula Chip
                if (!entry.damageCalculation.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF101622),
                        border = BorderStroke(0.5.dp, Color(0xFF64B5F6).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📐", fontSize = 9.sp)
                            Text(
                                text = entry.damageCalculation,
                                fontSize = 9.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF81D4FA)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CombatLogItem(entry: CombatLogEntry) {
    val tacticalType = remember(entry.type, entry.message, entry.actionName) {
        TacticalAnimationType.fromLogEntry(entry.type, entry.message, entry.actionName)
    }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TacticalLottieMicroBadge(
                animationType = tacticalType,
                sizeDp = 20.dp,
                showBorder = false
            )
            Text(
                text = "Kolo ${entry.turn}:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
            Text(
                text = entry.message,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f)
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
    var selectedBossForCombat by remember { mutableStateOf<com.example.haremdark.models.Boss?>(null) }
    var showPartyDialogForEncounter by remember { mutableStateOf<com.example.haremdark.data.PartyCombatCatalog.PartyEncounterDefinition?>(null) }

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
                onChallenge = { selectedBossForCombat = boss }
            )
        }
    }
    
    if (selectedBossForCombat != null) {
        val boss = selectedBossForCombat
        if (boss != null) {
        Dialog(onDismissRequest = { selectedBossForCombat = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Příprava na boj: ${boss.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Vyber bojový loadout a šampiona pro tento střet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // --- QUICK LOADOUT SELECTION CAROUSEL ---
                    Text("⚔️ Bojový Loadout (přepnout set výbavy):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val allLoadouts = remember(gameState.savedLoadouts) { engine.getAllLoadouts() }
                    val activeId = gameState.activeLoadoutId ?: engine.defaultCombatLoadouts.first().id
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        items(allLoadouts) { loadout ->
                            val isSelected = (loadout.id == activeId)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF5E1738) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF80AB) else Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.clickable {
                                    engine.applyLoadout(loadout.id, null)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(loadout.icon, fontSize = 13.sp)
                                    Column {
                                        Text(
                                            text = loadout.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = loadout.situationTag,
                                            fontSize = 9.sp,
                                            color = if (isSelected) Color(0xFFFF80AB) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    val selectedLoadoutObj = allLoadouts.firstOrNull { it.id == activeId }
                    if (selectedLoadoutObj != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33000000),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "ℹ️ ${selectedLoadoutObj.description}",
                                fontSize = 10.sp,
                                color = Color(0xFFE1BEE7),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Text("Koho chceš vyslat do boje?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF5E1738)),
                                border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val matchingEncounter = com.example.haremdark.data.PartyCombatCatalog.ENCOUNTERS.find { it.title.contains(boss.name) }
                                        ?: com.example.haremdark.data.PartyCombatCatalog.ENCOUNTERS.first()
                                    selectedBossForCombat = null
                                    showPartyDialogForEncounter = matchingEncounter
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛡️", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Bojová družina Harému (Až 4 dívky)", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                        Text("Vyslat sehraný tým s rolemi, synergiemi a kombem", fontSize = 11.sp, color = Color(0xFFFF80AB))
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    engine.startBossCombat(boss, null)
                                    selectedBossForCombat = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Pán Dominia (Sólo)", fontWeight = FontWeight.Bold)
                                        Text("Boj: ${gameState.player.skills["boj"] ?: 0} | HP: ${gameState.player.hp}/${gameState.player.maxHp}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        
                        items(gameState.characters.filter { it.hp > 0 }) { char ->
                            val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                            val hpBonus = char.equipment.values.filterNotNull().sumOf { it.hpBonus }
                            val totalCombat = (char.skills["combat"] ?: 0) + combatBonus
                            val totalMaxHp = char.maxHp + hpBonus
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    engine.startBossCombat(boss, char.id)
                                    selectedBossForCombat = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚔️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(char.name, fontWeight = FontWeight.Bold)
                                        Text("Boj: $totalCombat | HP: ${char.hp}/$totalMaxHp", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { selectedBossForCombat = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Zrušit")
                    }
                }
            }
        }
    }
    }

    if (showPartyDialogForEncounter != null) {
        val encounter = showPartyDialogForEncounter
        if (encounter != null) {
            PartySelectionDialog(
                gameState = gameState,
                engine = engine,
                preselectedEncounter = encounter,
                onDismiss = { showPartyDialogForEncounter = null },
                onStartCombat = { selectedGirlIds, includePlayer, enc ->
                    showPartyDialogForEncounter = null
                    engine.startPartyCombat(selectedGirlIds, includePlayer, enc)
                },
                onSaveFormation = { name, icon, memberIds, includePlayer ->
                    engine.savePartyFormation(name, icon, memberIds, includePlayer)
                },
                onDeleteFormation = { id ->
                    engine.deletePartyFormation(id)
                }
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
