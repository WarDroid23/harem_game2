package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.NavSound
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.models.Achievement
import com.example.haremdark.models.AchievementCategory
import com.example.haremdark.models.getRelationship
import com.example.haremdark.models.AchievementList
import com.example.haremdark.models.GameSave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    gameState: GameSave,
    engine: GameEngine,
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        engine.checkAchievements()
    }

    var selectedCategory by remember { mutableStateOf(AchievementCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val unlockedIds = gameState.player.unlockedAchievements
    val allAchievements = AchievementList.allAchievements

    val totalCount = allAchievements.size
    val unlockedCount = unlockedIds.size
    val totalProgressFraction = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    val filteredAchievements = remember(selectedCategory, searchQuery, unlockedIds) {
        allAchievements.filter { ach ->
            val matchesCategory = (selectedCategory == AchievementCategory.ALL) || (ach.category == selectedCategory)
            val matchesSearch = searchQuery.isEmpty() ||
                    ach.title.contains(searchQuery, ignoreCase = true) ||
                    ach.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("achievement_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Top Summary Dashboard Header
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏆", fontSize = 22.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Milníky & Výzvy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Odemčeno $unlockedCount z $totalCount milníků",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Active Title Indicator
                    val currentTitle = gameState.player.activeTitle ?: "Neznámý vládce"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("👑 Titul:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currentTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Overall Completion Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Celkový postup plnění",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(totalProgressFraction * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { totalProgressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Category Filter Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AchievementCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                val categoryCount = if (cat == AchievementCategory.ALL) allAchievements.size else allAchievements.count { it.category == cat }
                val categoryUnlocked = if (cat == AchievementCategory.ALL) unlockedIds.size else allAchievements.count { it.category == cat && unlockedIds.contains(it.id) }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                        selectedCategory = cat
                    },
                    label = {
                        Text(
                            text = "${cat.icon} ${cat.displayName} ($categoryUnlocked/$categoryCount)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Achievement List
        if (filteredAchievements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Žádné milníky v té kategorii nebyly nalezeny.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredAchievements, key = { it.id }) { ach ->
                    val isUnlocked = unlockedIds.contains(ach.id)
                    val (currentVal, targetVal) = getAchievementProgressValues(ach.id, gameState)
                    val progressFraction = if (isUnlocked) 1.0f else (currentVal.toFloat() / targetVal.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val isEquippedTitle = gameState.player.activeTitle == (ach.titleReward ?: ach.title)

                    AchievementListItem(
                        achievement = ach,
                        isUnlocked = isUnlocked,
                        currentVal = currentVal,
                        targetVal = targetVal,
                        progressFraction = progressFraction,
                        isEquippedTitle = isEquippedTitle,
                        onEquipTitle = {
                            val titleToSet = ach.titleReward ?: ach.title
                            engine.setActiveTitle(titleToSet)
                            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
                            Toast.makeText(context, "👑 Titul nastaven: $titleToSet", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual Achievement List Item Component with progress bar and rewards
 */
@Composable
fun AchievementListItem(
    achievement: Achievement,
    isUnlocked: Boolean,
    currentVal: Int,
    targetVal: Int,
    progressFraction: Float,
    isEquippedTitle: Boolean,
    onEquipTitle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            width = if (isUnlocked) 1.5.dp else 1.dp,
            color = if (isUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.DarkGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUnlocked) {
                            Text(text = achievement.badgeIcon, fontSize = 26.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Zamčeno",
                                tint = Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Title and Description
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = achievement.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (isUnlocked) {
                            Text("✅", fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = achievement.description,
                        fontSize = 11.sp,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Category Tag Icon
                Text(
                    text = achievement.category.icon,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Progress Bar Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUnlocked) "Splněno 100%" else "Postup: $currentVal / $targetVal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }

            // Rewards & Title Equip Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reward Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "💰 +${achievement.rewardGold}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF7E57C2).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⭐ +${achievement.rewardXp} XP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB39DDB),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (achievement.titleReward != null || achievement.isTitle) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF29B6F6).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "👑 ${achievement.titleReward ?: achievement.title}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81D4FA),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Equip Title Button if Unlocked
                if (isUnlocked && (achievement.isTitle || achievement.titleReward != null)) {
                    if (isEquippedTitle) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Aktivní",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEquipTitle,
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Použít titul", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculates current progress value and target value for any achievement
 */
private fun getAchievementProgressValues(id: String, state: GameSave): Pair<Int, Int> {
    return when (id) {
        "ach_first_win" -> Pair(state.player.battlesWon.coerceAtMost(1), 1)
        "ach_battles_10" -> Pair(state.player.battlesWon.coerceAtMost(10), 10)
        "ach_battles_100" -> Pair(state.player.battlesWon.coerceAtMost(100), 100)
        "ach_boss_slayer" -> Pair(state.defeatedBosses.size.coerceAtMost(3), 3)
        "ach_arena_champion" -> Pair((state.characters.maxOfOrNull { it.level } ?: 1).coerceAtMost(10), 10)

        "ach_first_love" -> {
            val maxTier = state.characters.maxOfOrNull { com.example.haremdark.models.BondingLevel.fromAffinity(it.affinityPoints).ordinal + 1 } ?: 1
            Pair(maxTier.coerceAtMost(2), 2)
        }
        "ach_rel_soulmate" -> {
            val maxTier = state.characters.maxOfOrNull { com.example.haremdark.models.BondingLevel.fromAffinity(it.affinityPoints).ordinal + 1 } ?: 1
            Pair(maxTier.coerceAtMost(4), 4)
        }
        "ach_max_affinity" -> {
            val maxAff = state.characters.maxOfOrNull { it.affinityPoints } ?: 0
            Pair(maxAff.coerceAtMost(100), 100)
        }
        "ach_harem_5" -> Pair(state.characters.size.coerceAtMost(5), 5)
        "ach_harem_10" -> Pair(state.characters.size.coerceAtMost(10), 10)
        "ach_harem_20" -> Pair(state.characters.size.coerceAtMost(20), 20)
        "ach_affinity_total" -> Pair(state.characters.sumOf { it.affinityPoints }.coerceAtMost(250), 250)
        "ach_blood_sister" -> {
            val hasIt = state.characters.any { it.getRelationship() == com.example.haremdark.models.RelStatus.BLOOD_SISTER }
            Pair(if (hasIt) 1 else 0, 1)
        }

        "ach_wealth_1k" -> Pair(state.player.gold.coerceAtMost(1000), 1000)
        "ach_wealthy" -> Pair(state.player.gold.coerceAtMost(10000), 10000)

        "ach_domain_max" -> {
            val fortressLevel = state.buildings.firstOrNull { it.type == "pevnost" }?.level ?: 1
            Pair(fortressLevel.coerceAtMost(5), 5)
        }
        "ach_buildings_10" -> {
            val totalLevels = state.buildings.sumOf { it.level }
            Pair(totalLevels.coerceAtMost(10), 10)
        }

        "ach_events_10" -> {
            val count = state.completedQuests.size + state.gameLog.size
            Pair(count.coerceAtMost(10), 10)
        }

        else -> Pair(0, 1)
    }
}
