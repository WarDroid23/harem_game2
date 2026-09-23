package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun CraftingScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val recipes = GameContent.ALCHEMY_RECIPES
    val resources = gameState.player.craftingResources

    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Alchymistická laboratoř", style = MaterialTheme.typography.titleLarge) }
        
        items(recipes) { recipe ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                    Text(recipe.description, style = MaterialTheme.typography.bodySmall)
                    
                    Text("Potřebné suroviny:", style = MaterialTheme.typography.labelMedium)
                    recipe.materials.forEach { (matId, amount) ->
                        val current = resources[matId] ?: 0
                        Text("$matId: $current / $amount")
                    }
                    
                    Button(onClick = { engine.craftItem(recipe.id) }) {
                        Text("Vyrobit")
                    }
                }
            }
        }
    }
}
