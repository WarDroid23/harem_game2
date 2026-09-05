import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """            current.copy(
                player = p,
                characters = updatedCharacters,
                buildings = updatedBuildings,
                dailyMissions = if (newDay > current.lastMissionUpdateDay) newMissions else current.dailyMissions,
                lastMissionUpdateDay = if (newDay > current.lastMissionUpdateDay) newDay else current.lastMissionUpdateDay,
                gameLog = logs,
                activeBuffs = newBuffs,
                resourceHistory = updatedResourceHistory
            )
        }
        autoSave()
    }"""
    
replacement = """            current.copy(
                player = p,
                characters = updatedCharacters,
                buildings = updatedBuildings,
                dailyMissions = if (newDay > current.lastMissionUpdateDay) newMissions else current.dailyMissions,
                lastMissionUpdateDay = if (newDay > current.lastMissionUpdateDay) newDay else current.lastMissionUpdateDay,
                gameLog = logs,
                activeBuffs = newBuffs,
                resourceHistory = updatedResourceHistory
            )
        }
        
        val newAchs = checkAchievements()
        if (newAchs.isNotEmpty()) {
            newAchs.forEach { achId ->
                val ach = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == achId }
                if (ach != null) {
                    addLog("🏆 ÚSPĚCH ODBLOKOVÁN: ${ach.badgeIcon} ${ach.title} - ${ach.description}")
                }
            }
        }
        
        autoSave()
    }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
