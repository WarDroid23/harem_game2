import re

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()

target1 = """                            onMarry = onMarry,
                            onRent = onRent
                        )
                        4 -> SkillTreeTab(character = character, onUpgradeSkill = onUpgradeSkill)"""
replacement1 = """                            onMarry = onMarry,
                            onRent = onRent,
                            onUpgradeSkill = onUpgradeSkill
                        )
                        4 -> SkillTreeTab(character = character, onUpgradeSkill = onUpgradeSkill)"""
text = text.replace(target1, replacement1)

target2 = """        onMarry = onMarry,
        onRent = onRent
    )
}"""
replacement2 = """        onMarry = onMarry,
        onRent = onRent,
        onUpgradeSkill = onUpgradeSkill
    )
}"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)
