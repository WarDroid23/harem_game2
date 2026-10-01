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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.haremdark.data.BondTierCatalog
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.BondColorPalette
import com.example.haremdark.models.BondPortraitVariant
import com.example.haremdark.models.BondTierMilestone
import com.example.haremdark.models.Character

/**
 * Visual Bond Tier Customizer & Progression Showcase Modal.
 * Enables inspecting intimacy milestones, unlocking and equipping custom color palettes,
 * and swapping unlocked portrait artwork variants.
 */
@Composable
fun BondTierCustomizerModal(
    character: Character,
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val activePalette = remember(character.customPaletteId) {
        BondTierCatalog.getActivePalette(character)
    }
    val primaryColor = Color(activePalette.primaryColorHex)
    val secondaryColor = Color(activePalette.secondaryColorHex)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(
                2.dp,
                Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor, Color.Black)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar with Character Name, Tier Badge, Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentTier = remember(character.affinityPoints) {
                        BondTierCatalog.getTierForAffinity(character.affinityPoints)
                    }
                    val accentColor = Color(activePalette.accentColorHex)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.25f),
                            border = BorderStroke(1.5.dp, primaryColor)
                        ) {
                            Text(
                                text = currentTier.icon,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(6.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = character.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor
                                ) {
                                    Text(
                                        text = currentTier.stageTitle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (currentTier.tierLevel == 6) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Náklonnost: ${character.affinityPoints} pts • Aktivní paleta: ${activePalette.name}",
                                fontSize = 11.sp,
                                color = accentColor
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                BondTierCustomizerContent(
                    character = character,
                    engine = engine
                )
            }
        }
    }
}

/**
 * Extracted core content for Bond Tier customization to be used in both Modals and Tabs.
 */
