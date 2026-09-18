package com.example.haremdark.domain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class HaremSound {
    FLIRT,
    GIFT,
    TRAIN,
    SEDUCE,
    AFFINITY_UP,
    SECRET_WHISPER,
    FAIL,
    CRAFTING_SUCCESS,
    SKILL_UNLOCK
}

enum class EventSound {
    EVENT_SUMMON,
    EVENT_CHOICE,
    EVENT_DISMISS,
    COUNTDOWN_PULSE
}

enum class CombatSound {
    COMBAT_START,
    PLAYER_SLASH,
    CRITICAL_HIT,
    DARK_SPELL,
    SHIELD_BLOCK,
    ENEMY_STRIKE,
    BOSS_SPECIAL,
    VICTORY,
    DEFEAT
}

enum class LocationAmbientSound(
    val domainId: String,
    val title: String,
    val description: String,
    val icon: String,
    val weatherTheme: String
) {
    DARK_FOREST(
        domainId = "temny_hvozd",
        title = "Šepot nočního hvozdu",
        description = "Mlžný vánek v korunách stromů a tiché noční šelesty",
        icon = "🌲",
        weatherTheme = "Mlžný hvozd"
    ),
    BLOOD_TAVERN(
        domainId = "hostinec_u_krvave_panny",
        title = "Hostinec U Krvavé Panny",
        description = "Akustická loutna barda, praskání krbu a šum hospody",
        icon = "🍻",
        weatherTheme = "Teplý krb & víno"
    ),
    TEMPLE_RUINS(
        domainId = "ruiny_chramu",
        title = "Ztracený chrám Luny",
        description = "Sakrální mystický chór, éterická rezonance a chrámový zvon",
        icon = "🏛️",
        weatherTheme = "Posvátná mlha"
    ),
    SEWER_UNDERWORLD(
        domainId = "stoky_doupata",
        title = "Podzemní stoky & Doupata",
        description = "Rytmické kapky vody v kryptách a hluboká ozvěna temnoty",
        icon = "🐀",
        weatherTheme = "Kryptová vlhkost"
    ),
    MOONLIT_PORT(
        domainId = "mesicni_pristav",
        title = "Měsíční přístav",
        description = "Šumění mořského příboje, vítr plachet a mlžný maják",
        icon = "🌊",
        weatherTheme = "Příboj & Mlha"
    ),
    MERCENARY_CAMP(
        domainId = "tabor_zoldnerek",
        title = "Tábor Černých Růží",
        description = "Rytmus válečných bubnů a rinčení ocelových čepelí",
        icon = "⚔️",
        weatherTheme = "Válečné ohně"
    ),
    NOBLE_MANOR(
        domainId = "slechticke_panstvi",
        title = "Šlechtické panství",
        description = "Aristokratický cembalový valčík a křišťálový třpyt",
        icon = "🏰",
        weatherTheme = "Palácový lesk"
    ),
    BLOOD_CATACOMBS(
        domainId = "krvave_katakomby",
        title = "Krvavé katakomby",
        description = "Tlukot temného srdce a hluboké okultní basové vibrace",
        icon = "🩸",
        weatherTheme = "Krvavá aura"
    ),
    DEMONIC_ABYSS(
        domainId = "propast_behemoth",
        title = "Trhlina v propasti",
        description = "Hučení lávy, démonický ryk a sálající žár podsvětí",
        icon = "🌋",
        weatherTheme = "Magmatický žár"
    ),
    ASTRAL_CITADEL(
        domainId = "astralni_citadela",
        title = "Astrální citadela",
        description = "Kosmické krystalické zvonkohry a harmonie astrálních sfér",
        icon = "✨",
        weatherTheme = "Hvězdný svit"
    ),
    NYMPH_VALLEY(
        domainId = "zakazane_udoli_nymf",
        title = "Zakázané údolí nymf",
        description = "Rajská nymfí harfa, zurčící pramen a něžný šepot víl",
        icon = "🌸",
        weatherTheme = "Věčné jaro"
    );

    companion object {
        fun fromDomainId(domainId: String): LocationAmbientSound {
            return entries.find { it.domainId == domainId } ?: DARK_FOREST
        }
    }
}

object SoundEffectManager {
    private const val TAG = "SoundEffectManager"
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isAmbientLoopEnabled = MutableStateFlow(true)
    val isAmbientLoopEnabled = _isAmbientLoopEnabled.asStateFlow()

