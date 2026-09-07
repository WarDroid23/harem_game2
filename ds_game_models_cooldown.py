import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

old_combat_session = """    var isOver: Boolean = false,
    var victory: Boolean = false,
    var lootGained: String? = null
)"""

new_combat_session = """    var isOver: Boolean = false,
    var victory: Boolean = false,
    var lootGained: String? = null,
    var skillCooldowns: Map<String, Int> = emptyMap()
)"""

text = text.replace(old_combat_session, new_combat_session)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)

