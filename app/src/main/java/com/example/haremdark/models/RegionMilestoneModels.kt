package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class PointOfInterestType(val icon: String, val displayName: String, val colorHex: Long) {
    ALTAR("⛩️", "Oltář temných sil", 0xFF9C27B0),
    RUINS("🏛️", "Prastaré ruiny", 0xFFFFB300),
    TRADER("💎", "Tajemný kupec", 0xFF00E676),
    BLOOD_FOUNTAIN("🩸", "Krvavý pramen", 0xFFE91E63),
    MONSTER_LAIR("💀", "Doupě nestvůr", 0xFFD32F2F),
    SECRET_SANCTUARY("✨", "Skrytá svatyně rozkoše", 0xFFFF4081),
    WATCHTOWER("🗼", "Pozorovací věž dominia", 0xFF29B6F6)
}

@Serializable
data class RegionPointOfInterest(
    val id: String,
    val domainId: String,
    val name: String,
    val type: PointOfInterestType,
    val description: String,
    val relativeX: Float = 0.5f,
    val relativeY: Float = 0.5f,
    val energyCost: Int = 6,
    val darkCost: Int = 0,
    val goldCost: Int = 0,
    val interactionLabel: String = "Prozkoumat svatyni",
    val rewardSummary: String = "+Zkušenosti, Temná esence, Šance na dívku",
    val unlockedByDefault: Boolean = false
)

@Serializable
data class StoryMilestone(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val subtitle: String,
    val synopsis: String,
    val unlocksRegionIds: List<String>,
    val requiredPlayerLevel: Int = 1,
    val requiredQuests: List<String> = emptyList(),
    val requiredDefeatedBosses: List<String> = emptyList(),
    val requiredHaremCount: Int = 0,
    val requiredTerritoriesCaptured: Int = 0,
    val rewardGold: Int = 300,
    val rewardDarkEnergy: Int = 30,
    val rewardSexEnergy: Int = 30,
    val rewardXp: Int = 200,
    val rewardTitle: String? = null,
    val unlockCrestIcon: String = "⚔️"
)

