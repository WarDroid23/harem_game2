import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

target = """        InventoryItem("hojivy_balzam", "Hojivý balzám", "Okamžitě uzdravuje 45 HP pánovi nebo dívce.", 3, 25, "combat", "🧪", "Běžný", "+45 HP", null, 0, 0, 0),"""
replacement = """        InventoryItem("hojivy_balzam", "Hojivý balzám", "Okamžitě uzdravuje 45 HP pánovi nebo dívce.", 3, 25, "combat", "🧪", "Běžný", "+45 HP", null, 0, 0, 0),
        InventoryItem("krvavy_mec", "Krvavý meč", "Zvyšuje útok.", 1, 100, "equipment", "🗡️", "Vzácný", "+15 Boj", "weapon", 15, 0, 0),
        InventoryItem("stribrna_zbroj", "Stříbrná zbroj", "Zvyšuje obranu a životy.", 1, 120, "equipment", "🛡️", "Vzácný", "+10 Obrana, +20 HP", "armor", 0, 10, 20),
        InventoryItem("nahrdelnik_odvahy", "Náhrdelník odvahy", "Magický doplněk.", 2, 80, "equipment", "💍", "Epický", "+5 Boj, +5 Obrana", "accessory", 5, 5, 0),"""
text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
