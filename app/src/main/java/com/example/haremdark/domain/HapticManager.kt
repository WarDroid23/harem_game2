package com.example.haremdark.domain

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HapticManager {
    private const val TAG = "HapticManager"
    private const val PREFS_NAME = "haptic_prefs"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    private var appContext: Context? = null
    
    private val _isHapticsEnabledFlow = MutableStateFlow(true)
    val isHapticsEnabledFlow: StateFlow<Boolean> = _isHapticsEnabledFlow.asStateFlow()

    var isHapticsEnabled: Boolean
        get() = _isHapticsEnabledFlow.value
        set(value) {
            _isHapticsEnabledFlow.value = value
            savePreference(value)
        }

    fun init(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val enabled = prefs?.getBoolean(KEY_HAPTICS_ENABLED, true) ?: true
            _isHapticsEnabledFlow.value = enabled
        } catch (e: Exception) {
            Log.e(TAG, "Error loading haptic preferences", e)
        }
    }

    fun toggleHaptics(): Boolean {
        isHapticsEnabled = !isHapticsEnabled
        return isHapticsEnabled
    }

    private fun savePreference(enabled: Boolean) {
        try {
            appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ?.edit()
                ?.putBoolean(KEY_HAPTICS_ENABLED, enabled)
                ?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving haptic preference", e)
        }
    }

    /**
     * Standard click haptic feedback for buttons, navigation, and menu selections.
     */
    fun vibrateClick() {
        if (!isHapticsEnabled) return
        try {
            val ctx = appContext ?: return
            val vibrator = getVibrator(ctx) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating click", e)
        }
    }

    /**
     * Heavy haptic feedback for skill activations, heavy strikes, and special actions.
     */
    fun vibrateHeavy() {
        if (!isHapticsEnabled) return
        try {
            val ctx = appContext ?: return
            val vibrator = getVibrator(ctx) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(60)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating heavy", e)
        }
    }

    /**
     * Critical hit / Supernova haptic feedback with double click / sharp pulse.
     */
    fun vibrateCritical() {
        if (!isHapticsEnabled) return
        try {
            val ctx = appContext ?: return
            val vibrator = getVibrator(ctx) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 40, 60, 100),
                        intArrayOf(0, 255, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(180)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating critical", e)
        }
    }

    /**
     * Miss / Evasion haptic feedback with a very short, sharp pulse.
     */
    fun vibrateMiss() {
        if (!isHapticsEnabled) return
        try {
            val ctx = appContext ?: return
            val vibrator = getVibrator(ctx) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating miss", e)
        }
    }

    /**
     * Status effect trigger haptic feedback with a longer, pulsating vibration.
     */
    fun vibrateStatusEffect() {
        if (!isHapticsEnabled) return
        try {
            val ctx = appContext ?: return
            val vibrator = getVibrator(ctx) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 50, 100),
                        intArrayOf(0, 180, 0, 180),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(250)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating status effect", e)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
