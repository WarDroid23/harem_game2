package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class SkillNode(
    val id: String,
    val title: String,
    val description: String,
    val cost: Int,
    val requiresId: String? = null,
    val icon: String = "✨"
)

object SkillTreeData {
    val nodes = listOf(
        SkillNode("trade_1", "Zlatý obchod", "Zvyšuje příjmy z pronájmu otrokyň o 10%.", 1, null, "💰"),
        SkillNode("trade_2", "Mistrovské obchody", "Zvyšuje příjmy z pronájmu otrokyň o dalších 15%.", 2, "trade_1", "💎"),
        SkillNode("influence_1", "Harémové charisma", "Zvyšuje zisk vlivu při interakcích o 5.", 1, null, "👑"),
        SkillNode("influence_2", "Politický vliv", "Zvyšuje zisk vlivu při interakcích o dalších 10.", 2, "influence_1", "🏛️")
    )
}
