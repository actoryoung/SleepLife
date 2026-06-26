package com.sleeplife.app.ui.viewmodels

import com.sleeplife.app.TestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.SessionType
import com.sleeplife.app.data.repository.PomodoroRepository
import com.sleeplife.app.utils.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroViewModelTest : TestBase() {

    private lateinit var pomodoroRepository: PomodoroRepository
    private lateinit var viewModel: PomodoroViewModel

    @Before
    override fun setUp() {
        super.setUp()
        pomodoroRepository = mockk(relaxed = true)

        every { pomodoroRepository.getTodaySessions() } returns flowOf(emptyList())
        coEvery { pomodoroRepository.getTodayFocusTime() } returns 0

        viewModel = PomodoroViewModel(pomodoroRepository)
    }

    @Test
    fun `startSession should set running state on success`() = runTest {
        coEvery { pomodoroRepository.insertSessionWithValidation(any()) } returns Result.Success(20L)

        viewModel.startSession("任务A", 25)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isRunning)
        assertEquals(20L, state.currentSessionId)
        assertEquals(25 * 60, state.totalSeconds)
        assertNull(state.errorMessage)
    }

    @Test
    fun `startSession_withInvalidDuration_showsError`() = runTest {
        coEvery { pomodoroRepository.insertSessionWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("时长至少为1分钟"))

        viewModel.startSession("Task", 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("时长至少为1分钟", state.errorMessage)
        assertTrue(!state.isRunning)
        assertNull(state.currentSessionId)
    }

    @Test
    fun `pauseSession should set running false`() = runTest {
        coEvery { pomodoroRepository.insertSessionWithValidation(any()) } returns Result.Success(21L)

        viewModel.startSession("任务B", 25)
        runCurrent()
        viewModel.pauseSession()

        assertTrue(!viewModel.uiState.value.isRunning)
    }

    @Test
    fun `stopSession should reset current session fields`() = runTest {
        val session = TestDataFactory.createPomodoroSession(id = 22L, taskName = "任务C", duration = 25, completed = false)

        coEvery { pomodoroRepository.insertSessionWithValidation(any()) } returns Result.Success(22L)
        coEvery { pomodoroRepository.getSessionById(22L) } returns session
        coEvery { pomodoroRepository.updateSessionWithValidation(any()) } returns Result.Success(Unit)

        viewModel.startSession("任务C", 25)
        runCurrent()
        viewModel.stopSession()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.isRunning)
        assertNull(state.currentSessionId)
        assertEquals("", state.currentTaskName)
    }

    @Test
    fun `onSessionTypeChange should update selected type`() = runTest {
        viewModel.onSessionTypeChange(SessionType.SHORT_BREAK)

        assertEquals(SessionType.SHORT_BREAK, viewModel.uiState.value.sessionType)
    }
}
