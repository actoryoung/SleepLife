package com.sleeplife.app.utils

import com.sleeplife.app.data.entities.Habit
import com.sleeplife.app.data.entities.HabitCheckIn
import com.sleeplife.app.data.entities.Note
import com.sleeplife.app.data.entities.NoteMood
import com.sleeplife.app.data.entities.PomodoroSession
import com.sleeplife.app.data.entities.SessionType
import com.sleeplife.app.data.entities.SleepQuality
import com.sleeplife.app.data.entities.SleepRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Factory for creating test data objects with sensible defaults.
 * Helps maintain consistency across tests and reduces boilerplate.
 */
object TestDataFactory {

    // Sleep Records
    fun createSleepRecord(
        id: Long = 0,
        startTime: LocalDateTime = Clock.System.now().minus(8.hours).toLocalDateTime(TimeZone.currentSystemDefault()),
        endTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        quality: SleepQuality = SleepQuality.GOOD,
        notes: String = "Test sleep notes"
    ): SleepRecord {
        return SleepRecord(
            id = id,
            startTime = startTime,
            endTime = endTime,
            quality = quality,
            notes = notes
        )
    }

    fun createSleepRecords(count: Int): List<SleepRecord> {
        return (1..count).map { index ->
            createSleepRecord(
                id = index.toLong(),
                startTime = Clock.System.now().minus((8 * index).hours).toLocalDateTime(TimeZone.currentSystemDefault()),
                endTime = Clock.System.now().minus((8 * index - 7).hours).toLocalDateTime(TimeZone.currentSystemDefault())
            )
        }
    }

    // Habits
    fun createHabit(
        id: Long = 0,
        name: String = "Test Habit",
        description: String = "Test habit description",
        icon: String = "📌",
        color: Long = 0xFF2196F3,
        targetDays: Int = 30,
        isActive: Boolean = true,
        createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): Habit {
        return Habit(
            id = id,
            name = name,
            description = description,
            icon = icon,
            color = color,
            targetDays = targetDays,
            isActive = isActive,
            createdAt = createdAt
        )
    }

    fun createHabits(count: Int): List<Habit> {
        return (1..count).map { index ->
            createHabit(
                id = index.toLong(),
                name = "Habit $index"
            )
        }
    }

    fun createHabitCheckIn(
        id: Long = 0,
        habitId: Long = 1,
        checkInDate: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        note: String = "",
        completedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): HabitCheckIn {
        return HabitCheckIn(
            id = id,
            habitId = habitId,
            checkInDate = checkInDate,
            note = note,
            completedAt = completedAt
        )
    }

    // Notes
    fun createNote(
        id: Long = 0,
        title: String = "Test Note",
        content: String = "Test note content",
        mood: NoteMood = NoteMood.NEUTRAL,
        tags: String = "test,unit",
        isFavorite: Boolean = false,
        createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            mood = mood,
            tags = tags,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun createNotes(count: Int): List<Note> {
        return (1..count).map { index ->
            createNote(
                id = index.toLong(),
                title = "Note $index"
            )
        }
    }

    // Pomodoro Sessions
    fun createPomodoroSession(
        id: Long = 0,
        taskName: String = "Test Task",
        duration: Int = 25,
        actualDuration: Int = 25,
        sessionType: SessionType = SessionType.WORK,
        completed: Boolean = true,
        interrupted: Boolean = false,
        startTime: LocalDateTime = Clock.System.now().minus(25.minutes).toLocalDateTime(TimeZone.currentSystemDefault()),
        endTime: LocalDateTime? = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): PomodoroSession {
        return PomodoroSession(
            id = id,
            taskName = taskName,
            duration = duration,
            actualDuration = actualDuration,
            sessionType = sessionType,
            completed = completed,
            interrupted = interrupted,
            startTime = startTime,
            endTime = endTime
        )
    }

    fun createPomodoroSessions(count: Int): List<PomodoroSession> {
        return (1..count).map { index ->
            createPomodoroSession(
                id = index.toLong(),
                taskName = "Task $index"
            )
        }
    }

    // Validation Test Cases
    object InvalidData {
        // Invalid Sleep Records
        val sleepRecordEndBeforeStart = createSleepRecord(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().minus(1.hours).toLocalDateTime(TimeZone.currentSystemDefault())
        )

        val sleepRecordTooLong = createSleepRecord(
            startTime = Clock.System.now().minus(30.hours).toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )

        val sleepRecordNotesTooLong = createSleepRecord(
            notes = "a".repeat(501)
        )

        // Invalid Habits
        val habitEmptyName = createHabit(name = "")
        val habitNameTooLong = createHabit(name = "a".repeat(101))
        val habitInvalidTargetDays = createHabit(targetDays = 0)

        // Invalid Notes
        val noteEmptyTitle = createNote(title = "")
        val noteTitleTooLong = createNote(title = "a".repeat(201))
        val noteContentTooLong = createNote(content = "a".repeat(10001))

        // Invalid Pomodoro
        val pomodoroEmptyTask = createPomodoroSession(taskName = "")
        val pomodoroInvalidDuration = createPomodoroSession(duration = 0)
    }
}
