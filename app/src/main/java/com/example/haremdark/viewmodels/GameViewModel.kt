package com.example.haremdark.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haremdark.domain.DomainResourceManager
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.BondingLevel
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.RelStatus
import com.example.haremdark.models.getRelationship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Data class representing the current domain resources and capacity state.
 */
data class DomainResources(
    val gold: Int = 0,
    val wood: Int = 0,
    val stone: Int = 0,
    val iron: Int = 0,
    val mana: Int = 0,
    val maxMana: Int = 100,
    val sexEnergy: Int = 0,
    val maxSexEnergy: Int = 100,
    val darkEnergy: Int = 0,
    val maxDarkEnergy: Int = 100,
    val influence: Int = 0,
    val maxInfluence: Int = 100,
    val manaEssence: Int = 0,
    val population: Int = 0,
    val maxPopulation: Int = 50,
    val dailyYieldGold: Int = 0,
    val dailyYieldWood: Int = 0,
    val dailyYieldStone: Int = 0,
    val dailyYieldIron: Int = 0,
    val dailyYieldMana: Int = 0,
    val dailyYieldManaEssence: Int = 0
)

/**
 * Summary of a character's active relationship with the Lord/Player.
 */
data class ActiveRelationship(
    val characterId: String,
    val characterName: String,
    val archetypeId: String,
    val relationshipStatus: RelStatus,
    val bondingLevel: BondingLevel,
    val affinityLevel: Int,
    val affinityPoints: Int,
    val loyalty: Int,
    val affection: Int,
    val obedience: Int,
    val morale: Int,
    val lust: Int,
    val fear: Int,
    val isWife: Boolean,
    val isPartner: Boolean,
    val isFavorite: Boolean,
    val activeBuffTitle: String,
    val activeBuffDescription: String,
    val activeBuffValue: Float,
    val dailyTalksRemaining: Int,
    val dailyGiftsRemaining: Int
)

/**
 * Overview statistics of the character list.
 */
data class CharacterListSummary(
    val totalCharacters: Int = 0,
    val wivesCount: Int = 0,
    val partnersCount: Int = 0,
    val favoritesCount: Int = 0,
    val rentedCount: Int = 0,
    val pregnantCount: Int = 0,
    val averageLoyalty: Int = 0,
    val averageAffection: Int = 0,
    val averageMorale: Int = 0
)

/**
 * ViewModel managing the primary game state, including domain resources,
 * character list, and active relationships.
 */
class GameViewModel(private val engine: GameEngine) : ViewModel() {

    // 1. Direct GameSave state
    val gameState: StateFlow<GameSave> = engine.gameState

    // 2. Current Domain Resources
    val resources: StateFlow<DomainResources> = engine.gameState
        .map { save ->
            val player = save.player
            val yield = DomainResourceManager().calculateDailyYield(save)
            DomainResources(
                gold = player.gold,
                wood = player.wood,
                stone = player.stone,
                iron = player.iron,
                mana = player.mana,
                maxMana = player.maxMana,
                sexEnergy = player.sexEnergy,
                maxSexEnergy = player.maxSexEnergy,
                darkEnergy = player.darkEnergy,
                maxDarkEnergy = player.maxDarkEnergy,
                influence = player.influence,
                maxInfluence = player.maxInfluence,
                manaEssence = player.manaEssence,
                population = player.population,
                maxPopulation = player.maxPopulation,
                dailyYieldGold = yield.gold,
                dailyYieldWood = yield.wood,
                dailyYieldStone = yield.stone,
                dailyYieldIron = yield.iron,
                dailyYieldMana = yield.mana,
                dailyYieldManaEssence = yield.manaEssence
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DomainResources()
        )

    // 3. Character List
    val characterList: StateFlow<List<Character>> = engine.gameState
        .map { it.characters }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = engine.gameState.value.characters
        )

