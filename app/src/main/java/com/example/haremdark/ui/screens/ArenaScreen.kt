package com.example.haremdark.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.R
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import kotlin.random.Random

@Composable
fun ArenaScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedGirls by remember { mutableStateOf(setOf<String>()) }
    var arenaLog by remember { mutableStateOf(listOf<String>()) }
    var battleFinished by remember { mutableStateOf(false) }

    val maxTeamSize = 3

    if (battleFinished) {
        // Battle Results Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Záznam z Arény",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF140D1E), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(arenaLog) { log ->
                    Text(text = log, color = Color(0xFFE1BEE7), fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    battleFinished = false
                    selectedGirls = setOf()
                    arenaLog = emptyList()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Vrátit se do výběru bojovnic", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_arena_battle),
                    contentDescription = "Krvavá Aréna",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000)))))
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                ) {
                    Text("Krvavá Aréna Dominia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Prestiž pána: ${gameState.player.prestige} 🏆", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        Text(
            text = "Vyber svůj harémový tým (Max $maxTeamSize): ${selectedGirls.size}/$maxTeamSize",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gameState.characters) { girl ->
                val isSelected = selectedGirls.contains(girl.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) {
                                selectedGirls = selectedGirls - girl.id
                            } else if (selectedGirls.size < maxTeamSize && girl.hp > 10) {
                                selectedGirls = selectedGirls + girl.id
                            }
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(girl.name, fontWeight = FontWeight.Bold)
                            Text("HP: ${girl.hp}/${girl.maxHp} | Loajalita: ${girl.loajalita}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Vybráno", tint = MaterialTheme.colorScheme.primary)
                        } else if (girl.hp <= 10) {
                            Text("Zraněná", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val result = engine.runArenaExpedition(selectedGirls.toList())
                arenaLog = result
                battleFinished = true
            },
            enabled = selectedGirls.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("VSTOUPIT DO ARÉNY", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
