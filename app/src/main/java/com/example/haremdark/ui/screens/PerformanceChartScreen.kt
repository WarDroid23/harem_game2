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
fun PerformanceChartScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    
    // Aggregation Logic (simplified for 30 days)
    val history = gameState.resourceHistory.takeLast(30)
    val goldTrend = history.map { it.goldProduced.toFloat() }.toTypedArray()
    
    val chartEntryModel = entryModelOf(*goldTrend)

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Výkon Dominia (Posledních 30 dní)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis()
        )
    }
}
