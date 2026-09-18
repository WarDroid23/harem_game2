package com.example.haremdark.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.AffinityTierInfo
import com.example.haremdark.models.Character

@Composable
fun RelationshipMilestoneSystem(
    character: Character,
    modifier: Modifier = Modifier
) {
    val currentPoints = character.affinityPoints
    val currentTier = AffinityData.getTierForPoints(currentPoints)
    val tiers = AffinityData.TIERS

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Milníky vztahu a odemčené výhody",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // The Progress Timeline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(110.dp)
        ) {
            // Background line
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .height(4.dp)
            ) {
                drawLine(
                    color = Color.DarkGray.copy(alpha = 0.3f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            // Filled progress line
            val totalMaxPoints = tiers.last().minPoints + 100 // Approximation for the end of the line
            val overallProgress = (currentPoints.toFloat() / totalMaxPoints.toFloat()).coerceIn(0f, 1f)
            val animatedOverallProgress by animateFloatAsState(
                targetValue = overallProgress,
                animationSpec = spring(stiffness = 100f),
                label = "OverallAffinityTimeline"
            )

            Canvas(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .height(4.dp)
            ) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE91E63),
                            Color(tiers[currentTier.level - 1].colorHex)
                        )
                    ),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * animatedOverallProgress, size.height / 2),
                    strokeWidth = 6.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            // Milestone Nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tiers.forEach { tier ->
                    MilestoneNode(
                        tier = tier,
                        isUnlocked = currentPoints >= tier.minPoints,
                        isCurrent = currentTier.level == tier.level,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }

        // Legend / Active Tier Details
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(currentTier.icon, fontSize = 20.sp)
                    Text(
                        text = "Aktivní úroveň: ${currentTier.title}",
                        fontWeight = FontWeight.Bold,
                        color = Color(currentTier.colorHex)
                    )
                }
                
                HorizontalDivider(alpha = 0.1f)

                MilestoneBenefitRow(
                    icon = "💬",
                    label = "Unikátní dialogy:",
                    value = "Odemčeno ${currentTier.level} sad reakcí",
                    color = Color(currentTier.colorHex)
                )

                MilestoneBenefitRow(
                    icon = "⚔️",
                    label = "Bojová synergie:",
                    value = currentTier.combatBonusDescription,
                    color = Color(currentTier.colorHex)
                )

                MilestoneBenefitRow(
                    icon = "✨",
                    label = "Speciální perk:",
                    value = currentTier.perkDescription,
                    color = Color(currentTier.colorHex)
                )
            }
        }
    }
}

@Composable
fun MilestoneNode(
    tier: AffinityTierInfo,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isUnlocked) Color(tier.colorHex) else Color.Gray.copy(alpha = 0.5f),
        label = "MilestoneNodeColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.25f else 1.0f,
        label = "MilestoneNodeScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(50.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) color.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                    width = if (isCurrent) 3.dp else 2.dp,
                    color = color,
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tier.icon,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = "Lvl ${tier.level}",
            fontSize = 9.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray,
            textAlign = TextAlign.Center
        )

        if (isCurrent) {
            Surface(
                color = color,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "AKTIVNÍ",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun MilestoneBenefitRow(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun HorizontalDivider(alpha: Float = 0.2f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
    )
}

