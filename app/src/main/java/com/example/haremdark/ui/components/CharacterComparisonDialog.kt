package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.models.Character

enum class ComparisonTab(val title: String, val icon: String) {
    STATS("Statistiky", "📊"),
    MILESTONES("Milníky & Dialogy", "🏆")
}

@Composable
fun CharacterComparisonDialog(
    characters: List<Character>,
    initialChar1: Character? = null,
    onDismiss: () -> Unit
) {
    var char1Id by remember { mutableStateOf(initialChar1?.id ?: characters.firstOrNull()?.id) }
    var char2Id by remember { mutableStateOf(characters.getOrNull(1)?.id ?: characters.firstOrNull()?.id) }

    var showSelector1 by remember { mutableStateOf(false) }
    var showSelector2 by remember { mutableStateOf(false) }

    var activeTab by remember { mutableStateOf(ComparisonTab.STATS) }

    val char1 = characters.firstOrNull { it.id == char1Id } ?: characters.firstOrNull()
    val char2 = characters.firstOrNull { it.id == char2Id } ?: characters.getOrNull(1) ?: char1

    val color1 = Color(0xFF9C27B0) // Purple for Char 1
    val color2 = Color(0xFFE91E63) // Pink for Char 2

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160F23)),
            border = BorderStroke(1.5.dp, Color(0xFFAB47BC)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 4.dp)
                .testTag("character_comparison_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
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
                        Text("⚖️", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Porovnání postav",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Porovnej atributy a odemčené milníky dialogů",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White.copy(alpha = 0.8f))
                    }
                }

                // Selection Header (Character A vs Character B)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character A Selector Button
                    Button(
                        onClick = { showSelector1 = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color1),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_char_1_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(char1?.statusIcon ?: "👤", fontSize = 12.sp)
                            Text(
                                text = char1?.name ?: "Vyber postavu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Text("VS", fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700), fontSize = 13.sp)

                    // Character B Selector Button
                    Button(
                        onClick = { showSelector2 = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color2),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_char_2_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(char2?.statusIcon ?: "👤", fontSize = 12.sp)
                            Text(
                                text = char2?.name ?: "Vyber postavu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Tab Switcher (Statistiky vs Milníky & Dialogy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonTab.entries.forEach { tab ->
                        val isSelected = activeTab == tab
                        Button(
                            onClick = {
                                activeTab = tab
                                HapticManager.vibrateDialogueCycle()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF6A1B9A) else Color(0xFF261938),
                                contentColor = if (isSelected) Color.White else Color.Gray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("comparison_tab_${tab.name.lowercase()}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(tab.icon, fontSize = 13.sp)
                                Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Main Comparison Content Area
                if (char1 != null && char2 != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (activeTab) {
                            ComparisonTab.STATS -> {
                                StatsComparisonContent(
                                    char1 = char1,
                                    char2 = char2,
                                    color1 = color1,
                                    color2 = color2
                                )
                            }
                            ComparisonTab.MILESTONES -> {
                                MilestonesAndDialoguesComparisonContent(
                                    char1 = char1,
                                    char2 = char2,
                                    color1 = color1,
                                    color2 = color2
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Character 1 Selection Dialog
    if (showSelector1) {
        CharacterSelectionListModal(
            title = "Vyber první postavu",
            characters = characters,
            onSelect = {
                char1Id = it.id
                showSelector1 = false
            },
            onDismiss = { showSelector1 = false }
        )
    }

    // Character 2 Selection Dialog
    if (showSelector2) {
        CharacterSelectionListModal(
            title = "Vyber druhou postavu",
            characters = characters,
            onSelect = {
                char2Id = it.id
                showSelector2 = false
            },
            onDismiss = { showSelector2 = false }
        )
    }
}

@Composable
private fun StatsComparisonContent(
    char1: Character,
    char2: Character,
    color1: Color,
    color2: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick summary row with Level and Affinity
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF221831),
                border = BorderStroke(0.5.dp, Color(0xFF4A355A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(char1.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color1)
                        Text("Úroveň: ${char1.level} (XP: ${char1.xp})", fontSize = 10.sp, color = Color.White)
                        Text("Pouto: ${char1.affinityPoints} pts (Úr. ${char1.affinityLevel})", fontSize = 10.sp, color = Color(0xFFFFD54F))
                    }
                    Text("⚔️", fontSize = 16.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(char2.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color2)
                        Text("Úroveň: ${char2.level} (XP: ${char2.xp})", fontSize = 10.sp, color = Color.White)
                        Text("Pouto: ${char2.affinityPoints} pts (Úr. ${char2.affinityLevel})", fontSize = 10.sp, color = Color(0xFFFFD54F))
                    }
                }
            }
        }

        item {
            ComparisonBarRow(
                label = "Body Náklonnosti (Affinity Points)",
                val1 = char1.affinityPoints.toFloat(),
                val2 = char2.affinityPoints.toFloat(),
                maxVal = 350f,
                customText1 = "${char1.affinityPoints} pts",
                customText2 = "${char2.affinityPoints} pts",
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Síla (Strength)",
                val1 = char1.effectiveStrength.toFloat(),
                val2 = char2.effectiveStrength.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Loajalita (Loyalty)",
                val1 = char1.loajalita.toFloat(),
                val2 = char2.loajalita.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Náklonnost (Affection)",
                val1 = char1.srdce.toFloat(),
                val2 = char2.srdce.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Důvěra (Trust)",
                val1 = char1.duvera.toFloat(),
                val2 = char2.duvera.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Poslušnost (Obedience)",
                val1 = char1.poslusnost.toFloat(),
                val2 = char2.poslusnost.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Touha (Desire)",
                val1 = char1.touha.toFloat(),
                val2 = char2.touha.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Nálada / Morálka (Mood)",
                val1 = char1.moodScore.toFloat(),
                val2 = char2.moodScore.toFloat(),
                maxVal = 100f,
                color1 = color1,
                color2 = color2
            )
        }

        item {
            ComparisonBarRow(
                label = "Rarita (★)",
                val1 = (char1.rarity * 20).toFloat(),
                val2 = (char2.rarity * 20).toFloat(),
                maxVal = 100f,
                customText1 = "${char1.rarity}★",
                customText2 = "${char2.rarity}★",
                color1 = color1,
                color2 = color2
            )
        }

        // --- AFFINITY MILESTONE PASSIVE BONUSES SECTION ---
        item {
            val char1Bonuses = AffinityMilestoneCalculator.getCumulativePassiveBonuses(char1)
            val char2Bonuses = AffinityMilestoneCalculator.getCumulativePassiveBonuses(char2)

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2B163B),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🏆 Pasivní Bojové & Surovinové Bonusy z Milníků",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )

                    ComparisonBarRow(
                        label = "Pasivní HP z Milníků",
                        val1 = char1Bonuses.totalHp.toFloat(),
                        val2 = char2Bonuses.totalHp.toFloat(),
                        maxVal = 200f,
                        customText1 = "+${char1Bonuses.totalHp} HP",
                        customText2 = "+${char2Bonuses.totalHp} HP",
                        color1 = color1,
                        color2 = color2
                    )

                    ComparisonBarRow(
                        label = "Pasivní Útok (%) z Milníků",
                        val1 = char1Bonuses.totalAttackPercent.toFloat(),
                        val2 = char2Bonuses.totalAttackPercent.toFloat(),
                        maxVal = 80f,
                        customText1 = "+${char1Bonuses.totalAttackPercent}%",
                        customText2 = "+${char2Bonuses.totalAttackPercent}%",
                        color1 = color1,
                        color2 = color2
                    )

                    ComparisonBarRow(
                        label = "Pasivní Obrana z Milníků",
                        val1 = char1Bonuses.totalDefense.toFloat(),
                        val2 = char2Bonuses.totalDefense.toFloat(),
                        maxVal = 80f,
                        customText1 = "+${char1Bonuses.totalDefense}",
                        customText2 = "+${char2Bonuses.totalDefense}",
                        color1 = color1,
                        color2 = color2
                    )

                    ComparisonBarRow(
                        label = "Denní Pasivní Zlato z Milníků",
                        val1 = char1Bonuses.totalDailyGold.toFloat(),
                        val2 = char2Bonuses.totalDailyGold.toFloat(),
                        maxVal = 100f,
                        customText1 = "+${char1Bonuses.totalDailyGold} G/den",
                        customText2 = "+${char2Bonuses.totalDailyGold} G/den",
                        color1 = color1,
                        color2 = color2
                    )

                    ComparisonBarRow(
                        label = "Denní Pasivní Mana z Milníků",
                        val1 = char1Bonuses.totalDailyMana.toFloat(),
                        val2 = char2Bonuses.totalDailyMana.toFloat(),
                        maxVal = 50f,
                        customText1 = "+${char1Bonuses.totalDailyMana} M/den",
                        customText2 = "+${char2Bonuses.totalDailyMana} M/den",
                        color1 = color1,
                        color2 = color2
                    )

                    ComparisonBarRow(
                        label = "Denní Pasivní Materiály z Milníků",
                        val1 = char1Bonuses.totalDailyMaterials.toFloat(),
                        val2 = char2Bonuses.totalDailyMaterials.toFloat(),
                        maxVal = 60f,
                        customText1 = "+${char1Bonuses.totalDailyMaterials} Mat/den",
                        customText2 = "+${char2Bonuses.totalDailyMaterials} Mat/den",
                        color1 = color1,
                        color2 = color2
                    )
                }
            }
        }
    }
}

/**
 * Side-by-side comparison of unlocked flavor dialogue milestones for the two characters.
 */
@Composable
private fun MilestonesAndDialoguesComparisonContent(
    char1: Character,
    char2: Character,
    color1: Color,
    color2: Color
) {
    val milestones = AffinityData.AFFINITY_MILESTONES
    val char1CrossedCount = milestones.count { char1.affinityPoints >= it }
    val char2CrossedCount = milestones.count { char2.affinityPoints >= it }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Overview Summary Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF221831),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Přehled Odemčených Milníků",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Celkem 6 milníků",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Char 1 badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = color1.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, color1)
                        ) {
                            Text(
                                text = "${char1.name}: $char1CrossedCount / ${milestones.size} odemčeno",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = color1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Char 2 badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = color2.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, color2)
                        ) {
                            Text(
                                text = "${char2.name}: $char2CrossedCount / ${milestones.size} odemčeno",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = color2,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Each Milestone Card with side-by-side unlocked dialogue lines
        items(milestones) { threshold ->
            val badgeInfo = AffinityData.getMilestoneBadgeInfo(threshold)
            val isUnlocked1 = char1.affinityPoints >= threshold
            val isUnlocked2 = char2.affinityPoints >= threshold

            val dialogue1 = remember(char1.id, threshold) {
                AffinityData.getCelebratoryMilestoneLine(char1, threshold)
            }
            val dialogue2 = remember(char2.id, threshold) {
                AffinityData.getCelebratoryMilestoneLine(char2, threshold)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F152E)),
                border = BorderStroke(
                    1.dp,
                    if (isUnlocked1 || isUnlocked2) Color(0xFFFFD700).copy(alpha = 0.5f) else Color(0xFF3E2C4F)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Milestone Header: Badge Title & Threshold requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(badgeInfo.first, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD700))
                            Text("($threshold pts)", fontSize = 10.sp, color = Color.Gray)
                        }

                        Text(badgeInfo.second, fontSize = 9.sp, color = Color.LightGray.copy(alpha = 0.8f))
                    }

                    // Side-by-side Character Columns for this Milestone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Character 1 Side
                        MilestoneDialogueSideCard(
                            character = char1,
                            threshold = threshold,
                            isUnlocked = isUnlocked1,
                            dialogue = dialogue1,
                            color = color1,
                            modifier = Modifier.weight(1f)
                        )

                        // Character 2 Side
                        MilestoneDialogueSideCard(
                            character = char2,
                            threshold = threshold,
                            isUnlocked = isUnlocked2,
                            dialogue = dialogue2,
                            color = color2,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneDialogueSideCard(
    character: Character,
    threshold: Int,
    isUnlocked: Boolean,
    dialogue: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isUnlocked) color.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.3f),
        border = BorderStroke(
            0.5.dp,
            if (isUnlocked) color.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Status Chip & Character Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = character.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1
                )

                if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "✓ Odemčeno",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    val remaining = (threshold - character.affinityPoints).coerceAtLeast(1)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "🔒 -$remaining pts",
                            fontSize = 8.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Flavor Dialogue or Locked Teaser
            if (isUnlocked) {
                Text(
                    text = "„$dialogue“",
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 13.sp
                )

                // Listen Audio Cue button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            HapticManager.vibrateDialogueCycle()
                            SoundEffectManager.playFlavorDialogueChime(threshold / 60 + 1)
                            VoiceManager.speak(dialogue, character.archetypeId)
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Přehrát hlas",
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Zamčeno",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Dialog uzamčen do $threshold bodů náklonnosti.",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Milestone Passive Combat & Resource Bonuses Card
            val milestoneBonus = com.example.haremdark.data.AffinityMilestoneBonusCatalog.getBonusForMilestone(character.archetypeId, threshold)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isUnlocked) Color(0xFF1E132D) else Color.Black.copy(alpha = 0.25f),
                border = BorderStroke(0.5.dp, if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else Color.DarkGray.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "⚔️ Boj: +${milestoneBonus.hpBonus} HP, +${(milestoneBonus.attackBonusPercent * 100).toInt()}% ATK, +${milestoneBonus.defenseBonus} DEF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUnlocked) Color(0xFFFFD54F) else Color.Gray
                    )
                    if (milestoneBonus.combatPerkSummary.isNotBlank()) {
                        Text(
                            text = "✨ ${milestoneBonus.combatPerkSummary}",
                            fontSize = 8.sp,
                            color = if (isUnlocked) Color(0xFFFF80AB) else Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (milestoneBonus.dailyGold > 0 || milestoneBonus.dailyMana > 0 || (milestoneBonus.dailyWood + milestoneBonus.dailyStone + milestoneBonus.dailyIron) > 0) {
                        Text(
                            text = "💎 Zisk: +${milestoneBonus.dailyGold} G, +${milestoneBonus.dailyMana} M, +${milestoneBonus.dailyWood + milestoneBonus.dailyStone + milestoneBonus.dailyIron} Mat/den",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnlocked) Color(0xFF80DEEA) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonBarRow(
    label: String,
    val1: Float,
    val2: Float,
    maxVal: Float = 100f,
    customText1: String? = null,
    customText2: String? = null,
    color1: Color,
    color2: Color
) {
    val fraction1 = (val1 / maxVal).coerceIn(0f, 1f)
    val fraction2 = (val2 / maxVal).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF221831),
        border = BorderStroke(0.5.dp, Color(0xFF4A355A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )

            // Bar 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { fraction1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color1,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = customText1 ?: "${val1.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color1,
                    modifier = Modifier.width(42.dp),
                    textAlign = TextAlign.End
                )
            }

            // Bar 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { fraction2 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color2,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = customText2 ?: "${val2.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color2,
                    modifier = Modifier.width(42.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun CharacterSelectionListModal(
    title: String,
    characters: List<Character>,
    onSelect: (Character) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191224)),
            border = BorderStroke(1.dp, Color(0xFFAB47BC)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(characters) { character ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF261836),
                            border = BorderStroke(0.5.dp, Color(0xFF4A355A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(character) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(character.statusIcon, fontSize = 18.sp)
                                    Column {
                                        Text(
                                            text = character.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Pouto: ${character.affinityPoints} pts • Úr. ${character.affinityLevel} • ${character.rarity}★",
                                            fontSize = 10.sp,
                                            color = Color(0xFFFF80AB)
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Zavřít", fontSize = 11.sp)
                }
            }
        }
    }
}
