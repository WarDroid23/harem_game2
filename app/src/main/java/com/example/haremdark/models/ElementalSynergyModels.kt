package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Represents an active Elemental Synergy bonus created when characters with
 * complementary affinity elements are assembled in the same party.
 *
 * Each synergy triggers passive damage resistance buffs against specific incoming
 * enemy elements or across-the-board elemental attacks.
 */
@Serializable
data class ElementalSynergyBuff(
    val id: String,
    val name: String,
    val icon: String,
    val titleBadge: String,
    val description: String,
    val requiredElements: List<Element>,
    val resistedElements: List<Element>,
    val damageResistancePercent: Float, // e.g., 0.22f = 22% damage reduction
    val allElementResistancePercent: Float = 0f, // e.g., 0.05f = +5% to all other elements
    val bonusPerkDescription: String = "",
    val participatingMemberNames: List<String> = emptyList()
) {
    /**
     * Checks if this synergy provides resistance against an incoming attack element.
     */
    fun protectsAgainst(attackerElement: Element): Boolean {
        return resistedElements.contains(attackerElement) || allElementResistancePercent > 0f
    }

    /**
     * Calculates the exact resistance modifier applied against an incoming element.
     */
    fun getMitigationRatio(attackerElement: Element): Float {
        return when {
            resistedElements.contains(attackerElement) -> damageResistancePercent + allElementResistancePercent
            allElementResistancePercent > 0f -> allElementResistancePercent
            else -> 0f
        }
    }
}

/**
 * Result of passive damage mitigation calculated against an incoming enemy attack.
 */
@Serializable
data class SynergyMitigationResult(
    val totalMitigationRatio: Float, // Total percentage reduction (0.0 to 0.50 capped)
    val mitigatedDamageAmount: Int, // Flat HP damage prevented
    val finalDamageAfterSynergy: Int,
    val contributingSynergies: List<ElementalSynergyBuff>,
    val explanationText: String
)

/**
 * Engine catalog for evaluating, unlocking, and applying Elemental Synergy bonuses
 * for complementary affinity elements.
 */
object ElementalSynergyManager {

