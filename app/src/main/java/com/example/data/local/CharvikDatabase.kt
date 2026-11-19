package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BabyProfile
import com.example.data.model.CareAlarm
import com.example.data.model.CareEvent
import com.example.data.model.DigestionLog
import com.example.data.model.FeedLog
import com.example.data.model.VaccineRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CharvikDao {
  @Query("SELECT * FROM baby_profile WHERE id = 'charvik' LIMIT 1")
  fun observeProfile(): Flow<BabyProfile?>

  @Query("SELECT * FROM baby_profile WHERE id = 'charvik' LIMIT 1")
  suspend fun getProfileOnce(): BabyProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProfile(profile: BabyProfile)

  @Query("UPDATE baby_profile SET lastWeightKg = :weightKg WHERE id = 'charvik'")
  suspend fun updateWeight(weightKg: Double)

  @Query("SELECT * FROM feed_logs ORDER BY loggedAtMillis DESC LIMIT 100")
  fun observeFeeds(): Flow<List<FeedLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeed(feed: FeedLog)

  @Query("SELECT * FROM digestion_logs ORDER BY loggedAtMillis DESC LIMIT 100")
  fun observeDigestionLogs(): Flow<List<DigestionLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDigestionLog(log: DigestionLog)

  @Query("SELECT * FROM care_events ORDER BY loggedAtMillis DESC LIMIT 150")
  fun observeCareEvents(): Flow<List<CareEvent>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCareEvent(event: CareEvent)

  @Query("DELETE FROM care_events WHERE id = :eventId")
  suspend fun deleteCareEventById(eventId: String)

  @Query("SELECT * FROM vaccine_records ORDER BY dueDateIso ASC")
  fun observeVaccines(): Flow<List<VaccineRecord>>

  @Query("SELECT COUNT(*) FROM vaccine_records")
  suspend fun getVaccineCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVaccines(vaccines: List<VaccineRecord>)

  @Query(
    """
    UPDATE vaccine_records
    SET status = :status,
        administeredDateIso = :administeredDateIso,
        hospitalNote = :hospitalNote,
        reminderEnabled = :reminderEnabled
    WHERE id = :vaccineId
    """
  )
  suspend fun updateVaccine(
    vaccineId: String,
    status: String,
    administeredDateIso: String?,
    hospitalNote: String?,
    reminderEnabled: Boolean,
  )

  @Query("SELECT * FROM care_alarms ORDER BY hour ASC, minute ASC")
  fun observeAlarms(): Flow<List<CareAlarm>>

  @Query("SELECT COUNT(*) FROM care_alarms")
  suspend fun getAlarmCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAlarm(alarm: CareAlarm)

  @Query("DELETE FROM care_alarms WHERE id = :alarmId")
  suspend fun deleteAlarmById(alarmId: String)
}

@Database(
  entities = [
    BabyProfile::class,
    FeedLog::class,
    DigestionLog::class,
    CareEvent::class,
    VaccineRecord::class,
    CareAlarm::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class CharvikDatabase : RoomDatabase() {
  abstract fun charvikDao(): CharvikDao

  companion object {
    @Volatile
    private var INSTANCE: CharvikDatabase? = null

    fun getInstance(context: Context): CharvikDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          CharvikDatabase::class.java,
          "charvik_local.db",
        ).build().also { INSTANCE = it }
      }
    }
  }
}
