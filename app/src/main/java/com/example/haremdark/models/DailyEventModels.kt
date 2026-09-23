package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyEvent(
    val title: String,
    val description: String,
    val type: EventType,
    val impact: String
)

enum class EventType { CHALLENGE, OPPORTUNITY }

object DailyEventSystem {
    val potentialEvents = listOf(
        DailyEvent("Náhlá daňová kontrola", "Inkvizice požaduje nečekanou daň z tvých zásob.", EventType.CHALLENGE, "-50 Zlatých"),
        DailyEvent("Neznámý posel", "Do sídla dorazil posel s nabídkou vzácného artefaktu.", EventType.OPPORTUNITY, "+1 Vzácný předmět"),
        DailyEvent("Šeptání v uličkách", "Dozvěděl ses o tajné aukci otrokyň.", EventType.OPPORTUNITY, "+1 Nová akce v Aukci"),
        DailyEvent("Vzpoura v podsvětí", "Tvá mafiánská území čelí útokům.", EventType.CHALLENGE, "-10 Vliv")
    )

    fun generateDailyEvents(count: Int = 2): List<DailyEvent> {
        return potentialEvents.shuffled().take(count)
    }
}