@Composable
fun BondTierCustomizerContent(
    character: Character,
    engine: GameEngine
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Milníky & Přehled, 1: Barevné Palety, 2: Galerie Portrétů

    val currentTier = remember(character.affinityPoints) {
        BondTierCatalog.getTierForAffinity(character.affinityPoints)
    }
    val activePalette = remember(character.customPaletteId) {
        BondTierCatalog.getActivePalette(character)
    }
    val activePortraitRes = remember(character.customPortraitVariantId, character.equippedSkin, character.archetypeId) {
        BondTierCatalog.getActivePortraitRes(character)
    }
    val availablePortraits = remember(character.archetypeId) {
        BondTierCatalog.getAvailablePortraitsForCharacter(character)
    }

    val primaryColor = Color(activePalette.primaryColorHex)
    val accentColor = Color(activePalette.accentColorHex)

    Column(modifier = Modifier.fillMaxSize()) {
        // Live Preview Hero Banner
        LiveBondHeroPreview(
            character = character,
            currentTier = currentTier,
            activePalette = activePalette,
            activePortraitRes = activePortraitRes
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Switcher
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = primaryColor,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🏆", fontSize = 12.sp)
                        Text("Stupně Pouta", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🎨", fontSize = 12.sp)
                        Text("Palety (${BondTierCatalog.ALL_PALETTES.count { character.affinityLevel >= it.requiredAffinityLevel }}/${BondTierCatalog.ALL_PALETTES.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🖼️", fontSize = 12.sp)
                        Text("Portréty (${availablePortraits.count { character.affinityLevel >= it.requiredAffinityLevel }}/${availablePortraits.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> BondTierRoadmapTab(
                    character = character,
                    currentTier = currentTier,
                    onSelectPalette = { paletteId ->
                        val (success, msg) = engine.setCustomPalette(character.id, paletteId)
                        HapticManager.vibrateHeavy()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> BondPalettesTab(
                    character = character,
                    activePaletteId = character.customPaletteId,
                    onEquipPalette = { paletteId ->
                        val (success, msg) = engine.setCustomPalette(character.id, paletteId)
                        HapticManager.vibrateHeavy()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
                2 -> BondPortraitsTab(
                    character = character,
                    availablePortraits = availablePortraits,
                    activeVariantId = character.customPortraitVariantId,
                    onEquipPortrait = { variantId ->
                        val (success, msg) = engine.setCustomPortraitVariant(character.id, variantId)
                        HapticManager.vibrateHeavy()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

/**
 * Live interactive hero preview displaying the character in their equipped custom palette & portrait.
 */
@Composable
fun LiveBondHeroPreview(
    character: Character,
    currentTier: BondTierMilestone,
    activePalette: BondColorPalette,
    activePortraitRes: Int
) {
    val primaryColor = Color(activePalette.primaryColorHex)
    val accentColor = Color(activePalette.accentColorHex)
    val glowColor = Color(activePalette.glowColorHex)

    // Animated breathing aura pulse
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    colors = activePalette.cardGradientHexes.map { Color(it) }.ifEmpty { listOf(primaryColor, Color.Black) }
                ),
                RoundedCornerShape(16.dp)
            )
            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = glowColor, spotColor = glowColor),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Portrait Avatar with Glowing Aura Ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, primaryColor, RoundedCornerShape(14.dp))
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(activePortraitRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Tier badge in corner
                Surface(
                    shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 8.dp, topEnd = 0.dp, bottomStart = 0.dp),
                    color = primaryColor,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = currentTier.icon,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Companion Lore & Dynamic Active Buffs Summary
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = currentTier.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor
                    )
                }
                Text(
                    text = currentTier.subtitle,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Quote
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primaryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = currentTier.dialogueQuote,
                        fontSize = 10.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Affinity Progress Bar to next tier
                val nextTier = BondTierCatalog.BOND_TIERS.firstOrNull { it.tierLevel == currentTier.tierLevel + 1 }
                if (nextTier != null) {
                    val progress = ((character.affinityPoints - currentTier.minAffinityPoints).toFloat() /
                            (nextTier.minAffinityPoints - currentTier.minAffinityPoints).toFloat()).coerceIn(0f, 1f)

                    Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Postup na ${nextTier.stageTitle}", fontSize = 9.sp, color = Color.Gray)
                            Text("${character.affinityPoints} / ${nextTier.minAffinityPoints} pts", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = primaryColor,
                            trackColor = Color.DarkGray
                        )
                    }
                } else {
                    Text("👑 Dosažen maximální transcendentní stupeň pouta!", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                }
            }
        }
    }
}

/**
 * Roadmap tab listing all 6 Bond Tier Milestones with unlocked perks and dialogue.
 */
@Composable
fun BondTierRoadmapTab(
    character: Character,
    currentTier: BondTierMilestone,
    onSelectPalette: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(BondTierCatalog.BOND_TIERS, key = { it.tierLevel }) { tier ->
            val isUnlocked = character.affinityLevel >= tier.tierLevel
            val isCurrent = currentTier.tierLevel == tier.tierLevel
            val tierColor = Color(tier.palette.primaryColorHex)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color(tier.palette.primaryColorHex).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    if (isCurrent) 2.dp else 1.dp,
                    if (isCurrent) tierColor else if (isUnlocked) tierColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tier.icon, fontSize = 20.sp)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "${tier.stageTitle}: ${tier.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isUnlocked) tierColor else Color.Gray
                                    )
                                    if (isCurrent) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = tierColor) {
                                            Text("AKTUÁLNÍ", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = "Požadavek: ${tier.minAffinityPoints} bodů náklonnosti",
                                    fontSize = 10.sp,
                                    color = if (isUnlocked) Color.LightGray else Color.Gray
                                )
                            }
                        }

                        if (!isUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.DarkGray.copy(alpha = 0.8f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                    Text("Zamčeno", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            Text("✓ Odemčeno", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                        }
                    }

                    // Perk details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚔️ Bojový bonus:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tierColor)
                            Text(tier.combatPerkSummary, fontSize = 10.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("👑 Vizuální & Harém:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            Text(tier.haremPerkSummary, fontSize = 10.sp, color = Color.LightGray)
                        }
                    }

                    // Quote
                    if (isUnlocked) {
                        Text(
                            text = tier.dialogueQuote,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = tierColor.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab displaying all unlockable color palettes with swatches and equip buttons.
 */
@Composable
fun BondPalettesTab(
    character: Character,
    activePaletteId: String,
    onEquipPalette: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(BondTierCatalog.ALL_PALETTES, key = { it.id }) { palette ->
            val isUnlocked = character.affinityLevel >= palette.requiredAffinityLevel
            val isEquipped = activePaletteId == palette.id
            val primaryColor = Color(palette.primaryColorHex)
            val secondaryColor = Color(palette.secondaryColorHex)
            val accentColor = Color(palette.accentColorHex)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(
                    if (isEquipped) 2.dp else 1.dp,
                    if (isEquipped) primaryColor else if (isUnlocked) primaryColor.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(palette.icon, fontSize = 22.sp)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(palette.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    if (isEquipped) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = primaryColor) {
                                            Text("✓ AKTIVNÍ", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = if (palette.id == "imperial_gold") Color.Black else Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = "Odemčeno na Stupni Pouta ${palette.requiredAffinityLevel} (${palette.titleReward})",
                                    fontSize = 10.sp,
                                    color = if (isUnlocked) accentColor else Color.Gray
                                )
                            }
                        }

                        // Swatch Circles
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(primaryColor))
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(secondaryColor))
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(accentColor))
                        }
                    }

                    Text(
                        text = palette.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "✨ Bonusy: ${palette.statPerkSummary}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Equip Button
                    if (isUnlocked) {
                        if (!isEquipped) {
                            Button(
                                onClick = { onEquipPalette(palette.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("Aktivovat Paletu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (palette.id == "imperial_gold") Color.Black else Color.White)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vyžaduje Stupeň Pouta ${palette.requiredAffinityLevel}", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab displaying unlocked portrait variants and character skins.
 */
@Composable
fun BondPortraitsTab(
    character: Character,
    availablePortraits: List<BondPortraitVariant>,
    activeVariantId: String,
    onEquipPortrait: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Default Option
        item {
            val isDefaultActive = activeVariantId == "default" || activeVariantId.isBlank()
            val baseRes = StaticData.getPortraitForArchetype(character.archetypeId)
            PortraitVariantCard(
                title = "Výchozí Podoba",
                badgeIcon = "👤",
                requiredLevel = 1,
                isUnlocked = true,
                isEquipped = isDefaultActive,
                drawableRes = baseRes,
                description = "Základní podoba otrokyně v dominijních komnatách.",
                auraType = "NONE",
                onEquip = { onEquipPortrait("default") }
            )
        }

        items(availablePortraits, key = { it.id }) { variant ->
            val isUnlocked = character.affinityLevel >= variant.requiredAffinityLevel
            val isEquipped = activeVariantId == variant.id

            PortraitVariantCard(
                title = variant.title,
                badgeIcon = variant.badgeIcon,
                requiredLevel = variant.requiredAffinityLevel,
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                drawableRes = variant.drawableRes,
                description = variant.description,
                auraType = variant.auraParticleType,
                onEquip = { onEquipPortrait(variant.id) }
            )
        }
    }
}

@Composable
fun PortraitVariantCard(
    title: String,
    badgeIcon: String,
    requiredLevel: Int,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    drawableRes: Int,
    description: String,
    auraType: String,
    onEquip: () -> Unit
) {
    val tierColor = when (requiredLevel) {
        1 -> Color(0xFF78909C)
        2 -> Color(0xFF4CAF50)
        3 -> Color(0xFF00E5FF)
        4 -> Color(0xFFFF4081)
        5 -> Color(0xFFE040FB)
        else -> Color(0xFFFFD700)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            if (isEquipped) 2.dp else 1.dp,
            if (isEquipped) tierColor else if (isUnlocked) tierColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, if (isUnlocked) tierColor else Color.Gray, RoundedCornerShape(10.dp))
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(drawableRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (!isUnlocked) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                    }
                }

                // Top Badge
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    color = tierColor,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text("$badgeIcon Tier $requiredLevel", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (requiredLevel == 6) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }

                if (isEquipped) {
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 6.dp),
                        color = Color(0xFF00E676),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("✓ AKTIVNÍ", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = description,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isUnlocked) {
                if (!isEquipped) {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tierColor),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth().height(28.dp)
                    ) {
                        Text("Použít", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (requiredLevel == 6) Color.Black else Color.White)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.DarkGray.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Stupeň $requiredLevel+", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
