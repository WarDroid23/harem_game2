package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.R
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.*

@Composable
fun WorldMapScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDomainId by remember { mutableStateOf(gameState.currentDomainId) }
    val selectedDomain = DomainData.getDomainById(selectedDomainId)
    val player = gameState.player

    var showMilestonesModal by remember { mutableStateOf(false) }
    var selectedPoiForModal by remember { mutableStateOf<RegionPointOfInterest?>(null) }
    var showLoreModal by remember { mutableStateOf(false) }

    // Find the current active story milestone that needs completion
    val nextMilestone = StoryMilestoneData.MILESTONES.find { !gameState.completedMilestones.contains(it.id) }
        ?: StoryMilestoneData.MILESTONES.last()
    val isNextMilestoneCompleted = gameState.completedMilestones.contains(nextMilestone.id)
    val (canClaimNextMilestone, milestoneRequirements) = engine.checkMilestoneEligibility(nextMilestone)

    val explorationProgress = gameState.regionExplorationProgress[selectedDomain.id] ?: 0
    val dominionLevel = gameState.regionDominionLevel[selectedDomain.id] ?: 0

    val isUnlocked = gameState.unlockedDomains.contains(selectedDomain.id) || player.level >= selectedDomain.minPlayerLevel
    val isCurrent = gameState.currentDomainId == selectedDomain.id

    // Animation for milestone claim beacon
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // --- 1. STORY MILESTONE TIMELINE & PROGRESSION HEADER ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102A)),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (canClaimNextMilestone && !isNextMilestoneCompleted) 2.dp else 1.dp,
                    color = if (canClaimNextMilestone && !isNextMilestoneCompleted) Color(0xFFFFD700).copy(alpha = beaconGlow) else Color(0xFFE91E63).copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Text(nextMilestone.unlockCrestIcon, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "PŘÍBĚHOVÝ POSTUP SVĚTA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF80AB),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = nextMilestone.chapterTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { showMilestonesModal = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Všechny milníky",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }

                    Text(
                        text = nextMilestone.subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    // Objective checklist
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x550E0318), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        milestoneRequirements.forEach { (reqText, isDone) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isDone) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = reqText,
                                    fontSize = 11.sp,
                                    color = if (isDone) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.6f),
                                    fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Milestone Claim / Status Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎁 Odměna: +${nextMilestone.rewardGold} zl., +${nextMilestone.rewardDarkEnergy} TE, Odemčení teritorií",
                            fontSize = 10.sp,
                            color = Color(0xFFFFD700)
                        )

                        if (!isNextMilestoneCompleted) {
                            Button(
                                onClick = {
                                    val (success, msg) = engine.claimStoryMilestone(nextMilestone.id)
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                enabled = canClaimNextMilestone,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canClaimNextMilestone) Color(0xFFFFD700) else Color(0xFF424242),
                                    contentColor = if (canClaimNextMilestone) Color.Black else Color.LightGray
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (canClaimNextMilestone) "Odemknout kapitolu!" else "Podmínky nesplněny",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dokončeno", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. INTERACTIVE MINIMAP OVERLAY WITH FOG OF WAR ---
        item {
            com.example.haremdark.ui.components.MinimapOverlay(
                gameState = gameState,
                selectedDomainId = selectedDomainId,
                onDomainSelect = { id -> selectedDomainId = id }
            )
        }

        // --- 3. REGION SELECTOR CAROUSEL ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Oblasti temného světa (${gameState.unlockedDomains.size}/${DomainData.DOMAINS.size} odemčeno):",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = { showMilestonesModal = true }) {
                    Text("Kronika", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(DomainData.DOMAINS) { domain ->
                    val domainUnlocked = gameState.unlockedDomains.contains(domain.id) || player.level >= domain.minPlayerLevel
                    val domainCurrent = gameState.currentDomainId == domain.id
                    val domainSelected = selectedDomainId == domain.id
                    val domainExpl = gameState.regionExplorationProgress[domain.id] ?: 0

                    DomainChipCard(
                        domain = domain,
                        isSelected = domainSelected,
                        isCurrent = domainCurrent,
                        isUnlocked = domainUnlocked,
                        explorationPercent = domainExpl,
                        onClick = { selectedDomainId = domain.id }
                    )
                }
            }
        }

        // --- 4. SELECTED DOMAIN DETAILED SHOWCASE ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header & Difficulty Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedDomain.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = selectedDomain.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "📍 ${selectedDomain.region} • Kapitola ${selectedDomain.storyChapter}",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD700)
                            )
                        }

                        // Difficulty Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (selectedDomain.difficultyStars) {
                                1 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                2 -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                3 -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                4 -> Color(0xFFE91E63).copy(alpha = 0.2f)
                                else -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (selectedDomain.difficultyStars) {
                                    1 -> Color(0xFF4CAF50)
                                    2 -> Color(0xFF2196F3)
                                    3 -> Color(0xFFFF9800)
                                    4 -> Color(0xFFE91E63)
                                    else -> Color(0xFF9C27B0)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "★".repeat(selectedDomain.difficultyStars),
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedDomain.difficulty,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Weather & Environmental Affinities
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33000000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🌪️", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "Prostředí: ${selectedDomain.environmentWeather}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF80D8FF)
                                )
                                Text(
                                    text = "Efekt: ${selectedDomain.environmentBonus}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    // Lore Chronicle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "📜 Kronika oblasti:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedDomain.loreChronicle.ifEmpty { selectedDomain.description },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }

                    // Exploration & Subjugation Progress
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🧭 Prozkoumanost oblasti: $explorationProgress%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "👑 Nadvláda dominia: $dominionLevel%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { explorationProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Scout & Subjugate Quick Actions
                    if (isUnlocked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val (ok, msg) = engine.scoutRegion(selectedDomain.id)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rychlý průzkum (8 SE)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val (ok, msg) = engine.subjugateRegionDominion(selectedDomain.id)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = dominionLevel < 100 && explorationProgress >= 50,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
                            ) {
                                Icon(Icons.Default.Castle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (dominionLevel >= 100) "Podrobeno" else "Nadvláda (${150 + selectedDomain.difficultyStars * 50} zl.)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Points of Interest (POIs) in this region
                    if (selectedDomain.pointsOfInterest.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Text(
                            text = "✨ Významná místa a svatyně v oblasti:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedDomain.pointsOfInterest.forEach { poi ->
                                val isDiscovered = gameState.discoveredLandmarks.contains(poi.id)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(poi.type.colorHex).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedPoiForModal = poi
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(poi.type.icon, fontSize = 22.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = poi.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(poi.type.colorHex)
                                                )
                                                if (isDiscovered) {
                                                    Text("✓ Navštíveno", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(
                                                text = poi.description,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Character Reward Cards
                    Text(
                        text = "💎 Dívky a archetypy k získání v této oblasti:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        selectedDomain.potentialRewards.forEach { reward ->
                            CharacterRewardItem(reward = reward)
                        }
                    }

                    // Resource Drops
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📦 Naleziště surovin a relikvií:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedDomain.resourceDrops.joinToString(" • "),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Travel & Expedition Action Buttons
                    if (!isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Column {
                                    Text(
                                        text = "Zamčeno: ${selectedDomain.requiredMilestoneTitle ?: "Vyžaduje úroveň ${selectedDomain.minPlayerLevel}"}",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedDomain.subjugationRequirement != null) {
                                        Text(
                                            text = "Podmínka: ${selectedDomain.subjugationRequirement}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isCurrent) {
                                val alreadyVisited = gameState.unlockedDomains.contains(selectedDomain.id)
                                if (alreadyVisited) {
                                    Button(
                                        onClick = {
                                            val res = engine.fastTravelToDomain(selectedDomain.id)
                                            Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = "Rychlé cestování", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Rychlá cesta (1 SE)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val res = engine.travelToDomain(selectedDomain.id)
                                            Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Cestovat sem (${selectedDomain.travelCostEnergy} SE)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Zde sídlíš", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val res = engine.exploreDomain(selectedDomain.id)
                                    Toast.makeText(context, res.second, Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Výprava & Lov (${15 + selectedDomain.difficultyStars * 2} SE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: STORY MILESTONES ROADMAP ---
    if (showMilestonesModal) {
        Dialog(onDismissRequest = { showMilestonesModal = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF140722),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF80AB).copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚔️ KRONIKA PŘÍBĚHOVÝCH MILNÍKŮ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFFD700)
                        )
                        IconButton(onClick = { showMilestonesModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                        }
                    }

                    Text(
                        text = "Plněním úkolů, porážením bossů a rozšiřováním harému odemykáš nové kapitoly temného světa a jejich regiony.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(StoryMilestoneData.MILESTONES) { ms ->
                            val isCompleted = gameState.completedMilestones.contains(ms.id)
                            val (canClaim, checks) = engine.checkMilestoneEligibility(ms)

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCompleted) Color(0xFF261238) else Color(0xFF1C0D2A)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isCompleted) Color(0xFF4CAF50) else if (canClaim) Color(0xFFFFD700) else Color(0x33FFFFFF)
                                )
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
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(ms.unlockCrestIcon, fontSize = 18.sp)
                                            Text(
                                                text = ms.chapterTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isCompleted) Color(0xFF4CAF50) else Color.White
                                            )
                                        }

                                        if (isCompleted) {
                                            Text("✓ DOKONČENO", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                        } else if (canClaim) {
                                            Text("🌟 PŘIPRAVENO", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("🔒 UZAMČENO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text(
                                        text = ms.synopsis,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                        lineHeight = 14.sp
                                    )

                                    // Checks
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x44000000), RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        checks.forEach { (text, ok) ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = if (ok) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = text,
                                                    fontSize = 10.sp,
                                                    color = if (ok) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }

                                    // Unlocked domains
                                    val regionNames = ms.unlocksRegionIds.map { DomainData.getDomainById(it).name }.joinToString(", ")
                                    Text(
                                        text = "🗺️ Odemyká teritoria: $regionNames",
                                        fontSize = 10.sp,
                                        color = Color(0xFF80D8FF),
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (!isCompleted && canClaim) {
                                        Button(
                                            onClick = {
                                                val (ok, resMsg) = engine.claimStoryMilestone(ms.id)
                                                Toast.makeText(context, resMsg, Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Převzít odměny a odemknout teritoria", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

    // --- DIALOG: POINT OF INTEREST INTERACTION ---
    selectedPoiForModal?.let { poi ->
        Dialog(onDismissRequest = { selectedPoiForModal = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF160924),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(poi.type.colorHex)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Text(poi.type.icon, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = poi.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(poi.type.colorHex)
                                )
                                Text(
                                    text = poi.type.displayName,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(onClick = { selectedPoiForModal = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                        }
                    }

                    Text(
                        text = poi.description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x44000000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "✨ Možné efekty & dary:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = poi.rewardSummary,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val (ok, resMsg) = engine.interactWithRegionPoi(poi.id, selectedDomain.id)
                            Toast.makeText(context, resMsg, Toast.LENGTH_LONG).show()
                            selectedPoiForModal = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(poi.type.colorHex))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${poi.interactionLabel} (${poi.energyCost} SE)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DomainChipCard(
    domain: DomainLocation,
    isSelected: Boolean,
    isCurrent: Boolean,
    isUnlocked: Boolean,
    explorationPercent: Int = 0,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
        else null,
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★".repeat(domain.difficultyStars),
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!isUnlocked) {
                    Icon(Icons.Default.Lock, contentDescription = "Zamčeno", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                } else if (isCurrent) {
                    Text("📍", fontSize = 11.sp)
                } else {
                    Text("$explorationPercent%", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = domain.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Kap. ${domain.storyChapter} • ${domain.difficulty}",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CharacterRewardItem(reward: CharacterReward) {
    val portraitRes = StaticData.getPortraitForArchetype(reward.archetypeId)
    val rarityColor = when (reward.rarity) {
        "Legendární" -> Color(0xFFFFD700)
        "Epická" -> Color(0xFFE040FB)
        "Vzácná" -> Color(0xFF29B6F6)
        else -> Color(0xFF81C784)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, rarityColor, CircleShape)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(portraitRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = reward.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Fallback",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reward.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = rarityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = reward.rarity,
                            color = rarityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = reward.traitDescription,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${reward.dropRatePercent}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
        }
    }
}
