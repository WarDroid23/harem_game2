package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave

@Composable
fun GiftingModal(
    character: Character,
    gameState: GameSave,
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val giftItems = gameState.player.items.filter { it.category == "gift" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Darovat ${character.name}") },
        text = {
            if (giftItems.isEmpty()) {
                Text("Nemáš žádné dárky v inventáři.")
            } else {
                LazyColumn {
                    items(giftItems) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            leadingContent = { Text(item.icon) },
                            trailingContent = {
                                Button(onClick = {
                                    engine.giveDirectGift(item.id, character.id)
                                    onDismiss()
                                }) {
                                    Text("Darovat")
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít")
            }
        }
    )
}
