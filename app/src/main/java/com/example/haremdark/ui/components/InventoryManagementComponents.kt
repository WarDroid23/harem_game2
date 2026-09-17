package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.InventoryFilterUtils
import com.example.haremdark.models.InventoryItem
import com.example.haremdark.models.ItemAffinityFilter
import com.example.haremdark.models.ItemQuantityFilter
import com.example.haremdark.models.ItemTypeCategory
import com.example.haremdark.models.Player

enum class InventoryViewMode(val title: String, val icon: String) {
    BAG("Batoh", "🎒"),
    STORAGE("Sklad", "🏛️"),
    ALL("Vše", "📦")
}

enum class InventorySourceFilter(val label: String, val icon: String) {
    ALL("Všechny zdroje", "✨"),
    EXPLORATION("Z průzkumu", "🧭"),
    COMBAT("Z bojů", "⚔️"),
    GIFTS("Dary", "🎁"),
    EQUIPMENT("Výbava", "🛡️"),
    POTIONS("Lektvary", "🧪")
}

enum class InventorySortMode(val label: String, val icon: String = "") {
    TYPE("🏷️ Typ / Kategorie", "🏷️"),
    AFFINITY_DESC("💖 Náklonnost (Nejvyšší bonus)", "💖"),
    AFFINITY_ASC("🤍 Náklonnost (Nejnižší bonus)", "🤍"),
    COUNT_DESC("🔢 Počet kusů (Nejvíce)", "🔢"),
    COUNT_ASC("📉 Počet kusů (Nejméně)", "📉"),
    RARITY_DESC("💎 Vzácnost (Nejvyšší)", "💎"),
    RARITY_ASC("🔹 Vzácnost (Nejnižší)", "🔹"),
    PRICE_DESC("💰 Hodnota (Nejvyšší)", "💰"),
    NAME_ASC("🔤 Název (A-Z)", "🔤"),
    SOURCE("🧭 Původ (Průzkum / Boj)", "🧭")
}

/**
 * Visual Progress Bars strip for all party members in the character interaction screen.
 */
@Composable
fun PartyMembersStatusBar(
    characters: List<Character>,
    player: Player,
    activeCharacter: Character,
    onSelectCharacter: (Character) -> Unit,
    onTogglePartyMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePartyIds = player.activePartyIds

    // Determine party list: ensure activeCharacter is displayed along with other party members
    val partyCharacters = remember(characters, activePartyIds, activeCharacter) {
        val inParty = characters.filter { activePartyIds.contains(it.id) }
        val list = if (inParty.isEmpty()) {
            characters.filter { it.oblibena || it.jeManzelkou || it.partnerka }.ifEmpty { characters.take(4) }
        } else {
            inParty
        }
        if (!list.any { it.id == activeCharacter.id }) {
            listOf(activeCharacter) + list
        } else {
            list
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚔️", fontSize = 16.sp)
                    Text(
                        text = "Stav družiny a vitální ukazatele",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${partyCharacters.size} bojovnic",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Party Members Horizontal Carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                // 1. Lord Card (Player stats)
                item {
                    LordPartyMemberCard(player = player)
                }

                // 2. Concubines Party Members Cards
                items(partyCharacters, key = { it.id }) { concubine ->
                    val isSelected = concubine.id == activeCharacter.id
                    val isInActiveParty = activePartyIds.contains(concubine.id)

                    PartyMemberMiniCard(
                        character = concubine,
                        isSelected = isSelected,
                        isInParty = isInActiveParty,
                        onClick = { onSelectCharacter(concubine) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LordPartyMemberCard(
    player: Player,
    modifier: Modifier = Modifier
) {
    val hpPct = (player.hp.toFloat() / player.maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val manaPct = (player.mana.toFloat() / player.maxMana.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val dominancePct = (player.dominance.toFloat() / 100f).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .width(135.dp)
            .height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1528)
        ),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Vládce dominia",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Mini Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MiniStatBar(
                    label = "HP",
                    value = "${player.hp}/${player.maxHp}",
                    progress = hpPct,
                    barColor = Color(0xFFE53935)
                )
                MiniStatBar(
                    label = "MP",
                    value = "${player.mana}/${player.maxMana}",
                    progress = manaPct,
                    barColor = Color(0xFF1E88E5)
                )
                MiniStatBar(
                    label = "VL",
                    value = "${player.dominance}%",
                    progress = dominancePct,
                    barColor = Color(0xFFFFB300)
                )
            }
        }
    }
}

@Composable
private fun PartyMemberMiniCard(
    character: Character,
    isSelected: Boolean,
    isInParty: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
    val hpPct = (character.hp.toFloat() / character.maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val manaPct = (character.mana.toFloat() / character.maxMana.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val tier = AffinityData.getTierForPoints(character.affinityPoints)
    val affPct = (character.affinityPoints.toFloat() / tier.maxPoints.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .width(135.dp)
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF140D20)
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(portraitRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isInParty) "⚔️ Bojová družina" else "🏛️ Harém",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isInParty) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            // Mini Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MiniStatBar(
                    label = "HP",
                    value = "${character.hp}/${character.maxHp}",
                    progress = hpPct,
                    barColor = Color(0xFFE53935)
                )
                MiniStatBar(
                    label = "MP",
                    value = "${character.mana}/${character.maxMana}",
                    progress = manaPct,
                    barColor = Color(0xFF00B0FF)
                )
                MiniStatBar(
                    label = "💖",
                    value = "Úr.${character.affinityLevel}",
                    progress = affPct,
                    barColor = Color(0xFFE91E63)
                )
            }
        }
    }
}

