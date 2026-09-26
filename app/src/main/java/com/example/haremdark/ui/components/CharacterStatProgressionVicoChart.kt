package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.Character
import com.example.haremdark.models.StatRecord
import com.example.haremdark.models.getSafeStatTrend
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

/**
 * Metric options for stat progression visualization.
 */
enum class StatChartMetric(
    val title: String,
    val icon: String,
    val color: Color,
    val unit: String
) {
    ALL("Všechny", "📊", Color(0xFFE040FB), "body"),
    STRENGTH("Síla / Útok", "⚔️", Color(0xFFFF7043), "DMG"),
    DEFENSE("Obrana", "🛡️", Color(0xFF26C6DA), "DEF"),
    HP("Zdraví (HP)", "💚", Color(0xFF66BB6A), "HP"),
    POWER("Bojová síla", "⚡", Color(0xFFFFCA28), "PWR"),
    MANA("Mana", "🔮", Color(0xFF7E57C2), "MP")
}

/**
 * Chart display style modes.
 */
enum class StatChartDisplayMode(val label: String, val icon: ImageVector) {
    LINE("Křivka", Icons.AutoMirrored.Filled.TrendingUp),
    COLUMN("Sloupce", Icons.Default.BarChart)
}

/**
 * Time filter options for stat trends.
 */
enum class StatTimeFilter(val label: String) {
    ALL("Celá historie"),
    LAST_7("7 dní"),
    LAST_14("14 dní")
}

/**
 * Highly polished Vico Chart component integrated into the Character Detail view,
 * visualizing stat progression over time for harem members.
 */
