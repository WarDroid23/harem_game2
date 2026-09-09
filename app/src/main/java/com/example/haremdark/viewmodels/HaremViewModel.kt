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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HaremFilterCriteria(
    val status: String = "Všechny",
    val role: String = "Všechny",
    val affinityLevel: String = "Všechny"
)

class HaremViewModel(private val engine: GameEngine) : ViewModel() {
    val selectedHaremTab = MutableStateFlow(0)
    
    val filterCriteria = MutableStateFlow(HaremFilterCriteria())
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
        filterCriteria,
        searchQuery,
        selectedSort
    ) { gameState, criteria, query, sort ->
        
        var list = gameState.characters

        // 1. Status Filter
        list = when (criteria.status) {
            "Oblíbená" -> list.filter { it.oblibena }
            "Ve vztahu" -> list.filter { it.jeManzelkou || it.partnerka }
            "Na nájmu" -> list.filter { it.naNajmu }
            "Březí" -> list.filter { it.tehotna }
            else -> list
        }

        // 2. Role (Archetype Group) Filter
        list = when (criteria.role) {
            "Válečnice" -> list.filter { it.archetypeId in listOf("odvazna", "vzdorna", "krvava_subka", "zlomena") }
            "Mágyně" -> list.filter { it.archetypeId in listOf("touha", "nymfomanka", "posedla", "hysterialni") }
            "Intrikánky" -> list.filter { it.archetypeId in listOf("slechticna", "manipulativni", "chladna") }
            "Služky" -> list.filter { it.archetypeId in listOf("subka", "ustrasena", "ticha_panenka") }
            else -> list
        }

        // 3. Affinity Level Filter
        list = when (criteria.affinityLevel) {
            "Úroveň 1-2" -> list.filter { it.affinityLevel in 1..2 }
            "Úroveň 3-4" -> list.filter { it.affinityLevel in 3..4 }
            "Úroveň 5+" -> list.filter { it.affinityLevel >= 5 }
            else -> list
        }

        // 4. Search Query
        if (query.isNotBlank()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) || it.archetypeId.contains(query, ignoreCase = true) }
        }
        
        // 5. Sort
        when (sort) {
            "Náklonnost" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.affinityPoints })
            "Rarita / Úroveň" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.rarity }.thenByDescending { it.affinityLevel })
            "Nedávno" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.lastInteractionDay })
            "Role (Archetyp)" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenBy { it.archetypeId })
            "Bojová síla" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.hp + it.maxHp })
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
    
    fun setFilterCriteria(criteria: HaremFilterCriteria) {
        filterCriteria.value = criteria
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

    // --- Time-Limited Harem Event Triggers ---
    val activeTimeLimitedEvent = MutableStateFlow<com.example.haremdark.data.TimeLimitedHaremEvent?>(null)
    val eventRemainingSeconds = MutableStateFlow(0)
    val isEventDialogueOpen = MutableStateFlow(false)
    private var timerJob: kotlinx.coroutines.Job? = null

    fun triggerTimeLimitedEvent(targetCharacterId: String? = null): Boolean {
        val characters = engine.gameState.value.characters
        if (characters.isEmpty()) return false

        val targetChar = if (targetCharacterId != null) {
            characters.firstOrNull { it.id == targetCharacterId } ?: characters.random()
        } else {
            characters.random()
        }

        val event = com.example.haremdark.data.HaremEventData.generateEventForCharacter(targetChar)
        timerJob?.cancel()
        activeTimeLimitedEvent.value = event
        eventRemainingSeconds.value = event.durationSeconds

        timerJob = viewModelScope.launch {
            while (isActive && eventRemainingSeconds.value > 0) {
                kotlinx.coroutines.delay(1000L)
                val current = eventRemainingSeconds.value - 1
                eventRemainingSeconds.value = current
                if (current <= 0) {
                    activeTimeLimitedEvent.value = null
                    isEventDialogueOpen.value = false
                    engine.addLog("⏳ Čas vypršel – ${targetChar.name} se zklamaně stáhla do svých komnat.")
                    break
                }
            }
        }
        return true
    }

    fun dismissActiveEvent() {
        timerJob?.cancel()
        activeTimeLimitedEvent.value = null
        isEventDialogueOpen.value = false
    }

    fun openActiveEventDialogue() {
        if (activeTimeLimitedEvent.value != null) {
            isEventDialogueOpen.value = true
        }
    }

    fun closeActiveEventDialogue() {
        isEventDialogueOpen.value = false
    }

    fun resolveActiveEventChoice(choice: com.example.haremdark.data.DialogueChoice): Pair<Boolean, String> {
        val event = activeTimeLimitedEvent.value ?: return Pair(false, "Žádná aktivní událost.")
        val result = engine.executeSpecialDialogueChoice(event.characterId, choice)
        timerJob?.cancel()
        activeTimeLimitedEvent.value = null
        isEventDialogueOpen.value = false
        return result
    }
}
