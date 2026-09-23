package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine

@Composable
fun DashboardScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    val dailyEvents by engine.dailyEvents.collectAsState()
    val player = gameState.player

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Palubní deska Dominia", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Treasury Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pokladnice", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("${player.gold} Zlatých", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StatRow("Zdraví", player.hp, player.maxHp, Color(0xFFEF5350))
                StatRow("Energie (Mana)", player.mana, player.maxMana, Color(0xFF42A5F5))
                StatRow("Vliv", player.influence, player.maxInfluence, Color(0xFF66BB6A))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Event Log
        com.example.haremdark.ui.components.DailyEventLogComponent(events = dailyEvents)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Performance Chart
        com.example.haremdark.ui.screens.PerformanceChartScreen(engine = engine)
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
