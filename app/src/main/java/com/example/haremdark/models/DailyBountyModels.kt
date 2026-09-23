package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyBounty(
    val id: String,
    val title: String,
    val description: String,
    val targetEnemyArchetype: String,
    val requiredElement: String,
    val rewardMaterialId: String,
    val rewardAmount: Int,
    val isCompleted: Boolean = false
)
