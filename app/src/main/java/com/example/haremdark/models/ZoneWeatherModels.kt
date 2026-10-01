package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Atmospheric and elemental weather condition for world zones and expeditions.
 */
@Serializable
data class ZoneWeatherCondition(
    val id: String,
    val name: String,
    val icon: String,
    val title: String,
    val description: String,
    val dominantElement: String,
    val goldMultiplier: Float = 1.0f,
    val darkEnergyMultiplier: Float = 1.0f,
    val xpMultiplier: Float = 1.0f,
    val materialsBonusPercent: Int = 0,
    val boostedMaterials: List<String> = emptyList(),
    val dangerModifier: Int = 0,
    val successRateBonus: Int = 0,
    val combatStatBuff: String = "",
    val particleType: String = "none",
    val primaryColorHex: Long = 0xFF9C27B0,
    val secondaryColorHex: Long = 0xFFBA68C8
)

/**
 * Result calculating full resource yield, multipliers and efficiency recommendations.
 */
data class ExpeditionEfficiencyResult(
    val efficiencyScore: Int, // e.g. 70 to 180%
    val efficiencyTier: String, // "Suboptimální", "Dobrá", "Vynikající", "Maximální"
    val goldYield: Int,
    val darkEnergyYield: Int,
    val materialsBonusText: String,
    val synergyMultiplier: Float,
    val weatherBoostSummary: String,
    val recommendedArchetypes: List<String>
)

object ZoneWeatherCatalog {
    val ARCANE_STORM = ZoneWeatherCondition(
        id = "arcane_storm",
        name = "Bouře temné many",
        icon = "⚡",
        title = "Magická bouře stínové many",
        description = "Blesky čisté temné energie bičují terén. Zvyšují zisk magické esence a temné energie na maximum.",
        dominantElement = "Dark/Lightning",
        goldMultiplier = 1.1f,
        darkEnergyMultiplier = 1.45f,
        xpMultiplier = 1.2f,
        materialsBonusPercent = 35,
        boostedMaterials = listOf("mana_esence", "temny_strep"),
        dangerModifier = 1,
        successRateBonus = 5,
        combatStatBuff = "+25% Magické poškození, -10% Obrana",
        particleType = "lightning",
        primaryColorHex = 0xFF7C4DFF,
        secondaryColorHex = 0xFFE040FB
    )

    val BLOOD_RAIN = ZoneWeatherCondition(
        id = "blood_rain",
        name = "Krvavý déšť",
        icon = "🩸",
        title = "Krvavé srážky vampýrů",
        description = "Karmínový déšť probouzí divokou touhu a zvyšuje výskyt dračí krve a bojových esencí.",
        dominantElement = "Blood/Fire",
        goldMultiplier = 1.15f,
        darkEnergyMultiplier = 1.25f,
        xpMultiplier = 1.4f,
        materialsBonusPercent = 40,
        boostedMaterials = listOf("draci_krev", "temny_strep"),
        dangerModifier = 2,
        successRateBonus = -5,
        combatStatBuff = "+15% Šance na kritický úder, +20% Útok",
        particleType = "blood_rain",
        primaryColorHex = 0xFFD50000,
        secondaryColorHex = 0xFFFF1744
    )

    val FROST_BLIZZARD = ZoneWeatherCondition(
        id = "frost_blizzard",
        name = "Mrazivá vánice",
        icon = "❄️",
        title = "Arktická ledová vánice",
        description = "Ledový vichr odkrývá žíly krystalů a pevné železné rudy v zamrzlých skalách.",
        dominantElement = "Ice/Water",
        goldMultiplier = 1.2f,
        darkEnergyMultiplier = 1.1f,
        xpMultiplier = 1.15f,
        materialsBonusPercent = 35,
        boostedMaterials = listOf("zelezna_ruda", "krystal"),
        dangerModifier = 0,
        successRateBonus = 10,
        combatStatBuff = "+25% Síla štítů, -10% Rychlost",
        particleType = "snow",
        primaryColorHex = 0xFF00B0FF,
        secondaryColorHex = 0xFF80D8FF
    )

    val SHADOW_MIST = ZoneWeatherCondition(
        id = "shadow_mist",
        name = "Mlha zapomnění",
        icon = "🌫️",
        title = "Hustá stínová mlha",
        description = "Mlha ukrývá expedici před dravci a umožňuje nenápadný sběr dřevohorce a temných střepů.",
        dominantElement = "Shadow/Wind",
        goldMultiplier = 1.25f,
        darkEnergyMultiplier = 1.3f,
        xpMultiplier = 1.1f,
        materialsBonusPercent = 45,
        boostedMaterials = listOf("temny_strep", "drevohorec"),
        dangerModifier = -1,
        successRateBonus = 15,
        combatStatBuff = "+20% Uhýbání, Skrytí před hlídkami",
        particleType = "fog",
        primaryColorHex = 0xFF78909C,
        secondaryColorHex = 0xFFB0BEC5
    )

