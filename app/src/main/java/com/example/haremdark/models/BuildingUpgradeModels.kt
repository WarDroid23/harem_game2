package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class BuildingUpgrade(
    val level: Int,
    val costGold: Int,
    val costWood: Int,
    val costStone: Int,
    val effectDescription: String
)

@Serializable
data class BuildingDefinition(
    val type: String,
    val name: String,
    val description: String,
    val maxLevel: Int,
    val upgrades: List<BuildingUpgrade>
)

object BuildingCatalog {
    val definitions = listOf(
        BuildingDefinition(
            type = "barracks",
            name = "Kasárny",
            description = "Zvyšuje bojové statistiky harémových členek.",
            maxLevel = 5,
            upgrades = listOf(
                BuildingUpgrade(1, 200, 100, 50, "+5% Síla"),
                BuildingUpgrade(2, 400, 200, 100, "+10% Síla, +5% Obrana"),
                BuildingUpgrade(3, 800, 400, 200, "+15% Síla, +10% Obrana"),
                BuildingUpgrade(4, 1600, 800, 400, "+20% Síla, +15% Obrana"),
                BuildingUpgrade(5, 3200, 1600, 800, "+25% Síla, +20% Obrana")
            )
        ),
        BuildingDefinition(
            type = "mine",
            name = "Železné doly",
            description = "Zvyšuje produkci železa a zlata.",
            maxLevel = 5,
            upgrades = listOf(
                BuildingUpgrade(1, 150, 50, 20, "+10% Produkce železa"),
                BuildingUpgrade(2, 300, 100, 40, "+20% Produkce železa, +5% Zlato"),
                BuildingUpgrade(3, 600, 200, 80, "+30% Produkce železa, +10% Zlato"),
                BuildingUpgrade(4, 1200, 400, 160, "+40% Produkce železa, +15% Zlato"),
                BuildingUpgrade(5, 2400, 800, 320, "+50% Produkce železa, +20% Zlato")
            )
        ),
        BuildingDefinition(
            type = "training_hall",
            name = "Výcviková hala",
            description = "Zvyšuje rychlost získávání XP pro harém.",
            maxLevel = 5,
            upgrades = listOf(
                BuildingUpgrade(1, 200, 100, 50, "+10% XP zisk"),
                BuildingUpgrade(2, 400, 200, 100, "+20% XP zisk"),
                BuildingUpgrade(3, 800, 400, 200, "+30% XP zisk"),
                BuildingUpgrade(4, 1600, 800, 400, "+40% XP zisk"),
                BuildingUpgrade(5, 3200, 1600, 800, "+50% XP zisk")
            )
        )
    )
}
