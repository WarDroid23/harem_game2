package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.R
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.PartyCombatCatalog
import com.example.haremdark.data.PrestigeSkinsCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.data.VisualSkinDefinition
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.PartyCombatScreen
import com.example.haremdark.ui.components.PartySelectionDialog

@Composable
fun ArenaScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val partyCombatSession by engine.partyCombatSession.collectAsState()
    var selectedArenaTab by remember { mutableIntStateOf(0) } // 0: Vlnový Gauntlet, 1: Síň Prestiže & Skíny, 2: Bleskové Střety
    var showPartyBuilderDialog by remember { mutableStateOf(false) }
    var selectedEncounterForDialog by remember { mutableStateOf<PartyCombatCatalog.PartyEncounterDefinition?>(null) }
    var selectedSkinDetail by remember { mutableStateOf<VisualSkinDefinition?>(null) }
    var skinFilterArchetype by remember { mutableStateOf("all") }

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
        // --- 1. HERO ARENA BANNER WITH LIVE PRESTIGE COUNTER ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.height(175.dp).fillMaxWidth()) {
                    val arenaBg = when (gameState.haremLevel) {
                        1 -> R.drawable.img_arena_battle
                        2 -> R.drawable.img_arena_domain_2
                        3 -> R.drawable.img_arena_domain_3
                        4 -> R.drawable.img_arena_domain_4
                        else -> R.drawable.img_arena_domain_5
                    }
                    Image(
                        painter = painterResource(id = arenaBg),
                        contentDescription = "Krvavá Aréna Dominia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xB0120616),
                                        Color(0xFA120616)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "⚔️ Vlnová Aréna & Prestiž",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD700),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🏆", fontSize = 12.sp)
                                    Text(
                                        "${gameState.player.prestige}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        "Prestiž",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3E2723)
                                    )
                                }
                            }
                        }

                        Text(
                            "Bojuj se svou harémovou družinou proti vlnám nepřátel, získávej Prestiž a odemykej luxusní vizuální skíny!",
                            color = Color(0xFFFF80AB),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- 2. ARENA NAVIGATION TABS ---
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0E24)),
                border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.3f))
            ) {
                TabRow(
                    selectedTabIndex = selectedArenaTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFF80AB)
                ) {
                    Tab(
                        selected = selectedArenaTab == 0,
                        onClick = {
                            selectedArenaTab = 0
                            HapticManager.vibrateClick()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🌊", fontSize = 13.sp)
                                Text("Vlnový Turnaj", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedArenaTab == 1,
                        onClick = {
                            selectedArenaTab = 1
                            HapticManager.vibrateClick()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("👑", fontSize = 13.sp)
                                Text("Klenotnice Skínů", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedArenaTab == 2,
                        onClick = {
                            selectedArenaTab = 2
                            HapticManager.vibrateClick()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🗡️", fontSize = 13.sp)
                                Text("Střety & Bossové", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                }
            }
        }

        // --- TAB CONTENT ---
        when (selectedArenaTab) {
            0 -> {
                // ==========================================
                // TAB 0: WAVE GAUNTLET ARENA MODE
                // ==========================================
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1035)),
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
                                    "K dispozici: $healthyCount bojovnic • Nastav pozice (Přední/Střed/Zadní)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }

                            Button(
                                onClick = {
                                    selectedEncounterForDialog = null
                                    showPartyBuilderDialog = true
                                    HapticManager.vibrateClick()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B)),
                                modifier = Modifier.testTag("setup_party_button")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sestavit tým", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Ready Combatants Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Nasazené hrdinky & Vybavené skíny:",
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
                                    val portraitRes = StaticData.getPortraitForCharacter(girl)
                                    val affinityTier = AffinityData.getTierForPoints(girl.affinityPoints)
                                    val combatSkill = girl.skills["combat"] ?: 5
                                    val hasCustomSkin = girl.equippedSkin != "default"

                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1024)),
                                        border = BorderStroke(1.dp, if (hasCustomSkin) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)),
                                        modifier = Modifier.width(115.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, if (hasCustomSkin) Color(0xFFFFD700) else Color(0xFFFF4081), CircleShape)
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
                                            if (hasCustomSkin) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF4A148C)
                                                ) {
                                                    Text("👑 SKIN", fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                                                }
                                            }
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

                item {
                    Text(
                        "🌊 Vlnové Zkoušky (Wave Gauntlets):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFF80AB)
                    )
                }

                items(PartyCombatCatalog.WAVE_ARENA_ENCOUNTERS) { waveGauntlet ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C0C22)),
                        border = BorderStroke(1.5.dp, if (waveGauntlet.totalWaves >= 5) Color(0xFFFFD700) else Color(0xFFFF4081).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Title & Wave Count Header
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
                                        color = Color(0xFF3D1030),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(waveGauntlet.icon, fontSize = 20.sp)
                                        }
                                    }
                                    Column {
                                        Text(
                                            waveGauntlet.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = if (waveGauntlet.totalWaves >= 5) Color(0xFFFFD700) else Color.White
                                        )
                                        Text(
                                            "${waveGauntlet.tierName} • ${waveGauntlet.location}",
                                            fontSize = 10.sp,
                                            color = Color(0xFFCE93D8)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF4A148C),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                                ) {
                                    Text(
                                        "🌊 ${waveGauntlet.totalWaves} Vln",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                waveGauntlet.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )

                            // Waves Preview Carousel
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Hordy nepřátel v jednotlivých vlnách:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    waveGauntlet.wavesList.forEachIndexed { waveIdx, enemiesInWave ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF28101E),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text("Vlna ${waveIdx + 1}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF80AB))
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    enemiesInWave.take(2).forEach { e ->
                                                        Text(e.icon, fontSize = 12.sp)
                                                    }
                                                }
                                                val totalHp = enemiesInWave.sumOf { it.hp }
                                                Text("$totalHp HP", fontSize = 8.sp, color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
                                            }
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFD700)
                                    ) {
                                        Text(
                                            "🏆 +${waveGauntlet.rewardPrestige} Prestiž",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text("🪙 +${waveGauntlet.rewardGold}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                    Text("⭐ +${waveGauntlet.rewardXp} XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF80D8FF))
                                }

                                Button(
                                    onClick = {
                                        selectedEncounterForDialog = waveGauntlet
                                        showPartyBuilderDialog = true
                                        HapticManager.vibrateClick()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("launch_wave_gauntlet_${waveGauntlet.id}")
                                ) {
                                    Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Vstoupit do Vln", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // ==========================================
                // TAB 1: PRESTIGE SKIN VAULT (KLENOTNICE SKÍNŮ)
                // ==========================================
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF281032)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                    Text(
                                        "Klenotnice Mýtických Skínů",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                                Text(
                                    "Získávej 🏆 Prestiž ve Vlnové Aréně a odemykej luxusní portréty a bojové aury!",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD700),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🏆", fontSize = 14.sp)
                                    Text(
                                        "${gameState.player.prestige}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // Archetype Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = skinFilterArchetype == "all",
                            onClick = { skinFilterArchetype = "all" },
                            label = { Text("Všechny (${PrestigeSkinsCatalog.ALL_SKINS.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = skinFilterArchetype == "subka",
                            onClick = { skinFilterArchetype = "subka" },
                            label = { Text("Elena (Elfka)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = skinFilterArchetype == "odvazna",
                            onClick = { skinFilterArchetype = "odvazna" },
                            label = { Text("Aurelia (Dračí)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = skinFilterArchetype == "touha",
                            onClick = { skinFilterArchetype = "touha" },
                            label = { Text("Lilith (Sukuba)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                val filteredSkins = PrestigeSkinsCatalog.ALL_SKINS.filter { skin ->
                    when (skinFilterArchetype) {
                        "subka" -> skin.characterArchetypeId == "subka"
                        "odvazna" -> skin.characterArchetypeId == "odvazna"
                        "touha" -> skin.characterArchetypeId == "touha"
                        "slechticna" -> skin.characterArchetypeId == "slechticna"
                        else -> true
                    }
                }

                items(filteredSkins) { skin ->
                    val matchingGirl = gameState.characters.find { char ->
                        char.name.contains(skin.targetCharacterName, ignoreCase = true) ||
                                (skin.characterArchetypeId != "all" && char.archetypeId == skin.characterArchetypeId)
                    } ?: gameState.characters.firstOrNull()

                    val isUnlocked = matchingGirl?.unlockedSkins?.contains(skin.id) == true
                    val isEquipped = matchingGirl?.equippedSkin == skin.id
                    val canAfford = gameState.player.prestige >= skin.prestigeCost

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0C24)),
                        border = BorderStroke(
                            width = if (isEquipped) 2.dp else 1.dp,
                            color = if (isEquipped) Color(0xFFFFD700) else if (isUnlocked) Color(0xFF00E676) else Color(0xFFFF4081).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Full-Resolution Skin Portrait
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, Color(skin.auraColorHex), RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedSkinDetail = skin
                                            HapticManager.vibrateClick()
                                        }
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(skin.drawableRes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = skin.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                                        color = Color(skin.auraColorHex).copy(alpha = 0.85f),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            "${skin.tierIcon} ${skin.tier}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            skin.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        skin.title,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF80AB)
                                    )
                                    Text(
                                        skin.combatPerkSummary,
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        skin.signatureQuote,
                                        fontSize = 9.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFFCE93D8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            // Skin Actions & Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isEquipped) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF00E676)
                                        ) {
                                            Text(
                                                "✨ VYBAVENO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    } else if (isUnlocked) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF388E3C)
                                        ) {
                                            Text(
                                                "🔓 ODEMČENO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFFD700)
                                        ) {
                                            Text(
                                                "🏆 Cena: ${skin.prestigeCost} Prestiže",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedSkinDetail = skin
                                            HapticManager.vibrateClick()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Detail & Lore", fontSize = 10.sp)
                                    }

                                    if (isEquipped) {
                                        Button(
                                            onClick = {
                                                if (matchingGirl != null) {
                                                    engine.unequipVisualSkin(matchingGirl.id)
                                                    Toast.makeText(context, "Obnoven výchozí vzhled.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Sundat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (isUnlocked) {
                                        Button(
                                            onClick = {
                                                if (matchingGirl != null) {
                                                    engine.equipVisualSkin(matchingGirl.id, skin.id)
                                                    SoundEffectManager.playRelationshipTier(4)
                                                    Toast.makeText(context, "Skin ${skin.name} byl vybaven!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("equip_skin_${skin.id}")
                                        ) {
                                            Text("Vybavit", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (matchingGirl != null) {
                                                    val success = engine.unlockVisualSkin(matchingGirl.id, skin.id)
                                                    if (success) {
                                                        Toast.makeText(context, "👑 Odemčen skin ${skin.name}!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Nedostatek prestiže! Vybojuj ji v aréně.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            enabled = canAfford,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), disabledContainerColor = Color(0xFF424242)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("unlock_skin_${skin.id}")
                                        ) {
                                            Text(
                                                "Odemknout (${skin.prestigeCost} 🏆)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (canAfford) Color.Black else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // ==========================================
                // TAB 2: SINGLE SKIRMISHES & BOSS ENCOUNTERS
                // ==========================================
                item {
                    Text(
                        "🗡️ Samostatné Střety & Souboje s Bossy:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                items(PartyCombatCatalog.ENCOUNTERS) { encounter ->
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
                                        HapticManager.vibrateClick()
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
        }
    }

    // Party Builder Modal
    if (showPartyBuilderDialog) {
        PartySelectionDialog(
            gameState = gameState,
            engine = engine,
            preselectedEncounter = selectedEncounterForDialog,
            onDismiss = { showPartyBuilderDialog = false },
            onStartCombat = { selectedGirlIds, includePlayer, encounter ->
                showPartyBuilderDialog = false
                engine.startPartyCombat(selectedGirlIds, includePlayer, encounter)
            },
            onSaveFormation = { name, icon, memberIds, includePlayer ->
                engine.savePartyFormation(name, icon, memberIds, includePlayer)
            },
            onDeleteFormation = { id ->
                engine.deletePartyFormation(id)
            }
        )
    }

    // Skin Detail & Lore Preview Modal
    selectedSkinDetail?.let { skin ->
        Dialog(onDismissRequest = { selectedSkinDetail = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0C24)),
                border = BorderStroke(2.dp, Color(skin.auraColorHex)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(3.dp, Color(skin.auraColorHex), RoundedCornerShape(16.dp))
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(skin.drawableRes)
                                .crossfade(true)
                                .build(),
                            contentDescription = skin.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 10.dp),
                            color = Color(skin.auraColorHex),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                "${skin.tierIcon} ${skin.tier}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        skin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        skin.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF80AB),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2A1035),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚔️ Bojové pasivní bonusy:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            Text(skin.combatPerkSummary, fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF140718),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📜 Historie & Proměna:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8))
                            Text(skin.loreStory, fontSize = 11.sp, color = Color(0xFFE0E0E0), lineHeight = 16.sp)
                        }
                    }

                    Text(
                        skin.signatureQuote,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFFFF80AB),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { selectedSkinDetail = null },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zavřít", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
