package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.Note
import com.sleeplife.app.data.entities.NoteMood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteValidatorTest {

    // -----------------------------------------------------------------------
    // Helper: build a Note with overridable fields
    // -----------------------------------------------------------------------
    private fun createNote(
        title: String = "Test Note",
        content: String = "Test note content",
        mood: NoteMood = NoteMood.NEUTRAL,
        tags: String = "test,unit",
        isFavorite: Boolean = false
    ): Note = Note(
        title = title,
        content = content,
        mood = mood,
        tags = tags,
        isFavorite = isFavorite
    )

    // -----------------------------------------------------------------------
    // Valid
    // -----------------------------------------------------------------------
    @Test
    fun `given valid note, should return Valid`() {
        val note = createNote()
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for a valid note, got: $result", result is ValidationResult.Valid)
    }

    @Test
    fun `given note with exactly 200-character title, should return Valid`() {
        val note = createNote(title = "x".repeat(200))
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for exactly 200-char title", result is ValidationResult.Valid)
    }

    @Test
    fun `given note with exactly 10000-character content, should return Valid`() {
        val note = createNote(content = "x".repeat(10000))
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for exactly 10000-char content", result is ValidationResult.Valid)
    }

    @Test
    fun `given note with empty tags, should return Valid`() {
        val note = createNote(tags = "")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for empty tags", result is ValidationResult.Valid)
    }

    @Test
    fun `given note with exactly 10 tags, should return Valid`() {
        val tags = (1..10).joinToString(",") { "tag$it" }
        val note = createNote(tags = tags)
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for exactly 10 tags", result is ValidationResult.Valid)
    }

    @Test
    fun `given note with tag exactly 50 characters long, should return Valid`() {
        val note = createNote(tags = "x".repeat(50))
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for tag exactly 50 chars", result is ValidationResult.Valid)
    }

    @Test
    fun `given note with tags separated by commas and spaces, should parse correctly and return Valid`() {
        val note = createNote(tags = " tag1 , tag2 , tag3 ")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Valid for tags with spaces around commas", result is ValidationResult.Valid)
    }

    // -----------------------------------------------------------------------
    // Title validation
    // -----------------------------------------------------------------------
    @Test
    fun `given blank title, should return Error`() {
        val note = createNote(title = "")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for blank title", result is ValidationResult.Error)
        assertEquals("标题不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given whitespace-only title, should return Error`() {
        val note = createNote(title = "   ")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for whitespace-only title", result is ValidationResult.Error)
        assertEquals("标题不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given title longer than 200 characters, should return Error`() {
        val note = createNote(title = "x".repeat(201))
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for title > 200 chars", result is ValidationResult.Error)
        assertEquals("标题不能超过200字符", (result as ValidationResult.Error).message)
    }

    // -----------------------------------------------------------------------
    // Content validation
    // -----------------------------------------------------------------------
    @Test
    fun `given blank content, should return Error`() {
        val note = createNote(content = "")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for blank content", result is ValidationResult.Error)
        assertEquals("内容不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given whitespace-only content, should return Error`() {
        val note = createNote(content = "   ")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for whitespace-only content", result is ValidationResult.Error)
        assertEquals("内容不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given content longer than 10000 characters, should return Error`() {
        val note = createNote(content = "x".repeat(10001))
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for content > 10000 chars", result is ValidationResult.Error)
        assertEquals("内容不能超过10000字符", (result as ValidationResult.Error).message)
    }

    // -----------------------------------------------------------------------
    // Tags validation
    // -----------------------------------------------------------------------
    @Test
    fun `given more than 10 tags, should return Error`() {
        val tags = (1..11).joinToString(",") { "tag$it" }
        val note = createNote(tags = tags)
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for > 10 tags", result is ValidationResult.Error)
        assertEquals("最多只能添加10个标签", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given tag longer than 50 characters, should return Error`() {
        val note = createNote(tags = "x".repeat(51))
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error for tag > 50 chars", result is ValidationResult.Error)
        assertEquals("标签不能超过50字符", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given one valid tag and one too-long tag, should return Error`() {
        val note = createNote(tags = "valid,${"x".repeat(51)}")
        val result = NoteValidator.validateNote(note)

        assertTrue("Expected Error when one tag exceeds 50 chars", result is ValidationResult.Error)
        assertEquals("标签不能超过50字符", (result as ValidationResult.Error).message)
    }

    // -----------------------------------------------------------------------
    // validateTitle helper
    // -----------------------------------------------------------------------
    @Test
    fun `validateTitle with valid title should return Valid`() {
        val result = NoteValidator.validateTitle("My Note")

        assertTrue("Expected Valid for valid title", result is ValidationResult.Valid)
    }

    @Test
    fun `validateTitle with blank title should return Error`() {
        val result = NoteValidator.validateTitle("")

        assertTrue("Expected Error for blank title", result is ValidationResult.Error)
        assertEquals("标题不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateTitle with title longer than 200 chars should return Error`() {
        val result = NoteValidator.validateTitle("x".repeat(201))

        assertTrue("Expected Error for title > 200 chars", result is ValidationResult.Error)
        assertEquals("标题不能超过200字符", (result as ValidationResult.Error).message)
    }
}
