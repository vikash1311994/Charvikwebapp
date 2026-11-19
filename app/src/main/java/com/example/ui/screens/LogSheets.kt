package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AlarmType
import com.example.data.model.CharvikKnowledge
import com.example.data.model.FeedTargets
import com.example.data.model.FeedType
import com.example.data.model.RefluxType
import com.example.data.model.StoolColor
import com.example.ui.theme.JetBrainsMonoFamily
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogFeedSheet(
  targets: FeedTargets,
  onDismiss: () -> Unit,
  onSave: (
    feedType: FeedType,
    breastMin: Long,
    bottleMl: Long,
    burpedMidFeed: Boolean,
    uprightMin: Long,
    salineBefore: Boolean,
    note: String?,
    autoStartRefluxHold: Boolean,
    scheduleNextFeedAlarm: Boolean,
  ) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var feedType by remember { mutableStateOf(FeedType.BREAST_PLUS_BOTTLE) }
  var breastMin by remember { mutableIntStateOf(12) }
  var bottleMl by remember { mutableIntStateOf((targets.perFeedMinMl + targets.perFeedMaxMl) / 2) }
  var burpedMidFeed by remember { mutableStateOf(true) }
  var salineBefore by remember { mutableStateOf(false) }
  var uprightMin by remember { mutableIntStateOf(25) }
  var autoStartReflux by remember { mutableStateOf(true) }
  var scheduleAlarm by remember { mutableStateOf(true) }
  var note by remember { mutableStateOf("") }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text("Log Charvik's Feed", style = MaterialTheme.typography.headlineSmall)
      Text(
        "Target: ${targets.perFeedMinMl}–${targets.perFeedMaxMl} ml • Breast first (10–15m) → Paced horizontal bottle",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FeedType.entries.forEach { type ->
          FilterChip(
            selected = feedType == type,
            onClick = { feedType = type },
            label = { Text(type.shortLabel) },
          )
        }
      }

      if (feedType == FeedType.BREAST_PLUS_BOTTLE || feedType == FeedType.BREAST_ONLY) {
        Text(
          "Breastfeeding Duration: $breastMin min (target 10–15m)",
          style = MaterialTheme.typography.titleSmall,
        )
        Slider(
          value = breastMin.toFloat(),
          onValueChange = { breastMin = it.roundToInt() },
          valueRange = 0f..35f,
        )
      }

      if (feedType != FeedType.BREAST_ONLY) {
        Text(
          "Bottle Volume: $bottleMl ml (target ${targets.perFeedMinMl}–${targets.perFeedMaxMl} ml)",
          style = MaterialTheme.typography.titleSmall,
        )
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(30, 45, 50, 55, 60, 65, 70, 80).forEach { ml ->
            FilterChip(
              selected = bottleMl == ml,
              onClick = { bottleMl = ml },
              label = { Text("$ml ml") },
            )
          }
        }
        Slider(
          value = bottleMl.toFloat(),
          onValueChange = { bottleMl = it.roundToInt() },
          valueRange = 0f..120f,
        )
      }

      SheetToggleRow(
        title = "Mid-feed burp at 25–30 ml",
        checked = burpedMidFeed,
        onCheckedChange = { burpedMidFeed = it },
      )
      SheetToggleRow(
        title = "Noblok saline drops 10m pre-feed",
        checked = salineBefore,
        onCheckedChange = { salineBefore = it },
      )
      SheetToggleRow(
        title = "Auto-start ${uprightMin}m 45° reflux hold timer",
        checked = autoStartReflux,
        onCheckedChange = { autoStartReflux = it },
      )
      SheetToggleRow(
        title = "Set next feed alarm sound (2.5 hrs)",
        checked = scheduleAlarm,
        onCheckedChange = { scheduleAlarm = it },
      )

      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Optional feed note (latch, sleepy, hiccups)") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2,
      )

      Button(
        onClick = {
          onSave(
            feedType,
            if (feedType == FeedType.BOTTLE_EBM || feedType == FeedType.BOTTLE_FORMULA) 0L else breastMin.toLong(),
            if (feedType == FeedType.BREAST_ONLY) 0L else bottleMl.toLong(),
            burpedMidFeed,
            uprightMin.toLong(),
            salineBefore,
            note,
            autoStartReflux,
            scheduleAlarm,
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_feed_button"),
      ) {
        Text("Save Feed Log")
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogDigestionSheet(
  onDismiss: () -> Unit,
  onSave: (
    wetDiaper: Boolean,
    stoolPassed: Boolean,
    stoolColor: StoolColor,
    refluxType: RefluxType,
    hiccups: Boolean,
    gas: Boolean,
    note: String?,
  ) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var wetDiaper by remember { mutableStateOf(true) }
  var stoolPassed by remember { mutableStateOf(false) }
  var stoolColor by remember { mutableStateOf(StoolColor.YELLOW_SEEDY) }
  var refluxType by remember { mutableStateOf(RefluxType.NONE) }
  var hiccups by remember { mutableStateOf(false) }
  var gas by remember { mutableStateOf(false) }
  var note by remember { mutableStateOf("") }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text("Log Diaper & Digestion", style = MaterialTheme.typography.headlineSmall)
      Text(
        "Hydration check: ≥ 6 wet diapers in 24 hrs",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      SheetToggleRow("Wet Diaper", wetDiaper) { wetDiaper = it }
      SheetToggleRow("Stool / Poop Passed", stoolPassed) { stoolPassed = it }

      if (stoolPassed) {
        Text("Stool Appearance", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StoolColor.entries.filter { it != StoolColor.NONE }.forEach { color ->
            FilterChip(
              selected = stoolColor == color,
              onClick = { stoolColor = color },
              label = { Text(color.label) },
            )
          }
        }
        Text(
          stoolColor.clinicalNote,
          style = MaterialTheme.typography.bodySmall,
          color = if (stoolColor.isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
      }

      Text("Silent Reflux / Spit-up", style = MaterialTheme.typography.titleSmall)
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RefluxType.entries.forEach { r ->
          FilterChip(
            selected = refluxType == r,
            onClick = { refluxType = r },
            label = { Text(r.label) },
          )
        }
      }

      SheetToggleRow("Immature diaphragm hiccups", hiccups) { hiccups = it }
      SheetToggleRow("Gas / straining before stool", gas) { gas = it }

      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Digestion note (optional)") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2,
      )

      Button(
        onClick = {
          onSave(wetDiaper, stoolPassed, stoolColor, refluxType, hiccups, gas, note)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_digestion_button"),
      ) {
        Text("Save Digestion Log")
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWeightSheet(
  currentWeightKg: Double,
  onDismiss: () -> Unit,
  onSave: (weightKg: Double, note: String?) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var weight by remember { mutableFloatStateOf(currentWeightKg.toFloat().coerceIn(2.0f, 8.0f)) }
  var note by remember { mutableStateOf("") }
  val previewTargets = CharvikKnowledge.computeFeedTargets(weight.toDouble())

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text("Update Charvik's Weight", style = MaterialTheme.typography.headlineSmall)
      Text(
        text = String.format(Locale.US, "%.2f kg", weight),
        style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
        color = MaterialTheme.colorScheme.primary,
      )
      Slider(
        value = weight,
        onValueChange = { weight = (it * 100f).roundToInt() / 100f },
        valueRange = 2.0f..6.5f,
      )
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            "Updated Daily Fluid Target: ${previewTargets.dailyFluidMinMl}–${previewTargets.dailyFluidMaxMl} ml/day",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
          Text(
            if (previewTargets.nextStageUnlocked) "60–70 ml/feed stage unlocked!"
            else "Current feed target: 45–60 ml/feed (60–70 ml unlocks at 3.00 kg)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Clinic / home scale note (optional)") },
        modifier = Modifier.fillMaxWidth(),
      )
      Button(
        onClick = { onSave(weight.toDouble(), note) },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_weight_button"),
      ) {
        Text("Save Weight")
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAlarmSheet(
  onDismiss: () -> Unit,
  onSave: (AlarmType, String, Int, Int, Boolean) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var alarmType by remember { mutableStateOf(AlarmType.FEED) }
  var label by remember { mutableStateOf("Charvik Feed Reminder") }
  var hour by remember { mutableIntStateOf(7) }
  var minute by remember { mutableIntStateOf(30) }
  var repeatDaily by remember { mutableStateOf(true) }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text("New Alarm with Sound", style = MaterialTheme.typography.headlineSmall)
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AlarmType.entries.forEach { t ->
          FilterChip(
            selected = alarmType == t,
            onClick = {
              alarmType = t
              label = t.label
            },
            label = { Text(t.label) },
          )
        }
      }
      OutlinedTextField(
        value = label,
        onValueChange = { label = it },
        label = { Text("Alarm label") },
        modifier = Modifier.fillMaxWidth(),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Time: ${CharvikKnowledge.formatClock(hour, minute)}", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(onClick = { hour = (hour + 1) % 24 }) { Text("+1h") }
          Button(onClick = { minute = (minute + 15) % 60 }) { Text("+15m") }
        }
      }
      SheetToggleRow("Repeat every day", repeatDaily) { repeatDaily = it }
      Button(
        onClick = {
          onSave(alarmType, label.ifBlank { alarmType.label }, hour, minute, repeatDaily)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_alarm_button"),
      ) {
        Text("Schedule Alarm")
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun SheetToggleRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier
        .weight(1f)
        .padding(end = 8.dp),
    )
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
