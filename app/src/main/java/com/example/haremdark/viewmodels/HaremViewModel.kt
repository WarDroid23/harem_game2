package com.example.haremdark.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HaremViewModel(private val engine: GameEngine) : ViewModel() {

    val selectedHaremTab = MutableStateFlow(0)
    val selectedFilter = MutableStateFlow(0)
    val selectedSort = MutableStateFlow("Náklonnost")
    val searchQuery = MutableStateFlow("")

    val selectedCharacterForProfile = MutableStateFlow<Character?>(null)
    val selectedCharacterForInteraction = MutableStateFlow<Character?>(null)

    val haremMembers: StateFlow<List<Character>> = engine.gameState
        .map { it.characters }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = engine.gameState.value.characters
        )

    val filteredList: StateFlow<List<Character>> = combine(
        engine.gameState,
        selectedFilter,
        searchQuery,
        selectedSort
    ) { gameState, filter, query, sort ->
        var list = when (filter) {
            1 -> gameState.characters.filter { it.oblibena }
            2 -> gameState.characters.filter { it.jeManzelkou || it.partnerka }
            3 -> gameState.characters.filter { it.naNajmu }
            4 -> gameState.characters.filter { it.tehotna }
            else -> gameState.characters
        }

        if (query.isNotBlank()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) || it.archetypeId.contains(query, ignoreCase = true) }
        }

        when (sort) {
            "Náklonnost" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.affinityPoints })
            "Rarita" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.rarity })
            "Nedávno" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.lastInteractionDay })
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun selectTab(index: Int) {
        selectedHaremTab.value = index
    }
    
    fun setFilter(filter: Int) {
        selectedFilter.value = filter
    }
    
    fun setSort(sort: String) {
        selectedSort.value = sort
    }
    
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
    
    fun openProfile(character: Character?) {
        selectedCharacterForProfile.value = character
    }
    
    fun openInteraction(character: Character?) {
        selectedCharacterForInteraction.value = character
    }
}
