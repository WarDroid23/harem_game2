package com.example.haremdark.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.Character
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.VoiceManager

@Composable
fun DailyAssignmentsTab(character: Character, engine: GameEngine?) {
    val assignments = listOf(
        Pair("cooking", "🍲 Vaření" to "Připraví výborné jídlo. Zvyšuje tvou sexuální energii o +5 každý den."),
        Pair("alchemy", "🧪 Sběr bylin" to "Prohledá okolí pro alchymii. Zvyšuje tvou temnou energii o +3 každý den."),
        Pair("library", "📚 Organizace knihovny" to "Studuje a uklízí archivy. Dívka získá +10 ZK každý den."),
        Pair("cleaning", "🧹 Úklid panství" to "Udržuje tvé sídlo v čistotě. Získáš +10 zlatých každý den."),
        Pair(null, "❌ Žádný úkol" to "Dívka nebude mít přidělený žádný specifický denní úkol.")
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "📋 Denní Úkoly",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Přiděl dívce konkrétní práci, kterou bude vykonávat každý den, a zajisti si tak stabilní přísun bonusů.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(assignments) { (id, info) ->
                val (title, desc) = info
                val isSelected = character.dailyAssignment == id
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        engine?.setDailyAssignment(character.id, id)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            Text(desc, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                        }
                        if (isSelected) {
                            Text("✓ Aktivní", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyMomentsTab(character: Character) {
    // Filter high-loyalty/affinity history logs
    val keyLogs = character.affinityHistory.filter { 
        it.source.contains("Erotická") || it.source.contains("Dvojitá") || it.source.contains("Oslava") || it.source.contains("Romance") || it.source.contains("Milník") || it.points >= 50
    }.reversed() // Show newest first

    // If empty, just show some default or the best logs
    val displayLogs = if (keyLogs.isNotEmpty()) keyLogs else character.affinityHistory.sortedByDescending { it.points }.take(5)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "🕰️ Klíčové Momenty (Flashbacky)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Přehraj si zvukové vzpomínky na nejdůležitější okamžiky vašeho vztahu.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (displayLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Zatím jste nezažili žádné významné okamžiky.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Den ${log.day}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                Text(log.source, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Dosáhla úrovně náklonnosti: ${log.points}", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                val text = "Den ${log.day}. Pamatuji si to... ${log.source}. Má náklonnost k tobě tehdy byla na úrovni ${log.points}."
                                VoiceManager.speak(text, character.archetypeId)
                            }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Přehrát", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
