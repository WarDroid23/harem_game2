import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

target = """    var oblibena: Boolean = false,
    var romanceBody: Int = 0,"""

replacement = """    var oblibena: Boolean = false,
    var isPinned: Boolean = false,
    var romanceBody: Int = 0,"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
