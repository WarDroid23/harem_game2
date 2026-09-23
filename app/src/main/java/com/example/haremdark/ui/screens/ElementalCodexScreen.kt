package com.example.haremdark.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.ElementalCodexData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.domain.NavSound
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalCodexEntry
import com.example.haremdark.models.GameSave

enum class ElementalCodexFilter {
    ALL,
    DISCOVERED,
    LOCKED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementalCodexScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    onNavigateToTraining: ((Element) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ElementalCodexFilter.ALL) }
    var expandedElement by remember { mutableStateOf<Element?>(null) }
    var detailDialogEntry by remember { mutableStateOf<ElementalCodexEntry?>(null) }

    val allEntries = remember { ElementalCodexData.ENTRIES }
    val discoveredElements = remember(gameState) {
        ElementalCodexData.getDiscoveredElements(gameState)
    }
    val discoveredCount = discoveredElements.size
    val totalCount = allEntries.size

    // Calculate total multiplier sum and training sessions
    val totalTrainingSessions = remember(gameState) {
        gameState.characters.sumOf { it.totalTrainingSessions }
    }
    val totalMultiplierSum = remember(gameState) {
        gameState.characters.sumOf { char ->
            char.elementalMultipliers.values.sum().toDouble()
        }.toFloat()
    }

    // Filtered entries
    val filteredEntries = remember(searchQuery, selectedFilter, discoveredElements) {
        allEntries.filter { entry ->
            val isDiscovered = discoveredElements.contains(entry.element)
            val matchesFilter = when (selectedFilter) {
                ElementalCodexFilter.ALL -> true
                ElementalCodexFilter.DISCOVERED -> isDiscovered
                ElementalCodexFilter.LOCKED -> !isDiscovered
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val query = searchQuery.trim().lowercase()
                entry.name.lowercase().contains(query) ||
                        entry.epithet.lowercase().contains(query) ||
                        entry.ancientArchon.lowercase().contains(query) ||
                        entry.statusEffectName.lowercase().contains(query) ||
                        (isDiscovered && entry.historicalOrigin.lowercase().contains(query))
            }
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Elementární Kodex",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pradávné lore a historie objevených živlů",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = {
                                SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                onBack()
                            },
                            modifier = Modifier.testTag("elemental_codex_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zpět",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .size(24.dp)
                        )
                    }
                },
                actions = {
                    // Pill showing discovered count
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$discoveredCount / $totalCount",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Lore & Progress Overview Banner
            item {
                ElementalCodexProgressBanner(
                    discoveredCount = discoveredCount,
                    totalCount = totalCount,
                    totalTrainingSessions = totalTrainingSessions,
                    totalMultiplierSum = totalMultiplierSum
                )
            }

            // 2. Search & Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("elemental_codex_search_field"),
                        placeholder = {
                            Text("Hledat živel, patrona, efekt...", fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Vymazat", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    // Filter chips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == ElementalCodexFilter.ALL,
                            onClick = {
                                selectedFilter = ElementalCodexFilter.ALL
                                SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                            },
                            label = { Text("Všechny (${allEntries.size})", fontSize = 11.sp) },
                            modifier = Modifier.testTag("elemental_codex_filter_all")
                        )
                        FilterChip(
                            selected = selectedFilter == ElementalCodexFilter.DISCOVERED,
                            onClick = {
                                selectedFilter = ElementalCodexFilter.DISCOVERED
                                SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                            },
                            label = { Text("Probuzené ($discoveredCount)", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.testTag("elemental_codex_filter_discovered")
                        )
                        FilterChip(
                            selected = selectedFilter == ElementalCodexFilter.LOCKED,
                            onClick = {
                                selectedFilter = ElementalCodexFilter.LOCKED
                                SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                            },
                            label = { Text("Zapečetěné (${totalCount - discoveredCount})", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.testTag("elemental_codex_filter_locked")
                        )
                    }
                }
            }

            // 3. Empty state if search finds nothing
            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Nenalezeny žádné záznamy živlů",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zkus změnit hledaný výraz nebo zvol jiný filtr.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. List of Elemental Codex Entries
            items(filteredEntries, key = { it.id }) { entry ->
                val isDiscovered = discoveredElements.contains(entry.element)
                val isExpanded = expandedElement == entry.element
                val trainedCharacters = remember(gameState, entry.element) {
                    ElementalCodexData.getTrainedCharactersForElement(gameState, entry.element)
                }

                ElementalCodexCard(
                    entry = entry,
                    isDiscovered = isDiscovered,
                    isExpanded = isExpanded,
                    trainedCharacters = trainedCharacters,
                    onToggleExpand = {
                        expandedElement = if (isExpanded) null else entry.element
                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                    },
                    onOpenDetail = {
                        detailDialogEntry = entry
                        SoundEffectManager.playNavigation(NavSound.CODEX_OPEN)
                    },
                    onNavigateToTraining = {
                        onNavigateToTraining?.invoke(entry.element)
                    }
                )
            }
        }
    }

    // Detail Reading Dialog
    detailDialogEntry?.let { entry ->
        ElementalLoreDetailDialog(
            entry = entry,
            isDiscovered = discoveredElements.contains(entry.element),
            trainedCharacters = ElementalCodexData.getTrainedCharactersForElement(gameState, entry.element),
            onDismiss = { detailDialogEntry = null },
            onTrain = {
                detailDialogEntry = null
                onNavigateToTraining?.invoke(entry.element)
            }
        )
    }
}

