package com.example.haremdark.domain

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.haremdark.models.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceTriggerType {
    AFFINITY_LEVEL_UP,
    COMBAT_START,
    COMBAT_SPECIAL,
    VICTORY
}

object VoiceManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var toneGenerator: ToneGenerator? = null
    private var pendingSpeech: Pair<String, String?>? = null

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingText = MutableStateFlow<String?>(null)
    val speakingText: StateFlow<String?> = _speakingText.asStateFlow()

    private val _speechRate = MutableStateFlow(0.95f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitchModifier = MutableStateFlow(1.0f)
    val pitchModifier: StateFlow<Float> = _pitchModifier.asStateFlow()

    fun setTtsEnabled(enabled: Boolean) {
        _isTtsEnabled.value = enabled
        if (!enabled) {
            stop()
        }
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.0f)
    }

    fun setPitchModifier(pitch: Float) {
        _pitchModifier.value = pitch.coerceIn(0.5f, 2.0f)
    }

    fun init(context: Context) {
        if (tts == null) {
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error initializing TextToSpeech: ${e.message}")
            }
        }
        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            } catch (e: Exception) {
                Log.w("VoiceManager", "ToneGenerator init error: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val czechLocale = Locale("cs", "CZ")
                val result = tts?.setLanguage(czechLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val defResult = tts?.setLanguage(Locale.getDefault())
                    if (defResult == TextToSpeech.LANG_MISSING_DATA || defResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                        Log.w("VoiceManager", "Czech language missing in TTS, fallback to English")
                    }
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _speakingText.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _speakingText.value = null
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _speakingText.value = null
                    }
                })

                isInitialized = true
                Log.i("VoiceManager", "TextToSpeech successfully initialized.")

                // Execute any speech queued during init
                pendingSpeech?.let { (text, archetype) ->
                    pendingSpeech = null
                    speak(text, archetype)
                }
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error setting up TTS settings: ${e.message}")
                isInitialized = true
            }
        } else {
            Log.e("VoiceManager", "TextToSpeech initialization failed with status: $status")
        }
    }

    fun playAudioClip(isCombat: Boolean) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            }
            if (isCombat) {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 260)
            } else {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 260)
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Tone play error: ${e.message}")
        }
    }

    fun cleanTextForSpeech(raw: String): String {
        var cleaned = raw
        // Remove emoji glyphs
        cleaned = cleaned.replace(Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "")
        // Remove stage directions in brackets/parentheses/asterisks like [usměje se], (šeptá), *vzdychne*
        cleaned = cleaned.replace(Regex("\\[.*?\\]|\\(.*?\\)|\\*.*?\\*"), " ")
        // Remove special symbols, quotes, bullets
        cleaned = cleaned.replace(Regex("[„“”\"'•★✦✨💖💬🚨⚔️🛡️👑🤝⛓️😨💀]"), " ")
        // Remove colon if it's SpeakerName: "text"
        if (cleaned.contains(":")) {
            val parts = cleaned.split(":", limit = 2)
            if (parts.size > 1 && parts[1].trim().length > 3) {
                cleaned = parts[1]
            }
        }
        // Collapse whitespace
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
        // Truncate if overly long to prevent endless reading
        if (cleaned.length > 250) {
            cleaned = cleaned.take(250).substringBeforeLast(" ") + "."
        }
        return cleaned
    }

    fun applyVoicePitchForArchetype(archetype: String?) {
        if (!isInitialized) return
        try {
            val userPitch = _pitchModifier.value
            val userRate = _speechRate.value

            val (basePitch, baseRate) = when (archetype) {
                "subka" -> Pair(1.35f, 0.92f) // sweet, soft, delicate
                "slechta" -> Pair(1.05f, 0.98f) // proud, poised, aristocratic
                "bojovnice" -> Pair(0.92f, 1.05f) // resolute, brave, energetic
                "intrikanka" -> Pair(1.18f, 0.90f) // seductive, whispering, mysterious
                else -> Pair(1.15f, 0.95f) // default feminine
            }

            tts?.setPitch(basePitch * userPitch)
            tts?.setSpeechRate(baseRate * userRate)
        } catch (e: Exception) {
            Log.w("VoiceManager", "Voice pitch set error: ${e.message}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _speakingText.value = null
        } catch (e: Exception) {
            Log.w("VoiceManager", "Error stopping TTS: ${e.message}")
        }
    }

    fun speak(text: String, archetype: String? = null) {
        if (!_isTtsEnabled.value) return

        val cleaned = cleanTextForSpeech(text)
        if (cleaned.isBlank()) return

        if (!isInitialized) {
            pendingSpeech = Pair(cleaned, archetype)
            return
        }

        try {
            applyVoicePitchForArchetype(archetype)
            tts?.stop()
            val utteranceId = "harem_speech_${System.currentTimeMillis()}"
            _speakingText.value = cleaned
            tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.w("VoiceManager", "Error during TTS speak: ${e.message}")
            _isSpeaking.value = false
            _speakingText.value = null
        }
    }

    fun speakDialogue(speakerName: String?, dialogueText: String, archetype: String? = null) {
        speak(dialogueText, archetype)
    }

    fun previewArchetypeVoice(archetype: String) {
        val sampleLine = when (archetype) {
            "subka" -> "Můj pane, mé srdce bije jen pro tebe! Navždy budu tvou oddanou dívkou."
            "slechta" -> "Má čest i mé srdce patří plně tobě, pane. Společně ovládneme tuto říši."
            "bojovnice" -> "Můj meč a můj život patří tobě, veliteli! Zničíme každého nepřítele!"
            "intrikanka" -> "Mmm, pane... mé intriky a tvá moc jsou dokonalý pár temnoty."
            else -> "Cítím, jak se naše pouto prohloubilo, můj pane."
        }
        speak(sampleLine, archetype)
    }

    fun playTriggerVoice(trigger: VoiceTriggerType, character: Character?, fallbackName: String = "Pán Dominia"): String {
        val isCombat = (trigger == VoiceTriggerType.COMBAT_START || trigger == VoiceTriggerType.COMBAT_SPECIAL)
        playAudioClip(isCombat)

        val voiceLine = when (trigger) {
            VoiceTriggerType.AFFINITY_LEVEL_UP -> {
                when (character?.archetypeId) {
                    "subka" -> listOf(
                        "Můj pane... mé srdce bije jen pro tebe! Cítím, jak se naše pouto prohloubilo.",
                        "Už pro mě na světě není nikdo důležitější než ty, pane. Navždy tvá!",
                        "Každý tvůj pohled mě naplňuje teplem. Naše pouto dosáhlo nové výšiny!"
                    ).random()
                    "slechta" -> listOf(
                        "Dokázal jsi svou vznešenost, pane. Má čest i mé srdce patří plně tobě.",
                        "Naše spojení se stalo nepřemožitelným. Nikdo v říši se nám nepostaví!",
                        "Jsem hrdá, že stojím po tvém boku jako tvá vyvolená dáma."
                    ).random()
                    "bojovnice" -> listOf(
                        "Můj meč a můj život patří tobě, veliteli! S tímto poutem zničíme každého nepřítele!",
                        "Krev a oddanost! Jsem hrdá, že bojuji po tvém boku, můj vládce!",
                        "Cítím tvou sílu ve svých žilách. Budu tvým nejvěrnějším štítem!"
                    ).random()
                    "intrikanka" -> listOf(
                        "Mmm, pane... omotal sis mě kolem prstu víc, než jsem kdy čekala. Naše pouto je mocná zbraň.",
                        "Důvěřuji ti víc než komukoliv jinému. Společně ovládneme celé toto město!",
                        "Mé intriky a tvá moc... jsme dokonalý pár temnoty."
                    ).random()
                    else -> "Cítím, jak se naše pouto prohloubilo, můj pane. Děkuji ti za tvou přízeň."
                }
            }
            VoiceTriggerType.COMBAT_START -> {
                if (character != null) {
                    when (character.archetypeId) {
                        "subka" -> listOf(
                            "Budu tě chránit i za cenu vlastního života, pane! Do boje!",
                            "Seberu veškerou svou odvahu! Pro mého pána a naše dominium!",
                            "Můj pane, zůstaň za mnou, nikdo ti neublíží!"
                        ).random()
                        "slechta" -> listOf(
                            "Poznejte hněv mého rodu a sílu mého pána! Zemřete v prachu!",
                            "Předstoupili jste před svůj osud. Naše moc vás smete!",
                            "Nikdo se nebude protivit vůli našeho trůnu!"
                        ).random()
                        "bojovnice" -> listOf(
                            "Rozsekám každého, kdo se opováží zkřížit cestu mému pánovi! K boji!",
                            "Krev a ocel! Žádné slitování s těmi, kdo stojí proti nám!",
                            "Můj meč žízní po krvi nepřítele! Za Pána Dominia!"
                        ).random()
                        "intrikanka" -> listOf(
                            "Tvoje slabiny jsou zjevné. Skončíš dřív, než si to uvědomíš!",
                            "Další hlupák do pasti. Pán se postará o tvůj rychlý konec.",
                            "Tvá zkáza byla zpečetěna v okamžiku, kdy jsi vkročil sem."
                        ).random()
                        else -> "Za dominium a našeho pána! K boji!"
                    }
                } else {
                    listOf(
                        "Dominium temnoty vás rozdrtí! Připravte se na zkázu!",
                        "Má temná magie a ocel vám nedají žádnou šanci!",
                        "Všichni padnou před mou vůlí! Do boje!"
                    ).random()
                }
            }
            VoiceTriggerType.COMBAT_SPECIAL -> {
                character?.let { com.example.haremdark.data.AffinityData.getRandomActiveDialogue(it) }
                    ?: "Pociť plnou sílu temného úderu!"
            }
            VoiceTriggerType.VICTORY -> {
                character?.let { "${it.name}: „Nepřítel padl k tvým nohám, můj pane!“" }
                    ?: "Vítězství náleží temnému dominiu!"
            }
        }

        speak(voiceLine, character?.archetypeId)
        return voiceLine
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w("VoiceManager", "Error shutting down TTS: ${e.message}")
        }
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // ignore
        }
        isInitialized = false
        _isSpeaking.value = false
        _speakingText.value = null
    }
}
