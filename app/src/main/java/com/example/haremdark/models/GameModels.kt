package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class Weapon(
    val name: String,
    val type: String, // "kratka", "dlouha", "magicka", "bic"
    val damage: Int,
    val price: Int,
    val weight: Float = 1.0f,
    val special: String? = null,
    val darkBonus: Int = 0
)

@Serializable
data class Boss(
    val id: String,
    val name: String,
    val location: String,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val rewardGold: Int,
    val rewardXp: Int,
    val phaseName: String,
    val description: String
)

@Serializable
data class Quest(
    val id: String,
    val title: String,
    val category: String, // "Příběh", "Podsvětí", "Inkvizice"
    val description: String,
    val reqLevel: Int,
    val reqGold: Int = 0,
    val reqConcubines: Int = 0,
    val rewardGold: Int,
    val rewardXp: Int,
    val rewardDarkEnergy: Int = 0,
    val rewardReputation: Int = 0
)

@Serializable
data class AlchemyRecipe(
    val id: String,
    val name: String,
    val description: String,
    val goldCost: Int,
    val darkCost: Int,
    val resultItem: InventoryItem
)

@Serializable
data class InventoryItem(
    val id: String,
    val name: String,
    val description: String,
    var count: Int = 1,
    val price: Int = 10,
    val category: String = "potion", // "gift", "combat", "potion", "consumable", "quest", "artifact", "key", "alchemy"
    val icon: String = "📦",
    val rarity: String = "Běžný", // "Běžný", "Vzácný", "Epický", "Legendární"
    val effectDescription: String = ""
)

@Serializable
data class Building(
    val type: String,
    var level: Int = 0,
    val name: String,
    val description: String,
    val baseCost: Int = 150
)

@Serializable
data class MafiaTerritory(
    val id: String,
    val name: String,
    var level: Int = 1,
    var securityLevel: Int = 50,
    val baseIncome: Int = 40
)

@Serializable
data class Agent(
    val name: String,
    var level: Int = 1,
    var loyalty: Int = 60,
    val specialty: String = "inkasista",
    var tired: Int = 0
)

@Serializable
data class CharacterReward(
    val archetypeId: String,
    val name: String,
    val rarity: String, // "Běžná", "Vzácná", "Epická", "Legendární"
    val dropRatePercent: Int,
    val traitDescription: String
)

@Serializable
data class DomainLocation(
    val id: String,
    val name: String,
    val title: String,
    val region: String,
    val difficulty: String, // "Snadná", "Střední", "Těžká", "Smrtící", "Královská"
    val difficultyStars: Int = 1,
    val minPlayerLevel: Int = 1,
    val travelCostEnergy: Int = 5,
    val travelCostGold: Int = 0,
    val description: String,
    val potentialArchetypes: List<String> = emptyList(),
    val potentialRewards: List<CharacterReward> = emptyList(),
    val resourceDrops: List<String> = emptyList(),
    val bossId: String? = null,
    val bannerDrawableRes: Int = 0,
    val accentColor: Long = 0xFF9C27B0,
    val mapX: Float = 0.5f,
    val mapY: Float = 0.5f
)

@Serializable
data class Concubine(
    val id: String,
    var name: String,
    var age: Int = 19,
    var archetypeId: String = "subka",
    var rarity: Int = 1,
    var hp: Int = 100,
    var maxHp: Int = 100,
    var srdce: Int = 70,
    var poslusnost: Int = 30,
    var vlhkost: Int = 50,
    var submisivita: Int = 40,
    var loajalita: Int = 30,
    var nalada: String = "neutrální",
    var plodnost: Int = 50,
    var duvera: Int = 30,
    var touha: Int = 50,
    var strach: Int = 30,
    var broken: Int = 0,
    var mindbreak: Int = 0,
    var painAddiction: Int = 0,
    var humiliation: Int = 0,
    var bloodlust: Int = 0,
    var scarred: Int = 0,
    var ownedMark: Boolean = false,
    var tehotna: Boolean = false,
    var dnyTehotenstvi: Int = 0,
    var deti: Int = 0,
    var zavislost: Int = 0,
    var typZavislosti: String? = null,
    var fazeZkazenosti: Int = 0,
    var role: String = "členka harému",
    var partnerka: Boolean = false,
    var jeManzelkou: Boolean = false,
    var oblibena: Boolean = false,
    var romanceBody: Int = 0,
    var affinityPoints: Int = 15,
    var affinityLevel: Int = 1,
    var lastInteractionDay: Int = 0,
    var osudId: String = "",
    var osudKrok: Int = 0,
    var naNajmu: Boolean = false,
    var klient: String? = null,
    var typNajmu: String? = null,
    var najemZbyvaDni: Int = 0,
    var najemPrijemCelkem: Int = 0
)

