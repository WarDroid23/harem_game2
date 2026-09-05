import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """            player.gold += session.boss.rewardGold
            addPlayerXp(session.boss.rewardXp)
            player.killCount += 1
            lootInfo = "+${session.boss.rewardGold} zlatých • +${session.boss.rewardXp} XP"
            newLogEntries.add(0, CombatLogEntry(
                turn = currentTurn,
                type = "victory",
                message = "🏆 VÍTĚZSTVÍ! Protivník ${session.boss.name} padl! Zisk: $lootInfo."
            ))"""

replacement = """            player.gold += session.boss.rewardGold
            addPlayerXp(session.boss.rewardXp)
            player.killCount += 1
            
            var droppedItem: com.example.haremdark.models.InventoryItem? = null
            if (Random.nextInt(100) < 35) { // 35% chance to drop item
                val possibleDrops = listOf(
                    com.example.haremdark.models.InventoryItem("hojivy_balzam", "Hojivý balzám", "Okamžitě uzdravuje 45 HP.", 1, 25, "combat", "🧪", "Běžný", "+45 HP", null, 0, 0, 0),
                    com.example.haremdark.models.InventoryItem("krvavy_mec", "Krvavý meč", "Zvyšuje útok.", 1, 100, "equipment", "🗡️", "Vzácný", "+15 Boj", "weapon", 15, 0, 0),
                    com.example.haremdark.models.InventoryItem("stribrna_zbroj", "Stříbrná zbroj", "Zvyšuje obranu.", 1, 120, "equipment", "🛡️", "Vzácný", "+10 Obrana", "armor", 0, 10, 0)
                )
                droppedItem = possibleDrops.random()
                val items = player.items.toMutableList()
                val existingItemIdx = items.indexOfFirst { it.id == droppedItem.id }
                if (existingItemIdx != -1) {
                    val ei = items[existingItemIdx]
                    items[existingItemIdx] = ei.copy(count = ei.count + 1)
                } else {
                    items.add(droppedItem)
                }
                player.items = items
            }
            
            val itemDropStr = if (droppedItem != null) " • Nalezeno: ${droppedItem.name}" else ""
            lootInfo = "+${session.boss.rewardGold} zlatých • +${session.boss.rewardXp} XP$itemDropStr"
            
            newLogEntries.add(0, CombatLogEntry(
                turn = currentTurn,
                type = "victory",
                message = "🏆 VÍTĚZSTVÍ! Protivník ${session.boss.name} padl! Zisk: $lootInfo."
            ))"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
