import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()
    
# Fix float mismatch
text = re.sub(
    r'var finalEnemyDmg = \(rawBossDmg - defenseReduction\)\.coerceAtLeast\(4\)',
    r'var finalEnemyDmg = (rawBossDmg - defenseReduction).coerceAtLeast(4f).toInt()',
    text
)
with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)


with open('app/src/main/java/com/example/haremdark/ui/components/TurnBasedCombatModule.kt', 'r') as f:
    text = f.read()

target = """fun EnemyRosterView(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedTierFilter by remember { mutableStateOf("all") }"""

replacement = """fun EnemyRosterView(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedTierFilter by remember { mutableStateOf("all") }
    var selectedBossForCombat by remember { mutableStateOf<com.example.haremdark.data.Boss?>(null) }"""
text = text.replace(target, replacement)
with open('app/src/main/java/com/example/haremdark/ui/components/TurnBasedCombatModule.kt', 'w') as f:
    f.write(text)
