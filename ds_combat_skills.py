import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

# 1. Add Vitality to Max HP in startCombat
old_start = """                val hpBonus = char.equipment.values.filterNotNull().sumOf { it.hpBonus }
                fighterName = char.name"""
new_start = """                val hpBonus = char.equipment.values.filterNotNull().sumOf { it.hpBonus } + ((char.skills["vitality"] ?: 0) * 10)
                fighterName = char.name"""
text = text.replace(old_start, new_start)

# 2. Add bloodlust logic in attackBoss (Player attacks)
old_combat_dmg = """                val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                val defBonus = char.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                
                combatSkill += combatBonus
                defenseSkill += defBonus
                
                if (char.archetypeId in listOf("odvazna", "vzdorna", "krvava_subka", "zlomena")) {
                    combatSkill = (combatSkill * 1.2).toInt() // +20% bonus for warriors
                }"""
                
new_combat_dmg = """                val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                val defBonus = char.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                
                combatSkill += combatBonus + ((char.skills["combat"] ?: 0) * 5)
                defenseSkill += defBonus + ((char.skills["defense"] ?: 0) * 2)
                
                if (char.archetypeId in listOf("odvazna", "vzdorna", "krvava_subka", "zlomena")) {
                    combatSkill = (combatSkill * 1.2).toInt() // +20% bonus for warriors
                }
                
                // Bloodlust: apply bleed to enemy
                val bloodlustLvl = char.skills["bloodlust"] ?: 0
                if (bloodlustLvl > 0 && Random.nextInt(100) < (bloodlustLvl * 15)) {
                    session.enemyBleedTurns = 2
                    addLog("🩸 Krvavá žízeň! ${char.name} způsobila krvácení.")
                }"""

text = text.replace(old_combat_dmg, new_combat_dmg)

# 3. Add iron_skin logic in Enemy Attack phase
old_enemy_atk = """                val dodgeChance = 10 + (defenseSkill / 2)
                if (Random.nextInt(100) < dodgeChance) {"""
new_enemy_atk = """                val ironSkinLvl = char?.skills?.get("iron_skin") ?: 0
                val blockChance = ironSkinLvl * 15
                val dodgeChance = 10 + (defenseSkill / 2)
                if (Random.nextInt(100) < blockChance) {
                    finalEnemyDmg = 0
                    defenseNotice = " (Zablokováno Železnou kůží!)"
                } else if (Random.nextInt(100) < dodgeChance) {"""
text = text.replace(old_enemy_atk, new_enemy_atk)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)

