package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.CombatStatusEffect
import kotlinx.coroutines.launch

/**
 * Filter mode for turn-by-turn combat logs.
 */
enum class TurnLogFilter(val label: String, val icon: String) {
    ALL("Všechny tahy", "📜"),
    DAMAGE_ONLY("Poškození", "💥"),
    STATUS_EFFECTS("Aplikované stavy", "✨"),
    ALLIES("Družina", "🗡️"),
    ENEMIES("Nepřátelé", "👹"),
    CRITICALS("Kritické zásahy", "⚡")
}

/**
 * Display layout style: Grouped turn cards vs flat chronological list.
 */
enum class TurnLogLayoutMode(val label: String, val icon: String) {
    TURN_BY_TURN("Po kolech", "🎯"),
    CHRONOLOGICAL("Časová osa", "⏱️")
}

/**
 * Aggregated statistics for a single combat turn.
 */
data class TurnSummaryData(
    val turnNumber: Int,
    val entries: List<CombatLogEntry>,
    val totalDamageDealtByAllies: Int,
    val totalDamageTakenFromEnemies: Int,
    val statusEffectsApplied: List<CombatStatusEffect>,
    val totalHealing: Int,
    val hasCritical: Boolean
)

/**
 * A comprehensive, scrollable turn-by-turn combat log component
 * displaying damage dealt, status effects applied, and tactical breakdown.
 */
