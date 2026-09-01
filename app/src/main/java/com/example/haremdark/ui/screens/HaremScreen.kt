package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.R
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Concubine
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.ConcubineCard
import com.example.haremdark.ui.components.ConcubineDetailDialog
import com.example.haremdark.ui.components.ConcubineGridCard
import com.example.haremdark.ui.components.InteractionDialog

@Composable
fun HaremScreen(
    gameState: GameSave,
    engine: GameEngine,
    onNavigateToHunt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedHaremTab by remember { mutableIntStateOf(0) }
    val haremTabs = listOf("🔲 Mřížka", "🛏️ Komnaty", "👑 Hierarchie", "👶 Dynastie", "👗 Garderóba", "🖼️ Galerie")

    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Všechny", "★ Oblíbená", "💍 Vztahy", "💰 Na nájmu", "🤰 Březí")
    
    var selectedSort by remember { mutableStateOf("Náklonnost") }
    val sortOptions = listOf("Náklonnost", "Rarita", "Nedávno")
    var sortExpanded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    var selectedConcubineForProfile by remember { mutableStateOf<Concubine?>(null) }
    var selectedConcubineForInteraction by remember { mutableStateOf<Concubine?>(null) }

    val filteredList = remember(gameState.concubines, selectedFilter, searchQuery, selectedSort) {
        var list = when (selectedFilter) {
            1 -> gameState.concubines.filter { it.oblibena }
            2 -> gameState.concubines.filter { it.jeManzelkou || it.partnerka }
            3 -> gameState.concubines.filter { it.naNajmu }
            4 -> gameState.concubines.filter { it.tehotna }
            else -> gameState.concubines
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) || it.archetypeId.contains(searchQuery, ignoreCase = true) }
        }
        
        when (selectedSort) {
            "Náklonnost" -> list = list.sortedByDescending { it.affinityPoints }
            "Rarita" -> list = list.sortedByDescending { it.rarity }
            "Nedávno" -> list = list.sortedByDescending { it.lastInteractionDay }
        }
        
        list
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Harem Top Sub-Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedHaremTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                haremTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedHaremTab == index,
                        onClick = { selectedHaremTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedHaremTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            if (gameState.concubines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentDissatisfied,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "Tvoje komnaty jsou prázdné.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Zatím jsi neulovil žádnou otrokyni do svého harému. Vydej se na průzkum dominií a ulov si první krásky, které budou sloužit tvým temným tužbám.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToHunt,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vyrazit na Lov", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
            Box(modifier = Modifier.weight(1f)) {
            when (selectedHaremTab) {
                0 -> {
                    // --- TAB 0: DEDICATED HAREM GRID SCREEN ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Search & Quick Stats Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Hledat...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Box {
                                IconButton(
                                    onClick = { sortExpanded = true },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .height(56.dp)
                                        .width(48.dp)
                                ) {
                                    Icon(Icons.Default.Sort, contentDescription = "Třídit", tint = MaterialTheme.colorScheme.primary)
                                }
                                DropdownMenu(
                                    expanded = sortExpanded,
                                    onDismissRequest = { sortExpanded = false }
                                ) {
                                    sortOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = { 
                                                selectedSort = option
                                                sortExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${gameState.concubines.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                        Text("dívek", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }

                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filters.forEachIndexed { index, filter ->
                                FilterChip(
                                    selected = selectedFilter == index,
                                    onClick = { selectedFilter = index },
                                    label = { Text(filter, fontSize = 11.sp, fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        // Grid of Characters
                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SentimentDissatisfied,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Žádná dívka neodpovídá zvolenému filtru.",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                    Button(
                                        onClick = onNavigateToHunt,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Jít na lov nových otrokyň", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(top = 2.dp, bottom = 90.dp)
                            ) {
                                items(filteredList, key = { it.id }) { concubine ->
                                    ConcubineGridCard(
                                        concubine = concubine,
                                        onClick = { selectedConcubineForProfile = concubine },
                                        onFavoriteClick = {
                                            val res = engine.setFavorite(concubine.id)
                                            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // --- TAB 1: CONCUBINE CHAMBERS (LIST VIEW) ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
                    ) {
                        // Hero Boudoir Banner
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column {
                                    Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_harem_boudoir),
                                            contentDescription = "Komnaty harému",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(Color.Transparent, Color(0xDD1B0914))
                                                    )
                                                )
                                        )
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Komnaty harému (Úroveň ${gameState.haremLevel})",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${gameState.concubines.size} dívek",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFFFD700),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "Pasivní příjem: +${gameState.haremLevel * 10} zl./den • EXP: ${gameState.haremExp}/${gameState.haremMaxExp}",
                                                fontSize = 10.sp,
                                                color = Color(0xFFFFCDD2)
                                            )
                                        }
                                    }

                                    // Progress bar
                                    val progress = (gameState.haremExp.toFloat() / gameState.haremMaxExp.toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }

                        // Filter Chips
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                filters.forEachIndexed { index, filter ->
                                    FilterChip(
                                        selected = selectedFilter == index,
                                        onClick = { selectedFilter = index },
                                        label = { Text(filter, fontSize = 11.sp, fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        // Concubines List
                        if (filteredList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SentimentDissatisfied,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Žádná dívka neodpovídá zvolenému filtru.",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredList, key = { it.id }) { concubine ->
                                ConcubineCard(
                                    concubine = concubine,
                                    onInteractClick = { selectedConcubineForProfile = concubine },
                                    onDetailClick = { selectedConcubineForProfile = concubine },
                                    onFavoriteClick = {
                                        val res = engine.setFavorite(concubine.id)
                                        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // --- TAB 2: HIERARCHY & ROLES ---
                    HaremHierarchyTab(gameState = gameState, engine = engine)
                }

                3 -> {
                    // --- TAB 3: DYNASTY & CHILDREN ---
                    HaremDynastyTab(gameState = gameState, engine = engine)
                }

                4 -> {
                    // --- TAB 4: WARDROBE & JEWELRY ---
                    HaremWardrobeTab(gameState = gameState, engine = engine)
                }

                5 -> {
                    // --- TAB 5: EMBEDDED GALLERY ---
                    GalleryScreen(gameState = gameState)
                }
            }
            }
            }
        }

        // Floating Action Button for Hunt (when on Grid or Chambers tab)
        if (!gameState.concubines.isEmpty() && (selectedHaremTab == 0 || selectedHaremTab == 1)) {
            FloatingActionButton(
                onClick = onNavigateToHunt,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ulovit dívku")
                    Text("Ulovit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Comprehensive Character Profile & Gifting Dialog
    selectedConcubineForProfile?.let { concubine ->
        val currentConcubine = gameState.concubines.firstOrNull { it.id == concubine.id } ?: concubine
        ConcubineDetailDialog(
            concubine = currentConcubine,
            player = gameState.player,
            onDismiss = { selectedConcubineForProfile = null },
            onGiveDirectGift = { gift ->
                val (success, msg) = engine.giveDirectGift(gift.id, currentConcubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onUseInventoryItem = { item ->
                val (success, msg) = engine.useItemOnConcubine(item.id, currentConcubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onExecuteInteraction = { interaction ->
                val (success, msg) = engine.executeInteraction(currentConcubine.id, interaction)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onCourtRomance = {
                val (success, msg) = engine.courtRomance(currentConcubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onMarry = {
                val (success, msg) = engine.marryConcubine(currentConcubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            },
            onRent = { client, days ->
                val (success, msg) = engine.rentSlave(currentConcubine.id, client, days)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) selectedConcubineForProfile = null
            }
        )
    }
}

@Composable
fun HaremHierarchyTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val concubines = gameState.concubines

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hierarchie a role v paláci", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Urči postavení každé dívky. Vrchní favoritka posiluje tvou duševní sílu v soubojích. Správkyně zvyšují denní výnosy dominia.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Rank categories
        val ranks = listOf(
            Triple("👑 Vrchní favoritka & Družky", "Dívky s nejvyšším postavením, které tě provázejí a udělují požehnání.", concubines.filter { it.oblibena || it.jeManzelkou }),
            Triple("💎 Důvěrnice & Konkubíny", "Věrné členky harému s vysokou loajalitou a vlivem.", concubines.filter { !it.oblibena && !it.jeManzelkou && it.loajalita >= 50 }),
            Triple("🗝️ Komorné & Služebné", "Dívky plnící denní povinnosti v komnatách a paláci.", concubines.filter { !it.oblibena && !it.jeManzelkou && it.loajalita in 25..49 }),
            Triple("⛓️ Pokorné otrokyně", "Nově ulovené nebo zkrocené dívky vyžadující další výcvik a poslušnost.", concubines.filter { !it.oblibena && !it.jeManzelkou && it.loajalita < 25 })
        )

        ranks.forEach { (rankTitle, rankDesc, rankList) ->
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(rankTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("${rankList.size} dívek", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Text(rankDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                        if (rankList.isEmpty()) {
                            Text("Žádná dívka v této hodnosti.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            rankList.forEach { c ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(c.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Loajalita: ${c.loajalita}% • Poslušnost: ${c.poslusnost}% • ${c.role}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (!c.oblibena) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val res = engine.setFavorite(c.id)
                                                        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("★ Jmenovat", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HaremDynastyTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val pregnantConcubines = gameState.concubines.filter { it.tehotna }
    val totalChildren = gameState.concubines.sumOf { it.deti }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("👶 Dynastie dominia & Potomci", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Udržuj a rozšiřuj rodovou linii temného pána. Těhotné otrokyně vyžadují péči. V budoucnu se tvoji potomci stanou generály a dědici.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Celkem dětí", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("$totalChildren", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Probíhající těhotenství", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("${pregnantConcubines.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                            }
                        }
                    }
                }
            }
        }

        // Section: Currently pregnant concubines
        item {
            Text("Probíhající těhotenství v harému", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }

        if (pregnantConcubines.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "V současnosti není žádná dívka v harému těhotná.\nVyužij milostné a intimní interakce k zplození potomka.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(pregnantConcubines) { concubine ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🤰 ${concubine.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE91E63).copy(alpha = 0.2f)) {
                                Text("Den těhotenství: ${concubine.dnyTehotenstvi}/9", fontSize = 10.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        val progress = (concubine.dnyTehotenstvi.toFloat() / 9f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFE91E63)
                        )

                        Text("Péče o nastávající matku zaručuje zdravé a silné potomky pro tvé dominium.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Dynasty Training
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🛡️ Trénink a vzdělávání rodové linie", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Investuj do soukromých učitelů šermu, temné magie a diplomacie pro své děti.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    Button(
                        onClick = {
                            if (gameState.player.gold >= 100) {
                                engine.updateState { state ->
                                    val newPlayer = state.player.copy(gold = (state.player.gold - 100).coerceAtLeast(0))
                                    state.copy(player = newPlayer)
                                }
                                Toast.makeText(context, "📚 Učitelé byli najati! Následníci získávají nové vědomosti.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Nemáš dostatek zlata (100 zl.)!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Najmout mistry pro dynastii (100 zl.)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HaremWardrobeTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val wardrobeItems = listOf(
        Triple("Hedvábné průsvitné roucho", "Luxusní oděv zvyšující poslušnost a půvab dívky o +15.", 60),
        Triple("Kožený postroj & pouta", "Zvyšuje submisivitu a zamezuje pokusům o útěk.", 85),
        Triple("Zlatý obojek dominia", "Klenot dokazující absolutní vlastnictví pánem. +20 Loajalita.", 140),
        Triple("Rubínový amulet vášně", "Probouzí neuhasitelnou touhu a zvyšuje příjem z nájmu o +25%.", 180),
        Triple("Černá sametová večerní róba", "Prestižní šat pro doprovod pána na šlechtické hostiny.", 120)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("👗 Garderóba & Šperky harému", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Nakupuj luxusní oděvy, korzety a ozdoby pro své otrokyně. Zvyšují jejich loajalitu, půvab a výnosy ze správy komnat.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }

        items(wardrobeItems) { (name, desc, price) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                            Text("💰 $price zl.", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    Button(
                        onClick = {
                            if (gameState.player.gold >= price) {
                                engine.updateState { state ->
                                    val newPlayer = state.player.copy(gold = (state.player.gold - price).coerceAtLeast(0))
                                    state.copy(player = newPlayer)
                                }
                                Toast.makeText(context, "✨ Zakoupeno: $name! Harém získá nový luxus.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Nemáš dostatek zlata ($price zl.)!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = gameState.player.gold >= price,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Zakoupit pro harém", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
