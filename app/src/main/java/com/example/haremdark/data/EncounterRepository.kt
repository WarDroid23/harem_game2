package com.example.haremdark.data

import com.example.haremdark.models.EncounterCard
import com.example.haremdark.models.EncounterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class EncounterRepository {
    private val _deck = MutableStateFlow<List<EncounterCard>>(listOf(
        EncounterCard("1", "Záhadný cizinec", "Cizinec ti nabízí informaci o šlechtici. Riskneš zdraví?", EncounterType.STORY, healthChange = -10, influenceChange = 15),
        EncounterCard("2", "Přepadení v temnotě", "Zbojníci tě přepadli! Musíš bojovat.", EncounterType.COMBAT, healthChange = -20, influenceChange = 5),
        EncounterCard("3", "Veřejná slavnost", "Tvůj vliv roste díky tvé účasti na slavnosti.", EncounterType.EVENT, healthChange = 5, influenceChange = 20)
    ))
    val deck = _deck.asStateFlow()

    fun removeCard(card: EncounterCard) {
        _deck.value = _deck.value.filter { it.id != card.id }
    }
}
