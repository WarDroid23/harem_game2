package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.AffinityTierInfo
import com.example.haremdark.data.BondTierCatalog
import com.example.haremdark.data.CharacterSpecificBuff
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.Character

/**
 * Data bundle representing an individual affinity milestone and its unique unlocked combat rewards.
 */
data class AffinityMilestoneReward(
    val tierLevel: Int,
    val tierInfo: AffinityTierInfo,
    val characterSpecificBuff: CharacterSpecificBuff,
    val isUnlocked: Boolean,
    val progress: Float,
    val pointsNeededToUnlock: Int
)

/**
 * Calculates all milestone rewards and cumulative passive bonuses for a character.
 */
object AffinityMilestoneCalculator {

    fun getMilestonesForCharacter(character: Character): List<AffinityMilestoneReward> {
        val currentPoints = character.affinityPoints
        val archetype = character.archetypeId

        return AffinityData.TIERS.map { tierInfo ->
            val buff = AffinityData.getCharacterSpecificBuff(archetype, tierInfo.level)
            val isUnlocked = currentPoints >= tierInfo.minPoints
            val progress = if (tierInfo.minPoints == 0) {
                1.0f
            } else {
                (currentPoints.toFloat() / tierInfo.minPoints.toFloat()).coerceIn(0f, 1f)
            }
            val pointsNeeded = (tierInfo.minPoints - currentPoints).coerceAtLeast(0)

            AffinityMilestoneReward(
                tierLevel = tierInfo.level,
                tierInfo = tierInfo,
                characterSpecificBuff = buff,
                isUnlocked = isUnlocked,
                progress = progress,
                pointsNeededToUnlock = pointsNeeded
            )
        }
    }

    fun getCumulativePassiveBonuses(character: Character): CumulativeAffinityCombatBonuses {
        val currentTier = AffinityData.getLevelForPoints(character.affinityPoints)
        val activeTierInfo = AffinityData.getTierForPoints(character.affinityPoints)
        val activeBuff = AffinityData.getCharacterSpecificBuff(character.archetypeId, currentTier)

        // Tier passive bonuses
        val tierHp = activeTierInfo.hpBonus
        val tierAtkPercent = activeTierInfo.attackBonusPercent
        val tierDef = activeTierInfo.defenseBonus
        val tierCrit = activeTierInfo.critBonusPercent
        val tierHpRegen = activeTierInfo.hpRegenBonus
        val tierDarkEnergy = activeTierInfo.darkEnergyBonus

        // Unique character-specific buff bonuses
        val buffHp = activeBuff.hpBonus
        val buffAtkPercent = (activeBuff.attackBonusPercent * 100).toInt()
        val buffDef = activeBuff.defenseBonus
        val buffCrit = activeBuff.critBonusPercent
        val buffHpRegen = activeBuff.hpRegenPerTurn
        val buffDarkEnergy = activeBuff.darkEnergyRegenPerTurn

        return CumulativeAffinityCombatBonuses(
            totalHp = tierHp + buffHp,
            totalAttackPercent = tierAtkPercent + buffAtkPercent,
            totalDefense = tierDef + buffDef,
            totalCritPercent = tierCrit + buffCrit,
            totalHpRegenPerTurn = tierHpRegen + buffHpRegen,
            totalDarkEnergyPerTurn = tierDarkEnergy + buffDarkEnergy,
            activeBuffName = activeBuff.name,
            activeBuffIcon = activeBuff.icon,
            activeBuffSummary = activeBuff.perkEffectSummary,
            specialEffectTag = activeBuff.specialEffectTag
        )
    }
}

data class CumulativeAffinityCombatBonuses(
    val totalHp: Int,
    val totalAttackPercent: Int,
    val totalDefense: Int,
    val totalCritPercent: Int,
    val totalHpRegenPerTurn: Int,
    val totalDarkEnergyPerTurn: Int,
    val activeBuffName: String,
    val activeBuffIcon: String,
    val activeBuffSummary: String,
    val specialEffectTag: String
)

/**
 * A rich Material 3 card component displaying unlocked unique combat buffs
 * and cumulative passive bonuses tied to affinity milestones in the character profile.
 */
