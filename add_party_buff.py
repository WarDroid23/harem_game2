import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

party_buff_code = """
@Serializable
data class PartyBuff(
    val id: String,
    val name: String,
    val description: String,
    var durationDays: Int,
    val type: String, // e.g., "DAMAGE", "DEFENSE", "RESOURCE_BOOST"
    val value: Int
)
"""

if "data class PartyBuff" not in text:
    text = text.replace("data class GameSave(", party_buff_code + "\n@Serializable\ndata class GameSave(")

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
