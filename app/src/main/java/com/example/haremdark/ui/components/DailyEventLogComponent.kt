package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.haremdark.models.DailyEvent
import com.example.haremdark.models.EventType

@Composable
fun DailyEventLogComponent(events: List<DailyEvent>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dnešní události", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
            items(events) { event ->
                Card(
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (event.type == EventType.CHALLENGE) Color(0xFFC62828) else Color(0xFF2E7D32)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(event.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(event.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        Text("Dopad: ${event.impact}", style = MaterialTheme.typography.labelSmall, color = Color.Yellow)
                    }
                }
            }
        }
    }
}