    /**
     * Standard definitions of complementary element synergies and multi-element harmonies.
     */
    val DEFINED_SYNERGIES = listOf(
        ElementalSynergyBuff(
            id = "magmatic_bastion",
            name = "Magmatický krunýř",
            icon = "🔥🌿",
            titleBadge = "Oheň + Země (Rezistence -22%)",
            description = "Spojení zemského nerostu a žáru ohnivé výhně taví přicházející údery do neškodné strusky.",
            requiredElements = listOf(Element.FIRE, Element.EARTH),
            resistedElements = listOf(Element.FIRE, Element.EARTH, Element.PHYSICAL),
            damageResistancePercent = 0.22f,
            allElementResistancePercent = 0.03f,
            bonusPerkDescription = "-22% Poškození od Ohně, Země a Fyzických útoků (-3% od všech ostatních)."
        ),
        ElementalSynergyBuff(
            id = "glacial_mirror",
            name = "Glaciální zrcadlo",
            icon = "💧❄️",
            titleBadge = "Voda + Led (Rezistence -25%)",
            description = "Ledová tříšť a hlubinná voda vytvářejí krystalické zrcadlo pohlcující chlad, proudy a prudké poryvy.",
            requiredElements = listOf(Element.WATER, Element.ICE),
            resistedElements = listOf(Element.WATER, Element.ICE, Element.AIR),
            damageResistancePercent = 0.25f,
            allElementResistancePercent = 0.02f,
            bonusPerkDescription = "-25% Poškození od Vody, Ledu a Vzduchu."
        ),
        ElementalSynergyBuff(
            id = "storm_aegis",
            name = "Bouřková rezonance",
            icon = "⚡💨",
            titleBadge = "Blesk + Vzduch (Rezistence -24%)",
            description = "Vysokonapěťová vzdušná vířivá bariéra odklání bleskové a větrné útoky dříve, než zasáhnou cíl.",
            requiredElements = listOf(Element.LIGHTNING, Element.AIR),
            resistedElements = listOf(Element.LIGHTNING, Element.AIR, Element.EARTH),
            damageResistancePercent = 0.24f,
            allElementResistancePercent = 0.02f,
            bonusPerkDescription = "-24% Poškození od Blesků, Větru a Země."
        ),
        ElementalSynergyBuff(
            id = "twilight_eclipse",
            name = "Eclipsní rovnováha",
            icon = "🔮✨",
            titleBadge = "Temnota + Světlo (Rezistence -28%)",
            description = "Dokonalá kosmická symbióza stínu a posvátného světla neutralizuje nepřátelské kletby i paprsky absolutního jasu.",
            requiredElements = listOf(Element.DARK, Element.HOLY),
            resistedElements = listOf(Element.DARK, Element.HOLY),
            damageResistancePercent = 0.28f,
            allElementResistancePercent = 0.06f,
            bonusPerkDescription = "-28% Poškození od Temnoty a Světla & -6% pasivní redukce všech elementů."
        ),
        ElementalSynergyBuff(
            id = "plasma_discharge",
            name = "Termoelektrická plazma",
            icon = "🔥⚡",
            titleBadge = "Oheň + Blesk (Rezistence -22%)",
            description = "Vysokoenergetická ionizační aura rozptyluje prudké žhavé i elektrické výboje nepřátel.",
            requiredElements = listOf(Element.FIRE, Element.LIGHTNING),
            resistedElements = listOf(Element.FIRE, Element.LIGHTNING, Element.ICE),
            damageResistancePercent = 0.22f,
            allElementResistancePercent = 0.02f,
            bonusPerkDescription = "-22% Poškození od Ohně, Blesků a Ledu."
        ),
        ElementalSynergyBuff(
            id = "primal_biosphere",
            name = "Pralesní záštita",
            icon = "💧🌿",
            titleBadge = "Voda + Země (Rezistence -22%)",
            description = "Živá symbióza pramenité vody a mateřské země pohlcuje otřesy a neustále zpevňuje vitální tkáně.",
            requiredElements = listOf(Element.WATER, Element.EARTH),
            resistedElements = listOf(Element.WATER, Element.EARTH, Element.PHYSICAL),
            damageResistancePercent = 0.22f,
            allElementResistancePercent = 0.03f,
            bonusPerkDescription = "-22% Poškození od Vody, Země a Fyzických úderů."
        ),
        ElementalSynergyBuff(
            id = "umbral_carapace",
            name = "Umbrální zbroj",
            icon = "🗡️🔮",
            titleBadge = "Fyzická + Temnota (Rezistence -25%)",
            description = "Těžká ocelová zbroj prosycená esencí temnoty drasticky tlumí fyzické údery i stínové zásahy.",
            requiredElements = listOf(Element.PHYSICAL, Element.DARK),
            resistedElements = listOf(Element.PHYSICAL, Element.DARK),
            damageResistancePercent = 0.25f,
            allElementResistancePercent = 0.03f,
            bonusPerkDescription = "-25% Poškození od Fyzických úderů a Temnoty."
        ),
        ElementalSynergyBuff(
            id = "elemental_pantheon",
            name = "Panteon Živlů",
            icon = "🌟🌈",
            titleBadge = "4+ Různé Živly (Všestranná -18%)",
            description = "Absolutní harmonie různorodosti elementů chrání celou družinu před jakoukoliv nepřátelskou elementární silou.",
            requiredElements = emptyList(), // Evaluated dynamically for 4+ distinct elements
            resistedElements = listOf(
                Element.FIRE, Element.WATER, Element.EARTH, Element.AIR,
                Element.ICE, Element.LIGHTNING, Element.DARK, Element.HOLY, Element.PHYSICAL
            ),
            damageResistancePercent = 0.18f,
            allElementResistancePercent = 0.18f,
            bonusPerkDescription = "-18% Rezistence proti všem elementárním i fyzickým útokům."
        )
    )

