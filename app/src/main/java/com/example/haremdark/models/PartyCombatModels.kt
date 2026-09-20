package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Combat roles for harem party members.
 */
enum class CombatRole(
    val title: String,
    val icon: String,
    val description: String,
    val statTag: String
) {
    TANK_GUARDIAN("Strážkyně", "🛡️", "Vysoká obrana, přitahuje pozornost a kryje družku", "Vysoké HP & Obrana"),
    PHYSICAL_DPS("Krvavá gladiátorka", "⚔️", "Vysoké fyzické poškození, krvácení a kritické zásahy", "Vysoký Útok & Krit"),
    DARK_SORCERESS("Temná čarodějka", "🔮", "Magické ničivé plošné útoky, kletby a vysávání many", "Vysoká Magie & Plošné"),
    HEALER_PRIESTESS("Svatá kněžka", "✨", "Léčení spojenců, odstraňování kleteb a posvátné štíty", "Léčení & Podpora"),
    SIREN_DEBUFFER("Vábivá siréna", "💋", "Omračování, oslabení nepřátel a posilování týmové many", "Kontrola & Oslabení"),
    ASSASSIN_BLADE("Stínová vražedkyně", "🗡️", "Blesková rychlost, ignorace zbroje a okamžité popravy", "Rychlost & Průraz")
}

enum class Element {
    PHYSICAL, FIRE, ICE, LIGHTNING, DARK, HOLY
}

/**
 * Character combat line positioning for tactical formation bonuses and targeting modifiers.
 */
@Serializable
enum class FormationPosition(
    val title: String,
    val icon: String,
    val shortTag: String,
    val description: String,
    val statSummary: String,
    val targetingWeight: Float, // Higher means enemies attack this line first
    val defenseModifierPercent: Float, // +0.25f means +25% defense
    val physicalAttackModifierPercent: Float, // +0.15f means +15% attack
    val magicHealModifierPercent: Float, // +0.25f means +25% magic & healing
    val critModifierPercent: Int, // +12% crit
    val manaRegenBonus: Int, // +8 mana per round
    val speedModifierPercent: Float,
    val damageMitigationPercent: Float // Flat % incoming damage reduction
) {
    FRONT_LINE(
        title = "Přední linie (Vanguard)",
        icon = "🛡️",
        shortTag = "PŘEDNÍ",
        description = "První linie obrany absorbující primární údery. Poskytuje robustní obranu a redukci zranění, stahuje na sebe pozornost nepřátel.",
        statSummary = "+25% Obrana • +15% Redukce DMG • +70% Cílení nepřátel",
        targetingWeight = 2.0f,
        defenseModifierPercent = 0.25f,
        physicalAttackModifierPercent = 0.05f,
        magicHealModifierPercent = -0.10f,
        critModifierPercent = 0,
        manaRegenBonus = 0,
        speedModifierPercent = -0.05f,
        damageMitigationPercent = 0.15f
    ),
    MID_LINE(
        title = "Střední linie (Bojová linie)",
        icon = "⚔️",
        shortTag = "STŘED",
        description = "Útočná flexibilní pozice pro fyzické bojovnice a duelantky. Zvyšuje útočnou sílu, rychlost a kritické zásahy.",
        statSummary = "+15% Útok • +10% Rychlost • +12% Krit • Vyvážené cílení",
        targetingWeight = 1.0f,
        defenseModifierPercent = 0.0f,
        physicalAttackModifierPercent = 0.15f,
        magicHealModifierPercent = 0.05f,
        critModifierPercent = 12,
        manaRegenBonus = 2,
        speedModifierPercent = 0.10f,
        damageMitigationPercent = 0.0f
    ),
    BACK_LINE(
        title = "Zadní linie (Kouzelnické křídlo)",
        icon = "🔮",
        shortTag = "ZADNÍ",
        description = "Chráněné křídlo pro kouzelnice, sirény a léčitelky. Poskytuje vysoký bonus k magii a léčení s minimálním rizikem zásahu.",
        statSummary = "+25% Magie & Léčení • +8 MP/kolo • -60% Cílení nepřátel • -15% Obrana",
        targetingWeight = 0.35f,
        defenseModifierPercent = -0.15f,
        physicalAttackModifierPercent = -0.10f,
        magicHealModifierPercent = 0.25f,
        critModifierPercent = 5,
        manaRegenBonus = 8,
        speedModifierPercent = 0.0f,
        damageMitigationPercent = 0.0f
    );

    companion object {
        fun fromString(value: String?): FormationPosition = when (value?.uppercase()) {
            "FRONT", "FRONT_LINE", "VANGUARD", "PŘEDNÍ" -> FRONT_LINE
            "BACK", "BACK_LINE", "REARGUARD", "SANCTUARY", "ZADNÍ" -> BACK_LINE
            else -> MID_LINE
        }
    }
}

