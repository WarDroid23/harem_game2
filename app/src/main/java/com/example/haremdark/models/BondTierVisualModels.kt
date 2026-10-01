package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Color palette definition unlocked as a companion's intimacy (Bond Tier) progresses.
 */
@Serializable
data class BondColorPalette(
    val id: String,
    val name: String,
    val requiredAffinityLevel: Int,
    val icon: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val glowColorHex: Long,
    val cardGradientHexes: List<Long>,
    val description: String,
    val titleReward: String,
    val statPerkSummary: String
)

/**
 * Unlocked Portrait Art Variant associated with Bond Tier milestones.
 */
@Serializable
data class BondPortraitVariant(
    val id: String,
    val title: String,
    val requiredAffinityLevel: Int,
    val targetArchetypeId: String, // "subka", "odvazna", "touha", "slechticna", "all"
    val drawableRes: Int,
    val badgeIcon: String,
    val description: String,
    val auraParticleType: String = "NONE" // "NONE", "EMERALD_GLOW", "CYAN_CRYSTAL", "HEART_PULSE", "ASTRAL_SHIMMER", "GOLDEN_FLAMES"
)

/**
 * Complete Bond Tier Milestone definition containing perks, cosmetic unlocks, and narrative rewards.
 */
@Serializable
data class BondTierMilestone(
    val tierLevel: Int,
    val name: String,
    val stageTitle: String,
    val subtitle: String,
    val icon: String,
    val minAffinityPoints: Int,
    val maxAffinityPoints: Int,
    val palette: BondColorPalette,
    val unlockedPortraitVariants: List<BondPortraitVariant> = emptyList(),
    val combatPerkSummary: String,
    val haremPerkSummary: String,
    val dialogueQuote: String
)
