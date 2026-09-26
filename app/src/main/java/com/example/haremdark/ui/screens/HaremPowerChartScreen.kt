package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun HaremPowerChartScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    
    // Aggregation Logic: Sum of power of all characters over the last 7 days
    // Characters have statHistory which is List<StatRecord>
    // We need to group by day and sum the power
    val allHistory = gameState.characters.flatMap { it.statHistory }
    val groupedByDay = allHistory.groupBy { it.day }
    val sortedDays = groupedByDay.keys.sorted().takeLast(7)
    
    val powerTrend = sortedDays.map { day ->
        groupedByDay[day]?.sumOf { it.powerLevel }?.toFloat() ?: 0f
    }.toTypedArray()
    
    val chartEntryModel = entryModelOf(*powerTrend)

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("📈 Celková síla harému (Posledních 7 dní)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis()
        )
    }
}
