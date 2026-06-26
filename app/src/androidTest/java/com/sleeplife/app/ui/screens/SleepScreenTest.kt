package com.sleeplife.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sleeplife.app.data.repository.SleepRepository
import com.sleeplife.app.ui.screens.sleep.SleepScreen
import com.sleeplife.app.ui.theme.SleepLifeTheme
import com.sleeplife.app.ui.viewmodels.SleepViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI 集成测试：睡眠追踪界面
 */
class SleepScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: SleepRepository
    private lateinit var viewModel: SleepViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        coEvery { repository.getRecentSleepRecords(any()) } returns flowOf(emptyList())
        coEvery { repository.getTodaySleep() } returns null
        viewModel = SleepViewModel(repository)
    }

    @Test
    fun sleepScreen_displaysInitialState() {
        composeTestRule.setContent {
            SleepLifeTheme {
                SleepScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("开始睡眠").assertIsDisplayed()
        composeTestRule.onNodeWithText("最近睡眠记录").assertIsDisplayed()
    }

    @Test
    fun sleepScreen_clickStartButton_changesState() {
        composeTestRule.setContent {
            SleepLifeTheme {
                SleepScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("开始睡眠").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("正在睡眠中...").assertIsDisplayed()
        composeTestRule.onNodeWithText("结束睡眠").assertIsDisplayed()
    }

    @Test
    fun sleepScreen_clickStopButton_showsDialog() {
        composeTestRule.setContent {
            SleepLifeTheme {
                SleepScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("开始睡眠").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("结束睡眠").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("睡眠质量").assertIsDisplayed()
    }
}
