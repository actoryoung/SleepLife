package com.sleeplife.app.data.repository

import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.core.validators.SleepValidator
import com.sleeplife.app.core.validators.ValidationResult
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

    suspend fun insertSleepRecordWithValidation(sleepRecord: SleepRecord): Result<Long> {
        val validationResult = SleepValidator.validateSleepRecord(sleepRecord)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(sleepRecordDao.insertSleepRecord(sleepRecord))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("插入失败", e))
        }
    }

    suspend fun updateSleepRecordWithValidation(sleepRecord: SleepRecord): Result<Unit> {
        val validationResult = SleepValidator.validateSleepRecord(sleepRecord)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(sleepRecordDao.updateSleepRecord(sleepRecord))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("更新失败", e))
        }
    }

    suspend fun deleteSleepRecordsWithValidation(id: Long): Result<Unit> {
        return try {
            Result.Success(sleepRecordDao.deleteSleepRecordById(id))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("删除失败", e))
        }
    }
}