@Composable
fun TurnByTurnCombatLogComponent(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 460.dp,
    initialFilter: TurnLogFilter = TurnLogFilter.ALL,
    initialLayoutMode: TurnLogLayoutMode = TurnLogLayoutMode.TURN_BY_TURN,
    showHeaderStats: Boolean = true,
    onClose: (() -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var layoutMode by remember { mutableStateOf(initialLayoutMode) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedTurnNumber by remember { mutableStateOf<Int?>(null) }
    var selectedStatusDetail by remember { mutableStateOf<CombatStatusEffect?>(null) }
    var showOnlyStatusApplied by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isMuted by SoundEffectManager.isMuted.collectAsState()

    // Aggregate turn data
    val turnSummaries = remember(logs) {
        val grouped = logs.groupBy { it.turn }
        grouped.map { (turnNum, turnEntries) ->
            val allyDmg = turnEntries.filter { it.type.startsWith("player") || it.type == "combo" }.sumOf { it.damageDealt }
            val enemyDmg = turnEntries.filter { it.type.startsWith("enemy") || (it.type == "system" && it.damageDealt > 0) }.sumOf { it.damageDealt }
            val healing = turnEntries.sumOf { it.healingReceived }
            val statuses = turnEntries.flatMap { entry ->
                entry.statusEffectsApplied.ifEmpty { extractFallbackStatuses(entry) }
            }
            val hasCrit = turnEntries.any { it.isCritical || it.message.contains("KRIT", ignoreCase = true) }

            TurnSummaryData(
                turnNumber = turnNum,
                entries = turnEntries,
                totalDamageDealtByAllies = allyDmg,
                totalDamageTakenFromEnemies = enemyDmg,
                statusEffectsApplied = statuses,
                totalHealing = healing,
                hasCritical = hasCrit
            )
        }.sortedByDescending { it.turnNumber }
    }

    // Overall Combat Metrics
    val totalDamageDealt = remember(logs) {
        logs.filter { it.type.startsWith("player") || it.type == "combo" }.sumOf { it.damageDealt }
    }
    val totalDamageTaken = remember(logs) {
        logs.filter { it.type.startsWith("enemy") }.sumOf { it.damageDealt }
    }
    val totalStatusesCount = remember(logs) {
        logs.sumOf { it.statusEffectsApplied.size.coerceAtLeast(extractFallbackStatuses(it).size) }
    }
    val totalCrits = remember(logs) {
        logs.count { it.isCritical || it.message.contains("KRIT", ignoreCase = true) }
    }
    val maxTurn = remember(logs) {
        logs.maxOfOrNull { it.turn } ?: 1
    }

    // Auto-scroll on new entries
    LaunchedEffect(logs.size, logs.firstOrNull()?.turn) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Filter logic for flat list
    val filteredLogs = remember(logs, selectedFilter, searchQuery, showOnlyStatusApplied) {
        logs.filter { entry ->
            val queryMatch = searchQuery.isBlank() ||
                entry.message.contains(searchQuery, ignoreCase = true) ||
                entry.actor.contains(searchQuery, ignoreCase = true) ||
                entry.actionName.contains(searchQuery, ignoreCase = true) ||
                entry.targetName.contains(searchQuery, ignoreCase = true)

            val statusList = entry.statusEffectsApplied.ifEmpty { extractFallbackStatuses(entry) }
            val statusMatch = if (showOnlyStatusApplied) statusList.isNotEmpty() else true

            val categoryMatch = when (selectedFilter) {
                TurnLogFilter.ALL -> true
                TurnLogFilter.DAMAGE_ONLY -> entry.damageDealt > 0
                TurnLogFilter.STATUS_EFFECTS -> statusList.isNotEmpty()
                TurnLogFilter.ALLIES -> entry.type.startsWith("player") || entry.type == "combo"
                TurnLogFilter.ENEMIES -> entry.type.startsWith("enemy")
                TurnLogFilter.CRITICALS -> entry.isCritical || entry.message.contains("KRIT", ignoreCase = true)
            }

            queryMatch && statusMatch && categoryMatch
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("turn_by_turn_combat_log"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA12081C)),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.6f),
                    Color(0xFF8E24AA).copy(alpha = 0.4f),
                    Color(0xFF1B0F24)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- TOP HEADER BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A148C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚔️", fontSize = 14.sp)
                    }

                    Column {
                        Text(
                            text = "Bojový Protokol Tah po Tahu",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Kolo $maxTurn | ${logs.size} událostí | $totalStatusesCount stavů",
                            fontSize = 10.sp,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Mute / Unmute sound button
                    IconButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            SoundEffectManager.toggleMute()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Zvuk vypnut" else "Zvuk zapnut",
                            tint = if (isMuted) Color(0xFFFF5252) else Color(0xFFFFD700),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Layout Mode Toggle Button (Turn-by-turn vs timeline)
                    FilledTonalButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            layoutMode = if (layoutMode == TurnLogLayoutMode.TURN_BY_TURN) {
                                TurnLogLayoutMode.CHRONOLOGICAL
                            } else {
                                TurnLogLayoutMode.TURN_BY_TURN
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF311B92),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "${layoutMode.icon} ${layoutMode.label}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onClose != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onClose()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // --- COMBAT STATS KPI SUMMARY ROW ---
            if (showHeaderStats) {
                CombatKpiSummaryRow(
                    totalTurns = maxTurn,
                    totalDamageDealt = totalDamageDealt,
                    totalDamageTaken = totalDamageTaken,
                    totalStatusesApplied = totalStatusesCount,
                    totalCriticalHits = totalCrits
                )
            }

            // --- SEARCH BAR & FILTERS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Hledat útok, postavu, stav (např. 'Krvácení')...",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("combat_log_search_field"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF5E35B1).copy(alpha = 0.5f),
                        focusedContainerColor = Color(0xFF1B0F24),
                        unfocusedContainerColor = Color(0xFF140B1A)
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Smazat", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                )

                // Quick toggle for status effects applied
                FilterChip(
                    selected = showOnlyStatusApplied,
                    onClick = {
                        HapticManager.vibrateClick()
                        showOnlyStatusApplied = !showOnlyStatusApplied
                    },
                    label = { Text("✨ Jen stavy", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8E24AA),
                        selectedLabelColor = Color(0xFFFFD700),
                        containerColor = Color(0xFF1E1028),
                        labelColor = Color.LightGray
                    ),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (showOnlyStatusApplied) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.height(34.dp)
                )
            }

            // --- FILTER CHIPS ROW ---
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TurnLogFilter.entries) { filter ->
                    val isSelected = (selectedFilter == filter)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF6A1B9A) else Color(0xFF1C0D26),
                        border = BorderStroke(
                            width = if (isSelected) 1.2.dp else 0.6.dp,
                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .testTag("combat_log_filter_${filter.name}")
                            .clickable {
                                HapticManager.vibrateClick()
                                selectedFilter = filter
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(filter.icon, fontSize = 10.sp)
                            Text(
                                text = filter.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFFFD700) else Color.LightGray
                            )
                        }
                    }
                }
            }

            // --- MAIN SCROLLABLE CONTENT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                if (layoutMode == TurnLogLayoutMode.TURN_BY_TURN) {
                    // Grouped Turn-by-Turn Accordion
                    if (turnSummaries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Žádné záznamy o tazích", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeight),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(turnSummaries, key = { it.turnNumber }) { turnSummary ->
                                val isExpanded = (expandedTurnNumber == turnSummary.turnNumber)

                                TurnAccordionCard(
                                    turnSummary = turnSummary,
                                    isExpanded = isExpanded,
                                    onToggleExpand = {
                                        HapticManager.vibrateClick()
                                        expandedTurnNumber = if (isExpanded) null else turnSummary.turnNumber
                                    },
                                    onSelectStatus = { status ->
                                        HapticManager.vibrateClick()
                                        selectedStatusDetail = status
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Chronological Feed of Individual Log Items
                    if (filteredLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Žádné odpovídající záznamy v této kategorii", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeight),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredLogs.size) { idx ->
                                val entry = filteredLogs[idx]
                                TurnLogDetailedCard(
                                    entry = entry,
                                    onSelectStatus = { status ->
                                        HapticManager.vibrateClick()
                                        selectedStatusDetail = status
                                    }
                                )
                            }
                        }
                    }
                }

                // Floating Quick-Scroll Navigation (Jump to Top & Jump to Bottom)
                val totalItemsCount = if (layoutMode == TurnLogLayoutMode.TURN_BY_TURN) turnSummaries.size else filteredLogs.size
                val canScrollUp = listState.firstVisibleItemIndex > 1
                val canScrollDown = totalItemsCount > 3 && listState.firstVisibleItemIndex < (totalItemsCount - 2)

                if (canScrollUp || canScrollDown) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canScrollUp) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xEE2A123D),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        HapticManager.vibrateClick()
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Na začátek",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (canScrollDown) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF6A1B9A).copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        HapticManager.vibrateClick()
                                        coroutineScope.launch {
                                            val target = (totalItemsCount - 1).coerceAtLeast(0)
                                            listState.animateScrollToItem(target)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Na konec (nejnovější tahy)",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for detailed status effect inspection
    if (selectedStatusDetail != null) {
        StatusEffectDetailPopup(
            effect = selectedStatusDetail!!,
            onDismiss = { selectedStatusDetail = null }
        )
    }
}

