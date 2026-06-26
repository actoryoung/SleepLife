package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.PomodoroSession
import com.sleeplife.app.data.entities.SessionType
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class PomodoroValidatorTest {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun createSession(
        taskName: String = "Test Task",
        duration: Int = 25,
        actualDuration: Int = 25,
        sessionType: SessionType = SessionType.WORK,
        completed: Boolean = true,
        interrupted: Boolean = false
    ): PomodoroSession {
        val startInstant = Clock.System.now() - 25.minutes
        return PomodoroSession(
            taskName = taskName,
            duration = duration,
            actualDuration = actualDuration,
            sessionType = sessionType,
            completed = completed,
            interrupted = interrupted,
            startTime = startInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = now
        )
    }

    @Test
    fun `given valid pomodoro session, should return Valid`() {
        val session = createSession()
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue(
            "Expected Valid for a valid session, got: $result",
            result is ValidationResult.Valid
        )
    }

    @Test
    fun `given session with exactly 200-character task name, should return Valid`() {
        val session = createSession(taskName = "x".repeat(200))
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue(
            "Expected Valid for exactly 200-char task name",
            result is ValidationResult.Valid
        )
    }

    @Test
    fun `given session with duration of 1 minute, should return Valid`() {
        val session = createSession(duration = 1)
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Valid for duration = 1", result is ValidationResult.Valid)
    }

    @Test
    fun `given session with duration of 120 minutes, should return Valid`() {
        val session = createSession(duration = 120)
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Valid for duration = 120", result is ValidationResult.Valid)
    }

    @Test
    fun `given blank task name, should return Error`() {
        val session = createSession(taskName = "")
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Error for blank task name", result is ValidationResult.Error)
        assertEquals("任务名称不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given whitespace-only task name, should return Error`() {
        val session = createSession(taskName = "   ")
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Error for whitespace-only task name", result is ValidationResult.Error)
        assertEquals("任务名称不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given task name longer than 200 characters, should return Error`() {
        val session = createSession(taskName = "x".repeat(201))
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Error for task name > 200 chars", result is ValidationResult.Error)
        assertEquals("任务名称不能超过200字符", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given duration less than 1 minute, should return Error`() {
        val session = createSession(duration = 0)
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Error for duration < 1", result is ValidationResult.Error)
        assertEquals("时长至少为1分钟", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given negative duration, should return Error`() {
        val session = createSession(duration = -5)
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Error for negative duration", result is ValidationResult.Error)
        assertEquals("时长至少为1分钟", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given duration greater than 120 minutes, should return Error`() {
        val session = createSession(duration = 121)
        val result = PomodoroValidator.validatePomodoroSession(session)
        assertTrue("Expected Error for duration > 120", result is ValidationResult.Error)
        assertEquals("时长不能超过120分钟", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateTaskName with valid name should return Valid`() {
        val result = PomodoroValidator.validateTaskName("Code Review")
        assertTrue("Expected Valid for valid task name", result is ValidationResult.Valid)
    }

    @Test
    fun `validateTaskName with blank name should return Error`() {
        val result = PomodoroValidator.validateTaskName("")
        assertTrue("Expected Error for blank task name", result is ValidationResult.Error)
        assertEquals("任务名称不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateTaskName with name longer than 200 chars should return Error`() {
        val result = PomodoroValidator.validateTaskName("x".repeat(201))
        assertTrue("Expected Error for task name > 200 chars", result is ValidationResult.Error)
        assertEquals("任务名称不能超过200字符", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateDuration with valid duration should return Valid`() {
        val result = PomodoroValidator.validateDuration(25)
        assertTrue("Expected Valid for valid duration", result is ValidationResult.Valid)
    }

    @Test
    fun `validateDuration with duration 0 should return Error`() {
        val result = PomodoroValidator.validateDuration(0)
        assertTrue("Expected Error for duration 0", result is ValidationResult.Error)
        assertEquals("时长至少为1分钟", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateDuration with duration 121 should return Error`() {
        val result = PomodoroValidator.validateDuration(121)
        assertTrue("Expected Error for duration 121", result is ValidationResult.Error)
        assertEquals("时长不能超过120分钟", (result as ValidationResult.Error).message)
    }
}
