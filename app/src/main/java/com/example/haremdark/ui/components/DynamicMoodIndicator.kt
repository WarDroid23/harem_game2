package com.example.haremdark.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.getSafeInteractionLogs

/**
 * State representing a dynamic character mood evaluated from both
 * the current affinity milestone level and recent interactions.
 */
data class DynamicMoodState(
    val title: String,
    val icon: String,
    val primaryColor: Color,
    val borderColor: Color,
    val glowColor: Color,
    val description: String,
    val recentInteractionImpact: String,
    val affinityInfluenceText: String,
    val moodScore: Int,
    val affinityPoints: Int,
    val affinityRankName: String
)

/**
 * Evaluates the dynamic mood state combining affinity progression
 * and the latest interaction history.
 */
object DynamicMoodEvaluator {

    fun evaluate(character: Character): DynamicMoodState {
        val affinity = character.affinityPoints
        val moodScore = character.moodScore.coerceIn(0, 100)
        val rank = AffinityMilestoneRank.getRankForPoints(affinity)

        val recentLog = character.interactionLogs.lastOrNull()
            ?: character.getSafeInteractionLogs().firstOrNull()

        val recentType = recentLog?.type?.lowercase() ?: ""
        val recentTitle = recentLog?.title ?: "Běžný den v komnatách"
        val recentDesc = recentLog?.description ?: ""

        // Check recent interaction influence
        val isRecentTrest = recentType in listOf("trest", "disciplína", "pokárání", "výprask") ||
                recentTitle.contains("trest", ignoreCase = true) || recentTitle.contains("pokárání", ignoreCase = true)

        val isRecentDar = recentType in listOf("dar", "odměna", "romance", "večeře") ||
                recentTitle.contains("dar", ignoreCase = true) || recentTitle.contains("večeře", ignoreCase = true)

        val isRecentIntimita = recentType in listOf("intimita", "noc", "masáž", "lázeň") ||
                recentTitle.contains("intim", ignoreCase = true) || recentTitle.contains("noc", ignoreCase = true)

        val isRecentVycvik = recentType in listOf("výcvik", "dril", "cvičení", "boj") ||
                recentTitle.contains("výcvik", ignoreCase = true) || recentTitle.contains("dril", ignoreCase = true)

        val isRecentRozhovor = recentType in listOf("rozhovor", "svěřování", "rada") ||
                recentTitle.contains("rozhovor", ignoreCase = true)

        return when {
            // 1. Recent punishment / discipline
            isRecentTrest -> {
                if (affinity >= 120) {
                    DynamicMoodState(
                        title = "Sladké pokání",
                        icon = "⛓️💜",
                        primaryColor = Color(0xFFBA68C8),
                        borderColor = Color(0xFFE1BEE7),
                        glowColor = Color(0x66BA68C8),
                        description = "I přes přísnost trestu ji silné pouto (${affinity} pts) naplňuje pokornou oddaností.",
                        recentInteractionImpact = "Nedávný trest: $recentTitle",
                        affinityInfluenceText = "Zlaté pouto (${rank.title}) přeměňuje pokárání v touhu zalíbit se pánovi.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                } else {
                    DynamicMoodState(
                        title = "Zastrašená a opatrná",
                        icon = "😨🌧️",
                        primaryColor = Color(0xFFFF7043),
                        borderColor = Color(0xFFFFCCBC),
                        glowColor = Color(0x66FF7043),
                        description = "Cítí dopad nedávného trestu a chová obezřetný odstup od pána.",
                        recentInteractionImpact = "Nedávný trest: $recentTitle",
                        affinityInfluenceText = "Nízké pouto (${affinity} pts) zatím nenabízí dostatek důvěry.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                }
            }

            // 2. Recent gift / reward / romance
            isRecentDar -> {
                if (affinity >= 120) {
                    DynamicMoodState(
                        title = "Zbožňující extáze",
                        icon = "💖✨",
                        primaryColor = Color(0xFFFF4081),
                        borderColor = Color(0xFFFF80AB),
                        glowColor = Color(0x88FF4081),
                        description = "Rozzářená tvář a tlukot srdce. Tvá štědrost ji naplňuje bezmeznou láskou.",
                        recentInteractionImpact = "Nedávný dar: $recentTitle",
                        affinityInfluenceText = "Vysoký stupeň náklonnosti (${affinity} pts) násobí radost z darů.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                } else if (affinity >= 30) {
                    DynamicMoodState(
                        title = "Potěšená a vřelá",
                        icon = "🥰🌸",
                        primaryColor = Color(0xFFF06292),
                        borderColor = Color(0xFFF8BBD0),
                        glowColor = Color(0x66F06292),
                        description = "Úsměv a vděčnost. Tvá pozornost posiluje její sympatie a otevřenost.",
                        recentInteractionImpact = "Nedávná přízeň: $recentTitle",
                        affinityInfluenceText = "Upevněné pouto (${rank.title}) vytváří pocit bezpečí.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                } else {
                    DynamicMoodState(
                        title = "Příjemně zaskočená",
                        icon = "😳🎁",
                        primaryColor = Color(0xFFFFB74D),
                        borderColor = Color(0xFFFFE0B2),
                        glowColor = Color(0x55FFB74D),
                        description = "Nečekala vlídnost. Začíná vnímat, že pán není jen přísný vládce.",
                        recentInteractionImpact = "Nedávný dar: $recentTitle",
                        affinityInfluenceText = "Počáteční novic pouta (${affinity} pts) začíná roztávat.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                }
            }

            // 3. Recent intimacy / bedchamber
            isRecentIntimita -> {
                if (affinity >= 120) {
                    DynamicMoodState(
                        title = "Nenasytná vášeň",
                        icon = "🔥💋",
                        primaryColor = Color(0xFFE91E63),
                        borderColor = Color(0xFFFF4081),
                        glowColor = Color(0x99E91E63),
                        description = "Hoří touhou po další blízkosti. Vzpomínka na noční chvíle v ní vře.",
                        recentInteractionImpact = "Nedávná intimita: $recentTitle",
                        affinityInfluenceText = "Zlaté/Diamantové pouto přetváří intimitu v magickou rozkoš.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                } else {
                    DynamicMoodState(
                        title = "Vzrušená a nesmělá",
                        icon = "🫦✨",
                        primaryColor = Color(0xFFFF80AB),
                        borderColor = Color(0xFFFFCDD2),
                        glowColor = Color(0x66FF80AB),
                        description = "Její tváře se červenají. Nedávné doteky v ní probudily novou zvědavost.",
                        recentInteractionImpact = "Nedávná intimita: $recentTitle",
                        affinityInfluenceText = "Pouto (${affinity} pts) postupně odemyká její smyslnost.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                }
            }

            // 4. Recent training / discipline
            isRecentVycvik -> {
                if (affinity >= 70) {
                    DynamicMoodState(
                        title = "Takticky odhodlaná",
                        icon = "⚔️⚡",
                        primaryColor = Color(0xFF00E5FF),
                        borderColor = Color(0xFF80D8FF),
                        glowColor = Color(0x6600E5FF),
                        description = "Je připravena bojovat po boku svého pána a krýt mu záda v každé bitvě.",
                        recentInteractionImpact = "Nedávný výcvik: $recentTitle",
                        affinityInfluenceText = "Bojová synergie pouta (${affinity} pts) zajišťuje plnou koordinaci.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                } else {
                    DynamicMoodState(
                        title = "Ukázněná a soustředěná",
                        icon = "🛡️⚡",
                        primaryColor = Color(0xFF64B5F6),
                        borderColor = Color(0xFFBBDEFB),
                        glowColor = Color(0x5564B5F6),
                        description = "Plní zadané úkoly a udržuje se v bojové pohotovosti.",
                        recentInteractionImpact = "Nedávný výcvik: $recentTitle",
                        affinityInfluenceText = "Výcvik formuje disciplínu v raném stádiu pouta.",
                        moodScore = moodScore,
                        affinityPoints = affinity,
                        affinityRankName = rank.title
                    )
                }
            }

            // 5. Recent conversation / trust
            isRecentRozhovor -> {
                DynamicMoodState(
                    title = "Otevřená a klidná",
                    icon = "🤝🌤️",
                    primaryColor = Color(0xFF81C784),
                    borderColor = Color(0xFFC8E6C9),
                    glowColor = Color(0x6681C784),
                    description = "Cítí se vyslechnuta. Osobní rozhovor prohloubil vzájemnou důvěru.",
                    recentInteractionImpact = "Nedávný rozhovor: $recentTitle",
                    affinityInfluenceText = "Pouto (${affinity} pts) posiluje duševní klid a porozumění.",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            // 6. Affinity-tier baseline states
            affinity >= 350 -> {
                DynamicMoodState(
                    title = "Suverénní harmonie",
                    icon = "👑💖",
                    primaryColor = Color(0xFFFFD700),
                    borderColor = Color(0xFFFFF9C4),
                    glowColor = Color(0x88FFD700),
                    description = "Dosažen absolutní suverénní milník. Její bytost a štěstí jsou navždy spojeny s tvým panstvím.",
                    recentInteractionImpact = "Trvalá přítomnost pána",
                    affinityInfluenceText = "Věčný Mistr pouta (350+ pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            affinity >= 250 -> {
                DynamicMoodState(
                    title = "Božská oddanost",
                    icon = "💎✨",
                    primaryColor = Color(0xFF00E5FF),
                    borderColor = Color(0xFFE0F7FA),
                    glowColor = Color(0x7700E5FF),
                    description = "Hluboké pouto s pánem prozařuje její nitro. Cítí se uctívána a milována.",
                    recentInteractionImpact = "Dlouhodobé soužití",
                    affinityInfluenceText = "Diamantový milník (${affinity} pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            affinity >= 180 -> {
                DynamicMoodState(
                    title = "Spřízněná duše",
                    icon = "💠💜",
                    primaryColor = Color(0xFFB388FF),
                    borderColor = Color(0xFFEDE7F6),
                    glowColor = Color(0x66B388FF),
                    description = "Nerozlučné pouto duší. Rozumí ti i beze slov a cítí tvé emoce.",
                    recentInteractionImpact = "Dlouhodobé soužití",
                    affinityInfluenceText = "Platinový milník (${affinity} pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            affinity >= 120 -> {
                DynamicMoodState(
                    title = "Oddaná láska",
                    icon = "😍💖",
                    primaryColor = Color(0xFFFF4081),
                    borderColor = Color(0xFFFF80AB),
                    glowColor = Color(0x66FF4081),
                    description = "Plně ti důvěřuje a vidí v tobě svého ochránce i životní lásku.",
                    recentInteractionImpact = "Pravidelná péče",
                    affinityInfluenceText = "Zlatý milník pouta (${affinity} pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            affinity >= 70 -> {
                DynamicMoodState(
                    title = "Vřelá důvěrnice",
                    icon = "😊🌸",
                    primaryColor = Color(0xFF66BB6A),
                    borderColor = Color(0xFFA5D6A7),
                    glowColor = Color(0x5566BB6A),
                    description = "Je v tvé přítomnosti uvolněná, upřímná a ochotná sdílet svá tajemství.",
                    recentInteractionImpact = "Poklidný den",
                    affinityInfluenceText = "Stříbrný milník pouta (${affinity} pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            affinity >= 30 -> {
                DynamicMoodState(
                    title = "Vstřícná",
                    icon = "🙂🌿",
                    primaryColor = Color(0xFF4DD0E1),
                    borderColor = Color(0xFFB2EBF2),
                    glowColor = Color(0x444DD0E1),
                    description = "Počáteční chlad ustoupil. Začíná se zajímat o tvé plány a přeje si tvou chválu.",
                    recentInteractionImpact = "Budování pouta",
                    affinityInfluenceText = "Bronzový milník pouta (${affinity} pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }

            else -> {
                DynamicMoodState(
                    title = "Opatrná zdrženlivost",
                    icon = "😐⛓️",
                    primaryColor = Color(0xFF90A4AE),
                    borderColor = Color(0xFFCFD8DC),
                    glowColor = Color(0x3390A4AE),
                    description = "Zatím si tě ostražitě prohlíží. Důvěru si teprve musíš zasloužit činy a dary.",
                    recentInteractionImpact = "Nová otrokyně",
                    affinityInfluenceText = "Železný novic pouta (${affinity} pts).",
                    moodScore = moodScore,
                    affinityPoints = affinity,
                    affinityRankName = rank.title
                )
            }
        }
    }
}

/**
 * Compact circular Mood Indicator designed to sit on or immediately adjacent to
 * the character avatar (e.g. at the bottom-end corner in avatar circles).
 */
@Composable
fun DynamicMoodAvatarCornerBadge(
    character: Character,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 20.dp
) {
    val moodState = remember(character.affinityPoints, character.moodScore, character.interactionLogs.size) {
        DynamicMoodEvaluator.evaluate(character)
    }

    var showDetailDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "MoodPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MoodGlowAnim"
    )

    Box(
        modifier = modifier
            .size(badgeSize)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = moodState.glowColor,
                spotColor = moodState.glowColor
            )
            .clip(CircleShape)
            .background(Color(0xFF1A1026))
            .border(
                width = 1.5.dp,
                color = moodState.primaryColor.copy(alpha = pulseGlow),
                shape = CircleShape
            )
            .clickable {
                HapticManager.vibrateClick()
                showDetailDialog = true
            }
            .semantics { contentDescription = "Aktuální nálada: ${moodState.title}" }
            .testTag("dynamic_mood_avatar_corner_badge"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = moodState.icon.take(2),
            fontSize = (badgeSize.value * 0.55f).sp,
            textAlign = TextAlign.Center
        )
    }

    if (showDetailDialog) {
        DynamicMoodDetailDialog(
            character = character,
            moodState = moodState,
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * Prominent dynamic Mood Indicator chip / badge designed for character detail headers,
 * dialogues, or profile cards directly next to the avatar.
 */
@Composable
fun DynamicMoodAvatarHeaderBadge(
    character: Character,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val moodState = remember(character.affinityPoints, character.moodScore, character.interactionLogs.size) {
        DynamicMoodEvaluator.evaluate(character)
    }

    var showDetailDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "HeaderMoodPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeaderMoodAlpha"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1C0F2A).copy(alpha = 0.85f),
        border = BorderStroke(
            width = 1.2.dp,
            color = moodState.primaryColor.copy(alpha = pulseAlpha)
        ),
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(12.dp), ambientColor = moodState.glowColor)
            .clickable {
                HapticManager.vibrateClick()
                showDetailDialog = true
            }
            .semantics { contentDescription = "Nálada: ${moodState.title} (${moodState.affinityRankName})" }
            .testTag("dynamic_mood_header_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Animated Status Dot with Mood Color
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(moodState.primaryColor.copy(alpha = pulseAlpha))
            )

            Text(moodState.icon, fontSize = 13.sp)

            Text(
                text = moodState.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = moodState.primaryColor
            )

            if (!compact) {
                Text(
                    text = "(${moodState.affinityRankName})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDetailDialog) {
        DynamicMoodDetailDialog(
            character = character,
            moodState = moodState,
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * Detailed modal explaining the character's dynamic mood breakdown,
 * recent interaction consequences, and affinity progression impact.
 */
@Composable
fun DynamicMoodDetailDialog(
    character: Character,
    moodState: DynamicMoodState,
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val trendSummary = remember(character.affinityPoints, character.interactionLogs.size) {
        DynamicMood24hEvaluator.evaluate24Hours(character)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF170C24)),
            border = BorderStroke(1.5.dp, moodState.primaryColor.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("dynamic_mood_detail_dialog")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(moodState.primaryColor.copy(alpha = 0.2f))
                                .border(1.2.dp, moodState.primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(moodState.icon, fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = moodState.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = moodState.primaryColor
                            )
                            Text(
                                text = "Ukazatel nálady: ${character.name}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = moodState.primaryColor.copy(alpha = 0.2f))

                // Navigation Tabs next to / within Mood Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("💭 Nálada", "🔥 24h Heat Mapa", "📜 Log & Trendy").forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) moodState.primaryColor.copy(alpha = 0.85f) else Color(0xFF221136),
                            border = BorderStroke(1.dp, if (isSelected) Color.White.copy(alpha = 0.6f) else Color(0xFF381F4E)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    HapticManager.vibrateClick()
                                    selectedTab = index
                                }
                                .testTag("mood_dialog_tab_$index")
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFFCE93D8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Mood Description Card
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, moodState.primaryColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "💭 Vnitřní rozpoložení",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = moodState.primaryColor
                                        )
                                        Text(
                                            text = moodState.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                // Recent Interaction Influence
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF221332),
                                    border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("⚡", fontSize = 13.sp)
                                            Text("Vliv nedávných interakcí", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8))
                                        }
                                        Text(
                                            text = moodState.recentInteractionImpact,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFE1BEE7)
                                        )
                                    }
                                }

                                // Affinity Milestone Level Influence
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF14241E),
                                    border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("💖", fontSize = 13.sp)
                                            Text("Vliv úrovně náklonnosti (${moodState.affinityRankName} • ${moodState.affinityPoints} pts)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                                        }
                                        Text(
                                            text = moodState.affinityInfluenceText,
                                            fontSize = 11.sp,
                                            color = Color(0xFFC8E6C9)
                                        )
                                    }
                                }

                                // Mood Score Progress Bar
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Stav duševní nálady:", fontSize = 11.sp, color = Color.Gray)
                                        Text("${moodState.moodScore} / 100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = moodState.primaryColor)
                                    }
                                    LinearProgressIndicator(
                                        progress = { moodState.moodScore / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = moodState.primaryColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    DynamicMood24hHeatMap(trendSummary = trendSummary)
                                }
                            }
                        }
                        2 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    DynamicMood24hLogView(trendSummary = trendSummary)
                                }
                                item {
                                    DynamicMood24hTrendAnalysis(trendSummary = trendSummary)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = moodState.primaryColor.copy(alpha = 0.85f))
                ) {
                    Text("Rozumím", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
