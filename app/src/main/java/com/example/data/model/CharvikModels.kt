package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FeedType(val label: String, val shortLabel: String) {
  BREAST_PLUS_BOTTLE("Breast + Paced Bottle", "Breast + Bottle"),
  BREAST_ONLY("Breastfeed Only", "Breast"),
  BOTTLE_EBM("Expressed Breast Milk (EBM)", "EBM Bottle"),
  BOTTLE_FORMULA("Formula Top-Up", "Formula"),
  ;

  companion object {
    fun safeValueOf(raw: String): FeedType =
      entries.firstOrNull { it.name == raw } ?: BREAST_PLUS_BOTTLE
  }
}

enum class StoolColor(val label: String, val clinicalNote: String, val isWarning: Boolean = false) {
  NONE("No Stool", "Wet diaper or reflux check only"),
  YELLOW_SEEDY("Yellow Seedy", "Normal breastfed newborn stool"),
  MUSTARD("Mustard Paste", "Healthy transitional/milk stool"),
  GREEN("Greenish", "Often normal; watch if frothy or frequent"),
  WATERY("Watery / Diarrhea", "Monitor hydration closely; call doctor if persistent", true),
  HARD("Hard / Pellet", "Sign of possible dehydration or milk intolerance", true),
  MUCOUSY("Mucousy / Blood-streaked", "Discuss with pediatrician if recurring", true),
  ;

  companion object {
    fun safeValueOf(raw: String): StoolColor =
      entries.firstOrNull { it.name == raw } ?: NONE
  }
}

enum class RefluxType(val label: String, val isWarning: Boolean = false) {
  NONE("None", false),
  MILD_MUCUS("Mild Silent Reflux / Throat Mucus", false),
  SMALL_SPIT("Small Posset / Spit-up", false),
  PROJECTILE("Forceful / Projectile Vomiting", true),
  ;

  companion object {
    fun safeValueOf(raw: String): RefluxType =
      entries.firstOrNull { it.name == raw } ?: NONE
  }
}

enum class CareCategory {
  PUMP,
  NASAL_SUCTION,
  WEIGHT,
  VITALS,
  ROUTINE,
  ;

  companion object {
    fun safeValueOf(raw: String): CareCategory =
      entries.firstOrNull { it.name == raw } ?: ROUTINE
  }
}

enum class VaccineStatus {
  PENDING,
  DONE,
  ;

  companion object {
    fun safeValueOf(raw: String): VaccineStatus =
      entries.firstOrNull { it.name == raw } ?: PENDING
  }
}

enum class AlarmType(val label: String) {
  FEED("Next Feed Alarm"),
  PUMP("Pumping Session"),
  VITAMIN_D_SUN("Morning Sun & Vitamin D3"),
  VACCINE("Apollo Vaccination Due"),
  CUSTOM("Custom Care Alarm"),
  ;

  companion object {
    fun safeValueOf(raw: String): AlarmType =
      entries.firstOrNull { it.name == raw } ?: CUSTOM
  }
}

@Entity(tableName = "baby_profile")
data class BabyProfile(
  @PrimaryKey val id: String = "charvik",
  val babyName: String = "Charvik",
  val dob: String = "2026-10-26",
  val termStatus: String = "late_preterm",
  val dueDateReached: Boolean = true,
  val lastWeightKg: Double = 2.67,
  val feedIntervalHours: Double = 2.5,
  val postFeedUprightMin: Long = 25L,
)

@Entity(tableName = "feed_logs")
data class FeedLog(
  @PrimaryKey val id: String,
  val feedType: String = FeedType.BREAST_PLUS_BOTTLE.name,
  val breastMinutes: Long = 0L,
  val bottleMl: Long = 0L,
  val burpedMidFeed: Boolean = false,
  val uprightHoldMinutes: Long = 25L,
  val salineBeforeFeed: Boolean = false,
  val note: String? = null,
  val loggedAtMillis: Long = 0L,
)

@Entity(tableName = "digestion_logs")
data class DigestionLog(
  @PrimaryKey val id: String,
  val wetDiaper: Boolean = true,
  val stoolPassed: Boolean = false,
  val stoolColor: String = StoolColor.NONE.name,
  val refluxOrSpitUp: String = RefluxType.NONE.name,
  val hiccups: Boolean = false,
  val gasOrDiscomfort: Boolean = false,
  val note: String? = null,
  val loggedAtMillis: Long = 0L,
)

@Entity(tableName = "care_events")
data class CareEvent(
  @PrimaryKey val id: String,
  val category: String = CareCategory.ROUTINE.name,
  val title: String = "",
  val numericValue: Double = 0.0,
  val secondaryValue: Double = 0.0,
  val flagAlert: Boolean = false,
  val dateKey: String = "",
  val note: String? = null,
  val loggedAtMillis: Long = 0L,
)

@Entity(tableName = "vaccine_records")
data class VaccineRecord(
  @PrimaryKey val id: String,
  val vaccineCode: String = "",
  val name: String = "",
  val stageLabel: String = "",
  val dueDateIso: String = "",
  val status: String = VaccineStatus.PENDING.name,
  val administeredDateIso: String? = null,
  val hospitalNote: String? = null,
  val reminderEnabled: Boolean = true,
)

@Entity(tableName = "care_alarms")
data class CareAlarm(
  @PrimaryKey val id: String,
  val alarmType: String = AlarmType.FEED.name,
  val label: String = "",
  val hour: Long = 7L,
  val minute: Long = 30L,
  val triggerAtMillis: Long = 0L,
  val repeatDaily: Boolean = false,
  val enabled: Boolean = true,
)
