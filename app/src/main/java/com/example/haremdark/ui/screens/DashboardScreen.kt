package com.example.haremdark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine

@Composable
fun DashboardScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    val dailyEvents by engine.dailyEvents.collectAsState()
    val player = gameState.player

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_header_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏰 Palubní deska Dominia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pán: ${player.name} • Den ${player.day} (Úroveň ${player.level})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "⚡ Aktivní",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Primary Game Statistics Grid Layout
        Text(
            text = "📊 Hlavní statistiky dominia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val statItems = listOf(
            StatGridItem("Zlato", "${player.gold}", "Pokladnice", Icons.Default.MonetizationOn, Color(0xFFFFD700)),
            StatGridItem("Sexuální energie", "${player.sexEnergy}/${player.maxSexEnergy}", "Vitalita", Icons.Default.Bolt, Color(0xFFFF80AB)),
            StatGridItem("Mana / Energie", "${player.mana}/${player.maxMana}", "Magická síla", Icons.Default.AutoAwesome, Color(0xFF80D8FF)),
            StatGridItem("Vliv (Influence)", "${player.influence}", "Moc ve městě", Icons.Default.Public, Color(0xFF03A9F4)),
            StatGridItem("Mana Essence", "${player.manaEssence}", "Alchymie", Icons.Default.Science, Color(0xFF00E676)),
            StatGridItem("Velikost Harému", "${gameState.characters.size} dívek", "Společenky", Icons.Default.Groups, Color(0xFFE040FB))
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            statItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_grid_card_${item.title.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(item.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = item.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = item.value,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 9.sp,
                                        color = item.color,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress & Status Indicators Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "📈 Pokrok stavu pána",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatRow("Zdraví (HP)", player.hp, player.maxHp, Color(0xFFEF5350))
                StatRow("Mana", player.mana, player.maxMana, Color(0xFF42A5F5))
                StatRow("Vliv", player.influence, player.maxInfluence.coerceAtLeast(100), Color(0xFF66BB6A))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Daily Event Log Component
        com.example.haremdark.ui.components.DailyEventLogComponent(events = dailyEvents)

        Spacer(modifier = Modifier.height(8.dp))

        // Performance Chart
        PerformanceChartScreen(engine = engine)
        
        Spacer(modifier = Modifier.height(8.dp))

        // Harem Power Chart
        Card(modifier = Modifier.fillMaxWidth()) {
            HaremPowerChartScreen(engine = engine)
        }
        
        // Crafting System
        com.example.haremdark.ui.components.CraftingComponent(engine = engine, gameState = gameState)
    }
}

@Composable
fun StatRow(label: String, current: Int, max: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$current/$max", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (current.toFloat() / max.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp)
        )
    }
}

data class StatGridItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)
