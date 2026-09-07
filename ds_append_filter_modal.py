import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

filter_modal = """
    // Advanced Filter Modal
    if (filterSheetExpanded) {
        AlertDialog(
            onDismissRequest = { filterSheetExpanded = false },
            title = { Text("Filtrovat a Řadit", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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

                    // Role / Faction
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
                        affinityOptions.chunked(2).forEach { rowOps ->
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
                    Text("Zavřít", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        haremViewModel.setFilterCriteria(com.example.haremdark.viewmodels.HaremFilterCriteria())
                        haremViewModel.setSort("Náklonnost")
                    }
                ) {
                    Text("Resetovat filtry")
                }
            }
        )
    }
"""

end_of_func = "            onRent = { client, days ->\n                val (success, msg) = engine.rentSlave(currentConcubine.id, client, days)\n                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()\n            },\n            onUpgradeSkill = { skillId ->\n                val (success, msg) = engine.upgradeConcubineSkill(currentConcubine.id, skillId)\n                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()\n            },\n            onEquipItem = { itemId, slotId ->\n                val (success, msg) = engine.equipItemOnConcubine(currentConcubine.id, itemId, slotId)\n                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()\n            },\n            onUnequipItem = { slotId ->\n                val (success, msg) = engine.unequipItemFromConcubine(currentConcubine.id, slotId)\n                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()\n            }\n        )\n    }\n"

if end_of_func in text:
    text = text.replace(end_of_func, end_of_func + filter_modal)
    with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
        f.write(text)
    print("Modal appended successfully!")
else:
    print("Could not find the end of the HaremScreen function.")
    # let's try a fallback by appending right before the HaremHierarchyTab
    fallback = "@Composable\nfun HaremHierarchyTab("
    if fallback in text:
        text = text.replace(fallback, filter_modal + "\n" + fallback)
        with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
            f.write(text)
        print("Modal appended successfully via fallback!")

