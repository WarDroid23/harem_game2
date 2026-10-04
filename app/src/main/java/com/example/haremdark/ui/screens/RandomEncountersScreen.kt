package com.example.haremdark.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.EventCategory
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.NarrativeEvent
import com.example.haremdark.ui.components.NarrativeEventDialog

/**
 * Screen / Tab dedicated to the Random Narrative Event Generator and encounter chronicles.
 */
@Composable
fun RandomEncountersScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<EventCategory?>(null) }
    var activeEvent by remember { mutableStateOf<NarrativeEvent?>(null) }
    var totalEncountersResolved by remember { mutableIntStateOf(0) }

    val player = gameState.player

    // Modal dialog when an event is triggered
    if (activeEvent != null) {
        NarrativeEventDialog(
            event = activeEvent!!,
            playerSkills = player.skills,
            onSelectChoice = { choiceId ->
                val result = engine.resolveNarrativeEventChoice(activeEvent!!, choiceId)
                totalEncountersResolved++
                result
            },
            onDismiss = { activeEvent = null }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // --- 1. HERO BANNER & TRIGGER CONSOLE ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("random_event_generator_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0E26)),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.8f),
                            Color(0xFF8E24AA).copy(alpha = 0.5f),
                            Color(0xFF4A148C)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎲", fontSize = 22.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = "Generátor Náhodných Událostí",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Rozhodnutí, morální dilemata a unikátní odměny",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF80AB)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Vydej se na neprobádané stezky dominia nebo do temných uliček metropole. Každá volba ovlivní vliv, suroviny a loajalitu tvých dívek.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )

                    // Category Filter Selector
                    Text(
                        text = "Zaměření sféry střetnutí:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = (selectedCategoryFilter == null),
                                onClick = {
                                    HapticManager.vibrateClick()
                                    selectedCategoryFilter = null
                                },
                                label = { Text("✨ Všechny sféry", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6A1B9A),
                                    selectedLabelColor = Color(0xFFFFD700)
                                )
                            )
                        }

                        items(EventCategory.entries) { category ->
                            val isSelected = (selectedCategoryFilter == category)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    HapticManager.vibrateClick()
                                    selectedCategoryFilter = category
                                },
                                leadingIcon = {
                                    Text(category.icon, fontSize = 11.sp)
                                },
                                label = { Text(category.displayName, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6A1B9A),
                                    selectedLabelColor = Color(0xFFFFD700)
                                )
                            )
                        }
                    }

                    // Primary Trigger Action Button
                    Button(
                        onClick = {
                            HapticManager.vibrateHeavy()
                            val event = engine.triggerRandomNarrativeEvent(selectedCategoryFilter)
                            activeEvent = event
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B1FA2)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("trigger_random_event_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎲", fontSize = 18.sp)
                            Text(
                                "Spustit Náhodné Střetnutí",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. REPLAYABILITY & RECENT DECISIONS CHRONICLE ---
        item {
            Text(
                text = "📜 Kronika Nedávných Rozhodnutí a Vlivu",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F)
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (gameState.influenceLog.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF14081E),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Zatím jsi neuskutečnil žádná klíčová příběhová rozhodnutí. Stiskni tlačítko výše pro spuštění prvního střetnutí!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    gameState.influenceLog.takeLast(6).reversed().forEach { log ->
                        ChronicleDecisionCard(log = log)
                    }
                }
            }
        }

        // --- 3. REPLAYABILITY STATS ---
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160B20)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "⭐ Herní Zkušenost & Dovednosti",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Dovednosti jako Svádění (${player.skills["svadeni"] ?: 0}), Velení (${player.skills["veleni"] ?: 0}), Temnota (${player.skills["temnota"] ?: 0}) a Vyjednávání (${player.skills["vyjednavani"] ?: 0}) výrazně zvyšují šanci na úspěch při rizikových volbách.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChronicleDecisionCard(log: com.example.haremdark.models.InfluenceLogEntry) {
    val isPositive = log.influenceChange >= 0

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF190C22),
        border = BorderStroke(0.8.dp, if (isPositive) Color(0xFF4CAF50).copy(alpha = 0.35f) else Color(0xFFF44336).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Den ${log.day} • ${log.characterName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFFF80AB)
                )
                Text(
                    text = "Volba: ${log.choiceDescription}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Důsledek: ${log.reason}",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isPositive) Color(0xFF1B5E20).copy(alpha = 0.8f) else Color(0xFFB71C1C).copy(alpha = 0.8f)
            ) {
                Text(
                    text = if (isPositive) "+${log.influenceChange} Vliv" else "${log.influenceChange} Vliv",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
