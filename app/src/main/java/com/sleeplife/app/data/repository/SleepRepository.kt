package com.sleeplife.app.data.repository

import com.sleeplife.app.data.dao.SleepRecordDao
import com.sleeplife.app.data.entities.SleepRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val sleepRecordDao: SleepRecordDao
) {
    fun getAllSleepRecords(): Flow<List<SleepRecord>> = sleepRecordDao.getAllSleepRecords()

    fun getRecentSleepRecords(limit: Int = 7): Flow<List<SleepRecord>> =
        sleepRecordDao.getRecentSleepRecords(limit)

    suspend fun getSleepRecordById(id: Long): SleepRecord? = sleepRecordDao.getSleepRecordById(id)

    suspend fun getTodaySleep(): SleepRecord? = sleepRecordDao.getTodaySleep()

    suspend fun insertSleepRecord(sleepRecord: SleepRecord): Long =
        sleepRecordDao.insertSleepRecord(sleepRecord)

    suspend fun updateSleepRecord(sleepRecord: SleepRecord) =
        sleepRecordDao.updateSleepRecord(sleepRecord)

    suspend fun deleteSleepRecord(sleepRecord: SleepRecord) =
        sleepRecordDao.deleteSleepRecord(sleepRecord)

    suspend fun deleteSleepRecordById(id: Long) =
        sleepRecordDao.deleteSleepRecordById(id)
}
