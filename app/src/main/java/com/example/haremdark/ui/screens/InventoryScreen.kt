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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.haremdark.data.GiftInventoryCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InventoryItem
import com.example.haremdark.models.EquipmentLoadout
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.domain.VoiceTriggerType
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow

enum class ItemSortOption(val title: String, val icon: String) {
    RARITY_DESC("Vzácnost (Nejvyšší)", "💎"),
    RARITY_ASC("Vzácnost (Nejnižší)", "🔹"),
    AFFINITY_DESC("Náklonnost (Nejvyšší)", "💖"),
    AFFINITY_ASC("Náklonnost (Nejnižší)", "🤍"),
    TYPE("Kategorie / Typ", "🏷️"),
    PRICE_DESC("Hodnota (Nejvyšší)", "💰"),
    PRICE_ASC("Hodnota (Nejnižší)", "🪙"),
    COUNT_DESC("Počet (Nejvíce)", "🔢"),
    COUNT_ASC("Počet (Nejméně)", "📉"),
    NAME_ASC("Název (A-Z)", "🔤"),
    NAME_DESC("Název (Z-A)", "🔡")
}

enum class ItemRarityFilter(val rawValue: String, val displayName: String, val color: Color) {
    ALL("ALL", "Vše", Color(0xFFB0BEC5)),
    LEGENDARY("Legendární", "👑 Legendární", Color(0xFFFFD700)),
    EPIC("Epický", "🔮 Epické", Color(0xFFE040FB)),
    RARE("Vzácný", "💎 Vzácné", Color(0xFF00E5FF)),
    COMMON("Běžný", "🌿 Běžné", Color(0xFF81C784))
}

enum class ItemAffinityFilter(val displayName: String, val minAffinity: Int, val icon: String) {
    ALL("Vše", 0, "✨"),
    ANY_BONUS("S bonusem (>0)", 1, "💖"),
    HIGH_BONUS("Vysoký bonus (≥20)", 20, "🔥"),
    LEGENDARY_BONUS("Královský dar (≥40)", 40, "👑")
}

fun getItemAffinityBonusValue(item: InventoryItem): Int {
    val catalogGift = GiftInventoryCatalog.ALL_GIFTS.find { it.id == item.id }
    if (catalogGift != null) {
        return catalogGift.baseAffinity
    }
    val regexAffinity = Regex("""\+(\d+)\s*(?:Náklonnost|Affinity|Pouto)""", RegexOption.IGNORE_CASE)
    val matchAff = regexAffinity.find(item.effectDescription)
    if (matchAff != null) {
        return matchAff.groupValues[1].toIntOrNull() ?: 0
    }
    val regexLoyalty = Regex("""\+(\d+)\s*(?:Loajalita|Loyalty|Věrnost)""", RegexOption.IGNORE_CASE)
    val matchLoy = regexLoyalty.find(item.effectDescription)
    if (matchLoy != null) {
        val pts = matchLoy.groupValues[1].toIntOrNull() ?: 0
        return pts * 2
    }
    val cat = item.category.lowercase()
    if (cat.contains("gift") || cat.contains("dar") || item.id.startsWith("gift_") || item.id == "drahy_obojek") {
        return when (item.rarity) {
            "Legendární", "Mýtický" -> 50
            "Epický" -> 35
            "Vzácný" -> 22
            else -> 15
        }
    }
    return 0
}

fun getRarityRank(rarity: String): Int = when (rarity.lowercase()) {
    "mýtický", "mythic" -> 5
    "legendární", "legendary" -> 4
    "epický", "epic" -> 3
    "vzácný", "rare" -> 2
    else -> 1
}

fun getItemCategoryKey(item: InventoryItem): String {
    val cat = item.category.lowercase()
    return when {
        cat.contains("gift") || cat.contains("dar") || item.id.startsWith("gift_") || item.id == "drahy_obojek" -> "gift"
        cat.contains("quest") || cat.contains("artifact") || cat.contains("key") || cat.contains("relic") || cat.contains("document") || item.id.contains("pecet") || item.id.contains("klic") || item.id.contains("listina") -> "quest"
        else -> "combat"
    }
}

