package com.sleeplife.app.data.dao

import androidx.room.*
import com.sleeplife.app.data.entities.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE completed = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE date(startTime/1000, 'unixepoch') = date('now') ORDER BY startTime DESC")
    fun getTodaySessions(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): PomodoroSession?

    @Query("SELECT SUM(actualDuration) FROM pomodoro_sessions WHERE completed = 1 AND date(startTime/1000, 'unixepoch') = date('now')")
    suspend fun getTodayFocusTime(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long

    @Update
    suspend fun updateSession(session: PomodoroSession)

    @Delete
    suspend fun deleteSession(session: PomodoroSession)

    @Query("DELETE FROM pomodoro_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}
