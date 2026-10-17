package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.AffinityMilestoneBonusCatalog
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character

/**
 * Data structure holding calculated milestone progression metrics.
 */
data class NextAffinityMilestoneProgress(
    val currentPoints: Int,
    val previousThreshold: Int,
    val nextThreshold: Int?,
    val isMaxMilestone: Boolean,
    val bracketProgress: Float, // 0.0 to 1.0 within current milestone bracket
    val totalProgress: Float,   // 0.0 to 1.0 from 0 to 350 max points
    val pointsNeeded: Int,
    val nextBadgeTitle: String,
    val nextBadgeSubtitle: String,
    val currentBadgeTitle: String,
    val nextMilestoneBonus: com.example.haremdark.data.AffinityMilestoneBonus?
)

object AffinityMilestoneProgressCalculator {

    fun calculate(currentPoints: Int, archetypeId: String = "subka"): NextAffinityMilestoneProgress {
        val milestones = AffinityData.AFFINITY_MILESTONES // [30, 70, 120, 180, 250, 350]
        val maxMilestone = milestones.last()

        val nextThreshold = milestones.firstOrNull { it > currentPoints }
        val prevThreshold = milestones.filter { it <= currentPoints }.maxOrNull() ?: 0

        val isMax = nextThreshold == null || currentPoints >= maxMilestone

        val bracketProgress = if (isMax) {
            1.0f
        } else {
            val span = (nextThreshold - prevThreshold).toFloat()
            if (span > 0) {
                ((currentPoints - prevThreshold).toFloat() / span).coerceIn(0f, 1f)
            } else 1.0f
        }

        val totalProgress = (currentPoints.toFloat() / maxMilestone.toFloat()).coerceIn(0f, 1f)
        val pointsNeeded = if (isMax || nextThreshold == null) 0 else (nextThreshold - currentPoints).coerceAtLeast(0)

        val nextBadge = if (nextThreshold != null) {
            AffinityData.getMilestoneBadgeInfo(nextThreshold)
        } else {
            Pair("👑 Věčná Královna", "Dosažen maximální milník")
        }

        val currentBadge = if (prevThreshold > 0) {
            AffinityData.getMilestoneBadgeInfo(prevThreshold).first
        } else {
            "⛓️ Cizinka"
        }

        val bonus = if (nextThreshold != null) {
            AffinityMilestoneBonusCatalog.getBonusForMilestone(archetypeId, nextThreshold)
        } else null

        return NextAffinityMilestoneProgress(
            currentPoints = currentPoints,
            previousThreshold = prevThreshold,
            nextThreshold = nextThreshold,
            isMaxMilestone = isMax,
            bracketProgress = bracketProgress,
            totalProgress = totalProgress,
            pointsNeeded = pointsNeeded,
            nextBadgeTitle = nextBadge.first,
            nextBadgeSubtitle = nextBadge.second,
            currentBadgeTitle = currentBadge,
            nextMilestoneBonus = bonus
        )
    }
}

/**
 * A prominent, interactive visual progress bar component that clearly displays
 * the player's progress toward the next affinity milestone with dynamic threshold labels,
 * bracket percentage, upcoming perk preview, and milestone nodes.
 */
