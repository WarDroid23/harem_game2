import re

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'r') as f:
    text = f.read()

target = """fun CharacterGridCard(
    character: Character,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""

replacement = """fun CharacterGridCard(
    character: Character,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'w') as f:
    f.write(text)