    val SOLAR_RADIANCE = ZoneWeatherCondition(
        id = "solar_radiance",
        name = "Sluneční žár",
        icon = "☀️",
        title = "Zlaté sluneční paprsky",
        description = "Vysoká viditelnost a prohřátá půda umožňují vytěžit maximální množství zlata a vzácného dřeva.",
        dominantElement = "Light/Holy",
        goldMultiplier = 1.5f,
        darkEnergyMultiplier = 0.9f,
        xpMultiplier = 1.25f,
        materialsBonusPercent = 30,
        boostedMaterials = listOf("drevohorec", "zelezna_ruda"),
        dangerModifier = 0,
        successRateBonus = 10,
        combatStatBuff = "+15% Zlato, +10% Morálka",
        particleType = "sunbeams",
        primaryColorHex = 0xFFFFAB00,
        secondaryColorHex = 0xFFFFD700
    )

    val ASTRAL_CONJUNCTION = ZoneWeatherCondition(
        id = "astral_conjunction",
        name = "Astrální konjunkce",
        icon = "🌌",
        title = "Zarovnání sfér a hvězd",
        description = "Vzácný vesmírný úkaz otevírá trhliny s hojným výskytem všech vzácných crafting surovin.",
        dominantElement = "Cosmic/Astral",
        goldMultiplier = 1.4f,
        darkEnergyMultiplier = 1.5f,
        xpMultiplier = 1.5f,
        materialsBonusPercent = 60,
        boostedMaterials = listOf("mesicni_prach", "krystal", "draci_krev", "mana_esence"),
        dangerModifier = 1,
        successRateBonus = 20,
        combatStatBuff = "+30% Všechny parametry & Vzácný loot",
        particleType = "astral_sparks",
        primaryColorHex = 0xFFAA00FF,
        secondaryColorHex = 0xFFEA80FC
    )

    val MOONLIGHT_SERENITY = ZoneWeatherCondition(
        id = "moonlight_serenity",
        name = "Měsíční klid",
        icon = "🌙",
        title = "Třpytivý měsíční svit",
        description = "Klidná noc pod stříbrným měsícem s vysokou koncentrací měsíčního prachu a regenerací.",
        dominantElement = "Moon/Spirit",
        goldMultiplier = 1.15f,
        darkEnergyMultiplier = 1.35f,
        xpMultiplier = 1.2f,
        materialsBonusPercent = 35,
        boostedMaterials = listOf("mesicni_prach", "mana_esence"),
        dangerModifier = -1,
        successRateBonus = 15,
        combatStatBuff = "+15% Regenerace HP, +15% Loajalita",
        particleType = "moon_glow",
        primaryColorHex = 0xFF3F51B5,
        secondaryColorHex = 0xFF7986CB
    )

    val ALL_CONDITIONS = listOf(
        ARCANE_STORM,
        BLOOD_RAIN,
        FROST_BLIZZARD,
        SHADOW_MIST,
        SOLAR_RADIANCE,
        ASTRAL_CONJUNCTION,
        MOONLIGHT_SERENITY
    )
}

object ZoneWeatherSystem {
    // 30 minutes cycle per weather change
    private const val CYCLE_DURATION_MS = 30 * 60 * 1000L

    /**
     * Calculates the deterministic weather for an idle expedition zone at a given timestamp.
     */
    fun getWeatherForZone(zoneId: String, timestamp: Long = System.currentTimeMillis()): ZoneWeatherCondition {
        val cycleIndex = (timestamp / CYCLE_DURATION_MS).toInt()
        val seed = zoneId.hashCode() + cycleIndex
        val weatherList = ZoneWeatherCatalog.ALL_CONDITIONS
        val index = kotlin.math.abs(seed) % weatherList.size
        return weatherList[index]
    }

    /**
     * Calculates the deterministic weather for a world map domain.
     */
    fun getWeatherForDomain(domainId: String, timestamp: Long = System.currentTimeMillis()): ZoneWeatherCondition {
        val cycleIndex = (timestamp / CYCLE_DURATION_MS).toInt()
        val seed = domainId.hashCode() + (cycleIndex * 7)
        val weatherList = ZoneWeatherCatalog.ALL_CONDITIONS
        val index = kotlin.math.abs(seed) % weatherList.size
        return weatherList[index]
    }

