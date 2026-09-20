package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class DropChance(
    val itemName: String,
    val probabilityPercent: Int,
    val itemType: String = "Surovina",
    val isRare: Boolean = false
)

/**
 * Data class for Bestiary entries.
 */
@Serializable
data class BestiaryEntry(
    val enemyId: String,
    val name: String,
    val icon: String,
    val title: String,
    val description: String,
    var encounterCount: Int = 0,
    val weaknesses: List<String> = emptyList(),
    val commonDrops: List<String> = emptyList(),
    val rareDrops: List<String> = emptyList(),
    val dropChances: List<DropChance> = emptyList(),
    val isDiscovered: Boolean = false,
    val hp: Int = 100,
    val attack: Int = 20,
    val defense: Int = 10,
    val speed: Int = 10,
    val element: String = "Fyzický",
    val isBoss: Boolean = false
)
