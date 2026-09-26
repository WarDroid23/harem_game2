package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine

@Composable
fun GuildScreen(engine: GameEngine) {
    val gameState by engine.gameState.collectAsState()
    val guild = gameState.guild

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🏰 Cech (Guilda)", style = MaterialTheme.typography.headlineMedium)
        
        if (guild == null) {
            Button(onClick = { engine.createGuild("Cech Stínů") }) {
                Text("Vytvořit cech")
            }
        } else {
            Text("Jsi členem: ${guild.name}")
            // Messaging
            var newMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(guild.messages) { msg ->
                    Text("${msg.senderName}: ${msg.message}")
                }
            }
            Row {
                TextField(value = newMessage, onValueChange = { newMessage = it }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    if (newMessage.isNotBlank()) {
                        engine.sendGuildMessage("Pán", newMessage)
                        newMessage = ""
                    }
                }) {
                    Text("Poslat")
                }
            }
        }
    }
}
