package com.example.haremdark.models

import kotlinx.serialization.Serializable
import com.example.haremdark.models.BestiaryEntry
import com.example.haremdark.models.InfluenceLogEntry

@Serializable
enum class CombatStrategy(val displayName: String, val icon: String, val description: String) {
    BALANCED("Vyvážená", "⚖️", "Standardní přístup k boji bez specifických bonusů."),
    AGGRESSIVE("Agresivní", "⚔️", "Zaměřuje se na útok. +20% poškození, ale -15% obrana."),
    DEFENSIVE("Defenzivní", "🛡️", "Zaměřuje se na přežití. +20% obrana, ale -15% poškození."),
    SUPPORT("Podpora", "✨", "Zaměřuje se na léčení a buffy. +25% účinnost léčení, ale -20% poškození.")
}

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
    val reqCharacters: Int = 0,
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
    val category: String = "potion", // "equipment", "gift", "combat", "potion", "consumable", "quest", "artifact", "key", "alchemy"
    val icon: String = "📦",
    val rarity: String = "Běžný", // "Běžný", "Vzácný", "Epický", "Legendární"
    val effectDescription: String = "",
    val equipSlot: String? = null, // "weapon", "armor", "accessory"
    val combatBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val synergyBuffValue: Float = 0f,
    val source: String = "Obchod", // "Průzkum", "Boj", "Alchymie", "Úkol", "Dar", "Obchod"
    var isStored: Boolean = false,
    var isFavorite: Boolean = false
)

@Serializable
data class Building(
    val type: String,
    var level: Int = 0,
    val name: String,
    val description: String,
    val baseCost: Int = 150,
    val baseCostWood: Int = 50,
    val baseCostStone: Int = 20,
    val baseCostIron: Int = 0
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
    val mapY: Float = 0.5f,
    val npcTrader: Boolean = false,
    val npcName: String? = null,
    val storyChapter: Int = 1,
    val requiredMilestoneId: String? = null,
    val requiredMilestoneTitle: String? = null,
    val subjugationRequirement: String? = null,
    val environmentWeather: String = "Mlha stínů",
    val environmentBonus: String = "+10% šance na průzkum",
    val pointsOfInterest: List<RegionPointOfInterest> = emptyList(),
    val loreChronicle: String = ""
)

@Serializable
data class KeyMemory(
    val id: String,
    val title: String,
    val description: String,
    val illustrationUrl: String? = null
)

