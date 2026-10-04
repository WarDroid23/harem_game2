package com.example.haremdark.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.NavSound
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.CharacterStatProgressionVicoChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterStatsSummaryScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null
) {
    var selectedCharacterId by remember(gameState.characters) {
        mutableStateOf(gameState.characters.firstOrNull()?.id ?: "")
    }

    val selectedChar = gameState.characters.find { it.id == selectedCharacterId } ?: gameState.characters.firstOrNull()
    val currentDay = gameState.player.day.coerceAtLeast(1)

    // Calculate overall empire / harem progression metrics safely
    val totalPower = gameState.characters.sumOf { char -> (char.statHistory.lastOrNull()?.powerLevel ?: (char.strength * 10)).toLong() }
    val avgAttack = if (gameState.characters.isNotEmpty()) {
        gameState.characters.map { char -> char.skills["combat"] ?: 5 }.average().toInt() * 10
    } else 0
    val avgDefense = if (gameState.characters.isNotEmpty()) {
        gameState.characters.map { char -> char.skills["defense"] ?: 3 }.average().toInt() * 10
    } else 0
    val maxAffinity = gameState.characters.maxOfOrNull { char -> char.affinityPoints } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "📊 Souhrn Statistik & Vývoje",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Vico grafy progrese atributů v čase pro Pána a Harém",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF80AB)
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                onBack()
                            },
                            modifier = Modifier.testTag("btn_back_stats_summary")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zpět",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF14081E)
                )
            )
        },
        containerColor = Color(0xFF0D0414),
        modifier = Modifier.testTag("character_stats_summary_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Overview Summary Cards Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E102E),
                    border = BorderStroke(1.5.dp, Color(0xFF9C27B0).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📈", fontSize = 22.sp)
                                Text(
                                    text = "Imperium Analytics",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "Den $currentDay",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // 4 Metric Chips Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatSummaryMetricBox(
                                title = "Celková Síla",
                                value = "$totalPower PWR",
                                icon = "⚡",
                                color = Color(0xFFFFCA28),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryMetricBox(
                                title = "Prům. Útok",
                                value = "$avgAttack DMG",
                                icon = "⚔️",
                                color = Color(0xFFFF7043),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryMetricBox(
                                title = "Prům. Obrana",
                                value = "$avgDefense DEF",
                                icon = "🛡️",
                                color = Color(0xFF26C6DA),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryMetricBox(
                                title = "Max Afinita",
                                value = "$maxAffinity PTS",
                                icon = "💖",
                                color = Color(0xFFE040FB),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Character Selector Carousel
            if (gameState.characters.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "👥 Vyber družku pro detailní Vico graf vývoje:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF80AB)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            gameState.characters.forEach { char ->
                                val isSelected = (char.id == selectedCharacterId)
                                val emoji = when (char.archetypeId) {
                                    "sukuba", "krvava_subka" -> "😈"
                                    "chladna" -> "❄️"
                                    "draci_divka" -> "🔥"
                                    "ticha_panenka" -> "🎭"
                                    else -> "💖"
                                }
                                val pwr = char.statHistory.lastOrNull()?.powerLevel ?: (char.strength * 10)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) Color(0xFF38101C) else Color(0xFF180A22),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFF1744) else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            HapticManager.vibrateClick()
                                            selectedCharacterId = char.id
                                        }
                                    ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                        Column {
                                            Text(
                                                text = char.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFFFF8A80) else Color.White
                                            )
                                            Text(
                                                text = "Úroveň ${char.level} • $pwr PWR",
                                                fontSize = 10.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Vico Charts Progression Component for Selected Character
            if (selectedChar != null) {
                item {
                    CharacterStatProgressionVicoChart(
                        character = selectedChar,
                        currentDay = currentDay,
                        isFullTab = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E102E),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 32.sp)
                            Text(
                                text = "Žádné družky v harému",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Pro zobrazení Vico grafů progrese přidejte družky do svého harému.",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatSummaryMetricBox(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14081E),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color.LightGray,
                maxLines = 1
            )
        }
    }
}
