package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class CraftingMaterial(
    val id: String,
    val name: String,
    var count: Int = 0
)

@Serializable
data class CraftingRecipe(
    val id: String,
    val resultItemId: String,
    val materialsRequired: Map<String, Int>, // MaterialId to amount
    val goldCost: Int
)
