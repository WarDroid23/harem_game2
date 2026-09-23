package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.Character
import com.example.haremdark.models.StatRecord
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun CharacterStatProgressionScreen(
    character: Character,
    modifier: Modifier = Modifier
) {
    val statHistory = character.statHistory

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Progrese statistik: ${character.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            if (statHistory.size >= 2) {
                Card(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Síla v čase", fontWeight = FontWeight.Bold)
                        val entries = statHistory.mapIndexed { index, record ->
                            FloatEntry(x = index.toFloat(), y = record.strength.toFloat())
                        }
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(entries),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        )
                    }
                }
            } else {
                Text("Nedostatek dat pro graf.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}
