package com.sleeplife.app.data.repository

import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.core.validators.NoteValidator
import com.sleeplife.app.core.validators.ValidationResult
import com.sleeplife.app.data.dao.NoteDao
import com.sleeplife.app.data.entities.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getFavoriteNotes(): Flow<List<Note>> = noteDao.getFavoriteNotes()

    fun getRecentNotes(limit: Int = 10): Flow<List<Note>> = noteDao.getRecentNotes(limit)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun toggleFavorite(id: Long) = noteDao.toggleFavorite(id)

    suspend fun insertNoteWithValidation(note: Note): Result<Long> {
        val validationResult = NoteValidator.validateNote(note)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(noteDao.insertNote(note))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("插入失败", e))
        }
    }

    suspend fun updateNoteWithValidation(note: Note): Result<Unit> {
        val validationResult = NoteValidator.validateNote(note)
        if (validationResult is ValidationResult.Error) {
            return Result.Error(AppException.ValidationException(validationResult.message))
        }
        return try {
            Result.Success(noteDao.updateNote(note))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("更新失败", e))
        }
    }

    suspend fun deleteNoteWithValidation(note: Note): Result<Unit> {
        return try {
            Result.Success(noteDao.deleteNote(note))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("删除失败", e))
        }
    }

    suspend fun toggleFavoriteWithValidation(id: Long): Result<Unit> {
        return try {
            Result.Success(noteDao.toggleFavorite(id))
        } catch (e: Exception) {
            Result.Error(AppException.DatabaseException("操作失败", e))
        }
    }
}
