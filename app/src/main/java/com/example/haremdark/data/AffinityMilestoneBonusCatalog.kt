package com.example.haremdark.data

import com.example.haremdark.models.Character

/**
 * Model representing both passive combat bonuses and passive resource generation
 * unlocked upon reaching a specific affinity milestone.
 */
data class AffinityMilestoneBonus(
    val milestoneThreshold: Int, // 30, 70, 120, 180, 250, 350
    val tierLevel: Int, // 1 to 6
    val title: String,
    val badgeIcon: String,
    val subtitle: String,
    // Passive Combat Stats
    val hpBonus: Int,
    val attackBonusPercent: Float,
    val defenseBonus: Int,
    val critBonusPercent: Int,
    val hpRegenPerTurn: Int = 0,
    val darkEnergyPerTurn: Int = 0,
    val combatPerkTag: String = "",
    val combatPerkSummary: String = "",
    // Passive Resource Generation Stats (produced per day)
    val dailyGold: Int = 0,
    val dailyMana: Int = 0,
    val dailyManaEssence: Int = 0,
    val dailyWood: Int = 0,
    val dailyStone: Int = 0,
    val dailyIron: Int = 0,
    val dailyPopulationGrowth: Int = 0,
    val resourcePerkTag: String = "",
    val resourcePerkSummary: String = ""
) {
    val dailyMaterials: Int get() = dailyWood + dailyStone + dailyIron
}

data class CumulativeAffinityResourceBonuses(
    val totalDailyGold: Int = 0,
    val totalDailyMana: Int = 0,
    val totalDailyManaEssence: Int = 0,
    val totalDailyWood: Int = 0,
    val totalDailyStone: Int = 0,
    val totalDailyIron: Int = 0,
    val totalDailyPopulationGrowth: Int = 0,
    val activePerks: List<String> = emptyList()
) {
    val totalDailyMaterials: Int get() = totalDailyWood + totalDailyStone + totalDailyIron
}

data class HaremAffinityResourceSummary(
    val totalGold: Int,
    val totalMana: Int,
    val totalManaEssence: Int,
    val totalWood: Int,
    val totalStone: Int,
    val totalIron: Int,
    val totalPopulationGrowth: Int,
    val contributorsCount: Int,
    val totalMilestonesUnlocked: Int,
    val breakdownByCharacter: List<CharacterResourceContribution>
)

data class CharacterResourceContribution(
    val characterId: String,
    val characterName: String,
    val archetypeId: String,
    val statusIcon: String,
    val affinityPoints: Int,
    val unlockedMilestonesCount: Int,
    val dailyGold: Int,
    val dailyMana: Int,
    val dailyManaEssence: Int,
    val dailyMaterials: Int,
    val primaryPerkName: String
)

/**
 * Catalog defining the specific combat and resource generation bonuses for every affinity milestone
 * across different archetypes.
 */
object AffinityMilestoneBonusCatalog {

    val MILESTONE_THRESHOLDS = listOf(30, 70, 120, 180, 250, 350)

    /**
     * Returns all 6 milestone bonus configurations tailored for the given archetype.
     */
    fun getMilestonesForArchetype(archetypeId: String): List<AffinityMilestoneBonus> {
        return when (archetypeId) {
            "subka", "ticha_panenka", "ustrasena" -> createMaidDomesticBonuses(archetypeId)
            "sukuba", "posedla", "krvava_subka" -> createOccultDemonicBonuses(archetypeId)
            "draci_divka", "odvazna", "vzdorna" -> createDragonWarriorBonuses(archetypeId)
            "slechticna", "manipulativni", "chladna" -> createNobleTreasuryBonuses(archetypeId)
            "touha", "nymfomanka", "zlomena", "hysterialni" -> createSensualEnchantressBonuses(archetypeId)
            else -> createStandardBonuses(archetypeId)
        }
    }

    fun getBonusForMilestone(archetypeId: String, threshold: Int): AffinityMilestoneBonus {
        val list = getMilestonesForArchetype(archetypeId)
        return list.firstOrNull { it.milestoneThreshold == threshold } ?: list.first()
    }

    fun getUnlockedMilestones(character: Character): List<AffinityMilestoneBonus> {
        val all = getMilestonesForArchetype(character.archetypeId)
        return all.filter { character.affinityPoints >= it.milestoneThreshold }
    }