/**
 * Top bar summarizing combat KPIs: turns, damage dealt, taken, statuses, crits.
 */
@Composable
private fun CombatKpiSummaryRow(
    totalTurns: Int,
    totalDamageDealt: Int,
    totalDamageTaken: Int,
    totalStatusesApplied: Int,
    totalCriticalHits: Int
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF170922),
        border = BorderStroke(0.8.dp, Color(0xFF7B1FA2).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KpiMetricItem(label = "Tahy", value = "$totalTurns", icon = "🎯", color = Color(0xFFCE93D8))
            VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.1f))
            KpiMetricItem(label = "Uděleno", value = "$totalDamageDealt", icon = "💥", color = Color(0xFFFF8A80))
            VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.1f))
            KpiMetricItem(label = "Utrženo", value = "$totalDamageTaken", icon = "🛡️", color = Color(0xFFFF5252))
            VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.1f))
            KpiMetricItem(label = "Stavy", value = "$totalStatusesApplied", icon = "✨", color = Color(0xFF69F0AE))
            VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.1f))
            KpiMetricItem(label = "Krity", value = "$totalCriticalHits", icon = "⚡", color = Color(0xFFFFD700))
        }
    }
}

@Composable
private fun KpiMetricItem(
    label: String,
    value: String,
    icon: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(icon, fontSize = 9.sp)
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color.LightGray.copy(alpha = 0.7f)
        )
    }
}

