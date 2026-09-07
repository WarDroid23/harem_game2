import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

# Remove floating modal at the bottom
text = re.sub(r'// Advanced Filter Modal\s+if \(filterSheetExpanded\).*?@Composable\nfun HaremHierarchyTab', '@Composable\nfun HaremHierarchyTab', text, flags=re.DOTALL)

# Add variables back just to make it compile, then we will refine it.
vars_inject = """    val selectedHaremTab by haremViewModel.selectedHaremTab.collectAsState()
    val filterCriteria by haremViewModel.filterCriteria.collectAsState()
    val selectedSort by haremViewModel.selectedSort.collectAsState()
    val searchQuery by haremViewModel.searchQuery.collectAsState()
    val selectedCharacterForProfile by haremViewModel.selectedCharacterForProfile.collectAsState()
    val selectedCharacterForInteraction by haremViewModel.selectedCharacterForInteraction.collectAsState()
    val filteredList by haremViewModel.filteredList.collectAsState()

    val haremTabs = listOf("🔲 Mřížka", "🛏️ Komnaty", "👑 Hierarchie", "👶 Dynastie", "👗 Garderóba", "📚 Archiv", "🖼️ Galerie")
    
    var filterSheetExpanded by remember { mutableStateOf(false) }

    // legacy variables to prevent unresolved references during transition
    val selectedFilter = 0
    val filters = listOf("Všechny")
    val sortOptions = listOf("Náklonnost")
    var sortExpanded by remember { mutableStateOf(false) }"""

text = re.sub(r'    val selectedHaremTab by haremViewModel.*?var filterSheetExpanded by remember \{ mutableStateOf\(false\) \}', vars_inject, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(text)