    fun getCumulativeResourceBonuses(character: Character): CumulativeAffinityResourceBonuses {
        val unlocked = getUnlockedMilestones(character)
        if (unlocked.isEmpty()) {
            return CumulativeAffinityResourceBonuses()
        }

        var gold = 0
        var mana = 0
        var essence = 0
        var wood = 0
        var stone = 0
        var iron = 0
        var pop = 0
        val perks = mutableListOf<String>()

        unlocked.forEach { m ->
            gold += m.dailyGold
            mana += m.dailyMana
            essence += m.dailyManaEssence
            wood += m.dailyWood
            stone += m.dailyStone
            iron += m.dailyIron
            pop += m.dailyPopulationGrowth
            if (m.resourcePerkSummary.isNotBlank()) {
                perks.add(m.resourcePerkSummary)
            }
        }

        // Oblíbená / favoritka and partnerka multipliers
        val multiplier = when {
            character.oblibena && (character.partnerka || character.jeManzelkou) -> 1.30f
            character.oblibena || character.partnerka || character.jeManzelkou -> 1.15f
            else -> 1.0f
        }

        return CumulativeAffinityResourceBonuses(
            totalDailyGold = (gold * multiplier).toInt(),
            totalDailyMana = (mana * multiplier).toInt(),
            totalDailyManaEssence = (essence * multiplier).toInt(),
            totalDailyWood = (wood * multiplier).toInt(),
            totalDailyStone = (stone * multiplier).toInt(),
            totalDailyIron = (iron * multiplier).toInt(),
            totalDailyPopulationGrowth = pop,
            activePerks = perks
        )
    }

    /**
     * Aggregates total passive resource generation across all harem characters based on unlocked affinity milestones.
     */
    fun calculateTotalHaremYield(characters: List<Character>): HaremAffinityResourceSummary {
        var totalGold = 0
        var totalMana = 0
        var totalEssence = 0
        var totalWood = 0
        var totalStone = 0
        var totalIron = 0
        var totalPop = 0
        var contributors = 0
        var totalMilestones = 0
        val breakdown = mutableListOf<CharacterResourceContribution>()

        characters.forEach { char ->
            val unlocked = getUnlockedMilestones(char)
            if (unlocked.isNotEmpty()) {
                contributors++
                totalMilestones += unlocked.size
                val res = getCumulativeResourceBonuses(char)
                totalGold += res.totalDailyGold
                totalMana += res.totalDailyMana
                totalEssence += res.totalDailyManaEssence
                totalWood += res.totalDailyWood
                totalStone += res.totalDailyStone
                totalIron += res.totalDailyIron
                totalPop += res.totalDailyPopulationGrowth

                val primaryPerk = unlocked.lastOrNull { it.resourcePerkSummary.isNotBlank() }?.resourcePerkSummary
                    ?: "Základní podpora panství"

                breakdown.add(
                    CharacterResourceContribution(
                        characterId = char.id,
                        characterName = char.name,
                        archetypeId = char.archetypeId,
                        statusIcon = char.statusIcon,
                        affinityPoints = char.affinityPoints,
                        unlockedMilestonesCount = unlocked.size,
                        dailyGold = res.totalDailyGold,
                        dailyMana = res.totalDailyMana,
                        dailyManaEssence = res.totalDailyManaEssence,
                        dailyMaterials = res.totalDailyMaterials,
                        primaryPerkName = primaryPerk
                    )
                )
            }
        }

        return HaremAffinityResourceSummary(
            totalGold = totalGold,
            totalMana = totalMana,
            totalManaEssence = totalEssence,
            totalWood = totalWood,
            totalStone = totalStone,
            totalIron = totalIron,
            totalPopulationGrowth = totalPop,
            contributorsCount = contributors,
            totalMilestonesUnlocked = totalMilestones,
            breakdownByCharacter = breakdown.sortedByDescending { it.dailyGold }
        )
    }

    // --- ARCHETYPE SPECIFIC FACTORIES ---

