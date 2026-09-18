package com.example.haremdark.models

import kotlinx.serialization.Serializable

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
    val isDiscovered: Boolean = false
)
