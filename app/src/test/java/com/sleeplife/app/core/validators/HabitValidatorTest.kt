package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitValidatorTest {

    // -----------------------------------------------------------------------
    // Helper: build a Habit with overridable fields
    // -----------------------------------------------------------------------
    private fun createHabit(
        name: String = "Test Habit",
        description: String = "Test habit description",
        icon: String = "📌",
        targetDays: Int = 30,
        color: Long = 0xFF2196F3,
        isActive: Boolean = true
    ): Habit = Habit(
        name = name,
        description = description,
        icon = icon,
        targetDays = targetDays,
        color = color,
        isActive = isActive
    )

    // -----------------------------------------------------------------------
    // Valid
    // -----------------------------------------------------------------------
    @Test
    fun `given valid habit, should return Valid`() {
        val habit = createHabit()
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Valid for a valid habit, got: $result", result is ValidationResult.Valid)
    }

    @Test
    fun `given habit with exactly 100-character name, should return Valid`() {
        val habit = createHabit(name = "x".repeat(100))
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Valid for exactly 100-char name", result is ValidationResult.Valid)
    }

    @Test
    fun `given habit with exactly 500-character description, should return Valid`() {
        val habit = createHabit(description = "x".repeat(500))
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Valid for exactly 500-char description", result is ValidationResult.Valid)
    }

    @Test
    fun `given habit with targetDays of 1, should return Valid`() {
        val habit = createHabit(targetDays = 1)
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Valid for targetDays = 1", result is ValidationResult.Valid)
    }

    @Test
    fun `given habit with targetDays of 365, should return Valid`() {
        val habit = createHabit(targetDays = 365)
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Valid for targetDays = 365", result is ValidationResult.Valid)
    }

    // -----------------------------------------------------------------------
    // Name validation
    // -----------------------------------------------------------------------
    @Test
    fun `given blank name, should return Error`() {
        val habit = createHabit(name = "")
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for blank name", result is ValidationResult.Error)
        assertEquals("习惯名称不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given whitespace-only name, should return Error`() {
        val habit = createHabit(name = "   ")
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for whitespace-only name", result is ValidationResult.Error)
        assertEquals("习惯名称不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given name longer than 100 characters, should return Error`() {
        val habit = createHabit(name = "x".repeat(101))
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for name > 100 chars", result is ValidationResult.Error)
        assertEquals("习惯名称不能超过100字符", (result as ValidationResult.Error).message)
    }

    // -----------------------------------------------------------------------
    // Description validation
    // -----------------------------------------------------------------------
    @Test
    fun `given description longer than 500 characters, should return Error`() {
        val habit = createHabit(description = "x".repeat(501))
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for description > 500 chars", result is ValidationResult.Error)
        assertEquals("描述不能超过500字符", (result as ValidationResult.Error).message)
    }

    // -----------------------------------------------------------------------
    // Target days validation
    // -----------------------------------------------------------------------
    @Test
    fun `given targetDays less than 1, should return Error`() {
        val habit = createHabit(targetDays = 0)
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for targetDays < 1", result is ValidationResult.Error)
        assertEquals("目标天数至少为1天", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given targetDays negative, should return Error`() {
        val habit = createHabit(targetDays = -5)
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for negative targetDays", result is ValidationResult.Error)
        assertEquals("目标天数至少为1天", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given targetDays greater than 365, should return Error`() {
        val habit = createHabit(targetDays = 366)
        val result = HabitValidator.validateHabit(habit)

        assertTrue("Expected Error for targetDays > 365", result is ValidationResult.Error)
        assertEquals("目标天数不能超过365天", (result as ValidationResult.Error).message)
    }

    // -----------------------------------------------------------------------
    // validateName helper
    // -----------------------------------------------------------------------
    @Test
    fun `validateName with valid name should return Valid`() {
        val result = HabitValidator.validateName("Daily Run")

        assertTrue("Expected Valid for valid name", result is ValidationResult.Valid)
    }

    @Test
    fun `validateName with blank name should return Error`() {
        val result = HabitValidator.validateName("")

        assertTrue("Expected Error for blank name", result is ValidationResult.Error)
        assertEquals("习惯名称不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateName with name longer than 100 chars should return Error`() {
        val result = HabitValidator.validateName("x".repeat(101))

        assertTrue("Expected Error for name > 100 chars", result is ValidationResult.Error)
        assertEquals("习惯名称不能超过100字符", (result as ValidationResult.Error).message)
    }
}
