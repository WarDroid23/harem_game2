package com.example.haremdark.data

import com.example.haremdark.models.*

/**
 * Type of node in the character skill tree.
 */
enum class SkillNodeType(val label: String, val badgeColorHex: Long) {
    ACTIVE_ABILITY("Aktivní schopnost", 0xFFFF4081),
    PASSIVE_PERK("Bojová pasivka", 0xFF00E5FF),
    SYNERGY_MASTERY("Harémová synergie", 0xFFFFD700)
}

data class SkillUpgradeCost(
    val goldCost: Int = 0,
    val darkEnergyCost: Int = 0,
    val xpCost: Int = 0,
    val spCost: Int = 0,
    val darkShards: Int = 0, // temny_strep
    val manaEssence: Int = 0, // mana_esence
    val moonDust: Int = 0, // mesicni_prach
    val dragonBlood: Int = 0, // draci_krev
    val crystals: Int = 0 // krystal
)

/**
 * Definition of an individual skill progression node for harem companions.
 */
data class CharacterSkillNode(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val branchName: String, // "Boj" (Combat), "Magie & Efekty" (Magic/Debuffs), "Obrana & Podpora" (Defense/Support)
    val nodeType: SkillNodeType,
    val tier: Int, // 1, 2, 3, 4
    val xpCost: Int,
    val spCost: Int = 1,
    val reqNodeId: String? = null,
    val reqLevel: Int = 1,
    val reqAffinityLevel: Int = 0,
    val reqLoajalita: Int = 0,
    val x: Float = 0f, // Normalized x (0-100)
    val y: Float = 0f, // Normalized y (0-100)
    val activeSkill: PartyCombatSkill? = null,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val critBonus: Int = 0,
    val speedBonus: Int = 0,
    val lifestealPercent: Int = 0,
    val damageMitigationPercent: Int = 0,
    val bonusComboGain: Int = 0,
    val manaRegenBonus: Int = 0,
    val maxRank: Int = 5,
    val specialEffectText: String? = null
)

object CharacterSkillCatalog {

