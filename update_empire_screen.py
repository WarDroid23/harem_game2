import re

with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'r') as f:
    text = f.read()

target1 = """    val tabs = listOf("🗡️ Mafie", "🏰 Budovy", "💰 Nájem", "📈 Produkce")"""
replacement1 = """    val tabs = listOf("🗡️ Mafie", "🏰 Budovy", "💰 Nájem", "📈 Produkce", "⛓️ Trh")"""
text = text.replace(target1, replacement1)

target2 = """            2 -> RentalsHubTab(gameState)
            3 -> StatisticsTab(gameState)
        }
    }
}"""
replacement2 = """            2 -> RentalsHubTab(gameState)
            3 -> StatisticsTab(gameState)
            4 -> RecruitmentTab(gameState, engine)
        }
    }
}

@Composable
fun RecruitmentTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val player = gameState.player

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trh s otroky (Nábor)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Rozšiř svůj harém nákupem nových dívek. Kvalita a vzácnost dívky závisí na množství investovaných zdrojů.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Kapacita harému: ${gameState.characters.size} / ${player.maxPopulation}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (gameState.characters.size >= player.maxPopulation) Color.Red else MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        item {
            RecruitmentOption(
                title = "Běžný otrok",
                desc = "Mladá, nezkušená dívka pochybného původu. (Úroveň 1)",
                goldCost = 250,
                manaCost = 0,
                playerGold = player.gold,
                playerMana = player.mana,
                onRecruit = {
                    val (success, msg) = engine.recruitCharacter("basic")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        item {
            RecruitmentOption(
                title = "Vzácný zajatec",
                desc = "Dívka z lepší rodiny, možná nižší šlechta, zajatá při raziích. (Úroveň 2, lepší staty)",
                goldCost = 600,
                manaCost = 20,
                playerGold = player.gold,
                playerMana = player.mana,
                onRecruit = {
                    val (success, msg) = engine.recruitCharacter("advanced")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        item {
            RecruitmentOption(
                title = "Exkluzivní trofej",
                desc = "Prvotřídní kráska s magickým nadáním či královskou krví, ukradená z tajných aukcí. (Úroveň 3, nejlepší staty)",
                goldCost = 1500,
                manaCost = 50,
                playerGold = player.gold,
                playerMana = player.mana,
                onRecruit = {
                    val (success, msg) = engine.recruitCharacter("elite")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun RecruitmentOption(
    title: String,
    desc: String,
    goldCost: Int,
    manaCost: Int,
    playerGold: Int,
    playerMana: Int,
    onRecruit: () -> Unit
) {
    val canAfford = playerGold >= goldCost && playerMana >= manaCost
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goldCost > 0) {
                        Text("💰 $goldCost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (playerGold >= goldCost) Color(0xFFFFD700) else Color.Red)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (manaCost > 0) {
                        Text("🔮 $manaCost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (playerMana >= manaCost) Color(0xFFE040FB) else Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRecruit,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Koupit na trhu", fontWeight = FontWeight.Bold)
            }
        }
    }
}"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'w') as f:
    f.write(text)
