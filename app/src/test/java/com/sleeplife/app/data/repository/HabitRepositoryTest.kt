package com.sleeplife.app.data.repository

import com.sleeplife.app.RepositoryTestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.Habit
import com.sleeplife.app.data.entities.HabitCheckIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HabitRepository
 */
class HabitRepositoryTest : RepositoryTestBase() {

    private lateinit var habitRepository: HabitRepository

    private val testHabit = Habit(
        id = 0,
        name = "Test Habit",
        description = "Test habit description",
        icon = "📌",
        targetDays = 30,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        isActive = true
    )

    private val testCheckIn = HabitCheckIn(
        id = 0,
        habitId = 1,
        checkInDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        note = "Check-in note"
    )

    @Before
    override fun setUp() {
        super.setUp()
        habitRepository = HabitRepository(habitDao)
    }

    // ==================== Habit Insertion Tests ====================

    @Test
    fun `insertHabitWithValidation should return Success when habit is valid`() = runTest {
        // Arrange
        val validHabit = testHabit.copy(name = "Valid Habit", targetDays = 30)

        // Act
        val result = habitRepository.insertHabitWithValidation(validHabit)

        // Assert
        assertTrue(result.isSuccess())
        assertTrue((result as Result.Success).data > 0)
    }

    @Test
    fun `insertHabitWithValidation should return Error when name is empty`() = runTest {
        // Arrange
        val invalidHabit = testHabit.copy(name = "")

        // Act
        val result = habitRepository.insertHabitWithValidation(invalidHabit)

        // Assert
        assertTrue(result.isError())
        assertFalse(result.isSuccess())
    }

    @Test
    fun `insertHabitWithValidation should return Error when name exceeds max length`() = runTest {
        // Arrange
        val invalidHabit = testHabit.copy(name = "x".repeat(101)) // Exceeds max 100 chars

        // Act
        val result = habitRepository.insertHabitWithValidation(invalidHabit)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertHabitWithValidation_emptyName_returnsError`() = runTest {
        // Arrange
        val invalidHabit = testHabit.copy(name = "   ") // Blank name with whitespace

        // Act
        val result = habitRepository.insertHabitWithValidation(invalidHabit)

        // Assert
        assertTrue(result.isError())
        val error = result as Result.Error
        assertTrue(error.exception is AppException.ValidationException)
    }

    @Test
    fun `insertHabitWithValidation should return Error when targetDays is invalid`() = runTest {
        // Arrange
        val invalidHabit = testHabit.copy(targetDays = 0) // Must be 1-365

        // Act
        val result = habitRepository.insertHabitWithValidation(invalidHabit)

        // Assert
        assertTrue(result.isError())
    }

    // ==================== Habit Update Tests ====================

    @Test
    fun `updateHabitWithValidation should return Success when habit is valid`() = runTest {
        // Arrange
        val habit = testHabit.copy(name = "Valid Habit")
        val insertedId = habitDao.insertHabit(habit)
        val updatedHabit = habit.copy(id = insertedId, name = "Updated Habit")

        // Act
        val result = habitRepository.updateHabitWithValidation(updatedHabit)

        // Assert
        assertTrue(result.isSuccess())
        val fetchedHabit = habitDao.getHabitById(insertedId)
        assertEquals("Updated Habit", fetchedHabit?.name)
    }

    // ==================== Habit Deletion Tests ====================

    @Test
    fun `deleteHabitWithValidation should return Success and delete habit`() = runTest {
        // Arrange
        val habit = testHabit.copy(name = "Valid Habit")
        val insertedId = habitDao.insertHabit(habit)
        val insertedHabit = habit.copy(id = insertedId)

        // Act
        val result = habitRepository.deleteHabitWithValidation(insertedHabit)

        // Assert
        assertTrue(result.isSuccess())
        assertNull(habitDao.getHabitById(insertedId))
    }

    // ==================== Check-in Tests ====================

    @Test
    fun `insertCheckInWithValidation should return Success when check-in is valid`() = runTest {
        // Arrange
        val habit = testHabit.copy(name = "Valid Habit")
        val habitId = habitDao.insertHabit(habit)
        val checkIn = testCheckIn.copy(habitId = habitId)

        // Act
        val result = habitRepository.insertCheckInWithValidation(checkIn)

        // Assert
        assertTrue(result.isSuccess())
        assertTrue((result as Result.Success).data > 0)
    }

    @Test
    fun `deleteCheckInWithValidation should return Success and delete check-in`() = runTest {
        // Arrange
        val habit = testHabit.copy(name = "Valid Habit")
        val habitId = habitDao.insertHabit(habit)
        val checkIn = testCheckIn.copy(habitId = habitId)
        val insertedId = habitDao.insertCheckIn(checkIn)
        val insertedCheckIn = checkIn.copy(id = insertedId)

        // Act
        val result = habitRepository.deleteCheckInWithValidation(insertedId)

        // Assert
        assertTrue(result.isSuccess())
        assertNull(habitDao.getCheckInCount(habitId).let { if (it == 0) null else "exists" })
    }

    // ==================== Query Tests ====================

    @Test
    fun `getAllActiveHabits should return only active habits`() = runTest {
        // Arrange
        val activeHabit = testHabit.copy(name = "Active Habit", isActive = true)
        val inactiveHabit = testHabit.copy(name = "Inactive Habit", isActive = false)
        habitDao.insertHabit(activeHabit)
        habitDao.insertHabit(inactiveHabit)

        // Act & Assert
        val habits = habitRepository.getAllActiveHabits().first()
        assertEquals(1, habits.size)
        assertEquals("Active Habit", habits[0].name)
    }

    @Test
    fun `getHabitCheckIns should return all check-ins for a habit`() = runTest {
        // Arrange
        val habit = testHabit.copy(name = "Valid Habit")
        val habitId = habitDao.insertHabit(habit)
        repeat(3) { i ->
            habitDao.insertCheckIn(
                testCheckIn.copy(
                    habitId = habitId,
                    note = "Check-in $i"
                )
            )
        }

        // Act & Assert
        val checkIns = habitRepository.getHabitCheckIns(habitId).first()
        assertEquals(3, checkIns.size)
    }

    @Test
    fun `getCheckInCount should return correct count`() = runTest {
        // Arrange
        val habit = testHabit.copy(name = "Valid Habit")
        val habitId = habitDao.insertHabit(habit)
        repeat(5) { i ->
            habitDao.insertCheckIn(
                testCheckIn.copy(
                    habitId = habitId,
                    note = "Check-in $i"
                )
            )
        }

        // Act
        val count = habitRepository.getCheckInCount(habitId)

        // Assert
        assertEquals(5, count)
    }
}
