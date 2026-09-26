package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class SkillNode(
    val id: String,
    val title: String,
    val description: String,
    val cost: Int,
    val requiresId: String? = null,
    val icon: String = "✨",
    val isActiveCombatSkill: Boolean = false, // Active or Passive
    val damageMultiplier: Float = 0f, // For combat
    val minLevel: Int = 1,
    val minRarity: String = "Common" // Common, Rare, Epic, Legendary
)

object SkillTreeData {
    val nodes = listOf(
        // Passive nodes
        SkillNode("trade_1", "Zlatý obchod", "Zvyšuje příjmy z pronájmu otrokyň o 10%.", 1, null, "💰"),
        SkillNode("trade_2", "Mistrovské obchody", "Zvyšuje příjmy z pronájmu otrokyň o dalších 15%.", 2, "trade_1", "💎"),
        
        // Active combat nodes
        SkillNode("fireball_1", "Ohnivá koule", "Vystřelí ohnivou kouli, způsobí 1.5x poškození.", 1, null, "🔥", true, 1.5f, 5, "Common"),
        SkillNode("shadow_strike_1", "Stínový úder", "Smrtící úder ze stínů, způsobí 2x poškození.", 2, null, "🌑", true, 2.0f, 10, "Rare"),
        SkillNode("supernova_1", "Supernova", "Zničí nepřátele extrémní energií, způsobí 4x poškození.", 5, null, "✨", true, 4.0f, 20, "Legendary")
    )
}
