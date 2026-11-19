package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class FeedTargets(
  val weightKg: Double,
  val dailyFluidMinMl: Int,
  val dailyFluidMaxMl: Int,
  val perFeedMinMl: Int,
  val perFeedMaxMl: Int,
  val nextStageUnlocked: Boolean,
  val progressTo3Kg: Float,
  val intervalHoursMin: Double,
  val intervalHoursMax: Double,
  val midFeedBurpMl: Int,
  val postFeedUprightMin: Int,
)

data class AgeSummary(
  val daysOld: Int,
  val weeks: Int,
  val remainingDays: Int,
  val dobDisplay: String,
  val correctedNote: String,
)

data class RoutineItemSpec(
  val key: String,
  val title: String,
  val subtitle: String,
  val windowHint: String,
)

data class ApolloVaccineSeed(
  val code: String,
  val name: String,
  val stageLabel: String,
  val offsetDaysFromBirth: Int,
  val offsetMonthsFromBirth: Int = 0,
  val defaultDone: Boolean = false,
  val clinicalPurpose: String,
)

data class SafetyRuleItem(
  val title: String,
  val detail: String,
  val isRestrictedItem: Boolean,
)

object CharvikKnowledge {

  const val CHARVIK_DOB_ISO = "2026-10-26"
  const val MAX_NASAL_SUCTIONS_24H = 4
  const val MIN_WET_DIAPERS_24H = 6
  const val MAX_RESTING_BPM = 60
  const val FEVER_THRESHOLD_F = 100.4
  const val HYPOTHERMIA_THRESHOLD_F = 97.7

