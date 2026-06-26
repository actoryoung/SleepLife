package com.sleeplife.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sleeplife.app.data.repository.NoteRepository
import com.sleeplife.app.ui.screens.notes.NotesScreen
import com.sleeplife.app.ui.theme.SleepLifeTheme
import com.sleeplife.app.ui.viewmodels.NotesViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI 集成测试：笔记日记界面
 */
class NotesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: NoteRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        coEvery { repository.getAllNotes() } returns flowOf(emptyList())
        coEvery { repository.getFavoriteNotes() } returns flowOf(emptyList())
        viewModel = NotesViewModel(repository)
    }

    @Test
    fun notesScreen_displaysTopBar() {
        composeTestRule.setContent {
            SleepLifeTheme {
                NotesScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("笔记日记").assertIsDisplayed()
    }

    @Test
    fun notesScreen_hasSearchAndFilter() {
        composeTestRule.setContent {
            SleepLifeTheme {
                NotesScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("搜索笔记...").assertIsDisplayed()
        composeTestRule.onNodeWithText("收藏").assertIsDisplayed()
    }

    @Test
    fun notesScreen_displaysEmptyState() {
        composeTestRule.setContent {
            SleepLifeTheme {
                NotesScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("还没有笔记").assertIsDisplayed()
    }

    @Test
    fun notesScreen_hasFAB() {
        composeTestRule.setContent {
            SleepLifeTheme {
                NotesScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Note").assertIsDisplayed()
    }
}
