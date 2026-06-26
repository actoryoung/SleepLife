package com.sleeplife.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sleeplife.app.data.repository.PomodoroRepository
import com.sleeplife.app.ui.screens.pomodoro.PomodoroScreen
import com.sleeplife.app.ui.theme.SleepLifeTheme
import com.sleeplife.app.ui.viewmodels.PomodoroViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI 集成测试：番茄钟界面
 */
class PomodoroScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: PomodoroRepository
    private lateinit var viewModel: PomodoroViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        coEvery { repository.getTodaySessions() } returns flowOf(emptyList())
        coEvery { repository.getTodayFocusTime() } returns 0
        viewModel = PomodoroViewModel(repository)
    }

    @Test
    fun pomodoroScreen_displaysTimer() {
        composeTestRule.setContent {
            SleepLifeTheme {
                PomodoroScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("开始专注").assertIsDisplayed()
    }

    @Test
    fun pomodoroScreen_displaysTodayStats() {
        composeTestRule.setContent {
            SleepLifeTheme {
                PomodoroScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("今日专注").assertIsDisplayed()
        composeTestRule.onNodeWithText("今日专注记录").assertIsDisplayed()
    }

    @Test
    fun pomodoroScreen_clickStartButton_opensDialog() {
        composeTestRule.setContent {
            SleepLifeTheme {
                PomodoroScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("开始专注").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("任务名称").assertIsDisplayed()
    }
}
