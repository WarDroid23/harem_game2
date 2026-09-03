import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = "resourceManager.applyYield(p, modifiedYield)"
replacement = """resourceManager.applyYield(p, modifiedYield)
            if (yield.wood > 0 || yield.stone > 0 || yield.iron > 0 || yield.mana > 0) {
                addLog("🏘️ Dominium vyprodukovalo: +${yield.wood} dreva, +${yield.stone} kameni, +${yield.iron} zeleza, +${yield.mana} many. Populace vzrostla o ${yield.populationGrowth}.")
            }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