    private fun createMaidDomesticBonuses(archetypeId: String): List<AffinityMilestoneBonus> = listOf(
        AffinityMilestoneBonus(
            milestoneThreshold = 30,
            tierLevel = 1,
            title = "První Pouto",
            badgeIcon = "⛓️",
            subtitle = "Pokorná péče",
            hpBonus = 25,
            attackBonusPercent = 0.04f,
            defenseBonus = 4,
            critBonusPercent = 2,
            dailyGold = 25,
            dailyMana = 2,
            resourcePerkTag = "ROOM_CLEANING",
            resourcePerkSummary = "Čisté komnaty (+25 💰/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 70,
            tierLevel = 2,
            title = "Důvěrnice",
            badgeIcon = "💎",
            subtitle = "Správa komnat",
            hpBonus = 50,
            attackBonusPercent = 0.08f,
            defenseBonus = 8,
            critBonusPercent = 4,
            dailyGold = 50,
            dailyMana = 5,
            dailyWood = 6,
            resourcePerkTag = "ESTATE_MAINTENANCE",
            resourcePerkSummary = "Údržba paláce (+50 💰, +6 🪵/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 120,
            tierLevel = 3,
            title = "Oddaná Milenka",
            badgeIcon = "💖",
            subtitle = "Domácí pohoda",
            hpBonus = 85,
            attackBonusPercent = 0.14f,
            defenseBonus = 12,
            critBonusPercent = 6,
            hpRegenPerTurn = 3,
            combatPerkTag = "MAID_DEVOTION",
            combatPerkSummary = "Oddaná služba (+3 HP regenerace)",
            dailyGold = 95,
            dailyMana = 10,
            dailyManaEssence = 1,
            dailyWood = 10,
            dailyPopulationGrowth = 1,
            resourcePerkTag = "CARETAKER",
            resourcePerkSummary = "Péče o poddané (+1 Populační růst)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 180,
            tierLevel = 4,
            title = "Paní Komnat",
            badgeIcon = "👑",
            subtitle = "Vedení harému",
            hpBonus = 140,
            attackBonusPercent = 0.22f,
            defenseBonus = 18,
            critBonusPercent = 8,
            hpRegenPerTurn = 6,
            combatPerkTag = "DOMESTIC_SHIELD",
            combatPerkSummary = "Ochranný plášť (+6 HP/kolo, +18 Obrana)",
            dailyGold = 160,
            dailyMana = 18,
            dailyManaEssence = 2,
            dailyWood = 16,
            dailyStone = 12,
            resourcePerkTag = "HARMONY_STEWARD",
            resourcePerkSummary = "Vrchní hospodyně (+160 💰, +28 📦/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 250,
            tierLevel = 5,
            title = "Spřízněná Duše",
            badgeIcon = "✨",
            subtitle = "Duševní odevzdání",
            hpBonus = 220,
            attackBonusPercent = 0.32f,
            defenseBonus = 26,
            critBonusPercent = 12,
            hpRegenPerTurn = 10,
            darkEnergyPerTurn = 4,
            combatPerkTag = "SOUL_BOND_CARE",
            combatPerkSummary = "Harmonické pouto (+10 HP/kolo, +4 TE/kolo)",
            dailyGold = 260,
            dailyMana = 30,
            dailyManaEssence = 4,
            dailyWood = 25,
            dailyStone = 20,
            dailyPopulationGrowth = 2,
            resourcePerkTag = "DOMINION_HEARTH",
            resourcePerkSummary = "Srdce dominia (+260 💰, +2 Populační růst)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 350,
            tierLevel = 6,
            title = "Temná Královna",
            badgeIcon = "🌌",
            subtitle = "Císařská vládkyně domu",
            hpBonus = 320,
            attackBonusPercent = 0.45f,
            defenseBonus = 38,
            critBonusPercent = 16,
            hpRegenPerTurn = 18,
            darkEnergyPerTurn = 8,
            combatPerkTag = "QUEEN_ETERNAL_WARD",
            combatPerkSummary = "Věčná stráž (+320 HP, +18 HP/kolo)",
            dailyGold = 450,
            dailyMana = 50,
            dailyManaEssence = 8,
            dailyWood = 40,
            dailyStone = 30,
            dailyPopulationGrowth = 4,
            resourcePerkTag = "SUPREME_PROSPERITY",
            resourcePerkSummary = "Vrcholný blahobyt (+450 💰, +4 Pop/den)"
        )
    )

