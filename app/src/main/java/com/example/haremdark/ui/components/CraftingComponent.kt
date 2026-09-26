package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun CraftingComponent(engine: GameEngine, gameState: GameSave) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⚒️ Kovárna a recyklace", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { engine.quickSalvage() }) {
                Text("Rychlá recyklace (vše běžné)")
            }
        }
    }
}
