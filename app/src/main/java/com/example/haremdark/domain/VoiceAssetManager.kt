package com.example.haremdark.domain

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.haremdark.models.Character
import com.example.haremdark.ui.components.CharacterEmotionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages playback of high-quality pre-recorded voice assets for key story moments.
 * Supports mapping between story IDs and raw resources.
 */
object VoiceAssetManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAssetId: String? = null

    private val _isAssetPlaying = MutableStateFlow(false)
    val isAssetPlaying: StateFlow<Boolean> = _isAssetPlaying.asStateFlow()

    // Registry of available high-quality voice assets
    // Key format: "episodeId_pageIndex_mood" or specific identifiers
    private val voiceRegistry = mapOf<String, Int>(
        // Example mappings for "subka_ep1"
        "subka_ep1_p1_neutral" to 101, // Mock resource IDs (would be R.raw.xxx)
        "subka_ep1_p3_love" to 102
    )

    fun playVoiceAsset(
        context: Context,
        episodeId: String,
        pageIndex: Int,
        mood: CharacterEmotionType,
        customVoiceId: String? = null
    ) {
        val assetKey = customVoiceId ?: "${episodeId}_p${pageIndex}_${mood.name.lowercase()}"
        
        // In a real app, we would resolve this to R.raw.voice_file
        // Since we don't have actual raw files, we'll simulate the system logic
        Log.d("VoiceAssetManager", "Attempting to play voice asset: $assetKey")
        
        // Stop any current playback
        stopPlayback()

        // Integration with VoiceManager (stop TTS if asset is playing)
        VoiceManager.stop()

        // Simulate successful resolution and playback start
        _isAssetPlaying.value = true
        currentAssetId = assetKey
        
        // Note: Actual MediaPlayer implementation would require real R.raw files
        // mediaPlayer = MediaPlayer.create(context, resId)
        // mediaPlayer?.start()
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isAssetPlaying.value = false
            currentAssetId = null
        } catch (e: Exception) {
            Log.w("VoiceAssetManager", "Error stopping playback: ${e.message}")
        }
    }

    /**
     * Determines if a specific story page has a corresponding high-quality audio asset.
     */
    fun hasAssetFor(episodeId: String, pageIndex: Int, mood: CharacterEmotionType): Boolean {
        // For demo purposes, we'll say true for some specific early story pages
        if (episodeId == "subka_ep1" && (pageIndex == 1 || pageIndex == 3)) return true
        return false
    }
}
