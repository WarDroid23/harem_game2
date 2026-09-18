package com.example.haremdark.models

import kotlinx.serialization.Serializable

/**
 * Data class for tracking influence impacts from narrative choices.
 */
@Serializable
data class InfluenceLogEntry(
    val day: Int,
    val characterId: String,
    val characterName: String,
    val choiceDescription: String,
    val influenceChange: Int,
    val reason: String
)
