package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CharvikKnowledge
import com.example.data.model.FeedType
import com.example.data.model.RefluxType
import com.example.data.model.StoolColor
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.ActiveTimerState
import com.example.ui.viewmodel.CharvikDashboardData
import java.util.Locale

@Composable
fun TodayScreen(
  data: CharvikDashboardData,
  nowMillis: Long,
  refluxTimer: ActiveTimerState,
  isAlarmRinging: Boolean,
  ringingTitle: String,
  onStopRinging: () -> Unit,
  onSnoozeRinging: () -> Unit,
  isDarkTheme: Boolean,
  onToggleDarkTheme: () -> Unit,
  onOpenFeedSheet: () -> Unit,
  onOpenDigestionSheet: () -> Unit,
  onOpenWeightSheet: () -> Unit,
  onStartRefluxHold: () -> Unit,
  onStopRefluxHold: () -> Unit,
  onToggleRoutine: (key: String, completed: Boolean) -> Unit,
  onNavigateToTimers: () -> Unit,
) {
  val targets = data.feedTargets
  val lastFeed = data.lastFeedMillis
  val minutesSinceLastFeed = if (lastFeed != null) ((nowMillis - lastFeed) / 60_000L).toInt().coerceAtLeast(0) else null
  val feedGapOverdue = minutesSinceLastFeed != null && minutesSinceLastFeed >= 180
  val feedGapDue = minutesSinceLastFeed != null && minutesSinceLastFeed in 135..179

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      ActiveAlarmBanner(
        isRinging = isAlarmRinging,
        title = ringingTitle,
        onStop = onStopRinging,
        onSnooze = onSnoozeRinging,
      )
      HeroBabyBanner(
        ageSummary = data.ageSummary,
        targets = targets,
        isDarkTheme = isDarkTheme,
        onToggleDarkTheme = onToggleDarkTheme,
        onUpdateWeightClick = onOpenWeightSheet,
      )
    }

    // Feed Countdown & Active Reflux Hold Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
          containerColor = when {
            feedGapOverdue -> MaterialTheme.colorScheme.errorContainer
            feedGapDue -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.primaryContainer
          }
        ),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(
                imageVector = if (feedGapOverdue) Icons.Default.Warning else Icons.Default.AccessTime,
                contentDescription = "Feed interval status",
                tint = if (feedGapOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              )
              Text(
                text = when {
                  minutesSinceLastFeed == null -> "No feeds logged today yet"
                  feedGapOverdue -> "Feed gap > 3 hrs (${minutesSinceLastFeed / 60}h ${minutesSinceLastFeed % 60}m)"
                  feedGapDue -> "Feed window open (${minutesSinceLastFeed / 60}h ${minutesSinceLastFeed % 60}m ago)"
                  else -> "Last feed ${minutesSinceLastFeed / 60}h ${minutesSinceLastFeed % 60}m ago"
                },
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Text(
              text = "${targets.perFeedMinMl}–${targets.perFeedMaxMl} ml",
              style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
            )
          }

          Text(
            text = "Every 2.0–2.5 hrs (8–10/day) • Breast 10–15m first → Paced horizontal bottle • Mid-feed burp at 30 ml",
            style = MaterialTheme.typography.bodySmall,
          )

          if (refluxTimer.running || refluxTimer.completedAlert) {
            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                    text = if (refluxTimer.completedAlert) "Reflux Hold Complete!" else refluxTimer.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Text(
                    text = if (refluxTimer.completedAlert) "Safe to lay Charvik flat on his back"
                    else "Keep upright ~45° • ${formatMmSs(refluxTimer.remainingSeconds)} left",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                  )
                }
                FilledTonalButton(
                  onClick = onStopRefluxHold,
                  modifier = Modifier.testTag("stop_reflux_timer_today"),
                ) {
                  Text(if (refluxTimer.completedAlert) "Done" else "Stop")
                }
              }
            }
          }
        }
      }
    }

    // Quick Action Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = onOpenFeedSheet,
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("quick_log_feed_button"),
        ) {
          Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Log Feed")
        }
        FilledTonalButton(
          onClick = onOpenDigestionSheet,
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("quick_log_diaper_button"),
        ) {
          Icon(Icons.Default.BabyChangingStation, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Diaper/Gas")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilledTonalButton(
          onClick = { if (refluxTimer.running) onNavigateToTimers() else onStartRefluxHold() },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_reflux_hold_button"),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          ),
        ) {
          Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (refluxTimer.running) "Reflux ${formatMmSs(refluxTimer.remainingSeconds)}" else "25m Reflux Hold")
        }
        FilledTonalButton(
          onClick = onOpenWeightSheet,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_weight_button"),
        ) {
          Icon(Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Log Weight")
        }
      }
    }

    // 3 Progress Rings
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        StatRingCard(
          title = "Bottle Intake",
          valueText = "${data.todayBottleMl}",
          targetText = "${targets.dailyFluidMinMl}–${targets.dailyFluidMaxMl} ml",
          progress = data.todayBottleMl.toFloat() / targets.dailyFluidMinMl.coerceAtLeast(1),
          ringColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f),
        )
        StatRingCard(
          title = "Wet Diapers",
          valueText = "${data.wetDiapersLast24h}/6",
          targetText = "≥ 6 in 24h",
          progress = data.wetDiapersLast24h / 6f,
          ringColor = MaterialTheme.colorScheme.tertiary,
          isAlert = data.digestionLogs.isNotEmpty() && data.wetDiapersLast24h < 6,
          modifier = Modifier.weight(1f),
        )
        StatRingCard(
          title = "Suctions",
          valueText = "${data.suctionsToday}/4",
          targetText = "Max 3–4/day",
          progress = data.suctionsToday / 4f,
          ringColor = MaterialTheme.colorScheme.secondary,
          isAlert = data.suctionsToday >= CharvikKnowledge.MAX_NASAL_SUCTIONS_24H,
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Daily Routine Checklist
    item {
      Text(
        text = "Daily Care Checklist",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
          CharvikKnowledge.dailyRoutineSpecs.forEach { spec ->
            val checked = data.completedRoutineKeysToday.contains(spec.key)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleRoutine(spec.key, checked) }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("routine_${spec.key}"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (checked) "Completed" else "Pending",
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(spec.title, style = MaterialTheme.typography.titleSmall)
                Text(
                  spec.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
              ) {
                Text(
                  text = spec.windowHint,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
          }
        }
      }
    }

    // Recent Feeds & Digestion Activity
    item {
      Text(
        text = "Recent Feeds & Output",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    if (data.feeds.isEmpty() && data.digestionLogs.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text("No logs yet today", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Tap Log Feed or Diaper/Gas above to start tracking Charvik's 24-hour intake and hydration.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      items(data.feeds.take(5), key = { "feed_${it.id}" }) { feed ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = FeedType.safeValueOf(feed.feedType).label,
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = buildString {
                  if (feed.breastMinutes > 0) append("${feed.breastMinutes}m breast")
                  if (feed.breastMinutes > 0 && feed.bottleMl > 0) append(" + ")
                  if (feed.bottleMl > 0) append("${feed.bottleMl} ml bottle")
                  if (feed.burpedMidFeed) append(" • Burped")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              if (!feed.note.isNullOrBlank()) {
                Text(
                  text = feed.note,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
            }
            Text(
              text = CharvikKnowledge.formatTime(feed.loggedAtMillis),
              style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      items(data.digestionLogs.take(4), key = { "dig_${it.id}" }) { dig ->
        val stool = StoolColor.safeValueOf(dig.stoolColor)
        val reflux = RefluxType.safeValueOf(dig.refluxOrSpitUp)
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = buildString {
                  if (dig.wetDiaper) append("Wet Diaper")
                  if (dig.wetDiaper && dig.stoolPassed) append(" + ")
                  if (dig.stoolPassed) append("Stool (${stool.label})")
                  if (!dig.wetDiaper && !dig.stoolPassed) append("Digestion Observation")
                },
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = buildString {
                  if (reflux != RefluxType.NONE) append("Reflux: ${reflux.label}")
                  if (dig.hiccups) append(if (isNotEmpty()) " • Hiccups" else "Hiccups")
                  if (dig.gasOrDiscomfort) append(if (isNotEmpty()) " • Gas/straining" else "Gas/straining")
                  if (isEmpty()) append(String.format(Locale.US, "Logged cleanly"))
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = CharvikKnowledge.formatTime(dig.loggedAtMillis),
              style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}
