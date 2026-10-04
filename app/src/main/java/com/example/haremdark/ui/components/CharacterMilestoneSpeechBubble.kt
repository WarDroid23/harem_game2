package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.Character

/**
 * Animated, interactive Speech Bubble rendered in Character Profile screen.
 * Triggers celebratory animations when affinity milestones are crossed (30, 70, 120, 180, 250, 350)
 * and enables players to tap to cycle through unlocked flavor dialogue lines.
 */
@Composable
fun CharacterMilestoneSpeechBubble(
    character: Character,
    currentDialogue: String,
    isMilestoneCelebration: Boolean,
    milestoneTitle: String,
    onBubbleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Bounce / Pop animation on dialogue change or milestone trigger
    var triggerBounce by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (triggerBounce) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        finishedListener = { triggerBounce = false },
        label = "bubbleBounce"
    )

    LaunchedEffect(currentDialogue, isMilestoneCelebration) {
        triggerBounce = true
    }

    // Glowing border pulse for milestone celebrations
    val infiniteTransition = rememberInfiniteTransition(label = "milestoneGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    val currentTier = remember(character.affinityPoints) {
        AffinityData.getLevelForPoints(character.affinityPoints)
    }
    val tierInfo = remember(currentTier) {
        AffinityData.TIERS.find { it.level == currentTier } ?: AffinityData.TIERS.first()
    }

    val bubbleBorder = if (isMilestoneCelebration) {
        BorderStroke(
            2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = borderAlpha),
                    Color(0xFFFF4081).copy(alpha = borderAlpha),
                    Color(0xFFFFD700).copy(alpha = borderAlpha)
                )
            )
        )
    } else {
        BorderStroke(1.dp, Color(tierInfo.colorHex).copy(alpha = 0.45f))
    }

    val bubbleBg = if (isMilestoneCelebration) {
        Brush.verticalGradient(
            listOf(
                Color(0xF02A0B36),
                Color(0xF0180826)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xEE1E0B2B),
                Color(0xEE12061C)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Speech Bubble Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            border = bubbleBorder,
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .scale(bounceScale)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleBg)
                .clickable { onBubbleTap() }
                .testTag("character_milestone_speech_bubble")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header row: Badge and Tap Hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMilestoneCelebration) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "🌟 MILNÍK ODEMČEN: $milestoneTitle",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(tierInfo.colorHex).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(tierInfo.colorHex).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(tierInfo.icon, fontSize = 11.sp)
                                Text(
                                    text = "${tierInfo.title} (${character.affinityPoints} pts)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(tierInfo.colorHex)
                                )
                            }
                        }
                    }

                    // Interactive tap indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Klepni pro další",
                            fontSize = 9.sp,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Flavor Dialogue Text with Animated Content Transition
                AnimatedContent(
                    targetState = currentDialogue,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + slideInVertically { it / 3 })
                            .togetherWith(fadeOut(animationSpec = tween(150)) + slideOutVertically { -it / 3 })
                    },
                    label = "dialogueTextAnim"
                ) { text ->
                    Text(
                        text = if (text.startsWith("„") || text.startsWith("\"")) text else "„$text“",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = if (isMilestoneCelebration) Color(0xFFFFF9C4) else Color(0xFFF3E5F5),
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Subtle progress indicator to next milestone
                val nextMilestone = AffinityData.AFFINITY_MILESTONES.firstOrNull { it > character.affinityPoints }
                if (nextMilestone != null) {
                    val prevMilestone = AffinityData.AFFINITY_MILESTONES.lastOrNull { it <= character.affinityPoints } ?: 0
                    val progress = ((character.affinityPoints - prevMilestone).toFloat() / (nextMilestone - prevMilestone).toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(CircleShape),
                            color = Color(tierInfo.colorHex),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Text(
                            text = "Další milník: $nextMilestone pts",
                            fontSize = 8.sp,
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