    private fun createOccultDemonicBonuses(archetypeId: String): List<AffinityMilestoneBonus> = listOf(
        AffinityMilestoneBonus(
            milestoneThreshold = 30,
            tierLevel = 1,
            title = "První Pouto",
            badgeIcon = "⛓️",
            subtitle = "Stínový šepot",
            hpBonus = 18,
            attackBonusPercent = 0.06f,
            defenseBonus = 2,
            critBonusPercent = 4,
            darkEnergyPerTurn = 1,
            dailyGold = 15,
            dailyMana = 10,
            resourcePerkTag = "SHADOW_WELL",
            resourcePerkSummary = "Studnice stínů (+10 🔮/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 70,
            tierLevel = 2,
            title = "Důvěrnice",
            badgeIcon = "💎",
            subtitle = "Karmínová touha",
            hpBonus = 40,
            attackBonusPercent = 0.12f,
            defenseBonus = 5,
            critBonusPercent = 7,
            darkEnergyPerTurn = 2,
            dailyGold = 35,
            dailyMana = 24,
            dailyManaEssence = 1,
            resourcePerkTag = "BLOOD_ESSENCE",
            resourcePerkSummary = "Krvavá esence (+24 🔮, +1 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 120,
            tierLevel = 3,
            title = "Oddaná Milenka",
            badgeIcon = "💖",
            subtitle = "Démonické spojení",
            hpBonus = 75,
            attackBonusPercent = 0.20f,
            defenseBonus = 9,
            critBonusPercent = 11,
            hpRegenPerTurn = 2,
            darkEnergyPerTurn = 4,
            combatPerkTag = "ABYSS_SIPHON",
            combatPerkSummary = "Propastné sání (+4 TE/kolo, +20% Útok)",
            dailyGold = 65,
            dailyMana = 45,
            dailyManaEssence = 2,
            dailyIron = 8,
            resourcePerkTag = "MANA_GENERATOR",
            resourcePerkSummary = "Démonické žřídlo (+45 🔮, +2 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 180,
            tierLevel = 4,
            title = "Paní Komnat",
            badgeIcon = "👑",
            subtitle = "Pekelná vášeň",
            hpBonus = 125,
            attackBonusPercent = 0.30f,
            defenseBonus = 14,
            critBonusPercent = 15,
            hpRegenPerTurn = 4,
            darkEnergyPerTurn = 7,
            combatPerkTag = "HELLFIRE_SOUL",
            combatPerkSummary = "Pekelná duše (+30% Útok, +7 TE/kolo)",
            dailyGold = 120,
            dailyMana = 75,
            dailyManaEssence = 4,
            dailyIron = 15,
            resourcePerkTag = "SOUL_FORGE",
            resourcePerkSummary = "Kovárna duší (+75 🔮, +4 💎, +15 ⛓️/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 250,
            tierLevel = 5,
            title = "Spřízněná Duše",
            badgeIcon = "✨",
            subtitle = "Temné transcendence",
            hpBonus = 195,
            attackBonusPercent = 0.42f,
            defenseBonus = 20,
            critBonusPercent = 20,
            hpRegenPerTurn = 7,
            darkEnergyPerTurn = 10,
            combatPerkTag = "NETHER_APOCALYPSE",
            combatPerkSummary = "Záhrobní apokalypsa (+42% Útok, +10 TE/kolo)",
            dailyGold = 210,
            dailyMana = 120,
            dailyManaEssence = 7,
            dailyIron = 25,
            resourcePerkTag = "VOID_FONT",
            resourcePerkSummary = "Pramen prázdnoty (+120 🔮, +7 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 350,
            tierLevel = 6,
            title = "Temná Královna",
            badgeIcon = "🌌",
            subtitle = "Absolutní vládkyně stínů",
            hpBonus = 290,
            attackBonusPercent = 0.58f,
            defenseBonus = 30,
            critBonusPercent = 26,
            hpRegenPerTurn = 12,
            darkEnergyPerTurn = 15,
            combatPerkTag = "SHADOW_EMPRESS",
            combatPerkSummary = "Královna stínů (+58% Útok, +15 TE/kolo, +290 HP)",
            dailyGold = 350,
            dailyMana = 180,
            dailyManaEssence = 12,
            dailyIron = 40,
            resourcePerkTag = "ETERNAL_ECLIPSE",
            resourcePerkSummary = "Věčné zatmění (+180 🔮, +12 💎/den)"
        )
    )

