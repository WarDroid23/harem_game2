package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.CombatLogEntry
import kotlinx.coroutines.launch

/**
 * A highly interactive, styled scrollable combat log component that parses battle history.
 * Custom-styles critical hits, status effect triggers, heals, system updates, and element weaknesses.
 */
@Composable
fun ScrollableCombatLog(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier,
    maxHeightDp: Int = 180,
    onLogClick: ((CombatLogEntry) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Automatically scroll to the latest combat action when a new log arrives
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13091B)),
        border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Title, total count & Auto-scroll indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📜", fontSize = 14.sp)
                    Text(
                        text = "Bojový Protokol Střetnutí",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Badge(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ) {
                        Text("${logs.size}", fontSize = 9.sp)
                    }
                }

                // Scroll to Bottom Quick Button
                if (listState.firstVisibleItemIndex > 0) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Posunout dolů",
                            tint = Color(0xFFFF80AB),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeightDp.dp)
                        .background(Color(0xFF0C0512), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Boje nebyly dosud zahájeny.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeightDp.dp)
                        .background(Color(0xFF0C0512), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs) { entry ->
                        CombatLogItem(
                            entry = entry,
                            onClick = { onLogClick?.invoke(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CombatLogItem(
    entry: CombatLogEntry,
    onClick: () -> Unit
) {
    // Style parsing based on the message content and log type
    val isCritical = entry.message.contains("KRIT", ignoreCase = true) || entry.type == "player_special" || entry.type == "enemy_special"
    val isStatusTrigger = entry.message.contains("Omráčen", ignoreCase = true) || 
                          entry.message.contains("Hoření", ignoreCase = true) ||
                          entry.message.contains("Otráven", ignoreCase = true) || 
                          entry.message.contains("Krvácení", ignoreCase = true)

    val itemBgColor = when {
        entry.type == "victory" -> Color(0x334CAF50)
        entry.type == "defeat" -> Color(0x33F44336)
        isCritical -> Color(0x44D32F2F)
        isStatusTrigger -> Color(0x2200E5FF)
        else -> Color.Transparent
    }

    val itemBorderColor = when {
        entry.type == "victory" -> Color(0xFF4CAF50).copy(alpha = 0.5f)
        entry.type == "defeat" -> Color(0xFFF44336).copy(alpha = 0.5f)
        isCritical -> Color(0xFFFF5252).copy(alpha = 0.6f)
        isStatusTrigger -> Color(0xFF00E5FF).copy(alpha = 0.4f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val textColor = when (entry.type) {
        "player_attack", "player_spell" -> Color(0xFFFFCC80) // Light orange for player offenses
        "player_heal" -> Color(0xFFA5D6A7)                 // Soft green for healing
        "player_support" -> Color(0xFF80D8FF)              // Soft blue for buffs
        "enemy_attack", "enemy_special" -> Color(0xFFFF8A80) // Soft red for enemy attacks
        "victory" -> Color(0xFFFFD700)                     // Gold for victory
        "defeat" -> Color(0xFFEF5350)                      // Red for defeat
        "system" -> Color(0xFFCFD8DC)                      // Cool gray for environment hazards
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(itemBgColor)
            .border(BorderStroke(1.dp, itemBorderColor), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Header Row: Turn tracker, Actor badge & critical badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF311B92).copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "KOLO ${entry.turn}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE040FB),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                        )
                    }

                    if (entry.actor.isNotBlank()) {
                        Text(
                            text = entry.actor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entry.type.startsWith("player")) Color(0xFFCE93D8) else Color(0xFFFF8A80),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isCritical) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFD32F2F)
                        ) {
                            Text(
                                text = "💥 KRITICKÝ ÚDER",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    if (isStatusTrigger) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00B0FF)
                        ) {
                            Text(
                                text = "⚡ STAVOVÝ EFEKT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }
            }

            // Message text
            Text(
                text = entry.message,
                color = textColor,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = if (isCritical) FontWeight.Bold else FontWeight.Normal
            )

            // Optional detailed math calculation / formula line
            if (!entry.damageCalculation.isNullOrBlank()) {
                Text(
                    text = "⚙️ Výpočet: " + entry.damageCalculation,
                    color = Color.LightGray.copy(alpha = 0.6f),
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Optional Narrative Text
            if (!entry.narrativeText.isNullOrBlank()) {
                Text(
                    text = "💬 \"${entry.narrativeText}\"",
                    color = Color(0xFFCE93D8).copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
