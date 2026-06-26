package com.sleeplife.app.data.repository

import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.core.validators.HabitValidator
import com.sleeplife.app.core.validators.ValidationResult
import com.sleeplife.app.data.dao.HabitDao
import com.sleeplife.app.data.entities.Habit
import com.sleeplife.app.data.entities.HabitCheckIn
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    suspend fun getAllActiveHabitsSync(): List<Habit> = habitDao.getAllActiveHabitsSync()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    // Check-in operations
    fun getHabitCheckIns(habitId: Long): Flow<List<HabitCheckIn>> =
        habitDao.getHabitCheckIns(habitId)

    suspend fun getCheckInCount(habitId: Long): Int = habitDao.getCheckInCount(habitId)

    suspend fun getTodayCheckIn(habitId: Long): HabitCheckIn? = habitDao.getTodayCheckIn(habitId)

    suspend fun insertCheckIn(checkIn: HabitCheckIn): Long = habitDao.insertCheckIn(checkIn)

    suspend fun deleteCheckIn(checkIn: HabitCheckIn) = habitDao.deleteCheckIn(checkIn)

    suspend fun deleteCheckInById(id: Long) = habitDao.deleteCheckInById(id)

    suspend fun insertHabitWithValidation(habit: Habit): Result<Long> {
        val validationResult = HabitValidator.validateHabit(habit)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(habitDao.insertHabit(habit))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("插入失败", e))
        }
    }

    suspend fun updateHabitWithValidation(habit: Habit): Result<Unit> {
        val validationResult = HabitValidator.validateHabit(habit)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(habitDao.updateHabit(habit))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("更新失败", e))
        }
    }

    suspend fun deleteHabitWithValidation(habit: Habit): Result<Unit> {
        return try {
            Result.Success(habitDao.deleteHabit(habit))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("删除失败", e))
        }
    }

    suspend fun insertCheckInWithValidation(checkIn: HabitCheckIn): Result<Long> {
        return try {
            Result.Success(habitDao.insertCheckIn(checkIn))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("插入失败", e))
        }
    }

    suspend fun deleteCheckInWithValidation(id: Long): Result<Unit> {
        return try {
            Result.Success(habitDao.deleteCheckInById(id))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("删除失败", e))
        }
    }
}
