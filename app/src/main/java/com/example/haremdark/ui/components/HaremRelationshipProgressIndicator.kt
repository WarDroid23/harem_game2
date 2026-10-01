package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.BondTierCatalog
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.HaremCharacter
import com.example.haremdark.models.BondTierMilestone
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Custom Compose component showcasing visual relationship progress indicators
 * for Harem relationship levels, now fully integrated with the Bond Tier system.
 */

@Composable
fun HaremRelationshipProgressIndicator(
    affinityPoints: Int,
    characterName: String,
    archetypeId: String = "draci_divka",
    loyalty: Int = 50,
    morale: Int = 50,
    trust: Int = 50,
    desire: Int = 50,
    submissiveness: Int = 50,
    onInteract: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bondTier = BondTierCatalog.getTierForAffinity(affinityPoints)
    val tiers = BondTierCatalog.BOND_TIERS
    
    val palette = bondTier.palette
    val primaryColor = Color(palette.primaryColorHex)
    
    val currentInTier = (affinityPoints - bondTier.minAffinityPoints).coerceAtLeast(0)
    val nextTier = tiers.getOrNull(bondTier.tierLevel)
    val tierSpan = if (nextTier != null) (nextTier.minAffinityPoints - bondTier.minAffinityPoints) else 100
    val progressFraction = (currentInTier.toFloat() / tierSpan.toFloat()).coerceIn(0f, 1f)

    var selectedTierIndex by remember { mutableIntStateOf((bondTier.tierLevel - 1).coerceIn(0, tiers.size - 1)) }
    var showPerksDialog by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ArcProgressAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0814)),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(bondTier.icon, fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Pouto vztahu & Náklonnost",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = bondTier.stageTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, primaryColor),
                    modifier = Modifier.clickable {
                        SoundEffectManager.playRelationshipTier(bondTier.tierLevel)
                        HapticManager.vibrateClick()
                        showPerksDialog = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("✨ výhody", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Custom Canvas Radial Arc Progress Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                RelationshipArcGauge(
                    progress = animatedProgress,
                    tierColor = primaryColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Center Badge Overlay inside Arc
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 18.dp)
                ) {
                    val heartPulseScale = rememberInfiniteTransition(label = "Pulse").animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "HeartPulse"
                    )

                    Text(
                        text = bondTier.icon,
                        fontSize = 32.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = heartPulseScale.value
                            scaleY = heartPulseScale.value
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "$currentInTier / $tierSpan PTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }

            // Multi-Stage Node Milestone Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Milníky vztahu (Stupně 1 - ${tiers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )

                RelationshipMilestoneTimeline(
                    tiers = tiers,
                    currentTierLevel = bondTier.tierLevel,
                    affinityPoints = affinityPoints,
                    selectedIndex = selectedTierIndex,
                    onSelectTier = { idx ->
                        selectedTierIndex = idx
                        HapticManager.vibrateClick()
                    }
                )
            }

            // Selected Milestone Info Preview Box
            val previewTier = tiers.getOrElse(selectedTierIndex) { bondTier }
            val isUnlocked = affinityPoints >= previewTier.minAffinityPoints
            val previewColor = Color(previewTier.palette.primaryColorHex)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A1226),
                border = BorderStroke(1.dp, if (isUnlocked) previewColor.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            Text(previewTier.icon, fontSize = 16.sp)
                            Text(
                                text = "${previewTier.stageTitle}: ${previewTier.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) previewColor else Color.Gray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFF424242)
                        ) {
                            Text(
                                text = if (isUnlocked) "ODEMČENO" else "ZAMČENO (${previewTier.minAffinityPoints} PTS)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "• Bojový bonus: ${previewTier.combatPerkSummary}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "• Výhoda harému: ${previewTier.haremPerkSummary}",
                        fontSize = 11.sp,
                        color = Color(0xFFFFD700).copy(alpha = 0.9f)
                    )
                }
            }

            // Sub-Attribute Breakdown
            RelationshipSubAttributesView(
                affection = affinityPoints,
                loyalty = loyalty,
                morale = morale,
                trust = trust,
                desire = desire
            )

            // Dynamic Thought / Active Dialogue Quote
            val quote = remember(affinityPoints, archetypeId) {
                AffinityData.getRandomActiveDialogue(affinityPoints, archetypeId)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF12071B),
                border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💭", fontSize = 16.sp)
                    Text(
                        text = "„$quote“",
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFFE1BEE7),
                        lineHeight = 15.sp
                    )
                }
            }

            // Optional Quick Action Triggers
            if (onInteract != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onInteract("talk") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                    ) {
                        Text("💬 Rozhovor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onInteract("gift") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAD1457))
                    ) {
                        Text("🎁 Dát dárek", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showPerksDialog) {
        RelationshipPerksDetailDialog(
            currentBondTier = bondTier,
            allBondTiers = tiers,
            affinityPoints = affinityPoints,
            onDismiss = { showPerksDialog = false }
        )
    }
}

/**
 * Universal Composable extension for HaremCharacter model
 */
@Composable
fun HaremRelationshipProgressIndicator(
    character: HaremCharacter,
    onInteract: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    HaremRelationshipProgressIndicator(
        affinityPoints = character.affection,
        characterName = character.name,
        archetypeId = character.archetypeId,
        loyalty = character.loyalty,
        morale = character.morale,
        trust = character.obedience,
        desire = character.affection,
        submissiveness = character.obedience,
        onInteract = onInteract,
        modifier = modifier
    )
}

/**
 * Universal Composable extension for Character model
 */
@Composable
fun HaremRelationshipProgressIndicator(
    character: Character,
    onInteract: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    HaremRelationshipProgressIndicator(
        affinityPoints = character.affinityPoints,
        characterName = character.name,
        archetypeId = character.archetypeId,
        loyalty = character.loajalita,
        morale = character.duvera,
        trust = character.duvera,
        desire = character.touha,
        submissiveness = character.submisivita,
        onInteract = onInteract,
        modifier = modifier
    )
}

/**
 * Condensed compact visual relationship progress bar for Character Cards / List Items
 */
@Composable
fun CompactRelationshipProgressIndicator(
    affinityPoints: Int,
    modifier: Modifier = Modifier
) {
    val bondTier = BondTierCatalog.getTierForAffinity(affinityPoints)
    val tiers = BondTierCatalog.BOND_TIERS
    
    val currentInTier = (affinityPoints - bondTier.minAffinityPoints).coerceAtLeast(0)
    val nextTier = tiers.getOrNull(bondTier.tierLevel)
    val tierSpan = if (nextTier != null) (nextTier.minAffinityPoints - bondTier.minAffinityPoints) else 100
    val progress = (currentInTier.toFloat() / tierSpan.toFloat()).coerceIn(0f, 1f)
    
    val primaryColor = Color(bondTier.palette.primaryColorHex)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = primaryColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(bondTier.icon, fontSize = 12.sp)
                    Text(
                        text = bondTier.stageTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                Text(
                    text = "$currentInTier/$tierSpan PTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Animated Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF4081),
                                    primaryColor
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * Custom Canvas Arc Progress Gauge with glowing track, start/end angles, and tick marks.
 */
@Composable
private fun RelationshipArcGauge(
    progress: Float,
    tierColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f + 10.dp.toPx())
        val radius = (minOf(width, height) / 2.2f)

        val startAngle = 150f
        val sweepAngle = 240f

        // Background Track Arc
        drawArc(
            color = Color.DarkGray.copy(alpha = 0.25f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
        )

        // Progress Arc with Gradient
        if (progress > 0f) {
            val progressSweep = sweepAngle * progress
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFE91E63),
                        tierColor,
                        Color(0xFFFFD700)
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            // Glowing Arc Tip Indicator Dot
            val endAngleRad = ((startAngle + progressSweep) * PI / 180f).toDouble()
            val tipX = center.x + radius * cos(endAngleRad).toFloat()
            val tipY = center.y + radius * sin(endAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = tierColor,
                radius = 5.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }

        // Tick marks at 25%, 50%, 75%
        for (i in 0..4) {
            val tickAngle = startAngle + (sweepAngle / 4f) * i
            val tickRad = (tickAngle * PI / 180f).toDouble()
            val innerR = radius - 12.dp.toPx()
            val outerR = radius + 12.dp.toPx()

            val startX = center.x + innerR * cos(tickRad).toFloat()
            val startY = center.y + innerR * sin(tickRad).toFloat()
            val endX = center.x + outerR * cos(tickRad).toFloat()
            val endY = center.y + outerR * sin(tickRad).toFloat()

            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Custom Multi-Stage Node Timeline connecting relationship tiers 1 to 6
 */
@Composable
private fun RelationshipMilestoneTimeline(
    tiers: List<BondTierMilestone>,
    currentTierLevel: Int,
    affinityPoints: Int,
    selectedIndex: Int,
    onSelectTier: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // Connecting Line in Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(horizontal = 24.dp)
        ) {
            val totalNodes = tiers.size
            val activeProgressRatio = ((currentTierLevel - 1).toFloat() / (totalNodes - 1).toFloat()).coerceIn(0f, 1f)

            // Background line
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.3f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Active glow line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE91E63),
                        Color(tiers[(currentTierLevel - 1).coerceIn(0, tiers.size - 1)].palette.primaryColorHex)
                    )
                ),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width * activeProgressRatio, size.height / 2),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Milestone Nodes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tiers.forEachIndexed { index, tier ->
                val isUnlocked = affinityPoints >= tier.minAffinityPoints
                val isCurrent = currentTierLevel == tier.tierLevel
                val isSelected = selectedIndex == index
                val tierColor = Color(tier.palette.primaryColorHex)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectTier(index) }
                        .padding(horizontal = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 36.dp else 30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) tierColor.copy(alpha = 0.25f) else Color(0xFF221530)
                            )
                            .border(
                                width = if (isSelected || isCurrent) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFD700) else if (isUnlocked) tierColor else Color.Gray.copy(alpha = 0.4f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tier.icon,
                            fontSize = if (isSelected) 16.sp else 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "St.${tier.tierLevel}",
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent || isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isUnlocked) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Breakdown of sub-attributes
 */
@Composable
private fun RelationshipSubAttributesView(
    affection: Int,
    loyalty: Int,
    morale: Int,
    trust: Int,
    desire: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Vlastnosti & Citový stav",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AttributeChip("💖 Pouto", "$affection pts", Color(0xFFFF4081), (affection / 250f).coerceIn(0f, 1f), Modifier.weight(1f))
            AttributeChip("🛡️ Loajalita", "$loyalty%", Color(0xFF4CAF50), loyalty / 100f, Modifier.weight(1f))
            AttributeChip("✨ Morálka", "$morale%", Color(0xFFFFD700), morale / 100f, Modifier.weight(1f))
            AttributeChip("💎 Důvěra", "$trust%", Color(0xFF00BCD4), trust / 100f, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AttributeChip(
    label: String,
    valueStr: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = valueStr,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
    }
}

/**
 * Dialog displaying full perk breakdown and dialogue quotes for all Bond Tiers
 */
@Composable
private fun RelationshipPerksDetailDialog(
    currentBondTier: BondTierMilestone,
    allBondTiers: List<BondTierMilestone>,
    affinityPoints: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("👑", fontSize = 24.sp)
                Text("Výhody Stupňů Pouta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allBondTiers, key = { it.tierLevel }) { itemTier ->
                    val isUnlocked = affinityPoints >= itemTier.minAffinityPoints
                    val tierColor = Color(itemTier.palette.primaryColorHex)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUnlocked) tierColor.copy(alpha = 0.12f) else Color(0xFF1B1126),
                        border = BorderStroke(1.dp, if (isUnlocked) tierColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(itemTier.icon, fontSize = 16.sp)
                                    Text(
                                        text = "${itemTier.stageTitle}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) tierColor else Color.Gray
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFF424242)
                                ) {
                                    Text(
                                        text = if (isUnlocked) "ODEMČENO" else "${itemTier.minAffinityPoints} PTS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "⚔️ ${itemTier.combatPerkSummary}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "✨ ${itemTier.haremPerkSummary}",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700).copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Text("Zavřít", color = Color.White)
            }
        },
        containerColor = Color(0xFF180A28)
    )
}
