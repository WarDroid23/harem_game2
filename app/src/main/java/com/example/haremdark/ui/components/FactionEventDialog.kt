package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.models.FactionEvent

@Composable
fun FactionEventDialog(event: FactionEvent, onResolve: (com.example.haremdark.models.EventOption) -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("${event.faction.title}: ${event.title}") },
        text = {
            Column {
                Text(event.description)
                Spacer(modifier = Modifier.height(16.dp))
                event.options.forEach { option ->
                    Button(
                        onClick = { onResolve(option) },
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Text(option.text)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