    /**
     * Calculates remaining time in milliseconds until current weather shifts.
     */
    fun getTimeUntilWeatherChange(timestamp: Long = System.currentTimeMillis()): Long {
        val elapsedInCycle = timestamp % CYCLE_DURATION_MS
        return (CYCLE_DURATION_MS - elapsedInCycle).coerceAtLeast(0L)
    }

    /**
     * Formats remaining time (e.g., "24m 15s")
     */
    fun formatRemainingTime(millis: Long): String {
        val minutes = (millis / (1000 * 60)) % 60
        val seconds = (millis / 1000) % 60
        return "${minutes}m ${seconds}s"
    }

    /**
     * Calculates forecast for the next upcoming cycles.
     */
    fun getForecastForZone(zoneId: String, count: Int = 3): List<Pair<Long, ZoneWeatherCondition>> {
        val now = System.currentTimeMillis()
        val currentCycleStart = now - (now % CYCLE_DURATION_MS)
        return (1..count).map { offset ->
            val futureTime = currentCycleStart + (offset * CYCLE_DURATION_MS)
            Pair(futureTime, getWeatherForZone(zoneId, futureTime))
        }
    }

    /**
     * Calculates efficiency analysis for expedition zone planning.
     */
    fun calculateExpeditionEfficiency(
        zone: IdleExpeditionZone,
        weather: ZoneWeatherCondition,
        assignedCharacters: List<Character>
    ): ExpeditionEfficiencyResult {
        var baseScore = 100

        // Weather multiplier boosts
        if (weather.goldMultiplier > 1.0f) baseScore += ((weather.goldMultiplier - 1.0f) * 40).toInt()
        if (weather.darkEnergyMultiplier > 1.0f) baseScore += ((weather.darkEnergyMultiplier - 1.0f) * 50).toInt()
        baseScore += (weather.materialsBonusPercent / 3)

        // Matching materials with zone drops
        val boostedDrops = zone.possibleMaterials.filter { it in weather.boostedMaterials }
        if (boostedDrops.isNotEmpty()) {
            baseScore += boostedDrops.size * 15
        }

        // Squad combat power bonus
        val squadPower = assignedCharacters.fold(0) { acc, c ->
            acc + (c.skills["combat"] ?: 0) * 15 + c.affinityPoints * 2 + c.level * 10 + 50
        }
        val powerRatio = if (zone.recommendedPower > 0) squadPower.toFloat() / zone.recommendedPower else 1.0f
        if (powerRatio >= 1.5f) baseScore += 25
        else if (powerRatio >= 1.0f) baseScore += 10
        else baseScore -= 15

        val finalScore = baseScore.coerceIn(50, 200)

        val tier = when {
            finalScore >= 150 -> "Maximální (S+)"
            finalScore >= 125 -> "Vynikající (A)"
            finalScore >= 100 -> "Standardní (B)"
            else -> "Snížená (C)"
        }

        val calculatedGold = (zone.baseGoldReward * weather.goldMultiplier * (if (powerRatio > 1f) 1.15f else 1.0f)).toInt()
        val calculatedDarkEnergy = (zone.baseDarkEnergyReward * weather.darkEnergyMultiplier * (if (powerRatio > 1f) 1.15f else 1.0f)).toInt()

        val materialsBonusSummary = if (boostedDrops.isNotEmpty()) {
            "+${weather.materialsBonusPercent}% šance na ${boostedDrops.joinToString(", ")}"
        } else {
            "+${weather.materialsBonusPercent}% bonus k materiálům"
        }

        val recommended = when (weather.dominantElement) {
            "Dark/Lightning" -> listOf("Čarodějka stínů", "Temná elfka")
            "Blood/Fire" -> listOf("Drací princezna", "Vampýrka")
            "Ice/Water" -> listOf("Ledová rytířka", "Kněžka vody")
            "Shadow/Wind" -> listOf("Asasínka stínů", "Zlodějka")
            "Light/Holy" -> listOf("Svatá paladinka", "Bylinkářka")
            else -> listOf("Všechny bojovnice")
        }

        return ExpeditionEfficiencyResult(
            efficiencyScore = finalScore,
            efficiencyTier = tier,
            goldYield = calculatedGold,
            darkEnergyYield = calculatedDarkEnergy,
            materialsBonusText = materialsBonusSummary,
            synergyMultiplier = powerRatio,
            weatherBoostSummary = "${weather.icon} ${weather.name}: ${weather.combatStatBuff}",
            recommendedArchetypes = recommended
        )
    }
}
