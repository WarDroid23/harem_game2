package com.example.haremdark.models

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * Metric breakdown of combat efficiency.
 */
@Serializable
data class EfficiencyMetric(
    val category: String, // "Rychlost", "Přežití", "Technika", "Taktika", "Hazard"
    val label: String,
    val valueDisplay: String,
    val scorePoints: Int,
    val grade: String, // "S+", "S", "A", "B", "C"
    val icon: String
)

/**
 * Combat efficiency score and ranking based on combat performance.
 */
@Serializable
data class CombatEfficiencyScore(
    val roundsTaken: Int,
    val hpPercentageRemaining: Float, // 0.0f - 1.0f
    val combosExecuted: Int,
    val statusEffectsInflicted: Int,
    val flawlessVictory: Boolean,
    val hazardDamageAvoided: Boolean = true,
    val baseScore: Int = 500,
    val speedScore: Int = 0,
    val survivalScore: Int = 0,
    val techniqueScore: Int = 0,
    val tacticalScore: Int = 0,
    val totalScore: Int = 1000,
    val efficiencyPercent: Int = 85,
    val rank: String = "S", // "S+", "S", "A", "B", "C"
    val rankTitle: String = "Dominantní triumf",
    val lootRollMultiplier: Float = 1.0f,
    val bonusDropRolls: Int = 2,
    val breakdown: List<EfficiencyMetric> = emptyList()
)

/**
 * Specialized crafting resource used in alchemy, weapon enhancement, and rituals.
 */
@Serializable
data class CraftingResource(
    val id: String,
    val name: String,
    val icon: String,
    val rarity: String, // "Běžný", "Vzácný", "Epický", "Legendární"
    val count: Int = 1,
    val description: String,
    val goldValue: Int = 15,
    val source: String = "Bojová kořist"
)

/**
 * Fragment of rare equipment that can be assembled/forged into complete gear.
 */
@Serializable
data class EquipmentFragment(
    val id: String,
    val targetEquipmentId: String,
    val targetEquipmentName: String,
    val targetEquipmentIcon: String,
    val targetSlot: String, // "weapon", "armor", "accessory"
    val rarity: String, // "Vzácný", "Epický", "Legendární"
    val currentCount: Int = 1,
    val requiredCount: Int = 5,
    val description: String,
    val statPreview: String,
    val forgeResultItem: InventoryItem
) {
    val isReadyToForge: Boolean
        get() = currentCount >= requiredCount

    val progressPercent: Float
        get() = (currentCount.toFloat() / requiredCount.toFloat()).coerceIn(0f, 1f)
}

/**
 * Complete loot distribution bundle awarded after winning a battle.
 */
@Serializable
data class LootDistributionResult(
    val efficiencyScore: CombatEfficiencyScore,
    val goldRewarded: Int,
    val bloodRubiesRewarded: Int,
    val playerXpRewarded: Int,
    val fragmentsRewarded: List<EquipmentFragment> = emptyList(),
    val resourcesRewarded: List<CraftingResource> = emptyList(),
    val bonusItemsRewarded: List<InventoryItem> = emptyList(),
    val bonusRollCount: Int = 2,
    val jackpotRerollTriggered: Boolean = false,
    val mvpName: String? = null,
    val mvpCharacterId: String? = null,
    val mvpBonusXp: Int = 0,
    val companionXpGains: Map<String, Int> = emptyMap()
)

/**
 * Catalog and helper methods for post-battle loot distribution.
 */
object LootDistributionCatalog {