    /**
     * Universal and Archetype-specific skill nodes available in the game.
     */
    val ALL_SKILL_NODES: List<CharacterSkillNode> = listOf(
        // === WARRIOR / GLADIATOR BRANCH (Odvaha & Čepel) - Left Side ===
        CharacterSkillNode(
            id = "warrior_strike_1",
            name = "Rychlý výpad",
            icon = "⚔️",
            description = "Základní bojový trénink zvyšující razanci úderů.",
            branchName = "Boj",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 1,
            xpCost = 60,
            spCost = 1,
            reqLevel = 1,
            x = 25f,
            y = 20f,
            attackBonus = 5,
            critBonus = 3,
            specialEffectText = "+5 Útok, +3% Kritický zásah"
        ),
        CharacterSkillNode(
            id = "warrior_active_cleave",
            name = "Krvavé rozpolcení",
            icon = "🪓",
            description = "Silný švih zbraní způsobující těžké zranění a krvácení cíle.",
            branchName = "Boj",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 2,
            xpCost = 140,
            spCost = 1,
            reqNodeId = "warrior_strike_1",
            reqLevel = 2,
            reqAffinityLevel = 2,
            x = 15f,
            y = 40f,
            activeSkill = PartyCombatSkill(
                id = "skill_cleave",
                name = "Krvavé rozpolcení",
                icon = "🪓",
                description = "Zasadí 180% poškození a způsobí krvácení na 2 kola.",
                manaCost = 22,
                cooldownTurns = 2,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.PHYSICAL_ATTACK,
                powerMultiplier = 1.8f,
                appliedStatus = CombatStatusEffect("bleed_cleave", "Krvácení", "🩸", "BLEED", value = 14, durationTurns = 2),
                animationType = "SLASH",
                voiceQuote = "Tvoje krev zbarví mou čepel!"
            ),
            attackBonus = 4,
            specialEffectText = "Odemkne aktivní schopnost 'Krvavé rozpolcení'"
        ),
        CharacterSkillNode(
            id = "warrior_passive_vampire",
            name = "Upíří žízeň",
            icon = "🩸",
            description = "Každý zásah obnoví zdraví útočnice podle způsobeného poškození.",
            branchName = "Boj",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 3,
            xpCost = 250,
            spCost = 1,
            reqNodeId = "warrior_active_cleave",
            reqLevel = 3,
            reqLoajalita = 40,
            x = 25f,
            y = 60f,
            lifestealPercent = 15,
            hpBonus = 20,
            specialEffectText = "Vysaje 15% způsobeného poškození zpět jako HP, +20 Max HP"
        ),
        CharacterSkillNode(
            id = "warrior_active_berserk",
            name = "Vřava hněvu",
            icon = "🌪️",
            description = "Bojový vír zasahující všechny nepřátele v bitevní řadě najednou.",
            branchName = "Boj",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 4,
            xpCost = 450,
            spCost = 2,
            reqNodeId = "warrior_passive_vampire",
            reqLevel = 5,
            reqAffinityLevel = 4,
            x = 15f,
            y = 85f,
            activeSkill = PartyCombatSkill(
                id = "skill_berserk_whirlwind",
                name = "Vřava hněvu",
                icon = "🌪️",
                description = "Zasáhne VŠECHNY nepřátele za 150% poškození a sníží jejich obranu.",
                manaCost = 38,
                manaEssenceCost = 5,
                cooldownTurns = 3,
                targetType = SkillTargetType.ALL_ENEMIES,
                category = SkillCategory.PHYSICAL_ATTACK,
                powerMultiplier = 1.5f,
                appliedStatus = CombatStatusEffect("sunder_armor", "Rozdrcená zbroj", "🛡️", "DEF_BUFF", value = -6, durationTurns = 2),
                animationType = "HEAVY_STRIKE",
                voiceQuote = "Nikdo z vás z této arény neodejde živý!"
            ),
            attackBonus = 8,
            critBonus = 6,
            specialEffectText = "Odemkne plošnou ultimátní schopnost 'Vřava hněvu'"
        ),

        // === SORCERESS & SHADOW MAGIC BRANCH (Magie & Prokletí) - Right Side ===
        CharacterSkillNode(
            id = "sorc_mana_focus_1",
            name = "Temná meditace",
            icon = "🔮",
            description = "Koncentruje stínovou auru pro urychlenou regeneraci many.",
            branchName = "Magie & Efekty",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 1,
            xpCost = 60,
            spCost = 1,
            reqLevel = 1,
            x = 75f,
            y = 20f,
            manaRegenBonus = 6,
            attackBonus = 4,
            specialEffectText = "+6 Mana regenerace za kolo, +4 Magický útok"
        ),
        CharacterSkillNode(
            id = "sorc_active_shadow_bolt",
            name = "Stínová koule zkázy",
            icon = "👁️",
            description = "Vrhne kouli temné energie, která ignoruje 50% obrany nepřítele.",
            branchName = "Magie & Efekty",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 2,
            xpCost = 140,
            spCost = 1,
            reqNodeId = "sorc_mana_focus_1",
            reqLevel = 2,
            reqAffinityLevel = 2,
            x = 85f,
            y = 40f,
            activeSkill = PartyCombatSkill(
                id = "skill_shadow_bolt",
                name = "Stínová koule zkázy",
                icon = "👁️",
                description = "200% Magické temné poškození. Ignoruje polovinu obrany cíle.",
                manaCost = 25,
                cooldownTurns = 2,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.DARK_MAGIC,
                powerMultiplier = 2.0f,
                animationType = "DARK_BURST",
                voiceQuote = "Stíny tě pohltí a vymažou tvou existenci!"
            ),
            critBonus = 5,
            specialEffectText = "Odemkne aktivní kouzlo 'Stínová koule zkázy'"
        ),
        CharacterSkillNode(
            id = "sorc_passive_curse_aura",
            name = "Oslavující aura",
            icon = "💀",
            description = "Přítomnost čarodějky neustále oslabuje útok a odolnost nepřátel.",
            branchName = "Magie & Efekty",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 3,
            xpCost = 260,
            spCost = 1,
            reqNodeId = "sorc_active_shadow_bolt",
            reqLevel = 3,
            reqLoajalita = 40,
            x = 75f,
            y = 60f,
            bonusComboGain = 8,
            attackBonus = 6,
            specialEffectText = "+8 Bodů k Harémovému Kombu při každém kouzlu, +6 Útok"
        ),
        CharacterSkillNode(
            id = "sorc_active_abyssal_meteor",
            name = "Hvězda prázdnoty",
            icon = "🌌",
            description = "Přivolá meteor z temných sfér, který spálí a omráčí celé nepřátelské uskupení.",
            branchName = "Magie & Efekty",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 4,
            xpCost = 480,
            spCost = 2,
            reqNodeId = "sorc_passive_curse_aura",
            reqLevel = 5,
            reqAffinityLevel = 4,
            x = 85f,
            y = 85f,
            activeSkill = PartyCombatSkill(
                id = "skill_abyssal_meteor",
                name = "Hvězda prázdnoty",
                icon = "🌌",
                description = "190% Plamenné temné poškození všem nepřátelům + šance na omráčení.",
                manaCost = 45,
                manaEssenceCost = 8,
                cooldownTurns = 4,
                targetType = SkillTargetType.ALL_ENEMIES,
                category = SkillCategory.DARK_MAGIC,
                powerMultiplier = 1.9f,
                appliedStatus = CombatStatusEffect("stun_meteor", "Omráčení stínem", "💫", "STUN", value = 1, durationTurns = 1),
                animationType = "DARK_BURST",
                voiceQuote = "Temnota pána zapálí samotné nebe!"
            ),
            attackBonus = 10,
            specialEffectText = "Odemkne ultimátní plošné kouzlo 'Hvězda prázdnoty'"
        ),

        // === DEFENDER & SUPPORT BRANCH (Obrana & Podpora) - Center ===
        CharacterSkillNode(
            id = "def_shield_up_1",
            name = "Obranný postoj",
            icon = "🛡️",
            description = "Základy štítového boje a krytí slabin družiny.",
            branchName = "Obrana & Podpora",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 1,
            xpCost = 60,
            spCost = 1,
            reqLevel = 1,
            x = 50f,
            y = 15f,
            defenseBonus = 6,
            hpBonus = 25,
            specialEffectText = "+6 Obrana, +25 Max HP"
        ),
        CharacterSkillNode(
            id = "def_active_holy_shield",
            name = "Posvátný ochranný štít",
            icon = "✨",
            description = "Obalí vybranou společnici nebo sebe magickým štítem absorbujícím zranění.",
            branchName = "Obrana & Podpora",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 2,
            xpCost = 140,
            spCost = 1,
            reqNodeId = "def_shield_up_1",
            reqLevel = 2,
            reqAffinityLevel = 1,
            x = 50f,
            y = 35f,
            activeSkill = PartyCombatSkill(
                id = "skill_holy_shield",
                name = "Posvátný ochranný štít",
                icon = "✨",
                description = "Poskytne spojenkyni štít absorbující 45 bodů poškození po dobu 3 kol.",
                manaCost = 20,
                cooldownTurns = 2,
                targetType = SkillTargetType.SINGLE_ALLY,
                category = SkillCategory.SUPPORT_BUFF,
                healAmount = 20,
                appliedStatus = CombatStatusEffect("shield_buff", "Bariéra", "🛡️", "SHIELD", value = 45, durationTurns = 3),
                animationType = "HAREM_SUPPORT",
                voiceQuote = "Můj štít tě ochrání před každou ranou!"
            ),
            defenseBonus = 4,
            specialEffectText = "Odemkne podpůrné kouzlo 'Posvátný ochranný štít'"
        ),
        CharacterSkillNode(
            id = "def_passive_aegis",
            name = "Železná vůle pána",
            icon = "🧱",
            description = "Trvalá redukce veškerého příchozího fyzického a magického poškození.",
            branchName = "Obrana & Podpora",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 3,
            xpCost = 250,
            spCost = 1,
            reqNodeId = "def_active_holy_shield",
            reqLevel = 3,
            reqLoajalita = 50,
            x = 50f,
            y = 55f,
            damageMitigationPercent = 15,
            defenseBonus = 8,
            hpBonus = 35,
            specialEffectText = "Snižuje veškeré obdržené poškození o 15%, +8 Obrana"
        ),
        CharacterSkillNode(
            id = "def_active_divine_blessing",
            name = "Extáze oddanosti",
            icon = "💖",
            description = "Vyléčí celý tým a naplní družinu euforií zvyšující útok o 25%.",
            branchName = "Obrana & Podpora",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 4,
            xpCost = 460,
            spCost = 2,
            reqNodeId = "def_passive_aegis",
            reqLevel = 5,
            reqAffinityLevel = 5,
            x = 50f,
            y = 80f,
            activeSkill = PartyCombatSkill(
                id = "skill_harem_ecstasy",
                name = "Extáze oddanosti",
                icon = "💖",
                description = "Vyléčí VŠECHNY spojence za 50 HP a zvýší jejich útok o +8 na 3 kola.",
                manaCost = 40,
                manaEssenceCost = 6,
                cooldownTurns = 3,
                targetType = SkillTargetType.ALL_ALLIES,
                category = SkillCategory.HOLY_HEAL,
                healAmount = 50,
                appliedStatus = CombatStatusEffect("atk_buff_ecstasy", "Extáze lásky", "⚔️", "ATK_BUFF", value = 8, durationTurns = 3),
                animationType = "HAREM_SUPPORT",
                voiceQuote = "Pro našeho pána dáme všechno!"
            ),
            hpBonus = 40,
            specialEffectText = "Odemkne ultimátní týmové léčení 'Extáze oddanosti'"
        ),

        // === ASSASSIN & SIREN BRANCH (Rychlost, Jed & Šarm) - Far Sides / Hidden Paths ===
        CharacterSkillNode(
            id = "assassin_reflex_1",
            name = "Bleskové reflexy",
            icon = "⚡",
            description = "Zvyšuje rychlost jednání a šanci na smrtící kritické zásahy.",
            branchName = "Boj",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 1,
            xpCost = 60,
            spCost = 1,
            reqLevel = 1,
            x = 10f,
            y = 15f,
            speedBonus = 6,
            critBonus = 8,
            specialEffectText = "+6 Rychlost, +8% Šance na kritický úder"
        ),
        CharacterSkillNode(
            id = "assassin_active_venom",
            name = "Smrtící stínový bod",
            icon = "🗡️",
            description = "Bleskový zásah do tepny, který otráví cíl a způsobí masivní poškození.",
            branchName = "Boj",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 2,
            xpCost = 150,
            spCost = 1,
            reqNodeId = "assassin_reflex_1",
            reqLevel = 2,
            reqAffinityLevel = 3,
            x = 5f,
            y = 35f,
            activeSkill = PartyCombatSkill(
                id = "skill_venom_strike",
                name = "Smrtící stínový bod",
                icon = "🗡️",
                description = "210% Kritické bodnutí + těžký jed na 3 kola (18 dmg/kolo).",
                manaCost = 24,
                cooldownTurns = 2,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.PHYSICAL_ATTACK,
                powerMultiplier = 2.1f,
                appliedStatus = CombatStatusEffect("venom_lethal", "Smrtící jed", "🧪", "POISON", value = 18, durationTurns = 3),
                animationType = "SLASH",
                voiceQuote = "Než mrkneš, bude po tobě..."
            ),
            attackBonus = 5,
            specialEffectText = "Odemkne útočnou schopnost 'Smrtící stínový bod'"
        ),
        CharacterSkillNode(
            id = "assassin_passive_fatal",
            name = "Popravčí instinkt",
            icon = "🎯",
            description = "Pokud má cíl méně než 40% HP, zásahy způsobují o 40% vyšší poškození.",
            branchName = "Boj",
            nodeType = SkillNodeType.PASSIVE_PERK,
            tier = 3,
            xpCost = 270,
            spCost = 1,
            reqNodeId = "assassin_active_venom",
            reqLevel = 3,
            reqLoajalita = 60,
            x = 10f,
            y = 55f,
            critBonus = 10,
            attackBonus = 6,
            specialEffectText = "+10% Krit, +40% Bonus k poškození zraněných cílů (<40% HP)"
        ),
        CharacterSkillNode(
            id = "siren_active_bewitch",
            name = "Vábivý polibek sirény",
            icon = "💋",
            description = "Okouzlí nepřítele, zcela ho omráčí na 2 kola a donutí ho snížit svou obranu.",
            branchName = "Magie & Efekty",
            nodeType = SkillNodeType.ACTIVE_ABILITY,
            tier = 4,
            xpCost = 470,
            spCost = 2,
            reqNodeId = "assassin_passive_fatal",
            reqLevel = 5,
            reqAffinityLevel = 5,
            x = 5f,
            y = 75f,
            activeSkill = PartyCombatSkill(
                id = "skill_siren_charm",
                name = "Vábivý polibek sirény",
                icon = "💋",
                description = "Omráčí nepřítele na 2 kola a sníží jeho útok i obranu o 35%.",
                manaCost = 35,
                manaEssenceCost = 4,
                cooldownTurns = 3,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.HEX_DEBUFF,
                appliedStatus = CombatStatusEffect("siren_charm", "Očarování", "💫", "STUN", value = 2, durationTurns = 2),
                animationType = "HAREM_SUPPORT",
                voiceQuote = "Podlehni mému kouzlu a padni na kolena!"
            ),
            bonusComboGain = 15,
            specialEffectText = "Odemkne silné kontrolní kouzlo 'Vábivý polibek sirény'"
        )
    )


