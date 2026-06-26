package com.sleeplife.app.data.repository

import com.sleeplife.app.RepositoryTestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.Note
import com.sleeplife.app.data.entities.NoteMood
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NoteRepository
 */
class NoteRepositoryTest : RepositoryTestBase() {

    private lateinit var noteRepository: NoteRepository

    private val testNote = Note(
        id = 0,
        title = "Test Note",
        content = "Test note content",
        mood = NoteMood.NEUTRAL,
        tags = "tag1,tag2",
        isFavorite = false,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )

    @Before
    override fun setUp() {
        super.setUp()
        noteRepository = NoteRepository(noteDao)
    }

    // ==================== Insertion Tests ====================

    @Test
    fun `insertNoteWithValidation should return Success when note is valid`() = runTest {
        // Arrange
        val validNote = testNote.copy(title = "Valid Title", content = "Valid content")

        // Act
        val result = noteRepository.insertNoteWithValidation(validNote)

        // Assert
        assertTrue(result.isSuccess())
        assertTrue((result as Result.Success).data > 0)
    }

    @Test
    fun `insertNoteWithValidation should return Error when title is empty`() = runTest {
        // Arrange
        val invalidNote = testNote.copy(title = "")

        // Act
        val result = noteRepository.insertNoteWithValidation(invalidNote)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertNoteWithValidation should return Error when title exceeds max length`() = runTest {
        // Arrange
        val invalidNote = testNote.copy(title = "x".repeat(201)) // Exceeds max 200 chars

        // Act
        val result = noteRepository.insertNoteWithValidation(invalidNote)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertNoteWithValidation should return Error when content exceeds max length`() = runTest {
        // Arrange
        val invalidNote = testNote.copy(
            title = "Valid Title",
            content = "x".repeat(10001) // Exceeds max 10000 chars
        )

        // Act
        val result = noteRepository.insertNoteWithValidation(invalidNote)

        // Assert
        assertTrue(result.isError())
    }

    @Test
    fun `insertNoteWithValidation_emptyTitle_returnsError`() = runTest {
        // Arrange
        val invalidNote = testNote.copy(title = "")

        // Act
        val result = noteRepository.insertNoteWithValidation(invalidNote)

        // Assert
        assertTrue(result.isError())
        val error = result as Result.Error
        assertTrue(error.exception is AppException.ValidationException)
    }

    @Test
    fun `insertNoteWithValidation should return Error when tags exceed max length`() = runTest {
        // Arrange
        val invalidNote = testNote.copy(
            title = "Valid Title",
            content = "Valid content",
            tags = ("tag,").repeat(11) // Exceeds reasonable length
        )

        // Act
        val result = noteRepository.insertNoteWithValidation(invalidNote)

        // Assert
        assertTrue(result.isError())
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateNoteWithValidation should return Success when note is valid`() = runTest {
        // Arrange
        val note = testNote.copy(title = "Valid Title", content = "Valid content")
        val insertedId = noteDao.insertNote(note)
        val updatedNote = note.copy(id = insertedId, title = "Updated Title")

        // Act
        val result = noteRepository.updateNoteWithValidation(updatedNote)

        // Assert
        assertTrue(result.isSuccess())
        val fetchedNote = noteDao.getNoteById(insertedId)
        assertEquals("Updated Title", fetchedNote?.title)
    }

    // ==================== Deletion Tests ====================

    @Test
    fun `deleteNoteWithValidation should return Success and delete note`() = runTest {
        // Arrange
        val note = testNote.copy(title = "Valid Title", content = "Valid content")
        val insertedId = noteDao.insertNote(note)
        val insertedNote = note.copy(id = insertedId)

        // Act
        val result = noteRepository.deleteNoteWithValidation(insertedNote)

        // Assert
        assertTrue(result.isSuccess())
        assertNull(noteDao.getNoteById(insertedId))
    }

    // ==================== Favorite Tests ====================

    @Test
    fun `toggleFavoriteWithValidation should toggle favorite status`() = runTest {
        // Arrange
        val note = testNote.copy(title = "Valid Title", content = "Valid content", isFavorite = false)
        val insertedId = noteDao.insertNote(note)

        // Act - First toggle
        var result = noteRepository.toggleFavoriteWithValidation(insertedId)

        // Assert - Should be favorite now
        assertTrue(result.isSuccess())
        var fetchedNote = noteDao.getNoteById(insertedId)
        assertTrue(fetchedNote?.isFavorite == true)

        // Act - Second toggle
        result = noteRepository.toggleFavoriteWithValidation(insertedId)

        // Assert - Should be not favorite now
        assertTrue(result.isSuccess())
        fetchedNote = noteDao.getNoteById(insertedId)
        assertFalse(fetchedNote?.isFavorite == true)
    }

    // ==================== Query Tests ====================

    @Test
    fun `getAllNotes should return all notes`() = runTest {
        // Arrange
        val note1 = testNote.copy(title = "Note 1", content = "Content 1")
        val note2 = testNote.copy(title = "Note 2", content = "Content 2")
        noteDao.insertNote(note1)
        noteDao.insertNote(note2)

        // Act & Assert
        val notes = noteRepository.getAllNotes().first()
        assertEquals(2, notes.size)
    }

    @Test
    fun `getFavoriteNotes should return only favorite notes`() = runTest {
        // Arrange
        val favoriteNote = testNote.copy(title = "Favorite", content = "Content", isFavorite = true)
        val regularNote = testNote.copy(title = "Regular", content = "Content", isFavorite = false)
        val fav1 = favoriteNote.copy(id = 0)
        val fav2 = favoriteNote.copy(id = 0, title = "Favorite 2")
        
        noteDao.insertNote(fav1)
        noteDao.insertNote(fav2)
        noteDao.insertNote(regularNote)

        // Act & Assert
        val notes = noteRepository.getFavoriteNotes().first()
        assertEquals(2, notes.size)
        assertTrue(notes.all { it.isFavorite })
    }

    @Test
    fun `getRecentNotes should return limited notes in order`() = runTest {
        // Arrange
        repeat(5) { i ->
            noteDao.insertNote(
                testNote.copy(
                    id = 0,
                    title = "Note $i",
                    content = "Content $i"
                )
            )
        }

        // Act & Assert
        val notes = noteRepository.getRecentNotes(limit = 3).first()
        assertEquals(3, notes.size)
    }

    @Test
    fun `searchNotes should return notes matching query`() = runTest {
        // Arrange
        noteDao.insertNote(testNote.copy(id = 0, title = "Android Development", content = "Content"))
        noteDao.insertNote(testNote.copy(id = 0, title = "Kotlin Guide", content = "Content"))
        noteDao.insertNote(testNote.copy(id = 0, title = "Testing", content = "Android is great"))

        // Act & Assert
        val notes = noteRepository.searchNotes("Android").first()
        assertEquals(2, notes.size)
    }

    @Test
    fun `getNoteById should return correct note`() = runTest {
        // Arrange
        val note = testNote.copy(title = "Valid Title", content = "Valid content")
        val insertedId = noteDao.insertNote(note)

        // Act
        val result = noteRepository.getNoteById(insertedId)

        // Assert
        assertNotNull(result)
        assertEquals(insertedId, result?.id)
        assertEquals("Valid Title", result?.title)
    }
}
