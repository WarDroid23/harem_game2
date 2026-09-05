import re

with open('app/src/main/java/com/example/haremdark/ui/screens/ProgressionScreen.kt', 'r') as f:
    text = f.read()

target1 = """                            Text("Pán dominia: ${player.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)"""

replacement1 = """                            Column {
                                Text("Pán dominia: ${player.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (player.activeTitle != null) {
                                    val titleObj = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == player.activeTitle }
                                    if (titleObj != null) {
                                        Text("${titleObj.badgeIcon} ${titleObj.title}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }"""

text = text.replace(target1, replacement1)

target2 = """        // Skill Tree List
        item {
            Text("Dovednosti Pána", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }"""

replacement2 = """        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Titul a Úspěchy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (player.unlockedAchievements.isEmpty()) {
                        Text("Zatím nemáš žádné úspěchy. Buduj dominium a harém!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        player.unlockedAchievements.forEach { achId ->
                            val ach = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == achId }
                            if (ach != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ach.badgeIcon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(ach.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }
                                    if (ach.isTitle) {
                                        OutlinedButton(
                                            onClick = {
                                                engine.setActiveTitle(ach.id)
                                                Toast.makeText(context, "Titul nastaven!", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(if (player.activeTitle == ach.id) "Aktivní" else "Vybrat", fontSize = 10.sp)
                                        }
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        }
        
        // Skill Tree List
        item {
            Text("Dovednosti Pána", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }"""

text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/ui/screens/ProgressionScreen.kt', 'w') as f:
    f.write(text)
