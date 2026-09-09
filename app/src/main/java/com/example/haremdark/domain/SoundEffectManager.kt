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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    SECRET_WHISPER
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

object SoundEffectManager {
    private const val TAG = "SoundEffectManager"
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator initialization error: ${e.message}")
        }
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
}
