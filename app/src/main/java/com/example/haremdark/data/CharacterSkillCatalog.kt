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
    val specialEffectText: String? = null
)

object CharacterSkillCatalog {

    /**
     * Universal and Archetype-specific skill nodes available in the game.
     */
    val ALL_SKILL_NODES: List<CharacterSkillNode> = listOf(
        // === WARRIOR / GLADIATOR BRANCH (Odvaha & Čepel) ===
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
            activeSkill = PartyCombatSkill(
                id = "skill_berserk_whirlwind",
                name = "Vřava hněvu",
                icon = "🌪️",
                description = "Zasáhne VŠECHNY nepřátele za 150% poškození a sníží jejich obranu.",
                manaCost = 38,
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

        // === SORCERESS & SHADOW MAGIC BRANCH (Magie & Prokletí) ===
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
            activeSkill = PartyCombatSkill(
                id = "skill_abyssal_meteor",
                name = "Hvězda prázdnoty",
                icon = "🌌",
                description = "190% Plamenné temné poškození všem nepřátelům + šance na omráčení.",
                manaCost = 45,
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

        // === DEFENDER, HEALER & SUPPORT BRANCH (Obrana & Podpora) ===
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
            activeSkill = PartyCombatSkill(
                id = "skill_harem_ecstasy",
                name = "Extáze oddanosti",
                icon = "💖",
                description = "Vyléčí VŠECHNY spojence za 50 HP a zvýší jejich útok o +8 na 3 kola.",
                manaCost = 40,
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

        // === ASSASSIN & SIREN BRANCH (Rychlost, Jed & Šarm) ===
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
            activeSkill = PartyCombatSkill(
                id = "skill_siren_charm",
                name = "Vábivý polibek sirény",
                icon = "💋",
                description = "Omráčí nepřítele na 2 kola a sníží jeho útok i obranu o 35%.",
                manaCost = 35,
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
            return false // Already unlocked
        }
        if (character.level < node.reqLevel) {
            return false
        }
        if (node.reqNodeId != null) {
            val hasReq = character.unlockedPassives.contains(node.reqNodeId) || character.unlockedCombatSkills.contains(node.reqNodeId)
            if (!hasReq) return false
        }
        return true
    }

    /**
     * Calculate total combat stat bonuses from all unlocked passives on a character.
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
            if (character.unlockedPassives.contains(node.id)) {
                bonusAtk += node.attackBonus
                bonusDef += node.defenseBonus
                bonusHp += node.hpBonus
                bonusCrit += node.critBonus
                bonusSpeed += node.speedBonus
                lifesteal += node.lifestealPercent
                mitigation += node.damageMitigationPercent
                comboBonus += node.bonusComboGain
                manaRegen += node.manaRegenBonus
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
     * Get all active skills unlocked by a character.
     */
    fun getUnlockedActiveSkills(character: Character): List<PartyCombatSkill> {
        return ALL_SKILL_NODES.filter { node ->
            node.activeSkill != null && character.unlockedCombatSkills.contains(node.id)
        }.mapNotNull { it.activeSkill }
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
