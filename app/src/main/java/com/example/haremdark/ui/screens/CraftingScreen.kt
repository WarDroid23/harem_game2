package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.models.AlchemyRecipe
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.Character
import com.example.haremdark.models.InventoryItem

private data class ExchangeOption(
    val id: String,
    val title: String,
    val goldCost: Int,
    val darkCost: Int,
    val materialKey: String,
    val grantAmount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftingScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = gameState.player
    val haremCharacters = gameState.characters
    val recipes = GameContent.ALCHEMY_RECIPES
    val resources = player.craftingResources

    var selectedCategoryTab by remember { mutableIntStateOf(0) } // 0: Zbraně, 1: Zbroje, 2: Doplňky, 3: Alchymie, 4: Dary, 5: Úlomky
    var showResourceExchangeModal by remember { mutableStateOf(false) }
    var selectedItemToEquip by remember { mutableStateOf<InventoryItem?>(null) }
    var showEquipMemberModal by remember { mutableStateOf(false) }

    // Material names mapping
    val materialNames = mapOf(
        "temny_strep" to "💎 Temné střepy",
        "mana_esence" to "✨ Mana esence",
        "zelezna_ruda" to "⚙️ Železná ruda",
        "drevohorec" to "🪵 Dřevohorec",
        "mesicni_prach" to "🌙 Měsíční prach",
        "draci_krev" to "🩸 Dračí krev",
        "krystal" to "💎 Krystaly"
    )

    val filteredRecipes = remember(recipes, selectedCategoryTab) {
        when (selectedCategoryTab) {
            0 -> recipes.filter { it.resultItem.equipSlot == "weapon" }
            1 -> recipes.filter { it.resultItem.equipSlot == "armor" }
            2 -> recipes.filter { it.resultItem.equipSlot == "accessory" }
            3 -> recipes.filter { it.resultItem.category == "potion" }
            4 -> recipes.filter { it.resultItem.category == "gift" || it.resultItem.category == "artifact" }
            else -> recipes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "⚒️ Kovárna, Alchymie & Výroba Výbavy",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Kování unikátních zbraní, zbrojí a doplňků z temných střepů",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160A24)
                )
            )
        },
        containerColor = Color(0xFF0F0618)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TOP RESOURCE OVERVIEW & ACTIONS BAR ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E0E32),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "📦 Tvé Suroviny & Esence:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            ResourceChip("💰 Zlato", player.gold.toString(), Color(0xFFFFC107))
                        }
                        item {
                            ResourceChip("🔮 Temná energie", "${player.darkEnergy}/${player.maxDarkEnergy}", Color(0xFF9C27B0))
                        }
                        item {
                            ResourceChip("💎 Temné střepy", (resources["temny_strep"] ?: 0).toString(), Color(0xFFE91E63))
                        }
                        item {
                            ResourceChip("✨ Mana esence", (resources["mana_esence"] ?: 0).toString(), Color(0xFF03A9F4))
                        }
                        item {
                            ResourceChip("⚙️ Železná ruda", (resources["zelezna_ruda"] ?: 0).toString(), Color(0xFF9E9E9E))
                        }
                        item {
                            ResourceChip("🪵 Dřevohorec", (resources["drevohorec"] ?: 0).toString(), Color(0xFF8D6E63))
                        }
                        item {
                            ResourceChip("🌙 Měsíční prach", (resources["mesicni_prach"] ?: 0).toString(), Color(0xFFBA68C8))
                        }
                        item {
                            ResourceChip("🩸 Dračí krev", (resources["draci_krev"] ?: 0).toString(), Color(0xFFF44336))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showResourceExchangeModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF9C27B0)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE1BEE7))
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🛒 Získat Suroviny", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                engine.quickSalvage()
                                SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                                HapticManager.vibrateHeavy()
                                Toast.makeText(context, "Běžné předměty byly recyklovány na materiál!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("♻️ Recyklace", fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- CATEGORY TABS ---
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryTab,
                containerColor = Color(0xFF160A24),
                contentColor = Color(0xFFFFD700),
                edgePadding = 12.dp,
                divider = {}
            ) {
                val tabs = listOf(
                    "⚔️ Zbraně",
                    "🛡️ Zbroje",
                    "💍 Doplňky",
                    "🧪 Alchymie",
                    "🎁 Dary",
                    "🧩 Úlomky"
                )
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategoryTab == index,
                        onClick = {
                            selectedCategoryTab = index
                            HapticManager.vibrateClick()
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedCategoryTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategoryTab == index) Color(0xFFFFD700) else Color.LightGray,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF381E52), thickness = 1.dp)

            // --- MAIN CRAFTING LIST CONTENT ---
            if (selectedCategoryTab == 5) {
                // Fragment Forge Tab
                val fragments = engine.getAvailableFragmentsWithProgress()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF26123D)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🧩", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Kovárna vzácných úlomků", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text("Sbírej úlomky z dungeonů a bitev pro zhotovení unikátních artefaktů.", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    if (fragments.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Zatím nemáš žádné úlomky výbavy v inventáři.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(fragments) { frag ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0E32)),
                                border = BorderStroke(1.dp, Color(0xFF6A1B9A))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(frag.targetEquipmentIcon, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(frag.targetEquipmentName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                            Text("Složitost: ${frag.rarity}", fontSize = 11.sp, color = getRarityColor(frag.rarity))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (frag.currentCount >= frag.requiredCount) Color(0xFF2E7D32) else Color(0xFF424242)
                                        ) {
                                            Text(
                                                "${frag.currentCount} / ${frag.requiredCount}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(frag.description, fontSize = 12.sp, color = Color.LightGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Statistiky: ${frag.statPreview}", fontSize = 11.sp, color = Color(0xFF81D4FA))

                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { (frag.currentCount.toFloat() / frag.requiredCount.toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFFAB47BC),
                                        trackColor = Color(0xFF381E52)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val (success, msg) = engine.forgeEquipmentFromFragments(frag.id)
                                            if (success) {
                                                SoundEffectManager.playCombat(com.example.haremdark.domain.CombatSound.CRITICAL_SUPERNOVA)
                                                HapticManager.vibrateHeavy()
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        },
                                        enabled = frag.currentCount >= frag.requiredCount,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                                    ) {
                                        Text("⚡ Ukovat z úlomků")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Standard Recipes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredRecipes.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("V této kategorii nejsou k dispozici žádné recepty.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(filteredRecipes) { recipe ->
                            val resultItem = recipe.resultItem
                            val canAffordGold = player.gold >= recipe.goldCost
                            val canAffordDark = player.darkEnergy >= recipe.darkCost
                            val materialsSatisfied = recipe.materials.all { (matId, reqAmount) ->
                                (resources[matId] ?: 0) >= reqAmount
                            }
                            val canCraft = canAffordGold && canAffordDark && materialsSatisfied

                            val existingCountInInventory = player.items.firstOrNull { it.id == resultItem.id }?.count ?: 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B2E)),
                                border = BorderStroke(1.dp, if (canCraft) Color(0xFF8E24AA) else Color(0xFF381E52))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF2A153E),
                                            border = BorderStroke(1.dp, getRarityColor(resultItem.rarity)),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(resultItem.icon, fontSize = 24.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    recipe.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 15.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = getRarityColor(resultItem.rarity).copy(alpha = 0.2f),
                                                    border = BorderStroke(1.dp, getRarityColor(resultItem.rarity))
                                                ) {
                                                    Text(
                                                        resultItem.rarity,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = getRarityColor(resultItem.rarity)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                recipe.description,
                                                fontSize = 12.sp,
                                                color = Color.LightGray,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Effect / Combat Preview
                                    if (resultItem.effectDescription.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF231038),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "⚡ Účinky: ${resultItem.effectDescription}",
                                                modifier = Modifier.padding(8.dp),
                                                fontSize = 12.sp,
                                                color = Color(0xFF81D4FA),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Material & Cost Breakdown
                                    Text("Potřebné suroviny:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD700))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Gold Cost
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("💰 Zlato", fontSize = 12.sp, color = Color.White)
                                            Text(
                                                "${player.gold} / ${recipe.goldCost}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (canAffordGold) Color(0xFF81C784) else Color(0xFFE57373)
                                            )
                                        }

                                        // Dark Energy Cost
                                        if (recipe.darkCost > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🔮 Temná energie", fontSize = 12.sp, color = Color.White)
                                                Text(
                                                    "${player.darkEnergy} / ${recipe.darkCost}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (canAffordDark) Color(0xFF81C784) else Color(0xFFE57373)
                                                )
                                            }
                                        }

                                        // Material Map
                                        recipe.materials.forEach { (matId, reqAmount) ->
                                            val currentMat = resources[matId] ?: 0
                                            val isOk = currentMat >= reqAmount
                                            val displayName = materialNames[matId] ?: matId

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(displayName, fontSize = 12.sp, color = Color.White)
                                                Text(
                                                    "$currentMat / $reqAmount ${if (isOk) "✅" else "❌"}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOk) Color(0xFF81C784) else Color(0xFFE57373)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val (success, msg) = engine.craftItem(recipe.id)
                                                if (success) {
                                                    SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                                                    HapticManager.vibrateHeavy()
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            enabled = canCraft,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF8E24AA),
                                                disabledContainerColor = Color(0xFF381E52)
                                            )
                                        ) {
                                            Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (resultItem.equipSlot != null) "🔨 Ukovat" else "🧪 Vyrobit", fontSize = 13.sp)
                                        }

                                        if (resultItem.equipSlot != null && existingCountInInventory > 0) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedItemToEquip = resultItem
                                                    showEquipMemberModal = true
                                                    HapticManager.vibrateClick()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784))
                                            ) {
                                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("👤 Vybavit ($existingCountInInventory)", fontSize = 12.sp)
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

    // --- MODAL: RESOURCE EXCHANGE & TRADING ---
    if (showResourceExchangeModal) {
        AlertDialog(
            onDismissRequest = { showResourceExchangeModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛒 Nákup & Směna Surovin", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Chybí ti suroviny pro kování? Můžeš si je zakoupit ze statního pokladu nebo výměnou za Temnou energii.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    val exchangeOptions = listOf(
                        ExchangeOption("temny_strep", "💎 Temný střep x5", 100, 10, "temny_strep", 5),
                        ExchangeOption("mana_esence", "✨ Mana esence x5", 80, 15, "mana_esence", 5),
                        ExchangeOption("zelezna_ruda", "⚙️ Železná ruda x10", 60, 0, "zelezna_ruda", 10),
                        ExchangeOption("drevohorec", "🪵 Dřevohorec x15", 50, 0, "drevohorec", 15),
                        ExchangeOption("mesicni_prach", "🌙 Měsíční prach x3", 150, 20, "mesicni_prach", 3),
                        ExchangeOption("draci_krev", "🩸 Dračí krev x1", 200, 25, "draci_krev", 1)
                    )

                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(exchangeOptions) { option ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF231038)),
                                border = BorderStroke(1.dp, Color(0xFF4A148C))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(option.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("Cena: ${option.goldCost} Zlata ${if (option.darkCost > 0) "+ ${option.darkCost} TE" else ""}", fontSize = 11.sp, color = Color(0xFFFFC107))
                                    }

                                    Button(
                                        onClick = {
                                            if (player.gold >= option.goldCost && player.darkEnergy >= option.darkCost) {
                                                engine.updateState { state ->
                                                    val newResources = state.player.craftingResources.toMutableMap()
                                                    newResources[option.materialKey] = (newResources[option.materialKey] ?: 0) + option.grantAmount
                                                    val newP = state.player.copy(
                                                        gold = state.player.gold - option.goldCost,
                                                        darkEnergy = (state.player.darkEnergy - option.darkCost).coerceAtLeast(0),
                                                        craftingResources = newResources
                                                    )
                                                    state.copy(player = newP)
                                                }
                                                SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                                                HapticManager.vibrateClick()
                                                Toast.makeText(context, "Zakoupeno ${option.title}!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Nedostatek zdrojů na nákup!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                                    ) {
                                        Text("Koupit", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showResourceExchangeModal = false }) {
                    Text("Zavřít", color = Color(0xFFFFD700))
                }
            },
            containerColor = Color(0xFF1B0B2E)
        )
    }

    // --- MODAL: DIRECT EQUIP TO HAREM MEMBER ---
    if (showEquipMemberModal && selectedItemToEquip != null) {
        val item = selectedItemToEquip!!
        AlertDialog(
            onDismissRequest = {
                showEquipMemberModal = false
                selectedItemToEquip = null
            },
            title = {
                Text("👤 Vybavit ${item.name}", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Vyber členku harému, které chceš tuto výbavu nasadit:", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (haremCharacters.isEmpty()) {
                        Text("Nemáš v harému žádné aktivní dívky.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(haremCharacters) { girl ->
                                val slotId = item.equipSlot ?: "weapon"
                                val currentEquipped = girl.equipment[slotId]

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            engine.equipItemToCharacter(girl.id, item.id, slotId)
                                            SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                                            HapticManager.vibrateClick()
                                            Toast.makeText(context, "${item.name} nasazeno dívce ${girl.name}!", Toast.LENGTH_SHORT).show()
                                            showEquipMemberModal = false
                                            selectedItemToEquip = null
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231038)),
                                    border = BorderStroke(1.dp, Color(0xFF6A1B9A))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💃", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(girl.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                            Text(
                                                "Nasadit do slotu $slotId ${if (currentEquipped != null) "(Nahradí ${currentEquipped.name})" else "(Prázdné)"}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF81C784))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEquipMemberModal = false
                    selectedItemToEquip = null
                }) {
                    Text("Zrušit", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1B0B2E)
        )
    }
}

@Composable
private fun ResourceChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2A153E),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private fun getRarityColor(rarity: String): Color {
    return when (rarity.lowercase()) {
        "legendární", "legendary" -> Color(0xFFFFC107)
        "epický", "epic" -> Color(0xFFAB47BC)
        "vzácný", "rare" -> Color(0xFF66BB6A)
        else -> Color(0xFFB0BEC5)
    }
}
