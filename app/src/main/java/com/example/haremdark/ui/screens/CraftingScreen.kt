package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.AlchemyRecipe
import com.example.haremdark.models.GameSave

@Composable
fun CraftingScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Alchymistická laboratoř", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GameContent.ALCHEMY_RECIPES) { recipe ->
                CraftingRecipeCard(recipe = recipe, gameState = gameState, engine = engine)
            }
        }
    }
}

@Composable
fun CraftingRecipeCard(
    recipe: AlchemyRecipe,
    gameState: GameSave,
    engine: GameEngine
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = gameState.player

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.titleLarge)
            Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
            Text("Náklady: ${recipe.goldCost} zlata, ${recipe.darkCost} temné energie", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { 
                val (success, msg) = engine.craftItem(recipe.id)
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }) {
                Text("Syntetizovat")
            }
        }
    }
}
