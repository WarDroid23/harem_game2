package com.example.haremdark.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InventoryItem

/**
 * Definition of an interactive equipment socket.
 */
enum class GearSlotType(
    val slotKey: String,
    val title: String,
    val icon: String,
    val acceptedCategories: List<String>
) {
    WEAPON("weapon", "Zbraň", "⚔️", listOf("weapon", "equipment")),
    ARMOR("armor", "Zbroj & Oděv", "🛡️", listOf("armor", "equipment")),
    ACCESSORY("accessory", "Doplněk & Šperk", "💍", listOf("accessory", "equipment", "gift")),
    ARTIFACT("artifact", "Relikvie & Artefakt", "🔮", listOf("artifact", "relic", "quest", "equipment"))
}

/**
 * Slot-Based & Drag/Assign Equipment Screen allowing players to equip items
 * found in events, quests, and alchemy to boost character attributes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    gameState: GameSave,
    engine: GameEngine,
    initialCharacterId: String? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val characters = gameState.characters
    val player = gameState.player

    var selectedCharId by remember { mutableStateOf(initialCharacterId ?: characters.firstOrNull()?.id) }
    val selectedCharacter = remember(selectedCharId, characters) {
        characters.firstOrNull { it.id == selectedCharId } ?: characters.firstOrNull()
    }

    var activeSlotFilter by remember { mutableStateOf<GearSlotType?>(null) }
    var itemForDetailDialog by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedInventoryItem by remember { mutableStateOf<InventoryItem?>(null) }

    // Floating stat change banner state
    var recentStatBoostMessage by remember { mutableStateOf<String?>(null) }

    // Filter available inventory items suitable for equipment
    val equipableInventoryItems = remember(player.items, activeSlotFilter) {
        player.items.filter { item ->
            val slot = item.equipSlot?.lowercase() ?: ""
            val cat = item.category.lowercase()
            val isGear = slot.isNotBlank() || cat in listOf("equipment", "weapon", "armor", "accessory", "artifact", "relic") ||
                item.combatBonus > 0 || item.defenseBonus > 0 || item.hpBonus > 0

            if (!isGear) return@filter false

            if (activeSlotFilter == null) true
            else {
                when (activeSlotFilter!!) {
                    GearSlotType.WEAPON -> slot == "weapon" || cat == "weapon" || item.combatBonus > 0
                    GearSlotType.ARMOR -> slot == "armor" || cat == "armor" || item.defenseBonus > 0
                    GearSlotType.ACCESSORY -> slot == "accessory" || cat == "accessory" || (item.hpBonus > 0 && slot != "armor")
                    GearSlotType.ARTIFACT -> slot == "artifact" || slot == "relic" || cat in listOf("artifact", "relic", "quest") || item.synergyBuffValue > 0
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "🛡️ Vybavení Postav (Sloty)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                        Text(
                            "Nasaď výstroj nalezenou v událostech a posil statistiky dívek",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onBack()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                actions = {
                    // Auto-Equip Best Gear Button
                    if (selectedCharacter != null) {
                        IconButton(
                            onClick = {
                                HapticManager.vibrateHeavy()
                                autoEquipBestGear(selectedCharacter, player.items, engine)
                                recentStatBoostMessage = "⚡ Nasazena optimální výstroj!"
                                Toast.makeText(context, "Optimální výstroj byla nasazena!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Automaticky nejlepší", tint = Color(0xFFFFD700))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140B1A)
                )
            )
        }
    ) { padding ->
        if (characters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nemáš žádné společnice v harému.", color = Color.Gray)
            }
            return@Scaffold
        }

        val currentChar = selectedCharacter ?: characters.first()

        // Calculated active combat stats with equipment
        val hpBonus = currentChar.equipment.values.filterNotNull().sumOf { it.hpBonus }
        val combatBonus = currentChar.equipment.values.filterNotNull().sumOf { it.combatBonus }
        val defBonus = currentChar.equipment.values.filterNotNull().sumOf { it.defenseBonus }

        val totalStr = currentChar.strength + (currentChar.skills["combat"] ?: 0) + combatBonus + (currentChar.attributes.strength * 2)
        val totalDef = (currentChar.skills["defense"] ?: 0) + defBonus + currentChar.attributes.defense
        val totalHp = currentChar.maxHp + hpBonus
        val totalPower = (totalStr * 4) + (totalDef * 3) + (totalHp / 2) + (currentChar.level * 15)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0715))
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1. CHARACTER SELECTOR CAROUSEL ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(characters) { char ->
                        val isSelected = (char.id == currentChar.id)
                        val portraitRes = StaticData.getPortraitForArchetype(char.archetypeId)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF6A1B9A).copy(alpha = 0.5f) else Color(0xFF1B0F24),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    HapticManager.vibrateClick()
                                    selectedCharId = char.id
                                    activeSlotFilter = null
                                }
                                .testTag("char_select_${char.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(portraitRes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = char.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        error = { Text(char.statusIcon, fontSize = 16.sp) }
                                    )
                                }

                                Column {
                                    Text(
                                        text = char.name,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Lvl ${char.level} • ${char.role.take(14)}",
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. POWER RATING & STATS DASHBOARD ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("equipment_power_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C0E28)),
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFD700).copy(alpha = 0.7f), Color(0xFF8E24AA).copy(alpha = 0.3f))
                        )
                    )
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
                            Column {
                                Text(
                                    text = "${currentChar.name} — Bojová Síla",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Hodnocení zbraní, zbroje a relikvií",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                            }

                            // Total Power Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF7B1FA2),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("⚡", fontSize = 12.sp)
                                    Text(
                                        text = "$totalPower BP",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }

                        // Stat Bars Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatDisplayBadge(
                                label = "Útok / Síla",
                                value = "$totalStr",
                                bonus = if (combatBonus > 0) "+$combatBonus" else null,
                                icon = "⚔️",
                                color = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f)
                            )
                            StatDisplayBadge(
                                label = "Obrana",
                                value = "$totalDef",
                                bonus = if (defBonus > 0) "+$defBonus" else null,
                                icon = "🛡️",
                                color = Color(0xFF64B5F6),
                                modifier = Modifier.weight(1f)
                            )
                            StatDisplayBadge(
                                label = "Max Zdraví",
                                value = "$totalHp",
                                bonus = if (hpBonus > 0) "+$hpBonus" else null,
                                icon = "❤️",
                                color = Color(0xFF81C784),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- 3. THE 4 INTERACTIVE EQUIPMENT SOCKETS (PAPER DOLL) ---
            item {
                Text(
                    text = "📦 Aktivní Výstroj (Klepnutím na slot otevřeš nabídku)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GearSlotType.entries.forEach { slotType ->
                        val equippedItem = currentChar.equipment[slotType.slotKey]
                        val isSlotActive = (activeSlotFilter == slotType)

                        EquipmentSlotRow(
                            slotType = slotType,
                            item = equippedItem,
                            isSelected = isSlotActive,
                            onSlotClick = {
                                HapticManager.vibrateClick()
                                activeSlotFilter = if (isSlotActive) null else slotType
                            },
                            onUnequip = {
                                HapticManager.vibrateHeavy()
                                engine.unequipItemFromCharacter(currentChar.id, slotType.slotKey)
                                Toast.makeText(context, "Předmět odebrán do inventáře", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // --- 4. INVENTORY GEAR SELECTION DRAWER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeSlotFilter != null) "🎒 Dostupné předměty pro: ${activeSlotFilter!!.title}" else "🎒 Veškerá výbava v batohu (${equipableInventoryItems.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF80AB)
                    )

                    if (activeSlotFilter != null) {
                        TextButton(
                            onClick = { activeSlotFilter = null },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Zobrazit vše", fontSize = 10.sp, color = Color(0xFFFFD700))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (equipableInventoryItems.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF14081E),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "V inventáři nemáš žádné vhodné předměty pro tento slot. Prozkoumej události nebo navštiv alchymii a dražbu!",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        equipableInventoryItems.forEach { item ->
                            InventoryEquipCard(
                                item = item,
                                targetCharacter = currentChar,
                                onEquip = { targetSlot ->
                                    HapticManager.vibrateHeavy()
                                    engine.equipItemToCharacter(currentChar.id, item.id, targetSlot)
                                    Toast.makeText(context, "${item.name} nasazen do slotu $targetSlot!", Toast.LENGTH_SHORT).show()
                                },
                                onInspect = {
                                    itemForDetailDialog = item
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Item Detailed Lore & Stats Dialog
    if (itemForDetailDialog != null) {
        val item = itemForDetailDialog!!
        ItemDetailModal(
            item = item,
            onDismiss = { itemForDetailDialog = null },
            onEquipDirectly = { slotKey ->
                if (selectedCharacter != null) {
                    engine.equipItemToCharacter(selectedCharacter.id, item.id, slotKey)
                    itemForDetailDialog = null
                    Toast.makeText(context, "${item.name} byl nasazen!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/**
 * Individual Socket Row (Paper Doll Slot).
 */
