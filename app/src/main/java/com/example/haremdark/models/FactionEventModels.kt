package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class FactionType(val title: String) {
    MERCHANT_GUILD("Obchodní cech"),
    INQUISITION("Inkvizice"),
    RIVAL_LORD("Soupeřící lord"),
    CRIMINAL_SYNDICATE("Zločinecký syndikát")
}

@Serializable
data class FactionEvent(
    val id: String,
    val faction: FactionType,
    val title: String,
    val description: String,
    val type: FactionEventType, // TRADE_DEAL, WAR_DECLARATION, TRIBUTE_DEMAND
    val goldImpact: Int = 0,
    val resourceImpact: Map<String, Int> = emptyMap(),
    val influenceImpact: Int = 0,
    val options: List<EventOption>
)

@Serializable
enum class FactionEventType {
    TRADE_DEAL, WAR_DECLARATION, TRIBUTE_DEMAND
}

@Serializable
data class EventOption(
    val text: String,
    val goldReward: Int = 0,
    val resourceReward: Map<String, Int> = emptyMap(),
    val influenceChange: Int = 0,
    val description: String
)

object FactionEventGenerator {
    fun generateRandomEvent(): FactionEvent {
        return when ((0..2).random()) {
            0 -> FactionEvent(
                id = "trade_${System.currentTimeMillis()}",
                faction = FactionType.MERCHANT_GUILD,
                title = "Nabídka obchodu",
                description = "Obchodní cech nabízí výměnu dřeva za zlato.",
                type = FactionEventType.TRADE_DEAL,
                options = listOf(
                    EventOption("Přijmout", goldReward = 200, description = "Získáš 200 zlata, ale ztratíš 100 dřeva.", resourceReward = mapOf("wood" to -100)),
                    EventOption("Odmítnout", description = "Nic se nestane.")
                )
            )
            1 -> FactionEvent(
                id = "war_${System.currentTimeMillis()}",
                faction = FactionType.RIVAL_LORD,
                title = "Vyhlášení války",
                description = "Soupeřící lord zaútočil na tvé hranice!",
                type = FactionEventType.WAR_DECLARATION,
                options = listOf(
                    EventOption("Bránit se", goldReward = -300, influenceChange = 20, description = "Ztratíš 300 zlata, ale zvýšíš vliv."),
                    EventOption("Platit reparace", goldReward = -500, description = "Ztratíš 500 zlata, ale předejdeš bojům.")
                )
            )
            else -> FactionEvent(
                id = "tribute_${System.currentTimeMillis()}",
                faction = FactionType.INQUISITION,
                title = "Požadavek na tribut",
                description = "Inkvizice žádá o příspěvek na svaté tažení.",
                type = FactionEventType.TRIBUTE_DEMAND,
                options = listOf(
                    EventOption("Zaplatit", goldReward = -400, influenceChange = 10, description = "Ztratíš 400 zlata, ale zlepšíš vztah s Inkvizicí."),
                    EventOption("Vzdorovat", influenceChange = -20, description = "Ztratíš 20 vlivu, ale ušetříš zlato.")
                )
            )
        }
    }
}
