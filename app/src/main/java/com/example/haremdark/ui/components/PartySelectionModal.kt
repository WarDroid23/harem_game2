package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.PartyCombatCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.CombatRole
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalSynergyManager
import com.example.haremdark.models.ElementalSynergyBuff

enum class PartySortOption(val title: String, val icon: String) {
    AFFINITY("Úroveň náklonnosti", "💖"),
    CLASS_ROLE("Třída / Role", "🎭"),
    ATTACK("Bojový útok", "⚔️"),
    HP("Životy (HP)", "💚"),
    SPEED("Rychlost", "⚡"),
    LOYALTY("Loajalita & Morálka", "👑"),
    NAME("Jméno (A-Z)", "🔤")
}

enum class PartyAffinityFilter(val title: String, val levelRange: IntRange?) {
    ALL("Všechny úrovně", null),
    TIER_0_1("Neznámá (0-1)", 0..1),
    TIER_2_3("Důvěrnice (2-3)", 2..3),
    TIER_4_5("Milenka (4-5)", 4..5),
    TIER_6_PLUS("Královna (6+)", 6..10)
}

@Composable
fun PartySelectionDialog(
    gameState: GameSave,
    engine: GameEngine,
    preselectedEncounter: PartyCombatCatalog.PartyEncounterDefinition?,
    onDismiss: () -> Unit,
    onStartCombat: (selectedGirlIds: List<String>, includePlayer: Boolean, encounter: PartyCombatCatalog.PartyEncounterDefinition) -> Unit,
    onSaveFormation: ((name: String, icon: String, memberIds: List<String>, includePlayer: Boolean) -> Unit)? = null,
    onDeleteFormation: ((id: String) -> Unit)? = null
) {
    var selectedEncounter by remember {
        mutableStateOf(preselectedEncounter ?: PartyCombatCatalog.ENCOUNTERS.first())
    }

    var showSaveFormationDialog by remember { mutableStateOf(false) }
    var showPreBattleFormation by remember { mutableStateOf(false) }
    var formationNameInput by remember { mutableStateOf("Boss Squad") }
    var formationIconInput by remember { mutableStateOf("🐉") }

    // Sorting and Filtering states for party recruitment / selection
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<CombatRole?>(null) }
    var selectedAffinityFilter by remember { mutableStateOf(PartyAffinityFilter.ALL) }
    var selectedSortOption by remember { mutableStateOf(PartySortOption.AFFINITY) }
    var sortDescending by remember { mutableStateOf(true) }
    var onlyFullHp by remember { mutableStateOf(false) }
    var showFilterChips by remember { mutableStateOf(true) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    // Default select up to 3 healthiest girls
    val availableGirls = remember(gameState.characters) {
        gameState.characters.filter { it.hp > 0 }
    }

    var selectedGirls by remember {
        mutableStateOf(
            availableGirls.sortedByDescending { (it.skills["combat"] ?: 0) + it.affinityPoints }
                .take(3)
                .map { it.id }
                .toList()
        )
    }

    var includePlayer by remember { mutableStateOf(true) }

    val maxGirlsAllowed = if (includePlayer) 3 else 4

    // Filtered and Sorted Party Members List
    val filteredAndSortedGirls = remember(
        availableGirls,
        searchQuery,
        selectedRoleFilter,
        selectedAffinityFilter,
        selectedSortOption,
        sortDescending,
        onlyFullHp
    ) {
        availableGirls
            .filter { girl ->
                if (searchQuery.isNotBlank()) {
                    girl.name.contains(searchQuery, ignoreCase = true) ||
                    girl.archetype.contains(searchQuery, ignoreCase = true)
                } else true
            }
            .filter { girl ->
                if (selectedRoleFilter != null) {
                    PartyCombatCatalog.getRoleForArchetype(girl.archetype) == selectedRoleFilter
                } else true
            }
            .filter { girl ->
                val range = selectedAffinityFilter.levelRange
                if (range != null) {
                    girl.affinityLevel in range
                } else true
            }
            .filter { girl ->
                if (onlyFullHp) girl.hp >= girl.maxHp else true
            }
            .let { list ->
                when (selectedSortOption) {
                    PartySortOption.AFFINITY -> {
                        if (sortDescending) list.sortedByDescending { it.affinityLevel * 1000 + it.affinityPoints }
                        else list.sortedBy { it.affinityLevel * 1000 + it.affinityPoints }
                    }
                    PartySortOption.CLASS_ROLE -> {
                        if (sortDescending) list.sortedByDescending { PartyCombatCatalog.getRoleForArchetype(it.archetype).title }
                        else list.sortedBy { PartyCombatCatalog.getRoleForArchetype(it.archetype).title }
                    }
                    PartySortOption.ATTACK -> {
                        if (sortDescending) list.sortedByDescending { it.skills["combat"] ?: 5 }
                        else list.sortedBy { it.skills["combat"] ?: 5 }
                    }
                    PartySortOption.HP -> {
                        if (sortDescending) list.sortedByDescending { it.maxHp }
                        else list.sortedBy { it.maxHp }
                    }
                    PartySortOption.SPEED -> {
                        if (sortDescending) list.sortedByDescending { it.skills["agility"] ?: it.skills["speed"] ?: 10 }
                        else list.sortedBy { it.skills["agility"] ?: it.skills["speed"] ?: 10 }
                    }
                    PartySortOption.LOYALTY -> {
                        if (sortDescending) list.sortedByDescending { it.loajalita + it.morale }
                        else list.sortedBy { it.loajalita + it.morale }
                    }
                    PartySortOption.NAME -> {
                        if (sortDescending) list.sortedByDescending { it.name }
                        else list.sortedBy { it.name }
                    }
                }
            }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF160D1E),
            border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "⚔️ Sestavení bojové družiny",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            "Vyber hrdinky z harému (${selectedGirls.size}/$maxGirlsAllowed)",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.Gray)
                    }
                }

                // --- 1. ENCOUNTER SELECTOR CAROUSEL ---
                Text("Vyber arénový střet / výpravu:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PartyCombatCatalog.ENCOUNTERS) { encounter ->
                        val isSelected = (encounter.id == selectedEncounter.id)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF4A1435) else Color(0xFF22132A),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.clickable { selectedEncounter = encounter }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(encounter.icon, fontSize = 16.sp)
                                Column {
                                    Text(encounter.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isSelected) Color(0xFFFFD700) else Color.White, maxLines = 1)
                                    Text(encounter.tierName, fontSize = 9.sp, color = Color(0xFFB0BEC5))
                                }
                            }
                        }
                    }
                }

                // --- SAVED PARTY FORMATIONS CAROUSEL ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️ Uložené formace týmu:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFFD700))
                    TextButton(
                        onClick = { showSaveFormationDialog = true },
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF4081))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uložit aktuální", fontSize = 10.sp, color = Color(0xFFFF4081))
                    }
                }

                val allFormations = remember(gameState.savedPartyFormations) {
                    val defaultPresets = listOf(
                        com.example.haremdark.models.PartyFormation("preset_1", "Úderná trojka", "⚔️", availableGirls.take(3).map { it.id }, true),
                        com.example.haremdark.models.PartyFormation("preset_2", "Obranná falanga", "🛡️", availableGirls.take(3).map { it.id }, false)
                    )
                    defaultPresets + gameState.savedPartyFormations
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allFormations) { formation ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF261536),
                            border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.5f)),
                            modifier = Modifier.clickable {
                                selectedGirls = formation.memberIds.filter { id -> availableGirls.any { it.id == id } }
                                includePlayer = formation.includePlayer
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(formation.icon, fontSize = 14.sp)
                                Column {
                                    Text(formation.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, maxLines = 1)
                                    Text("${formation.memberIds.size} hrdinek", fontSize = 8.sp, color = Color(0xFFB39DDB))
                                }
                                if (!formation.id.startsWith("preset_")) {
                                    IconButton(
                                        onClick = { onDeleteFormation?.invoke(formation.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 2. PLAYER INCLUSION TOGGLE ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (includePlayer) Color(0xFF2E1B3E) else Color(0xFF1E1424)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👑", fontSize = 20.sp)
                            Column {
                                Text("Pán Dominia (Velitel)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Vstoupí osobně do boje s čepelí dominia", fontSize = 10.sp, color = Color(0xFFB39DDB))
                            }
                        }

                        Switch(
                            checked = includePlayer,
                            onCheckedChange = { checked ->
                                includePlayer = checked
                                if (checked && selectedGirls.size > 3) {
                                    selectedGirls = selectedGirls.take(3).toList()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700), checkedTrackColor = Color(0xFF7B1FA2))
                        )
                    }
                }

                // --- SLOT-BASED FORMATION UI ---
                Text("Formace družiny (Pozice poskytují bonusy):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                val slotBonuses = listOf(
                    "Vanguard" to "+15% HP & Obrana",
                    "Flank" to "+10% Útok & Rychlost",
                    "Rearguard" to "+15% Crit & Mana",
                    "Support" to "+5 Rychlost & Obrana"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until maxGirlsAllowed) {
                        val girlId = selectedGirls.getOrNull(i)
                        val char = girlId?.let { id -> availableGirls.find { it.id == id } }
                        val bonusInfo = slotBonuses.getOrNull(if (includePlayer) i + 1 else i) ?: ("Slot ${i+1}" to "")
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).clickable {
                                if (girlId != null) {
                                    selectedGirls = selectedGirls.filter { it != girlId }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (char != null) Color(0xFFFF4081) else Color.Gray, RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2E1B3E)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (char != null) {
                                    val portraitRes = StaticData.getPortraitForArchetype(char.archetype)
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(portraitRes).crossfade(true).build(),
                                        contentDescription = char.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Prázdné", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(bonusInfo.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            Text(bonusInfo.second, fontSize = 8.sp, color = Color(0xFFB39DDB), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // --- REAL-TIME ELEMENTAL SYNERGY PREVIEW ---
                val draftedElements = remember(selectedGirls, includePlayer, availableGirls) {
                    val pairs = mutableListOf<Pair<String, Element>>()
                    if (includePlayer) {
                        pairs.add("Pán Dominia" to Element.DARK)
                    }
                    selectedGirls.forEach { id ->
                        val char = availableGirls.find { it.id == id }
                        if (char != null) {
                            val elem = when (char.archetypeId) {
                                "sukuba", "krvava_subka" -> Element.DARK
                                "chladna" -> Element.ICE
                                "draci_divka" -> Element.FIRE
                                "subka" -> Element.WATER
                                "touha" -> Element.LIGHTNING
                                "knezkyn" -> Element.HOLY
                                "vzdorna" -> Element.EARTH
                                else -> Element.PHYSICAL
                            }
                            pairs.add(char.name to elem)
                        }
                    }
                    pairs
                }

                val activeElementalSynergies = remember(draftedElements) {
                    ElementalSynergyManager.evaluateSynergiesFromPairs(draftedElements)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E102E),
                    border = BorderStroke(1.dp, if (activeElementalSynergies.isNotEmpty()) Color(0xFFAB47BC) else Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🛡️", fontSize = 11.sp)
                                Text(
                                    "Elementární Synergie Týmu:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (activeElementalSynergies.isNotEmpty()) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (activeElementalSynergies.isNotEmpty()) "${activeElementalSynergies.size} aktivní" else "0 aktivních",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeElementalSynergies.isNotEmpty()) Color(0xFF69F0AE) else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (activeElementalSynergies.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(activeElementalSynergies) { syn ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF2C1642),
                                        border = BorderStroke(0.5.dp, Color(0xFFCE93D8).copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(syn.icon, fontSize = 11.sp)
                                            Text(
                                                syn.name,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                "-${(syn.damageResistancePercent * 100).toInt()}% DMG",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF69F0AE)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "💡 Tip: Sestavte dívky s komplementárními živly (např. Oheň + Země, Voda + Led, Blesk + Vzduch) pro aktivaci pasivních štítů!",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                }

                // --- 3. ROSTER OF HAREM GIRLS (WITH SEARCH, SORTING & FILTERING) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search & Sorting Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dostupné společnice v dominiu:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Sort Dropdown Button
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2A163B),
                                    border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { sortDropdownExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(selectedSortOption.icon, fontSize = 11.sp)
                                        Text(
                                            selectedSortOption.title,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700)
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = sortDropdownExpanded,
                                    onDismissRequest = { sortDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFF211130))
                                ) {
                                    PartySortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(option.icon, fontSize = 14.sp)
                                                    Text(
                                                        option.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (selectedSortOption == option) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (selectedSortOption == option) Color(0xFFFFD700) else Color.White
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedSortOption = option
                                                sortDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Sort Direction Toggle (Asc / Desc)
                            IconButton(
                                onClick = { sortDescending = !sortDescending },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF2A163B), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    if (sortDescending) "⬇️" else "⬆️",
                                    fontSize = 11.sp
                                )
                            }

                            // Toggle Filter Chips
                            IconButton(
                                onClick = { showFilterChips = !showFilterChips },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(if (showFilterChips) Color(0xFF51183E) else Color(0xFF2A163B), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filtry",
                                    tint = if (showFilterChips) Color(0xFFFF4081) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Search input & Filter chips
                    if (showFilterChips) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Search bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Hledat hrdinku podle jména či role...", fontSize = 10.sp, color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Clear, contentDescription = "Vymazat", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF4081),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedContainerColor = Color(0xFF1E1026),
                                    unfocusedContainerColor = Color(0xFF180C1E)
                                )
                            )

                            // Class / Combat Role Filter Chips Row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedRoleFilter == null,
                                        onClick = { selectedRoleFilter = null },
                                        label = { Text("Všechny třídy", fontSize = 9.sp) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF7B1FA2),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF1F122B),
                                            labelColor = Color(0xFFB0BEC5)
                                        )
                                    )
                                }
                                items(CombatRole.values()) { role ->
                                    val isSelected = selectedRoleFilter == role
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedRoleFilter = if (isSelected) null else role
                                        },
                                        label = {
                                            Text("${role.icon} ${role.title}", fontSize = 9.sp)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFC2185B),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF1F122B),
                                            labelColor = Color(0xFFB0BEC5)
                                        )
                                    )
                                }
                            }

                            // Affinity Tier Filter Chips Row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(PartyAffinityFilter.values()) { affFilter ->
                                    val isSelected = selectedAffinityFilter == affFilter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedAffinityFilter = affFilter },
                                        label = {
                                            Text(
                                                affFilter.title,
                                                fontSize = 9.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFE91E63),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF1F122B),
                                            labelColor = Color(0xFFB0BEC5)
                                        )
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = onlyFullHp,
                                        onClick = { onlyFullHp = !onlyFullHp },
                                        label = { Text("💚 Jen plné HP", fontSize = 9.sp) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF2E7D32),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF1F122B),
                                            labelColor = Color(0xFFB0BEC5)
                                        )
                                    )
                                }
                            }

                            // Summary count & active filters reset
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Zobrazeno ${filteredAndSortedGirls.size} z ${availableGirls.size} hrdinek",
                                    fontSize = 9.sp,
                                    color = Color(0xFFB39DDB)
                                )

                                if (searchQuery.isNotEmpty() || selectedRoleFilter != null || selectedAffinityFilter != PartyAffinityFilter.ALL || onlyFullHp) {
                                    Text(
                                        "Vymazat filtry ✕",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFF4081),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                searchQuery = ""
                                                selectedRoleFilter = null
                                                selectedAffinityFilter = PartyAffinityFilter.ALL
                                                onlyFullHp = false
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Girls List or Empty State
                    if (availableGirls.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Všechny tvé dívky jsou zraněné! Ošetři je v komnatách dominia.", color = Color(0xFFFF8A80), fontSize = 12.sp)
                        }
                    } else if (filteredAndSortedGirls.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF1A0E22), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔍 Žádná hrdinka neodpovídá zvoleným filtrům", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        selectedRoleFilter = null
                                        selectedAffinityFilter = PartyAffinityFilter.ALL
                                        onlyFullHp = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Zrušit všechny filtry", fontSize = 10.sp)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredAndSortedGirls, key = { it.id }) { char ->
                                val isSelected = selectedGirls.contains(char.id)
                                val role = PartyCombatCatalog.getRoleForArchetype(char.archetype)
                                val portraitRes = StaticData.getPortraitForArchetype(char.archetype)
                                val affinityTier = AffinityData.getTierForPoints(char.affinityPoints)
                                val combatSkill = char.skills["combat"] ?: 5
                                val speedStat = char.skills["agility"] ?: char.skills["speed"] ?: 10

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) {
                                                selectedGirls = selectedGirls - char.id
                                            } else {
                                                if (selectedGirls.size < maxGirlsAllowed) {
                                                    selectedGirls = selectedGirls + char.id
                                                }
                                            }
                                        }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF3B152A) else Color(0xFF1E1224)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .border(1.5.dp, Color(affinityTier.colorHex), CircleShape)
                                            ) {
                                                SubcomposeAsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(portraitRes)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = char.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(char.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF311B92)
                                                    ) {
                                                        Text(
                                                            text = "${role.icon} ${role.title}",
                                                            fontSize = 8.sp,
                                                            color = Color(0xFFB388FF),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }

                                                // Affinity & Multi-Tier Relationship Stage badge
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(affinityTier.colorHex).copy(alpha = 0.2f),
                                                    border = BorderStroke(0.5.dp, Color(affinityTier.colorHex).copy(alpha = 0.6f))
                                                ) {
                                                    Text(
                                                        text = "${affinityTier.icon} ${affinityTier.stageName} (Lv.${affinityTier.level})",
                                                        fontSize = 8.sp,
                                                        color = Color(affinityTier.colorHex),
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }

                                                // Combat Bonus derived from relationship tier
                                                Text(
                                                    text = affinityTier.combatBonusDescription,
                                                    fontSize = 8.sp,
                                                    color = Color(0xFFFFD54F),
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                // Base Stats Breakdown Bar
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("💚 ${char.hp}/${char.maxHp} HP", fontSize = 9.sp, color = if (char.hp == char.maxHp) Color(0xFF81C784) else Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                                                    Text("⚔️ Útok: $combatSkill", fontSize = 9.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                                                    Text("⚡ Spd: $speedStat", fontSize = 9.sp, color = Color(0xFF80D8FF))
                                                    Text("✨ Morálka: ${char.morale}", fontSize = 9.sp, color = Color(0xFFFFD54F))
                                                }
                                            }
                                        }

                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked && selectedGirls.size < maxGirlsAllowed) {
                                                    selectedGirls = selectedGirls + char.id
                                                } else if (!checked) {
                                                    selectedGirls = selectedGirls - char.id
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFFF4081),
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 4. PRE-BATTLE FORMATION & START COMBAT BUTTONS ---
                val canStart = selectedGirls.isNotEmpty() || includePlayer
                OutlinedButton(
                    onClick = { showPreBattleFormation = true },
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                ) {
                    Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🛡️ Pokročilá formace a pozice v liniích", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        onStartCombat(selectedGirls.toList(), includePlayer, selectedEncounter)
                    },
                    enabled = canStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC2185B),
                        disabledContainerColor = Color(0xFF4A182E)
                    )
                ) {
                    Icon(Icons.Default.SportsKabaddi, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VYSLAT DRUŽINU DO BOJE (${if (includePlayer) selectedGirls.size + 1 else selectedGirls.size} bojovníků)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = if (canStart) Color.White else Color.Gray
                    )
                }
            }
        }
    }

    if (showPreBattleFormation) {
        PreBattleFormationDialog(
            gameState = gameState,
            engine = engine,
            selectedCharacterIds = selectedGirls,
            includePlayer = includePlayer,
            onDismiss = { showPreBattleFormation = false },
            onSaveAndProceed = { updatedFormations ->
                engine.updatePartyFormations(updatedFormations)
                showPreBattleFormation = false
                onStartCombat(selectedGirls.toList(), includePlayer, selectedEncounter)
            }
        )
    }

    if (showSaveFormationDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFormationDialog = false },
            title = { Text("Uložit bojovou formaci", color = Color.White, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formationNameInput,
                        onValueChange = { formationNameInput = it },
                        label = { Text("Název formace") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formationIconInput,
                        onValueChange = { formationIconInput = it },
                        label = { Text("Ikona / Emoji") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveFormation?.invoke(formationNameInput, formationIconInput, selectedGirls, includePlayer)
                        showSaveFormationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text("Uložit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveFormationDialog = false }) {
                    Text("Zrušit", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E122B)
        )
    }
}

@Composable
fun CompanionEquipmentCard(
    char: Character,
    engine: GameEngine,
    gameState: GameSave
) {
    var showEquipment by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                val defBonus = char.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                if (combatBonus > 0 || defBonus > 0) {
                    Text("⚔️ +$combatBonus | 🛡️ +$defBonus", fontSize = 9.sp, color = Color(0xFFFFD700))
                }
            }

            TextButton(
                onClick = { showEquipment = !showEquipment },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (showEquipment) "Skrýt výbavu ▲" else "🛡️ Výbava ▼",
                    fontSize = 10.sp,
                    color = Color(0xFFFF80AB)
                )
            }
        }

        if (showEquipment) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF140810), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val equippedItems = char.equipment.values.filterNotNull()
                val matchingSetCount = equippedItems.groupBy { it.source }.entries.maxByOrNull { it.value.size }?.let { if (it.value.size >= 2) it.value.size else 0 } ?: 0
                if (matchingSetCount >= 2) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF673AB7).copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color(0xFFE040FB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✨", fontSize = 12.sp)
                            Column {
                                Text("Aktivní set výbavy (${matchingSetCount}ks ze stejného zdroje)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE040FB))
                                Text("Bonus: +15% Útok & +15% Obrana", fontSize = 9.sp, color = Color(0xFFFFD700))
                            }
                        }
                    }
                }
                val slots = listOf(
                    "weapon" to "Zbraň 🗡️",
                    "accessory" to "Doplněk 💍",
                    "armor" to "Zbroj 🛡️"
                )

                for ((slotId, slotName) in slots) {
                    val equippedItem = char.equipment[slotId]
                    val availableItems = gameState.player.items.filter { it.category == "equipment" && it.equipSlot == slotId && it.count > 0 }
                    var showSlotPicker by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF220A17), RoundedCornerShape(6.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(equippedItem?.icon ?: "⭕", fontSize = 16.sp)
                            Column {
                                Text(slotName, fontSize = 9.sp, color = Color(0xFFB39DDB))
                                Text(
                                    text = equippedItem?.name ?: "Prázdno",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (equippedItem != null) Color(0xFFFF80AB) else Color.Gray
                                )
                                if (equippedItem != null && !equippedItem.effectDescription.isNullOrEmpty()) {
                                    Text(
                                        text = equippedItem.effectDescription,
                                        fontSize = 9.sp,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (equippedItem != null) {
                                TextButton(
                                    onClick = { engine.unequipItemFromCharacter(char.id, slotId) },
                                    contentPadding = PaddingValues(4.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Odebrat", fontSize = 9.sp, color = Color(0xFFFF5252))
                                }
                            }
                            Button(
                                onClick = { showSlotPicker = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                            ) {
                                Text(if (equippedItem != null) "Změnit" else "Vybavit", fontSize = 9.sp)
                            }
                        }
                    }

                    if (showSlotPicker) {
                        AlertDialog(
                            onDismissRequest = { showSlotPicker = false },
                            title = { Text("Vyber předmět pro: $slotName", color = Color.White, fontSize = 14.sp) },
                            text = {
                                if (availableItems.isEmpty()) {
                                    Text("Nemáš v inventáři žádné volné předměty pro tento slot.", color = Color.Gray, fontSize = 12.sp)
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(availableItems) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF2B1638))
                                                    .clickable {
                                                        engine.equipItemToCharacter(char.id, item.id, slotId)
                                                        showSlotPicker = false
                                                    }
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(item.icon, fontSize = 20.sp)
                                                    Column {
                                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                        Text(item.effectDescription ?: "", fontSize = 10.sp, color = Color(0xFF00E5FF))
                                                    }
                                                }
                                                Text("Vybavit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showSlotPicker = false }) {
                                    Text("Zavřít", color = Color.Gray)
                                }
                            },
                            containerColor = Color(0xFF160D1E)
                        )
                    }
                }
            }
        }
    }
}

