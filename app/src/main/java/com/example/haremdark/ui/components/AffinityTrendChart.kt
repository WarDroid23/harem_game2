package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.AffinityTierInfo
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.models.AffinityPointRecord
import com.example.haremdark.models.Character
import com.example.haremdark.models.getSafeAffinityTrend
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlin.math.ceil

enum class DashboardChartMode(val title: String, val icon: ImageVector) {
    LINE_CUMULATIVE("Křivka Vývoje", Icons.Default.ShowChart),
    COLUMN_GAINS("Přírůstky Bodů", Icons.Default.BarChart),
    SOURCES_BREAKDOWN("Zdroje Pouta", Icons.Default.PieChart)
}

enum class DashboardTimeFilter(val label: String) {
    ALL("Všechny Dny"),
    LAST_7("7 Dní"),
    LAST_14("14 Dní"),
    SURGES("Skoky ≥ 10")
}

data class AffinityDeltaRecord(
    val record: AffinityPointRecord,
    val delta: Int,
    val cumulativePoints: Int
)

/**
 * Visual dashboard in the character menu visualizing the player's affection progression
 * trend over time using Vico charts, KPI metrics, velocity analysis, and milestone timelines.
 */
@Composable
fun AffinityProgressionDashboard(
    character: Character,
    currentDay: Int = 1,
    engine: GameEngine? = null,
    onTriggerAffinityEffect: ((AffinityBurstType, Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(DashboardChartMode.LINE_CUMULATIVE) }
    var timeFilter by remember { mutableStateOf(DashboardTimeFilter.ALL) }
    var selectedRecordIndex by remember { mutableIntStateOf(-1) }

    val currentTier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }
    val nextTier = remember(character.affinityPoints) {
        AffinityData.TIERS.firstOrNull { it.level == currentTier.level + 1 }
    }

    val accentColor = Color(currentTier.colorHex)

    // Base raw trend records (guaranteed >= 2 points)
    val rawTrendRecords = remember(character.affinityPoints, character.affinityHistory.size, currentDay) {
        character.getSafeAffinityTrend(currentDay)
    }

    // Compute deltas between adjacent points
    val deltaRecords = remember(rawTrendRecords) {
        rawTrendRecords.mapIndexed { idx, rec ->
            val prevPoints = if (idx > 0) rawTrendRecords[idx - 1].points else (rec.points * 0.5f).toInt()
            val delta = (rec.points - prevPoints).coerceAtLeast(0)
            AffinityDeltaRecord(rec, delta, rec.points)
        }
    }

    // Apply time-range filter
    val filteredDeltaRecords = remember(deltaRecords, timeFilter, currentDay) {
        when (timeFilter) {
            DashboardTimeFilter.ALL -> deltaRecords
            DashboardTimeFilter.LAST_7 -> deltaRecords.filter { it.record.day >= (currentDay - 7).coerceAtLeast(1) }
                .ifEmpty { deltaRecords.takeLast(7) }
            DashboardTimeFilter.LAST_14 -> deltaRecords.filter { it.record.day >= (currentDay - 14).coerceAtLeast(1) }
                .ifEmpty { deltaRecords.takeLast(14) }
            DashboardTimeFilter.SURGES -> deltaRecords.filter { it.delta >= 10 }
                .ifEmpty { deltaRecords }
        }
    }

    // Prepared data for Vico Line Chart (Cumulative points)
    val lineChartEntries = remember(filteredDeltaRecords) {
        filteredDeltaRecords.mapIndexed { index, item ->
            FloatEntry(x = index.toFloat(), y = item.cumulativePoints.toFloat())
        }
    }

    // Prepared data for Vico Column Chart (Gain deltas)
    val columnChartEntries = remember(filteredDeltaRecords) {
        filteredDeltaRecords.mapIndexed { index, item ->
            FloatEntry(x = index.toFloat(), y = item.delta.toFloat().coerceAtLeast(1f))
        }
    }

    // Velocity & Analytics Calculations
    val initialPoints = rawTrendRecords.firstOrNull()?.points ?: 0
    val totalGain = (character.affinityPoints - initialPoints).coerceAtLeast(0)
    val avgGainPerEvent = if (deltaRecords.isNotEmpty()) {
        deltaRecords.map { it.delta }.average().toFloat()
    } else 0f
    val maxSurge = deltaRecords.maxByOrNull { it.delta }
    val pointsToNextTier = nextTier?.let { (it.minPoints - character.affinityPoints).coerceAtLeast(0) } ?: 0
    val estimatedEventsToNext = if (avgGainPerEvent > 1f && pointsToNextTier > 0) {
        ceil(pointsToNextTier / avgGainPerEvent).toInt()
    } else null

    // Source breakdown categorization
    val sourceBreakdown = remember(rawTrendRecords) {
        val groups = mutableMapOf(
            "🎁 Dary" to 0,
            "💬 Rozhovory" to 0,
            "👑 Přízeň" to 0,
            "⚔️ Boj & Akce" to 0,
            "✨ Ostatní" to 0
        )
        deltaRecords.forEach { item ->
            val src = item.record.source.lowercase()
            when {
                src.contains("dar") || src.contains("gift") -> groups["🎁 Dary"] = (groups["🎁 Dary"] ?: 0) + item.delta
                src.contains("rozhovor") || src.contains("volba") || src.contains("dialog") -> groups["💬 Rozhovory"] = (groups["💬 Rozhovory"] ?: 0) + item.delta
                src.contains("přízeň") || src.contains("pochval") || src.contains("objetí") -> groups["👑 Přízeň"] = (groups["👑 Přízeň"] ?: 0) + item.delta
                src.contains("boj") || src.contains("bitva") || src.contains("mise") || src.contains("výcvik") -> groups["⚔️ Boj & Akce"] = (groups["⚔️ Boj & Akce"] ?: 0) + item.delta
                else -> groups["✨ Ostatní"] = (groups["✨ Ostatní"] ?: 0) + item.delta
            }
        }
        groups.filter { it.value > 0 }
    }

    val totalCategorizedPoints = sourceBreakdown.values.sum().coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Dashboard Banner & Header
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
                        color = accentColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, accentColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Dashboard Vývoje Pouta",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vico vizualizace růstu náklonnosti v čase",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(currentTier.icon, fontSize = 13.sp)
                        Text(
                            text = "${character.affinityPoints} pts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }
            }

            // 4 Key Analytics KPI Cards in 2x2 Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI 1: Current Tier & Progress
                    DashboardKpiCard(
                        title = "Stupeň Vztahu",
                        value = "${currentTier.icon} Lv.${currentTier.level}",
                        subtext = currentTier.title,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )

                    // KPI 2: Velocity / Average Gain
                    DashboardKpiCard(
                        title = "Průměrný Růst",
                        value = String.format("+%.1f pts", avgGainPerEvent),
                        subtext = if (totalGain > 0) "+$totalGain celkem" else "Stabilní",
                        accentColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI 3: Peak Surge
                    val surgeText = maxSurge?.let { "+${it.delta} pts" } ?: "—"
                    val surgeSubtext = maxSurge?.record?.source?.take(18) ?: "Žádný skok"
                    DashboardKpiCard(
                        title = "Největší Skok",
                        value = surgeText,
                        subtext = surgeSubtext,
                        accentColor = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f)
                    )

                    // KPI 4: Projection / ETA to Next Tier
                    val projectionValue = when {
                        nextTier == null -> "👑 Max"
                        estimatedEventsToNext != null -> "~$estimatedEventsToNext událostí"
                        else -> "Sbírej dary"
                    }
                    val projectionSubtext = if (nextTier != null) "Zbývá $pointsToNextTier pts" else "Plná oddanost"
                    DashboardKpiCard(
                        title = "Odhad k Lv.${(currentTier.level + 1).coerceAtMost(6)}",
                        value = projectionValue,
                        subtext = projectionSubtext,
                        accentColor = if (nextTier != null) Color(nextTier.colorHex) else Color(0xFFFFD700),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Chart View Mode Selector Tabs
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DashboardChartMode.entries.forEach { mode ->
                        val isSelected = chartMode == mode
                        Button(
                            onClick = { chartMode = mode },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) accentColor else Color.Transparent,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(mode.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(mode.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Time Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rozsah časové osy:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DashboardTimeFilter.entries.forEach { filter ->
                        val isSelected = timeFilter == filter
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { timeFilter = filter }
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Main Vico Chart Container Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0818).copy(alpha = 0.85f))
                    .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                when (chartMode) {
                    DashboardChartMode.LINE_CUMULATIVE -> {
                        if (lineChartEntries.isNotEmpty()) {
                            Chart(
                                chart = lineChart(
                                    lines = listOf(
                                        LineChart.LineSpec(
                                            lineColor = accentColor.toArgb(),
                                            lineBackgroundShader = verticalGradient(
                                                arrayOf(
                                                    accentColor.copy(alpha = 0.5f),
                                                    accentColor.copy(alpha = 0.02f)
                                                )
                                            )
                                        )
                                    )
                                ),
                                model = entryModelOf(lineChartEntries),
                                startAxis = rememberStartAxis(
                                    title = "Body"
                                ),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = { value, _ ->
                                        val idx = value.toInt()
                                        if (idx in filteredDeltaRecords.indices) "D.${filteredDeltaRecords[idx].record.day}" else ""
                                    },
                                    title = "Den"
                                ),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nedostatek dat pro graf", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    DashboardChartMode.COLUMN_GAINS -> {
                        if (columnChartEntries.isNotEmpty()) {
                            Chart(
                                chart = columnChart(),
                                model = entryModelOf(columnChartEntries),
                                startAxis = rememberStartAxis(
                                    title = "Zisk"
                                ),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = { value, _ ->
                                        val idx = value.toInt()
                                        if (idx in filteredDeltaRecords.indices) "D.${filteredDeltaRecords[idx].record.day}" else ""
                                    },
                                    title = "Den"
                                ),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nedostatek dat pro graf přírůstků", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    DashboardChartMode.SOURCES_BREAKDOWN -> {
                        // Custom Rich Breakdown View
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "Rozpad bodů podle aktivit a darů",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            sourceBreakdown.forEach { (sourceName, points) ->
                                val fraction = (points.toFloat() / totalCategorizedPoints).coerceIn(0f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(sourceName, fontSize = 11.sp, color = Color.White)
                                        Text(
                                            text = "$points pts (${(fraction * 100).toInt()}%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = accentColor,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Next Tier Target Progression Banner
            if (nextTier != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(nextTier.colorHex).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(nextTier.colorHex).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(nextTier.icon, fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "Další milník: ${nextTier.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(nextTier.colorHex)
                                )
                                Text(
                                    text = "Odemkne nový bojový buff & intimní dialog",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Text(
                            text = "Zbývá $pointsToNextTier pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(nextTier.colorHex)
                        )
                    }
                }
            }

            // Interactive Milestone History Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historické události pouta (Klepnutím pro detail):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
                Text(
                    text = "${filteredDeltaRecords.size} záznamů",
                    fontSize = 10.sp,
                    color = accentColor
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredDeltaRecords.reversed()) { deltaItem ->
                    val isSelected = selectedRecordIndex == deltaItem.record.day
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.clickable {
                            selectedRecordIndex = if (isSelected) -1 else deltaItem.record.day
                            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
                            onTriggerAffinityEffect?.invoke(AffinityBurstType.HEARTS, 1.0f)
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Den ${deltaItem.record.day}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (deltaItem.delta >= 10) Color(0xFF4CAF50).copy(alpha = 0.2f) else accentColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+${deltaItem.delta} pts",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (deltaItem.delta >= 10) Color(0xFF81C784) else accentColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = deltaItem.record.source,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                            Text(
                                text = "Celkem: ${deltaItem.cumulativePoints} pts",
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Milestone Detail Inspector Popup/Card (when a record is selected)
            AnimatedVisibility(visible = selectedRecordIndex != -1) {
                val selectedItem = filteredDeltaRecords.find { it.record.day == selectedRecordIndex }
                if (selectedItem != null) {
                    val dialogueQuote = remember(selectedItem.record.day, character.archetypeId) {
                        val tierAtPoint = AffinityData.getTierForPoints(selectedItem.cumulativePoints)
                        val dialogues = AffinityData.getDialoguesForTier(character.archetypeId, tierAtPoint.level)
                        if (dialogues.isNotEmpty()) dialogues.random() else "Mé srdce je ti oddáno, můj pane..."
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📜", fontSize = 16.sp)
                                    Text(
                                        text = "Den ${selectedItem.record.day}: ${selectedItem.record.source}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { VoiceManager.speak(dialogueQuote, character.archetypeId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Přehrát hlas",
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${character.name}: „$dialogueQuote“",
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Metric KPI Box for displaying key progress indicators.
 */
@Composable
fun DashboardKpiCard(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

/**
 * Backwards-compatible adapter so existing references to [AffinityTrendChart]
 * directly render the enhanced [AffinityProgressionDashboard].
 */
@Composable
fun AffinityTrendChart(
    character: Character,
    currentDay: Int = 1,
    modifier: Modifier = Modifier
) {
    AffinityProgressionDashboard(
        character = character,
        currentDay = currentDay,
        modifier = modifier
    )
}
