package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.DrugData
import com.example.haremdark.data.DrugDefinition
import com.example.haremdark.data.DrugIngredient
import com.example.haremdark.data.SourcingExpedition
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.ActiveDrugBuff
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InventoryItem

@Composable
fun DrugResourceManager(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    // 0: Posílení & Zásoby (Active Buffs & Stock)
    // 1: Syntéza substancí (Crafting)
    // 2: Zdroje surovin (Sourcing & Expeditions)

    var concubineDialogDrug by remember { mutableStateOf<DrugDefinition?>(null) }
    var concubineDialogTargetBuff by remember { mutableStateOf(false) } // true = activate buff, false = one-time use

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header Banner with Cartel & Lab Overview
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
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
                            text = "⚗️ Alchymistické dominium & Drogy",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Syntetizuj zakázané substance a udržuj dlouhodobá posílení harému.",
                            fontSize = 11.sp,
                            color = Color(0xFFCE93D8)
                        )
                    }

                    // Lab Overseer badge if assigned
                    val labOverseer = gameState.characters.firstOrNull { it.role == "Správkyně laboratoře" }
                    if (labOverseer != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF8E24AA).copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBA68C8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🧪", fontSize = 12.sp)
                                Text(
                                    text = "${labOverseer.name}: +50% výnos",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }
                    }
                }

                // Quick Resource Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ResourcePill("💰 ${gameState.player.gold}", "Zlato", Color(0xFFFFD700))
                    ResourcePill("🔮 ${gameState.player.darkEnergy}", "Temnota", Color(0xFFAB47BC))
                    ResourcePill("🌲 ${gameState.player.wood}", "Byliny", Color(0xFF66BB6A))
                    ResourcePill("✨ ${gameState.player.mana}", "Mana", Color(0xFF42A5F5))
                    ResourcePill("☠️ ${gameState.player.toxicity}/${gameState.player.maxToxicity}", "Toxicita", Color(0xFFEF5350))
                }
            }
        }

        // Segmented Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf(
                "💊 Posílení (${gameState.activeDrugBuffs.size})",
                "⚗️ Výroba",
                "🌿 Zdroje & Sběr"
            )
            tabs.forEachIndexed { index, title ->
                val selected = selectedSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedSubTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // SubTab Content
        when (selectedSubTab) {
            0 -> ActiveBuffsAndStockTab(gameState, engine) { drug, forBuff ->
                concubineDialogDrug = drug
                concubineDialogTargetBuff = forBuff
            }
            1 -> CraftingSynthesisTab(gameState, engine)
            2 -> SourcingAndHarvestTab(gameState, engine)
        }
    }

    // Dialog to pick concubine for drug administration / buff
    if (concubineDialogDrug != null) {
        val drug = concubineDialogDrug!!
        AlertDialog(
            onDismissRequest = { concubineDialogDrug = null },
            title = {
                Text(
                    text = "${drug.icon} ${if (concubineDialogTargetBuff) "Aktivovat posílení" else "Podat dávku"}: ${drug.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Vyber dívku, která má obdržet substanci:\n${drug.concubineEffectSummary}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(gameState.characters) { girl ->
                            val isAddicted = girl.zavislost > 30
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (concubineDialogTargetBuff) {
                                            val (ok, msg) = engine.activateDrugBuff(
                                                drugId = drug.id,
                                                targetType = "concubine",
                                                characterId = girl.id,
                                                autoRenew = true
                                            )
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            val (ok, msg) = engine.administerDrugToConcubine(drug.id, girl.id)
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                        concubineDialogDrug = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(girl.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "Touha: ${girl.touha}% • Loajalita: ${girl.loajalita}% • Závislost: ${girl.zavislost}%",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isAddicted) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE91E63).copy(alpha = 0.2f)) {
                                            Text("⚡ Závislá", fontSize = 9.sp, color = Color(0xFFE91E63), modifier = Modifier.padding(3.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { concubineDialogDrug = null }) {
                    Text("Zavřít")
                }
            }
        )
    }
}

@Composable
private fun ActiveBuffsAndStockTab(
    gameState: GameSave,
    engine: GameEngine,
    onAdministerToGirl: (DrugDefinition, Boolean) -> Unit
) {
    val context = LocalContext.current
    val drugItems = gameState.player.items.filter { it.category == "drug" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Section: Active Maintained Buffs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌟 Aktivní posílení charakterů (${gameState.activeDrugBuffs.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        if (gameState.activeDrugBuffs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Žádné aktivní posílení z enhancerů.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Aktivuj níže ze skladu dávku pro Pána nebo dívku z harému. Posílení vydrží 3 dny a při zapnutém udržování spotřebovává 1 dávku každé ráno.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(gameState.activeDrugBuffs) { buff ->
                val stockCount = gameState.player.items.firstOrNull { it.id == buff.drugId }?.count ?: 0
                val isStockLow = stockCount <= 1 && buff.autoRenew

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStockLow) Color(0xFF3E1C24) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = if (isStockLow) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350)) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(buff.icon, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "${buff.name} • ${buff.targetCharacterName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Kategorie: ${buff.category}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Remaining duration indicator
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "⏳ ${buff.remainingDays} / ${buff.maxDays} dny",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Effect description
                        Text(
                            text = buff.effectSummary,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Maintenance row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Denní udržování:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (buff.autoRenew) "ZAPNUTO" else "VYPNUTO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (buff.autoRenew) Color(0xFF66BB6A) else Color(0xFFB0BEC5)
                                    )
                                }
                                Text(
                                    text = "Zásoby ve skladu: $stockCount dávek",
                                    fontSize = 10.sp,
                                    color = if (stockCount > 0) Color(0xFFFFD700) else Color(0xFFEF5350)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        val (ok, msg) = engine.toggleBuffAutoRenew(buff.id)
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (buff.autoRenew) "Pozastavit" else "Udržovat", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val (ok, msg) = engine.dismissDrugBuff(buff.id)
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ukončit", fontSize = 11.sp, color = Color(0xFFEF5350))
                                }
                            }
                        }

                        if (isStockLow) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF5350).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "⚠️ Pozor: Zásoby substance jsou kriticky nízké ($stockCount dávek)! Pokud zítra dojdou, posílení vyprší a může nastat abstinenční syndrom.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFEF5350),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Drug Warehouse & Available Enhancers
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 Sklad hotových substancí",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "${drugItems.sumOf { it.count }} dávek celkem",
                    fontSize = 11.sp,
                    color = Color(0xFFFFD700)
                )
            }
        }

        if (drugItems.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Sklad substancí je prázdný.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Přejdi do záložky 'Výroba' a syntetizuj nové substance ze surovin.", fontSize = 11.sp, color = Color(0xFFCE93D8))
                    }
                }
            }
        } else {
            items(drugItems) { item ->
                val drugDef = DrugData.getDrugById(item.id)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(item.icon, fontSize = 24.sp)
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = drugDef?.category ?: "Narkotikum",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Skladem: ${item.count} ks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = item.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )

                        if (drugDef != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("👑 Efekt na Pána: ${drugDef.playerEffectSummary}", fontSize = 10.sp, color = Color(0xFFBA68C8))
                                    Text("💋 Efekt na Harém: ${drugDef.concubineEffectSummary}", fontSize = 10.sp, color = Color(0xFFF48FB1))
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Activate Buff for Lord
                            Button(
                                onClick = {
                                    val (ok, msg) = engine.activateDrugBuff(
                                        drugId = item.id,
                                        targetType = "player",
                                        autoRenew = true
                                    )
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Aktivovat pro Pána", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Apply to Concubine
                            FilledTonalButton(
                                onClick = {
                                    if (drugDef != null) {
                                        onAdministerToGirl(drugDef, true)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Aplikovat na dívku", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CraftingSynthesisTab(
    gameState: GameSave,
    engine: GameEngine
) {
    val context = LocalContext.current
    var batchCount by remember { mutableIntStateOf(1) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Batch selector header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dávkování syntézy:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Zvol počet dávek k současné výrobě", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 3, 5).forEach { b ->
                            val selected = batchCount == b
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                modifier = Modifier.clickable { batchCount = b }
                            ) {
                                Text(
                                    text = "${b}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // All Drug Recipes
        items(DrugData.ALL_DRUGS) { drug ->
            val totalGold = drug.goldCost * batchCount
            val totalDark = drug.darkCost * batchCount
            val totalWood = drug.woodCost * batchCount
            val totalMana = drug.manaCost * batchCount

            val canAffordGold = gameState.player.gold >= totalGold
            val canAffordDark = gameState.player.darkEnergy >= totalDark
            val canAffordWood = gameState.player.wood >= totalWood
            val canAffordMana = gameState.player.mana >= totalMana

            val ingredientChecks = drug.requiredIngredients.map { (ingId, neededPerBatch) ->
                val neededTotal = neededPerBatch * batchCount
                val ingDef = DrugData.getIngredientById(ingId)
                val available = gameState.player.items.firstOrNull { it.id == ingId }?.count ?: 0
                Triple(ingDef, neededTotal, available)
            }
            val hasAllIngredients = ingredientChecks.all { it.third >= it.second }
            val canCraft = canAffordGold && canAffordDark && canAffordWood && canAffordMana && hasAllIngredients

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(drug.icon, fontSize = 26.sp)
                            Column {
                                Text(drug.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = "Pouliční název: \"${drug.streetName}\" • ${drug.category}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCE93D8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Výnos: +${drug.yieldCount * batchCount} ks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = drug.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    // Resource costs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CostTag("💰 $totalGold", canAffordGold)
                        CostTag("🔮 $totalDark", canAffordDark)
                        CostTag("🌲 $totalWood", canAffordWood)
                        CostTag("✨ $totalMana", canAffordMana)
                    }

                    // Required raw ingredients
                    if (drug.requiredIngredients.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Požadované suroviny:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                ingredientChecks.forEach { (ingDef, needed, available) ->
                                    val ok = available >= needed
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${ingDef?.icon ?: "🌿"} ${ingDef?.name ?: "Surovina"}",
                                            fontSize = 11.sp,
                                            color = if (ok) MaterialTheme.colorScheme.onSurface else Color(0xFFEF5350)
                                        )
                                        Text(
                                            text = "$available / $needed ks",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ok) Color(0xFF66BB6A) else Color(0xFFEF5350)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Craft Button
                    Button(
                        onClick = {
                            val (ok, msg) = engine.craftDrug(drug.id, batchCount)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canCraft,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (canCraft) "Syntetizovat (${batchCount}x)" else "Nedostatek surovin / energie",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourcingAndHarvestTab(
    gameState: GameSave,
    engine: GameEngine
) {
    val context = LocalContext.current
    val hasHarvestedToday = gameState.player.greenhouseHarvestDay == gameState.player.day

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Section: Estate Greenhouse (Pěstírna panství)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(14.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🌱", fontSize = 24.sp)
                            Column {
                                Text("Botanická pěstírna panství", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Denní sklizeň léčivých a omamných bylin", fontSize = 10.sp, color = Color(0xFF81C784))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!hasHarvestedToday) Color(0xFF66BB6A).copy(alpha = 0.2f) else MaterialTheme.colorScheme.background
                        ) {
                            Text(
                                text = if (!hasHarvestedToday) "Připraveno ke sklizni" else "Sklizeno dnes",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!hasHarvestedToday) Color(0xFF66BB6A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "V pěstírně dozrává Horská očistná šalvěj, lístky Černého lotosu a Stínový mák. Při harmonii harému nad 80% vykvetou i krystaly Hvězdného prachu!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Button(
                        onClick = {
                            val (ok, msg) = engine.harvestEstateGarden()
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !hasHarvestedToday,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (!hasHarvestedToday) "Sklidit byliny z pěstírny" else "Dnešní sklizeň již proběhla (nové zítra)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Raw Ingredients Inventory Summary
        item {
            Text(
                text = "🌿 Zásoby alchymistických surovin",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DrugData.ALL_INGREDIENTS.forEach { ing ->
                        val count = gameState.player.items.firstOrNull { it.id == ing.id }?.count ?: 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(ing.icon, fontSize = 16.sp)
                                Column {
                                    Text(ing.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Naleziště: ${ing.sourceLocation}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("$count ks", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (count > 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedButton(
                                    onClick = {
                                        val (ok, msg) = engine.buyIngredientBlackMarket(ing.id, 2)
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Koupit 2x (${ing.buyPrice * 2} zl)", fontSize = 10.sp)
                                }
                            }
                        }
                        if (ing != DrugData.ALL_INGREDIENTS.last()) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Section: Sourcing Expeditions
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🗺️ Průzkumné výpravy za surovinami",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        items(DrugData.ALL_EXPEDITIONS) { exp ->
            val canAffordEnergy = gameState.player.sexEnergy >= exp.energyCost
            val canAffordDark = gameState.player.darkEnergy >= exp.darkCost

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(exp.icon, fontSize = 24.sp)
                            Column {
                                Text(exp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(exp.dangerSummary, fontSize = 10.sp, color = Color(0xFFFFB74D))
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                                Text("⚡ ${exp.energyCost} SE", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFAB47BC).copy(alpha = 0.2f)) {
                                Text("🔮 ${exp.darkCost} TE", fontSize = 10.sp, color = Color(0xFFAB47BC), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Text(exp.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))

                    // Possible yields
                    val yieldIcons = exp.possibleIngredients.mapNotNull { DrugData.getIngredientById(it) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Možný zisk:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        yieldIcons.forEach { ing ->
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.background) {
                                Text("${ing.icon} ${ing.name}", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val (ok, msg) = engine.startSourcingExpedition(exp.id)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canAffordEnergy && canAffordDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Vyslat výpravu", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourcePill(value: String, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun CostTag(text: String, isMet: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isMet) MaterialTheme.colorScheme.background else Color(0xFFEF5350).copy(alpha = 0.2f),
        border = if (!isMet) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350)) else null
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isMet) MaterialTheme.colorScheme.onSurface else Color(0xFFEF5350),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
