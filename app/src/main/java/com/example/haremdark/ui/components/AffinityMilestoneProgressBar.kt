package com.example.haremdark.ui.components

import kotlinx.coroutines.launch
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.AffinityMilestoneBonusCatalog
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character

/**
 * Milestone rank definitions providing color-coded border frames based on
 * current affinity milestones reached:
 *  - UNRANKED (0-29 pts): Iron / Novice frame (Temná ocel)
 *  - BRONZE (30-69 pts): Classic burnished Bronze frame (Bronzový rám pro 30 bodů)
 *  - SILVER (70-119 pts): Radiant Silver frame (Stříbrný rám pro 70 bodů)
 *  - GOLD (120-179 pts): Brilliant Gold frame (Zlatý rám pro 120 bodů)
 *  - PLATINUM (180-249 pts): Celestial Platinum frame (Platinový rám pro 180 bodů)
 *  - DIAMOND (250-349 pts): Prismatic Diamond frame (Diamantový rám pro 250 bodů)
 *  - MASTER (350+ pts): Sovereign Crimson-Gold Master frame (Suverénní mistrovský rám pro 350 bodů)
 */
enum class AffinityMilestoneRank(
    val threshold: Int,
    val title: String,
    val rankLabel: String,
    val englishName: String,
    val rankBadgeIcon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val gradientColors: List<Color>,
    val glowColor: Color,
    val badgeBackgroundColor: Color,
    val badgeTextColor: Color,
    val borderWidth: Dp,
    val borderShadowElevation: Dp,
    val description: String
) {
    UNRANKED(
        threshold = 0,
        title = "Železo",
        rankLabel = "Železný novic",
        englishName = "Iron",
        rankBadgeIcon = "⛓️",
        primaryColor = Color(0xFF78909C),
        secondaryColor = Color(0xFF455A64),
        accentColor = Color(0xFFCFD8DC),
        gradientColors = listOf(Color(0xFF90A4AE), Color(0xFF607D8B), Color(0xFF37474F)),
        glowColor = Color(0x3378909C),
        badgeBackgroundColor = Color(0xFF21272B),
        badgeTextColor = Color(0xFFECEFF1),
        borderWidth = 1.2.dp,
        borderShadowElevation = 2.dp,
        description = "Základní novic pouta bez dosaženého milníku (<30 pts)."
    ),
    BRONZE(
        threshold = 30,
        title = "Bronz",
        rankLabel = "Bronzový rám",
        englishName = "Bronze",
        rankBadgeIcon = "🥉",
        primaryColor = Color(0xFFCD7F32), // Classic burnished bronze
        secondaryColor = Color(0xFF8D4F23), // Deep bronze shade
        accentColor = Color(0xFFFFA07A), // Polished copper highlight
        gradientColors = listOf(Color(0xFFE28C46), Color(0xFFCD7F32), Color(0xFF8D4F23), Color(0xFFFFA07A)),
        glowColor = Color(0x66CD7F32),
        badgeBackgroundColor = Color(0xFF2C1608),
        badgeTextColor = Color(0xFFFFB74D),
        borderWidth = 2.0.dp,
        borderShadowElevation = 5.dp,
        description = "Bronzový stupeň pouta dosažený při 30 bodech (První Pouto)."
    ),
    SILVER(
        threshold = 70,
        title = "Stříbro",
        rankLabel = "Stříbrný rám",
        englishName = "Silver",
        rankBadgeIcon = "🥈",
        primaryColor = Color(0xFFE0E0E0), // Lustrous silver
        secondaryColor = Color(0xFF9E9E9E), // Platinum silver
        accentColor = Color(0xFFFFFFFF), // Radiant white metallic reflection
        gradientColors = listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color(0xFF757575), Color(0xFFFFFFFF)),
        glowColor = Color(0x77E0E0E0),
        badgeBackgroundColor = Color(0xFF1B2228),
        badgeTextColor = Color(0xFFF5F5F5),
        borderWidth = 2.4.dp,
        borderShadowElevation = 7.dp,
        description = "Stříbrný stupeň pouta dosažený při 70 bodech (Důvěrnice komnat)."
    ),
    GOLD(
        threshold = 120,
        title = "Zlato",
        rankLabel = "Zlatý rám",
        englishName = "Gold",
        rankBadgeIcon = "🥇",
        primaryColor = Color(0xFFFFD700), // Pure brilliant gold
        secondaryColor = Color(0xFFFFA000), // Amber gold
        accentColor = Color(0xFFFFF9C4), // Lustrous gold sheen
        gradientColors = listOf(Color(0xFFFFF176), Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFFD700), Color(0xFFFFE082)),
        glowColor = Color(0x88FFD700),
        badgeBackgroundColor = Color(0xFF332004),
        badgeTextColor = Color(0xFFFFD700),
        borderWidth = 2.6.dp,
        borderShadowElevation = 9.dp,
        description = "Zlatý stupeň pouta dosažený při 120 bodech (Oddaná Milenka)."
    ),
    PLATINUM(
        threshold = 180,
        title = "Platina",
        rankLabel = "Platinový rám",
        englishName = "Platinum",
        rankBadgeIcon = "💠",
        primaryColor = Color(0xFF00E5FF), // Celestial platinum cyan
        secondaryColor = Color(0xFF0288D1),
        accentColor = Color(0xFFE0F7FA),
        gradientColors = listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF00E5FF), Color(0xFF0288D1), Color(0xFFE0F7FA)),
        glowColor = Color(0x8800E5FF),
        badgeBackgroundColor = Color(0xFF041E26),
        badgeTextColor = Color(0xFF80DEEA),
        borderWidth = 2.8.dp,
        borderShadowElevation = 10.dp,
        description = "Platinový stupeň pouta dosažený při 180 bodech (Paní Komnat)."
    ),
    DIAMOND(
        threshold = 250,
        title = "Diamant",
        rankLabel = "Diamantový rám",
        englishName = "Diamond",
        rankBadgeIcon = "💎",
        primaryColor = Color(0xFFE040FB), // Prismatic diamond violet-pink
        secondaryColor = Color(0xFF7C4DFF),
        accentColor = Color(0xFFF8BBD0),
        gradientColors = listOf(Color(0xFFF8BBD0), Color(0xFFEA80FC), Color(0xFFE040FB), Color(0xFF7C4DFF), Color(0xFFFFFFFF)),
        glowColor = Color(0x99E040FB),
        badgeBackgroundColor = Color(0xFF260833),
        badgeTextColor = Color(0xFFEA80FC),
        borderWidth = 3.0.dp,
        borderShadowElevation = 11.dp,
        description = "Diamantový stupeň pouta dosažený při 250 bodech (Spřízněná Duše)."
    ),
    MASTER(
        threshold = 350,
        title = "Věčný Mistr",
        rankLabel = "Mistrovský suverén",
        englishName = "Master",
        rankBadgeIcon = "👑",
        primaryColor = Color(0xFFFF1744), // Sovereign crimson & radiant gold
        secondaryColor = Color(0xFFFFD700),
        accentColor = Color(0xFFFF8A80),
        gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFF1744), Color(0xFFD50000), Color(0xFFFFD700), Color(0xFFFF80AB)),
        glowColor = Color(0xAAFF1744),
        badgeBackgroundColor = Color(0xFF380612),
        badgeTextColor = Color(0xFFFF80AB),
        borderWidth = 3.2.dp,
        borderShadowElevation = 13.dp,
        description = "Absolutní mistrovský suverénní stupeň dosažený při 350 bodech (Věčná Královna Dominia)."
    );

    companion object {
        fun getRankForPoints(points: Int): AffinityMilestoneRank {
            return when {
                points >= 350 -> MASTER
                points >= 250 -> DIAMOND
                points >= 180 -> PLATINUM
                points >= 120 -> GOLD
                points >= 70 -> SILVER
                points >= 30 -> BRONZE
                else -> UNRANKED
            }
        }

        fun getRankForMilestone(milestoneThreshold: Int): AffinityMilestoneRank {
            return when (milestoneThreshold) {
                350 -> MASTER
                250 -> DIAMOND
                180 -> PLATINUM
                120 -> GOLD
                70 -> SILVER
                30 -> BRONZE
                else -> UNRANKED
            }
        }
    }
}

