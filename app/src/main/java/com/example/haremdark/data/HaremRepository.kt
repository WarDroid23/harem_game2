package com.example.haremdark.data

import com.example.haremdark.models.AffinityPointRecord
import com.example.haremdark.models.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Result bundle for affinity alterations, detailing points gained and tier promotion.
 */
data class AffinityUpdateResult(
    val character: Character,
    val previousLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val currentPoints: Int
)

/**
 * Basic repository interface for managing Harem Characters and their primary attributes
 * such as loyalty, strength, and affinity levels.
 */
interface HaremRepository {
    val charactersFlow: StateFlow<List<Character>>

    fun getAllCharacters(): List<Character>
    fun getCharacterById(id: String): Character?

    /**
     * Updates the loyalty attribute (0 to 100).
     */
    fun updateLoyalty(characterId: String, delta: Int): Character?

    /**
     * Updates the strength attribute (combat power, min 1).
     */
    fun updateStrength(characterId: String, delta: Int): Character?

    /**
     * Adds or adjusts affinity points and recalculates affinity level.
     */
    fun updateAffinity(characterId: String, pointsDelta: Int, currentDay: Int = 1, source: String = "Interakce"): AffinityUpdateResult?

    /**
     * Explicitly sets the affinity level (1 to 6).
     */
    fun setAffinityLevel(characterId: String, newLevel: Int): Character?

    /**
     * Adds a new character to the harem.
     */
    fun addCharacter(character: Character)

    /**
     * Removes a character by ID.
     */
    fun removeCharacter(characterId: String): Boolean

    /**
     * Replaces or updates the entire character record.
     */
    fun updateCharacter(updated: Character): Character?

    /**
     * Refreshes the repository's internal list from an external source (e.g. GameState).
     */
    fun syncCharacters(characters: List<Character>)
}

/**
 * Thread-safe in-memory repository implementation with reactive StateFlow dispatch.
 */
class HaremRepositoryImpl(
    initialList: List<Character> = emptyList(),
    private val onDataChanged: ((List<Character>) -> Unit)? = null
) : HaremRepository {

    private val _charactersFlow = MutableStateFlow(initialList)
    override val charactersFlow: StateFlow<List<Character>> = _charactersFlow.asStateFlow()

    override fun getAllCharacters(): List<Character> = _charactersFlow.value

    override fun getCharacterById(id: String): Character? =
        _charactersFlow.value.find { it.id == id }

    override fun updateLoyalty(characterId: String, delta: Int): Character? {
        var updatedCharacter: Character? = null
        _charactersFlow.update { list ->
            list.map { char ->
                if (char.id == characterId) {
                    val newLoyalty = (char.loajalita + delta).coerceIn(0, 100)
                    char.loajalita = newLoyalty
                    char.loyalty = newLoyalty
                    updatedCharacter = char
                    char
                } else char
            }
        }
        notifyChanges()
        return updatedCharacter
    }

    override fun updateStrength(characterId: String, delta: Int): Character? {
        var updatedCharacter: Character? = null
        _charactersFlow.update { list ->
            list.map { char ->
                if (char.id == characterId) {
                    val newStrength = (char.strength + delta).coerceAtLeast(1)
                    char.strength = newStrength
                    // Also mirror into combat skill if present
                    val currCombat = char.skills["combat"] ?: 0
                    char.skills["combat"] = (currCombat + (delta / 5)).coerceAtLeast(0)
                    updatedCharacter = char
                    char
                } else char
            }
        }
        notifyChanges()
        return updatedCharacter
    }

    override fun updateAffinity(
        characterId: String,
        pointsDelta: Int,
        currentDay: Int,
        source: String
    ): AffinityUpdateResult? {
        var result: AffinityUpdateResult? = null
        _charactersFlow.update { list ->
            list.map { char ->
                if (char.id == characterId) {
                    val prevPoints = char.affinityPoints
                    val prevLevel = char.affinityLevel
                    val newPoints = (prevPoints + pointsDelta).coerceAtLeast(0)
                    val newLevel = AffinityData.getLevelForPoints(newPoints)

                    char.affinityPoints = newPoints
                    char.affinityLevel = newLevel
                    char.affinityHistory.add(AffinityPointRecord(currentDay, newPoints, source))

                    result = AffinityUpdateResult(
                        character = char,
                        previousLevel = prevLevel,
                        newLevel = newLevel,
                        leveledUp = newLevel > prevLevel,
                        currentPoints = newPoints
                    )
                    char
                } else char
            }
        }
        notifyChanges()
        return result
    }

    override fun setAffinityLevel(characterId: String, newLevel: Int): Character? {
        val clampedLevel = newLevel.coerceIn(1, 6)
        var updated: Character? = null
        _charactersFlow.update { list ->
            list.map { char ->
                if (char.id == characterId) {
                    char.affinityLevel = clampedLevel
                    val minPointsForLevel = AffinityData.getPointsThreshold(clampedLevel)
                    if (char.affinityPoints < minPointsForLevel) {
                        char.affinityPoints = minPointsForLevel
                    }
                    updated = char
                    char
                } else char
            }
        }
        notifyChanges()
        return updated
    }

    override fun addCharacter(character: Character) {
        _charactersFlow.update { current ->
            if (current.any { it.id == character.id }) current
            else current + character
        }
        notifyChanges()
    }

    override fun removeCharacter(characterId: String): Boolean {
        var removed = false
        _charactersFlow.update { current ->
            val filtered = current.filterNot { it.id == characterId }
            if (filtered.size != current.size) {
                removed = true
                filtered
            } else current
        }
        if (removed) notifyChanges()
        return removed
    }

    override fun updateCharacter(updated: Character): Character? {
        var found = false
        _charactersFlow.update { current ->
            current.map {
                if (it.id == updated.id) {
                    found = true
                    updated
                } else it
            }
        }
        if (found) notifyChanges()
        return if (found) updated else null
    }

    override fun syncCharacters(characters: List<Character>) {
        _charactersFlow.value = characters
    }

    private fun notifyChanges() {
        onDataChanged?.invoke(_charactersFlow.value)
    }
}
