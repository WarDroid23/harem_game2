package com.example.haremdark.ui.components

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.*

/**
 * Top Enemy Battle Line with clickable target selection and attack animation lunges.
 */
@Composable
fun EnemyBattleLine(
    enemies: List<CombatEnemy>,
    selectedTargetIndex: Int,
    onSelectTarget: (Int) -> Unit,
    attackerEnemyIndex: Int? = -1,
    hitTargetEnemyIndex: Int? = -1,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(enemies.indices.toList()) { index ->
            val enemy = enemies[index]
            val isSelected = (index == selectedTargetIndex && enemy.isAlive)
            val isAttacking = (index == attackerEnemyIndex)
            val isHit = (index == hitTargetEnemyIndex)

            val infiniteTransition = rememberInfiniteTransition()
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val lungeOffsetY = if (isAttacking) 14.dp else 0.dp
            val shakeOffsetX = if (isHit) 6.dp else 0.dp

            Card(
                modifier = Modifier
                    .width(115.dp)
                    .offset(x = shakeOffsetX, y = lungeOffsetY)
                    .scale(if (isAttacking) 1.08f else pulseScale)
                    .clickable(enabled = enemy.isAlive) { onSelectTarget(index) }
                    .border(
                        width = if (isAttacking) 3.dp else if (isSelected) 2.5.dp else 1.dp,
                        color = if (isAttacking) Color(0xFFFF9100) else if (isSelected) Color(0xFFFF1744) else if (enemy.isBoss) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (!enemy.isAlive) Color(0xFF1E1012).copy(alpha = 0.5f)
                    else if (isHit) Color(0xFF880E4F)
                    else if (isSelected) Color(0xFF381016)
                    else Color(0xFF1F121A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Header: Target reticle & Icon
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = if (enemy.isBoss) Color(0xFFFFD700).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(enemy.icon, fontSize = 20.sp, modifier = Modifier.padding(4.dp))
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = "Cíl",
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Enemy Name & Title
                    Text(
                        text = enemy.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (enemy.isAlive) Color.White else Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (enemy.isBoss) "👑 BOSS" else enemy.title,
                        fontSize = 9.sp,
                        color = if (enemy.isBoss) Color(0xFFFFD700) else Color(0xFFB0BEC5),
                        fontWeight = FontWeight.SemiBold
                    )

                    // HP Bar
                    if (enemy.isAlive) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { enemy.hpPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (enemy.hpPercent > 0.5f) Color(0xFFE53935) else Color(0xFFFF1744),
                                trackColor = Color(0xFF3E1B24),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("HP", fontSize = 8.sp, color = Color.Gray)
                                Text("${enemy.hp}/${enemy.maxHp}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text("☠️ PADL", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }

                    // Status Effects row
                    if (enemy.statusEffects.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            enemy.statusEffects.take(3).forEach { eff ->
                                Text(eff.icon, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Party Formation Row showing all 1-4 harem heroes with attack animation lunges.
 */
@Composable
fun PartyFormationRow(
    party: List<PartyMember>,
    activeTurnIndex: Int,
    selectedTargetAllyIndex: Int,
    onSelectAlly: (Int) -> Unit,
    attackerPartyIndex: Int? = -1,
    hitTargetPartyIndex: Int? = -1,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(party.indices.toList()) { index ->
            val member = party[index]
            val isActiveTurn = (index == activeTurnIndex && member.isAlive)
            val isSelectedAlly = (index == selectedTargetAllyIndex)
            val isAttacking = (index == attackerPartyIndex)
            val isHit = (index == hitTargetPartyIndex)

            val borderBrush = when {
                isAttacking -> Brush.sweepGradient(listOf(Color(0xFFFF4081), Color(0xFFFFD700), Color(0xFFFF4081)))
                isActiveTurn -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4081), Color(0xFFFFD700)))
                isSelectedAlly -> Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF69F0AE)))
                else -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)))
            }

            val lungeOffsetY = if (isAttacking) (-14).dp else 0.dp
            val shakeOffsetX = if (isHit) 6.dp else 0.dp

            Card(
                modifier = Modifier
                    .width(130.dp)
                    .offset(x = shakeOffsetX, y = lungeOffsetY)
                    .scale(if (isAttacking) 1.08f else 1f)
                    .clickable(enabled = member.isAlive) { onSelectAlly(index) }
                    .border(
                        width = if (isAttacking) 3.dp else if (isActiveTurn) 2.5.dp else if (isSelectedAlly) 1.5.dp else 1.dp,
                        brush = borderBrush,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (!member.isAlive) Color(0xFF1B141E).copy(alpha = 0.5f)
                    else if (isHit) Color(0xFF880E4F)
                    else if (isActiveTurn) Color(0xFF38152D)
                    else Color(0xFF1E1022)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Portrait, Role & Turn Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, if (isActiveTurn) Color(0xFFFFD700) else Color.Gray, CircleShape)
                        ) {
                            if (member.isPlayer) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFF4A148C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👑", fontSize = 18.sp)
                                }
                            } else {
                                val portraitRes = StaticData.getPortraitForArchetype(member.archetypeId)
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(portraitRes)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = member.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    error = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color(0xFF311B92)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(member.role.icon, fontSize = 16.sp)
                                        }
                                    }
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (isActiveTurn) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD700)
                                ) {
                                    Text("NA TAHU", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            } else {
                                Text(member.role.icon, fontSize = 12.sp)
                            }

                            if (member.isDefending) {
                                Text("🛡️ Kryt", fontSize = 8.sp, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Name
                    Text(
                        text = member.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (member.isAlive) Color.White else Color.Gray
                    )

                    if (member.isAlive) {
                        // HP Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { member.hpPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (member.hpPercent > 0.4f) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                trackColor = Color(0xFF1B381D),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("HP", fontSize = 8.sp, color = Color(0xFF81C784))
                                Text("${member.hp}/${member.maxHp}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Mana Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { member.manaPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF29B6F6),
                                trackColor = Color(0xFF0D253D),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("MP", fontSize = 8.sp, color = Color(0xFF80D8FF))
                                Text("${member.mana}/${member.maxMana}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Text("💀 Zraněna / V bezvědomí", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    // Status Icons
                    if (member.statusEffects.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            member.statusEffects.take(3).forEach { eff ->
                                Text(eff.icon, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Harem Ultimate Combo Gauge.
 */
@Composable
fun HaremUltimateComboGauge(
    gauge: Int,
    maxGauge: Int,
    onTriggerCombo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = gauge >= maxGauge
    val progress = (gauge.toFloat() / maxGauge.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1C0D20),
        border = BorderStroke(1.dp, if (isReady) Color(0xFFFF4081) else Color.White.copy(alpha = 0.15f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isReady) "✨ HARÉMOVÉ COMBO PŘIPRAVENO!" else "💖 Společná vášeň & Pouto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isReady) Color(0xFFFF4081) else Color(0xFFE1BEE7)
                    )
                    Text("$gauge/$maxGauge", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF80AB))
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFF4081),
                    trackColor = Color(0xFF381024),
                )
            }

            Button(
                onClick = onTriggerCombo,
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63),
                    disabledContainerColor = Color(0xFF4A1E32)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💥 SPUSTIT COMBO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) Color.White else Color.Gray
                )
            }
        }
    }
}