@Serializable
data class Character(
    val id: String,
    var name: String,
    var age: Int = 19,
    var archetypeId: String = "subka",
    var rarity: Int = 1,
    var hp: Int = 100,
    var maxHp: Int = 100,
    var mana: Int = 50,
    var maxMana: Int = 50,
    var srdce: Int = 70,
    var poslusnost: Int = 30,
    var vlhkost: Int = 50,
    var submisivita: Int = 40,
    var loajalita: Int = 30,
    var strength: Int = 15,
    var nalada: String = "neutrální",
    var statusIcon: String = "😐",
    var morale: Int = 50,
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
    var isPinned: Boolean = false,
    var romanceBody: Int = 0,
    var affinityPoints: Int = 15,
    var affinityLevel: Int = 1,
    var lastInteractionDay: Int = 0,
    var dailyTalksCount: Int = 0,
    var dailyGiftsCount: Int = 0,
    var osudId: String = "",
    var osudKrok: Int = 0,
    var naNajmu: Boolean = false,
    var klient: String? = null,
    var typNajmu: String? = null,
    var najemZbyvaDni: Int = 0,
    var najemPrijemCelkem: Int = 0,
    var inventory: MutableList<InventoryItem> = mutableListOf(),
    var level: Int = 1,
    var xp: Int = 0,
    var skillPoints: Int = 0,
    var skills: MutableMap<String, Int> = mutableMapOf("combat" to 0, "defense" to 0, "production" to 0, "rental" to 0),
    var equippedWeapon: Weapon? = null,
    var equipment: MutableMap<String, InventoryItem?> = mutableMapOf("weapon" to null, "armor" to null, "accessory" to null),
    var affinityHistory: MutableList<AffinityPointRecord> = mutableListOf(),
    var relationshipHistory: MutableList<RelationshipRecord> = mutableListOf(),
    var unlockedPassives: MutableList<String> = mutableListOf(),
    var unlockedCombatSkills: MutableList<String> = mutableListOf(),
    var interactionLogs: MutableList<InteractionLogEntry> = mutableListOf(),
    var moraleHistory: MutableList<MoraleRecord> = mutableListOf(),
    var trainingPresets: MutableList<TrainingPreset> = mutableListOf(),
    var breakthroughActive: Boolean = false,
    var breakthroughType: String? = null,
    var breakthroughExpiryDay: Int = 0,
    var keyMemories: MutableList<KeyMemory> = mutableListOf(),
    var traits: MutableList<String> = mutableListOf(),
    var milestoneRewardsUnlocked: MutableSet<Int> = mutableSetOf(),
    var totalTrainingSessions: Int = 0,
    var completedPresets: MutableSet<String> = mutableSetOf(),
    var dailyAssignment: String? = null,
    var completedBondStories: MutableSet<String> = mutableSetOf(),
    var unlockedVoiceLines: MutableSet<String> = mutableSetOf(),
    var unlockedEmotions: MutableSet<String> = mutableSetOf("BLUSH", "CHEER", "LOVE", "SHY", "SPARKLE"),
    var favorBoostActive: Boolean = false,
    var favorBoostDaysRemaining: Int = 0,
    var preferredCombatRole: CombatStrategy = CombatStrategy.BALANCED,
    var preferredFormation: String = "MID",
    var moodScore: Int = 50, // 0-100 scale
    var dailyInteractionsCount: Int = 0,
    var unlockedSkins: MutableList<String> = mutableListOf("default")
) {
    var loyalty: Int
        get() = loajalita
        set(value) {
            val oldLoyalty = loajalita
            loajalita = value.coerceIn(0, 100)
            checkMilestones(oldLoyalty, loajalita)
        }

    private fun checkMilestones(old: Int, new: Int) {
        val thresholds = listOf(25, 50, 75)
        thresholds.forEach { threshold ->
            if (old < threshold && new >= threshold) {
                if (!milestoneRewardsUnlocked.contains(threshold)) {
                    milestoneRewardsUnlocked.add(threshold)
                    // Logic for unlocking rewards will be handled in GameEngine or UI via a callback
                }
            }
        }
    }

    fun getLoyaltyStatus(): String = when {
        loajalita >= 80 -> "Oddaná fanatička"
        loajalita >= 60 -> "Věrná společnice"
        loajalita >= 40 -> "Poslušná"
        loajalita >= 20 -> "Ostražitá"
        else -> "Vzpurná a zrádná"
    }

    val maxDailyTalks: Int
        get() {
            var max = 2
            if (oblibena) max += 1
            if (partnerka || jeManzelkou) max += 1
            if (affinityLevel >= 5) max += 1
            return max
        }

    val maxDailyGifts: Int
        get() {
            var max = 3
            if (oblibena) max += 1
            if (partnerka || jeManzelkou) max += 1
            if (affinityLevel >= 4) max += 1
            return max
        }

    val dailyTalksRemaining: Int
        get() = (maxDailyTalks - dailyTalksCount).coerceAtLeast(0)

    val dailyGiftsRemaining: Int
        get() = (maxDailyGifts - dailyGiftsCount).coerceAtLeast(0)

    val canTalkToday: Boolean
        get() = dailyTalksRemaining > 0

    val canGiftToday: Boolean
        get() = dailyGiftsRemaining > 0

    val archetype: String get() = archetypeId

    fun getStrengthStatus(): String = when {
        strength >= 80 -> "Legendární šampionka"
        strength >= 60 -> "Mistryně čepele"
        strength >= 40 -> "Zkušená válečnice"
        strength >= 25 -> "Bojeschopná"
        else -> "Křehká kráska"
    }
}

@Serializable
data class RelationshipRecord(
    val day: Int,
    val prompt: String,
    val choice: String,
    val feedback: String,
    val outcomeEffects: String
)

@Serializable
data class InteractionLogEntry(
    val day: Int,
    val type: String, // "výcvik", "rozhovor", "trest", "odměna", "morálka", "dar"
    val title: String,
    val description: String,
    val statChanges: String = "",
    val rank: String? = null
)

@Serializable
data class MoraleRecord(
    val day: Int,
    val morale: Int,
    val source: String = "Denní výcvik"
)