@Composable
fun CharacterAffinityCombatBuffsCard(
    character: Character,
    modifier: Modifier = Modifier,
    onOpenMilestonesList: (() -> Unit)? = null
) {
    val currentPoints = character.affinityPoints
    val currentTierLevel = remember(currentPoints) { AffinityData.getLevelForPoints(currentPoints) }
    val currentTier = remember(currentPoints) { AffinityData.getTierForPoints(currentPoints) }
    val milestones = remember(character.affinityPoints, character.archetypeId) {
        AffinityMilestoneCalculator.getMilestonesForCharacter(character)
    }
    val cumulativeBonuses = remember(character.affinityPoints, character.archetypeId) {
        AffinityMilestoneCalculator.getCumulativePassiveBonuses(character)
    }

    var selectedMilestoneForDetail by remember { mutableStateOf<AffinityMilestoneReward?>(null) }
    var isExpandedList by remember { mutableStateOf(false) }

    val unlockedCount = milestones.count { it.isUnlocked }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("character_affinity_combat_buffs_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF160A22)),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.7f),
                    Color(currentTier.colorHex).copy(alpha = 0.5f),
                    Color(0xFF261038)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- HEADER ---
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A148C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = "Bojové Milníky & Pasivní Bonusy",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "$unlockedCount z 6 milníků odemčeno (${character.affinityPoints} bodů)",
                            fontSize = 10.sp,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(currentTier.colorHex).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(currentTier.colorHex))
                ) {
                    Text(
                        text = "Úroveň $currentTierLevel / 6",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(currentTier.colorHex),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // --- HERO BANNER: ACTIVE COMBAT BUFF ---
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF231032),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.45f)),
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(cumulativeBonuses.activeBuffIcon, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = cumulativeBonuses.activeBuffName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Unikátní afinitní schopnost",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.4f),
                            border = BorderStroke(0.6.dp, Color(0xFF69F0AE))
                        ) {
                            Text(
                                text = "✨ AKTIVNÍ V BOJI",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF69F0AE),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = cumulativeBonuses.activeBuffSummary,
                        fontSize = 11.sp,
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.SemiBold
                    )

                    if (cumulativeBonuses.specialEffectTag.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Speciální efekt:", fontSize = 9.sp, color = Color.Gray)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF6A1B9A).copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, Color(0xFFE040FB))
                            ) {
                                Text(
                                    text = "⚡ ${cumulativeBonuses.specialEffectTag}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE040FB),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- CUMULATIVE PASSIVE BONUSES GRID ---
            Text(
                text = "Celkové kumulativní pasivní bonusy z milníků:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray.copy(alpha = 0.9f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PassiveStatBadge(
                    icon = "💖",
                    label = "Max HP",
                    value = "+${cumulativeBonuses.totalHp}",
                    color = Color(0xFF81C784),
                    modifier = Modifier.weight(1f)
                )
                PassiveStatBadge(
                    icon = "⚔️",
                    label = "Útok",
                    value = "+${cumulativeBonuses.totalAttackPercent}%",
                    color = Color(0xFFFF8A80),
                    modifier = Modifier.weight(1f)
                )
                PassiveStatBadge(
                    icon = "🛡️",
                    label = "Obrana",
                    value = "+${cumulativeBonuses.totalDefense}",
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f)
                )
                PassiveStatBadge(
                    icon = "⚡",
                    label = "Crit",
                    value = "+${cumulativeBonuses.totalCritPercent}%",
                    color = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )
                if (cumulativeBonuses.totalHpRegenPerTurn > 0) {
                    PassiveStatBadge(
                        icon = "🩸",
                        label = "Regen",
                        value = "+${cumulativeBonuses.totalHpRegenPerTurn}/k",
                        color = Color(0xFFFF4081),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- MILESTONE TIMELINE / NODES ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Přehled všech 6 milníků:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(
                    onClick = {
                        HapticManager.vibrateClick()
                        isExpandedList = !isExpandedList
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isExpandedList) "Sbalit seznam ▲" else "Zobrazit všech 6 ▼",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            // Horizontal mini cards scroll for milestones
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(milestones) { milestone ->
                    MilestoneQuickCard(
                        milestone = milestone,
                        isCurrentTier = milestone.tierLevel == currentTierLevel,
                        onClick = {
                            HapticManager.vibrateClick()
                            selectedMilestoneForDetail = milestone
                        }
                    )
                }
            }

            // Expanded vertical breakdown if user clicked "Zobrazit všech 6"
            AnimatedVisibility(
                visible = isExpandedList,
                enter = fadeIn() + androidx.compose.animation.expandVertically(),
                exit = fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    milestones.forEach { milestone ->
                        MilestoneDetailedRow(
                            milestone = milestone,
                            isCurrentTier = milestone.tierLevel == currentTierLevel,
                            onClick = {
                                HapticManager.vibrateClick()
                                selectedMilestoneForDetail = milestone
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal popup when clicking on a milestone card
    if (selectedMilestoneForDetail != null) {
        MilestoneDetailDialog(
            milestone = selectedMilestoneForDetail!!,
            onDismiss = { selectedMilestoneForDetail = null }
        )
    }
}

/**
 * Single passive stat metric card.
 */
@Composable
private fun PassiveStatBadge(
    icon: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1F0E2A),
        border = BorderStroke(0.6.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(icon, fontSize = 8.sp)
                Text(value, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
            }
            Text(label, fontSize = 7.sp, color = Color.LightGray.copy(alpha = 0.7f))
        }
    }
}

/**
 * Compact horizontal milestone card.
 */
@Composable
private fun MilestoneQuickCard(
    milestone: AffinityMilestoneReward,
    isCurrentTier: Boolean,
    onClick: () -> Unit
) {
    val tierColor = Color(milestone.tierInfo.colorHex)
    val cardBg = if (milestone.isUnlocked) {
        if (isCurrentTier) Color(0xFF2D143E) else Color(0xFF1E0E29)
    } else {
        Color(0xFF120818)
    }

    val borderColor = if (isCurrentTier) {
        Color(0xFFFFD700)
    } else if (milestone.isUnlocked) {
        tierColor.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cardBg,
        border = BorderStroke(if (isCurrentTier) 1.2.dp else 0.8.dp, borderColor),
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() }
            .testTag("milestone_quick_card_${milestone.tierLevel}")
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (milestone.isUnlocked) tierColor.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Tier ${milestone.tierLevel}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (milestone.isUnlocked) tierColor else Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                if (milestone.isUnlocked) {
                    Text("✓", fontSize = 10.sp, color = Color(0xFF69F0AE), fontWeight = FontWeight.Black)
                } else {
                    Text("🔒", fontSize = 9.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(milestone.characterSpecificBuff.icon, fontSize = 14.sp)
                Text(
                    text = milestone.characterSpecificBuff.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (milestone.isUnlocked) Color.White else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = milestone.characterSpecificBuff.perkEffectSummary,
                fontSize = 8.sp,
                color = if (milestone.isUnlocked) Color(0xFFFFD54F) else Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 10.sp
            )

            if (!milestone.isUnlocked) {
                LinearProgressIndicator(
                    progress = { milestone.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFFFD700),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = "Zbývá ${milestone.pointsNeededToUnlock} bodů",
                    fontSize = 7.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Detailed vertical row representation for expanded view.
 */
@Composable
private fun MilestoneDetailedRow(
    milestone: AffinityMilestoneReward,
    isCurrentTier: Boolean,
    onClick: () -> Unit
) {
    val tierColor = Color(milestone.tierInfo.colorHex)
    val containerBg = if (milestone.isUnlocked) Color(0xFF1F0E2A) else Color(0xFF130919)
    val borderCol = if (isCurrentTier) Color(0xFFFFD700) else if (milestone.isUnlocked) tierColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerBg,
        border = BorderStroke(if (isCurrentTier) 1.2.dp else 0.6.dp, borderCol),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (milestone.isUnlocked) tierColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, if (milestone.isUnlocked) tierColor else Color.Gray.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = milestone.characterSpecificBuff.icon,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milník ${milestone.tierLevel}: ${milestone.characterSpecificBuff.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (milestone.isUnlocked) Color.White else Color.Gray
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (milestone.isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.35f) else Color.DarkGray.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = if (milestone.isUnlocked) "ODEMČENO" else "OD ${milestone.tierInfo.minPoints} BODŮ",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (milestone.isUnlocked) Color(0xFF69F0AE) else Color.LightGray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = milestone.characterSpecificBuff.perkEffectSummary,
                    fontSize = 10.sp,
                    color = if (milestone.isUnlocked) Color(0xFFFFE082) else Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "🛡️ Pasiv: ${milestone.tierInfo.combatBonusDescription}",
                    fontSize = 9.sp,
                    color = if (milestone.isUnlocked) Color(0xFFFF80AB) else Color.Gray
                )
            }
        }
    }
}

/**
 * Inspection modal for a selected milestone reward.
 */
@Composable
private fun MilestoneDetailDialog(
    milestone: AffinityMilestoneReward,
    onDismiss: () -> Unit
) {
    val tierColor = Color(milestone.tierInfo.colorHex)
    val buff = milestone.characterSpecificBuff

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF140B1A),
            border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(buff.icon, fontSize = 24.sp)
                        Column {
                            Text(
                                text = buff.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Milník ${milestone.tierLevel} • ${milestone.tierInfo.title}",
                                fontSize = 10.sp,
                                color = tierColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Status Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (milestone.isUnlocked) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFF3E2723).copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (milestone.isUnlocked) Color(0xFF69F0AE) else Color(0xFFFF8A80)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (milestone.isUnlocked) "✓ Tento milník je aktivní v boji!" else "🔒 Vyžaduje ${milestone.tierInfo.minPoints} bodů náklonnosti",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (milestone.isUnlocked) Color(0xFF69F0AE) else Color(0xFFFF8A80)
                        )
                        if (!milestone.isUnlocked) {
                            Text("Zbývá ${milestone.pointsNeededToUnlock} pts", fontSize = 9.sp, color = Color.LightGray)
                        }
                    }
                }

                // Lore Description
                Text(
                    text = buff.description,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                // Combat Benefits Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1F0E2A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚔️ Účinek v boji:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        Text(buff.perkEffectSummary, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("🛡️ Pasivní bonus: ${milestone.tierInfo.combatBonusDescription}", fontSize = 10.sp, color = Color(0xFFFF80AB))
                        if (buff.specialEffectTag.isNotBlank()) {
                            Text("⚡ Značka účinku: ${buff.specialEffectTag}", fontSize = 10.sp, color = Color(0xFFE040FB))
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text("Rozumím", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
