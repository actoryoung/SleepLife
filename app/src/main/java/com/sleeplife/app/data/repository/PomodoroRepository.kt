package com.sleeplife.app.data.repository

import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.core.validators.PomodoroValidator
import com.sleeplife.app.core.validators.ValidationResult
import com.sleeplife.app.data.dao.PomodoroSessionDao
import com.sleeplife.app.data.entities.PomodoroSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroSessionDao: PomodoroSessionDao
) {
    fun getAllSessions(): Flow<List<PomodoroSession>> = pomodoroSessionDao.getAllSessions()

    fun getCompletedSessions(): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getCompletedSessions()

    fun getTodaySessions(): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getTodaySessions()

    suspend fun getSessionById(id: Long): PomodoroSession? = pomodoroSessionDao.getSessionById(id)

    suspend fun getTodayFocusTime(): Int = pomodoroSessionDao.getTodayFocusTime() ?: 0

    suspend fun insertSession(session: PomodoroSession): Long =
        pomodoroSessionDao.insertSession(session)

    suspend fun updateSession(session: PomodoroSession) = pomodoroSessionDao.updateSession(session)

    suspend fun deleteSession(session: PomodoroSession) = pomodoroSessionDao.deleteSession(session)

    suspend fun deleteSessionById(id: Long) = pomodoroSessionDao.deleteSessionById(id)

    suspend fun insertSessionWithValidation(session: PomodoroSession): Result<Long> {
        val validationResult = PomodoroValidator.validatePomodoroSession(session)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(pomodoroSessionDao.insertSession(session))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("插入失败", e))
        }
    }

    suspend fun updateSessionWithValidation(session: PomodoroSession): Result<Unit> {
        val validationResult = PomodoroValidator.validatePomodoroSession(session)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(pomodoroSessionDao.updateSession(session))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("更新失败", e))
        }
    }

    suspend fun deleteSessionWithValidation(session: PomodoroSession): Result<Unit> {
        return try {
            Result.Success(pomodoroSessionDao.deleteSession(session))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("删除失败", e))
        }
    }
}
