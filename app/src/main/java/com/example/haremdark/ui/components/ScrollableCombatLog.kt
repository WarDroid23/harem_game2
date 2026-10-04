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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.CombatLogEntry
import kotlinx.coroutines.launch

/**
 * Filter options for the scrollable combat log.
 */
enum class CombatLogCategoryFilter(val label: String, val icon: String) {
    ALL("Vše", "📜"),
    ALLIES("Spojenci", "🗡️"),
    ENEMIES("Nepřátelé", "👹"),
    TACTICS("Taktika & Kryt", "🛡️"),
    SPECIAL("Kouzla & Komba", "✨")
}

/**
 * A highly interactive, styled scrollable combat log view that displays
 * real-time event messages during combat encounters to provide tactical feedback.
 */
@Composable
fun ScrollableCombatLogView(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 240.dp,
    showTacticalAdvice: Boolean = true,
    onOpenFullscreen: (() -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf(CombatLogCategoryFilter.ALL) }
    var expandedDetailIndex by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isMuted by SoundEffectManager.isMuted.collectAsState()

    // Smooth auto-scroll to latest turn action when new logs arrive
    LaunchedEffect(logs.size, logs.firstOrNull()?.turn) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Filtered logs list
    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            CombatLogCategoryFilter.ALLIES -> logs.filter { it.type.startsWith("player") || it.type.contains("support") || it.type == "combo" }
            CombatLogCategoryFilter.ENEMIES -> logs.filter { it.type.startsWith("enemy") }
            CombatLogCategoryFilter.TACTICS -> logs.filter { it.type.contains("defend") || it.type.contains("heal") || it.type.contains("retreat") }
            CombatLogCategoryFilter.SPECIAL -> logs.filter { it.type.contains("spell") || it.type.contains("special") || it.elementalBreakdown != null }
            CombatLogCategoryFilter.ALL -> logs
        }
    }

    // Tactical feedback recommendation based on the latest battle events
    val tacticalAdvice = remember(logs.firstOrNull()) {
        val latest = logs.firstOrNull()
        when {
            latest == null -> "⚔️ Bitva začíná! Sleduj tahy soupeře a využívej slabin."
            latest.message.contains("KRIT", ignoreCase = true) -> "💥 Kritický zásah! Využijte oslabení k následnému útoku."
            latest.type.startsWith("enemy") && (latest.damageDealt > 25) -> "⚠️ Nepřítel útočí velkou silou! Zvažte 'Defend' pro -65% poškození nebo 'Heal'."
            latest.elementalBreakdown?.matchupType?.isAdvantage == true -> "✨ Zasažena elementární slabina! Opakujte útok pro synergie."
            latest.message.contains("KRYT") || latest.type == "player_defend" -> "🛡️ Obranný postoj aktivní! Příchozí zranění je redukováno o 65%."
            latest.type == "victory" -> "🏆 Vítězství! Nepřátelská linie byla prolomena."
            latest.type == "defeat" -> "💀 Porážka! Příště upravte formaci a strategii."
            else -> "🎯 Taktická rada: Sledujte stavové efekty a využívejte elementární synergie."
        }
    }

    // Pulsing live indicator dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scrollable_combat_log_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE12081C)),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.5f),
                    Color(0xFF8E24AA).copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- HEADER BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "⚔️ Bojový Protokol",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    // Real-Time Live Indicator Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2E7D32).copy(alpha = 0.3f),
                        border = BorderStroke(0.8.dp, Color(0xFF69F0AE).copy(alpha = pulseAlpha))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF69F0AE).copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = "LIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF69F0AE)
                            )
                        }
                    }

                    Badge(containerColor = Color(0xFF4A148C)) {
                        Text("${logs.size}", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Audio Sound Effects Toggle
                    IconButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            SoundEffectManager.toggleMute()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Zvuk vypnut" else "Zvuk zapnut",
                            tint = if (isMuted) Color(0xFFFF5252) else Color(0xFFFFD700),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Fullscreen modal button
                    if (onOpenFullscreen != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onOpenFullscreen()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Zvětšit",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- REAL-TIME TACTICAL ADVICE BANNER ---
            if (showTacticalAdvice) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1028),
                    border = BorderStroke(0.8.dp, Color(0xFFFF80AB).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = tacticalAdvice,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- FILTER CHIPS ROW ---
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CombatLogCategoryFilter.entries) { filter ->
                    val isSelected = (selectedFilter == filter)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF6A1B9A) else Color(0xFF1B0F24),
                        border = BorderStroke(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.clickable {
                            HapticManager.vibrateClick()
                            selectedFilter = filter
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(filter.icon, fontSize = 9.sp)
                            Text(
                                text = filter.label,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFFFD700) else Color.LightGray
                            )
                        }
                    }
                }
            }

            // --- SCROLLABLE LOG LIST ---
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Žádné záznamy v této kategorii", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxHeight),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredLogs.size) { index ->
                            val entry = filteredLogs[index]
                            val isExpanded = (expandedDetailIndex == index)

                            CombatLogItemCard(
                                entry = entry,
                                isExpanded = isExpanded,
                                onClick = {
                                    HapticManager.vibrateClick()
                                    expandedDetailIndex = if (isExpanded) null else index
                                }
                            )
                        }
                    }

                    // Floating jump-to-latest button when scrolled down
                    if (listState.firstVisibleItemIndex > 2) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6A1B9A).copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Na nejnovější",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual turn action card inside the scrollable combat log.
 */
