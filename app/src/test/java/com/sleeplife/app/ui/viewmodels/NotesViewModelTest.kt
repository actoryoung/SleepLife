package com.sleeplife.app.ui.viewmodels

import com.sleeplife.app.TestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.NoteMood
import com.sleeplife.app.data.repository.NoteRepository
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
class NotesViewModelTest : TestBase() {

    private lateinit var noteRepository: NoteRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    override fun setUp() {
        super.setUp()
        noteRepository = mockk()

        every { noteRepository.getAllNotes() } returns flowOf(emptyList())
        every { noteRepository.getFavoriteNotes() } returns flowOf(emptyList())

        viewModel = NotesViewModel(noteRepository)
    }

    @Test
    fun `addNote should close dialog on success`() = runTest {
        coEvery { noteRepository.insertNoteWithValidation(any()) } returns Result.Success(1L)

        viewModel.showAddNoteDialog()
        viewModel.addNote("标题", "内容", NoteMood.NEUTRAL, "test")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.showAddDialog)
        assertNull(state.errorMessage)
    }

    @Test
    fun `addNote should set error on repository error`() = runTest {
        coEvery { noteRepository.insertNoteWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("标题不能为空"))

        viewModel.addNote("", "内容", NoteMood.NEUTRAL, "")
        advanceUntilIdle()

        assertEquals("标题不能为空", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `addNote_withInvalidData_showsError`() = runTest {
        coEvery { noteRepository.insertNoteWithValidation(any()) } returns
            Result.Error(AppException.ValidationException("标题不能为空"))

        viewModel.showAddNoteDialog()
        viewModel.addNote("", "内容", NoteMood.NEUTRAL, "")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("标题不能为空", state.errorMessage)
        assertTrue(state.showAddDialog)
    }

    @Test
    fun `toggleFavorite should clear error on success`() = runTest {
        val note = TestDataFactory.createNote(id = 8L)
        coEvery { noteRepository.toggleFavoriteWithValidation(8L) } returns Result.Success(Unit)

        viewModel.toggleFavorite(note)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `toggleShowFavorites should flip state`() = runTest {
        val before = viewModel.uiState.value.showFavoritesOnly

        viewModel.toggleShowFavorites()

        assertEquals(!before, viewModel.uiState.value.showFavoritesOnly)
    }
}
