package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class CharvikAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      ACTION_DISMISS -> {
        val alarmId = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID) ?: "default"
        AlarmSoundPlayer.stop()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(alarmId.hashCode())
      }

      ACTION_SNOOZE_10 -> {
        val alarmId = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID) ?: "snooze"
        val title = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_TITLE) ?: "Charvik Care Alarm"
        val body = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_BODY) ?: "Snoozed reminder"
        val type = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_TYPE) ?: "CUSTOM"
        AlarmSoundPlayer.stop()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(alarmId.hashCode())
        CharvikAlarmScheduler.scheduleOneShotAlarm(
          context = context,
          alarmId = "${alarmId}_snooze",
          triggerAtMillis = System.currentTimeMillis() + 10 * 60 * 1000L,
          title = title,
          body = body,
          alarmType = type,
        )
      }

      else -> {
        val alarmId = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID) ?: "charvik_alarm"
        val title = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_TITLE) ?: "Charvik Care Reminder"
        val body = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_BODY) ?: "Tap to open Charvik tracker"
        val alarmType = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_TYPE) ?: "FEED"
        val repeatDaily = intent.getBooleanExtra(CharvikAlarmScheduler.EXTRA_REPEAT_DAILY, false)
        val hour = intent.getIntExtra(CharvikAlarmScheduler.EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(CharvikAlarmScheduler.EXTRA_MINUTE, 30)

        if (repeatDaily) {
          CharvikAlarmScheduler.scheduleDailyAlarm(context, alarmId, hour, minute, title, body, alarmType)
        }

        CharvikAlarmScheduler.ensureNotificationChannels(context)
        AlarmSoundPlayer.startLoopingAlarm(context, title)

        val ringIntent = Intent(context, AlarmRingingActivity::class.java).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
          putExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID, alarmId)
          putExtra(CharvikAlarmScheduler.EXTRA_TITLE, title)
          putExtra(CharvikAlarmScheduler.EXTRA_BODY, body)
          putExtra(CharvikAlarmScheduler.EXTRA_ALARM_TYPE, alarmType)
        }
        val fullScreenPending = PendingIntent.getActivity(
          context,
          alarmId.hashCode() + 1,
          ringIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPending = PendingIntent.getActivity(
          context,
          alarmId.hashCode() + 2,
          openAppIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val dismissIntent = Intent(context, CharvikAlarmReceiver::class.java).apply {
          action = ACTION_DISMISS
          putExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPending = PendingIntent.getBroadcast(
          context,
          alarmId.hashCode() + 3,
          dismissIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val snoozeIntent = Intent(context, CharvikAlarmReceiver::class.java).apply {
          action = ACTION_SNOOZE_10
          putExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID, alarmId)
          putExtra(CharvikAlarmScheduler.EXTRA_TITLE, title)
          putExtra(CharvikAlarmScheduler.EXTRA_BODY, body)
          putExtra(CharvikAlarmScheduler.EXTRA_ALARM_TYPE, alarmType)
        }
        val snoozePending = PendingIntent.getBroadcast(
          context,
          alarmId.hashCode() + 4,
          snoozeIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CharvikAlarmScheduler.CHANNEL_ID_ALARM)
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle(title)
          .setContentText(body)
          .setStyle(NotificationCompat.BigTextStyle().bigText(body))
          .setPriority(NotificationCompat.PRIORITY_MAX)
          .setCategory(NotificationCompat.CATEGORY_ALARM)
          .setAutoCancel(false)
          .setOngoing(true)
          .setContentIntent(contentPending)
          .setFullScreenIntent(fullScreenPending, true)
          .addAction(0, "Dismiss", dismissPending)
          .addAction(0, "Snooze 10m", snoozePending)
          .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(alarmId.hashCode(), notification)
      }
    }
  }

  companion object {
    const val ACTION_FIRE_ALARM = "com.example.charvik.ACTION_FIRE_ALARM"
    const val ACTION_DISMISS = "com.example.charvik.ACTION_DISMISS"
    const val ACTION_SNOOZE_10 = "com.example.charvik.ACTION_SNOOZE_10"
  }
}