/**
 * Data structure holding calculated milestone progression metrics,
 * including current milestone rank and dynamic color-coded frame configurations.
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
    val currentRank: AffinityMilestoneRank = AffinityMilestoneRank.getRankForPoints(currentPoints),
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
        val pointsNeeded = if (nextThreshold != null && !isMax) (nextThreshold - currentPoints).coerceAtLeast(0) else 0

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

        val currentRank = AffinityMilestoneRank.getRankForPoints(currentPoints)

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
            currentRank = currentRank,
            nextMilestoneBonus = bonus
        )
    }
}

/**
 * A prominent, interactive visual progress bar component that clearly displays
 * the player's progress toward the next affinity milestone with:
 *  - Color-coded border frames based on milestone rank (Bronze for 30, Silver for 70, Gold for 120, etc.)
 *  - Dynamic label showing current/next threshold values
 *  - Bracket percentage & shimmer progress indicator
 *  - Milestone node timeline
 *  - Upcoming perk preview
 */
@Composable
fun AffinityMilestoneProgressBar(
    character: Character,
    modifier: Modifier = Modifier,
    showPerkPreview: Boolean = true,
    showMilestoneNodes: Boolean = true,
    onMilestoneCrossed: ((Int) -> Unit)? = null,
    onMilestoneClick: ((Int) -> Unit)? = null
) {
    val progressData = remember(character.affinityPoints, character.archetypeId) {
        AffinityMilestoneProgressCalculator.calculate(character.affinityPoints, character.archetypeId)
    }

    val currentRank = progressData.currentRank
    var isExpandedDetails by remember { mutableStateOf(false) }
    var showRankDialog by remember { mutableStateOf(false) }

    // Milestone threshold celebration states & Lottie animation trigger
    var previousAffinityPoints by remember(character.id) { mutableIntStateOf(character.affinityPoints) }
    var activeCelebrationMilestone by remember { mutableStateOf<Int?>(null) }
    var showInBarCelebrationBurst by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val pulseScaleAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    val pulseGlowAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    // Automatic Lottie animation trigger whenever affinity crosses a milestone threshold
    // and smooth pulse effect when gaining affinity points
    LaunchedEffect(character.affinityPoints) {
        val newPoints = character.affinityPoints
        val oldPoints = previousAffinityPoints
        val crossed = AffinityData.AFFINITY_MILESTONES.lastOrNull { threshold ->
            (oldPoints < threshold && newPoints >= threshold) ||
            (newPoints >= threshold && threshold > character.lastAcknowledgedAffinityMilestone)
        }

        if (newPoints > oldPoints && oldPoints != 0) {
            // Subtle pulse scale and glow animation on earning new affinity points
            launch {
                pulseScaleAnim.animateTo(1.045f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                pulseScaleAnim.animateTo(0.985f, animationSpec = tween(120, easing = FastOutSlowInEasing))
                pulseScaleAnim.animateTo(1.0f, animationSpec = tween(100, easing = FastOutSlowInEasing))
            }
            launch {
                pulseGlowAnim.animateTo(0.6f, animationSpec = tween(150))
                pulseGlowAnim.animateTo(0f, animationSpec = tween(350))
            }
            HapticManager.vibrateClick()
        }

        if (crossed != null) {
            character.lastAcknowledgedAffinityMilestone = crossed
            activeCelebrationMilestone = crossed
            showInBarCelebrationBurst = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            HapticManager.vibrateMilestoneCelebration()
            SoundEffectManager.playMilestoneCelebration(crossed)
            onMilestoneCrossed?.invoke(crossed)
        }
        previousAffinityPoints = newPoints
    }

    // Smooth animated progress value with dynamic filling transition
    val animatedBracketProgress by animateFloatAsState(
        targetValue = progressData.bracketProgress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
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

    val rankBorderBrush = remember(currentRank) {
        Brush.linearGradient(colors = currentRank.gradientColors)
    }

    val gradientBrush = remember(currentRank, tierColor, accentGold) {
        Brush.horizontalGradient(
            colors = listOf(
                currentRank.primaryColor.copy(alpha = 0.9f),
                tierColor,
                currentRank.accentColor
            )
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF160E22)),
        border = BorderStroke(
            width = currentRank.borderWidth + (pulseGlowAnim.value * 1.5f).dp,
            brush = rankBorderBrush
        ),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pulseScaleAnim.value
                scaleY = pulseScaleAnim.value
            }
            .shadow(
                elevation = currentRank.borderShadowElevation + (pulseGlowAnim.value * 6f).dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = currentRank.glowColor,
                spotColor = currentRank.glowColor
            )
            .testTag("affinity_milestone_progress_bar_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- HEADER ROW: CURRENT STATUS, DYNAMIC THRESHOLD LABEL & COLOR-CODED RANK FRAME BADGE ---
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(currentRank.primaryColor.copy(alpha = 0.45f), Color(0xFF221133))
                                )
                            )
                            .border(currentRank.borderWidth.coerceAtMost(2.dp), rankBorderBrush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (progressData.isMaxMilestone) "👑" else currentRank.rankBadgeIcon,
                            fontSize = 17.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Postup k Milníku",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            // COLOR-CODED MILESTONE RANK BADGE CHIP (Bronze for 30, Silver for 70, Gold for 120, etc.)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = currentRank.badgeBackgroundColor,
                                border = BorderStroke(1.dp, currentRank.primaryColor.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .testTag("affinity_milestone_rank_frame_${currentRank.name.lowercase()}")
                                    .clickable {
                                        HapticManager.vibrateClick()
                                        showRankDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(currentRank.rankBadgeIcon, fontSize = 9.sp)
                                    Text(
                                        text = currentRank.rankLabel,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = currentRank.badgeTextColor
                                    )
                                }
                            }

                            // Manual Celebration Trigger Button to replay Lottie celebration
                            IconButton(
                                onClick = {
                                    val triggerThreshold = activeCelebrationMilestone ?: currentRank.threshold.takeIf { it > 0 } ?: 30
                                    HapticManager.vibrateMilestoneCelebration()
                                    SoundEffectManager.playMilestoneCelebration(triggerThreshold)
                                    activeCelebrationMilestone = triggerThreshold
                                    showInBarCelebrationBurst = true
                                },
                                modifier = Modifier
                                    .size(22.dp)
                                    .testTag("trigger_milestone_celebration_button")
                            ) {
                                Text("✨", fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = if (progressData.isMaxMilestone) {
                                "Dosažena absolutní úroveň"
                            } else {
                                "Cíl: ${progressData.nextBadgeTitle}"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (progressData.isMaxMilestone) currentRank.accentColor else Color(0xFFFF80AB)
                        )
                    }
                }

                // DYNAMIC CURRENT / NEXT THRESHOLD BADGE WITH COLOR-CODED RANK FRAME ACCENT
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF241138),
                    border = BorderStroke(1.2.dp, currentRank.primaryColor.copy(alpha = 0.85f)),
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
                            color = currentRank.accentColor
                        )
                    }
                }
            }

            // --- IN-BAR CELEBRATORY LOTTIE ANIMATION BANNER ---
            AnimatedVisibility(
                visible = showInBarCelebrationBurst && (activeCelebrationMilestone != null || currentRank != AffinityMilestoneRank.UNRANKED),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val celebrationThreshold = activeCelebrationMilestone ?: currentRank.threshold.takeIf { it > 0 } ?: 30
                val lottieType = remember(celebrationThreshold) { MilestoneLottieType.forThreshold(celebrationThreshold) }
                val jsonString = remember(lottieType) { MilestoneLottieJsonCatalog.getJson(lottieType) }
                val comp by rememberLottieComposition(LottieCompositionSpec.JsonString(jsonString))
                val animState = animateLottieCompositionAsState(
                    composition = comp,
                    isPlaying = true,
                    iterations = 3
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentRank.badgeBackgroundColor,
                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(currentRank.gradientColors)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("in_bar_lottie_celebration_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(currentRank.primaryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                comp?.let { c ->
                                    LottieAnimation(
                                        composition = c,
                                        progress = { animState.progress },
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "🎉 MILNÍK PŘEKROČEN!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = currentRank.accentColor
                                )
                                Text(
                                    text = "Odemčen ${currentRank.title} rám (${celebrationThreshold}p)!",
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    HapticManager.vibrateMilestoneCelebration()
                                    SoundEffectManager.playMilestoneCelebration(celebrationThreshold)
                                    activeCelebrationMilestone = celebrationThreshold
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = currentRank.primaryColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("play_celebration_lottie_button")
                            ) {
                                Text("Oslavit", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            IconButton(
                                onClick = { showInBarCelebrationBurst = false },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Zavřít",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- PROGRESS BAR WITH GLOW, COLOR-CODED FRAME & MILESTONE INDICATORS ---
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
                            "Všechny milníky odemčeny (${currentRank.title})"
                        } else {
                            "Od ${progressData.previousThreshold} bodů do ${progressData.nextThreshold} bodů"
                        },
                        fontSize = 10.sp,
                        color = Color.LightGray.copy(alpha = 0.8f)
                    )

                    Text(
                        text = if (progressData.isMaxMilestone) {
                            "100% SPLNĚNO"
                        } else {
                            "Zbývá ${progressData.pointsNeeded} pts (${(animatedBracketProgress * 100).toInt()}%)"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progressData.isMaxMilestone) Color(0xFF69F0AE) else currentRank.accentColor
                    )
                }

                // Main Visual Progress Bar with color-coded border frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D0616))
                        .border(
                            BorderStroke(
                                1.2.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        currentRank.primaryColor.copy(alpha = 0.8f),
                                        currentRank.secondaryColor.copy(alpha = 0.5f),
                                        currentRank.accentColor.copy(alpha = 0.8f)
                                    )
                                )
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .semantics {
                            contentDescription = "Postup náklonnosti: ${progressData.currentPoints} z ${progressData.nextThreshold ?: 350}, rám ${currentRank.title}"
                        }
                ) {
                    // Filled progress bar with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedBracketProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
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

            // --- MILESTONE NODES TRACKER (HORIZONTAL TICKS WITH RANK COLORS) ---
            if (showMilestoneNodes) {
                MilestoneNodesRow(
                    currentPoints = progressData.currentPoints,
                    milestones = AffinityData.AFFINITY_MILESTONES,
                    activeTierColor = tierColor,
                    onNodeClick = { threshold ->
                        if (onMilestoneClick != null) {
                            onMilestoneClick(threshold)
                        } else if (character.affinityPoints >= threshold) {
                            HapticManager.vibrateMilestoneCelebration()
                            SoundEffectManager.playMilestoneCelebration(threshold)
                            activeCelebrationMilestone = threshold
                            showInBarCelebrationBurst = true
                        }
                    }
                )
            }

            // --- UPCOMING MILESTONE PERK PREVIEW & EXPANDABLE CARD ---
            if (showPerkPreview && !progressData.isMaxMilestone && progressData.nextMilestoneBonus != null) {
                val bonus = progressData.nextMilestoneBonus
                val nextRank = AffinityMilestoneRank.getRankForMilestone(bonus.milestoneThreshold)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF221333),
                    border = BorderStroke(1.dp, nextRank.primaryColor.copy(alpha = 0.5f)),
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
                                    color = nextRank.accentColor
                                )
                                // Next Rank Frame Preview Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = nextRank.badgeBackgroundColor,
                                    border = BorderStroke(0.6.dp, nextRank.primaryColor)
                                ) {
                                    Text(
                                        text = "${nextRank.rankBadgeIcon} ${nextRank.title}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = nextRank.badgeTextColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    HapticManager.vibrateClick()
                                    isExpandedDetails = !isExpandedDetails
                                },
                                modifier = Modifier.size(24.dp)
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

    if (showRankDialog) {
        AffinityMilestoneRankDialog(
            currentRank = currentRank,
            currentPoints = progressData.currentPoints,
            onDismiss = { showRankDialog = false }
        )
    }

    // Celebratory Lottie animation overlay triggered upon crossing milestone threshold
    activeCelebrationMilestone?.let { milestoneThreshold ->
        AffinityMilestoneLottieOverlay(
            milestoneThreshold = milestoneThreshold,
            character = character,
            onDismiss = {
                activeCelebrationMilestone = null
            }
        )
    }
}

/**
 * Visual nodes timeline depicting the 6 milestones (30, 70, 120, 180, 250, 350)
 * with rank-themed status indicators and threshold points.
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
            val nodeRank = AffinityMilestoneRank.getRankForMilestone(threshold)

            val nodeBg = when {
                isUnlocked -> nodeRank.primaryColor.copy(alpha = 0.3f)
                isNextTarget -> nodeRank.accentColor.copy(alpha = 0.25f)
                else -> Color.Black.copy(alpha = 0.35f)
            }

            val nodeBorder = when {
                isUnlocked -> nodeRank.primaryColor
                isNextTarget -> nodeRank.accentColor
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
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(nodeBg)
                        .border(1.2.dp, nodeBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(nodeRank.rankBadgeIcon, fontSize = 10.sp)
                    } else if (isNextTarget) {
                        Text("🎯", fontSize = 9.sp)
                    } else {
                        Text("🔒", fontSize = 8.sp)
                    }
                }

                Text(
                    text = "${threshold}p",
                    fontSize = 8.sp,
                    fontWeight = if (isNextTarget || isUnlocked) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnlocked) nodeRank.accentColor else if (isNextTarget) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
    }
}

/**
 * Compact version designed for smaller cards, lists, character headers, or dialog summaries,
 * featuring the color-coded milestone rank frame and dynamic current/next threshold label.
 */
@Composable
fun CompactAffinityMilestoneProgressBar(
    character: Character,
    modifier: Modifier = Modifier,
    barHeight: Dp = 6.dp,
    onMilestoneCrossed: ((Int) -> Unit)? = null
) {
    val progressData = remember(character.affinityPoints, character.archetypeId) {
        AffinityMilestoneProgressCalculator.calculate(character.affinityPoints, character.archetypeId)
    }

    val currentRank = progressData.currentRank

    var previousCompactPoints by remember(character.id) { mutableIntStateOf(character.affinityPoints) }
    var compactCelebrationMilestone by remember { mutableStateOf<Int?>(null) }

    val haptic = LocalHapticFeedback.current
    val compactPulseScale = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(character.affinityPoints) {
        val newPoints = character.affinityPoints
        val oldPoints = previousCompactPoints
        val crossed = AffinityData.AFFINITY_MILESTONES.lastOrNull { threshold ->
            (oldPoints < threshold && newPoints >= threshold) ||
            (newPoints >= threshold && threshold > character.lastAcknowledgedAffinityMilestone)
        }

        if (newPoints > oldPoints && oldPoints != 0) {
            launch {
                compactPulseScale.animateTo(1.035f, animationSpec = tween(140, easing = FastOutSlowInEasing))
                compactPulseScale.animateTo(1.0f, animationSpec = tween(120, easing = FastOutSlowInEasing))
            }
            HapticManager.vibrateClick()
        }

        if (crossed != null) {
            character.lastAcknowledgedAffinityMilestone = crossed
            compactCelebrationMilestone = crossed
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            HapticManager.vibrateMilestoneCelebration()
            SoundEffectManager.playMilestoneCelebration(crossed)
            onMilestoneCrossed?.invoke(crossed)
        }
        previousCompactPoints = newPoints
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressData.bracketProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "CompactAffinityProgressAnim"
    )

    val currentTier = AffinityData.getTierForPoints(character.affinityPoints)
    val tierColor = Color(currentTier.colorHex)
    val accentGold = Color(0xFFFFD700)

    val rankBorderBrush = remember(currentRank) {
        Brush.horizontalGradient(currentRank.gradientColors)
    }

    // Color-coded outer container with rank frame border
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140A1F)),
        border = BorderStroke(
            width = currentRank.borderWidth.coerceAtMost(1.8.dp),
            brush = rankBorderBrush
        ),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = compactPulseScale.value
                scaleY = compactPulseScale.value
            }
            .testTag("compact_affinity_milestone_progress_bar_rank_${currentRank.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Dynamic Label showing current/next threshold values and milestone rank frame
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Color-Coded Rank Badge Chip
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = currentRank.badgeBackgroundColor,
                        border = BorderStroke(0.8.dp, currentRank.primaryColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(currentRank.rankBadgeIcon, fontSize = 9.sp)
                            Text(
                                text = currentRank.title,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentRank.badgeTextColor
                            )
                        }
                    }

                    Text(
                        text = if (progressData.isMaxMilestone) {
                            "MAX: ${progressData.currentBadgeTitle}"
                        } else {
                            "Cíl: ${progressData.nextBadgeTitle}"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dynamic current / next threshold text
                Text(
                    text = if (progressData.isMaxMilestone) {
                        "${progressData.currentPoints} pts (MAX)"
                    } else {
                        "${progressData.currentPoints} / ${progressData.nextThreshold} pts (${(animatedProgress * 100).toInt()}%)"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = currentRank.accentColor
                )
            }

            // Progress bar track with rank border frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(barHeight / 2))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(
                        0.8.dp,
                        Brush.horizontalGradient(
                            listOf(
                                currentRank.primaryColor.copy(alpha = 0.6f),
                                currentRank.accentColor.copy(alpha = 0.6f)
                            )
                        ),
                        RoundedCornerShape(barHeight / 2)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(barHeight / 2))
                        .background(
                            Brush.horizontalGradient(
                                listOf(currentRank.primaryColor, currentRank.accentColor, accentGold)
                            )
                        )
                )
            }
        }
    }

    // Celebratory Lottie animation overlay triggered upon crossing milestone threshold
    compactCelebrationMilestone?.let { threshold ->
        AffinityMilestoneLottieOverlay(
            milestoneThreshold = threshold,
            character = character,
            onDismiss = { compactCelebrationMilestone = null }
        )
    }
}

