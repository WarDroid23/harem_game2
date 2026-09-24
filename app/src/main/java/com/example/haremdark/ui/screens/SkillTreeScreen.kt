package com.example.haremdark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.HaremCharacter
import com.example.haremdark.models.SkillTreeData

@Composable
fun SkillTreeScreen(engine: GameEngine) {
    val characters by engine.haremCharacterRepository.characters.collectAsState()
    var selectedCharacter by remember { mutableStateOf<HaremCharacter?>(characters.firstOrNull()) }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Character List
        LazyColumn(modifier = Modifier.width(200.dp)) {
            items(characters) { char ->
                Button(
                    onClick = { selectedCharacter = char },
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                ) {
                    Text(char.name)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Skill Tree
        selectedCharacter?.let { char ->
            Column {
                Text("Dovednosti: ${char.name} (Body: ${char.availableSkillPoints})", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(SkillTreeData.nodes) { node ->
                        val isUnlocked = char.unlockedSkills.contains(node.id)
                        val canUnlock = char.availableSkillPoints >= node.cost && 
                                       (node.requiresId == null || char.unlockedSkills.contains(node.requiresId))
                        
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(node.title, style = MaterialTheme.typography.titleMedium)
                                Text(node.description)
                                Text("Cena: ${node.cost} bodů")
                                Button(
                                    onClick = { engine.purchaseSkillNode(char.id, node.id) },
                                    enabled = !isUnlocked && canUnlock
                                ) {
                                    Text(if (isUnlocked) "Odemčeno" else "Odemknout")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
