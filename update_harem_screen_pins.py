import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

target1 = """                                CharacterCard(
                                    character = character,
                                    onInteractClick = { haremViewModel.openInteraction(character) },
                                    onDetailClick = { haremViewModel.openProfile(character) },
                                    onFavoriteClick = {"""

replacement1 = """                                CharacterCard(
                                    character = character,
                                    onInteractClick = { haremViewModel.openInteraction(character) },
                                    onDetailClick = { haremViewModel.openProfile(character) },
                                    onPinClick = {
                                        val (success, res) = engine.togglePin(character.id)
                                        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                    },
                                    onFavoriteClick = {"""

text = text.replace(target1, replacement1)

target2 = """                                    CharacterGridCard(
                                        character = character,
                                        onClick = { haremViewModel.openProfile(character) },
                                        onFavoriteClick = {"""

replacement2 = """                                    CharacterGridCard(
                                        character = character,
                                        onClick = { haremViewModel.openProfile(character) },
                                        onPinClick = {
                                            val (success, res) = engine.togglePin(character.id)
                                            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                        },
                                        onFavoriteClick = {"""

text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(text)