fun Character.getSafeMoraleTrend(currentDay: Int = 1): List<MoraleRecord> {
    if (moraleHistory.isNotEmpty()) {
        val last7 = moraleHistory.sortedBy { it.day }.takeLast(7)
        if (last7.size >= 2) return last7
    }
    val base = morale
    val startDay = (currentDay - 6).coerceAtLeast(1)
    return listOf(
        MoraleRecord(startDay, (base - 18).coerceIn(15, 100), "Příchod do komnat"),
        MoraleRecord(startDay + 1, (base - 14).coerceIn(15, 100), "Základní výcvik"),
        MoraleRecord(startDay + 2, (base - 10).coerceIn(15, 100), "Kázeňský dril"),
        MoraleRecord(startDay + 3, (base - 5).coerceIn(15, 100), "Lázeň & Odměna"),
        MoraleRecord(startDay + 4, (base - 8).coerceIn(15, 100), "Pochvala pána"),
        MoraleRecord(startDay + 5, (base - 2).coerceIn(15, 100), "Rytmický výcvik"),
        MoraleRecord(currentDay.coerceAtLeast(startDay + 6), base, "Aktuální stav morálky")
    )
}

@Serializable
data class TrainingPresetAction(
    val id: String, // "rhythm", "reflex", "praise", "bath", "whip"
    val name: String,
    val energyCost: Int = 10,
    val loyaltyGain: Int = 8,
    val moraleGain: Int = 6,
    val icon: String = "🎯"
)

@Serializable
data class TrainingPreset(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val actions: List<TrainingPresetAction>,
    var partnerId: String? = null
)

fun getDefaultTrainingPresets(): List<TrainingPreset> {
    return listOf(
        TrainingPreset(
            id = "preset_discipline",
            title = "⚡ Blesková Kázeň",
            description = "Intenzivní dril zaměřený na rychlou poslušnost a reflexy.",
            icon = "⚡",
            actions = listOf(
                TrainingPresetAction("reflex", "Reflexní test poslušnosti", 10, 12, 8, "⚡"),
                TrainingPresetAction("rhythm", "Rytmický dril rozkazů", 12, 10, 6, "🎵"),
                TrainingPresetAction("reflex", "Rychlá prověrka poslušnosti", 10, 12, 8, "⚡")
            )
        ),
        TrainingPreset(
            id = "preset_care",
            title = "🌸 Péče & Povzbuzení",
            description = "Harmonické spojení pochvaly a péče pro obnovu morálky a loajality.",
            icon = "🌸",
            actions = listOf(
                TrainingPresetAction("praise", "Veřejná pochvala", 8, 8, 12, "✨"),
                TrainingPresetAction("bath", "Společná lázeň s oleji", 15, 14, 18, "🛁"),
                TrainingPresetAction("rhythm", "Rytmický rozhovor", 10, 10, 10, "🎵")
            )
        ),
        TrainingPreset(
            id = "preset_master_drill",
            title = "🔥 Kompletní Dril Dominy",
            description = "Vyvážený dril kombinující pokárání, reflexní zkoušku i závěrečnou odměnu.",
            icon = "🔥",
            actions = listOf(
                TrainingPresetAction("whip", "Spoutání a pokárání", 12, 10, -5, "⛓️"),
                TrainingPresetAction("reflex", "Reflexní zkouška okamžiku", 10, 14, 8, "⚡"),
                TrainingPresetAction("bath", "Očistná lázeň s odměnou", 15, 12, 15, "🛁")
            )
        )
    )
}

fun Character.getSafeInteractionLogs(currentDay: Int = 1): List<InteractionLogEntry> {
    if (interactionLogs.isNotEmpty()) {
        return interactionLogs.sortedByDescending { it.day }
    }
    val startDay = (currentDay - 2).coerceAtLeast(1)
    return listOf(
        InteractionLogEntry(
            day = startDay,
            type = "příchod",
            title = "Příchod do sídla pána",
            description = "Dívka byla zařazena do komnat a složila první přísahu poslušnosti.",
            statChanges = "+15 Loajalita, +50 Morálka"
        ),
        InteractionLogEntry(
            day = startDay + 1,
            type = "rozhovor",
            title = "První důvěrný rozhovor",
            description = "Osobní rozprava o její minulosti a očekáváních v harému.",
            statChanges = "+10 Důvěra, +8 Loajalita"
        ),
        InteractionLogEntry(
            day = currentDay,
            type = "výcvik",
            title = "Úvodní výcvik poslušnosti",
            description = "Absolvování základního tréninku kázzně a plnění rozkazů.",
            statChanges = "+12 Poslušnost, +10 Morálka",
            rank = "A"
        )
    )
}

@Serializable
data class AffinityPointRecord(
    val day: Int,
    val points: Int,
    val source: String = "Interakce"
)

