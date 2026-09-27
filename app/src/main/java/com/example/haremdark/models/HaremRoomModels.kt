package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class RoomDecorationSlot(val title: String, val icon: String) {
    BED("Postel & Nebesa", "🛏️"),
    FURNITURE("Nábytek & Křesla", "🪑"),
    LIGHTING("Atmosféra & Světla", "🕯️"),
    WALL_DECOR("Tapiserie & Zrcadla", "🖼️"),
    AROMATICS("Květiny & Afrodiziaka", "🌸")
}

@Serializable
data class RoomDecorationItem(
    val id: String,
    val name: String,
    val slot: RoomDecorationSlot,
    val level: Int = 1,
    val icon: String,
    val description: String,
    val comfortPoints: Int,
    val goldCost: Int,
    val darkEnergyCost: Int,
    val materialCosts: Map<String, Int> = emptyMap(),
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val moraleBonusPerDay: Int = 2,
    val affinityMultiplier: Float = 0.05f
)

@Serializable
data class RoomStatBonus(
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val moraleBonusPerDay: Int = 0,
    val affinityMultiplier: Float = 0f
)

@Serializable
data class HaremRoom(
    val id: String,
    val name: String,
    val icon: String = "🏰",
    var assignedCharacterId: String? = null,
    val installedDecorations: MutableMap<String, RoomDecorationItem> = mutableMapOf(), // slot.name to item
    var themeName: String = "Královské apartmá"
) {
    val totalComfort: Int
        get() = installedDecorations.values.fold(0) { acc, item -> acc + item.comfortPoints }

    val totalAttackBonus: Int
        get() = installedDecorations.values.fold(0) { acc, item -> acc + item.attackBonus }

    val totalDefenseBonus: Int
        get() = installedDecorations.values.fold(0) { acc, item -> acc + item.defenseBonus }

    val totalHpBonus: Int
        get() = installedDecorations.values.fold(0) { acc, item -> acc + item.hpBonus }

    val totalMoraleBonusPerDay: Int
        get() = installedDecorations.values.fold(0) { acc, item -> acc + item.moraleBonusPerDay }

    val totalAffinityBonus: Float
        get() = installedDecorations.values.fold(0f) { acc, item -> acc + item.affinityMultiplier }

    fun getCumulativeStatBonus(): RoomStatBonus {
        return RoomStatBonus(
            attackBonus = totalAttackBonus,
            defenseBonus = totalDefenseBonus,
            hpBonus = totalHpBonus,
            moraleBonusPerDay = totalMoraleBonusPerDay,
            affinityMultiplier = totalAffinityBonus
        )
    }
}

