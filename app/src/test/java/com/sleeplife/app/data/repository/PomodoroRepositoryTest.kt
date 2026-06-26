package com.sleeplife.app.data.repository

import com.sleeplife.app.RepositoryTestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.PomodoroSession
import com.sleeplife.app.data.entities.SessionType
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PomodoroRepository
 */
class PomodoroRepositoryTest : RepositoryTestBase() {

    private lateinit var pomodoroRepository: PomodoroRepository

    private val testSession = PomodoroSession(
        id = 0,
        taskName = "Test Task",
        duration = 25,
        actualDuration = 0,
        sessionType = SessionType.WORK,
        completed = false,
        interrupted = false,
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        endTime = null
    )

    @Before
    override fun setUp() {
        super.setUp()
        pomodoroRepository = PomodoroRepository(pomodoroDao)
    }

    // ==================== Insertion Tests ====================

    @Test
    fun `insertSessionWithValidation should return Success when session is valid`() = runTest {
        // Arrange
        val validSession = testSession.copy(
            taskName = "Valid Task",
            duration = 25,
            completed = false
        )

        // Act
        val result = pomodoroRepository.insertSessionWithValidation(validSession)

        // Assert
        assertTrue(result.isSuccess())
        assertTrue((result as Result.Success).data > 0)
    }

    @Test
    fun `insertSessionWithValidation should return Error when task name is empty`() = runTest {
        // Arrange
        val invalidSession = testSession.copy(taskName = "")

        // Act
        val result = pomodoroRepository.insertSessionWithValidation(invalidSession)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertSessionWithValidation should return Error when task name exceeds max length`() = runTest {
        // Arrange
        val invalidSession = testSession.copy(taskName = "x".repeat(201)) // Exceeds max 200 chars

        // Act
        val result = pomodoroRepository.insertSessionWithValidation(invalidSession)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertSessionWithValidation should return Error when duration is out of range`() = runTest {
        // Arrange
        val invalidSession = testSession.copy(
            taskName = "Valid Task",
            duration = 0 // Must be 1-120
        )

        // Act
        val result = pomodoroRepository.insertSessionWithValidation(invalidSession)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertSessionWithValidation_invalidDuration_returnsError`() = runTest {
        // Arrange
        val invalidSession = testSession.copy(
            taskName = "Valid Task",
            duration = 0 // Invalid: must be at least 1
        )

        // Act
        val result = pomodoroRepository.insertSessionWithValidation(invalidSession)

        // Assert
        assertTrue(result.isError())
        val error = result as Result.Error
        assertTrue(error.exception is AppException.ValidationException)
    }

    @Test
    fun `insertSessionWithValidation should return Error when duration exceeds maximum`() = runTest {
        // Arrange
        val invalidSession = testSession.copy(
            taskName = "Valid Task",
            duration = 121 // Exceeds max 120
        )

        // Act
        val result = pomodoroRepository.insertSessionWithValidation(invalidSession)

        // Assert
        assertTrue(result.isError())
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateSessionWithValidation should return Success when session is valid`() = runTest {
        // Arrange
        val session = testSession.copy(
            taskName = "Valid Task",
            duration = 25,
            completed = false
        )
        val insertedId = pomodoroDao.insertSession(session)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val updatedSession = session.copy(
            id = insertedId,
            completed = true,
            endTime = now,
            actualDuration = 25
        )

        // Act
        val result = pomodoroRepository.updateSessionWithValidation(updatedSession)

        // Assert
        assertTrue(result.isSuccess())
        val fetchedSession = pomodoroDao.getSessionById(insertedId)
        assertTrue(fetchedSession?.completed == true)
    }

    // ==================== Deletion Tests ====================

    @Test
    fun `deleteSessionWithValidation should return Success and delete session`() = runTest {
        // Arrange
        val session = testSession.copy(taskName = "Valid Task", duration = 25)
        val insertedId = pomodoroDao.insertSession(session)
        val insertedSession = session.copy(id = insertedId)

        // Act
        val result = pomodoroRepository.deleteSessionWithValidation(insertedSession)

        // Assert
        assertTrue(result.isSuccess())
        assertNull(pomodoroDao.getSessionById(insertedId))
    }

    // ==================== Query Tests ====================

    @Test
    fun `getAllSessions should return all sessions`() = runTest {
        // Arrange
        val session1 = testSession.copy(id = 0, taskName = "Task 1")
        val session2 = testSession.copy(id = 0, taskName = "Task 2")
        pomodoroDao.insertSession(session1)
        pomodoroDao.insertSession(session2)

        // Act & Assert
        val sessions = pomodoroRepository.getAllSessions().first()
        assertEquals(2, sessions.size)
    }

    @Test
    fun `getCompletedSessions should return only completed sessions`() = runTest {
        // Arrange
        val completedSession = testSession.copy(
            id = 0,
            taskName = "Completed Task",
            completed = true
        )
        val runningSession = testSession.copy(
            id = 0,
            taskName = "Running Task",
            completed = false
        )
        pomodoroDao.insertSession(completedSession)
        pomodoroDao.insertSession(runningSession)

        // Act & Assert
        val sessions = pomodoroRepository.getCompletedSessions().first()
        assertEquals(1, sessions.size)
        assertTrue(sessions[0].completed)
    }

    @Test
    fun `getTodaySessions should return sessions created today`() = runTest {
        // Arrange
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val todaySession = testSession.copy(
            id = 0,
            taskName = "Today Task",
            startTime = now
        )
        pomodoroDao.insertSession(todaySession)

        // Act & Assert
        val sessions = pomodoroRepository.getTodaySessions().first()
        assertTrue(sessions.size > 0)
    }

    @Test
    fun `getSessionById should return correct session`() = runTest {
        // Arrange
        val session = testSession.copy(taskName = "Valid Task", duration = 25)
        val insertedId = pomodoroDao.insertSession(session)

        // Act
        val result = pomodoroRepository.getSessionById(insertedId)

        // Assert
        assertNotNull(result)
        assertEquals(insertedId, result?.id)
        assertEquals("Valid Task", result?.taskName)
    }

    @Test
    fun `getTodayFocusTime should return total focus time for today`() = runTest {
        // Arrange
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        repeat(3) { i ->
            pomodoroDao.insertSession(
                testSession.copy(
                    id = 0,
                    taskName = "Task $i",
                    startTime = now,
                    completed = true,
                    actualDuration = 25
                )
            )
        }

        // Act
        val totalTime = pomodoroRepository.getTodayFocusTime()

        // Assert
        assertEquals(75, totalTime) // 3 sessions × 25 minutes
    }
}
