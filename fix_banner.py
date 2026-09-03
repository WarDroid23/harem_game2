with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'r') as f:
    text = f.read()

banner_code = """
@Composable
fun AnimatedResourceItem(icon: String, name: String, value: Int, maxValue: Int? = null) {
    var previousValue by remember { mutableIntStateOf(value) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(value) {
        if (value != previousValue) {
            scale.animateTo(1.2f, animationSpec = tween(150))
            scale.animateTo(1f, animationSpec = tween(300))
        }
        previousValue = value
    }

    val displayValue = if (maxValue != null) "$value/$maxValue" else "$value"

    Row(
        modifier = Modifier
            .scale(scale.value)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(displayValue, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DomainResourceBanner(gameState: GameSave) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedResourceItem("🪵", "Dřevo", gameState.player.wood)
        AnimatedResourceItem("🪨", "Kamení", gameState.player.stone)
        AnimatedResourceItem("⛓️", "Železo", gameState.player.iron)
        AnimatedResourceItem("🔮", "Mana", gameState.player.mana, gameState.player.maxMana)
        AnimatedResourceItem("👥", "Populace", gameState.player.population, gameState.player.maxPopulation)
    }
}
"""

if "fun AnimatedResourceItem" not in text:
    text += banner_code

with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'w') as f:
    f.write(text)
