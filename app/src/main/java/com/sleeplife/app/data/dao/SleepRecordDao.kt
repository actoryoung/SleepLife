package com.sleeplife.app.data.dao

import androidx.room.*
import com.sleeplife.app.data.entities.SleepRecord

@Dao
interface SleepRecordDao {
    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC")
    fun getAllSleepRecords(): kotlinx.coroutines.flow.Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE id = :id")
    suspend fun getSleepRecordById(id: Long): SleepRecord?

    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSleepRecords(limit: Int = 7): kotlinx.coroutines.flow.Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE startTime LIKE date('now') || '%' AND endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getTodaySleep(): SleepRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepRecord(sleepRecord: SleepRecord): Long

    @Update
    suspend fun updateSleepRecord(sleepRecord: SleepRecord)

    @Delete
    suspend fun deleteSleepRecord(sleepRecord: SleepRecord)

    @Query("DELETE FROM sleep_records WHERE id = :id")
    suspend fun deleteSleepRecordById(id: Long)
}
