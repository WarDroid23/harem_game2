package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.InteractionLogEntry
import com.example.haremdark.models.getSafeInteractionLogs

/**
 * Represents one single hour time slot in the 24-hour interaction heat map.
 */
data class HourlyInteractionSlot(
    val hourOffset: Int, // 0 = current hour (Now), 23 = 23 hours ago
    val displayHour: String, // e.g. "14:00"
    val relativeLabel: String, // e.g. "Nyní", "Před 2 h"
    val timeOfDayPeriod: String, // "Noc", "Ráno", "Dopoledne", "Odpoledne", "Večer"
    val interactionCount: Int,
    val interactions: List<InteractionLogEntry>,
    val dominantEmotion: String,
    val emotionIcon: String,
    val valenceScore: Int, // 0 - 100
    val heatColor: Color,
    val quote: String
)

/**
 * Summary of emotional response trends over the last 24 hours.
 */
data class EmotionalTrendSummary(
    val totalInteractions24h: Int,
    val averageValenceScore: Int,
    val dominantEmotionTitle: String,
    val dominantEmotionIcon: String,
    val dominantEmotionPercentage: Int,
    val stabilityRating: String,
    val affinityGain24h: Int,
    val slots: List<HourlyInteractionSlot>,
    val recentLogs24h: List<InteractionLogEntry>,
    val emotionDistribution: List<Pair<String, Int>>
)

/**
 * Evaluates the 24-hour interaction heat map and emotional response trends for a character.
 */
object DynamicMood24hEvaluator {

