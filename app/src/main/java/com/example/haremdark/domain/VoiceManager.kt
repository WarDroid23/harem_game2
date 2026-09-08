package com.example.haremdark.domain

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.haremdark.models.Character
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

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
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
            val result = tts?.setLanguage(Locale("cs", "CZ"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
                Log.w("VoiceManager", "Czech language not supported for TTS, falling back to English")
            }
            isInitialized = true
            
            try {
                // Adjust pitch and rate to sound feminine and clear
                tts?.setPitch(1.2f)
                tts?.setSpeechRate(0.95f)
            } catch (e: Exception) {
                Log.w("VoiceManager", "Could not set custom voice settings: ${e.message}")
            }
        } else {
            Log.e("VoiceManager", "Initialization Failed!")
        }
    }

    fun playAudioClip(isCombat: Boolean) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            }
            if (isCombat) {
                // Battle horn/alert sound for combat encounter
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 260)
            } else {
                // Melodic pleasant chime for affinity level up
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 260)
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Tone play error: ${e.message}")
        }
    }

    fun applyVoicePitchForArchetype(archetype: String?) {
        if (!isInitialized) return
        try {
            when (archetype) {
                "subka" -> {
                    tts?.setPitch(1.35f) // high, soft, sweet
                    tts?.setSpeechRate(0.92f)
                }
                "slechta" -> {
                    tts?.setPitch(1.05f) // proud, commanding, steady
                    tts?.setSpeechRate(0.98f)
                }
                "bojovnice" -> {
                    tts?.setPitch(0.92f) // fierce, resolute
                    tts?.setSpeechRate(1.05f)
                }
                "intrikanka" -> {
                    tts?.setPitch(1.15f) // whispering, seductive, cunning
                    tts?.setSpeechRate(0.92f)
                }
                else -> {
                    tts?.setPitch(1.15f)
                    tts?.setSpeechRate(0.95f)
                }
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Voice pitch set error: ${e.message}")
        }
    }

    fun speak(text: String, archetype: String? = null) {
        if (isInitialized) {
            applyVoicePitchForArchetype(archetype)
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
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
        tts?.stop()
        tts?.shutdown()
        tts = null
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // ignore
        }
        isInitialized = false
    }
}
