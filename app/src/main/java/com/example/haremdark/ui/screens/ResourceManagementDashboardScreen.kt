package com.example.haremdark.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.DailyResourceStat
import com.example.haremdark.models.InfluenceLogEntry
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
 * Chart display tabs for the Resource Management Dashboard.
 */
enum class ResourceDashboardTab(
    val title: String,
    val icon: ImageVector,
    val subtitle: String
) {
    CURRENCY("Měna & Zlato", Icons.Default.MonetizationOn, "Růst zlata, many a energie v čase"),
    MATERIALS("Stavební Suroviny", Icons.Default.Category, "Dřevo, kamení a železo z budov"),
    INFLUENCE("Vliv & Reputace", Icons.Default.WorkspacePremium, "Růst politické moci a autority"),
    PROSPERITY("Index Dominia", Icons.AutoMirrored.Filled.TrendingUp, "Celkový ekonomický index a harémová harmonie")
}

enum class TimeRangeFilter(val label: String, val days: Int) {
    SEVEN_DAYS("7 Dní", 7),
    FOURTEEN_DAYS("14 Dní", 14),
    THIRTY_DAYS("30 Dní", 30)
}

/**
 * Comprehensive Resource Management Dashboard to track Currency, Materials,
 * and Influence points using Vico charts to visualize growth over time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceManagementDashboardScreen(
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    onNavigateToBuildings: (() -> Unit)? = null
) {
    val gameState by engine.gameState.collectAsState()
    val player = gameState.player
    val currentDay = player.day

    var selectedTab by remember { mutableStateOf(ResourceDashboardTab.CURRENCY) }
    var selectedTimeRange by remember { mutableStateOf(TimeRangeFilter.THIRTY_DAYS) }

    // Synthesize / load realistic historical resource tracking data
    val baseCharacters = gameState.characters
    val currentMorale = if (baseCharacters.isNotEmpty()) {
        baseCharacters.map { it.morale }.average().toInt().coerceIn(10, 100)
    } else 50

    val fullResourceHistory: List<DailyResourceStat> = remember(gameState.resourceHistory, currentDay, currentMorale) {
        val raw = gameState.resourceHistory
        if (raw.size >= 30) {
            raw.takeLast(30)
        } else {
            // Build progressive historical dataset leading to current state
            val list = mutableListOf<DailyResourceStat>()
            val daysCount = 30
            val startDay = (currentDay - (daysCount - 1)).coerceAtLeast(1)

            for (i in 0 until daysCount) {
                val dayNum = startDay + i
                val dayProgress = i / (daysCount - 1.0)
                val baseGold = 60 + (dayProgress * 180).toInt() + ((i % 4) * 12)
                val baseMana = 25 + (dayProgress * 75).toInt() + ((i % 3) * 8)
                val baseEssence = 2 + (dayProgress * 12).toInt()
                val baseWood = 20 + (dayProgress * 55).toInt() + ((i % 5) * 6)
                val baseStone = 12 + (dayProgress * 40).toInt() + ((i % 4) * 4)
                val baseIron = 6 + (dayProgress * 30).toInt() + ((i % 3) * 3)
                val dayMorale = (currentMorale - 12 + (dayProgress * 12).toInt() + ((i % 3) * 2)).coerceIn(20, 100)

                val existing = raw.find { it.day == dayNum }
                if (existing != null) {
                    list.add(existing)
                } else {
                    list.add(
                        DailyResourceStat(
                            day = dayNum,
                            goldProduced = baseGold,
                            manaProduced = baseMana,
                            manaEssenceProduced = baseEssence,
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

    // Filtered by active time range
    val filteredHistory = remember(fullResourceHistory, selectedTimeRange) {
        fullResourceHistory.takeLast(selectedTimeRange.days)
    }

    // Synthesize historical influence points curve based on logs and progression
    val influenceHistory: List<Pair<Int, Int>> = remember(gameState.influenceLog, filteredHistory, player.influence) {
        val targetInfluence = player.influence
        val days = filteredHistory.map { it.day }
        if (days.isEmpty()) listOf(Pair(1, targetInfluence))
        else {
            val startInf = (targetInfluence * 0.45f).toInt().coerceAtLeast(10)
            days.mapIndexed { index, day ->
                val progress = index / (days.size - 1.0f).coerceAtLeast(1.0f)
                val logImpact = gameState.influenceLog.filter { it.day <= day }.sumOf { it.influenceChange }
                val value = (startInf + (targetInfluence - startInf) * progress + (logImpact * 0.5f)).toInt().coerceIn(5, 100)
                Pair(day, value)
            }
        }
    }

    // Daily Production estimations from buildings & characters
    val sawmillBuilding = gameState.buildings.firstOrNull { it.type == "pila" }
    val quarryBuilding = gameState.buildings.firstOrNull { it.type == "kamenolom" }
    val mineBuilding = gameState.buildings.firstOrNull { it.type == "dul" }
    val labBuilding = gameState.buildings.firstOrNull { it.type == "laborator" }

    val dailyWoodRate = 15 + (sawmillBuilding?.level ?: 0) * 12
    val dailyStoneRate = 10 + (quarryBuilding?.level ?: 0) * 8
    val dailyIronRate = 5 + (mineBuilding?.level ?: 0) * 5
    val dailyGoldRate = 50 + (gameState.territories.sumOf { it.baseIncome }) + (player.skills["obchod"] ?: 0) * 10
    val dailyManaEssenceRate = 2 + (labBuilding?.level ?: 0) * 2

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "📊 Správa Surovin & Vlivu",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                        Text(
                            "Vico vizualizace růstu měny, materiálů a vlivu v čase",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onBack()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            engine.addLog("📊 Palubní deska zdrojů a vlivu byla aktualizována.")
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Obnovit", tint = Color(0xFFFFD700))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140B1A)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0715))
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- 1. TOP STATS OVERVIEW CARDS ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💎 Přehled Zásob & Toků Dominia (Den ${player.day})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Currency row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResourceKpiCard(
                        title = "Zlato",
                        value = "${player.gold}",
                        subValue = "+$dailyGoldRate/den",
                        icon = "💰",
                        color = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_gold"
                    )
                    ResourceKpiCard(
                        title = "Esence Many",
                        value = "${player.manaEssence}",
                        subValue = "+$dailyManaEssenceRate/den",
                        icon = "🔮",
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_mana_essence"
                    )
                    ResourceKpiCard(
                        title = "Vliv Dominia",
                        value = "${player.influence}/${player.maxInfluence}",
                        subValue = "Reputace: ${player.reputation}",
                        icon = "👑",
                        color = Color(0xFFFF4081),
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_influence"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Materials row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResourceKpiCard(
                        title = "Dřevo",
                        value = "${player.wood}",
                        subValue = "+$dailyWoodRate/den",
                        icon = "🪵",
                        color = Color(0xFF8D6E63),
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_wood"
                    )
                    ResourceKpiCard(
                        title = "Kamení",
                        value = "${player.stone}",
                        subValue = "+$dailyStoneRate/den",
                        icon = "🪨",
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stone"
                    )
                    ResourceKpiCard(
                        title = "Železo",
                        value = "${player.iron}",
                        subValue = "+$dailyIronRate/den",
                        icon = "⛓️",
                        color = Color(0xFF78909C),
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_iron"
                    )
                }
            }

            // --- 2. TAB SELECTOR & TIMEFRAME FILTERS ---
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1026),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Category Tabs
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ResourceDashboardTab.entries) { tab ->
                                val isSelected = (selectedTab == tab)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        HapticManager.vibrateClick()
                                        selectedTab = tab
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) Color(0xFFFFD700) else Color.LightGray
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6A1B9A),
                                        selectedLabelColor = Color(0xFFFFD700),
                                        containerColor = Color(0xFF2A1535),
                                        labelColor = Color.White
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }

                        // Time Range Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rozsah historie:",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TimeRangeFilter.entries.forEach { range ->
                                    val isRangeSelected = (selectedTimeRange == range)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isRangeSelected) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.Transparent,
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isRangeSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.clickable {
                                            HapticManager.vibrateClick()
                                            selectedTimeRange = range
                                        }
                                    ) {
                                        Text(
                                            text = range.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isRangeSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isRangeSelected) Color(0xFFFFD700) else Color.Gray,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. VICO CHART CONTAINER CARD ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_vico_chart_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF190C22)),
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = 0.6f),
                                Color(0xFF8E24AA).copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Chart Header with Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedTab.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = selectedTab.subtitle,
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }

                            // Growth percentage badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, Color(0xFF69F0AE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(12.dp))
                                    Text("+28.4% růst", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF69F0AE))
                                }
                            }
                        }

                        // Chart Rendering Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F0614).copy(alpha = 0.85f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            when (selectedTab) {
                                ResourceDashboardTab.CURRENCY -> {
                                    // Vico Line Chart: Gold (Yellow) & Mana (Cyan)
                                    val goldEntries = filteredHistory.mapIndexed { idx, it ->
                                        FloatEntry(idx.toFloat(), it.goldProduced.toFloat())
                                    }
                                    val manaEntries = filteredHistory.mapIndexed { idx, it ->
                                        FloatEntry(idx.toFloat(), it.manaProduced.toFloat())
                                    }

                                    val goldColor = Color(0xFFFFD700)
                                    val manaColor = Color(0xFF00E5FF)

                                    if (goldEntries.isNotEmpty()) {
                                        Chart(
                                            chart = lineChart(
                                                lines = listOf(
                                                    LineChart.LineSpec(
                                                        lineColor = goldColor.toArgb(),
                                                        lineBackgroundShader = verticalGradient(
                                                            arrayOf(goldColor.copy(alpha = 0.45f), goldColor.copy(alpha = 0.01f))
                                                        )
                                                    ),
                                                    LineChart.LineSpec(
                                                        lineColor = manaColor.toArgb(),
                                                        lineBackgroundShader = verticalGradient(
                                                            arrayOf(manaColor.copy(alpha = 0.35f), manaColor.copy(alpha = 0.01f))
                                                        )
                                                    )
                                                )
                                            ),
                                            model = entryModelOf(goldEntries, manaEntries),
                                            startAxis = rememberStartAxis(title = "Výnos (zl/MP)"),
                                            bottomAxis = rememberBottomAxis(
                                                valueFormatter = { value, _ ->
                                                    val idx = value.toInt()
                                                    if (idx in filteredHistory.indices) "D.${filteredHistory[idx].day}" else ""
                                                },
                                                title = "Den"
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                ResourceDashboardTab.MATERIALS -> {
                                    // Vico Multi-Series Line Chart: Wood (Brown/Amber), Stone (Grey), Iron (Cyan/Steel)
                                    val woodEntries = filteredHistory.mapIndexed { idx, it ->
                                        FloatEntry(idx.toFloat(), it.woodProduced.toFloat())
                                    }
                                    val stoneEntries = filteredHistory.mapIndexed { idx, it ->
                                        FloatEntry(idx.toFloat(), it.stoneProduced.toFloat())
                                    }
                                    val ironEntries = filteredHistory.mapIndexed { idx, it ->
                                        FloatEntry(idx.toFloat(), it.ironProduced.toFloat())
                                    }

                                    val woodColor = Color(0xFFFFB74D)
                                    val stoneColor = Color(0xFFB0BEC5)
                                    val ironColor = Color(0xFF64B5F6)

                                    if (woodEntries.isNotEmpty()) {
                                        Chart(
                                            chart = lineChart(
                                                lines = listOf(
                                                    LineChart.LineSpec(
                                                        lineColor = woodColor.toArgb(),
                                                        lineBackgroundShader = verticalGradient(
                                                            arrayOf(woodColor.copy(alpha = 0.35f), woodColor.copy(alpha = 0.01f))
                                                        )
                                                    ),
                                                    LineChart.LineSpec(
                                                        lineColor = stoneColor.toArgb()
                                                    ),
                                                    LineChart.LineSpec(
                                                        lineColor = ironColor.toArgb()
                                                    )
                                                )
                                            ),
                                            model = entryModelOf(woodEntries, stoneEntries, ironEntries),
                                            startAxis = rememberStartAxis(title = "Kusů / den"),
                                            bottomAxis = rememberBottomAxis(
                                                valueFormatter = { value, _ ->
                                                    val idx = value.toInt()
                                                    if (idx in filteredHistory.indices) "D.${filteredHistory[idx].day}" else ""
                                                },
                                                title = "Den"
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                ResourceDashboardTab.INFLUENCE -> {
                                    // Vico Line Chart: Influence Points Growth (Pink/Magenta)
                                    val infEntries = influenceHistory.mapIndexed { idx, pair ->
                                        FloatEntry(idx.toFloat(), pair.second.toFloat())
                                    }
                                    val infColor = Color(0xFFFF4081)

                                    if (infEntries.isNotEmpty()) {
                                        Chart(
                                            chart = lineChart(
                                                lines = listOf(
                                                    LineChart.LineSpec(
                                                        lineColor = infColor.toArgb(),
                                                        lineBackgroundShader = verticalGradient(
                                                            arrayOf(infColor.copy(alpha = 0.5f), infColor.copy(alpha = 0.02f))
                                                        )
                                                    )
                                                )
                                            ),
                                            model = entryModelOf(infEntries),
                                            startAxis = rememberStartAxis(title = "Body Vlivu (0-100)"),
                                            bottomAxis = rememberBottomAxis(
                                                valueFormatter = { value, _ ->
                                                    val idx = value.toInt()
                                                    if (idx in influenceHistory.indices) "D.${influenceHistory[idx].first}" else ""
                                                },
                                                title = "Den"
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                ResourceDashboardTab.PROSPERITY -> {
                                    // Vico Column Chart: Dominion Prosperity Output Score
                                    val prosperityEntries = filteredHistory.mapIndexed { idx, it ->
                                        val score = (it.goldProduced * 0.4f) + (it.woodProduced * 0.3f) + (it.averageMorale * 0.5f)
                                        FloatEntry(idx.toFloat(), score)
                                    }

                                    if (prosperityEntries.isNotEmpty()) {
                                        Chart(
                                            chart = columnChart(),
                                            model = entryModelOf(prosperityEntries),
                                            startAxis = rememberStartAxis(title = "Index Prosperit"),
                                            bottomAxis = rememberBottomAxis(
                                                valueFormatter = { value, _ ->
                                                    val idx = value.toInt()
                                                    if (idx in filteredHistory.indices) "D.${filteredHistory[idx].day}" else ""
                                                },
                                                title = "Den"
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Chart Custom Legend Row
                        when (selectedTab) {
                            ResourceDashboardTab.CURRENCY -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ChartLegendIndicator(color = Color(0xFFFFD700), label = "Zlato (Zisk / den)")
                                    Spacer(modifier = Modifier.width(16.dp))
                                    ChartLegendIndicator(color = Color(0xFF00E5FF), label = "Mana (MP / den)")
                                }
                            }
                            ResourceDashboardTab.MATERIALS -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ChartLegendIndicator(color = Color(0xFFFFB74D), label = "Dřevo")
                                    Spacer(modifier = Modifier.width(12.dp))
                                    ChartLegendIndicator(color = Color(0xFFB0BEC5), label = "Kamení")
                                    Spacer(modifier = Modifier.width(12.dp))
                                    ChartLegendIndicator(color = Color(0xFF64B5F6), label = "Železo")
                                }
                            }
                            ResourceDashboardTab.INFLUENCE -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ChartLegendIndicator(color = Color(0xFFFF4081), label = "Vliv v metropoli (${player.influence} bodů)")
                                }
                            }
                            ResourceDashboardTab.PROSPERITY -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ChartLegendIndicator(color = Color(0xFF9C27B0), label = "Agregovaný index produkce a morálky")
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. PRODUCTION BUILDINGS & MODIFIERS BREAKDOWN ---
            item {
                Text(
                    text = "🏗️ Produkční Budovy & Násobiče Dominia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BuildingProductionRow(
                        buildingName = "Pila na temné dřevo",
                        level = sawmillBuilding?.level ?: 0,
                        rateText = "+$dailyWoodRate dřeva/den",
                        icon = "🪵",
                        bonusDescription = "Zvyšuje denní přísun dřeva pro stavbu a opevnění"
                    )
                    BuildingProductionRow(
                        buildingName = "Mramorový kamenolom",
                        level = quarryBuilding?.level ?: 0,
                        rateText = "+$dailyStoneRate kamení/den",
                        icon = "🪨",
                        bonusDescription = "Základní surovina pro rozvoj sídla a lázní"
                    )
                    BuildingProductionRow(
                        buildingName = "Hlubinný důl na železo",
                        level = mineBuilding?.level ?: 0,
                        rateText = "+$dailyIronRate železa/den",
                        icon = "⛓️",
                        bonusDescription = "Klíčové pro kování zbraní a brnění v kovárně"
                    )
                    BuildingProductionRow(
                        buildingName = "Alchymistická laboratoř",
                        level = labBuilding?.level ?: 0,
                        rateText = "+$dailyManaEssenceRate esencí/den",
                        icon = "⚗️",
                        bonusDescription = "Syntetizuje esence many pro rituály a bojové dovednosti"
                    )
                }
            }

            // --- 5. INFLUENCE & NARRATIVE DECISIONS LOG ---
            if (gameState.influenceLog.isNotEmpty()) {
                item {
                    Text(
                        text = "📜 Nedávné Změny Vlivu & Politická Rozhodnutí",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF80AB)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        gameState.influenceLog.takeLast(4).reversed().forEach { logEntry ->
                            InfluenceLogMiniCard(logEntry = logEntry)
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

/**
 * Compact, attractive KPI metric card for resources.
 */
@Composable
private fun ResourceKpiCard(
    title: String,
    value: String,
    subValue: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1026)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 14.sp)
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
            Text(
                text = subValue,
                fontSize = 8.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BuildingProductionRow(
    buildingName: String,
    level: Int,
    rateText: String,
    icon: String,
    bonusDescription: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1A0E22),
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 16.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$buildingName (Úroveň $level)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2E7D32).copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = rateText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = bonusDescription,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun InfluenceLogMiniCard(logEntry: InfluenceLogEntry) {
    val isPositive = logEntry.influenceChange >= 0
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF190B20),
        border = BorderStroke(0.8.dp, if (isPositive) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color(0xFFF44336).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Den ${logEntry.day} • ${logEntry.characterName}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF80AB)
                )
                Text(
                    text = logEntry.choiceDescription,
                    fontSize = 9.sp,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isPositive) Color(0xFF1B5E20) else Color(0xFFB71C1C)
            ) {
                Text(
                    text = if (isPositive) "+${logEntry.influenceChange} Vliv" else "${logEntry.influenceChange} Vliv",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ChartLegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
    }
}