/**
 * Progress & overview banner at the top of the Elemental Codex.
 */
@Composable
private fun ElementalCodexProgressBanner(
    discoveredCount: Int,
    totalCount: Int,
    totalTrainingSessions: Int,
    totalMultiplierSum: Float
) {
    val progress = (discoveredCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "CodexProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    Color(0xFFE040FB).copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        Color(0xFFE040FB).copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📜", fontSize = 20.sp)
                    }

                    Column {
                        Text(
                            text = "Knihovna Prvotních Sil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Probuzeno $discoveredCount z $totalCount živlů světa",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Text(
                text = "Historické záznamy odhalují pravou podstatu a mýty každého elementu, jakmile jej poprvé probudíš v komnatách afinity při sparringu s dívkami.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            // Quick stats row
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatPill(
                    icon = "⚔️",
                    label = "Tréninky",
                    value = "$totalTrainingSessions"
                )
                StatPill(
                    icon = "⚡",
                    label = "Afinitní síla",
                    value = "${"%.1f".format(totalMultiplierSum)}x"
                )
                StatPill(
                    icon = "✨",
                    label = "Aury v Kodexu",
                    value = if (discoveredCount >= totalCount) "Kompletní" else "$discoveredCount / $totalCount"
                )
            }
        }
    }
}

