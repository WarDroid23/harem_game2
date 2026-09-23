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
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun DomainExpansionScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val player = gameState.player

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rozšíření dominia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Aktuální úroveň: ${player.domainExpansionLevel}", fontSize = 14.sp)
                    Text("Vylepšuj své dominium pro zvýšení kapacity harému a pasivní bonusy.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
        }

        item {
            Text("Dostupná vylepšení", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        // Example upgrade: Capacity
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Zvětšení ubytovny", fontWeight = FontWeight.Bold)
                    Text("Zvyšuje maximální počet otrokyň o 10.", fontSize = 12.sp)
                    Button(
                        onClick = { /* Implement logic in GameEngine */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vylepšit (1000 Zlata)")
                    }
                }
            }
        }
    }
}
