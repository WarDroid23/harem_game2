import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        val fortressLevel = current.buildings.firstOrNull { it.id == "b1" }?.level ?: 1"""
replacement = """        val fortressLevel = current.buildings.firstOrNull { it.type == "pevnost" }?.level ?: 1"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
