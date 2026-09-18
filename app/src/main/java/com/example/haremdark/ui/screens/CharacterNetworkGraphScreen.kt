package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.CharacterDetailDialog
import com.example.haremdark.ui.components.CharacterNetworkGraphComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterNetworkGraphScreen(
    gameState: GameSave,
    engine: GameEngine,
    onMenuClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedCharacterForModal by remember { mutableStateOf<Character?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🕸️ Síť Vztahů Harému") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CharacterNetworkGraphComponent(
                characters = gameState.characters,
                player = gameState.player,
                engine = engine,
                onSelectCharacter = { char ->
                    selectedCharacterForModal = char
                }
            )

            selectedCharacterForModal?.let { character ->
                val currentConcubine = gameState.characters.firstOrNull { it.id == character.id } ?: character
                CharacterDetailDialog(
                    character = currentConcubine,
                    player = gameState.player,
                    onDismiss = { selectedCharacterForModal = null },
                    onGiveDirectGift = { gift ->
                        val (success, msg) = engine.giveDirectGift(gift.id, currentConcubine.id)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        VoiceManager.speak(msg, currentConcubine.archetypeId)
                    },
                    onUseInventoryItem = { item ->
                        val (success, msg) = engine.useItemOnConcubine(item.id, currentConcubine.id)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        VoiceManager.speak(msg, currentConcubine.archetypeId)
                    },
                    onExecuteInteraction = { interaction ->
                        val (success, msg) = engine.executeInteraction(currentConcubine.id, interaction)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (interaction.id == "drobna") {
                            VoiceManager.speakPet(currentConcubine)
                        } else if (interaction.id == "pochvala") {
                            VoiceManager.speakPraise(currentConcubine)
                        } else {
                            VoiceManager.speak(msg, currentConcubine.archetypeId)
                        }
                    },
                    onCourtRomance = {
                        val (success, msg) = engine.courtRomance(currentConcubine.id)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        VoiceManager.speak(msg, currentConcubine.archetypeId)
                    },
                    onMarry = {
                        val (success, msg) = engine.marryConcubine(currentConcubine.id)
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        VoiceManager.speak(msg, currentConcubine.archetypeId)
                    },
                    onRent = { client, days ->
                        val (success, msg) = engine.rentSlave(currentConcubine.id, client, days)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        VoiceManager.speak(msg, currentConcubine.archetypeId)
                        if (success) selectedCharacterForModal = null
                    },
                    onUpgradeSkill = { skill ->
                        val (success, msg) = engine.upgradeCharacterSkill(currentConcubine.id, skill)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        VoiceManager.speak(msg, currentConcubine.archetypeId)
                    },
                    onEquipItem = { itemId, slotId ->
                        engine.equipItemToCharacter(currentConcubine.id, itemId, slotId)
                        Toast.makeText(context, "Předmět vybaven.", Toast.LENGTH_SHORT).show()
                    },
                    onUnequipItem = { slotId ->
                        engine.unequipItemFromCharacter(currentConcubine.id, slotId)
                        Toast.makeText(context, "Předmět odepnut.", Toast.LENGTH_SHORT).show()
                    },
                    engine = engine
                )
            }
        }
    }
}
