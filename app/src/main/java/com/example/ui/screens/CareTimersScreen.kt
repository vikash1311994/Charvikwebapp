package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CharvikKnowledge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.ActiveTimerState
import com.example.ui.viewmodel.CharvikDashboardData
import com.example.ui.viewmodel.PumpTimerState
import kotlin.math.roundToInt

@Composable
fun CareTimersScreen(
  data: CharvikDashboardData,
  refluxTimer: ActiveTimerState,
  pumpTimer: PumpTimerState,
  isAlarmRinging: Boolean,
  ringingTitle: String,
  onStopRinging: () -> Unit,
  onSnoozeRinging: () -> Unit,
  onStartReflux: (minutes: Int) -> Unit,
  onStopReflux: () -> Unit,
  onStartPump: (minutes: Int) -> Unit,
  onStopPump: () -> Unit,
  onSavePumpSession: (minutes: Int, ml: Int, scheduleNext: Boolean) -> Unit,
  onLogNasalSuction: () -> Unit,
) {
  var selectedRefluxMin by remember { mutableIntStateOf(25) }
  var selectedPumpMin by remember { mutableIntStateOf(12) }
  var pumpedMl by remember { mutableIntStateOf(50) }
  var scheduleNextPump by remember { mutableStateOf(true) }

  val targets = data.feedTargets
  val suctionCapReached = data.suctionsToday >= CharvikKnowledge.MAX_NASAL_SUCTIONS_24H

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
      Text(
        text = "Care Timers & Feeding",
        style = MaterialTheme.typography.headlineMedium,
      )
      Text(
        text = "Reflux holds, two-phase pumping, and nasal saline limits with alarm alerts",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // 1. Reflux Hold Timer (20-30 min upright ~45°)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text("Reflux Upright Hold (~45°)", style = MaterialTheme.typography.titleMedium)
            }
            Text(
              text = "20–30 min",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          Text(
            text = formatMmSs(
              if (refluxTimer.running || refluxTimer.completedAlert) refluxTimer.remainingSeconds
              else selectedRefluxMin * 60
            ),
            style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
            color = if (refluxTimer.completedAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          )

          if (!refluxTimer.running) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(20, 25, 30).forEach { min ->
                FilterChip(
                  selected = selectedRefluxMin == min,
                  onClick = { selectedRefluxMin = min },
                  label = { Text("${min}m") },
                )
              }
            }
          } else {
            val total = refluxTimer.totalSeconds.coerceAtLeast(1)
            val progress = 1f - (refluxTimer.remainingSeconds.toFloat() / total)
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            )
          }

          Text(
            text = "Hold Charvik upright at ~45° after every feed before laying flat on his back. Alarm rings at 00:00.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!refluxTimer.running) {
              Button(
                onClick = { onStartReflux(selectedRefluxMin) },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("start_reflux_timer_button"),
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start ${selectedRefluxMin}m Hold")
              }
            } else {
              Button(
                onClick = onStopReflux,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("stop_reflux_timer_button"),
              ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop Hold Timer")
              }
            }
          }
        }
      }
    }

    // 2. Pumping Timer (Stimulation 2 min -> Expression 8-13 min)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
              Text("Pumping Session Timer", style = MaterialTheme.typography.titleMedium)
            }
            Surface(
              color = if (pumpTimer.running && pumpTimer.inStimulationPhase) {
                MaterialTheme.colorScheme.tertiaryContainer
              } else {
                MaterialTheme.colorScheme.secondaryContainer
              },
              shape = MaterialTheme.shapes.small,
            ) {
              Text(
                text = when {
                  pumpTimer.completedAlert -> "Complete"
                  pumpTimer.running && pumpTimer.inStimulationPhase -> "Stimulation (0–2m)"
                  pumpTimer.running -> "Expression (2–15m)"
                  else -> "Every 2.5–3 hrs"
                },
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }

          Text(
            text = if (pumpTimer.running || pumpTimer.completedAlert) {
              "${formatMmSs(pumpTimer.elapsedSeconds)} / ${formatMmSs(pumpTimer.targetSeconds)}"
            } else {
              "00:00 / ${formatMmSs(selectedPumpMin * 60)}"
            },
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFamily),
            color = MaterialTheme.colorScheme.onSurface,
          )

          if (!pumpTimer.running) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(10, 12, 15).forEach { min ->
                FilterChip(
                  selected = selectedPumpMin == min,
                  onClick = { selectedPumpMin = min },
                  label = { Text("${min}m") },
                )
              }
            }
          } else {
            LinearProgressIndicator(
              progress = { pumpTimer.elapsedSeconds.toFloat() / pumpTimer.targetSeconds.coerceAtLeast(1) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!pumpTimer.running) {
              Button(
                onClick = { onStartPump(selectedPumpMin) },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("start_pump_timer_button"),
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Pump")
              }
            } else {
              FilledTonalButton(
                onClick = onStopPump,
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("stop_pump_timer_button"),
              ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop")
              }
            }
          }

          Text("Expressed Volume: $pumpedMl ml", style = MaterialTheme.typography.labelLarge)
          Slider(
            value = pumpedMl.toFloat(),
            onValueChange = { pumpedMl = it.roundToInt() },
            valueRange = 10f..180f,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Set alarm for next pump (2h 45m)", style = MaterialTheme.typography.bodySmall)
            Switch(checked = scheduleNextPump, onCheckedChange = { scheduleNextPump = it })
          }
          FilledTonalButton(
            onClick = {
              val loggedMins = if (pumpTimer.elapsedSeconds >= 60) pumpTimer.elapsedSeconds / 60 else selectedPumpMin
              onSavePumpSession(loggedMins, pumpedMl, scheduleNextPump)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("save_pump_session_button"),
          ) {
            Text("Log Pump Session")
          }
        }
      }
    }

    // 3. Nasal Care (Noblok saline + 3-4/day suction cap)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
          containerColor = if (suctionCapReached) MaterialTheme.colorScheme.errorContainer
          else MaterialTheme.colorScheme.surface
        ),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text("Nasal Care (Noblok Saline)", style = MaterialTheme.typography.titleMedium)
            }
            Text(
              text = "${data.suctionsToday} / 4 today",
              style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
              color = if (suctionCapReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
          }
          Text(
            text = "• Put 1–2 drops Noblok plain saline per nostril 10 min before feeding to loosen milk mucus.\n• Cap gentle nasal suctioning at max 3–4 times/day to avoid mucosal swelling.",
            style = MaterialTheme.typography.bodySmall,
          )
          if (suctionCapReached) {
            Text(
              text = "Daily cap (4 suctions) reached! Prefer saline drops + upright positioning unless breathing is obstructed.",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error,
            )
          }
          Button(
            onClick = onLogNasalSuction,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("log_nasal_suction_button"),
          ) {
            Text("Log Nasal Saline / Suction")
          }
        }
      }
    }

    // 4. Feeding Volume & Method Protocol Reference
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Charvik's Feed Formula", style = MaterialTheme.typography.titleMedium)
          }
          Text(
            text = "Current weight: ${String.format(java.util.Locale.US, "%.2f", targets.weightKg)} kg → ${targets.dailyFluidMinMl}–${targets.dailyFluidMaxMl} ml/day (150–180 ml × kg)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "• Per feed: ${targets.perFeedMinMl}–${targets.perFeedMaxMl} ml every 2.0–2.5 hrs (8–10 feeds/day).\n• Sequence: Breast first (10–15 min) → Paced bottle top-up.\n• Nipple & hold: Slow-flow (SS / 0m+) nipple held horizontal.\n• Mid-feed burp: Pause at 25–30 ml to burp before finishing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