fun getItemCategoryOrderRank(item: InventoryItem): Int = when (getItemCategoryKey(item)) {
    "gift" -> 1
    "combat" -> 2
    "quest" -> 3
    else -> 4
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
    var selectedSort by remember { mutableStateOf(ItemSortOption.RARITY_DESC) }
    var selectedRarityFilter by remember { mutableStateOf(ItemRarityFilter.ALL) }
    var selectedAffinityFilter by remember { mutableStateOf(ItemAffinityFilter.ALL) }
    var isFilterPanelExpanded by remember { mutableStateOf(false) }

    var selectedItemForGift by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForDetails by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForSell by remember { mutableStateOf<InventoryItem?>(null) }
    var inspectedQuestLore by remember { mutableStateOf<Pair<String, String>?>(null) }

    val activeFilterCount = remember(selectedRarityFilter, selectedAffinityFilter, searchQuery) {
        var count = 0
        if (selectedRarityFilter != ItemRarityFilter.ALL) count++
        if (selectedAffinityFilter != ItemAffinityFilter.ALL) count++
        if (searchQuery.isNotBlank()) count++
        count
    }

    val filteredItems = remember(
        player.items,
        selectedTab,
        searchQuery,
        selectedSort,
        selectedRarityFilter,
        selectedAffinityFilter
    ) {
        var list = player.items.filter { it.count > 0 }

        // Category filter
        list = when (selectedTab) {
            0 -> list.filter { getItemCategoryKey(it) == "gift" }
            1 -> list.filter { getItemCategoryKey(it) == "combat" }
            2 -> list.filter { getItemCategoryKey(it) == "quest" }
            else -> list
        }

        // Rarity Filter
        if (selectedRarityFilter != ItemRarityFilter.ALL) {
            list = list.filter { it.rarity.equals(selectedRarityFilter.rawValue, ignoreCase = true) }
        }

        // Affinity Bonus Filter
        if (selectedAffinityFilter != ItemAffinityFilter.ALL) {
            list = list.filter { getItemAffinityBonusValue(it) >= selectedAffinityFilter.minAffinity }
        }

        // Search filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.rarity.lowercase().contains(q) ||
                it.effectDescription.lowercase().contains(q) ||
                getItemCategoryKey(it).lowercase().contains(q)
            }
        }

        // Sorting
        when (selectedSort) {
            ItemSortOption.RARITY_DESC -> list.sortedWith(
                compareByDescending<InventoryItem> { getRarityRank(it.rarity) }
                    .thenByDescending { getItemAffinityBonusValue(it) }
                    .thenByDescending { it.price }
            )
            ItemSortOption.RARITY_ASC -> list.sortedWith(
                compareBy<InventoryItem> { getRarityRank(it.rarity) }
                    .thenBy { it.name }
            )
            ItemSortOption.AFFINITY_DESC -> list.sortedWith(
                compareByDescending<InventoryItem> { getItemAffinityBonusValue(it) }
                    .thenByDescending { getRarityRank(it.rarity) }
                    .thenByDescending { it.price }
            )
            ItemSortOption.AFFINITY_ASC -> list.sortedWith(
                compareBy<InventoryItem> { getItemAffinityBonusValue(it) }
                    .thenBy { it.name }
            )
            ItemSortOption.TYPE -> list.sortedWith(
                compareBy<InventoryItem> { getItemCategoryOrderRank(it) }
                    .thenByDescending { getRarityRank(it.rarity) }
                    .thenBy { it.name }
            )
            ItemSortOption.PRICE_DESC -> list.sortedByDescending { it.price * it.count }
            ItemSortOption.PRICE_ASC -> list.sortedBy { it.price }
            ItemSortOption.COUNT_DESC -> list.sortedByDescending { it.count }
            ItemSortOption.COUNT_ASC -> list.sortedBy { it.count }
            ItemSortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            ItemSortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
        }
    }

    val totalItemCount = remember(player.items) { player.items.sumOf { it.count } }
    val totalInventoryValue = remember(player.items) { player.items.sumOf { it.price * it.count } }
    val giftItemsCount = remember(player.items) { player.items.filter { getItemCategoryKey(it) == "gift" }.sumOf { it.count } }
    val combatItemsCount = remember(player.items) { player.items.filter { getItemCategoryKey(it) == "combat" }.sumOf { it.count } }
    val questItemsCount = remember(player.items) { player.items.filter { getItemCategoryKey(it) == "quest" }.sumOf { it.count } }

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
        
        if (player.items.filter { it.count > 0 }.isEmpty()) {
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
                        imageVector = Icons.Default.WorkOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = "Tvoje brašna je prázdná.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Zatím nevlastníš žádné předměty. Získáš je bojem, nákupem na tržnici nebo vařením v alchymistické laboratoři.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {

        // --- CATEGORY SELECTOR TABS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            CategoryTabChip(
                title = "Sety",
                count = engine.getAllLoadouts().size,
                icon = "⚔️",
                selected = selectedTab == 4,
                accentColor = Color(0xFF00BCD4),
                onClick = { selectedTab = 4 },
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedTab == 4) {
            // --- LOADOUT MANAGEMENT VIEW ---
            LoadoutsInventoryTab(gameState = gameState, engine = engine)
        } else {
            // --- SEARCH BAR, FILTER BUTTON & SORT BUTTON ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hledat název, efekt, vzácnost...", fontSize = 11.sp) },
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
                    modifier = Modifier.weight(1f).height(48.dp)
                )

                // Expandable Filter Toggle Button
                OutlinedButton(
                    onClick = { isFilterPanelExpanded = !isFilterPanelExpanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (activeFilterCount > 0 || isFilterPanelExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtry",
                        tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    if (activeFilterCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$activeFilterCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                // Sort Dropdown Menu Box
                var sortMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { sortMenuExpanded = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(selectedSort.icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Řazení", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        Text(
                            text = "📐 Seřadit inventář:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        ItemSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                leadingIcon = { Text(option.icon, fontSize = 16.sp) },
                                text = {
                                    Text(
                                        option.title,
                                        fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
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

            // --- QUICK SORT CHIPS ROW ---
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        text = "Řadit:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                item {
                    val isRarityActive = selectedSort == ItemSortOption.RARITY_DESC || selectedSort == ItemSortOption.RARITY_ASC
                    FilterChip(
                        selected = isRarityActive,
                        onClick = {
                            selectedSort = if (selectedSort == ItemSortOption.RARITY_DESC) ItemSortOption.RARITY_ASC else ItemSortOption.RARITY_DESC
                        },
                        label = {
                            Text(
                                "💎 Vzácnost ${if (selectedSort == ItemSortOption.RARITY_ASC) "▲" else if (selectedSort == ItemSortOption.RARITY_DESC) "▼" else ""}",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    val isAffinityActive = selectedSort == ItemSortOption.AFFINITY_DESC || selectedSort == ItemSortOption.AFFINITY_ASC
                    FilterChip(
                        selected = isAffinityActive,
                        onClick = {
                            selectedSort = if (selectedSort == ItemSortOption.AFFINITY_DESC) ItemSortOption.AFFINITY_ASC else ItemSortOption.AFFINITY_DESC
                        },
                        label = {
                            Text(
                                "💖 Náklonnost ${if (selectedSort == ItemSortOption.AFFINITY_ASC) "▲" else if (selectedSort == ItemSortOption.AFFINITY_DESC) "▼" else ""}",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    val isTypeActive = selectedSort == ItemSortOption.TYPE
                    FilterChip(
                        selected = isTypeActive,
                        onClick = { selectedSort = ItemSortOption.TYPE },
                        label = { Text("🏷️ Typ", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    val isPriceActive = selectedSort == ItemSortOption.PRICE_DESC || selectedSort == ItemSortOption.PRICE_ASC
                    FilterChip(
                        selected = isPriceActive,
                        onClick = {
                            selectedSort = if (selectedSort == ItemSortOption.PRICE_DESC) ItemSortOption.PRICE_ASC else ItemSortOption.PRICE_DESC
                        },
                        label = {
                            Text(
                                "💰 Hodnota ${if (selectedSort == ItemSortOption.PRICE_ASC) "▲" else if (selectedSort == ItemSortOption.PRICE_DESC) "▼" else ""}",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    val isCountActive = selectedSort == ItemSortOption.COUNT_DESC || selectedSort == ItemSortOption.COUNT_ASC
                    FilterChip(
                        selected = isCountActive,
                        onClick = {
                            selectedSort = if (selectedSort == ItemSortOption.COUNT_DESC) ItemSortOption.COUNT_ASC else ItemSortOption.COUNT_DESC
                        },
                        label = {
                            Text(
                                "🔢 Počet ${if (selectedSort == ItemSortOption.COUNT_ASC) "▲" else if (selectedSort == ItemSortOption.COUNT_DESC) "▼" else ""}",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    val isNameActive = selectedSort == ItemSortOption.NAME_ASC || selectedSort == ItemSortOption.NAME_DESC
                    FilterChip(
                        selected = isNameActive,
                        onClick = {
                            selectedSort = if (selectedSort == ItemSortOption.NAME_ASC) ItemSortOption.NAME_DESC else ItemSortOption.NAME_ASC
                        },
                        label = {
                            Text(
                                "🔤 Název ${if (selectedSort == ItemSortOption.NAME_DESC) "Z-A" else "A-Z"}",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // --- EXPANDABLE ADVANCED FILTERS PANEL ---
            AnimatedVisibility(
                visible = isFilterPanelExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header & Clear Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Podrobné filtry",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (activeFilterCount > 0) {
                                TextButton(
                                    onClick = {
                                        selectedRarityFilter = ItemRarityFilter.ALL
                                        selectedAffinityFilter = ItemAffinityFilter.ALL
                                        searchQuery = ""
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Vymazat filtry", fontSize = 11.sp)
                                }
                            }
                        }

                        // Filter by Rarity
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Vzácnost předmětů:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(ItemRarityFilter.values()) { rarityFilter ->
                                    val isSelected = selectedRarityFilter == rarityFilter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedRarityFilter = rarityFilter },
                                        label = {
                                            Text(
                                                rarityFilter.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) rarityFilter.color else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = rarityFilter.color.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Filter by Affinity Bonus Value
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Bonus náklonnosti (Dary):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF80AB))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(ItemAffinityFilter.values()) { affFilter ->
                                    val isSelected = selectedAffinityFilter == affFilter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedAffinityFilter = affFilter },
                                        label = {
                                            Text(
                                                "${affFilter.icon} ${affFilter.displayName}",
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results count status bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zobrazeno ${filteredItems.size} z $totalItemCount předmětů",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Řazení: ${selectedSort.title}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            }

            // --- EMPTY STATE / ITEMS LIST ---
            if (filteredItems.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (activeFilterCount > 0) "🔍 Žádné předměty neodpovídají zvoleným filtrům" else when (selectedTab) {
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
                            text = if (activeFilterCount > 0) "Zkuste upravit vyhledávání, resetovat vzácnost nebo vymazat filtry." else when (selectedTab) {
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
                            if (activeFilterCount > 0) {
                                Button(
                                    onClick = {
                                        selectedRarityFilter = ItemRarityFilter.ALL
                                        selectedAffinityFilter = ItemAffinityFilter.ALL
                                        searchQuery = ""
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Zrušit filtry")
                                }
                            } else if (selectedTab == 1 && onNavigateToActivities != null) {
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
                        val catType = getItemCategoryKey(item)
                        val affinityVal = getItemAffinityBonusValue(item)

                        // Compute which harem girls love this gift if it's a gift
                        val favoriteGirls = remember(item.id, gameState.characters) {
                            if (catType == "gift") {
                                val catalogGift = GiftInventoryCatalog.ALL_GIFTS.find { it.id == item.id }
                                if (catalogGift != null) {
                                    gameState.characters.filter { char ->
                                        catalogGift.favoriteArchetypes.contains(char.archetypeId)
                                    }.map { it.name }
                                } else emptyList()
                            } else emptyList()
                        }

                        InventoryItemCard(
                            item = item,
                            categoryType = catType,
                            affinityBonus = affinityVal,
                            favoriteGirls = favoriteGirls,
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
        }
    }

    // --- GIFT SELECTION MODAL ---
    selectedItemForGift?.let { giftItem ->
        GiftToCharacterModal(
            item = giftItem,
            characters = gameState.characters,
            onDismiss = { selectedItemForGift = null },
            onSelectCharacter = { characterId ->
                val (success, msg) = engine.useItemOnConcubine(giftItem.id, characterId)
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
        val catType = getItemCategoryKey(item)
        val itemAffinity = getItemAffinityBonusValue(item)
        AlertDialog(
            onDismissRequest = { selectedItemForDetails = null },
            icon = { Text(item.icon.ifBlank { if (catType == "gift") "🎁" else if (catType == "quest") "📜" else "🧪" }, fontSize = 32.sp) },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (item.rarity) {
                                    "Legendární" -> Color(0xFFFFD700)
                                    "Epický" -> Color(0xFFE040FB)
                                    "Vzácný" -> Color(0xFF00E5FF)
                                    else -> Color(0xFF81C784)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (itemAffinity > 0) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE91E63).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "💖 +$itemAffinity Náklonnost",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF80AB),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
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
    affinityBonus: Int = 0,
    favoriteGirls: List<String> = emptyList(),
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
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

                            if (affinityBonus > 0) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE91E63).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "💖 +$affinityBonus",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF80AB),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = "• ${item.price} zl.",
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

            // Favorite Girls hint
            if (favoriteGirls.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Oblíbeno u:", fontSize = 9.sp, color = Color(0xFFFF80AB).copy(alpha = 0.8f))
                    favoriteGirls.take(3).forEach { girlName ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "✨ $girlName",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF80AB),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
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
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
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
fun GiftToCharacterModal(
    item: InventoryItem,
    characters: List<Character>,
    onDismiss: () -> Unit,
    onSelectCharacter: (String) -> Unit
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

                if (characters.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("V harému zatím nemáš žádné dívky.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(characters) { character ->
                            val tierInfo = AffinityData.getTierForPoints(character.affinityPoints)
                            val archetype = StaticData.ARCHETYPES[character.archetypeId]

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1A27)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(tierInfo.colorHex).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().clickable { onSelectCharacter(character.id) }
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
                                                text = character.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${archetype?.name ?: character.archetypeId} • Věk ${character.age}",
                                                fontSize = 10.sp,
                                                color = Color(0xFFFF80AB)
                                            )
                                            Text(
                                                text = "${tierInfo.title} (${character.affinityPoints} bodů)",
                                                fontSize = 9.sp,
                                                color = Color(tierInfo.colorHex)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onSelectCharacter(character.id) },
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

@Composable
fun LoadoutsInventoryTab(
    gameState: GameSave,
    engine: GameEngine
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedLoadoutToApply by remember { mutableStateOf<EquipmentLoadout?>(null) }
    var newName by remember { mutableStateOf("") }
    var selectedSituation by remember { mutableStateOf("DPS") }
    var selectedIcon by remember { mutableStateOf("⚔️") }
    var sourceCharacterId by remember { mutableStateOf<String?>(null) }

    val allLoadouts = remember(gameState.savedLoadouts) { engine.getAllLoadouts() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚔️ Bojové Sety & Loadouty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "Přednastavené konfigurace výbavy pro různé situace v boji.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                newName = "Vlastní set #${allLoadouts.size + 1}"
                                showCreateDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uložit set", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33000000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Taktický tip: Přepínejte mezi vysokým poškozením (DPS), obranou (Tank) nebo temnou magií před každým bossem jediným klepnutím!",
                            fontSize = 11.sp,
                            color = Color(0xFFCE93D8),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        items(allLoadouts, key = { it.id }) { loadout ->
            val isActive = loadout.id == gameState.activeLoadoutId

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFF2E1C2B) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isActive) Color(0xFFFF80AB) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header Row: Icon, Name, Situation Badge, Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(loadout.icon, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = loadout.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isActive) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF9C27B0).copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "Situace: ${loadout.situationTag}",
                                        color = Color(0xFFFF80AB),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "✅ AKTIVNÍ",
                                    color = Color(0xFF81C784),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Description
                    Text(
                        text = loadout.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    // Equipment slots breakdown
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x22000000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🗡️ Zbraň:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(
                                    loadout.weaponItem?.name ?: "Základní zbraň",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFCC80)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🛡️ Zbroj:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(
                                    loadout.armorItem?.name ?: "Žádná zbroj",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF90CAF9)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("💍 Doplněk:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(
                                    loadout.accessoryItem?.name ?: "Žádný amulet",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCE93D8)
                                )
                            }
                        }
                    }

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sound test button
                        IconButton(
                            onClick = {
                                val deployed = gameState.characters.firstOrNull()
                                val line = VoiceManager.playTriggerVoice(VoiceTriggerType.COMBAT_START, deployed)
                                Toast.makeText(context, "📣 Bojový pokřik: „$line“", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Bojový pokřik", tint = Color(0xFFFF80AB))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Delete button if custom
                            if (!loadout.isDefaultPreset) {
                                OutlinedButton(
                                    onClick = {
                                        val removed = engine.deleteCustomLoadout(loadout.id)
                                        val msg = if (removed) "Set '${loadout.name}' byl smazán." else "Set nelze smazat."
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Smazat", fontSize = 11.sp)
                                }
                            }

                            // Quick Apply button
                            Button(
                                onClick = {
                                    selectedLoadoutToApply = loadout
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("⚡ Aktivovat set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to choose who to equip with the loadout
    selectedLoadoutToApply?.let { loadout ->
        Dialog(onDismissRequest = { selectedLoadoutToApply = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Aktivovat ${loadout.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vyberte, komu chcete tuto bojovou konfiguraci nasadit:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val (success, msg) = engine.applyLoadout(loadout.id, null)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    selectedLoadoutToApply = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Pán Dominia (Ty)", fontWeight = FontWeight.Bold)
                                        Text("Aktivovat zbraň a nastavit jako hlavní bojový profil", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        items(gameState.characters) { girl ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val (success, msg) = engine.applyLoadout(loadout.id, girl.id)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    selectedLoadoutToApply = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚔️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(girl.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Boj: ${girl.skills["combat"] ?: 0} | HP: ${girl.hp}/${girl.maxHp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { selectedLoadoutToApply = null },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Zavřít")
                    }
                }
            }
        }
    }

    // Modal to create/save a new loadout
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("💾 Uložit nový bojový set") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Vytvoří nový loadout z aktuálně nasazené výbavy hráče nebo vybrané dívky.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Název loadoutu") },
                        placeholder = { Text("např. Elitní assasin") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Zdroj výbavy:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = sourceCharacterId == null,
                                onClick = { sourceCharacterId = null },
                                label = { Text("👑 Pán", fontSize = 11.sp) }
                            )
                        }
                        items(gameState.characters) { char ->
                            FilterChip(
                                selected = sourceCharacterId == char.id,
                                onClick = { sourceCharacterId = char.id },
                                label = { Text(char.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Bojová situace / Typ:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val situations = listOf("DPS", "TANK", "DARK_MAGIC", "BLEED", "BALANCED", "CUSTOM")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(situations) { tag ->
                            FilterChip(
                                selected = selectedSituation == tag,
                                onClick = { selectedSituation = tag },
                                label = { Text(tag, fontSize = 10.sp) }
                            )
                        }
                    }

                    Text("Ikona:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val icons = listOf("⚔️", "🛡️", "🔮", "🩸", "⚖️", "👑", "🏹")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(icons) { ic ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedIcon == ic) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedIcon = ic }.padding(2.dp)
                            ) {
                                Text(ic, fontSize = 18.sp, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val saved = engine.saveCurrentLoadout(
                        name = newName.ifBlank { "Vlastní set #${allLoadouts.size + 1}" },
                        situationTag = selectedSituation,
                        icon = selectedIcon,
                        characterId = sourceCharacterId
                    )
                    Toast.makeText(context, "Bojový set '${saved.name}' byl vytvořen!", Toast.LENGTH_SHORT).show()
                    showCreateDialog = false
                }) {
                    Text("Uložit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Zrušit")
                }
            }
        )
    }
}
