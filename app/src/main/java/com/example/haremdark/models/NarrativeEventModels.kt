package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Category of random narrative events.
 */
@Serializable
enum class EventCategory(val displayName: String, val icon: String, val colorHex: Long) {
    WILDERNESS("Průzkum divočiny", "🌲", 0xFF4CAF50),
    METROPOLIS("Intriky metropole", "🏛️", 0xFFFFB300),
    HAREM_COURT("Harémové záležitosti", "💖", 0xFFE91E63),
    OCCULT_ANOMALY("Temný okultismus", "🔮", 0xFF9C27B0),
    INQUISITION("Inkvizice & Soud", "⚖️", 0xFFF44336),
    UNDERGROUND("Podsvětí & Syndikát", "🗡️", 0xFF78909C)
}

/**
 * Rarity tier of random events affecting reward scale and uniqueness.
 */
@Serializable
enum class EventRarity(val title: String, val colorHex: Long, val weight: Int) {
    COMMON("Běžná", 0xFFB0BEC5, 50),
    UNCOMMON("Neobvyklá", 0xFF4CAF50, 30),
    RARE("Vzácná", 0xFF29B6F6, 15),
    EPIC("Epická", 0xFFAB47BC, 8),
    LEGENDARY("Legendární", 0xFFFFD700, 3)
}

/**
 * Skill check requirement for branching choices.
 */
@Serializable
data class SkillCheckRequirement(
    val skillKey: String, // e.g., "svadeni", "veleni", "temnota", "vyjednavani", "dominance", "boj"
    val skillDisplayName: String,
    val reqLevel: Int = 1,
    val baseSuccessPercent: Int = 70
)

/**
 * Detailed outcome resulting from making a specific choice in a narrative encounter.
 */
@Serializable
data class EventOutcome(
    val outcomeTitle: String,
    val outcomeNarrative: String,
    val goldDelta: Int = 0,
    val manaDelta: Int = 0,
    val darkEnergyDelta: Int = 0,
    val sexEnergyDelta: Int = 0,
    val influenceDelta: Int = 0,
    val reputationDelta: Int = 0,
    val inquisitionAlertDelta: Int = 0,
    val materialsReward: Map<String, Int> = emptyMap(), // "wood", "stone", "iron", etc.
    val itemReward: InventoryItem? = null,
    val haremExp: Int = 0,
    val playerXp: Int = 0,
    val moraleDelta: Int = 0,
    val recruitedGirlArchetype: String? = null,
    val recruitedGirlName: String? = null,
    val recruitedGirlRole: String? = null,
    val logSummary: String
)

/**
 * A branching decision choice in a narrative event.
 */
@Serializable
data class NarrativeEventChoice(
    val id: String,
    val choiceText: String,
    val description: String,
    val icon: String = "👉",
    val costGold: Int = 0,
    val costDarkEnergy: Int = 0,
    val costSexEnergy: Int = 0,
    val skillCheck: SkillCheckRequirement? = null,
    val successOutcome: EventOutcome,
    val failureOutcome: EventOutcome? = null
)

/**
 * Complete narrative encounter event.
 */
@Serializable
data class NarrativeEvent(
    val id: String,
    val title: String,
    val icon: String,
    val category: EventCategory,
    val rarity: EventRarity,
    val locationTag: String,
    val narrativeStory: String,
    val involvedCompanionName: String? = null,
    val choices: List<NarrativeEventChoice>
)
