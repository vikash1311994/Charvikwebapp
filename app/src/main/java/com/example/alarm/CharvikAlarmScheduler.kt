package com.example.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import java.util.Calendar

object CharvikAlarmScheduler {
  const val CHANNEL_ID_ALARM = "charvik_alarm_channel"
  const val CHANNEL_ID_REMINDER = "charvik_reminder_channel"

  const val EXTRA_ALARM_ID = "extra_alarm_id"
  const val EXTRA_TITLE = "extra_title"
  const val EXTRA_BODY = "extra_body"
  const val EXTRA_ALARM_TYPE = "extra_alarm_type"
  const val EXTRA_REPEAT_DAILY = "extra_repeat_daily"
  const val EXTRA_HOUR = "extra_hour"
  const val EXTRA_MINUTE = "extra_minute"

  private const val PREFS_NAME = "charvik_alarm_registry"

  fun ensureNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val alarmChannel = NotificationChannel(
      CHANNEL_ID_ALARM,
      "Charvik Care Alarms",
      NotificationManager.IMPORTANCE_HIGH,
    ).apply {
      description = "Loud alarm alerts for feeds, reflux holds, pumping, and vaccines"
      enableVibration(true)
      vibrationPattern = longArrayOf(0, 500, 300, 500)
      setSound(
        alarmSound,
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_ALARM)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build(),
      )
    }

    val reminderChannel = NotificationChannel(
      CHANNEL_ID_REMINDER,
      "Charvik Daily Reminders",
      NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
      description = "Gentle reminders for morning sun, Vitamin D3, and routine checks"
    }

    nm.createNotificationChannel(alarmChannel)
    nm.createNotificationChannel(reminderChannel)
  }

  fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return am.canScheduleExactAlarms()
  }

  fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }
  }

  fun scheduleOneShotAlarm(
    context: Context,
    alarmId: String,
    triggerAtMillis: Long,
    title: String,
    body: String,
    alarmType: String,
  ) {
    ensureNotificationChannels(context)
    val safeTrigger = maxOf(triggerAtMillis, System.currentTimeMillis() + 3_000L)
    val requestCode = alarmId.hashCode()
    val pending = buildAlarmPendingIntent(
      context = context,
      requestCode = requestCode,
      alarmId = alarmId,
      title = title,
      body = body,
      alarmType = alarmType,
      repeatDaily = false,
      hour = 0,
      minute = 0,
    )
    scheduleInternal(context, safeTrigger, pending)
    saveAlarmMeta(context, alarmId, safeTrigger, title, body, alarmType, false, 0, 0)
  }

  fun scheduleDailyAlarm(
    context: Context,
    alarmId: String,
    hour: Int,
    minute: Int,
    title: String,
    body: String,
    alarmType: String,
  ): Long {
    ensureNotificationChannels(context)
    val nextTrigger = computeNextDailyTrigger(hour, minute)
    val requestCode = alarmId.hashCode()
    val pending = buildAlarmPendingIntent(
      context = context,
      requestCode = requestCode,
      alarmId = alarmId,
      title = title,
      body = body,
      alarmType = alarmType,
      repeatDaily = true,
      hour = hour,
      minute = minute,
    )
    scheduleInternal(context, nextTrigger, pending)
    saveAlarmMeta(context, alarmId, nextTrigger, title, body, alarmType, true, hour, minute)
    return nextTrigger
  }

  fun cancelAlarm(context: Context, alarmId: String) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, CharvikAlarmReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
      context,
      alarmId.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    am.cancel(pending)
    pending.cancel()
    removeAlarmMeta(context, alarmId)
  }

  fun computeNextDailyTrigger(hour: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
      set(Calendar.MINUTE, minute.coerceIn(0, 59))
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    if (cal.timeInMillis <= nowMillis + 2_000L) {
      cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
  }

  private fun scheduleInternal(context: Context, triggerAtMillis: Long, pending: PendingIntent) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (canScheduleExactAlarms(context)) {
      am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
    } else {
      am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
    }
  }

  private fun buildAlarmPendingIntent(
    context: Context,
    requestCode: Int,
    alarmId: String,
    title: String,
    body: String,
    alarmType: String,
    repeatDaily: Boolean,
    hour: Int,
    minute: Int,
  ): PendingIntent {
    val intent = Intent(context, CharvikAlarmReceiver::class.java).apply {
      action = CharvikAlarmReceiver.ACTION_FIRE_ALARM
      putExtra(EXTRA_ALARM_ID, alarmId)
      putExtra(EXTRA_TITLE, title)
      putExtra(EXTRA_BODY, body)
      putExtra(EXTRA_ALARM_TYPE, alarmType)
      putExtra(EXTRA_REPEAT_DAILY, repeatDaily)
      putExtra(EXTRA_HOUR, hour)
      putExtra(EXTRA_MINUTE, minute)
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun saveAlarmMeta(
    context: Context,
    alarmId: String,
    triggerAtMillis: Long,
    title: String,
    body: String,
    alarmType: String,
    repeatDaily: Boolean,
    hour: Int,
    minute: Int,
  ) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val encoded = listOf(
      triggerAtMillis.toString(),
      if (repeatDaily) "1" else "0",
      hour.toString(),
      minute.toString(),
      alarmType,
      title.replace("|", " "),
      body.replace("|", " "),
    ).joinToString("|")
    prefs.edit().putString(alarmId, encoded).apply()
  }

  private fun removeAlarmMeta(context: Context, alarmId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().remove(alarmId).apply()
  }

  fun rescheduleAllSavedAlarms(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    for ((alarmId, value) in prefs.all) {
      val raw = value as? String ?: continue
      val parts = raw.split("|")
      if (parts.size < 7) continue
      val triggerAt = parts[0].toLongOrNull() ?: continue
      val repeatDaily = parts[1] == "1"
      val hour = parts[2].toIntOrNull() ?: 7
      val minute = parts[3].toIntOrNull() ?: 30
      val alarmType = parts[4]
      val title = parts[5]
      val body = parts[6]
      if (repeatDaily) {
        scheduleDailyAlarm(context, alarmId, hour, minute, title, body, alarmType)
      } else if (triggerAt > now) {
        scheduleOneShotAlarm(context, alarmId, triggerAt, title, body, alarmType)
      } else {
        removeAlarmMeta(context, alarmId)
      }
    }
  }
}
