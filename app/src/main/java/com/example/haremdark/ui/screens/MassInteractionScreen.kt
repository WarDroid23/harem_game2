package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.data.SpecialScene
import com.example.haremdark.data.DailyObjective
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.NavSound
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.airbnb.lottie.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MassInteractionScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val characters = gameState.characters
    val player = gameState.player
    val stateManager = engine.gameStateManager

    // Refresh state triggers
    var refreshKey by remember { mutableIntStateOf(0) }

    // Tabs: 0 -> Družina (Grid of Companions), 1 -> Denní Úkoly (Daily Objectives)
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredCharacters = remember(characters, searchQuery, refreshKey) {
        if (searchQuery.isBlank()) characters
        else characters.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    var selectedCharacterForDetails by remember { mutableStateOf<Character?>(null) }
    var activeEventScene by remember { mutableStateOf<SpecialScene?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "⚡ Hromadná Správa & Úkoly",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Hromadný odpočinek, lázně, milníky a denní výzvy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("mass_interaction_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Player Resources Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Den ${player.day} • Zdroje:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "💰 ${player.gold} zlata",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "⚡ ${player.sexEnergy}/${player.maxSexEnergy} SE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4081)
                    )
                    Text(
                        text = "🔮 ${player.haremHarmony}% Harmonie",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }
            }

            // Tab bar selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        HapticManager.vibrateClick()
                        selectedTab = 0
                    },
                    text = { Text("🌸 Družina Harému", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        HapticManager.vibrateClick()
                        selectedTab = 1
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🎯 Denní Úkoly", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            // Show glowing badge if there are completed unclaimed objectives
                            val unclaimedCount = stateManager.loadState().dailyObjectives.count { it.isCompleted && !it.isClaimed }
                            if (unclaimedCount > 0) {
                                Badge(containerColor = Color.Red) {
                                    Text("$unclaimedCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // --- COMPANIONS GRID TAB ---

                // Mass Action Controls Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MassActionButton(
                        icon = "💬",
                        title = "Pozdravit",
                        cost = "Zdarma",
                        enabled = true,
                        onClick = {
                            HapticManager.vibrateClick()
                            val (success, log) = engine.executeMassGreeting()
                            if (success) {
                                Toast.makeText(context, "👋 Celý harém pozdraven!", Toast.LENGTH_SHORT).show()
                                refreshKey++
                            } else {
                                Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    val hasEnergy = player.sexEnergy >= 15
                    MassActionButton(
                        icon = "⚡",
                        title = "Trénovat",
                        cost = "15 SE",
                        enabled = hasEnergy,
                        onClick = {
                            HapticManager.vibrateClick()
                            val (success, log) = engine.executeMassTraining()
                            if (success) {
                                Toast.makeText(context, "💪 Společný trénink dokončen!", Toast.LENGTH_SHORT).show()
                                refreshKey++
                            } else {
                                Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    val hasGoldGifts = player.gold >= 100
                    MassActionButton(
                        icon = "🎁",
                        title = "Rozdat dary",
                        cost = "100 💰",
                        enabled = hasGoldGifts,
                        onClick = {
                            HapticManager.vibrateClick()
                            val (success, log) = engine.executeMassGifts()
                            if (success) {
                                Toast.makeText(context, "🎁 Dívky obdržely dárky!", Toast.LENGTH_SHORT).show()
                                refreshKey++
                            } else {
                                Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val hasBanquet = player.gold >= 60 && player.sexEnergy >= 10
                    MassActionButton(
                        icon = "🥂",
                        title = "Hostina",
                        cost = "60 💰 + 10 SE",
                        enabled = hasBanquet,
                        onClick = {
                            HapticManager.vibrateClick()
                            val (success, log) = engine.hostHaremBanquet()
                            if (success) {
                                Toast.makeText(context, "🥂 Hostina uspořádána!", Toast.LENGTH_SHORT).show()
                                refreshKey++
                            } else {
                                Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    val hasRest = player.gold >= 50 && player.sexEnergy >= 10
                    MassActionButton(
                        icon = "🧖",
                        title = "Spa & Relax",
                        cost = "50 💰 + 10 SE",
                        enabled = hasRest,
                        onClick = {
                            HapticManager.vibrateClick()
                            val (success, log) = engine.executeMassRest()
                            if (success) {
                                Toast.makeText(context, "🧖 Celý harém zrelaxován!", Toast.LENGTH_SHORT).show()
                                refreshKey++
                            } else {
                                Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    val hasGoldHeal = player.gold >= 40
                    MassActionButton(
                        icon = "🧪",
                        title = "Ošetřit",
                        cost = "40 💰",
                        enabled = hasGoldHeal,
                        onClick = {
                            HapticManager.vibrateClick()
                            val (success, log) = engine.executeMassHealing()
                            if (success) {
                                Toast.makeText(context, "🧪 Celý harém uzdraven!", Toast.LENGTH_SHORT).show()
                                refreshKey++
                            } else {
                                Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hledat dceru...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("mass_action_search_input")
                )

                // Grid of Girls
                if (filteredCharacters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌸", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "V harému zatím nejsou žádné dívky." else "Žádná dívka neodpovídá hledání.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 165.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("mass_action_girls_grid")
                    ) {
                        items(filteredCharacters, key = { it.id }) { character ->
                            val fatigue = stateManager.getFatigue(character.id)
                            val stress = stateManager.getStress(character.id)
                            GridCharacterCardExtended(
                                character = character,
                                fatigue = fatigue,
                                stress = stress,
                                onClick = {
                                    HapticManager.vibrateClick()
                                    SoundEffectManager.playCharacterVoice(character.voicePackId, com.example.haremdark.domain.CharacterVoiceType.TAP_GREETING)
                                    selectedCharacterForDetails = character
                                }
                            )
                        }
                    }
                }
            } else {
                // --- DAILY OBJECTIVES TAB ---
                val objectives = stateManager.loadState().dailyObjectives

                if (objectives.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dnes nejsou k dispozici žádné denní úkoly.\nOdpočiň si na nový den k jejich vygenerování.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(objectives) { obj ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (obj.isClaimed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else if (obj.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (obj.isClaimed) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    else if (obj.isCompleted) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = obj.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (obj.isClaimed) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (obj.isClaimed) {
                                            Text("✔️ SPLNĚNO", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        } else if (obj.isCompleted) {
                                            Text("🎉 PŘIPRAVENO", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("⏳ PROBÍHÁ", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text(
                                        text = obj.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Progress bar
                                    val progress = (obj.currentCount.toFloat() / obj.targetCount.toFloat()).coerceIn(0f, 1f)
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Postup úkolu", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${obj.currentCount} / ${obj.targetCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            color = if (obj.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                            trackColor = Color(0xFFEEEEEE),
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                        )
                                    }

                                    // Rewards list & Claim button
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                            Text("Odměny za úkol:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (obj.rewardGold > 0) {
                                                    Text("💰 +${obj.rewardGold}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                                }
                                                if (obj.rewardSexEnergy > 0) {
                                                    Text("⚡ +${obj.rewardSexEnergy} SE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4081))
                                                }
                                                if (obj.rewardAffection > 0) {
                                                    Text("💖 +${obj.rewardAffection} Náklonnost všech dcer", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                                                }
                                            }
                                        }

                                        if (obj.isCompleted && !obj.isClaimed) {
                                            Button(
                                                onClick = {
                                                    HapticManager.vibrateClick()
                                                    stateManager.claimObjectiveReward(obj.id) { gold, sexEnergy, affectionBoost ->
                                                        // Give gold & energy directly
                                                        engine.updateState { state ->
                                                            val updatedPlayer = state.player.copy(
                                                                gold = state.player.gold + gold,
                                                                sexEnergy = (state.player.sexEnergy + sexEnergy).coerceAtMost(state.player.maxSexEnergy)
                                                            )
                                                            // Boost affection of all girls
                                                            val boostedChars = state.characters.map { c ->
                                                                val newAttr = c.attributes.copy(
                                                                    affection = (c.attributes.affection + affectionBoost).coerceAtMost(100)
                                                                )
                                                                c.copy(
                                                                    attributes = newAttr,
                                                                    affinityPoints = (c.affinityPoints + affectionBoost).coerceAtMost(100)
                                                                )
                                                            }
                                                            state.copy(player = updatedPlayer, characters = boostedChars)
                                                        }
                                                        Toast.makeText(context, "🎁 Odměna vybrána! +$gold💰, +$sexEnergy SE, +$affectionBoost Náklonnost dcer!", Toast.LENGTH_LONG).show()
                                                    }
                                                    refreshKey++
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Vyzvednout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (obj.isClaimed) {
                                            OutlinedButton(
                                                onClick = {},
                                                enabled = false,
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Vybráno", fontSize = 11.sp)
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

    // Detail Dialog (Character details, fatigue management, and Affection dialogues/special scenes)
    selectedCharacterForDetails?.let { character ->
        val fatigue = stateManager.getFatigue(character.id)
        val stress = stateManager.getStress(character.id)
        val availableScenes = stateManager.getDialogueScenesForAffection(
            character.id,
            character.name,
            character.archetypeId,
            character.affinityPoints
        )

        AlertDialog(
            onDismissRequest = { selectedCharacterForDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(character.statusIcon, fontSize = 24.sp)
                    Column {
                        Text(character.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(character.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick stats display
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📊 Kondice a stav dcery", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            
                            // Fatigue Display
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("😴 Únava dcery", fontSize = 11.sp)
                                Text("$fatigue / 100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (fatigue > 60) Color(0xFFE65100) else Color.Gray)
                            }
                            LinearProgressIndicator(
                                progress = { fatigue.toFloat() / 100f },
                                color = Color(0xFFE65100),
                                trackColor = Color(0xFFEEEEEE),
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )

                            // Stress Display
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("😰 Stres dcery", fontSize = 11.sp)
                                Text("$stress / 100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (stress > 60) Color(0xFFE91E63) else Color.Gray)
                            }
                            LinearProgressIndicator(
                                progress = { stress.toFloat() / 100f },
                                color = Color(0xFFE91E63),
                                trackColor = Color(0xFFEEEEEE),
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    fatigue > 70 -> "😴 Extrémně unavená! Její loajalita bude klesat každý den a má oslabený výkon v boji."
                                    stress > 70 -> "😰 Vysoký stres! Hrozí nervové vyčerpání. Doporučuje se odeslat ji na odpočinek."
                                    else -> "✅ V dobré kondici, připravena k plnění rozkazů."
                                },
                                fontSize = 10.sp,
                                color = if (fatigue > 70 || stress > 70) Color(0xFFE53935) else Color(0xFF4CAF50),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    // REST CYCLE CONTROLS FOR SINGLE CHARACTER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (player.gold >= 15) {
                                    engine.updateState { state ->
                                        val p = state.player.copy(gold = state.player.gold - 15)
                                        state.copy(player = p)
                                    }
                                    stateManager.updateFatigueAndStress(character.id, -45, -35)
                                    stateManager.incrementObjectiveProgress("rest", 1)
                                    Toast.makeText(context, "🧖 ${character.name} si dopřála odpočinek v lázních!", Toast.LENGTH_SHORT).show()
                                    refreshKey++
                                    selectedCharacterForDetails = null
                                } else {
                                    Toast.makeText(context, "Nedostatek zlata (potřebuješ 15 💰)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🧖 Osobní Lázně (15💰)", fontSize = 11.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                if (player.sexEnergy >= 8) {
                                    engine.updateState { state ->
                                        val p = state.player.copy(sexEnergy = state.player.sexEnergy - 8)
                                        state.copy(player = p)
                                    }
                                    stateManager.updateFatigueAndStress(character.id, -20, -45)
                                    stateManager.incrementObjectiveProgress("rest", 1)
                                    Toast.makeText(context, "💖 Utěšil jsi dceru a zbavil ji veškerého stresu!", Toast.LENGTH_SHORT).show()
                                    refreshKey++
                                    selectedCharacterForDetails = null
                                } else {
                                    Toast.makeText(context, "Nedostatek SE (potřebuješ 8 SE)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💖 Utěšit dceru (8 SE)", fontSize = 11.sp)
                        }
                    }

                    // AFFECTION DIALOGUES / EVENTS SCENES UNLOCKED
                    Text(
                        "🎬 Scény náklonnosti (${character.affinityPoints} AP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (availableScenes.isEmpty()) {
                        Text(
                            "Zatím nejsou odemčené žádné jedinečné scény. Zvyšuj její náklonnost dary a pozdravy, abys odemkl příběhové milníky na 40, 70 a 95 AP.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        availableScenes.forEach { scene ->
                            val isClaimed = stateManager.isSceneClaimed(scene.id)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isClaimed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isClaimed) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(scene.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("AP milník: ${scene.requiredAffection}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Button(
                                        onClick = {
                                            selectedCharacterForDetails = null
                                            activeEventScene = scene
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isClaimed) MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.primary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(if (isClaimed) "Přečíst znovu" else "Spustit scénu", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedCharacterForDetails = null }) {
                    Text("Zavřít")
                }
            }
        )
    }

    // Actual Special Event Scene Dialogue Modal Player
    activeEventScene?.let { scene ->
        AlertDialog(
            onDismissRequest = { activeEventScene = null },
            title = {
                Text(
                    text = "🎬 ${scene.title}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = scene.description,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = scene.dialogue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Odměna za odemčení:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )
                    Text(
                        text = scene.rewardText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isClaimed = stateManager.isSceneClaimed(scene.id)
                        if (!isClaimed) {
                            stateManager.claimScene(scene.id)
                            // Award stat bonuses retroactively directly to the character
                            selectedCharacterForDetails?.let { char ->
                                engine.updateState { state ->
                                    val updatedChars = state.characters.map { c ->
                                        if (c.id == char.id) {
                                            c.copy(
                                                loajalita = (c.loajalita + 15).coerceAtMost(100),
                                                morale = (c.morale + 20).coerceAtMost(100),
                                                hp = (c.hp + 20).coerceAtMost(c.maxHp)
                                            )
                                        } else c
                                    }
                                    state.copy(characters = updatedChars)
                                }
                            }
                            engine.addLog("🎬 Příběh: Odemčen milník afinity '${scene.title}'!")
                            Toast.makeText(context, "🎁 Odměna byla připsána dceři!", Toast.LENGTH_SHORT).show()
                        }
                        activeEventScene = null
                        refreshKey++
                    }
                ) {
                    Text(if (stateManager.isSceneClaimed(scene.id)) "Zavřít příběh" else "Vzít odměnu a dokončit")
                }
            }
        )
    }
}

@Composable
fun GridCharacterCardExtended(
    character: Character,
    fatigue: Int,
    stress: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
    val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)

    // Lottie Animation Loading
    val fatigueCritical = fatigue > 70
    val milestoneReached = character.affinityPoints >= 70

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("grid_card_${character.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            Color(character.rarityEnum.color).copy(alpha = 0.35f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Row: Small Avatar & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(34.dp)) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(portraitRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(1.dp, Color(character.rarityEnum.color), CircleShape)
                    )

                    // Lottie heart overlay if milestone reached
                    if (milestoneReached) {
                        val heartComposition by rememberLottieComposition(
                            LottieCompositionSpec.Url("https://assets5.lottiefiles.com/packages/lf20_96bovpxo.json")
                        )
                        val heartProgress by animateLottieCompositionAsState(
                            composition = heartComposition,
                            iterations = LottieConstants.IterateForever
                        )
                        LottieAnimation(
                            composition = heartComposition,
                            progress = { heartProgress },
                            modifier = Modifier.fillMaxSize().scale(1.2f)
                        )
                    }

                    // Lottie alert overlay if fatigue is critical
                    if (fatigueCritical) {
                        val fatigueComposition by rememberLottieComposition(
                            LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_y9m9m9.json")
                        )
                        val fatigueProgress by animateLottieCompositionAsState(
                            composition = fatigueComposition,
                            iterations = LottieConstants.IterateForever
                        )
                        LottieAnimation(
                            composition = fatigueComposition,
                            progress = { fatigueProgress },
                            modifier = Modifier.align(Alignment.BottomEnd).size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Úr. ${character.level}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = affinityTier.icon,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Top)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Character Stats
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                // HP Bar
                val hpProgress = (character.hp.toFloat() / character.maxHp.toFloat()).coerceIn(0f, 1f)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💚 HP", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${character.hp}/${character.maxHp}", fontSize = 8.sp)
                    }
                    LinearProgressIndicator(
                        progress = { hpProgress },
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0).copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }

                // Obedience Bar
                val obedienceProgress = (character.poslusnost.toFloat() / 100f).coerceIn(0f, 1f)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ Poslušnost", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${character.poslusnost}%", fontSize = 8.sp)
                    }
                    LinearProgressIndicator(
                        progress = { obedienceProgress },
                        color = Color(0xFF2196F3),
                        trackColor = Color(0xFFE0E0E0).copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }

                // Loyalty Bar
                val loyaltyProgress = (character.loajalita.toFloat() / 100f).coerceIn(0f, 1f)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🤝 Loajalita", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${character.loajalita}%", fontSize = 8.sp)
                    }
                    LinearProgressIndicator(
                        progress = { loyaltyProgress },
                        color = Color(0xFF9C27B0),
                        trackColor = Color(0xFFE0E0E0).copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }

                // Fatigue Progress Bar (Orange)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("😴 Únava", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("$fatigue%", fontSize = 8.sp)
                    }
                    LinearProgressIndicator(
                        progress = { fatigue.toFloat() / 100f },
                        color = Color(0xFFE65100),
                        trackColor = Color(0xFFE0E0E0).copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }

                // Stress Progress Bar (Pink)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("😰 Stres", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("$stress%", fontSize = 8.sp)
                    }
                    LinearProgressIndicator(
                        progress = { stress.toFloat() / 100f },
                        color = Color(0xFFE91E63),
                        trackColor = Color(0xFFE0E0E0).copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Recent Event Preview
            val latestEvent = character.interactionLogs.lastOrNull()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                    .padding(4.dp)
            ) {
                Column {
                    Text(
                        text = "POSLEDNÍ UDÁLOST",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = latestEvent?.title ?: "Žádná nedávná aktivita",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = latestEvent?.description ?: "Dívka je připravena k interakci.",
                        fontSize = 7.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MassActionButton(
    icon: String,
    title: String,
    cost: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 12.sp)
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cost,
                    fontSize = 8.sp,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