  private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
  private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)

  fun todayDateKey(nowMillis: Long = System.currentTimeMillis()): String =
    isoFormat.format(Date(nowMillis))

  fun formatDisplayDate(isoDate: String): String {
    return try {
      val parsed = isoFormat.parse(isoDate)
      if (parsed != null) displayDateFormat.format(parsed) else isoDate
    } catch (_: Exception) {
      isoDate
    }
  }

  fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

  fun formatClock(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
      set(Calendar.MINUTE, minute.coerceIn(0, 59))
    }
    return timeFormat.format(cal.time)
  }

  fun calculateAge(dobIso: String = CHARVIK_DOB_ISO, nowMillis: Long = System.currentTimeMillis()): AgeSummary {
    val dobDate = try {
      isoFormat.parse(dobIso)
    } catch (_: Exception) {
      null
    }
    val dobCal = Calendar.getInstance().apply {
      if (dobDate != null) time = dobDate else set(2026, Calendar.OCTOBER, 26)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val nowCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((nowCal.timeInMillis - dobCal.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
    val weeks = diffDays / 7
    val remDays = diffDays % 7
    return AgeSummary(
      daysOld = diffDays,
      weeks = weeks,
      remainingDays = remDays,
      dobDisplay = formatDisplayDate(dobIso),
      correctedNote = "Late-preterm • Reached 40 wks corrected age Nov 15–16",
    )
  }

  fun computeFeedTargets(weightKg: Double): FeedTargets {
    val safeWeight = weightKg.coerceIn(1.5, 15.0)
    val dailyMin = (safeWeight * 150.0).roundToInt()
    val dailyMax = (safeWeight * 180.0).roundToInt()
    val nextStageUnlocked = safeWeight >= 3.0
    val perFeedMin = if (nextStageUnlocked) 60 else 45
    val perFeedMax = if (nextStageUnlocked) 70 else 60
    val progress = ((safeWeight - 2.2) / (3.0 - 2.2)).toFloat().coerceIn(0f, 1f)
    return FeedTargets(
      weightKg = safeWeight,
      dailyFluidMinMl = dailyMin,
      dailyFluidMaxMl = dailyMax,
      perFeedMinMl = perFeedMin,
      perFeedMaxMl = perFeedMax,
      nextStageUnlocked = nextStageUnlocked,
      progressTo3Kg = progress,
      intervalHoursMin = 2.0,
      intervalHoursMax = 2.5,
      midFeedBurpMl = 30,
      postFeedUprightMin = 25,
    )
  }

  fun computeDueDateIso(dobIso: String, offsetDays: Int, offsetMonths: Int): String {
    val parsed = try {
      isoFormat.parse(dobIso)
    } catch (_: Exception) {
      null
    }
    val cal = Calendar.getInstance().apply {
      if (parsed != null) time = parsed else set(2026, Calendar.OCTOBER, 26)
      if (offsetMonths > 0) add(Calendar.MONTH, offsetMonths)
      if (offsetDays > 0) add(Calendar.DAY_OF_YEAR, offsetDays)
    }
    return isoFormat.format(cal.time)
  }

  fun daysUntilIso(targetIso: String, nowMillis: Long = System.currentTimeMillis()): Int {
    val targetDate = try {
      isoFormat.parse(targetIso)
    } catch (_: Exception) {
      null
    } ?: return 999
    val todayCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val targetCal = Calendar.getInstance().apply {
      time = targetDate
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return ((targetCal.timeInMillis - todayCal.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt()
  }

  val dailyRoutineSpecs = listOf(
    RoutineItemSpec(
      key = "VITAMIN_D3",
      title = "Vitamin D3 Drops",
      subtitle = "Give prescribed daily dose (usually 400 IU)",
      windowHint = "Daily",
    ),
    RoutineItemSpec(
      key = "MORNING_SUN",
      title = "Indirect Morning Sunlight",
      subtitle = "10–15 min soft indirect light near window",
      windowHint = "7:30 – 8:30 AM",
    ),
    RoutineItemSpec(
      key = "FACE_WIPE",
      title = "Plain Water Face Wipe",
      subtitle = "Gentle lukewarm wipe for cheek milia (no soaps/creams)",
      windowHint = "Morning / Bath",
    ),
    RoutineItemSpec(
      key = "HEAD_MASSAGE",
      title = "Light Head Massage",
      subtitle = "2–3 min gentle stroke — ZERO pressure on soft spot",
      windowHint = "Calm alert window",
    ),
  )

  val digestionTips = listOf(
    "Mid-feed burp at 25–30 ml reduces trapped air and prevents silent reflux discomfort.",
    "Keep Charvik upright (~45°) for 20–30 min after every feed before laying flat on his back.",
    "For paced bottle feeding, use slow-flow (SS / 0m+) nipple held horizontal so he sets the pace.",
    "Wait at least 30 minutes after feeding before gentle bicycle-legs or tummy time for gas.",
    "Immature diaphragm hiccups and brief cheek milia are normal in late-preterm newborns — no ghutti or water needed.",
    "Check hydration by wet diapers: target ≥ 6 soaked wet diapers every 24 hours.",
  )

  val safetyChecklist = listOf(
    SafetyRuleItem(
      title = "Safe Sleep Position",
      detail = "Always flat on back on a firm mattress. Zero pillows, loose blankets, wedges, or sleeping on an adult's chest.",
      isRestrictedItem = false,
    ),
    SafetyRuleItem(
      title = "No Direct Sunlight",
      detail = "Only indirect morning light (7:30–8:30 AM) for 10–15 min. Direct sun risks overheating & UV burn.",
      isRestrictedItem = true,
    ),
    SafetyRuleItem(
      title = "No Mustard Oil or Kajal",
      detail = "Never apply mustard oil or kajal (including homemade badam kajal) on skin or eyes.",
      isRestrictedItem = true,
    ),
    SafetyRuleItem(
      title = "No Medicated Nasal Drops",
      detail = "Use only plain saline (Noblok) 1–2 drops/nostril 10 min pre-feed; max 3–4 suctions/day.",
      isRestrictedItem = true,
    ),
    SafetyRuleItem(
      title = "No Water, Honey, or Janam Ghutti",
      detail = "Strictly breast milk / prescribed formula only until 6 months. Honey carries infant botulism risk.",
      isRestrictedItem = true,
    ),
  )

  // Apollo Hospitals / IAP Pediatric Immunization Schedule
  val apolloVaccineSchedule = listOf(
    ApolloVaccineSeed("BCG", "BCG (Tuberculosis)", "At Birth", 0, 0, true, "Protects against severe childhood tuberculosis"),
    ApolloVaccineSeed("OPV_0", "OPV 0 (Oral Polio)", "At Birth", 0, 0, true, "Birth dose oral polio drop"),
    ApolloVaccineSeed("HEPB_1", "Hepatitis B (Birth Dose)", "At Birth", 0, 0, true, "First hepatitis B protection dose"),

    ApolloVaccineSeed("DTWP_1", "DTwP / DTaP 1 (Hexavalent)", "6 Weeks", 42, 0, false, "Diphtheria, Tetanus, Pertussis dose 1"),
    ApolloVaccineSeed("IPV_1", "IPV 1 (Inactivated Polio)", "6 Weeks", 42, 0, false, "Injectable polio dose 1"),
    ApolloVaccineSeed("HIB_1", "Hib 1", "6 Weeks", 42, 0, false, "Haemophilus influenzae type b dose 1"),
    ApolloVaccineSeed("HEPB_2", "Hepatitis B Dose 2", "6 Weeks", 42, 0, false, "Second hepatitis B dose (often in 6-in-1)"),
    ApolloVaccineSeed("ROTA_1", "Rotavirus 1 (Oral)", "6 Weeks", 42, 0, false, "Protects against severe rotavirus diarrhea"),
    ApolloVaccineSeed("PCV_1", "PCV 1 (Pneumococcal)", "6 Weeks", 42, 0, false, "Pneumococcal conjugate vaccine dose 1"),

    ApolloVaccineSeed("DTWP_2", "DTwP / DTaP 2", "10 Weeks", 70, 0, false, "Diphtheria, Tetanus, Pertussis dose 2"),
    ApolloVaccineSeed("IPV_2", "IPV 2", "10 Weeks", 70, 0, false, "Injectable polio dose 2"),
    ApolloVaccineSeed("HIB_2", "Hib 2", "10 Weeks", 70, 0, false, "Haemophilus influenzae type b dose 2"),
    ApolloVaccineSeed("ROTA_2", "Rotavirus 2 (Oral)", "10 Weeks", 70, 0, false, "Rotavirus oral vaccine dose 2"),
    ApolloVaccineSeed("PCV_2", "PCV 2", "10 Weeks", 70, 0, false, "Pneumococcal conjugate vaccine dose 2"),

    ApolloVaccineSeed("DTWP_3", "DTwP / DTaP 3", "14 Weeks", 98, 0, false, "Diphtheria, Tetanus, Pertussis dose 3"),
    ApolloVaccineSeed("IPV_3", "IPV 3", "14 Weeks", 98, 0, false, "Injectable polio dose 3"),
    ApolloVaccineSeed("HIB_3", "Hib 3", "14 Weeks", 98, 0, false, "Haemophilus influenzae type b dose 3"),
    ApolloVaccineSeed("ROTA_3", "Rotavirus 3 (Oral)", "14 Weeks", 98, 0, false, "Rotavirus oral vaccine dose 3"),
    ApolloVaccineSeed("PCV_3", "PCV 3", "14 Weeks", 98, 0, false, "Pneumococcal conjugate vaccine dose 3"),

    ApolloVaccineSeed("FLU_1", "Influenza (IIV) Dose 1", "6 Months", 0, 6, false, "Seasonal flu protection (first priming dose)"),
    ApolloVaccineSeed("FLU_2", "Influenza (IIV) Dose 2", "7 Months", 0, 7, false, "Second priming flu dose (4 weeks after Dose 1)"),
    ApolloVaccineSeed("MMR_1", "MMR 1", "9 Months", 0, 9, false, "Measles, Mumps, Rubella dose 1"),
    ApolloVaccineSeed("TCV_1", "Typhoid Conjugate (TCV)", "9 Months", 0, 9, false, "Single-dose typhoid conjugate vaccine"),
    ApolloVaccineSeed("HEPA_1", "Hepatitis A Dose 1", "12 Months", 0, 12, false, "Hepatitis A primary protection"),
    ApolloVaccineSeed("MMR_2", "MMR 2", "15 Months", 0, 15, false, "Measles, Mumps, Rubella dose 2"),
    ApolloVaccineSeed("VARICELLA_1", "Varicella 1 (Chickenpox)", "15 Months", 0, 15, false, "Chickenpox protection dose 1"),
    ApolloVaccineSeed("PCV_B", "PCV Booster", "15 Months", 0, 15, false, "Pneumococcal booster dose"),
    ApolloVaccineSeed("DTP_B1", "DTwP / DTaP Booster 1 + IPV B1 + Hib B1", "16–18 Months", 0, 17, false, "First childhood booster series"),
    ApolloVaccineSeed("HEPA_2", "Hepatitis A Dose 2", "18 Months", 0, 18, false, "Long-term Hepatitis A immunity"),
  )
}
