package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class FormationRow {
    FRONT, MIDDLE, BACK
}

@Serializable
data class FormationSlot(
    val row: FormationRow,
    val characterId: String? = null
)
