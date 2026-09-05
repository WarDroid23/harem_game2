with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'a') as f:
    f.write("""

@Composable
fun CharacterEquipmentTab(character: Character, player: Player, engine: GameEngine) {
    val equipmentSlots = listOf("weapon" to "Zbraň", "armor" to "Zbroj", "accessory" to "Doplněk")
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🛡️ Bojové statistiky", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val combatBonus = character.equipment.values.filterNotNull().sumOf { it.combatBonus }
                    val defBonus = character.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                    val hpBonus = character.equipment.values.filterNotNull().sumOf { it.hpBonus }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatCounter("HP", "${character.hp}/${character.maxHp + hpBonus} ${if(hpBonus>0) "(+$hpBonus)" else ""}")
                        StatCounter("Boj", "${character.skills["combat"] ?: 0} ${if(combatBonus>0) "(+$combatBonus)" else ""}")
                        StatCounter("Obrana", "${character.skills["defense"] ?: 0} ${if(defBonus>0) "(+$defBonus)" else ""}")
                    }
                }
            }
        }
        
        items(equipmentSlots) { (slotId, slotName) ->
            val equippedItem = character.equipment[slotId]
            val availableItems = player.items.filter { it.category == "equipment" && it.equipSlot == slotId && it.count > 0 }
            
            var showInventoryMenu by remember { mutableStateOf(false) }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(slotName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (equippedItem != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(equippedItem.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(equippedItem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(equippedItem.effectDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Button(
                                onClick = { engine.unequipItemFromCharacter(character.id, slotId) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Odebrat")
                            }
                        }
                    } else {
                        Button(
                            onClick = { showInventoryMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Vybavit předmět", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            
            if (showInventoryMenu) {
                Dialog(onDismissRequest = { showInventoryMenu = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Vyber předmět pro: $slotName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (availableItems.isEmpty()) {
                                Text("Nemáš žádné volné předměty pro tento slot.", modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(availableItems) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { 
                                                    engine.equipItemToCharacter(character.id, item.id, slotId)
                                                    showInventoryMenu = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.icon, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("${item.name} (x${item.count})", fontWeight = FontWeight.Bold)
                                                Text(item.effectDescription, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showInventoryMenu = false }, modifier = Modifier.align(Alignment.End)) {
                                Text("Zavřít")
                            }
                        }
                    }
                }
            }
        }
    }
}
""")
