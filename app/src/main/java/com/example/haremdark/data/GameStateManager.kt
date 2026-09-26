package com.example.haremdark.data

import android.content.Context
import com.example.haremdark.models.Character
import com.example.haremdark.models.InventoryItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

@Serializable
data class PlayerSkillState(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val manaCost: Int,
    val baseCooldownTurns: Int
)

@Serializable
data class SpecialScene(
    val id: String,
    val title: String,
    val description: String,
    val dialogue: String,
    val rewardText: String,
    val requiredAffection: Int
)

@Serializable
data class DailyObjective(
    val id: String,
    val title: String,
    val description: String,
    val targetType: String, // "interact", "train", "gift", "rest", "banquet", "heal"
    val targetCount: Int,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardGold: Int = 0,
    val rewardSexEnergy: Int = 0,
    val rewardAffection: Int = 0
)

@Serializable
data class GameStateManagerState(
    val characterStats: Map<String, CharacterStatSummary> = emptyMap(),
    val inventoryItems: List<InventoryItem> = emptyList(),
    val recruitmentProgress: Map<String, Int> = emptyMap(), // Character ID -> Progress % (0 to 100)
    val playerSkills: Map<String, PlayerSkillState> = emptyMap(),
    val skillCooldowns: Map<String, Int> = emptyMap(),
    // Added fields for fatigue, stress, and claimed scenes
    val characterFatigue: Map<String, Int> = emptyMap(), // Character ID -> Fatigue (0-100)
    val characterStress: Map<String, Int> = emptyMap(),  // Character ID -> Stress (0-100)
    val claimedSceneIds: Set<String> = emptySet(),        // Set of claimed scene IDs
    val dailyObjectives: List<DailyObjective> = emptyList(),
    val objectivesLastGeneratedDay: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class CharacterStatSummary(
    val id: String,
    val name: String,
    val level: Int,
    val strength: Int,
    val defense: Int,
    val affection: Int,
    val loyalty: Int,
    val obedience: Int
)

class GameStateManager(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val saveFile: File
        get() = File(context.filesDir, "game_state_manager_persistent_data.json")

    /**
     * Persist the current state to disk using Kotlin Serialization.
     */
    fun saveState(state: GameStateManagerState): Boolean {
        return try {
            val serialized = json.encodeToString(state)
            saveFile.writeText(serialized)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Load the persisted state from disk.
     */
    fun loadState(): GameStateManagerState {
        return try {
            if (saveFile.exists()) {
                val contents = saveFile.readText()
                json.decodeFromString<GameStateManagerState>(contents)
            } else {
                GameStateManagerState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GameStateManagerState()
        }
    }

    /**
     * Update statistics for a specific character.
     */
    fun updateCharacterStats(
        charId: String,
        name: String,
        level: Int,
        strength: Int,
        defense: Int,
        affection: Int,
        loyalty: Int,
        obedience: Int
    ): GameStateManagerState {
        val currentState = loadState()
        val updatedStats = currentState.characterStats.toMutableMap()
        updatedStats[charId] = CharacterStatSummary(
            id = charId,
            name = name,
            level = level,
            strength = strength,
            defense = defense,
            affection = affection,
            loyalty = loyalty,
            obedience = obedience
        )
        val newState = currentState.copy(
            characterStats = updatedStats,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Set or update fatigue and stress for a character.
     */
    fun updateFatigueAndStress(
        charId: String,
        fatigueDelta: Int,
        stressDelta: Int
    ): GameStateManagerState {
        val currentState = loadState()
        val updatedFatigue = currentState.characterFatigue.toMutableMap()
        val updatedStress = currentState.characterStress.toMutableMap()

        val currentFatigue = updatedFatigue[charId] ?: 10
        val currentStress = updatedStress[charId] ?: 5

        updatedFatigue[charId] = (currentFatigue + fatigueDelta).coerceIn(0, 100)
        updatedStress[charId] = (currentStress + stressDelta).coerceIn(0, 100)

        val newState = currentState.copy(
            characterFatigue = updatedFatigue,
            characterStress = updatedStress,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Set directly.
     */
    fun setFatigueAndStress(
        charId: String,
        fatigue: Int,
        stress: Int
    ): GameStateManagerState {
        val currentState = loadState()
        val updatedFatigue = currentState.characterFatigue.toMutableMap()
        val updatedStress = currentState.characterStress.toMutableMap()

        updatedFatigue[charId] = fatigue.coerceIn(0, 100)
        updatedStress[charId] = stress.coerceIn(0, 100)

        val newState = currentState.copy(
            characterFatigue = updatedFatigue,
            characterStress = updatedStress,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    fun getFatigue(charId: String): Int {
        return loadState().characterFatigue[charId] ?: 10
    }

    fun getStress(charId: String): Int {
        return loadState().characterStress[charId] ?: 5
    }

    /**
     * Mark a scene as claimed/read.
     */
    fun claimScene(sceneId: String): GameStateManagerState {
        val currentState = loadState()
        val updatedClaims = currentState.claimedSceneIds.toMutableSet()
        updatedClaims.add(sceneId)

        val newState = currentState.copy(
            claimedSceneIds = updatedClaims,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    fun isSceneClaimed(sceneId: String): Boolean {
        return loadState().claimedSceneIds.contains(sceneId)
    }

    /**
     * Get the dialogue and event scenes available for a specific girl based on her affection.
     */
    fun getDialogueScenesForAffection(
        charId: String,
        name: String,
        archetypeId: String,
        affection: Int
    ): List<SpecialScene> {
        val scenes = when (archetypeId) {
            "draci_divka" -> listOf(
                SpecialScene(
                    id = "${charId}_scene_40",
                    title = "🔥 Dračí Jiskra",
                    description = "Aurelia ti u večerního krbu vypráví o zrození dračího plamene v jejím rodu a poprvé se dotkne tvé dlaně.",
                    dialogue = "Aurelia: 'Můj oheň dokáže sežehnout celé armády, Pane... ale ve tvé blízkosti cítím hřejivý klid. Možná, že mé srdce nepatří jen bitvě, ale i tobě.'",
                    rewardText = "+10 Loajalita, +15 Morálka, +10% Síla",
                    requiredAffection = 40
                ),
                SpecialScene(
                    id = "${charId}_scene_70",
                    title = "🐉 Slib Ohně",
                    description = "Aurelia ti slavnostně daruje šupinu ze svého srdce a slibuje věčnou věrnost.",
                    dialogue = "Aurelia: 'Tato šupina chrání před jakýmkoli žárem. Nos ji u sebe. Můj plamen tě od nynějška nikdy nespálí, budu tvým věčným štítem i mečem.'",
                    rewardText = "+15 Loajalita, +25 Max HP, Unikátní dračí aura",
                    requiredAffection = 70
                ),
                SpecialScene(
                    id = "${charId}_scene_95",
                    title = "👑 Věčné Spojenectví",
                    description = "Duševní rituál, který navždy propojí tvou krev s dračí magií Aurelie.",
                    dialogue = "Aurelia: 'Jsme jedno tělo, jedna duše. Můj oheň je tvým ohněm. Tvůj nepřítel bude popelem. Miluji tě víc než svobodu samotnou.'",
                    rewardText = "+25 Loajalita, +50 Power Level, Titul: Dračí Pán",
                    requiredAffection = 95
                )
            )
            "sukuba" -> listOf(
                SpecialScene(
                    id = "${charId}_scene_40",
                    title = "😈 Šepot Stínů",
                    description = "Lilith se ti potichu svěřuje se svým věčným osaměním v temnotě podsvětí.",
                    dialogue = "Lilith: 'Všichni ode mě očekávají jen svádění a zradu, můj Pane. Ale ty... ty mě vnímáš jako bytost. Tvůj pohled mě spaluje víc než jakákoli magie.'",
                    rewardText = "+12 Poslušnost, +10 Důvěra",
                    requiredAffection = 40
                ),
                SpecialScene(
                    id = "${charId}_scene_70",
                    title = "🖤 Smlouva Krve",
                    description = "Lilith ti nabízí krevní rituál absolutního odevzdání výměnou za to, že ji nikdy neopustíš.",
                    dialogue = "Lilith: 'Vypij mou krev a dovol mi okusit tvou. Budu ti patřit celá, se všemi svými hříchy i magií. Slib mi jen, že mě nenecháš znovu samotnou.'",
                    rewardText = "+20 Loajalita, +15 Max Mana, Odolnost proti temnotě",
                    requiredAffection = 70
                ),
                SpecialScene(
                    id = "${charId}_scene_95",
                    title = "💖 Absolutní Posedlost",
                    description = "Lilith se ti zcela odevzdává a přiznává, že jsi jejím jediným smyslem existence.",
                    dialogue = "Lilith: 'Můj drahý... jsi mým pánem, mým bohem, mým vším. Bez tebe je svět jen chladnou prázdnotou. Dovol mi zůstat po tvém boku navždy.'",
                    rewardText = "+30 Loajalita, +40 Power Level, Titul: Vládce stínů",
                    requiredAffection = 95
                )
            )
            "knezkyn" -> listOf(
                SpecialScene(
                    id = "${charId}_scene_40",
                    title = "✨ Ztracená Víra",
                    description = "Sestra Elena ti odhaluje pravdu o svém vyhnanství ze svatého řádu.",
                    dialogue = "Elena: 'Když mě řád zavrhl, myslela jsem, že světlo mě navždy opustilo. Ale v tvém dominiu... nacházím nový, upřímnější smysl pro svou víru.'",
                    rewardText = "+15 Morálka, +12 Důvěra",
                    requiredAffection = 40
                ),
                SpecialScene(
                    id = "${charId}_scene_70",
                    title = "🌸 Nové Světlo",
                    description = "Elena se s tebou společně modlí a její léčivá záře zesílí díky tvé podpoře.",
                    dialogue = "Elena: 'Mé modlitby již nepatří vzdáleným bohům, ale tobě a bezpečí našich lidí. Dovol mi, abych tě chránila svým světlem před každým stínem.'",
                    rewardText = "+15 Loajalita, +20 Léčení, +15% Obrana",
                    requiredAffection = 70
                ),
                SpecialScene(
                    id = "${charId}_scene_95",
                    title = "🕊️ Svaté Smíření",
                    description = "Elena požehná tvé duši a slibuje ti absolutní duchovní oddanost.",
                    dialogue = "Elena: 'Cesta stínu, kterou kráčíš, je nebezpečná... ale já půjdu s tebou. Budu tvým světlem v nejtemnějších hlubinách. Miluji tě celou svou duší.'",
                    rewardText = "+30 Loajalita, +30 Power Level, Titul: Svatý Ochránce",
                    requiredAffection = 95
                )
            )
            else -> listOf(
                SpecialScene(
                    id = "${charId}_scene_40",
                    title = "💬 Důvěrná Rozprava",
                    description = "Společné klidné chvíle v komnatách, kde se $name otevírá a sdílí své sny.",
                    dialogue = "$name: 'Můj Pane, tyto společné chvíle pro mě znamenají víc než cokoli jiného. Děkuji ti, že si na mě uděláš čas v tomto krutém světě.'",
                    rewardText = "+8 Loajalita, +12 Morálka",
                    requiredAffection = 40
                ),
                SpecialScene(
                    id = "${charId}_scene_70",
                    title = "💖 Hluboké Pouto",
                    description = "$name skládá osobní slib věrnosti a vyjadřuje ti svou upřímnou podporu.",
                    dialogue = "$name: 'Moje věrnost ti nepatří kvůli tvé síle, ale kvůli tvému srdci. Budu stát při tobě, ať už nás čeká vítězství, nebo pád.'",
                    rewardText = "+15 Loajalita, +15% Obrana",
                    requiredAffection = 70
                ),
                SpecialScene(
                    id = "${charId}_scene_95",
                    title = "👑 Osudové Spojení",
                    description = "Okamžik absolutního sblížení, kdy ti $name zcela odevzdává své srdce.",
                    dialogue = "$name: 'Ty jsi mým osudem, můj drahý. Patřím ti celá, dnes i navždy. Moje srdce tluče jen pro tebe.'",
                    rewardText = "+25 Loajalita, +30 Power Level",
                    requiredAffection = 95
                )
            )
        }
        return scenes.filter { affection >= it.requiredAffection }
    }

    /**
     * Add or update an item in the inventory.
     */
    fun addInventoryItem(item: InventoryItem): GameStateManagerState {
        val currentState = loadState()
        val updatedInventory = currentState.inventoryItems.toMutableList()
        val existingIndex = updatedInventory.indexOfFirst { it.id == item.id }

        if (existingIndex != -1) {
            val existing = updatedInventory[existingIndex]
            updatedInventory[existingIndex] = existing.copy(count = existing.count + item.count)
        } else {
            updatedInventory.add(item)
        }

        val newState = currentState.copy(
            inventoryItems = updatedInventory,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Remove or decrement an item from the inventory.
     */
    fun removeInventoryItem(itemId: String, countToRemove: Int): GameStateManagerState {
        val currentState = loadState()
        val updatedInventory = currentState.inventoryItems.toMutableList()
        val existingIndex = updatedInventory.indexOfFirst { it.id == itemId }

        if (existingIndex != -1) {
            val existing = updatedInventory[existingIndex]
            if (existing.count <= countToRemove) {
                updatedInventory.removeAt(existingIndex)
            } else {
                updatedInventory[existingIndex] = existing.copy(count = existing.count - countToRemove)
            }
        }

        val newState = currentState.copy(
            inventoryItems = updatedInventory,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Set or increment the progress of a harem character recruitment.
     */
    fun advanceRecruitmentProgress(charId: String, increment: Int): GameStateManagerState {
        val currentState = loadState()
        val updatedProgress = currentState.recruitmentProgress.toMutableMap()
        val currentProgress = updatedProgress[charId] ?: 0
        updatedProgress[charId] = (currentProgress + increment).coerceIn(0, 100)

        val newState = currentState.copy(
            recruitmentProgress = updatedProgress,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Check if a character has been fully recruited (reaches 100% progress).
     */
    fun isCharacterRecruited(charId: String): Boolean {
        return (loadState().recruitmentProgress[charId] ?: 0) >= 100
    }

    /**
     * Generate 3 randomized daily objectives for the given day if they haven't been generated yet.
     */
    fun generateDailyObjectives(day: Int): GameStateManagerState {
        val currentState = loadState()
        if (currentState.objectivesLastGeneratedDay == day && currentState.dailyObjectives.isNotEmpty()) {
            return currentState
        }

        val pool = listOf(
            DailyObjective(
                id = "obj_interact",
                title = "💬 Důvěrná slova",
                description = "Pozdrav nebo promluv s dívkami v harému celkem 3×.",
                targetType = "interact",
                targetCount = 3,
                rewardGold = 20,
                rewardAffection = 4
            ),
            DailyObjective(
                id = "obj_train",
                title = "⚡ Kázeňský dril",
                description = "Dokonči 2× společný výcvik poslušnosti dcer.",
                targetType = "train",
                targetCount = 2,
                rewardGold = 35,
                rewardSexEnergy = 5
            ),
            DailyObjective(
                id = "obj_rest",
                title = "🧖 Osobní péče",
                description = "Dopřej dceři osobní lázně nebo ji utěš celkem 2×.",
                targetType = "rest",
                targetCount = 2,
                rewardGold = 20,
                rewardAffection = 6
            ),
            DailyObjective(
                id = "obj_banquet",
                title = "🥂 Noční hostina",
                description = "Uspořádej pro svůj harém 1× velkolepou noční hostinu.",
                targetType = "banquet",
                targetCount = 1,
                rewardGold = 40,
                rewardAffection = 10
            ),
            DailyObjective(
                id = "obj_gift",
                title = "🎁 Radost z dávání",
                description = "Rozdej drobné dárky dcerám v harému celkem 3×.",
                targetType = "gift",
                targetCount = 3,
                rewardGold = 50,
                rewardSexEnergy = 8
            ),
            DailyObjective(
                id = "obj_heal",
                title = "🧪 Alchymistické ošetření",
                description = "Aplikuj léčivá tonika k uzdravení harému celkem 2×.",
                targetType = "heal",
                targetCount = 2,
                rewardGold = 30,
                rewardSexEnergy = 4
            )
        )

        // Select 3 random unique objectives
        val selected = pool.shuffled().take(3)

        val newState = currentState.copy(
            dailyObjectives = selected,
            objectivesLastGeneratedDay = day,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Increment progress for any active objective matching the targetType.
     */
    fun incrementObjectiveProgress(targetType: String, amount: Int = 1): GameStateManagerState {
        val currentState = loadState()
        if (currentState.dailyObjectives.isEmpty()) return currentState

        val updatedObjectives = currentState.dailyObjectives.map { obj ->
            if (obj.targetType == targetType && !obj.isClaimed && !obj.isCompleted) {
                val newCount = obj.currentCount + amount
                val completed = newCount >= obj.targetCount
                obj.copy(
                    currentCount = newCount.coerceAtMost(obj.targetCount),
                    isCompleted = completed
                )
            } else obj
        }

        val newState = currentState.copy(
            dailyObjectives = updatedObjectives,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }

    /**
     * Claim objective reward. Runs a callback to award actual player resources, then marks objective as claimed.
     */
    fun claimObjectiveReward(
        objectiveId: String,
        onRewardClaimed: (gold: Int, sexEnergy: Int, affectionBoost: Int) -> Unit
    ): GameStateManagerState {
        val currentState = loadState()
        val updatedObjectives = currentState.dailyObjectives.map { obj ->
            if (obj.id == objectiveId && obj.isCompleted && !obj.isClaimed) {
                onRewardClaimed(obj.rewardGold, obj.rewardSexEnergy, obj.rewardAffection)
                obj.copy(isClaimed = true)
            } else obj
        }

        val newState = currentState.copy(
            dailyObjectives = updatedObjectives,
            lastUpdated = System.currentTimeMillis()
        )
        saveState(newState)
        return newState
    }
}