    val ALL_CRAFTING_RESOURCES = listOf(
        CraftingResource(
            id = "dark_crystal",
            name = "Krystal temnoty",
            icon = "💎",
            rarity = "Epický",
            count = 1,
            description = "Zkrystalizovaná esence temné magie vyzařující mrazivé pulzy moci.",
            goldValue = 60
        ),
        CraftingResource(
            id = "dragon_blood",
            name = "Dračí krev",
            icon = "🩸",
            rarity = "Legendární",
            count = 1,
            description = "Vroucí krev prastarého draka propůjčující zbraním nesmírnou ničivou sílu.",
            goldValue = 120
        ),
        CraftingResource(
            id = "ether_essence",
            name = "Éterická esence",
            icon = "✨",
            rarity = "Vzácný",
            count = 1,
            description = "Průzračná duchovní substance nezbytná pro alchymistickou syntézu.",
            goldValue = 35
        ),
        CraftingResource(
            id = "shadow_thread",
            name = "Stínové vlákno",
            icon = "🕸️",
            rarity = "Vzácný",
            count = 1,
            description = "Vlákno utkané ze stínů podsvětí, ideální pro posvátné i svůdné oděvy.",
            goldValue = 40
        ),
        CraftingResource(
            id = "moon_dust",
            name = "Měsíční prach",
            icon = "🌙",
            rarity = "Vzácný",
            count = 1,
            description = "Třpytivý prach absorbující noční svit a zvyšující mentální soustředění.",
            goldValue = 45
        ),
        CraftingResource(
            id = "silver_ore",
            name = "Mystická stříbrná ruda",
            icon = "🪙",
            rarity = "Běžný",
            count = 1,
            description = "Ušlechtilý kov s vysokou magickou vodivostí pro kovářské práce.",
            goldValue = 20
        ),
        CraftingResource(
            id = "mandrake_root",
            name = "Kořen mandragory",
            icon = "🌿",
            rarity = "Běžný",
            count = 1,
            description = "Léčivá bylina rostoucí na bojištích podsvětí.",
            goldValue = 18
        ),
        CraftingResource(
            id = "blood_ruby_dust",
            name = "Prach krvavého rubínu",
            icon = "🔴",
            rarity = "Epický",
            count = 1,
            description = "Jemně mletý rubín používaný k zakletí zbraní a posílení harému.",
            goldValue = 75
        ),
        CraftingResource(
            id = "void_core",
            name = "Jádro prázdnoty",
            icon = "🌀",
            rarity = "Legendární",
            count = 1,
            description = "Pulzující relikvie z nejhlubších propastí schopná přepsat zákony reality.",
            goldValue = 150
        )
    )

