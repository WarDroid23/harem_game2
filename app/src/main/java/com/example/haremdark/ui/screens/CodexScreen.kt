package com.example.haremdark.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.CodexData
import com.example.haremdark.data.CodexEntry
import com.example.haremdark.models.Player
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.NavSound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodexScreen(
    player: Player,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Lore") }
    val categories = listOf("Lore", "Živly", "Bestiář", "Postavy", "Události", "Předměty")
    
    val unlockedIds = player.unlockedCodexIds
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Kodex Dominia", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E)
                )
            )
        },
        containerColor = Color(0xFF0F0F1E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Selector
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color(0xFFBB86FC),
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                        color = Color(0xFFBB86FC)
                    )
                }
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { 
                            selectedCategory = category 
                            SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                        },
                        text = { 
                            Text(
                                category, 
                                fontSize = 14.sp,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }

            // Entries List
            AnimatedContent(
                targetState = selectedCategory,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "CodexCategoryTransition",
                modifier = Modifier.weight(1f)
            ) { targetCategory ->
                val filteredEntries = CodexData.ENTRIES.filter { it.category == targetCategory }
                
                if (filteredEntries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.MenuBook, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "V této kategorii zatím nejsou žádné záznamy.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredEntries) { entry ->
                            CodexLoreEntryCard(
                                entry = entry,
                                isUnlocked = unlockedIds.contains(entry.id)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodexLoreEntryCard(
    entry: CodexEntry,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF252545) else Color(0xFF1A1A2E).copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp, 
            if (isUnlocked) Color(0xFFBB86FC).copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUnlocked) Color(0xFFBB86FC).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isUnlocked) entry.icon else "❓",
                            fontSize = 24.sp,
                            modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isUnlocked) entry.title else "Neznámý záznam",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color.White else Color.Gray
                    )
                    if (isUnlocked) {
                        Text(
                            text = entry.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBB86FC).copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "Podmínka: ${entry.unlockCondition}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }

                if (isUnlocked) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = "Unlocked",
                        tint = Color(0xFF03DAC6),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Lock, 
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