    private fun createDragonWarriorBonuses(archetypeId: String): List<AffinityMilestoneBonus> = listOf(
        AffinityMilestoneBonus(
            milestoneThreshold = 30,
            tierLevel = 1,
            title = "První Pouto",
            badgeIcon = "⛓️",
            subtitle = "Dračí jiskra",
            hpBonus = 30,
            attackBonusPercent = 0.05f,
            defenseBonus = 5,
            critBonusPercent = 3,
            dailyGold = 20,
            dailyMana = 2,
            dailyIron = 5,
            resourcePerkTag = "IRON_VEIN",
            resourcePerkSummary = "Železná žíla (+5 ⛓️/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 70,
            tierLevel = 2,
            title = "Důvěrnice",
            badgeIcon = "💎",
            subtitle = "Bojová čest",
            hpBonus = 65,
            attackBonusPercent = 0.11f,
            defenseBonus = 10,
            critBonusPercent = 6,
            dailyGold = 45,
            dailyMana = 4,
            dailyIron = 12,
            dailyStone = 10,
            resourcePerkTag = "QUARRY_MASTERY",
            resourcePerkSummary = "Důlní těžba (+12 ⛓️, +10 🪨/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 120,
            tierLevel = 3,
            title = "Oddaná Milenka",
            badgeIcon = "💖",
            subtitle = "Dračí pancíř",
            hpBonus = 110,
            attackBonusPercent = 0.18f,
            defenseBonus = 18,
            critBonusPercent = 9,
            hpRegenPerTurn = 3,
            combatPerkTag = "DRAGON_SCALE",
            combatPerkSummary = "Dračí šupiny (+18 Obrana, +110 HP)",
            dailyGold = 85,
            dailyMana = 8,
            dailyManaEssence = 1,
            dailyIron = 22,
            dailyStone = 18,
            resourcePerkTag = "WEAPON_SMITH",
            resourcePerkSummary = "Zbrojní přísun (+22 ⛓️, +18 🪨/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 180,
            tierLevel = 4,
            title = "Paní Komnat",
            badgeIcon = "👑",
            subtitle = "Dračí zuřivost",
            hpBonus = 175,
            attackBonusPercent = 0.28f,
            defenseBonus = 26,
            critBonusPercent = 14,
            hpRegenPerTurn = 6,
            combatPerkTag = "DRACONIC_FURY",
            combatPerkSummary = "Dračí hněv (+28% Útok, +175 HP)",
            dailyGold = 150,
            dailyMana = 15,
            dailyManaEssence = 2,
            dailyIron = 38,
            dailyStone = 30,
            resourcePerkTag = "DRAGON_VAULT",
            resourcePerkSummary = "Dračí klenotnice (+150 💰, +38 ⛓️/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 250,
            tierLevel = 5,
            title = "Spřízněná Duše",
            badgeIcon = "✨",
            subtitle = "Společník v plamenech",
            hpBonus = 260,
            attackBonusPercent = 0.40f,
            defenseBonus = 36,
            critBonusPercent = 18,
            hpRegenPerTurn = 10,
            combatPerkTag = "INFERNAL_ROAR",
            combatPerkSummary = "Pekelný řev (+40% Útok, +36 Obrana)",
            dailyGold = 240,
            dailyMana = 25,
            dailyManaEssence = 4,
            dailyIron = 60,
            dailyStone = 45,
            resourcePerkTag = "IMPERIAL_ARSENAL",
            resourcePerkSummary = "Císařská zbrojnice (+240 💰, +60 ⛓️/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 350,
            tierLevel = 6,
            title = "Temná Královna",
            badgeIcon = "🌌",
            subtitle = "Dračí císařovna",
            hpBonus = 380,
            attackBonusPercent = 0.55f,
            defenseBonus = 48,
            critBonusPercent = 24,
            hpRegenPerTurn = 16,
            darkEnergyPerTurn = 6,
            combatPerkTag = "WYRM_EMPRESS",
            combatPerkSummary = "Dračí panovnice (+55% Útok, +380 HP, +48 Obrana)",
            dailyGold = 400,
            dailyMana = 40,
            dailyManaEssence = 8,
            dailyIron = 95,
            dailyStone = 70,
            resourcePerkTag = "TITAN_FOUNDRY",
            resourcePerkSummary = "Slévárna titánů (+400 💰, +95 ⛓️, +70 🪨/den)"
        )
    )

