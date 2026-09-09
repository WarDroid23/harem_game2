package com.example.haremdark.data

import com.example.haremdark.models.Character

data class NarrativeEvent(
    val characterId: String,
    val characterName: String,
    val affinityLevel: Int,
    val title: String,
    val description: String,
    val rewardText: String
)

object AffinityEventData {
    fun getEventFor(character: Character, newLevel: Int): NarrativeEvent? {
        val title = "Pouto prohloubeno - Úroveň $newLevel"
        val (desc, reward) = when (newLevel) {
            2 -> "Led kolem jejího srdce taje. Získáváš její důvěru." to "+5 Krystalů Temnoty"
            3 -> "Její tělo a mysl se ti začínají odevzdávat. Objevuješ její tajemství." to "+10 Krystalů Temnoty, +5% Útok v boji"
            4 -> "Její oddanost hraničí s fanatismem. Bude tě následovat do pekla." to "Odblokována nová bojová schopnost"
            5 -> "Jste propojeni temnou magií. Její srdce bije jen pro tebe." to "Získána Ultimátní Věrnost, Exkluzivní zbraň"
            else -> return null
        }
        
        return NarrativeEvent(
            characterId = character.id,
            characterName = character.name,
            affinityLevel = newLevel,
            title = title,
            description = desc,
            rewardText = reward
        )
    }
}
