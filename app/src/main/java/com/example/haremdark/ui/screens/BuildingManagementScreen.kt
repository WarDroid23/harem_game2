package com.example.haremdark.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Building

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingManagementScreen(
    engine: GameEngine,
    onBack: (() -> Unit)? = null
) {
    val gameState by engine.gameState.collectAsState()
    val buildings = gameState.buildings
    val player = gameState.player
    val context = LocalContext.current

    var selectedCategory by remember { mutableIntStateOf(0) } // 0: Vše, 1: Produkce, 2: Obrana, 3: Harém

    // Calculate domain defense level
    val baseDefense = 50
    val barracksLevel = buildings.find { it.type == "barracks" || it.type == "ubytovny" }?.level ?: 1
    val templeLevel = buildings.find { it.type == "chram_temnoty" }?.level ?: 0
    val dungeonsLevel = buildings.find { it.type == "mucirna" }?.level ?: 0
    val totalDefenseLevel = baseDefense + (barracksLevel * 25) + (templeLevel * 15) + (dungeonsLevel * 10)

    val defenseRating = when {
        totalDefenseLevel >= 150 -> "Nedobytná pevnost (Riziko přepadu: 2%)"
        totalDefenseLevel >= 100 -> "Silné opevnění (Riziko přepadu: 5%)"
        totalDefenseLevel >= 75 -> "Střední obrana (Riziko přepadu: 12%)"
        else -> "Základní posádka (Riziko přepadu: 25%)"
    }

    // Calculate resource output multipliers based on buildings
    val lumberCamp = buildings.find { it.type == "drevohorec" }?.level ?: 0
    val quarry = buildings.find { it.type == "kamenolom" }?.level ?: 0
    val ironMine = buildings.find { it.type == "zelezny_dul" || it.type == "mine" }?.level ?: 0
    val darkTemple = buildings.find { it.type == "chram_temnoty" }?.level ?: 0
    val chambers = buildings.find { it.type == "komnaty" }?.level ?: 0
    val gardens = buildings.find { it.type == "zahrady" }?.level ?: 0
    val lab = buildings.find { it.type == "laborator" }?.level ?: 0

    val woodMultiplier = 1.0f + (lumberCamp * 0.15f)
    val stoneMultiplier = 1.0f + (quarry * 0.15f)
    val ironMultiplier = 1.0f + (ironMine * 0.20f)
    val goldMultiplier = 1.0f + (ironMine * 0.05f) + (gardens * 0.10f)
    val manaMultiplier = 1.0f + (darkTemple * 0.20f) + (lab * 0.10f)
    val haremMoraleBonus = chambers * 5 + gardens * 8

    val filteredBuildings = remember(buildings, selectedCategory) {
        when (selectedCategory) {
            1 -> buildings.filter { it.type in listOf("drevohorec", "kamenolom", "zelezny_dul", "mine") }
            2 -> buildings.filter { it.type in listOf("ubytovny", "mucirna", "chram_temnoty", "barracks") }
            3 -> buildings.filter { it.type in listOf("komnaty", "lazne", "zahrady", "laborator", "training_hall") }
            else -> buildings
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🏰 Správa Budov & Dominia",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Obrana dominia a násobiče produkce surovin",
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
                // Domain Defense & Multipliers Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1424)),
                    border = BorderStroke(1.2.dp, Color(0xFF9C27B0).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text("Úroveň obrany dominia", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        "🛡️ $totalDefenseLevel bodů",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA5D6A7)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    defenseRating,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text("📈 Globální násobiče produkce dominia:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("💰 Zlato: ${"%.2f".format(goldMultiplier)}x", fontSize = 11.sp, color = Color(0xFFFFE082))
                                Text("🪵 Dřevo: ${"%.2f".format(woodMultiplier)}x", fontSize = 11.sp, color = Color(0xFFA1887F))
                            }
                            Column {
                                Text("🪨 Kámen: ${"%.2f".format(stoneMultiplier)}x", fontSize = 11.sp, color = Color(0xFFCFD8DC))
                                Text("⚙️ Železo: ${"%.2f".format(ironMultiplier)}x", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                            }
                            Column {
                                Text("🔮 Mana: ${"%.2f".format(manaMultiplier)}x", fontSize = 11.sp, color = Color(0xFF81D4FA))
                                Text("💖 Morálka: +$haremMoraleBonus", fontSize = 11.sp, color = Color(0xFFFF80AB))
                            }
                        }
                    }
                }
            }

            item {
                // Sklad / Player resources bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💰 ${player.gold}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        Text("🪵 ${player.wood}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA1887F))
                        Text("🪨 ${player.stone}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCFD8DC))
                        Text("⚙️ ${player.iron}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5))
                        Text("🧪 ${player.manaEssence}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    }
                }
            }

            item {
                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == 0,
                        onClick = { selectedCategory = 0 },
                        label = { Text("Vše (${buildings.size})", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCategory == 1,
                        onClick = { selectedCategory = 1 },
                        label = { Text("Suroviny", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCategory == 2,
                        onClick = { selectedCategory = 2 },
                        label = { Text("Obrana", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCategory == 3,
                        onClick = { selectedCategory = 3 },
                        label = { Text("Harém", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            items(filteredBuildings) { building ->
                BuildingUpgradeCard(
                    building = building,
                    playerGold = player.gold,
                    playerWood = player.wood,
                    playerStone = player.stone,
                    playerIron = player.iron,
                    onUpgrade = {
                        val (success, msg) = engine.upgradeBuilding(building.type)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BuildingUpgradeCard(
    building: Building,
    playerGold: Int,
    playerWood: Int,
    playerStone: Int,
    playerIron: Int,
    onUpgrade: () -> Unit
) {
    val nextLevel = building.level + 1
    val costGold = building.baseCost * nextLevel
    val costWood = building.baseCostWood * nextLevel
    val costStone = building.baseCostStone * nextLevel
    val costIron = building.baseCostIron * nextLevel

    val canAfford = playerGold >= costGold &&
                    playerWood >= costWood &&
                    playerStone >= costStone &&
                    playerIron >= costIron

    val icon = when (building.type) {
        "drevohorec" -> "🪵"
        "kamenolom" -> "🪨"
        "zelezny_dul", "mine" -> "⛏️"
        "chram_temnoty" -> "🔮"
        "ubytovny", "barracks" -> "🛡️"
        "komnaty" -> "👑"
        "lazne" -> "♨️"
        "mucirna" -> "⛓️"
        "laborator" -> "🧪"
        "zahrady" -> "🌺"
        "training_hall" -> "⚔️"
        else -> "🏰"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(icon, fontSize = 22.sp)
                    Column {
                        Text(
                            building.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            building.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        "Úroveň ${building.level}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Current effect & Next level preview
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Aktuální bonus: +${(building.level * 15)}% efektivita",
                        fontSize = 11.sp,
                        color = Color(0xFF81C784)
                    )
                    Text(
                        text = "Další úroveň ($nextLevel): +${(nextLevel * 15)}% efektivita & obrana dominia",
                        fontSize = 10.sp,
                        color = Color(0xFFFFD54F)
                    )
                }
            }

            // Cost Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cena vylepšení:", fontSize = 10.sp, color = Color.Gray)
                if (costGold > 0) {
                    ResourceCostChip(
                        label = "$costGold zl",
                        isSufficient = playerGold >= costGold,
                        colorHex = 0xFFFFD700
                    )
                }
                if (costWood > 0) {
                    ResourceCostChip(
                        label = "$costWood dř",
                        isSufficient = playerWood >= costWood,
                        colorHex = 0xFFA1887F
                    )
                }
                if (costStone > 0) {
                    ResourceCostChip(
                        label = "$costStone kam",
                        isSufficient = playerStone >= costStone,
                        colorHex = 0xFFCFD8DC
                    )
                }
                if (costIron > 0) {
                    ResourceCostChip(
                        label = "$costIron žel",
                        isSufficient = playerIron >= costIron,
                        colorHex = 0xFFB0BEC5
                    )
                }
            }

            // Upgrade Button
            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C)
                )
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (canAfford) "Vylepšit na Úroveň $nextLevel" else "Nedostatek surovin na vylepšení",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ResourceCostChip(label: String, isSufficient: Boolean, colorHex: Long) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isSufficient) Color(colorHex).copy(alpha = 0.15f) else Color(0xFFEF5350).copy(alpha = 0.2f),
        border = BorderStroke(
            0.8.dp,
            if (isSufficient) Color(colorHex).copy(alpha = 0.5f) else Color(0xFFEF5350).copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSufficient) Color(colorHex) else Color(0xFFEF5350),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}
