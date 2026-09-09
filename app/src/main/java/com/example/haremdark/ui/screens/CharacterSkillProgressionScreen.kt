package com.example.haremdark.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.CharacterSkillCatalog
import com.example.haremdark.data.CharacterSkillNode
import com.example.haremdark.data.SkillNodeType
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.EventSound
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSkillProgressionScreen(
    engine: GameEngine,
    onNavigateBack: () -> Unit,
    initialCharacterId: String? = null,
    modifier: Modifier = Modifier
) {
    val gameState by engine.gameState.collectAsState()
    val characters = gameState.characters

    var selectedCharacterId by remember(initialCharacterId, characters) {
        mutableStateOf(
            if (initialCharacterId != null && characters.any { it.id == initialCharacterId }) {
                initialCharacterId
            } else {
                characters.firstOrNull()?.id ?: ""
            }
        )
    }

    val selectedChar = characters.firstOrNull { it.id == selectedCharacterId }
    var selectedBranchFilter by remember { mutableStateOf("Vše") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "✨ Bojový vývoj dovedností",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Odemkni aktivní schopnosti a pasivní perky za ZK",
                            fontSize = 11.sp,
                            color = Color(0xFFFF80AB)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140810)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F060C),
        modifier = modifier
    ) { innerPadding ->
        if (characters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V tvém harému zatím nejsou žádné dívky.\nNaverbuj nové společnice v tržnici!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Horizontal Girl Carousel
                item {
                    Text(
                        text = "VYBER SPOLEČNICI K TRÉNINKU",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE1BEE7),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(characters) { char ->
                            val isSelected = (char.id == selectedCharacterId)
                            val unlockedCount = char.unlockedPassives.size + char.unlockedCombatSkills.size

                            Card(
                                modifier = Modifier
                                    .width(105.dp)
                                    .clickable {
                                        SoundEffectManager.playEvent(EventSound.EVENT_CHOICE)
                                        selectedCharacterId = char.id
                                    }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF330E20) else Color(0xFF1D0B16)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.5.dp,
                                                if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.2f),
                                                CircleShape
                                            )
                                    ) {
                                        Image(
                                            painter = painterResource(id = StaticData.getPortraitForArchetype(char.archetypeId)),
                                            contentDescription = char.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Text(
                                        text = char.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF51102A)
                                    ) {
                                        Text(
                                            text = "Úr. ${char.level}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "✨ $unlockedCount schopností",
                                        fontSize = 9.sp,
                                        color = Color(0xFFB0BEC5)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Selected Character Hero Card & Currency Hub
                if (selectedChar != null) {
                    item {
                        CharacterProgressHeroCard(
                            character = selectedChar,
                            onConvertXpToSp = {
                                val (ok, msg) = engine.convertCharacterXpToSp(selectedChar.id)
                                if (ok) SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
                            }
                        )
                    }

                    // 3. Active Passive Bonuses Summary
                    item {
                        val bonuses = CharacterSkillCatalog.calculatePassiveBonuses(selectedChar)
                        PassiveBonusSummaryCard(bonuses = bonuses)
                    }

                    // 4. Branch Filter Chips
                    item {
                        val branches = listOf("Vše", "Boj", "Magie & Efekty", "Obrana & Podpora")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(branches) { branch ->
                                val isSelected = (selectedBranchFilter == branch)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        SoundEffectManager.playEvent(EventSound.EVENT_CHOICE)
                                        selectedBranchFilter = branch
                                    },
                                    label = { Text(branch, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF880E4F),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E0E18),
                                        labelColor = Color(0xFFCFD8DC)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.2f),
                                        selectedBorderColor = Color(0xFFFF4081),
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }

                    // 5. Skill Nodes List
                    val allNodes = CharacterSkillCatalog.getSkillTreeForCharacter(selectedChar)
                    val filteredNodes = allNodes.filter { node ->
                        if (selectedBranchFilter == "Vše") true else node.branchName == selectedBranchFilter
                    }

                    items(filteredNodes) { node ->
                        val isUnlocked = selectedChar.unlockedPassives.contains(node.id) || selectedChar.unlockedCombatSkills.contains(node.id)
                        val canUnlock = CharacterSkillCatalog.canUnlockNode(selectedChar, node)

                        SkillNodeProgressionCard(
                            node = node,
                            character = selectedChar,
                            isUnlocked = isUnlocked,
                            canUnlock = canUnlock,
                            onUnlockWithXp = {
                                val (ok, msg) = engine.unlockCharacterSkillWithXp(selectedChar.id, node.id)
                                if (ok) {
                                    SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
                                }
                            },
                            onUnlockWithSp = {
                                val (ok, msg) = engine.unlockCharacterSkillWithSp(selectedChar.id, node.id)
                                if (ok) {
                                    SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterProgressHeroCard(
    character: Character,
    onConvertXpToSp: () -> Unit
) {
    val reqXpForNextLevel = character.level * 100
    val progress = (character.xp.toFloat() / reqXpForNextLevel.toFloat()).coerceIn(0f, 1f)
    val loyaltyTier = StaticData.getLoyaltyTier(character.loajalita)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFF4081), Color(0xFFFFD700))),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF220A17)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFFFD700), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = StaticData.getPortraitForArchetype(character.archetypeId)),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${character.archetypeId} • Úroveň ${character.level}",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Oddanost: ${loyaltyTier.title} (${character.affinityPoints} bodů)",
                        fontSize = 11.sp,
                        color = Color(0xFFFF80AB)
                    )
                }
            }

            // XP and SP stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bojové ZK: ${character.xp} / $reqXpForNextLevel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(180.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF4081),
                        trackColor = Color(0xFF4A142A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3E1229),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💎", fontSize = 14.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Dovednostní body", fontSize = 9.sp, color = Color(0xFFB0BEC5))
                            Text("${character.skillPoints} SP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                    }
                }
            }

            // XP conversion button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onConvertXpToSp,
                    enabled = character.xp >= 100,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFFD700)
                    ),
                    border = BorderStroke(1.dp, if (character.xp >= 100) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("🔄 Převést 100 ZK ➔ 1 SP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PassiveBonusSummaryCard(bonuses: com.example.haremdark.data.CombatPassiveBonuses) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF190C18)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🛡️", fontSize = 14.sp)
                Text(
                    text = "AKTIVNÍ PASIVNÍ BONUSY ZE STROMU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatBadgeItem(label = "Útok", value = "+${bonuses.attackBonus}", icon = "⚔️", color = Color(0xFFFF5252))
                StatBadgeItem(label = "Obrana", value = "+${bonuses.defenseBonus}", icon = "🛡️", color = Color(0xFF448AFF))
                StatBadgeItem(label = "Max HP", value = "+${bonuses.hpBonus}", icon = "❤️", color = Color(0xFF00E676))
                StatBadgeItem(label = "Krit", value = "+${bonuses.critBonus}%", icon = "💥", color = Color(0xFFFFD700))
                if (bonuses.lifestealPercent > 0) {
                    StatBadgeItem(label = "Vysávání", value = "${bonuses.lifestealPercent}%", icon = "🩸", color = Color(0xFFFF1744))
                }
            }
        }
    }
}