fun Character.getSafeAffinityTrend(currentDay: Int = 1): List<AffinityPointRecord> {
    if (affinityHistory.size >= 2) {
        return affinityHistory.toList()
    }
    if (affinityHistory.size == 1) {
        val single = affinityHistory.first()
        val startDay = (single.day - 2).coerceAtLeast(1)
        val initialPoints = (single.points * 0.4f).toInt().coerceAtLeast(0)
        val baseline = listOf(
            AffinityPointRecord(startDay, initialPoints, "Počátek pouta"),
            single
        )
        return baseline
    }
    // Provide realistic baseline affinity progression curve up to current points
    val baseline = (affinityPoints * 0.25f).toInt().coerceAtLeast(5)
    val step1 = (affinityPoints * 0.50f).toInt().coerceAtLeast(baseline + 2)
    val step2 = (affinityPoints * 0.78f).toInt().coerceAtLeast(step1 + 2)
    val startDay = (currentDay - 3).coerceAtLeast(1)
    
    val generated = mutableListOf(
        AffinityPointRecord(startDay, baseline, "Příchod do komnat"),
        AffinityPointRecord(startDay + 1, step1, "Pozornost & Dary"),
        AffinityPointRecord(startDay + 2, step2, "Důvěrný rozhovor"),
        AffinityPointRecord(currentDay.coerceAtLeast(startDay + 3), affinityPoints, "Aktuální pouto")
    )
    
    // Save generated baseline to history so it persists
    affinityHistory.clear()
    affinityHistory.addAll(generated)
    
    return generated
}

fun Character.addAffinityHistory(day: Int, source: String = "Interakce") {
    // Avoid adding duplicate records for the same day if points haven't changed much
    val lastRecord = affinityHistory.lastOrNull()
    if (lastRecord != null && lastRecord.day == day && lastRecord.points == affinityPoints) {
        return
    }
    affinityHistory.add(AffinityPointRecord(day, affinityPoints, source))
    // Keep only last 20 records in memory
    if (affinityHistory.size > 20) {
        affinityHistory.removeAt(0)
    }
}


@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val badgeIcon: String,
    val isTitle: Boolean = false
)

object AchievementList {
    val allAchievements = listOf(
        Achievement("ach_battles_100", "Bůh války", "Vyhraj 100 bitev.", "⚔️", true),
        Achievement("ach_max_affinity", "Nejhlubší pouto", "Dosáhni maximální náklonnosti (100) u jedné z dívek.", "💞", true),
        Achievement("ach_harem_10", "Sběratel krásy", "Získej alespoň 10 dívek do svého harému.", "👥", true),
        Achievement("ach_harem_20", "Pán harému", "Shromáždi ohromných 20 dívek ve svém harému.", "👑", true),
        Achievement("ach_affinity_total", "Casanova podsvětí", "Dosáhni celkové náklonnosti (Affinity) 250 napříč harémem.", "💖", true),
        Achievement("ach_boss_slayer", "Ničitel bossů", "Poraz alespoň 3 bosse ve výpravách.", "💀", true),
        Achievement("ach_arena_champion", "Král Arény", "Dostaň tvou dívku na úroveň 10 pomocí arénových bojů.", "⚔️", true),
        Achievement("ach_wealthy", "Midasův dotek", "Našetři alespoň 10 000 zlatých.", "💰", true),
        Achievement("ach_domain_max", "Temný vládce", "Vylepši svou Pevnost na úroveň 5.", "🏰", true),
        Achievement("ach_blood_sister", "Krvavá přísaha", "Získej dívku se vztahem 'Krvavá sestra'.", "🩸", true)
    )
}

