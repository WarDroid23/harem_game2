package com.example.haremdark.data

import com.example.haremdark.models.Element
import com.example.haremdark.models.HaremCharacter
import com.example.haremdark.models.HaremRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Sorting criteria for harem character state queries.
 */
enum class HaremCharacterSort {
    AFFECTION,
    POWER_LEVEL,
    NAME,
    LEVEL,
    LOYALTY,
    ROLE
}

/**
 * Repository interface defining state management contracts for Harem characters.
 */
interface HaremCharacterRepository {
    /** Reactive flow emitting the up-to-date collection of characters. */
    val characters: StateFlow<List<HaremCharacter>>

    /** Snapshot of all characters currently in the harem. */
    fun getAll(): List<HaremCharacter>

    /** Retrieve character by unique identifier. */
    fun getById(id: String): HaremCharacter?

    /** Find character by exact or partial name. */
    fun getByName(name: String): HaremCharacter?

    /** Retrieve all characters fulfilling a specific combat/social role. */
    fun getByRole(role: HaremRole): List<HaremCharacter>

    /** Retrieve list of favorited companions. */
    fun getFavorites(): List<HaremCharacter>

    /** Add a new character to the repository state. */
    fun add(character: HaremCharacter): HaremCharacter

    /** Add multiple characters at once. */
    fun addAll(newCharacters: Collection<HaremCharacter>)

    /** Update an existing character record. */
    fun update(character: HaremCharacter): HaremCharacter?

    /** Remove a character by identifier. */
    fun remove(id: String): Boolean

    /**
     * Increment or decrement character's affection score.
     * @param delta Value to add (or subtract if negative).
     * @return Updated character, or null if not found.
     */
    fun updateAffection(id: String, delta: Int): HaremCharacter?

    /**
     * Directly set character's affection score.
     */
    fun setAffection(id: String, newAffection: Int): HaremCharacter?

    /**
     * Increment or decrement character's combat power level.
     * @param delta Value to add (or subtract if negative).
     * @return Updated character, or null if not found.
     */
    fun updatePowerLevel(id: String, delta: Int): HaremCharacter?

    /**
     * Directly set character's combat power level.
     */
    fun setPowerLevel(id: String, newPowerLevel: Int): HaremCharacter?

    /**
     * Change a character's harem role.
     */
    fun updateRole(id: String, newRole: HaremRole): HaremCharacter?

    /**
     * Toggle the favorite status of a character.
     */
    fun toggleFavorite(id: String): HaremCharacter?

    /**
     * Update health points (and optionally maximum HP).
     */
    fun updateHealth(id: String, currentHp: Int, maxHp: Int? = null): HaremCharacter?

    /**
     * Level up character, scaling power level and max HP.
     */
    fun levelUp(id: String): HaremCharacter?

    /**
     * Update skills.
     */
    fun updateSkills(id: String, skills: List<String>): HaremCharacter?

    /**
     * Update skill points.
     */
    fun updateSkillPoints(id: String, points: Int): HaremCharacter?

    /**
     * Filter characters matching specific criteria.
     */
    fun filter(
        query: String = "",
        role: HaremRole? = null,
        minAffection: Int? = null,
        minPower: Int? = null,
        isFavorite: Boolean? = null
    ): List<HaremCharacter>

    /**
     * Sort characters according to desired metric.
     */
    fun sort(criteria: HaremCharacterSort, ascending: Boolean = false): List<HaremCharacter>

    /**
     * Replaces repository contents (e.g. from persistent storage or network sync).
     */
    fun sync(charactersList: List<HaremCharacter>)

    /**
     * Clear all characters from state.
     */
    fun clear()
}

/**
 * Thread-safe implementation of [HaremCharacterRepository] using [MutableStateFlow].
 */
