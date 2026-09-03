with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'r') as f:
    text = f.read()

target = """    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {"""

replacement = """    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DomainResourceBanner(gameState)
"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'w') as f:
    f.write(text)