@Composable
private fun MiniStatBar(
    label: String,
    value: String,
    progress: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(500), label = "progress")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = barColor,
            modifier = Modifier.width(18.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
        Text(
            text = value,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

/**
 * High-fidelity, detailed visual progress bars for Health, Mana, and Affinity of the active character.
 */
@Composable
fun PartyMemberDetailedProgressBarsCard(
    character: Character,
    player: Player,
    onToggleParty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hpPct = (character.hp.toFloat() / character.maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val manaPct = (character.mana.toFloat() / character.maxMana.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val tier = AffinityData.getTierForPoints(character.affinityPoints)
    val affProgress = AffinityData.getProgressInTier(character.affinityPoints)
    val affPct = if (affProgress.second > 0) {
        (affProgress.first.toFloat() / affProgress.second.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val isInParty = player.activePartyIds.contains(character.id)
    var activeBarTooltip by remember { mutableStateOf<StatBarTooltipType?>(null) }

    if (activeBarTooltip != null) {
        StatBarExplanationDialog(
            tooltipType = activeBarTooltip!!,
            character = character,
            onDismiss = { activeBarTooltip = null }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row with Name, Party Status, and Party Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${character.role})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (isInParty) "⚔️ Aktivní v bojové družině" else "🏛️ V zázemí harému",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isInParty) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onToggleParty,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInParty) Color(0xFFC2185B).copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isInParty) "Odebrat z party" else "➕ Do party",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Explanatory hint banner for new players explaining long-press tooltips
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Tip pro nováčky: Podržením lišty (HP, MP, Náklonnost) zobrazíš vysvětlení ukazatele.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 1. HEALTH PROGRESS BAR
            DetailedStatProgressBar(
                title = "Zdraví (HP)",
                icon = "❤️",
                current = character.hp,
                max = character.maxHp,
                percentage = hpPct,
                startColor = Color(0xFFB71C1C),
                endColor = Color(0xFFFF5252),
                statusNote = when {
                    hpPct >= 0.8f -> "Plné zdraví"
                    hpPct >= 0.4f -> "Lehce zraněna"
                    else -> "Kritický stav!"
                },
                tooltipType = StatBarTooltipType.HEALTH,
                onShowTooltip = { activeBarTooltip = it }
            )

            // 2. MANA PROGRESS BAR
            DetailedStatProgressBar(
                title = "Mana (MP)",
                icon = "🔮",
                current = character.mana,
                max = character.maxMana,
                percentage = manaPct,
                startColor = Color(0xFF0D47A1),
                endColor = Color(0xFF00E5FF),
                statusNote = when {
                    manaPct >= 0.7f -> "Plná koncentrace"
                    manaPct >= 0.3f -> "Aura připravena"
                    else -> "Vyčerpaná mana"
                },
                tooltipType = StatBarTooltipType.MANA,
                onShowTooltip = { activeBarTooltip = it }
            )

            // 3. AFFINITY PROGRESS BAR
            DetailedStatProgressBar(
                title = "Pouto a náklonnost",
                icon = "💖",
                current = character.affinityPoints,
                max = tier.maxPoints,
                percentage = affPct,
                startColor = Color(0xFF7B1FA2),
                endColor = Color(0xFFFF4081),
                customLevelBadge = "Úr. ${tier.level} • ${tier.title}",
                statusNote = "+${tier.perkDescription}",
                tooltipType = StatBarTooltipType.AFFINITY,
                onShowTooltip = { activeBarTooltip = it }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailedStatProgressBar(
    title: String,
    icon: String,
    current: Int,
    max: Int,
    percentage: Float,
    startColor: Color,
    endColor: Color,
    statusNote: String,
    customLevelBadge: String? = null,
    tooltipType: StatBarTooltipType? = null,
    onShowTooltip: ((StatBarTooltipType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(600),
        label = "detailedProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (tooltipType != null && onShowTooltip != null) {
                    Modifier.combinedClickable(
                        onClick = { onShowTooltip(tooltipType) },
                        onLongClick = { onShowTooltip(tooltipType) }
                    )
                } else Modifier
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(icon, fontSize = 14.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tooltipType != null && onShowTooltip != null) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Vysvětlení $title",
                        tint = endColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                }
                if (customLevelBadge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = endColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = customLevelBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = endColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = "$current / $max (${(percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = endColor
            )
        }

        // Custom Gradient Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(startColor, endColor)
                        )
                    )
            )
        }

        Text(
            text = statusNote,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Inventory Management System Panel
 * Supports viewing items stored in Bag vs. Castle Storage,
 * quick one-click storing of exploration/combat loot,
 * filtering by source origin (🧭 Průzkum, ⚔️ Boj), and sorting.
 */
@Composable
fun InventoryManagementPanel(
    gameState: GameSave,
    engine: GameEngine,
    activeCharacter: Character? = null,
    onUseItemOnCharacter: ((InventoryItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = gameState.player

    var viewMode by remember { mutableStateOf(InventoryViewMode.ALL) }
    var sourceFilter by remember { mutableStateOf(InventorySourceFilter.ALL) }
    var selectedTypeFilter by remember { mutableStateOf(ItemTypeCategory.ALL) }
    var selectedAffinityFilter by remember { mutableStateOf(ItemAffinityFilter.ALL) }
    var selectedQuantityFilter by remember { mutableStateOf(ItemQuantityFilter.ALL) }
    var sortMode by remember { mutableStateOf(InventorySortMode.TYPE) }
    var searchQuery by remember { mutableStateOf("") }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    var isFilterPanelExpanded by remember { mutableStateOf(false) }

    // Aggregate items based on viewMode
    val allItems = remember(player.items, player.storedItems, viewMode) {
        val bagItems = player.items.map { it.copy(isStored = false) }
        val storedItems = player.storedItems.map { it.copy(isStored = true) }
        when (viewMode) {
            InventoryViewMode.BAG -> bagItems
            InventoryViewMode.STORAGE -> storedItems
            InventoryViewMode.ALL -> bagItems + storedItems
        }
    }

    // Active filters count
    val activeFilterCount = (if (selectedTypeFilter != ItemTypeCategory.ALL) 1 else 0) +
            (if (selectedAffinityFilter != ItemAffinityFilter.ALL) 1 else 0) +
            (if (selectedQuantityFilter != ItemQuantityFilter.ALL) 1 else 0) +
            (if (sourceFilter != InventorySourceFilter.ALL) 1 else 0) +
            (if (searchQuery.isNotBlank()) 1 else 0)

    // Filter & Sort
    val displayedItems = remember(
        allItems,
        sourceFilter,
        selectedTypeFilter,
        selectedAffinityFilter,
        selectedQuantityFilter,
        sortMode,
        searchQuery
    ) {
        var list = allItems.filter { it.count > 0 }

        // Filter by Type
        list = list.filter { InventoryFilterUtils.matchesType(it, selectedTypeFilter) }

        // Filter by Affinity Bonus
        if (selectedAffinityFilter != ItemAffinityFilter.ALL) {
            list = list.filter { InventoryFilterUtils.getItemAffinityBonusValue(it) >= selectedAffinityFilter.minAffinity }
        }

        // Filter by Quantity
        list = list.filter { InventoryFilterUtils.matchesQuantity(it, selectedQuantityFilter) }

        // Source / Category filter
        list = when (sourceFilter) {
            InventorySourceFilter.ALL -> list
            InventorySourceFilter.EXPLORATION -> list.filter { it.source == "Průzkum" }
            InventorySourceFilter.COMBAT -> list.filter { it.source == "Boj" }
            InventorySourceFilter.GIFTS -> list.filter { it.category == "gift" }
            InventorySourceFilter.EQUIPMENT -> list.filter { it.category == "equipment" || it.equipSlot != null }
            InventorySourceFilter.POTIONS -> list.filter { it.category in listOf("potion", "combat", "consumable", "alchemy") }
        }

        // Search Query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.source.lowercase().contains(q) ||
                it.rarity.lowercase().contains(q) ||
                it.effectDescription.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }

        // Sorting by Type, Affinity Bonus, Quantity, Rarity, Price, Name, Source
        when (sortMode) {
            InventorySortMode.TYPE -> list.sortedWith(
                compareBy<InventoryItem> { InventoryFilterUtils.getItemCategoryOrderRank(it) }
                    .thenByDescending { InventoryFilterUtils.getItemAffinityBonusValue(it) }
                    .thenByDescending { it.count }
                    .thenBy { it.name }
            )
            InventorySortMode.AFFINITY_DESC -> list.sortedWith(
                compareByDescending<InventoryItem> { InventoryFilterUtils.getItemAffinityBonusValue(it) }
                    .thenByDescending { it.count }
                    .thenBy { it.name }
            )
            InventorySortMode.AFFINITY_ASC -> list.sortedWith(
                compareBy<InventoryItem> { InventoryFilterUtils.getItemAffinityBonusValue(it) }
                    .thenByDescending { it.count }
                    .thenBy { it.name }
            )
            InventorySortMode.COUNT_DESC -> list.sortedWith(
                compareByDescending<InventoryItem> { it.count }
                    .thenByDescending { InventoryFilterUtils.getRarityRank(it.rarity) }
                    .thenBy { it.name }
            )
            InventorySortMode.COUNT_ASC -> list.sortedWith(
                compareBy<InventoryItem> { it.count }
                    .thenBy { it.name }
            )
            InventorySortMode.RARITY_DESC -> list.sortedWith(
                compareByDescending<InventoryItem> { InventoryFilterUtils.getRarityRank(it.rarity) }
                    .thenByDescending { it.price }
                    .thenBy { it.name }
            )
            InventorySortMode.RARITY_ASC -> list.sortedWith(
                compareBy<InventoryItem> { InventoryFilterUtils.getRarityRank(it.rarity) }
                    .thenBy { it.name }
            )
            InventorySortMode.PRICE_DESC -> list.sortedByDescending { it.price * it.count }
            InventorySortMode.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            InventorySortMode.SOURCE -> list.sortedWith(
                compareBy<InventoryItem> { it.source }
                    .thenByDescending { InventoryFilterUtils.getRarityRank(it.rarity) }
            )
        }
    }

    val totalBagCount = remember(player.items) { player.items.sumOf { it.count } }
    val totalStorageCount = remember(player.storedItems) { player.storedItems.sumOf { it.count } }
    val explorationAndCombatCount = remember(player.items) {
        player.items.filter { it.source == "Průzkum" || it.source == "Boj" || it.category in listOf("artifact", "quest") }.sumOf { it.count }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- 1. QUICK LOOT STASH & SUMMARY CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "🏛️ Správa inventáře a zámeckého skladu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Batoh: $totalBagCount ks • Sklad dominia: $totalStorageCount ks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1-Click Store Loot Button
                Button(
                    onClick = {
                        val (stored, msg) = engine.storeAllExplorationAndCombatLoot()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = explorationAndCombatCount > 0
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (explorationAndCombatCount > 0) {
                            "📥 Uložit kořist z průzkumů a bojů do skladu ($explorationAndCombatCount ks)"
                        } else {
                            "Všechna bojová kořist je uložena ve skladu"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 2. VIEW MODE SWITCHER (Batoh / Sklad / Vše) ---
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            InventoryViewMode.values().forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = viewMode == mode,
                    onClick = { viewMode = mode },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = InventoryViewMode.values().size)
                ) {
                    Text("${mode.icon} ${mode.title}", fontSize = 11.sp)
                }
            }
        }

        // --- 3. SEARCH BAR, FILTER BUTTON & SORT BUTTON ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Hledat v inventáři...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Expand Toggle Button (with active filter badge)
            OutlinedButton(
                onClick = { isFilterPanelExpanded = !isFilterPanelExpanded },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isFilterPanelExpanded || activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                )
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = "Filtry", modifier = Modifier.size(18.dp))
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
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sort Menu Button
            Box {
                OutlinedButton(
                    onClick = { isSortMenuOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Řazení", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(sortMode.icon.ifEmpty { "🔀" }, fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = isSortMenuOpen,
                    onDismissRequest = { isSortMenuOpen = false }
                ) {
                    Text(
                        text = "Řadit předměty podle:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    HorizontalDivider()
                    InventorySortMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label, fontSize = 13.sp) },
                            onClick = {
                                sortMode = mode
                                isSortMenuOpen = false
                            },
                            trailingIcon = {
                                if (sortMode == mode) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- 4. QUICK SORT SHORTCUT CHIPS ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = sortMode == InventorySortMode.TYPE,
                    onClick = { sortMode = InventorySortMode.TYPE },
                    label = { Text("🏷️ Typ", fontSize = 11.sp, fontWeight = if (sortMode == InventorySortMode.TYPE) FontWeight.Bold else FontWeight.Normal) }
                )
            }
            item {
                FilterChip(
                    selected = sortMode == InventorySortMode.AFFINITY_DESC || sortMode == InventorySortMode.AFFINITY_ASC,
                    onClick = {
                        sortMode = if (sortMode == InventorySortMode.AFFINITY_DESC) InventorySortMode.AFFINITY_ASC else InventorySortMode.AFFINITY_DESC
                    },
                    label = {
                        val arrow = if (sortMode == InventorySortMode.AFFINITY_ASC) "▲" else "▼"
                        Text("💖 Náklonnost $arrow", fontSize = 11.sp, fontWeight = if (sortMode == InventorySortMode.AFFINITY_DESC || sortMode == InventorySortMode.AFFINITY_ASC) FontWeight.Bold else FontWeight.Normal)
                    }
                )
            }
            item {
                FilterChip(
                    selected = sortMode == InventorySortMode.COUNT_DESC || sortMode == InventorySortMode.COUNT_ASC,
                    onClick = {
                        sortMode = if (sortMode == InventorySortMode.COUNT_DESC) InventorySortMode.COUNT_ASC else InventorySortMode.COUNT_DESC
                    },
                    label = {
                        val arrow = if (sortMode == InventorySortMode.COUNT_ASC) "▲" else "▼"
                        Text("🔢 Počet $arrow", fontSize = 11.sp, fontWeight = if (sortMode == InventorySortMode.COUNT_DESC || sortMode == InventorySortMode.COUNT_ASC) FontWeight.Bold else FontWeight.Normal)
                    }
                )
            }
            item {
                FilterChip(
                    selected = sortMode == InventorySortMode.RARITY_DESC || sortMode == InventorySortMode.RARITY_ASC,
                    onClick = {
                        sortMode = if (sortMode == InventorySortMode.RARITY_DESC) InventorySortMode.RARITY_ASC else InventorySortMode.RARITY_DESC
                    },
                    label = {
                        val arrow = if (sortMode == InventorySortMode.RARITY_ASC) "▲" else "▼"
                        Text("💎 Vzácnost $arrow", fontSize = 11.sp, fontWeight = if (sortMode == InventorySortMode.RARITY_DESC || sortMode == InventorySortMode.RARITY_ASC) FontWeight.Bold else FontWeight.Normal)
                    }
                )
            }
            item {
                FilterChip(
                    selected = sortMode == InventorySortMode.SOURCE,
                    onClick = { sortMode = InventorySortMode.SOURCE },
                    label = { Text("🧭 Původ", fontSize = 11.sp) }
                )
            }
        }

        // --- 5. EXPANDABLE ADVANCED FILTER PANEL ---
        AnimatedVisibility(
            visible = isFilterPanelExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header with reset button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎯 Pokročilé filtry inventáře",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (activeFilterCount > 0) {
                            TextButton(
                                onClick = {
                                    selectedTypeFilter = ItemTypeCategory.ALL
                                    selectedAffinityFilter = ItemAffinityFilter.ALL
                                    selectedQuantityFilter = ItemQuantityFilter.ALL
                                    sourceFilter = InventorySourceFilter.ALL
                                    searchQuery = ""
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Vymazat filtry", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // A. FILTER BY TYPE
                    Text("🏷️ Typ / Kategorie předmětu:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(ItemTypeCategory.values()) { typeCat ->
                            FilterChip(
                                selected = selectedTypeFilter == typeCat,
                                onClick = { selectedTypeFilter = typeCat },
                                label = { Text("${typeCat.icon} ${typeCat.displayName}", fontSize = 10.sp) }
                            )
                        }
                    }

                    // B. FILTER BY AFFINITY BONUS
                    Text("💖 Bonus náklonnosti (pro dívky):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(ItemAffinityFilter.values()) { affFilter ->
                            FilterChip(
                                selected = selectedAffinityFilter == affFilter,
                                onClick = { selectedAffinityFilter = affFilter },
                                label = { Text("${affFilter.icon} ${affFilter.displayName}", fontSize = 10.sp) }
                            )
                        }
                    }

                    // C. FILTER BY QUANTITY
                    Text("🔢 Počet kusů na skladě / v batohu:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(ItemQuantityFilter.values()) { qtyFilter ->
                            FilterChip(
                                selected = selectedQuantityFilter == qtyFilter,
                                onClick = { selectedQuantityFilter = qtyFilter },
                                label = { Text("${qtyFilter.icon} ${qtyFilter.displayName}", fontSize = 10.sp) }
                            )
                        }
                    }

                    // D. FILTER BY SOURCE / ORIGIN
                    Text("🧭 Zdroj získání:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(InventorySourceFilter.values()) { sFilter ->
                            FilterChip(
                                selected = sourceFilter == sFilter,
                                onClick = { sourceFilter = sFilter },
                                label = { Text("${sFilter.icon} ${sFilter.label}", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Results Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${displayedItems.size} položek nalezeno",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Řazení: ${sortMode.label}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // --- 6. ITEM LIST ---
        if (displayedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📦", fontSize = 32.sp)
                    Text(
                        text = "V této kategorii nejsou žádné odpovídající předměty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (activeFilterCount > 0) {
                        TextButton(
                            onClick = {
                                selectedTypeFilter = ItemTypeCategory.ALL
                                selectedAffinityFilter = ItemAffinityFilter.ALL
                                selectedQuantityFilter = ItemQuantityFilter.ALL
                                sourceFilter = InventorySourceFilter.ALL
                                searchQuery = ""
                            }
                        ) {
                            Text("Vymazat všechny filtry", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedItems, key = { "${it.id}_${it.isStored}" }) { item ->
                    InventoryItemCard(
                        item = item,
                        activeCharacter = activeCharacter,
                        onStore = {
                            val (success, msg) = engine.storeItemToStorage(item.id, 1)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onWithdraw = {
                            val (success, msg) = engine.withdrawItemFromStorage(item.id, 1)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onUse = {
                            onUseItemOnCharacter?.invoke(item)
                        },
                        onToggleFavorite = {
                            engine.toggleItemFavorite(item.id, item.isStored)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    activeCharacter: Character?,
    onStore: () -> Unit,
    onWithdraw: () -> Unit,
    onUse: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rarityColor = getRarityColor(item.rarity)
    val affinityBonus = InventoryFilterUtils.getItemAffinityBonusValue(item)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon & Count Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(rarityColor.copy(alpha = 0.15f))
                    .border(1.dp, rarityColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 24.sp)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                ) {
                    Text(
                        text = "x${item.count}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.count >= 5) Color(0xFFFFD54F) else Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, false)
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Oblíbené (Chráněno)",
                            tint = if (item.isFavorite) Color(0xFFFF4081) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Badges Row: Category, Origin, Location, Affinity Bonus
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        val catLabel = when (item.category) {
                            "gift" -> "🎁 Dar"
                            "potion" -> "🧪 Lektvar"
                            "combat" -> "⚔️ Bojové"
                            "equipment" -> "🛡️ Výbava"
                            "quest" -> "📜 Úkol"
                            "artifact" -> "✨ Artefakt"
                            else -> "📦 ${item.category}"
                        }
                        Text(
                            text = catLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Affinity Bonus Badge (if applicable)
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

                    // Origin Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (item.source) {
                            "Průzkum" -> Color(0xFF00897B).copy(alpha = 0.2f)
                            "Boj" -> Color(0xFFD32F2F).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = when (item.source) {
                                "Průzkum" -> "🧭 Průzkum"
                                "Boj" -> "⚔️ Boj"
                                else -> "📦 Obchod"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.source) {
                                "Průzkum" -> Color(0xFF26A69A)
                                "Boj" -> Color(0xFFEF5350)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Stored Status Chip
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (item.isStored) Color(0xFF3F51B5).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (item.isStored) "🏛️ Sklad" else "🎒 Batoh",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isStored) Color(0xFF7986CB) else Color(0xFF81C784),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.effectDescription.isNotBlank()) {
                    Text(
                        text = "✨ ${item.effectDescription}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Actions Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.isStored) {
                    Button(
                        onClick = onWithdraw,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("📤 Do batohu", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStore,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("📥 Do skladu", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (activeCharacter != null && (item.category in listOf("gift", "potion", "combat", "consumable"))) {
                        FilledTonalButton(
                            onClick = onUse,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("💖 Použít", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun getRarityWeight(rarity: String): Int = when (rarity.lowercase()) {
    "mýtický", "mythic" -> 5
    "legendární", "legendary" -> 4
    "epický", "epic" -> 3
    "vzácný", "rare" -> 2
    else -> 1
}

private fun getRarityColor(rarity: String): Color = when (rarity.lowercase()) {
    "mýtický", "mythic" -> Color(0xFFFF1744)
    "legendární", "legendary" -> Color(0xFFFFD700)
    "epický", "epic" -> Color(0xFFAB47BC)
    "vzácný", "rare" -> Color(0xFF29B6F6)
    else -> Color(0xFF9E9E9E)
}
