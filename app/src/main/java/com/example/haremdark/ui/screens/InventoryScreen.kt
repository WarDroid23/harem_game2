package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.R
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Concubine
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InventoryItem

enum class ItemSortOption(val title: String) {
    COUNT_DESC("Největší počet"),
    PRICE_DESC("Nejvyšší hodnota"),
    NAME_ASC("Název A-Z"),
    RARITY("Vzácnost")
}

@Composable
fun InventoryScreen(
    gameState: GameSave,
    engine: GameEngine,
    onNavigateToHarem: (() -> Unit)? = null,
    onNavigateToActivities: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = gameState.player

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Gifts, 1: Combat Consumables, 2: Quest Items, 3: All
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(ItemSortOption.COUNT_DESC) }
    var selectedItemForGift by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForDetails by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForSell by remember { mutableStateOf<InventoryItem?>(null) }
    var inspectedQuestLore by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Helper categorization
    fun getItemCategoryType(item: InventoryItem): String {
        val cat = item.category.lowercase()
        return when {
            cat.contains("gift") || cat.contains("dar") || item.id.startsWith("gift_") || item.id == "drahy_obojek" -> "gift"
            cat.contains("quest") || cat.contains("artifact") || cat.contains("key") || cat.contains("relic") || cat.contains("document") || item.id.contains("pecet") || item.id.contains("klic") || item.id.contains("listina") -> "quest"
            else -> "combat"
        }
    }

    val filteredItems = remember(player.items, selectedTab, searchQuery, selectedSort) {
        var list = player.items.filter { it.count > 0 }

        // Category filter
        list = when (selectedTab) {
            0 -> list.filter { getItemCategoryType(it) == "gift" }
            1 -> list.filter { getItemCategoryType(it) == "combat" }
            2 -> list.filter { getItemCategoryType(it) == "quest" }
            else -> list
        }

        // Search filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.rarity.lowercase().contains(q) ||
                it.effectDescription.lowercase().contains(q)
            }
        }

        // Sorting
        when (selectedSort) {
            ItemSortOption.COUNT_DESC -> list.sortedByDescending { it.count }
            ItemSortOption.PRICE_DESC -> list.sortedByDescending { it.price * it.count }
            ItemSortOption.NAME_ASC -> list.sortedBy { it.name }
            ItemSortOption.RARITY -> list.sortedByDescending {
                when (it.rarity) {
                    "Legendární" -> 4
                    "Epický" -> 3
                    "Vzácný" -> 2
                    else -> 1
                }
            }
        }
    }

    val totalItemCount = remember(player.items) { player.items.sumOf { it.count } }
    val totalInventoryValue = remember(player.items) { player.items.sumOf { it.price * it.count } }
    val giftItemsCount = remember(player.items) { player.items.filter { getItemCategoryType(it) == "gift" }.sumOf { it.count } }
    val combatItemsCount = remember(player.items) { player.items.filter { getItemCategoryType(it) == "combat" }.sumOf { it.count } }
    val questItemsCount = remember(player.items) { player.items.filter { getItemCategoryType(it) == "quest" }.sumOf { it.count } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- INVENTORY HERO HEADER BANNER ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.hero_dark_dominion),
                    contentDescription = "Brašna Pána",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xEE1A0F1D), Color(0xCC2A1429), Color(0xDD120B15))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎒", fontSize = 20.sp)
                            Text(
                                text = "Královská brašna pána",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Kapacita: $totalItemCount předmětů • Celková hodnota: $totalInventoryValue zl.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Tříděno do kategorií: Dary, Bojová alchymie & Úkolové relikvie",
                            fontSize = 10.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }

                    // Gold balance pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2E2010),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("💰", fontSize = 12.sp)
                            Text(
                                text = "${player.gold}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // --- CATEGORY SELECTOR TABS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryTabChip(
                title = "Dary",
                count = giftItemsCount,
                icon = "🎁",
                selected = selectedTab == 0,
                accentColor = Color(0xFFE91E63),
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            CategoryTabChip(
                title = "Bojové",
                count = combatItemsCount,
                icon = "🧪",
                selected = selectedTab == 1,
                accentColor = Color(0xFF4CAF50),
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
            CategoryTabChip(
                title = "Úkolové",
                count = questItemsCount,
                icon = "📜",
                selected = selectedTab == 2,
                accentColor = Color(0xFFFF9800),
                onClick = { selectedTab = 2 },
                modifier = Modifier.weight(1f)
            )
            CategoryTabChip(
                title = "Vše",
                count = totalItemCount,
                icon = "🎒",
                selected = selectedTab == 3,
                accentColor = Color(0xFF9C27B0),
                onClick = { selectedTab = 3 },
                modifier = Modifier.weight(0.85f)
            )
        }

        // --- SEARCH BAR & SORTING ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Hledat v inventáři...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f).height(50.dp)
            )

            // Sort Menu Box
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { sortMenuExpanded = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Řazení", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    ItemSortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.title,
                                    fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                selectedSort = option
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- EMPTY STATE / ITEMS LIST ---
        if (filteredItems.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "🎁 V této kategorii nemáš žádné dary pro harém."
                            1 -> "🧪 Žádné bojové lektvary v brašně."
                            2 -> "📜 Žádné úkolové předměty nenalezeny."
                            else -> "🎒 Brašna je v této sekci prázdná."
                        },
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (selectedTab) {
                            0 -> "Dary můžeš zakoupit na tržnici nebo získat z průzkumu dominií a úkolů."
                            1 -> "Lektvary můžeš uvařit v Alchymistické laboratoři v Pevnosti nebo vybojovat v aréně."
                            2 -> "Úkolové předměty získáš plněním misí mafie, zkoumáním mapy a lovem bossů."
                            else -> "Získej nové předměty bojem, alchymií nebo misemi."
                        },
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedTab == 1 && onNavigateToActivities != null) {
                            Button(
                                onClick = onNavigateToActivities,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Do Alchymie")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val catType = getItemCategoryType(item)
                    InventoryItemCard(
                        item = item,
                        categoryType = catType,
                        onGiftClick = { selectedItemForGift = item },
                        onUseCombatClick = {
                            val (success, msg) = engine.useCombatConsumableOnPlayer(item.id)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onInspectQuestClick = {
                            val (success, lore) = engine.inspectQuestItem(item.id)
                            inspectedQuestLore = Pair(item.name, lore)
                        },
                        onDetailsClick = { selectedItemForDetails = item },
                        onSellClick = { selectedItemForSell = item }
                    )
                }
            }
        }
    }

    // --- GIFT SELECTION MODAL ---
    selectedItemForGift?.let { giftItem ->
        GiftToConcubineModal(
            item = giftItem,
            concubines = gameState.concubines,
            onDismiss = { selectedItemForGift = null },
            onSelectConcubine = { concubineId ->
                val (success, msg) = engine.useItemOnConcubine(giftItem.id, concubineId)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                selectedItemForGift = null
            }
        )
    }

    // --- QUEST ITEM LORE INSPECTION DIALOG ---
    inspectedQuestLore?.let { (itemName, loreText) ->
        AlertDialog(
            onDismissRequest = { inspectedQuestLore = null },
            icon = { Text("📜", fontSize = 28.sp) },
            title = { Text(itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(loreText, fontSize = 13.sp, lineHeight = 18.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "✨ Předmět je bezpečně uchován ve tvé brašně pro příběhové události a vyjednávání.",
                            fontSize = 11.sp,
                            color = Color(0xFFFFB74D),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { inspectedQuestLore = null }, shape = RoundedCornerShape(8.dp)) {
                    Text("Uložit poznatky")
                }
            }
        )
    }

    // --- ITEM DETAILS & LORE MODAL ---
    selectedItemForDetails?.let { item ->
        val catType = getItemCategoryType(item)
        AlertDialog(
            onDismissRequest = { selectedItemForDetails = null },
            icon = { Text(item.icon.ifBlank { if (catType == "gift") "🎁" else if (catType == "quest") "📜" else "🧪" }, fontSize = 32.sp) },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Vzácnost: ${item.rarity} • ${item.count}x v brašně",
                        fontSize = 11.sp,
                        color = when (item.rarity) {
                            "Legendární" -> Color(0xFFFFD700)
                            "Epický" -> Color(0xFFE040FB)
                            "Vzácný" -> Color(0xFF00E5FF)
                            else -> Color(0xFF81C784)
                        }
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.description, fontSize = 13.sp, lineHeight = 18.sp)
                    if (item.effectDescription.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "⚡ Efekt: ${item.effectDescription}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    Text(
                        text = "Prodejní hodnota: ${item.price} zl. za kus (Celkem: ${item.price * item.count} zl.)",
                        fontSize = 11.sp,
                        color = Color(0xFFFFD700)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedItemForDetails = null }) {
                    Text("Zavřít")
                }
            }
        )
    }

    // --- SELL ITEM MODAL ---
    selectedItemForSell?.let { item ->
        var sellCount by remember { mutableIntStateOf(1) }
        AlertDialog(
            onDismissRequest = { selectedItemForSell = null },
            icon = { Text("💰", fontSize = 28.sp) },
            title = { Text("Prodat: ${item.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Zvol množství kusů k odprodeji vetešníkovi:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (sellCount > 1) sellCount-- },
                            enabled = sellCount > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Méně")
                        }
                        Text(
                            text = "$sellCount / ${item.count} ks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (sellCount < item.count) sellCount++ },
                            enabled = sellCount < item.count
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Více")
                        }
                    }
                    Text(
                        text = "Zisk ze směny: ${item.price * sellCount} zlatých",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (success, msg) = engine.sellInventoryItem(item.id, sellCount)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        selectedItemForSell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67D0A))
                ) {
                    Text("Prodat za ${item.price * sellCount} zl.")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForSell = null }) {
                    Text("Zrušit")
                }
            }
        )
    }
}

