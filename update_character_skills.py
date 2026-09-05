with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

target = """    var inventory: MutableList<InventoryItem> = mutableListOf(),
    var equippedWeapon: Weapon? = null
)"""

replacement = """    var inventory: MutableList<InventoryItem> = mutableListOf(),
    var level: Int = 1,
    var xp: Int = 0,
    var skillPoints: Int = 0,
    var skills: MutableMap<String, Int> = mutableMapOf("combat" to 0, "defense" to 0, "production" to 0, "rental" to 0),
    var equippedWeapon: Weapon? = null
)"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
