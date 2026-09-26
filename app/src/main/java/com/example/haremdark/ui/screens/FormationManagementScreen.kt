package com.example.haremdark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.FormationRow
import com.example.haremdark.models.HaremCharacter

@Composable
fun FormationManagementScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    val characters = gameState.characters
    
    // Simple state for formation (Map of Row to List of CharacterIds)
    // For now, let's keep it simple with just rows
    var frontRow by remember { mutableStateOf(listOf<HaremCharacter>()) }
    var middleRow by remember { mutableStateOf(listOf<HaremCharacter>()) }
    var backRow by remember { mutableStateOf(listOf<HaremCharacter>()) }
    
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("⚔️ Správa formace", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Přední linie (Tank)", style = MaterialTheme.typography.titleMedium)
        FormationRowView(row = FormationRow.FRONT, characters = frontRow)
        
        Text("Střední linie (DPS)", style = MaterialTheme.typography.titleMedium)
        FormationRowView(row = FormationRow.MIDDLE, characters = middleRow)
        
        Text("Zadní linie (Support/Magic)", style = MaterialTheme.typography.titleMedium)
        FormationRowView(row = FormationRow.BACK, characters = backRow)
    }
}

@Composable
fun FormationRowView(row: FormationRow, characters: List<HaremCharacter>) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).height(60.dp), verticalAlignment = Alignment.CenterVertically) {
            if (characters.isEmpty()) {
                Text("Prázdné místo", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            } else {
                characters.forEach { char ->
                    Text(char.name, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