    private fun createNobleTreasuryBonuses(archetypeId: String): List<AffinityMilestoneBonus> = listOf(
        AffinityMilestoneBonus(
            milestoneThreshold = 30,
            tierLevel = 1,
            title = "První Pouto",
            badgeIcon = "⛓️",
            subtitle = "Panská správa",
            hpBonus = 20,
            attackBonusPercent = 0.04f,
            defenseBonus = 3,
            critBonusPercent = 3,
            dailyGold = 35,
            dailyMana = 3,
            resourcePerkTag = "ROYAL_COINS",
            resourcePerkSummary = "Panské poplatky (+35 💰/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 70,
            tierLevel = 2,
            title = "Důvěrnice",
            badgeIcon = "💎",
            subtitle = "Obchodní kontakty",
            hpBonus = 45,
            attackBonusPercent = 0.09f,
            defenseBonus = 7,
            critBonusPercent = 5,
            dailyGold = 80,
            dailyMana = 7,
            dailyWood = 8,
            dailyStone = 6,
            resourcePerkTag = "TRADE_ROUTE",
            resourcePerkSummary = "Obchodní stezky (+80 💰, +14 📦/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 120,
            tierLevel = 3,
            title = "Oddaná Milenka",
            badgeIcon = "💖",
            subtitle = "Šlechtický vliv",
            hpBonus = 80,
            attackBonusPercent = 0.16f,
            defenseBonus = 12,
            critBonusPercent = 8,
            hpRegenPerTurn = 2,
            combatPerkTag = "NOBLE_AUTHORITY",
            combatPerkSummary = "Šlechtická autorita (+16% Útok, +12 Obrana)",
            dailyGold = 150,
            dailyMana = 15,
            dailyManaEssence = 2,
            dailyWood = 15,
            dailyStone = 12,
            resourcePerkTag = "TAX_COLLECTOR",
            resourcePerkSummary = "Výběr tributu (+150 💰, +2 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 180,
            tierLevel = 4,
            title = "Paní Komnat",
            badgeIcon = "👑",
            subtitle = "Vznešená pokladnice",
            hpBonus = 135,
            attackBonusPercent = 0.25f,
            defenseBonus = 18,
            critBonusPercent = 12,
            hpRegenPerTurn = 5,
            combatPerkTag = "QUEEN_DECREE",
            combatPerkSummary = "Královský edikt (+25% Útok, +5 HP/kolo)",
            dailyGold = 260,
            dailyMana = 25,
            dailyManaEssence = 4,
            dailyIron = 18,
            resourcePerkTag = "DOMINION_TREASURY",
            resourcePerkSummary = "Pokladnice Dominia (+260 💰, +4 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 250,
            tierLevel = 5,
            title = "Spřízněná Duše",
            badgeIcon = "✨",
            subtitle = "Dynastický monopol",
            hpBonus = 210,
            attackBonusPercent = 0.36f,
            defenseBonus = 26,
            critBonusPercent = 16,
            hpRegenPerTurn = 9,
            darkEnergyPerTurn = 4,
            combatPerkTag = "GOLDEN_DYNASTY",
            combatPerkSummary = "Zlatá dynastie (+36% Útok, +210 HP)",
            dailyGold = 420,
            dailyMana = 40,
            dailyManaEssence = 7,
            dailyIron = 30,
            dailyPopulationGrowth = 3,
            resourcePerkTag = "TRADE_MONOPOLY",
            resourcePerkSummary = "Obchodní monopol (+420 💰, +3 Pop/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 350,
            tierLevel = 6,
            title = "Temná Královna",
            badgeIcon = "🌌",
            subtitle = "Věčná císařská pokladnice",
            hpBonus = 310,
            attackBonusPercent = 0.50f,
            defenseBonus = 38,
            critBonusPercent = 22,
            hpRegenPerTurn = 15,
            darkEnergyPerTurn = 8,
            combatPerkTag = "SOVEREIGN_EMPRESS",
            combatPerkSummary = "Vznešená suverénka (+50% Útok, +310 HP, +15 HP/kolo)",
            dailyGold = 650,
            dailyMana = 65,
            dailyManaEssence = 14,
            dailyIron = 50,
            dailyPopulationGrowth = 5,
            resourcePerkTag = "EMPIRE_TRIBUTE",
            resourcePerkSummary = "Císařský tribut (+650 💰, +14 💎, +5 Pop/den)"
        )
    )

