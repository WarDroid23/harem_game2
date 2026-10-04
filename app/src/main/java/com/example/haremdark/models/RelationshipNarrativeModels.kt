package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Stages of character relationship and emotional bond.
 */
@Serializable
enum class RelationshipStage(
    val stageRank: Int,
    val title: String,
    val minAffection: Int,
    val maxAffection: Int,
    val icon: String,
    val colorHex: Long,
    val description: String
) {
    COLD_DISTANCE(
        1,
        "Chladný odstup",
        0,
        24,
        "❄️",
        0xFF90CAF9,
        "Dívka si udržuje odstup a opatrnost. Její odpovědi jsou formální a zdrženlivé."
    ),
    GUARDED_TRUST(
        2,
        "Opatrná důvěra",
        25,
        49,
        "🌿",
        0xFF81C784,
        "Začíná ti důvěřovat a otevírá se o své minulosti. Sdílí drobné postřehy a rady."
    ),
    AFFECTION_ALLIANCE(
        3,
        "Náklonnost & Spojenectví",
        50,
        74,
        "💖",
        0xFFFF80AB,
        "Vnímá tě jako ochránce a pána svého srdce. Odemčeny unikátní osobní dialogy a bojové synergie."
    ),
    PASSIONATE_DEVOTION(
        4,
        "Vášnivá oddanost",
        75,
        89,
        "🔥",
        0xFFFF5252,
        "Její tělo i duše patří tobě. Odemčeny intimní noční rozmluvy, tajné osobní úkoly a +20% bojový bonus."
    ),
    SOULBOUND_ETERNITY(
        5,
        "Spřízněná duše & Pouto věčnosti",
        90,
        150,
        "👑",
        0xFFFFD700,
        "Absolutní pouto přesahující smrt. Odemčeno unikátní příběhové vyvrcholení a ultimátní harémové kombo."
    );

    companion object {
        fun fromAffection(affection: Int): RelationshipStage {
            return entries.filter { affection >= it.minAffection }.maxByOrNull { it.minAffection } ?: COLD_DISTANCE
        }
    }
}

/**
 * Player's relational tendency/style towards the companion.
 */
@Serializable
enum class RelationshipTendency(val title: String, val icon: String, val colorHex: Long) {
    ROMANTIC_DEVOTION("Romantická náklonnost", "🌹", 0xFFFF4081),
    DOMINANT_AUTHORITY("Dominantní autorita", "⛓️", 0xFF9C27B0),
    WARRIOR_COMRADE("Bojové spojenectví", "⚔️", 0xFFFF9800),
    CORRUPTED_INTIMACY("Temná vášeň", "🔮", 0xFF7C4DFF)
}

/**
 * A branching option in a relationship dialogue tree.
 */
@Serializable
data class DialogueChoiceOption(
    val id: String,
    val playerText: String,
    val tendency: RelationshipTendency,
    val affectionDelta: Int = 0,
    val loyaltyDelta: Int = 0,
    val obedienceDelta: Int = 0,
    val desireDelta: Int = 0,
    val responseText: String,
    val responseEmotion: String,
    val statBonusDescription: String? = null
)

/**
 * An unlockable narrative conversation path with a companion.
 */
@Serializable
data class CharacterNarrativePath(
    val id: String,
    val title: String,
    val icon: String,
    val requiredStage: RelationshipStage,
    val requiredInteractionsCount: Int,
    val synopsis: String,
    val promptDialogue: String,
    val characterInitialThought: String,
    val options: List<DialogueChoiceOption>,
    val perkRewardTitle: String? = null,
    val perkRewardDescription: String? = null
)

/**
 * Summary of past interaction events and dialogue milestones.
 */
@Serializable
data class InteractionHistoryMilestone(
    val day: Int,
    val title: String,
    val summary: String,
    val affectionGained: Int,
    val tendency: RelationshipTendency,
    val unlockedDialogueTitle: String? = null
)
