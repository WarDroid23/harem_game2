with open("./app/src/main/java/com/example/haremdark/MainActivity.kt", "r") as f:
    lines = f.readlines()

replacement = """                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                                label = { Text("Inventář") },
                                selected = currentRoute == "inventory",
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate("inventory") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                                label = { Text("Úspěchy") },
                                selected = currentRoute == "achievements",
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate("achievements") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
"""

lines = lines[:88] + [replacement] + lines[93:]

with open("./app/src/main/java/com/example/haremdark/MainActivity.kt", "w") as f:
    f.writelines(lines)
