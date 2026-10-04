package com.example.haremdark.ui.sound

import android.content.Context
import androidx.compose.runtime.*
import com.example.haremdark.domain.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Sound triggers for combat actions and tactical feedback
 */
sealed class CombatSoundTrigger(val displayName: String, val icon: String) {
    object Start : CombatSoundTrigger("Zahájení bitvy", "⚔️")
    data class MeleeSlash(val isCrit: Boolean = false) : CombatSoundTrigger(if (isCrit) "Kritický sek" else "Útok čepelí", "🗡️")
    object DarkSpell : CombatSoundTrigger("Temná magie", "🔮")
    object ShieldBlock : CombatSoundTrigger("Blok štítem", "🛡️")
    object CriticalSupernova : CombatSoundTrigger("Supernova kritický zásah", "💥")
    object SkillActivation : CombatSoundTrigger("Aktivace dovednosti", "✨")
    object BleedTrigger : CombatSoundTrigger("Krvácení", "🩸")
    object PoisonTrigger : CombatSoundTrigger("Jedovatý efekt", "🧪")
    object HealRestore : CombatSoundTrigger("Léčení & Obnova", "💚")
    object Dodge : CombatSoundTrigger("Úhyb / Uhnutí", "💨")
    object Victory : CombatSoundTrigger("Vítězství", "🏆")
    object Defeat : CombatSoundTrigger("Porážka", "💀")
}

/**
 * Sound triggers for narrative events, choices, rewards and interactions
 */
sealed class EventSoundTrigger(val displayName: String, val icon: String) {
    object NarrativeSummon : EventSoundTrigger("Příchod události", "📜")
    object ChoiceSelected : EventSoundTrigger("Výběr možnosti", "✨")
    object EventDismissed : EventSoundTrigger("Zavření události", "✖️")
    object CountdownPulse : EventSoundTrigger("Časový puls", "⏳")
    object AffinityGain : EventSoundTrigger("Zvýšení náklonnosti", "💖")
    object LevelUp : EventSoundTrigger("Nová úroveň", "⭐")
    object RewardGained : EventSoundTrigger("Zisk odměny", "🎁")
    object SkillUnlocked : EventSoundTrigger("Odemčení dovednosti", "🔓")
    object CraftingSuccess : EventSoundTrigger("Úspěšná výroba", "🔨")
}

/**
 * Simple SoundManager for Jetpack Compose.
 * Wraps background ambient tracks, combat sound effects, event triggers, and volume states.
 */
