with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

target = """    val activeBuffs: List<PartyBuff> = emptyList()
)"""

replacement = """    val activeBuffs: List<PartyBuff> = emptyList(),
    val resourceHistory: List<DailyResourceStat> = emptyList()
)

@Serializable
data class DailyResourceStat(
    val day: Int,
    val goldProduced: Int,
    val manaProduced: Int,
    val woodProduced: Int,
    val stoneProduced: Int,
    val ironProduced: Int
)"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