/**
 * Team formation synergy calculated from the composition of lines.
 */
@Serializable
data class TeamFormationSynergy(
    val name: String,
    val icon: String,
    val description: String,
    val frontCount: Int,
    val midCount: Int,
    val backCount: Int,
    val teamBuffDescription: String
) {
    companion object {
        fun calculateFormationSynergy(party: List<PartyMember>): TeamFormationSynergy {
            val front = party.count { it.formationPosition == FormationPosition.FRONT_LINE }
            val mid = party.count { it.formationPosition == FormationPosition.MID_LINE }
            val back = party.count { it.formationPosition == FormationPosition.BACK_LINE }

            return when {
                front >= 2 && back >= 1 -> TeamFormationSynergy(
                    name = "Nedobytná bašta (Aegis Wall)",
                    icon = "🏰",
                    description = "Vyvážená pevná formace s těžkým předním štítem a chráněným kouzelnickým křídlem.",
                    frontCount = front,
                    midCount = mid,
                    backCount = back,
                    teamBuffDescription = "+15% Týmová obrana & +10% Regenerace many"
                )
                mid >= 2 && front >= 1 -> TeamFormationSynergy(
                    name = "Útočný hrot (Spearhead Strike)",
                    icon = "🔱",
                    description = "Agresivní klínová formace zaměřená na bleskové proražení a drtivé kritické zásahy.",
                    frontCount = front,
                    midCount = mid,
                    backCount = back,
                    teamBuffDescription = "+15% Kritické poškození & +15 Kombo při startu"
                )
                back >= 2 -> TeamFormationSynergy(
                    name = "Mystické arkánum (Arcane Sanctuary)",
                    icon = "🌌",
                    description = "Dominance kouzelnic v bezpečném zázemí umožňující masivní plošná kouzla a nepřetržité léčení.",
                    frontCount = front,
                    midCount = mid,
                    backCount = back,
                    teamBuffDescription = "+20% Účinnost kouzel & léčení celého týmu"
                )
                front >= 3 -> TeamFormationSynergy(
                    name = "Železná falanga (Iron Phalanx)",
                    icon = "🛡️",
                    description = "Trojitý obranný val zcela znemožňující nepřátelům ohrozit slabší spojence.",
                    frontCount = front,
                    midCount = mid,
                    backCount = back,
                    teamBuffDescription = "+25% Redukce plošného poškození týmu"
                )
                else -> TeamFormationSynergy(
                    name = "Taktická flexibilita (Adaptive Flow)",
                    icon = "⚖️",
                    description = "Rovnoměrné rozmístění jednotek pro plynulou reakci na jakéhokoliv nepřítele.",
                    frontCount = front,
                    midCount = mid,
                    backCount = back,
                    teamBuffDescription = "+5% ke všem bojovým atributům"
                )
            }
        }
    }
}

/**
 * Status effects active on combatants.
 */