@Serializable
data class Player(
    var name: String = "LordRusty23",
    var level: Int = 1,
    var xp: Int = 0,
    var xpNext: Int = 100,
    var hp: Int = 100,
    var maxHp: Int = 100,
    var gold: Int = 500,
    var sexEnergy: Int = 100,
    var darkEnergy: Int = 50,
    var maxSexEnergy: Int = 100,
    var maxDarkEnergy: Int = 100,
    var dominance: Int = 5,
    var killCount: Int = 0,
    var day: Int = 1,
    var skillPoints: Int = 2,
    var skills: MutableMap<String, Int> = mutableMapOf(
        "svadeni" to 0,
        "obchod" to 0,
        "veleni" to 0,
        "temnota" to 0,
        "obrana" to 0,
        "dominance" to 0,
        "strelba" to 0,
        "boj" to 0,
        "vyjednavani" to 0,
        "vytrvalost" to 0
    ),
    var reputation: Int = 5,
    var cityTitle: String = "Neznámý vládce",
    var inquisitionInfluence: Int = 15,
    var equippedWeaponIndex: Int = 0,
    var weapons: MutableList<Weapon> = mutableListOf(
        Weapon("Dýka ze stříbra", "kratka", 15, 100),
        Weapon("Bič z dračí kůže", "bic", 25, 250)
    ),
    var items: MutableList<InventoryItem> = mutableListOf(
        // Combat Consumables
        InventoryItem("hojivy_balzam", "Hojivý balzám", "Okamžitě uzdravuje 45 HP pánovi nebo dívce.", 3, 25, "combat", "🧪", "Běžný", "+45 HP"),
        InventoryItem("elixir_touhy", "Elixír touhy", "Okamžitě probouzí v těle nespoutanou vášeň a doplňuje energii.", 2, 40, "combat", "🔮", "Vzácný", "+35 TE & +35 SE"),
        InventoryItem("serum_poslusnost", "Sérum poslušnosti", "Koncentrovaná alchymie podlamující vzdor a strach.", 1, 90, "combat", "💉", "Epický", "+15 Poslušnost & Loajalita"),
        // Gifts
        InventoryItem("gift_roses", "Kytice nočních růží", "Voňavé temné růže, které vyvolávají příjemné chvění a něhu.", 2, 25, "gift", "🌹", "Běžný", "+8 Loajalita, +6 Touha, +10 Náklonnost"),
        InventoryItem("drahy_obojek", "Zlatý obojek pána", "Symbol absolutního vlastnictví a věrnosti vyrytý rodovým erbem.", 1, 150, "gift", "👑", "Legendární", "+25 Loajalita, +25 Poslušnost"),
        InventoryItem("gift_perfume", "Noční parfém", "Omamná afrodiziakální esence z půlnočních květů.", 1, 70, "gift", "🌸", "Vzácný", "+12 Loajalita, +14 Touha"),
        // Quest Items
        InventoryItem("cerna_pecet", "Pečeť Černého syndikátu", "Vzácná vosková pečeť potvrzující autoritu v podsvětí města.", 1, 200, "quest", "📜", "Vzácný", "Odemknutí vlivu u pašeráků"),
        InventoryItem("temny_klic", "Klíč ke starým kobkám", "Prastarý železný klíč nalezený v podzemních ruinách chrámu.", 1, 120, "quest", "🗝️", "Epický", "Přístup k tajné kryptě"),
        InventoryItem("kralovska_listina", "Královská výsadní listina", "Listina s puncem královského rodu pro jednání s inkvizicí.", 1, 350, "quest", "⚜️", "Legendární", "+25 Reputace v metropoli")
    ),
    var bankGold: Int = 0,
    var agents: MutableList<Agent> = mutableListOf(
        Agent("Vesper, Noční stín", 1, 75, "vymahač")
    )
)

@Serializable
data class CombatLogEntry(
    val turn: Int,
    val type: String, // "player_attack", "player_spell", "player_heal", "player_defend", "player_support", "enemy_attack", "enemy_special", "system", "victory", "defeat"
    val message: String
)

@Serializable
data class CombatSession(
    val boss: Boss,
    var bossHp: Int,
    val bossMaxHp: Int,
    var playerHp: Int,
    val playerMaxHp: Int,
    var turnCount: Int = 1,
    var isDefending: Boolean = false,
    var enemyBleedTurns: Int = 0,
    var enemyStunned: Boolean = false,
    var activeBuff: String? = null,
    val logEntries: List<CombatLogEntry> = emptyList(),
    val log: List<String> = emptyList(),
    var isOver: Boolean = false,
    var victory: Boolean = false,
    var lootGained: String? = null
)

@Serializable
data class GameSave(
    val version: String = "22.1-dark",
    val saveDate: String,
    val slotNumber: Int,
    val player: Player,
    val concubines: List<Concubine>,
    val haremLevel: Int = 1,
    val haremExp: Int = 0,
    val haremMaxExp: Int = 100,
    val buildings: List<Building>,
    val territories: List<MafiaTerritory>,
    val currentDomainId: String = "temny_hvozd",
    val unlockedDomains: List<String> = listOf("temny_hvozd", "ruiny_chramu"),
    val defeatedBosses: List<String> = emptyList(),
    val currentTheme: String = "Temné dominium",
    val completedQuests: List<String> = emptyList(),
    val gameLog: List<String> = emptyList()
)
