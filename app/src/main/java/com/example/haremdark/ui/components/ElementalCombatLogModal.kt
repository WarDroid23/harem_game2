package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalBattleSummary
import com.example.haremdark.models.ElementalDamageBreakdown
import com.example.haremdark.models.ElementalMatchupType

/**
 * Full-featured modal displaying the Elemental Combat Log with turn-by-turn
 * mathematical calculations, affinity multiplier effectiveness, element matchup advantages,
 * and battle-wide affinity summary statistics.
 */
@Composable
fun ElementalCombatLogModal(
    logs: List<CombatLogEntry>,
    onDismiss: () -> Unit
) {
    val summary = remember(logs) { ElementalBattleSummary.fromLogs(logs) }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, WEAKNESS, RESISTED, PARTY_ONLY
    var selectedRound by remember { mutableStateOf<Int?>(null) }

    val filteredBreakdowns = remember(logs, selectedFilter, selectedRound) {
        summary.elementalTurnBreakdowns.filter { b ->
            val matchRound = selectedRound == null || b.round == selectedRound
            val matchFilter = when (selectedFilter) {
                "WEAKNESS" -> b.matchupType.isAdvantage
                "RESISTED" -> b.matchupType.isDisadvantage
                "PARTY_ONLY" -> b.isPlayerPartyAttacker
                else -> true
            }
            matchRound && matchFilter
        }
    }

    val availableRounds = remember(logs) {
        summary.elementalTurnBreakdowns.map { it.round }.distinct().sorted()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F0816),
            border = BorderStroke(1.5.dp, Color(0xFFB388FF).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 680.dp)
                .padding(vertical = 12.dp)
                .testTag("elemental_combat_log_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row
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
                            color = Color(0x33B388FF),
                            border = BorderStroke(1.dp, Color(0xFFB388FF)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Elementární Bojový Deník",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = "Kalkulace poškození & efektivita afinit",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_elemental_log_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                // Battle Affinity Performance Summary Card
                ElementalSummaryCard(summary = summary)

                // Filters Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == "ALL",
                                onClick = { selectedFilter = "ALL" },
                                label = { Text("Všechny údery (${summary.totalAttacksAnalyzed})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7E57C2),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "PARTY_ONLY",
                                onClick = { selectedFilter = "PARTY_ONLY" },
                                label = { Text("👑 Naše družina", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFAB47BC),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "WEAKNESS",
                                onClick = { selectedFilter = "WEAKNESS" },
                                label = { Text("💥 Slabiny (${summary.weaknessHitsTriggered})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE65100),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "RESISTED",
                                onClick = { selectedFilter = "RESISTED" },
                                label = { Text("🛡️ Odolnosti (${summary.resistedHitsEncountered})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF37474F),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Round Filter Chips if more than 1 round exists
                    if (availableRounds.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedRound == null) Color(0xFF4A148C) else Color(0xFF1E1029),
                                    border = BorderStroke(1.dp, if (selectedRound == null) Color(0xFFB388FF) else Color(0x33B388FF)),
                                    modifier = Modifier.clickable { selectedRound = null }
                                ) {
                                    Text(
                                        text = "Všechna kola",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            items(availableRounds) { round ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedRound == round) Color(0xFF4A148C) else Color(0xFF1E1029),
                                    border = BorderStroke(1.dp, if (selectedRound == round) Color(0xFFB388FF) else Color(0x33B388FF)),
                                    modifier = Modifier.clickable { selectedRound = round }
                                ) {
                                    Text(
                                        text = "Kolo $round",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Turn Breakdown List
                if (filteredBreakdowns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF150A21), RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🛡️", fontSize = 32.sp)
                            Text(
                                text = "Žádné záznamy neodpovídají zvolenému filtru.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF0A0410), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                            .testTag("elemental_breakdown_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredBreakdowns.reversed()) { breakdown ->
                            ElementalDamageBreakdownCard(breakdown = breakdown)
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_elemental_log_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF311B92)
                    )
                ) {
                    Text("Návrat na bojiště", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Banner displaying the global effectiveness of affinity and elemental interactions.
 */
@Composable
private fun ElementalSummaryCard(summary: ElementalBattleSummary) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E102E),
        border = BorderStroke(1.dp, Color(0xFF9575CD).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
                    Text("📊", fontSize = 13.sp)
                    Text(
                        text = "Celkový dopad afinit v tomto boji",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE1BEE7)
                    )
                }
                if (summary.bestAffinityContributor.isNotBlank()) {
                    Text(
                        text = "👑 MVP: ${summary.bestAffinityContributor}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total extra damage from affinity
                SummaryMetricItem(
                    icon = "📈",
                    label = "Afinita Extra DMG",
                    value = "+${summary.totalAffinityBonusDamageGained}",
                    valueColor = Color(0xFF69F0AE)
                )
                // Weakness hits
                SummaryMetricItem(
                    icon = "💥",
                    label = "Zásahů do slabin",
                    value = "${summary.weaknessHitsTriggered}x",
                    valueColor = Color(0xFFFFAB40)
                )
                // Resisted hits
                SummaryMetricItem(
                    icon = "🛡️",
                    label = "Ztlumených zásahů",
                    value = "${summary.resistedHitsEncountered}x",
                    valueColor = Color(0xFF90CAF9)
                )
                // Max hit
                SummaryMetricItem(
                    icon = "⚔️",
                    label = "Max poškození",
                    value = "${summary.highestElementalDamageHit}",
                    valueColor = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricItem(
    icon: String,
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

/**
 * Detailed turn-by-turn damage breakdown card.
 */
@Composable
fun ElementalDamageBreakdownCard(
    breakdown: ElementalDamageBreakdown,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val borderColor = when (breakdown.matchupType) {
        ElementalMatchupType.EXTREME_WEAKNESS, ElementalMatchupType.SUPER_EFFECTIVE -> Color(0xFFFF9800)
        ElementalMatchupType.RESISTED, ElementalMatchupType.EXTREME_RESISTANCE -> Color(0xFF546E7A)
        ElementalMatchupType.NEUTRAL -> Color(0xFF7E57C2).copy(alpha = 0.5f)
    }

    val cardBg = if (breakdown.isPlayerPartyAttacker) {
        Color(0xFF160E24)
    } else {
        Color(0xFF200E14)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
            .testTag("elemental_breakdown_card_${breakdown.turn}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Turn & Round Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (breakdown.isPlayerPartyAttacker) Color(0xFF4A148C) else Color(0xFFB71C1C),
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = if (breakdown.isPlayerPartyAttacker) "👑 Kolo ${breakdown.round} • Tah ${breakdown.turn}" else "👹 Kolo ${breakdown.round} • Tah ${breakdown.turn}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = breakdown.actionName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Matchup Advantage Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(breakdown.matchupType.tagColorHex).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(breakdown.matchupType.tagColorHex))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(breakdown.matchupType.icon, fontSize = 9.sp)
                        Text(
                            text = breakdown.matchupType.shortLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(breakdown.matchupType.tagColorHex)
                        )
                    }
                }
            }

            // Matchup Combatants Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Attacker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(getElementIcon(breakdown.attackerElement), fontSize = 14.sp)
                    Column {
                        Text(
                            text = breakdown.attackerName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = breakdown.attackerElement.name,
                            fontSize = 8.sp,
                            color = getElementColor(breakdown.attackerElement)
                        )
                    }
                }

                // Arrow indicator with total damage
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⚔️ ➔", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF5252).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "${breakdown.finalDamage} DMG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Defender
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = breakdown.defenderName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = breakdown.defenderElement.name,
                            fontSize = 8.sp,
                            color = getElementColor(breakdown.defenderElement)
                        )
                    }
                    Text(getElementIcon(breakdown.defenderElement), fontSize = 14.sp)
                }
            }

            // Calculation Chips Row (Base * Affinity * Matchup - Def)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0B0612),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Base
                    FormulaStepBadge(
                        label = "Základ",
                        value = "${breakdown.basePower}",
                        textColor = Color(0xFFCFD8DC)
                    )
                    Text("×", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))

                    // Character Affinity Multiplier
                    FormulaStepBadge(
                        label = "Afinita",
                        value = "${"%.2f".format(breakdown.affinityMultiplier)}x (${breakdown.affinityPercentString})",
                        textColor = Color(0xFFCE93D8),
                        highlight = breakdown.affinityMultiplier > 1.0f
                    )
                    Text("×", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))

                    // Element Matchup Multiplier
                    FormulaStepBadge(
                        label = "Živel",
                        value = "${"%.2f".format(breakdown.elementMatchupMultiplier)}x",
                        textColor = Color(breakdown.matchupType.tagColorHex),
                        highlight = breakdown.elementMatchupMultiplier != 1.0f
                    )
                    Text("-", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))

                    // Defense
                    FormulaStepBadge(
                        label = "Obrana",
                        value = "-${breakdown.defenseMitigation}",
                        textColor = Color(0xFF90A4AE)
                    )

                    // Elemental Synergy
                    if (breakdown.synergyMitigatedDamage > 0) {
                        Text("-", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                        FormulaStepBadge(
                            label = "Synergie",
                            value = "-${breakdown.synergyMitigatedDamage}",
                            textColor = Color(0xFF69F0AE),
                            highlight = true
                        )
                    }

                    Text("=", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))

                    // Result
                    FormulaStepBadge(
                        label = "Výsledek",
                        value = "${breakdown.finalDamage}",
                        textColor = Color(0xFFFFD54F),
                        highlight = true
                    )
                }
            }

            // Tactical Note & Affinity Gain Highlight
            if (breakdown.affinityBonusDamageGained > 0 || breakdown.matchupBonusDamageGained != 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("💡", fontSize = 10.sp)
                    Text(
                        text = breakdown.tacticalNote,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        color = Color(0xFFE1BEE7),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Méně" else "Více",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expandable Mathematical Breakdown Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF07030C), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📐 Kompletní matematický vzorec:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Text(
                        text = breakdown.formulaDisplay,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color(0xFF80D8FF)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Čistý přínos tréninku afinity:",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "+${breakdown.affinityBonusDamageGained} DMG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF69F0AE)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vliv elementární převahy:",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (breakdown.matchupBonusDamageGained >= 0) "+${breakdown.matchupBonusDamageGained} DMG" else "${breakdown.matchupBonusDamageGained} DMG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (breakdown.matchupBonusDamageGained >= 0) Color(0xFFFFAB40) else Color(0xFF90A4AE)
                        )
                    }

                    if (breakdown.isCritical) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kritický zásah:",
                                fontSize = 9.sp,
                                color = Color(0xFFFF5252)
                            )
                            Text(
                                text = "x${"%.2f".format(breakdown.critMultiplier)} (+65% DMG)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaStepBadge(
    label: String,
    value: String,
    textColor: Color,
    highlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

private fun getElementIcon(element: Element): String {
    return when (element) {
        Element.FIRE -> "🔥"
        Element.WATER -> "💧"
        Element.EARTH -> "🌿"
        Element.AIR -> "💨"
        Element.ICE -> "❄️"
        Element.LIGHTNING -> "⚡"
        Element.DARK -> "🔮"
        Element.HOLY -> "✨"
        Element.PHYSICAL -> "🗡️"
    }
}

private fun getElementColor(element: Element): Color {
    return when (element) {
        Element.FIRE -> Color(0xFFFF7043)
        Element.WATER -> Color(0xFF42A5F5)
        Element.EARTH -> Color(0xFF81C784)
        Element.AIR -> Color(0xFF80DEEA)
        Element.ICE -> Color(0xFF80D8FF)
        Element.LIGHTNING -> Color(0xFFFFD54F)
        Element.DARK -> Color(0xFFAB47BC)
        Element.HOLY -> Color(0xFFFFEE58)
        Element.PHYSICAL -> Color(0xFFB0BEC5)
    }
}
