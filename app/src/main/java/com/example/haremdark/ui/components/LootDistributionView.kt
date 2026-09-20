package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.models.*

@Composable
fun PostBattleLootDistributionDialog(
    loot: LootDistributionResult,
    encounterTitle: String,
    onForgeFragment: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val efficiency = loot.efficiencyScore
    val rankColor = when (efficiency.rank) {
        "S+" -> Color(0xFFFFD700)
        "S" -> Color(0xFFFF4081)
        "A" -> Color(0xFF00E5FF)
        "B" -> Color(0xFF69F0AE)
        else -> Color(0xFFB0BEC5)
    }

    var showEfficiencyBreakdown by remember { mutableStateOf(false) }
    var selectedResourceDetail by remember { mutableStateOf<CraftingResource?>(null) }
    var forgedFragmentIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Pulsing glow animation for rank badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF13091B),
            border = BorderStroke(2.dp, Brush.verticalGradient(listOf(rankColor, Color(0xFF6A1B9A)))),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .padding(4.dp)
                .testTag("post_battle_loot_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Efficiency Rank & Header Banner
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = rankColor.copy(alpha = 0.2f),
                            border = BorderStroke(2.dp, rankColor),
                            modifier = Modifier
                                .size(64.dp)
                                .scale(pulseScale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = efficiency.rank,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = rankColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = efficiency.rankTitle.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = rankColor,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Bojová efektivita: ${efficiency.efficiencyPercent}% • Skóre: ${efficiency.totalScore} bodů",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE1BEE7)
                        )

                        Text(
                            text = "Střet: $encounterTitle",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. Efficiency Scoring Breakdown (Collapsible / Toggleable)
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E0E2B),
                        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEfficiencyBreakdown = !showEfficiencyBreakdown }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📊", fontSize = 14.sp)
                                    Text(
                                        text = "Rozpis bojové efektivity",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE1BEE7)
                                    )
                                }
                                Text(
                                    text = if (showEfficiencyBreakdown) "Skrýt ▲" else "Zobrazit detaily ▼",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF80AB),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedVisibility(visible = showEfficiencyBreakdown) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    efficiency.breakdown.forEach { metric ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color(0xFF14081E),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(metric.icon, fontSize = 13.sp)
                                                Text(
                                                    text = metric.label,
                                                    fontSize = 10.sp,
                                                    color = Color.White
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = metric.valueDisplay,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFFD700)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = rankColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = metric.grade,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = rankColor,
                                                        modifier = Modifier.padding(
                                                            horizontal = 4.dp,
                                                            vertical = 1.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Jackpot Bonus Banner (if triggered by S+ score)
                if (loot.jackpotRerollTriggered) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF331600),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎰", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "JACKPOT BOJOVÉ EFEKTIVITY!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "Za hodnocení S+ byl aktivován bonusový roll na legendární suroviny!",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFE082)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Equipment Fragments Section
                if (loot.fragmentsRewarded.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🧩", fontSize = 14.sp)
                                    Text(
                                        text = "Úlomky vzácné výbavy (${loot.fragmentsRewarded.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                                Text(
                                    text = "Efektivní bonus",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFF80AB)
                                )
                            }

                            loot.fragmentsRewarded.forEach { fragment ->
                                val isForged = forgedFragmentIds.contains(fragment.id)
                                EquipmentFragmentCard(
                                    fragment = fragment,
                                    isForged = isForged,
                                    onForge = {
                                        forgedFragmentIds = forgedFragmentIds + fragment.id
                                        onForgeFragment(fragment.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Crafting Resources Section
                if (loot.resourcesRewarded.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💎", fontSize = 14.sp)
                                    Text(
                                        text = "Získané řemeslné suroviny (${loot.resourcesRewarded.sumOf { it.count }} ks)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF80D8FF)
                                    )
                                }
                                Text(
                                    text = "Klikni pro popis",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }

                            // Flow-like grid of resources
                            val resourceChunks = remember(loot.resourcesRewarded) { loot.resourcesRewarded.chunked(2) }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (rowItems in resourceChunks) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        for (res in rowItems) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                CraftingResourcePill(
                                                    resource = res,
                                                    onClick = { selectedResourceDetail = res }
                                                )
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Currencies & Master XP Showcase
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF200E2B),
                        border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            LootStatBadge("🪙 Zlato", "+${loot.goldRewarded}", Color(0xFFFFD700))
                            LootStatBadge("🩸 Rubíny", "+${loot.bloodRubiesRewarded}", Color(0xFFFF1744))
                            LootStatBadge("⭐ ZK Pána", "+${loot.playerXpRewarded}", Color(0xFF80D8FF))
                        }
                    }
                }

                // 7. MVP Companion Recognition
                if (!loot.mvpName.isNullOrBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2A1024),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("👑", fontSize = 22.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "MVP BATTLE: ${loot.mvpName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "Získává bonus +${loot.mvpBonusXp} ZK za špičkovou efektivitu",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFF80AB)
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Confirm & Collect Action Button
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("collect_loot_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = rankColor)
                    ) {
                        Text(
                            text = "🏆 PŘEVZÍT VŠECHNU KOŘIST A ZK",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Resource Detail Dialog
    selectedResourceDetail?.let { res ->
        CraftingResourceDetailModal(
            resource = res,
            onDismiss = { selectedResourceDetail = null }
        )
    }
}

@Composable
fun EquipmentFragmentCard(
    fragment: EquipmentFragment,
    isForged: Boolean,
    onForge: () -> Unit
) {
    val rarityColor = when (fragment.rarity) {
        "Legendární" -> Color(0xFFFFD700)
        "Epický" -> Color(0xFFE040FB)
        "Vzácný" -> Color(0xFF40C4FF)
        else -> Color.White
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1B0B26),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(fragment.targetEquipmentIcon, fontSize = 20.sp)
                    Column {
                        Text(
                            text = fragment.targetEquipmentName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = rarityColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = fragment.statPreview,
                            fontSize = 9.sp,
                            color = Color(0xFFB0BEC5),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = rarityColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, rarityColor)
                ) {
                    Text(
                        text = "${fragment.currentCount}/${fragment.requiredCount} Úlomků",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = rarityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { fragment.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = rarityColor,
                trackColor = Color(0xFF2C133D)
            )

            if (fragment.isReadyToForge && !isForged) {
                Button(
                    onClick = onForge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(
                        "⚡ VYKOVAT CELÝ PŘEDMĚT TEĎ",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else if (isForged) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✅ PŘEDMĚT ÚSPĚŠNĚ UKOVÁN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5D6A7),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CraftingResourcePill(
    resource: CraftingResource,
    onClick: () -> Unit
) {
    val rarityColor = when (resource.rarity) {
        "Legendární" -> Color(0xFFFFD700)
        "Epický" -> Color(0xFFE040FB)
        "Vzácný" -> Color(0xFF40C4FF)
        else -> Color(0xFFB0BEC5)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1A0C24),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(resource.icon, fontSize = 16.sp)
                Column {
                    Text(
                        text = resource.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = rarityColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = resource.rarity,
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = rarityColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "+${resource.count}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = rarityColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CraftingResourceDetailModal(
    resource: CraftingResource,
    onDismiss: () -> Unit
) {
    val rarityColor = when (resource.rarity) {
        "Legendární" -> Color(0xFFFFD700)
        "Epický" -> Color(0xFFE040FB)
        "Vzácný" -> Color(0xFF40C4FF)
        else -> Color(0xFFB0BEC5)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF190B23),
            border = BorderStroke(1.5.dp, rarityColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(resource.icon, fontSize = 36.sp)
                Text(
                    text = resource.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = rarityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Vzácnost: ${resource.rarity} • Hodnota: ${resource.goldValue} zlata",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = rarityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = resource.description,
                    fontSize = 11.sp,
                    color = Color(0xFFE1BEE7),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = rarityColor)
                ) {
                    Text("Zavřít", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun LootStatBadge(
    title: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 9.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )
    }
}

/**
 * Reusable Fragment Forge view for viewing and forging all collected fragments.
 */
@Composable
fun FragmentForgeSection(
    fragments: List<EquipmentFragment>,
    onForge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF210F2E),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🔨", fontSize = 28.sp)
                Column {
                    Text(
                        text = "Kovárna úlomků výbavy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Sbírej úlomky z bojů s vysokou efektivitou a ukovej legendární zbraně a zbroje.",
                        fontSize = 10.sp,
                        color = Color(0xFFE1BEE7)
                    )
                }
            }
        }

        fragments.forEach { frag ->
            EquipmentFragmentCard(
                fragment = frag,
                isForged = false,
                onForge = { onForge(frag.id) }
            )
        }
    }
}