@Composable
fun CombatLogItemCard(
    entry: CombatLogEntry,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val isCritical = entry.message.contains("KRIT", ignoreCase = true)
    val isPlayerOrAlly = entry.type.startsWith("player") || entry.type == "combo"
    val isEnemy = entry.type.startsWith("enemy")
    val isHeal = entry.type.contains("heal") || entry.type.contains("support")
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
        isCritical -> Color(0xFF241604)
        isHeal -> Color(0xFF071F12)
        isDefend -> Color(0xFF091624)
        isPlayerOrAlly -> Color(0xFF1F1124)
        isEnemy -> Color(0xFF220A10)
        else -> Color(0xFF140B1A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(
            width = if (isCritical) 1.dp else 0.5.dp,
            color = accentColor.copy(alpha = if (isCritical) 0.8f else 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Turn Badge
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

                // Message Text
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = if (isCritical) FontWeight.ExtraBold else FontWeight.Normal,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Damage / Heal / Block Pill
                if (entry.damageDealt > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isPlayerOrAlly) Color(0xFFB71C1C).copy(alpha = 0.6f) else Color(0xFFD50000).copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "-${entry.damageDealt} HP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else if (isDefend) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1565C0).copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "🛡️ KRYT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF90CAF9),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Applied status effects chips
            if (entry.statusEffectsApplied.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.statusEffectsApplied.forEach { effect ->
                        StatusEffectChip(effect = effect, onClick = onClick)
                    }
                }
            }

            // Expanded Narrative & Damage Calculation Breakdown
            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))

                if (!entry.narrativeText.isNullOrBlank()) {
                    Text(
                        text = entry.narrativeText,
                        fontSize = 9.sp,
                        color = Color.LightGray.copy(alpha = 0.9f),
                        lineHeight = 12.sp
                    )
                }

                if (!entry.damageCalculation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.4f),
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

                if (entry.elementalBreakdown != null) {
                    val elem = entry.elementalBreakdown
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🌪️ Element: ${elem.attackerElement.name} vs ${elem.defenderElement.name} (${elem.matchupType.title} ${elem.elementMatchupMultiplier}x)",
                        fontSize = 8.sp,
                        color = Color(0xFF69F0AE),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * A sleek, semi-transparent collapsible overlay for combat logs.
 * Used in party battle screens and encounters.
 */
@Composable
fun CombatLogOverlay(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        if (isExpanded) {
            TurnByTurnCombatLogComponent(
                logs = logs,
                maxHeight = 240.dp,
                showHeaderStats = false,
                onClose = { isExpanded = false }
            )
        } else {
            // Minimized glance pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xDD140B1A),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticManager.vibrateClick()
                        isExpanded = true
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(15.dp)
                        )
                        val latestMsg = logs.firstOrNull()?.message ?: "Zahajování souboje..."
                        Text(
                            text = latestMsg,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Badge(containerColor = Color(0xFF6A1B9A)) {
                            Text("${logs.size}", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Rozbalit",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
