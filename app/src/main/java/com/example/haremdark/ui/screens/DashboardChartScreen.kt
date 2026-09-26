package com.example.haremdark.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.DailyResourceStat
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardChartScreen(
    engine: GameEngine,
    onBack: (() -> Unit)? = null
) {
    val gameState by engine.gameState.collectAsState()
    var selectedMetricTab by remember { mutableIntStateOf(0) } // 0: Gold & Mana, 1: Harem Morale, 2: Wood & Iron

    // Generate safe 30-day history if records are sparse
    val currentDay = gameState.player.day
    val baseCharacters = gameState.characters
    val currentMorale = if (baseCharacters.isNotEmpty()) {
        baseCharacters.map { it.morale }.average().toInt().coerceIn(10, 100)
    } else 50

    val safe30DayHistory: List<DailyResourceStat> = remember(gameState.resourceHistory, currentDay, currentMorale) {
        val raw = gameState.resourceHistory
        if (raw.size >= 15) {
            raw.takeLast(30)
        } else {
            // Synthesize realistic 30-day curve ending at currentDay
            val list = mutableListOf<DailyResourceStat>()
            val startDay = (currentDay - 29).coerceAtLeast(1)
            for (i in 0 until 30) {
                val dayNum = startDay + i
                val dayProgress = i / 29.0
                // Slight upward progression in resources
                val baseGold = 50 + (dayProgress * 150).toInt() + ((i % 5) * 10)
                val baseMana = 20 + (dayProgress * 80).toInt() + ((i % 4) * 8)
                val baseWood = 15 + (dayProgress * 40).toInt()
                val baseStone = 10 + (dayProgress * 30).toInt()
                val baseIron = 5 + (dayProgress * 25).toInt()
                // Morale varies around currentMorale
                val dayMorale = (currentMorale - 15 + (dayProgress * 15).toInt() + ((i % 3) * 3)).coerceIn(20, 100)

                val existing = raw.find { it.day == dayNum }
                if (existing != null) {
                    list.add(existing)
                } else {
                    list.add(
                        DailyResourceStat(
                            day = dayNum,
                            goldProduced = baseGold,
                            manaProduced = baseMana,
                            manaEssenceProduced = baseMana / 4,
                            woodProduced = baseWood,
                            stoneProduced = baseStone,
                            ironProduced = baseIron,
                            averageMorale = dayMorale
                        )
                    )
                }
            }
            list
        }
    }

    // Chart Entry Models
    val chartModel = remember(safe30DayHistory, selectedMetricTab) {
        when (selectedMetricTab) {
            0 -> {
                // Gold & Mana
                val goldEntries = safe30DayHistory.mapIndexed { idx, it ->
                    FloatEntry(idx.toFloat(), it.goldProduced.toFloat())
                }
                val manaEntries = safe30DayHistory.mapIndexed { idx, it ->
                    FloatEntry(idx.toFloat(), it.manaProduced.toFloat())
                }
                entryModelOf(goldEntries, manaEntries)
            }
            1 -> {
                // Harem Morale Levels (0 - 100%)
                val moraleEntries = safe30DayHistory.mapIndexed { idx, it ->
                    FloatEntry(idx.toFloat(), it.averageMorale.toFloat())
                }
                entryModelOf(moraleEntries)
            }
            else -> {
                // Wood & Iron
                val woodEntries = safe30DayHistory.mapIndexed { idx, it ->
                    FloatEntry(idx.toFloat(), it.woodProduced.toFloat())
                }
                val ironEntries = safe30DayHistory.mapIndexed { idx, it ->
                    FloatEntry(idx.toFloat(), it.ironProduced.toFloat())
                }
                entryModelOf(woodEntries, ironEntries)
            }
        }
    }

    val totalGold30d = safe30DayHistory.sumOf { it.goldProduced }
    val totalMana30d = safe30DayHistory.sumOf { it.manaProduced }
    val avgMorale30d = if (safe30DayHistory.isNotEmpty()) safe30DayHistory.map { it.averageMorale }.average().toInt() else 50
    val latestMorale = safe30DayHistory.lastOrNull()?.averageMorale ?: currentMorale

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "📈 Analýza Výkonu Dominia",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Trendy produkce surovin a morálky harému (posledních 30 dní)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Quick Summary Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261D10)),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("💰 Zlato (30 dní)", fontSize = 11.sp, color = Color(0xFFFFD54F))
                            Text("$totalGold30d", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082))
                            Text("Ø ${(totalGold30d / 30.0).toInt()} zl/den", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10202A)),
                        border = BorderStroke(1.dp, Color(0xFF29B6F6).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🔮 Mana (30 dní)", fontSize = 11.sp, color = Color(0xFF81D4FA))
                            Text("$totalMana30d", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB3E5FC))
                            Text("Ø ${(totalMana30d / 30.0).toInt()} MP/den", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1224)),
                        border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("💖 Morálka", fontSize = 11.sp, color = Color(0xFFFF80AB))
                            Text("$latestMorale%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4081))
                            Text("Průměr: $avgMorale30d%", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                // Tab Selection for Metric View
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedMetricTab == 0,
                            onClick = { selectedMetricTab = 0 },
                            label = { Text("💰 Zlato & Mana", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedMetricTab == 1,
                            onClick = { selectedMetricTab = 1 },
                            label = { Text("💖 Morálka Harému", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedMetricTab == 2,
                            onClick = { selectedMetricTab = 2 },
                            label = { Text("🌲 Dřevo & Železo", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                // Vico Line Chart Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedMetricTab) {
                                    0 -> "Křivka Produkce: Zlato vs. Mana"
                                    1 -> "Historický vývoj Morálky Harému (0 - 100)"
                                    else -> "Produkce stavebních surovin (Dřevo & Železo)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "30 dní",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Chart Legend
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (selectedMetricTab) {
                                0 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFC107), RoundedCornerShape(2.dp)))
                                        Text("Zlato", fontSize = 11.sp, color = Color(0xFFFFD54F))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF03A9F4), RoundedCornerShape(2.dp)))
                                        Text("Mana", fontSize = 11.sp, color = Color(0xFF81D4FA))
                                    }
                                }
                                1 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFE91E63), RoundedCornerShape(2.dp)))
                                        Text("Průměrná morálka dívek (bodů)", fontSize = 11.sp, color = Color(0xFFFF80AB))
                                    }
                                }
                                else -> {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF8D6E63), RoundedCornerShape(2.dp)))
                                        Text("Dřevo", fontSize = 11.sp, color = Color(0xFFA1887F))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF78909C), RoundedCornerShape(2.dp)))
                                        Text("Železo", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vico Interactive Line Chart
                        Chart(
                            chart = lineChart(),
                            model = chartModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Osa X: Den (relativně za 30 dní) | Osa Y: Množství / Hodnota",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                // Section Header for Daily Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📋 Detailní denní záznamy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${safe30DayHistory.size} dnů",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            items(safe30DayHistory.reversed()) { stat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Den ${stat.day}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "🌲 ${stat.woodProduced} dř • ⛏️ ${stat.stoneProduced} kam • ⚙️ ${stat.ironProduced} žel",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "💰 +${stat.goldProduced} zl • 🔮 +${stat.manaProduced} MP",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                "💖 Morálka: ${stat.averageMorale}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    stat.averageMorale >= 70 -> Color(0xFF81C784)
                                    stat.averageMorale >= 40 -> Color(0xFFFFD54F)
                                    else -> Color(0xFFEF5350)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
