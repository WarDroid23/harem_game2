import re
with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

if "import kotlinx.coroutines.launch" not in text:
    text = text.replace("import kotlinx.coroutines.flow.MutableStateFlow", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.flow.MutableStateFlow")
    with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
        f.write(text)
