package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine

@Composable
fun MarketScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    val player = gameState.player

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Trh", style = MaterialTheme.typography.headlineMedium)
        Text("Zlato: ${player.gold}", style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Prodej surovin", style = MaterialTheme.typography.titleLarge)
        
        Button(onClick = { engine.sellResource("wood", 10, 5) }) { Text("Prodat 10 dřeva (50 zl)") }
        Button(onClick = { engine.sellResource("stone", 10, 8) }) { Text("Prodat 10 kamene (80 zl)") }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Prodej otrokyň", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(gameState.characters) { char ->
                Card(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(char.name)
                        Button(onClick = { engine.sellCharacter(char.id) }) { Text("Prodat za 500 zl") }
                    }
                }
            }
        }
    }
}
