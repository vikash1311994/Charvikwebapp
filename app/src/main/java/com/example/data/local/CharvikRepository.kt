package com.example.data.local

import com.example.data.model.AlarmType
import com.example.data.model.BabyProfile
import com.example.data.model.CareAlarm
import com.example.data.model.CareCategory
import com.example.data.model.CareEvent
import com.example.data.model.CharvikKnowledge
import com.example.data.model.DigestionLog
import com.example.data.model.FeedLog
import com.example.data.model.FeedType
import com.example.data.model.RefluxType
import com.example.data.model.StoolColor
import com.example.data.model.VaccineRecord
import com.example.data.model.VaccineStatus
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class CharvikRepository(private val dao: CharvikDao) {

  fun observeProfile(): Flow<BabyProfile?> = dao.observeProfile()
  fun observeFeeds(): Flow<List<FeedLog>> = dao.observeFeeds()
  fun observeDigestionLogs(): Flow<List<DigestionLog>> = dao.observeDigestionLogs()
  fun observeCareEvents(): Flow<List<CareEvent>> = dao.observeCareEvents()
  fun observeVaccines(): Flow<List<VaccineRecord>> = dao.observeVaccines()
  fun observeAlarms(): Flow<List<CareAlarm>> = dao.observeAlarms()

  suspend fun ensureSeeded(): Result<Unit> {
    return try {
      if (dao.getProfileOnce() == null) {
        dao.upsertProfile(
          BabyProfile(
            id = "charvik",
            babyName = "Charvik",
            dob = CharvikKnowledge.CHARVIK_DOB_ISO,
            termStatus = "late_preterm",
            dueDateReached = true,
            lastWeightKg = 2.67,
            feedIntervalHours = 2.5,
            postFeedUprightMin = 25L,
          )
        )
      }

      if (dao.getVaccineCount() == 0) {
        val records = CharvikKnowledge.apolloVaccineSchedule.map { seed ->
          val dueDate = CharvikKnowledge.computeDueDateIso(
            CharvikKnowledge.CHARVIK_DOB_ISO,
            seed.offsetDaysFromBirth,
            seed.offsetMonthsFromBirth,
          )
          VaccineRecord(
            id = seed.code,
            vaccineCode = seed.code,
            name = seed.name,
            stageLabel = seed.stageLabel,
            dueDateIso = dueDate,
            status = if (seed.defaultDone) VaccineStatus.DONE.name else VaccineStatus.PENDING.name,
            administeredDateIso = if (seed.defaultDone) CharvikKnowledge.CHARVIK_DOB_ISO else null,
            hospitalNote = if (seed.defaultDone) "Apollo Hospitals (Birth Dose)" else null,
            reminderEnabled = !seed.defaultDone,
          )
        }
        dao.insertVaccines(records)
      }

      if (dao.getAlarmCount() == 0) {
        dao.upsertAlarm(
          CareAlarm(
            id = "daily_morning_sun_d3",
            alarmType = AlarmType.VITAMIN_D_SUN.name,
            label = "Indirect Morning Sun (7:30–8:30 AM) & Vitamin D3",
            hour = 7L,
            minute = 30L,
            triggerAtMillis = 0L,
            repeatDaily = true,
            enabled = true,
          )
        )
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun logFeed(
    feedType: FeedType,
    breastMinutes: Long,
    bottleMl: Long,
    burpedMidFeed: Boolean,
    uprightHoldMinutes: Long,
    salineBeforeFeed: Boolean,
    note: String?,
    loggedAtMillis: Long = System.currentTimeMillis(),
  ): Result<Unit> {
    return try {
      val cleanNote = note?.trim()?.take(300)?.takeIf { it.isNotEmpty() }
      dao.insertFeed(
        FeedLog(
          id = "feed_${UUID.randomUUID()}",
          feedType = feedType.name,
          breastMinutes = breastMinutes.coerceIn(0L, 180L),
          bottleMl = bottleMl.coerceIn(0L, 400L),
          burpedMidFeed = burpedMidFeed,
          uprightHoldMinutes = uprightHoldMinutes.coerceIn(0L, 120L),
          salineBeforeFeed = salineBeforeFeed,
          note = cleanNote,
          loggedAtMillis = loggedAtMillis,
        )
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun logDigestion(
    wetDiaper: Boolean,
    stoolPassed: Boolean,
    stoolColor: StoolColor,
    refluxOrSpitUp: RefluxType,
    hiccups: Boolean,
    gasOrDiscomfort: Boolean,
    note: String?,
    loggedAtMillis: Long = System.currentTimeMillis(),
  ): Result<Unit> {
    return try {
      val cleanNote = note?.trim()?.take(300)?.takeIf { it.isNotEmpty() }
      dao.insertDigestionLog(
        DigestionLog(
          id = "dig_${UUID.randomUUID()}",
          wetDiaper = wetDiaper,
          stoolPassed = stoolPassed,
          stoolColor = (if (stoolPassed) stoolColor else StoolColor.NONE).name,
          refluxOrSpitUp = refluxOrSpitUp.name,
          hiccups = hiccups,
          gasOrDiscomfort = gasOrDiscomfort,
          note = cleanNote,
          loggedAtMillis = loggedAtMillis,
        )
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun logCareEvent(
    category: CareCategory,
    title: String,
    numericValue: Double,
    secondaryValue: Double = 0.0,
    flagAlert: Boolean = false,
    note: String? = null,
    loggedAtMillis: Long = System.currentTimeMillis(),
    customDocId: String? = null,
  ): Result<Unit> {
    return try {
      val id = customDocId?.takeIf { it.isNotBlank() } ?: "evt_${UUID.randomUUID()}"
      val cleanNote = note?.trim()?.take(300)?.takeIf { it.isNotEmpty() }
      dao.insertCareEvent(
        CareEvent(
          id = id,
          category = category.name,
          title = title.trim().take(100).ifEmpty { category.name },
          numericValue = numericValue.coerceIn(0.0, 10000.0),
          secondaryValue = secondaryValue.coerceIn(0.0, 10000.0),
          flagAlert = flagAlert,
          dateKey = CharvikKnowledge.todayDateKey(loggedAtMillis),
          note = cleanNote,
          loggedAtMillis = loggedAtMillis,
        )
      )
      if (category == CareCategory.WEIGHT && numericValue in 0.5..25.0) {
        dao.updateWeight(numericValue)
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteCareEvent(eventId: String): Result<Unit> {
    return try {
      if (eventId.isNotBlank()) dao.deleteCareEventById(eventId)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateVaccine(
    vaccineId: String,
    status: VaccineStatus,
    administeredDateIso: String?,
    hospitalNote: String?,
    reminderEnabled: Boolean,
  ): Result<Unit> {
    return try {
      dao.updateVaccine(
        vaccineId = vaccineId,
        status = status.name,
        administeredDateIso = administeredDateIso?.trim()?.take(20)?.takeIf { it.isNotEmpty() },
        hospitalNote = hospitalNote?.trim()?.take(300)?.takeIf { it.isNotEmpty() },
        reminderEnabled = reminderEnabled,
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun upsertAlarm(
    alarmId: String,
    alarmType: AlarmType,
    label: String,
    hour: Int,
    minute: Int,
    triggerAtMillis: Long,
    repeatDaily: Boolean,
    enabled: Boolean,
  ): Result<Unit> {
    return try {
      val safeId = alarmId.ifBlank { "alarm_${System.currentTimeMillis()}" }
      dao.upsertAlarm(
        CareAlarm(
          id = safeId,
          alarmType = alarmType.name,
          label = label.trim().take(100),
          hour = hour.toLong().coerceIn(0L, 23L),
          minute = minute.toLong().coerceIn(0L, 59L),
          triggerAtMillis = triggerAtMillis.coerceAtLeast(0L),
          repeatDaily = repeatDaily,
          enabled = enabled,
        )
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteAlarm(alarmId: String): Result<Unit> {
    return try {
      if (alarmId.isNotBlank()) dao.deleteAlarmById(alarmId)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
