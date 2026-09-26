package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class AllianceType(val title: String, val bonusDescription: String) {
    DEFENSIVE_PACT("Obranný pakt", "Sdílená obrana, +10% k obraně dominiu"),
    TRADE_ALLIANCE("Obchodní aliance", "+15% ke zlatu z obchodů"),
    CULTURAL_EXCHANGE("Kulturní výměna", "+10% k náklonnosti harému")
}

@Serializable
data class Alliance(
    val faction: FactionType,
    val type: AllianceType,
    var durationDays: Int
)