    fun evaluate24Hours(character: Character): EmotionalTrendSummary {
        val affinity = character.affinityPoints
        val rawLogs = character.interactionLogs.ifEmpty { character.getSafeInteractionLogs() }

        // Generate 24 slots: hourOffset 23 down to 0
        val currentHour = 14 // standard mid-day reference for consistent UI indexing
        val slots = (23 downTo 0).map { offset ->
            val slotHourNumber = ((currentHour - offset) % 24 + 24) % 24
            val displayHour = String.format("%02d:00", slotHourNumber)
            val relativeLabel = when (offset) {
                0 -> "Nyní"
                1 -> "Před 1 h"
                in 2..4 -> "Před $offset h"
                else -> "Před $offset h"
            }
            val period = when (slotHourNumber) {
                in 0..5 -> "Hluboká noc"
                in 6..9 -> "Ranní probuzení"
                in 10..13 -> "Dopolední komnaty"
                in 14..17 -> "Odpolední aktivity"
                in 18..21 -> "Večerní audience"
                else -> "Noční odpočinek"
            }

            // Match user interactions to this slot
            // We check explicit hourOffset or match log items to realistic slots based on index
            val matchingInteractions = rawLogs.filter { log ->
                val logOffset = if (log.hourOffset in 0..23) log.hourOffset else {
                    // Distribute existing logs across realistic hours (recent in 0, older in 2, 6, 12, etc.)
                    val idx = rawLogs.indexOf(log)
                    when (idx) {
                        0 -> 0
                        1 -> 2
                        2 -> 5
                        3 -> 11
                        4 -> 16
                        else -> 20
                    }
                }
                logOffset == offset
            }

            val count = matchingInteractions.size
            val primaryType = matchingInteractions.firstOrNull()?.type?.lowercase() ?: ""
            val primaryTitle = matchingInteractions.firstOrNull()?.title ?: ""

            // Determine dominant emotion and heat color for this hour
            val (emotion, icon, valence, color, quote) = when {
                count == 0 -> {
                    // Inactive hours - gentle baseline resting state
                    if (slotHourNumber in 1..5) {
                        Tuple5("Pokojný spánek", "🌙", 65, Color(0xFF181024), "Spí v komnatách a nabírá sílu.")
                    } else {
                        Tuple5("Tichý odpočinek", "💤", 60, Color(0xFF1E132D), "Čeká v komnatách na další rozkaz.")
                    }
                }
                primaryType.contains("dar") || primaryTitle.contains("dar", ignoreCase = true) || primaryTitle.contains("večeře", ignoreCase = true) -> {
                    if (affinity >= 100) {
                        Tuple5("Zbožňující vášeň", "💖", 95, Color(0xFFFF4081), "Oči se jí rozzářily a srdce jí buší vděčností za dar.")
                    } else {
                        Tuple5("Potěšená vděčnost", "🌸", 80, Color(0xFFF06292), "Přijala dar s pokorným úsměvem a novou nadějí.")
                    }
                }
                primaryType.contains("intim") || primaryTitle.contains("intim", ignoreCase = true) || primaryTitle.contains("noc", ignoreCase = true) -> {
                    Tuple5("Spalující vášeň", "🔥", 98, Color(0xFFE91E63), "Touží po dalším doteku. Vzpomínka na intimitu ji hřeje.")
                }
                primaryType.contains("trest") || primaryTitle.contains("trest", ignoreCase = true) -> {
                    if (affinity >= 100) {
                        Tuple5("Sladké pokání", "⛓️", 70, Color(0xFFBA68C8), "Trest v ní upevnil oddanost a touhu se ti plně podvolit.")
                    } else {
                        Tuple5("Bázlivá opatrnost", "😨", 35, Color(0xFFFF7043), "Po trestu cítí nejistotu a chová se obezřetně.")
                    }
                }
                primaryType.contains("výcvik") || primaryTitle.contains("výcvik", ignoreCase = true) || primaryTitle.contains("dril", ignoreCase = true) -> {
                    Tuple5("Bojová bdělost", "⚔️", 78, Color(0xFF00E5FF), "Trénink zostřil její reflexy a posílil poslušnost.")
                }
                primaryType.contains("rozhovor") || primaryTitle.contains("rozhovor", ignoreCase = true) -> {
                    Tuple5("Otevřená důvěra", "🤝", 85, Color(0xFF66BB6A), "Hluboký rozhovor uvolnil její zábrany a prohloubil pouto.")
                }
                else -> {
                    Tuple5("Vstřícná nálada", "✨", 75, Color(0xFFB388FF), "Vstřícně plní své denní povinnosti.")
                }
            }

            HourlyInteractionSlot(
                hourOffset = offset,
                displayHour = displayHour,
                relativeLabel = relativeLabel,
                timeOfDayPeriod = period,
                interactionCount = count,
                interactions = matchingInteractions,
                dominantEmotion = emotion,
                emotionIcon = icon,
                valenceScore = valence,
                heatColor = color,
                quote = quote
            )
        }

        val activeSlots = slots.filter { it.interactionCount > 0 }
        val totalInteractions = slots.sumOf { it.interactionCount }
        val avgValence = if (activeSlots.isNotEmpty()) {
            activeSlots.map { it.valenceScore }.average().toInt()
        } else 70

        // Find dominant emotion across active slots
        val emotionCounts = mutableMapOf<String, Int>()
        activeSlots.forEach { slot ->
            emotionCounts[slot.dominantEmotion] = (emotionCounts[slot.dominantEmotion] ?: 0) + slot.interactionCount
        }
        val topEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "Oddaná láska"
        val topCount = emotionCounts[topEmotion] ?: 1
        val topPercentage = if (totalInteractions > 0) ((topCount.toFloat() / totalInteractions) * 100).toInt() else 85

        val dominantIcon = when {
            topEmotion.contains("vášeň", ignoreCase = true) -> "🔥"
            topEmotion.contains("dar", ignoreCase = true) || topEmotion.contains("vděčnost", ignoreCase = true) -> "💖"
            topEmotion.contains("důvěra", ignoreCase = true) -> "🤝"
            topEmotion.contains("boj", ignoreCase = true) || topEmotion.contains("výcvik", ignoreCase = true) -> "⚔️"
            topEmotion.contains("pokání", ignoreCase = true) -> "⛓️"
            else -> "✨"
        }

        val stability = when {
            avgValence >= 85 -> "Vysoce stabilní harmonie (94%)"
            avgValence >= 65 -> "Vyrovnaná poddajnost (78%)"
            else -> "Kolísavé napětí (52%)"
        }

        val affinityGained = totalInteractions * 8 + 6

        val distribution = listOf(
            Pair("Láska & Dary", if (totalInteractions > 0) (totalInteractions * 35 / 100).coerceAtLeast(1) else 3),
            Pair("Vášeň & Intimita", if (totalInteractions > 0) (totalInteractions * 30 / 100).coerceAtLeast(1) else 2),
            Pair("Důvěrný rozhovor", if (totalInteractions > 0) (totalInteractions * 20 / 100).coerceAtLeast(1) else 2),
            Pair("Výcvik & Kázeň", if (totalInteractions > 0) (totalInteractions * 15 / 100).coerceAtLeast(1) else 1)
        )

        return EmotionalTrendSummary(
            totalInteractions24h = totalInteractions.coerceAtLeast(rawLogs.size),
            averageValenceScore = avgValence,
            dominantEmotionTitle = topEmotion,
            dominantEmotionIcon = dominantIcon,
            dominantEmotionPercentage = topPercentage.coerceIn(40, 100),
            stabilityRating = stability,
            affinityGain24h = affinityGained,
            slots = slots,
            recentLogs24h = rawLogs.take(10),
            emotionDistribution = distribution
        )
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}

/**
 * Tabbed control bar designed to sit directly next to the Mood Indicator badge
 * on character headers or profile banners.
 */
@Composable
fun MoodTrendsTabbedControl(
    character: Character,
    modifier: Modifier = Modifier,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showFullTrackerDialog by remember { mutableStateOf(false) }

    val tabs = listOf("🗺️ Heat Map", "📜 24h Log", "📈 Trendy")

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1B0F2A).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.45f)),
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .testTag("mood_trends_tabbed_control")
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFAB47BC).copy(alpha = 0.85f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "TabBgAnim"
                )
                val tabTextColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFFCE93D8),
                    animationSpec = tween(200),
                    label = "TabTextColorAnim"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tabBg)
                        .clickable {
                            HapticManager.vibrateClick()
                            selectedTab = index
                            showFullTrackerDialog = true
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("mood_trends_tab_$index")
                        .semantics { contentDescription = "Zobrazit $title" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = tabTextColor
                    )
                }
            }
        }
    }

    if (showFullTrackerDialog) {
        DynamicMood24hTrackerDialog(
            character = character,
            initialTabIndex = selectedTab,
            onDismiss = { showFullTrackerDialog = false }
        )
    }
}