@Composable
private fun EquipmentSlotRow(
    slotType: GearSlotType,
    item: InventoryItem?,
    isSelected: Boolean,
    onSlotClick: () -> Unit,
    onUnequip: () -> Unit
) {
    val isEquipped = (item != null)
    val rarityColor = if (isEquipped) {
        when (item!!.rarity.lowercase()) {
            "legendární" -> Color(0xFFFFD700)
            "epický" -> Color(0xFFE040FB)
            "vzácný" -> Color(0xFF00E5FF)
            else -> Color(0xFF81C784)
        }
    } else Color.Gray

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2C163A) else if (isEquipped) Color(0xFF1B0E24) else Color(0xFF13081A),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFFFFD700) else if (isEquipped) rarityColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSlotClick() }
            .testTag("gear_slot_${slotType.slotKey}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Slot Socket Icon
            Surface(
                shape = CircleShape,
                color = if (isEquipped) rarityColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, if (isEquipped) rarityColor else Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item?.icon ?: slotType.icon,
                        fontSize = if (isEquipped) 20.sp else 16.sp
                    )
                }
            }

            // Slot Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = slotType.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )

                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = rarityColor.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = item!!.rarity,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = rarityColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (isEquipped) {
                    Text(
                        text = item!!.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Stat bonuses summary
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.combatBonus > 0) Text("⚔️ +${item.combatBonus} Útok", fontSize = 9.sp, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                        if (item.defenseBonus > 0) Text("🛡️ +${item.defenseBonus} Obrana", fontSize = 9.sp, color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold)
                        if (item.hpBonus > 0) Text("❤️ +${item.hpBonus} HP", fontSize = 9.sp, color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Prázdný slot (Klepnutím nasadíš)",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Unequip Action Button
            if (isEquipped) {
                IconButton(
                    onClick = onUnequip,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Odstrojit",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Nasadit",
                    tint = Color.LightGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Item Card in the Inventory Drawer ready to be equipped.
 */
@Composable
private fun InventoryEquipCard(
    item: InventoryItem,
    targetCharacter: Character,
    onEquip: (String) -> Unit,
    onInspect: () -> Unit
) {
    val detectedSlot = item.equipSlot?.lowercase() ?: when {
        item.combatBonus > 0 -> "weapon"
        item.defenseBonus > 0 -> "armor"
        item.hpBonus > 0 -> "accessory"
        else -> "artifact"
    }

    val currentEquipped = targetCharacter.equipment[detectedSlot]
    val combatDiff = item.combatBonus - (currentEquipped?.combatBonus ?: 0)
    val defDiff = item.defenseBonus - (currentEquipped?.defenseBonus ?: 0)
    val hpDiff = item.hpBonus - (currentEquipped?.hpBonus ?: 0)

    val rarityColor = when (item.rarity.lowercase()) {
        "legendární" -> Color(0xFFFFD700)
        "epický" -> Color(0xFFE040FB)
        "vzácný" -> Color(0xFF00E5FF)
        else -> Color(0xFF81C784)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF180D22),
        border = BorderStroke(0.8.dp, rarityColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("inventory_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = rarityColor.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, rarityColor),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.icon, fontSize = 18.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.source,
                        fontSize = 8.sp,
                        color = Color(0xFFFFD700)
                    )
                }

                // Stat Bonuses & Comparison preview
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.combatBonus > 0) {
                        Text(
                            text = "⚔️ +${item.combatBonus}" + if (combatDiff != 0) " (${if (combatDiff > 0) "+$combatDiff" else "$combatDiff"})" else "",
                            fontSize = 9.sp,
                            color = if (combatDiff > 0) Color(0xFF69F0AE) else Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (item.defenseBonus > 0) {
                        Text(
                            text = "🛡️ +${item.defenseBonus}" + if (defDiff != 0) " (${if (defDiff > 0) "+$defDiff" else "$defDiff"})" else "",
                            fontSize = 9.sp,
                            color = if (defDiff > 0) Color(0xFF69F0AE) else Color(0xFF81D4FA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (item.hpBonus > 0) {
                        Text(
                            text = "❤️ +${item.hpBonus}" + if (hpDiff != 0) " (${if (hpDiff > 0) "+$hpDiff" else "$hpDiff"})" else "",
                            fontSize = 9.sp,
                            color = if (hpDiff > 0) Color(0xFF69F0AE) else Color(0xFFA5D6A7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Equip Button
            Button(
                onClick = { onEquip(detectedSlot) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Nasadit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            }
        }
    }
}

@Composable
private fun StatDisplayBadge(
    label: String,
    value: String,
    bonus: String?,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF14081C),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("$icon $label", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
                if (bonus != null) {
                    Text("($bonus)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                }
            }
        }
    }
}

/**
 * Detailed Modal for item inspection and lore.
 */
@Composable
private fun ItemDetailModal(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onEquipDirectly: (String) -> Unit
) {
    val detectedSlot = item.equipSlot?.lowercase() ?: "weapon"
    val rarityColor = when (item.rarity.lowercase()) {
        "legendární" -> Color(0xFFFFD700)
        "epický" -> Color(0xFFE040FB)
        "vzácný" -> Color(0xFF00E5FF)
        else -> Color(0xFF81C784)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0D24)),
            border = BorderStroke(1.5.dp, rarityColor),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = rarityColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, rarityColor),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(item.icon, fontSize = 22.sp)
                            }
                        }

                        Column {
                            Text(item.name, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                            Text("${item.rarity} • Původ: ${item.source}", fontSize = 10.sp, color = rarityColor)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.LightGray)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Stat Bonuses list
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (item.combatBonus > 0) Text("⚔️ Bojový bonus: +${item.combatBonus} k síle úderu", fontSize = 11.sp, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                    if (item.defenseBonus > 0) Text("🛡️ Obranný bonus: +${item.defenseBonus} k absorpci zranění", fontSize = 11.sp, color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold)
                    if (item.hpBonus > 0) Text("❤️ Zdravotní bonus: +${item.hpBonus} k maximálnímu HP", fontSize = 11.sp, color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
                    if (item.effectDescription.isNotBlank()) Text("✨ Efekt: ${item.effectDescription}", fontSize = 10.sp, color = Color(0xFFFFD54F))
                }

                Button(
                    onClick = { onEquipDirectly(detectedSlot) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nasadit tento předmět", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                }
            }
        }
    }
}

/**
 * Calculates and equips the optimal gear items for a companion.
 */
private fun autoEquipBestGear(
    character: Character,
    inventory: List<InventoryItem>,
    engine: GameEngine
) {
    GearSlotType.entries.forEach { slotType ->
        val candidate = inventory
            .filter { item ->
                val slot = item.equipSlot?.lowercase() ?: ""
                val cat = item.category.lowercase()
                when (slotType) {
                    GearSlotType.WEAPON -> slot == "weapon" || cat == "weapon" || item.combatBonus > 0
                    GearSlotType.ARMOR -> slot == "armor" || cat == "armor" || item.defenseBonus > 0
                    GearSlotType.ACCESSORY -> slot == "accessory" || cat == "accessory" || (item.hpBonus > 0 && slot != "armor")
                    GearSlotType.ARTIFACT -> slot == "artifact" || slot == "relic" || cat in listOf("artifact", "relic") || item.synergyBuffValue > 0
                }
            }
            .maxByOrNull { (it.combatBonus * 4) + (it.defenseBonus * 3) + (it.hpBonus / 2) }

        if (candidate != null) {
            engine.equipItemToCharacter(character.id, candidate.id, slotType.slotKey)
        }
    }
}
