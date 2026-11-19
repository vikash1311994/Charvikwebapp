package com.example.alarm

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CharvikTheme

class AlarmRingingActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val alarmId = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_ID) ?: "charvik_alarm"
    val title = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_TITLE) ?: "Charvik Care Alarm"
    val body = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_BODY) ?: "Time for Charvik's scheduled care."
    val alarmType = intent.getStringExtra(CharvikAlarmScheduler.EXTRA_ALARM_TYPE) ?: "FEED"

    setContent {
      CharvikTheme {
        AlarmRingingScreen(
          title = title,
          body = body,
          onDismiss = {
            AlarmSoundPlayer.stop()
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(alarmId.hashCode())
            finish()
          },
          onSnooze = {
            AlarmSoundPlayer.stop()
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(alarmId.hashCode())
            CharvikAlarmScheduler.scheduleOneShotAlarm(
              context = this,
              alarmId = "${alarmId}_snooze",
              triggerAtMillis = System.currentTimeMillis() + 10 * 60 * 1000L,
              title = title,
              body = body,
              alarmType = alarmType,
            )
            finish()
          },
        )
      }
    }
  }
}

@Composable
private fun AlarmRingingScreen(
  title: String,
  body: String,
  onDismiss: () -> Unit,
  onSnooze: () -> Unit,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "alarm_pulse")
  val pulse by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(750),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse",
  )

  Surface(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.primaryContainer,
            )
          )
        )
        .safeDrawingPadding()
        .padding(24.dp),
    ) {
      Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier = Modifier
            .size(120.dp)
            .scale(pulse)
            .background(
              color = MaterialTheme.colorScheme.secondary,
              shape = CircleShape,
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Alarm ringing",
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(56.dp),
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text = "CHARVIK CARE ALERT",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.displaySmall,
          color = MaterialTheme.colorScheme.onPrimary,
          textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = body,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
          textAlign = TextAlign.Center,
        )
      }

      Row(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onSnooze,
          modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .testTag("alarm_snooze_button"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        ) {
          Icon(Icons.Default.Snooze, contentDescription = null)
          Spacer(modifier = Modifier.size(8.dp))
          Text("Snooze 10m")
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .testTag("alarm_dismiss_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
          ),
        ) {
          Icon(Icons.Default.AlarmOff, contentDescription = null)
          Spacer(modifier = Modifier.size(8.dp))
          Text("Dismiss")
        }
      }
    }
  }
}