@Composable
fun StatBadgeItem(label: String, value: String, icon: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$icon $value", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun SkillNodeProgressionCard(
    node: CharacterSkillNode,
    character: Character,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    onUnlockWithXp: () -> Unit,
    onUnlockWithSp: () -> Unit
) {
    val borderColor = when {
        isUnlocked -> Color(0xFFFFD700)
        canUnlock -> Color(0xFFFF4081)
        else -> Color.White.copy(alpha = 0.1f)
    }

    val containerBg = when {
        isUnlocked -> Color(0xFF2B1220)
        canUnlock -> Color(0xFF1E0E18)
        else -> Color(0xFF140810)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon + Name + Node Type Badge + Tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF331424),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(node.icon, fontSize = 20.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = node.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isUnlocked) Color(0xFFFFD700) else Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(node.nodeType.badgeColorHex).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = node.nodeType.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(node.nodeType.badgeColorHex),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Větev: ${node.branchName} • Stupeň ${node.tier}",
                        fontSize = 10.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }

                if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "✅ AKTIVNÍ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Description
            Text(
                text = node.description,
                fontSize = 12.sp,
                color = Color(0xFFE0E0E0),
                lineHeight = 16.sp
            )

            // Active ability details (if active)
            if (node.activeSkill != null) {
                val act = node.activeSkill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF280B1B),
                    border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("💧 Mana: ${act.manaCost}", fontSize = 11.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.SemiBold)
                            Text("⏳ Obnova: ${act.cooldownTurns} kola", fontSize = 11.sp, color = Color(0xFFFFB74D), fontWeight = FontWeight.SemiBold)
                            Text("🎯 Cíl: ${act.targetType.name}", fontSize = 10.sp, color = Color(0xFFE1BEE7))
                        }
                        if (act.voiceQuote != null) {
                            Text(
                                text = "💬 \"${act.voiceQuote}\"",
                                fontSize = 10.sp,
                                color = Color(0xFFFF80AB),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Stat bonus tag
            if (node.specialEffectText != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E2838)
                ) {
                    Text(
                        text = "⭐ Efekt: ${node.specialEffectText}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF80D8FF),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Action / Requirement footer
            if (!isUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Requirements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val levelMet = character.level >= node.reqLevel
                        Text(
                            text = "Požadovaná úroveň: ${node.reqLevel} ${if (levelMet) "✅" else "❌"}",
                            fontSize = 11.sp,
                            color = if (levelMet) Color(0xFF00E676) else Color(0xFFFF5252)
                        )
                        if (node.reqNodeId != null) {
                            val reqMet = character.unlockedPassives.contains(node.reqNodeId) || character.unlockedCombatSkills.contains(node.reqNodeId)
                            Text(
                                text = "Předchůdce: ${if (reqMet) "✅ Splněno" else "🔒 Zamčeno"}",
                                fontSize = 11.sp,
                                color = if (reqMet) Color(0xFF00E676) else Color(0xFFFF5252)
                            )
                        }
                    }

                    // Unlock buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onUnlockWithXp,
                            enabled = canUnlock && character.xp >= node.xpCost,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Odemknout (${node.xpCost} ZK)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onUnlockWithSp,
                            enabled = canUnlock && character.skillPoints >= node.spCost,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Odemknout (${node.spCost} SP)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
