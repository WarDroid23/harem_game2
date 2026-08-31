package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun SaveSettingsScreen(
    gameState: GameSave,
    currentTheme: String,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themes = listOf(
        "Temné dominium" to Color(0xFFB71C1C),
        "Krvavý trůn" to Color(0xFFD32F2F),
        "Ledová panenka" to Color(0xFF00B0FF),
        "Zelený had" to Color(0xFF00C853),
        "Růžový hedváb" to Color(0xFFE91E63),
        "Monochrom" to Color(0xFFE0E0E0)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Theme Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vizuální téma dominia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Zvol si atmosféru a barevnou paletu rozhraní hry:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        themes.forEach { (themeName, color) ->
                            val isSelected = currentTheme == themeName
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { engine.setTheme(themeName) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = themeName.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Save & Load Slots
        item {
            Text("Ukládání a Načítání hry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        // Autosave info
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Slot 0: Automatické uložení", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(engine.getSlotSummary(0), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    OutlinedButton(
                        onClick = {
                            val ok = engine.loadFromSlot(0)
                            Toast.makeText(context, if (ok) "Autosave načten!" else "Chyba načtení!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Načíst", fontSize = 11.sp)
                    }
                }
            }
        }

        // Slots 1 to 5
        items((1..5).toList()) { slot ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Slot $slot", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(engine.getSlotSummary(slot), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                engine.saveToSlot(slot)
                                Toast.makeText(context, "Hra uložena do slotu $slot!", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Uložit", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val ok = engine.loadFromSlot(slot)
                                Toast.makeText(context, if (ok) "Slot $slot načten!" else "Slot je prázdný!", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Načíst", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Global Statistics
        item {
            Text("Globální statistiky dominia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val totalKids = gameState.concubines.sumOf { it.deti }
                    val totalWives = gameState.concubines.count { it.jeManzelkou }
                    val totalRentEarned = gameState.concubines.sumOf { it.najemPrijemCelkem }

                    GlobalStatRow("Dnů vlády nad dominiem", "${gameState.player.day}")
                    GlobalStatRow("Počet dívek v harému", "${gameState.concubines.size}")
                    GlobalStatRow("Počet manželek", "$totalWives")
                    GlobalStatRow("Narozených dětí páně", "$totalKids")
                    GlobalStatRow("Poražených bossů", "${gameState.defeatedBosses.size}")
                    GlobalStatRow("Zabitých nepřátel celkem", "${gameState.player.killCount}")
                    GlobalStatRow("Celkový výdělek z nájmů", "$totalRentEarned zlatých")
                }
            }
        }
    }
}

@Composable
fun GlobalStatRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