    val ALL_EQUIPMENT_FRAGMENTS = listOf(
        EquipmentFragment(
            id = "frag_shadow_dagger",
            targetEquipmentId = "eq_shadow_dagger",
            targetEquipmentName = "Stínová dýka vraha",
            targetEquipmentIcon = "🗡️",
            targetSlot = "weapon",
            rarity = "Vzácný",
            currentCount = 0,
            requiredCount = 4,
            description = "Úlomek čepele ukuté v hlubinách podsvětí.",
            statPreview = "+18 Útok • +12% Šance na Krit • +5 Temná energie",
            forgeResultItem = InventoryItem(
                id = "eq_shadow_dagger",
                name = "Stínová dýka vraha",
                description = "Kompletní smrtící dýka ukovaná z fragmentů. Každý zásah má šanci způsobit krvácení.",
                count = 1,
                price = 320,
                category = "equipment",
                icon = "🗡️",
                rarity = "Vzácný",
                effectDescription = "+18 Boj, +12% Krit, +5 Temná síla",
                equipSlot = "weapon",
                combatBonus = 18,
                source = "Kovárna úlomků"
            )
        ),
        EquipmentFragment(
            id = "frag_blood_armor",
            targetEquipmentId = "eq_blood_armor",
            targetEquipmentName = "Plát krvavého rytíře",
            targetEquipmentIcon = "🛡️",
            targetSlot = "armor",
            rarity = "Epický",
            currentCount = 0,
            requiredCount = 5,
            description = "Úlomek plátového brnění napuštěného krví démonických válečníků.",
            statPreview = "+16 Obrana • +45 Max HP • +10% Odolnost",
            forgeResultItem = InventoryItem(
                id = "eq_blood_armor",
                name = "Plát krvavého rytíře",
                description = "Těžké plátové brnění chránící nositelku před smrtícími ranami.",
                count = 1,
                price = 450,
                category = "equipment",
                icon = "🛡️",
                rarity = "Epický",
                effectDescription = "+16 Obrana, +45 Max HP",
                equipSlot = "armor",
                defenseBonus = 16,
                hpBonus = 45,
                source = "Kovárna úlomků"
            )
        ),
        EquipmentFragment(
            id = "frag_succubus_whip",
            targetEquipmentId = "eq_succubus_whip",
            targetEquipmentName = "Bič rozkoše a bolesti",
            targetEquipmentIcon = "⛓️",
            targetSlot = "weapon",
            rarity = "Epický",
            currentCount = 0,
            requiredCount = 5,
            description = "Úlomek ostnatého biče vládkyně nočních komnat.",
            statPreview = "+22 Útok • +15% Poslušnost dívek • +10 Touha",
            forgeResultItem = InventoryItem(
                id = "eq_succubus_whip",
                name = "Bič rozkoše a bolesti",
                description = "Legendární bič posilující autoritu pána a zvyšující poškození i poslušnost.",
                count = 1,
                price = 500,
                category = "equipment",
                icon = "⛓️",
                rarity = "Epický",
                effectDescription = "+22 Boj, +15 Poslušnost, +10 Touha",
                equipSlot = "weapon",
                combatBonus = 22,
                source = "Kovárna úlomků"
            )
        ),
        EquipmentFragment(
            id = "frag_dragon_amulet",
            targetEquipmentId = "eq_dragon_amulet",
            targetEquipmentName = "Amulet dračího srdce",
            targetEquipmentIcon = "🧿",
            targetSlot = "accessory",
            rarity = "Legendární",
            currentCount = 0,
            requiredCount = 6,
            description = "Zářící úlomek dračího rubínu vyzařující nesmrtelný žár.",
            statPreview = "+14 Boj • +10 Obrana • +35 HP • +20% Ohnivé DMG",
            forgeResultItem = InventoryItem(
                id = "eq_dragon_amulet",
                name = "Amulet dračího srdce",
                description = "Posvátný amulet pulzující dračím plamenem, který propůjčuje nositeli nevídanou sílu.",
                count = 1,
                price = 750,
                category = "equipment",
                icon = "🧿",
                rarity = "Legendární",
                effectDescription = "+14 Boj, +10 Obrana, +35 HP",
                equipSlot = "accessory",
                combatBonus = 14,
                defenseBonus = 10,
                hpBonus = 35,
                source = "Kovárna úlomků"
            )
        ),
        EquipmentFragment(
            id = "frag_astral_corset",
            targetEquipmentId = "eq_astral_corset",
            targetEquipmentName = "Hedvábný korzet nočních hvězd",
            targetEquipmentIcon = "🎀",
            targetSlot = "armor",
            rarity = "Epický",
            currentCount = 0,
            requiredCount = 4,
            description = "Úlomek hvězdného hedvábí utkaného z půlnočních mlh.",
            statPreview = "+12 Obrana • +25 Max HP • +30 Náklonnost nositelky",
            forgeResultItem = InventoryItem(
                id = "eq_astral_corset",
                name = "Hedvábný korzet nočních hvězd",
                description = "Svůdný korzet poskytující magickou ochranu a posilující intimní pouto se společnicí.",
                count = 1,
                price = 420,
                category = "equipment",
                icon = "🎀",
                rarity = "Epický",
                effectDescription = "+12 Obrana, +25 HP, +30 Náklonnost",
                equipSlot = "armor",
                defenseBonus = 12,
                hpBonus = 25,
                source = "Kovárna úlomků"
            )
        ),
        EquipmentFragment(
            id = "frag_void_grimoire",
            targetEquipmentId = "eq_void_grimoire",
            targetEquipmentName = "Grimoár temného rituálu",
            targetEquipmentIcon = "📖",
            targetSlot = "accessory",
            rarity = "Legendární",
            currentCount = 0,
            requiredCount = 6,
            description = "Starodávný list potažený černou kůží se zapovězenými kouzly.",
            statPreview = "+18 Magický útok • +50 Mana • +15% Účinek komb",
            forgeResultItem = InventoryItem(
                id = "eq_void_grimoire",
                name = "Grimoár temného rituálu",
                description = "Prastará kniha zvyšující magické i harémové schopnosti družiny.",
                count = 1,
                price = 800,
                category = "equipment",
                icon = "📖",
                rarity = "Legendární",
                effectDescription = "+18 Boj, +50 Mana, +15% Kombo síla",
                equipSlot = "accessory",
                combatBonus = 18,
                hpBonus = 20,
                source = "Kovárna úlomků"
            )
        )
    )