@Serializable
data class Player(
    var name: String = "LordRusty23",
    var level: Int = 1,
    var xp: Int = 0,
    var xpNext: Int = 100,
    var prestige: Int = 0,
    var population: Int = 10,
    var maxPopulation: Int = 50,
    var mana: Int = 50,
    var maxMana: Int = 100,
    var hp: Int = 100,
    var maxHp: Int = 100,
    var gold: Int = 500,
    var influence: Int = 35,
    var maxInfluence: Int = 100,
    var manaEssence: Int = 0,
    var wood: Int = 100,
    var stone: Int = 50,
    var iron: Int = 10,
    var sexEnergy: Int = 100,
    var darkEnergy: Int = 50,
    var maxSexEnergy: Int = 100,
    var maxDarkEnergy: Int = 100,
    var dominance: Int = 5,
    var killCount: Int = 0,
    var battlesWon: Int = 0,
    var day: Int = 1,
    var skillPoints: Int = 2,
    var unlockedCodexIds: Set<String> = emptySet(),
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
    var activeTitle: String? = null,
    var unlockedGlobalMilestones: MutableSet<String> = mutableSetOf(),
    var unlockedLoreConnectionIds: MutableSet<String> = mutableSetOf(),
    var unlockedAvatarFrames: MutableSet<String> = mutableSetOf("Bronzový rám rozkoše"),
    var unlockedTitleTags: MutableSet<String> = mutableSetOf("Neznámý vládce"),
    var selectedAvatarFrame: String = "Bronzový rám rozkoše",
    var unlockedAchievements: MutableList<String> = mutableListOf(),
    var domainExpansionLevel: Int = 1,
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
    var storedItems: MutableList<InventoryItem> = mutableListOf(),
    var equipmentFragments: MutableMap<String, Int> = mutableMapOf(),
    var craftingResources: MutableMap<String, Int> = mutableMapOf(),
    var activePartyIds: MutableList<String> = mutableListOf(),
    var partyFormationMap: MutableMap<String, String> = mutableMapOf(),
    var bankGold: Int = 0,
    var toxicity: Int = 0,
    var maxToxicity: Int = 100,
    var haremHarmony: Int = 85,
    var drugsCraftedTotal: Int = 0,
    var drugsSoldTotal: Int = 0,
    var greenhouseHarvestDay: Int = 0,
    var agents: MutableList<Agent> = mutableListOf(
        Agent("Vesper, Noční stín", 1, 75, "vymahač")
    )
)

@Serializable
data class CombatLogEntry(
    val turn: Int,
    val type: String, // "player_attack", "player_spell", "player_heal", "player_defend", "player_support", "enemy_attack", "enemy_special", "system", "victory", "defeat"
    val message: String,
    val actor: String = "",
    val actionName: String = "",
    val damageDealt: Int = 0,
    val damageCalculation: String? = null,
    val narrativeText: String? = null
)

@Serializable
data class CombatSession(
    val boss: Boss,
    var bossHp: Int,
    val bossMaxHp: Int,
    var playerHp: Int,
    val playerMaxHp: Int,
    var deployedCharacterId: String? = null, // ID of the harem character deployed
    var wave: Int = 1, // Waves of enemies
    var maxWaves: Int = 1,
    var turnCount: Int = 1,
    var isDefending: Boolean = false,
    var enemyBleedTurns: Int = 0,
    var enemyStunned: Boolean = false,
    var activeBuff: String? = null,
    val logEntries: List<CombatLogEntry> = emptyList(),
    val log: List<String> = emptyList(),
    var isOver: Boolean = false,
    var victory: Boolean = false,
    var lootGained: String? = null,
    var skillCooldowns: Map<String, Int> = emptyMap(),
    val environmentalHazard: EnvironmentalHazard? = null,
    var hazardCountdown: Int = 2,
    var lastHazardTriggerMessage: String? = null
)

@Serializable
data class DailyMission(
    val id: String,
    val type: String, // e.g., "HUNT", "GIFT", "INTERACT", "EXPLORE", "TREAT", "GREET", "GRANT_FAVOR"
    val description: String,
    val targetCount: Int,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false,
    var isClaimed: Boolean = false,
    val rewardGold: Int = 0,
    val rewardDarkEnergy: Int = 0,
    val rewardSexEnergy: Int = 0,
    val targetCharacterId: String? = null,
    val rewardAffinity: Int = 0,
    val rewardItem: InventoryItem? = null
)

@Serializable
data class PartyBuff(
    val id: String,
    val name: String,
    val description: String,
    var durationDays: Int,
    val type: String, // e.g., "DAMAGE", "DEFENSE", "RESOURCE_BOOST"
    val value: Int
)

@Serializable
data class ActiveDrugBuff(
    val id: String,
    val drugId: String,
    val name: String,
    val icon: String,
    val targetType: String, // "player" or "concubine"
    val targetCharacterId: String? = null,
    val targetCharacterName: String = "Pán",
    var remainingDays: Int = 3,
    val maxDays: Int = 3,
    val effectSummary: String,
    val category: String = "Afrodiziakum",
    var autoRenew: Boolean = true,
    var withdrawalWarning: Boolean = false
)