/**
 * Accordion Card representing an entire combat turn.
 */
@Composable
private fun TurnAccordionCard(
    turnSummary: TurnSummaryData,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectStatus: (CombatStatusEffect) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("combat_turn_card_${turnSummary.turnNumber}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF180C22)),
        border = BorderStroke(
            width = if (isExpanded) 1.2.dp else 0.6.dp,
            color = if (isExpanded) Color(0xFFFFD700) else Color(0xFF8E24AA).copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Turn Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Turn Number Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF4A148C),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "KOLO ${turnSummary.turnNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Turn Damage Summary Pills
                    if (turnSummary.totalDamageDealtByAllies > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, Color(0xFF69F0AE))
                        ) {
                            Text(
                                text = "⚔️ +${turnSummary.totalDamageDealtByAllies} DMG",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF69F0AE),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (turnSummary.totalDamageTakenFromEnemies > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFB71C1C).copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, Color(0xFFFF5252))
                        ) {
                            Text(
                                text = "🛡️ -${turnSummary.totalDamageTakenFromEnemies} HP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (turnSummary.statusEffectsApplied.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF6A1B9A).copy(alpha = 0.4f),
                            border = BorderStroke(0.5.dp, Color(0xFFE040FB))
                        ) {
                            Text(
                                text = "✨ ${turnSummary.statusEffectsApplied.size} stavů",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE040FB),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${turnSummary.entries.size} akcí",
                        fontSize = 9.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Sbalit" else "Rozbalit",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Applied Statuses summary chips row on collapsed/expanded
            if (turnSummary.statusEffectsApplied.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(turnSummary.statusEffectsApplied) { effect ->
                        StatusEffectChip(effect = effect, onClick = { onSelectStatus(effect) })
                    }
                }
            }

            // Expanded List of Action Events in this Turn
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0616))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    turnSummary.entries.forEach { entry ->
                        TurnLogDetailedCard(entry = entry, onSelectStatus = onSelectStatus)
                    }
                }
            }
        }
    }
}

/**
 * Detailed Card for an individual combat log action entry.
 * Explicitly displays damage dealt, status effects applied, actor, target, and calculation.
 */
