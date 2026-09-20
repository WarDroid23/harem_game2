package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Types of environmental hazards in battle scenarios.
 */
enum class HazardType {
    LAVA_ERUPTION,      // Fire dmg + BURN
    TOXIC_MIASMA,       // Poison DoT + POISON
    FROSTBITE_TEMPEST,  // Ice dmg + FREEZE / SLOW
    THUNDER_SURGE,      // Lightning dmg + SHOCK
    SHADOW_ABYSS,       // Dark dmg + BLEED / CURSE
    HOLY_RADIANCE       // Holy restorative aura + REGEN / BLESSING
}

/**
 * Target selection rule for periodic hazard triggers.
 */
enum class HazardTargetRule {
    RANDOM_ANY_COMBATANT, // Hits any random unit on the battlefield (friend or foe)
    RANDOM_ENEMY_ONLY,
    RANDOM_ALLY_ONLY,
    ALL_COMBATANTS,
    LOWEST_HP_COMBATANT
}

/**
 * Environmental hazard and terrain modifier definition.
 */
@Serializable
data class EnvironmentalHazard(
    val id: String,
    val name: String,
    val icon: String,
    val title: String,
    val hazardType: HazardType,
    val description: String,
    val terrainModifierDesc: String,
    val dangerLevel: String = "Vysoké riziko", // "Střední", "Vysoké riziko", "Extrémní nebezpečí"
    val triggerIntervalTurns: Int = 2, // Periodic trigger interval in turns/rounds
    val baseDamage: Int = 18,
    val statusEffectToApply: CombatStatusEffect? = null,
    val targetRule: HazardTargetRule = HazardTargetRule.RANDOM_ANY_COMBATANT,
    val themeColorHex: Long = 0xFFD84315,
    val element: Element = Element.FIRE,
    val particleEffect: String = "FIRE_EMBERS"
) {
    companion object {
        /**
         * Resolves the appropriate environmental hazard based on location and encounter context.
         */
        fun getHazardForLocation(location: String): EnvironmentalHazard {
            return when {
                location.contains("Sopk", ignoreCase = true) || location.contains("Láv", ignoreCase = true) || location.contains("Kráter", ignoreCase = true) || location.contains("Pevnost", ignoreCase = true) ->
                    createLavaFissureHazard()
                location.contains("Močál", ignoreCase = true) || location.contains("Bažin", ignoreCase = true) || location.contains("Hvozd", ignoreCase = true) || location.contains("Džungl", ignoreCase = true) ->
                    createToxicSwampHazard()
                location.contains("Led", ignoreCase = true) || location.contains("Hor", ignoreCase = true) || location.contains("Sever", ignoreCase = true) || location.contains("Krypt", ignoreCase = true) ->
                    createFrostbiteGlacierHazard()
                location.contains("Bouř", ignoreCase = true) || location.contains("Věž", ignoreCase = true) || location.contains("Blesk", ignoreCase = true) ->
                    createThunderSurgeHazard()
                location.contains("Chrám", ignoreCase = true) || location.contains("Svatyn", ignoreCase = true) || location.contains("Oltář", ignoreCase = true) ->
                    createHolySanctuaryHazard()
                else ->
                    createShadowAbyssHazard()
            }
        }

        fun createLavaFissureHazard(): EnvironmentalHazard = EnvironmentalHazard(
            id = "hazard_lava",
            name = "Sopečné trhliny",
            icon = "🌋",
            title = "Roztavené lávové gejzíry",
            hazardType = HazardType.LAVA_ERUPTION,
            description = "Země puká a chrlí proudy roztaveného magmatu. Periodicky exploduje pod náhodným bojovníkem na bojišti.",
            terrainModifierDesc = "+20% k Ohnivému poškození, -10% Obrana všech jednotek",
            dangerLevel = "Extrémní nebezpečí",
            triggerIntervalTurns = 2,
            baseDamage = 22,
            statusEffectToApply = CombatStatusEffect(
                id = "env_burn",
                name = "Zapálení magmatem",
                icon = "🔥",
                type = "BURN",
                value = 12,
                durationTurns = 2,
                maxDuration = 2,
                description = "Tělo hoří lávovým žárem! Na začátku každého kola způsobuje 12 poškození.",
                element = Element.FIRE
            ),
            targetRule = HazardTargetRule.RANDOM_ANY_COMBATANT,
            themeColorHex = 0xFFFF3D00,
            element = Element.FIRE,
            particleEffect = "FIRE_EMBERS"
        )

        fun createToxicSwampHazard(): EnvironmentalHazard = EnvironmentalHazard(
            id = "hazard_poison",
            name = "Toxická mlha",
            icon = "🧪",
            title = "Miasma hniloby a jedu",
            hazardType = HazardType.TOXIC_MIASMA,
            description = "Husté jedovaté plyny stoupají z bažin. Periodicky infikují náhodného bojovníka silným jedem.",
            terrainModifierDesc = "+25% Trvání debuffů, snižuje regeneraci o 20%",
            dangerLevel = "Vysoké riziko",
            triggerIntervalTurns = 2,
            baseDamage = 16,
            statusEffectToApply = CombatStatusEffect(
                id = "env_poison",
                name = "Toxická nákaza",
                icon = "☠️",
                type = "POISON",
                value = 14,
                durationTurns = 3,
                maxDuration = 3,
                description = "Zákeřný jed koluje v tepnách. Způsobuje 14 poškození každé kolo.",
                element = Element.DARK
            ),
            targetRule = HazardTargetRule.RANDOM_ANY_COMBATANT,
            themeColorHex = 0xFF76FF03,
            element = Element.DARK,
            particleEffect = "TOXIC_SPORES"
        )

        fun createFrostbiteGlacierHazard(): EnvironmentalHazard = EnvironmentalHazard(
            id = "hazard_frost",
            name = "Ledové rampouchy",
            icon = "❄️",
            title = "Mrazivá ledovcová bouře",
            hazardType = HazardType.FROSTBITE_TEMPEST,
            description = "Ostré ledové krystaly a mrazivý vichr padají ze skal. Mohou zmrazit zasaženou oběť.",
            terrainModifierDesc = "-15% Rychlost všech jednotek, +10% šance na odražení úderu",
            dangerLevel = "Střední riziko",
            triggerIntervalTurns = 3,
            baseDamage = 18,
            statusEffectToApply = CombatStatusEffect(
                id = "env_freeze",
                name = "Omrznutí ledem",
                icon = "🧊",
                type = "FREEZE",
                value = 8,
                durationTurns = 2,
                maxDuration = 2,
                description = "Tělo pokryté ledovou krustou. Zpomaluje akce a blokuje obranné krytí.",
                element = Element.ICE
            ),
            targetRule = HazardTargetRule.RANDOM_ANY_COMBATANT,
            themeColorHex = 0xFF00E5FF,
            element = Element.ICE,
            particleEffect = "FROST_SHARDS"
        )

        fun createThunderSurgeHazard(): EnvironmentalHazard = EnvironmentalHazard(
            id = "hazard_lightning",
            name = "Bleskové anomálie",
            icon = "⚡",
            title = "Bouřkové statické výboje",
            hazardType = HazardType.THUNDER_SURGE,
            description = "Silné elektrické napětí v atmosféře arény vyvolává náhodné bleskové údery.",
            terrainModifierDesc = "+20% Šance na kritický úder, blesky odčerpávají manu",
            dangerLevel = "Extrémní nebezpečí",
            triggerIntervalTurns = 2,
            baseDamage = 26,
            statusEffectToApply = CombatStatusEffect(
                id = "env_shock",
                name = "Elektrický šok",
                icon = "⚡",
                type = "SHOCK",
                value = 16,
                durationTurns = 2,
                maxDuration = 2,
                description = "Vysoké napětí v těle. Každé kolo odčerpá 16 bodů many nebo způsobí zranění.",
                element = Element.LIGHTNING
            ),
            targetRule = HazardTargetRule.RANDOM_ANY_COMBATANT,
            themeColorHex = 0xFFFFEA00,
            element = Element.LIGHTNING,
            particleEffect = "LIGHTNING_SPARKS"
        )

        fun createShadowAbyssHazard(): EnvironmentalHazard = EnvironmentalHazard(
            id = "hazard_shadow",
            name = "Stínové propasti",
            icon = "👁️",
            title = "Přízračné duše temnoty",
            hazardType = HazardType.SHADOW_ABYSS,
            description = "Temné stíny požírají životní sílu náhodných bojovníků a způsobují krvácení.",
            terrainModifierDesc = "+20% Síla Temné magie, léčení spojenců je ztíženo",
            dangerLevel = "Vysoké riziko",
            triggerIntervalTurns = 2,
            baseDamage = 20,
            statusEffectToApply = CombatStatusEffect(
                id = "env_bleed",
                name = "Stínové drápy",
                icon = "🩸",
                type = "BLEED",
                value = 14,
                durationTurns = 2,
                maxDuration = 2,
                description = "Tržné rány způsobené stíny. Ztráta 14 HP na začátku každého kola.",
                element = Element.DARK
            ),
            targetRule = HazardTargetRule.RANDOM_ANY_COMBATANT,
            themeColorHex = 0xFFD500F9,
            element = Element.DARK,
            particleEffect = "SHADOW_VOID"
        )

        fun createHolySanctuaryHazard(): EnvironmentalHazard = EnvironmentalHazard(
            id = "hazard_holy",
            name = "Posvátná svatyně",
            icon = "✨",
            title = "Aura nebeského oltáře",
            hazardType = HazardType.HOLY_RADIANCE,
            description = "Prastaré světelné runy periodicky sesílají regenerační auru na spojence!",
            terrainModifierDesc = "+25% Síla léčivých kouzel a posvátných štítů",
            dangerLevel = "Příznivý terén",
            triggerIntervalTurns = 2,
            baseDamage = 0,
            statusEffectToApply = CombatStatusEffect(
                id = "env_regen",
                name = "Nebeská záře",
                icon = "✨",
                type = "REGEN",
                value = 22,
                durationTurns = 2,
                maxDuration = 2,
                description = "Posvátná aura obnovuje 22 HP na začátku každého kola.",
                element = Element.HOLY
            ),
            targetRule = HazardTargetRule.RANDOM_ALLY_ONLY,
            themeColorHex = 0xFFFFD700,
            element = Element.HOLY,
            particleEffect = "HOLY_SPARKLES"
        )
    }
}