    private val _currentAmbient = MutableStateFlow<LocationAmbientSound?>(null)
    val currentAmbient = _currentAmbient.asStateFlow()

    private val _isAmbientPlaying = MutableStateFlow(false)
    val isAmbientPlaying = _isAmbientPlaying.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private var ambientLoopJob: kotlinx.coroutines.Job? = null
    private var crossfadeJob: kotlinx.coroutines.Job? = null
    private var currentAmbientDomainId: String? = null
    private var targetAmbientDomainId: String? = null
    
    // Volume levels for crossfading (0.0f to 1.0f)
    private val _primaryAmbientVolume = MutableStateFlow(1.0f)
    private val _secondaryAmbientVolume = MutableStateFlow(0.0f)

    private val _bgmVolume = MutableStateFlow(0.8f)
    val bgmVolume = _bgmVolume.asStateFlow()

    private val _sfxVolume = MutableStateFlow(0.8f)
    val sfxVolume = _sfxVolume.asStateFlow()

    private val _voiceVolume = MutableStateFlow(0.8f)
    val voiceVolume = _voiceVolume.asStateFlow()

    fun setBgmVolume(volume: Float) {
        _bgmVolume.value = volume.coerceIn(0f, 1f)
    }

    fun setSfxVolume(volume: Float) {
        _sfxVolume.value = volume.coerceIn(0f, 1f)
    }