@Composable
fun AffinityMilestoneProgressBar(
    character: Character,
    modifier: Modifier = Modifier,
    showPerkPreview: Boolean = true,
    showMilestoneNodes: Boolean = true,
    onMilestoneClick: ((Int) -> Unit)? = null
) {
    val progressData = remember(character.affinityPoints, character.archetypeId) {
        AffinityMilestoneProgressCalculator.calculate(character.affinityPoints, character.archetypeId)
    }

    var isExpandedDetails by remember { mutableStateOf(false) }

    // Smooth animated progress value
    val animatedBracketProgress by animateFloatAsState(
        targetValue = progressData.bracketProgress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "AffinityMilestoneProgressAnim"
    )

    // Shimmer glow pulse animation along the bar
    val infiniteTransition = rememberInfiniteTransition(label = "AffinityBarGlow")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerAlpha"
    )

    val currentTier = AffinityData.getTierForPoints(character.affinityPoints)
    val tierColor = Color(currentTier.colorHex)
    val accentGold = Color(0xFFFFD700)

    val gradientBrush = remember(tierColor, accentGold) {
        Brush.horizontalGradient(
            colors = listOf(
                tierColor.copy(alpha = 0.85f),
                tierColor,
                accentGold
            )
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF180F25)),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentGold.copy(alpha = 0.5f),
                    tierColor.copy(alpha = 0.3f),
                    Color(0xFF2B163B)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("affinity_milestone_progress_bar_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- HEADER ROW: CURRENT STATUS & DYNAMIC THRESHOLD LABEL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(tierColor.copy(alpha = 0.5f), Color(0xFF2F1746))
                                )
                            )
                            .border(1.dp, accentGold.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (progressData.isMaxMilestone) "👑" else currentTier.icon,
                            fontSize = 17.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Postup k Milníku Pouta",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (progressData.isMaxMilestone) {
                                "Dosažena absolutní úroveň"
                            } else {
                                "Cíl: ${progressData.nextBadgeTitle}"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (progressData.isMaxMilestone) accentGold else Color(0xFFFF80AB)
                        )
                    }
                }

                // DYNAMIC CURRENT / NEXT THRESHOLD BADGE
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF27133C),
                    border = BorderStroke(1.dp, accentGold.copy(alpha = 0.65f)),
                    modifier = Modifier.testTag("dynamic_affinity_threshold_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("💖", fontSize = 11.sp)
                        Text(
                            text = if (progressData.isMaxMilestone) {
                                "${progressData.currentPoints} pts (MAX)"
                            } else {
                                "${progressData.currentPoints} / ${progressData.nextThreshold} pts"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = accentGold
                        )
                    }
                }
            }

            // --- PROGRESS BAR WITH GLOW & MILESTONE INDICATORS ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Secondary info row: Points remaining & percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (progressData.isMaxMilestone) {
                            "Všechny milníky odemčeny"
                        } else {
                            "Od ${progressData.previousThreshold} bodů do ${progressData.nextThreshold} bodů"
                        },
                        fontSize = 10.sp,
                        color = Color.LightGray.copy(alpha = 0.75f)
                    )

                    Text(
                        text = if (progressData.isMaxMilestone) {
                            "100% SPLNĚNO"
                        } else {
                            "Zbývá ${progressData.pointsNeeded} pts (${(animatedBracketProgress * 100).toInt()}%)"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progressData.isMaxMilestone) Color(0xFF69F0AE) else Color(0xFFFFE082)
                    )
                }

                // Main Visual Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF0F0717))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(7.dp))
                        .semantics {
                            contentDescription = "Postup náklonnosti: ${progressData.currentPoints} z ${progressData.nextThreshold ?: 350}"
                        }
                ) {
                    // Filled progress bar with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedBracketProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(7.dp))
                            .background(gradientBrush)
                    )

                    // Shimmer head overlay at leading progress edge
                    if (animatedBracketProgress in 0.05f..0.98f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedBracketProgress)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = shimmerAlpha * 0.9f)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // --- MILESTONE NODES TRACKER (HORIZONTAL TICKS) ---
            if (showMilestoneNodes) {
                MilestoneNodesRow(
                    currentPoints = progressData.currentPoints,
                    milestones = AffinityData.AFFINITY_MILESTONES,
                    activeTierColor = tierColor,
                    onNodeClick = onMilestoneClick
                )
            }

            // --- UPCOMING MILESTONE PERK PREVIEW & EXPANDABLE CARD ---
            if (showPerkPreview && !progressData.isMaxMilestone && progressData.nextMilestoneBonus != null) {
                val bonus = progressData.nextMilestoneBonus
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF221333),
                    border = BorderStroke(0.8.dp, accentGold.copy(alpha = 0.35f)),
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
                                Text(bonus.badgeIcon, fontSize = 14.sp)
                                Text(
                                    text = "Odměna milníku (${bonus.milestoneThreshold} pts):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold
                                )
                            }

                            IconButton(
                                onClick = {
                                    HapticManager.vibrateClick()
                                    isExpandedDetails = !isExpandedDetails
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Více detailů",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Compact Perks Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF381B4B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "⚔️ +${bonus.hpBonus} HP • +${(bonus.attackBonusPercent * 100).toInt()}% ATK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF80AB),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (bonus.dailyGold > 0 || bonus.dailyMana > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E2840),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "💎 +${bonus.dailyGold} G • +${bonus.dailyMana} M/den",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF80DEEA),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Expanded view with narrative teaser and perk effects
                        AnimatedVisibility(
                            visible = isExpandedDetails,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Text(
                                    text = "📜 ${progressData.nextBadgeSubtitle}",
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.LightGray
                                )
                                if (bonus.combatPerkSummary.isNotBlank()) {
                                    Text(
                                        text = "⚡ Bojový efekt: ${bonus.combatPerkSummary}",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFD54F)
                                    )
                                }
                                if (bonus.resourcePerkSummary.isNotBlank()) {
                                    Text(
                                        text = "✨ Podpora dominia: ${bonus.resourcePerkSummary}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF80DEEA)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual nodes timeline depicting the 6 milestones (30, 70, 120, 180, 250, 350)
 * with status indicators and threshold points.
 */
@Composable
private fun MilestoneNodesRow(
    currentPoints: Int,
    milestones: List<Int>,
    activeTierColor: Color,
    onNodeClick: ((Int) -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        milestones.forEach { threshold ->
            val isUnlocked = currentPoints >= threshold
            val isNextTarget = !isUnlocked && (milestones.firstOrNull { it > currentPoints } == threshold)

            val nodeBg = when {
                isUnlocked -> Color(0xFF4CAF50).copy(alpha = 0.25f)
                isNextTarget -> Color(0xFFFFD700).copy(alpha = 0.25f)
                else -> Color.Black.copy(alpha = 0.35f)
            }

            val nodeBorder = when {
                isUnlocked -> Color(0xFF81C784)
                isNextTarget -> Color(0xFFFFD700)
                else -> Color.Gray.copy(alpha = 0.3f)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = onNodeClick != null) {
                        onNodeClick?.invoke(threshold)
                    }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(nodeBg)
                        .border(1.dp, nodeBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text("✓", fontSize = 10.sp, color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold)
                    } else if (isNextTarget) {
                        Text("🎯", fontSize = 9.sp)
                    } else {
                        Text("🔒", fontSize = 8.sp)
                    }
                }

                Text(
                    text = "${threshold}p",
                    fontSize = 8.sp,
                    fontWeight = if (isNextTarget) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnlocked) Color(0xFF81C784) else if (isNextTarget) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
    }
}

/**
 * Compact version designed for smaller cards, lists, character headers, or dialog summaries.
 */
@Composable
fun CompactAffinityMilestoneProgressBar(
    character: Character,
    modifier: Modifier = Modifier,
    barHeight: Dp = 6.dp
) {
    val progressData = remember(character.affinityPoints, character.archetypeId) {
        AffinityMilestoneProgressCalculator.calculate(character.affinityPoints, character.archetypeId)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressData.bracketProgress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "CompactAffinityProgressAnim"
    )

    val currentTier = AffinityData.getTierForPoints(character.affinityPoints)
    val tierColor = Color(currentTier.colorHex)
    val accentGold = Color(0xFFFFD700)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_affinity_milestone_progress_bar"),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Dynamic Label showing current/next threshold values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(currentTier.icon, fontSize = 11.sp)
                Text(
                    text = if (progressData.isMaxMilestone) {
                        "Milník MAX: ${progressData.currentBadgeTitle}"
                    } else {
                        "Milník: ${progressData.nextBadgeTitle}"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = if (progressData.isMaxMilestone) {
                    "${progressData.currentPoints} pts (MAX)"
                } else {
                    "${progressData.currentPoints} / ${progressData.nextThreshold} pts"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentGold
            )
        }

        // Progress bar line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(barHeight / 2))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(barHeight / 2))
                    .background(
                        Brush.horizontalGradient(
                            listOf(tierColor, accentGold)
                        )
                    )
            )
        }
    }
}