    /**
     * Analyzes the active party members and identifies all triggered elemental synergies
     * based on their affinity elements.
     */
    fun calculateActiveSynergies(party: List<PartyMember>): List<ElementalSynergyBuff> {
        val memberElementPairs = party.filter { it.isAlive }.map { it.name to it.element }
        return evaluateSynergiesFromPairs(memberElementPairs)
    }

    /**
     * Evaluates active synergies from a list of (MemberName, Element) pairs.
     */
    fun evaluateSynergiesFromPairs(members: List<Pair<String, Element>>): List<ElementalSynergyBuff> {
        val activeBuffs = mutableListOf<ElementalSynergyBuff>()
        val presentElements = members.map { it.second }.toSet()

        // 1. Check pairwise complementary synergies
        DEFINED_SYNERGIES.filter { it.id != "elemental_pantheon" }.forEach { def ->
            val hasAll = def.requiredElements.all { req -> presentElements.contains(req) }
            if (hasAll) {
                val participantNames = members
                    .filter { def.requiredElements.contains(it.second) }
                    .map { it.first }
                    .distinct()

                activeBuffs.add(
                    def.copy(participatingMemberNames = participantNames)
                )
            }
        }

        // 2. Check Elemental Pantheon Harmony (4 or more distinct elements)
        if (presentElements.size >= 4) {
            val pantheonDef = DEFINED_SYNERGIES.first { it.id == "elemental_pantheon" }
            activeBuffs.add(
                pantheonDef.copy(participatingMemberNames = members.map { it.first }.distinct())
            )
        }

        return activeBuffs
    }

    /**
     * Computes the total passive damage mitigation against an incoming enemy attack element.
     * Stacks complementary synergies and caps at 50% max reduction for combat balance.
     */
    fun calculateDamageMitigation(
        incomingDamage: Int,
        attackerElement: Element,
        activeSynergies: List<ElementalSynergyBuff>
    ): SynergyMitigationResult {
        if (activeSynergies.isEmpty() || incomingDamage <= 0) {
            return SynergyMitigationResult(
                totalMitigationRatio = 0f,
                mitigatedDamageAmount = 0,
                finalDamageAfterSynergy = incomingDamage,
                contributingSynergies = emptyList(),
                explanationText = "Žádná elementární synergie neposkytuje rezistenci."
            )
        }

        val contributing = activeSynergies.filter { it.protectsAgainst(attackerElement) }
        if (contributing.isEmpty()) {
            return SynergyMitigationResult(
                totalMitigationRatio = 0f,
                mitigatedDamageAmount = 0,
                finalDamageAfterSynergy = incomingDamage,
                contributingSynergies = emptyList(),
                explanationText = "Aktivní synergie nepokrývají element útočníka (${attackerElement.name})."
            )
        }

        // Aggregate mitigation ratios with diminishing returns, capped at 50%
        var rawMitigation = 0f
        contributing.forEach { buff ->
            val ratio = buff.getMitigationRatio(attackerElement)
            // Multiplicative stacking: (1 - existing) * ratio
            rawMitigation += (1.0f - rawMitigation) * ratio
        }

        val totalMitigationRatio = rawMitigation.coerceIn(0f, 0.50f)
        val mitigatedAmount = (incomingDamage * totalMitigationRatio).toInt()
        val finalDamage = (incomingDamage - mitigatedAmount).coerceAtLeast(1)

        val explanation = buildString {
            append("🛡️ Elementární rezistence: -${(totalMitigationRatio * 100).toInt()}% (-$mitigatedAmount DMG). ")
            append("Aktivní štíty: ${contributing.joinToString { it.name }}")
        }

        return SynergyMitigationResult(
            totalMitigationRatio = totalMitigationRatio,
            mitigatedDamageAmount = mitigatedAmount,
            finalDamageAfterSynergy = finalDamage,
            contributingSynergies = contributing,
            explanationText = explanation
        )
    }
}
