package com.example.haremdark.models

import androidx.compose.ui.graphics.Color

/**
 * Model representing an elemental affinity entry in the Elemental Codex.
 * Stores comprehensive lore, historical origin, combat characteristics,
 * and discovery metadata.
 */
data class ElementalCodexEntry(
    val element: Element,
    val id: String,
    val name: String,
    val epithet: String,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val historicalOrigin: String,
    val trainingLore: String,
    val tacticalNature: String,
    val ancientArchon: String,
    val sacredRelic: String,
    val statusEffectName: String,
    val resonanceBlessing: String,
    val unlockHint: String = "Zahaj a dokonči elementární trénink s libovolnou hrdinkou v Komnatách afinity."
)
