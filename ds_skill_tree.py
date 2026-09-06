import re

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()

# I will replace the existing SkillTreeTab and SkillRow entirely with the new UI.
old_skill_tree = r'@Composable\nfun SkillTreeTab\(.*?\}\n\n@Composable\nfun SkillRow\(.*?\n\}'

new_skill_tree = """@Composable
fun SkillTreeTab(character: Character, onUpgradeSkill: (String) -> Unit) {
    var selectedBranch by remember { mutableStateOf("Boj") }
    val branches = listOf("Boj", "Podpora")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with level, xp, and available points
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Úroveň ${character.level}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("ZK: ${character.xp} / ${character.level * 100}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${character.skillPoints} SP", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Dostupné body", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            LinearProgressIndicator(
                progress = { (character.xp.toFloat() / (character.level * 100).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
        }

        // Branch Selection
        TabRow(
            selectedTabIndex = branches.indexOf(selectedBranch),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            branches.forEach { branch ->
                Tab(
                    selected = selectedBranch == branch,
                    onClick = { selectedBranch = branch },
                    text = { Text(branch, fontWeight = FontWeight.Bold) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedBranch == "Boj") {
                SkillTreeLayout(
                    character = character,
                    onUpgradeSkill = onUpgradeSkill,
                    skills = listOf(
                        SkillNodeData("combat", "Útok", "🗡️", "+5 Poškození v boji", 0),
                        SkillNodeData("defense", "Obrana", "🛡️", "+2 Obrana v boji", 1),
                        SkillNodeData("vitality", "Vitalita", "❤️", "+10 Zdraví", 1),
                        SkillNodeData("bloodlust", "Krvavá žízeň", "🩸", "Šance na krvácení", 2, req = "combat", reqLvl = 3),
                        SkillNodeData("iron_skin", "Železná kůže", "🧱", "Šance blokovat útok", 2, req = "defense", reqLvl = 3)
                    )
                )
            } else {
                SkillTreeLayout(
                    character = character,
                    onUpgradeSkill = onUpgradeSkill,
                    skills = listOf(
                        SkillNodeData("production", "Produkce", "⚒️", "+2% Produkce surovin", 0),
                        SkillNodeData("rental", "Nájmy", "💰", "+15 Zlata z nájmů", 0),
                        SkillNodeData("charm", "Šarm", "✨", "+10% Zisk náklonnosti", 1, req = "rental", reqLvl = 2),
                        SkillNodeData("efficiency", "Efektivita", "⚙️", "Sníží únavu z práce", 1, req = "production", reqLvl = 2),
                        SkillNodeData("loyalty_boost", "Oddanost", "💖", "Zabraňuje ztrátě důvěry", 2, req = "charm", reqLvl = 3)
                    )
                )
            }
        }
    }
}

data class SkillNodeData(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val tier: Int,
    val req: String? = null,
    val reqLvl: Int = 0
)

@Composable
fun SkillTreeLayout(character: Character, onUpgradeSkill: (String) -> Unit, skills: List<SkillNodeData>) {
    val maxTier = skills.maxOfOrNull { it.tier } ?: 0
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        for (tier in 0..maxTier) {
            val tierSkills = skills.filter { it.tier == tier }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tierSkills.forEach { skill ->
                    val currentLvl = character.skills[skill.id] ?: 0
                    val reqMet = skill.req == null || (character.skills[skill.req] ?: 0) >= skill.reqLvl
                    val canUpgrade = character.skillPoints > 0 && reqMet
                    
                    SkillNode(
                        skill = skill,
                        currentLvl = currentLvl,
                        canUpgrade = canUpgrade,
                        reqMet = reqMet,
                        onUpgrade = { onUpgradeSkill(skill.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillNode(
    skill: SkillNodeData,
    currentLvl: Int,
    canUpgrade: Boolean,
    reqMet: Boolean,
    onUpgrade: () -> Unit
) {
    val bgColor = if (currentLvl > 0) MaterialTheme.colorScheme.primary 
                  else if (reqMet) MaterialTheme.colorScheme.surfaceVariant 
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                  
    val contentColor = if (currentLvl > 0) MaterialTheme.colorScheme.onPrimary 
                       else if (reqMet) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = canUpgrade) { onUpgrade() }
            .background(bgColor)
            .padding(8.dp)
    ) {
        Text(skill.icon, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text(skill.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("Lvl $currentLvl", fontSize = 10.sp, color = contentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(skill.desc, fontSize = 9.sp, color = contentColor.copy(alpha = 0.8f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 10.sp)
        
        if (!reqMet) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vyžaduje:", fontSize = 8.sp, color = MaterialTheme.colorScheme.error)
            Text("${skill.req} Lvl ${skill.reqLvl}", fontSize = 8.sp, color = MaterialTheme.colorScheme.error)
        } else if (canUpgrade) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Vylepšit", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
"""

text = re.sub(old_skill_tree, new_skill_tree, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)

