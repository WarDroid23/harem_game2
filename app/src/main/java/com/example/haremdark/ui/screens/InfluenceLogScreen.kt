package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InfluenceLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfluenceLogScreen(
    gameState: GameSave,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log vlivu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<-")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gameState.influenceLog.reversed()) { entry ->
                InfluenceLogCard(entry)
            }
        }
    }
}

@Composable
fun InfluenceLogCard(entry: InfluenceLogEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Den ${entry.day}: ${entry.characterName}", style = MaterialTheme.typography.titleMedium)
            Text(text = entry.choiceDescription, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Vliv: ${if(entry.influenceChange >= 0) "+" else ""}${entry.influenceChange}", 
                 style = MaterialTheme.typography.labelLarge,
                 color = if (entry.influenceChange >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Text(text = entry.reason, style = MaterialTheme.typography.bodySmall)
        }
    }
}
