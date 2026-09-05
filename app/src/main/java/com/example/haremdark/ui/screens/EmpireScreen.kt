package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun EmpireScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🗡️ Mafie", "🏰 Budovy", "💰 Nájem", "📈 Produkce", "⛓️ Trh")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DomainResourceBanner(gameState)

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> MafiaTab(gameState, engine)
            1 -> BuildingsTab(gameState, engine)
            2 -> RentalsHubTab(gameState)
            3 -> StatisticsTab(gameState)
            4 -> RecruitmentTab(gameState, engine)
        }
    }
}

@Composable
fun RecruitmentTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val player = gameState.player

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trh s otroky (Nábor)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Rozšiř svůj harém nákupem nových dívek. Kvalita a vzácnost dívky závisí na množství investovaných zdrojů.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Kapacita harému: ${gameState.characters.size} / ${player.maxPopulation}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (gameState.characters.size >= player.maxPopulation) Color.Red else MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        item {
            RecruitmentOption(
                title = "Běžný otrok",
                desc = "Mladá, nezkušená dívka pochybného původu. (Úroveň 1)",
                goldCost = 250,
                manaCost = 0,
                playerGold = player.gold,
                playerMana = player.mana,
                onRecruit = {
                    val (success, msg) = engine.recruitCharacter("basic")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        item {
            RecruitmentOption(
                title = "Vzácný zajatec",
                desc = "Dívka z lepší rodiny, možná nižší šlechta, zajatá při raziích. (Úroveň 2, lepší staty)",
                goldCost = 600,
                manaCost = 20,
                playerGold = player.gold,
                playerMana = player.mana,
                onRecruit = {
                    val (success, msg) = engine.recruitCharacter("advanced")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        item {
            RecruitmentOption(
                title = "Exkluzivní trofej",
                desc = "Prvotřídní kráska s magickým nadáním či královskou krví, ukradená z tajných aukcí. (Úroveň 3, nejlepší staty)",
                goldCost = 1500,
                manaCost = 50,
                playerGold = player.gold,
                playerMana = player.mana,
                onRecruit = {
                    val (success, msg) = engine.recruitCharacter("elite")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun RecruitmentOption(
    title: String,
    desc: String,
    goldCost: Int,
    manaCost: Int,
    playerGold: Int,
    playerMana: Int,
    onRecruit: () -> Unit
) {
    val canAfford = playerGold >= goldCost && playerMana >= manaCost
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goldCost > 0) {
                        Text("💰 $goldCost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (playerGold >= goldCost) Color(0xFFFFD700) else Color.Red)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (manaCost > 0) {
                        Text("🔮 $manaCost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (playerMana >= manaCost) Color(0xFFE040FB) else Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRecruit,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Koupit na trhu", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MafiaTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val totalIncome = gameState.territories.filter { it.level > 0 }.sumOf { it.baseIncome * it.level }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vliv v podsvětí města", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Celkový denní výpalné z mafie:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Text("+$totalIncome zl./den", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        items(gameState.territories) { territory ->
            val upgradeCost = territory.baseIncome * (territory.level + 1) * 3
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(territory.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (territory.level > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color(0x33888888)
                        ) {
                            Text(
                                text = if (territory.level > 0) "Úroveň ${territory.level}" else "Neovládnuto",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (territory.level > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Výnos: ${if (territory.level > 0) "+${territory.baseIncome * territory.level} zl./den" else "0 zl."}", fontSize = 12.sp, color = Color(0xFFFFD700))
                        Text("Bezpečnost: ${territory.securityLevel}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }

                    Button(
                        onClick = {
                            val (success, msg) = engine.upgradeTerritory(territory.id)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = gameState.player.gold >= upgradeCost,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (territory.level == 0) "Ovládnout území ($upgradeCost zlatých)" else "Povýšit vliv ($upgradeCost zlatých)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuildingsTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(gameState.buildings) { building ->
            val cost = building.baseCost * (building.level + 1)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(building.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Úroveň ${building.level}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(building.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))

                    Button(
                        onClick = {
                            val (success, msg) = engine.upgradeBuilding(building.type)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = gameState.player.gold >= cost,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Vylepšit budovu ($cost zlatých)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RentalsHubTab(gameState: GameSave) {
    val rented = gameState.characters.filter { it.naNajmu }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Přehled nájmů harému", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Otrokyně pronajaté klientům přinášejí 50 zlatých za každý den služby. Po uplynutí lhůty se vrací do komnat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }

        if (rented.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("V současnosti není žádná otrokyně na nájmu.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            items(rented) { character ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(character.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Klient: ${character.klient ?: "Neznámý"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Vyděláno celkem: ${character.najemPrijemCelkem} zlatých", fontSize = 11.sp, color = Color(0xFFFFD700))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF3E2723)) {
                            Text(
                                text = "Zbývá ${character.najemZbyvaDni} dní",
                                color = Color(0xFFFFB74D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedResourceItem(icon: String, name: String, value: Int, maxValue: Int? = null) {
    var previousValue by remember { mutableIntStateOf(value) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(value) {
        if (value != previousValue) {
            scale.animateTo(1.2f, animationSpec = tween(150))
            scale.animateTo(1f, animationSpec = tween(300))
        }
        previousValue = value
    }

    val displayValue = if (maxValue != null) "$value/$maxValue" else "$value"

    Row(
        modifier = Modifier
            .scale(scale.value)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(displayValue, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DomainResourceBanner(gameState: GameSave) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedResourceItem("🪵", "Dřevo", gameState.player.wood)
        AnimatedResourceItem("🪨", "Kamení", gameState.player.stone)
        AnimatedResourceItem("⛓️", "Železo", gameState.player.iron)
        AnimatedResourceItem("🔮", "Mana", gameState.player.mana, gameState.player.maxMana)
        AnimatedResourceItem("👥", "Populace", gameState.player.population, gameState.player.maxPopulation)
    }
}

@Composable
fun StatisticsTab(gameState: GameSave) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Historie produkce zlata", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    if (gameState.resourceHistory.size >= 2) {
                        val goldEntries = gameState.resourceHistory.mapIndexed { index, stat ->
                            FloatEntry(x = index.toFloat(), y = stat.goldProduced.toFloat())
                        }
                        
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(goldEntries),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                        Text(
                            text = "Není dostatek dat pro graf (ukonči den).",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f),
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Historie produkce surovin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    if (gameState.resourceHistory.size >= 2) {
                        val woodEntries = gameState.resourceHistory.mapIndexed { index, stat ->
                            FloatEntry(x = index.toFloat(), y = stat.woodProduced.toFloat())
                        }
                        
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(woodEntries),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        Text("Graf ukazuje dřevo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                    } else {
                        Text(
                            text = "Není dostatek dat.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                        )
                    }
                }
            }
        }
    }
}