/**
 * Unified row placing the Dynamic Mood Indicator and the 24h Heat Map / Trends Tabbed View side by side.
 */
@Composable
fun MoodIndicatorWithTrendsTabbedView(
    character: Character,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("mood_indicator_with_trends_tabbed_view"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Mood Indicator Badge
        DynamicMoodAvatarHeaderBadge(
            character = character,
            compact = true
        )

        // Tabbed View next to Mood Indicator
        MoodTrendsTabbedControl(
            character = character
        )
    }
}

/**
 * Interactive 24-hour heat map grid showing interaction frequency and emotional response.
 */
@Composable
fun DynamicMood24hHeatMap(
    trendSummary: EmotionalTrendSummary,
    modifier: Modifier = Modifier,
    onSlotSelected: ((HourlyInteractionSlot) -> Unit)? = null
) {
    var selectedSlotOffset by remember { mutableIntStateOf(0) }
    val selectedSlot = trendSummary.slots.firstOrNull { it.hourOffset == selectedSlotOffset }
        ?: trendSummary.slots.first()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130922)),
        border = BorderStroke(1.2.dp, Color(0xFFBA68C8).copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_mood_24h_heat_map")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", fontSize = 16.sp)
                    Column {
                        Text(
                            text = "24-Hodinová Heat Mapa Interakcí",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE1BEE7)
                        )
                        Text(
                            text = "Frekvence a emoční odezvy hodinu po hodině",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF241438),
                    border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${trendSummary.totalInteractions24h} akcí / 24h",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF80AB),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 24 Hour Matrix Grid (4 rows x 6 columns)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F061C))
                    .border(1.dp, Color(0xFF3B1E54), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val chunks = trendSummary.slots.chunked(6)
                chunks.forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            val isSelected = slot.hourOffset == selectedSlotOffset
                            val hasInteractions = slot.interactionCount > 0

                            val cellBg = if (hasInteractions) {
                                slot.heatColor.copy(alpha = if (isSelected) 0.95f else 0.70f)
                            } else {
                                if (isSelected) Color(0xFF381F4E) else Color(0xFF1A0E28)
                            }

                            val borderCol = if (isSelected) {
                                Color(0xFFFFD700)
                            } else if (hasInteractions) {
                                slot.heatColor
                            } else {
                                Color(0xFF2E1944)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cellBg)
                                    .border(if (isSelected) 2.dp else 1.dp, borderCol, RoundedCornerShape(6.dp))
                                    .clickable {
                                        HapticManager.vibrateClick()
                                        selectedSlotOffset = slot.hourOffset
                                        onSlotSelected?.invoke(slot)
                                    }
                                    .testTag("heat_cell_${slot.hourOffset}")
                                    .semantics { contentDescription = "${slot.displayHour}: ${slot.dominantEmotion}" },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = slot.displayHour.take(2) + "h",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (hasInteractions) Color.White else Color.Gray
                                    )
                                    Text(
                                        text = if (hasInteractions) slot.emotionIcon else "·",
                                        fontSize = if (hasInteractions) 12.sp else 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aktivita:", fontSize = 10.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF1A0E28), CircleShape))
                    Text("0", fontSize = 9.sp, color = Color.Gray)
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF8E24AA), CircleShape))
                    Text("1-2", fontSize = 9.sp, color = Color.Gray)
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF4081), CircleShape))
                    Text("3+", fontSize = 9.sp, color = Color.Gray)
                }
            }

            // Selected Hour Slot Detail Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF201034),
                border = BorderStroke(1.dp, selectedSlot.heatColor.copy(alpha = 0.5f)),
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(selectedSlot.emotionIcon, fontSize = 16.sp)
                            Text(
                                text = "${selectedSlot.displayHour} (${selectedSlot.relativeLabel})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${selectedSlot.interactionCount} akcí • Emoční valence ${selectedSlot.valenceScore}%",
                            fontSize = 10.sp,
                            color = selectedSlot.heatColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Rozpoložení: ${selectedSlot.dominantEmotion} (${selectedSlot.timeOfDayPeriod})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCE93D8)
                    )

                    Text(
                        text = "💭 \"${selectedSlot.quote}\"",
                        fontSize = 11.sp,
                        color = Color(0xFFE1BEE7),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Chronological feed of recent user interactions and corresponding emotional response trends.
 */
@Composable
fun DynamicMood24hLogView(
    trendSummary: EmotionalTrendSummary,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Vše") }
    val filters = listOf("Vše", "Dary", "Intimita", "Výcvik", "Trest")

    val filteredLogs = remember(selectedFilter, trendSummary.recentLogs24h) {
        if (selectedFilter == "Vše") trendSummary.recentLogs24h else {
            trendSummary.recentLogs24h.filter { log ->
                val type = log.type.lowercase()
                val title = log.title.lowercase()
                when (selectedFilter) {
                    "Dary" -> type.contains("dar") || title.contains("dar")
                    "Intimita" -> type.contains("intim") || title.contains("intim")
                    "Výcvik" -> type.contains("výcvik") || title.contains("dril")
                    "Trest" -> type.contains("trest") || title.contains("pokárání")
                    else -> true
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_mood_24h_log_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF9C27B0) else Color(0xFF1B0F2A),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFE1BEE7) else Color(0xFF381F4E)),
                    modifier = Modifier.clickable {
                        HapticManager.vibrateClick()
                        selectedFilter = filter
                    }
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFFCE93D8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF170C24),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Text(
                    text = "Žádné interakce neodpovídají filtru za posledních 24 hodin.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredLogs.forEach { log ->
                    val (badgeIcon, badgeColor, emotionQuote) = when {
                        log.type.contains("dar", ignoreCase = true) || log.title.contains("dar", ignoreCase = true) ->
                            Triple("💖 Dar", Color(0xFFFF4081), "Cítí se milována a zahrnuta přízní.")
                        log.type.contains("intim", ignoreCase = true) || log.title.contains("intim", ignoreCase = true) ->
                            Triple("🔥 Intimita", Color(0xFFE91E63), "Touha a vášeň prohlubují tělesné i duševní pouto.")
                        log.type.contains("trest", ignoreCase = true) ->
                            Triple("⛓️ Trest", Color(0xFFBA68C8), "Přijala pokárání s poslušnou oddaností.")
                        log.type.contains("výcvik", ignoreCase = true) ->
                            Triple("⚔️ Výcvik", Color(0xFF00E5FF), "Zlepšila svou bojovou kázeň a pohotovost.")
                        else ->
                            Triple("💬 Rozhovor", Color(0xFF81C784), "Rozhovor posílil vzájemné porozumění.")
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0E2A)),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeColor.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = badgeIcon,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = if (log.day > 0) "Den ${log.day}" else "Dnes",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = log.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (log.description.isNotBlank()) {
                                Text(
                                    text = log.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 15.sp
                                )
                            }

                            // Emotional impact trend
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10071C),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "💭 Emoce: $emotionQuote",
                                        fontSize = 10.sp,
                                        color = Color(0xFFCE93D8)
                                    )
                                    if (log.statChanges.isNotBlank()) {
                                        Text(
                                            text = log.statChanges,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700)
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
}

