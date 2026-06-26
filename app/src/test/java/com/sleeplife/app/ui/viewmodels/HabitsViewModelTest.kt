package com.sleeplife.app.ui.viewmodels

import com.sleeplife.app.TestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.Habit
import com.sleeplife.app.data.repository.HabitRepository
import com.sleeplife.app.utils.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModelTest : TestBase() {

    private lateinit var habitRepository: HabitRepository
    private lateinit var viewModel: HabitsViewModel

    @Before
    override fun setUp() {
        super.setUp()
        habitRepository = mockk()

        every { habitRepository.getAllActiveHabits() } returns flowOf(emptyList())
        coEvery { habitRepository.getAllActiveHabitsSync() } returns emptyList()

        viewModel = HabitsViewModel(habitRepository)
    }

    @Test
    fun `addHabit should close dialog on success`() = runTest {
        coEvery { habitRepository.insertHabitWithValidation(any()) } returns Result.Success(10L)
        coEvery { habitRepository.getAllActiveHabitsSync() } returns listOf(TestDataFactory.createHabit(id = 10L))
        coEvery { habitRepository.getCheckInCount(any()) } returns 0
        coEvery { habitRepository.getTodayCheckIn(any()) } returns null

        viewModel.showAddHabitDialog()
        viewModel.addHabit("读书", "每天读书", "📚", 30, 0xFF2196F3)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.showAddDialog)
        assertNull(state.errorMessage)
    }

    @Test
    fun `addHabit should set error when repository returns error`() = runTest {
        coEvery { habitRepository.insertHabitWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("习惯名称不能为空"))

        viewModel.addHabit("", "", "📌", 10, 0xFF2196F3)
        advanceUntilIdle()

        assertEquals("习惯名称不能为空", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `addHabit_withInvalidData_showsError`() = runTest {
        coEvery { habitRepository.insertHabitWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("习惯名称不能为空"))

        viewModel.showAddHabitDialog()
        viewModel.addHabit("", "", "📌", 10, 0xFF2196F3)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("习惯名称不能为空", state.errorMessage)
        assertTrue(state.showAddDialog)
    }

    @Test
    fun `checkInHabit should clear error on success`() = runTest {
        coEvery { habitRepository.insertCheckInWithValidation(any()) } returns Result.Success(1L)
        coEvery { habitRepository.getAllActiveHabitsSync() } returns emptyList()

        viewModel.checkInHabit(1L)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `selectHabit should update selected habit`() = runTest {
        val habit: Habit = TestDataFactory.createHabit(id = 9L)

        viewModel.selectHabit(habit)
        advanceUntilIdle()

        assertEquals(9L, viewModel.selectedHabit.value?.id)
    }
}