@Composable
fun CharacterStatProgressionVicoChart(
    character: Character,
    currentDay: Int = 1,
    modifier: Modifier = Modifier,
    isFullTab: Boolean = false,
    onOpenFullDetail: (() -> Unit)? = null
) {
    var selectedMetric by remember { mutableStateOf(StatChartMetric.ALL) }
    var displayMode by remember { mutableStateOf(StatChartDisplayMode.LINE) }
    var timeFilter by remember { mutableStateOf(StatTimeFilter.ALL) }
    var showHistoryTable by remember { mutableStateOf(false) }

    // Fetch or construct safe progression records for this harem member
    val rawRecords = remember(character.id, character.statHistory.size, currentDay) {
        character.getSafeStatTrend(currentDay)
    }

    // Filter records according to the chosen time range
    val filteredRecords = remember(rawRecords, timeFilter, currentDay) {
        when (timeFilter) {
            StatTimeFilter.ALL -> rawRecords
            StatTimeFilter.LAST_7 -> rawRecords.filter { it.day >= (currentDay - 7).coerceAtLeast(1) }
                .ifEmpty { rawRecords.takeLast(7) }
            StatTimeFilter.LAST_14 -> rawRecords.filter { it.day >= (currentDay - 14).coerceAtLeast(1) }
                .ifEmpty { rawRecords.takeLast(14) }
        }
    }

    // Prepare FloatEntry lists for Vico charts
    val strengthEntries = remember(filteredRecords) {
        filteredRecords.mapIndexed { idx, rec -> FloatEntry(idx.toFloat(), rec.strength.toFloat()) }
    }
    val defenseEntries = remember(filteredRecords) {
        filteredRecords.mapIndexed { idx, rec -> FloatEntry(idx.toFloat(), rec.defense.toFloat()) }
    }
    val hpEntries = remember(filteredRecords) {
        filteredRecords.mapIndexed { idx, rec -> FloatEntry(idx.toFloat(), rec.hp.toFloat()) }
    }
    val powerEntries = remember(filteredRecords) {
        filteredRecords.mapIndexed { idx, rec -> FloatEntry(idx.toFloat(), rec.powerLevel.toFloat()) }
    }
    val manaEntries = remember(filteredRecords) {
        filteredRecords.mapIndexed { idx, rec -> FloatEntry(idx.toFloat(), rec.mana.toFloat()) }
    }

    // Determine current values for delta calculations
    val currentStatValue: Int
    val initialStatValue: Int
    val metricUnit: String
    val metricColor: Color

    when (selectedMetric) {
        StatChartMetric.STRENGTH -> {
            initialStatValue = filteredRecords.firstOrNull()?.strength ?: 0
            currentStatValue = filteredRecords.lastOrNull()?.strength ?: 0
            metricUnit = "DMG"
            metricColor = StatChartMetric.STRENGTH.color
        }
        StatChartMetric.DEFENSE -> {
            initialStatValue = filteredRecords.firstOrNull()?.defense ?: 0
            currentStatValue = filteredRecords.lastOrNull()?.defense ?: 0
            metricUnit = "DEF"
            metricColor = StatChartMetric.DEFENSE.color
        }
        StatChartMetric.HP -> {
            initialStatValue = filteredRecords.firstOrNull()?.hp ?: 0
            currentStatValue = filteredRecords.lastOrNull()?.hp ?: 0
            metricUnit = "HP"
            metricColor = StatChartMetric.HP.color
        }
        StatChartMetric.POWER -> {
            initialStatValue = filteredRecords.firstOrNull()?.powerLevel ?: 0
            currentStatValue = filteredRecords.lastOrNull()?.powerLevel ?: 0
            metricUnit = "PWR"
            metricColor = StatChartMetric.POWER.color
        }
        StatChartMetric.MANA -> {
            initialStatValue = filteredRecords.firstOrNull()?.mana ?: 0
            currentStatValue = filteredRecords.lastOrNull()?.mana ?: 0
            metricUnit = "MP"
            metricColor = StatChartMetric.MANA.color
        }
        StatChartMetric.ALL -> {
            initialStatValue = filteredRecords.firstOrNull()?.powerLevel ?: 0
            currentStatValue = filteredRecords.lastOrNull()?.powerLevel ?: 0
            metricUnit = "PWR"
            metricColor = StatChartMetric.ALL.color
        }
    }

    val deltaGrowth = currentStatValue - initialStatValue
    val growthPercent = if (initialStatValue > 0) {
        ((deltaGrowth.toFloat() / initialStatValue) * 100f)
    } else 0f

    Card(
        modifier = modifier
            .testTag("character_stat_progression_card")
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, metricColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Navigation/Action affordances
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(metricColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedMetric.icon, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Progrese Statistik v Čase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vico grafická analýza růstu: ${character.name}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                if (onOpenFullDetail != null && !isFullTab) {
                    IconButton(
                        onClick = onOpenFullDetail,
                        modifier = Modifier.testTag("btn_open_full_stat_progression")
                    ) {
                        Icon(
                            Icons.Default.OpenInFull,
                            contentDescription = "Otevřít celou analýzu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stat Metric Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatChartMetric.entries.forEach { metric ->
                    val isSelected = selectedMetric == metric
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMetric = metric },
                        label = {
                            Text(
                                text = "${metric.icon} ${metric.title}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = metric.color.copy(alpha = 0.25f),
                            selectedLabelColor = metric.color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) metric.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("chip_metric_${metric.name.lowercase()}")
                    )
                }
            }

            // Secondary Controls: Mode Toggle (Line/Column) & Time Range Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatTimeFilter.entries.forEach { filter ->
                        val isSelected = timeFilter == filter
                        SuggestionChip(
                            onClick = { timeFilter = filter },
                            label = { Text(filter.label, fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                // Display Mode Segmented Toggle (Line vs Column)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        StatChartDisplayMode.entries.forEach { mode ->
                            val isSelected = displayMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) metricColor.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { displayMode = mode }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = mode.icon,
                                        contentDescription = mode.label,
                                        tint = if (isSelected) metricColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = mode.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) metricColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stat Growth Highlight Metric Banner
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, metricColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Počáteční: $initialStatValue $metricUnit",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Aktuální: $currentStatValue $metricUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = metricColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val growthSign = if (deltaGrowth >= 0) "+" else ""
                        Text(
                            text = "$growthSign$deltaGrowth $metricUnit ($growthSign${String.format("%.1f", growthPercent)}%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deltaGrowth >= 0) Color(0xFF66BB6A) else Color(0xFFEF5350)
                        )
                        Text(
                            text = if (deltaGrowth >= 0) "Vzestupný trend ↗" else "Pokles ↘",
                            fontSize = 10.sp,
                            color = if (deltaGrowth >= 0) Color(0xFF66BB6A) else Color(0xFFEF5350)
                        )
                    }
                }
            }

            // MAIN VICO CHART CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isFullTab) 260.dp else 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(1.dp, metricColor.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                if (filteredRecords.isNotEmpty()) {
                    when (displayMode) {
                        StatChartDisplayMode.LINE -> {
                            if (selectedMetric == StatChartMetric.ALL) {
                                // Multi-line chart displaying Strength, Defense, HP, and Power Level simultaneously
                                val multiLineModel = entryModelOf(
                                    powerEntries,
                                    hpEntries,
                                    strengthEntries,
                                    defenseEntries
                                )

                                val lineSpecs = listOf(
                                    LineChart.LineSpec(
                                        lineColor = StatChartMetric.POWER.color.toArgb(),
                                        lineBackgroundShader = verticalGradient(
                                            arrayOf(
                                                StatChartMetric.POWER.color.copy(alpha = 0.45f),
                                                StatChartMetric.POWER.color.copy(alpha = 0.02f)
                                            )
                                        )
                                    ),
                                    LineChart.LineSpec(
                                        lineColor = StatChartMetric.HP.color.toArgb(),
                                        lineBackgroundShader = verticalGradient(
                                            arrayOf(
                                                StatChartMetric.HP.color.copy(alpha = 0.35f),
                                                StatChartMetric.HP.color.copy(alpha = 0.02f)
                                            )
                                        )
                                    ),
                                    LineChart.LineSpec(
                                        lineColor = StatChartMetric.STRENGTH.color.toArgb(),
                                        lineBackgroundShader = verticalGradient(
                                            arrayOf(
                                                StatChartMetric.STRENGTH.color.copy(alpha = 0.35f),
                                                StatChartMetric.STRENGTH.color.copy(alpha = 0.02f)
                                            )
                                        )
                                    ),
                                    LineChart.LineSpec(
                                        lineColor = StatChartMetric.DEFENSE.color.toArgb(),
                                        lineBackgroundShader = verticalGradient(
                                            arrayOf(
                                                StatChartMetric.DEFENSE.color.copy(alpha = 0.35f),
                                                StatChartMetric.DEFENSE.color.copy(alpha = 0.02f)
                                            )
                                        )
                                    )
                                )

                                Chart(
                                    chart = lineChart(lines = lineSpecs),
                                    model = multiLineModel,
                                    startAxis = rememberStartAxis(title = "Hodnota"),
                                    bottomAxis = rememberBottomAxis(
                                        valueFormatter = { value, _ ->
                                            val idx = value.toInt()
                                            if (idx in filteredRecords.indices) "D.${filteredRecords[idx].day}" else ""
                                        },
                                        title = "Herní Den"
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Single selected metric with rich focused line and gradient
                                val activeEntries = when (selectedMetric) {
                                    StatChartMetric.STRENGTH -> strengthEntries
                                    StatChartMetric.DEFENSE -> defenseEntries
                                    StatChartMetric.HP -> hpEntries
                                    StatChartMetric.POWER -> powerEntries
                                    StatChartMetric.MANA -> manaEntries
                                    StatChartMetric.ALL -> powerEntries
                                }

                                val singleModel = entryModelOf(activeEntries)
                                val singleLineSpec = LineChart.LineSpec(
                                    lineColor = metricColor.toArgb(),
                                    lineBackgroundShader = verticalGradient(
                                        arrayOf(
                                            metricColor.copy(alpha = 0.5f),
                                            metricColor.copy(alpha = 0.02f)
                                        )
                                    )
                                )

                                Chart(
                                    chart = lineChart(lines = listOf(singleLineSpec)),
                                    model = singleModel,
                                    startAxis = rememberStartAxis(title = selectedMetric.unit),
                                    bottomAxis = rememberBottomAxis(
                                        valueFormatter = { value, _ ->
                                            val idx = value.toInt()
                                            if (idx in filteredRecords.indices) "D.${filteredRecords[idx].day}" else ""
                                        },
                                        title = "Herní Den"
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        StatChartDisplayMode.COLUMN -> {
                            val activeEntries = when (selectedMetric) {
                                StatChartMetric.STRENGTH -> strengthEntries
                                StatChartMetric.DEFENSE -> defenseEntries
                                StatChartMetric.HP -> hpEntries
                                StatChartMetric.POWER -> powerEntries
                                StatChartMetric.MANA -> manaEntries
                                StatChartMetric.ALL -> powerEntries
                            }

                            Chart(
                                chart = columnChart(),
                                model = entryModelOf(activeEntries),
                                startAxis = rememberStartAxis(title = metricUnit),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = { value, _ ->
                                        val idx = value.toInt()
                                        if (idx in filteredRecords.indices) "D.${filteredRecords[idx].day}" else ""
                                    },
                                    title = "Herní Den"
                                ),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nedostatek dat pro zobrazení grafu.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Legend when 'ALL' metrics are displayed
            if (selectedMetric == StatChartMetric.ALL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatProgressionLegendItem(color = StatChartMetric.POWER.color, label = "⚡ Bojová síla")
                    StatProgressionLegendItem(color = StatChartMetric.HP.color, label = "💚 HP")
                    StatProgressionLegendItem(color = StatChartMetric.STRENGTH.color, label = "⚔️ Síla")
                    StatProgressionLegendItem(color = StatChartMetric.DEFENSE.color, label = "🛡️ Obrana")
                }
            }

            // History Table Expansion Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHistoryTable = !showHistoryTable }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 Podrobný denní přehled záznamů (${filteredRecords.size} dní)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (showHistoryTable) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Collapsible Record Log Table
            AnimatedVisibility(visible = showHistoryTable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Den", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("⚔️ Síla", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("🛡️ Obr.", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("💚 HP", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("⚡ Moc", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    filteredRecords.takeLast(10).forEach { record ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Den ${record.day}", fontSize = 10.sp, modifier = Modifier.weight(1f))
                            Text("${record.strength}", fontSize = 10.sp, color = StatChartMetric.STRENGTH.color, modifier = Modifier.weight(1f))
                            Text("${record.defense}", fontSize = 10.sp, color = StatChartMetric.DEFENSE.color, modifier = Modifier.weight(1f))
                            Text("${record.hp}", fontSize = 10.sp, color = StatChartMetric.HP.color, modifier = Modifier.weight(1f))
                            Text("${record.powerLevel}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = StatChartMetric.POWER.color, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatProgressionLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}
