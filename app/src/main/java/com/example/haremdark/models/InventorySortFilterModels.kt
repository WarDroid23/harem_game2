package com.example.haremdark.models

import androidx.compose.ui.graphics.Color
import com.example.haremdark.data.GiftInventoryCatalog

/**
 * Filter for item types/categories in inventory screens.
 */
enum class ItemTypeCategory(val id: String, val displayName: String, val icon: String) {
    ALL("all", "Všechny typy", "✨"),
    GIFT("gift", "Dary pro dívky", "🎁"),
    POTION("potion", "Lektvary & Alchymie", "🧪"),
    EQUIPMENT("equipment", "Výbava & Zbraně", "🛡️"),
    QUEST("quest", "Úkolové relikvie", "📜"),
    ARTIFACT("artifact", "Vzácné artefakty", "🔮")
}

/**
 * Filter for item quantity (stack size) in inventory screens.
 */
enum class ItemQuantityFilter(val id: String, val displayName: String, val icon: String) {
    ALL("all", "Všechna množství", "📦"),
    SINGLE("single", "Pouze 1 kus", "1️⃣"),
    MULTIPLE("multiple", "Více kusů (>1)", "🔢"),
    LARGE_STACK("large", "Zásoba (≥5 ks)", "📚"),
    BULK("bulk", "Hromady (≥10 ks)", "🏭")
}

/**
 * Filter for affinity bonus provided to concubines.
 */
enum class ItemAffinityFilter(val displayName: String, val minAffinity: Int, val icon: String) {
    ALL("Vše", 0, "✨"),
    ANY_BONUS("S bonusem (>0)", 1, "💖"),
    HIGH_BONUS("Vysoký bonus (≥20)", 20, "🔥"),
    LEGENDARY_BONUS("Královský dar (≥40)", 40, "👑")
}

/**
 * Global helpers for inventory items sorting and filtering.
 */
object InventoryFilterUtils {

    fun getItemAffinityBonusValue(item: InventoryItem): Int {
        val catalogGift = GiftInventoryCatalog.ALL_GIFTS.find { it.id == item.id }
        if (catalogGift != null) {
            return catalogGift.baseAffinity
        }
        val regexAffinity = Regex("""\+(\d+)\s*(?:Náklonnost|Affinity|Pouto)""", RegexOption.IGNORE_CASE)
        val matchAff = regexAffinity.find(item.effectDescription)
        if (matchAff != null) {
            return matchAff.groupValues[1].toIntOrNull() ?: 0
        }
        val regexLoyalty = Regex("""\+(\d+)\s*(?:Loajalita|Loyalty|Věrnost)""", RegexOption.IGNORE_CASE)
        val matchLoy = regexLoyalty.find(item.effectDescription)
        if (matchLoy != null) {
            val pts = matchLoy.groupValues[1].toIntOrNull() ?: 0
            return pts * 2
        }
        val cat = item.category.lowercase()
        if (cat.contains("gift") || cat.contains("dar") || item.id.startsWith("gift_") || item.id == "drahy_obojek") {
            return when (item.rarity) {
                "Legendární", "Mýtický" -> 50
                "Epický" -> 35
                "Vzácný" -> 22
                else -> 15
            }
        }
        return 0
    }

    fun getRarityRank(rarity: String): Int = when (rarity.lowercase()) {
        "mýtický", "mythic" -> 5
        "legendární", "legendary" -> 4
        "epický", "epic" -> 3
        "vzácný", "rare" -> 2
        else -> 1
    }

    fun getItemCategoryKey(item: InventoryItem): String {
        val cat = item.category.lowercase()
        return when {
            cat.contains("gift") || cat.contains("dar") || item.id.startsWith("gift_") || item.id == "drahy_obojek" -> "gift"
            cat.contains("equipment") || cat.contains("weapon") || cat.contains("armor") || cat.contains("accessory") || item.equipSlot != null -> "equipment"
            cat.contains("quest") || cat.contains("key") || cat.contains("relic") || cat.contains("document") || item.id.contains("pecet") || item.id.contains("klic") || item.id.contains("listina") -> "quest"
            cat.contains("artifact") -> "artifact"
            else -> "potion" // potions, combat consumables, alchemy
        }
    }

    fun getItemCategoryOrderRank(item: InventoryItem): Int = when (getItemCategoryKey(item)) {
        "gift" -> 1
        "potion" -> 2
        "equipment" -> 3
        "artifact" -> 4
        "quest" -> 5
        else -> 6
    }

    fun matchesType(item: InventoryItem, filter: ItemTypeCategory): Boolean {
        if (filter == ItemTypeCategory.ALL) return true
        val catKey = getItemCategoryKey(item)
        return when (filter) {
            ItemTypeCategory.ALL -> true
            ItemTypeCategory.GIFT -> catKey == "gift"
            ItemTypeCategory.POTION -> catKey == "potion" || item.category in listOf("potion", "combat", "consumable", "alchemy")
            ItemTypeCategory.EQUIPMENT -> catKey == "equipment" || item.equipSlot != null
            ItemTypeCategory.QUEST -> catKey == "quest" || item.category in listOf("quest", "key")
            ItemTypeCategory.ARTIFACT -> catKey == "artifact" || item.category == "artifact"
        }
    }

    fun matchesQuantity(item: InventoryItem, filter: ItemQuantityFilter): Boolean {
        return when (filter) {
            ItemQuantityFilter.ALL -> true
            ItemQuantityFilter.SINGLE -> item.count == 1
            ItemQuantityFilter.MULTIPLE -> item.count > 1
            ItemQuantityFilter.LARGE_STACK -> item.count >= 5
            ItemQuantityFilter.BULK -> item.count >= 10
        }
    }
}
