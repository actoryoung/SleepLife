package com.sleeplife.app.data.repository

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
}
