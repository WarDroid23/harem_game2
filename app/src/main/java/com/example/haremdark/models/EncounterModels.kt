package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class EncounterType { STORY, COMBAT, EVENT }

@Serializable
data class EncounterCard(
    val id: String,
    val title: String,
    val description: String,
    val type: EncounterType,
    val healthChange: Int = 0,
    val influenceChange: Int = 0,
    val icon: String = "🃏"
)