object RoomDecorationCatalog {
    val ALL_DECORATIONS = listOf(
        // === BEDS & CANOPIES ===
        RoomDecorationItem(
            id = "bed_silk_basic",
            name = "Hedvábná postel s mechem",
            slot = RoomDecorationSlot.BED,
            level = 1,
            icon = "🛏️",
            description = "Pohodlné lůžko vystlané měkkým mechem a nočním hedvábím.",
            comfortPoints = 15,
            goldCost = 100,
            darkEnergyCost = 10,
            materialCosts = mapOf("drevohorec" to 10),
            hpBonus = 15,
            moraleBonusPerDay = 3
        ),
        RoomDecorationItem(
            id = "bed_velvet_canopy",
            name = "Sametová nebesa s runami",
            slot = RoomDecorationSlot.BED,
            level = 2,
            icon = "🛋️",
            description = "Těžký sametový baldachýn protkaný ochrannými runami pána.",
            comfortPoints = 35,
            goldCost = 250,
            darkEnergyCost = 25,
            materialCosts = mapOf("drevohorec" to 20, "temny_strep" to 4),
            hpBonus = 35,
            defenseBonus = 5,
            moraleBonusPerDay = 6,
            affinityMultiplier = 0.10f
        ),
        RoomDecorationItem(
            id = "bed_empress_gold",
            name = "Zlaté lože Císařovny",
            slot = RoomDecorationSlot.BED,
            level = 3,
            icon = "👑",
            description = "Královské zlaté lože s matrací z labutího peří a purpurovými záclonami.",
            comfortPoints = 75,
            goldCost = 600,
            darkEnergyCost = 60,
            materialCosts = mapOf("drevohorec" to 35, "temny_strep" to 10, "mana_esence" to 10),
            hpBonus = 70,
            defenseBonus = 12,
            attackBonus = 10,
            moraleBonusPerDay = 12,
            affinityMultiplier = 0.20f
        ),

        // === FURNITURE ===
        RoomDecorationItem(
            id = "furn_carved_chair",
            name = "Vyřezávané mramorové křeslo",
            slot = RoomDecorationSlot.FURNITURE,
            level = 1,
            icon = "🪑",
            description = "Elegantní kreslo z černého mramoru polstrované temným sametem.",
            comfortPoints = 12,
            goldCost = 80,
            darkEnergyCost = 5,
            materialCosts = mapOf("zelezna_ruda" to 8),
            defenseBonus = 5,
            moraleBonusPerDay = 2
        ),
        RoomDecorationItem(
            id = "furn_vanity_table",
            name = "Zlacený toaletní stolek",
            slot = RoomDecorationSlot.FURNITURE,
            level = 2,
            icon = "🪞",
            description = "Toaletní stolek se skleněnými flakonky na parfémy a líčidla.",
            comfortPoints = 30,
            goldCost = 200,
            darkEnergyCost = 20,
            materialCosts = mapOf("drevohorec" to 15, "mana_esence" to 5),
            attackBonus = 8,
            moraleBonusPerDay = 5,
            affinityMultiplier = 0.10f
        ),
        RoomDecorationItem(
            id = "furn_mahogany_chest",
            name = "Truhla z mahagonu a dračích kostí",
            slot = RoomDecorationSlot.FURNITURE,
            level = 3,
            icon = "🧰",
            description = "Masivní truhla na uložení šperků, šatů a osobních tajností dívky.",
            comfortPoints = 60,
            goldCost = 450,
            darkEnergyCost = 45,
            materialCosts = mapOf("drevohorec" to 30, "temny_strep" to 8, "draci_krev" to 2),
            attackBonus = 18,
            defenseBonus = 10,
            moraleBonusPerDay = 10
        ),

        // === LIGHTING & AMBIENCE ===
        RoomDecorationItem(
            id = "light_ruby_candles",
            name = "Magické rubínové svíce",
            slot = RoomDecorationSlot.LIGHTING,
            level = 1,
            icon = "🕯️",
            description = "Svíce vyzařující hřejivé rubínové světlo a podmanivou vůni.",
            comfortPoints = 10,
            goldCost = 60,
            darkEnergyCost = 10,
            materialCosts = mapOf("mana_esence" to 4),
            attackBonus = 4,
            moraleBonusPerDay = 2
        ),
        RoomDecorationItem(
            id = "light_crystal_chandelier",
            name = "Stínový křišťálový lustr",
            slot = RoomDecorationSlot.LIGHTING,
            level = 2,
            icon = "💡",
            description = "Nádherný lustr z černého křišťálu vrhající po stěnách tajuplné stíny.",
            comfortPoints = 28,
            goldCost = 220,
            darkEnergyCost = 25,
            materialCosts = mapOf("krystal" to 6, "temny_strep" to 5),
            attackBonus = 12,
            defenseBonus = 4,
            moraleBonusPerDay = 5
        ),

        // === WALL DECOR & RUGS ===
        RoomDecorationItem(
            id = "decor_persian_rug",
            name = "Perský sametový koberec",
            slot = RoomDecorationSlot.WALL_DECOR,
            level = 1,
            icon = "🧶",
            description = "Hustý rukodělný koberec tlumící kroky a hřející bosé nožky.",
            comfortPoints = 12,
            goldCost = 90,
            darkEnergyCost = 5,
            hpBonus = 10,
            moraleBonusPerDay = 2
        ),
        RoomDecorationItem(
            id = "decor_dark_tapestry",
            name = "Temná nástěnná tapiserie",
            slot = RoomDecorationSlot.WALL_DECOR,
            level = 2,
            icon = "🖼️",
            description = "Tapiserie zobrazující pád Starého řádu a triumf tvého rodu.",
            comfortPoints = 32,
            goldCost = 240,
            darkEnergyCost = 20,
            materialCosts = mapOf("temny_strep" to 6),
            attackBonus = 10,
            defenseBonus = 8,
            moraleBonusPerDay = 5
        ),

        // === AROMATICS & FLORA ===
        RoomDecorationItem(
            id = "flora_midnight_orchids",
            name = "Půlnoční afrodiziakální orchideje",
            slot = RoomDecorationSlot.AROMATICS,
            level = 1,
            icon = "🌸",
            description = "Kvetoucí orchideje uvolňující do vzduchu vášeň a touhu.",
            comfortPoints = 15,
            goldCost = 110,
            darkEnergyCost = 15,
            materialCosts = mapOf("mana_esence" to 5),
            moraleBonusPerDay = 4,
            affinityMultiplier = 0.08f
        ),
        RoomDecorationItem(
            id = "flora_censer_dragon",
            name = "Aromatická kadidelnice s drakem",
            slot = RoomDecorationSlot.AROMATICS,
            level = 2,
            icon = "🏺",
            description = "Mosazná kadidelnice dýmající omamné vonné živice ze svatyně.",
            comfortPoints = 38,
            goldCost = 280,
            darkEnergyCost = 30,
            materialCosts = mapOf("draci_krev" to 2, "mesicni_prach" to 3),
            attackBonus = 12,
            hpBonus = 25,
            moraleBonusPerDay = 7,
            affinityMultiplier = 0.15f
        )
    )

    fun DEFAULT_INITIAL_ROOMS(): List<HaremRoom> {
        return listOf(
            HaremRoom("room_1", "Královské apartmá stínů", "👑", themeName = "Královský luxus"),
            HaremRoom("room_2", "Hedvábný boudoir touhy", "🌹", themeName = "Sedmý závoj"),
            HaremRoom("room_3", "Mystická věž nebes", "🔮", themeName = "Astraální svatyně"),
            HaremRoom("room_4", "Sametová komnata rozkoše", "🛋️", themeName = "Noční samet"),
            HaremRoom("room_5", "Obsidiánová krypta vášeň", "🖤", themeName = "Gotické sídlo"),
            HaremRoom("room_6", "Zlatý palác nadvlády", "⚜️", themeName = "Císařský trůn")
        )
    }
}