class HaremCharacterRepositoryImpl(
    initialCharacters: List<HaremCharacter>? = null,
    private val onStateChanged: ((List<HaremCharacter>) -> Unit)? = null
) : HaremCharacterRepository {

    private val _characters = MutableStateFlow(
        initialCharacters ?: defaultHaremRoster()
    )
    override val characters: StateFlow<List<HaremCharacter>> = _characters.asStateFlow()

    override fun getAll(): List<HaremCharacter> = _characters.value

    override fun getById(id: String): HaremCharacter? =
        _characters.value.find { it.id == id }

    override fun getByName(name: String): HaremCharacter? =
        _characters.value.find { it.name.equals(name, ignoreCase = true) }
            ?: _characters.value.find { it.name.contains(name, ignoreCase = true) }

    override fun getByRole(role: HaremRole): List<HaremCharacter> =
        _characters.value.filter { it.role == role }

    override fun getFavorites(): List<HaremCharacter> =
        _characters.value.filter { it.isFavorite }

    override fun add(character: HaremCharacter): HaremCharacter {
        _characters.update { current ->
            if (current.any { it.id == character.id }) {
                current.map { if (it.id == character.id) character else it }
            } else {
                current + character
            }
        }
        notifyStateChanged()
        return character
    }

    override fun addAll(newCharacters: Collection<HaremCharacter>) {
        _characters.update { current ->
            val existingIds = current.map { it.id }.toSet()
            val filteredNew = newCharacters.filterNot { existingIds.contains(it.id) }
            current + filteredNew
        }
        notifyStateChanged()
    }

    override fun update(character: HaremCharacter): HaremCharacter? {
        var found = false
        _characters.update { current ->
            current.map {
                if (it.id == character.id) {
                    found = true
                    character
                } else it
            }
        }
        if (found) notifyStateChanged()
        return if (found) character else null
    }

    override fun remove(id: String): Boolean {
        var removed = false
        _characters.update { current ->
            val filtered = current.filterNot { it.id == id }
            if (filtered.size != current.size) {
                removed = true
                filtered
            } else current
        }
        if (removed) notifyStateChanged()
        return removed
    }

    override fun updateAffection(id: String, delta: Int): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val newAffection = (char.affection + delta).coerceAtLeast(0)
                    val newLoyalty = if (delta > 0) (char.loyalty + (delta / 4)).coerceAtMost(100) else char.loyalty
                    val result = char.copy(
                        affection = newAffection,
                        loyalty = newLoyalty
                    )
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun setAffection(id: String, newAffection: Int): HaremCharacter? {
        var updated: HaremCharacter? = null
        val safeAffection = newAffection.coerceAtLeast(0)
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val result = char.copy(affection = safeAffection)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun updatePowerLevel(id: String, delta: Int): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val newPower = (char.powerLevel + delta).coerceAtLeast(1)
                    val result = char.copy(powerLevel = newPower)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun setPowerLevel(id: String, newPowerLevel: Int): HaremCharacter? {
        var updated: HaremCharacter? = null
        val safePower = newPowerLevel.coerceAtLeast(1)
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val result = char.copy(powerLevel = safePower)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun updateRole(id: String, newRole: HaremRole): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val result = char.copy(role = newRole)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun toggleFavorite(id: String): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val result = char.copy(isFavorite = !char.isFavorite)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun updateHealth(id: String, currentHp: Int, maxHp: Int?): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val targetMaxHp = maxHp?.coerceAtLeast(1) ?: char.maxHp
                    val targetHp = currentHp.coerceIn(0, targetMaxHp)
                    val newStatus = if (targetHp == 0) "V bezvědomí" else if (char.status == "V bezvědomí") "Aktivní" else char.status
                    val result = char.copy(
                        currentHp = targetHp,
                        maxHp = targetMaxHp,
                        status = newStatus
                    )
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun levelUp(id: String): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val newLevel = char.level + 1
                    val powerGain = 25 + (newLevel * 5)
                    val hpGain = 20
                    val result = char.copy(
                        level = newLevel,
                        powerLevel = char.powerLevel + powerGain,
                        maxHp = char.maxHp + hpGain,
                        currentHp = char.currentHp + hpGain,
                        maxMana = char.maxMana + 10,
                        currentMana = char.currentMana + 10
                    )
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun updateSkills(id: String, skills: List<String>): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val result = char.copy(unlockedSkills = skills)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun updateSkillPoints(id: String, points: Int): HaremCharacter? {
        var updated: HaremCharacter? = null
        _characters.update { current ->
            current.map { char ->
                if (char.id == id) {
                    val result = char.copy(availableSkillPoints = points)
                    updated = result
                    result
                } else char
            }
        }
        if (updated != null) notifyStateChanged()
        return updated
    }

    override fun filter(
        query: String,
        role: HaremRole?,
        minAffection: Int?,
        minPower: Int?,
        isFavorite: Boolean?
    ): List<HaremCharacter> {
        return _characters.value.filter { char ->
            val matchesQuery = query.isBlank() ||
                char.name.contains(query, ignoreCase = true) ||
                char.archetypeId.contains(query, ignoreCase = true) ||
                char.title.contains(query, ignoreCase = true)
            val matchesRole = role == null || char.role == role
            val matchesAffection = minAffection == null || char.affection >= minAffection
            val matchesPower = minPower == null || char.powerLevel >= minPower
            val matchesFavorite = isFavorite == null || char.isFavorite == isFavorite

            matchesQuery && matchesRole && matchesAffection && matchesPower && matchesFavorite
        }
    }

    override fun sort(criteria: HaremCharacterSort, ascending: Boolean): List<HaremCharacter> {
        val list = _characters.value
        val sorted = when (criteria) {
            HaremCharacterSort.AFFECTION -> list.sortedBy { it.affection }
            HaremCharacterSort.POWER_LEVEL -> list.sortedBy { it.powerLevel }
            HaremCharacterSort.NAME -> list.sortedBy { it.name.lowercase() }
            HaremCharacterSort.LEVEL -> list.sortedBy { it.level }
            HaremCharacterSort.LOYALTY -> list.sortedBy { it.loyalty }
            HaremCharacterSort.ROLE -> list.sortedBy { it.role.title }
        }
        return if (ascending) sorted else sorted.reversed()
    }

    override fun sync(charactersList: List<HaremCharacter>) {
        _characters.value = charactersList
        notifyStateChanged()
    }

    override fun clear() {
        _characters.value = emptyList()
        notifyStateChanged()
    }

    private fun notifyStateChanged() {
        onStateChanged?.invoke(_characters.value)
    }

    companion object {
        /**
         * Standard canon roster of harem companions for initial startup.
         */
        fun defaultHaremRoster(): List<HaremCharacter> {
            return listOf(
                HaremCharacter(
                    id = "char_aurelia",
                    name = "Aurelia Plamenná",
                    affection = 78,
                    powerLevel = 320,
                    role = HaremRole.WARRIOR,
                    archetypeId = "draci_divka",
                    title = "Dračí princezna",
                    level = 6,
                    loyalty = 85,
                    currentHp = 180,
                    maxHp = 180,
                    element = Element.FIRE,
                    avatarIcon = "🐉",
                    isFavorite = true,
                    bio = "Hrdá dračí válečnice s nezdolným ohněm v srdci. Její loajalita k Pánovi je absolutní."
                ),
                HaremCharacter(
                    id = "char_lilith",
                    name = "Lilith z Hlubin",
                    affection = 85,
                    powerLevel = 290,
                    role = HaremRole.SORCERESS,
                    archetypeId = "sukuba",
                    title = "Královna stínů",
                    level = 5,
                    loyalty = 80,
                    currentHp = 140,
                    maxHp = 140,
                    element = Element.DARK,
                    avatarIcon = "😈",
                    isFavorite = true,
                    bio = "Půvabná a nebezpečná sukuba vládnoucí temným prokletím a podmanivé magii."
                ),
                HaremCharacter(
                    id = "char_elena",
                    name = "Sestra Elena",
                    affection = 64,
                    powerLevel = 210,
                    role = HaremRole.HEALER,
                    archetypeId = "knezkyn",
                    title = "Padlá kněžka",
                    level = 4,
                    loyalty = 72,
                    currentHp = 130,
                    maxHp = 130,
                    element = Element.HOLY,
                    avatarIcon = "✨",
                    bio = "Bývalá svatá kněžka, jejíž čisté světlo a léčivá moc nyní slouží dominatu."
                ),
                HaremCharacter(
                    id = "char_valeriya",
                    name = "Valeriya Krvavá",
                    affection = 55,
                    powerLevel = 275,
                    role = HaremRole.GUARDIAN,
                    archetypeId = "vzdorna",
                    title = "Nezlomná orkyně",
                    level = 5,
                    loyalty = 68,
                    currentHp = 220,
                    maxHp = 220,
                    element = Element.EARTH,
                    avatarIcon = "🛡️",
                    bio = "Urostlá válečnice se štítem těžším než skála. Tvoří živou zeď chránící Pána."
                ),
                HaremCharacter(
                    id = "char_katarina",
                    name = "Katarina Šepot",
                    affection = 70,
                    powerLevel = 260,
                    role = HaremRole.ASSASSIN,
                    archetypeId = "krvava_subka",
                    title = "Čepel stínů",
                    level = 5,
                    loyalty = 75,
                    currentHp = 125,
                    maxHp = 125,
                    element = Element.DARK,
                    avatarIcon = "🗡️",
                    bio = "Tichá vražedkyně schopná zasáhnout nepřítele dříve, než stačí vykřiknout."
                ),
                HaremCharacter(
                    id = "char_freya",
                    name = "Freya Mrazivá",
                    affection = 60,
                    powerLevel = 240,
                    role = HaremRole.SIREN,
                    archetypeId = "chladna",
                    title = "Ledová dáma",
                    level = 4,
                    loyalty = 65,
                    currentHp = 135,
                    maxHp = 135,
                    element = Element.ICE,
                    avatarIcon = "❄️",
                    bio = "Vznešená aristokratka s chladnou myslí a ledovým pohledem, který mrazí vůli nepřátel."
                )
            )
        }
    }
}
