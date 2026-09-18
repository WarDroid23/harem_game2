package com.example.haremdark.data

import kotlinx.serialization.Serializable

@Serializable
data class GlobalAffinityMilestone(
    val id: String,
    val title: String,
    val requiredGlobalAffinity: Int,
    val rewardTitleTag: String,
    val rewardAvatarFrame: String,
    val rewardGold: Int,
    val rewardDarkEnergy: Int,
    val description: String,
    val icon: String,
    val colorHex: Long
)

object GlobalAffinityMilestoneData {
    val MILESTONES = listOf(
        GlobalAffinityMilestone(
            id = "global_affinity_1",
            title = "Počátek okouzlení",
            requiredGlobalAffinity = 50,
            rewardTitleTag = "Učedník lásky",
            rewardAvatarFrame = "Bronzový rám rozkoše",
            rewardGold = 100,
            rewardDarkEnergy = 20,
            description = "Dosáhni součtu 50 bodů náklonnosti napříč všemi dívkami v harému.",
            icon = "🥉",
            colorHex = 0xFFCD7F32
        ),
        GlobalAffinityMilestone(
            id = "global_affinity_2",
            title = "Pán svádění",
            requiredGlobalAffinity = 150,
            rewardTitleTag = "Mistr svádění",
            rewardAvatarFrame = "Stříbrný rám vášně",
            rewardGold = 250,
            rewardDarkEnergy = 50,
            description = "Dosáhni součtu 150 bodů náklonnosti napříč všemi dívkami.",
            icon = "🥈",
            colorHex = 0xFFC0C0C0
        ),
        GlobalAffinityMilestone(
            id = "global_affinity_3",
            title = "Vévoda harému",
            requiredGlobalAffinity = 350,
            rewardTitleTag = "Vévoda temného harému",
            rewardAvatarFrame = "Zlatý královský rám",
            rewardGold = 500,
            rewardDarkEnergy = 120,
            description = "Dosáhni součtu 350 bodů náklonnosti v celém panství.",
            icon = "🥇",
            colorHex = 0xFFFFD700
        ),
        GlobalAffinityMilestone(
            id = "global_affinity_4",
            title = "Císař věčné rozkoše",
            requiredGlobalAffinity = 700,
            rewardTitleTag = "Císař temné vášně",
            rewardAvatarFrame = "Rubínový imperiální rám",
            rewardGold = 1000,
            rewardDarkEnergy = 250,
            description = "Dosáhni součtu 700 bodů náklonnosti v celém dominiu.",
            icon = "💎",
            colorHex = 0xFFE91E63
        ),
        GlobalAffinityMilestone(
            id = "global_affinity_5",
            title = "Bohoslavený vládce",
            requiredGlobalAffinity = 1200,
            rewardTitleTag = "Věčný Sovereign",
            rewardAvatarFrame = "Nesmrtelný astrální diamantový rám",
            rewardGold = 2500,
            rewardDarkEnergy = 600,
            description = "Dosáhni součtu 1200 bodů náklonnosti a ovládni absolutní oddanost harému.",
            icon = "👑",
            colorHex = 0xFF00E5FF
        )
    )
}
