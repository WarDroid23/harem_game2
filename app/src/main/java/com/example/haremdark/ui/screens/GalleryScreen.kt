package com.example.haremdark.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.GalleryArchetypeEntry
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.GameSave

@Composable
fun GalleryScreen(
    gameState: GameSave,
    onNavigateToHarem: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Všechny", "Odemčené", "Zamčené")
    var selectedEntry by remember { mutableStateOf<GalleryArchetypeEntry?>(null) }

    val recruitedArchetypeIds = remember(gameState.concubines) {
        gameState.concubines.map { it.archetypeId }.toSet()
    }

    val totalArchetypes = StaticData.GALLERY_ENTRIES.size
    val unlockedCount = StaticData.GALLERY_ENTRIES.count { recruitedArchetypeIds.contains(it.archetypeId) }
    val progressPercent = if (totalArchetypes > 0) (unlockedCount * 100 / totalArchetypes) else 0

    val displayedEntries = remember(selectedFilter, recruitedArchetypeIds) {
        when (selectedFilter) {
            1 -> StaticData.GALLERY_ENTRIES.filter { recruitedArchetypeIds.contains(it.archetypeId) }
            2 -> StaticData.GALLERY_ENTRIES.filter { !recruitedArchetypeIds.contains(it.archetypeId) }
            else -> StaticData.GALLERY_ENTRIES
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Gallery Progress Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Galerie postav a archetypů",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sbírka všech dívek a bytostí, které lze v temném světě získat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "$unlockedCount / $totalArchetypes ($progressPercent%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (unlockedCount.toFloat() / totalArchetypes.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEachIndexed { index, title ->
                val isSelected = selectedFilter == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = index },
                    label = {
                        val count = when (index) {
                            1 -> unlockedCount
                            2 -> totalArchetypes - unlockedCount
                            else -> totalArchetypes
                        }
                        Text("$title ($count)", fontSize = 12.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // 2-Column Grid of Character Archetypes
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedEntries) { entry ->
                val isUnlocked = recruitedArchetypeIds.contains(entry.archetypeId)
                val recruitedGirls = gameState.concubines.filter { it.archetypeId == entry.archetypeId }

                GalleryCard(
                    entry = entry,
                    isUnlocked = isUnlocked,
                    recruitedCount = recruitedGirls.size,
                    highestPhase = recruitedGirls.maxOfOrNull { it.fazeZkazenosti } ?: 0,
                    onClick = { selectedEntry = entry }
                )
            }
        }
    }

    // Detail Lore & Dossier Modal
    selectedEntry?.let { entry ->
        val isUnlocked = recruitedArchetypeIds.contains(entry.archetypeId)
        val recruitedGirls = gameState.concubines.filter { it.archetypeId == entry.archetypeId }

        GalleryDetailDialog(
            entry = entry,
            isUnlocked = isUnlocked,
            recruitedGirls = recruitedGirls,
            onDismiss = { selectedEntry = null },
            onGoToHarem = {
                selectedEntry = null
                onNavigateToHarem()
            }
        )
    }
}

@Composable
fun GalleryCard(
    entry: GalleryArchetypeEntry,
    isUnlocked: Boolean,
    recruitedCount: Int,
    highestPhase: Int,
    onClick: () -> Unit
) {
    val accentColor = Color(entry.accentColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .then(
                if (isUnlocked) Modifier.border(1.5.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                else Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF140D1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isUnlocked) {
                // Character Portrait Image
                Image(
                    painter = painterResource(id = entry.drawableRes),
                    contentDescription = entry.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x88000000),
                                    Color(0xEE12081C)
                                ),
                                startY = 60f
                            )
                        )
                )

                // Top Badge: Unlocked status & count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "✓ Odemčeno",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (recruitedCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xDD000000)
                        ) {
                            Text(
                                text = "★ $recruitedCount v harému",
                                color = Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Bottom Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E0E0),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x99000000)
                        ) {
                            Text(
                                text = "Fáze: $highestPhase/15",
                                color = Color(0xFFCE93D8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x99000000)
                        ) {
                            Text(
                                text = entry.difficulty,
                                color = Color(0xFFFFB74D),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            } else {
                // Locked Silhouette Placeholder Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF231633))
                            .border(1.dp, Color(0x44FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Zamčeno",
                            tint = Color(0xFF9575CD),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "???",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB39DDB)
                    )

                    Text(
                        text = "Neznámá dívka",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2A1B3D)
                    ) {
                        Text(
                            text = "🔒 Klikni pro nápovědu",
                            color = Color(0xFFCE93D8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryDetailDialog(
    entry: GalleryArchetypeEntry,
    isUnlocked: Boolean,
    recruitedGirls: List<com.example.haremdark.models.Concubine>,
    onDismiss: () -> Unit,
    onGoToHarem: () -> Unit
) {
    val accentColor = Color(entry.accentColor)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Image Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    if (isUnlocked) {
                        Image(
                            painter = painterResource(id = entry.drawableRes),
                            contentDescription = entry.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xBB000000),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF2A1538), Color(0xFF13091B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Zamčeno",
                                    tint = Color(0xFFAB47BC),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Neznámý archetyp", color = Color(0xFFE1BEE7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnlocked) accentColor else Color(0xFF616161),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isUnlocked) "✓ ODEMČENO V GALERII" else "🔒 ZATÍM NEZÍSKÁNO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main Info Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    // Quote
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💬", fontSize = 18.sp)
                            Text(
                                text = entry.quote,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lore & Description
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📜 Příběh a povaha",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = entry.loreDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Recruitment Clue & Perk
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🗺️ Kde dívku najít / získat:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFFFB74D)
                            )
                            Text(
                                text = entry.recruitmentHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                            Text(
                                text = "⚡ Speciální perky & bonusy:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF69F0AE)
                            )
                            Text(
                                text = entry.perk,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Favorite Gifts
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎁 Oblíbené dary pro tento archetyp:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFFF80AB)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                entry.favoriteGifts.forEach { gift ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFF4081).copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = gift,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF4081),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recruited Maiden Instances in Harem
                    if (recruitedGirls.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "👑 Dívky tohoto archetypu ve tvém harému (${recruitedGirls.size}):",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFFFD700)
                                )
                                recruitedGirls.forEach { girl ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${girl.name} (Věk ${girl.age})",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Fáze ${girl.fazeZkazenosti} • Loajalita ${girl.loajalita}%",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onGoToHarem,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Přejít do Harému k dívkám", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