    private fun createSensualEnchantressBonuses(archetypeId: String): List<AffinityMilestoneBonus> = listOf(
        AffinityMilestoneBonus(
            milestoneThreshold = 30,
            tierLevel = 1,
            title = "První Pouto",
            badgeIcon = "⛓️",
            subtitle = "Sladká touha",
            hpBonus = 20,
            attackBonusPercent = 0.05f,
            defenseBonus = 3,
            critBonusPercent = 3,
            dailyGold = 20,
            dailyMana = 6,
            resourcePerkTag = "APHRODISIAC_DEW",
            resourcePerkSummary = "Rosná esence (+20 💰, +6 🔮/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 70,
            tierLevel = 2,
            title = "Důvěrnice",
            badgeIcon = "💎",
            subtitle = "Opilost smyslů",
            hpBonus = 45,
            attackBonusPercent = 0.10f,
            defenseBonus = 6,
            critBonusPercent = 6,
            dailyGold = 45,
            dailyMana = 15,
            dailyManaEssence = 1,
            resourcePerkTag = "PLEASURE_ESSENCE",
            resourcePerkSummary = "Esence rozkoše (+45 💰, +15 🔮, +1 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 120,
            tierLevel = 3,
            title = "Oddaná Milenka",
            badgeIcon = "💖",
            subtitle = "Extatické pouto",
            hpBonus = 80,
            attackBonusPercent = 0.17f,
            defenseBonus = 10,
            critBonusPercent = 9,
            hpRegenPerTurn = 3,
            combatPerkTag = "ECSTATIC_PULSE",
            combatPerkSummary = "Extatický puls (+3 HP/kolo, +17% Útok)",
            dailyGold = 85,
            dailyMana = 30,
            dailyManaEssence = 2,
            dailyWood = 10,
            resourcePerkTag = "ELIXIR_STASH",
            resourcePerkSummary = "Zásoba elixírů (+85 💰, +30 🔮, +2 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 180,
            tierLevel = 4,
            title = "Paní Komnat",
            badgeIcon = "👑",
            subtitle = "Svůdné panství",
            hpBonus = 135,
            attackBonusPercent = 0.27f,
            defenseBonus = 16,
            critBonusPercent = 13,
            hpRegenPerTurn = 6,
            darkEnergyPerTurn = 4,
            combatPerkTag = "SIREN_SEDUCTION",
            combatPerkSummary = "Siréní vábení (+27% Útok, +4 TE/kolo)",
            dailyGold = 160,
            dailyMana = 50,
            dailyManaEssence = 4,
            dailyIron = 15,
            resourcePerkTag = "ALCHEMICAL_LAB",
            resourcePerkSummary = "Alchymistická dílna (+160 💰, +50 🔮, +4 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 250,
            tierLevel = 5,
            title = "Spřízněná Duše",
            badgeIcon = "✨",
            subtitle = "Věčná extáze",
            hpBonus = 215,
            attackBonusPercent = 0.38f,
            defenseBonus = 24,
            critBonusPercent = 18,
            hpRegenPerTurn = 10,
            darkEnergyPerTurn = 7,
            combatPerkTag = "HYPNOTIC_TRANCE",
            combatPerkSummary = "Hypnotický trans (+38% Útok, +10 HP/kolo)",
            dailyGold = 280,
            dailyMana = 85,
            dailyManaEssence = 7,
            dailyPopulationGrowth = 2,
            resourcePerkTag = "NECTAR_FOUNTAIN",
            resourcePerkSummary = "Fontána nektaru (+280 💰, +85 🔮, +7 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 350,
            tierLevel = 6,
            title = "Temná Královna",
            badgeIcon = "🌌",
            subtitle = "Bohyně rozkoše a zmaru",
            hpBonus = 320,
            attackBonusPercent = 0.52f,
            defenseBonus = 36,
            critBonusPercent = 24,
            hpRegenPerTurn = 16,
            darkEnergyPerTurn = 11,
            combatPerkTag = "GODDESS_DELIGHT",
            combatPerkSummary = "Bohyně rozkoše (+52% Útok, +320 HP, +16 HP/kolo)",
            dailyGold = 440,
            dailyMana = 135,
            dailyManaEssence = 12,
            dailyPopulationGrowth = 4,
            resourcePerkTag = "CELESTIAL_APHRODISIA",
            resourcePerkSummary = "Nebeská afrodiziaka (+440 💰, +135 🔮, +12 💎/den)"
        )
    )

