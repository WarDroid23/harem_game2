import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

# 1. Top variables block replacement
old_vars = """    val selectedHaremTab by haremViewModel.selectedHaremTab.collectAsState()
    val selectedFilter by haremViewModel.selectedFilter.collectAsState()
    val selectedSort by haremViewModel.selectedSort.collectAsState()
    val searchQuery by haremViewModel.searchQuery.collectAsState()
    val selectedCharacterForProfile by haremViewModel.selectedCharacterForProfile.collectAsState()
    val selectedCharacterForInteraction by haremViewModel.selectedCharacterForInteraction.collectAsState()
    val filteredList by haremViewModel.filteredList.collectAsState()

    val haremTabs = listOf("🔲 Mřížka", "🛏️ Komnaty", "👑 Hierarchie", "👶 Dynastie", "👗 Garderóba", "📚 Archiv", "🖼️ Galerie")
    val filters = listOf("Všechny", "★ Oblíbená", "💍 Vztahy", "💰 Na nájmu", "🤰 Březí", "⚔️ Válečnice", "🔮 Mágyně", "👑 Intrikánky", "🔗 Služky")
    val sortOptions = listOf("Náklonnost", "Rarita / Úroveň", "Role (Archetyp)", "Bojová síla", "Nedávno")
    var sortExpanded by remember { mutableStateOf(false) }"""

new_vars = """    val selectedHaremTab by haremViewModel.selectedHaremTab.collectAsState()
    val filterCriteria by haremViewModel.filterCriteria.collectAsState()
    val selectedSort by haremViewModel.selectedSort.collectAsState()
    val searchQuery by haremViewModel.searchQuery.collectAsState()
    val selectedCharacterForProfile by haremViewModel.selectedCharacterForProfile.collectAsState()
    val selectedCharacterForInteraction by haremViewModel.selectedCharacterForInteraction.collectAsState()
    val filteredList by haremViewModel.filteredList.collectAsState()

    val haremTabs = listOf("🔲 Mřížka", "🛏️ Komnaty", "👑 Hierarchie", "👶 Dynastie", "👗 Garderóba", "📚 Archiv", "🖼️ Galerie")
    
    var filterSheetExpanded by remember { mutableStateOf(false) }"""
text = text.replace(old_vars, new_vars)

# 2. Search & Quick Stats Banner and Filter Chips replacement
old_search_banner = """                        // Search & Quick Stats Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { haremViewModel.setSearchQuery(it) },
                                placeholder = { Text("Hledat...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { haremViewModel.setSearchQuery("") }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Box {
                                IconButton(
                                    onClick = { sortExpanded = true },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .height(56.dp)
                                        .width(48.dp)
                                ) {
                                    Icon(Icons.Default.Sort, contentDescription = "Třídit", tint = MaterialTheme.colorScheme.primary)
                                }
                                DropdownMenu(
                                    expanded = sortExpanded,
                                    onDismissRequest = { sortExpanded = false }
                                ) {
                                    sortOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = { 
                                                haremViewModel.setSort(option)
                                                sortExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${gameState.characters.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                        Text("dívek", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                        
                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filters.forEachIndexed { index, filter ->
                                FilterChip(
                                    selected = selectedFilter == index,
                                    onClick = { haremViewModel.setFilter(index) },
                                    label = { Text(filter, fontSize = 11.sp, fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }"""

new_search_banner = """                        // Search & Filters Action Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { haremViewModel.setSearchQuery(it) },
                                placeholder = { Text("Hledat jméno...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { haremViewModel.setSearchQuery("") }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Button(
                                onClick = { filterSheetExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(56.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Filtry", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Filtry", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        // Active Filter Summaries
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val activeFilters = buildList {
                                if (filterCriteria.status != "Všechny") add("Status: ${filterCriteria.status}")
                                if (filterCriteria.role != "Všechny") add("Role: ${filterCriteria.role}")
                                if (filterCriteria.affinityLevel != "Všechny") add("Vztah: ${filterCriteria.affinityLevel}")
                                add("Řazení: $selectedSort")
                            }
                            activeFilters.forEach { f ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(f, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }"""

text = text.replace(old_search_banner, new_search_banner)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(text)

