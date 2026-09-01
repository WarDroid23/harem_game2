package com.example.haremdark.domain

import android.content.Context
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import kotlin.random.Random

class GameEngine(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _gameState = MutableStateFlow(loadInitialState())
    val gameState: StateFlow<GameSave> = _gameState.asStateFlow()

    private val _combatState = MutableStateFlow<CombatSession?>(null)
    val combatState: StateFlow<CombatSession?> = _combatState.asStateFlow()

    private val _currentTheme = MutableStateFlow("Temné dominium")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    init {
        _currentTheme.value = _gameState.value.currentTheme
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

    private fun loadInitialState(): GameSave {
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        val savedJson = prefs.getString("save_slot_autosave", null)
            ?: prefs.getString("save_slot_1", null)
        if (savedJson != null) {
            try {
                return json.decodeFromString<GameSave>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return GameContent.createInitialSave()
    }

    fun updateState(transform: (GameSave) -> GameSave) {
        val current = _gameState.value
        val transformed = transform(current)
        // Ensure Player and collections have distinct references so StateFlow and Compose always re-render in real-time
        val p = transformed.player
        val finalState = transformed.copy(
            player = p.copy(
                skills = p.skills.toMutableMap(),
                weapons = p.weapons.map { it.copy() }.toMutableList(),
                items = p.items.map { it.copy() }.toMutableList(),
                agents = p.agents.map { it.copy() }.toMutableList()
            ),
            concubines = transformed.concubines.map { it.copy() },
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
        concubineId: String,
        giftName: String,
        goldCost: Int,
        loyaltyBoost: Int,
        desireBoost: Int,
        obedienceBoost: Int,
        trustBoost: Int,
        flavorText: String
    ): Pair<Boolean, String> {
        val current = _gameState.value
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nebyla nalezena.")

        if (current.player.gold < goldCost) {
            return Pair(false, "Nemáš dostatek zlata ($goldCost zl.)!")
        }

        val affinityGain = (loyaltyBoost + trustBoost) / 2 + 12
        val successMsg = "🎁 Předal jsi dar '$giftName' dívce ${concubine.name}. $flavorText"
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - goldCost).coerceAtLeast(0))
            val updatedConcubines = state.concubines.map { c ->
                if (c.id == concubineId) {
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
            state.copy(player = p, concubines = updatedConcubines, gameLog = logs)
        }
        addHaremExp(10)
        return Pair(true, successMsg)
    }

    fun giveInventoryGift(concubineId: String, itemId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count > 0 }
            ?: return Pair(false, "Předmět není v tvém inventáři.")
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
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

            val updatedConcubines = state.concubines.map { c ->
                if (c.id == concubineId) {
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
            state.copy(player = p, concubines = updatedConcubines, gameLog = logs)
        }
        addHaremExp(8)
        return Pair(true, resultMsg)
    }

    fun setTheme(themeName: String) {
        _currentTheme.value = themeName
        updateState { it.copy(currentTheme = themeName) }
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
            current.concubines.forEach { c ->
                if (c.affinityLevel >= 2) level2Count++
                if (c.affinityLevel >= 4) level4Count++
                if (c.affinityLevel >= 6) level6Count++
            }

            // Passive income from mafia and buildings
            val buildingIncome = current.buildings.sumOf { it.level * 15 }
            val territoryIncome = current.territories.filter { it.level > 0 }.sumOf { it.baseIncome * it.level }
            
            val haremIncomeBase = current.haremLevel * 10
            val haremIncomeBonus = (haremIncomeBase * (0.10f * level2Count)).toInt()
            
            val basePassive = buildingIncome + territoryIncome + haremIncomeBase + haremIncomeBonus
            val globalIncomeMultiplier = 1.0f + (0.50f * level6Count)
            val totalPassive = (basePassive * globalIncomeMultiplier).toInt()

            // Process slave rentals
            var rentalIncome = 0
            val updatedConcubines = current.concubines.map { c ->
                val copy = c.copy()
                if (copy.naNajmu) {
                    val dailyIncome = when (copy.klient) {
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
                    copy.hp = (copy.hp + 10 + bathLevel * 5).coerceAtMost(copy.maxHp)
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
            val wives = updatedConcubines.filter { it.jeManzelkou }
            val favorites = updatedConcubines.filter { it.oblibena }

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

            p.gold += totalPassive + rentalIncome
            p.day = newDay
            p.maxSexEnergy = newMaxSex
            p.maxDarkEnergy = newMaxDark
            p.sexEnergy = newMaxSex
            p.darkEnergy = newMaxDark
            p.hp = p.maxHp

            // Random Jealousy / Night incident check if favorite exists
            if (favorites.isNotEmpty() && updatedConcubines.size > 1 && Random.nextFloat() < 0.35f) {
                val fav = favorites.first()
                val other = updatedConcubines.filter { !it.oblibena }.randomOrNull()
                if (other != null) {
                    other.strach = (other.strach + 4).coerceAtMost(100)
                    other.humiliation = (other.humiliation + 3).coerceAtMost(100)
                    addLog("★ Noční incident: Ostatní dívky žárlí na oblíbenkyni ${fav.name}. ${other.name} cítí tlak v harému.")
                }
            }

            val logEntry = "🌅 Den $newDay svítá. Energie plně obnovena (${p.sexEnergy}/${p.darkEnergy}). Příjem: +${totalPassive + rentalIncome} zlatých."
            val logs = (listOf(logEntry) + current.gameLog).take(30)
            
            val newMissions = listOf(
                DailyMission(id = "m1_$newDay", type = "INTERACT", description = "Provést interakce s dívkami", targetCount = 3, rewardGold = 50, rewardSexEnergy = 20),
                DailyMission(id = "m2_$newDay", type = "EXPLORE", description = "Vyrazit na výpravu", targetCount = 1, rewardGold = 100, rewardDarkEnergy = 15),
                DailyMission(id = "m3_$newDay", type = "GIFT", description = "Darovat předmět z brašny", targetCount = 1, rewardGold = 75)
            )

            current.copy(
                player = p,
                concubines = updatedConcubines,
                gameLog = logs,
                dailyMissions = newMissions,
                lastMissionUpdateDay = newDay
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
    fun executeInteraction(concubineId: String, interaction: GameInteraction): Pair<Boolean, String> {
        val current = _gameState.value
        val player = current.player
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
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
        if (interaction.requiresFavorite && !concubine.oblibena) {
            return Pair(false, "Tato akce vyžaduje, aby byla dívka jmenována tvou Oblíbenkyní ★!")
        }
        if (interaction.requiresWife && !concubine.jeManzelkou) {
            return Pair(false, "Tato akce vyžaduje manželský svazek 💍!")
        }
        if (concubine.fazeZkazenosti < interaction.minPhase) {
            return Pair(false, "Dívka musí dosáhnout alespoň fáze zkázanosti ${interaction.minPhase}!")
        }

        player.sexEnergy -= interaction.energyCost
        player.darkEnergy -= interaction.darkCost
        player.gold -= interaction.goldCost

        // Apply interaction
        val message = interaction.applyEffect(concubine, player)

        // Affinity Passives Processing for Intimate Interactions
        if (interaction.type == "intimni") {
            if (concubine.affinityLevel >= 3) {
                // Level 3: +15% efektivita (we boost stats directly here), +10% zisk temné energie
                concubine.touha = (concubine.touha + 3).coerceAtMost(100)
                concubine.vlhkost = (concubine.vlhkost + 3).coerceAtMost(100)
                concubine.duvera = (concubine.duvera + 3).coerceAtMost(100)
                player.darkEnergy = (player.darkEnergy + 5).coerceAtMost(player.maxDarkEnergy)
            }
            if (concubine.affinityLevel >= 4 && !concubine.tehotna && interaction.id == "eroticka_noc") {
                // Level 4: +20% šance na zplození dědice (extra roll)
                if ((1..100).random() <= 20) {
                    concubine.tehotna = true
                    concubine.dnyTehotenstvi = 0
                }
            }
        }

        // Recalculate degradation phase
        val newPhase = StaticData.calculatePhase(
            broken = concubine.broken,
            mindbreak = concubine.mindbreak,
            poslusnost = concubine.poslusnost,
            loajalita = concubine.loajalita,
            painAddiction = concubine.painAddiction,
            scarred = concubine.scarred,
            touha = concubine.touha,
            humiliation = concubine.humiliation,
            zavislost = concubine.zavislost,
            age = concubine.age,
            pregnant = concubine.tehotna
        )
        if (newPhase > concubine.fazeZkazenosti) {
            concubine.fazeZkazenosti = newPhase
            val phaseInfo = StaticData.DEGRADATION_PHASES[newPhase]
            addLog("★ ${concubine.name} postoupila do fáze zkázanosti: ${phaseInfo?.name ?: "$newPhase"}!")
        }

        // Add player XP & harem EXP
        addPlayerXp(12)
        addHaremExp(8)
        progressMission("INTERACT", 1)

        addLog(message)
        updateState { it.copy() }
        return Pair(true, message)
    }

    fun setFavorite(concubineId: String): String {
        var msg = ""
        updateState { current ->
            val updated = current.concubines.map { c ->
                val copy = c.copy()
                if (copy.id == concubineId) {
                    copy.oblibena = true
                    copy.loajalita = (copy.loajalita + 15).coerceAtMost(100)
                    copy.duvera = (copy.duvera + 10).coerceAtMost(100)
                    msg = "★ ${copy.name} byla jmenována tvou jedinou vyvolenou Oblíbenkyní! Ostatní v harému zatajily dech."
                } else {
                    copy.oblibena = false
                }
                copy
            }
            current.copy(concubines = updated)
        }
        addLog(msg)
        return msg
    }

    fun courtRomance(concubineId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (current.player.gold < 50) {
            return Pair(false, "Dvoření vyžaduje 50 zlatých na dary a hostinu.")
        }
        current.player.gold -= 50
        concubine.romanceBody += 15
        concubine.duvera = (concubine.duvera + 10).coerceAtMost(100)
        concubine.srdce = (concubine.srdce + 12).coerceAtMost(100)

        if (concubine.romanceBody >= 50 && !concubine.partnerka) {
            concubine.partnerka = true
            addLog("♥ ${concubine.name} přijala tvůj slib a stala se tvou oficiální Partnerkou!")
            return Pair(true, "♥ ${concubine.name} je nyní tvou Partnerkou!")
        }

        val res = "${concubine.name} byla potěšena tvou přízní (Romance: ${concubine.romanceBody}/100)."
        addLog(res)
        updateState { it.copy() }
        return Pair(true, res)
    }

    fun marryConcubine(concubineId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (concubine.romanceBody < 80 || concubine.loajalita < 70) {
            return Pair(false, "Svatba vyžaduje alespoň 80 Romance a 70 Loajality!")
        }
        if (current.player.gold < 300) {
            return Pair(false, "Královská svatba vyžaduje 300 zlatých na obřad.")
        }

        current.player.gold -= 300
        concubine.jeManzelkou = true
        concubine.partnerka = true
        concubine.loajalita = 100
        concubine.duvera = 100
        current.player.reputation += 15
        current.player.maxSexEnergy = (current.player.maxSexEnergy + 10).coerceAtMost(250)
        current.player.maxDarkEnergy = (current.player.maxDarkEnergy + 5).coerceAtMost(200)

        val msg = "💍 SLAVNOSTNÍ SVATBA! ${concubine.name} se stala tvou Manželkou a Paní dominia! Max energie trvale navýšena."
        addLog(msg)
        updateState { it.copy() }
        return Pair(true, msg)
    }

    // --- HUNTING & RECRUITMENT ---
    fun hunt(locationName: String): Pair<Concubine?, String> {
        val current = _gameState.value
        val player = current.player

        if (player.sexEnergy < 15) {
            return Pair(null, "Na lov potřebuješ alespoň 15 energie.")
        }
        player.sexEnergy -= 15

        val archetypes = StaticData.ARCHETYPES.keys.toList()
        val randomArchetype = archetypes.random()
        val randomName = StaticData.NAMES.filter { n -> current.concubines.none { it.name == n } }.randomOrNull()
            ?: "Dívka ze stínů ${Random.nextInt(10, 99)}"

        val age = Random.nextInt(18, 28)
        val newGirl = Concubine(
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

        updateState { it.copy(concubines = it.concubines + newGirl) }
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

    fun exploreDomain(domainId: String): Pair<Concubine?, String> {
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

        val randomName = StaticData.NAMES.filter { n -> current.concubines.none { it.name == n } }.randomOrNull()
            ?: "Dívka z ${domain.name} ${Random.nextInt(10, 99)}"

        val age = Random.nextInt(18, 27)
        val initialAffinity = Random.nextInt(10, 25)
        val newGirl = Concubine(
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
                concubines = state.concubines + newGirl,
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

        val randomName = StaticData.NAMES.filter { n -> current.concubines.none { it.name == n } }.randomOrNull()
            ?: "Otrokyně z aukce ${Random.nextInt(10, 99)}"

        val age = Random.nextInt(18, 26)
        val newConcubine = Concubine(
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

        val msg = "🏛️ Vydražil jsi otrokyni ${newConcubine.name} za $price zlatých!"
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - price).coerceAtLeast(0))
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = p, concubines = state.concubines + newConcubine, gameLog = logs)
        }
        addHaremExp(30)
        return Pair(true, msg)
    }

    // --- SLAVE RENTAL ---
    fun rentSlave(concubineId: String, clientType: String, days: Int): Pair<Boolean, String> {
        val current = _gameState.value
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (concubine.naNajmu) {
            return Pair(false, "Dívka je již na nájmu u klienta ${concubine.klient}!")
        }
        if (concubine.hp < 40) {
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
        val msg = "💰 ${concubine.name} byla pronajata klientovi ($clientType) na $days dní. Obdržel jsi zálohu $upfrontGold zlatých."

        updateState { state ->
            val p = state.player.copy(gold = state.player.gold + upfrontGold)
            val updatedConcubines = state.concubines.map { c ->
                if (c.id == concubineId) {
                    val copy = c.copy()
                    copy.naNajmu = true
                    copy.klient = clientType
                    copy.typNajmu = clientType
                    copy.najemZbyvaDni = days
                    copy
                } else c
            }
            val logs = (listOf(msg) + state.gameLog).take(30)
            state.copy(player = p, concubines = updatedConcubines, gameLog = logs)
        }
        return Pair(true, msg)
    }

    // --- UPGRADES ---
    fun upgradeBuilding(buildingType: String): Pair<Boolean, String> {
        val current = _gameState.value
        val building = current.buildings.firstOrNull { it.type == buildingType }
            ?: return Pair(false, "Budova nenalezena.")

        val cost = (building.baseCost * (building.level + 1))
        if (current.player.gold < cost) {
            return Pair(false, "Vylepšení vyžaduje $cost zlatých (máš ${current.player.gold})!")
        }

        val nextLevel = building.level + 1
        val msg = "🏰 Budova ${building.name} vylepšena na úroveň $nextLevel!"
        updateState { state ->
            val p = state.player.copy(gold = (state.player.gold - cost).coerceAtLeast(0))
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

    fun giveDirectGift(giftId: String, concubineId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val gift = GameContent.DIRECT_GIFTS.firstOrNull { it.id == giftId }
            ?: return Pair(false, "Dar nebyl nalezen.")
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nenalezena.")

        if (current.player.gold < gift.goldCost) {
            return Pair(false, "Nedostatek zlata! Potřebuješ ${gift.goldCost} zlatých (máš ${current.player.gold}).")
        }

        val affinityGain = (gift.loyaltyBoost + gift.trustBoost + gift.romanceBoost) / 2 + 10
        val msg = "🎁 ${concubine.name} ${gift.flavorMessage} (+${gift.loyaltyBoost} loajalita, +${gift.desireBoost} touha, +$affinityGain náklonnost)"
        updateState { state ->
            val updatedConcubines = state.concubines.map { c ->
                if (c.id == concubineId) {
                    val newLoyalty = (c.loajalita + gift.loyaltyBoost).coerceAtMost(100)
                    val newDesire = (c.touha + gift.desireBoost).coerceAtMost(100)
                    val newObedience = (c.poslusnost + gift.obedienceBoost).coerceAtMost(100)
                    val newTrust = (c.duvera + gift.trustBoost).coerceAtMost(100)
                    val newRomance = (c.romanceBody + gift.romanceBoost).coerceAtMost(100)
                    val isPartner = c.partnerka || newRomance >= 50
                    val newAffinity = c.affinityPoints + affinityGain
                    val newAffinityLvl = AffinityData.getLevelForPoints(newAffinity)
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
                concubines = updatedConcubines,
                gameLog = logs
            )
        }
        addPlayerXp(12)
        progressMission("GIFT", 1)
        return Pair(true, msg)
    }

    fun useItemOnConcubine(itemId: String, concubineId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count > 0 }
            ?: return Pair(false, "Předmět není v inventáři.")
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nenalezena.")

        var msg = "Předal jsi ${item.name} dívce ${concubine.name}."

        updateState { state ->
            val updatedItems = state.player.items.mapNotNull { itm ->
                if (itm.id == itemId) {
                    val remaining = itm.count - 1
                    if (remaining > 0) itm.copy(count = remaining) else null
                } else itm.copy()
            }.toMutableList()

            val updatedConcubines = state.concubines.map { c ->
                if (c.id == concubineId) {
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
                concubines = updatedConcubines,
                gameLog = logs
            )
        }
        addPlayerXp(12)
        progressMission("GIFT", 1)
        return Pair(true, msg)
    }

    fun useCombatConsumableOnPlayer(itemId: String): Pair<Boolean, String> {
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
        if (quest.reqConcubines > 0 && current.concubines.size < quest.reqConcubines) {
            return Pair(false, "Vyžaduje alespoň ${quest.reqConcubines} otrokyň v harému!")
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
    fun startBossCombat(boss: Boss) {
        val player = _gameState.value.player
        val initialEntry = CombatLogEntry(
            turn = 1,
            type = "system",
            message = "⚔️ Vstoupil jsi do arény proti: ${boss.name} (${boss.phaseName})!"
        )
        _combatState.value = CombatSession(
            boss = boss,
            bossHp = boss.hp,
            bossMaxHp = boss.maxHp,
            playerHp = player.hp,
            playerMaxHp = player.maxHp,
            turnCount = 1,
            isDefending = false,
            enemyBleedTurns = 0,
            enemyStunned = false,
            activeBuff = null,
            logEntries = listOf(initialEntry),
            log = listOf(initialEntry.message),
            isOver = false,
            victory = false,
            lootGained = null
        )
    }

    fun executeCombatTurn(action: String, itemId: String? = null) {
        val session = _combatState.value ?: return
        if (session.isOver) return

        val player = _gameState.value.player
        val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull() ?: Weapon("Pěsti temnoty", "kratka", 10, 0)
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

        // 1. Process Player Action
        val level5Count = _gameState.value.concubines.count { it.affinityLevel >= 5 }
        val playerMultiplier = 1.0f + (0.25f * level5Count)

        when (action) {
            "attack", "slash" -> {
                val isCrit = Random.nextInt(100) < (15 + (player.skills["boj"] ?: 0) * 2)
                val critMultiplier = if (isCrit) 1.65f else 1.0f
                val rawDmg = weapon.damage + (player.skills["boj"] ?: 0) * 3 + Random.nextInt(-2, 5)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.35f)) * critMultiplier) * playerMultiplier).toInt().coerceAtLeast(6)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                val critText = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isCrit) "player_attack" else "player_attack",
                    message = "🗡️ Sek zbraní ${weapon.name} udělil $finalDmg poškození!$critText"
                ))
            }
            "heavy_strike" -> {
                val isCrit = Random.nextInt(100) < 25
                val multiplier = if (isCrit) 2.2f else 1.5f
                val rawDmg = (weapon.damage * 1.5f) + (player.skills["boj"] ?: 0) * 4 + Random.nextInt(2, 10)
                val finalDmg = (((rawDmg - (session.boss.defense * 0.25f)) * multiplier) * playerMultiplier).toInt().coerceAtLeast(12)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_attack",
                    message = "⚡ Těžký drtivý úder zasadil $finalDmg drtivého poškození!" + (if (isCrit) " ⭐ Zničující dopad!" else "")
                ))
            }
            "bleed_strike" -> {
                val rawDmg = weapon.damage + (player.skills["boj"] ?: 0) * 2 + Random.nextInt(0, 4)
                val finalDmg = ((rawDmg - (session.boss.defense * 0.3f)) * playerMultiplier).toInt().coerceAtLeast(5)
                newBossHp = (newBossHp - finalDmg).coerceAtLeast(0)
                newBleedTurns = 3
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_attack",
                    message = "🩸 Krvavé bodnutí způsobilo $finalDmg zranění a otevřelo hluboké krvácející rány (3 kola)!"
                ))
            }
            "dark_burst" -> {
                if (player.darkEnergy >= 10) {
                    player.darkEnergy -= 10
                    newPlayerDark = player.darkEnergy
                    val darkDmg = ((38 + (player.skills["temnota"] ?: 0) * 6 + (weapon.darkBonus) + Random.nextInt(2, 12)) * playerMultiplier).toInt()
                    newBossHp = (newBossHp - darkDmg).coerceAtLeast(0)
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_spell",
                        message = "🔮 Temný výboj zasáhl cíl magickou silou za $darkDmg stínového poškození (-10 Temné energie)."
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš dostatek temné energie na výboj (vyžaduje 10)!"
                    ))
                }
            }
            "curse_shadow" -> {
                if (player.darkEnergy >= 15) {
                    player.darkEnergy -= 15
                    newPlayerDark = player.darkEnergy
                    val curseDmg = ((25 + (player.skills["temnota"] ?: 0) * 4) * playerMultiplier).toInt()
                    newBossHp = (newBossHp - curseDmg).coerceAtLeast(0)
                    activeBuff = "Prokletí stínů (Nepřítel oslaben)"
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_spell",
                        message = "👁️ Prokletí stínů srazilo nepřítele za $curseDmg zranění a oslabilo jeho útoky (-15 TE)."
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš dostatek temné energie na Prokletí (vyžaduje 15)!"
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
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_spell",
                        message = "🖤 Vysátí duše vytrhlo z nepřítele životní esenci za $drainDmg poškození a uzdravilo tě o +$healed HP (-20 TE)!"
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš dostatek temné energie na Vysátí duše (vyžaduje 20)!"
                    ))
                }
            }
            "defend" -> {
                isDefending = true
                val gainedDark = 8
                player.darkEnergy = (player.darkEnergy + gainedDark).coerceAtMost(player.maxDarkEnergy)
                newPlayerDark = player.darkEnergy
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "player_defend",
                    message = "🛡️ Zaujal jsi neprostupný obranný postoj (-65% utrženého zranění v tomto kole, +$gainedDark TE)!"
                ))
            }
            "harem_support" -> {
                val concubines = _gameState.value.concubines
                val favorite = concubines.firstOrNull { it.oblibena } ?: concubines.firstOrNull { it.jeManzelkou } ?: concubines.firstOrNull()
                if (favorite != null) {
                    val healAmt = 28 + (favorite.loajalita / 5)
                    val energyAmt = 15
                    newPlayerHp = (newPlayerHp + healAmt).coerceAtMost(session.playerMaxHp)
                    player.darkEnergy = (player.darkEnergy + energyAmt).coerceAtMost(player.maxDarkEnergy)
                    newPlayerDark = player.darkEnergy
                    activeBuff = "Požehnání harému (${favorite.name})"
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "player_support",
                        message = "💖 ${favorite.name} ti poslala duševní podporu z harému! Obdržel jsi +$healAmt HP a +$energyAmt TE!"
                    ))
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ V tvém harému není žádná otrokyně, která by ti dodala sílu!"
                    ))
                }
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
                            newLogEntries.add(0, CombatLogEntry(
                                turn = currentTurn,
                                type = "player_heal",
                                message = "🧪 Použil jsi ${item.name} a vyléčil se o +$healAmt HP."
                            ))
                        }
                        "elixir_touhy" -> {
                            player.darkEnergy = (player.darkEnergy + 35).coerceAtMost(player.maxDarkEnergy)
                            player.sexEnergy = (player.sexEnergy + 35).coerceAtMost(player.maxSexEnergy)
                            newPlayerDark = player.darkEnergy
                            newLogEntries.add(0, CombatLogEntry(
                                turn = currentTurn,
                                type = "player_heal",
                                message = "🧪 Vypil jsi ${item.name}! Tvé tělo zaplavila energie (+35 TE, +35 SE)."
                            ))
                        }
                        else -> {
                            val healAmt = 30
                            newPlayerHp = (newPlayerHp + healAmt).coerceAtMost(session.playerMaxHp)
                            newLogEntries.add(0, CombatLogEntry(
                                turn = currentTurn,
                                type = "player_heal",
                                message = "🧪 Využil jsi předmět ${item.name} (+30 HP)."
                            ))
                        }
                    }
                } else {
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "system",
                        message = "❌ Nemáš tento předmět k dispozici!"
                    ))
                }
            }
            "flee" -> {
                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = "system",
                    message = "🏃 Takticky jsi ustoupil a opustil bojiště."
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
                message = "🩸 Krvácení způsobilo nepříteli $bleedDmg zranění (zbývá $newBleedTurns kol)."
            ))
        }

        // 3. Check Enemy Defeat
        if (newBossHp <= 0) {
            isOver = true
            victory = true
            player.gold += session.boss.rewardGold
            addPlayerXp(session.boss.rewardXp)
            player.killCount += 1
            lootInfo = "+${session.boss.rewardGold} zlatých • +${session.boss.rewardXp} XP"
            newLogEntries.add(0, CombatLogEntry(
                turn = currentTurn,
                type = "victory",
                message = "🏆 VÍTĚZSTVÍ! Protivník ${session.boss.name} padl! Zisk: $lootInfo."
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
                    message = "💫 ${session.boss.name} je omráčen a vynechává své kolo!"
                ))
            } else {
                val isSpecialAttack = (currentTurn % 3 == 0)
                val baseEnemyAtk = session.boss.attack
                val defenseReduction = (player.skills["obrana"] ?: 0) * 2

                val rawBossDmg = if (isSpecialAttack) {
                    (baseEnemyAtk * 1.45f).toInt() + Random.nextInt(1, 6)
                } else {
                    baseEnemyAtk + Random.nextInt(-2, 4)
                }

                var finalEnemyDmg = (rawBossDmg - defenseReduction).coerceAtLeast(4)
                finalEnemyDmg = (finalEnemyDmg * (1.0f / playerMultiplier)).toInt()
                if (isDefending) {
                    finalEnemyDmg = (finalEnemyDmg * 0.35f).toInt().coerceAtLeast(2)
                }
                if (activeBuff?.contains("Prokletí") == true) {
                    finalEnemyDmg = (finalEnemyDmg * 0.75f).toInt().coerceAtLeast(2)
                }

                newPlayerHp = (newPlayerHp - finalEnemyDmg).coerceAtLeast(0)

                val attackTitle = if (isSpecialAttack) "💥 ${session.boss.name} provedl speciální techniku [${session.boss.phaseName}]" else "⚔️ ${session.boss.name} zaútočil"
                val defenseNotice = if (isDefending) " (útok odražen štítem!)" else ""

                newLogEntries.add(0, CombatLogEntry(
                    turn = currentTurn,
                    type = if (isSpecialAttack) "enemy_special" else "enemy_attack",
                    message = "$attackTitle za $finalEnemyDmg poškození!$defenseNotice"
                ))

                if (newPlayerHp <= 0) {
                    isOver = true
                    victory = false
                    newPlayerHp = 25
                    newLogEntries.add(0, CombatLogEntry(
                        turn = currentTurn,
                        type = "defeat",
                        message = "💀 Byl jsi v boji poražen! Tví poddaní tě odnesli zpět do bezpečí pevnosti."
                    ))
                    addLog("💀 Pán utrpěl porážku v boji proti ${session.boss.name}!")
                }
            }
        }

        player.hp = newPlayerHp
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
            saveDate = "Den ${state.player.day} - ${state.concubines.size} dívek"
        )
        _gameState.value = current
        val str = json.encodeToString(current)
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("save_slot_$slot", str).apply()
        addLog("💾 Hra byla uložena do slotu $slot.")
        return true
    }

    fun loadFromSlot(slot: Int): Boolean {
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        val str = prefs.getString("save_slot_$slot", null) ?: return false
        return try {
            val loaded = json.decodeFromString<GameSave>(str)
            _gameState.value = loaded
            _currentTheme.value = loaded.currentTheme
            addLog("📂 Hra načtena ze slotu $slot.")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun autoSave() {
        val state = _gameState.value
        val current = state.copy(
            slotNumber = 0,
            saveDate = "Den ${state.player.day} (Autosave)"
        )
        val str = json.encodeToString(current)
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("save_slot_autosave", str).apply()
    }

    fun getSlotSummary(slot: Int): String {
        val prefs = context.getSharedPreferences("harem_dark_prefs", Context.MODE_PRIVATE)
        val key = if (slot == 0) "save_slot_autosave" else "save_slot_$slot"
        val str = prefs.getString(key, null) ?: return "Prázdný slot"
        return try {
            val save = json.decodeFromString<GameSave>(str)
            "Den ${save.player.day} | ${save.player.gold} zlata | Harém: ${save.concubines.size} dívek"
        } catch (e: Exception) {
            "Poškozená data"
        }
    }
}
