import re

with open('app/src/main/java/com/example/haremdark/MainActivity.kt', 'r') as f:
    text = f.read()

text = text.replace('val currentTheme by engine.currentTheme.collectAsState()', 'val currentTheme by engine.currentTheme.collectAsState()\n            val dailyReward by engine.dailyRewardAvailable.collectAsState()')


dialog_ui = """                // Daily Login Reward Dialog
                dailyReward?.let { reward ->
                    AlertDialog(
                        onDismissRequest = { /* forced claim */ },
                        title = { 
                            Text(
                                "🎁 Denní odměna", 
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Přihlášení v řadě: ${reward.consecutiveDays} dní", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text("Tvá věrnost dominiu je odměněna, můj pane:")
                                
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("💰", fontSize = 24.sp)
                                    Text("+${reward.rewardGold} Zlatých")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🔮", fontSize = 24.sp)
                                    Text("+${reward.rewardMana} Temné energie")
                                }
                                
                                if (reward.itemReward != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Vzácný dar k jubileu:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(reward.itemReward.icon, fontSize = 32.sp)
                                        Column {
                                            Text(reward.itemReward.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            Text(reward.itemReward.effectDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { 
                                    engine.claimDailyReward() 
                                    Toast.makeText(context, "Denní odměna vybrána!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Vybrat odměnu")
                            }
                        }
                    )
                }

                // Next Day Rest Confirmation Dialog"""

text = text.replace('// Next Day Rest Confirmation Dialog', dialog_ui)

with open('app/src/main/java/com/example/haremdark/MainActivity.kt', 'w') as f:
    f.write(text)