    /**
     * Get the current rank of a skill on a character (0 if locked, 1-5 if unlocked/upgraded).
     */
    fun getSkillRank(character: Character, skillId: String): Int {
        val customRank = character.skillRanks[skillId]
        if (customRank != null && customRank > 0) return customRank
        val isUnlocked = character.unlockedPassives.contains(skillId) || character.unlockedCombatSkills.contains(skillId)
        return if (isUnlocked) 1 else 0
    }

    /**
     * Calculate resource and material cost to upgrade a skill node to the next rank.
     */
    fun getUpgradeCostForRank(node: CharacterSkillNode, targetRank: Int): SkillUpgradeCost {
        val tier = node.tier.coerceAtLeast(1)
        val rankMultiplier = targetRank.coerceAtLeast(1)
        return when (targetRank) {
            1 -> SkillUpgradeCost(
                goldCost = 50 * tier,
                darkEnergyCost = 20 * tier,
                xpCost = node.xpCost,
                spCost = node.spCost,
                darkShards = 5 * tier,
                manaEssence = if (tier >= 2) 3 * tier else 0,
                moonDust = if (tier >= 3) 2 * tier else 0,
                dragonBlood = if (tier >= 4) 1 else 0,
                crystals = if (tier >= 3) 2 else 0
            )
            2 -> SkillUpgradeCost(
                goldCost = 100 * tier,
                darkEnergyCost = 40 * tier,
                xpCost = (node.xpCost * 1.3f).toInt(),
                spCost = 1,
                darkShards = 10 * tier,
                manaEssence = 6 * tier,
                moonDust = if (tier >= 2) 4 * tier else 0,
                dragonBlood = if (tier >= 4) 1 else 0,
                crystals = 4 * tier
            )
            3 -> SkillUpgradeCost(
                goldCost = 200 * tier,
                darkEnergyCost = 80 * tier,
                xpCost = (node.xpCost * 1.8f).toInt(),
                spCost = 1,
                darkShards = 18 * tier,
                manaEssence = 12 * tier,
                moonDust = 8 * tier,
                dragonBlood = if (tier >= 3) 2 * tier else 0,
                crystals = 8 * tier
            )
            4 -> SkillUpgradeCost(
                goldCost = 350 * tier,
                darkEnergyCost = 150 * tier,
                xpCost = (node.xpCost * 2.5f).toInt(),
                spCost = 2,
                darkShards = 30 * tier,
                manaEssence = 20 * tier,
                moonDust = 15 * tier,
                dragonBlood = 3 * tier,
                crystals = 15 * tier
            )
            else -> SkillUpgradeCost( // Rank 5 - Master Rank
                goldCost = 600 * tier,
                darkEnergyCost = 250 * tier,
                xpCost = (node.xpCost * 3.5f).toInt(),
                spCost = 2,
                darkShards = 50 * tier,
                manaEssence = 35 * tier,
                moonDust = 25 * tier,
                dragonBlood = 5 * tier,
                crystals = 25 * tier
            )
        }
    }

