package com.example.haremdark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.CodexData
import com.example.haremdark.data.CodexEntry
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun CodexTab(gameState: GameSave, engine: GameEngine) {
    var selectedCategory by remember { mutableStateOf("Vše") }
    val categories = listOf("Vše", "Lore", "Bestiář", "Postavy")
    
    // Check and unlock new entries automatically
    LaunchedEffect(gameState) {
        val newUnlocks = CodexData.checkUnlocks(gameState)
        if (newUnlocks.size > gameState.player.unlockedCodexIds.size) {
            engine.updateState { it.copy(player = it.player.copy(unlockedCodexIds = newUnlocks)) }
        }
    }
    
    val allEntries = CodexData.ENTRIES
    val unlockedIds = gameState.player.unlockedCodexIds
    
    val filteredEntries = if (selectedCategory == "Vše") {
        allEntries
    } else {
        allEntries.filter { it.category == selectedCategory }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Category filters
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }
        
        // Progress stats
        val unlockedCount = allEntries.count { unlockedIds.contains(it.id) }
        Text(
            text = "Odemčeno: $unlockedCount / ${allEntries.size}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(filteredEntries) { entry ->
                val isUnlocked = unlockedIds.contains(entry.id)
                CodexEntryCard(entry, isUnlocked)
            }
        }
    }
}

@Composable
fun CodexEntryCard(entry: CodexEntry, isUnlocked: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(entry.icon, fontSize = 24.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Uzamčeno",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Titles
                Column(modifier = Modifier.weight(1f)) {
                    if (isUnlocked) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = entry.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Neznámý záznam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Podmínka: ${entry.unlockCondition}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Category badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = entry.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Expanded Content
            if (isUnlocked && expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = entry.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}
