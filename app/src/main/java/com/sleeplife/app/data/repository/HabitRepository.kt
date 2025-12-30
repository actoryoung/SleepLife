package com.sleeplife.app.data.repository

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
}