    /**
     * Compute comprehensive combat efficiency score.
     */
    fun calculateEfficiencyScore(
        roundsTaken: Int,
        alivePartyCount: Int,
        totalPartyCount: Int,
        combosExecuted: Int,
        statusEffectsInflicted: Int,
        isBoss: Boolean,
        hazardSurvived: Boolean = true
    ): CombatEfficiencyScore {
        val totalParty = totalPartyCount.coerceAtLeast(1)
        val hpPercent = (alivePartyCount.toFloat() / totalParty.toFloat()).coerceIn(0f, 1f)
        val flawless = (alivePartyCount == totalParty)

        // 1. Speed score (Faster is better)
        val speedPts = ((12 - roundsTaken) * 45).coerceIn(0, 450)

        // 2. Survival score (More allies alive = higher score)
        val survivalPts = (hpPercent * 350).toInt() + (if (flawless) 150 else 0)

        // 3. Technique score (Combos and chains)
        val techPts = (combosExecuted * 60).coerceAtMost(240)

        // 4. Tactical score (Status effects applied & boss mastery)
        val tacticPts = (statusEffectsInflicted * 30).coerceAtMost(180) + (if (isBoss) 150 else 0)

        val totalPts = 400 + speedPts + survivalPts + techPts + tacticPts

        val (rank, rankTitle, multiplier, bonusRolls) = when {
            totalPts >= 1250 -> Tuple4("S+", "Absolutní dominance", 1.8f, 5)
            totalPts >= 1050 -> Tuple4("S", "Mistrovský triumf", 1.5f, 4)
            totalPts >= 850 -> Tuple4("A", "Slavné vítězství", 1.25f, 3)
            totalPts >= 650 -> Tuple4("B", "Taktické vítězství", 1.1f, 2)
            else -> Tuple4("C", "Vyčerpávající boj", 1.0f, 1)
        }

        val efficiencyPercent = ((totalPts.toFloat() / 1400f) * 100).toInt().coerceIn(40, 100)

        val breakdown = listOf(
            EfficiencyMetric(
                category = "Rychlost",
                label = "Délka střetu: $roundsTaken kol",
                valueDisplay = "+$speedPts bodů",
                scorePoints = speedPts,
                grade = if (roundsTaken <= 3) "S+" else if (roundsTaken <= 5) "A" else "B",
                icon = "⚡"
            ),
            EfficiencyMetric(
                category = "Přežití",
                label = "Přeživší spojenci: $alivePartyCount / $totalParty (${(hpPercent * 100).toInt()}%)",
                valueDisplay = "+$survivalPts bodů",
                scorePoints = survivalPts,
                grade = if (flawless) "S+" else if (hpPercent >= 0.75f) "A" else "B",
                icon = "🛡️"
            ),
            EfficiencyMetric(
                category = "Technika",
                label = "Provedená komba: $combosExecuted",
                valueDisplay = "+$techPts bodů",
                scorePoints = techPts,
                grade = if (combosExecuted >= 2) "S" else if (combosExecuted == 1) "A" else "B",
                icon = "✨"
            ),
            EfficiencyMetric(
                category = "Taktika",
                label = "Aplikované statusy & Taktika",
                valueDisplay = "+$tacticPts bodů",
                scorePoints = tacticPts,
                grade = if (isBoss) "S+" else "A",
                icon = "🎯"
            )
        )

        return CombatEfficiencyScore(
            roundsTaken = roundsTaken,
            hpPercentageRemaining = hpPercent,
            combosExecuted = combosExecuted,
            statusEffectsInflicted = statusEffectsInflicted,
            flawlessVictory = flawless,
            hazardDamageAvoided = hazardSurvived,
            baseScore = 400,
            speedScore = speedPts,
            survivalScore = survivalPts,
            techniqueScore = techPts,
            tacticalScore = tacticPts,
            totalScore = totalPts,
            efficiencyPercent = efficiencyPercent,
            rank = rank,
            rankTitle = rankTitle,
            lootRollMultiplier = multiplier,
            bonusDropRolls = bonusRolls,
            breakdown = breakdown
        )
    }

