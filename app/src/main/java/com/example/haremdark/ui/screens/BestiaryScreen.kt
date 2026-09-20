package com.example.haremdark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.BestiaryCatalog
import com.example.haremdark.models.BestiaryEntry
import com.example.haremdark.models.GameSave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiaryScreen(
    gameState: GameSave,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Vše") }
    val filterTabs = listOf("Vše", "Objevené", "Bossi", "Uzamčené")
    var selectedEntryDetail by remember { mutableStateOf<BestiaryEntry?>(null) }

    // Merge catalog with player saved progress
    val savedMap = gameState.bestiaryEntries.associateBy { it.enemyId }
    val allEntries = BestiaryCatalog.ALL_ENTRIES.map { catalogEntry ->
        val saved = savedMap[catalogEntry.enemyId]
        if (saved != null) {
            catalogEntry.copy(
                isDiscovered = saved.isDiscovered,
                encounterCount = saved.encounterCount,
                hp = if (saved.hp > 0) saved.hp else catalogEntry.hp,
                attack = if (saved.attack > 0) saved.attack else catalogEntry.attack,
                defense = if (saved.defense > 0) saved.defense else catalogEntry.defense,
                speed = if (saved.speed > 0) saved.speed else catalogEntry.speed,
                description = saved.description.ifEmpty { catalogEntry.description },
                dropChances = if (saved.dropChances.isNotEmpty()) saved.dropChances else catalogEntry.dropChances
            )
        } else {
            catalogEntry
        }
    }

    val filteredEntries = allEntries.filter { entry ->
        val matchesSearch = entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.title.contains(searchQuery, ignoreCase = true) ||
                entry.description.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Objevené" -> entry.isDiscovered
            "Bossi" -> entry.isBoss && entry.isDiscovered
            "Uzamčené" -> !entry.isDiscovered
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val discoveredCount = allEntries.count { it.isDiscovered }
    val totalCount = allEntries.size
    val progressPercent = if (totalCount > 0) (discoveredCount.toFloat() / totalCount.toFloat()) * 100f else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bestiář příšer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📖", fontSize = 24.sp)
                            Column {
                                Text("Objevené druhy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Pátrej a porážej nepřátele v boji", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                        Text(
                            text = "$discoveredCount / $totalCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Hledat v bestiáři...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Hledat") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vymazat")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = filterTabs.indexOf(selectedFilter),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                filterTabs.forEach { tab ->
                    Tab(
                        selected = selectedFilter == tab,
                        onClick = { selectedFilter = tab },
                        text = { Text(tab, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            // Entries List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEntries) { entry ->
                    BestiaryCard(
                        entry = entry,
                        onClick = { if (entry.isDiscovered) selectedEntryDetail = entry }
                    )
                }
            }
        }
    }

    // Detail Dialog with Tabs (Overview, Stats, Drops)
    if (selectedEntryDetail != null) {
        BestiaryDetailDialog(
            entry = selectedEntryDetail!!,
            onDismiss = { selectedEntryDetail = null }
        )
    }
}

@Composable
fun BestiaryCard(
    entry: BestiaryEntry,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isDiscovered) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = entry.isDiscovered, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (entry.isDiscovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.DarkGray.copy(alpha = 0.2f)
                    )
                    .border(
                        1.dp,
                        if (entry.isDiscovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isDiscovered) {
                    Text(entry.icon, fontSize = 28.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Uzamčeno",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                if (entry.isDiscovered) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (entry.isBoss) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "BOSS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${entry.title} • Živel: ${entry.element}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Střetnutí: ${entry.encounterCount}x",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Text(
                        text = "Neznámý protivník",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Poražte nepřítele v boji pro objev záznamu",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            if (entry.isDiscovered) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detail",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BestiaryDetailDialog(
    entry: BestiaryEntry,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val detailTabs = listOf("📜 Lore", "📊 Staty", "🎁 Drops & Šance")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít")
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(entry.icon, fontSize = 32.sp)
                    Column {
                        Text(entry.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(entry.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                // Tabs inside dialog
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    detailTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 380.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Tab 0: Lore & Description
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Příběh a pozadí", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(entry.description, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Justify)
                                    }
                                }
                            }
                            item {
                                Text("Pokořeno celkem: ${entry.encounterCount}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    1 -> {
                        // Tab 1: Stats & Weaknesses
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text("📊 Bojové statistiky", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatBox("❤️ HP", "${entry.hp}", Modifier.weight(1f))
                                    StatBox("⚔️ Útok", "${entry.attack}", Modifier.weight(1f))
                                    StatBox("🛡️ Obrana", "${entry.defense}", Modifier.weight(1f))
                                    StatBox("⚡ Rychl.", "${entry.speed}", Modifier.weight(1f))
                                }
                            }

                            item {
                                Text("🎯 Slabiny a odolnosti", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (entry.weaknesses.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        entry.weaknesses.forEach { weakness ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = weakness,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text("Žádné zjevné slabiny.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    2 -> {
                        // Tab 2: Drops & Probabilities
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "🎁 Pravděpodobnost kořisti a dropů",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Šance na získání surovin a fragmentů při vítězství v boji:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (entry.dropChances.isNotEmpty()) {
                                items(entry.dropChances) { drop ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (drop.isRare) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (drop.isRare) "🌟 ${drop.itemName}" else "📦 ${drop.itemName}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (drop.isRare) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Text(
                                                    text = "Typ: ${drop.itemType}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (drop.probabilityPercent >= 50) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "${drop.probabilityPercent}% šance",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (drop.probabilityPercent >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Text("Žádné evidované dropy pro tohoto nepřítele.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
