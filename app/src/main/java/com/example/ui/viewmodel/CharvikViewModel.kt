package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmSoundPlayer
import com.example.alarm.CharvikAlarmScheduler
import com.example.data.local.CharvikRepository
import com.example.data.model.AgeSummary
import com.example.data.model.AlarmType
import com.example.data.model.BabyProfile
import com.example.data.model.CareAlarm
import com.example.data.model.CareCategory
import com.example.data.model.CareEvent
import com.example.data.model.CharvikKnowledge
import com.example.data.model.DigestionLog
import com.example.data.model.FeedLog
import com.example.data.model.FeedTargets
import com.example.data.model.FeedType
import com.example.data.model.RefluxType
import com.example.data.model.StoolColor
import com.example.data.model.VaccineRecord
import com.example.data.model.VaccineStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class ActiveTimerState(
  val running: Boolean = false,
  val remainingSeconds: Int = 0,
  val totalSeconds: Int = 0,
  val label: String = "",
  val completedAlert: Boolean = false,
)

data class PumpTimerState(
  val running: Boolean = false,
  val elapsedSeconds: Int = 0,
  val targetSeconds: Int = 12 * 60,
  val completedAlert: Boolean = false,
) {
  val inStimulationPhase: Boolean get() = elapsedSeconds < 120
  val remainingSeconds: Int get() = (targetSeconds - elapsedSeconds).coerceAtLeast(0)
}

data class BreathingCheckState(
  val running: Boolean = false,
  val remainingSeconds: Int = 60,
  val breathTaps: Int = 0,
  val finished: Boolean = false,
  val retractions: Boolean = false,
  val nasalFlaring: Boolean = false,
  val grunting: Boolean = false,
) {
  val estimatedBpm: Int
    get() {
      val elapsed = (60 - remainingSeconds).coerceAtLeast(1)
      return if (finished) breathTaps else ((breathTaps * 60f) / elapsed).toInt()
    }

  val hasRedFlag: Boolean
    get() = (finished && breathTaps > CharvikKnowledge.MAX_RESTING_BPM) ||
      retractions || nasalFlaring || grunting
}

data class CharvikDashboardData(
  val profile: BabyProfile,
  val ageSummary: AgeSummary,
  val feedTargets: FeedTargets,
  val feeds: List<FeedLog>,
  val digestionLogs: List<DigestionLog>,
  val careEvents: List<CareEvent>,
  val vaccines: List<VaccineRecord>,
  val alarms: List<CareAlarm>,
  val todayBottleMl: Int,
  val todayFeedCount: Int,
  val lastFeedMillis: Long?,
  val wetDiapersLast24h: Int,
  val stoolsLast24h: Int,
  val suctionsToday: Int,
  val completedRoutineKeysToday: Set<String>,
  val latestWeightEvents: List<CareEvent>,
  val latestVitalsEvent: CareEvent?,
)

