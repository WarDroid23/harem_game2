package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class CharacterArchetype(
    val id: String,
    val name: String,
    val description: String,
    val submisivitaMod: Float = 1.0f,
    val poslusnostMod: Float = 1.0f,
    val duveraMod: Float = 1.0f,
    val strachMod: Float = 1.0f,
    val touhaMod: Float = 1.0f,
    val vlhkostMod: Float = 1.0f,
    val humiliationMod: Float = 1.0f,
    val painAddictionMod: Float = 1.0f,
    val mindbreakMod: Float = 1.0f,
    val scarredMod: Float = 1.0f,
    val reakceNaTrest: Float = 1.0f,
    val reakceNaOdmenu: Float = 1.0f,
    val utekSance: Float = 0.05f
)

@Serializable
data class DegradationPhase(
    val level: Int,
    val name: String,
    val description: String,
    val bonuses: Map<String, Int> = emptyMap(),
    val reqZavislost: Int = 0,
    val reqBroken: Int = 0,
    val reqTouha: Int = 0,
    val reqHumiliation: Int = 0,
    val reqMindbreak: Int = 0,
    val reqPoslusnost: Int = 0,
    val reqPainAddiction: Int = 0,
    val reqScarred: Int = 0,
    val reqLoajalita: Int = 0,
    val reqAge: Int = 0,
    val reqPregnant: Boolean = false
)

@Serializable
data class LoyaltyTier(
    val id: String,
    val min: Int,
    val max: Int,
    val title: String,
    val description: String,
    val escapeMod: Float,
    val rewardMod: Float,
    val punishMod: Float,
    val colorHex: Long
)