/**
 * Visual breakdown of emotional response trends over 24 hours.
 */
@Composable
fun DynamicMood24hTrendAnalysis(
    trendSummary: EmotionalTrendSummary,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF150A24)),
        border = BorderStroke(1.2.dp, Color(0xFFBA68C8).copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_mood_24h_trend_analysis")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📈 Emoční Trendy za 24 Hodin",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE1BEE7)
                )
                Text(
                    text = trendSummary.stabilityRating,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF81C784)
                )
            }

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Card 1: Dominant Emotion
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF221136),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Dominantní emoce", fontSize = 9.sp, color = Color.Gray)
                        Text(
                            text = "${trendSummary.dominantEmotionIcon} ${trendSummary.dominantEmotionTitle}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4081),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("${trendSummary.dominantEmotionPercentage}% času", fontSize = 9.sp, color = Color(0xFFFF80AB))
                    }
                }

                // Card 2: Average Mood Valence
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF221136),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Průměrná valence", fontSize = 9.sp, color = Color.Gray)
                        Text(
                            text = "${trendSummary.averageValenceScore} / 100",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Text("Emoční skóre", fontSize = 9.sp, color = Color(0xFF80D8FF))
                    }
                }

                // Card 3: Affinity Gain
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF221136),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Růst pouta 24h", fontSize = 9.sp, color = Color.Gray)
                        Text(
                            text = "+${trendSummary.affinityGain24h} pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text("Získaná náklonnost", fontSize = 9.sp, color = Color(0xFFFFF59D))
                    }
                }
            }

            // Emotion Distribution Bars
            Text(
                text = "Rozložení interakcí a odezev",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCE93D8)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                trendSummary.emotionDistribution.forEach { (label, count) ->
                    val ratio = (count.toFloat() / 10f).coerceIn(0.1f, 1f)
                    val barColor = when {
                        label.contains("Láska") -> Color(0xFFFF4081)
                        label.contains("Vášeň") -> Color(0xFFE91E63)
                        label.contains("Důvěrný") -> Color(0xFF66BB6A)
                        else -> Color(0xFF00E5FF)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontSize = 10.sp, color = Color.White)
                            Text("$count záznamů", fontSize = 10.sp, color = barColor, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = barColor,
                            trackColor = Color(0xFF25143A)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Tracker Dialog opened when user interacts with the tabs next to the Mood Indicator.
 */
@Composable
fun DynamicMood24hTrackerDialog(
    character: Character,
    initialTabIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTabIndex) }
    val trendSummary = remember(character.affinityPoints, character.interactionLogs.size) {
        DynamicMood24hEvaluator.evaluate24Hours(character)
    }

    val tabTitles = listOf("🔥 Heat Mapa", "📜 Log Interakcí", "📈 Emoční Trendy")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140822)),
            border = BorderStroke(1.5.dp, Color(0xFFBA68C8).copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("dynamic_mood_24h_tracker_dialog")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with character name and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(trendSummary.dominantEmotionIcon, fontSize = 20.sp)
                        Column {
                            Text(
                                text = "24h Emoční Odezva: ${character.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Historie nálad, interakcí a emočních vln",
                                fontSize = 10.sp,
                                color = Color(0xFFCE93D8)
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

                HorizontalDivider(color = Color(0xFF381F4E))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1B0E2E),
                    contentColor = Color(0xFFFF4081),
                    edgePadding = 4.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFFFF4081)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                HapticManager.vibrateClick()
                                selectedTab = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    DynamicMood24hHeatMap(trendSummary = trendSummary)
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    DynamicMood24hLogView(trendSummary = trendSummary)
                                }
                            }
                        }
                        2 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    DynamicMood24hTrendAnalysis(trendSummary = trendSummary)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zavřít Přehled", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