object StoryMilestoneData {
    val MILESTONES = listOf(
        StoryMilestone(
            id = "milestone_chapter_1",
            chapterNumber = 1,
            chapterTitle = "Kapitola I: Stíny Hvozdu a Založení",
            subtitle = "První kroky k vybudování temného impéria",
            synopsis = "Vynořil ses z vyhnanství a ovládl první opuštěnou pevnost v pohraničí Temného hvozdu. Dívky z okolních stezek hledají tvou ochranu nebo podléhají tvé vůli.",
            unlocksRegionIds = listOf("temny_hvozd", "hostinec_u_krvave_panny"),
            requiredPlayerLevel = 1,
            requiredHaremCount = 1,
            rewardGold = 250,
            rewardDarkEnergy = 25,
            rewardSexEnergy = 30,
            rewardXp = 150,
            rewardTitle = "Pán temného hvozdu",
            unlockCrestIcon = "🌲"
        ),
        StoryMilestone(
            id = "milestone_chapter_2",
            chapterNumber = 2,
            chapterTitle = "Kapitola II: Pád podsvětí a chrámové kulty",
            subtitle = "Proniknutí do městských stok a znesvěcených krypt",
            synopsis = "Městské podsvětí skýtá bohatství i divoké ženy. Zkorumpované kulty v ruinách starého chrámu uctívají noční rituály a čekají na nového vůdce.",
            unlocksRegionIds = listOf("ruiny_chramu", "stoky_doupata"),
            requiredPlayerLevel = 2,
            requiredQuests = listOf("quest_1"),
            requiredHaremCount = 2,
            rewardGold = 450,
            rewardDarkEnergy = 40,
            rewardSexEnergy = 40,
            rewardXp = 250,
            rewardTitle = "Vládce podsvětí",
            unlockCrestIcon = "🗝️"
        ),
        StoryMilestone(
            id = "milestone_chapter_3",
            chapterNumber = 3,
            chapterTitle = "Kapitola III: Krvavé moře a pakt žoldnéřek",
            subtitle = "Ovládnutí pobřežních doků a vojenského klanu",
            synopsis = "Měsíční přístav otevírá brány zámořským exotickým kurtizánám a pašerákům. V kamenné pustině leží tábor Černých Růží, hrdých bojovnic, které uznávají pouze sílu a odvahu.",
            unlocksRegionIds = listOf("mesicni_pristav", "tabor_zoldnerek"),
            requiredPlayerLevel = 4,
            requiredQuests = listOf("quest_2"),
            requiredDefeatedBosses = listOf("bandita_ze_stok"),
            requiredHaremCount = 3,
            rewardGold = 750,
            rewardDarkEnergy = 60,
            rewardSexEnergy = 50,
            rewardXp = 400,
            rewardTitle = "Admirál Měsíčního zálivu",
            unlockCrestIcon = "⚓"
        ),
        StoryMilestone(
            id = "milestone_chapter_4",
            chapterNumber = 4,
            chapterTitle = "Kapitola IV: Rozvrat Inkvizice a pád aristokracie",
            subtitle = "Pokoření královského distriktu a vampírských krypt",
            synopsis = "Pýcha šlechty padá. Inkvizitoři Černé pečeti ztrácejí půdu pod nohama, zatímco komnaty vznešených dam a pradávné vampírské katakomby přecházejí pod tvou pevnou nadvládu.",
            unlocksRegionIds = listOf("slechticke_panstvi", "krvave_katakomby"),
            requiredPlayerLevel = 5,
            requiredQuests = listOf("quest_3"),
            requiredDefeatedBosses = listOf("otrokarska_hlidka"),
            requiredHaremCount = 4,
            rewardGold = 1200,
            rewardDarkEnergy = 80,
            rewardSexEnergy = 70,
            rewardXp = 600,
            rewardTitle = "Krotitel aristokracie",
            unlockCrestIcon = "👑"
        ),
        StoryMilestone(
            id = "milestone_chapter_5",
            chapterNumber = 5,
            chapterTitle = "Kapitola V: Plameny propasti a Astrální citadela",
            subtitle = "Démonické výhně a magické sféry vyšší moci",
            synopsis = "Zemská kůra puká a otevírá Trhlinu Behemotha. V oblacích se zhmotňuje Astrální citadela, kde nejmocnější čarodějky studují zakázanou magii dimenzí.",
            unlocksRegionIds = listOf("propast_behemoth", "astralni_citadela"),
            requiredPlayerLevel = 6,
            requiredQuests = listOf("quest_4"),
            requiredDefeatedBosses = listOf("inkvizitor_cerne_peceti"),
            requiredHaremCount = 5,
            rewardGold = 1800,
            rewardDarkEnergy = 120,
            rewardSexEnergy = 90,
            rewardXp = 900,
            rewardTitle = "Arcimág stínů & Démonický pán",
            unlockCrestIcon = "🔥"
        ),
        StoryMilestone(
            id = "milestone_chapter_6",
            chapterNumber = 6,
            chapterTitle = "Kapitola VI: Zakázané údolí věčné rozkoše",
            subtitle = "Mýtické srdce temného světa a absolutní panování",
            synopsis = "Bájné údolí věčného pramene nymf a bohyň se otevírá jen absolutnímu vládci. Zde se sny o neomezeném harému stávají věčnou realitou.",
            unlocksRegionIds = listOf("zakazane_udoli_nymf"),
            requiredPlayerLevel = 7,
            requiredDefeatedBosses = listOf("arcidemon_behemoth"),
            requiredHaremCount = 6,
            rewardGold = 3000,
            rewardDarkEnergy = 200,
            rewardSexEnergy = 150,
            rewardXp = 1500,
            rewardTitle = "Císař temného světa",
            unlockCrestIcon = "⚜️"
        )
    )

    fun getMilestoneById(id: String): StoryMilestone? = MILESTONES.find { it.id == id }

    fun getMilestoneForRegion(regionId: String): StoryMilestone? {
        return MILESTONES.find { it.unlocksRegionIds.contains(regionId) }
    }
}