/**
 * Educational dialog explaining the color-coded milestone rank border frames.
 */
@Composable
fun AffinityMilestoneRankDialog(
    currentRank: AffinityMilestoneRank,
    currentPoints: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF180F25)),
            border = BorderStroke(2.dp, Brush.linearGradient(currentRank.gradientColors)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("affinity_milestone_rank_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(currentRank.rankBadgeIcon, fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Barevné Rámy Milníků",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tvůj aktuální rám: ${currentRank.title} ($currentPoints pts)",
                                fontSize = 11.sp,
                                color = currentRank.accentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Zavřít",
                            tint = Color.LightGray
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                Text(
                    text = "Barevné rámy se dynamicky odemykají a mění podle dosaženého milníku náklonnosti postavy. Každý rám reprezentuje hloubku a stabilitu vašeho pouta:",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 15.sp
                )

                // List of all ranks
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AffinityMilestoneRank.values()) { rank ->
                        val isCurrent = rank == currentRank
                        val isPassed = currentPoints >= rank.threshold

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) rank.badgeBackgroundColor else Color(0xFF221533),
                            border = BorderStroke(
                                width = if (isCurrent) rank.borderWidth else 1.dp,
                                brush = Brush.horizontalGradient(rank.gradientColors)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(rank.primaryColor.copy(alpha = 0.25f))
                                            .border(1.dp, rank.primaryColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(rank.rankBadgeIcon, fontSize = 13.sp)
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${rank.title} Rám",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = rank.accentColor
                                            )
                                            Text(
                                                text = "(${rank.threshold}+ pts)",
                                                fontSize = 10.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                        Text(
                                            text = rank.description,
                                            fontSize = 9.sp,
                                            color = Color.LightGray.copy(alpha = 0.85f),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = rank.primaryColor
                                    ) {
                                        Text(
                                            text = "AKTIVNÍ",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (isPassed) {
                                    Text("✓ Odemčeno", fontSize = 10.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                                } else {
                                    Text("🔒 Zamčeno", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = currentRank.primaryColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("close_rank_dialog_button")
                ) {
                    Text(
                        text = "Rozumím",
                        fontWeight = FontWeight.Bold,
                        color = if (currentRank == AffinityMilestoneRank.SILVER || currentRank == AffinityMilestoneRank.GOLD) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