@Composable
fun TurnLogDetailedCard(
    entry: CombatLogEntry,
    onSelectStatus: (CombatStatusEffect) -> Unit
) {
    var showMathDetail by remember { mutableStateOf(false) }

    val isCritical = entry.isCritical || entry.message.contains("KRIT", ignoreCase = true)
    val isPlayerOrAlly = entry.type.startsWith("player") || entry.type == "combo"
    val isEnemy = entry.type.startsWith("enemy")
    val isHeal = entry.type.contains("heal") || entry.healingReceived > 0
    val isDefend = entry.type.contains("defend") || entry.message.contains("KRYT")

    val accentColor = when {
        isCritical -> Color(0xFFFFD700)
        isHeal -> Color(0xFF69F0AE)
        isDefend -> Color(0xFF64B5F6)
        isPlayerOrAlly -> Color(0xFFFFAB40)
        isEnemy -> Color(0xFFFF5252)
        entry.type == "victory" -> Color(0xFFFFD700)
        entry.type == "defeat" -> Color(0xFFD50000)
        else -> Color(0xFFCE93D8)
    }

    val containerBg = when {
        isCritical -> Color(0xFF261403)
        isHeal -> Color(0xFF062012)
        isDefend -> Color(0xFF08192A)
        isPlayerOrAlly -> Color(0xFF1E1026)
        isEnemy -> Color(0xFF240A10)
        else -> Color(0xFF13091A)
    }

    val statusList = remember(entry) {
        entry.statusEffectsApplied.ifEmpty { extractFallbackStatuses(entry) }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerBg,
        border = BorderStroke(
            width = if (isCritical) 1.dp else 0.5.dp,
            color = accentColor.copy(alpha = if (isCritical) 0.8f else 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Main Top Row: Turn tag, Action Name, Actor -> Target, and Damage Dealt Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Turn Tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, accentColor)
                    ) {
                        Text(
                            text = "T.${entry.turn}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Actor and Target Pill
                    if (entry.actor.isNotBlank()) {
                        Text(
                            text = entry.actor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (entry.targetName.isNotBlank() && entry.targetName != entry.actor) {
                            Text("➡️", fontSize = 8.sp)
                            Text(
                                text = entry.targetName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }

                // Explicit Damage Dealt Pill / Heal Pill
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (entry.damageDealt > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isPlayerOrAlly) Color(0xFFB71C1C) else Color(0xFFD50000),
                            border = if (isCritical) BorderStroke(1.dp, Color(0xFFFFD700)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (isCritical) {
                                    Text("💥", fontSize = 8.sp)
                                }
                                Text(
                                    text = "-${entry.damageDealt} DMG",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (entry.healingReceived > 0 || isHeal) {
                        val healVal = if (entry.healingReceived > 0) entry.healingReceived else 25
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1B5E20)
                        ) {
                            Text(
                                text = "+$healVal HP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB9F6CA),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Action Message Body
            Text(
                text = entry.message,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = Color.White.copy(alpha = 0.95f),
                fontWeight = if (isCritical) FontWeight.SemiBold else FontWeight.Normal
            )

            // Applied Status Effects Section
            if (statusList.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Aplikované stavy:",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCE93D8)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(statusList) { status ->
                            StatusEffectChip(effect = status, onClick = { onSelectStatus(status) })
                        }
                    }
                }
            }

            // Elemental Synergy & Weakness Info
            if (entry.elementalBreakdown != null) {
                val elem = entry.elementalBreakdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFF004D40).copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, Color(0xFF64FFDA))
                    ) {
                        Text(
                            text = "🌪️ ${elem.attackerElement.name} ➡️ ${elem.defenderElement.name} (${elem.matchupType.title} ${elem.elementMatchupMultiplier}x)",
                            fontSize = 8.sp,
                            color = Color(0xFF64FFDA),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (elem.synergyMitigatedDamage > 0) {
                        Text(
                            text = "🛡️ Zmírněno: -${elem.synergyMitigatedDamage} DMG",
                            fontSize = 8.sp,
                            color = Color(0xFF80D8FF)
                        )
                    }
                }
            }

            // Expandable Damage Formula and Narrative Quote
            if (!entry.damageCalculation.isNullOrBlank() || !entry.narrativeText.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMathDetail = !showMathDetail },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showMathDetail) "Skrýt matematický výpočet" else "📐 Zobrazit výpočet a popis",
                        fontSize = 8.sp,
                        color = Color(0xFF80D8FF),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (showMathDetail) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (showMathDetail) {
                    if (!entry.narrativeText.isNullOrBlank()) {
                        Text(
                            text = "\"${entry.narrativeText}\"",
                            fontSize = 9.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color(0xFFFFD54F).copy(alpha = 0.9f)
                        )
                    }

                    if (!entry.damageCalculation.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📐 ${entry.damageCalculation}",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF80D8FF),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chip badge rendering an applied status effect with icon, name, and duration.
 */
@Composable
fun StatusEffectChip(
    effect: CombatStatusEffect,
    onClick: () -> Unit
) {
    val isBuff = effect.isBuff
    val chipBg = if (isBuff) Color(0xFF003314) else Color(0xFF330811)
    val chipBorder = if (isBuff) Color(0xFF69F0AE) else Color(0xFFFF5252)
    val textColor = if (isBuff) Color(0xFFB9F6CA) else Color(0xFFFF8A80)

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = chipBg,
        border = BorderStroke(0.6.dp, chipBorder),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("status_effect_chip_${effect.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(effect.icon.ifBlank { if (isBuff) "✨" else "🩸" }, fontSize = 9.sp)
            Text(
                text = "${effect.name} (${effect.durationTurns}k)",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Detailed inspection popup for an applied status effect.
 */
@Composable
fun StatusEffectDetailPopup(
    effect: CombatStatusEffect,
    onDismiss: () -> Unit
) {
    val isBuff = effect.isBuff
    val accentColor = if (isBuff) Color(0xFF69F0AE) else Color(0xFFFF5252)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF140B1A),
            border = BorderStroke(1.2.dp, accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Text(effect.icon, fontSize = 22.sp)
                        Column {
                            Text(
                                text = effect.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isBuff) "🌟 Posilující Stav (Buff)" else "🩸 Oslabující Stav (Debuff)",
                                fontSize = 10.sp,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.8.dp)

                // Attributes Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Trvání", fontSize = 9.sp, color = Color.Gray)
                        Text("${effect.durationTurns} kola", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Hodnota / Síla", fontSize = 9.sp, color = Color.Gray)
                        Text(if (effect.value > 0) "+${effect.value}%" else "Efekt", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                    Column {
                        Text("Typ účinku", fontSize = 9.sp, color = Color.Gray)
                        Text(effect.type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }

                // Description
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1026),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = effect.description.ifBlank { "Tento stav ovlivňuje bojovníka po zadaný počet kol a modifikuje jeho bojové statistiky." },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text("Rozumím", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Intelligent helper to extract status effects from message text if they were not explicitly
 * attached in older entries or hazard events.
 */
private fun extractFallbackStatuses(entry: CombatLogEntry): List<CombatStatusEffect> {
    val list = mutableListOf<CombatStatusEffect>()
    val msg = entry.message

    if (msg.contains("Krvácení", ignoreCase = true) || msg.contains("Krvav", ignoreCase = true)) {
        list.add(CombatStatusEffect("bleed_auto", "Krvácení", "🩸", "BLEED", value = 15, durationTurns = 2, description = "Způsobuje zranění na začátku každého kola."))
    }
    if (msg.contains("Popálení", ignoreCase = true) || msg.contains("Spálen", ignoreCase = true)) {
        list.add(CombatStatusEffect("burn_auto", "Popálení", "🔥", "BURN", value = 18, durationTurns = 2, description = "Živelné zranění ohněm každé kolo."))
    }
    if (msg.contains("Omráčení", ignoreCase = true) || msg.contains("Omráčen", ignoreCase = true)) {
        list.add(CombatStatusEffect("stun_auto", "Omráčení", "💫", "STUN", value = 1, durationTurns = 1, description = "Postava ztrácí možnost jednat v tomto kole."))
    }
    if (msg.contains("Jed", ignoreCase = true) || msg.contains("Otráven", ignoreCase = true)) {
        list.add(CombatStatusEffect("poison_auto", "Jed", "☠️", "POISON", value = 12, durationTurns = 3, description = "Pomalé toxické poškození redukující odolnosti."))
    }
    if (msg.contains("Zmrazení", ignoreCase = true) || msg.contains("Zmrazen", ignoreCase = true)) {
        list.add(CombatStatusEffect("freeze_auto", "Zmrazení", "❄️", "FREEZE", value = 20, durationTurns = 2, description = "Snížená rychlost a zvýšené poškození bleskem."))
    }
    if (msg.contains("Soustředěný cíl", ignoreCase = true) || msg.contains("Soustředěná palba", ignoreCase = true)) {
        list.add(CombatStatusEffect("mark_auto", "Soustředěný cíl", "🎯", "DEBUFF", value = 35, durationTurns = 2, description = "Zvýšená zranitelnost o 35%."))
    }
    if (msg.contains("Totální zteč", ignoreCase = true)) {
        list.add(CombatStatusEffect("assault_auto", "Totální zteč", "⚔️", "ATK_BUFF", value = 30, durationTurns = 2, description = "+30% útočné poškození."))
    }
    if (msg.contains("Afinitní schopnost", ignoreCase = true)) {
        list.add(CombatStatusEffect("affinity_auto", "Pouto Afinity", "💖", "AFFINITY_BUFF", value = 40, durationTurns = 3, description = "+40% bojová síla z lásky."))
    }

    return list
}
