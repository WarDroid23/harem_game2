package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.R
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character

data class EvaluatedSynergyPerk(
    val id: String,
    val title: String,
    val icon: String,
    val requirementText: String,
    val bonusText: String,
    val description: String,
    val isUnlocked: Boolean,
    val characterName: String,
    val characterId: String,
    val characterImageRes: Int,
    val currentTraitSummary: String,
    val activationReason: String,
    val missingRequirementText: String,
    val progressPercent: Float
)

object SynergyEvaluator {

    fun evaluateCharacterSynergies(character: Character): List<EvaluatedSynergyPerk> {
        val perks = mutableListOf<EvaluatedSynergyPerk>()
        val imgRes = StaticData.getPortraitForArchetype(character.archetypeId)

        // 1. Pouto Věrnosti & Důvěry
        val trustVal = character.duvera
        val loyaltyVal = character.loajalita
        val t1Unlocked = trustVal >= 60 && loyaltyVal >= 60
        val t1Prog = ((trustVal.coerceAtMost(60) + loyaltyVal.coerceAtMost(60)).toFloat() / 120f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_vernost_duvera",
                title = "Pouto Věrnosti & Důvěry",
                icon = "🤝🛡️",
                requirementText = "Důvěra ≥ 60 & Loajalita ≥ 60",
                bonusText = "+15% Odolnost celé družiny & pasivní obnova 5 HP/s v boji",
                description = "Vysoká důvěra spojená s neotřesitelnou loajalitou vytváří ochranné pouto, které zaobluje zranění celé družiny.",
                isUnlocked = t1Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "Důvěra: $trustVal/100, Loajalita: $loyaltyVal/100",
                activationReason = "Splněno: Důvěra ($trustVal/60) a Loajalita ($loyaltyVal/60) dosáhly požadované hranice.",
                missingRequirementText = if (t1Unlocked) "" else buildMissingText(
                    listOf("Důvěra" to (60 - trustVal), "Loajalita" to (60 - loyaltyVal))
                ),
                progressPercent = t1Prog
            )
        )

