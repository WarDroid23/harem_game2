package com.example.haremdark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.AchievementList
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.GameTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    gameState: GameSave,
    engine: GameEngine,
    onMenuClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        engine.checkAchievements()
    }

    Box(modifier = Modifier.fillMaxSize()) { val padding = PaddingValues(0.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Milníky a Výzvy",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val unlockedIds = gameState.player.unlockedAchievements
            val allAchievements = AchievementList.allAchievements

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(allAchievements) { ach ->
                    val isUnlocked = unlockedIds.contains(ach.id)
                    val progressText = getProgressString(ach.id, gameState)
                    val progressFraction = getProgressFraction(ach.id, gameState)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Text(text = ach.badgeIcon, fontSize = 24.sp)
                                } else {
                                    Icon(
                                        Icons.Default.Lock, 
                                        contentDescription = "Zamčeno", 
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ach.description,
                                    fontSize = 14.sp,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { if (isUnlocked) 1f else progressFraction },
                                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isUnlocked) "100%" else progressText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getProgressString(id: String, state: GameSave): String {
    return when (id) {
        "ach_battles_100" -> "${state.player.battlesWon}/100"
        "ach_max_affinity" -> {
            val maxAff = state.characters.maxOfOrNull { it.affinityPoints } ?: 0
            "${maxAff.coerceAtMost(100)}/100"
        }
        "ach_harem_10" -> "${state.characters.size}/10"
        "ach_harem_20" -> "${state.characters.size}/20"
        "ach_affinity_total" -> "${state.characters.sumOf { it.affinityPoints }}/250"
        "ach_boss_slayer" -> "${state.defeatedBosses.size}/3"
        "ach_arena_champion" -> {
            val maxLevel = state.characters.maxOfOrNull { it.level } ?: 0
            "${maxLevel.coerceAtMost(10)}/10"
        }
        "ach_wealthy" -> {
            val gold = state.player.gold
            "${gold.coerceAtMost(10000)}/10k"
        }
        "ach_domain_max" -> {
            val fortressLevel = state.buildings.firstOrNull { it.type == "pevnost" }?.level ?: 1
            "${fortressLevel.coerceAtMost(5)}/5"
        }
        "ach_blood_sister" -> {
            val hasIt = state.characters.any { it.fazeZkazenosti >= 5 && it.bloodlust >= 50 }
            if (hasIt) "1/1" else "0/1"
        }
        else -> "0/1"
    }
}

private fun getProgressFraction(id: String, state: GameSave): Float {
    return when (id) {
        "ach_battles_100" -> (state.player.battlesWon / 100f).coerceIn(0f, 1f)
        "ach_max_affinity" -> {
            val maxAff = state.characters.maxOfOrNull { it.affinityPoints } ?: 0
            (maxAff / 100f).coerceIn(0f, 1f)
        }
        "ach_harem_10" -> (state.characters.size / 10f).coerceIn(0f, 1f)
        "ach_harem_20" -> (state.characters.size / 20f).coerceIn(0f, 1f)
        "ach_affinity_total" -> (state.characters.sumOf { it.affinityPoints } / 250f).coerceIn(0f, 1f)
        "ach_boss_slayer" -> (state.defeatedBosses.size / 3f).coerceIn(0f, 1f)
        "ach_arena_champion" -> {
            val maxLevel = state.characters.maxOfOrNull { it.level } ?: 0
            (maxLevel / 10f).coerceIn(0f, 1f)
        }
        "ach_wealthy" -> (state.player.gold / 10000f).coerceIn(0f, 1f)
        "ach_domain_max" -> {
            val fortressLevel = state.buildings.firstOrNull { it.type == "pevnost" }?.level ?: 1
            (fortressLevel / 5f).coerceIn(0f, 1f)
        }
        "ach_blood_sister" -> {
            val hasIt = state.characters.any { it.fazeZkazenosti >= 5 && it.bloodlust >= 50 }
            if (hasIt) 1f else 0f
        }
        else -> 0f
    }
}
