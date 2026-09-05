import re

with open('app/src/main/java/com/example/haremdark/ui/components/GameTopBar.kt', 'r') as f:
    text = f.read()

target = """                    Column {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("""

replacement = """                    Column {
                        var displayName = player.name
                        if (player.activeTitle != null) {
                            val tObj = com.example.haremdark.models.AchievementList.allAchievements.find { it.id == player.activeTitle }
                            if (tObj != null) displayName = "${tObj.badgeIcon} " + displayName
                        }
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/components/GameTopBar.kt', 'w') as f:
    f.write(text)