        // 2. Železná Ukázněnost & Submise
        val obedienceVal = character.poslusnost
        val submissionVal = character.submisivita
        val t2Unlocked = obedienceVal >= 60 && submissionVal >= 60
        val t2Prog = ((obedienceVal.coerceAtMost(60) + submissionVal.coerceAtMost(60)).toFloat() / 120f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_ukaznenost_submise",
                title = "Železná Ukázněnost & Submise",
                icon = "⚡⛓️",
                requirementText = "Poslušnost ≥ 60 & Submise ≥ 60",
                bonusText = "+20% Poškození kritickým zásahem v boji & -10% cena akcí v komnatách",
                description = "Plná oddanost rozkazům a odevzdaná submisivita umožňují provádět útoky s neúprosnou přesností.",
                isUnlocked = t2Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "Poslušnost: $obedienceVal/100, Submise: $submissionVal/100",
                activationReason = "Splněno: Poslušnost ($obedienceVal/60) a Submise ($submissionVal/60) dosáhly požadované hranice.",
                missingRequirementText = if (t2Unlocked) "" else buildMissingText(
                    listOf("Poslušnost" to (60 - obedienceVal), "Submise" to (60 - submissionVal))
                ),
                progressPercent = t2Prog
            )
        )

        // 3. Plamen Vášně & Morálky
        val desireVal = character.touha
        val moraleVal = character.morale
        val t3Unlocked = desireVal >= 60 && moraleVal >= 60
        val t3Prog = ((desireVal.coerceAtMost(60) + moraleVal.coerceAtMost(60)).toFloat() / 120f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_plamen_vasne",
                title = "Plamen Vášně & Morálky",
                icon = "🔥✨",
                requirementText = "Touha ≥ 60 & Morálka ≥ 60",
                bonusText = "+15 Temné sexuální energie po každém boji & +10% šance na unikátní dialogy",
                description = "Spalující fyzická touha posílená vysokým duševním optimismem čerpá dodatečnou energii pro pána.",
                isUnlocked = t3Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "Touha: $desireVal/100, Morálka: $moraleVal/100",
                activationReason = "Splněno: Touha ($desireVal/60) a Morálka ($moraleVal/60) dosáhly požadované hranice.",
                missingRequirementText = if (t3Unlocked) "" else buildMissingText(
                    listOf("Touha" to (60 - desireVal), "Morálka" to (60 - moraleVal))
                ),
                progressPercent = t3Prog
            )
        )

        // 4. Vnímavá Oddanost
        val t4Unlocked = trustVal >= 70 && obedienceVal >= 70
        val t4Prog = ((trustVal.coerceAtMost(70) + obedienceVal.coerceAtMost(70)).toFloat() / 140f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_vnimava_oddanost",
                title = "Vnímavá Oddanost",
                icon = "🧠🎯",
                requirementText = "Důvěra ≥ 70 & Poslušnost ≥ 70",
                bonusText = "+25% Zisk zkušeností (XP) v bojích & +15% Rarita nacházených kořistí",
                description = "Hluboká důvěra s ukázněnou poslušností dovoluje otrokyni předvídat každý pánův požadavek s dokonalým načasováním.",
                isUnlocked = t4Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "Důvěra: $trustVal/100, Poslušnost: $obedienceVal/100",
                activationReason = "Splněno: Důvěra ($trustVal/70) a Poslušnost ($obedienceVal/70) odemkly tuto taktickou výhodu.",
                missingRequirementText = if (t4Unlocked) "" else buildMissingText(
                    listOf("Důvěra" to (70 - trustVal), "Poslušnost" to (70 - obedienceVal))
                ),
                progressPercent = t4Prog
            )
        )

        // 5. Divoká Bojová Touha
        val t5Unlocked = desireVal >= 70 && loyaltyVal >= 70
        val t5Prog = ((desireVal.coerceAtMost(70) + loyaltyVal.coerceAtMost(70)).toFloat() / 140f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_divoka_touha",
                title = "Divoká Bojová Touha",
                icon = "⚔️🔥",
                requirementText = "Touha ≥ 70 & Loajalita ≥ 70",
                bonusText = "+18% Rychlost útoků v boji & +10% Morálka celého harému",
                description = "Nespoutaná touha chránit a imponovat pánovi pohání zběsilou agresivitu na bojišti.",
                isUnlocked = t5Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "Touha: $desireVal/100, Loajalita: $loyaltyVal/100",
                activationReason = "Splněno: Touha ($desireVal/70) a Loajalita ($loyaltyVal/70) posilují bojový hněv.",
                missingRequirementText = if (t5Unlocked) "" else buildMissingText(
                    listOf("Touha" to (70 - desireVal), "Loajalita" to (70 - loyaltyVal))
                ),
                progressPercent = t5Prog
            )
        )

        // 6. Absolutní Odevzdání
        val t6Unlocked = submissionVal >= 80 && trustVal >= 80
        val t6Prog = ((submissionVal.coerceAtMost(80) + trustVal.coerceAtMost(80)).toFloat() / 160f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_absolutni_odevzdani",
                title = "Absolutní Odevzdání",
                icon = "👑🖤",
                requirementText = "Submise ≥ 80 & Důvěra ≥ 80",
                bonusText = "+30% Zisk zlata z výprav & Imunita družiny vůči efekturnímu strachu",
                description = "Bezpodmínečná odevzdanost a absolutní pocit bezpečí přinášejí pánovu impériu nepopsatelná bohatství.",
                isUnlocked = t6Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "Submise: $submissionVal/100, Důvěra: $trustVal/100",
                activationReason = "Splněno: Submise ($submissionVal/80) a Důvěra ($trustVal/80) dosáhly vrcholu oddanosti.",
                missingRequirementText = if (t6Unlocked) "" else buildMissingText(
                    listOf("Submise" to (80 - submissionVal), "Důvěra" to (80 - trustVal))
                ),
                progressPercent = t6Prog
            )
        )

        // 7. Harémová Harmonie
        val allTraits = listOf(trustVal, loyaltyVal, obedienceVal, desireVal, submissionVal, moraleVal)
        val t7Unlocked = allTraits.all { it >= 50 }
        val t7Prog = (allTraits.sumOf { it.coerceAtMost(50) }.toFloat() / 300f)
        perks.add(
            EvaluatedSynergyPerk(
                id = "${character.id}_haremi_harmonie",
                title = "Harémová Harmonie",
                icon = "👑✨",
                requirementText = "Všechny vlastnosti (Důvěra, Loajalita, Poslušnost, Touha, Submise, Morálka) ≥ 50",
                bonusText = "+10% K všem základním statistikám celé družiny (HP, Útok, Obrana, Krit)",
                description = "Vyvážený vývoj všech psychických a emocionálních stránek vytváří dokonalou vnitřní rovnováhu.",
                isUnlocked = t7Unlocked,
                characterName = character.name,
                characterId = character.id,
                characterImageRes = imgRes,
                currentTraitSummary = "D:$trustVal, L:$loyaltyVal, P:$obedienceVal, T:$desireVal, S:$submissionVal, M:$moraleVal",
                activationReason = "Splněno: Všechny emocionální vlastnosti otrokyně přesáhly hodnotu 50.",
                missingRequirementText = if (t7Unlocked) "" else "Chybí zvýšit nejnižší vlastnosti pod 50 bodů.",
                progressPercent = t7Prog
            )
        )

        return perks
    }

    private fun buildMissingText(missingMap: List<Pair<String, Int>>): String {
        val needed = missingMap.filter { it.second > 0 }
        if (needed.isEmpty()) return ""
        return "⚠️ Chybí k aktivaci: " + needed.joinToString(", ") { "${it.first} (+${it.second} bodů)" }
    }
}

