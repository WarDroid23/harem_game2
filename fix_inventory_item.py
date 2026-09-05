import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

target = """data class InventoryItem(
    val id: String,
    val name: String,
    val description: String,
    var count: Int = 1,
    val price: Int = 10,
    val category: String = "potion", // "equipment", "gift", "combat", "potion", "consumable", "quest", "artifact", "key", "alchemy"
    val equipSlot: String? = null, // "weapon", "armor", "accessory"
    val combatBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val icon: String = "📦",
    val rarity: String = "Běžný", // "Běžný", "Vzácný", "Epický", "Legendární"
    val effectDescription: String = ""
)"""

replacement = """data class InventoryItem(
    val id: String,
    val name: String,
    val description: String,
    var count: Int = 1,
    val price: Int = 10,
    val category: String = "potion", // "equipment", "gift", "combat", "potion", "consumable", "quest", "artifact", "key", "alchemy"
    val icon: String = "📦",
    val rarity: String = "Běžný", // "Běžný", "Vzácný", "Epický", "Legendární"
    val effectDescription: String = "",
    val equipSlot: String? = null, // "weapon", "armor", "accessory"
    val combatBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0
)"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
