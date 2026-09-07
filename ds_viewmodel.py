import re

with open('app/src/main/java/com/example/haremdark/viewmodels/HaremViewModel.kt', 'r') as f:
    text = f.read()

new_class_and_state = """data class HaremFilterCriteria(
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
}
"""

start_idx = text.find("class HaremViewModel")
if start_idx != -1:
    old_section = text[start_idx:]
    text = text.replace(old_section, new_class_and_state)
    with open('app/src/main/java/com/example/haremdark/viewmodels/HaremViewModel.kt', 'w') as f:
        f.write(text)
    print("Replaced ViewModel successfully!")
else:
    print("Could not find ViewModel class.")

