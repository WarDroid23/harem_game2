import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

new_methods = """
    fun equipItemToCharacter(characterId: String, itemId: String, slotId: String) {
        updateState { current ->
            val player = current.player
            val itemIndex = player.items.indexOfFirst { it.id == itemId && it.count > 0 }
            if (itemIndex == -1) return@updateState current
            
            val itemToEquip = player.items[itemIndex]
            
            val updatedCharacters = current.characters.map { char ->
                if (char.id == characterId) {
                    val currentEquipped = char.equipment[slotId]
                    
                    // Put old item back in inventory if exists
                    val newItems = player.items.toMutableList()
                    if (currentEquipped != null) {
                        val existingItemIdx = newItems.indexOfFirst { it.id == currentEquipped.id }
                        if (existingItemIdx != -1) {
                            val ei = newItems[existingItemIdx]
                            newItems[existingItemIdx] = ei.copy(count = ei.count + 1)
                        } else {
                            newItems.add(currentEquipped.copy(count = 1))
                        }
                    }
                    
                    // Remove 1 from inventory for the new item
                    val newEquipIdx = newItems.indexOfFirst { it.id == itemId }
                    val ne = newItems[newEquipIdx]
                    if (ne.count > 1) {
                        newItems[newEquipIdx] = ne.copy(count = ne.count - 1)
                    } else {
                        newItems.removeAt(newEquipIdx)
                    }
                    
                    // Equip
                    val newEquipMap = char.equipment.toMutableMap()
                    newEquipMap[slotId] = itemToEquip.copy(count = 1)
                    
                    current.player.items = newItems
                    
                    char.copy(equipment = newEquipMap)
                } else char
            }
            current.copy(characters = updatedCharacters)
        }
        autoSave()
    }
    
    fun unequipItemFromCharacter(characterId: String, slotId: String) {
        updateState { current ->
            val player = current.player
            val updatedCharacters = current.characters.map { char ->
                if (char.id == characterId) {
                    val currentEquipped = char.equipment[slotId]
                    if (currentEquipped != null) {
                        val newItems = player.items.toMutableList()
                        val existingItemIdx = newItems.indexOfFirst { it.id == currentEquipped.id }
                        if (existingItemIdx != -1) {
                            val ei = newItems[existingItemIdx]
                            newItems[existingItemIdx] = ei.copy(count = ei.count + 1)
                        } else {
                            newItems.add(currentEquipped.copy(count = 1))
                        }
                        
                        val newEquipMap = char.equipment.toMutableMap()
                        newEquipMap[slotId] = null
                        
                        current.player.items = newItems
                        char.copy(equipment = newEquipMap)
                    } else {
                        char
                    }
                } else char
            }
            current.copy(characters = updatedCharacters)
        }
        autoSave()
    }
"""

text = text.rstrip()
if text.endswith('}'):
    text = text[:-1] + new_methods + "\n}\n"

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