@Serializable
data class EquipmentLoadout(
    val id: String,
    val name: String,
    val icon: String = "⚔️",
    val description: String = "",
    val situationTag: String = "DPS", // "DPS", "TANK", "DARK_MAGIC", "BLEED", "BALANCED", "CUSTOM"
    val weaponItem: InventoryItem? = null,
    val armorItem: InventoryItem? = null,
    val accessoryItem: InventoryItem? = null,
    val playerWeaponIndex: Int = 0,
    val targetCharacterId: String? = null,
    val isDefaultPreset: Boolean = false
)

@Serializable
data class MapBookmark(
    val id: String,
    val domainId: String,
    val customName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class GameSave(
    val version: String = "22.1-dark",
    val saveDate: String,
    val slotNumber: Int,
    val player: Player,
    val characters: List<Character>,
    val haremLevel: Int = 1,
    val domainExpansionLevel: Int = 1,
    val haremExp: Int = 0,
    val haremMaxExp: Int = 100,
    val buildings: List<Building>,
    val territories: List<MafiaTerritory>,
    val currentDomainId: String = "temny_hvozd",
    val unlockedDomains: List<String> = listOf("temny_hvozd", "ruiny_chramu"),
    val defeatedBosses: List<String> = emptyList(),
    val currentTheme: String = "Temné dominium",
    val isLightMode: Boolean = false,
    val completedQuests: List<String> = emptyList(),
    val completedMilestones: List<String> = listOf("milestone_chapter_1"),
    val regionExplorationProgress: Map<String, Int> = emptyMap(),
    val discoveredLandmarks: List<String> = emptyList(),
    val regionDominionLevel: Map<String, Int> = emptyMap(),
    val dailyMissions: List<DailyMission> = emptyList(),
    val lastMissionUpdateDay: Int = 0,
    val gameLog: List<String> = emptyList(),
    val activeBuffs: List<PartyBuff> = emptyList(),
    val activeDrugBuffs: List<ActiveDrugBuff> = emptyList(),
    val resourceHistory: List<DailyResourceStat> = emptyList(),
    val savedLoadouts: List<EquipmentLoadout> = emptyList(),
    val activeLoadoutId: String? = null,
    val savedPartyFormations: List<PartyFormation> = emptyList(),
    val fogOfWarEnabled: Boolean = true,
    val mapBookmarks: List<MapBookmark> = emptyList(),
    val bestiaryEntries: List<BestiaryEntry> = emptyList(),
    val influenceLog: List<InfluenceLogEntry> = emptyList()
)

@Serializable
data class PartyFormation(
    val id: String,
    val name: String,
    val icon: String,
    val memberIds: List<String>,
    val includePlayer: Boolean = true
)

@Serializable
data class DailyResourceStat(
    val day: Int,
    val goldProduced: Int,
    val manaProduced: Int,
    val manaEssenceProduced: Int,
    val woodProduced: Int,
    val stoneProduced: Int,
    val ironProduced: Int
)


enum class RelStatus(val title: String, val buffType: String, val buffValue: Float, val description: String) {
    BLOOD_SISTER("Krvavá sestra", "COMBAT_DMG", 0.15f, "+15% k poškození v boji (aktivní v aréně)"),
    DEVOTED("Oddaná", "GLOBAL_RES", 0.05f, "+5% ke globální produkci surovin"),
    IN_LOVE("Zamilovaná", "MORALE_RES", 0.10f, "+10% šance na pozitivní noční eventy a rychlé hojení"),
    BROKEN("Zlomená otrokyně", "OBEDIENCE", 0.20f, "+20% zisk zlata ze všech pronájmů, nižší obrana"),
    OBEDIENT("Poslušná", "RESOURCE", 0.02f, "+2% ke globální produkci surovin"),
    REBELLIOUS("Rebelující", "PENALTY", -0.10f, "-10% poškození v boji"),
    NEUTRAL("Neutrální", "NONE", 0f, "Žádný zvláštní efekt")
}

fun Character.getRelationship(): RelStatus {
    if (fazeZkazenosti >= 5 && bloodlust >= 50) return RelStatus.BLOOD_SISTER
    if (loajalita >= 80 && duvera >= 80 && romanceBody >= 50) return RelStatus.DEVOTED
    if (oblibena || (romanceBody >= 30 && loajalita > 50)) return RelStatus.IN_LOVE
    if (strach >= 70 && broken >= 40) return RelStatus.BROKEN
    if (poslusnost >= 60 && strach >= 40) return RelStatus.OBEDIENT
    if (loajalita <= 30 && strach <= 30) return RelStatus.REBELLIOUS
    return RelStatus.NEUTRAL
}