    fun setVoiceVolume(volume: Float) {
        _voiceVolume.value = volume.coerceIn(0f, 1f)
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator initialization error: ${e.message}")
        }
    }

    fun toggleAmbientLoop() {
        _isAmbientLoopEnabled.value = !_isAmbientLoopEnabled.value
        if (!_isAmbientLoopEnabled.value) {
            stopAmbientAtmosphere()
        } else {
            currentAmbientDomainId?.let { startAmbientAtmosphereLoop(it) }
        }
    }

    fun setAmbientLoopEnabled(enabled: Boolean) {
        _isAmbientLoopEnabled.value = enabled
        if (!enabled) {
            stopAmbientAtmosphere()
        } else {
            currentAmbientDomainId?.let { startAmbientAtmosphereLoop(it) }
        }
    }

    /**
     * Crossfades to a new location ambient soundscape
     */
    fun crossfadeAmbientAtmosphere(newDomainId: String) {
        if (targetAmbientDomainId == newDomainId) return
        targetAmbientDomainId = newDomainId
        
        crossfadeJob?.cancel()
        crossfadeJob = scope.launch {
            // Smoothly crossfade volumes over 2 seconds
            val steps = 20
            val duration = 2000L
            val stepDelay = duration / steps
            
            val startPrimary = _primaryAmbientVolume.value
            val startSecondary = _secondaryAmbientVolume.value
            
            // Phase 1: Start secondary loop with target sound
            val secondaryAmbient = LocationAmbientSound.fromDomainId(newDomainId)
            
            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                _primaryAmbientVolume.value = startPrimary * (1f - progress)
                _secondaryAmbientVolume.value = progress
                delay(stepDelay)
            }
            
            // Phase 2: Finalize swap
            _currentAmbient.value = secondaryAmbient
            currentAmbientDomainId = newDomainId
            _primaryAmbientVolume.value = 1.0f
            _secondaryAmbientVolume.value = 0.0f
            
            // Ensure the main loop is running with the new domain
            startAmbientAtmosphereLoop(newDomainId)
        }
    }

    fun playLocationAmbient(domainId: String, force: Boolean = false, volumeMultiplier: Float = 1.0f) {
        val ambient = LocationAmbientSound.fromDomainId(domainId)
        if (!force) {
            _currentAmbient.value = ambient
            currentAmbientDomainId = domainId
        }

        if (_isMuted.value) return
        if (_isAmbientPlaying.value && !force) return

        scope.launch {
            if (!force) _isAmbientPlaying.value = true
            try {
                val samples = when (ambient) {
                    LocationAmbientSound.DARK_FOREST -> synthesizeDarkForestAmbient()
                    LocationAmbientSound.BLOOD_TAVERN -> synthesizeTavernAmbient()
                    LocationAmbientSound.TEMPLE_RUINS -> synthesizeTempleRuinsAmbient()
                    LocationAmbientSound.SEWER_UNDERWORLD -> synthesizeSewerAmbient()
                    LocationAmbientSound.MOONLIT_PORT -> synthesizeMoonlitPortAmbient()
                    LocationAmbientSound.MERCENARY_CAMP -> synthesizeMercenaryCampAmbient()
                    LocationAmbientSound.NOBLE_MANOR -> synthesizeNobleManorAmbient()
                    LocationAmbientSound.BLOOD_CATACOMBS -> synthesizeBloodCatacombsAmbient()
                    LocationAmbientSound.DEMONIC_ABYSS -> synthesizeDemonicAbyssAmbient()
                    LocationAmbientSound.ASTRAL_CITADEL -> synthesizeAstralCitadelAmbient()
                    LocationAmbientSound.NYMPH_VALLEY -> synthesizeNymphValleyAmbient()
                }
                
                // Apply volume multiplier to samples for crossfading
                if (volumeMultiplier < 0.99f) {
                    for (i in samples.indices) {
                        samples[i] = (samples[i] * volumeMultiplier).toInt().toShort()
                    }
                }
                
                playPcmTrack(samples)
            } catch (e: Exception) {
                Log.w(TAG, "playLocationAmbient error: ${e.message}")
            } finally {
                if (!force) _isAmbientPlaying.value = false
            }
        }
    }

    fun startAmbientAtmosphereLoop(domainId: String) {
        if (currentAmbientDomainId == domainId && ambientLoopJob?.isActive == true) return
        
        val ambient = LocationAmbientSound.fromDomainId(domainId)
        _currentAmbient.value = ambient
        currentAmbientDomainId = domainId
        targetAmbientDomainId = domainId

        ambientLoopJob?.cancel()
        if (!_isAmbientLoopEnabled.value || _isMuted.value) return

        ambientLoopJob = scope.launch {
            while (isActive && _isAmbientLoopEnabled.value && !_isMuted.value) {
                // Use primary volume for the active loop
                playLocationAmbient(domainId, force = true, volumeMultiplier = _primaryAmbientVolume.value)
                
                // Wait between atmospheric sound pulses
                delay(8000L)
            }
        }
    }

    fun stopAmbientAtmosphere() {
        ambientLoopJob?.cancel()
        ambientLoopJob = null
        crossfadeJob?.cancel()
        crossfadeJob = null
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
    }

    fun playEventTrigger() {
        playEvent(EventSound.EVENT_SUMMON)
    }

    fun playAffinityGain() {
        playHarem(HaremSound.AFFINITY_UP)
    }

    fun playLevelUp() {
        playHarem(HaremSound.AFFINITY_UP)
    }

    fun playIntimacyFx() {
        playHarem(HaremSound.SEDUCE)
    }

    fun playMoodFeedback(mood: String, contextType: String = "interaction") {
        if (_isMuted.value) return
        scope.launch {
            try {
                val isTraining = contextType == "training"
                val toneFreq = if (isTraining) {
                    when (mood) {
                        "Disciplinovaná", "Soustředěná", "Pozorná" -> 880.0 // Higher pitch for training success
                        "Zmatená", "Váhavá" -> 440.0 // Low pitch for training fail
                        else -> 660.0
                    }
                } else {
                    // Praise or interaction
                    when (mood) {
                        "Šťastná", "Veselá", "Nadšená", "Extatická", "Oddaná", "Uvolněná", "Potěšená" -> 1046.5 // High chime for happiness
                        "Zklamaná", "Smutná", "Depresivní", "Ponížená" -> 349.23 // Lower, sadder pitch
                        "Vzrušená", "Toužebná", "Poddajná" -> 987.77 // Sweeter pitch
                        "Naštvaná", "Vzdorná", "Agresivní", "Rozzlobená" -> 220.0 // Harsh low
                        else -> 523.25
                    }
                }
                
                val durationSec = if (isTraining) 0.5 else 1.0
                val count = (SAMPLE_RATE * durationSec).toInt()
                val buffer = ShortArray(count)
                for (i in 0 until count) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = kotlin.math.exp(-5.0 * t)
                    val freq = toneFreq + (if (isTraining) 0.0 else kotlin.math.sin(2.0 * kotlin.math.PI * 5.0 * t) * 10.0) // Vibrato for non-training
                    val total = (kotlin.math.sin(2.0 * kotlin.math.PI * freq * t) * 0.7 + kotlin.math.sin(4.0 * kotlin.math.PI * freq * t) * 0.3) * env * Short.MAX_VALUE * 0.5
                    buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcmTrack(buffer)
            } catch (e: Exception) {
                fallbackTone(ToneGenerator.TONE_PROP_BEEP, 150)
            }
        }
    }

    fun playHarem(sound: HaremSound) {
        if (_isMuted.value) return
        scope.launch {
            try {
                when (sound) {
                    HaremSound.FLIRT -> {
                        // Ascending gentle bells (880Hz -> 1174Hz)
                        playPcmTrack(synthesizeArpeggio(listOf(880.0, 1174.0), 0.14, 0.7f))
                    }
                    HaremSound.GIFT -> {
                        // Sparkling coins / jewel cascade (1046Hz -> 1318Hz -> 1568Hz)
                        playPcmTrack(synthesizeArpeggio(listOf(1046.5, 1318.5, 1568.0, 2093.0), 0.09, 0.75f))
                    }
                    HaremSound.TRAIN -> {
                        // Resonant low strike with decaying impact
                        playPcmTrack(synthesizeStrike(baseFreq = 160.0, durationSec = 0.28, decayRate = 18.0))
                    }
                    HaremSound.SEDUCE -> {
                        // Warm sensual harmonic chord (A minor / mysterious harmonic 440 + 523 + 659)
                        playPcmTrack(synthesizeChord(listOf(440.0, 523.25, 659.25), 0.45, 0.65f))
                    }
                    HaremSound.AFFINITY_UP -> {
                        // Triumphant ascending royal fanfare
                        playPcmTrack(synthesizeFanfare(listOf(523.25, 659.25, 783.99, 1046.5), 0.16))
                    }
                    HaremSound.SECRET_WHISPER -> {
                        playPcmTrack(synthesizeArpeggio(listOf(740.0, 880.0, 1108.7), 0.12, 0.5f))
                    }
                    HaremSound.FAIL -> {
                        // Low descending failure tone (330Hz -> 220Hz)
                        playPcmTrack(synthesizeDescending(329.6, 220.0, 0.35))
                    }
                    HaremSound.CRAFTING_SUCCESS -> {
                        // Resonant success chime
                        playPcmTrack(synthesizeArpeggio(listOf(523.25, 659.25, 783.99, 1046.5), 0.1, 0.7f))
                    }
                    HaremSound.SKILL_UNLOCK -> {
                        // Triumphant chime for skill unlock
                        playPcmTrack(synthesizeFanfare(listOf(659.25, 783.99, 1046.5, 1318.51), 0.15))
                    }
                }
            } catch (e: Exception) {
                fallbackTone(ToneGenerator.TONE_PROP_BEEP, 150)
            }
        }
    }

    fun playEvent(sound: EventSound) {
        if (_isMuted.value) return
        scope.launch {
            try {
                when (sound) {
                    EventSound.EVENT_SUMMON -> {
                        // Urgent ominous brass herald alert (587Hz -> 784Hz -> 880Hz)
                        playPcmTrack(synthesizeHeraldAlert())
                    }
                    EventSound.EVENT_CHOICE -> {
                        // Resonant affirmative chime (523Hz -> 659Hz)
                        playPcmTrack(synthesizeArpeggio(listOf(523.25, 659.25), 0.18, 0.8f))
                    }
                    EventSound.EVENT_DISMISS -> {
                        // Soft descending tone (440Hz -> 330Hz)
                        playPcmTrack(synthesizeDescending(440.0, 329.6, 0.2))
                    }
                    EventSound.COUNTDOWN_PULSE -> {
                        playPcmTrack(synthesizeTone(880.0, 0.08, 0.4f))
                    }
                }
            } catch (e: Exception) {
                fallbackTone(ToneGenerator.TONE_PROP_PROMPT, 180)
            }
        }
    }

    fun playCombat(sound: CombatSound) {
        if (_isMuted.value) return
        scope.launch {
            try {
                when (sound) {
                    CombatSound.COMBAT_START -> {
                        // Battle horn (low 220Hz into 293Hz swell)
                        playPcmTrack(synthesizeBattleHorn())
                    }
                    CombatSound.PLAYER_SLASH -> {
                        // Quick blade whoosh + sharp metallic impact
                        playPcmTrack(synthesizeSlash())
                    }
                    CombatSound.CRITICAL_HIT -> {
                        // Heavy explosive crunch & shockwave
                        playPcmTrack(synthesizeCriticalImpact())
                    }
                    CombatSound.DARK_SPELL -> {
                        // Ominous arcane pulse with rising eerie harmonics
                        playPcmTrack(synthesizeDarkSpell())
                    }
                    CombatSound.SHIELD_BLOCK -> {
                        // Sharp metallic shield clang
                        playPcmTrack(synthesizeShieldClang())
                    }
                    CombatSound.ENEMY_STRIKE -> {
                        // Heavy blunt thud
                        playPcmTrack(synthesizeStrike(baseFreq = 110.0, durationSec = 0.22, decayRate = 16.0))
                    }
                    CombatSound.BOSS_SPECIAL -> {
                        // Menacing multi-tone alarm / dark siren
                        playPcmTrack(synthesizeBossAlarm())
                    }
                    CombatSound.VICTORY -> {
                        // Victory fanfare chord progression
                        playPcmTrack(synthesizeFanfare(listOf(523.25, 659.25, 783.99, 1046.5), 0.22))
                    }
                    CombatSound.DEFEAT -> {
                        // Descending somber minor chords
                        playPcmTrack(synthesizeDescending(392.0, 220.0, 0.6))
                    }
                }
            } catch (e: Exception) {
                fallbackTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 220)
            }
        }
    }

    private fun fallbackTone(toneType: Int, durationMs: Int) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            }
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.w(TAG, "Fallback tone error: ${e.message}")
        }
    }

    private fun playPcmTrack(samples: ShortArray) {
        if (samples.isEmpty()) return
        var track: AudioTrack? = null
        try {
            val bufferSize = samples.size * 2
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()

            // Wait until played before release in coroutine
            val durationMs = (samples.size.toDouble() / SAMPLE_RATE * 1000).toLong() + 50
            Thread.sleep(durationMs.coerceIn(50L, 1200L))
        } catch (e: Exception) {
            Log.w(TAG, "AudioTrack play error: ${e.message}")
            fallbackTone(ToneGenerator.TONE_PROP_ACK, 120)
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (ignored: Exception) {}
        }
    }

    // --- SYNTHESIZERS ---

    private fun synthesizeTone(freq: Double, durationSec: Double, volume: Float = 0.8f): ShortArray {
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = (1.0 - (i.toDouble() / count)) // linear decay
            val sample = sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * volume
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeArpeggio(notes: List<Double>, noteDurationSec: Double, volume: Float = 0.7f): ShortArray {
        val noteSamples = (SAMPLE_RATE * noteDurationSec).toInt()
        val totalCount = noteSamples * notes.size
        val buffer = ShortArray(totalCount)

        for (n in notes.indices) {
            val freq = notes[n]
            val offset = n * noteSamples
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = exp(-5.0 * (i.toDouble() / noteSamples))
                val sample = sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * volume
                buffer[offset + i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return buffer
    }

    private fun synthesizeChord(notes: List<Double>, durationSec: Double, volume: Float = 0.6f): ShortArray {
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-3.0 * (i.toDouble() / count))
            var sum = 0.0
            for (freq in notes) {
                sum += sin(2.0 * PI * freq * t)
            }
            sum = (sum / notes.size) * envelope * Short.MAX_VALUE * volume
            buffer[i] = sum.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeFanfare(notes: List<Double>, noteDurationSec: Double): ShortArray {
        val noteSamples = (SAMPLE_RATE * noteDurationSec).toInt()
        val lastNoteSamples = (noteSamples * 2.2).toInt()
        val totalCount = noteSamples * (notes.size - 1) + lastNoteSamples
        val buffer = ShortArray(totalCount)

        var offset = 0
        for (n in notes.indices) {
            val freq = notes[n]
            val currentNoteCount = if (n == notes.lastIndex) lastNoteSamples else noteSamples
            for (i in 0 until currentNoteCount) {
                val t = i.toDouble() / SAMPLE_RATE
                val decayFactor = if (n == notes.lastIndex) 2.0 else 4.0
                val envelope = exp(-decayFactor * (i.toDouble() / currentNoteCount))
                // Add harmonic rich overtone
                val sample = (sin(2.0 * PI * freq * t) * 0.75 + sin(4.0 * PI * freq * t) * 0.25) *
                        envelope * Short.MAX_VALUE * 0.75
                buffer[offset + i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            offset += currentNoteCount
        }
        return buffer
    }

    private fun synthesizeStrike(baseFreq: Double, durationSec: Double, decayRate: Double): ShortArray {
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-decayRate * (i.toDouble() / count))
            val pitchDrop = baseFreq * (1.0 - 0.4 * (i.toDouble() / count))
            val sample = (sin(2.0 * PI * pitchDrop * t) + 0.3 * (Math.random() * 2.0 - 1.0)) *
                    envelope * Short.MAX_VALUE * 0.85
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeDescending(startFreq: Double, endFreq: Double, durationSec: Double): ShortArray {
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = 1.0 - progress
            val sample = sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.7
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeSlash(): ShortArray {
        // Fast white noise burst blended into high-frequency metallic slice
        val durationSec = 0.2
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val envelope = exp(-12.0 * progress)
            val freq = 1200.0 - 800.0 * progress
            val noise = (Math.random() * 2.0 - 1.0) * 0.6
            val tone = sin(2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)) * 0.4
            val sample = (noise + tone) * envelope * Short.MAX_VALUE * 0.9
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeCriticalImpact(): ShortArray {
        // Heavy low boom + high metallic shatter
        val durationSec = 0.35
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val envelope = exp(-8.0 * progress)
            val subBoom = sin(2.0 * PI * (90.0 - 40.0 * progress) * (i.toDouble() / SAMPLE_RATE)) * 0.7
            val crunch = (Math.random() * 2.0 - 1.0) * 0.35 * exp(-18.0 * progress)
            val sample = (subBoom + crunch) * envelope * Short.MAX_VALUE * 0.95
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeDarkSpell(): ShortArray {
        // Eerie pulsating harmonic sweep
        val durationSec = 0.38
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = sin(PI * progress) // bell curve
            val wobble = sin(2.0 * PI * 14.0 * t) * 35.0
            val base = sin(2.0 * PI * (220.0 + 160.0 * progress + wobble) * t) * 0.6
            val highHarmonic = sin(2.0 * PI * (660.0 + 320.0 * progress) * t) * 0.4
            val sample = (base + highHarmonic) * envelope * Short.MAX_VALUE * 0.8
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeShieldClang(): ShortArray {
        // High-pitched metallic ring with fast decay
        val durationSec = 0.25
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-14.0 * progress)
            val clang = (sin(2.0 * PI * 1650.0 * t) * 0.5 + sin(2.0 * PI * 2400.0 * t) * 0.3 + sin(2.0 * PI * 850.0 * t) * 0.2)
            val sample = clang * envelope * Short.MAX_VALUE * 0.85
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeHeraldAlert(): ShortArray {
        // Alert chime: 587Hz -> 784Hz -> 880Hz rapid herald call
        return synthesizeArpeggio(listOf(587.33, 783.99, 880.0), 0.12, 0.85f)
    }

    private fun synthesizeBattleHorn(): ShortArray {
        // Ominous brass swell
        val durationSec = 0.5
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = if (progress < 0.2) (progress / 0.2) else exp(-2.5 * (progress - 0.2))
            val freq = 220.0 + 73.0 * progress
            val tone = (sin(2.0 * PI * freq * t) * 0.65 + sin(4.0 * PI * freq * t) * 0.35)
            val sample = tone * envelope * Short.MAX_VALUE * 0.8
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeBossAlarm(): ShortArray {
        // Dissonant dual alarm
        val durationSec = 0.35
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = 1.0 - progress
            val alarm = sin(2.0 * PI * 440.0 * t) * 0.5 + sin(2.0 * PI * 466.16 * t) * 0.5 // minor 2nd clash
            val sample = alarm * envelope * Short.MAX_VALUE * 0.85
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    // --- LOCATION AMBIENT SOUNDSCAPES ---

    private fun synthesizeDarkForestAmbient(): ShortArray {
        val durationSec = 1.3
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            val env = sin(PI * progress) // smooth fade in/out
            // Rustling wind filtered noise
            val windNoise = (Math.random() * 2.0 - 1.0) * 0.25 * (sin(2.0 * PI * 1.5 * t) * 0.5 + 0.5)
            // Eerie nocturnal flute (A4 -> C5 gently fluctuating)
            val woodwind = sin(2.0 * PI * (440.0 + sin(2.0 * PI * 3.0 * t) * 15.0) * t) * 0.4
            // Night cricket trill in the background
            val cricket = if ((t * 8).toInt() % 2 == 0) sin(2.0 * PI * 3600.0 * t) * 0.15 else 0.0
            val total = (windNoise + woodwind + cricket) * env * Short.MAX_VALUE * 0.75
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeTavernAmbient(): ShortArray {
        val durationSec = 1.2
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val luteNotes = listOf(293.66, 349.23, 440.0, 587.33) // D minor lute strum
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            var luteSum = 0.0
            for (idx in luteNotes.indices) {
                val noteOffset = idx * 0.12
                if (t >= noteOffset) {
                    val noteT = t - noteOffset
                    val noteEnv = exp(-6.0 * noteT)
                    val freq = luteNotes[idx]
                    // Pluck harmonic
                    luteSum += (sin(2.0 * PI * freq * noteT) * 0.6 + sin(4.0 * PI * freq * noteT) * 0.3 + sin(6.0 * PI * freq * noteT) * 0.1) * noteEnv
                }
            }
            // Tavern glass clink at t = 0.6s
            var clink = 0.0
            if (t in 0.6..0.9) {
                val cT = t - 0.6
                clink = sin(2.0 * PI * 2200.0 * cT) * exp(-20.0 * cT) * 0.4
            }
            val env = if (progress < 0.9) 1.0 else (1.0 - progress) / 0.1
            val total = (luteSum * 0.3 + clink) * env * Short.MAX_VALUE * 0.8
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeTempleRuinsAmbient(): ShortArray {
        val durationSec = 1.5
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val choirFreqs = listOf(110.0, 164.81, 220.0, 329.63) // Sacred E minor / A drone
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            val env = sin(PI * progress) // bell curve
            var choir = 0.0
            for (f in choirFreqs) {
                choir += sin(2.0 * PI * f * t) * 0.25
            }
            // Crystal chime bell ping at t = 0.2s
            var bell = 0.0
            if (t >= 0.2) {
                val bT = t - 0.2
                bell = (sin(2.0 * PI * 1760.0 * bT) * 0.5 + sin(2.0 * PI * 2640.0 * bT) * 0.3) * exp(-3.5 * bT)
            }
            val total = (choir * 0.6 + bell * 0.4) * env * Short.MAX_VALUE * 0.8
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeSewerAmbient(): ShortArray {
        val durationSec = 1.2
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val dropTimes = listOf(0.1, 0.45, 0.8)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            // Low cavern hum
            val cavernHum = sin(2.0 * PI * 70.0 * t) * 0.15
            var drops = 0.0
            for (dt in dropTimes) {
                if (t >= dt) {
                    val dropT = t - dt
                    val dropFreq = 1800.0 - 1000.0 * (dropT * 8.0).coerceAtMost(1.0)
                    val dropEnv = exp(-18.0 * dropT)
                    val echo = exp(-6.0 * dropT) * sin(2.0 * PI * 440.0 * dropT) * 0.2
                    drops += (sin(2.0 * PI * dropFreq * dropT) * dropEnv + echo) * 0.4
                }
            }
            val total = (cavernHum + drops) * Short.MAX_VALUE * 0.85
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeMoonlitPortAmbient(): ShortArray {
        val durationSec = 1.4
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            // Ocean wave swell
            val swellEnv = sin(PI * progress)
            val waveNoise = (Math.random() * 2.0 - 1.0) * 0.3 * swellEnv
            // Distant foghorn tone at 146Hz
            val foghorn = if (progress in 0.2..0.8) {
                sin(2.0 * PI * 146.83 * t) * 0.35 * sin(PI * ((progress - 0.2) / 0.6))
            } else 0.0
            // Port buoy bell ping
            val bell = if (t >= 0.7) {
                sin(2.0 * PI * 1046.5 * (t - 0.7)) * exp(-7.0 * (t - 0.7)) * 0.25
            } else 0.0
            val total = (waveNoise + foghorn + bell) * Short.MAX_VALUE * 0.8
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeMercenaryCampAmbient(): ShortArray {
        val durationSec = 1.2
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val drumHits = listOf(0.05, 0.45, 0.85)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var drums = 0.0
            for (dh in drumHits) {
                if (t >= dh) {
                    val dT = t - dh
                    val drumFreq = 65.0 - 20.0 * (dT * 5.0).coerceAtMost(1.0)
                    drums += sin(2.0 * PI * drumFreq * dT) * exp(-9.0 * dT) * 0.6
                }
            }
            // Blade clang at t = 0.3s
            val blade = if (t >= 0.3) {
                val bT = t - 0.3
                (sin(2.0 * PI * 2200.0 * bT) * 0.5 + sin(2.0 * PI * 2900.0 * bT) * 0.3) * exp(-16.0 * bT) * 0.35
            } else 0.0
            val total = (drums + blade) * Short.MAX_VALUE * 0.85
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeNobleManorAmbient(): ShortArray {
        val durationSec = 1.3
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        // Baroque Harpsichord arpeggio (C Major 7: C5, E5, G5, B5, C6)
        val harpsiNotes = listOf(523.25, 659.25, 783.99, 987.77, 1046.5)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var harpSum = 0.0
            for (idx in harpsiNotes.indices) {
                val noteOffset = idx * 0.14
                if (t >= noteOffset) {
                    val nT = t - noteOffset
                    val nEnv = exp(-7.0 * nT)
                    val freq = harpsiNotes[idx]
                    // Bright plucky timbre with 2nd and 3rd harmonics
                    harpSum += (sin(2.0 * PI * freq * nT) * 0.5 + sin(4.0 * PI * freq * nT) * 0.3 + sin(6.0 * PI * freq * nT) * 0.2) * nEnv
                }
            }
            val total = harpSum * 0.4 * Short.MAX_VALUE * 0.85
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeBloodCatacombsAmbient(): ShortArray {
        val durationSec = 1.3
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val heartbeatTimes = listOf(0.1, 0.35, 0.75, 1.0)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            // Dark occult sub drone
            val occultDrone = (sin(2.0 * PI * 55.0 * t) * 0.3 + sin(2.0 * PI * 110.0 * t) * 0.2) * sin(PI * (i.toDouble() / count))
            var heartbeat = 0.0
            for (hT in heartbeatTimes) {
                if (t >= hT) {
                    val dt = t - hT
                    val hbFreq = 50.0 - 15.0 * (dt * 10.0).coerceAtMost(1.0)
                    heartbeat += sin(2.0 * PI * hbFreq * dt) * exp(-12.0 * dt) * 0.55
                }
            }
            val total = (occultDrone + heartbeat) * Short.MAX_VALUE * 0.85
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeDemonicAbyssAmbient(): ShortArray {
        val durationSec = 1.3
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            val env = sin(PI * progress)
            // Infernal lava rumble noise
            val magmaRumble = (Math.random() * 2.0 - 1.0) * 0.3 * (sin(2.0 * PI * 3.0 * t) * 0.5 + 0.5)
            // Demonic sub bass roar (80Hz to 55Hz pitch bend)
            val roarFreq = 85.0 - 30.0 * progress
            val subRoar = (sin(2.0 * PI * roarFreq * t) * 0.5 + sin(4.0 * PI * roarFreq * t) * 0.25)
            val total = (magmaRumble + subRoar * 0.5) * env * Short.MAX_VALUE * 0.85
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeAstralCitadelAmbient(): ShortArray {
        val durationSec = 1.4
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val astralNotes = listOf(659.25, 830.61, 987.77, 1318.51, 1661.22) // Celestial Pentatonic
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var celestialSum = 0.0
            for (idx in astralNotes.indices) {
                val nOffset = idx * 0.12
                if (t >= nOffset) {
                    val nT = t - nOffset
                    val shimmer = sin(2.0 * PI * 18.0 * nT) * 0.2 + 0.8 // shimmering twinkle
                    val nEnv = exp(-4.0 * nT) * shimmer
                    celestialSum += sin(2.0 * PI * astralNotes[idx] * nT) * nEnv
                }
            }
            val total = celestialSum * 0.35 * Short.MAX_VALUE * 0.8
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeNymphValleyAmbient(): ShortArray {
        val durationSec = 1.4
        val count = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(count)
        val harpNotes = listOf(349.23, 440.0, 523.25, 659.25, 783.99, 1046.5) // F Maj 9 glissando
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var harpSum = 0.0
            for (idx in harpNotes.indices) {
                val nOffset = idx * 0.1
                if (t >= nOffset) {
                    val nT = t - nOffset
                    val nEnv = exp(-5.0 * nT)
                    harpSum += (sin(2.0 * PI * harpNotes[idx] * nT) * 0.7 + sin(4.0 * PI * harpNotes[idx] * nT) * 0.3) * nEnv
                }
            }
            // Gentle bird chirp at t = 0.7s
            val bird = if (t in 0.7..1.0) {
                val bT = t - 0.7
                val birdFreq = 2800.0 + sin(2.0 * PI * 35.0 * bT) * 500.0
                sin(2.0 * PI * birdFreq * bT) * exp(-10.0 * bT) * 0.3
            } else 0.0
            val total = (harpSum * 0.35 + bird) * Short.MAX_VALUE * 0.8
            buffer[i] = total.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
