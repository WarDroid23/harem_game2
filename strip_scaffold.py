with open("./app/src/main/java/com/example/haremdark/ui/screens/AchievementScreen.kt", "r") as f:
    content = f.read()

import re

# Remove the Scaffold and topBar
content = re.sub(r'Scaffold\(\s*topBar = \{[^}]+\}\s*\)\s*\{\s*padding ->', 'Box(modifier = Modifier.fillMaxSize()) { val padding = PaddingValues(0.dp)', content, flags=re.MULTILINE|re.DOTALL)

with open("./app/src/main/java/com/example/haremdark/ui/screens/AchievementScreen.kt", "w") as f:
    f.write(content)
