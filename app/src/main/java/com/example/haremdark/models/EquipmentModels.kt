package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class EquipmentSlot {
    WEAPON, ARMOR, ACCESSORY
}

@Serializable
data class Equipment(
    val id: String,
    val name: String,
    val slot: EquipmentSlot,
    val statBonuses: Map<String, Int> // e.g., "strength" to 10
)
