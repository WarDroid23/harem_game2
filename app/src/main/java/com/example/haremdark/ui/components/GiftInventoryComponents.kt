package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.GiftInventoryCatalog
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.*

/**
 * Modernized, highly interactive Gift Inventory component integrated into the character interaction modal.
 */
@Composable
fun CollectibleGiftInventoryTab(
    character: Character,
    player: Player,
    engine: GameEngine?,
    onDirectGiftLegacy: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(GiftInventoryUiState()) }

    // Build the inventory slots by querying the catalog against the player's possession
    val allCatalogGifts = remember { GiftInventoryCatalog.ALL_GIFTS }

    val currentInventorySlots = remember(player.items, allCatalogGifts) {
        allCatalogGifts.map { gift ->
            val ownedItem = player.items.find { it.id == gift.id }
            GiftInventorySlot(
                item = gift,
                quantity = ownedItem?.count ?: 0
            )
        }
    }

    // Filtered slots according to Category, Search, and Favorites
    val displayedSlots = remember(
        currentInventorySlots,
        uiState.selectedCategory,
        uiState.searchQuery,
        uiState.filterOnlyFavorites,
        character.archetypeId
    ) {
        var list = currentInventorySlots

        if (uiState.selectedCategory != null) {
            list = list.filter { it.item.category == uiState.selectedCategory }
        }

        if (uiState.filterOnlyFavorites) {
            list = list.filter { it.item.isFavoriteOf(character.archetypeId) }
        }

        if (uiState.searchQuery.isNotBlank()) {
            val q = uiState.searchQuery.trim().lowercase()
            list = list.filter {
                it.item.name.lowercase().contains(q) ||
                it.item.description.lowercase().contains(q) ||
                it.item.category.displayName.lowercase().contains(q)
            }
        }

        // Sort: Owned first, then Favorites, then Rarity
        list.sortedWith(
            compareByDescending<GiftInventorySlot> { it.quantity > 0 }
                .thenByDescending { it.item.isFavoriteOf(character.archetypeId) }
                .thenByDescending { it.item.rarity.ordinal }
        )
    }

    val affinityTier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // TOP SUMMARY BAR: Wealth & Character Affinity Status
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🪙", fontSize = 20.sp)
                    Column {
                        Text("Pokladnice Pána", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("${player.gold} zlata", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 14.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(affinityTier.colorHex).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(affinityTier.colorHex).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(affinityTier.icon, fontSize = 12.sp)
                        Text(
                            text = "${affinityTier.title} (Úr. ${affinityTier.level})",
                            color = Color(affinityTier.colorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // CELEBRATION / RESULT BANNER (when a gift was just given)
        AnimatedVisibility(
            visible = uiState.lastGiftResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.lastGiftResult?.let { result ->
                GiftReactionCard(
                    result = result,
                    onDismiss = { uiState = uiState.copy(lastGiftResult = null) }
                )
            }
        }

        // SEARCH & CATEGORY BAR
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { uiState = uiState.copy(searchQuery = it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Hledat dar...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Hledat", modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { uiState = uiState.copy(searchQuery = "") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Filter Favorites Toggle Button
                FilterChip(
                    selected = uiState.filterOnlyFavorites,
                    onClick = { uiState = uiState.copy(filterOnlyFavorites = !uiState.filterOnlyFavorites) },
                    label = { Text("⭐ Oblíbené", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE91E63).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFFFF80AB)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick Merchant Shop Button
                FilledTonalIconButton(
                    onClick = { uiState = uiState.copy(showPurchaseDialog = true) },
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text("🛍️", fontSize = 18.sp)
                }
            }

            // Category Horizontal Scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { uiState = uiState.copy(selectedCategory = null) },
                        label = { Text("Všechny (${currentInventorySlots.size})", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                items(GiftCategory.entries.toTypedArray()) { cat ->
                    val countInCat = currentInventorySlots.count { it.item.category == cat && it.quantity > 0 }
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = {
                            uiState = uiState.copy(selectedCategory = if (uiState.selectedCategory == cat) null else cat)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(cat.icon, fontSize = 12.sp)
                                Text(
                                    text = if (countInCat > 0) "${cat.displayName} ($countInCat)" else cat.displayName,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // GRID OF GIFT ITEMS
        if (displayedSlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("🎁", fontSize = 32.sp)
                    Text(
                        text = "Žádné dary neodpovídají filtru.",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(
                        onClick = { uiState = uiState.copy(showPurchaseDialog = true) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🛍️ Navštívit Kupce s dary", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedSlots, key = { it.item.id }) { slot ->
                    val isSelected = uiState.selectedSlot?.item?.id == slot.item.id
                    val isFavorite = slot.item.isFavoriteOf(character.archetypeId)

                    GiftItemGridCard(
                        slot = slot,
                        isSelected = isSelected,
                        isFavorite = isFavorite,
                        onClick = {
                            val nextQty = if (slot.quantity > 0) 1 else 1
                            uiState = uiState.copy(
                                selectedSlot = slot,
                                quantityToGift = nextQty
                            )
                        }
                    )
                }
            }
        }

        // SELECTED ITEM INSPECTION & ACTION PANEL
        uiState.selectedSlot?.let { slot ->
            GiftInspectionBottomPanel(
                slot = slot,
                character = character,
                player = player,
                quantity = uiState.quantityToGift,
                onQuantityChange = { uiState = uiState.copy(quantityToGift = it) },
                onGiftConfirmed = { count ->
                    if (engine != null) {
                        if (slot.quantity >= count) {
                            val result = engine.giveCollectibleGift(character.id, slot.item.id, count)
                            if (result != null) {
                                uiState = uiState.copy(
                                    lastGiftResult = result,
                                    quantityToGift = 1
                                )
                                Toast.makeText(context, "🎁 Darováno dívce ${character.name}!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Player doesn't have enough in inventory; offer direct gold purchase & gift
                            val totalCost = slot.item.goldCost * count
                            if (player.gold >= totalCost) {
                                val buyResult = engine.purchaseGiftToInventory(slot.item.id, count)
                                if (buyResult.first) {
                                    val result = engine.giveCollectibleGift(character.id, slot.item.id, count)
                                    if (result != null) {
                                        uiState = uiState.copy(
                                            lastGiftResult = result,
                                            quantityToGift = 1
                                        )
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Nemáš dostatek tohoto předmětu ani zlata!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else if (onDirectGiftLegacy != null) {
                        onDirectGiftLegacy(slot.item.id)
                    }
                },
                onQuickBuy = { count ->
                    if (engine != null) {
                        val buyRes = engine.purchaseGiftToInventory(slot.item.id, count)
                        Toast.makeText(context, buyRes.second, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // MERCHANT QUICK PURCHASE DIALOG
    if (uiState.showPurchaseDialog) {
        GiftMerchantDialog(
            player = player,
            character = character,
            onDismiss = { uiState = uiState.copy(showPurchaseDialog = false) },
            onPurchaseItem = { gift, count ->
                if (engine != null) {
                    val res = engine.purchaseGiftToInventory(gift.id, count)
                    Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/**
 * Single grid card displaying a collectible gift with rarity styling, count badge, and favorite indicator.
 */
@Composable
fun GiftItemGridCard(
    slot: GiftInventorySlot,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val isOwned = slot.quantity > 0
    val rarity = slot.item.rarity

    val borderStroke = when {
        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        isFavorite && isOwned -> BorderStroke(1.5.dp, Color(0xFFFF4081))
        isFavorite -> BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f))
        else -> BorderStroke(1.dp, rarity.composeColor.copy(alpha = if (isOwned) 0.5f else 0.2f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else if (isOwned) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon with subtle glow if favorite
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFavorite) Color(0xFFFF4081).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                ) {
                    Text(
                        text = slot.item.icon,
                        fontSize = 24.sp
                    )
                }

                // Item Name
                Text(
                    text = slot.item.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = if (isOwned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                // Rarity / Affinity Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = rarity.composeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+${slot.item.baseAffinity} 💖",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = rarity.composeColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Top-Right Quantity Badge
            if (isOwned) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "${slot.quantity}x",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "0x",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            // Top-Left Favorite Star
            if (isFavorite) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF4081),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 8.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom inspection panel when a gift slot is clicked, allowing stat preview, quantity selection, and gifting.
 */
@Composable
fun GiftInspectionBottomPanel(
    slot: GiftInventorySlot,
    character: Character,
    player: Player,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onGiftConfirmed: (Int) -> Unit,
    onQuickBuy: (Int) -> Unit
) {
    val isOwned = slot.quantity > 0
    val isFavorite = slot.item.isFavoriteOf(character.archetypeId)
    val totalAffinityPerItem = slot.item.calculateTotalAffinity(character.archetypeId)
    val totalAffinity = totalAffinityPerItem * quantity
    val totalLoyalty = slot.item.calculateLoyaltyGain(character.archetypeId) * quantity
    val totalDesire = slot.item.calculateDesireGain(character.archetypeId) * quantity
    val totalTrust = slot.item.calculateTrustGain(character.archetypeId) * quantity

    val maxCanAffordOrOwned = if (isOwned) slot.quantity else (player.gold / slot.item.goldCost.coerceAtLeast(1))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row with Name, Category, and Favorite Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(slot.item.icon, fontSize = 24.sp)
                    Column {
                        Text(
                            text = slot.item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${slot.item.category.displayName} • ${slot.item.rarity.title}",
                            fontSize = 11.sp,
                            color = slot.item.rarity.composeColor
                        )
                    }
                }

                if (isFavorite) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF4081).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFF4081))
                    ) {
                        Text(
                            text = "⭐ Oblíbený dar (+50% bonus)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4081),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Description
            Text(
                text = slot.item.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Stat Gain Breakdown Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE91E63).copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("+$totalAffinity", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63), fontSize = 12.sp)
                        Text("💖 Náklonnost", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("+$totalLoyalty", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                        Text("🛡️ Loajalita", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFF4081).copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("+$totalDesire", fontWeight = FontWeight.Bold, color = Color(0xFFFF4081), fontSize = 12.sp)
                        Text("🔥 Touha", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF2196F3).copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("+$totalTrust", fontWeight = FontWeight.Bold, color = Color(0xFF2196F3), fontSize = 12.sp)
                        Text("🤝 Důvěra", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            // Quantity Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOwned) "V inventáři: ${slot.quantity}x" else "Nemáš v inventáři (Cena: ${slot.item.goldCost} zlata)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOwned) MaterialTheme.colorScheme.primary else Color(0xFFFFD700)
                )

                // Quantity Counter Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1, 5, 10).forEach { qtyChoice ->
                        val canSelect = if (isOwned) slot.quantity >= qtyChoice else player.gold >= (slot.item.goldCost * qtyChoice)
                        SuggestionChip(
                            onClick = { onQuantityChange(qtyChoice) },
                            label = { Text("${qtyChoice}x", fontSize = 10.sp) },
                            border = if (quantity == qtyChoice) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (quantity == qtyChoice) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    if (isOwned && slot.quantity > 1) {
                        SuggestionChip(
                            onClick = { onQuantityChange(slot.quantity) },
                            label = { Text("MAX (${slot.quantity}x)", fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (quantity == slot.quantity) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // ACTION BUTTONS
            if (isOwned) {
                Button(
                    onClick = { onGiftConfirmed(quantity.coerceAtMost(slot.quantity)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) Color(0xFFD81B60) else Color(0xFF8E24AA)
                    )
                ) {
                    Text(
                        text = "🎁 Darovat $quantity× ${slot.item.name} (+${totalAffinity} 💖)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                val canAffordDirect = player.gold >= (slot.item.goldCost * quantity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onQuickBuy(quantity) },
                        enabled = canAffordDirect,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🛍️ Koupit ${quantity}x (${slot.item.goldCost * quantity} 🪙)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onGiftConfirmed(quantity) },
                        enabled = canAffordDirect,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
                    ) {
                        Text("🪙 Koupit & Darovat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Celebratory reaction banner displayed when a gift is given.
 */
@Composable
fun GiftReactionCard(
    result: GiftActionResult,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.leveledUp) Color(0xFF4A148C) else Color(0xFF1E1B2E)
        ),
        border = BorderStroke(
            1.5.dp,
            if (result.leveledUp) Color(0xFFFFD700) else Color(0xFFFF4081)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    Text(if (result.leveledUp) "🌟" else "💖", fontSize = 18.sp)
                    Text(
                        text = if (result.leveledUp) "Úroveň vztahu zvýšena na ${result.newAffinityLevel}!" else "Dar úspěšně předán!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (result.leveledUp) Color(0xFFFFD700) else Color(0xFFFF80AB)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }

            // Character reaction dialogue
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💬 ${result.character.name}: „${result.dialogueResponse}“",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Gains recap
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+${result.affinityGained} 💖 Náklonnost", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF80AB))
                Text("+${result.loyaltyGained} 🛡️ Loajalita", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                Text("+${result.desireGained} 🔥 Touha", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
            }
        }
    }
}

/**
 * Merchant dialog allowing the player to purchase collectible gifts in bulk with gold.
 */
@Composable
fun GiftMerchantDialog(
    player: Player,
    character: Character,
    onDismiss: () -> Unit,
    onPurchaseItem: (GiftItemData, Int) -> Unit
) {
    val catalog = remember { GiftInventoryCatalog.ALL_GIFTS }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛍️", fontSize = 24.sp)
                        Column {
                            Text("Kupec s dary a kuriozitami", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("K dispozici: ${player.gold} zlata", fontSize = 11.sp, color = Color(0xFFFFD700))
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                HorizontalDivider()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(catalog) { gift ->
                        val isFav = gift.isFavoriteOf(character.archetypeId)
                        val canAfford = player.gold >= gift.goldCost

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(gift.icon, fontSize = 22.sp)
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(gift.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            if (isFav) {
                                                Text("⭐", fontSize = 10.sp)
                                            }
                                        }
                                        Text(gift.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("+${gift.baseAffinity} Náklonnost • +${gift.loyaltyBonus} Loajalita", fontSize = 9.sp, color = gift.rarity.composeColor)
                                    }
                                }

                                Button(
                                    onClick = { onPurchaseItem(gift, 1) },
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
                                ) {
                                    Text("${gift.goldCost} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