@Composable
fun SynergyDashboardDialog(
    characters: List<Character>,
    onDismiss: () -> Unit
) {
    var selectedCharacterFilter by remember { mutableStateOf<String?>(null) } // null = All
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, UNLOCKED, LOCKED

    val allEvaluatedPerks = remember(characters) {
        characters.flatMap { SynergyEvaluator.evaluateCharacterSynergies(it) }
    }

    val activePerksCount = remember(allEvaluatedPerks) {
        allEvaluatedPerks.count { it.isUnlocked }
    }

    val filteredPerks = remember(allEvaluatedPerks, selectedCharacterFilter, selectedStatusFilter) {
        allEvaluatedPerks.filter { perk ->
            val matchChar = selectedCharacterFilter == null || perk.characterId == selectedCharacterFilter
            val matchStatus = when (selectedStatusFilter) {
                "UNLOCKED" -> perk.isUnlocked
                "LOCKED" -> !perk.isUnlocked
                else -> true
            }
            matchChar && matchStatus
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Synergický Dashboard Harému",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "Přehled pasivních buffů z kombinací vlastností (Důvěra, Loajalita...)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                // Global Stats Overview Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activePerksCount > 0) Color(0xFF1B5E20).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (activePerksCount > 0) Color(0xFF4CAF50).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("👑", fontSize = 16.sp)
                                Text(
                                    text = "Aktivní pasivní efekty družiny:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (activePerksCount > 0) "Aktivní perky poskytují trvalé bojové, ekonomické i regenerační výhody." else "Zatím žádná otrokyně nedosáhla potřebných kombinací vlastností.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activePerksCount > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "⚡ $activePerksCount Aktivní",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activePerksCount > 0) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Filter Controls Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Character Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCharacterFilter == null,
                                onClick = { selectedCharacterFilter = null },
                                label = { Text("Všichni (${characters.size})", fontSize = 11.sp) },
                                leadingIcon = { Text("👑", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(0xFFFFD700)
                                )
                            )
                        }

                        items(characters) { char ->
                            val isSel = selectedCharacterFilter == char.id
                            val charActiveCount = remember(allEvaluatedPerks, char.id) {
                                allEvaluatedPerks.count { it.characterId == char.id && it.isUnlocked }
                            }

                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCharacterFilter = if (isSel) null else char.id },
                                label = { Text("${char.name} ($charActiveCount)", fontSize = 11.sp) },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = StaticData.getPortraitForArchetype(char.archetypeId)),
                                        contentDescription = char.name,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Status Filter Tabs Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf(
                            "ALL" to "Všechny perky (${allEvaluatedPerks.size})",
                            "UNLOCKED" to "✅ Aktivní ($activePerksCount)",
                            "LOCKED" to "🔒 Zamčené (${allEvaluatedPerks.size - activePerksCount})"
                        )

                        statuses.forEach { (code, label) ->
                            val isSel = selectedStatusFilter == code
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedStatusFilter = code }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Synergy Perks List
                if (filteredPerks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Žádné perky neodpovídají zvoleným filtrům.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPerks, key = { it.id }) { perk ->
                            SynergyPerkCard(perk = perk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SynergyPerkCard(perk: EvaluatedSynergyPerk) {
    val tier = remember(perk.isUnlocked) {
        if (perk.isUnlocked) Color(0xFF2E7D32) else Color(0xFF424242)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (perk.isUnlocked) Color(0xFF1B5E20).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (perk.isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Row with Character & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = perk.characterImageRes),
                        contentDescription = perk.characterName,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = perk.characterName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = perk.currentTraitSummary,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (perk.isUnlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (perk.isUnlocked) Color(0xFF81C784) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (perk.isUnlocked) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("AKTIVNÍ BUFF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                            Text("ZAMČENO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Title & Icon Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(perk.icon, fontSize = 20.sp)
                Column {
                    Text(
                        text = perk.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (perk.isUnlocked) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Požadavek: ${perk.requirementText}",
                        fontSize = 10.sp,
                        color = Color(0xFFFFB74D),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bonus Effect Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (perk.isUnlocked) Color(0xFF388E3C).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, if (perk.isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "✨ PASIVNÍ EFEKT BUFFU:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (perk.isUnlocked) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = perk.bonusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (perk.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Explanation & Activation Reason
            Text(
                text = perk.description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (perk.isUnlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("💡", fontSize = 11.sp)
                    Text(
                        text = perk.activationReason,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF81C784)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = perk.missingRequirementText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D)
                    )

                    LinearProgressIndicator(
                        progress = { perk.progressPercent.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFFFB74D),
                        trackColor = Color(0xFFFFB74D).copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}
