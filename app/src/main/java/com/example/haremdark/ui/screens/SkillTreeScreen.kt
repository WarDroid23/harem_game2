package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.HaremCharacter
import com.example.haremdark.models.SkillBranch
import com.example.haremdark.models.SkillNode
import com.example.haremdark.models.SkillTreeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillTreeScreen(
    engine: GameEngine,
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val characters by engine.haremCharacterRepository.characters.collectAsState()
    var selectedCharacterId by remember { mutableStateOf<String?>(characters.firstOrNull()?.id) }

    // Always find latest selected character state from state flow
    val selectedCharacter = characters.find { it.id == selectedCharacterId } ?: characters.firstOrNull()

    var selectedBranchFilter by remember { mutableStateOf<SkillBranch?>(null) }
    var activeSkillTypeFilter by remember { mutableStateOf<String>("ALL") } // "ALL", "ACTIVE", "PASSIVE"

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Strom Dovedností Harému",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Rozvíjejte bojové pasivy a aktivní schopnosti svých členek",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("skill_tree_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- FEEDBACK BANNER ---
            AnimatedVisibility(
                visible = feedbackMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                feedbackMessage?.let { msg ->
                    Surface(
                        color = Color(0xFF1E3A8A).copy(alpha = 0.9f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(msg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { feedbackMessage = null }) {
                                Text("OK", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // --- CHARACTER SELECTOR ROW ---
            Text(
                text = "VOLEBNÍ SEZNAM ČLENEK HARÉMU",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            if (characters.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "V harému zatím nejsou žádné dívky k tréninku.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(characters, key = { it.id }) { char ->
                        val isSelected = char.id == selectedCharacter?.id
                        val unlockedCount = char.unlockedSkills.size

                        Surface(
                            onClick = { selectedCharacterId = char.id },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .width(140.dp)
                                .testTag("select_char_${char.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(char.avatarIcon, fontSize = 18.sp)
                                    Text(
                                        char.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    "Lvl ${char.level} • ${char.role.title}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFFD700).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "⭐ ${char.experience} XP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "🔓 $unlockedCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SELECTED CHARACTER SUMMARY & XP MANAGEMENT HEADER ---
            selectedCharacter?.let { char ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(char.avatarIcon, fontSize = 24.sp)
                                Column {
                                    Text(
                                        char.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "${char.role.icon} ${char.role.title} • Úroveň ${char.level} (${char.powerTier})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⭐", fontSize = 12.sp)
                                        Text(
                                            "${char.experience} XP",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFFFD700)
                                        )
                                        Text("•", fontSize = 12.sp, color = Color.Gray)
                                        Text("🔮", fontSize = 12.sp)
                                        Text(
                                            "${char.availableSkillPoints} SP",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFA855F7)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // XP Quick Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    engine.earnCharacterXp(char.id, 100)
                                    feedbackMessage = "🏋️ ${char.name} absolvovala intenzivní trénink (+100 XP)!"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_train_xp_${char.id}"),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Výcvik (+100 XP)", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val (success, msg) = engine.convertXpToSkillPoints(char.id, 100)
                                    feedbackMessage = msg
                                    if (!success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = char.experience >= 100,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_convert_sp_${char.id}"),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("100 XP ➔ 1 SP", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // --- BRANCH & TYPE FILTER TABS ---
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    // Branch Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedBranchFilter == null,
                                onClick = { selectedBranchFilter = null },
                                label = { Text("Všechny větvě") },
                                leadingIcon = { Text("🌳", fontSize = 12.sp) }
                            )
                        }

                        items(SkillBranch.entries.toTypedArray()) { branch ->
                            FilterChip(
                                selected = selectedBranchFilter == branch,
                                onClick = { selectedBranchFilter = branch },
                                label = { Text(branch.title) },
                                leadingIcon = { Text(branch.icon, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Active vs Passive Skill Filter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        FilterChip(
                            selected = activeSkillTypeFilter == "ALL",
                            onClick = { activeSkillTypeFilter = "ALL" },
                            label = { Text("Vše (Typ)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = activeSkillTypeFilter == "ACTIVE",
                            onClick = { activeSkillTypeFilter = "ACTIVE" },
                            label = { Text("⚡ Aktivní kouzla", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = activeSkillTypeFilter == "PASSIVE",
                            onClick = { activeSkillTypeFilter = "PASSIVE" },
                            label = { Text("🛡️ Pasivní buffy", fontSize = 11.sp) }
                        )
                    }
                }

                // --- SKILL NODES LIST ---
                val filteredNodes = SkillTreeData.nodes.filter { node ->
                    val matchesBranch = selectedBranchFilter == null || node.branch == selectedBranchFilter
                    val matchesType = when (activeSkillTypeFilter) {
                        "ACTIVE" -> node.isActiveCombatSkill
                        "PASSIVE" -> !node.isActiveCombatSkill
                        else -> true
                    }
                    matchesBranch && matchesType
                }

                if (filteredNodes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pro zvolené filtry nebyly nalezeny žádné dovednosti.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredNodes, key = { it.id }) { node ->
                            SkillNodeCard(
                                node = node,
                                character = char,
                                onUnlockWithSp = {
                                    val (success, msg) = engine.purchaseSkillNode(char.id, node.id, useXp = false)
                                    feedbackMessage = msg
                                    if (!success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onUnlockWithXp = {
                                    val (success, msg) = engine.purchaseSkillNode(char.id, node.id, useXp = true)
                                    feedbackMessage = msg
                                    if (!success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillNodeCard(
    node: SkillNode,
    character: HaremCharacter,
    onUnlockWithSp: () -> Unit,
    onUnlockWithXp: () -> Unit
) {
    val isUnlocked = character.unlockedSkills.contains(node.id)

    val prerequisiteNode = remember(node.requiresId) {
        node.requiresId?.let { reqId ->
            SkillTreeData.nodes.find { it.id == reqId }
        }
    }
    val hasPrerequisite = node.requiresId == null || character.unlockedSkills.contains(node.requiresId)

    val canUnlockWithSp = !isUnlocked && hasPrerequisite && character.availableSkillPoints >= node.cost
    val canUnlockWithXp = !isUnlocked && hasPrerequisite && character.experience >= node.xpCost

    val cardBorderColor = when {
        isUnlocked -> Color(0xFF10B981) // Emerald Green
        canUnlockWithSp || canUnlockWithXp -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val containerColor = when {
        isUnlocked -> Color(0xFF064E3B).copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skill_node_${node.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon, Title, Type Badge, Unlocked Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) Color(0xFF10B981).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(node.icon, fontSize = 22.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                node.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isUnlocked) Color(0xFF34D399) else MaterialTheme.colorScheme.onSurface
                            )

                            // Type Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (node.isActiveCombatSkill)
                                    Color(0xFFEF4444).copy(alpha = 0.2f)
                                else
                                    Color(0xFF3B82F6).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (node.isActiveCombatSkill) "⚡ AKTIVNÍ" else "🛡️ PASIVNÍ",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (node.isActiveCombatSkill) Color(0xFFFCA5A5) else Color(0xFF93C5FD),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${node.branch.icon} ${node.branch.title}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF059669),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Text("ODEMČENO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                node.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )

            // Stat & Combat Bonus Chip
            if (node.statBonusSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "Účinek: ${node.statBonusSummary}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Prerequisite warning if missing
            if (!hasPrerequisite && prerequisiteNode != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF7F1D1D).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(12.dp))
                        Text(
                            "Vyžaduje odemknout: ${prerequisiteNode.title}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }

            // Action Buttons
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Unlock with Skill Points
                    Button(
                        onClick = onUnlockWithSp,
                        enabled = canUnlockWithSp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("unlock_sp_${node.id}"),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🔮", fontSize = 12.sp)
                            Text("Za ${node.cost} SP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Option 2: Unlock with Accumulated XP
                    Button(
                        onClick = onUnlockWithXp,
                        enabled = canUnlockWithXp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            disabledContainerColor = Color(0xFF4C1D95).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("unlock_xp_${node.id}"),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⭐", fontSize = 12.sp)
                            Text("Za ${node.xpCost} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