@Composable
private fun StatPill(icon: String, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Individual entry card in the Elemental Codex list.
 */
@Composable
private fun ElementalCodexCard(
    entry: ElementalCodexEntry,
    isDiscovered: Boolean,
    isExpanded: Boolean,
    trainedCharacters: List<Pair<Character, Float>>,
    onToggleExpand: () -> Unit,
    onOpenDetail: () -> Unit,
    onNavigateToTraining: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("elemental_card_${entry.element.name}")
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = BorderStroke(
            width = if (isDiscovered) 1.5.dp else 1.dp,
            brush = if (isDiscovered) {
                Brush.linearGradient(
                    listOf(
                        entry.primaryColor.copy(alpha = 0.7f),
                        entry.secondaryColor.copy(alpha = 0.4f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.Gray.copy(alpha = 0.25f),
                        Color.Gray.copy(alpha = 0.1f)
                    )
                )
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Element Icon Box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isDiscovered) {
                                Brush.linearGradient(
                                    listOf(
                                        entry.primaryColor.copy(alpha = 0.25f),
                                        entry.secondaryColor.copy(alpha = 0.15f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        Color.Gray.copy(alpha = 0.15f),
                                        Color.Gray.copy(alpha = 0.05f)
                                    )
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (isDiscovered) entry.primaryColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDiscovered) {
                        Text(entry.icon, fontSize = 26.sp)
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Zapečetěno",
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isDiscovered) entry.name else "Zapečetěný živel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDiscovered) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )

                        if (isDiscovered) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = entry.primaryColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PROBUZENO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = entry.primaryColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isDiscovered) entry.epithet else "Dosud neobjeveno v komnatách afinity",
                        fontSize = 12.sp,
                        color = if (isDiscovered) entry.primaryColor else Color.Gray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isDiscovered) {
                        Text(
                            text = "Patron: ${entry.ancientArchon}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Expand / Info indicator
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Zobrazit více",
                        tint = if (isDiscovered) entry.primaryColor else Color.Gray
                    )
                }
            }

            // Quick Tags (When discovered)
            if (isDiscovered) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = entry.primaryColor.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(entry.statusEffectName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, entry.secondaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🏆 ${entry.sacredRelic}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Locked state teaser & unlock instructions
            if (!isDiscovered) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Podmínka probuzení v Kodexu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFFFB74D)
                            )
                        }

                        Text(
                            text = "Tento živel dosud dříme v zapomnění. Jeho historický původ, patron a bojová tajemství se zapíší do Kodexu po dokončení prvního tréninku s libovolnou dívkou.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 15.sp
                        )

                        Button(
                            onClick = onNavigateToTraining,
                            colors = ButtonDefaults.buttonColors(containerColor = entry.primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("train_element_${entry.element.name}")
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Trénovat tento živel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Expanded Full Lore Content (When Discovered)
            AnimatedVisibility(
                visible = isDiscovered && isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // 1. Historický původ
                    LoreSection(
                        title = "📜 Pradávný historický původ",
                        accentColor = entry.primaryColor,
                        content = entry.historicalOrigin
                    )

                    // 2. Zasvěcení v tréninku
                    LoreSection(
                        title = "🔥 Zasvěcení v komnatách afinity",
                        accentColor = entry.secondaryColor,
                        content = entry.trainingLore
                    )

                    // 3. Taktická povaha & boj
                    LoreSection(
                        title = "⚔️ Taktická povaha a rezonance",
                        accentColor = entry.primaryColor,
                        content = entry.tacticalNature
                    )

                    // 4. Resonance Blessing
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = entry.primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, entry.primaryColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "Elementární Rezonance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = entry.primaryColor
                                )
                                Text(
                                    text = entry.resonanceBlessing,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 5. Harem Girls with this element trained
                    if (trainedCharacters.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "👑 Osvojeno v harému (${trainedCharacters.size}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(trainedCharacters) { (char, multiplier) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, entry.primaryColor.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Text(getCharacterEmoji(char), fontSize = 14.sp)
                                            Text(
                                                char.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = entry.primaryColor.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    "${"%.2f".format(multiplier)}x",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = entry.primaryColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onOpenDetail) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Číst celou kroniku", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onNavigateToTraining,
                            colors = ButtonDefaults.buttonColors(containerColor = entry.primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Trénovat živel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean reusable lore section container.
 */
@Composable
private fun LoreSection(
    title: String,
    accentColor: Color,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = content,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            lineHeight = 17.sp,
            textAlign = TextAlign.Justify
        )
    }
}

/**
 * Full-screen reading dialog for immersive exploration of elemental lore.
 */
@Composable
private fun ElementalLoreDetailDialog(
    entry: ElementalCodexEntry,
    isDiscovered: Boolean,
    trainedCharacters: List<Pair<Character, Float>>,
    onDismiss: () -> Unit,
    onTrain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onTrain,
                colors = ButtonDefaults.buttonColors(containerColor = entry.primaryColor)
            ) {
                Text("Trénovat živel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(entry.icon, fontSize = 28.sp)
                Column {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = entry.primaryColor
                    )
                    Text(
                        text = entry.epithet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Patron and Relic header
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = entry.primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, entry.primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🏛️ Patron: ${entry.ancientArchon}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = entry.primaryColor
                            )
                            Text(
                                text = "🏆 Relikvie: ${entry.sacredRelic}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "💥 Efekt: ${entry.statusEffectName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    LoreSection(
                        title = "Pradávný Původ Živlu",
                        accentColor = entry.primaryColor,
                        content = entry.historicalOrigin
                    )
                }

                item {
                    LoreSection(
                        title = "Tajemství Tréninku v Komnatách",
                        accentColor = entry.secondaryColor,
                        content = entry.trainingLore
                    )
                }

                item {
                    LoreSection(
                        title = "Bojová Povaha & Rezonance",
                        accentColor = entry.primaryColor,
                        content = entry.tacticalNature
                    )
                }

                if (trainedCharacters.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Ovládající hrdinky v panství:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            trainedCharacters.forEach { (char, mult) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${getCharacterEmoji(char)} ${char.name}", fontSize = 12.sp)
                                    Text("${"%.2f".format(mult)}x", fontWeight = FontWeight.Bold, color = entry.primaryColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Returns an expressive archetype icon or fallback icon for a character.
 */
private fun getCharacterEmoji(char: Character): String {
    return when (char.archetypeId) {
        "subka" -> "🌸"
        "odvazna" -> "⚔️"
        "touha" -> "💋"
        "zlomena" -> "⛓️"
        "chladna" -> "❄️"
        "sukuba" -> "🦇"
        "draci_divka" -> "🐉"
        else -> if (char.statusIcon.isNotBlank()) char.statusIcon else "✨"
    }
}
