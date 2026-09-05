import re

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()

target1 = """    onMarry: () -> Unit,
    onRent: (String, Int) -> Unit
) {"""
replacement1 = """    onMarry: () -> Unit,
    onRent: (String, Int) -> Unit,
    onUpgradeSkill: (String) -> Unit
) {"""
text = text.replace(target1, replacement1)

target2 = """    val sectionTabs = listOf("📊 Profil", "💖 Náklonnost", "🎁 Dary", "⚡ Akce")"""
replacement2 = """    val sectionTabs = listOf("📊 Profil", "💖 Náklonnost", "🎁 Dary", "⚡ Akce", "✨ Dovednosti")"""
text = text.replace(target2, replacement2)

target3 = """                        3 -> InteractionsSectionTab(
                            character = character,
                            player = player,
                            onExecuteInteraction = onExecuteInteraction,
                            onCourtRomance = onCourtRomance,
                            onMarry = onMarry,
                            onRent = onRent
                        )
                    }"""
replacement3 = """                        3 -> InteractionsSectionTab(
                            character = character,
                            player = player,
                            onExecuteInteraction = onExecuteInteraction,
                            onCourtRomance = onCourtRomance,
                            onMarry = onMarry,
                            onRent = onRent
                        )
                        4 -> SkillTreeTab(character = character, onUpgradeSkill = onUpgradeSkill)
                    }"""
text = text.replace(target3, replacement3)

skill_tree = """
@Composable
fun SkillTreeTab(character: Character, onUpgradeSkill: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Úroveň: ${character.level}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("ZK: ${character.xp} / ${character.level * 100}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Dostupné body (SP): ${character.skillPoints}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("Získávej ZK účastí v Aréně, abys odemkl další body dovedností.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        
        item {
            Text("⚔️ Bojové dovednosti", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        item {
            SkillRow(
                title = "Útok (Síla Krve)",
                desc = "+5 základní poškození v aréně za úroveň.",
                level = character.skills["combat"] ?: 0,
                canUpgrade = character.skillPoints > 0,
                onUpgrade = { onUpgradeSkill("combat") }
            )
        }
        item {
            SkillRow(
                title = "Obrana (Odolnost)",
                desc = "+2 základní obrana v aréně za úroveň.",
                level = character.skills["defense"] ?: 0,
                canUpgrade = character.skillPoints > 0,
                onUpgrade = { onUpgradeSkill("defense") }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("💰 Správa dominia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        item {
            SkillRow(
                title = "Produkce surovin",
                desc = "+2% ke globální produkci dominia (pokud není v nájmu).",
                level = character.skills["production"] ?: 0,
                canUpgrade = character.skillPoints > 0,
                onUpgrade = { onUpgradeSkill("production") }
            )
        }
        item {
            SkillRow(
                title = "Expert na nájmy",
                desc = "+15 zlatých k dennímu příjmu z pronájmu.",
                level = character.skills["rental"] ?: 0,
                canUpgrade = character.skillPoints > 0,
                onUpgrade = { onUpgradeSkill("rental") }
            )
        }
    }
}

@Composable
fun SkillRow(title: String, desc: String, level: Int, canUpgrade: Boolean, onUpgrade: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Úr. $level", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
"""

if "fun SkillTreeTab" not in text:
    text = text + skill_tree

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)