class CharvikViewModel(
  private val repository: CharvikRepository,
) : ViewModel() {

  private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
  val nowMillis: StateFlow<Long> = _nowMillis.asStateFlow()

  private val _refluxTimer = MutableStateFlow(
    ActiveTimerState(
      running = false,
      remainingSeconds = 25 * 60,
      totalSeconds = 25 * 60,
      label = "45° Upright Reflux Hold",
    )
  )
  val refluxTimer: StateFlow<ActiveTimerState> = _refluxTimer.asStateFlow()

  private val _pumpTimer = MutableStateFlow(PumpTimerState())
  val pumpTimer: StateFlow<PumpTimerState> = _pumpTimer.asStateFlow()

  private val _breathingCheck = MutableStateFlow(BreathingCheckState())
  val breathingCheck: StateFlow<BreathingCheckState> = _breathingCheck.asStateFlow()

  private val _actionBanner = MutableStateFlow<String?>(null)
  val actionBanner: StateFlow<String?> = _actionBanner.asStateFlow()

  private var refluxJob: Job? = null
  private var pumpJob: Job? = null
  private var breathingJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
    }
    viewModelScope.launch {
      while (true) {
        delay(30_000L)
        _nowMillis.value = System.currentTimeMillis()
      }
    }
  }

  val dashboardState: StateFlow<UiState<CharvikDashboardData>> = combine(
    repository.observeProfile(),
    repository.observeFeeds(),
    repository.observeDigestionLogs(),
    repository.observeCareEvents(),
    repository.observeVaccines(),
    repository.observeAlarms(),
    _nowMillis,
  ) { values ->
    val profile = (values[0] as? BabyProfile) ?: BabyProfile()
    @Suppress("UNCHECKED_CAST")
    val feeds = values[1] as List<FeedLog>
    @Suppress("UNCHECKED_CAST")
    val digestion = values[2] as List<DigestionLog>
    @Suppress("UNCHECKED_CAST")
    val events = values[3] as List<CareEvent>
    @Suppress("UNCHECKED_CAST")
    val vaccines = values[4] as List<VaccineRecord>
    @Suppress("UNCHECKED_CAST")
    val alarms = values[5] as List<CareAlarm>
    val now = values[6] as Long

    val todayKey = CharvikKnowledge.todayDateKey(now)
    val cutoff24h = now - 24L * 60L * 60L * 1000L

    val todayFeeds = feeds.filter { CharvikKnowledge.todayDateKey(it.loggedAtMillis) == todayKey }
    val todayBottleMl = todayFeeds.sumOf { it.bottleMl.toInt() }
    val lastFeedMillis = feeds.maxOfOrNull { it.loggedAtMillis }

    val logs24h = digestion.filter { it.loggedAtMillis >= cutoff24h }
    val wet24h = logs24h.count { it.wetDiaper }
    val stools24h = logs24h.count { it.stoolPassed }

    val suctionsToday = events.count {
      it.category == CareCategory.NASAL_SUCTION.name && it.dateKey == todayKey
    }
    val completedRoutineKeys = events
      .filter { it.category == CareCategory.ROUTINE.name && it.dateKey == todayKey }
      .map { it.title }
      .toSet()

    val weightEvents = events.filter { it.category == CareCategory.WEIGHT.name }
    val latestVitals = events.firstOrNull { it.category == CareCategory.VITALS.name }

    val ageSummary = CharvikKnowledge.calculateAge(profile.dob, now)
    val targets = CharvikKnowledge.computeFeedTargets(profile.lastWeightKg)

    UiState.Success(
      CharvikDashboardData(
        profile = profile,
        ageSummary = ageSummary,
        feedTargets = targets,
        feeds = feeds,
        digestionLogs = digestion,
        careEvents = events,
        vaccines = vaccines,
        alarms = alarms,
        todayBottleMl = todayBottleMl,
        todayFeedCount = todayFeeds.size,
        lastFeedMillis = lastFeedMillis,
        wetDiapersLast24h = wet24h,
        stoolsLast24h = stools24h,
        suctionsToday = suctionsToday,
        completedRoutineKeysToday = completedRoutineKeys,
        latestWeightEvents = weightEvents,
        latestVitalsEvent = latestVitals,
      )
    ) as UiState<CharvikDashboardData>
  }.catch { error ->
    if (error is CancellationException) throw error
    Log.w(TAG, "Dashboard stream error", error)
    emit(UiState.Error(error.message ?: "Unable to load Charvik logs."))
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = UiState.Loading,
  )

  fun clearBanner() {
    _actionBanner.value = null
  }

  fun logFeed(
    context: Context,
    feedType: FeedType,
    breastMinutes: Long,
    bottleMl: Long,
    burpedMidFeed: Boolean,
    uprightHoldMinutes: Long,
    salineBeforeFeed: Boolean,
    note: String?,
    autoStartRefluxHold: Boolean,
    scheduleNextFeedAlarm: Boolean,
    intervalHours: Double,
  ) {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val res = repository.logFeed(
        feedType = feedType,
        breastMinutes = breastMinutes,
        bottleMl = bottleMl,
        burpedMidFeed = burpedMidFeed,
        uprightHoldMinutes = uprightHoldMinutes,
        salineBeforeFeed = salineBeforeFeed,
        note = note,
        loggedAtMillis = now,
      )
      if (res.isSuccess) {
        _nowMillis.value = System.currentTimeMillis()
        if (autoStartRefluxHold) {
          startRefluxTimer(context, uprightHoldMinutes.toInt().coerceIn(20, 30))
        }
        if (scheduleNextFeedAlarm) {
          val triggerAt = now + (intervalHours * 60.0 * 60.0 * 1000.0).toLong()
          CharvikAlarmScheduler.scheduleOneShotAlarm(
            context = context,
            alarmId = "next_feed_alarm",
            triggerAtMillis = triggerAt,
            title = "Charvik Feed Due",
            body = "It has been ${intervalHours}h since the last feed. Target: 45–60 ml (breast first, mid-feed burp at 30 ml).",
            alarmType = AlarmType.FEED.name,
          )
          val cal = java.util.Calendar.getInstance().apply { timeInMillis = triggerAt }
          repository.upsertAlarm(
            alarmId = "next_feed_alarm",
            alarmType = AlarmType.FEED,
            label = "Next Feed (${intervalHours}h interval)",
            hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
            minute = cal.get(java.util.Calendar.MINUTE),
            triggerAtMillis = triggerAt,
            repeatDaily = false,
            enabled = true,
          )
        }
        _actionBanner.value = buildString {
          append("Feed saved")
          if (autoStartRefluxHold) append(" • ${uprightHoldMinutes}m reflux hold started")
          if (scheduleNextFeedAlarm) append(" • Next feed alarm set")
        }
      } else {
        _actionBanner.value = res.exceptionOrNull()?.message ?: "Failed to save feed."
      }
    }
  }

  fun logDigestion(
    wetDiaper: Boolean,
    stoolPassed: Boolean,
    stoolColor: StoolColor,
    refluxOrSpitUp: RefluxType,
    hiccups: Boolean,
    gasOrDiscomfort: Boolean,
    note: String?,
  ) {
    viewModelScope.launch {
      val res = repository.logDigestion(
        wetDiaper = wetDiaper,
        stoolPassed = stoolPassed,
        stoolColor = stoolColor,
        refluxOrSpitUp = refluxOrSpitUp,
        hiccups = hiccups,
        gasOrDiscomfort = gasOrDiscomfort,
        note = note,
      )
      _actionBanner.value = if (res.isSuccess) {
        _nowMillis.value = System.currentTimeMillis()
        "Digestion & diaper log saved"
      } else {
        res.exceptionOrNull()?.message ?: "Failed to save digestion log."
      }
    }
  }

  fun toggleRoutineItem(key: String, currentlyCompleted: Boolean, existingEvents: List<CareEvent>) {
    viewModelScope.launch {
      val todayKey = CharvikKnowledge.todayDateKey()
      if (currentlyCompleted) {
        val toRemove = existingEvents.firstOrNull {
          it.category == CareCategory.ROUTINE.name && it.dateKey == todayKey && it.title == key
        }
        if (toRemove != null && toRemove.id.isNotBlank()) {
          repository.deleteCareEvent(toRemove.id)
        }
      } else {
        repository.logCareEvent(
          category = CareCategory.ROUTINE,
          title = key,
          numericValue = 1.0,
          customDocId = "routine_${todayKey}_$key",
        )
      }
    }
  }

  fun logNasalSuction(currentCountToday: Int) {
    viewModelScope.launch {
      val nextCount = currentCountToday + 1
      val res = repository.logCareEvent(
        category = CareCategory.NASAL_SUCTION,
        title = "Noblok Saline & Gentle Suction",
        numericValue = nextCount.toDouble(),
        flagAlert = nextCount > CharvikKnowledge.MAX_NASAL_SUCTIONS_24H,
      )
      _actionBanner.value = if (res.isSuccess) {
        if (nextCount >= CharvikKnowledge.MAX_NASAL_SUCTIONS_24H) {
          "Nasal suction #$nextCount logged — daily limit (4/day) reached"
        } else {
          "Nasal saline/suction #$nextCount logged"
        }
      } else {
        res.exceptionOrNull()?.message ?: "Could not save nasal care log."
      }
    }
  }

  fun logWeight(weightKg: Double, note: String?) {
    viewModelScope.launch {
      val res = repository.logCareEvent(
        category = CareCategory.WEIGHT,
        title = "Weight Check",
        numericValue = weightKg,
        note = note,
      )
      _actionBanner.value = if (res.isSuccess) {
        if (weightKg >= 3.0) {
          "Weight ${String.format(java.util.Locale.US, "%.2f", weightKg)} kg saved — 60–70 ml/feed stage unlocked!"
        } else {
          "Weight ${String.format(java.util.Locale.US, "%.2f", weightKg)} kg saved"
        }
      } else {
        res.exceptionOrNull()?.message ?: "Failed to save weight."
      }
    }
  }

  fun logVitalsCheck(tempFahrenheit: Double, breathingBpm: Int, redFlagTriggered: Boolean, note: String?) {
    viewModelScope.launch {
      val res = repository.logCareEvent(
        category = CareCategory.VITALS,
        title = "Vitals & Safety Check",
        numericValue = tempFahrenheit,
        secondaryValue = breathingBpm.toDouble(),
        flagAlert = redFlagTriggered,
        note = note,
      )
      _actionBanner.value = if (res.isSuccess) {
        if (redFlagTriggered) "RED-FLAG VITALS LOGGED — Contact pediatrician immediately"
        else "Vitals check saved (${tempFahrenheit}°F, $breathingBpm bpm)"
      } else {
        res.exceptionOrNull()?.message ?: "Failed to save vitals check."
      }
    }
  }

  fun savePumpSession(context: Context, minutes: Int, pumpedMl: Int, scheduleNextPump: Boolean) {
    viewModelScope.launch {
      val res = repository.logCareEvent(
        category = CareCategory.PUMP,
        title = "Pumping Session",
        numericValue = minutes.toDouble(),
        secondaryValue = pumpedMl.toDouble(),
      )
      if (res.isSuccess && scheduleNextPump) {
        val triggerAt = System.currentTimeMillis() + (165L * 60L * 1000L)
        CharvikAlarmScheduler.scheduleOneShotAlarm(
          context = context,
          alarmId = "next_pump_alarm",
          triggerAtMillis = triggerAt,
          title = "Pumping Session Due",
          body = "2.5–3 hrs since last pump. Target 10–15 min (2m stimulation + pain-free expression).",
          alarmType = AlarmType.PUMP.name,
        )
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = triggerAt }
        repository.upsertAlarm(
          alarmId = "next_pump_alarm",
          alarmType = AlarmType.PUMP,
          label = "Next Pump Session (2h 45m)",
          hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
          minute = cal.get(java.util.Calendar.MINUTE),
          triggerAtMillis = triggerAt,
          repeatDaily = false,
          enabled = true,
        )
      }
      _actionBanner.value = if (res.isSuccess) {
        "Pump session (${minutes}m, ${pumpedMl} ml) saved"
      } else {
        res.exceptionOrNull()?.message ?: "Failed to save pump session."
      }
    }
  }

  fun startRefluxTimer(context: Context, minutes: Int = 25) {
    refluxJob?.cancel()
    AlarmSoundPlayer.stop()
    val totalSec = minutes.coerceIn(1, 60) * 60
    _refluxTimer.value = ActiveTimerState(
      running = true,
      remainingSeconds = totalSec,
      totalSeconds = totalSec,
      label = "45° Upright Reflux Hold (${minutes}m)",
      completedAlert = false,
    )
    CharvikAlarmScheduler.scheduleOneShotAlarm(
      context = context,
      alarmId = "reflux_hold_timer",
      triggerAtMillis = System.currentTimeMillis() + totalSec * 1000L,
      title = "Reflux Upright Hold Complete",
      body = "${minutes} minutes upright hold finished. Charvik can now be laid flat on his back on a firm mattress.",
      alarmType = AlarmType.CUSTOM.name,
    )
    refluxJob = viewModelScope.launch {
      while (_refluxTimer.value.running && _refluxTimer.value.remainingSeconds > 0) {
        delay(1000L)
        val next = _refluxTimer.value.remainingSeconds - 1
        _refluxTimer.value = _refluxTimer.value.copy(remainingSeconds = next)
      }
      if (_refluxTimer.value.remainingSeconds == 0 && _refluxTimer.value.running) {
        _refluxTimer.value = _refluxTimer.value.copy(running = false, completedAlert = true)
        AlarmSoundPlayer.startLoopingAlarm(context, "Reflux Upright Hold Complete")
      }
    }
  }

  fun stopRefluxTimer(context: Context) {
    refluxJob?.cancel()
    CharvikAlarmScheduler.cancelAlarm(context, "reflux_hold_timer")
    AlarmSoundPlayer.stop()
    _refluxTimer.value = _refluxTimer.value.copy(running = false, completedAlert = false)
  }

  fun startPumpTimer(context: Context, targetMinutes: Int = 12) {
    pumpJob?.cancel()
    AlarmSoundPlayer.stop()
    val targetSec = targetMinutes.coerceIn(5, 15) * 60
    _pumpTimer.value = PumpTimerState(
      running = true,
      elapsedSeconds = 0,
      targetSeconds = targetSec,
      completedAlert = false,
    )
    pumpJob = viewModelScope.launch {
      while (_pumpTimer.value.running && _pumpTimer.value.elapsedSeconds < targetSec) {
        delay(1000L)
        val nextElapsed = _pumpTimer.value.elapsedSeconds + 1
        if (nextElapsed == 120) {
          AlarmSoundPlayer.playSingleChime(context)
        }
        _pumpTimer.value = _pumpTimer.value.copy(elapsedSeconds = nextElapsed)
      }
      if (_pumpTimer.value.elapsedSeconds >= targetSec && _pumpTimer.value.running) {
        _pumpTimer.value = _pumpTimer.value.copy(running = false, completedAlert = true)
        AlarmSoundPlayer.startLoopingAlarm(context, "Pumping Session Complete (${targetMinutes}m)")
      }
    }
  }

  fun stopPumpTimer() {
    pumpJob?.cancel()
    AlarmSoundPlayer.stop()
    _pumpTimer.value = _pumpTimer.value.copy(running = false, completedAlert = false)
  }

  fun startBreathingCounter(context: Context) {
    breathingJob?.cancel()
    _breathingCheck.value = BreathingCheckState(
      running = true,
      remainingSeconds = 60,
      breathTaps = 0,
      finished = false,
    )
    breathingJob = viewModelScope.launch {
      while (_breathingCheck.value.running && _breathingCheck.value.remainingSeconds > 0) {
        delay(1000L)
        _breathingCheck.value = _breathingCheck.value.copy(
          remainingSeconds = _breathingCheck.value.remainingSeconds - 1
        )
      }
      if (_breathingCheck.value.remainingSeconds == 0 && _breathingCheck.value.running) {
        _breathingCheck.value = _breathingCheck.value.copy(running = false, finished = true)
        if (_breathingCheck.value.hasRedFlag) {
          AlarmSoundPlayer.startLoopingAlarm(context, "Breathing Red-Flag Alert (>60 bpm)")
        } else {
          AlarmSoundPlayer.playSingleChime(context)
        }
      }
    }
  }

  fun recordBreathTap() {
    if (_breathingCheck.value.running) {
      _breathingCheck.value = _breathingCheck.value.copy(
        breathTaps = _breathingCheck.value.breathTaps + 1
      )
    }
  }

  fun updateBreathingFlags(retractions: Boolean, nasalFlaring: Boolean, grunting: Boolean) {
    _breathingCheck.value = _breathingCheck.value.copy(
      retractions = retractions,
      nasalFlaring = nasalFlaring,
      grunting = grunting,
    )
  }

  fun resetBreathingCounter() {
    breathingJob?.cancel()
    AlarmSoundPlayer.stop()
    _breathingCheck.value = BreathingCheckState()
  }

  fun markVaccineStatus(
    context: Context,
    vaccine: VaccineRecord,
    done: Boolean,
    administeredDateIso: String?,
    hospitalNote: String?,
    reminderEnabled: Boolean,
  ) {
    viewModelScope.launch {
      val status = if (done) VaccineStatus.DONE else VaccineStatus.PENDING
      val res = repository.updateVaccine(
        vaccineId = vaccine.id,
        status = status,
        administeredDateIso = administeredDateIso,
        hospitalNote = hospitalNote,
        reminderEnabled = reminderEnabled,
      )
      val alarmId = "vax_${vaccine.vaccineCode}"
      if (done || !reminderEnabled) {
        CharvikAlarmScheduler.cancelAlarm(context, alarmId)
      } else {
        scheduleVaccineAlarm(context, vaccine)
      }
      _actionBanner.value = if (res.isSuccess) {
        "${vaccine.name} updated"
      } else {
        res.exceptionOrNull()?.message ?: "Failed to update vaccine."
      }
    }
  }

  private fun scheduleVaccineAlarm(context: Context, vaccine: VaccineRecord) {
    val days = CharvikKnowledge.daysUntilIso(vaccine.dueDateIso)
    if (days >= 0) {
      val cal = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, days)
        set(java.util.Calendar.HOUR_OF_DAY, 9)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
      }
      if (cal.timeInMillis > System.currentTimeMillis()) {
        CharvikAlarmScheduler.scheduleOneShotAlarm(
          context = context,
          alarmId = "vax_${vaccine.vaccineCode}",
          triggerAtMillis = cal.timeInMillis,
          title = "Apollo Vaccination Due Today",
          body = "${vaccine.name} (${vaccine.stageLabel}) is scheduled for Charvik today.",
          alarmType = AlarmType.VACCINE.name,
        )
      }
    }
  }

  fun toggleAlarm(context: Context, alarm: CareAlarm, enabled: Boolean) {
    viewModelScope.launch {
      val alarmId = alarm.id.ifBlank { "alarm_${System.currentTimeMillis()}" }
      val nextTrigger = if (enabled) {
        if (alarm.repeatDaily) {
          CharvikAlarmScheduler.scheduleDailyAlarm(
            context = context,
            alarmId = alarmId,
            hour = alarm.hour.toInt(),
            minute = alarm.minute.toInt(),
            title = alarm.label,
            body = "Scheduled reminder for Charvik.",
            alarmType = alarm.alarmType,
          )
        } else {
          val trigger = CharvikAlarmScheduler.computeNextDailyTrigger(alarm.hour.toInt(), alarm.minute.toInt())
          CharvikAlarmScheduler.scheduleOneShotAlarm(
            context = context,
            alarmId = alarmId,
            triggerAtMillis = trigger,
            title = alarm.label,
            body = "Scheduled reminder for Charvik.",
            alarmType = alarm.alarmType,
          )
          trigger
        }
      } else {
        CharvikAlarmScheduler.cancelAlarm(context, alarmId)
        alarm.triggerAtMillis
      }

      repository.upsertAlarm(
        alarmId = alarmId,
        alarmType = AlarmType.safeValueOf(alarm.alarmType),
        label = alarm.label,
        hour = alarm.hour.toInt(),
        minute = alarm.minute.toInt(),
        triggerAtMillis = nextTrigger,
        repeatDaily = alarm.repeatDaily,
        enabled = enabled,
      )
    }
  }

  fun createAlarm(
    context: Context,
    alarmType: AlarmType,
    label: String,
    hour: Int,
    minute: Int,
    repeatDaily: Boolean,
  ) {
    viewModelScope.launch {
      val alarmId = "alarm_${System.currentTimeMillis()}"
      val trigger = if (repeatDaily) {
        CharvikAlarmScheduler.scheduleDailyAlarm(
          context = context,
          alarmId = alarmId,
          hour = hour,
          minute = minute,
          title = label,
          body = "Charvik care alert: $label",
          alarmType = alarmType.name,
        )
      } else {
        val next = CharvikAlarmScheduler.computeNextDailyTrigger(hour, minute)
        CharvikAlarmScheduler.scheduleOneShotAlarm(
          context = context,
          alarmId = alarmId,
          triggerAtMillis = next,
          title = label,
          body = "Charvik care alert: $label",
          alarmType = alarmType.name,
        )
        next
      }
      val res = repository.upsertAlarm(
        alarmId = alarmId,
        alarmType = alarmType,
        label = label,
        hour = hour,
        minute = minute,
        triggerAtMillis = trigger,
        repeatDaily = repeatDaily,
        enabled = true,
      )
      _actionBanner.value = if (res.isSuccess) {
        "Alarm set for ${CharvikKnowledge.formatClock(hour, minute)}"
      } else {
        res.exceptionOrNull()?.message ?: "Could not save alarm."
      }
    }
  }

  fun deleteAlarm(context: Context, alarmId: String) {
    viewModelScope.launch {
      CharvikAlarmScheduler.cancelAlarm(context, alarmId)
      repository.deleteAlarm(alarmId)
    }
  }

  fun testAlarmSound(context: Context) {
    AlarmSoundPlayer.startLoopingAlarm(context, "Test Care Alarm")
  }

  fun stopAlarmSound() {
    AlarmSoundPlayer.stop()
  }

  fun snoozeAlarm(context: Context, title: String) {
    AlarmSoundPlayer.stop()
    val snoozeAt = System.currentTimeMillis() + 5L * 60L * 1000L
    CharvikAlarmScheduler.scheduleOneShotAlarm(
      context = context,
      alarmId = "snoozed_alarm",
      triggerAtMillis = snoozeAt,
      title = title.ifBlank { "Charvik Care Alarm (Snoozed)" },
      body = "5-minute snooze completed.",
      alarmType = AlarmType.CUSTOM.name,
    )
    _actionBanner.value = "Alarm snoozed for 5 minutes"
  }

  fun exportCalendarIcs(
    context: Context,
    vaccines: List<VaccineRecord>,
    alarms: List<CareAlarm>,
  ) {
    val res = com.example.alarm.IcsCalendarExporter.exportAndShare(context, vaccines, alarms)
    _actionBanner.value = if (res.isSuccess) {
      "Calendar (.ics) ready — import to Apple/Google Calendar"
    } else {
      res.exceptionOrNull()?.message ?: "Could not export calendar (.ics)"
    }
  }

  override fun onCleared() {
    super.onCleared()
    refluxJob?.cancel()
    pumpJob?.cancel()
    breathingJob?.cancel()
  }

  private companion object {
    const val TAG = "CharvikVM"
  }
}
