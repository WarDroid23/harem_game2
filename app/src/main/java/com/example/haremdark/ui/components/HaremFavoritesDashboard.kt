package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave

/**
 * Top-level Quick Access Dashboard for Pinned and Favorited Harem members.
 * Enables rapid inspection, one-tap micro-interactions, gifting, loadout swapping, and consort management.
 */
@Composable
fun HaremFavoritesDashboard(
    characters: List<Character>,
    gameState: GameSave,
    engine: GameEngine,
    onOpenProfile: (Character) -> Unit,
    onOpenInteraction: (Character) -> Unit,
    onOpenGifting: (Character) -> Unit,
    onOpenLoadout: (Character) -> Unit,
    onOpenSkillTree: (Character) -> Unit,
    onOpenChamber: ((Character) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    var showPinSelectionDialog by remember { mutableStateOf(false) }
    var showConsortBonusesDialog by remember { mutableStateOf(false) }
    var selectedCharForBondCustomizer by remember { mutableStateOf<Character?>(null) }

    // Filter pinned or favorited girls
    val pinnedFavorites = remember(characters) {
        characters.filter { it.isPinned || it.oblibena }
            .sortedWith(compareByDescending<Character> { it.oblibena }.thenByDescending { it.srdce })
    }

    val primaryConsort = remember(characters) {
        characters.firstOrNull { it.oblibena }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.7f),
                    Color(0xFFE040FB).copy(alpha = 0.5f),
                    Color(0xFFFF4081).copy(alpha = 0.7f)
                )
            )
        ),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Bar with Pinned Count, Consort Badge, Pin Button, Expand Chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
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
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Rychlý Přístup k Oblíbenkyním",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (pinnedFavorites.isNotEmpty()) Color(0xFFE91E63) else Color.Gray.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "${pinnedFavorites.size} připnuto",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (primaryConsort != null) {
                            Text(
                                text = "👑 Hlavní Vyvolená: ${primaryConsort.name} (+10% poškození party)",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "💡 Připni si své nejdůležitější otrokyně pro bleskovou správu",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pin Button
                    OutlinedButton(
                        onClick = { showPinSelectionDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF80AB)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Spravovat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Consort Bonuses Info Button
                    IconButton(
                        onClick = { showConsortBonusesDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("👑", fontSize = 14.sp)
                    }

                    // Expand / Collapse Chevron
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Sbalit" else "Rozbalit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (pinnedFavorites.isEmpty()) {
                        // Empty State Guide
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📌", fontSize = 24.sp)
                                Text(
                                    text = "Zatím nemáš připnuté žádné oblíbenkyně",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Připnuté dívky se zobrazí v této rychlé liště pro okamžité polibky, dary, rozmluvy a úpravu výbavy bez nutnosti hledání.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { showPinSelectionDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE91E63),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⭐ Vybrat & Připnout dívky", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Horizontal List of Pinned Character Cards
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(pinnedFavorites, key = { it.id }) { character ->
                                FavoriteQuickAccessCard(
                                    character = character,
                                    isPrimaryConsort = character.oblibena,
                                    engine = engine,
                                    onOpenProfile = { onOpenProfile(character) },
                                    onOpenInteraction = { onOpenInteraction(character) },
                                    onOpenGifting = { onOpenGifting(character) },
                                    onOpenLoadout = { onOpenLoadout(character) },
                                    onOpenSkillTree = { onOpenSkillTree(character) },
                                    onOpenChamber = { onOpenChamber?.invoke(character) },
                                    onOpenBondCustomizer = { selectedCharForBondCustomizer = character }
                                )
                            }

                            // Add More Tile
                            item {
                                AddFavoriteCardTile(onClick = { showPinSelectionDialog = true })
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (showPinSelectionDialog) {
        FavoritesSelectionDialog(
            characters = characters,
            engine = engine,
            onDismiss = { showPinSelectionDialog = false }
        )
    }

    if (showConsortBonusesDialog) {
        ConsortBonusesDialog(
            primaryConsort = primaryConsort,
            pinnedCount = pinnedFavorites.size,
            onDismiss = { showConsortBonusesDialog = false }
        )
    }

    selectedCharForBondCustomizer?.let { char ->
        val currentConcubine = characters.firstOrNull { it.id == char.id } ?: char
        BondTierCustomizerModal(
            character = currentConcubine,
            engine = engine,
            onDismiss = { selectedCharForBondCustomizer = null }
        )
    }
}

/**
 * Rich Card component representing a single pinned favorite companion in the top quick dashboard.
 */
@Composable
fun FavoriteQuickAccessCard(
    character: Character,
    isPrimaryConsort: Boolean,
    engine: GameEngine,
    onOpenProfile: () -> Unit,
    onOpenInteraction: () -> Unit,
    onOpenGifting: () -> Unit,
    onOpenLoadout: () -> Unit,
    onOpenSkillTree: () -> Unit,
    onOpenChamber: () -> Unit,
    onOpenBondCustomizer: () -> Unit = {}
) {
    val context = LocalContext.current
    val portraitRes = com.example.haremdark.data.BondTierCatalog.getActivePortraitRes(character)
    val archetype = StaticData.ARCHETYPES[character.archetypeId]
    val bondPalette = com.example.haremdark.data.BondTierCatalog.getActivePalette(character)
    val bondTier = com.example.haremdark.data.BondTierCatalog.getTierForAffinity(character.affinityPoints)
    val primaryColor = Color(bondPalette.primaryColorHex)

    var showQuickMenu by remember { mutableStateOf(false) }

    val borderColor = if (isPrimaryConsort) Color(0xFFFFD700) else primaryColor
    val glowColor = if (isPrimaryConsort) Color(0xFFFFD700).copy(alpha = 0.35f) else Color(bondPalette.glowColorHex)

    Card(
        modifier = Modifier
            .width(185.dp)
            .border(
                width = if (isPrimaryConsort) 2.dp else 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = if (isPrimaryConsort) listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                    else listOf(primaryColor, Color(bondPalette.secondaryColorHex))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = glowColor, spotColor = glowColor),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Portrait + Badges Top Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenProfile() }
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(portraitRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 36.sp)
                        }
                    }
                )

                // Dynamic Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (bondPalette.cardGradientHexes.isNotEmpty()) {
                                    bondPalette.cardGradientHexes.map { Color(it) }
                                } else {
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                },
                                startY = 40f
                            )
                        )
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (isPrimaryConsort) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("👑", fontSize = 9.sp)
                                Text("HLAVNÍ", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.9f)
                        ) {
                            Text("📌 PŘIPNUTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }

                    // Mood emoji badge
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = character.statusIcon.ifBlank { "😊" },
                            fontSize = 11.sp,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }

                // Name & Archetype in overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = character.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${archetype?.name ?: "Otrokyně"} • Lv.${character.level}",
                        fontSize = 9.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stats / Affinity Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = primaryColor.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onOpenBondCustomizer() }
                ) {
                    Text(
                        text = "${bondTier.icon} Stupeň ${bondTier.tierLevel}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Text(
                    text = "❤️ ${character.srdce}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Micro-Interaction Buttons Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quick Kiss
                IconButton(
                    onClick = {
                        val result = engine.quickAffectionKiss(character.id)
                        HapticManager.vibrateHeavy()
                        Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .background(Color(0xFFE91E63).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Text("💋", fontSize = 13.sp)
                }

                // Quick Praise
                IconButton(
                    onClick = {
                        val result = engine.quickPraise(character.id)
                        HapticManager.vibrateHeavy()
                        Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Text("👑", fontSize = 13.sp)
                }

                // Quick Treat
                IconButton(
                    onClick = {
                        val result = engine.quickTreat(character.id)
                        HapticManager.vibrateHeavy()
                        Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .background(Color(0xFF81C784).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Text("🍰", fontSize = 13.sp)
                }

                // More Menu Button
                Box(modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = { showQuickMenu = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(14.dp))
                    }

                    DropdownMenu(
                        expanded = showQuickMenu,
                        onDismissRequest = { showQuickMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("💬 Rozhovor & Interakce", fontSize = 12.sp) },
                            onClick = {
                                showQuickMenu = false
                                onOpenInteraction()
                            },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("🎁 Dát Dárek", fontSize = 12.sp) },
                            onClick = {
                                showQuickMenu = false
                                onOpenGifting()
                            },
                            leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("🛡️ Výbava & Sady", fontSize = 12.sp) },
                            onClick = {
                                showQuickMenu = false
                                onOpenLoadout()
                            },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("🌳 Strom schopností", fontSize = 12.sp) },
                            onClick = {
                                showQuickMenu = false
                                onOpenSkillTree()
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("🎨 Vzhled, Palety & Pouto", fontSize = 12.sp) },
                            onClick = {
                                showQuickMenu = false
                                onOpenBondCustomizer()
                            },
                            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        HorizontalDivider()
                        if (!isPrimaryConsort) {
                            DropdownMenuItem(
                                text = { Text("👑 Ustanovit Hlavní Vyvolenou", fontSize = 12.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showQuickMenu = false
                                    val msg = engine.setFavorite(character.id)
                                    HapticManager.vibrateHeavy()
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Text("👑", fontSize = 14.sp) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("📍 Odepnout z oblíbených", fontSize = 12.sp, color = Color(0xFFFF5252)) },
                            onClick = {
                                showQuickMenu = false
                                val result = engine.togglePin(character.id)
                                Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                            },
                            leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add Companion Tile at the end of the carousel.
 */
@Composable
fun AddFavoriteCardTile(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Připnout", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Připnout další",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Zvolit dívku",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Modal dialog for pinning/unpinning companions to the favorites quick dashboard.
 */
@Composable
fun FavoritesSelectionDialog(
    characters: List<Character>,
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(characters, searchQuery) {
        if (searchQuery.isBlank()) characters
        else characters.filter { it.name.contains(searchQuery, ignoreCase = true) || it.archetypeId.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⭐", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Správa Oblíbených & Připnutých",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Připnuté dívky se zobrazují na vrcholu harému",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hledat v harému...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // List of Characters
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { character ->
                        val isPinned = character.isPinned || character.oblibena
                        val isPrimary = character.oblibena
                        val archetype = StaticData.ARCHETYPES[character.archetypeId]
                        val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPrimary) Color(0xFFFFD700).copy(alpha = 0.12f)
                            else if (isPinned) Color(0xFFE91E63).copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isPrimary) Color(0xFFFFD700).copy(alpha = 0.8f)
                                else if (isPinned) Color(0xFFE91E63).copy(alpha = 0.8f)
                                else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Mini Avatar
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.5.dp,
                                            if (isPrimary) Color(0xFFFFD700) else if (isPinned) Color(0xFFE91E63) else Color.Gray,
                                            CircleShape
                                        )
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(portraitRes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = character.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(character.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        if (isPrimary) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFD700)) {
                                                Text("👑 HLAVNÍ", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${archetype?.name ?: "Otrokyně"} • Úroveň ${character.level} • ❤️ ${character.srdce}%",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Action Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Primary Favorite Toggle
                                    IconButton(
                                        onClick = {
                                            val msg = engine.setFavorite(character.id)
                                            HapticManager.vibrateHeavy()
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (isPrimary) Color(0xFFFFD700).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text("👑", fontSize = 14.sp)
                                    }

                                    // Pin / Unpin Toggle
                                    IconButton(
                                        onClick = {
                                            val result = engine.togglePin(character.id)
                                            Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (isPinned) Color(0xFFE91E63).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.Add,
                                            contentDescription = "Připnout",
                                            tint = if (isPinned) Color(0xFFFF4081) else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Hotovo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Information modal explaining consort and favorites benefits.
 */
@Composable
fun ConsortBonusesDialog(
    primaryConsort: Character?,
    pinnedCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👑", fontSize = 24.sp)
                    Text("Výhody Vyvolených & Oblíbenkyň", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFFFD700))
                }

                Text(
                    text = "Systém Oblíbených ti umožňuje efektivně spravovat své nejcennější společnice a získat mocné pasivní imperiální buffy.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Primary Consort Perk Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("👑 Hlavní Vyvolená (Královská Konkubína)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD700))
                        Text(
                            text = if (primaryConsort != null) "Aktivní: ${primaryConsort.name}" else "Aktuálně není jmenována žádná hlavní vyvolená.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text("• +10% celkové bojové poškození v týmových bitvách", fontSize = 10.sp, color = Color.LightGray)
                        Text("• +15% zisk Náklonnosti (Affinity) z darů a rozhovorů", fontSize = 10.sp, color = Color.LightGray)
                        Text("• Imunita vůči poklesu morálky a vzpourám", fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                // Pinned Members Perk Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📌 Rychlý Přístup (Aktivní: $pinnedCount dívek)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFF80AB))
                        Text("• 1-klikové polibky, pochvaly a sladké lahůdky", fontSize = 10.sp, color = Color.LightGray)
                        Text("• Přímé otevírání výbavy, stromů schopností a darů", fontSize = 10.sp, color = Color.LightGray)
                        Text("• Prioritní nasazení v expedičních misích a expedicích", fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rozumím")
                }
            }
        }
    }
}