    /**
     * Get nodes grouped by branch.
     */
    fun getSkillTreeForCharacter(character: Character): List<CharacterSkillNode> {
        return ALL_SKILL_NODES
    }

    /**
     * Determine if a character meets the requirements to unlock a node.
     */
    fun canUnlockNode(character: Character, node: CharacterSkillNode): Boolean {
        if (character.unlockedPassives.contains(node.id) || character.unlockedCombatSkills.contains(node.id)) {
            return false // Already unlocked (use upgrade instead)
        }
        if (character.level < node.reqLevel) {
            return false
        }
        if (character.affinityLevel < node.reqAffinityLevel) {
            return false
        }
        if (character.loajalita < node.reqLoajalita) {
            return false
        }
        if (node.reqNodeId != null) {
            val hasReq = character.unlockedPassives.contains(node.reqNodeId) || character.unlockedCombatSkills.contains(node.reqNodeId)
            if (!hasReq) return false
        }
        return true
    }

    /**
     * Determine if a character meets requirements to enhance an already unlocked skill.
     */
    fun canEnhanceNode(character: Character, node: CharacterSkillNode): Pair<Boolean, String> {
        val currentRank = getSkillRank(character, node.id)
        if (currentRank <= 0) {
            return Pair(false, "Schopnost musí být nejprve odemčena.")
        }
        if (currentRank >= node.maxRank) {
            return Pair(false, "Schopnost již dosáhla maximální úrovně (Rank ${node.maxRank}).")
        }
        val nextRank = currentRank + 1
        val reqLevelForNext = node.reqLevel + (nextRank - 1)
        if (character.level < reqLevelForNext) {
            return Pair(false, "Pro Rank $nextRank je vyžadována úroveň $reqLevelForNext.")
        }
        return Pair(true, "Lze vylepšit na Rank $nextRank.")
    }

