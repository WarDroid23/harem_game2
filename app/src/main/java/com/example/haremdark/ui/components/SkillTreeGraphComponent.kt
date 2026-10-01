package com.example.haremdark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.CharacterSkillCatalog
import com.example.haremdark.data.CharacterSkillNode
import com.example.haremdark.data.SkillNodeType
import com.example.haremdark.models.Character

@Composable
fun SkillTreeGraphComponent(
    character: Character,
    onUnlock: (CharacterSkillNode) -> Unit,
    onEnhanceWithResources: ((CharacterSkillNode) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val skills = CharacterSkillCatalog.ALL_SKILL_NODES
    val scrollState = rememberScrollState()
    var selectedNodeDetail by remember { mutableStateOf<CharacterSkillNode?>(null) }
    var tooltipNode by remember { mutableStateOf<CharacterSkillNode?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Legend Header Bar
        Surface(
            color = Color(0xFF161B22),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFFFFD700), label = "Masterováno")
                LegendItem(color = Color(0xFF00E5FF), label = "Dostupné")
                LegendItem(color = Color.Gray, label = "Uzamčeno")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Drawing connection lines between nodes
            Canvas(modifier = Modifier.fillMaxWidth().height(1000.dp)) {
                skills.forEach { node ->
                    if (node.reqNodeId != null) {
                        val parent = skills.find { it.id == node.reqNodeId }
                        if (parent != null) {
                            val start = Offset(parent.x * size.width / 100f, parent.y * 1000.dp.toPx() / 100f)
                            val end = Offset(node.x * size.width / 100f, node.y * 1000.dp.toPx() / 100f)
                            
                            val isUnlocked = character.unlockedCombatSkills.contains(node.id) || character.unlockedPassives.contains(node.id)
                            val isParentUnlocked = character.unlockedCombatSkills.contains(parent.id) || character.unlockedPassives.contains(parent.id)
                            
                            drawLine(
                                color = if (isUnlocked) Color(0xFFFFD700) else if (isParentUnlocked) Color(0xFF00E5FF).copy(alpha = 0.7f) else Color.DarkGray.copy(alpha = 0.4f),
                                start = start,
                                end = end,
                                strokeWidth = if (isUnlocked) 4.dp.toPx() else 2.dp.toPx(),
                                pathEffect = if (isUnlocked) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }

            // Placing nodes
            Box(modifier = Modifier.fillMaxWidth().height(1000.dp)) {
                skills.forEach { node ->
                    val isUnlocked = character.unlockedCombatSkills.contains(node.id) || character.unlockedPassives.contains(node.id)
                    val canUnlock = CharacterSkillCatalog.canUnlockNode(character, node)
                    
                    SkillGraphNode(
                        node = node,
                        isUnlocked = isUnlocked,
                        canUnlock = canUnlock,
                        onClick = { selectedNodeDetail = node },
                        onLongPress = { tooltipNode = node },
                        modifier = Modifier.offset(
                            x = (node.x * 2.8f).dp,
                            y = (node.y * 8.5f).dp
                        )
                    )
                }
            }
        }
    }

    // Long-Press Interactive Tooltip Dialog (Showing detailed cost and cooldown info)
    if (tooltipNode != null) {
        val node = tooltipNode!!
        val isUnlocked = character.unlockedCombatSkills.contains(node.id) || character.unlockedPassives.contains(node.id)
        val active = node.activeSkill

        AlertDialog(
            onDismissRequest = { tooltipNode = null },
            confirmButton = {
                TextButton(onClick = { tooltipNode = null }) {
                    Text("Rozumím", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(node.icon, fontSize = 28.sp)
                    Column {
                        Text("ℹ️ Detailní info: ${node.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        Text(node.nodeType.label, fontSize = 11.sp, color = Color(0xFFE1BEE7))
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(node.description, fontSize = 13.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = Color(0xFF221133),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚡ Náklady a časování:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF80AB))
                            Text("• Cena v SP: ${node.spCost} SP", fontSize = 12.sp, color = Color(0xFFFFD700))
                            if (active != null) {
                                Text("• Mana cena v boji: ${active.manaCost} MP", fontSize = 12.sp, color = Color(0xFF80D8FF))
                                Text("• Cooldown / Obnova: ${active.cooldownTurns} kola", fontSize = 12.sp, color = Color(0xFFFFE082))
                                Text("• Síla úderu: ${(active.powerMultiplier * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFF69F0AE))
                            } else {
                                Text("• Typ schopnosti: Pasivní perk (trvalý bonus)", fontSize = 12.sp, color = Color(0xFF00E5FF))
                            }
                        }
                    }

                    if (node.attackBonus > 0 || node.defenseBonus > 0 || node.hpBonus > 0) {
                        Surface(
                            color = Color(0xFF14201A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📈 Poskytované staty:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF69F0AE))
                                if (node.attackBonus > 0) Text("• Útok: +${node.attackBonus}", fontSize = 12.sp, color = Color.White)
                                if (node.defenseBonus > 0) Text("• Obrana: +${node.defenseBonus}", fontSize = 12.sp, color = Color.White)
                                if (node.hpBonus > 0) Text("• Maximální HP: +${node.hpBonus}", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Text(
                        text = if (isUnlocked) "Status: Masterováno ✓" else "Status: Neodemčeno",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color(0xFFFFD700) else Color.Gray
                    )
                }
            },
            containerColor = Color(0xFF160A1F),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Node Detail & Unlock/Enhance Dialog (Tap action)
    if (selectedNodeDetail != null) {
        val node = selectedNodeDetail!!
        val rank = CharacterSkillCatalog.getSkillRank(character, node.id)
        val isUnlocked = rank > 0
        val canUnlock = CharacterSkillCatalog.canUnlockNode(character, node)
        val (canEnhance, enhanceMsg) = CharacterSkillCatalog.canEnhanceNode(character, node)
        val nextRank = if (rank <= 0) 1 else rank + 1
        val upgradeCost = CharacterSkillCatalog.getUpgradeCostForRank(node, nextRank)

        AlertDialog(
            onDismissRequest = { selectedNodeDetail = null },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onEnhanceWithResources != null && (canUnlock || canEnhance)) {
                        Button(
                            onClick = {
                                onEnhanceWithResources(node)
                                selectedNodeDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text(
                                if (rank == 0) "✨ Odemknout (Suroviny)" else "🚀 Vylepšit na Rank $nextRank",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else if (!isUnlocked && canUnlock) {
                        Button(
                            onClick = {
                                onUnlock(node)
                                selectedNodeDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("Odemknout (${node.spCost} SP)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { selectedNodeDetail = null }) {
                            Text("Zavřít")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNodeDetail = null }) {
                    Text("Zavřít")
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(node.icon, fontSize = 28.sp)
                    Column {
                        Text(node.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (rank > 0) "⭐ RANK $rank / ${node.maxRank}" else "🔒 Uzamčeno",
                                    fontSize = 10.sp,
                                    color = if (rank > 0) Color(0xFFFFD700) else Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = node.nodeType.label,
                                fontSize = 10.sp,
                                color = Color(node.nodeType.badgeColorHex)
                            )
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(node.description, fontSize = 13.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = Color(0xFF221133),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚡ Bojové parametry (Rank ${rank.coerceAtLeast(1)}):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF80AB))
                            if (node.activeSkill != null) {
                                val act = node.activeSkill
                                Text("• Mana cena: ${act.manaCost} MP", fontSize = 12.sp, color = Color(0xFF80D8FF))
                                Text("• Cooldown: ${act.cooldownTurns} kola", fontSize = 12.sp, color = Color(0xFFFFE082))
                                if (act.powerMultiplier > 1f) {
                                    val multiplier = act.powerMultiplier + (rank.coerceAtLeast(1) - 1) * 0.25f
                                    Text("• Síla úderu: ${(multiplier * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFFFFD700))
                                }
                            }
                            if (node.attackBonus > 0) Text("• Bonus Útok: +${node.attackBonus * rank.coerceAtLeast(1)}", fontSize = 12.sp, color = Color(0xFFFF80AB))
                            if (node.defenseBonus > 0) Text("• Bonus Obrana: +${node.defenseBonus * rank.coerceAtLeast(1)}", fontSize = 12.sp, color = Color(0xFF80D8FF))
                            if (node.hpBonus > 0) Text("• Bonus HP: +${node.hpBonus * rank.coerceAtLeast(1)}", fontSize = 12.sp, color = Color(0xFF69F0AE))
                            if (node.lifestealPercent > 0) Text("• Lifesteal: +${node.lifestealPercent * rank.coerceAtLeast(1)}%", fontSize = 12.sp, color = Color(0xFFFF5252))
                        }
                    }

                    if (nextRank <= node.maxRank) {
                        Surface(
                            color = Color(0xFF16252E),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💎 Náklady na vylepšení (Rank $nextRank):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00E5FF))
                                Text("• Zlato: ${upgradeCost.goldCost}💰 • Temná E.: ${upgradeCost.darkEnergyCost}🔮 • ZK: ${upgradeCost.xpCost}", fontSize = 11.sp, color = Color.LightGray)
                                val matText = buildString {
                                    if (upgradeCost.darkShards > 0) append("💎 Střepy: ${upgradeCost.darkShards}  ")
                                    if (upgradeCost.manaEssence > 0) append("✨ Esence: ${upgradeCost.manaEssence}  ")
                                    if (upgradeCost.moonDust > 0) append("🌙 Prach: ${upgradeCost.moonDust}  ")
                                    if (upgradeCost.dragonBlood > 0) append("🩸 Krev: ${upgradeCost.dragonBlood}  ")
                                }
                                if (matText.isNotEmpty()) {
                                    Text("• Suroviny: $matText", fontSize = 11.sp, color = Color(0xFFFFD700))
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF1A1125),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            if (color == Color(0xFFFFD700)) {
                Box(modifier = Modifier.fillMaxSize().border(1.dp, Color.White, CircleShape))
            }
        }
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun SkillGraphNode(
    node: CharacterSkillNode,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Column(
        modifier = modifier
            .width(80.dp)
            .pointerInput(node.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .then(
                    if (isUnlocked) {
                        Modifier.background(Brush.radialGradient(listOf(Color(node.nodeType.badgeColorHex), Color(node.nodeType.badgeColorHex).copy(alpha = 0.6f))))
                    } else {
                        Modifier.background(if (canUnlock) Color(0xFF00E5FF).copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.5f))
                    }
                )
                .border(
                    width = if (isUnlocked) 2.5.dp else 2.dp,
                    color = if (isUnlocked) Color(0xFFFFD700) else if (canUnlock) Color(0xFF00E5FF).copy(alpha = pulseAlpha) else Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Text(node.icon, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = node.name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) Color(0xFFFFD700) else if (canUnlock) Color(0xFF00E5FF) else Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )

        if (!isUnlocked) {
            if (canUnlock) {
                Text(
                    text = "${node.spCost} SP",
                    fontSize = 8.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black
                )
            } else {
                Text(
                    text = "Uzamčeno",
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        } else {
            Text(
                text = "Master",
                fontSize = 8.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