@Composable
fun CategoryTabChip(
    title: String,
    count: Int,
    icon: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) accentColor else Color.Transparent
        ),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "($count)",
                    fontSize = 9.sp,
                    color = if (selected) accentColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    categoryType: String,
    onGiftClick: () -> Unit,
    onUseCombatClick: () -> Unit,
    onInspectQuestClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onSellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (categoryType) {
        "gift" -> Color(0xFFE91E63).copy(alpha = 0.4f)
        "quest" -> Color(0xFFFF9800).copy(alpha = 0.4f)
        else -> Color(0xFF4CAF50).copy(alpha = 0.4f)
    }

    val cardBg = when (categoryType) {
        "gift" -> Color(0xFF24141E)
        "quest" -> Color(0xFF261D15)
        else -> Color(0xFF132219)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Icon, Name, Category Tag & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.icon.ifBlank {
                                    when (categoryType) {
                                        "gift" -> "🎁"
                                        "quest" -> "📜"
                                        else -> "🧪"
                                    }
                                },
                                fontSize = 18.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (item.rarity) {
                                    "Legendární" -> Color(0xFFFFD700).copy(alpha = 0.2f)
                                    "Epický" -> Color(0xFFE040FB).copy(alpha = 0.2f)
                                    "Vzácný" -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                    else -> Color(0xFF81C784).copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = item.rarity,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.rarity) {
                                        "Legendární" -> Color(0xFFFFD700)
                                        "Epický" -> Color(0xFFE040FB)
                                        "Vzácný" -> Color(0xFF00E5FF)
                                        else -> Color(0xFF81C784)
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Text(
                                text = "• ${item.price} zl./ks",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                // Count Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "${item.count} ks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 15.sp
            )

            // Stat Boost / Effect Pill if present
            if (item.effectDescription.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (categoryType) {
                        "gift" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                        "quest" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        else -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "⚡ ${item.effectDescription}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (categoryType) {
                            "gift" -> Color(0xFFFF80AB)
                            "quest" -> Color(0xFFFFB74D)
                            else -> Color(0xFF81C784)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (categoryType) {
                    "gift" -> {
                        Button(
                            onClick = onGiftClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAD1457)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Darovat dívce", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "combat" -> {
                        Button(
                            onClick = onUseCombatClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Použít ihned", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "quest" -> {
                        Button(
                            onClick = onInspectQuestClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prozkoumat runy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Info / Details Button
                OutlinedButton(
                    onClick = onDetailsClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Detaily", modifier = Modifier.size(14.dp))
                }

                // Sell Button
                OutlinedButton(
                    onClick = onSellClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Prodat", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun GiftToConcubineModal(
    item: InventoryItem,
    concubines: List<Concubine>,
    onDismiss: () -> Unit,
    onSelectConcubine: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E121A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.icon.ifBlank { "🎁" }, fontSize = 22.sp)
                        Column {
                            Text("Darovat: ${item.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Vyber otrokyni, které předáš tento dar", fontSize = 11.sp, color = Color(0xFFFF80AB))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                if (concubines.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("V harému zatím nemáš žádné dívky.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(concubines) { concubine ->
                            val tierInfo = AffinityData.getTierForPoints(concubine.affinityPoints)
                            val archetype = StaticData.ARCHETYPES[concubine.archetypeId]

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1A27)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(tierInfo.colorHex).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().clickable { onSelectConcubine(concubine.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(tierInfo.colorHex).copy(alpha = 0.2f),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(tierInfo.icon, fontSize = 18.sp)
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = concubine.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${archetype?.name ?: concubine.archetypeId} • Věk ${concubine.age}",
                                                fontSize = 10.sp,
                                                color = Color(0xFFFF80AB)
                                            )
                                            Text(
                                                text = "${tierInfo.title} (${concubine.affinityPoints} bodů)",
                                                fontSize = 9.sp,
                                                color = Color(tierInfo.colorHex)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onSelectConcubine(concubine.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Darovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
