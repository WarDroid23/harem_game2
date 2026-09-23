package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.haremdark.models.EncounterCard

@Composable
fun EncounterCardComponent(
    card: EncounterCard,
    onResolve: (EncounterCard) -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${card.icon} ${card.title}", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = card.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onResolve(card) }) {
                Text("Rozhodnout")
            }
        }
    }
}