/**
 * Command Console with Action Tabs: Attack, Skills, Defend, Items.
 */
@Composable
fun PartyCommandConsole(
    activeMember: PartyMember?,
    targetEnemyName: String,
    playerItems: List<InventoryItem>,
    onBasicAttack: () -> Unit,
    onSkillSelect: (PartyCombatSkill) -> Unit,
    onDefend: () -> Unit,
    onUseItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Active Character Info & Action Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(activeMember?.favoriteWeaponIcon ?: "⚔️", fontSize = 16.sp)
                    Text(
                        text = "Rozkaz pro: ${activeMember?.name ?: "Bojovnici"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFFD700)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Cíl: $targetEnemyName",
                        fontSize = 10.sp,
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Tabs Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("⚔️ Útok", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("⚡ Dovednosti", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("🛡️ Kryt", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = { Text("🧪 Lektvary", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Basic Attack View
                    Button(
                        onClick = onBasicAttack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Provést útok na $targetEnemyName (Síla: ${activeMember?.attack ?: 20})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                1 -> {
                    // Skills List
                    val skills = activeMember?.skills ?: emptyList()
                    if (skills.isEmpty()) {
                        Text("Tato bojovnice nemá žádné speciální dovednosti.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(skills) { skill ->
                                val canAfford = (activeMember?.mana ?: 0) >= skill.manaCost
                                Card(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .clickable(enabled = canAfford) { onSkillSelect(skill) }
                                        .border(1.dp, if (canAfford) Color(0xFFAB47BC) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (canAfford) Color(0xFF2A1032) else Color(0xFF1E1622)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${skill.icon} ${skill.name}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (canAfford) Color.White else Color.Gray, maxLines = 1)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF0288D1).copy(alpha = 0.3f)
                                            ) {
                                                Text("${skill.manaCost} MP", fontSize = 9.sp, color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }

                                        Text(
                                            text = skill.description,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Defend View
                    Button(
                        onClick = onDefend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Obranný postoj (Sníží škody o 60% & Obnoví +15 MP)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                3 -> {
                    // Consumable Potions
                    val potions = playerItems.filter { it.count > 0 && it.category == "combat" }
                    if (potions.isEmpty()) {
                        Text("V inventáři nemáš žádné bojové lektvary.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(4.dp))
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(potions) { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1B3820),
                                    border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                    modifier = Modifier
                                        .clickable { onUseItem(item) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(item.icon.ifBlank { "🧪" }, fontSize = 16.sp)
                                        Column {
                                            Text("${item.name} (${item.count}x)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            Text(item.effectDescription.ifBlank { "+50 HP" }, fontSize = 9.sp, color = Color(0xFFA5D6A7))
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

/**
 * Victory Modal with detailed performance rank, MVP showcase, item drops, and companion XP gains.
 */
@Composable
fun PartyCombatVictoryDialog(
    session: PartyCombatSession,
    onDismiss: () -> Unit
) {
    val rewards = session.rewards
    val rankColor = when (rewards?.rank) {
        "S+" -> Color(0xFFFFD700)
        "S" -> Color(0xFFFF4081)
        "A" -> Color(0xFF00E5FF)
        "B" -> Color(0xFF69F0AE)
        else -> Color(0xFFB0BEC5)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF160A1F),
            border = BorderStroke(2.dp, rankColor),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Rank & Victory Header
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = rankColor.copy(alpha = 0.2f),
                            border = BorderStroke(2.dp, rankColor),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = rewards?.rank ?: "S",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = rankColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rewards?.rankTitle ?: "SLAVNÉ VÍTĚZSTVÍ!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = rankColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Střet: ${session.encounterTitle} • Skóre: ${rewards?.score ?: 1000} bodů",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. MVP Showcase Banner
                if (rewards?.mvpName != null && rewards.mvpName.isNotBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2C1022),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("👑", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "MVP BOJE: ${rewards.mvpName}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "Získává bonus +50% ZK a dodatečnou náklonnost",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFF80AB)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Currencies & Master Gains Grid
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF200E2B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            RewardPill("🪙 Zlato", "+${rewards?.gold ?: 0}", Color(0xFFFFD700))
                            RewardPill("🩸 Rubíny", "+${rewards?.bloodRubies ?: 0}", Color(0xFFFF1744))
                            RewardPill("⭐ XP Pána", "+${rewards?.playerXp ?: 0}", Color(0xFF80D8FF))
                            RewardPill("🏆 Prestiž", "+${rewards?.prestigeGain ?: 0}", Color(0xFFFF4081))
                        }
                    }
                }

                // 4. Companions Combat XP Gains
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF26122C),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✨ Získané bojové ZK pro společnice:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE1BEE7)
                            )
                            session.party.filter { !it.isPlayer }.forEach { girl ->
                                val xpGained = rewards?.characterXpGains?.get(girl.id) ?: (rewards?.playerXp?.div(2) ?: 40)
                                val isMvp = (girl.id == rewards?.mvpCharacterId)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(girl.name, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                        if (isMvp) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFD700).copy(alpha = 0.25f)) {
                                                Text("MVP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text("+$xpGained ZK ⚔️", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 5. Item Drops / Loot Box
                if (rewards != null && rewards.itemDropDetails.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF281120),
                            border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🎁 Kořist z bojiště (${rewards.itemDropDetails.size} předmětů):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                rewards.itemDropDetails.forEach { item ->
                                    val itemColor = when (item.rarity) {
                                        "LEGENDARY", "Legendární" -> Color(0xFFFFD700)
                                        "EPIC", "Epický" -> Color(0xFFE040FB)
                                        "RARE", "Vzácný" -> Color(0xFF40C4FF)
                                        else -> Color.White
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(item.icon, fontSize = 14.sp)
                                            Column {
                                                Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = itemColor)
                                                Text(item.description, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Text("${item.count}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Action Button
                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = rankColor)
                    ) {
                        Text("🏆 PŘEVZÍT ODMĚNY A ZK", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Defeat Modal.
 */
@Composable
fun PartyCombatDefeatDialog(
    session: PartyCombatSession,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1C0D12),
            border = BorderStroke(2.dp, Color(0xFFE53935)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("💀", fontSize = 48.sp)
                Text(
                    text = "DRUŽINA PADLA",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE53935)
                )

                Text(
                    text = "Nepřátelé byli příliš silní. Vaše zraněné dívky ustoupily do komnat k ošetření.",
                    fontSize = 12.sp,
                    color = Color(0xFFFFCDD2),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("USTOUPIT DO DOMINIA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RewardPill(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 9.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

/**
 * Combat Log History Modal for Party Battles.
 */
@Composable
fun PartyCombatLogModal(
    logs: List<CombatLogEntry>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF140B1A),
            border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📜 Záznam z probíhajícího boje",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFD700)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0C0610), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val reversedLogs = logs.reversed()
                    items(reversedLogs) { entry ->
                        val logColor = when (entry.type) {
                            "player_attack", "player_spell" -> Color(0xFFFFCC80)
                            "enemy_attack", "enemy_special" -> Color(0xFFFF8A80)
                            "player_heal" -> Color(0xFFA5D6A7)
                            "player_support" -> Color(0xFF80D8FF)
                            "victory" -> Color(0xFFFFD700)
                            "defeat" -> Color(0xFFE53935)
                            else -> Color(0xFFE0E0E0)
                        }
                        Text(
                            text = "[Kolo ${entry.turn}] ${entry.message}",
                            color = logColor,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Zavřít záznam")
                }
            }
        }
    }
}
