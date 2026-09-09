package com.example.haremdark.models

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Categories of collectible gifts that can be presented to harem characters.
 */
@Serializable
enum class GiftCategory(val displayName: String, val icon: String) {
    JEWELRY("Šperky & Klenoty", "💎"),
    FLOWERS_PERFUMES("Květiny & Parfémy", "🌹"),
    SWEETS_WINE("Víno & Pochoutky", "🍷"),
    APPAREL_SILK("Hedvábí & Šaty", "👘"),
    RELICS_CURIOS("Relikvie & Kuriozity", "📜"),
    POTIONS_ELIXIRS("Alchymie & Elixíry", "🧪"),
    SPECIAL_BONDS("Královská Pouta", "👑")
}

/**
 * Rarity tiers for gifts with visual accent styling and stat multipliers.
 */
@Serializable
enum class GiftRarity(
    val title: String,
    val colorHex: Long,
    val multiplier: Float
) {
    COMMON("Běžný", 0xFF9E9E9E, 1.0f),
    RARE("Vzácný", 0xFF29B6F6, 1.25f),
    EPIC("Epický", 0xFFAB47BC, 1.6f),
    LEGENDARY("Legendární", 0xFFFFB300, 2.2f),
    MYTHIC("Mýtický", 0xFFFF4081, 3.0f);

    val composeColor: Color
        get() = Color(colorHex)
}

/**
 * Rich data model representing a collectible gift item.
 */
@Serializable
data class GiftItemData(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: GiftCategory,
    val rarity: GiftRarity = GiftRarity.COMMON,
    val goldCost: Int = 30,
    val baseAffinity: Int = 15,
    val loyaltyBonus: Int = 8,
    val desireBonus: Int = 6,
    val trustBonus: Int = 6,
    val obedienceBonus: Int = 4,
    val favoriteArchetypes: List<String> = emptyList(),
    val dislikedArchetypes: List<String> = emptyList(),
    val flavorQuote: String = "přijala dar s potěšením.",
    val archetypeCustomReactions: Map<String, String> = emptyMap()
) {
    fun isFavoriteOf(archetypeId: String): Boolean =
        favoriteArchetypes.contains(archetypeId)

    fun isDislikedBy(archetypeId: String): Boolean =
        dislikedArchetypes.contains(archetypeId)

    fun calculateTotalAffinity(archetypeId: String): Int {
        val base = (baseAffinity * rarity.multiplier).toInt()
        return when {
            isFavoriteOf(archetypeId) -> (base * 1.5f).toInt() + 10
            isDislikedBy(archetypeId) -> (base * 0.6f).toInt().coerceAtLeast(3)
            else -> base
        }
    }

    fun calculateLoyaltyGain(archetypeId: String): Int {
        val base = loyaltyBonus
        return if (isFavoriteOf(archetypeId)) (base * 1.4f).toInt() + 3 else base
    }

    fun calculateDesireGain(archetypeId: String): Int {
        val base = desireBonus
        return if (isFavoriteOf(archetypeId)) (base * 1.4f).toInt() + 3 else base
    }

    fun calculateTrustGain(archetypeId: String): Int {
        val base = trustBonus
        return if (isFavoriteOf(archetypeId)) (base * 1.4f).toInt() + 2 else base
    }

    fun getReactionQuote(archetypeId: String, characterName: String): String {
        val custom = archetypeCustomReactions[archetypeId]
        if (!custom.isNullOrBlank()) return custom
        return if (isFavoriteOf(archetypeId)) {
            "Ach, můj pane... Přesně tohle jsem si tajně přála. Má oddanost patří jen tobě!"
        } else {
            "Děkuji ti za tento dar, můj pane. Velmi si vážím tvé štědrosti a přízně."
        }
    }
}

/**
 * Inventory slot holding a collectible gift item and its count in the player's possession.
 */
data class GiftInventorySlot(
    val item: GiftItemData,
    val quantity: Int
)

/**
 * Result bundle after giving a gift, providing rich feedback for UI animation and dialogue.
 */
data class GiftActionResult(
    val character: Character,
    val gift: GiftItemData,
    val quantity: Int,
    val affinityGained: Int,
    val loyaltyGained: Int,
    val desireGained: Int,
    val trustGained: Int,
    val leveledUp: Boolean,
    val newAffinityLevel: Int,
    val dialogueResponse: String,
    val isFavoriteMatch: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * UI State for the gift inventory browser and modal interactions.
 */
data class GiftInventoryUiState(
    val selectedCategory: GiftCategory? = null,
    val searchQuery: String = "",
    val selectedSlot: GiftInventorySlot? = null,
    val quantityToGift: Int = 1,
    val filterOnlyFavorites: Boolean = false,
    val showPurchaseDialog: Boolean = false,
    val lastGiftResult: GiftActionResult? = null,
    val isGiftingAnimationActive: Boolean = false
)
