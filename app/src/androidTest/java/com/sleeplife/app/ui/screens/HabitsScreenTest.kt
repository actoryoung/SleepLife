package com.sleeplife.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sleeplife.app.data.repository.HabitRepository
import com.sleeplife.app.ui.screens.habits.HabitsScreen
import com.sleeplife.app.ui.theme.SleepLifeTheme
import com.sleeplife.app.ui.viewmodels.HabitsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI 集成测试：习惯打卡界面
 */
class HabitsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: HabitRepository
    private lateinit var viewModel: HabitsViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        coEvery { repository.getAllActiveHabits() } returns flowOf(emptyList())
        viewModel = HabitsViewModel(repository)
    }

    @Test
    fun habitsScreen_displaysEmptyState() {
        composeTestRule.setContent {
            SleepLifeTheme {
                HabitsScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("还没有习惯").assertIsDisplayed()
        composeTestRule.onNodeWithText("习惯打卡").assertIsDisplayed()
    }

    @Test
    fun habitsScreen_hasFAB() {
        composeTestRule.setContent {
            SleepLifeTheme {
                HabitsScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Habit").assertIsDisplayed()
    }

    @Test
    fun habitsScreen_clickFAB_opensDialog() {
        composeTestRule.setContent {
            SleepLifeTheme {
                HabitsScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Habit").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("创建习惯").assertIsDisplayed()
        composeTestRule.onNodeWithText("习惯名称").assertIsDisplayed()
    }
}
