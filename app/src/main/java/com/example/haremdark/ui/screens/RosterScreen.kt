package com.example.haremdark.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.CharacterVoiceType
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.CharacterComparisonDialog
import com.example.haremdark.ui.components.CharacterDiscoveryDialog
import com.example.haremdark.ui.components.RecruitModalDialog
import com.example.haremdark.viewmodels.HaremFilterCriteria
import com.example.haremdark.viewmodels.HaremViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: () -> Unit
) {
    val haremViewModel: HaremViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HaremViewModel(engine) as T
            }
        }
    )

    val filteredList by haremViewModel.filteredList.collectAsState()
    val searchQuery by haremViewModel.searchQuery.collectAsState()
    val selectedSort by haremViewModel.selectedSort.collectAsState()
    val filterCriteria by haremViewModel.filterCriteria.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showRecruitDialog by remember { mutableStateOf(false) }
    var showComparisonDialog by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var discoveredCharacter by remember { mutableStateOf<Character?>(null) }

    // Determine if "Scroll to top" button should be visible
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Roster Postav",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val totalCount = gameState.characters.size
                        val displayedCount = filteredList.size
                        Text(
                            if (displayedCount == totalCount) "$totalCount aktivních dcer v dominiu"
                            else "$displayedCount z $totalCount dcer odpovídá filtru",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            showComparisonDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = "Porovnat", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Porovnat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            HapticManager.vibrateClick()
                            showRecruitDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Nábor", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nábor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (searchQuery.isNotBlank() || filterCriteria.role != "Všechny" || selectedSort != "Náklonnost") {
                        TextButton(
                            onClick = {
                                haremViewModel.setSearchQuery("")
                                haremViewModel.setFilterCriteria(HaremFilterCriteria())
                                haremViewModel.setSort("Náklonnost")
                            }
                        ) {
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Nahoru na začátek")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Search Bar & Filter Header
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { haremViewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Hledat dceru dle jména či archetypu...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { haremViewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Vymazat hledání")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Sort Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Řazení:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Sorting Dropdown Button
                        Box {
                            OutlinedButton(
                                onClick = { sortMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("⚡ $selectedSort", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Vybrat řazení", modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                Text(
                                    text = "📊 Seřadit postavy:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                val sortOptions = listOf("Náklonnost", "Úroveň", "Rarita", "Síla", "Loajalita", "Jméno")
                                sortOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = when(option) {
                                                    "Úroveň" -> "📈 Úroveň (Level)"
                                                    "Rarita" -> "💎 Rarita (Rarity)"
                                                    "Náklonnost" -> "💖 Skóre náklonnosti (Affection)"
                                                    "Síla" -> "⚔️ Síla (Strength)"
                                                    "Loajalita" -> "🛡️ Loajalita (Loyalty)"
                                                    else -> "🔤 Jméno (Name)"
                                                },
                                                fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            haremViewModel.setSort(option)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val sortOptions = listOf("Náklonnost", "Úroveň", "Rarita", "Síla", "Loajalita", "Jméno")
                        sortOptions.forEach { sortName ->
                            val isSelected = selectedSort == sortName
                            FilterChip(
                                selected = isSelected,
                                onClick = { haremViewModel.setSort(sortName) },
                                label = { Text(sortName, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Quick Role Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filtr:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val roleFilters = listOf("Všechny", "Válečnice", "Mágyně", "Intrikánky", "Služky")
                        roleFilters.forEach { roleName ->
                            val isSelected = filterCriteria.role == roleName
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haremViewModel.setFilterCriteria(filterCriteria.copy(role = roleName))
                                },
                                label = { Text(roleName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Recruit CTA Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticManager.vibrateClick()
                                showRecruitDialog = true
                            }
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
                                Text("⛓️", fontSize = 16.sp)
                                Column {
                                    Text(
                                        "Objevit novou dceru (Nábor RNG)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Průzkum, dražba zajatců & temné rituály",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("Zahájit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 2. Character List / Empty State
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (searchQuery.isNotBlank() || filterCriteria.role != "Všechny")
                                "Žádná postava neodpovídá zadanému filtru."
                            else "V harému zatím nemáš žádné postavy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (searchQuery.isNotBlank() || filterCriteria.role != "Všechny") {
                            Button(
                                onClick = {
                                    haremViewModel.setSearchQuery("")
                                    haremViewModel.setFilterCriteria(HaremFilterCriteria())
                                }
                            ) {
                                Text("Zobrazit všechny postavy")
                            }
                        }

                        Button(
                            onClick = {
                                HapticManager.vibrateClick()
                                showRecruitDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Objevit dceru (Nábor)")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredList,
                        key = { character -> character.id.ifBlank { "char_${character.name}_${character.hashCode()}" } }
                    ) { character ->
                        CharacterRosterCard(character = character)
                    }
                }
            }
        }

        if (showRecruitDialog) {
            RecruitModalDialog(
                gameState = gameState,
                engine = engine,
                onDismiss = { showRecruitDialog = false },
                onCharacterDiscovered = { newChar ->
                    showRecruitDialog = false
                    discoveredCharacter = newChar
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }

        if (showComparisonDialog) {
            CharacterComparisonDialog(
                characters = gameState.characters,
                onDismiss = { showComparisonDialog = false }
            )
        }

        discoveredCharacter?.let { newChar ->
            CharacterDiscoveryDialog(
                character = newChar,
                onDismiss = {
                    discoveredCharacter = null
                },
                onRecruitAnother = {
                    discoveredCharacter = null
                    showRecruitDialog = true
                }
            )
        }
    }
}

@Composable
fun CharacterRosterCard(character: Character) {
    val affinityTier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }
    val archetype = remember(character.archetypeId) {
        StaticData.ARCHETYPES[character.archetypeId]
    }
    val loyaltyTier = remember(character.loajalita) {
        StaticData.getLoyaltyTier(character.loajalita)
    }

    // Direct local portrait drawable resource with safe fallback
    val portraitRes = remember(character.archetypeId) {
        StaticData.getPortraitForArchetype(character.archetypeId)
    }

    // Expansion state for secondary attributes
    var isExpanded by remember { mutableStateOf(false) }

    // Core attributes from model
    val strength = character.effectiveStrength
    val loyalty = character.effectiveLoyalty
    val affection = character.effectiveAffection

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .clickable {
                HapticManager.vibrateClick()
                SoundEffectManager.playCharacterVoice(CharacterVoiceType.TAP_GREETING)
                isExpanded = !isExpanded
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            Color(affinityTier.colorHex).copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Portrait, Name, Level, Affinity tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Portrait with Level Badge
                Box(modifier = Modifier.size(68.dp)) {
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = character.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, Color(affinityTier.colorHex), RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            "Lvl ${character.level}",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Name, Archetype & Affinity Tier Title
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (character.oblibena) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Oblíbená",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (character.isPinned) {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = "Připnuto",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${archetype?.name ?: "Neznámý archetyp"} • ${character.age} let • ${character.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Affinity Stage Label & Tier Progress
                    val (currentInTier, tierSpan) = remember(character.affinityPoints) {
                        AffinityData.getProgressInTier(character.affinityPoints)
                    }
                    val progressValue = (currentInTier.toFloat() / tierSpan.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${affinityTier.icon} ${affinityTier.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(affinityTier.colorHex)
                        )
                        Text(
                            "$currentInTier / $tierSpan bodů",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = Color(affinityTier.colorHex),
                        trackColor = Color(affinityTier.colorHex).copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PRIMARY ATTRIBUTES (Strength, Loyalty, Affection)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "HLAVNÍ ATRIBUTY POSTAVY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. SÍLA (Strength)
                        AttributeDisplayCard(
                            label = "Síla",
                            value = "$strength",
                            subtext = "Bojový útok",
                            icon = Icons.Default.Bolt,
                            color = Color(0xFFFF5722),
                            progress = (strength.toFloat() / 100f).coerceIn(0f, 1f),
                            modifier = Modifier.weight(1f)
                        )

                        // 2. LOAJALITA (Loyalty)
                        AttributeDisplayCard(
                            label = "Loajalita",
                            value = "$loyalty%",
                            subtext = loyaltyTier.title,
                            icon = Icons.Default.Shield,
                            color = Color(0xFF4CAF50),
                            progress = (loyalty.toFloat() / 100f).coerceIn(0f, 1f),
                            modifier = Modifier.weight(1f)
                        )

                        // 3. NÁKLONNOST (Affection)
                        AttributeDisplayCard(
                            label = "Náklonnost",
                            value = "$affection%",
                            subtext = "Srdce dcery",
                            icon = Icons.Default.Favorite,
                            color = Color(0xFFE91E63),
                            progress = (affection.toFloat() / 100f).coerceIn(0f, 1f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // EXPANDABLE SECONDARY STATS (Obedience, Morale, Lust, Fear, Conditions)
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SecondaryStatChip(
                            label = "Poslušnost",
                            value = "${character.poslusnost}%",
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryStatChip(
                            label = "Morálka",
                            value = "${character.morale}%",
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryStatChip(
                            label = "Touha",
                            value = "${character.touha}%",
                            color = Color(0xFFFF4081),
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryStatChip(
                            label = "Strach",
                            value = "${character.strach}%",
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Special flags (Broken, Mindbreak, Pregnancy, Rental)
                    if (character.broken > 0 || character.mindbreak > 0 || character.tehotna || character.naNajmu) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (character.broken > 0) {
                                SecondaryStatChip(
                                    label = "Zlomení",
                                    value = "${character.broken}%",
                                    color = Color(0xFF795548),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (character.mindbreak > 0) {
                                SecondaryStatChip(
                                    label = "Mindbreak",
                                    value = "${character.mindbreak}%",
                                    color = Color(0xFF673AB7),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (character.tehotna) {
                                SecondaryStatChip(
                                    label = "Březí",
                                    value = "${character.dnyTehotenstvi}. den",
                                    color = Color(0xFFE040FB),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (character.naNajmu) {
                                SecondaryStatChip(
                                    label = "Na nájmu",
                                    value = "${character.najemZbyvaDni} dní",
                                    color = Color(0xFF00BCD4),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer bar: Mood status and toggle details button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(character.statusIcon, fontSize = 14.sp)
                    Text(
                        text = character.nalada.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = if (isExpanded) "Méně detailů" else "Více atributů",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttributeDisplayCard(
    label: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Text(
                    label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = color
            )

            Text(
                subtext,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SecondaryStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, color = color.copy(alpha = 0.8f))
        }
    }
}
