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
/**
 * A sleek, semi-transparent overlay for combat logs.
 * Can be minimized or expanded to show turn-by-turn actions.
 */
@Composable
fun CombatLogOverlay(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to latest
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = Color(0xCC0D0612), // Semi-transparent dark
            border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.3f)),
            modifier = Modifier.animateContentSize()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Header / Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ArrowDownward else Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isExpanded) "Bojový Protokol" else "Poslední akce: ${logs.firstOrNull()?.message ?: "Zahajování..."}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    
                    if (isExpanded) {
                        Badge(containerColor = Color(0xFFD32F2F)) {
                            Text("${logs.size}", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(logs) { entry ->
                            CombatLogItemOverlay(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CombatLogItemOverlay(entry: CombatLogEntry) {
    val isCritical = entry.message.contains("KRIT", ignoreCase = true)
    val isStatus = entry.type == "system" || entry.message.contains("⚡") || entry.message.contains("STAV")
    
    val accentColor = when (entry.type) {
        "player_attack", "player_spell" -> Color(0xFFFFCC80) // Orange
        "enemy_attack", "enemy_special" -> Color(0xFFFF8A80) // Red
        "player_heal" -> Color(0xFFA5D6A7) // Green
        "player_support" -> Color(0xFF80D8FF) // Blue
        "victory" -> Color(0xFFFFD700) // Gold
        "defeat" -> Color(0xFFEF5350) // Dark Red
        "system" -> Color(0xFFB0BEC5) // Gray
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Turn Badge
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = accentColor.copy(alpha = 0.15f),
            border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.4f))
        ) {
            Text(
                text = "T${entry.turn}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = if (isCritical) FontWeight.ExtraBold else FontWeight.Normal
                )
                
                if (entry.damageDealt > 0) {
                    Text(
                        text = "-${entry.damageDealt} HP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (entry.type.startsWith("player")) Color(0xFFFF5252) else Color(0xFFFF8A80)
                    )
                }
            }
            
            if (!entry.narrativeText.isNullOrBlank()) {
                Text(
                    text = entry.narrativeText,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
