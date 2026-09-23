package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Result of elemental affinity matchup in combat.
 */
@Serializable
enum class ElementalMatchupType(
    val title: String,
    val shortLabel: String,
    val icon: String,
    val modifierPercent: Int,
    val tagColorHex: Long,
    val description: String
) {
    EXTREME_WEAKNESS(
        title = "Extrémní Slabina",
        shortLabel = "+30% Zásah",
        icon = "💥⚡",
        modifierPercent = 30,
        tagColorHex = 0xFFFF1744,
        description = "Útok zasáhl fatální slabinu cíle s masivním zesílením!"
    ),
    SUPER_EFFECTIVE(
        title = "Slabina Živlu",
        shortLabel = "+25% Slabina",
        icon = "💥",
        modifierPercent = 25,
        tagColorHex = 0xFFFF7043,
        description = "Útok využil elementární převahu a pronikl obranou."
    ),
    NEUTRAL(
        title = "Neutrální Poměr",
        shortLabel = "1.0x Základ",
        icon = "⚖️",
        modifierPercent = 0,
        tagColorHex = 0xFFB0BEC5,
        description = "Živly nemají přímou výhodu ani nevýhodu."
    ),
    RESISTED(
        title = "Odolnost Živlu",
        shortLabel = "-25% Odolnost",
        icon = "🛡️",
        modifierPercent = -25,
        tagColorHex = 0xFF78909C,
        description = "Cíl je přirozeně odolný vůči tomuto elementu."
    ),
    EXTREME_RESISTANCE(
        title = "Těžká Odolnost",
        shortLabel = "-20% Ztlumeno",
        icon = "🔰",
        modifierPercent = -20,
        tagColorHex = 0xFF455A64,
        description = "Elementární ztlumení drasticky redukovalo sílu dopadu."
    );

    val isAdvantage: Boolean get() = modifierPercent > 0
    val isDisadvantage: Boolean get() = modifierPercent < 0
}

/**
 * Detailed turn-by-turn breakdown of an attack's mathematical damage calculation,
 * specifically highlighting the influence of elemental affinity multipliers,
 * element matchups, critical strikes, and defensive mitigation.
 */
@Serializable
data class ElementalDamageBreakdown(
    val turn: Int,
    val round: Int,
    val attackerName: String,
    val attackerElement: Element,
    val defenderName: String,
    val defenderElement: Element,
    val isPlayerPartyAttacker: Boolean,
    val actionName: String,
    val basePower: Int,
    val affinityMultiplier: Float, // Character's permanent affinity multiplier from training (e.g., 1.15x, 1.30x)
    val elementMatchupMultiplier: Float, // Multiplier from Element vs Element advantage/weakness (e.g. 1.25x, 0.75x)
    val matchupType: ElementalMatchupType,
    val isCritical: Boolean,
    val critMultiplier: Float = 1.0f,
    val comboMultiplier: Float = 1.0f,
    val synergyMultiplier: Float = 1.0f,
    val synergyMitigatedDamage: Int = 0,
    val activeSynergyName: String? = null,
    val targetDefense: Int,
    val defenseMitigation: Int,
    val rawCalculatedDamage: Int,
    val finalDamage: Int,
    val affinityBonusDamageGained: Int, // Flat damage gained directly from affinity training
    val matchupBonusDamageGained: Int, // Flat damage gained or lost from element matchup
    val formulaDisplay: String,
    val tacticalNote: String
) {
    /**
     * Percentage increase from affinity training alone, formatted for display.
     */
    val affinityPercentString: String get() {
        val pct = ((affinityMultiplier - 1.0f) * 100).toInt()
        return if (pct >= 0) "+$pct%" else "$pct%"
    }

    /**
     * Percentage difference from elemental matchup alone.
     */
    val matchupPercentString: String get() {
        val pct = ((elementMatchupMultiplier - 1.0f) * 100).toInt()
        return if (pct >= 0) "+$pct%" else "$pct%"
    }
}

/**
 * Aggregated analytics and effectiveness summary of the elemental affinity system in a combat session.
 */
@Serializable
data class ElementalBattleSummary(
    val totalAttacksAnalyzed: Int = 0,
    val totalPartyDamage: Int = 0,
    val totalEnemyDamage: Int = 0,
    val totalAffinityBonusDamageGained: Int = 0,
    val weaknessHitsTriggered: Int = 0,
    val resistedHitsEncountered: Int = 0,
    val highestElementalDamageHit: Int = 0,
    val bestAffinityContributor: String = "",
    val highestAffinityMultiplierObserved: Float = 1.0f,
    val elementalTurnBreakdowns: List<ElementalDamageBreakdown> = emptyList()
) {
    companion object {
        fun fromLogs(logs: List<CombatLogEntry>): ElementalBattleSummary {
            val breakdowns = logs.mapNotNull { it.elementalBreakdown }
            if (breakdowns.isEmpty()) return ElementalBattleSummary()

            val partyBreakdowns = breakdowns.filter { it.isPlayerPartyAttacker }
            val enemyBreakdowns = breakdowns.filter { !it.isPlayerPartyAttacker }

            val totalPartyDmg = partyBreakdowns.sumOf { it.finalDamage }
            val totalEnemyDmg = enemyBreakdowns.sumOf { it.finalDamage }
            val totalAffinityBonus = partyBreakdowns.sumOf { it.affinityBonusDamageGained }
            val weaknesses = breakdowns.count { it.matchupType.isAdvantage }
            val resisted = breakdowns.count { it.matchupType.isDisadvantage }
            val maxHit = breakdowns.maxOfOrNull { it.finalDamage } ?: 0
            val bestContributor = partyBreakdowns.maxByOrNull { it.affinityBonusDamageGained }?.attackerName ?: ""
            val highestMult = partyBreakdowns.maxOfOrNull { it.affinityMultiplier } ?: 1.0f

            return ElementalBattleSummary(
                totalAttacksAnalyzed = breakdowns.size,
                totalPartyDamage = totalPartyDmg,
                totalEnemyDamage = totalEnemyDmg,
                totalAffinityBonusDamageGained = totalAffinityBonus,
                weaknessHitsTriggered = weaknesses,
                resistedHitsEncountered = resisted,
                highestElementalDamageHit = maxHit,
                bestAffinityContributor = bestContributor,
                highestAffinityMultiplierObserved = highestMult,
                elementalTurnBreakdowns = breakdowns
            )
        }
    }
}
