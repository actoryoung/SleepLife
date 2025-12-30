package com.sleeplife.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplife.app.data.entities.Note
import com.sleeplife.app.data.entities.NoteMood
import com.sleeplife.app.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    val allNotes = noteRepository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteNotes = noteRepository.getFavoriteNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        _uiState.update { it.copy(showFavoritesOnly = false) }
    }

    fun toggleShowFavorites() {
        _uiState.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showAddNoteDialog() {
        _uiState.update { it.copy(
            showAddDialog = true,
            editingNote = null
        ) }
    }

    fun showEditNoteDialog(note: Note) {
        _uiState.update { it.copy(
            showAddDialog = true,
            editingNote = note
        ) }
    }

    fun hideAddNoteDialog() {
        _uiState.update { it.copy(
            showAddDialog = false,
            editingNote = null
        ) }
    }

    fun addNote(title: String, content: String, mood: NoteMood, tags: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                mood = mood,
                tags = tags
            )
            noteRepository.insertNote(note)
            hideAddNoteDialog()
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            val updatedNote = note.copy(
                updatedAt = kotlinx.datetime.LocalDateTime(
                    kotlinx.datetime.Clock.System.now().toString(),
                    kotlinx.datetime.format.DateTimeFormat.ISO_LOCAL_DATE_TIME
                )
            )
            noteRepository.updateNote(updatedNote)
            hideAddNoteDialog()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(note.id)
        }
    }

    fun getFilteredNotes(): StateFlow<List<Note>> {
        return combine(allNotes, _uiState) { notes, state ->
            var filtered = notes

            if (state.showFavoritesOnly) {
                filtered = filtered.filter { it.isFavorite }
            }

            if (state.searchQuery.isNotEmpty()) {
                filtered = filtered.filter { note ->
                    note.title.contains(state.searchQuery, ignoreCase = true) ||
                            note.content.contains(state.searchQuery, ignoreCase = true) ||
                            note.tags.contains(state.searchQuery, ignoreCase = true)
                }
            }

            filtered
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}

data class NotesUiState(
    val showFavoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val editingNote: com.sleeplife.app.data.entities.Note? = null
)
