package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
data class GuildMember(
    val characterId: String,
    val name: String,
    val contribution: Int = 0
)

@Serializable
data class GuildBoss(
    val id: String,
    val name: String,
    val maxHp: Int,
    var currentHp: Int
)

@Serializable
data class GuildMessage(
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Guild(
    val id: String,
    val name: String,
    val members: MutableList<GuildMember> = mutableListOf(),
    var goldDonated: Int = 0,
    var guildBoss: GuildBoss? = null,
    val messages: MutableList<GuildMessage> = mutableListOf()
)
