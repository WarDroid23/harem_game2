package com.example.haremdark.domain

import android.content.Context
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.DrugData
import com.example.haremdark.data.DrugDefinition
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.haremdark.models.InventoryItem
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import kotlin.random.Random

val Context.dataStore by preferencesDataStore(name = "harem_dark_saves")


data class DailyRewardState(
    val consecutiveDays: Int,
    val rewardGold: Int,
    val rewardMana: Int,
    val itemReward: InventoryItem?
)

class GameEngine(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _gameState = MutableStateFlow(GameContent.createInitialSave())
    val gameState: StateFlow<GameSave> = _gameState.asStateFlow()

    private val _combatState = MutableStateFlow<CombatSession?>(null)
    val combatState: StateFlow<CombatSession?> = _combatState.asStateFlow()

    private val _partyCombatSession = MutableStateFlow<PartyCombatSession?>(null)
    val partyCombatSession: StateFlow<PartyCombatSession?> = _partyCombatSession.asStateFlow()

    private val _narrativeEvents = kotlinx.coroutines.flow.MutableSharedFlow<com.example.haremdark.data.NarrativeEvent>(extraBufferCapacity = 10)
    val narrativeEvents: kotlinx.coroutines.flow.SharedFlow<com.example.haremdark.data.NarrativeEvent> = _narrativeEvents

    private val _currentTheme = MutableStateFlow("Temné dominium")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val _isLightMode = MutableStateFlow(false)
    val isLightMode: StateFlow<Boolean> = _isLightMode.asStateFlow()


    private val _dailyRewardAvailable = MutableStateFlow<DailyRewardState?>(null)
    val dailyRewardAvailable: StateFlow<DailyRewardState?> = _dailyRewardAvailable.asStateFlow()

    fun checkDailyLogin() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val lastLoginKey = longPreferencesKey("last_login_epoch_day")
            val streakKey = intPreferencesKey("consecutive_login_days")
            val prefs = context.dataStore.data.first()
            
            val lastLoginEpochDay = prefs[lastLoginKey] ?: 0L
            val consecutiveDays = prefs[streakKey] ?: 0
            
            val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            
            if (currentEpochDay > lastLoginEpochDay) {
                val isConsecutive = (currentEpochDay - lastLoginEpochDay) == 1L
                val newStreak = if (isConsecutive) consecutiveDays + 1 else 1
                
                val rewardGold = 100 + (newStreak * 20).coerceAtMost(400)
                val rewardMana = 20 + (newStreak * 5).coerceAtMost(100)
                
                val itemReward = if (newStreak % 5 == 0) {
                    InventoryItem(
                        id = "daily_gift_epic",
                        name = "Epický dar za věrnost",
                        description = "Cenný předmět pro tvůj harém. (Dárek)",
                        count = 1,
                        price = 250,
                        category = "gift",
                        icon = "🎁",
                        rarity = "Epický",
                        effectDescription = "Velmi zvyšuje náklonnost"
                    )
                } else null
                
                _dailyRewardAvailable.value = DailyRewardState(
                    consecutiveDays = newStreak,
                    rewardGold = rewardGold,
                    rewardMana = rewardMana,
                    itemReward = itemReward
                )
            }
        }
    }

    fun claimDailyReward() {
        val reward = _dailyRewardAvailable.value ?: return
        _dailyRewardAvailable.value = null
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val lastLoginKey = longPreferencesKey("last_login_epoch_day")
            val streakKey = intPreferencesKey("consecutive_login_days")
            val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            
            context.dataStore.edit { prefs ->
                prefs[lastLoginKey] = currentEpochDay
                prefs[streakKey] = reward.consecutiveDays
            }
            
            updateState { state ->
                val p = state.player
                val newItems = p.items.toMutableList()
                if (reward.itemReward != null) {
                    val existingIdx = newItems.indexOfFirst { it.id == reward.itemReward.id }
                    if (existingIdx != -1) {
                        val ex = newItems[existingIdx]
                        newItems[existingIdx] = ex.copy(count = ex.count + 1)
                    } else {
                        newItems.add(reward.itemReward)
                    }
                }
                
                state.copy(
                    player = p.copy(
                        gold = p.gold + reward.rewardGold,
                        darkEnergy = (p.darkEnergy + reward.rewardMana).coerceAtMost(p.maxDarkEnergy),
                        items = newItems
                    )
                )
            }
            autoSave()
        }
    }

    init {
        _currentTheme.value = _gameState.value.currentTheme
        _isLightMode.value = _gameState.value.isLightMode
        
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val savedState = loadStateSuspend("save_slot_autosave") 
                ?: loadStateSuspend("save_slot_1")
            
            if (savedState != null) {
                _gameState.value = savedState
                _currentTheme.value = savedState.currentTheme
                _isLightMode.value = savedState.isLightMode
            }
            
            checkDailyLogin()
        }
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            while (true) {
                kotlinx.coroutines.delay(5 * 60 * 1000L) // 5 minut
                autoSave()
            }
        }
        if (_gameState.value.dailyMissions.isEmpty() || _gameState.value.lastMissionUpdateDay != _gameState.value.player.day) {
            val day = _gameState.value.player.day
            val initMissions = listOf(
                DailyMission(id = "m1_$day", type = "INTERACT", description = "Provést interakce s dívkami", targetCount = 3, rewardGold = 50, rewardSexEnergy = 20),
                DailyMission(id = "m2_$day", type = "EXPLORE", description = "Vyrazit na výpravu", targetCount = 1, rewardGold = 100, rewardDarkEnergy = 15),
                DailyMission(id = "m3_$day", type = "GIFT", description = "Darovat předmět z brašny", targetCount = 1, rewardGold = 75)
            )
            _gameState.value = _gameState.value.copy(
                dailyMissions = initMissions,
                lastMissionUpdateDay = day
            )
        }
    }



    private suspend fun loadStateSuspend(keyStr: String): GameSave? {
        val key = stringPreferencesKey(keyStr)
        val prefs = context.dataStore.data.first()
        val savedJson = prefs[key]
        if (savedJson != null) {
            try {
                return json.decodeFromString<GameSave>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun saveStateAsync(keyStr: String, state: GameSave) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val key = stringPreferencesKey(keyStr)
            val str = json.encodeToString(state)
            context.dataStore.edit { prefs ->
                prefs[key] = str
            }
        }
    }

    fun updateState(transform: (GameSave) -> GameSave) {
        val current = _gameState.value
        val transformed = transform(current)
        
        // Narrative Events Check for Affinity Level Up
        current.characters.forEach { oldChar ->
            val newChar = transformed.characters.find { it.id == oldChar.id }
            if (newChar != null && newChar.affinityLevel > oldChar.affinityLevel) {
                val event = com.example.haremdark.data.AffinityEventData.getEventFor(newChar, newChar.affinityLevel)
                if (event != null) {
                    _narrativeEvents.tryEmit(event)
                }
            }
        }
        
        // Ensure Player and collections have distinct references so StateFlow and Compose always re-render in real-time
        val p = transformed.player
        val finalState = transformed.copy(
            player = p.copy(
                skills = p.skills.toMutableMap(),
                weapons = p.weapons.map { it.copy() }.toMutableList(),
                items = p.items.map { it.copy() }.toMutableList(),
                agents = p.agents.map { it.copy() }.toMutableList()
            ),
            characters = transformed.characters.map { it.copy() },
            buildings = transformed.buildings.map { it.copy() },
            territories = transformed.territories.map { it.copy() }
        )
        _gameState.value = finalState
    }

    fun spendGold(amount: Int, reason: String? = null): Boolean {
        val current = _gameState.value
        if (current.player.gold < amount) return false
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - amount).coerceAtLeast(0))
            if (!reason.isNullOrBlank()) {
                val logs = (listOf(reason) + state.gameLog).take(30)
                state.copy(player = p, gameLog = logs)
            } else {
                state.copy(player = p)
            }
        }
        return true
    }

    fun earnGold(amount: Int, reason: String? = null) {
        updateState { state ->
            val p = state.player.copy(gold = state.player.gold + amount)
            if (!reason.isNullOrBlank()) {
                val logs = (listOf(reason) + state.gameLog).take(30)
                state.copy(player = p, gameLog = logs)
            } else {
                state.copy(player = p)
            }
        }
    }

    fun buyWardrobeItem(name: String, desc: String, price: Int): Pair<Boolean, String> {
        val current = _gameState.value
        if (current.player.gold < price) {
            return Pair(false, "Nemáš dostatek zlata ($price zl.)!")
        }
        val msg = "✨ Zakoupeno: $name! Harém září novým luxusem."
        spendGold(price, msg)
        addHaremExp(15)
        return Pair(true, msg)
    }

    fun investDynastyTraining(cost: Int = 100): Pair<Boolean, String> {
        val current = _gameState.value
        if (current.player.gold < cost) {
            return Pair(false, "Nemáš dostatek zlata ($cost zl.)!")
        }
        val msg = "📚 Učitelé byli najati! Následníci dominia získávají nové vědomosti."
        spendGold(cost, msg)
        addPlayerXp(20)
        return Pair(true, msg)
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
        val current = _gameState.value
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nebyla nalezena.")

        if (current.player.gold < goldCost) {
            return Pair(false, "Nemáš dostatek zlata ($goldCost zl.)!")
        }

        val affinityGain = (loyaltyBoost + trustBoost) / 2 + 12
        val successMsg = "🎁 Předal jsi dar '$giftName' dívce ${character.name}. $flavorText"
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - goldCost).coerceAtLeast(0))
            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    val copy = c.copy()
                    copy.loajalita = (copy.loajalita + loyaltyBoost).coerceAtMost(100)
                    copy.touha = (copy.touha + desireBoost).coerceAtMost(100)
                    copy.poslusnost = (copy.poslusnost + obedienceBoost).coerceAtMost(100)
                    copy.duvera = (copy.duvera + trustBoost).coerceAtMost(100)
                    copy.strach = (copy.strach - 5).coerceAtLeast(0)
                    copy.srdce = (copy.srdce + 8).coerceAtMost(100)
                    copy.affinityPoints = copy.affinityPoints + affinityGain
                    copy.affinityLevel = AffinityData.getLevelForPoints(copy.affinityPoints)
                    copy.lastInteractionDay = current.player.day
                    val newPhase = StaticData.calculatePhase(
                        broken = copy.broken,
                        mindbreak = copy.mindbreak,
                        poslusnost = copy.poslusnost,
                        loajalita = copy.loajalita,
                        painAddiction = copy.painAddiction,
                        scarred = copy.scarred,
                        touha = copy.touha,
                        humiliation = copy.humiliation,
                        zavislost = copy.zavislost,
                        age = copy.age,
                        pregnant = copy.tehotna
                    )
                    if (newPhase > copy.fazeZkazenosti) {
                        copy.fazeZkazenosti = newPhase
                    }
                    copy
                } else c
            }
            val logs = (listOf(successMsg) + state.gameLog).take(30)
            state.copy(player = p, characters = updatedCharacters, gameLog = logs)
        }
        addHaremExp(10)
        return Pair(true, successMsg)
    }

    fun giveInventoryGift(characterId: String, itemId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count > 0 }
            ?: return Pair(false, "Předmět není v tvém inventáři.")
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nebyla nalezena.")

        var resultMsg = ""
        val affinityGain = when (itemId) {
            "drahy_obojek" -> 35
            "serum_poslusnost" -> 25
            "elixir_touhy" -> 20
            "hojivy_balzam" -> 15
            else -> 12
        }
        updateState { state ->
            val p = state.player.copy()
            val itemInInv = p.items.firstOrNull { it.id == itemId }
            if (itemInInv != null) {
                itemInInv.count -= 1
                if (itemInInv.count <= 0) {
                    p.items.remove(itemInInv)
                }
            }

            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    val copy = c.copy()
                    copy.affinityPoints = copy.affinityPoints + affinityGain
                    copy.affinityLevel = AffinityData.getLevelForPoints(copy.affinityPoints)
                    copy.lastInteractionDay = current.player.day
                    when (itemId) {
                        "elixir_touhy" -> {
                            copy.touha = (copy.touha + 30).coerceAtMost(100)
                            copy.vlhkost = (copy.vlhkost + 25).coerceAtMost(100)
                            copy.loajalita = (copy.loajalita + 5).coerceAtMost(100)
                            resultMsg = "🧪 ${copy.name} vypila Elixír touhy. Její tělo sálá nekontrolovatelnou vášní."
                        }
                        "hojivy_balzam" -> {
                            copy.hp = (copy.hp + 35).coerceAtMost(copy.maxHp)
                            copy.duvera = (copy.duvera + 10).coerceAtMost(100)
                            resultMsg = "🩹 Aplikoval jsi Hojivý balzám. Zranění ${copy.name} se zacelila (+35 HP)."
                        }
                        "serum_poslusnost" -> {
                            copy.poslusnost = (copy.poslusnost + 25).coerceAtMost(100)
                            copy.submisivita = (copy.submisivita + 20).coerceAtMost(100)
                            copy.loajalita = (copy.loajalita + 15).coerceAtMost(100)
                            resultMsg = "🧪 ${copy.name} pozřela Sérum poslušnosti. Její odpor byl zcela utlumen."
                        }
                        "drahy_obojek" -> {
                            copy.loajalita = (copy.loajalita + 25).coerceAtMost(100)
                            copy.submisivita = (copy.submisivita + 20).coerceAtMost(100)
                            copy.poslusnost = (copy.poslusnost + 15).coerceAtMost(100)
                            copy.ownedMark = true
                            resultMsg = "👑 Připnul jsi ${copy.name} Zlatý obojek pána. Znak absolutního vlastnictví září na jejím krku."
                        }
                        else -> {
                            copy.loajalita = (copy.loajalita + 12).coerceAtMost(100)
                            copy.duvera = (copy.duvera + 8).coerceAtMost(100)
                            resultMsg = "🎁 Daroval jsi ${item.name} dívce ${copy.name}. Její loajalita vzrostla."
                        }
                    }
                    val newPhase = StaticData.calculatePhase(
                        broken = copy.broken,
                        mindbreak = copy.mindbreak,
                        poslusnost = copy.poslusnost,
                        loajalita = copy.loajalita,
                        painAddiction = copy.painAddiction,
                        scarred = copy.scarred,
                        touha = copy.touha,
                        humiliation = copy.humiliation,
                        zavislost = copy.zavislost,
                        age = copy.age,
                        pregnant = copy.tehotna
                    )
                    if (newPhase > copy.fazeZkazenosti) {
                        copy.fazeZkazenosti = newPhase
                    }
                    copy
                } else c
            }

            val logs = (listOf(resultMsg) + state.gameLog).take(30)
            state.copy(player = p, characters = updatedCharacters, gameLog = logs)
        }
        addHaremExp(8)
        return Pair(true, resultMsg)
    }

    fun setTheme(themeName: String) {
        _currentTheme.value = themeName
        updateState { it.copy(currentTheme = themeName) }
        saveToSlot(slot = it_slot_number)
    }

    fun setLightMode(enabled: Boolean) {
        _isLightMode.value = enabled
        updateState { it.copy(isLightMode = enabled) }
        saveToSlot(slot = it_slot_number)
    }

    private var it_slot_number = 1

    fun addLog(message: String) {
        updateState { current ->
            val logs = (listOf(message) + current.gameLog).take(30)
            current.copy(gameLog = logs)
        }
    }

    // --- NEXT DAY / ODPOČINEK ---
    fun restNextDay(meditative: Boolean = false) {
        updateState { current ->
            val p = current.player
            val newDay = p.day + 1
            
            // Affinity Passive Multipliers Setup
            var level2Count = 0
            var level4Count = 0
            var level6Count = 0
            current.characters.forEach { c ->
                if (c.affinityLevel >= 2) level2Count++
                if (c.affinityLevel >= 4) level4Count++
                if (c.affinityLevel >= 6) level6Count++
            }

            // Domain Resources & Passive income
            val resourceManager = DomainResourceManager()
            val yield = resourceManager.calculateDailyYield(current)
            
            val haremIncomeBase = current.haremLevel * 10
            val haremIncomeBonus = (haremIncomeBase * (0.10f * level2Count)).toInt()
            
            val basePassiveGold = yield.gold + haremIncomeBase + haremIncomeBonus
            var globalIncomeMultiplier = 1.0f + (0.50f * level6Count)
            
            // Apply RESOURCE_BOOST buffs
            val resourceBuffs = current.activeBuffs.filter { it.type == "RESOURCE_BOOST" }.sumOf { it.value }
            if (resourceBuffs > 0) {
                globalIncomeMultiplier += (resourceBuffs / 100f)
            }
            
            // Add Relationship Buffs
            var relResMultiplier = 0.0f
            var skillResMultiplier = 0.0f
            current.characters.forEach { c ->
                val rel = c.getRelationship()
                if (rel == com.example.haremdark.models.RelStatus.DEVOTED) relResMultiplier += rel.buffValue
                if (rel == com.example.haremdark.models.RelStatus.OBEDIENT) relResMultiplier += rel.buffValue
                skillResMultiplier += (c.skills["production"] ?: 0) * 0.02f
            }
            globalIncomeMultiplier += relResMultiplier + skillResMultiplier
            
            val totalPassiveGold = (basePassiveGold * globalIncomeMultiplier).toInt()

            // Process slave rentals
            var rentalIncome = 0
            val updatedCharacters = current.characters.map { c ->
                val copy = c.copy()
                val rel = copy.getRelationship()
                if (copy.naNajmu) {
                    var dailyIncome = when (copy.klient) {
                        "Místní měšťané" -> 10
                        "Cech bohatých kupců" -> 30
                        "Šlechtický dvůr" -> 50
                        "Otrokářský syndikát" -> 80
                        "Inkviziční legie" -> 100
                        else -> 50
                    }
                    val dmg = when (copy.klient) {
                        "Otrokářský syndikát" -> 15
                        "Inkviziční legie" -> 25
                        else -> 0
                    }
                    
                    if (rel == com.example.haremdark.models.RelStatus.BROKEN) {
                        dailyIncome = (dailyIncome * (1.0f + rel.buffValue)).toInt()
                    }
                    dailyIncome += (copy.skills["rental"] ?: 0) * 15
                    
                    if (dmg > 0) {
                        copy.hp = (copy.hp - dmg).coerceAtLeast(0)
                        if (copy.hp == 0) {
                            addLog("🚨 ${copy.name} byla během pronájmu u '${copy.klient}' kriticky zraněna a odeslána zpět!")
                            copy.naNajmu = false
                            copy.klient = null
                            copy.typNajmu = null
                            copy.najemZbyvaDni = 0
                        }
                    }

                    if (copy.naNajmu) {
                        copy.najemZbyvaDni = (copy.najemZbyvaDni - 1).coerceAtLeast(0)
                        copy.najemPrijemCelkem += dailyIncome
                        rentalIncome += dailyIncome
                        if (copy.najemZbyvaDni == 0) {
                            copy.naNajmu = false
                            copy.klient = null
                            copy.typNajmu = null
                            addLog("Dívka ${copy.name} se vrátila z nájmu zpět do tvého harému.")
                        }
                    }
                } else if (copy.hp > 0) {
                    val bathLevel = current.buildings.firstOrNull { it.type == "lazne" }?.level ?: 0
                    var healAmount = 10 + bathLevel * 5
                    if (rel == com.example.haremdark.models.RelStatus.IN_LOVE) healAmount += (copy.maxHp * rel.buffValue).toInt()
                    copy.hp = (copy.hp + healAmount).coerceAtMost(copy.maxHp)
                }

                // Pregnancy progress
                if (copy.tehotna) {
                    copy.dnyTehotenstvi += 1
                    if (copy.dnyTehotenstvi >= 3) {
                        copy.tehotna = false
                        copy.dnyTehotenstvi = 0
                        copy.deti += 1
                        addLog("👶 ${copy.name} porodila tvého nového dědice dominia! Harém oslavuje.")
                    }
                }
                copy
            }

            // Relationship bonus to max energy
            val wives = updatedCharacters.filter { it.jeManzelkou }
            val favorites = updatedCharacters.filter { it.oblibena }

            var maxSexBonus = 0
            var maxDarkBonus = 0

            if (wives.isNotEmpty()) {
                maxSexBonus += 1
                maxDarkBonus += 1
            }
            if (favorites.isNotEmpty()) {
                maxSexBonus += 1
                if (favorites.any { it.fazeZkazenosti >= 8 || it.loajalita >= 80 }) {
                    maxDarkBonus += 1
                }
            }

            val newMaxSex = (p.maxSexEnergy + maxSexBonus + (level4Count * 10)).coerceAtMost(500)
            val newMaxDark = (p.maxDarkEnergy + (if (meditative) maxDarkBonus + 1 else maxDarkBonus)).coerceAtMost(200)

                        // Apply Domain Resources
            val modifiedYield = yield.copy(gold = totalPassiveGold + rentalIncome)
            resourceManager.applyYield(p, modifiedYield)
            
            // Record production history
            val newStat = DailyResourceStat(
                day = newDay,
                goldProduced = modifiedYield.gold,
                manaProduced = modifiedYield.mana,
                woodProduced = modifiedYield.wood,
                stoneProduced = modifiedYield.stone,
                ironProduced = modifiedYield.iron
            )
            val newHistory = (current.resourceHistory + newStat).takeLast(14) // Keep last 14 days

            if (yield.wood > 0 || yield.stone > 0 || yield.iron > 0 || yield.mana > 0) {
                addLog("🏘️ Dominium vyprodukovalo: +${yield.wood} dreva, +${yield.stone} kameni, +${yield.iron} zeleza, +${yield.mana} many. Populace vzrostla o ${yield.populationGrowth}.")
            }
            p.day = newDay
            p.maxSexEnergy = newMaxSex
            p.maxDarkEnergy = newMaxDark
            p.sexEnergy = newMaxSex
            p.darkEnergy = newMaxDark
            p.hp = p.maxHp
            p.toxicity = (p.toxicity - 8).coerceAtLeast(0)

            // Process Harem Roles perks
            val firstLady = updatedCharacters.firstOrNull { it.role == "Paní harému (První dáma)" }
            if (firstLady != null) {
                p.haremHarmony = (p.haremHarmony + 5).coerceAtMost(100)
            }
            val companion = updatedCharacters.firstOrNull { it.role == "Důvěrná společnice" }
            if (companion != null) {
                p.sexEnergy = (p.sexEnergy + 15).coerceAtMost(p.maxSexEnergy)
            }

            // Random Jealousy / Night incident check if favorite exists
            if (favorites.isNotEmpty() && updatedCharacters.size > 1 && Random.nextFloat() < 0.35f) {
                val fav = favorites.first()
                val other = updatedCharacters.filter { !it.oblibena }.randomOrNull()
                if (other != null) {
                    other.strach = (other.strach + 4).coerceAtMost(100)
                    other.humiliation = (other.humiliation + 3).coerceAtMost(100)
                    p.haremHarmony = (p.haremHarmony - 3).coerceAtLeast(10)
                    addLog("★ Noční incident: Ostatní dívky žárlí na oblíbenkyni ${fav.name}. ${other.name} cítí tlak v harému.")
                }
            }


            // Buff durations
            val newBuffs = current.activeBuffs.map { it.copy(durationDays = it.durationDays - 1) }.filter { it.durationDays > 0 }.toMutableList()
            var bondingLog: String? = null

            // Random Bonding Event
            val inLoveCount = updatedCharacters.count { it.getRelationship() == com.example.haremdark.models.RelStatus.IN_LOVE }
            val bondingChance = 0.3 + (inLoveCount * 0.10)
            if (updatedCharacters.size >= 2 && Math.random() < bondingChance) {
                val shuffled = updatedCharacters.shuffled()
                val c1 = shuffled[0]
                val c2 = shuffled[1]
                
                val eventType = listOf("COMBAT", "PRODUCTION", "MORALE").random()
                when (eventType) {
                    "COMBAT" -> {
                        bondingLog = "⚔️ Pouto: ${c1.name} a ${c2.name} spolu v noci trénovaly. Celý harém má bonus +10% k poškození na 2 dny!"
                        newBuffs.add(PartyBuff("bond_combat_${newDay}", "Bojové pouto", "Bonus k poškození z nočního tréninku.", 2, "DAMAGE", 10))
                    }
                    "PRODUCTION" -> {
                        bondingLog = "🛠️ Pouto: ${c1.name} a ${c2.name} zorganizovaly výpomoc v dominiu. Zvýšená produkce zlata o 15% na 2 dny!"
                        newBuffs.add(PartyBuff("bond_prod_${newDay}", "Organizační talent", "Bonus k produkci surovin.", 2, "RESOURCE_BOOST", 15))
                    }
                    "MORALE" -> {
                        bondingLog = "💕 Pouto: ${c1.name} a ${c2.name} strávily noc spolu a posílily své pouto. Dočasná odolnost a nadšení!"
                        newBuffs.add(PartyBuff("bond_morale_${newDay}", "Hřejivé pouto", "Pasivní odolnost harému.", 3, "DEFENSE", 10))
                        c1.vlhkost = (c1.vlhkost + 20).coerceAtMost(100)
                        c2.vlhkost = (c2.vlhkost + 20).coerceAtMost(100)
                        c1.loajalita = (c1.loajalita + 5).coerceAtMost(100)
                        c2.loajalita = (c2.loajalita + 5).coerceAtMost(100)
                    }
                }
            }

            val logEntry = "🌅 Den $newDay svítá. Energie plně obnovena (${p.sexEnergy}/${p.darkEnergy}). Příjem: +${totalPassiveGold + rentalIncome} zlatých."
            val logsList = mutableListOf(logEntry)
            if (bondingLog != null) logsList.add(bondingLog!!)

            // Process Active Drug Enhancers and Maintenance
            val remainingDrugBuffs = mutableListOf<com.example.haremdark.models.ActiveDrugBuff>()
            val playerItems = p.items.map { it.copy() }.toMutableList()

            for (buff in current.activeDrugBuffs) {
                if (buff.remainingDays > 1) {
                    remainingDrugBuffs.add(buff.copy(remainingDays = buff.remainingDays - 1))
                } else {
                    // remainingDays is 1 -> expiring today unless autoRenewed
                    if (buff.autoRenew) {
                        val stockItem = playerItems.firstOrNull { it.id == buff.drugId && it.count > 0 }
                        if (stockItem != null) {
                            // Deduct 1 dose
                            stockItem.count -= 1
                            if (stockItem.count <= 0) {
                                playerItems.removeAll { it.id == buff.drugId && it.count <= 0 }
                            }
                            remainingDrugBuffs.add(buff.copy(remainingDays = buff.maxDays, withdrawalWarning = false))
                            logsList.add("💊 Dávkování posílení: Spotřebována 1x dávka ${buff.name} pro ${buff.targetCharacterName}. Posílení úspěšně udrženo!")
                        } else {
                            // Out of stock! Buff expires and causes withdrawal
                            logsList.add("⚠️ VYČERPÁNY ZÁSOBY: Došly zásoby substance ${buff.name}! Posílení pro ${buff.targetCharacterName} vypršelo!")
                            if (buff.targetType == "concubine" && buff.targetCharacterId != null) {
                                val c = updatedCharacters.firstOrNull { it.id == buff.targetCharacterId }
                                if (c != null) {
                                    c.touha = (c.touha - 15).coerceAtLeast(0)
                                    c.strach = (c.strach + 12).coerceAtMost(100)
                                    c.duvera = (c.duvera - 8).coerceAtLeast(0)
                                    c.poslusnost = (c.poslusnost - 10).coerceAtLeast(0)
                                    logsList.add("⚡ Abstinenční záchvat: Dívka ${c.name} trpí nedostatkem substance! Vzrostl strach a poklesla poslušnost.")
                                }
                            }
                        }
                    } else {
                        logsList.add("⏳ Posílení ${buff.name} pro ${buff.targetCharacterName} vypršelo.")
                    }
                }
            }
            p.items = playerItems

            val logs = (logsList + current.gameLog).take(30)

            val newMissions = listOf(
                DailyMission(id = "m1_$newDay", type = "INTERACT", description = "Provést interakce s dívkami", targetCount = 3, rewardGold = 50, rewardSexEnergy = 20),
                DailyMission(id = "m2_$newDay", type = "EXPLORE", description = "Vyrazit na výpravu", targetCount = 1, rewardGold = 100, rewardDarkEnergy = 15),
                DailyMission(id = "m3_$newDay", type = "GIFT", description = "Darovat předmět z brašny", targetCount = 1, rewardGold = 75)
            )

            current.copy(
                player = p,
                characters = updatedCharacters,
                gameLog = logs,
                dailyMissions = newMissions,
                lastMissionUpdateDay = newDay,
                activeBuffs = newBuffs,
                activeDrugBuffs = remainingDrugBuffs,
                resourceHistory = newHistory
            )
        }
        autoSave()
    }

    // --- MISSIONS ---
    fun progressMission(type: String, amount: Int = 1) {
        updateState { current ->
            var updatedMissions = false
            val newMissions = current.dailyMissions.map { mission ->
                if (mission.type == type && !mission.isCompleted) {
                    val newProgress = (mission.currentProgress + amount).coerceAtMost(mission.targetCount)
                    if (newProgress > mission.currentProgress) {
                        updatedMissions = true
                        val completed = newProgress >= mission.targetCount
                        mission.copy(currentProgress = newProgress, isCompleted = completed)
                    } else mission
                } else mission
            }
            if (updatedMissions) {
                current.copy(dailyMissions = newMissions)
            } else current
        }
    }

    fun claimMissionReward(missionId: String): Pair<Boolean, String> {
        var result = Pair(false, "Chyba při vyzvednutí.")
        updateState { current ->
            val mission = current.dailyMissions.find { it.id == missionId }
            if (mission != null && mission.isCompleted && !mission.isClaimed) {
                val p = current.player
                p.gold += mission.rewardGold
                p.darkEnergy = (p.darkEnergy + mission.rewardDarkEnergy).coerceAtMost(p.maxDarkEnergy)
                p.sexEnergy = (p.sexEnergy + mission.rewardSexEnergy).coerceAtMost(p.maxSexEnergy)
                
                val newMissions = current.dailyMissions.map { 
                    if (it.id == missionId) it.copy(isClaimed = true) else it 
                }
                
                addLog("Odměna vyzvednuta za úkol '${mission.description}': ${mission.rewardGold} zl.")
                result = Pair(true, "Odměna vyzvednuta!")
                current.copy(dailyMissions = newMissions, player = p)
            } else {
                result = Pair(false, "Odměnu nelze vybrat.")
                current
            }
        }
        autoSave()
        return result
    }

    // --- CONCUBINE INTERACTIONS ---
    fun executeInteraction(characterId: String, interaction: GameInteraction): Pair<Boolean, String> {
        val current = _gameState.value
        val player = current.player
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nebyla nalezena.")

        if (player.sexEnergy < interaction.energyCost) {
            return Pair(false, "Nedostatek sexuální energie (${player.sexEnergy}/${interaction.energyCost})! Odpočiň si na nový den.")
        }
        if (player.darkEnergy < interaction.darkCost) {
            return Pair(false, "Nedostatek temné energie (${player.darkEnergy}/${interaction.darkCost})!")
        }
        if (player.gold < interaction.goldCost) {
            return Pair(false, "Nedostatek zlata (${player.gold}/${interaction.goldCost} zlatých)!")
        }
        if (interaction.requiresFavorite && !character.oblibena) {
            return Pair(false, "Tato akce vyžaduje, aby byla dívka jmenována tvou Oblíbenkyní ★!")
        }
        if (interaction.requiresWife && !character.jeManzelkou) {
            return Pair(false, "Tato akce vyžaduje manželský svazek 💍!")
        }
        if (character.fazeZkazenosti < interaction.minPhase) {
            return Pair(false, "Dívka musí dosáhnout alespoň fáze zkázanosti ${interaction.minPhase}!")
        }

        player.sexEnergy -= interaction.energyCost
        player.darkEnergy -= interaction.darkCost
        player.gold -= interaction.goldCost

        // Apply interaction
        val message = interaction.applyEffect(character, player)

        // Affinity Passives Processing for Intimate Interactions
        if (interaction.type == "intimni") {
            if (character.affinityLevel >= 3) {
                // Level 3: +15% efektivita (we boost stats directly here), +10% zisk temné energie
                character.touha = (character.touha + 3).coerceAtMost(100)
                character.vlhkost = (character.vlhkost + 3).coerceAtMost(100)
                character.duvera = (character.duvera + 3).coerceAtMost(100)
                player.darkEnergy = (player.darkEnergy + 5).coerceAtMost(player.maxDarkEnergy)
            }
            if (character.affinityLevel >= 4 && !character.tehotna && interaction.id == "eroticka_noc") {
                // Level 4: +20% šance na zplození dědice (extra roll)
                if ((1..100).random() <= 20) {
                    character.tehotna = true
                    character.dnyTehotenstvi = 0
                }
            }
        }

        // Recalculate degradation phase
        val newPhase = StaticData.calculatePhase(
            broken = character.broken,
            mindbreak = character.mindbreak,
            poslusnost = character.poslusnost,
            loajalita = character.loajalita,
            painAddiction = character.painAddiction,
            scarred = character.scarred,
            touha = character.touha,
            humiliation = character.humiliation,
            zavislost = character.zavislost,
            age = character.age,
            pregnant = character.tehotna
        )
        if (newPhase > character.fazeZkazenosti) {
            character.fazeZkazenosti = newPhase
            val phaseInfo = StaticData.DEGRADATION_PHASES[newPhase]
            addLog("★ ${character.name} postoupila do fáze zkázanosti: ${phaseInfo?.name ?: "$newPhase"}!")
        }

        // Affinity increase on interaction based on interaction depth and bond
        val prevAffinityLevel = character.affinityLevel
        val affinityGain = when (interaction.type) {
            "intimni" -> 8 + if (character.oblibena || character.jeManzelkou) 4 else 0
            "rozmluva" -> 5 + if (character.oblibena || character.jeManzelkou) 3 else 0
            else -> 4 + if (character.oblibena || character.jeManzelkou) 2 else 0
        }
        character.affinityPoints += affinityGain
        val newAffinityLevel = com.example.haremdark.data.AffinityData.getLevelForPoints(character.affinityPoints)
        character.affinityLevel = newAffinityLevel
        character.affinityHistory.add(AffinityPointRecord(_gameState.value.player.day, character.affinityPoints, "${interaction.name} (+$affinityGain pts)"))

        val tierInfo = com.example.haremdark.data.AffinityData.getTierForPoints(character.affinityPoints)
        val levelUpAnnouncement = if (newAffinityLevel > prevAffinityLevel) {
            "\n🌟 Pouto posíleno! ${character.name} dosáhla úrovně vztahu ${tierInfo.level}: ${tierInfo.title}! ${tierInfo.combatBonusDescription}"
        } else ""

        // Unique unlocked dialogue based on affinity tier & archetype
        val unlockedDialogue = com.example.haremdark.data.AffinityData.getRandomActiveDialogue(character)
        
        // Add player XP & harem EXP
        addPlayerXp(12)
        addHaremExp(8)
        progressMission("INTERACT", 1)

        val fullMessage = "$message (+$affinityGain náklonnost)\n💬 ${character.name}: „$unlockedDialogue“$levelUpAnnouncement"
        addLog(fullMessage)
        updateState { it.copy() }
        
        if (newAffinityLevel > prevAffinityLevel) {
            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
            com.example.haremdark.domain.VoiceManager.playTriggerVoice(
                com.example.haremdark.domain.VoiceTriggerType.AFFINITY_LEVEL_UP,
                character
            )
        } else {
            when (interaction.type) {
                "intimni" -> SoundEffectManager.playHarem(HaremSound.SEDUCE)
                "rozmluva" -> SoundEffectManager.playHarem(HaremSound.FLIRT)
                "disciplina", "vycvik" -> SoundEffectManager.playHarem(HaremSound.TRAIN)
                else -> SoundEffectManager.playHarem(HaremSound.FLIRT)
            }
            com.example.haremdark.domain.VoiceManager.speak(unlockedDialogue, character.archetypeId)
        }
        
        return Pair(true, fullMessage)
    }

    fun applyDialogueChoiceOutcome(
        characterId: String,
        affinityGain: Int,
        loyaltyGain: Int,
        trustGain: Int,
        submissivenessGain: Int,
        fearGain: Int,
        brokenGain: Int,
        logText: String,
        prompt: String = "",
        optionText: String = "",
        feedback: String = "",
        outcomeEffects: String = ""
    ): Pair<Boolean, String> {
        val current = _gameState.value
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nebyla nalezena.")

        if (prompt.isNotEmpty()) {
            character.relationshipHistory.add(
                com.example.haremdark.models.RelationshipRecord(
                    day = current.player.day,
                    prompt = prompt,
                    choice = optionText,
                    feedback = feedback,
                    outcomeEffects = outcomeEffects
                )
            )
        }

        val prevAffinityLevel = character.affinityLevel

        character.affinityPoints = (character.affinityPoints + affinityGain).coerceAtLeast(0)
        character.loajalita = (character.loajalita + loyaltyGain).coerceIn(0, 100)
        character.duvera = (character.duvera + trustGain).coerceIn(0, 100)
        character.submisivita = (character.submisivita + submissivenessGain).coerceIn(0, 100)
        character.strach = (character.strach + fearGain).coerceIn(0, 100)
        character.broken = (character.broken + brokenGain).coerceIn(0, 100)

        val newAffinityLevel = com.example.haremdark.data.AffinityData.getLevelForPoints(character.affinityPoints)
        character.affinityLevel = newAffinityLevel
        if (affinityGain > 0) {
            character.affinityHistory.add(AffinityPointRecord(current.player.day, character.affinityPoints, "Rozhovor (+$affinityGain pts)"))
        }

        // Recalculate degradation phase
        val newPhase = StaticData.calculatePhase(
            broken = character.broken,
            mindbreak = character.mindbreak,
            poslusnost = character.poslusnost,
            loajalita = character.loajalita,
            painAddiction = character.painAddiction,
            scarred = character.scarred,
            touha = character.touha,
            humiliation = character.humiliation,
            zavislost = character.zavislost,
            age = character.age,
            pregnant = character.tehotna
        )
        if (newPhase > character.fazeZkazenosti) {
            character.fazeZkazenosti = newPhase
            addLog("★ ${character.name} postoupila do fáze zkázanosti: ${StaticData.DEGRADATION_PHASES[newPhase]?.name ?: "$newPhase"}!")
        }

        addLog(logText)
        updateState { it.copy() }

        if (newAffinityLevel > prevAffinityLevel) {
            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
            com.example.haremdark.domain.VoiceManager.playTriggerVoice(
                com.example.haremdark.domain.VoiceTriggerType.AFFINITY_LEVEL_UP,
                character
            )
        } else {
            SoundEffectManager.playHarem(HaremSound.FLIRT)
        }

        return Pair(true, logText)
    }

    fun setFavorite(characterId: String): String {
        var msg = ""
        updateState { current ->
            val updated = current.characters.map { c ->
                val copy = c.copy()
                if (copy.id == characterId) {
                    copy.oblibena = true
                    copy.loajalita = (copy.loajalita + 15).coerceAtMost(100)
                    copy.duvera = (copy.duvera + 10).coerceAtMost(100)
                    msg = "★ ${copy.name} byla jmenována tvou jedinou vyvolenou Oblíbenkyní! Ostatní v harému zatajily dech."
                } else {
                    copy.oblibena = false
                }
                copy
            }
            current.copy(characters = updated)
        }
        addLog(msg)
        return msg
    }

    fun courtRomance(characterId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (current.player.gold < 50) {
            return Pair(false, "Dvoření vyžaduje 50 zlatých na dary a hostinu.")
        }
        current.player.gold -= 50
        character.romanceBody += 15
        character.duvera = (character.duvera + 10).coerceAtMost(100)
        character.srdce = (character.srdce + 12).coerceAtMost(100)

        if (character.romanceBody >= 50 && !character.partnerka) {
            character.partnerka = true
            addLog("♥ ${character.name} přijala tvůj slib a stala se tvou oficiální Partnerkou!")
            return Pair(true, "♥ ${character.name} je nyní tvou Partnerkou!")
        }

        val res = "${character.name} byla potěšena tvou přízní (Romance: ${character.romanceBody}/100)."
        addLog(res)
        updateState { it.copy() }
        return Pair(true, res)
    }

    fun marryConcubine(characterId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (character.romanceBody < 80 || character.loajalita < 70) {
            return Pair(false, "Svatba vyžaduje alespoň 80 Romance a 70 Loajality!")
        }
        if (current.player.gold < 300) {
            return Pair(false, "Královská svatba vyžaduje 300 zlatých na obřad.")
        }

        current.player.gold -= 300
        character.jeManzelkou = true
        character.partnerka = true
        character.loajalita = 100
        character.duvera = 100
        current.player.reputation += 15
        current.player.maxSexEnergy = (current.player.maxSexEnergy + 10).coerceAtMost(250)
        current.player.maxDarkEnergy = (current.player.maxDarkEnergy + 5).coerceAtMost(200)

        val msg = "💍 SLAVNOSTNÍ SVATBA! ${character.name} se stala tvou Manželkou a Paní dominia! Max energie trvale navýšena."
        addLog(msg)
        updateState { it.copy() }
        return Pair(true, msg)
    }

    // --- HUNTING & RECRUITMENT ---
    fun hunt(locationName: String): Pair<Character?, String> {
        val current = _gameState.value
        val player = current.player

        if (player.sexEnergy < 15) {
            return Pair(null, "Na lov potřebuješ alespoň 15 energie.")
        }
        player.sexEnergy -= 15

        val archetypes = StaticData.ARCHETYPES.keys.toList()
        val randomArchetype = archetypes.random()
        val randomName = StaticData.NAMES.filter { n -> current.characters.none { it.name == n } }.randomOrNull()
            ?: "Dívka ze stínů ${Random.nextInt(10, 99)}"

        val age = Random.nextInt(18, 28)
        val newGirl = Character(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            name = randomName,
            age = age,
            archetypeId = randomArchetype,
            hp = 100,
            maxHp = 100,
            srdce = Random.nextInt(40, 80),
            poslusnost = Random.nextInt(15, 45),
            vlhkost = Random.nextInt(30, 60),
            submisivita = Random.nextInt(20, 55),
            loajalita = Random.nextInt(10, 35),
            duvera = Random.nextInt(15, 40),
            touha = Random.nextInt(35, 75),
            strach = Random.nextInt(30, 70),
            broken = Random.nextInt(0, 20),
            fazeZkazenosti = 0,
            role = "Ulovená v lokaci $locationName"
        )

        updateState { it.copy(characters = it.characters + newGirl) }
        addHaremExp(20)
        addPlayerXp(25)

        val archetypeData = StaticData.ARCHETYPES[randomArchetype]
        val message = "🏹 Úspěšný lov v lokaci $locationName! Zajal jsi dívku jménem ${newGirl.name} (Věk: $age, Archetyp: ${archetypeData?.name ?: randomArchetype})."
        addLog(message)
        return Pair(newGirl, message)
    }

    // --- DOMAIN NAVIGATION & EXPLORATION ---
    fun travelToDomain(domainId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val domain = DomainData.getDomainById(domainId)
        val player = current.player

        if (current.currentDomainId == domainId) {
            return Pair(true, "Již se nacházíš v dominiu: ${domain.name} (${domain.title}).")
        }

        if (player.level < domain.minPlayerLevel) {
            return Pair(false, "Vstup do dominia '${domain.name}' vyžaduje úroveň pána alespoň ${domain.minPlayerLevel} (máš úroveň ${player.level})!")
        }

        if (player.sexEnergy < domain.travelCostEnergy) {
            return Pair(false, "Na přesun do dominia potřebuješ ${domain.travelCostEnergy} Sexuální energie!")
        }

        if (player.gold < domain.travelCostGold) {
            return Pair(false, "Na cestovní karavanu a ochranu potřebuješ ${domain.travelCostGold} zlatých!")
        }

        var goldChange = -domain.travelCostGold
        var darkEnergyChange = 0
        var xpChange = 20 + domain.difficultyStars * 10
        
        val encounterRoll = kotlin.random.Random.nextInt(100)
        val encounterMsg = when {
            encounterRoll < 15 -> {
                val foundGold = kotlin.random.Random.nextInt(50, 150)
                goldChange += foundGold
                "\n✨ Náhodná událost: Tvá karavana narazila na opuštěný vůz plný mincí. Získal jsi +$foundGold zlatých!"
            }
            encounterRoll < 30 -> {
                val foundDark = kotlin.random.Random.nextInt(10, 25)
                darkEnergyChange += foundDark
                "\n✨ Náhodná událost: Při průjezdu temným hvozdem jsi narazil na starý oltář a načerpal temnou sílu (+$foundDark temné energie)."
            }
            encounterRoll < 45 -> {
                xpChange += 25
                "\n⚔️ Náhodná událost: Přepadli vás lapkové, ale tvoji strážci je snadno zlikvidovali. Získal jsi dodatečné zkušenosti!"
            }
            encounterRoll < 60 -> {
                val foundGold = kotlin.random.Random.nextInt(100, 250)
                goldChange += foundGold
                "\n💎 Náhodná událost: V horském průsmyku jsi objevil ukrytý poklad zlodějů. Získal jsi +$foundGold zlatých!"
            }
            else -> "\nCesta proběhla klidně a bez dalších incidentů."
        }

        val msg = "🗺️ Přesunul jsi své sídlo a družinu do nového dominia: ${domain.name} • ${domain.title}!$encounterMsg"
        updateState { state ->
            val p = state.player.copy(
                sexEnergy = (state.player.sexEnergy - domain.travelCostEnergy).coerceAtLeast(0),
                gold = (state.player.gold + goldChange).coerceAtLeast(0),
                darkEnergy = (state.player.darkEnergy + darkEnergyChange).coerceAtMost(state.player.maxDarkEnergy)
            )
            val updatedUnlocked = if (!state.unlockedDomains.contains(domainId)) {
                state.unlockedDomains + domainId
            } else state.unlockedDomains
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = p,
                currentDomainId = domainId,
                unlockedDomains = updatedUnlocked,
                gameLog = logs
            )
        }
        addPlayerXp(xpChange)
        return Pair(true, msg)
    }

    fun fastTravelToDomain(domainId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val domain = DomainData.getDomainById(domainId)

        if (current.currentDomainId == domainId) {
            return Pair(true, "Již se nacházíš v dominiu: ${domain.name}.")
        }

        if (!current.unlockedDomains.contains(domainId)) {
            return Pair(false, "Do této lokace nemůžeš provést rychlé cestování, dokud ji nejdříve nenavštívíš standardní cestou!")
        }

        val energyCost = 1
        if (current.player.sexEnergy < energyCost) {
            return Pair(false, "Na rychlé cestování potřebuješ alespoň $energyCost Sexuální energie!")
        }

        val msg = "⚡ Rychlé cestování: Okamžitě ses teleportoval do již navštívené svatyně '${domain.name}'!"
        updateState { state ->
            val p = state.player.copy(
                sexEnergy = (state.player.sexEnergy - energyCost).coerceAtLeast(0)
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = p,
                currentDomainId = domainId,
                gameLog = logs
            )
        }
        return Pair(true, msg)
    }

    fun exploreDomain(domainId: String): Pair<Character?, String> {
        val current = _gameState.value
        val domain = DomainData.getDomainById(domainId)
        val player = current.player

        val energyCost = 15 + domain.difficultyStars * 2
        if (player.sexEnergy < energyCost) {
            return Pair(null, "Na průzkum dominia '${domain.name}' potřebuješ alespoň $energyCost Sexuální energie!")
        }

        val chosenArchetype = if (domain.potentialArchetypes.isNotEmpty()) {
            domain.potentialArchetypes.random()
        } else {
            StaticData.ARCHETYPES.keys.random()
        }

        val randomName = StaticData.NAMES.filter { n -> current.characters.none { it.name == n } }.randomOrNull()
            ?: "Dívka z ${domain.name} ${Random.nextInt(10, 99)}"

        val age = Random.nextInt(18, 27)
        val initialAffinity = Random.nextInt(10, 25)
        val newGirl = Character(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            name = randomName,
            age = age,
            archetypeId = chosenArchetype,
            hp = 100,
            maxHp = 100,
            srdce = Random.nextInt(50, 85),
            poslusnost = Random.nextInt(20, 50),
            vlhkost = Random.nextInt(35, 65),
            submisivita = Random.nextInt(25, 60),
            loajalita = Random.nextInt(15, 40),
            duvera = Random.nextInt(20, 45),
            touha = Random.nextInt(40, 80),
            strach = Random.nextInt(25, 65),
            broken = Random.nextInt(0, 15),
            fazeZkazenosti = 0,
            affinityPoints = initialAffinity,
            affinityLevel = AffinityData.getLevelForPoints(initialAffinity),
            role = "Ulovena v dominiu ${domain.name}"
        )

        val goldFound = Random.nextInt(20, 50) + domain.difficultyStars * 25
        val bonusDrop = domain.resourceDrops.randomOrNull() ?: "Zlato"

        val archetypeData = StaticData.ARCHETYPES[chosenArchetype]
        val message = "⚔️ Úspěšná výprava v dominiu '${domain.name}'! Nalezena dívka ${newGirl.name} (${archetypeData?.name ?: chosenArchetype})! Získáno: +$goldFound zl. a kořist [$bonusDrop]."

        updateState { state ->
            val p = state.player.copy(
                sexEnergy = (state.player.sexEnergy - energyCost).coerceAtLeast(0),
                gold = state.player.gold + goldFound
            )
            val updatedUnlocked = if (!state.unlockedDomains.contains(domainId)) {
                state.unlockedDomains + domainId
            } else state.unlockedDomains
            val logs = (listOf(message) + state.gameLog).take(30)
            state.copy(
                player = p,
                characters = state.characters + newGirl,
                currentDomainId = domainId,
                unlockedDomains = updatedUnlocked,
                gameLog = logs
            )
        }
        addHaremExp(25 + domain.difficultyStars * 10)
        addPlayerXp(30 + domain.difficultyStars * 15)
        progressMission("EXPLORE", 1)
        return Pair(newGirl, message)
    }

    // --- AUCTION HOUSE ---
    fun buyAuction(archetypeId: String, price: Int): Pair<Boolean, String> {
        val current = _gameState.value
        if (current.player.gold < price) {
            return Pair(false, "Nedostatek zlata pro nákup na dražbě (${current.player.gold}/$price zlatých)!")
        }

        val randomName = StaticData.NAMES.filter { n -> current.characters.none { it.name == n } }.randomOrNull()
            ?: "Otrokyně z aukce ${Random.nextInt(10, 99)}"

        val age = Random.nextInt(18, 26)
        val newCharacter = Character(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            name = randomName,
            age = age,
            archetypeId = archetypeId,
            hp = 100,
            maxHp = 100,
            srdce = 60,
            poslusnost = 40,
            submisivita = 50,
            loajalita = 30,
            duvera = 25,
            touha = 60,
            strach = 40,
            fazeZkazenosti = 1,
            role = "Zakoupená na dražbě"
        )

        val msg = "🏛️ Vydražil jsi otrokyni ${newCharacter.name} za $price zlatých!"
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - price).coerceAtLeast(0))
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = p, characters = state.characters + newCharacter, gameLog = logs)
        }
        addHaremExp(30)
        return Pair(true, msg)
    }

    // --- SLAVE RENTAL ---
    fun rentSlave(characterId: String, clientType: String, days: Int): Pair<Boolean, String> {
        val current = _gameState.value
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (character.naNajmu) {
            return Pair(false, "Dívka je již na nájmu u klienta ${character.klient}!")
        }
        if (character.hp < 40) {
            return Pair(false, "Dívka je příliš vyčerpaná na nájem!")
        }

        val dailyAdvance = when (clientType) {
            "Místní měšťané" -> 20
            "Cech bohatých kupců" -> 45
            "Šlechtický dvůr" -> 70
            "Otrokářský syndikát" -> 120
            "Inkviziční legie" -> 180
            else -> 45
        }

        val upfrontGold = days * dailyAdvance
        val msg = "💰 ${character.name} byla pronajata klientovi ($clientType) na $days dní. Obdržel jsi zálohu $upfrontGold zlatých."

        updateState { state ->
            val p = state.player.copy(gold = state.player.gold + upfrontGold)
            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    val copy = c.copy()
                    copy.naNajmu = true
                    copy.klient = clientType
                    copy.typNajmu = clientType
                    copy.najemZbyvaDni = days
                    copy
                } else c
            }
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = p, characters = updatedCharacters, gameLog = logs)
        }
        return Pair(true, msg)
    }

    // --- UPGRADES ---
    fun upgradeBuilding(buildingType: String): Pair<Boolean, String> {
        val current = _gameState.value
        val building = current.buildings.firstOrNull { it.type == buildingType }
            ?: return Pair(false, "Budova nenalezena.")

        val costGold = (building.baseCost * (building.level + 1))
        val costWood = (building.baseCostWood * (building.level + 1))
        val costStone = (building.baseCostStone * (building.level + 1))
        val costIron = (building.baseCostIron * (building.level + 1))
        
        if (current.player.gold < costGold || current.player.wood < costWood || current.player.stone < costStone || current.player.iron < costIron) {
            return Pair(false, "Nedostatek surovin! Potřebuješ: $costGold zl, $costWood dřeva, $costStone kamení, $costIron železa.")
        }

        val nextLevel = building.level + 1
        val msg = "🏰 Budova ${building.name} vylepšena na úroveň $nextLevel!"
        updateState { state ->
            val p = state.player.copy(
                gold = (state.player.gold - costGold).coerceAtLeast(0),
                wood = (state.player.wood - costWood).coerceAtLeast(0),
                stone = (state.player.stone - costStone).coerceAtLeast(0),
                iron = (state.player.iron - costIron).coerceAtLeast(0)
            )
            val updatedBuildings = state.buildings.map { b ->
                if (b.type == buildingType) {
                    val copy = b.copy()
                    copy.level = nextLevel
                    copy
                } else b
            }
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = p, buildings = updatedBuildings, gameLog = logs)
        }
        autoSave()
        return Pair(true, msg)
    }

    fun upgradeTerritory(territoryId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val territory = current.territories.firstOrNull { it.id == territoryId }
            ?: return Pair(false, "Území nenalezeno.")

        val cost = (territory.baseIncome * (territory.level + 1) * 3)
        if (current.player.gold < cost) {
            return Pair(false, "Ovládnutí území vyžaduje $cost zlatých (máš ${current.player.gold})!")
        }

        val nextLevel = territory.level + 1
        val msg = "🗡️ Území ${territory.name} povýšeno na úroveň $nextLevel! Pasivní příjem vzrostl."
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - cost).coerceAtLeast(0))
            val updatedTerritories = state.territories.map { t ->
                if (t.id == territoryId) {
                    val copy = t.copy()
                    copy.level = nextLevel
                    copy.securityLevel = (copy.securityLevel + 15).coerceAtMost(100)
                    copy
                } else t
            }
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = p, territories = updatedTerritories, gameLog = logs)
        }
        autoSave()
        return Pair(true, msg)
    }

    fun trainEndurance(): Pair<Boolean, String> {
        val current = _gameState.value
        val p = current.player
        val cost = 120 + (p.skills["vytrvalost"] ?: 0) * 80

        if (p.gold < cost) {
            return Pair(false, "Trénink výdrže vyžaduje $cost zlatých!")
        }
        if (p.skillPoints < 1) {
            return Pair(false, "Potřebuješ alespoň 1 volný bod dovednosti!")
        }

        val curEnd = p.skills["vytrvalost"] ?: 0
        val newEnd = curEnd + 1
        val newMaxSex = (p.maxSexEnergy + 8).coerceAtMost(250)
        val newMaxDark = (p.maxDarkEnergy + 5).coerceAtMost(200)

        val msg = "⚡ Trénink výdrže úspěšný! Max sex energie: $newMaxSex, Max temná energie: $newMaxDark."
        updateState { state ->
            val updatedSkills = state.player.skills.toMutableMap()
            updatedSkills["vytrvalost"] = newEnd
            val newP = state.player.copy(
                gold = (state.player.gold - cost).coerceAtLeast(0),
                skillPoints = (state.player.skillPoints - 1).coerceAtLeast(0),
                skills = updatedSkills,
                maxSexEnergy = newMaxSex,
                maxDarkEnergy = newMaxDark
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newP, gameLog = logs)
        }
        return Pair(true, msg)
    }

    fun upgradeSkill(skillKey: String): Pair<Boolean, String> {
        val current = _gameState.value
        val p = current.player
        if (p.skillPoints < 1) {
            return Pair(false, "Nemáš žádné volné body dovedností!")
        }

        val curVal = p.skills[skillKey] ?: 0
        val newVal = curVal + 1
        val msg = "⭐ Dovednost $skillKey zvýšena na $newVal!"
        updateState { state ->
            val updatedSkills = state.player.skills.toMutableMap()
            updatedSkills[skillKey] = newVal
            val newP = state.player.copy(
                skillPoints = (state.player.skillPoints - 1).coerceAtLeast(0),
                skills = updatedSkills
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newP, gameLog = logs)
        }
        return Pair(true, msg)
    }

    fun brewAlchemy(recipe: AlchemyRecipe): Pair<Boolean, String> {
        val current = _gameState.value
        val p = current.player

        if (p.gold < recipe.goldCost) {
            return Pair(false, "Nedostatek zlata pro alchymii (${p.gold}/${recipe.goldCost})!")
        }
        if (p.darkEnergy < recipe.darkCost) {
            return Pair(false, "Nedostatek temné energie (${p.darkEnergy}/${recipe.darkCost})!")
        }

        val msg = "🧪 Uvařil jsi ${recipe.resultItem.name}!"
        updateState { state ->
            val newItems = state.player.items.map { it.copy() }.toMutableList()
            val existing = newItems.firstOrNull { it.id == recipe.resultItem.id }
            if (existing != null) {
                existing.count += 1
            } else {
                newItems.add(recipe.resultItem.copy())
            }

            val newP = state.player.copy(
                gold = (state.player.gold - recipe.goldCost).coerceAtLeast(0),
                darkEnergy = (state.player.darkEnergy - recipe.darkCost).coerceAtLeast(0),
                items = newItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newP, gameLog = logs)
        }
        addPlayerXp(18)
        return Pair(true, msg)
    }

    // --- UNDERWORLD DRUGS & CARTEL SYSTEM ---

    fun craftDrug(drugId: String, batchCount: Int = 1): Pair<Boolean, String> {
        val countToCraft = batchCount.coerceAtLeast(1)
        val drug = DrugData.getDrugById(drugId) ?: return Pair(false, "Neznámá substance.")
        val current = _gameState.value
        val p = current.player

        val totalGold = drug.goldCost * countToCraft
        val totalDark = drug.darkCost * countToCraft
        val totalWood = drug.woodCost * countToCraft
        val totalMana = drug.manaCost * countToCraft

        if (p.gold < totalGold) {
            return Pair(false, "Nedostatek zlata pro syntézu (${p.gold}/$totalGold zlatých)!")
        }
        if (p.darkEnergy < totalDark) {
            return Pair(false, "Nedostatek temné energie (${p.darkEnergy}/$totalDark TE)!")
        }
        if (p.wood < totalWood) {
            return Pair(false, "Nedostatek bylin a esencí (${p.wood}/$totalWood)!")
        }
        if (p.mana < totalMana) {
            return Pair(false, "Nedostatek many (${p.mana}/$totalMana)!")
        }

        // Check required ingredients
        for ((ingId, neededPerBatch) in drug.requiredIngredients) {
            val totalNeeded = neededPerBatch * countToCraft
            val ingDef = DrugData.getIngredientById(ingId)
            val available = p.items.firstOrNull { it.id == ingId }?.count ?: 0
            if (available < totalNeeded) {
                return Pair(false, "Nedostatek suroviny: ${ingDef?.name ?: ingId} (máš $available/$totalNeeded)!")
            }
        }

        // Harem role perk: Správkyně laboratoře gives +50% extra yield
        val hasLabOverseer = current.characters.any { it.role == "Správkyně laboratoře" }
        val baseYield = drug.yieldCount * countToCraft
        val yield = if (hasLabOverseer) (baseYield * 1.5).toInt().coerceAtLeast(baseYield + 1) else baseYield

        val item = DrugData.toInventoryItem(drug, count = yield)
        val overseerNote = if (hasLabOverseer) " (Bonus Správkyně laboratoře: +${yield - baseYield} dávek!)" else ""
        val msg = "⚗️ V laboratoři jsi syntetizoval ${yield}x ${drug.name} [${drug.streetName}]$overseerNote!"

        updateState { state ->
            val newItems = state.player.items.map { it.copy() }.toMutableList()

            // Deduct ingredients
            for ((ingId, neededPerBatch) in drug.requiredIngredients) {
                val totalNeeded = neededPerBatch * countToCraft
                val existingIng = newItems.firstOrNull { it.id == ingId }
                if (existingIng != null) {
                    existingIng.count -= totalNeeded
                }
            }
            newItems.removeAll { it.category == "ingredient" && it.count <= 0 }

            // Add crafted drug
            val existing = newItems.firstOrNull { it.id == drug.id }
            if (existing != null) {
                existing.count += yield
            } else {
                newItems.add(item)
            }

            val newP = state.player.copy(
                gold = (state.player.gold - totalGold).coerceAtLeast(0),
                darkEnergy = (state.player.darkEnergy - totalDark).coerceAtLeast(0),
                wood = (state.player.wood - totalWood).coerceAtLeast(0),
                mana = (state.player.mana - totalMana).coerceAtLeast(0),
                drugsCraftedTotal = state.player.drugsCraftedTotal + yield,
                items = newItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newP, gameLog = logs)
        }
        addPlayerXp(20 * countToCraft)
        return Pair(true, msg)
    }

    fun startSourcingExpedition(expeditionId: String): Pair<Boolean, String> {
        val exp = DrugData.getExpeditionById(expeditionId) ?: return Pair(false, "Neznámá výprava.")
        val current = _gameState.value
        val p = current.player

        if (p.sexEnergy < exp.energyCost) {
            return Pair(false, "Nedostatek energie (${p.sexEnergy}/${exp.energyCost} SE) pro vyslání výpravy!")
        }
        if (p.darkEnergy < exp.darkCost) {
            return Pair(false, "Nedostatek temné energie (${p.darkEnergy}/${exp.darkCost} TE) pro ochranu výpravy!")
        }

        // Harem scout perks
        val scoutConcubine = current.characters.firstOrNull { it.role == "Strážkyně ložnice" || it.role == "Mafiánská kurýrka" }
        val bonusBatch = if (scoutConcubine != null) 1 else 0

        // Gather 2 random ingredients from possible
        val gathered = exp.possibleIngredients.shuffled().take(2).map { ingId ->
            val ingDef = DrugData.getIngredientById(ingId)!!
            val amount = kotlin.random.Random.nextInt(2, 4) + bonusBatch
            ingDef to amount
        }

        val scoutNote = if (scoutConcubine != null) " (Doprovod ${scoutConcubine.name}: +1 bonus k nalezeným surovinám!)" else ""
        val summary = gathered.joinToString(", ") { "${it.second}x ${it.first.name} ${it.first.icon}" }
        val msg = "🌿 Výprava do '${exp.name}' úspěšná! Nalezeno: $summary$scoutNote."

        updateState { state ->
            val updatedItems = state.player.items.map { it.copy() }.toMutableList()
            for ((ing, amt) in gathered) {
                val existing = updatedItems.firstOrNull { it.id == ing.id }
                if (existing != null) {
                    existing.count += amt
                } else {
                    updatedItems.add(DrugData.ingredientToInventoryItem(ing, count = amt))
                }
            }

            val newPlayer = state.player.copy(
                sexEnergy = (state.player.sexEnergy - exp.energyCost).coerceAtLeast(0),
                darkEnergy = (state.player.darkEnergy - exp.darkCost).coerceAtLeast(0),
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, gameLog = logs)
        }
        addPlayerXp(25)
        return Pair(true, msg)
    }

    fun buyIngredientBlackMarket(ingredientId: String, count: Int = 1): Pair<Boolean, String> {
        val ing = DrugData.getIngredientById(ingredientId) ?: return Pair(false, "Neznámá surovina.")
        val current = _gameState.value
        val p = current.player
        val totalCost = ing.buyPrice * count

        if (p.gold < totalCost) {
            return Pair(false, "Nedostatek zlata pro nákup na černém trhu (${p.gold}/$totalCost zlatých)!")
        }

        val msg = "💰 Zakoupeno ${count}x ${ing.name} ${ing.icon} za $totalCost zlatých od pašeráků."

        updateState { state ->
            val updatedItems = state.player.items.map { it.copy() }.toMutableList()
            val existing = updatedItems.firstOrNull { it.id == ing.id }
            if (existing != null) {
                existing.count += count
            } else {
                updatedItems.add(DrugData.ingredientToInventoryItem(ing, count = count))
            }

            val newPlayer = state.player.copy(
                gold = state.player.gold - totalCost,
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, gameLog = logs)
        }
        return Pair(true, msg)
    }

    fun harvestEstateGarden(): Pair<Boolean, String> {
        val current = _gameState.value
        val p = current.player

        if (p.greenhouseHarvestDay == p.day) {
            return Pair(false, "Dnešní sklizeň v pěstírně již proběhla! Nové byliny vykvetou zítra za úsvitu.")
        }

        val sage = DrugData.getIngredientById("ing_mountain_sage")!!
        val lotus = DrugData.getIngredientById("ing_lotus_leaf")!!
        val poppy = DrugData.getIngredientById("ing_shadow_poppy")!!
        val starDust = DrugData.getIngredientById("ing_star_dust")!!

        val sageCount = kotlin.random.Random.nextInt(2, 4)
        val lotusCount = kotlin.random.Random.nextInt(1, 3)
        val poppyCount = kotlin.random.Random.nextInt(2, 4)
        val harmonyBonus = if (p.haremHarmony >= 80) 1 else 0

        val harvested = mutableListOf(
            sage to sageCount,
            lotus to lotusCount,
            poppy to poppyCount
        )
        if (harmonyBonus > 0) {
            harvested.add(starDust to 1)
        }

        val harmonyNote = if (harmonyBonus > 0) " (Vysoká harmonie harému vyvolala rozkvět Hvězdného prachu!)" else ""
        val summary = harvested.joinToString(", ") { "${it.second}x ${it.first.name} ${it.first.icon}" }
        val msg = "🌱 Sklizeň v alchymistické pěstírně: Získáno $summary$harmonyNote!"

        updateState { state ->
            val updatedItems = state.player.items.map { it.copy() }.toMutableList()
            for ((ing, amt) in harvested) {
                val existing = updatedItems.firstOrNull { it.id == ing.id }
                if (existing != null) {
                    existing.count += amt
                } else {
                    updatedItems.add(DrugData.ingredientToInventoryItem(ing, count = amt))
                }
            }

            val newPlayer = state.player.copy(
                greenhouseHarvestDay = state.player.day,
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, gameLog = logs)
        }
        addPlayerXp(20)
        return Pair(true, msg)
    }

    fun activateDrugBuff(drugId: String, targetType: String, characterId: String? = null, autoRenew: Boolean = true): Pair<Boolean, String> {
        val drug = DrugData.getDrugById(drugId) ?: return Pair(false, "Neznámá substance.")
        val current = _gameState.value
        val hasStock = current.player.items.any { it.id == drugId && it.count > 0 }
        if (!hasStock) {
            return Pair(false, "Nemáš žádnou dávku ${drug.name} skladem! Nejprve ji vyrob v laboratoři.")
        }

        val targetName = if (targetType == "player") "Pán" else (current.characters.firstOrNull { it.id == characterId }?.name ?: "Dívka")

        // Apply immediate drug effect
        if (targetType == "player") {
            val (ok, resMsg) = useDrugOnPlayer(drugId)
            if (!ok) return Pair(false, resMsg)
        } else if (characterId != null) {
            val (ok, resMsg) = administerDrugToConcubine(drugId, characterId)
            if (!ok) return Pair(false, resMsg)
        }

        val buffId = "${drugId}_${characterId ?: "player"}"
        val effectText = if (targetType == "player") drug.playerEffectSummary else drug.concubineEffectSummary

        val newBuff = com.example.haremdark.models.ActiveDrugBuff(
            id = buffId,
            drugId = drug.id,
            name = drug.name,
            icon = drug.icon,
            targetType = targetType,
            targetCharacterId = characterId,
            targetCharacterName = targetName,
            remainingDays = 3,
            maxDays = 3,
            effectSummary = effectText,
            category = drug.category,
            autoRenew = autoRenew
        )

        updateState { state ->
            val filtered = state.activeDrugBuffs.filterNot { it.id == buffId }
            state.copy(activeDrugBuffs = filtered + newBuff)
        }

        val msg = "✨ Aktivováno posílení '${drug.name}' pro $targetName na 3 dny (Denní udržování: ${if (autoRenew) "ZAPNUTO" else "VYPNUTO"})!"
        return Pair(true, msg)
    }

    fun toggleBuffAutoRenew(buffId: String): Pair<Boolean, String> {
        var newState = false
        updateState { state ->
            val updated = state.activeDrugBuffs.map { buff ->
                if (buff.id == buffId) {
                    newState = !buff.autoRenew
                    buff.copy(autoRenew = newState)
                } else buff
            }
            state.copy(activeDrugBuffs = updated)
        }
        return Pair(true, if (newState) "Automatická obnova zapnuta." else "Automatická obnova vypnuta.")
    }

    fun dismissDrugBuff(buffId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val buff = current.activeDrugBuffs.firstOrNull { it.id == buffId }
            ?: return Pair(false, "Posílení nebylo nalezeno.")

        updateState { state ->
            state.copy(activeDrugBuffs = state.activeDrugBuffs.filterNot { it.id == buffId })
        }
        return Pair(true, "Posílení ${buff.name} pro ${buff.targetCharacterName} bylo ukončeno.")
    }

    fun useDrugOnPlayer(drugId: String): Pair<Boolean, String> {
        val drug = DrugData.getDrugById(drugId) ?: return Pair(false, "Neznámá substance.")
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == drugId && it.count > 0 }
            ?: return Pair(false, "Tuto substanci nemáš v zásobách!")

        val msg = "🌿 ${drug.name}: ${drug.flavorMessagePlayer}"

        updateState { state ->
            val p = state.player
            var newHp = p.hp
            var newSex = p.sexEnergy
            var newDark = p.darkEnergy
            var newDominance = p.dominance
            var newToxicity = (p.toxicity + drug.toxicityGain).coerceIn(0, p.maxToxicity)

            when (drugId) {
                "drug_cerny_lotos" -> {
                    newDark = (p.darkEnergy + 50).coerceAtMost(p.maxDarkEnergy)
                    newSex = (p.sexEnergy + 35).coerceAtMost(p.maxSexEnergy)
                }
                "drug_krvavy_prach" -> {
                    newHp = (p.hp + 25).coerceAtMost(p.maxHp)
                    newDominance += 3
                }
                "drug_stinove_opium" -> {
                    newHp = (p.hp + 60).coerceAtMost(p.maxHp)
                }
                "drug_krystalicka_extaze" -> {
                    newSex = (p.sexEnergy + 45).coerceAtMost(p.maxSexEnergy)
                    newDominance += 2
                }
                "drug_nocni_bes" -> {
                    newDark = (p.darkEnergy + 25).coerceAtMost(p.maxDarkEnergy)
                    newHp = (p.hp + 20).coerceAtMost(p.maxHp)
                }
                "drug_esence_zapomneni" -> {
                    newDark = (p.darkEnergy + 40).coerceAtMost(p.maxDarkEnergy)
                }
                "drug_cistici_elixir" -> {
                    newHp = (p.hp + 35).coerceAtMost(p.maxHp)
                    newToxicity = (p.toxicity - 40).coerceAtLeast(0)
                }
            }

            val updatedItems = p.items.mapNotNull { itm ->
                if (itm.id == drugId) {
                    val remaining = itm.count - 1
                    if (remaining > 0) itm.copy(count = remaining) else null
                } else itm.copy()
            }.toMutableList()

            val newPlayer = p.copy(
                hp = newHp,
                sexEnergy = newSex,
                darkEnergy = newDark,
                dominance = newDominance,
                toxicity = newToxicity,
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, gameLog = logs)
        }

        // If in combat, apply combat boost
        val session = _combatState.value
        if (session != null && !session.isOver) {
            val entries = session.logEntries.toMutableList()
            var newPlayerHp = session.playerHp
            when (drugId) {
                "drug_krvavy_prach" -> {
                    entries.add(CombatLogEntry(
                        turn = session.turnCount,
                        type = "buff",
                        message = "🩸 Krvavý amok! Útok pána vzrostl a v očích mu planou démonické plameny!"
                    ))
                }
                "drug_nocni_bes" -> {
                    entries.add(CombatLogEntry(
                        turn = session.turnCount,
                        type = "buff",
                        message = "🐍 Reflexy stínové zmije! Obrana pána vzrostla a bleskově uhýbá úderům!"
                    ))
                }
                "drug_stinove_opium", "drug_cistici_elixir" -> {
                    newPlayerHp = (session.playerHp + 45).coerceAtMost(session.playerMaxHp)
                }
            }
            _combatState.value = session.copy(logEntries = entries, playerHp = newPlayerHp)
        }

        addPlayerXp(12)
        return Pair(true, msg)
    }

    fun administerDrugToConcubine(drugId: String, characterId: String): Pair<Boolean, String> {
        val drug = DrugData.getDrugById(drugId) ?: return Pair(false, "Neznámá substance.")
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == drugId && it.count > 0 }
            ?: return Pair(false, "Tuto substanci nemáš v zásobách!")
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        var msg = "${character.name} ${drug.flavorMessageConcubine}"

        updateState { state ->
            val updatedItems = state.player.items.mapNotNull { itm ->
                if (itm.id == drugId) {
                    val remaining = itm.count - 1
                    if (remaining > 0) itm.copy(count = remaining) else null
                } else itm.copy()
            }.toMutableList()

            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    if (drugId == "drug_cistici_elixir") {
                        val newAddiction = (c.zavislost - 35).coerceAtLeast(0)
                        val newType = if (newAddiction == 0) null else c.typZavislosti
                        c.copy(
                            zavislost = newAddiction,
                            typZavislosti = newType,
                            hp = (c.hp + 30).coerceAtMost(c.maxHp),
                            duvera = (c.duvera + 12).coerceAtMost(100),
                            lastInteractionDay = state.player.day
                        )
                    } else {
                        val newAddiction = (c.zavislost + drug.addictionGain).coerceAtMost(100)
                        var newTouha = c.touha
                        var newVlhkost = c.vlhkost
                        var newLoajalita = c.loajalita
                        var newPoslusnost = c.poslusnost
                        var newStrach = c.strach
                        var newSubmisivita = c.submisivita
                        var newBloodlust = c.bloodlust
                        var newDuvera = c.duvera
                        var extraAffinity = 15

                        when (drugId) {
                            "drug_cerny_lotos" -> {
                                newTouha = (newTouha + 35).coerceAtMost(100)
                                newVlhkost = (newVlhkost + 30).coerceAtMost(100)
                                newLoajalita = (newLoajalita + 20).coerceAtMost(100)
                                extraAffinity = 18
                            }
                            "drug_krvavy_prach" -> {
                                newTouha = (newTouha + 30).coerceAtMost(100)
                                newBloodlust = (newBloodlust + 25).coerceAtMost(100)
                                extraAffinity = 14
                            }
                            "drug_stinove_opium" -> {
                                newStrach = (newStrach - 30).coerceAtLeast(0)
                                newPoslusnost = (newPoslusnost + 25).coerceAtMost(100)
                                newSubmisivita = (newSubmisivita + 25).coerceAtMost(100)
                                extraAffinity = 12
                            }
                            "drug_krystalicka_extaze" -> {
                                newTouha = (newTouha + 45).coerceAtMost(100)
                                newVlhkost = (newVlhkost + 40).coerceAtMost(100)
                                newLoajalita = (newLoajalita + 25).coerceAtMost(100)
                                extraAffinity = 22
                            }
                            "drug_nocni_bes" -> {
                                newBloodlust = (newBloodlust + 15).coerceAtMost(100)
                                newPoslusnost = (newPoslusnost + 15).coerceAtMost(100)
                                extraAffinity = 12
                            }
                            "drug_esence_zapomneni" -> {
                                newStrach = (newStrach - 40).coerceAtLeast(0)
                                newDuvera = (newDuvera + 35).coerceAtMost(100)
                                newLoajalita = (newLoajalita + 30).coerceAtMost(100)
                                newSubmisivita = (newSubmisivita + 25).coerceAtMost(100)
                                extraAffinity = 25
                            }
                        }

                        val newAff = c.affinityPoints + extraAffinity
                        c.copy(
                            zavislost = newAddiction,
                            typZavislosti = drug.name,
                            touha = newTouha,
                            vlhkost = newVlhkost,
                            loajalita = newLoajalita,
                            poslusnost = newPoslusnost,
                            strach = newStrach,
                            submisivita = newSubmisivita,
                            bloodlust = newBloodlust,
                            duvera = newDuvera,
                            affinityPoints = newAff,
                            affinityLevel = AffinityData.getLevelForPoints(newAff),
                            lastInteractionDay = state.player.day
                        )
                    }
                } else c
            }

            val newPlayer = state.player.copy(items = updatedItems)
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, characters = updatedCharacters, gameLog = logs)
        }

        addPlayerXp(15)
        return Pair(true, msg)
    }

    fun distributeDrugsOnTerritory(drugId: String, territoryId: String, quantity: Int = 1): Pair<Boolean, String> {
        val drug = DrugData.getDrugById(drugId) ?: return Pair(false, "Neznámá substance.")
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == drugId && it.count >= quantity }
            ?: return Pair(false, "Nemáš dostatek této substance pro distribuci (${quantity}x)!")
        val territory = current.territories.firstOrNull { it.id == territoryId }
            ?: return Pair(false, "Území nebylo nalezeno.")

        // Calculate sale profit
        val basePrice = drug.streetPrice * quantity
        val courierConcubine = current.characters.firstOrNull { it.role == "Mafiánská kurýrka" }
        val courierBonusMultiplier = if (courierConcubine != null) 1.35f else 1.0f
        val territoryMultiplier = 1.0f + (territory.level * 0.10f)

        val totalGold = (basePrice * courierBonusMultiplier * territoryMultiplier).toInt()
        val darkGain = 5 * quantity

        val courierNote = if (courierConcubine != null) " (Kurýrka ${courierConcubine.name}: +35% bonus!)" else ""
        val msg = "💰 Na černém trhu území '${territory.name}' bylo prodáno ${quantity}x ${drug.name} za $totalGold zlatých a +$darkGain TE$courierNote."

        updateState { state ->
            val updatedItems = state.player.items.mapNotNull { itm ->
                if (itm.id == drugId) {
                    val rem = itm.count - quantity
                    if (rem > 0) itm.copy(count = rem) else null
                } else itm.copy()
            }.toMutableList()

            val updatedTerritories = state.territories.map { t ->
                if (t.id == territoryId) {
                    t.copy(securityLevel = (t.securityLevel + (2 * quantity)).coerceAtMost(100))
                } else t
            }

            val newPlayer = state.player.copy(
                gold = state.player.gold + totalGold,
                darkEnergy = (state.player.darkEnergy + darkGain).coerceAtMost(state.player.maxDarkEnergy),
                drugsSoldTotal = state.player.drugsSoldTotal + quantity,
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = newPlayer,
                territories = updatedTerritories,
                gameLog = logs
            )
        }
        addPlayerXp(16 * quantity)
        return Pair(true, msg)
    }

    fun assignHaremRole(characterId: String, roleTitle: String): Pair<Boolean, String> {
        val current = _gameState.value
        val char = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        val msg = "👑 ${char.name} byla jmenována do role: $roleTitle!"

        updateState { state ->
            val updated = state.characters.map { c ->
                if (roleTitle == "Paní harému (První dáma)" && c.role == "Paní harému (První dáma)" && c.id != characterId) {
                    c.copy(role = "členka harému")
                } else if (c.id == characterId) {
                    c.copy(role = roleTitle)
                } else c
            }
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(characters = updated, gameLog = logs)
        }
        return Pair(true, msg)
    }

    fun hostHaremBanquet(): Pair<Boolean, String> {
        val current = _gameState.value
        val p = current.player
        val costGold = 60
        val costSex = 10

        if (p.gold < costGold) return Pair(false, "Nedostatek zlata na uspořádání hostiny ($costGold zlatých)!")
        if (p.sexEnergy < costSex) return Pair(false, "Nedostatek sexuální energie ($costSex SE)!")
        if (current.characters.isEmpty()) return Pair(false, "V harému nemáš žádné dívky!")

        val msg = "🥂 Uspořádal jsi velkolepou noční hostinu plnou vína a vybraných lahůdek pro celý svůj harém. Harmonie vzrostla na maximum (+20 harmonie, +6 loajalita všech dívek)!"

        updateState { state ->
            val updatedCharacters = state.characters.map { c ->
                c.copy(
                    loajalita = (c.loajalita + 6).coerceAtMost(100),
                    touha = (c.touha + 6).coerceAtMost(100),
                    duvera = (c.duvera + 4).coerceAtMost(100),
                    hp = (c.hp + 20).coerceAtMost(c.maxHp)
                )
            }
            val newPlayer = state.player.copy(
                gold = state.player.gold - costGold,
                sexEnergy = state.player.sexEnergy - costSex,
                haremHarmony = (state.player.haremHarmony + 20).coerceAtMost(100)
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, characters = updatedCharacters, gameLog = logs)
        }
        addPlayerXp(25)
        return Pair(true, msg)
    }

    fun giveDirectGift(giftId: String, characterId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val gift = GameContent.DIRECT_GIFTS.firstOrNull { it.id == giftId }
            ?: return Pair(false, "Dar nebyl nalezen.")
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (current.player.gold < gift.goldCost) {
            return Pair(false, "Nedostatek zlata! Potřebuješ ${gift.goldCost} zlatých (máš ${current.player.gold}).")
        }

        val affinityGain = (gift.loyaltyBoost + gift.trustBoost + gift.romanceBoost) / 2 + 10
        val prevAffinityLevel = character.affinityLevel
        val newAffinity = character.affinityPoints + affinityGain
        val newAffinityLvl = AffinityData.getLevelForPoints(newAffinity)
        val tierInfo = AffinityData.getTierForPoints(newAffinity)
        val levelUpAnnouncement = if (newAffinityLvl > prevAffinityLevel) {
            "\n🌟 Pouto posíleno! ${character.name} dosáhla úrovně vztahu ${tierInfo.level}: ${tierInfo.title}! ${tierInfo.combatBonusDescription}"
        } else ""
        val unlockedDialogue = AffinityData.getRandomActiveDialogue(character.copy(affinityPoints = newAffinity))
        val msg = "🎁 ${character.name} ${gift.flavorMessage} (+${gift.loyaltyBoost} loajalita, +${gift.desireBoost} touha, +$affinityGain náklonnost)\n💬 ${character.name}: „$unlockedDialogue“$levelUpAnnouncement"

        updateState { state ->
            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    val newLoyalty = (c.loajalita + gift.loyaltyBoost).coerceAtMost(100)
                    val newDesire = (c.touha + gift.desireBoost).coerceAtMost(100)
                    val newObedience = (c.poslusnost + gift.obedienceBoost).coerceAtMost(100)
                    val newTrust = (c.duvera + gift.trustBoost).coerceAtMost(100)
                    val newRomance = (c.romanceBody + gift.romanceBoost).coerceAtMost(100)
                    val isPartner = c.partnerka || newRomance >= 50
                    c.affinityHistory.add(AffinityPointRecord(state.player.day, newAffinity, "Dar: ${gift.name} (+$affinityGain pts)"))
                    c.copy(
                        loajalita = newLoyalty,
                        touha = newDesire,
                        poslusnost = newObedience,
                        duvera = newTrust,
                        romanceBody = newRomance,
                        partnerka = isPartner,
                        affinityPoints = newAffinity,
                        affinityLevel = newAffinityLvl,
                        lastInteractionDay = state.player.day
                    )
                } else c
            }
            val newPlayer = state.player.copy(
                gold = (state.player.gold - gift.goldCost).coerceAtLeast(0)
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = newPlayer,
                characters = updatedCharacters,
                gameLog = logs
            )
        }
        if (newAffinityLvl > prevAffinityLevel) {
            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
            val charForVoice = _gameState.value.characters.firstOrNull { it.id == characterId } ?: character
            com.example.haremdark.domain.VoiceManager.playTriggerVoice(
                com.example.haremdark.domain.VoiceTriggerType.AFFINITY_LEVEL_UP,
                charForVoice
            )
        } else {
            SoundEffectManager.playHarem(HaremSound.GIFT)
            com.example.haremdark.domain.VoiceManager.speak(unlockedDialogue, character.archetypeId)
        }
        addPlayerXp(12)
        progressMission("GIFT", 1)
        return Pair(true, msg)
    }

    fun purchaseGiftToInventory(giftId: String, count: Int = 1): Pair<Boolean, String> {
        val gift = com.example.haremdark.data.GiftInventoryCatalog.getGiftById(giftId)
            ?: return Pair(false, "Předmět nebyl nalezen.")
        val totalCost = gift.goldCost * count
        val current = _gameState.value
        if (current.player.gold < totalCost) {
            return Pair(false, "Nedostatek zlata! Potřebuješ $totalCost zlatých (máš ${current.player.gold}).")
        }

        updateState { state ->
            val updatedItems = state.player.items.toMutableList()
            val existing = updatedItems.find { it.id == gift.id }
            if (existing != null) {
                existing.count += count
            } else {
                updatedItems.add(
                    InventoryItem(
                        id = gift.id,
                        name = gift.name,
                        description = gift.description,
                        count = count,
                        price = gift.goldCost,
                        category = "gift",
                        icon = gift.icon,
                        rarity = gift.rarity.title,
                        effectDescription = "+${gift.baseAffinity} Náklonnost"
                    )
                )
            }
            state.copy(
                player = state.player.copy(
                    gold = state.player.gold - totalCost,
                    items = updatedItems
                ),
                gameLog = (listOf("🛍️ Zakoupeno: $count× ${gift.name} (${gift.icon}) do inventáře za $totalCost zlatých.") + state.gameLog).take(30)
            )
        }
        SoundEffectManager.playHarem(HaremSound.GIFT)
        return Pair(true, "Zakoupeno $count× ${gift.name} do tvého inventáře!")
    }

    fun giveCollectibleGift(
        characterId: String,
        giftId: String,
        count: Int = 1
    ): GiftActionResult? {
        val current = _gameState.value
        val gift = com.example.haremdark.data.GiftInventoryCatalog.getGiftById(giftId) ?: return null
        val character = current.characters.firstOrNull { it.id == characterId } ?: return null

        val inventoryItem = current.player.items.find { it.id == giftId }
        val availableCount = inventoryItem?.count ?: 0
        if (availableCount < count) return null

        val isFavorite = gift.isFavoriteOf(character.archetypeId)
        val singleAffinity = gift.calculateTotalAffinity(character.archetypeId)
        val singleLoyalty = gift.calculateLoyaltyGain(character.archetypeId)
        val singleDesire = gift.calculateDesireGain(character.archetypeId)
        val singleTrust = gift.calculateTrustGain(character.archetypeId)

        val totalAffinity = singleAffinity * count
        val totalLoyalty = singleLoyalty * count
        val totalDesire = singleDesire * count
        val totalTrust = singleTrust * count

        val prevAffinityLevel = character.affinityLevel
        val newAffinityPoints = character.affinityPoints + totalAffinity
        val newAffinityLevel = AffinityData.getLevelForPoints(newAffinityPoints)
        val leveledUp = newAffinityLevel > prevAffinityLevel
        val tierInfo = AffinityData.getTierForPoints(newAffinityPoints)

        val reactionQuote = gift.getReactionQuote(character.archetypeId, character.name)
        val levelUpMsg = if (leveledUp) {
            "\n🌟 Pouto posíleno! ${character.name} dosáhla úrovně ${tierInfo.level}: ${tierInfo.title}! ${tierInfo.combatBonusDescription}"
        } else ""

        val actionLog = "🎁 Darováno $count× ${gift.name} (${gift.icon}) pro ${character.name} (+$totalAffinity nákl., +$totalLoyalty loaj., +$totalDesire touha)$levelUpMsg"

        updateState { state ->
            val updatedItems = state.player.items.mapNotNull { item ->
                if (item.id == giftId) {
                    val remaining = item.count - count
                    if (remaining > 0) item.copy(count = remaining) else null
                } else item
            }.toMutableList()

            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    c.affinityHistory.add(
                        AffinityPointRecord(
                            day = state.player.day,
                            points = newAffinityPoints,
                            source = "Dar: $count× ${gift.name} (+$totalAffinity pts)"
                        )
                    )
                    c.copy(
                        loajalita = (c.loajalita + totalLoyalty).coerceIn(0, 100),
                        touha = (c.touha + totalDesire).coerceIn(0, 100),
                        duvera = (c.duvera + totalTrust).coerceIn(0, 100),
                        poslusnost = (c.poslusnost + (gift.obedienceBonus * count)).coerceIn(0, 100),
                        romanceBody = (c.romanceBody + (gift.desireBonus * count)).coerceIn(0, 100),
                        affinityPoints = newAffinityPoints,
                        affinityLevel = newAffinityLevel,
                        lastInteractionDay = state.player.day
                    )
                } else c
            }

            state.copy(
                player = state.player.copy(items = updatedItems),
                characters = updatedCharacters,
                gameLog = (listOf(actionLog) + state.gameLog).take(30)
            )
        }

        if (leveledUp) {
            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
            VoiceManager.playTriggerVoice(
                VoiceTriggerType.AFFINITY_LEVEL_UP,
                character.copy(affinityPoints = newAffinityPoints, affinityLevel = newAffinityLevel)
            )
        } else {
            SoundEffectManager.playHarem(HaremSound.GIFT)
            VoiceManager.speak(reactionQuote, character.archetypeId)
        }

        addPlayerXp(15 * count)
        progressMission("GIFT", count)

        return GiftActionResult(
            character = character.copy(affinityPoints = newAffinityPoints, affinityLevel = newAffinityLevel),
            gift = gift,
            quantity = count,
            affinityGained = totalAffinity,
            loyaltyGained = totalLoyalty,
            desireGained = totalDesire,
            trustGained = totalTrust,
            leveledUp = leveledUp,
            newAffinityLevel = newAffinityLevel,
            dialogueResponse = reactionQuote,
            isFavoriteMatch = isFavorite
        )
    }

    fun executeSpecialDialogueChoice(
        characterId: String,
        choice: com.example.haremdark.data.DialogueChoice
    ): Pair<Boolean, String> {
        val current = _gameState.value
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Konkubína nebyla nalezena.")

        val prevAffinityLevel = character.affinityLevel
        val newAffinity = character.affinityPoints + choice.affinityGain
        val newAffinityLvl = com.example.haremdark.data.AffinityData.getLevelForPoints(newAffinity)
        val tierInfo = com.example.haremdark.data.AffinityData.getTierForPoints(newAffinity)
        val levelUpAnnouncement = if (newAffinityLvl > prevAffinityLevel) {
            "\n🌟 Pouto posíleno! ${character.name} dosáhla úrovně vztahu ${tierInfo.level}: ${tierInfo.title}! ${tierInfo.combatBonusDescription}"
        } else ""

        val msg = "🕯️ Speciální událost s ${character.name}: ${choice.outcomeSummary}$levelUpAnnouncement"

        updateState { state ->
            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    var touha = c.touha
                    var poslusnost = c.poslusnost
                    var loajalita = c.loajalita
                    var duvera = c.duvera
                    var submisivita = c.submisivita
                    var vlhkost = c.vlhkost
                    var strach = c.strach
                    var srdce = c.srdce
                    var plodnost = c.plodnost
                    var maxHp = c.maxHp

                    choice.statChanges.forEach { (key, delta) ->
                        when (key) {
                            "touha" -> touha = (touha + delta).coerceIn(0, 100)
                            "poslusnost" -> poslusnost = (poslusnost + delta).coerceIn(0, 100)
                            "loajalita" -> loajalita = (loajalita + delta).coerceIn(0, 100)
                            "duvera" -> duvera = (duvera + delta).coerceIn(0, 100)
                            "submisivita" -> submisivita = (submisivita + delta).coerceIn(0, 100)
                            "vlhkost" -> vlhkost = (vlhkost + delta).coerceIn(0, 100)
                            "strach" -> strach = (strach + delta).coerceIn(0, 100)
                            "srdce" -> srdce = (srdce + delta).coerceIn(0, 100)
                            "plodnost" -> plodnost = (plodnost + delta).coerceIn(0, 100)
                            "maxHp" -> maxHp += delta
                        }
                    }

                    c.affinityHistory.add(AffinityPointRecord(state.player.day, newAffinity, "Volba: ${choice.text} (+${choice.affinityGain} pts)"))
                    c.copy(
                        touha = touha,
                        poslusnost = poslusnost,
                        loajalita = loajalita,
                        duvera = duvera,
                        submisivita = submisivita,
                        vlhkost = vlhkost,
                        strach = strach,
                        srdce = srdce,
                        plodnost = plodnost,
                        maxHp = maxHp,
                        hp = c.hp.coerceAtMost(maxHp),
                        affinityPoints = newAffinity,
                        affinityLevel = newAffinityLvl,
                        lastInteractionDay = state.player.day
                    )
                } else c
            }

            var newGold = state.player.gold
            var newDark = state.player.darkEnergy
            var newSex = state.player.sexEnergy
            var newMana = state.player.mana

            choice.statChanges.forEach { (key, delta) ->
                when (key) {
                    "gold" -> newGold = (newGold + delta).coerceAtLeast(0)
                    "darkEnergy" -> newDark = (newDark + delta).coerceIn(0, state.player.maxDarkEnergy)
                    "sexEnergy" -> newSex = (newSex + delta).coerceIn(0, state.player.maxSexEnergy)
                    "mana" -> newMana = (newMana + delta).coerceIn(0, state.player.maxMana)
                }
            }

            val newPlayer = state.player.copy(
                gold = newGold,
                darkEnergy = newDark,
                sexEnergy = newSex,
                mana = newMana
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = newPlayer,
                characters = updatedCharacters,
                gameLog = logs
            )
        }

        if (newAffinityLvl > prevAffinityLevel) {
            SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
            val charForVoice = _gameState.value.characters.firstOrNull { it.id == characterId } ?: character
            com.example.haremdark.domain.VoiceManager.playTriggerVoice(
                com.example.haremdark.domain.VoiceTriggerType.AFFINITY_LEVEL_UP,
                charForVoice
            )
        } else {
            SoundEffectManager.playEvent(EventSound.EVENT_CHOICE)
            com.example.haremdark.domain.VoiceManager.speak(choice.reactionText, character.archetypeId)
        }

        addPlayerXp(25)
        progressMission("INTERACT", 1)
        return Pair(true, msg)
    }

    fun useItemOnConcubine(itemId: String, characterId: String): Pair<Boolean, String> {
        if (itemId.startsWith("drug_")) {
            return administerDrugToConcubine(itemId, characterId)
        }
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count > 0 }
            ?: return Pair(false, "Předmět není v inventáři.")
        val character = current.characters.firstOrNull { it.id == characterId }
            ?: return Pair(false, "Dívka nenalezena.")

        var msg = "Předal jsi ${item.name} dívce ${character.name}."

        updateState { state ->
            val updatedItems = state.player.items.mapNotNull { itm ->
                if (itm.id == itemId) {
                    val remaining = itm.count - 1
                    if (remaining > 0) itm.copy(count = remaining) else null
                } else itm.copy()
            }.toMutableList()

            val updatedCharacters = state.characters.map { c ->
                if (c.id == characterId) {
                    when (itemId) {
                        "elixir_touhy" -> {
                            msg = "🔮 ${c.name} vypila Elixír touhy. Její tělo zaplavil horký žár (+30 touha, +25 vlhkost, +15 náklonnost)."
                            val newAff = c.affinityPoints + 15
                            c.copy(
                                touha = (c.touha + 30).coerceAtMost(100),
                                vlhkost = (c.vlhkost + 25).coerceAtMost(100),
                                affinityPoints = newAff,
                                affinityLevel = AffinityData.getLevelForPoints(newAff),
                                lastInteractionDay = state.player.day
                            )
                        }
                        "hojivy_balzam" -> {
                            msg = "🧪 Hojivý balzám ošetřil a zacelil zranění ${c.name} (+40 HP)."
                            c.copy(hp = (c.hp + 40).coerceAtMost(c.maxHp), lastInteractionDay = state.player.day)
                        }
                        "serum_poslusnost" -> {
                            msg = "💉 ${c.name} požila Sérum poslušnosti. Její odpor byl zlomen a odevzdala se tvé vůli (+25 poslušnost, +20 submisivita, +15 loajalita)."
                            val newAff = c.affinityPoints + 20
                            c.copy(
                                poslusnost = (c.poslusnost + 25).coerceAtMost(100),
                                submisivita = (c.submisivita + 20).coerceAtMost(100),
                                loajalita = (c.loajalita + 15).coerceAtMost(100),
                                affinityPoints = newAff,
                                affinityLevel = AffinityData.getLevelForPoints(newAff),
                                lastInteractionDay = state.player.day
                            )
                        }
                        "gift_roses" -> {
                            msg = "🌹 ${c.name} přijala kytici nočních růží s dojetím (+10 loajalita, +8 touha, +15 náklonnost)."
                            val newAff = c.affinityPoints + 15
                            c.copy(
                                loajalita = (c.loajalita + 10).coerceAtMost(100),
                                touha = (c.touha + 8).coerceAtMost(100),
                                romanceBody = (c.romanceBody + 10).coerceAtMost(100),
                                affinityPoints = newAff,
                                affinityLevel = AffinityData.getLevelForPoints(newAff),
                                lastInteractionDay = state.player.day
                            )
                        }
                        "drahy_obojek" -> {
                            msg = "👑 Pánův zlatý obojek byl uzamčen na hrdle ${c.name}. Její oddanost je absolutní (+30 loajalita, +25 poslušnost, +35 náklonnost)."
                            val newAff = c.affinityPoints + 35
                            c.copy(
                                loajalita = (c.loajalita + 30).coerceAtMost(100),
                                poslusnost = (c.poslusnost + 25).coerceAtMost(100),
                                submisivita = (c.submisivita + 20).coerceAtMost(100),
                                affinityPoints = newAff,
                                affinityLevel = AffinityData.getLevelForPoints(newAff),
                                lastInteractionDay = state.player.day
                            )
                        }
                        "gift_perfume" -> {
                            msg = "🌸 ${c.name} se navoněla nočním parfémem. Komnaty zaplnila sladká esence (+15 loajalita, +18 touha, +18 náklonnost)."
                            val newAff = c.affinityPoints + 18
                            c.copy(
                                loajalita = (c.loajalita + 15).coerceAtMost(100),
                                touha = (c.touha + 18).coerceAtMost(100),
                                vlhkost = (c.vlhkost + 15).coerceAtMost(100),
                                affinityPoints = newAff,
                                affinityLevel = AffinityData.getLevelForPoints(newAff),
                                lastInteractionDay = state.player.day
                            )
                        }
                        else -> {
                            msg = "🎁 Předal jsi ${item.name} dívce ${c.name} (+12 loajalita, +10 náklonnost)."
                            val newAff = c.affinityPoints + 10
                            c.copy(
                                loajalita = (c.loajalita + 12).coerceAtMost(100),
                                affinityPoints = newAff,
                                affinityLevel = AffinityData.getLevelForPoints(newAff),
                                lastInteractionDay = state.player.day
                            )
                        }
                    }
                } else c
            }

            val newPlayer = state.player.copy(items = updatedItems)
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = newPlayer,
                characters = updatedCharacters,
                gameLog = logs
            )
        }
        addPlayerXp(12)
        progressMission("GIFT", 1)
        return Pair(true, msg)
    }

    fun useCombatConsumableOnPlayer(itemId: String): Pair<Boolean, String> {
        if (itemId.startsWith("drug_")) {
            return useDrugOnPlayer(itemId)
        }
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count > 0 }
            ?: return Pair(false, "Předmět není k dispozici v brašně.")

        var msg = "Využil jsi ${item.name}."
        updateState { state ->
            val p = state.player
            var newHp = p.hp
            var newSex = p.sexEnergy
            var newDark = p.darkEnergy

            when (itemId) {
                "hojivy_balzam" -> {
                    val heal = 45
                    newHp = (p.hp + heal).coerceAtMost(p.maxHp)
                    msg = "🧪 Použil jsi Hojivý balzám! Tvá zranění byla ošetřena (+$heal HP)."
                }
                "elixir_touhy" -> {
                    newSex = (p.sexEnergy + 35).coerceAtMost(p.maxSexEnergy)
                    newDark = (p.darkEnergy + 35).coerceAtMost(p.maxDarkEnergy)
                    msg = "🔮 Vypil jsi Elixír touhy! Tvé tělo zaplavila vlna rozkoše a síly (+35 SE, +35 TE)."
                }
                "serum_poslusnost" -> {
                    newDark = (p.darkEnergy + 50).coerceAtMost(p.maxDarkEnergy)
                    msg = "💉 Použil jsi Sérum poslušnosti jako zdroj temné magie (+50 TE)."
                }
                else -> {
                    newHp = (p.hp + 30).coerceAtMost(p.maxHp)
                    msg = "✨ Použil jsi ${item.name} (+30 HP)."
                }
            }

            val updatedItems = p.items.mapNotNull { itm ->
                if (itm.id == itemId) {
                    val remaining = itm.count - 1
                    if (remaining > 0) itm.copy(count = remaining) else null
                } else itm.copy()
            }.toMutableList()

            val newPlayer = p.copy(
                hp = newHp,
                sexEnergy = newSex,
                darkEnergy = newDark,
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, gameLog = logs)
        }
        addPlayerXp(8)
        return Pair(true, msg)
    }

    fun inspectQuestItem(itemId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId }
            ?: return Pair(false, "Úkolový předmět nebyl nalezen.")

        val lore = when (itemId) {
            "cerna_pecet" -> "📜 Pečeť Černého syndikátu: Nese znak podsvětního cechu. Umožňuje zastrašovat vymahače a odemykat speciální nabídky na trhu otroků."
            "temny_klic" -> "🗝️ Klíč ke starým kobkám: Vykován z černé oceli a pokrytý runami. Pasuje do železných vrat v podzemí chrámu."
            "kralovska_listina" -> "⚜️ Královská výsadní listina: Puncovaná královskou pečetí. Poskytuje imunitu před okamžitými raziemi inkvizice a zvyšuje respekt šlechty."
            else -> "📜 ${item.name}: ${item.description}"
        }

        val msg = "🔍 Prozkoumán předmět: ${item.name}. $lore"
        addLog(msg)
        addPlayerXp(15)
        return Pair(true, lore)
    }

    fun sellInventoryItem(itemId: String, quantity: Int = 1): Pair<Boolean, String> {
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count >= quantity }
            ?: return Pair(false, "Nemáš dostatek kusů tohoto předmětu.")

        val earnedGold = item.price * quantity
        val msg = "💰 Prodal jsi ${quantity}x ${item.name} za $earnedGold zlatých!"

        updateState { state ->
            val updatedItems = state.player.items.mapNotNull { itm ->
                if (itm.id == itemId) {
                    val remaining = itm.count - quantity
                    if (remaining > 0) itm.copy(count = remaining) else null
                } else itm.copy()
            }.toMutableList()

            val newPlayer = state.player.copy(
                gold = state.player.gold + earnedGold,
                items = updatedItems
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = newPlayer, gameLog = logs)
        }
        return Pair(true, msg)
    }

    fun claimQuest(questId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val quest = GameContent.QUESTS.firstOrNull { it.id == questId }
            ?: return Pair(false, "Úkol nenalezen.")

        if (current.completedQuests.contains(questId)) {
            return Pair(false, "Úkol již byl splněn!")
        }
        if (current.player.level < quest.reqLevel) {
            return Pair(false, "Vyžaduje úroveň pána ${quest.reqLevel}!")
        }
        if (quest.reqCharacters > 0 && current.characters.size < quest.reqCharacters) {
            return Pair(false, "Vyžaduje alespoň ${quest.reqCharacters} otrokyň v harému!")
        }

        val msg = "📜 Úkol '${quest.title}' splněn! Obdržel jsi ${quest.rewardGold} zlatých a ${quest.rewardXp} XP."
        updateState { state ->
            val p = state.player.copy(
                gold = state.player.gold + quest.rewardGold,
                darkEnergy = (state.player.darkEnergy + quest.rewardDarkEnergy).coerceAtMost(state.player.maxDarkEnergy),
                reputation = state.player.reputation + quest.rewardReputation
            )
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(
                player = p,
                completedQuests = state.completedQuests + questId,
                gameLog = logs
            )
        }
        addPlayerXp(quest.rewardXp)
        return Pair(true, msg)
    }

    // --- COMBAT SYSTEM ---
    fun startBossCombat(boss: Boss, characterId: String? = null) {
        val current = _gameState.value
        val player = current.player
        
        var fighterName = "Pán Dominia"
        var fighterHp = player.hp
        var fighterMaxHp = player.maxHp
        
        if (characterId != null) {
            val char = current.characters.firstOrNull { it.id == characterId }
            if (char != null) {
                val hpBonus = char.equipment.values.filterNotNull().sumOf { it.hpBonus } + ((char.skills["vitality"] ?: 0) * 10)
                val affinityBonus = com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(char.affinityLevel)
                fighterName = char.name
                fighterMaxHp = char.maxHp + hpBonus + affinityBonus.hpBonus
                fighterHp = fighterMaxHp
            }
        } else {
            val haremAffinityHpBonus = current.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).hpBonus / 4 }
            fighterMaxHp = player.maxHp + haremAffinityHpBonus
            fighterHp = (player.hp + haremAffinityHpBonus).coerceAtMost(fighterMaxHp)
        }
        
        val fighterChar = current.characters.firstOrNull { it.id == characterId }
        val warCry = com.example.haremdark.domain.VoiceManager.playTriggerVoice(
            com.example.haremdark.domain.VoiceTriggerType.COMBAT_START,
            fighterChar,
            fighterName
        )

        val initialEntries = mutableListOf<CombatLogEntry>()
        initialEntries.add(CombatLogEntry(
            turn = 1,
            type = "system",
            message = "⚔️ $fighterName vstupuje do boje proti: ${boss.name} (${boss.phaseName})!"
        ))
        initialEntries.add(CombatLogEntry(
            turn = 1,
            type = "player_support",
            message = "📣 ${fighterChar?.name ?: "Pán Dominia"}: „$warCry“"
        ))

        if (characterId != null) {
            val char = current.characters.firstOrNull { it.id == characterId }
            if (char != null && char.affinityLevel >= 2) {
                val tier = com.example.haremdark.data.AffinityData.getTierForPoints(char.affinityPoints)
                initialEntries.add(CombatLogEntry(
                    turn = 1,
                    type = "buff",
                    message = "💖 Pouto oddanosti (${tier.title}): ${tier.combatBonusDescription}"
                ))
            }
        } else {
            val highAffinityCount = current.characters.count { it.affinityLevel >= 2 }
            if (highAffinityCount > 0) {
                val totalRegen = current.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).regenBonus }
                val totalDef = current.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).defenseBonus / 3 }
                initialEntries.add(CombatLogEntry(
                    turn = 1,
                    type = "buff",
                    message = "💖 Pasivní podpora harému ($highAffinityCount oddaných dívek): +$totalDef obrana, +$totalRegen HP/kolo!"
                ))
            }
        }
        
        _combatState.value = CombatSession(
            boss = boss,
            bossHp = boss.hp,
            bossMaxHp = boss.maxHp,
            playerHp = fighterHp,
            playerMaxHp = fighterMaxHp,
            deployedCharacterId = characterId,
            turnCount = 1,
            isDefending = false,
            enemyBleedTurns = 0,
            enemyStunned = false,
            activeBuff = null,
            logEntries = initialEntries,
            log = initialEntries.map { it.message },
            isOver = false,
            victory = false,
            lootGained = null
        )
        SoundEffectManager.playCombat(CombatSound.COMBAT_START)
    }

    fun executeCombatTurn(action: String, itemId: String? = null) {
        val session = _combatState.value ?: return
        if (session.isOver) return

        val currentGameState = _gameState.value
        val player = currentGameState.player
        
        var weaponDamage = 10
        var weaponName = "Holé pěsti"
        var combatSkill = player.skills["boj"] ?: 0
        var defenseSkill = player.skills["obrana"] ?: 0
        
        if (session.deployedCharacterId != null) {
            val char = currentGameState.characters.firstOrNull { it.id == session.deployedCharacterId }
            if (char != null) {
                combatSkill = char.skills["combat"] ?: 0
                defenseSkill = char.skills["defense"] ?: 0
                
                val combatBonus = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                val defBonus = char.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                
                combatSkill += combatBonus
                defenseSkill += defBonus
                
                val eqWeapon = char.equipment["weapon"]
                if (eqWeapon != null) {
                    weaponDamage = eqWeapon.combatBonus
                    weaponName = eqWeapon.name
                }
            }
        } else {
            val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull() ?: Weapon("Pěsti temnoty", "kratka", 10, 0)
            weaponDamage = weapon.damage
            weaponName = weapon.name
        }
        
        var newBossHp = session.bossHp
        var newPlayerHp = session.playerHp
        var newPlayerDark = player.darkEnergy
        
        var newBleedTurns = session.enemyBleedTurns
        var newStunned = session.enemyStunned
        var isDefending = false
        var activeBuff = session.activeBuff
        
        val newLogEntries = session.logEntries.toMutableList()
        val currentTurn = session.turnCount
        
        var isOver = false
        var victory = false
        var lootInfo: String? = null
        
        // 1. Process Player Action with Affinity Passive Combat Bonuses
        val deployedChar = currentGameState.characters.firstOrNull { it.id == session.deployedCharacterId }
        val deployedAffinityBonus = deployedChar?.let { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel) }
            ?: com.example.haremdark.data.AffinityCombatBonus(0, 0f, 0, 0, 0, 0)
        
        val haremCritBonus = currentGameState.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).critBonus / 3 } + deployedAffinityBonus.critBonus
        val haremDefenseBonus = currentGameState.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).defenseBonus / 3 } + deployedAffinityBonus.defenseBonus
        val haremDmgPercent = currentGameState.characters.sumOf { (com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).dmgMultiplierBonus * 0.4).toDouble() }.toFloat() + deployedAffinityBonus.dmgMultiplierBonus
        val totalAffinityMultiplier = 1.0f + haremDmgPercent
        val combatHeroName = if (session.deployedCharacterId != null) {
            currentGameState.characters.firstOrNull { it.id == session.deployedCharacterId }?.name ?: "Bojovnice"
        } else "Pán dominia"

        when (action) {
            "attack", "slash" -> {
                val isCrit = Random.nextInt(100) < (15 + combatSkill * 2 + haremCritBonus)
                val critMultiplier = if (isCrit) 1.65f else 1.0f
                val rawDmg = weaponDamage + combatSkill * 3 + Random.nextInt(-2, 5)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.35f)) * critMultiplier) * totalAffinityMultiplier).toInt().coerceAtLeast(6)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                val critText = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
                
                if (isCrit) {
                    SoundEffectManager.playCombat(CombatSound.CRITICAL_HIT)
                } else {
                    SoundEffectManager.playCombat(CombatSound.PLAYER_SLASH)
                }

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isCrit) "player_attack" else "player_attack",
                    message = "🗡️ Útok pomocí $weaponName udělil $finalDmg poškození!$critText",
                    actor = combatHeroName,
                    actionName = "Sek zbraní ($weaponName)",
                    damageDealt = finalDmg,
                    damageCalculation = "[$weaponName: $weaponDamage + Boj: ${combatSkill * 3}] * [Krit: x${"%.2f".format(critMultiplier)}] * [Harém: x${"%.2f".format(totalAffinityMultiplier)}] - [Obrana: ${"%.1f".format(session.boss.defense * 0.35f)}] = $finalDmg DMG",
                    narrativeText = if (isCrit) {
                        "Drtivý bleskový sek! Čepel $weaponName si našla nekrytou štěrbinu ve zbroji nepřítele a s ohlušujícím křupnutím rozťala tkáň!"
                    } else {
                        "Rychlý ztečný výpad zbraní $weaponName rozčísl stíny a zasáhl nepřítele do nekrytého boku."
                    }
                ))
            }
            "heavy_strike" -> {
                val isCrit = Random.nextInt(100) < (25 + haremCritBonus)
                val multiplier = if (isCrit) 2.2f else 1.5f
                val rawDmg = (weaponDamage * 1.5f) + combatSkill * 4 + Random.nextInt(2, 10)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.25f)) * multiplier) * totalAffinityMultiplier).toInt().coerceAtLeast(12)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)

                if (isCrit) {
                    SoundEffectManager.playCombat(CombatSound.CRITICAL_HIT)
                } else {
                    SoundEffectManager.playCombat(CombatSound.PLAYER_SLASH)
                }

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_special",
                    message = "⚔️ Těžký útok ubral ${finalDmg} HP!",
                    actor = combatHeroName,
                    actionName = "Zničující těžký útok",
                    damageDealt = finalDmg,
                    damageCalculation = "[Základ: ${(weaponDamage * 1.5f).toInt()} + Síla: ${combatSkill * 4}] * [Násobič: x${"%.2f".format(multiplier)}] * [Harém: x${"%.2f".format(totalAffinityMultiplier)}] - [Obrana: ${"%.1f".format(session.boss.defense * 0.25f)}] = $finalDmg DMG",
                    narrativeText = "Těžký obouruční rozmach otřásl zemí. Masivní úder dopadl plnou vahou a prorazil nepřátelský kryt v gejzíru trosek!"
                ))
            }
            "bleed_strike" -> {
                val isCrit = Random.nextInt(100) < (30 + haremCritBonus)
                val multiplier = if (isCrit) 1.8f else 1.3f
                val rawDmg = weaponDamage + combatSkill * 3 + Random.nextInt(4, 12)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.2f)) * multiplier) * totalAffinityMultiplier).toInt().coerceAtLeast(10)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                newBleedTurns = 3

                SoundEffectManager.playCombat(CombatSound.PLAYER_SLASH)

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_special",
                    message = "🩸 Krvavé bodnutí zasadilo ${finalDmg} zranění a otevřelo ránu krvácející 3 kola!",
                    actor = combatHeroName,
                    actionName = "Krvavé bodnutí do tepny",
                    damageDealt = finalDmg,
                    damageCalculation = "[$weaponName: $weaponDamage + Zásah: ${combatSkill * 3}] * [Bodnutí: x${"%.2f".format(multiplier)}] * [Harém: x${"%.2f".format(totalAffinityMultiplier)}] - [Obrana: ${"%.1f".format(session.boss.defense * 0.2f)}] = $finalDmg DMG (+Krvácení 3 kola)",
                    narrativeText = "Zákeřný hrot pronikl hluboko pod žebra. Temná krev se vyvalila z otevřené rány a protivník zachroptěl bolestí."
                ))
            }
            "dark_burst" -> {
                if (player.darkEnergy >= 10) {
                    player.darkEnergy -= 10
                    newPlayerDark = player.darkEnergy
                    val rawDmg = 28 + (player.skills["temnota"] ?: 0) * 5 + Random.nextInt(6, 14)
                    val finalDmg = (rawDmg * totalAffinityMultiplier).toInt().coerceAtLeast(18)
                    newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)

                    SoundEffectManager.playCombat(CombatSound.DARK_SPELL)

                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_spell",
                        message = "🔮 Temný výboj stínové energie prorazil obranu nepřítele za ${finalDmg} poškození (-10 TE)!",
                        actor = combatHeroName,
                        actionName = "Temný výboj stínové magie",
                        damageDealt = finalDmg,
                        damageCalculation = "[Magie temnoty: $rawDmg] * [Harém násobič: x${"%.2f".format(totalAffinityMultiplier)}] = $finalDmg magického DMG (-10 TE)",
                        narrativeText = "Stíny se shlukly kolem napřažené pravice a s hromovým třeskem vyšlehly vstříc cíli. Temný výboj sežehl auru nepřítele."
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš dostatek temné energie na Temný výboj (vyžaduje 10)!",
                        actor = "Systém",
                        actionName = "Nedostatek energie"
                    ))
                }
            }
            "curse_shadow" -> {
                if (player.darkEnergy >= 15) {
                    player.darkEnergy -= 15
                    newPlayerDark = player.darkEnergy
                    val curseDmg = ((25 + (player.skills["temnota"] ?: 0) * 4) * totalAffinityMultiplier).toInt()
                    newBossHp = (newBossHp - curseDmg).coerceAtLeast(0)
                    activeBuff = "Prokletí stínů (Nepřítel oslaben)"

                    SoundEffectManager.playCombat(CombatSound.DARK_SPELL)

                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_spell",
                        message = "👁️ Prokletí stínů srazilo nepřítele za $curseDmg zranění a oslabilo jeho útoky (-15 TE).",
                        actor = combatHeroName,
                        actionName = "Prokletí stínů",
                        damageDealt = curseDmg,
                        damageCalculation = "[Základ kletby: ${25 + (player.skills["temnota"] ?: 0) * 4}] * [Harém: x${"%.2f".format(totalAffinityMultiplier)}] = $curseDmg DMG + Oslabení útoků o -25%",
                        narrativeText = "Prastará zapovězená slova utvořila stínová pouta. Nepřítelovy končetiny ztěžkly a temná kletba začala sžírat jeho sílu."
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš dostatek temné energie na Prokletí (vyžaduje 15)!",
                        actor = "Systém",
                        actionName = "Nedostatek energie"
                    ))
                }
            }
            "soul_drain" -> {
                if (player.darkEnergy >= 20) {
                    player.darkEnergy -= 20
                    newPlayerDark = player.darkEnergy
                    val drainDmg = 32 + (player.skills["temnota"] ?: 0) * 5 + Random.nextInt(3, 9)
                    val healed = (drainDmg * 0.75f).toInt()
                    newBossHp = (newBossHp - drainDmg).coerceAtLeast(0)
                    newPlayerHp = (newPlayerHp + healed).coerceAtMost(session.playerMaxHp)

                    SoundEffectManager.playCombat(CombatSound.DARK_SPELL)

                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_spell",
                        message = "🖤 Vysátí duše vytrhlo z nepřítele životní esenci za $drainDmg poškození a uzdravilo tě o +$healed HP (-20 TE)!",
                        actor = combatHeroName,
                        actionName = "Vysátí duše",
                        damageDealt = drainDmg,
                        damageCalculation = "[Vysátí esence: $drainDmg DMG] -> Konverze života: +$healed HP (75% přeměna)",
                        narrativeText = "Z hrudi protivníka vytrhla černá chapadla vířící proud životní síly. Vstřebaná esence okamžitě zacelila tvé rány."
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš dostatek temné energie na Vysátí duše (vyžaduje 20)!",
                        actor = "Systém",
                        actionName = "Nedostatek energie"
                    ))
                }
            }
            "defend" -> {
                isDefending = true
                val gainedDark = 8
                player.darkEnergy = (player.darkEnergy + gainedDark).coerceAtMost(player.maxDarkEnergy)
                newPlayerDark = player.darkEnergy

                SoundEffectManager.playCombat(CombatSound.SHIELD_BLOCK)

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_defend",
                    message = "🛡️ Zaujal jsi neprostupný obranný postoj (-65% utrženého zranění v tomto kole, +$gainedDark TE)!",
                    actor = combatHeroName,
                    actionName = "Neprostupný kryt",
                    damageDealt = 0,
                    damageCalculation = "Aktivní blokování: redukce příchozího zranění o 65% • Obnova: +$gainedDark TE",
                    narrativeText = "Pevný kryt a magická bariéra vytvořily neprostupnou hradbu. Hrdina se připravil absorbovat a odrazit nadcházející nápor útoků."
                ))
            }
            "harem_support" -> {
                val characters = _gameState.value.characters
                val favorite = characters.firstOrNull { it.oblibena } ?: characters.firstOrNull { it.jeManzelkou } ?: characters.firstOrNull()
                if (favorite != null) {
                    val healAmt = 28 + (favorite.loajalita / 5)
                    val energyAmt = 15
                    newPlayerHp = (newPlayerHp + healAmt).coerceAtMost(session.playerMaxHp)
                    player.darkEnergy = (player.darkEnergy + energyAmt).coerceAtMost(player.maxDarkEnergy)
                    newPlayerDark = player.darkEnergy
                    activeBuff = "Požehnání harému (${favorite.name})"

                    SoundEffectManager.playHarem(HaremSound.SEDUCE)

                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_support",
                        message = "💖 ${favorite.name} ti poslala duševní podporu z harému! Obdržel jsi +$healAmt HP a +$energyAmt TE!",
                        actor = favorite.name,
                        actionName = "Požehnání harému",
                        damageDealt = 0,
                        damageCalculation = "Léčení: +$healAmt HP (28 + Loajalita/5) • Energie: +$energyAmt TE",
                        narrativeText = "Myšlenka na oddanou konkubínu ${favorite.name} a její vroucí pohled vnesly do tvého těla novou vlnu sil a odhodlání."
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ V tvém harému není žádná otrokyně, která by ti dodala sílu!",
                        actor = "Systém",
                        actionName = "Chybí podpora"
                    ))
                }
            }
            "char_special" -> {
                val deployedChar = currentGameState.characters.firstOrNull { it.id == session.deployedCharacterId }
                val charName = deployedChar?.name ?: "Bojovnice z harému"
                val charCombat = (deployedChar?.skills?.get("combat") ?: 1) + 4
                val isCrit = Random.nextInt(100) < (35 + haremCritBonus)
                val mult = if (isCrit) 2.2f else 1.6f
                val rawDmg = (weaponDamage * 1.6f) + charCombat * 5 + Random.nextInt(8, 18)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.2f)) * mult) * totalAffinityMultiplier).toInt().coerceAtLeast(16)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                val stunSuccess = Random.nextInt(100) < 35
                if (stunSuccess) {
                    newStunned = true
                }
                val stunText = if (stunSuccess) " • Protivník byl OMRÁČEN!" else ""
                val voiceShout = deployedChar?.let { com.example.haremdark.data.AffinityData.getRandomActiveDialogue(it) } ?: "Za mého pána!"

                SoundEffectManager.playCombat(CombatSound.CRITICAL_HIT)

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_special",
                    message = "✨ $charName: „$voiceShout“ aktivovala speciální techniku a zasadila $finalDmg poškození!$stunText",
                    actor = charName,
                    actionName = "Unikátní harémová technika",
                    damageDealt = finalDmg,
                    damageCalculation = "[$charName boj: $charCombat * 5 + Síla: ${(weaponDamage * 1.6f).toInt()}] * [Technika: x${"%.2f".format(mult)}] * [Pouto: x${"%.2f".format(totalAffinityMultiplier)}] = $finalDmg DMG",
                    narrativeText = "$charName se s půvabem a divokou oddaností vrhla vpřed. Se slovy „$voiceShout“ zasadila mistrný zásah!"
                ))
            }
            "item" -> {
                val targetItemId = itemId ?: "hojivy_balzam"
                val item = player.items.firstOrNull { it.id == targetItemId && it.count > 0 }
                if (item != null) {
                    item.count -= 1
                    if (item.count <= 0) player.items.remove(item)

                    when (item.id) {
                        "hojivy_balzam" -> {
                            val healAmt = 45
                            newPlayerHp = (newPlayerHp + healAmt).coerceAtMost(session.playerMaxHp)
                            SoundEffectManager.playHarem(HaremSound.GIFT)
                            newLogEntries.add(0, CombatLogEntry(
                                turn = currentTurn,
                                type = "player_heal",
                                message = "🧪 Použil jsi ${item.name} a vyléčil se o +$healAmt HP.",
                                actor = combatHeroName,
                                actionName = "Použití lektvaru",
                                damageDealt = 0,
                                damageCalculation = "Okamžitá regenerace: +$healAmt HP",
                                narrativeText = "Chladivý bylinný balzám pronikl do krvácejících ran a zacelil poškozené tkáně."
                            ))
                        }
                        "elixir_touhy" -> {
                            player.darkEnergy = (player.darkEnergy + 35).coerceAtMost(player.maxDarkEnergy)
                            player.sexEnergy = (player.sexEnergy + 35).coerceAtMost(player.maxSexEnergy)
                            newPlayerDark = player.darkEnergy
                            SoundEffectManager.playHarem(HaremSound.GIFT)
                            newLogEntries.add(0, CombatLogEntry(
                                turn = currentTurn,
                                type = "player_heal",
                                message = "🧪 Vypil jsi ${item.name}! Tvé tělo zaplavila energie (+35 TE, +35 SE).",
                                actor = combatHeroName,
                                actionName = "Vypití elixíru touhy",
                                damageDealt = 0,
                                damageCalculation = "Zisk energie: +35 TE, +35 SE",
                                narrativeText = "Rudý elixír rozdmýchal v žilách plamen touhy a stínové dominance."
                            ))
                        }
                        else -> {
                            val healAmt = 30
                            newPlayerHp = (newPlayerHp + healAmt).coerceAtMost(session.playerMaxHp)
                            SoundEffectManager.playHarem(HaremSound.GIFT)
                            newLogEntries.add(0, CombatLogEntry(
                                turn = currentTurn,
                                type = "player_heal",
                                message = "🧪 Využil jsi předmět ${item.name} (+30 HP).",
                                actor = combatHeroName,
                                actionName = "Použití předmětu",
                                damageDealt = 0,
                                damageCalculation = "Regenerace: +$healAmt HP",
                                narrativeText = "Předmět z batohu poskytl okamžitou úlevu uprostřed líté vřavy."
                            ))
                        }
                    }
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš tento předmět k dispozici!",
                        actor = "Systém",
                        actionName = "Předmět nedostupný"
                    ))
                }
            }
            "flee" -> {
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "system",
                    message = "🏃 Takticky jsi ustoupil a opustil bojiště.",
                    actor = combatHeroName,
                    actionName = "Ústup z boje",
                    narrativeText = "Pochopil jsi nevýhodu situace a rychlým ústupem do stínů jsi opustil nebezpečnou zónu."
                ))
                _combatState.value = null
                return
            }
        }

        // 2. Process Bleed Tick on Boss
        if (newBleedTurns > 0 && newBossHp > 0) {
            val bleedDmg = Random.nextInt(8, 14)
            newBossHp = (newBossHp - bleedDmg).coerceAtLeast(0)
            newBleedTurns -= 1
            newLogEntries.add(0, CombatLogEntry(
                turn = currentTurn,
                type = "player_spell",
                message = "🩸 Krvácení způsobilo nepříteli $bleedDmg zranění (zbývá $newBleedTurns kol).",
                actor = "Krvácení rány",
                actionName = "Účinek krvácení",
                damageDealt = bleedDmg,
                damageCalculation = "Ztráta krve: -$bleedDmg HP (zbývá $newBleedTurns kol)",
                narrativeText = "Z otevřené rány dál prudce prýští krev, oslabující nepřítelovu odolnost a reflexy."
            ))
        }

        // 3. Check Enemy Defeat
        if (newBossHp <= 0) {
            isOver = true
            victory = true
            player.gold += session.boss.rewardGold
            addPlayerXp(session.boss.rewardXp)
            player.killCount += 1
            player.battlesWon += 1
            
            var droppedItem: com.example.haremdark.models.InventoryItem? = null
            if (Random.nextInt(100) < 35) { // 35% chance to drop item
                val possibleDrops = listOf(
                    com.example.haremdark.models.InventoryItem("hojivy_balzam", "Hojivý balzám", "Okamžitě uzdravuje 45 HP.", 1, 25, "combat", "🧪", "Běžný", "+45 HP", null, 0, 0, 0),
                    com.example.haremdark.models.InventoryItem("krvavy_mec", "Krvavý meč", "Zvyšuje útok.", 1, 100, "equipment", "🗡️", "Vzácný", "+15 Boj", "weapon", 15, 0, 0),
                    com.example.haremdark.models.InventoryItem("stribrna_zbroj", "Stříbrná zbroj", "Zvyšuje obranu.", 1, 120, "equipment", "🛡️", "Vzácný", "+10 Obrana", "armor", 0, 10, 0)
                )
                droppedItem = possibleDrops.random()
                val items = player.items.toMutableList()
                val existingItemIdx = items.indexOfFirst { it.id == droppedItem.id }
                if (existingItemIdx != -1) {
                    val ei = items[existingItemIdx]
                    items[existingItemIdx] = ei.copy(count = ei.count + 1)
                } else {
                    items.add(droppedItem)
                }
                player.items = items
            }
            
            val itemDropStr = if (droppedItem != null) " • Nalezeno: ${droppedItem.name}" else ""
            val charExpStr = if (session.deployedCharacterId != null) " • Dívka +${session.boss.rewardXp} ZK" else ""
            lootInfo = "+${session.boss.rewardGold} zlatých • +${session.boss.rewardXp} XP$charExpStr$itemDropStr"

            SoundEffectManager.playCombat(CombatSound.VICTORY)
            
            newLogEntries.add(0, CombatLogEntry(
                turn = currentTurn,
                type = "victory",
                message = "🏆 VÍTĚZSTVÍ! Protivník ${session.boss.name} padl! Zisk: $lootInfo.",
                actor = "Dominium stínů",
                actionName = "Konečné vítězství",
                damageDealt = 0,
                damageCalculation = "Odměna: +${session.boss.rewardGold} G • +${session.boss.rewardXp} XP",
                narrativeText = "Protivník ${session.boss.name} klesl s posledním vzdechem k zemi. Bojiště utichlo a z jeho ostatků stoupá temná mlha plná kořisti a uznání!"
            ))
            addLog("🏆 Protivník ${session.boss.name} byl poražen v souboji!")
            updateState { it.copy(defeatedBosses = it.defeatedBosses + session.boss.id) }
        } else {
            // 4. Enemy Turn
            if (newStunned) {
                newStunned = false
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "system",
                    message = "💫 ${session.boss.name} je omráčen a vynechává své kolo!",
                    actor = session.boss.name,
                    actionName = "Stav omráčení",
                    narrativeText = "Protivník vrávorá a s omámeným zrakem se snaží znovu nabrat rovnováhu. Jeho tah propadá!"
                ))
            } else {
                val isSpecialAttack = (currentTurn % 3 == 0)
                val baseEnemyAtk = session.boss.attack
                val defenseReduction = (defenseSkill * 2.5f) + haremDefenseBonus

                val rawBossDmg = if (isSpecialAttack) {
                    (baseEnemyAtk * 1.45f).toInt() + Random.nextInt(1, 6)
                } else {
                    baseEnemyAtk + Random.nextInt(-2, 4)
                }

                var finalEnemyDmg = (rawBossDmg - defenseReduction).coerceAtLeast(3f).toInt()
                finalEnemyDmg = (finalEnemyDmg * (1.0f / totalAffinityMultiplier)).toInt()
                if (isDefending) {
                    finalEnemyDmg = (finalEnemyDmg * 0.35f).toInt().coerceAtLeast(2)
                }
                if (activeBuff?.contains("Prokletí") == true) {
                    finalEnemyDmg = (finalEnemyDmg * 0.75f).toInt().coerceAtLeast(2)
                }

                newPlayerHp = (newPlayerHp - finalEnemyDmg).coerceAtLeast(0)

                val attackTitle = if (isSpecialAttack) "💥 ${session.boss.name} provedl speciální techniku [${session.boss.phaseName}]" else "⚔️ ${session.boss.name} zaútočil"
                val defenseNotice = if (isDefending) " (útok odražen štítem!)" else ""

                if (isSpecialAttack) {
                    SoundEffectManager.playCombat(CombatSound.BOSS_SPECIAL)
                } else {
                    SoundEffectManager.playCombat(CombatSound.ENEMY_STRIKE)
                }

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isSpecialAttack) "enemy_special" else "enemy_attack",
                    message = "$attackTitle za $finalEnemyDmg poškození!$defenseNotice",
                    actor = session.boss.name,
                    actionName = if (isSpecialAttack) "Boss technika: ${session.boss.phaseName}" else "Zuřivý protiútok",
                    damageDealt = finalEnemyDmg,
                    damageCalculation = "[Útok bosse: $rawBossDmg] - [Obrana a harém: ${defenseReduction.toInt()}] * [Kryt: ${if (isDefending) "x0.35" else "x1.0"}] = $finalEnemyDmg DMG",
                    narrativeText = if (isSpecialAttack) {
                        "Aréna potemněla! ${session.boss.name} rozpoutal svou zhoubnou schopnost [${session.boss.phaseName}], jež zasáhla celou linii drtivou silou!"
                    } else {
                        "${session.boss.name} bleskově zaútočil zuřivým výpadem. Rána dopadla s brutální silou."
                    }
                ))

                if (newPlayerHp <= 0) {
                    isOver = true
                    victory = false
                    newPlayerHp = if (session.deployedCharacterId != null) 1 else 25
                    val msgName = if (session.deployedCharacterId != null) "Tvá dívka padla v boji" else "Byl jsi v boji poražen"

                    SoundEffectManager.playCombat(CombatSound.DEFEAT)

                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "defeat",
                        message = "💀 $msgName! Odnášíte zraněné do bezpečí pevnosti.",
                        actor = "Systém",
                        actionName = "Poražení v bitvě",
                        damageDealt = 0,
                        narrativeText = "Tlak nepřítele byl příliš zdrcující. Pod rouškou stínů stahujete raněné do bezpečí temných komnat, abyste nabrali nových sil."
                    ))
                    addLog("💀 Porážka v boji proti ${session.boss.name}!")
                }
            }
        }

        // 5. End of Turn Affinity Passives (Regen & Dark Energy)
        if (!isOver && newPlayerHp > 0) {
            val haremRegen = currentGameState.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).regenBonus }
            val totalRegen = haremRegen + deployedAffinityBonus.regenBonus
            if (totalRegen > 0 && newPlayerHp < session.playerMaxHp) {
                val healAmt = totalRegen.coerceAtMost(session.playerMaxHp - newPlayerHp)
                newPlayerHp += healAmt
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_heal",
                    message = "💖 Pouto náklonnosti: Pasivní regenerace harému vyléčila +$healAmt HP ($newPlayerHp/${session.playerMaxHp})!"
                ))
            }

            val darkGain = currentGameState.characters.sumOf { com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(it.affinityLevel).darkEnergyBonus } + deployedAffinityBonus.darkEnergyBonus
            if (darkGain > 0 && player.darkEnergy < player.maxDarkEnergy) {
                player.darkEnergy = (player.darkEnergy + darkGain).coerceAtMost(player.maxDarkEnergy)
                newPlayerDark = player.darkEnergy
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_spell",
                    message = "👑 Věčná královna: Pasivní dar věčného pouta obnovil +$darkGain temné energie ($newPlayerDark/${player.maxDarkEnergy})!"
                ))
            }
        }


        if (session.deployedCharacterId != null) {
            val updatedCharacters = currentGameState.characters.map { c ->
                if (c.id == session.deployedCharacterId) {
                    var newXp = c.xp + session.boss.rewardXp
                    var newLevel = c.level
                    var newSp = c.skillPoints
                    var newMaxHp = c.maxHp
                    var newHp = newPlayerHp
                    var nextLevelXp = newLevel * 100
                    
                    while (newXp >= nextLevelXp) {
                        newXp -= nextLevelXp
                        newLevel++
                        newSp++
                        newMaxHp += 5
                        newHp = newMaxHp // Full heal on level up
                        nextLevelXp = newLevel * 100
                    }
                    if (newPlayerHp > 0 && newHp < newMaxHp) {
                        newHp = newPlayerHp // Keep current damage if no level up
                    }
                    c.copy(hp = newHp, maxHp = newMaxHp, xp = newXp, level = newLevel, skillPoints = newSp)
                } else c
            }
            updateState { it.copy(characters = updatedCharacters) }
        } else {
            player.hp = newPlayerHp
        }

        
        _combatState.value = session.copy(
            bossHp = newBossHp,
            playerHp = newPlayerHp,
            turnCount = currentTurn + 1,
            isDefending = isDefending,
            enemyBleedTurns = newBleedTurns,
            enemyStunned = newStunned,
            activeBuff = activeBuff,
            logEntries = newLogEntries.take(40),
            log = newLogEntries.take(40).map { it.message },
            isOver = isOver,
            victory = victory,
            lootGained = lootInfo
        )
        updateState { it.copy() }
    }

    fun endCombat() {
        _combatState.value = null
        autoSave()
    }

    fun startPartyCombat(
        selectedGirlIds: List<String>,
        includePlayer: Boolean,
        encounter: com.example.haremdark.data.PartyCombatCatalog.PartyEncounterDefinition
    ) {
        val state = _gameState.value
        val session = PartyCombatManager.createSession(
            selectedCharacterIds = selectedGirlIds,
            allCharacters = state.characters,
            player = state.player,
            includePlayerAsLeader = includePlayer,
            encounterDef = encounter
        )
        _partyCombatSession.value = session
        addLog("⚔️ Zahájen skupinový tahový boj: ${encounter.title} (${session.party.size} bojovníků)!")
    }

    fun updatePartyCombatSession(session: PartyCombatSession) {
        _partyCombatSession.value = session
    }

    fun closePartyCombat() {
        _partyCombatSession.value = null
        autoSave()
    }

    fun awardPartyCombatVictory(
        gold: Int,
        xp: Int,
        prestige: Int,
        participatingGirlIds: List<String>,
        affinityGain: Int,
        loyaltyGain: Int
    ) {
        updateState { current ->
            val p = current.player.copy(
                gold = current.player.gold + gold,
                prestige = current.player.prestige + prestige
            )

            // Update participating girls' stats, affinity points, and loyalty
            val updatedCharacters = current.characters.map { girl ->
                if (participatingGirlIds.contains(girl.id)) {
                    val newAffinity = girl.affinityPoints + affinityGain
                    val newLoyalty = (girl.loajalita + loyaltyGain).coerceAtMost(100)
                    val newExp = girl.xp + xp / participatingGirlIds.size.coerceAtLeast(1)
                    val combatSkill = (girl.skills["combat"] ?: 5) + 1
                    val newSkills = girl.skills.toMutableMap().apply {
                        put("combat", combatSkill)
                    }
                    girl.copy(
                        affinityPoints = newAffinity,
                        loajalita = newLoyalty,
                        xp = newExp,
                        skills = newSkills
                    )
                } else girl
            }

            current.copy(
                player = p,
                characters = updatedCharacters
            )
        }

        addPlayerXp(xp)
        addHaremExp(xp / 2)
        addLog("🏆 Vítězství v týmovém souboji! Získáno: +$gold zlatých, +$xp XP, +$prestige prestiže a +$affinityGain náklonnosti pro zúčastněné dívky.")
        autoSave()
    }

    private fun addPlayerXp(amount: Int) {
        val p = _gameState.value.player
        p.xp += amount
        while (p.xp >= p.xpNext) {
            p.xp -= p.xpNext
            p.level += 1
            p.xpNext = (p.xpNext * 1.65).toInt()
            p.maxHp += 12
            p.hp = p.maxHp
            p.skillPoints += 1
            p.maxSexEnergy = (p.maxSexEnergy + 5).coerceAtMost(250)
            p.maxDarkEnergy = (p.maxDarkEnergy + 3).coerceAtMost(200)
            p.sexEnergy = p.maxSexEnergy
            p.darkEnergy = p.maxDarkEnergy
            addLog("⭐ LEVEL UP! Dosáhl jsi úrovně ${p.level}! Obdržel jsi dovednostní bod a vyšší strop energie.")
        }
    }

    private fun addHaremExp(amount: Int) {
        updateState { current ->
            var hExp = current.haremExp + amount
            var hLvl = current.haremLevel
            var hMax = current.haremMaxExp
            while (hExp >= hMax) {
                hExp -= hMax
                hLvl += 1
                hMax = (hMax * 1.8).toInt()
                addLog("🏰 Harém postoupil na úroveň $hLvl! Pasivní příjem zvýšen.")
            }
            current.copy(haremLevel = hLvl, haremExp = hExp, haremMaxExp = hMax)
        }
    }

    // --- SAVE / LOAD SYSTEM ---
    fun saveToSlot(slot: Int): Boolean {
        it_slot_number = slot
        val state = _gameState.value
        val current = state.copy(
            slotNumber = slot,
            saveDate = "Den ${state.player.day} - ${state.characters.size} dívek"
        )
        _gameState.value = current
        saveStateAsync("save_slot_$slot", current)
        addLog("💾 Hra byla uložena do slotu $slot.")
        return true
    }

    suspend fun loadFromSlotSuspend(slot: Int): Boolean {
        val key = when(slot) { 0 -> "save_slot_autosave"; 99 -> "save_slot_quicksave"; else -> "save_slot_$slot" }
        val loaded = loadStateSuspend(key) ?: return false
        _gameState.value = loaded
        _currentTheme.value = loaded.currentTheme
        _isLightMode.value = loaded.isLightMode
        addLog("📂 Hra načtena ze slotu $slot.")
        return true
    }

    fun autoSave() {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 0,
            saveDate = "Den ${state.player.day} (Autosave)"
        )
        saveStateAsync("save_slot_autosave", current)
    }

    fun quickSave(): Pair<Boolean, String> {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 99,
            saveDate = "Den ${state.player.day} (Rychlé uložení)"
        )
        _gameState.value = current
        saveStateAsync("save_slot_quicksave", current)
        saveStateAsync("save_slot_autosave", current)
        val msg = "💾 Rychlé uložení: Stav pána (Den ${state.player.day}), statistiky ${state.characters.size} dívek i výbava úspěšně uloženy do DataStore."
        addLog(msg)
        return Pair(true, msg)
    }

    suspend fun quickSaveSuspend(): Pair<Boolean, String> {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 99,
            saveDate = "Den ${state.player.day} (Rychlé uložení)"
        )
        _gameState.value = current
        val keyQuick = stringPreferencesKey("save_slot_quicksave")
        val keyAuto = stringPreferencesKey("save_slot_autosave")
        val jsonStr = json.encodeToString(current)
        context.dataStore.edit { prefs ->
            prefs[keyQuick] = jsonStr
            prefs[keyAuto] = jsonStr
        }
        val msg = "💾 Rychlé uložení: Stav pána (Den ${state.player.day}), statistiky ${state.characters.size} dívek i výbava úspěšně uloženy do DataStore."
        addLog(msg)
        return Pair(true, msg)
    }

    suspend fun getSlotSummary(slot: Int): String {
        val key = when(slot) { 0 -> "save_slot_autosave"; 99 -> "save_slot_quicksave"; else -> "save_slot_$slot" }
        val save = loadStateSuspend(key) ?: return "Prázdný slot"
        return "Den ${save.player.day} | ${save.player.gold} zlata | Harém: ${save.characters.size} dívek"
    }
    fun runArenaExpedition(girlIds: List<String>): List<String> {
        val current = _gameState.value
        val player = current.player
        val logs = mutableListOf<String>()
        var prestigeGain = 0
        var foundGear: String? = null

        val girlsInTeam = current.characters.filter { girlIds.contains(it.id) }
        
        var teamHp = girlsInTeam.sumOf { it.hp }
        
        var baseTeamDmg = girlsInTeam.sumOf { (it.loajalita / 10) + (it.bloodlust / 5) + 5 + ((it.skills["combat"] ?: 0) * 5) }
        val damageBuffs = current.activeBuffs.filter { it.type == "DAMAGE" }.sumOf { it.value }
        if (damageBuffs > 0) {
            baseTeamDmg = (baseTeamDmg * (1.0f + (damageBuffs / 100f))).toInt()
        }
        
        var relationshipDmgMultiplier = 1.0f
        var relationshipDefMultiplier = 1.0f
        
        girlsInTeam.forEach { c ->
            val rel = c.getRelationship()
            if (rel == com.example.haremdark.models.RelStatus.BLOOD_SISTER) relationshipDmgMultiplier += rel.buffValue
            if (rel == com.example.haremdark.models.RelStatus.REBELLIOUS) relationshipDmgMultiplier += rel.buffValue
            if (rel == com.example.haremdark.models.RelStatus.BROKEN) relationshipDefMultiplier -= 0.10f
        }
        
        val teamDmg = (baseTeamDmg * relationshipDmgMultiplier).toInt()
        var baseDefense = girlsInTeam.sumOf { (it.poslusnost / 15) + 2 + ((it.skills["defense"] ?: 0) * 2) }
        val defenseBuffs = current.activeBuffs.filter { it.type == "DEFENSE" }.sumOf { it.value }
        if (defenseBuffs > 0) {
            baseDefense += defenseBuffs
        }
        baseDefense = (baseDefense * relationshipDefMultiplier).toInt()
        val teamDefense = baseDefense

        logs.add("⚔️ Aréna začíná! Tým dívek vstupuje na písek arény.")
        logs.add("❤️ Počáteční zdraví týmu: $teamHp | ⚔️ Síla týmu: $teamDmg")

        var currentWave = 1
        var eHp = 0
        var enemyDmg = 0
        var enemyName = ""

        val difficultyScale = 1.0f + (player.level * 0.15f) + (current.haremLevel * 0.1f) + (current.characters.size * 0.05f)
        
        val enemyTypes = listOf(
            Triple("Goblini otrokáři", 20, 8),
            Triple("Žoldnéři Cechu", 35, 12),
            Triple("Zbloudilá Inkvizice", 50, 18),
            Triple("Krvaví kultisté", 70, 14),
            Triple("Divocí vlkodlaci", 80, 22),
            Triple("Stínoví démoni", 100, 25)
        )
        val bossTypes = listOf(
            Triple("Velitel Inkvizice (Boss)", 250, 40),
            Triple("Golemský Ničitel (Boss)", 350, 25),
            Triple("Prastarý Upír (Boss)", 200, 50)
        )

        while (teamHp > 0 && currentWave <= 10) {
            val isBoss = (currentWave % 5 == 0) // Boss on wave 5 and 10
            
            val baseEnemy = if (isBoss) bossTypes.random() else enemyTypes.random()
            enemyName = baseEnemy.first
            
            // Procedural scaling: scales by difficulty factor + wave number
            eHp = ((baseEnemy.second + (currentWave * 20)) * difficultyScale).toInt()
            enemyDmg = ((baseEnemy.third + (currentWave * 4)) * difficultyScale).toInt()
            
            logs.add("--- Vlna $currentWave: $enemyName ---")
            logs.add("⚔️ Nepřítel: Zdraví $eHp, Útok $enemyDmg")

            while (eHp > 0 && teamHp > 0) {
                // Team attacks
                val dmgDealt = (teamDmg + (0..8).random()).coerceAtLeast(1)
                eHp -= dmgDealt
                
                // Enemy attacks
                if (eHp > 0) {
                    val dmgTaken = (enemyDmg - teamDefense + (0..5).random()).coerceAtLeast(1)
                    teamHp -= dmgTaken
                }
            }

            if (teamHp > 0) {
                logs.add("✅ Vlna $currentWave poražena! (Zbývá HP týmu: $teamHp)")
                val gained = currentWave * 3
                prestigeGain += gained
                logs.add("🏆 Získáno +$gained prestiže.")

                // Rare gear drop
                if (foundGear == null && (0..100).random() < (currentWave * 4)) {
                    val newWeapons = listOf("Krvavá kosa", "Stínová dýka smrti", "Plamenný bič", "Mithrilový meč")
                    foundGear = newWeapons.random()
                    logs.add("🎁 VZÁCNÝ DROP: Získána zbraň '$foundGear'!")
                }
                currentWave++
            }
        }

        if (teamHp > 0) {
            logs.add("👑 Tým úspěšně přežil všech 10 vln arény! Publikum šílí.")
        } else {
            logs.add("☠️ Tvůj tým padl ve vlně $currentWave. Dívky musely být odtaženy zpět do komnat.")
        }

        logs.add("===========================")
        logs.add("CELKOVÝ VÝSLEDEK EXPEDICE:")
        logs.add("🏆 Celkem prestiže: +$prestigeGain")
        
        // Apply damage to individual girls
        val damagePercentage = if (teamHp <= 0) 1.0f else 1.0f - (teamHp.toFloat() / girlsInTeam.sumOf { it.maxHp })
        
        val xpGain = (currentWave - 1) * 20 + 10
        logs.add("🌟 Každá přeživší dívka v týmu získala +$xpGain ZK!")
        
        updateState { state ->
            val updatedGirls = state.characters.map { girl ->
                if (girlIds.contains(girl.id)) {
                    val individualDmg = (girl.maxHp * damagePercentage).toInt()
                    var newXp = girl.xp + xpGain
                    var newLevel = girl.level
                    var newSp = girl.skillPoints
                    
                    while (newXp >= newLevel * 100) {
                        newXp -= newLevel * 100
                        newLevel++
                        newSp++
                        logs.add("✨ ${girl.name} dosáhla úrovně $newLevel a získala 1 Dovednostní bod!")
                    }
                    
                    girl.copy(
                        hp = (girl.hp - individualDmg).coerceAtLeast(1),
                        xp = newXp,
                        level = newLevel,
                        skillPoints = newSp
                    )
                } else girl
            }
            
            val newPlayer = state.player.copy(prestige = state.player.prestige + prestigeGain)
            if (foundGear != null) {
                newPlayer.weapons.add(com.example.haremdark.models.Weapon(foundGear!!, "kratka", 25 + currentWave, 250, 1.0f, "Vzácná zbraň z Arény", 5))
            }
            
            state.copy(
                player = newPlayer,
                characters = updatedGirls
            )
        }
        autoSave()
        return logs
    }

    fun upgradeCharacterSkill(characterId: String, skillName: String): Pair<Boolean, String> {
        var result = Pair(false, "Chyba při vylepšení dovednosti.")
        updateState { current ->
            val charIndex = current.characters.indexOfFirst { it.id == characterId }
            if (charIndex != -1) {
                val char = current.characters[charIndex]
                if (char.skillPoints > 0) {
                    val updatedSkills = char.skills.toMutableMap()
                    val currentVal = updatedSkills[skillName] ?: 0
                    updatedSkills[skillName] = currentVal + 1
                    
                    val updatedChar = char.copy(
                        skillPoints = char.skillPoints - 1,
                        skills = updatedSkills
                    )
                    
                    val newList = current.characters.toMutableList()
                    newList[charIndex] = updatedChar
                    
                    result = Pair(true, "Dovednost vylepšena!")
                    current.copy(characters = newList)
                } else {
                    result = Pair(false, "Nedostatek dovednostních bodů.")
                    current
                }
            } else {
                current
            }
        }
        if (result.first) autoSave()
        return result
    }

    fun checkAchievements(): List<String> {
        val current = _gameState.value
        val player = current.player
        val newUnlocks = mutableListOf<String>()
        val currentUnlocks = player.unlockedAchievements.toMutableList()

        val allAchs = com.example.haremdark.models.AchievementList.allAchievements
        
        fun award(id: String) {
            if (!currentUnlocks.contains(id)) {
                currentUnlocks.add(id)
                newUnlocks.add(id)
            }
        }

        // Conditions
        if (player.battlesWon >= 100) award("ach_battles_100")
        if (current.characters.any { it.affinityPoints >= 100 }) award("ach_max_affinity")
        if (current.characters.size >= 10) award("ach_harem_10")
        if (current.characters.size >= 20) award("ach_harem_20")
        
        val totalAffinity = current.characters.sumOf { it.affinityPoints }
        if (totalAffinity >= 250) award("ach_affinity_total")
        
        if (current.defeatedBosses.size >= 3) award("ach_boss_slayer")
        
        if (current.characters.any { it.level >= 10 }) award("ach_arena_champion")
        
        if (player.gold >= 10000) award("ach_wealthy")
        
        val fortressLevel = current.buildings.firstOrNull { it.type == "pevnost" }?.level ?: 1
        if (fortressLevel >= 5) award("ach_domain_max")
        
        if (current.characters.any { it.getRelationship() == com.example.haremdark.models.RelStatus.BLOOD_SISTER }) award("ach_blood_sister")
        
        if (newUnlocks.isNotEmpty()) {
            val updatedPlayer = player.copy(unlockedAchievements = currentUnlocks)
            updateState { it.copy(player = updatedPlayer) }
            autoSave()
        }
        
        return newUnlocks
    }

    fun setActiveTitle(titleId: String?): Boolean {
        var success = false
        updateState { state ->
            if (titleId == null || state.player.unlockedAchievements.contains(titleId)) {
                success = true
                state.copy(player = state.player.copy(activeTitle = titleId))
            } else {
                state
            }
        }
        if (success) autoSave()
        return success
    }


    fun recruitCharacter(type: String): Pair<Boolean, String> {
        var result = Pair(false, "Neznámý typ náboru.")
        updateState { current ->
            val p = current.player
            
            // Define cost based on type
            val costGold: Int
            val costMana: Int
            val minRarity: Int
            val title: String
            
            when (type) {
                "basic" -> { costGold = 250; costMana = 0; minRarity = 1; title = "Běžný otrok" }
                "advanced" -> { costGold = 600; costMana = 20; minRarity = 2; title = "Vzácný zajatec" }
                "elite" -> { costGold = 1500; costMana = 50; minRarity = 3; title = "Exkluzivní trofej" }
                else -> return@updateState current
            }
            
            if (p.gold < costGold || p.mana < costMana) {
                result = Pair(false, "Nedostatek surovin (Potřebuješ $costGold Zlata a $costMana Many).")
                return@updateState current
            }
            
            if (current.characters.size >= p.maxPopulation) {
                result = Pair(false, "Tvůj harém je plný! (Kapacita: ${p.maxPopulation})")
                return@updateState current
            }
            
            // Generate char
            val names = listOf("Lumia", "Sera", "Thalia", "Vex", "Kaelia", "Rina", "Myra", "Nyx", "Elaria", "Zora", "Lyra", "Tess", "Aria", "Morgana", "Lilith", "Carmilla", "Isolde", "Ophelia")
            val randomName = names.random()
            val archetypes = com.example.haremdark.data.StaticData.ARCHETYPES.keys.toList()
            val chosenArchetype = archetypes.random()
            val age = (18..26).random()
            
            // Stats based on type
            val statBoost = minRarity * 15
            
            val newGirl = com.example.haremdark.models.Character(
                id = "c_${java.util.UUID.randomUUID().toString().take(8)}",
                name = randomName,
                age = age,
                archetypeId = chosenArchetype,
                rarity = minRarity,
                hp = 100 + (minRarity * 20),
                maxHp = 100 + (minRarity * 20),
                srdce = 50 + (0..statBoost).random(),
                poslusnost = 20 + (0..statBoost).random(),
                vlhkost = 40 + (0..statBoost).random(),
                submisivita = 30 + (0..statBoost).random(),
                loajalita = 20 + (0..statBoost).random(),
                touha = 40 + (0..statBoost).random(),
                level = minRarity,
                xp = 0,
                skillPoints = minRarity - 1,
                skills = mutableMapOf("combat" to (0..minRarity).random(), "defense" to (0..minRarity).random(), "production" to (0..minRarity).random(), "rental" to (0..minRarity).random())
            )
            
            val newPlayer = p.copy(
                gold = p.gold - costGold,
                mana = p.mana - costMana
            )
            
            val newList = current.characters.toMutableList()
            newList.add(newGirl)
            
            result = Pair(true, "Nábor úspěšný! Získal jsi novou dívku: $randomName.")
            
            current.copy(
                player = newPlayer,
                characters = newList,
                gameLog = current.gameLog + "⛓️ Úspěšný nábor ($title): $randomName se přidává do harému!"
            )
        }
        if (result.first) autoSave()
        return result
    }


    fun togglePin(characterId: String): Pair<Boolean, String> {
        var msg = ""
        var success = false
        updateState { current ->
            val updated = current.characters.map { c ->
                if (c.id == characterId) {
                    val pinned = !c.isPinned
                    msg = if (pinned) "${c.name} byla připnuta na vrch seznamu." else "${c.name} již není připnutá."
                    success = true
                    c.copy(isPinned = pinned)
                } else c
            }
            current.copy(characters = updated)
        }
        if (success) autoSave()
        return Pair(success, msg)
    }


    fun equipItemToCharacter(characterId: String, itemId: String, slotId: String) {
        updateState { current ->
            val player = current.player
            val itemIndex = player.items.indexOfFirst { it.id == itemId && it.count > 0 }
            if (itemIndex == -1) return@updateState current
            
            val itemToEquip = player.items[itemIndex]
            
            val updatedCharacters = current.characters.map { char ->
                if (char.id == characterId) {
                    val currentEquipped = char.equipment[slotId]
                    
                    // Put old item back in inventory if exists
                    val newItems = player.items.toMutableList()
                    if (currentEquipped != null) {
                        val existingItemIdx = newItems.indexOfFirst { it.id == currentEquipped.id }
                        if (existingItemIdx != -1) {
                            val ei = newItems[existingItemIdx]
                            newItems[existingItemIdx] = ei.copy(count = ei.count + 1)
                        } else {
                            newItems.add(currentEquipped.copy(count = 1))
                        }
                    }
                    
                    // Remove 1 from inventory for the new item
                    val newEquipIdx = newItems.indexOfFirst { it.id == itemId }
                    val ne = newItems[newEquipIdx]
                    if (ne.count > 1) {
                        newItems[newEquipIdx] = ne.copy(count = ne.count - 1)
                    } else {
                        newItems.removeAt(newEquipIdx)
                    }
                    
                    // Equip
                    val newEquipMap = char.equipment.toMutableMap()
                    newEquipMap[slotId] = itemToEquip.copy(count = 1)
                    
                    current.player.items = newItems
                    
                    char.copy(equipment = newEquipMap)
                } else char
            }
            current.copy(characters = updatedCharacters)
        }
        autoSave()
    }
    
    fun unequipItemFromCharacter(characterId: String, slotId: String) {
        updateState { current ->
            val player = current.player
            val updatedCharacters = current.characters.map { char ->
                if (char.id == characterId) {
                    val currentEquipped = char.equipment[slotId]
                    if (currentEquipped != null) {
                        val newItems = player.items.toMutableList()
                        val existingItemIdx = newItems.indexOfFirst { it.id == currentEquipped.id }
                        if (existingItemIdx != -1) {
                            val ei = newItems[existingItemIdx]
                            newItems[existingItemIdx] = ei.copy(count = ei.count + 1)
                        } else {
                            newItems.add(currentEquipped.copy(count = 1))
                        }
                        
                        val newEquipMap = char.equipment.toMutableMap()
                        newEquipMap[slotId] = null
                        
                        current.player.items = newItems
                        char.copy(equipment = newEquipMap)
                    } else {
                        char
                    }
                } else char
            }
            current.copy(characters = updatedCharacters)
        }
        autoSave()
    }

    // --- EQUIPMENT LOADOUT SYSTEM ---
    val defaultCombatLoadouts: List<EquipmentLoadout> = listOf(
        EquipmentLoadout(
            id = "preset_dps",
            name = "Agresivní útok",
            icon = "⚔️",
            description = "Maximalizuje útočné poškození a kritické zásahy pro rychlé zničení tuhých bossů.",
            situationTag = "DPS",
            weaponItem = InventoryItem("krvavy_mec_loadout", "Krvavý meč zkázy", "Masivní zvýšení útoku.", 1, 150, "equipment", "🗡️", "Epický", "+18 Boj, +8% Šance na kritický zásah", "weapon", 18, 0, 0),
            armorItem = InventoryItem("lehka_kuze_loadout", "Bitevní kožená zbroj", "Zvyšuje rychlost a obratnost.", 1, 100, "equipment", "🥋", "Vzácný", "+6 Boj, +4 Obrana", "armor", 6, 4, 10),
            accessoryItem = InventoryItem("prsten_hnevu_loadout", "Prsten krvavého hněvu", "Posiluje každý zásah smrtící zuřivostí.", 1, 120, "equipment", "💍", "Epický", "+10 Boj, +12% Průraznost", "accessory", 10, 0, 0),
            playerWeaponIndex = 0,
            isDefaultPreset = true
        ),
        EquipmentLoadout(
            id = "preset_tank",
            name = "Obranný štít",
            icon = "🛡️",
            description = "Zaměřeno na absorpci drtivých úderů, vysokou obranu a velké množství životů.",
            situationTag = "TANK",
            weaponItem = InventoryItem("tezky_palcat_loadout", "Těžký rytířský palcát", "Pevná zbraň schopná omráčit nepřítele.", 1, 120, "equipment", "🔨", "Vzácný", "+10 Boj, +3 Obrana", "weapon", 10, 3, 0),
            armorItem = InventoryItem("stribrna_zbroj_loadout", "Stříbrná plátová zbroj", "Masivní pláty chránící před smrtícími údery.", 1, 180, "equipment", "🛡️", "Epický", "+16 Obrana, +40 HP", "armor", 0, 16, 40),
            accessoryItem = InventoryItem("stozar_strazce_loadout", "Amulet železného strážce", "Snižuje veškeré příchozí poškození o 15%.", 1, 140, "equipment", "📿", "Epický", "+8 Obrana, +25 HP", "accessory", 0, 8, 25),
            playerWeaponIndex = 0,
            isDefaultPreset = true
        ),
        EquipmentLoadout(
            id = "preset_magic",
            name = "Temná magie",
            icon = "🔮",
            description = "Posiluje temnou energii, vysávání životů (lifesteal) a sílu stínových kouzel.",
            situationTag = "DARK_MAGIC",
            weaponItem = InventoryItem("stinova_hul_loadout", "Hůl nočních stínů", "Koncentruje temnou energii do drtivých výbojů.", 1, 160, "equipment", "🪄", "Epický", "+14 Boj, +25 Temná energie", "weapon", 14, 2, 0),
            armorItem = InventoryItem("temna_roba_loadout", "Róba stínového kněze", "Chrání před kouzly a urychluje doplňování many.", 1, 130, "equipment", "👘", "Vzácný", "+6 Boj, +8 Obrana, +20 HP", "armor", 6, 8, 20),
            accessoryItem = InventoryItem("oko_temnoty_loadout", "Oko prázdnoty", "Převádí způsobené zranění zpět do léčení.", 1, 150, "equipment", "🧿", "Legendární", "+8 Boj, +5 Obrana, +15 HP", "accessory", 8, 5, 15),
            playerWeaponIndex = 1,
            isDefaultPreset = true
        ),
        EquipmentLoadout(
            id = "preset_bleed",
            name = "Krvácivá dýka",
            icon = "🩸",
            description = "Rychlé zásahy způsobující nepříteli těžké krvácení a postupné oslabování.",
            situationTag = "BLEED",
            weaponItem = InventoryItem("otravena_dyka_loadout", "Otrávená dýka stínů", "Čepel napuštěná jedem působícím poškození každé kolo.", 1, 140, "equipment", "🗡️", "Epický", "+15 Boj, Krvácení (3 kola)", "weapon", 15, 0, 0),
            armorItem = InventoryItem("kape_stinu_loadout", "Kápě tichého asasína", "Umožňuje vyhnout se nejtěžším úderům.", 1, 110, "equipment", "🥷", "Vzácný", "+5 Boj, +7 Obrana", "armor", 5, 7, 0),
            accessoryItem = InventoryItem("zub_bestie_loadout", "Zub noční bestie", "Zvyšuje šanci na krvácivý zásah o 25%.", 1, 95, "equipment", "🦷", "Vzácný", "+8 Boj, +10 HP", "accessory", 8, 2, 10),
            playerWeaponIndex = 0,
            isDefaultPreset = true
        ),
        EquipmentLoadout(
            id = "preset_balanced",
            name = "Vyvážený šermíř",
            icon = "⚖️",
            description = "Univerzální kombinace útoku, obrany a výdrže vhodná pro všechny běžné expedice.",
            situationTag = "BALANCED",
            weaponItem = InventoryItem("vyvazeny_mec_loadout", "Ocelový meč velitele", "Dobře vyvážená čepel pro útok i krytí.", 1, 110, "equipment", "⚔️", "Vzácný", "+11 Boj, +4 Obrana", "weapon", 11, 4, 0),
            armorItem = InventoryItem("krouzkova_kosile_loadout", "Vyztužená kroužková košile", "Spolehlivá ochrana celého trupu.", 1, 100, "equipment", "🛡️", "Běžný", "+8 Obrana, +20 HP", "armor", 0, 8, 20),
            accessoryItem = InventoryItem("amulet_odvahy_loadout", "Amulet odvahy a cti", "Dodává klid a rozvahu v boji.", 1, 90, "equipment", "🏅", "Běžný", "+4 Boj, +4 Obrana, +10 HP", "accessory", 4, 4, 10),
            playerWeaponIndex = 0,
            isDefaultPreset = true
        )
    )

    fun getAllLoadouts(): List<EquipmentLoadout> {
        val userSaved = _gameState.value.savedLoadouts
        return defaultCombatLoadouts + userSaved
    }

    fun saveCurrentLoadout(name: String, situationTag: String, icon: String, characterId: String?): EquipmentLoadout {
        val current = _gameState.value
        val char = if (characterId != null) {
            current.characters.firstOrNull { it.id == characterId }
        } else {
            current.characters.firstOrNull()
        }

        val weapon = char?.equipment?.get("weapon")
        val armor = char?.equipment?.get("armor")
        val accessory = char?.equipment?.get("accessory")

        val newLoadout = EquipmentLoadout(
            id = "custom_loadout_${System.currentTimeMillis()}",
            name = name.ifBlank { "Vlastní set" },
            icon = icon.ifBlank { "⚔️" },
            description = "Uložený set výbavy (${if (char != null) char.name else "Pán"})",
            situationTag = situationTag,
            weaponItem = weapon,
            armorItem = armor,
            accessoryItem = accessory,
            playerWeaponIndex = current.player.equippedWeaponIndex,
            targetCharacterId = characterId
        )

        updateState { state ->
            val updatedLoadouts = state.savedLoadouts.filter { it.id != newLoadout.id } + newLoadout
            state.copy(
                savedLoadouts = updatedLoadouts,
                activeLoadoutId = newLoadout.id
            )
        }
        autoSave()
        return newLoadout
    }

    fun applyLoadout(loadoutId: String, characterId: String?): Pair<Boolean, String> {
        val all = getAllLoadouts()
        val loadout = all.firstOrNull { it.id == loadoutId }
            ?: return Pair(false, "Loadout nebyl nalezen.")

        var targetName = "Tým"
        updateState { current ->
            val targetCharId = characterId ?: loadout.targetCharacterId ?: current.characters.firstOrNull()?.id
            val updatedCharacters = current.characters.map { char ->
                if (char.id == targetCharId) {
                    targetName = char.name
                    val newEquipMap = char.equipment.toMutableMap()
                    if (loadout.weaponItem != null) newEquipMap["weapon"] = loadout.weaponItem
                    if (loadout.armorItem != null) newEquipMap["armor"] = loadout.armorItem
                    if (loadout.accessoryItem != null) newEquipMap["accessory"] = loadout.accessoryItem
                    char.copy(equipment = newEquipMap)
                } else char
            }

            val newPlayer = current.player.copy(
                equippedWeaponIndex = loadout.playerWeaponIndex.coerceIn(0, (current.player.weapons.size - 1).coerceAtLeast(0))
            )

            current.copy(
                player = newPlayer,
                characters = updatedCharacters,
                activeLoadoutId = loadout.id
            )
        }
        autoSave()
        return Pair(true, "Aktivován loadout '${loadout.name}' (${loadout.situationTag}) pro $targetName!")
    }

    fun deleteCustomLoadout(loadoutId: String): Boolean {
        var removed = false
        updateState { current ->
            val filtered = current.savedLoadouts.filter { it.id != loadoutId }
            if (filtered.size != current.savedLoadouts.size) {
                removed = true
                val newActive = if (current.activeLoadoutId == loadoutId) null else current.activeLoadoutId
                current.copy(savedLoadouts = filtered, activeLoadoutId = newActive)
            } else current
        }
        if (removed) autoSave()
        return removed
    }

    fun awardPartyCombatVictoryWithDetails(rewards: com.example.haremdark.models.PartyCombatRewards, session: com.example.haremdark.models.PartyCombatSession): List<String> {
        val logs = mutableListOf<String>()
        logs.add("🏆 [${rewards.rank}] ${rewards.rankTitle}!")
        logs.add("💰 Získáno: +${rewards.gold} Zlata, +${rewards.bloodRubies} Rubínů, +${rewards.prestigeGain} Prestiže.")

        updateState { current ->
            val player = current.player
            val newItems = player.items.toMutableList()

            // Add dropped items
            rewards.itemDropDetails.forEach { drop ->
                val existing = newItems.indexOfFirst { it.id == drop.id }
                if (existing != -1) {
                    val item = newItems[existing]
                    newItems[existing] = item.copy(count = item.count + drop.count)
                } else {
                    newItems.add(drop)
                }
                logs.add("🎁 Získán předmět: ${drop.icon} ${drop.name} (${drop.rarity})")
            }

            // Update characters with XP, levels, SP, loyalty and affinity
            val participatingIds = session.party.filter { !it.isPlayer }.map { it.id }.toSet()
            val updatedCharacters = current.characters.map { girl ->
                if (participatingIds.contains(girl.id)) {
                    val xpGained = rewards.characterXpGains[girl.id] ?: (rewards.playerXp / 2)
                    var newXp = girl.xp + xpGained
                    var newLevel = girl.level
                    var newSp = girl.skillPoints
                    val isMvp = (girl.id == rewards.mvpCharacterId)

                    while (newXp >= newLevel * 100) {
                        newXp -= newLevel * 100
                        newLevel++
                        newSp += 1
                        logs.add("✨ ${girl.name} postoupila na ÚROVEŇ $newLevel a získala +1 Dovednostní bod!")
                    }

                    val bonusAffinity = rewards.haremAffinityGain + (if (isMvp) 15 else 0)
                    val bonusLoyalty = rewards.haremLoyaltyGain + (if (isMvp) 5 else 0)

                    girl.copy(
                        xp = newXp,
                        level = newLevel,
                        skillPoints = newSp,
                        affinityPoints = girl.affinityPoints + bonusAffinity,
                        loajalita = (girl.loajalita + bonusLoyalty).coerceIn(0, 100)
                    )
                } else girl
            }

            val updatedPlayer = player.copy(
                gold = player.gold + rewards.gold,
                mana = (player.mana + rewards.bloodRubies).coerceAtLeast(0),
                prestige = player.prestige + rewards.prestigeGain,
                xp = player.xp + rewards.playerXp,
                battlesWon = player.battlesWon + 1,
                items = newItems
            )

            current.copy(
                player = updatedPlayer,
                characters = updatedCharacters,
                gameLog = current.gameLog + "⚔️ Vítězství v aréně [${rewards.rank}]: +${rewards.gold} Zlata, MVP: ${rewards.mvpName}"
            )
        }
        autoSave()
        return logs
    }

    fun unlockCharacterSkillWithXp(characterId: String, nodeId: String): Pair<Boolean, String> {
        var result = Pair(false, "Chyba při odemykání schopnosti.")
        val node = com.example.haremdark.data.CharacterSkillCatalog.ALL_SKILL_NODES.firstOrNull { it.id == nodeId }
            ?: return Pair(false, "Schopnost nebyla nalezena v katalogu.")

        updateState { current ->
            val charIndex = current.characters.indexOfFirst { it.id == characterId }
            if (charIndex == -1) return@updateState current

            val char = current.characters[charIndex]
            if (char.unlockedPassives.contains(nodeId) || char.unlockedCombatSkills.contains(nodeId)) {
                result = Pair(false, "Tato schopnost již byla odemčena.")
                return@updateState current
            }

            if (char.level < node.reqLevel) {
                result = Pair(false, "Vyžadována úroveň ${node.reqLevel} (Aktuální: ${char.level}).")
                return@updateState current
            }

            if (node.reqNodeId != null) {
                val hasReq = char.unlockedPassives.contains(node.reqNodeId) || char.unlockedCombatSkills.contains(node.reqNodeId)
                if (!hasReq) {
                    result = Pair(false, "Nejprve musíš odemknout předchozí dovednost ve stromu.")
                    return@updateState current
                }
            }

            if (char.xp < node.xpCost) {
                result = Pair(false, "Nedostatek ZK! Potřebuješ ${node.xpCost} ZK (Máš ${char.xp} ZK).")
                return@updateState current
            }

            val updatedPassives = char.unlockedPassives.toMutableList()
            val updatedCombatSkills = char.unlockedCombatSkills.toMutableList()

            if (node.nodeType == com.example.haremdark.data.SkillNodeType.ACTIVE_ABILITY) {
                updatedCombatSkills.add(nodeId)
            } else {
                updatedPassives.add(nodeId)
            }

            val updatedChar = char.copy(
                xp = char.xp - node.xpCost,
                unlockedPassives = updatedPassives,
                unlockedCombatSkills = updatedCombatSkills
            )

            val newList = current.characters.toMutableList()
            newList[charIndex] = updatedChar
            result = Pair(true, "Úspěšně odemčena schopnost '${node.name}'!")

            current.copy(
                characters = newList,
                gameLog = current.gameLog + "✨ ${char.name} odemkla schopnost: ${node.name} (${node.nodeType.label})"
            )
        }
        if (result.first) autoSave()
        return result
    }

    fun unlockCharacterSkillWithSp(characterId: String, nodeId: String): Pair<Boolean, String> {
        var result = Pair(false, "Chyba při odemykání schopnosti.")
        val node = com.example.haremdark.data.CharacterSkillCatalog.ALL_SKILL_NODES.firstOrNull { it.id == nodeId }
            ?: return Pair(false, "Schopnost nebyla nalezena v katalogu.")

        updateState { current ->
            val charIndex = current.characters.indexOfFirst { it.id == characterId }
            if (charIndex == -1) return@updateState current

            val char = current.characters[charIndex]
            if (char.unlockedPassives.contains(nodeId) || char.unlockedCombatSkills.contains(nodeId)) {
                result = Pair(false, "Tato schopnost již byla odemčena.")
                return@updateState current
            }

            if (char.level < node.reqLevel) {
                result = Pair(false, "Vyžadována úroveň ${node.reqLevel} (Aktuální: ${char.level}).")
                return@updateState current
            }

            if (node.reqNodeId != null) {
                val hasReq = char.unlockedPassives.contains(node.reqNodeId) || char.unlockedCombatSkills.contains(node.reqNodeId)
                if (!hasReq) {
                    result = Pair(false, "Nejprve musíš odemknout předchozí dovednost ve stromu.")
                    return@updateState current
                }
            }

            if (char.skillPoints < node.spCost) {
                result = Pair(false, "Nedostatek dovednostních bodů! Potřebuješ ${node.spCost} bodů (Máš ${char.skillPoints}).")
                return@updateState current
            }

            val updatedPassives = char.unlockedPassives.toMutableList()
            val updatedCombatSkills = char.unlockedCombatSkills.toMutableList()

            if (node.nodeType == com.example.haremdark.data.SkillNodeType.ACTIVE_ABILITY) {
                updatedCombatSkills.add(nodeId)
            } else {
                updatedPassives.add(nodeId)
            }

            val updatedChar = char.copy(
                skillPoints = char.skillPoints - node.spCost,
                unlockedPassives = updatedPassives,
                unlockedCombatSkills = updatedCombatSkills
            )

            val newList = current.characters.toMutableList()
            newList[charIndex] = updatedChar
            result = Pair(true, "Úspěšně odemčena schopnost '${node.name}' pomocí dovednostního bodu!")

            current.copy(
                characters = newList,
                gameLog = current.gameLog + "✨ ${char.name} odemkla schopnost: ${node.name} (${node.nodeType.label})"
            )
        }
        if (result.first) autoSave()
        return result
    }

    fun convertCharacterXpToSp(characterId: String): Pair<Boolean, String> {
        var result = Pair(false, "Chyba při konverzi ZK.")
        val costXp = 100
        updateState { current ->
            val charIndex = current.characters.indexOfFirst { it.id == characterId }
            if (charIndex == -1) return@updateState current

            val char = current.characters[charIndex]
            if (char.xp < costXp) {
                result = Pair(false, "Nedostatek ZK! Potřebuješ $costXp ZK pro získání 1 Dovednostního bodu (Máš ${char.xp} ZK).")
                return@updateState current
            }

            val updatedChar = char.copy(
                xp = char.xp - costXp,
                skillPoints = char.skillPoints + 1
            )

            val newList = current.characters.toMutableList()
            newList[charIndex] = updatedChar
            result = Pair(true, "${char.name} vyměnila $costXp ZK za +1 Dovednostní bod!")
            current.copy(characters = newList)
        }
        if (result.first) autoSave()
        return result
    }

}
