package com.example.haremdark.domain

import android.content.Context
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

    private fun updateState(transform: (GameSave) -> GameSave) {
        val current = _gameState.value
        val newState = transform(current)
        _gameState.value = newState
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

            // Passive income from mafia and buildings
            val buildingIncome = current.buildings.sumOf { it.level * 15 }
            val territoryIncome = current.territories.filter { it.level > 0 }.sumOf { it.baseIncome * it.level }
            val totalPassive = buildingIncome + territoryIncome + (current.haremLevel * 10)

            // Process slave rentals
            var rentalIncome = 0
            val updatedConcubines = current.concubines.map { c ->
                val copy = c.copy()
                if (copy.naNajmu) {
                    copy.najemZbyvaDni = (copy.najemZbyvaDni - 1).coerceAtLeast(0)
                    copy.najemPrijemCelkem += 50
                    rentalIncome += 50
                    if (copy.najemZbyvaDni == 0) {
                        copy.naNajmu = false
                        copy.klient = null
                        copy.typNajmu = null
                        addLog("Dívka ${copy.name} se vrátila z nájmu zpět do tvého harému.")
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

            val newMaxSex = (p.maxSexEnergy + maxSexBonus).coerceAtMost(250)
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

            current.copy(
                player = p,
                concubines = updatedConcubines,
                gameLog = logs
            )
        }
        autoSave()
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

    // --- AUCTION HOUSE ---
    fun buyAuction(archetypeId: String, price: Int): Pair<Boolean, String> {
        val current = _gameState.value
        if (current.player.gold < price) {
            return Pair(false, "Nedostatek zlata pro nákup na dražbě (${current.player.gold}/$price zlatých)!")
        }

        current.player.gold -= price
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

        updateState { it.copy(concubines = it.concubines + newConcubine) }
        addHaremExp(30)
        val msg = "🏛️ Vydražil jsi otrokyni ${newConcubine.name} za $price zlatých!"
        addLog(msg)
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

        val upfrontGold = days * 45
        current.player.gold += upfrontGold
        concubine.naNajmu = true
        concubine.klient = clientType
        concubine.typNajmu = "Služba v paláci"
        concubine.najemZbyvaDni = days

        val msg = "💰 ${concubine.name} byla pronajata klientovi ($clientType) na $days dní. Obdržel jsi zálohu $upfrontGold zlatých."
        addLog(msg)
        updateState { it.copy() }
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

        current.player.gold -= cost
        building.level += 1
        val msg = "🏰 Budova ${building.name} vylepšena na úroveň ${building.level}!"
        addLog(msg)
        updateState { it.copy() }
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

        current.player.gold -= cost
        territory.level += 1
        territory.securityLevel = (territory.securityLevel + 15).coerceAtMost(100)
        val msg = "🗡️ Území ${territory.name} povýšeno na úroveň ${territory.level}! Pasivní příjem vzrostl."
        addLog(msg)
        updateState { it.copy() }
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

        p.gold -= cost
        p.skillPoints -= 1
        val curEnd = p.skills["vytrvalost"] ?: 0
        p.skills["vytrvalost"] = curEnd + 1
        p.maxSexEnergy = (p.maxSexEnergy + 8).coerceAtMost(250)
        p.maxDarkEnergy = (p.maxDarkEnergy + 5).coerceAtMost(200)

        val msg = "⚡ Trénink výdrže úspěšný! Max sex energie: ${p.maxSexEnergy}, Max temná energie: ${p.maxDarkEnergy}."
        addLog(msg)
        updateState { it.copy() }
        return Pair(true, msg)
    }

    fun upgradeSkill(skillKey: String): Pair<Boolean, String> {
        val current = _gameState.value
        val p = current.player
        if (p.skillPoints < 1) {
            return Pair(false, "Nemáš žádné volné body dovedností!")
        }

        p.skillPoints -= 1
        val curVal = p.skills[skillKey] ?: 0
        p.skills[skillKey] = curVal + 1
        val msg = "⭐ Dovednost $skillKey zvýšena na ${p.skills[skillKey]}!"
        addLog(msg)
        updateState { it.copy() }
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

        p.gold -= recipe.goldCost
        p.darkEnergy -= recipe.darkCost

        val existing = p.items.firstOrNull { it.id == recipe.resultItem.id }
        if (existing != null) {
            existing.count += 1
        } else {
            p.items.add(recipe.resultItem.copy())
        }

        addPlayerXp(18)
        val msg = "🧪 Uvařil jsi ${recipe.resultItem.name}!"
        addLog(msg)
        updateState { it.copy() }
        return Pair(true, msg)
    }

    fun useItemOnConcubine(itemId: String, concubineId: String): Pair<Boolean, String> {
        val current = _gameState.value
        val item = current.player.items.firstOrNull { it.id == itemId && it.count > 0 }
            ?: return Pair(false, "Předmět není v inventáři.")
        val concubine = current.concubines.firstOrNull { it.id == concubineId }
            ?: return Pair(false, "Dívka nenalezena.")

        item.count -= 1
        if (item.count <= 0) current.player.items.remove(item)

        val msg = when (item.id) {
            "elixir_touhy" -> {
                concubine.touha = (concubine.touha + 30).coerceAtMost(100)
                concubine.vlhkost = (concubine.vlhkost + 25).coerceAtMost(100)
                "${concubine.name} vypila Elixír touhy. Její tváře planou touhou a vzrušením."
            }
            "hojivy_balzam" -> {
                concubine.hp = (concubine.hp + 35).coerceAtMost(concubine.maxHp)
                "Hojivý balzám zahojil zranění ${concubine.name} (+35 HP)."
            }
            "serum_poslusnost" -> {
                concubine.poslusnost = (concubine.poslusnost + 20).coerceAtMost(100)
                concubine.submisivita = (concubine.submisivita + 15).coerceAtMost(100)
                concubine.loajalita = (concubine.loajalita + 15).coerceAtMost(100)
                "${concubine.name} přijala sérum poslušnosti. Její pohled se stal naprosto odevzdaným."
            }
            else -> {
                concubine.loajalita = (concubine.loajalita + 10).coerceAtMost(100)
                "Předal jsi ${item.name} dívce ${concubine.name}."
            }
        }
        addLog(msg)
        updateState { it.copy() }
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

        current.player.gold += quest.rewardGold
        addPlayerXp(quest.rewardXp)
        current.player.darkEnergy = (current.player.darkEnergy + quest.rewardDarkEnergy).coerceAtMost(current.player.maxDarkEnergy)
        current.player.reputation += quest.rewardReputation

        val msg = "📜 Úkol '${quest.title}' splněn! Obdržel jsi ${quest.rewardGold} zlatých a ${quest.rewardXp} XP."
        addLog(msg)
        updateState { it.copy(completedQuests = it.completedQuests + questId) }
        return Pair(true, msg)
    }

    // --- COMBAT SYSTEM ---
    fun startBossCombat(boss: Boss) {
        _combatState.value = CombatSession(
            boss = boss,
            bossHp = boss.hp,
            bossMaxHp = boss.maxHp,
            playerHp = _gameState.value.player.hp,
            playerMaxHp = _gameState.value.player.maxHp,
            log = listOf("⚔️ Vstoupil jsi do arény proti: ${boss.name} (${boss.phaseName})!"),
            isOver = false,
            victory = false
        )
    }

    fun executeCombatTurn(action: String) {
        val session = _combatState.value ?: return
        if (session.isOver) return

        val player = _gameState.value.player
        val weapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.first()
        var newBossHp = session.bossHp
        var newPlayerHp = session.playerHp
        val newLog = session.log.toMutableList()
        var isOver = false
        var victory = false

        when (action) {
            "attack" -> {
                val playerDmg = (weapon.damage + (player.skills["boj"] ?: 0) * 3 + Random.nextInt(-3, 6)).coerceAtLeast(5)
                newBossHp = (newBossHp - playerDmg).coerceAtLeast(0)
                newLog.add(0, "🗡️ Zaútočil jsi zbraní ${weapon.name} a udělil $playerDmg poškození.")
            }
            "dark_burst" -> {
                if (player.darkEnergy >= 10) {
                    player.darkEnergy -= 10
                    val darkDmg = 35 + (player.skills["temnota"] ?: 0) * 5 + Random.nextInt(0, 10)
                    newBossHp = (newBossHp - darkDmg).coerceAtLeast(0)
                    newLog.add(0, "🔮 Temný výboj zasáhl cíl za $darkDmg drtivého poškození (-10 Temné energie).")
                } else {
                    newLog.add(0, "❌ Nemáš dostatek temné energie na výboj!")
                }
            }
            "heal" -> {
                val healItem = player.items.firstOrNull { it.id == "hojivy_balzam" && it.count > 0 }
                if (healItem != null) {
                    healItem.count -= 1
                    if (healItem.count <= 0) player.items.remove(healItem)
                    newPlayerHp = (newPlayerHp + 40).coerceAtMost(session.playerMaxHp)
                    newLog.add(0, "🧪 Použil jsi Hojivý balzám (+40 HP).")
                } else {
                    newLog.add(0, "❌ Nemáš v inventáři žádný hojivý balzám!")
                }
            }
            "flee" -> {
                newLog.add(0, "🏃 Uprchl jsi z boje.")
                _combatState.value = null
                return
            }
        }

        // Check boss death
        if (newBossHp <= 0) {
            isOver = true
            victory = true
            player.gold += session.boss.rewardGold
            addPlayerXp(session.boss.rewardXp)
            player.killCount += 1
            newLog.add(0, "🏆 VÍTĚZSTVÍ! Boss ${session.boss.name} byl poražen! Zisk: +${session.boss.rewardGold} zlatých, +${session.boss.rewardXp} XP.")
            addLog("🏆 Boss ${session.boss.name} byl poražen v souboji!")
            updateState { it.copy(defeatedBosses = it.defeatedBosses + session.boss.id) }
        } else {
            // Boss turn
            val bossDmg = (session.boss.attack - (player.skills["obrana"] ?: 0) * 2 + Random.nextInt(-2, 4)).coerceAtLeast(3)
            newPlayerHp = (newPlayerHp - bossDmg).coerceAtLeast(0)
            newLog.add(0, "💥 ${session.boss.name} ti zasadil úder za $bossDmg zranění.")

            if (newPlayerHp <= 0) {
                isOver = true
                victory = false
                newLog.add(0, "💀 Byl jsi poražen v boji! Probudil ses vyčerpaný ve své pevnosti.")
                newPlayerHp = 20
            }
        }

        player.hp = newPlayerHp
        _combatState.value = session.copy(
            bossHp = newBossHp,
            playerHp = newPlayerHp,
            log = newLog.take(20),
            isOver = isOver,
            victory = victory
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

data class CombatSession(
    val boss: Boss,
    val bossHp: Int,
    val bossMaxHp: Int,
    val playerHp: Int,
    val playerMaxHp: Int,
    val log: List<String>,
    val isOver: Boolean,
    val victory: Boolean
)
