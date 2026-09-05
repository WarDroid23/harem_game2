import re

with open('app/src/main/java/com/example/haremdark/ui/components/TurnBasedCombatModule.kt', 'r') as f:
    text = f.read()

target1 = """fun EnemyRosterView(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }"""

replacement1 = """fun EnemyRosterView(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }
    var selectedBossForCombat by remember { mutableStateOf<com.example.haremdark.data.Boss?>(null) }"""
text = text.replace(target1, replacement1)

target2 = """            EnemyCard(
                boss = boss,
                isDefeated = defeated,
                onChallenge = { engine.startBossCombat(boss) }
            )"""

replacement2 = """            EnemyCard(
                boss = boss,
                isDefeated = defeated,
                onChallenge = { selectedBossForCombat = boss }
            )"""
text = text.replace(target2, replacement2)

target3 = """    }
}

@Composable
fun RosterFilterChip(title: String, selected: Boolean, onClick: () -> Unit) {"""

replacement3 = """    }
    
    if (selectedBossForCombat != null) {
        val boss = selectedBossForCombat!!
        Dialog(onDismissRequest = { selectedBossForCombat = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Koho chceš vyslat do boje proti ${boss.name}?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    engine.startBossCombat(boss, null)
                                    selectedBossForCombat = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Pán Dominia (Ty)", fontWeight = FontWeight.Bold)
                                        Text("Boj: ${gameState.player.skills["boj"] ?: 0} | HP: ${gameState.player.hp}/${gameState.player.maxHp}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        
                        items(gameState.characters.filter { it.hp > 0 }) { char ->
                            val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                            val hpBonus = char.equipment.values.filterNotNull().sumOf { it.hpBonus }
                            val totalCombat = (char.skills["combat"] ?: 0) + combatBonus
                            val totalMaxHp = char.maxHp + hpBonus
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    engine.startBossCombat(boss, char.id)
                                    selectedBossForCombat = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚔️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(char.name, fontWeight = FontWeight.Bold)
                                        Text("Boj: $totalCombat | HP: ${char.hp}/$totalMaxHp", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { selectedBossForCombat = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Zrušit")
                    }
                }
            }
        }
    }
}

@Composable
fun RosterFilterChip(title: String, selected: Boolean, onClick: () -> Unit) {"""
text = text.replace(target3, replacement3)

with open('app/src/main/java/com/example/haremdark/ui/components/TurnBasedCombatModule.kt', 'w') as f:
    f.write(text)
