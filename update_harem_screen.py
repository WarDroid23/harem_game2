import re

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

target = """            onMarry = {
                val (success, msg) = engine.marryConcubine(currentConcubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            },
            onRent = { client, days ->
                val (success, msg) = engine.rentSlave(currentConcubine.id, client, days)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) haremViewModel.openProfile(null)
            }
        )"""

replacement = """            onMarry = {
                val (success, msg) = engine.marryConcubine(currentConcubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            },
            onRent = { client, days ->
                val (success, msg) = engine.rentSlave(currentConcubine.id, client, days)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) haremViewModel.openProfile(null)
            },
            onUpgradeSkill = { skill ->
                val (success, msg) = engine.upgradeCharacterSkill(currentConcubine.id, skill)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(text)