    /**
     * Calculate total combat stat bonuses from all unlocked passives on a character, scaled by their active rank.
     */
    fun calculatePassiveBonuses(character: Character): CombatPassiveBonuses {
        var bonusAtk = 0
        var bonusDef = 0
        var bonusHp = 0
        var bonusCrit = 0
        var bonusSpeed = 0
        var lifesteal = 0
        var mitigation = 0
        var comboBonus = 0
        var manaRegen = 0

        ALL_SKILL_NODES.forEach { node ->
            val rank = getSkillRank(character, node.id)
            if (rank > 0) {
                // Rank 1 gives 1x, Rank 2 gives 1.4x, Rank 3 gives 1.8x, Rank 4 gives 2.2x, Rank 5 gives 2.7x
                val rankMultiplier = 1.0f + (rank - 1) * 0.4f
                bonusAtk += (node.attackBonus * rankMultiplier).toInt()
                bonusDef += (node.defenseBonus * rankMultiplier).toInt()
                bonusHp += (node.hpBonus * rankMultiplier).toInt()
                bonusCrit += (node.critBonus * rankMultiplier).toInt()
                bonusSpeed += (node.speedBonus * rankMultiplier).toInt()
                lifesteal += (node.lifestealPercent * rankMultiplier).toInt()
                mitigation += (node.damageMitigationPercent * rankMultiplier).toInt()
                comboBonus += (node.bonusComboGain * rankMultiplier).toInt()
                manaRegen += (node.manaRegenBonus * rankMultiplier).toInt()
            }
        }

        return CombatPassiveBonuses(
            attackBonus = bonusAtk,
            defenseBonus = bonusDef,
            hpBonus = bonusHp,
            critBonus = bonusCrit,
            speedBonus = bonusSpeed,
            lifestealPercent = lifesteal,
            damageMitigationPercent = mitigation,
            bonusComboGain = comboBonus,
            manaRegenBonus = manaRegen
        )
    }

