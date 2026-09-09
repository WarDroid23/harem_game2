package com.example.haremdark.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.R
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.PartyCombatCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.PartyCombatScreen
import com.example.haremdark.ui.components.PartySelectionDialog

@Composable
fun ArenaScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val partyCombatSession by engine.partyCombatSession.collectAsState()
    var showPartyBuilderDialog by remember { mutableStateOf(false) }
    var selectedEncounterForDialog by remember { mutableStateOf<PartyCombatCatalog.PartyEncounterDefinition?>(null) }
    var arenaFilterTier by remember { mutableStateOf("all") }

    // If an active party battle is underway, display the full interactive Party Combat Screen!
    if (partyCombatSession != null) {
        val session = partyCombatSession
        if (session != null) {
            PartyCombatScreen(
                session = session,
                gameState = gameState,
                engine = engine,
                onSessionUpdated = { updated -> engine.updatePartyCombatSession(updated) },
                onExitCombat = { engine.closePartyCombat() },
                modifier = modifier
            )
            return
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // --- 1. HERO ARENA BANNER ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_arena_battle),
                        contentDescription = "Krvavá Aréna Dominia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD120616), Color(0xFA120616))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚔️ Krvavá Aréna Dominia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFD700)
                            ) {
                                Text(
                                    "🏆 Prestiž: ${gameState.player.prestige}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Sestav družinu ze svých hrdinek z harému a vyzvěte na souboj nepřátelské týmy!",
                            color = Color(0xFFFF80AB),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // --- 2. QUICK PARTY SETUP CTA CARD ---
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1235)),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🛡️ Bojová družina Harému",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700)
                        )
                        val healthyCount = gameState.characters.count { it.hp > 0 }
                        Text(
                            "K dispozici: $healthyCount připravených dívek k nasazení",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }

                    Button(
                        onClick = {
                            selectedEncounterForDialog = null
                            showPartyBuilderDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B))
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sestavit tým", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 3. AVAILABLE HAREM COMBATANTS ROSTER STRIP ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Dívky připravené do boje (role & statistiky):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )

                val readyGirls = gameState.characters.filter { it.hp > 0 }
                if (readyGirls.isEmpty()) {
                    Text("Nemáš žádné zdravé dívky. Ošetři je v komnatách dominia.", fontSize = 11.sp, color = Color(0xFFFF8A80))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(readyGirls) { girl ->
                            val role = PartyCombatCatalog.getRoleForArchetype(girl.archetype)
                            val portraitRes = StaticData.getPortraitForArchetype(girl.archetype)
                            val affinityTier = AffinityData.getTierForPoints(girl.affinityPoints)
                            val combatSkill = girl.skills["combat"] ?: 5

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1024)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.width(110.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(portraitRes)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = girl.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Text(girl.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF381024)
                                    ) {
                                        Text("${role.icon} ${role.title}", fontSize = 8.sp, color = Color(0xFFFF80AB), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                    Text("⚔️ Útok: $combatSkill", fontSize = 9.sp, color = Color(0xFFFFB74D))
                                    Text("💖 ${affinityTier.level}. stupeň", fontSize = 8.sp, color = Color(0xFFFF80AB))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. ENCOUNTER TIERS & STAGES SELECTOR ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Vyber arénovou zkoušku / střet:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = arenaFilterTier == "all",
                        onClick = { arenaFilterTier = "all" },
                        label = { Text("Všechny (${PartyCombatCatalog.ENCOUNTERS.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = arenaFilterTier == "easy",
                        onClick = { arenaFilterTier = "easy" },
                        label = { Text("Skirmish (1-2)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = arenaFilterTier == "boss",
                        onClick = { arenaFilterTier = "boss" },
                        label = { Text("Bossové (3-5)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        val filteredEncounters = PartyCombatCatalog.ENCOUNTERS.filter { enc ->
            when (arenaFilterTier) {
                "easy" -> enc.recommendedLevel <= 2
                "boss" -> enc.recommendedLevel >= 3
                else -> true
            }
        }

        items(filteredEncounters) { encounter ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0E22)),
                border = BorderStroke(1.dp, if (encounter.recommendedLevel >= 4) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF381028),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(encounter.icon, fontSize = 20.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = encounter.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (encounter.recommendedLevel >= 4) Color(0xFFFFD700) else Color.White
                                )
                                Text(
                                    text = "${encounter.tierName} • ${encounter.location}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E1238)
                        ) {
                            Text(
                                "Doporučeno: Lv ${encounter.recommendedLevel}",
                                fontSize = 9.sp,
                                color = Color(0xFFCE93D8),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = encounter.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    // Enemy lineup preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nepřátelé:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        encounter.enemies.forEach { enemy ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF281016)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(enemy.icon, fontSize = 11.sp)
                                    Text("${enemy.name} (${enemy.hp} HP)", fontSize = 9.sp, color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Rewards & Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🪙 +${encounter.rewardGold}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            Text("⭐ +${encounter.rewardXp} XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF80D8FF))
                            Text("🏆 +${encounter.rewardPrestige}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4081))
                        }

                        Button(
                            onClick = {
                                selectedEncounterForDialog = encounter
                                showPartyBuilderDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vyzvat k boji", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Party Builder Modal
    if (showPartyBuilderDialog) {
        PartySelectionDialog(
            gameState = gameState,
            preselectedEncounter = selectedEncounterForDialog,
            onDismiss = { showPartyBuilderDialog = false },
            onStartCombat = { selectedGirlIds, includePlayer, encounter ->
                showPartyBuilderDialog = false
                engine.startPartyCombat(selectedGirlIds, includePlayer, encounter)
            }
        )
    }
}
