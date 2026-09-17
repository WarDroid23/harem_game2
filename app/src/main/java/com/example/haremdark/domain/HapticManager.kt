package com.example.haremdark.domain

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object HapticManager {
    private const val TAG = "HapticManager"
    private var appContext: Context? = null
    var isHapticsEnabled = true

    fun init(context: Context) {
        appContext = context.applicationContext
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