@Serializable
data class CombatStatusEffect(
    val id: String,
    val name: String,
    val icon: String,
    val type: String, // "BLEED", "POISON", "STUN", "ATK_BUFF", "DEF_BUFF", "SHIELD", "TAUNT", "REGEN", "SILENCE", "BURN", "FREEZE", "SHOCK", "HAREM_BLESSING", "CRIT_BUFF", "MANA_SURGE", "RESONANCE"
    val value: Int = 0,
    var durationTurns: Int = 2,
    val maxDuration: Int = 2,
    val description: String = "",
    val element: Element = Element.PHYSICAL
) {
    val isBuff: Boolean get() = when (type.uppercase()) {
        "ATK_BUFF", "DEF_BUFF", "SHIELD", "REGEN", "HAREM_BLESSING", "CRIT_BUFF", "MANA_SURGE", "HOLY_WARD", "SPEED_BUFF", "HEAL", "DEVOTION", "RESONANCE", "BLESSING" -> true
        else -> false
    }
    val isDebuff: Boolean get() = !isBuff
    
    val countdownProgress: Float get() = if (maxDuration > 0) {
        (durationTurns.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val isExpiringSoon: Boolean get() = durationTurns <= 1
}

/**
 * Target type for combat skills.
 */
enum class SkillTargetType {
    SINGLE_ENEMY,
    ALL_ENEMIES,
    SINGLE_ALLY,
    ALL_ALLIES,
    SELF
}

/**
 * Classification of skill action.
 */
enum class SkillCategory {
    PHYSICAL_ATTACK,
    DARK_MAGIC,
    HOLY_HEAL,
    SUPPORT_BUFF,
    HEX_DEBUFF,
    ULTIMATE_COMBO
}

/**
 * Skill usable by a harem party member in battle.
 */
@Serializable
data class PartyCombatSkill(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val manaCost: Int = 0,
    val manaEssenceCost: Int = 0,
    val cooldownTurns: Int = 0,
    var currentCooldown: Int = 0,
    val targetType: SkillTargetType = SkillTargetType.SINGLE_ENEMY,
    val category: SkillCategory = SkillCategory.PHYSICAL_ATTACK,
    val powerMultiplier: Float = 1.0f,
    val baseDamageBonus: Int = 0,
    val healAmount: Int = 0,
    val appliedStatus: CombatStatusEffect? = null,
    val voiceQuote: String? = null,
    val animationType: String = "SLASH"
)

/**
 * Represents a member of the active party (Harem character or Lord).
 */
@Serializable
data class PartyMember(
    val id: String,
    val name: String,
    val isPlayer: Boolean = false,
    val archetypeId: String = "odvazna",
    val role: CombatRole = CombatRole.PHYSICAL_DPS,
    var formationPosition: FormationPosition = FormationPosition.MID_LINE,
    var hp: Int = 100,
    val maxHp: Int = 100,
    var mana: Int = 50,
    val maxMana: Int = 50,
    var attack: Int = 20,
    var defense: Int = 10,
    var speed: Int = 15,
    var critRatePercent: Int = 10,
    var isDefending: Boolean = false,
    var statusEffects: MutableList<CombatStatusEffect> = mutableListOf(),
    val skills: List<PartyCombatSkill> = emptyList(),
    val loyaltyTierName: String = "Loajální",
    val affinityBonusDmg: Float = 1.0f,
    val favoriteWeaponIcon: String = "🗡️",
    val relationshipTierLevel: Int = 1,
    val relationshipStageName: String = "Acquaintance",
    val relationshipCombatDescription: String = "Základní bojové zapojení",
    val combatRegenBonus: Int = 0,
    val strategy: CombatStrategy = CombatStrategy.BALANCED
) {
    val isAlive: Boolean get() = hp > 0
    val hpPercent: Float get() = if (maxHp > 0) (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f
    val manaPercent: Float get() = if (maxMana > 0) (mana.toFloat() / maxMana.toFloat()).coerceIn(0f, 1f) else 0f
    val isStunned: Boolean get() = statusEffects.any { it.type == "STUN" && it.durationTurns > 0 }
    val totalShield: Int get() = statusEffects.filter { it.type == "SHIELD" }.sumOf { it.value }

    // Effective stats with formation modifiers applied
    val effectiveDefenseModifier: Float get() = formationPosition.defenseModifierPercent
    val effectiveAttackModifier: Float get() = formationPosition.physicalAttackModifierPercent
    val effectiveCritRate: Int get() = critRatePercent + formationPosition.critModifierPercent
}

/**
 * An enemy combatant in party combat.
 */
@Serializable
data class CombatEnemy(
    val id: String,
    val name: String,
    val icon: String = "👹",
    val title: String = "Nepřítel",
    val archetype: String = "Běžný",
    var formationPosition: FormationPosition = FormationPosition.FRONT_LINE,
    var hp: Int = 120,
    val maxHp: Int = 120,
    val attack: Int = 18,
    val defense: Int = 8,
    val speed: Int = 12,
    var statusEffects: MutableList<CombatStatusEffect> = mutableListOf(),
    val isBoss: Boolean = false,
    val specialAttackChance: Int = 25,
    val rewardGold: Int = 80,
    val rewardXp: Int = 40,
    val loreDescription: String = "",
    val preferredTargetRole: CombatRole? = null,
    val tactic: EnemyTactic = EnemyTactic.BALANCED
) {
    val isAlive: Boolean get() = hp > 0
    val hpPercent: Float get() = if (maxHp > 0) (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f
    val isStunned: Boolean get() = statusEffects.any { it.type == "STUN" && it.durationTurns > 0 }
}

enum class EnemyTactic {
    BALANCED,
    AGGRESSIVE,
    DEFENSIVE,
    CAUTIOUS
}

/**
 * Team Synergy activated by combination of characters in the party.
 */
@Serializable
data class PartySynergy(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val attackBonusPercent: Float = 0f,
    val defenseBonusPercent: Float = 0f,
    val critBonusPercent: Int = 0,
    val manaRegenBonus: Int = 0
)

/**
 * Outcome / Rewards from a party combat victory.
 */
@Serializable
data class PartyCombatRewards(
    val gold: Int = 0,
    val bloodRubies: Int = 0,
    val playerXp: Int = 0,
    val haremAffinityGain: Int = 0,
    val haremLoyaltyGain: Int = 0,
    val prestigeGain: Int = 0,
    val lootItems: List<String> = emptyList(),
    val itemDropDetails: List<InventoryItem> = emptyList(),
    val mvpName: String? = null,
    val mvpCharacterId: String? = null,
    val mvpBonusXp: Int = 0,
    val rank: String = "S",
    val rankTitle: String = "Dominantní triumf",
    val score: Int = 1000,
    val roundsTaken: Int = 1,
    val flawlessVictory: Boolean = true,
    val comboExecuted: Boolean = false,
    val characterXpGains: Map<String, Int> = emptyMap(),
    val bonusMultiplier: Float = 1.0f,
    val lootDistribution: LootDistributionResult? = null
)

/**
 * Dynamic combat weather affecting visibility and status effects.
 */
@Serializable
data class CombatWeather(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val attackModifierPercent: Float = 0f,
    val defenseModifierPercent: Float = 0f,
    val dodgeChanceBonus: Int = 0,
    val statusEffectName: String? = null // e.g. "Chilled", "Sun-drenched", "Electrified"
) {
    companion object {
        fun getWeatherForLocation(location: String): CombatWeather {
            return when {
                location.contains("Led", ignoreCase = true) || location.contains("Hory", ignoreCase = true) ->
                    CombatWeather("blizzard", "Ledová vánice", "❄️", "Mrznoucí vichr udílí stav 'Chilled' (-15% Rychlosti, +10% Uhýbání).", -0.05f, 0.05f, 15, "Chilled")
                location.contains("Poušť", ignoreCase = true) || location.contains("Chrám", ignoreCase = true) || location.contains("Věž", ignoreCase = true) ->
                    CombatWeather("sunny", "Slunečný žár", "☀️", "Žhnoucí paprsky udílí stav 'Sun-drenched' (+15% Poškození, -10% Obrana).", 0.15f, -0.10f, 5, "Sun-drenched")
                location.contains("Dungeon", ignoreCase = true) || location.contains("Podzemí", ignoreCase = true) || location.contains("Katakomby", ignoreCase = true) ->
                    CombatWeather("fog", "Tmavá mlha", "🌫️", "Hustá temná mlha zastiňuje bojiště (+20% Uhýbání, snížená viditelnost).", 0f, 0f, 20, "Shadow Veil")
                else ->
                    CombatWeather("storm", "Temná bouře", "⚡", "Hromobití a déšť zvyšují kritickou rezonanci (+15% Šance na kritický úder).", 0.10f, 0f, 10, "Electrified")
            }
        }
    }
}

/**
 * Active Party Combat Session.
 */
@Serializable
data class PartyCombatSession(
    val id: String,
    val encounterTitle: String,
    val encounterLocation: String,
    val backgroundDrawableRes: Int,
    var party: List<PartyMember>,
    var enemies: List<CombatEnemy>,
    var currentTurnIndex: Int = 0, // Index into party or turn order
    var currentRound: Int = 1,
    var isEnemyPhase: Boolean = false,
    var selectedTargetEnemyIndex: Int = 0,
    var selectedTargetAllyIndex: Int = 0,
    var haremComboGauge: Int = 25, // 0 to 100
    val maxHaremComboGauge: Int = 100,
    var comboChainCount: Int = 0,
    val activeSynergies: List<PartySynergy> = emptyList(),
    val weather: CombatWeather = CombatWeather.getWeatherForLocation("Arena"),
    val environmentalHazard: EnvironmentalHazard? = null,
    var hazardCountdown: Int = 2,
    var lastHazardTriggerMessage: String? = null,
    val combatLogs: List<CombatLogEntry> = emptyList(),
    var teamFormationSynergy: TeamFormationSynergy? = null,
    var isFinished: Boolean = false,
    var isVictory: Boolean = false,
    var rewards: PartyCombatRewards? = null,
    var autoBattleEnabled: Boolean = false
) {
    val aliveParty: List<PartyMember> get() = party.filter { it.isAlive }
    val aliveEnemies: List<CombatEnemy> get() = enemies.filter { it.isAlive }
    val currentActiveMember: PartyMember? get() = aliveParty.getOrNull(currentTurnIndex.coerceIn(0, (aliveParty.size - 1).coerceAtLeast(0)))
    val isComboReady: Boolean get() = haremComboGauge >= maxHaremComboGauge

    // Formation line breakdowns
    val frontLineParty: List<PartyMember> get() = aliveParty.filter { it.formationPosition == FormationPosition.FRONT_LINE }
    val midLineParty: List<PartyMember> get() = aliveParty.filter { it.formationPosition == FormationPosition.MID_LINE }
    val backLineParty: List<PartyMember> get() = aliveParty.filter { it.formationPosition == FormationPosition.BACK_LINE }
}
