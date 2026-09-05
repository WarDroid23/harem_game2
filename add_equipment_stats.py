import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

target_item = """data class InventoryItem(
    val id: String,
    val name: String,
    val description: String,
    var count: Int = 1,
    val price: Int = 10,
    val category: String = "potion", // "gift", "combat", "potion", "consumable", "quest", "artifact", "key", "alchemy"
    val icon: String = "📦",
    val rarity: String = "Běžný", // "Běžný", "Vzácný", "Epický", "Legendární"
    val effectDescription: String = ""
)"""

replacement_item = """data class InventoryItem(
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

text = text.replace(target_item, replacement_item)

target_char = """    var inventory: MutableList<InventoryItem> = mutableListOf(),
    var level: Int = 1,
    var xp: Int = 0,
    var skillPoints: Int = 0,
    var skills: MutableMap<String, Int> = mutableMapOf("combat" to 0, "defense" to 0, "production" to 0, "rental" to 0),
    var equippedWeapon: Weapon? = null"""

replacement_char = """    var inventory: MutableList<InventoryItem> = mutableListOf(),
    var level: Int = 1,
    var xp: Int = 0,
    var skillPoints: Int = 0,
    var skills: MutableMap<String, Int> = mutableMapOf("combat" to 0, "defense" to 0, "production" to 0, "rental" to 0),
    var equippedWeapon: Weapon? = null,
    var equipment: MutableMap<String, InventoryItem?> = mutableMapOf("weapon" to null, "armor" to null, "accessory" to null)
"""
text = text.replace(target_char, replacement_char)

target_session = """data class CombatSession(
    val boss: Boss,
    var bossHp: Int,
    val bossMaxHp: Int,
    var playerHp: Int,
    val playerMaxHp: Int,
    var turnCount: Int = 1,"""

replacement_session = """data class CombatSession(
    val boss: Boss,
    var bossHp: Int,
    val bossMaxHp: Int,
    var playerHp: Int,
    val playerMaxHp: Int,
    var deployedCharacterId: String? = null, // ID of the harem character deployed
    var wave: Int = 1, // Waves of enemies
    var maxWaves: Int = 1,
    var turnCount: Int = 1,"""
text = text.replace(target_session, replacement_session)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
