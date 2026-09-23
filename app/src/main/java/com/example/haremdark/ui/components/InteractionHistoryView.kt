package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.InteractionLogEntry

@Composable
fun InteractionHistoryView(history: List<InteractionLogEntry>) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Historie interakcí", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history.reversed()) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261238)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFFCE93D8))
                            Text("Den ${entry.day}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(entry.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        if (entry.statChanges.isNotEmpty()) {
                            Text(entry.statChanges, fontSize = 10.sp, color = Color(0xFFFFD54F))
                        }
                    }
                }
            }
        }
    }
}
