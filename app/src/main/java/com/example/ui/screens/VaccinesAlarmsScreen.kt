package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.alarm.CharvikAlarmScheduler
import com.example.data.model.CareAlarm
import com.example.data.model.CharvikKnowledge
import com.example.data.model.VaccineRecord
import com.example.data.model.VaccineStatus
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.CharvikDashboardData

private enum class VaccineFilter(val label: String) {
  PENDING("Due & Upcoming"),
  DONE("Completed"),
  ALL("All"),
}

@Composable
fun VaccinesAlarmsScreen(
  data: CharvikDashboardData,
  isAlarmRinging: Boolean,
  ringingTitle: String,
  onStopRinging: () -> Unit,
  onSnoozeRinging: () -> Unit,
  onTestAlarmSound: () -> Unit,
  onExportCalendarIcs: () -> Unit,
  onOpenAddAlarmSheet: () -> Unit,
  onToggleAlarm: (CareAlarm, Boolean) -> Unit,
  onDeleteAlarm: (String) -> Unit,
  onUpdateVaccine: (
    vaccine: VaccineRecord,
    done: Boolean,
    administeredIso: String?,
    hospitalNote: String?,
    reminderEnabled: Boolean,
  ) -> Unit,
) {
  val context = LocalContext.current
  var selectedFilter by remember { mutableStateOf(VaccineFilter.PENDING) }
  var editingVaccine by remember { mutableStateOf<VaccineRecord?>(null) }

  val canExact = CharvikAlarmScheduler.canScheduleExactAlarms(context)
  val doneCount = data.vaccines.count { it.status == VaccineStatus.DONE.name }
  val filteredVaccines = remember(data.vaccines, selectedFilter) {
    when (selectedFilter) {
      VaccineFilter.PENDING -> data.vaccines.filter { it.status != VaccineStatus.DONE.name }
      VaccineFilter.DONE -> data.vaccines.filter { it.status == VaccineStatus.DONE.name }
      VaccineFilter.ALL -> data.vaccines
    }
  }

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
        text = "Apollo Vaccines & Alarms",
        style = MaterialTheme.typography.headlineMedium,
      )
      Text(
        text = "IAP pediatric schedule anchored to Oct 26, 2026 • $doneCount / ${data.vaccines.size} doses completed",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Alarm Sound & Schedule Controls
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text("Alarm Reminders (With Sound)", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
              onClick = onOpenAddAlarmSheet,
              modifier = Modifier.testTag("add_alarm_button"),
            ) {
              Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add")
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = if (isAlarmRinging) onStopRinging else onTestAlarmSound,
              modifier = Modifier
                .weight(1f)
                .testTag("test_alarm_sound_button"),
            ) {
              Text(if (isAlarmRinging) "Stop Test" else "Test Sound")
            }
            Button(
              onClick = onExportCalendarIcs,
              modifier = Modifier
                .weight(1f)
                .testTag("export_calendar_ics_button"),
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Calendar (.ics)")
            }
          }
          if (!canExact) {
            FilledTonalButton(
              onClick = { CharvikAlarmScheduler.openExactAlarmSettings(context) },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Allow Exact Alarms")
            }
          }

          data.alarms.forEach { alarm ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = CharvikKnowledge.formatClock(alarm.hour.toInt(), alarm.minute.toInt()) +
                      if (alarm.repeatDaily) " • Daily" else " • One-shot",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                  )
                  Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Switch(
                    checked = alarm.enabled,
                    onCheckedChange = { onToggleAlarm(alarm, it) },
                  )
                  IconButton(onClick = { onDeleteAlarm(alarm.id) }) {
                    Icon(
                      Icons.Default.DeleteOutline,
                      contentDescription = "Delete alarm",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Apollo Hospitals Immunization Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        VaccineFilter.entries.forEach { filter ->
          FilterChip(
            selected = selectedFilter == filter,
            onClick = { selectedFilter = filter },
            label = { Text(filter.label) },
          )
        }
      }
    }

    items(filteredVaccines, key = { it.id.ifBlank { it.vaccineCode } }) { vax ->
      val isDone = vax.status == VaccineStatus.DONE.name
      val daysUntil = CharvikKnowledge.daysUntilIso(vax.dueDateIso)
      val isDueSoon = !isDone && daysUntil in -7..21
      val purpose = CharvikKnowledge.apolloVaccineSchedule
        .firstOrNull { it.code == vax.vaccineCode }?.clinicalPurpose ?: "Apollo Hospitals IAP recommended dose"

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = when {
            isDone -> MaterialTheme.colorScheme.surface
            isDueSoon -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surface
          }
        ),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Vaccines,
                contentDescription = null,
                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
              )
              Column {
                Text(vax.name, style = MaterialTheme.typography.titleSmall)
                Text(
                  text = "${vax.stageLabel} • Due ${CharvikKnowledge.formatDisplayDate(vax.dueDateIso)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Surface(
              color = if (isDone) MaterialTheme.colorScheme.primaryContainer
              else if (isDueSoon) MaterialTheme.colorScheme.secondary
              else MaterialTheme.colorScheme.surfaceVariant,
              shape = MaterialTheme.shapes.small,
            ) {
              Text(
                text = when {
                  isDone -> "Done"
                  daysUntil < 0 -> "Overdue ${-daysUntil}d"
                  daysUntil == 0 -> "Due Today"
                  else -> "In ${daysUntil}d"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isDueSoon && !isDone) MaterialTheme.colorScheme.onSecondary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }

          Text(
            text = purpose,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (!vax.hospitalNote.isNullOrBlank()) {
            Text(
              text = "Note: ${vax.hospitalNote} (${vax.administeredDateIso ?: vax.dueDateIso})",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("9 AM Alarm", style = MaterialTheme.typography.labelSmall)
              Spacer(modifier = Modifier.width(6.dp))
              Switch(
                checked = vax.reminderEnabled,
                onCheckedChange = { enabled ->
                  onUpdateVaccine(vax, isDone, vax.administeredDateIso, vax.hospitalNote, enabled)
                },
              )
            }
            FilledTonalButton(
              onClick = { editingVaccine = vax },
              modifier = Modifier.testTag("edit_vaccine_${vax.vaccineCode}"),
            ) {
              Text(if (isDone) "Edit Record" else "Mark Given")
            }
          }
        }
      }
    }

    // Offline On-Device Storage Info Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text("100% Offline & Private", style = MaterialTheme.typography.titleSmall)
            Text(
              text = "All feeds, diapers, vaccines, and alarms are saved directly on your phone—no login or internet needed.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }

  val targetVax = editingVaccine
  if (targetVax != null) {
    var markDone by remember(targetVax) { mutableStateOf(true) }
    var dateIso by remember(targetVax) {
      mutableStateOf(targetVax.administeredDateIso ?: CharvikKnowledge.todayDateKey())
    }
    var hospitalNote by remember(targetVax) {
      mutableStateOf(targetVax.hospitalNote ?: "Apollo Hospitals Pediatrics")
    }
    AlertDialog(
      onDismissRequest = { editingVaccine = null },
      title = { Text(targetVax.name) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Stage: ${targetVax.stageLabel} • Due ${targetVax.dueDateIso}")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Completed / Given")
            Switch(checked = markDone, onCheckedChange = { markDone = it })
          }
          OutlinedTextField(
            value = dateIso,
            onValueChange = { dateIso = it },
            label = { Text("Date given (YYYY-MM-DD)") },
            singleLine = true,
          )
          OutlinedTextField(
            value = hospitalNote,
            onValueChange = { hospitalNote = it },
            label = { Text("Apollo doctor / brand / batch note") },
            maxLines = 2,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateVaccine(targetVax, markDone, dateIso, hospitalNote, !markDone)
            editingVaccine = null
          },
          modifier = Modifier.testTag("confirm_vaccine_dialog_button"),
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingVaccine = null }) {
          Text("Cancel")
        }
      },
    )
  }
}
