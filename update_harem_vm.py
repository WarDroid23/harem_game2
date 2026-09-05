import re

with open('app/src/main/java/com/example/haremdark/viewmodels/HaremViewModel.kt', 'r') as f:
    text = f.read()

target = """        when (sort) {
            "Náklonnost" -> list = list.sortedByDescending { it.affinityPoints }
            "Rarita" -> list = list.sortedByDescending { it.rarity }
            "Nedávno" -> list = list.sortedByDescending { it.lastInteractionDay }
        }"""

replacement = """        when (sort) {
            "Náklonnost" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.affinityPoints })
            "Rarita" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.rarity })
            "Nedávno" -> list = list.sortedWith(compareByDescending<Character> { it.isPinned }.thenByDescending { it.lastInteractionDay })
        }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/viewmodels/HaremViewModel.kt', 'w') as f:
    f.write(text)
