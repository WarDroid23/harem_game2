import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.haremdark.viewmodels.HaremViewModel
"""
content = content.replace('import com.example.haremdark.models.Character', imports + 'import com.example.haremdark.models.Character')

# Replace state variables
old_state = """    var selectedHaremTab by remember { mutableIntStateOf(0) }
    val haremTabs = listOf("🔲 Mřížka", "🛏️ Komnaty", "👑 Hierarchie", "👶 Dynastie", "👗 Garderóba", "📚 Archiv")

    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Všechny", "★ Oblíbená", "💍 Vztahy", "💰 Na nájmu", "🤰 Březí")
    
    var selectedSort by remember { mutableStateOf("Náklonnost") }
    val sortOptions = listOf("Náklonnost", "Rarita", "Nedávno")
    var sortExpanded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    var selectedCharacterForProfile by remember { mutableStateOf<Character?>(null) }
    var selectedCharacterForInteraction by remember { mutableStateOf<Character?>(null) }

    val filteredList = remember(gameState.characters, selectedFilter, searchQuery, selectedSort) {
        var list = when (selectedFilter) {
            1 -> gameState.characters.filter { it.oblibena }
            2 -> gameState.characters.filter { it.jeManzelkou || it.partnerka }
            3 -> gameState.characters.filter { it.naNajmu }
            4 -> gameState.characters.filter { it.tehotna }
            else -> gameState.characters
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) || it.archetypeId.contains(searchQuery, ignoreCase = true) }
        }
        
        when (selectedSort) {
            "Náklonnost" -> list = list.sortedByDescending { it.affinityPoints }
            "Rarita" -> list = list.sortedByDescending { it.rarity }
            "Nedávno" -> list = list.sortedByDescending { it.lastInteractionDay }
        }
        
        list
    }"""

new_state = """    val viewModel: HaremViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HaremViewModel(engine) as T
            }
        }
    )

    val selectedHaremTab by viewModel.selectedHaremTab.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCharacterForProfile by viewModel.selectedCharacterForProfile.collectAsState()
    val selectedCharacterForInteraction by viewModel.selectedCharacterForInteraction.collectAsState()
    val filteredList by viewModel.filteredList.collectAsState()

    val haremTabs = listOf("🔲 Mřížka", "🛏️ Komnaty", "👑 Hierarchie", "👶 Dynastie", "👗 Garderóba", "📚 Archiv")
    val filters = listOf("Všechny", "★ Oblíbená", "💍 Vztahy", "💰 Na nájmu", "🤰 Březí")
    val sortOptions = listOf("Náklonnost", "Rarita", "Nedávno")
    var sortExpanded by remember { mutableStateOf(false) }"""

content = content.replace(old_state, new_state)

content = content.replace("selectedHaremTab = index", "viewModel.selectTab(index)")
content = content.replace("selectedFilter = index", "viewModel.setFilter(index)")
content = content.replace("selectedSort = it", "viewModel.setSort(it)")
content = content.replace("searchQuery = it", "viewModel.setSearchQuery(it)")
content = content.replace("selectedCharacterForProfile = ", "viewModel.openProfile(")
content = content.replace("selectedCharacterForInteraction = ", "viewModel.openInteraction(")

# Fix viewModel.openProfile(...) syntax issues from simple replace
content = re.sub(r'viewModel\.openProfile\((.*?)\s*\}\)', r'viewModel.openProfile(\1); }', content)
content = re.sub(r'viewModel\.openInteraction\((.*?)\s*\}\)', r'viewModel.openInteraction(\1); }', content)

# A more robust regex replacement for the assignments that got replaced with openProfile(
content = content.replace("viewModel.openProfile(character", "viewModel.openProfile(character)")
content = content.replace("viewModel.openProfile(null", "viewModel.openProfile(null)")
content = content.replace("viewModel.openInteraction(character", "viewModel.openInteraction(character)")
content = content.replace("viewModel.openInteraction(null", "viewModel.openInteraction(null)")

# Clean up any malformed replacements (e.g. openProfile(null))
content = re.sub(r'viewModel\.openProfile\((.*?)\)\)', r'viewModel.openProfile(\1)', content)
content = re.sub(r'viewModel\.openInteraction\((.*?)\)\)', r'viewModel.openInteraction(\1)', content)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(content)
