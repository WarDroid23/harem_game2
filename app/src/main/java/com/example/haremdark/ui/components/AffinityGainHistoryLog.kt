package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.models.AffinityPointRecord
import com.example.haremdark.models.Character
import com.example.haremdark.models.getSafeAffinityTrend

enum class AffinityGainFilter(val label: String) {
    ALL("Všechny"),
    SIGNIFICANT("Skoky ≥ 10"),
    RECENT("Poslední dny")
}

/**
 * Visual log component tracking and displaying the history of affinity gains
 * for a character, showing date, source, and exact amount increased.
 */
@Composable
fun AffinityGainHistoryLog(
    character: Character,
    currentDay: Int = 1,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf(AffinityGainFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortDescending by remember { mutableStateOf(true) }
    var selectedRecordIndex by remember { mutableIntStateOf(-1) }

    val rawRecords = remember(character.affinityPoints, character.affinityHistory.size, currentDay) {
        character.getSafeAffinityTrend(currentDay)
    }

    // Compute delta for each record
    val recordsWithDeltas = remember(rawRecords) {
        rawRecords.mapIndexed { index, record ->
            val prevPoints = if (index > 0) rawRecords[index - 1].points else 0
            val computedGain = if (record.amountIncreased > 0) {
                record.amountIncreased
            } else {
                (record.points - prevPoints).coerceAtLeast(0)
            }
            AffinityLogItemData(
                record = record,
                gain = computedGain,
                cumulativePoints = record.points,
                milestoneReached = AffinityData.AFFINITY_MILESTONES.firstOrNull { ms ->
                    record.points >= ms && prevPoints < ms
                }
            )
        }
    }

    // Filter and Sort
    val displayedRecords = remember(recordsWithDeltas, filterType, searchQuery, sortDescending, currentDay) {
        var list = recordsWithDeltas

        list = when (filterType) {
            AffinityGainFilter.ALL -> list
            AffinityGainFilter.SIGNIFICANT -> list.filter { it.gain >= 10 }
            AffinityGainFilter.RECENT -> list.filter { it.record.day >= (currentDay - 7).coerceAtLeast(1) }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.record.source.contains(searchQuery, ignoreCase = true) ||
                it.record.displayDate.contains(searchQuery, ignoreCase = true)
            }
        }

        if (sortDescending) list.reversed() else list
    }

    val totalGained = remember(recordsWithDeltas) {
        recordsWithDeltas.sumOf { it.gain }
    }
    val avgGain = remember(recordsWithDeltas) {
        if (recordsWithDeltas.isNotEmpty()) totalGained.toFloat() / recordsWithDeltas.size else 0f
    }
    val maxSingleGain = remember(recordsWithDeltas) {
        recordsWithDeltas.maxOfOrNull { it.gain } ?: 0
    }

    val currentTier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }
    val tierColor = Color(currentTier.colorHex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("affinity_gain_history_log"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- SUMMARY STATS CARD ---
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
            border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📜", fontSize = 18.sp)
                        Text(
                            text = "Protokol Přírůstků Pouta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tierColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, tierColor)
                    ) {
                        Text(
                            text = "${recordsWithDeltas.size} záznamů",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // 3 KPI columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogKpiItem(
                        title = "Celkem získáno",
                        value = "+$totalGained pts",
                        color = Color(0xFF81C784),
                        modifier = Modifier.weight(1f)
                    )
                    LogKpiItem(
                        title = "Průměrný růst",
                        value = "+${"%.1f".format(avgGain)} pts",
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.weight(1f)
                    )
                    LogKpiItem(
                        title = "Největší skok",
                        value = "+$maxSingleGain pts",
                        color = Color(0xFFFF80AB),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- FILTER & SEARCH TOOLBAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter chips
            AffinityGainFilter.entries.forEach { f ->
                val isSelected = filterType == f
                FilterChip(
                    selected = isSelected,
                    onClick = { filterType = f },
                    label = { Text(f.label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tierColor.copy(alpha = 0.3f),
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) tierColor else Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sort direction button
            IconButton(
                onClick = { sortDescending = !sortDescending },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = if (sortDescending) "Nejnovější nahoře" else "Nejstarší nahoře",
                    tint = tierColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Hledat v historii darů a rozhovorů...", fontSize = 11.sp, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Vymazat", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                focusedBorderColor = tierColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            )
        )

        // --- TIMELINE LIST OF AFFINITY GAINS ---
        if (displayedRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Žádné záznamy neodpovídají zadanému filtru.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(displayedRecords) { idx, item ->
                    val isSelected = selectedRecordIndex == idx
                    AffinityGainTimelineItem(
                        item = item,
                        character = character,
                        isSelected = isSelected,
                        tierColor = tierColor,
                        onClick = {
                            selectedRecordIndex = if (isSelected) -1 else idx
                            HapticManager.vibrateDialogueCycle()
                            SoundEffectManager.playFlavorDialogueChime(item.record.points / 60 + 1)
                        }
                    )
                }
            }
        }
    }
}

data class AffinityLogItemData(
    val record: AffinityPointRecord,
    val gain: Int,
    val cumulativePoints: Int,
    val milestoneReached: Int?
)

@Composable
private fun LogKpiItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AffinityGainTimelineItem(
    item: AffinityLogItemData,
    character: Character,
    isSelected: Boolean,
    tierColor: Color,
    onClick: () -> Unit
) {
    val gainColor = when {
        item.gain >= 15 -> Color(0xFFFFD700) // Gold for major surges
        item.gain >= 8 -> Color(0xFF81C784)  // Green for good gains
        else -> Color(0xFF64B5F6)            // Blue for modest gains
    }

    val sourceIcon = when {
        item.record.source.contains("dar", ignoreCase = true) || item.record.source.contains("gift", ignoreCase = true) -> "🎁"
        item.record.source.contains("přízeň", ignoreCase = true) -> "👑"
        item.record.source.contains("rozhovor", ignoreCase = true) || item.record.source.contains("pozdrav", ignoreCase = true) -> "💬"
        item.record.source.contains("příběh", ignoreCase = true) || item.record.source.contains("volba", ignoreCase = true) -> "📖"
        item.record.source.contains("boj", ignoreCase = true) || item.record.source.contains("mise", ignoreCase = true) -> "⚔️"
        else -> "✨"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) tierColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.5.dp,
            if (isSelected) tierColor else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("affinity_gain_item_${item.record.day}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date & Source Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Date Chip
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2A1F3D)
                    ) {
                        Text(
                            text = item.record.displayDate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCE93D8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Text(sourceIcon, fontSize = 13.sp)

                    Text(
                        text = item.record.source,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Amount Increased Badge (+XX pts)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = gainColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, gainColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "+${item.gain}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = gainColor
                        )
                        Text(
                            text = "pts",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            color = gainColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Subline: Cumulative Points & Milestone Reached
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Celková úroveň: ${item.cumulativePoints} pts (Úr. ${AffinityData.getLevelForPoints(item.cumulativePoints)})",
                    fontSize = 9.sp,
                    color = Color.Gray
                )

                if (item.milestoneReached != null) {
                    val badge = AffinityData.getMilestoneBadgeInfo(item.milestoneReached)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.25f),
                        border = BorderStroke(0.5.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "🏆 ${badge.first}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Expanded detail: Unlocked quote or story memory on tap
            AnimatedVisibility(visible = isSelected) {
                val tierAtPoint = AffinityData.getTierForPoints(item.cumulativePoints)
                val quote = remember(item.cumulativePoints, character.archetypeId) {
                    val lines = AffinityData.getDialoguesForTier(character.archetypeId, tierAtPoint.level)
                    lines.firstOrNull() ?: "„Má náklonnost k tobě jenom roste, můj pane.“"
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    border = BorderStroke(0.5.dp, tierColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💬", fontSize = 14.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${character.name} (${tierAtPoint.title}):",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                            Text(
                                text = "„$quote“",
                                fontSize = 10.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(
                            onClick = { VoiceManager.speak(quote, character.archetypeId) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Přehrát hlas",
                                tint = tierColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
