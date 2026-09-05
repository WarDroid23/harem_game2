package com.example.haremdark.domain

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object VoiceManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("cs", "CZ"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
                Log.w("VoiceManager", "Czech language not supported for TTS, falling back to English")
            }
            isInitialized = true
            
            try {
                // Adjust pitch and rate to sound more feminine and softer
                tts?.setPitch(1.2f)
                tts?.setSpeechRate(0.9f)
            } catch (e: Exception) {
                Log.w("VoiceManager", "Could not set custom voice settings: ${e.message}")
            }
        } else {
            Log.e("VoiceManager", "Initialization Failed!")
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
