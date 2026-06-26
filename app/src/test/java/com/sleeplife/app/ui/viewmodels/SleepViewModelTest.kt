package com.sleeplife.app.ui.viewmodels

import com.sleeplife.app.TestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.SleepQuality
import com.sleeplife.app.data.repository.SleepRepository
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
class SleepViewModelTest : TestBase() {

    private lateinit var sleepRepository: SleepRepository
    private lateinit var viewModel: SleepViewModel

    @Before
    override fun setUp() {
        super.setUp()
        sleepRepository = mockk()

        every { sleepRepository.getRecentSleepRecords(7) } returns flowOf(emptyList())
        coEvery { sleepRepository.getTodaySleep() } returns null

        viewModel = SleepViewModel(sleepRepository)
    }

    @Test
    fun `startSleepTracking should set tracking state`() = runTest {
        coEvery { sleepRepository.insertSleepRecord(any()) } returns 100L

        viewModel.startSleepTracking()
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isTracking)
        assertEquals(100L, state.currentSleepId)
    }

    @Test
    fun `stopSleepTracking should reset tracking state when update succeeds`() = runTest {
        val record = TestDataFactory.createSleepRecord(id = 100L)

        coEvery { sleepRepository.insertSleepRecord(any()) } returns 100L
        coEvery { sleepRepository.getSleepRecordById(100L) } returns record
        coEvery { sleepRepository.updateSleepRecordWithValidation(any()) } returns Result.Success(Unit)
        coEvery { sleepRepository.getTodaySleep() } returns record

        viewModel.startSleepTracking()
        runCurrent()
        viewModel.stopSleepTracking(SleepQuality.GOOD, "ok")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.isTracking)
        assertNull(state.currentSleepId)
        assertNull(state.sleepStartTime)
        assertNull(state.errorMessage)
    }

    @Test
    fun `stopSleepTracking should set error when update fails`() = runTest {
        val record = TestDataFactory.createSleepRecord(id = 100L)

        coEvery { sleepRepository.insertSleepRecord(any()) } returns 100L
        coEvery { sleepRepository.getSleepRecordById(100L) } returns record
        coEvery { sleepRepository.updateSleepRecordWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("验证失败"))

        viewModel.startSleepTracking()
        runCurrent()
        viewModel.stopSleepTracking(SleepQuality.GOOD, "ok")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("验证失败", state.errorMessage)
    }

    @Test
    fun `addSleepRecord_withInvalidData_showsError`() = runTest {
        val now = TestDataFactory.createSleepRecord().startTime
        coEvery { sleepRepository.insertSleepRecordWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("结束时间必须晚于开始时间"))

        viewModel.showAddSleepDialog()
        viewModel.addSleepRecord(now, now, SleepQuality.AVERAGE, "note")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("结束时间必须晚于开始时间", state.errorMessage)
        assertTrue(state.showAddDialog)
    }

    @Test
    fun `addSleepRecord should close dialog on success`() = runTest {
        val now = TestDataFactory.createSleepRecord().startTime
        coEvery { sleepRepository.insertSleepRecordWithValidation(any()) } returns Result.Success(1L)
        coEvery { sleepRepository.getTodaySleep() } returns null

        viewModel.showAddSleepDialog()
        viewModel.addSleepRecord(now, now, SleepQuality.AVERAGE, "note")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.showAddDialog)
        assertNull(state.errorMessage)
    }
}
