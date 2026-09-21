package com.example.haremdark.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.haremdark.models.GameSave
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.saveDataStore by preferencesDataStore(name = "harem_dark_persistent_saves")

class SaveManager(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun getSlotKey(slot: Int): String = when (slot) {
        0 -> "save_slot_autosave"
        99 -> "save_slot_quicksave"
        else -> "save_slot_$slot"
    }

    suspend fun saveGame(slot: Int, state: GameSave) {
        val key = stringPreferencesKey(getSlotKey(slot))
        val serialized = json.encodeToString(state.copy(slotNumber = slot))
        context.saveDataStore.edit { prefs ->
            prefs[key] = serialized
        }
    }

    suspend fun loadGame(slot: Int): GameSave? {
        val key = stringPreferencesKey(getSlotKey(slot))
        val prefs = context.saveDataStore.data.first()
        val savedJson = prefs[key] ?: return null
        return try {
            json.decodeFromString<GameSave>(savedJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSlotSummaries(): Flow<Map<Int, String>> {
        return context.saveDataStore.data.map { prefs ->
            val summaries = mutableMapOf<Int, String>()
            listOf(0, 1, 2, 3, 99).forEach { slot ->
                val key = stringPreferencesKey(getSlotKey(slot))
                val savedJson = prefs[key]
                summaries[slot] = if (savedJson != null) {
                    try {
                        val save = json.decodeFromString<GameSave>(savedJson)
                        val codexCount = save.player.unlockedCodexIds.size
                        val avgAffinity = if (save.characters.isNotEmpty()) 
                            save.characters.map { it.affinityPoints }.average().toInt() else 0
                        val synergiesCount = save.activeBuffs.size + save.activeDrugBuffs.size
                        "Den ${save.player.day} | Náklonnost: $avgAffinity | Kodex: $codexCount | Synergie: $synergiesCount"
                    } catch (e: Exception) {
                        "Chyba dat"
                    }
                } else "Prázdný slot"
            }
            summaries
        }
    }
}