    /**
     * Generate randomized equipment fragments and crafting resources based on efficiency score.
     */
    fun rollPostBattleLoot(
        efficiency: CombatEfficiencyScore,
        baseGold: Int,
        baseXp: Int,
        isBoss: Boolean,
        mvpName: String? = null,
        mvpId: String? = null,
        companionIds: List<String> = emptyList()
    ): LootDistributionResult {
        val multiplier = efficiency.lootRollMultiplier
        val finalGold = (baseGold * multiplier).toInt().coerceAtLeast(30)
        val finalXp = (baseXp * multiplier).toInt().coerceAtLeast(20)
        val bloodRubies = if (isBoss) (16 * multiplier).toInt() else (6 * multiplier).toInt()

        val fragmentsAwarded = mutableListOf<EquipmentFragment>()
        val resourcesAwarded = mutableListOf<CraftingResource>()
        val bonusItems = mutableListOf<InventoryItem>()

        val numRolls = efficiency.bonusDropRolls + (if (isBoss) 2 else 0)

        // Roll for Equipment Fragments
        val fragmentCountToRoll = when (efficiency.rank) {
            "S+" -> Random.nextInt(2, 4)
            "S" -> Random.nextInt(1, 3)
            "A" -> Random.nextInt(1, 2)
            else -> if (Random.nextInt(100) < 55) 1 else 0
        }

        val availableFrags = ALL_EQUIPMENT_FRAGMENTS.shuffled()
        repeat(fragmentCountToRoll) { idx ->
            val template = availableFrags[idx % availableFrags.size]
            val droppedQty = if (efficiency.rank == "S+" && Random.nextBoolean()) 2 else 1
            fragmentsAwarded.add(template.copy(currentCount = droppedQty))
        }

        // Roll for Crafting Resources
        val resourceCountToRoll = (numRolls - fragmentCountToRoll).coerceAtLeast(2)
        val availableResources = ALL_CRAFTING_RESOURCES.shuffled()
        repeat(resourceCountToRoll) { idx ->
            val resTemplate = availableResources[idx % availableResources.size]
            val qty = when (resTemplate.rarity) {
                "Legendární" -> 1
                "Epický" -> Random.nextInt(1, 3)
                "Vzácný" -> Random.nextInt(2, 4)
                else -> Random.nextInt(2, 5)
            }
            resourcesAwarded.add(resTemplate.copy(count = qty))
        }

        // Special Jackpot reroll for S+ ranking
        val jackpotTriggered = efficiency.rank == "S+" && Random.nextInt(100) < 40
        if (jackpotTriggered) {
            val jackpotResource = ALL_CRAFTING_RESOURCES.first { it.rarity == "Legendární" }
            resourcesAwarded.add(jackpotResource.copy(count = 2))
        }

        // Companion XP distribution
        val compXpMap = mutableMapOf<String, Int>()
        val baseCompXp = (finalXp * 0.8f).toInt().coerceAtLeast(20)
        companionIds.forEach { id ->
            val isMvp = (id == mvpId)
            compXpMap[id] = if (isMvp) (baseCompXp * 1.5f).toInt() else baseCompXp
        }

        return LootDistributionResult(
            efficiencyScore = efficiency,
            goldRewarded = finalGold,
            bloodRubiesRewarded = bloodRubies,
            playerXpRewarded = finalXp,
            fragmentsRewarded = fragmentsAwarded,
            resourcesRewarded = resourcesAwarded,
            bonusItemsRewarded = bonusItems,
            bonusRollCount = numRolls,
            jackpotRerollTriggered = jackpotTriggered,
            mvpName = mvpName,
            mvpCharacterId = mvpId,
            mvpBonusXp = if (mvpId != null) (baseCompXp * 0.5f).toInt() else 0,
            companionXpGains = compXpMap
        )
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