    private fun createStandardBonuses(archetypeId: String): List<AffinityMilestoneBonus> = listOf(
        AffinityMilestoneBonus(
            milestoneThreshold = 30,
            tierLevel = 1,
            title = "První Pouto",
            badgeIcon = "⛓️",
            subtitle = "Základní pouto",
            hpBonus = 20,
            attackBonusPercent = 0.05f,
            defenseBonus = 3,
            critBonusPercent = 2,
            dailyGold = 20,
            dailyMana = 3,
            resourcePerkTag = "BASE_AID",
            resourcePerkSummary = "Pomoc v dominia (+20 💰/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 70,
            tierLevel = 2,
            title = "Důvěrnice",
            badgeIcon = "💎",
            subtitle = "Posílená věrnost",
            hpBonus = 45,
            attackBonusPercent = 0.10f,
            defenseBonus = 6,
            critBonusPercent = 5,
            dailyGold = 45,
            dailyMana = 8,
            dailyWood = 5,
            dailyStone = 5,
            resourcePerkTag = "LABOR_SUPPORT",
            resourcePerkSummary = "Podpora prací (+45 💰, +10 📦/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 120,
            tierLevel = 3,
            title = "Oddaná Milenka",
            badgeIcon = "💖",
            subtitle = "Hluboké pouto",
            hpBonus = 80,
            attackBonusPercent = 0.16f,
            defenseBonus = 11,
            critBonusPercent = 8,
            hpRegenPerTurn = 2,
            combatPerkTag = "RESOLUTE_BOND",
            combatPerkSummary = "Pevné pouto (+16% Útok, +2 HP/kolo)",
            dailyGold = 85,
            dailyMana = 16,
            dailyManaEssence = 1,
            dailyIron = 8,
            resourcePerkTag = "RESOURCE_FLOW",
            resourcePerkSummary = "Příliv zásob (+85 💰, +8 ⛓️, +1 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 180,
            tierLevel = 4,
            title = "Paní Komnat",
            badgeIcon = "👑",
            subtitle = "Vznešená ochrana",
            hpBonus = 130,
            attackBonusPercent = 0.26f,
            defenseBonus = 17,
            critBonusPercent = 12,
            hpRegenPerTurn = 5,
            darkEnergyPerTurn = 3,
            combatPerkTag = "CHAMBER_WARDEN",
            combatPerkSummary = "Strážkyně komnat (+26% Útok, +17 Obrana)",
            dailyGold = 150,
            dailyMana = 30,
            dailyManaEssence = 3,
            dailyWood = 15,
            dailyStone = 12,
            dailyIron = 12,
            resourcePerkTag = "MANOR_GROWTH",
            resourcePerkSummary = "Růst panství (+150 💰, +39 📦, +3 💎/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 250,
            tierLevel = 5,
            title = "Spřízněná Duše",
            badgeIcon = "✨",
            subtitle = "Harmonické spojení",
            hpBonus = 210,
            attackBonusPercent = 0.38f,
            defenseBonus = 25,
            critBonusPercent = 16,
            hpRegenPerTurn = 9,
            darkEnergyPerTurn = 6,
            combatPerkTag = "SOUL_RESONANCE",
            combatPerkSummary = "Rezonance duší (+38% Útok, +210 HP)",
            dailyGold = 250,
            dailyMana = 50,
            dailyManaEssence = 6,
            dailyIron = 25,
            dailyPopulationGrowth = 2,
            resourcePerkTag = "DOMINION_HARMONY",
            resourcePerkSummary = "Harmonie dominia (+250 💰, +2 Pop/den)"
        ),
        AffinityMilestoneBonus(
            milestoneThreshold = 350,
            tierLevel = 6,
            title = "Temná Královna",
            badgeIcon = "🌌",
            subtitle = "Císařská vláda",
            hpBonus = 310,
            attackBonusPercent = 0.52f,
            defenseBonus = 36,
            critBonusPercent = 22,
            hpRegenPerTurn = 15,
            darkEnergyPerTurn = 10,
            combatPerkTag = "IMPERIAL_SOVEREIGN",
            combatPerkSummary = "Císařská suverénka (+52% Útok, +310 HP, +15 HP/kolo)",
            dailyGold = 400,
            dailyMana = 80,
            dailyManaEssence = 10,
            dailyIron = 45,
            dailyPopulationGrowth = 4,
            resourcePerkTag = "ETERNAL_REIGN",
            resourcePerkSummary = "Věčná vláda (+400 💰, +10 💎, +4 Pop/den)"
        )
    )
}
