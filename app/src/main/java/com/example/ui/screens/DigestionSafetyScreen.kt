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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CharvikKnowledge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.BreathingCheckState
import com.example.ui.viewmodel.CharvikDashboardData
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DigestionSafetyScreen(
  data: CharvikDashboardData,
  breathingCheck: BreathingCheckState,
  isAlarmRinging: Boolean,
  ringingTitle: String,
  onStopRinging: () -> Unit,
  onSnoozeRinging: () -> Unit,
  onOpenDigestionSheet: () -> Unit,
  onOpenWeightSheet: () -> Unit,
  onStartBreathingCheck: () -> Unit,
  onTapBreath: () -> Unit,
  onUpdateBreathingFlags: (retractions: Boolean, flaring: Boolean, grunting: Boolean) -> Unit,
  onResetBreathing: () -> Unit,
  onSaveVitals: (tempF: Double, bpm: Int, redFlag: Boolean) -> Unit,
) {
  var tempF by remember { mutableFloatStateOf(98.4f) }
  val tempC = ((tempF - 32f) * 5f / 9f)
  val tempRedFlag = tempF > CharvikKnowledge.FEVER_THRESHOLD_F || tempF < CharvikKnowledge.HYPOTHERMIA_THRESHOLD_F
  val combinedRedFlag = tempRedFlag || breathingCheck.hasRedFlag

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
        text = "Digestion & Safety Checks",
        style = MaterialTheme.typography.headlineMedium,
      )
      Text(
        text = "Hydration, reflux & stool guide, 60s breathing counter, temperature red flags, and elder-safe rules",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // 1. Digestion & Hydration Summary + Guidance
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
            Column {
              Text("24-Hr Digestion & Output", style = MaterialTheme.typography.titleMedium)
              Text(
                text = "${data.wetDiapersLast24h} wet (target ≥ 6) • ${data.stoolsLast24h} stools in last 24h",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            FilledTonalButton(
              onClick = onOpenDigestionSheet,
              modifier = Modifier.testTag("digestion_screen_log_button"),
            ) {
              Text("Log")
            }
          }

          CharvikKnowledge.digestionTips.forEach { tip ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.Top,
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp),
              )
              Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }

    // 2. Hard Safety Red-Flag Checker (Breathing 60s + Temperature)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
          containerColor = if (combinedRedFlag) MaterialTheme.colorScheme.errorContainer
          else MaterialTheme.colorScheme.surface
        ),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              imageVector = if (combinedRedFlag) Icons.Default.Warning else Icons.Default.Air,
              contentDescription = null,
              tint = if (combinedRedFlag) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Text("60-Sec Resting Breathing Check", style = MaterialTheme.typography.titleMedium)
          }

          Text(
            text = "Count for 60 sec while Charvik is asleep with a clear nose. Alert triggers if > 60 breaths/min.",
            style = MaterialTheme.typography.bodySmall,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "${breathingCheck.breathTaps} breaths",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFamily),
              )
              Text(
                text = if (breathingCheck.running) "${breathingCheck.remainingSeconds}s left (~${breathingCheck.estimatedBpm} bpm)"
                else if (breathingCheck.finished) "Final: ${breathingCheck.breathTaps} bpm"
                else "Ready (60s window)",
                style = MaterialTheme.typography.bodySmall,
              )
            }
            if (!breathingCheck.running) {
              Button(
                onClick = onStartBreathingCheck,
                modifier = Modifier.testTag("start_breathing_check_button"),
              ) {
                Text(if (breathingCheck.finished) "Recount" else "Start 60s")
              }
            } else {
              FilledTonalButton(
                onClick = onResetBreathing,
                modifier = Modifier.testTag("reset_breathing_check_button"),
              ) {
                Text("Reset")
              }
            }
          }

          if (breathingCheck.running) {
            Button(
              onClick = onTapBreath,
              modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("tap_breath_button"),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
              Text("TAP ON EACH BREATH (${breathingCheck.breathTaps})", style = MaterialTheme.typography.titleMedium)
            }
          }

          FlagCheckRow("Rib retractions (chest pulling in)", breathingCheck.retractions) {
            onUpdateBreathingFlags(it, breathingCheck.nasalFlaring, breathingCheck.grunting)
          }
          FlagCheckRow("Nasal flaring", breathingCheck.nasalFlaring) {
            onUpdateBreathingFlags(breathingCheck.retractions, it, breathingCheck.grunting)
          }
          FlagCheckRow("Grunting sounds with breathing", breathingCheck.grunting) {
            onUpdateBreathingFlags(breathingCheck.retractions, breathingCheck.nasalFlaring, it)
          }

          // Temperature Slider
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(
              text = String.format(Locale.US, "Temperature: %.1f°F (%.1f°C)", tempF, tempC),
              style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
              color = if (tempRedFlag) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
          }
          Slider(
            value = tempF,
            onValueChange = { tempF = (it * 10f).roundToInt() / 10f },
            valueRange = 95.0f..104.0f,
          )
          Text(
            text = "Red-flag thresholds: > 100.4°F (38.0°C) or < 97.7°F (36.5°C)",
            style = MaterialTheme.typography.bodySmall,
          )

          if (combinedRedFlag) {
            Surface(
              color = MaterialTheme.colorScheme.error,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "IMMEDIATE DOCTOR ALERT: Red-flag breathing or temperature detected. Contact Apollo Pediatrics / emergency care now.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(12.dp),
              )
            }
          }

          Button(
            onClick = {
              val bpm = if (breathingCheck.finished) breathingCheck.breathTaps else breathingCheck.estimatedBpm
              onSaveVitals(tempF.toDouble(), bpm, combinedRedFlag)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("save_vitals_button"),
          ) {
            Text("Log Vitals Check")
          }
        }
      }
    }

    // 3. Weight Progress Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text("Weight & Growth Target", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
              onClick = onOpenWeightSheet,
              modifier = Modifier.testTag("safety_log_weight_button"),
            ) {
              Text("Update")
            }
          }
          Text(
            text = String.format(Locale.US, "Current: %.2f kg • Expected gain: 20–30 g/day", data.profile.lastWeightKg),
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            text = if (data.feedTargets.nextStageUnlocked) "Unlocked 60–70 ml/feed stage (≥ 3.0 kg)"
            else "At 3.0 kg, per-feed target unlocks from 45–60 ml to 60–70 ml",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    // 4. Sleep Safety & Elder-Friendly Restricted Items Guardrails
    item {
      Text(
        text = "Sleep Safety & Restricted Items",
        style = MaterialTheme.typography.titleLarge,
      )
    }

    items(CharvikKnowledge.safetyChecklist) { rule ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = if (rule.isRestrictedItem) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
          else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        ),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Icon(
            imageVector = if (rule.isRestrictedItem) Icons.Default.Block else Icons.Default.HealthAndSafety,
            contentDescription = null,
            tint = if (rule.isRestrictedItem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          )
          Column {
            Text(rule.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(rule.detail, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}

@Composable
private fun FlagCheckRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    Spacer(modifier = Modifier.width(6.dp))
    Text(label, style = MaterialTheme.typography.bodySmall)
  }
}
