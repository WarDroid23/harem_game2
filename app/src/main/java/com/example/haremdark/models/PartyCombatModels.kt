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

/**
 * Status effects active on combatants.
 */
@Serializable
data class CombatStatusEffect(
    val id: String,
    val name: String,
    val icon: String,
    val type: String, // "BLEED", "POISON", "STUN", "ATK_BUFF", "DEF_BUFF", "SHIELD", "TAUNT", "REGEN", "SILENCE"
    val value: Int = 0,
    var durationTurns: Int = 2,
    val description: String = ""
)

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
    var hp: Int = 100,
    val maxHp: Int = 100,
    var mana: Int = 50,
    val maxMana: Int = 50,
    val attack: Int = 20,
    val defense: Int = 10,
    val speed: Int = 15,
    val critRatePercent: Int = 10,
    var isDefending: Boolean = false,
    var statusEffects: MutableList<CombatStatusEffect> = mutableListOf(),
    val skills: List<PartyCombatSkill> = emptyList(),
    val loyaltyTierName: String = "Loajální",
    val affinityBonusDmg: Float = 1.0f,
    val favoriteWeaponIcon: String = "🗡️"
) {
    val isAlive: Boolean get() = hp > 0
    val hpPercent: Float get() = if (maxHp > 0) (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f
    val manaPercent: Float get() = if (maxMana > 0) (mana.toFloat() / maxMana.toFloat()).coerceIn(0f, 1f) else 0f
    val isStunned: Boolean get() = statusEffects.any { it.type == "STUN" && it.durationTurns > 0 }
    val totalShield: Int get() = statusEffects.filter { it.type == "SHIELD" }.sumOf { it.value }
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
    val preferredTargetRole: CombatRole? = null
) {
    val isAlive: Boolean get() = hp > 0
    val hpPercent: Float get() = if (maxHp > 0) (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f
    val isStunned: Boolean get() = statusEffects.any { it.type == "STUN" && it.durationTurns > 0 }
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
    val bonusMultiplier: Float = 1.0f
)

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
    val activeSynergies: List<PartySynergy> = emptyList(),
    val combatLogs: List<CombatLogEntry> = emptyList(),
    var isFinished: Boolean = false,
    var isVictory: Boolean = false,
    var rewards: PartyCombatRewards? = null,
    var autoBattleEnabled: Boolean = false
) {
    val aliveParty: List<PartyMember> get() = party.filter { it.isAlive }
    val aliveEnemies: List<CombatEnemy> get() = enemies.filter { it.isAlive }
    val currentActiveMember: PartyMember? get() = aliveParty.getOrNull(currentTurnIndex.coerceIn(0, (aliveParty.size - 1).coerceAtLeast(0)))
    val isComboReady: Boolean get() = haremComboGauge >= maxHaremComboGauge
}
