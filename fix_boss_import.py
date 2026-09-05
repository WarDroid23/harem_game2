import re

with open('app/src/main/java/com/example/haremdark/ui/components/TurnBasedCombatModule.kt', 'r') as f:
    text = f.read()

target = """var selectedBossForCombat by remember { mutableStateOf<com.example.haremdark.data.Boss?>(null) }"""
replacement = """var selectedBossForCombat by remember { mutableStateOf<com.example.haremdark.models.Boss?>(null) }"""
text = text.replace(target, replacement)

target2 = """val boss = selectedBossForCombat!!"""
replacement2 = """val boss = selectedBossForCombat"""
text = text.replace(target2, replacement2)

target3 = """Dialog(onDismissRequest = { selectedBossForCombat = null }) {"""
replacement3 = """if (boss != null) {
        Dialog(onDismissRequest = { selectedBossForCombat = null }) {"""
text = text.replace(target3, replacement3)

target4 = """                    Button(onClick = { selectedBossForCombat = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Zrušit")
                    }
                }
            }
        }
    }"""
replacement4 = """                    Button(onClick = { selectedBossForCombat = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Zrušit")
                    }
                }
            }
        }
    }
    }"""
text = text.replace(target4, replacement4)

with open('app/src/main/java/com/example/haremdark/ui/components/TurnBasedCombatModule.kt', 'w') as f:
    f.write(text)
