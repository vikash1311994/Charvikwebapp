package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AgeSummary
import com.example.data.model.FeedTargets
import com.example.ui.theme.JetBrainsMonoFamily
import java.util.Locale

fun formatMmSs(totalSeconds: Int): String {
  val safe = totalSeconds.coerceAtLeast(0)
  val mins = safe / 60
  val secs = safe % 60
  return String.format(Locale.US, "%02d:%02d", mins, secs)
}

@Composable
fun ActiveAlarmBanner(
  isRinging: Boolean,
  title: String,
  onStop: () -> Unit,
  onSnooze: (() -> Unit)? = null,
) {
  AnimatedVisibility(visible = isRinging) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
      shape = MaterialTheme.shapes.medium,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Alarm ringing",
            tint = MaterialTheme.colorScheme.error,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Alarm Sound Active",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
              text = title.ifBlank { "Care reminder" },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (onSnooze != null) {
            FilledTonalButton(
              onClick = onSnooze,
              modifier = Modifier
                .weight(1f)
                .testTag("snooze_ringing_banner_button"),
            ) {
              Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Snooze 5m")
            }
          }
          Button(
            onClick = onStop,
            modifier = Modifier
              .weight(1f)
              .testTag("stop_ringing_banner_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
          ) {
            Icon(Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Stop")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroBabyBanner(
  ageSummary: AgeSummary,
  targets: FeedTargets,
  isDarkTheme: Boolean = false,
  onToggleDarkTheme: (() -> Unit)? = null,
  onUpdateWeightClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Image(
        painter = painterResource(id = R.drawable.img_charvik_hero_1795029742228),
        contentDescription = "Charvik profile banner",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .matchParentSize()
          .clip(MaterialTheme.shapes.large),
        alpha = 0.45f,
      )
      Box(
        modifier = Modifier
          .matchParentSize()
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
              )
            )
          )
      )

      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(100.dp),
              ) {
                Text(
                  text = "DOB ${ageSummary.dobDisplay}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
              if (onToggleDarkTheme != null) {
                Surface(
                  onClick = onToggleDarkTheme,
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(100.dp),
                  modifier = Modifier.testTag("toggle_night_mode_button"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                      contentDescription = "Toggle night-feed dark mode",
                      modifier = Modifier.size(14.dp),
                      tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                      text = if (isDarkTheme) "Day" else "Night",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Charvik • ${ageSummary.daysOld} days",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${ageSummary.weeks}w ${ageSummary.remainingDays}d • ${ageSummary.correctedNote}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Surface(
            onClick = onUpdateWeightClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.testTag("hero_weight_chip"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.MonitorWeight,
                contentDescription = "Update weight",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = String.format(Locale.US, "%.2f kg", targets.weightKg),
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(
            "Mild silent reflux (nasal mucus)",
            "Newborn cheek milia",
            "Immature diaphragm hiccups",
          ).forEach { condition ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
              shape = RoundedCornerShape(100.dp),
            ) {
              Text(
                text = condition,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (targets.nextStageUnlocked) {
              "3.0 kg milestone reached • 60–70 ml/feed unlocked"
            } else {
              "Target 3.0 kg to unlock 60–70 ml/feed (+20–30 g/day)"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
          progress = { targets.progressTo3Kg },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(100.dp)),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        )
      }
    }
  }
}

@Composable
fun StatRingCard(
  title: String,
  valueText: String,
  targetText: String,
  progress: Float,
  ringColor: Color,
  isAlert: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer
      else MaterialTheme.colorScheme.surface,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val trackColor = MaterialTheme.colorScheme.surfaceVariant
      Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
          drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke,
          )
          drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke,
          )
        }
        Text(
          text = valueText,
          style = MaterialTheme.typography.labelLarge.copy(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
          ),
          color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = targetText,
        style = MaterialTheme.typography.bodySmall,
        color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
        else MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
