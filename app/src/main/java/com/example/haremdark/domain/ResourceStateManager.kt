package com.example.haremdark.domain

import com.example.haremdark.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Immutable snapshot of tracked in-game resources for reactive UI observation.
 */
data class GameResources(
    val gold: Int = 500,
    val mana: Int = 50,
    val maxMana: Int = 100,
    val influence: Int = 35,
    val maxInfluence: Int = 100,
    val darkEnergy: Int = 50,
    val maxDarkEnergy: Int = 100,
    val sexEnergy: Int = 100,
    val maxSexEnergy: Int = 100,
    val hp: Int = 100,
    val maxHp: Int = 100
) {
    val manaPercent: Float
        get() = (mana.toFloat() / maxMana.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val influencePercent: Float
        get() = (influence.toFloat() / maxInfluence.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val hpPercent: Float
        get() = (hp.toFloat() / maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val darkEnergyPercent: Float
        get() = (darkEnergy.toFloat() / maxDarkEnergy.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

/**
 * State manager responsible for tracking, modifying, and enforcing business rules
 * on primary player resources (Gold, Mana, Influence, Energy, HP).
 */
class ResourceStateManager(
    initialResources: GameResources = GameResources(),
    private val onResourceChanged: ((GameResources) -> Unit)? = null
) {
    private val _resources = MutableStateFlow(initialResources)
    val resources: StateFlow<GameResources> = _resources.asStateFlow()

    fun current(): GameResources = _resources.value

    /**
     * Synchronizes internal resource state with a Player entity instance.
     */
    fun syncFromPlayer(player: Player) {
        _resources.update {
            it.copy(
                gold = player.gold,
                mana = player.mana,
                maxMana = player.maxMana,
                influence = player.influence,
                maxInfluence = player.maxInfluence,
                darkEnergy = player.darkEnergy,
                maxDarkEnergy = player.maxDarkEnergy,
                sexEnergy = player.sexEnergy,
                maxSexEnergy = player.maxSexEnergy,
                hp = player.hp,
                maxHp = player.maxHp
            )
        }
        notifyChange()
    }

    /**
     * Applies internal state updates directly back to a Player entity.
     */
    fun applyToPlayer(player: Player) {
        val curr = _resources.value
        player.gold = curr.gold
        player.mana = curr.mana
        player.maxMana = curr.maxMana
        player.influence = curr.influence
        player.maxInfluence = curr.maxInfluence
        player.darkEnergy = curr.darkEnergy
        player.maxDarkEnergy = curr.maxDarkEnergy
        player.sexEnergy = curr.sexEnergy
        player.maxSexEnergy = curr.maxSexEnergy
        player.hp = curr.hp
        player.maxHp = curr.maxHp
    }

    // ==========================================
    // GOLD OPERATIONS
    // ==========================================

    fun addGold(amount: Int): Int {
        if (amount <= 0) return _resources.value.gold
        _resources.update { it.copy(gold = (it.gold + amount).coerceAtLeast(0)) }
        notifyChange()
        return _resources.value.gold
    }

    fun spendGold(amount: Int): Boolean {
        if (amount < 0) return false
        val curr = _resources.value.gold
        if (curr < amount) return false
        _resources.update { it.copy(gold = curr - amount) }
        notifyChange()
        return true
    }

    fun hasGold(amount: Int): Boolean = _resources.value.gold >= amount

    // ==========================================
    // MANA OPERATIONS
    // ==========================================

    fun addMana(amount: Int): Int {
        if (amount <= 0) return _resources.value.mana
        _resources.update {
            val newMana = (it.mana + amount).coerceAtMost(it.maxMana)
            it.copy(mana = newMana)
        }
        notifyChange()
        return _resources.value.mana
    }

    fun consumeMana(amount: Int): Boolean {
        if (amount < 0) return false
        val curr = _resources.value.mana
        if (curr < amount) return false
        _resources.update { it.copy(mana = curr - amount) }
        notifyChange()
        return true
    }

    fun hasMana(amount: Int): Boolean = _resources.value.mana >= amount

    // ==========================================
    // INFLUENCE OPERATIONS
    // ==========================================

    fun addInfluence(amount: Int): Int {
        if (amount <= 0) return _resources.value.influence
        _resources.update {
            val newInf = (it.influence + amount).coerceAtMost(it.maxInfluence)
            it.copy(influence = newInf)
        }
        notifyChange()
        return _resources.value.influence
    }

    fun spendInfluence(amount: Int): Boolean {
        if (amount < 0) return false
        val curr = _resources.value.influence
        if (curr < amount) return false
        _resources.update { it.copy(influence = curr - amount) }
        notifyChange()
        return true
    }

    fun hasInfluence(amount: Int): Boolean = _resources.value.influence >= amount

    // ==========================================
    // ENERGY & HP OPERATIONS
    // ==========================================

    fun restoreDailyResources() {
        _resources.update {
            it.copy(
                hp = it.maxHp,
                mana = (it.mana + 35).coerceAtMost(it.maxMana),
                darkEnergy = it.maxDarkEnergy,
                sexEnergy = it.maxSexEnergy,
                influence = (it.influence + 5).coerceAtMost(it.maxInfluence)
            )
        }
        notifyChange()
    }

    fun setAll(
        gold: Int? = null,
        mana: Int? = null,
        influence: Int? = null,
        hp: Int? = null,
        darkEnergy: Int? = null,
        sexEnergy: Int? = null
    ) {
        _resources.update {
            it.copy(
                gold = gold ?: it.gold,
                mana = mana?.coerceIn(0, it.maxMana) ?: it.mana,
                influence = influence?.coerceIn(0, it.maxInfluence) ?: it.influence,
                hp = hp?.coerceIn(0, it.maxHp) ?: it.hp,
                darkEnergy = darkEnergy?.coerceIn(0, it.maxDarkEnergy) ?: it.darkEnergy,
                sexEnergy = sexEnergy?.coerceIn(0, it.maxSexEnergy) ?: it.sexEnergy
            )
        }
        notifyChange()
    }

    private fun notifyChange() {
        onResourceChanged?.invoke(_resources.value)
    }
}
