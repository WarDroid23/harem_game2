with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

import re

old_logic = """        val cost = (building.baseCost * (building.level + 1))
        if (current.player.gold < cost) {
            return Pair(false, "Vylepšení vyžaduje $cost zlatých (máš ${current.player.gold})!")
        }

        val nextLevel = building.level + 1
        val msg = "🏰 Budova ${building.name} vylepšena na úroveň $nextLevel!"
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - cost).coerceAtLeast(0))"""

new_logic = """        val costGold = (building.baseCost * (building.level + 1))
        val costWood = (building.baseCostWood * (building.level + 1))
        val costStone = (building.baseCostStone * (building.level + 1))
        val costIron = (building.baseCostIron * (building.level + 1))
        
        if (current.player.gold < costGold || current.player.wood < costWood || current.player.stone < costStone || current.player.iron < costIron) {
            return Pair(false, "Nedostatek surovin! Potřebuješ: $costGold zl, $costWood dřeva, $costStone kamení, $costIron železa.")
        }

        val nextLevel = building.level + 1
        val msg = "🏰 Budova ${building.name} vylepšena na úroveň $nextLevel!"
        updateState { state ->
            val p = state.player.copy(
                gold = (state.player.gold - costGold).coerceAtLeast(0),
                wood = (state.player.wood - costWood).coerceAtLeast(0),
                stone = (state.player.stone - costStone).coerceAtLeast(0),
                iron = (state.player.iron - costIron).coerceAtLeast(0)
            )"""

text = text.replace(old_logic, new_logic)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