@Stable
class ComposeSoundManager(
    private val appContext: Context? = null
) {
    val isMuted: StateFlow<Boolean> = SoundEffectManager.isMuted
    val isAmbientPlaying: StateFlow<Boolean> = SoundEffectManager.isAmbientPlaying
    val isAmbientLoopEnabled: StateFlow<Boolean> = SoundEffectManager.isAmbientLoopEnabled
    val currentAmbient: StateFlow<LocationAmbientSound?> = SoundEffectManager.currentAmbient
    val bgmVolume: StateFlow<Float> = SoundEffectManager.bgmVolume
    val sfxVolume: StateFlow<Float> = SoundEffectManager.sfxVolume
    val voiceVolume: StateFlow<Float> = SoundEffectManager.voiceVolume

    private val _lastTriggeredName = mutableStateOf<String?>(null)
    val lastTriggeredName: State<String?> = _lastTriggeredName

    init {
        appContext?.let { SoundEffectManager.init(it) }
    }

    /**
     * Plays or crossfades into a background ambient soundtrack
     */
    fun playAmbient(track: LocationAmbientSound, crossfade: Boolean = true) {
        _lastTriggeredName.value = "Hudba: ${track.title}"
        if (crossfade) {
            SoundEffectManager.crossfadeAmbientAtmosphere(track.domainId)
        } else {
            SoundEffectManager.playLocationAmbient(track.domainId)
        }
    }

    /**
     * Stops the background ambient soundtrack
     */
    fun stopAmbient() {
        _lastTriggeredName.value = "Atmosféra zastavena"
        SoundEffectManager.stopAmbientAtmosphere()
    }

    /**
     * Toggles ambient atmosphere looping
     */
    fun toggleAmbientLoop() {
        SoundEffectManager.toggleAmbientLoop()
    }

    /**
     * Plays a combat sound effect
     */
    fun playCombatSound(trigger: CombatSoundTrigger) {
        _lastTriggeredName.value = "${trigger.icon} ${trigger.displayName}"
        when (trigger) {
            is CombatSoundTrigger.Start -> SoundEffectManager.playCombat(CombatSound.COMBAT_START)
            is CombatSoundTrigger.MeleeSlash -> {
                if (trigger.isCrit) {
                    SoundEffectManager.playCombat(CombatSound.CRITICAL_HIT)
                } else {
                    SoundEffectManager.playCombat(CombatSound.PLAYER_SLASH)
                }
            }
            is CombatSoundTrigger.DarkSpell -> SoundEffectManager.playCombat(CombatSound.DARK_SPELL)
            is CombatSoundTrigger.ShieldBlock -> SoundEffectManager.playCombat(CombatSound.SHIELD_BLOCK)
            is CombatSoundTrigger.CriticalSupernova -> SoundEffectManager.playCombat(CombatSound.CRITICAL_SUPERNOVA)
            is CombatSoundTrigger.SkillActivation -> SoundEffectManager.playCombat(CombatSound.SKILL_ACTIVATION)
            is CombatSoundTrigger.BleedTrigger -> SoundEffectManager.playCombat(CombatSound.STATUS_TRIGGER_BLEED)
            is CombatSoundTrigger.PoisonTrigger -> SoundEffectManager.playCombat(CombatSound.STATUS_TRIGGER_POISON)
            is CombatSoundTrigger.HealRestore -> SoundEffectManager.playCombat(CombatSound.HEAL_RESTORE)
            is CombatSoundTrigger.Dodge -> SoundEffectManager.playCombat(CombatSound.DODGE_EVADE)
            is CombatSoundTrigger.Victory -> SoundEffectManager.playCombat(CombatSound.VICTORY)
            is CombatSoundTrigger.Defeat -> SoundEffectManager.playCombat(CombatSound.DEFEAT)
        }
    }

    /**
     * Plays an event or narrative interaction sound effect
     */
    fun playEventSound(trigger: EventSoundTrigger) {
        _lastTriggeredName.value = "${trigger.icon} ${trigger.displayName}"
        when (trigger) {
            is EventSoundTrigger.NarrativeSummon -> SoundEffectManager.playEvent(EventSound.EVENT_SUMMON)
            is EventSoundTrigger.ChoiceSelected -> SoundEffectManager.playEvent(EventSound.EVENT_CHOICE)
            is EventSoundTrigger.EventDismissed -> SoundEffectManager.playEvent(EventSound.EVENT_DISMISS)
            is EventSoundTrigger.CountdownPulse -> SoundEffectManager.playEvent(EventSound.COUNTDOWN_PULSE)
            is EventSoundTrigger.AffinityGain -> SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
            is EventSoundTrigger.LevelUp -> SoundEffectManager.playLevelUp()
            is EventSoundTrigger.RewardGained -> SoundEffectManager.playHarem(HaremSound.GIFT)
            is EventSoundTrigger.SkillUnlocked -> SoundEffectManager.playHarem(HaremSound.SKILL_UNLOCK)
            is EventSoundTrigger.CraftingSuccess -> SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
        }
    }

    /**
     * Plays a UI navigation click
     */
    fun playMenuClick() {
        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
    }

    /**
     * Hook to trigger short, randomized audio clips when a character is selected.
     */
    fun playCharacterSelection(characterName: String? = null, voicePackId: String = "default") {
        _lastTriggeredName.value = characterName?.let { "Výběr postavy: $it" } ?: "Výběr postavy"
        val greetingTypes = listOf(
            CharacterVoiceType.TAP_GREETING,
            CharacterVoiceType.AFFECTION_TALK,
            CharacterVoiceType.BATTLE_ATTACK
        )
        val selectedVoiceType = greetingTypes.random()
        SoundEffectManager.playCharacterVoice(voicePackId, selectedVoiceType)
    }

    /**
     * Hook to trigger short, randomized audio clips when a character's affinity rank increases.
     */
    fun playAffinityRankUp(tierLevel: Int, characterName: String? = null) {
        _lastTriggeredName.value = characterName?.let { "Zvýšení ranku ($it): Úroveň $tierLevel" } ?: "Zvýšení ranku pouta: Úroveň $tierLevel"
        SoundEffectManager.playRelationshipTier(tierLevel)
    }

    /**
     * Toggles master mute state
     */
    fun toggleMute() {
        SoundEffectManager.toggleMute()
    }

    /**
     * Explicitly sets mute state
     */
    fun setMuted(muted: Boolean) {
        if (isMuted.value != muted) {
            SoundEffectManager.toggleMute()
        }
    }

    /**
     * Sets BGM / ambient atmosphere volume (0.0f - 1.0f)
     */
    fun setBgmVolume(volume: Float) {
        SoundEffectManager.setBgmVolume(volume)
    }

    /**
     * Sets sound effects volume (0.0f - 1.0f)
     */
    fun setSfxVolume(volume: Float) {
        SoundEffectManager.setSfxVolume(volume)
    }

    /**
     * Sets character voice volume (0.0f - 1.0f)
     */
    fun setVoiceVolume(volume: Float) {
        SoundEffectManager.setVoiceVolume(volume)
    }
}

/**
 * CompositionLocal providing access to the current ComposeSoundManager
 */
val LocalSoundManager = staticCompositionLocalOf { ComposeSoundManager() }

/**
 * Remembers a singleton or customized ComposeSoundManager instance
 */
@Composable
fun rememberSoundManager(): ComposeSoundManager {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember { ComposeSoundManager(context.applicationContext) }
}

/**
 * Composable side-effect that automatically plays and manages an ambient soundtrack
 * for a specific screen or location, stopping or restoring gracefully when the composable leaves composition.
 */
@Composable
fun AmbientSoundTrackEffect(
    track: LocationAmbientSound,
    enabled: Boolean = true,
    soundManager: ComposeSoundManager = LocalSoundManager.current
) {
    LaunchedEffect(track, enabled) {
        if (enabled) {
            soundManager.playAmbient(track, crossfade = true)
        }
    }
}

/**
 * Composable side-effect for triggering combat sound feedback on state changes
 */
@Composable
fun CombatSoundEffect(
    trigger: CombatSoundTrigger?,
    soundManager: ComposeSoundManager = LocalSoundManager.current
) {
    LaunchedEffect(trigger) {
        trigger?.let { soundManager.playCombatSound(it) }
    }
}

/**
 * Composable side-effect for triggering event sound feedback on state changes
 */
@Composable
fun EventSoundEffect(
    trigger: EventSoundTrigger?,
    soundManager: ComposeSoundManager = LocalSoundManager.current
) {
    LaunchedEffect(trigger) {
        trigger?.let { soundManager.playEventSound(it) }
    }
}