    /**
     * Get all active skills unlocked by a character, scaled by their rank level.
     */
    fun getUnlockedActiveSkills(character: Character): List<PartyCombatSkill> {
        return ALL_SKILL_NODES.filter { node ->
            node.activeSkill != null && (character.unlockedCombatSkills.contains(node.id) || getSkillRank(character, node.id) > 0)
        }.mapNotNull { node ->
            val baseSkill = node.activeSkill ?: return@mapNotNull null
            val rank = getSkillRank(character, node.id).coerceAtLeast(1)
            if (rank == 1) {
                baseSkill
            } else {
                // Enhance powerMultiplier (+0.25 per rank), lower mana cost (-2 MP per rank), increase heal
                val powerBonus = (rank - 1) * 0.25f
                val manaReduction = ((rank - 1) * 2).coerceAtMost(baseSkill.manaCost - 5)
                val healBonus = if (baseSkill.healAmount > 0) (rank - 1) * 15 else 0
                val enhancedStatus = baseSkill.appliedStatus?.let { status ->
                    status.copy(value = status.value + (rank - 1) * 4)
                }
                baseSkill.copy(
                    name = "${baseSkill.name} (R${rank})",
                    powerMultiplier = baseSkill.powerMultiplier + powerBonus,
                    manaCost = (baseSkill.manaCost - manaReduction).coerceAtLeast(5),
                    healAmount = baseSkill.healAmount + healBonus,
                    appliedStatus = enhancedStatus
                )
            }
        }
    }
}

data class CombatPassiveBonuses(
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val critBonus: Int = 0,
    val speedBonus: Int = 0,
    val lifestealPercent: Int = 0,
    val damageMitigationPercent: Int = 0,
    val bonusComboGain: Int = 0,
    val manaRegenBonus: Int = 0
)
