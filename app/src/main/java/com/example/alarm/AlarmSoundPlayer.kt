package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AlarmSoundPlayer {
  private const val TAG = "AlarmSoundPlayer"
  private var mediaPlayer: MediaPlayer? = null
  private var vibrator: Vibrator? = null

  private val _isRinging = MutableStateFlow(false)
  val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

  private val _ringingTitle = MutableStateFlow("")
  val ringingTitle: StateFlow<String> = _ringingTitle.asStateFlow()

  fun startLoopingAlarm(context: Context, title: String = "Charvik Care Alarm") {
    stop()
    _ringingTitle.value = title
    _isRinging.value = true

    val appContext = context.applicationContext
    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    if (alarmUri != null) {
      try {
        val player = MediaPlayer().apply {
          setDataSource(appContext, alarmUri)
          setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_ALARM)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          isLooping = true
          prepare()
          start()
        }
        mediaPlayer = player
      } catch (e: Exception) {
        Log.w(TAG, "Failed to start MediaPlayer alarm: ${e.message}")
      }
    }

    try {
      val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
      vibrator = vib
      val pattern = longArrayOf(0L, 600L, 400L, 600L, 400L)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib?.vibrate(VibrationEffect.createWaveform(pattern, 0))
      } else {
        @Suppress("DEPRECATION")
        vib?.vibrate(pattern, 0)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Vibration unavailable: ${e.message}")
    }
  }

  fun playSingleChime(context: Context) {
    try {
      val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      if (uri != null) {
        val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
        ringtone?.play()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to play chime: ${e.message}")
    }
  }

  fun stop() {
    try {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    } catch (_: Exception) {
    } finally {
      mediaPlayer = null
    }
    try {
      vibrator?.cancel()
    } catch (_: Exception) {
    } finally {
      vibrator = null
    }
    _isRinging.value = false
    _ringingTitle.value = ""
  }
}
