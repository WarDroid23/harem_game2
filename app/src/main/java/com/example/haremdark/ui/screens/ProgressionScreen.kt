package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ProgressionScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = gameState.player

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Player Level & XP Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Column {
                                Text("Pán dominia: ${player.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (player.activeTitle != null) {
                                    val titleObj = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == player.activeTitle }
                                    if (titleObj != null) {
                                        Text("${titleObj.badgeIcon} ${titleObj.title}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("Úroveň ${player.level} • Titul: ${player.cityTitle}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                            Text(
                                text = "⭐ ${player.skillPoints} bodů",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val xpProgress = (player.xp.toFloat() / player.xpNext.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Zkušenosti: ${player.xp}/${player.xpNext} XP", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("Reputace: ${player.reputation}", fontSize = 10.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Endurance Training (Trénink výdrže)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261822)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFE91E63))
                            Text("Trénink tělesné a temné výdrže", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    Text(
                        "Tréninkem trvale navýšíš maximální kapacitu Sexuální (+8) a Temné (+5) energie pro delší seance s harémem.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    val enduranceLevel = player.skills["vytrvalost"] ?: 0
                    val cost = 120 + enduranceLevel * 80

                    Button(
                        onClick = {
                            val (success, msg) = engine.trainEndurance()
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = player.gold >= cost && player.skillPoints >= 1,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Trénovat výdrž ($cost zlata + 1 bod)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Titul a Úspěchy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (player.unlockedAchievements.isEmpty()) {
                        Text("Zatím nemáš žádné úspěchy. Buduj dominium a harém!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        player.unlockedAchievements.forEach { achId ->
                            val ach = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == achId }
                            if (ach != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ach.badgeIcon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(ach.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }
                                    if (ach.isTitle) {
                                        OutlinedButton(
                                            onClick = {
                                                engine.setActiveTitle(ach.id)
                                                Toast.makeText(context, "Titul nastaven!", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(if (player.activeTitle == ach.id) "Aktivní" else "Vybrat", fontSize = 10.sp)
                                        }
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        }
        
        // Skill Tree List
        item {
            Text("Dovednosti Pána", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val skillDescriptions = mapOf(
            "svádění" to "Zvyšuje účinnost romantických a intimních interakcí.",
            "obchod" to "Zvyšuje zisky z mafie a snižuje ceny na aukcích.",
            "velení" to "Zvyšuje pasivní zisk zkušeností celého harému.",
            "temnota" to "Posiluje sílu temných rituálů a kouzel v boji.",
            "obrana" to "Snižuje poškození obdržené v soubojích s bossy.",
            "dominance" to "Zvyšuje zisk poslušnosti a submisivity při trestech.",
            "boj" to "Zvyšuje útočné poškození zbraněmi."
        )

        items(skillDescriptions.entries.toList()) { (skillKey, desc) ->
            val curLevel = player.skills[skillKey] ?: 0
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(skillKey.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("Úroveň: $curLevel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = {
                            val (success, msg) = engine.upgradeSkill(skillKey)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        enabled = player.skillPoints > 0,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+1", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Inventory & Equipment
        item {
            Text("Vybavení a Zbraně", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(player.weapons) { weapon ->
            val isEquipped = player.weapons.indexOf(weapon) == player.equippedWeaponIndex
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEquipped) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                border = if (isEquipped) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(weapon.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Poškození: ${weapon.damage} • Bonus k temnotě: +${weapon.darkBonus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    if (isEquipped) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                            Text("Nasezeno", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
