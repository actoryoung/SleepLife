package com.sleeplife.app.data.dao

import androidx.room.*
import com.sleeplife.app.data.entities.Habit
import com.sleeplife.app.data.entities.HabitCheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllActiveHabitsSync(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Check-in operations
    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId ORDER BY checkInDate DESC")
    fun getHabitCheckIns(habitId: Long): Flow<List<HabitCheckIn>>

    @Query("SELECT COUNT(*) FROM habit_checkins WHERE habitId = :habitId")
    suspend fun getCheckInCount(habitId: Long): Int

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId AND date(checkInDate/1000, 'unixepoch') = date('now') LIMIT 1")
    suspend fun getTodayCheckIn(habitId: Long): HabitCheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: HabitCheckIn): Long

    @Delete
    suspend fun deleteCheckIn(checkIn: HabitCheckIn)

    @Query("DELETE FROM habit_checkins WHERE id = :id")
    suspend fun deleteCheckInById(id: Long)
}
