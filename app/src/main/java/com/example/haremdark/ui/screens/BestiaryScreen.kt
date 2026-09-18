package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.models.BestiaryEntry
import com.example.haremdark.models.GameSave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiaryScreen(
    gameState: GameSave,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bestiář") },
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
            items(gameState.bestiaryEntries.filter { it.isDiscovered }) { entry ->
                BestiaryCard(entry)
            }
        }
    }
}

@Composable
fun BestiaryCard(entry: BestiaryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${entry.icon} ${entry.name}", style = MaterialTheme.typography.titleLarge)
            Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = entry.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Počet střetnutí: ${entry.encounterCount}")
        }
    }
}
