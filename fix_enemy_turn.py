import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target1 = """                val isSpecialAttack = (currentTurn % 3 == 0)
                val baseEnemyAtk = session.boss.attack
                val defenseReduction = (player.skills["obrana"] ?: 0) * 2"""

replacement1 = """                val isSpecialAttack = (currentTurn % 3 == 0)
                val baseEnemyAtk = session.boss.attack
                val defenseReduction = defenseSkill * 2.5f"""
text = text.replace(target1, replacement1)

target2 = """                if (newPlayerHp <= 0) {
                    isOver = true
                    victory = false
                    newPlayerHp = 25
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "defeat",
                        message = "💀 Byl jsi v boji poražen! Tví poddaní tě odnesli zpět do bezpečí pevnosti."
                    ))
                    addLog("💀 Pán utrpěl porážku v boji proti ${session.boss.name}!")
                }
            }
        }

        player.hp = newPlayerHp
        _combatState.value = session.copy("""

replacement2 = """                if (newPlayerHp <= 0) {
                    isOver = true
                    victory = false
                    newPlayerHp = if (session.deployedCharacterId != null) 1 else 25
                    val msgName = if (session.deployedCharacterId != null) "Tvá dívka padla v boji" else "Byl jsi v boji poražen"
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "defeat",
                        message = "💀 $msgName! Odnášíte zraněné do bezpečí pevnosti."
                    ))
                    addLog("💀 Porážka v boji proti ${session.boss.name}!")
                }
            }
        }

        if (session.deployedCharacterId != null) {
            val updatedCharacters = currentGameState.characters.map { c ->
                if (c.id == session.deployedCharacterId) c.copy(hp = newPlayerHp) else c
            }
            updateState { it.copy(characters = updatedCharacters) }
        } else {
            player.hp = newPlayerHp
        }
        
        _combatState.value = session.copy("""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
