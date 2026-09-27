package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Data model for a dangerous zone available for idle expeditions.
 */
@Serializable
data class IdleExpeditionZone(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val dangerLevel: Int, // 1 to 5
    val durationMinutes: Int, // e.g., 30, 120, 240, 480, 720
    val recommendedPower: Int,
    val requiredMembersCount: Int = 2,
    val baseGoldReward: Int,
    val baseDarkEnergyReward: Int,
    val baseHpReward: Int = 100,
    val possibleMaterials: List<String> = emptyList() // material ids: "temny_strep", "mana_esence", "draci_krev", "mesicni_prach", "krystal", "zelezna_ruda", "drevohorec"
)

/**
 * Saved state of an active expedition in progress or waiting to be claimed.
 */
@Serializable
data class ActiveIdleExpedition(
    val id: String,
    val zoneId: String,
    val assignedCharacterIds: List<String>,
    val startTimeMillis: Long,
    val durationMillis: Long,
    val isClaimed: Boolean = false
)

/**
 * Report generated upon completion of an expedition containing all gathered loot.
 */
@Serializable
data class IdleExpeditionReport(
    val id: String,
    val zoneId: String,
    val zoneName: String,
    val zoneIcon: String,
    val assignedCharacterNames: List<String>,
    val goldGained: Int,
    val darkEnergyGained: Int,
    val xpGainedPerMember: Int,
    val materialsGained: Map<String, Int> = emptyMap(),
    val itemGained: InventoryItem? = null,
    val successRatePercent: Int = 100,
    val flavorLog: String = "",
    val completedTimestamp: Long = System.currentTimeMillis()
)

object IdleExpeditionCatalog {
    val ZONES = listOf(
        IdleExpeditionZone(
            id = "temne_katakomby",
            name = "Temné katakomby zapomnění",
            icon = "💀",
            description = "Podzemní labyrint plný starobylých hrobek a zapomenutých cenností.",
            dangerLevel = 1,
            durationMinutes = 30,
            recommendedPower = 120,
            requiredMembersCount = 1,
            baseGoldReward = 250,
            baseDarkEnergyReward = 40,
            baseHpReward = 150,
            possibleMaterials = listOf("zelezna_ruda", "drevohorec", "temny_strep")
        ),
        IdleExpeditionZone(
            id = "ruiny_rozkose",
            name = "Ruiny Chrámu Rozkoše",
            icon = "🏰",
            description = "Svatyně stínů, kde pramení magická esence a leží vzácné lektvary.",
            dangerLevel = 2,
            durationMinutes = 120,
            recommendedPower = 220,
            requiredMembersCount = 2,
            baseGoldReward = 500,
            baseDarkEnergyReward = 80,
            baseHpReward = 250,
            possibleMaterials = listOf("temny_strep", "mana_esence", "mesicni_prach")
        ),
        IdleExpeditionZone(
            id = "krvavy_hvozd",
            name = "Krvavý hvozd prokletých",
            icon = "🩸",
            description = "Zapovězený les prosáklý temnotou. Zde rostou vzácné ingredience pro alchymii.",
            dangerLevel = 3,
            durationMinutes = 240,
            recommendedPower = 380,
            requiredMembersCount = 2,
            baseGoldReward = 900,
            baseDarkEnergyReward = 150,
            baseHpReward = 400,
            possibleMaterials = listOf("mana_esence", "mesicni_prach", "draci_krev", "krystal")
        ),
        IdleExpeditionZone(
            id = "mysticka_rokle",
            name = "Mystická rokle astrálních bouří",
            icon = "🔮",
            description = "Místo vysoké koncentrace magické energie a krystalů z prázdnoty.",
            dangerLevel = 4,
            durationMinutes = 480,
            recommendedPower = 550,
            requiredMembersCount = 3,
            baseGoldReward = 1600,
            baseDarkEnergyReward = 280,
            baseHpReward = 700,
            possibleMaterials = listOf("temny_strep", "mana_esence", "mesicni_prach", "krystal", "draci_krev")
        ),
        IdleExpeditionZone(
            id = "draci_sluj",
            name = "Dračí sluj Temné Císařovny",
            icon = "🐉",
            description = "Nejnebezpečnější teritórium dominia překypující dračí krví a legendárními poklady.",
            dangerLevel = 5,
            durationMinutes = 720,
            recommendedPower = 850,
            requiredMembersCount = 3,
            baseGoldReward = 3000,
            baseDarkEnergyReward = 500,
            baseHpReward = 1200,
            possibleMaterials = listOf("draci_krev", "krystal", "mesicni_prach", "temny_strep")
        )
    )

    fun getZoneById(id: String): IdleExpeditionZone? = ZONES.firstOrNull { it.id == id }
}
