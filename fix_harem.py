import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

# Restore selectedFilter in viewmodel just in case, but let's actually fix HaremScreen.kt

# Inject the modal inside HaremScreen right before closing brace of the main function:
# We need to find the end of HaremScreen.
# The HaremScreen function ends before `// Advanced Filter Modal` if it exists, or before `@Composable\nfun HaremHierarchyTab`
# But HaremScreen closes with:
#             }
#         )
#     }
# } // End of HaremScreen

match = re.search(r'            onUnequipItem = \{ slotId ->\n.*?Toast\.makeText\(context, "Předmět odepnut\.", Toast\.LENGTH_SHORT\)\.show\(\)\n            \}\n        \)\n    \}\n\}', text, re.DOTALL)

filter_modal = """
    if (filterSheetExpanded) {
        AlertDialog(
            onDismissRequest = { filterSheetExpanded = false },
            title = { Text("Filtrovat a Řadit", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val statusOptions = listOf("Všechny", "Oblíbená", "Ve vztahu", "Na nájmu", "Březí")
                    val roleOptions = listOf("Všechny", "Služky", "Válečnice", "Mágyně", "Intrikánky")
                    val affinityOptions = listOf("Všechny", "Úroveň 1-2", "Úroveň 3-4", "Úroveň 5+")
                    val sortOptionsList = listOf("Náklonnost", "Rarita / Úroveň", "Role (Archetyp)", "Bojová síla", "Nedávno")

                    // Status
                    Column {
                        Text("Stav (Status):", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                        statusOptions.chunked(3).forEach { rowOps ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowOps.forEach { op ->
                                    FilterChip(
                                        selected = filterCriteria.status == op,
                                        onClick = { haremViewModel.setFilterCriteria(filterCriteria.copy(status = op)) },
                                        label = { Text(op, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Role
                    Column {
                        Text("Role / Frakce:", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                        roleOptions.chunked(3).forEach { rowOps ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowOps.forEach { op ->
                                    FilterChip(
                                        selected = filterCriteria.role == op,
                                        onClick = { haremViewModel.setFilterCriteria(filterCriteria.copy(role = op)) },
                                        label = { Text(op, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Affinity Level
                    Column {
                        Text("Náklonnost:", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                        affinityOptions.chunked(3).forEach { rowOps ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowOps.forEach { op ->
                                    FilterChip(
                                        selected = filterCriteria.affinityLevel == op,
                                        onClick = { haremViewModel.setFilterCriteria(filterCriteria.copy(affinityLevel = op)) },
                                        label = { Text(op, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    androidx.compose.material3.HorizontalDivider()

                    // Sort By
                    Column {
                        Text("Seřadit podle:", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                        sortOptionsList.chunked(2).forEach { rowOps ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowOps.forEach { op ->
                                    FilterChip(
                                        selected = selectedSort == op,
                                        onClick = { haremViewModel.setSort(op) },
                                        label = { Text(op, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { filterSheetExpanded = false }) {
                    Text("Zavřít")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        haremViewModel.setFilterCriteria(com.example.haremdark.viewmodels.HaremFilterCriteria())
                        haremViewModel.setSort("Náklonnost")
                    }
                ) {
                    Text("Resetovat")
                }
            }
        )
    }
"""

if match:
    text = text.replace(match.group(0), match.group(0)[:-2] + filter_modal + "\n}")

# Fix Tab 0: Replace `filters.forEachIndexed { index, filter ->` entirely
tab0_filter_old = """                        // Filter Chips
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

active_filters_ui = """                        // Active Filter Summaries
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

text = text.replace(tab0_filter_old, active_filters_ui)

# Fix Tab 1:
tab1_filter_old = """                        // Filter Chips
                        item {
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
                            }
                        }"""

tab1_active_filters_ui = """                        // Active Filter Summaries
                        item {
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
                            }
                        }"""

text = text.replace(tab1_filter_old, tab1_active_filters_ui)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(text)