    // Character List Summary
    val characterSummary: StateFlow<CharacterListSummary> = characterList
        .map { characters ->
            if (characters.isEmpty()) {
                CharacterListSummary()
            } else {
                CharacterListSummary(
                    totalCharacters = characters.size,
                    wivesCount = characters.count { it.jeManzelkou },
                    partnersCount = characters.count { it.partnerka },
                    favoritesCount = characters.count { it.oblibena },
                    rentedCount = characters.count { it.naNajmu },
                    pregnantCount = characters.count { it.tehotna },
                    averageLoyalty = characters.map { it.loajalita }.average().toInt(),
                    averageAffection = characters.map { it.srdce }.average().toInt(),
                    averageMorale = characters.map { it.morale }.average().toInt()
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CharacterListSummary()
        )

    // 4. Active Relationships
    val activeRelationships: StateFlow<List<ActiveRelationship>> = engine.gameState
        .map { save ->
            save.characters.map { char ->
                val rel = char.getRelationship()
                val bonding = BondingLevel.fromAffinity(char.affinityPoints)
                ActiveRelationship(
                    characterId = char.id,
                    characterName = char.name,
                    archetypeId = char.archetypeId,
                    relationshipStatus = rel,
                    bondingLevel = bonding,
                    affinityLevel = char.affinityLevel,
                    affinityPoints = char.affinityPoints,
                    loyalty = char.loajalita,
                    affection = char.srdce,
                    obedience = char.poslusnost,
                    morale = char.morale,
                    lust = char.touha,
                    fear = char.strach,
                    isWife = char.jeManzelkou,
                    isPartner = char.partnerka,
                    isFavorite = char.oblibena,
                    activeBuffTitle = rel.title,
                    activeBuffDescription = rel.description,
                    activeBuffValue = rel.buffValue,
                    dailyTalksRemaining = char.dailyTalksRemaining,
                    dailyGiftsRemaining = char.dailyGiftsRemaining
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 5. Game Logs
    val gameLogs: StateFlow<List<String>> = engine.gameState
        .map { it.gameLog }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 6. Active Progression & Relationship Encounter
    val activeProgressionEncounter: StateFlow<com.example.haremdark.domain.ProgressionEncounter?> = engine.activeProgressionEncounter

    fun triggerRandomProgressionEncounter(): Boolean {
        return engine.triggerRandomProgressionEncounter()
    }

    fun dismissProgressionEncounter() {
        engine.dismissProgressionEncounter()
    }

    fun resolveProgressionEncounterChoice(choice: com.example.haremdark.domain.ProgressionEncounterChoice) {
        engine.resolveProgressionEncounterChoice(choice)
    }

    // Selection State
    val selectedCharacterId = MutableStateFlow<String?>(null)

    val selectedCharacter: StateFlow<Character?> = combine(
        characterList,
        selectedCharacterId
    ) { list, id ->
        list.find { it.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Resource Management Actions
    fun spendGold(amount: Int, reason: String? = null): Boolean {
        return engine.spendGold(amount, reason)
    }

    fun earnGold(amount: Int, reason: String? = null) {
        engine.earnGold(amount, reason)
    }

    fun sellResource(type: String, amount: Int, pricePerUnit: Int) {
        engine.sellResource(type, amount, pricePerUnit)
    }

    fun restNextDay(meditative: Boolean = false) {
        engine.restNextDay(meditative)
    }

    // Character Management Actions
    fun selectCharacter(id: String?) {
        selectedCharacterId.value = id
    }

    fun levelUpCharacter(characterId: String): Pair<Boolean, String> {
        return engine.levelUpCharacter(characterId)
    }

    fun sellCharacter(characterId: String): Boolean {
        return engine.sellCharacter(characterId)
    }

    fun leaseCharacter(characterId: String, durationDays: Int): Boolean {
        return engine.leaseCharacter(characterId, durationDays)
    }

    fun toggleFavorite(characterId: String) {
        engine.updateState { state ->
            val updated = state.characters.map { c ->
                if (c.id == characterId) c.copy(oblibena = !c.oblibena) else c
            }
            state.copy(characters = updated)
        }
    }

    fun togglePinned(characterId: String) {
        engine.updateState { state ->
            val updated = state.characters.map { c ->
                if (c.id == characterId) c.copy(isPinned = !c.isPinned) else c
            }
            state.copy(characters = updated)
        }
    }

    // Relationship Actions
    fun executeBondingInteraction(characterId: String, interactionType: String): Pair<Boolean, String> {
        return engine.executeBondingInteraction(characterId, interactionType)
    }

    fun giveInventoryGift(characterId: String, itemId: String): Pair<Boolean, String> {
        return engine.giveInventoryGift(characterId, itemId)
    }

    fun buyAndGiveDirectGift(
        characterId: String,
        giftName: String,
        goldCost: Int,
        loyaltyBoost: Int,
        desireBoost: Int,
        obedienceBoost: Int,
        trustBoost: Int,
        flavorText: String
    ): Pair<Boolean, String> {
        return engine.buyAndGiveDirectGift(
            characterId, giftName, goldCost, loyaltyBoost, desireBoost, obedienceBoost, trustBoost, flavorText
        )
    }

    fun setRelationshipRole(characterId: String, role: String, isWife: Boolean = false, isPartner: Boolean = false) {
        engine.updateState { state ->
            val updated = state.characters.map { c ->
                if (c.id == characterId) {
                    c.copy(role = role, jeManzelkou = isWife, partnerka = isPartner)
                } else c
            }
            state.copy(characters = updated)
        }
    }

    fun addLog(message: String) {
        engine.addLog(message)
    }
}
