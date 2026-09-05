import re

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'r') as f:
    text = f.read()

target1 = """fun CharacterCard(
    character: Character,
    onInteractClick: () -> Unit,
    onDetailClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""
replacement1 = """fun CharacterCard(
    character: Character,
    onInteractClick: () -> Unit,
    onDetailClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {"""
text = text.replace(target1, replacement1)

target2 = """                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (character.oblibena) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Oblíbená",
                        tint = if (character.oblibena) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }"""
replacement2 = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPinClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (character.isPinned) Icons.Default.PushPin else Icons.Default.LocationOn,
                            contentDescription = "Připnout",
                            tint = if (character.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (character.oblibena) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Vrchní favoritka",
                            tint = if (character.oblibena) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }"""
text = text.replace(target2, replacement2)

target3 = """fun CharacterGridCard(
    character: Character,
    onInteractClick: () -> Unit,
    onDetailClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""
replacement3 = """fun CharacterGridCard(
    character: Character,
    onInteractClick: () -> Unit,
    onDetailClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {"""
text = text.replace(target3, replacement3)

target4 = """                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x88000000), CircleShape)
                ) {
                    Icon(
                        imageVector = if (character.oblibena) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Oblíbená",
                        tint = if (character.oblibena) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }"""
replacement4 = """                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (character.oblibena) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Oblíbená",
                            tint = if (character.oblibena) Color(0xFFFFD700) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onPinClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (character.isPinned) Icons.Default.PushPin else Icons.Default.LocationOn,
                            contentDescription = "Připnout",
                            tint = if (character.isPinned) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }"""
text = text.replace(target4, replacement4)

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'w') as f:
    f.write(text)
