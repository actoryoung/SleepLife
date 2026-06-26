package com.sleeplife.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplife.app.data.entities.SleepQuality
import com.sleeplife.app.data.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val sleepRepository: SleepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    val recentSleepRecords = sleepRepository.getRecentSleepRecords(7)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadTodaySleep()
    }

    private fun loadTodaySleep() {
        viewModelScope.launch {
            val todaySleep = sleepRepository.getTodaySleep()
            _uiState.update { it.copy(
                todaySleep = todaySleep,
                isTracking = todaySleep?.endTime == null
            ) }
        }
    }

    fun startSleepTracking() {
        viewModelScope.launch {
            val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val sleepRecord = com.sleeplife.app.data.entities.SleepRecord(
                startTime = localDateTime,
                quality = SleepQuality.AVERAGE
            )

            val id = sleepRepository.insertSleepRecord(sleepRecord)
            _uiState.update { it.copy(
                isTracking = true,
                currentSleepId = id,
                sleepStartTime = localDateTime
            ) }
        }
    }

    fun stopSleepTracking(quality: SleepQuality, notes: String = "") {
        viewModelScope.launch {
            val endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            _uiState.value.currentSleepId?.let { id ->
                val record = sleepRepository.getSleepRecordById(id)
                record?.let {
                    val updatedRecord = it.copy(
                        endTime = endTime,
                        quality = quality,
                        notes = notes
                    )
                    val result = sleepRepository.updateSleepRecordWithValidation(updatedRecord)
                    if (result is com.sleeplife.app.core.Result.Error) {
                        _uiState.update { it.copy(errorMessage = result.exception.message) }
                        return@launch
                    }
                }
            }

            _uiState.update { it.copy(
                isTracking = false,
                currentSleepId = null,
                sleepStartTime = null,
                errorMessage = null
            ) }
        }
    }

    fun deleteSleepRecord(id: Long) {
        viewModelScope.launch {
            val result = sleepRepository.deleteSleepRecordsWithValidation(id)
            if (result is com.sleeplife.app.core.Result.Error) {
                _uiState.update { it.copy(errorMessage = result.exception.message) }
            }
        }
    }

    fun showAddSleepDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddSleepDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addSleepRecord(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        quality: SleepQuality,
        notes: String
    ) {
        viewModelScope.launch {
            val sleepRecord = com.sleeplife.app.data.entities.SleepRecord(
                startTime = startTime,
                endTime = endTime,
                quality = quality,
                notes = notes
            )
            val result = sleepRepository.insertSleepRecordWithValidation(sleepRecord)
            if (result is com.sleeplife.app.core.Result.Error) {
                _uiState.update { it.copy(errorMessage = result.exception.message) }
            } else {
                hideAddSleepDialog()
            }
        }
    }
}

data class SleepUiState(
    val isTracking: Boolean = false,
    val currentSleepId: Long? = null,
    val sleepStartTime: LocalDateTime? = null,
    val todaySleep: com.sleeplife.app.data.entities.SleepRecord? = null,
    val showAddDialog: Boolean = false,
    val errorMessage: String? = null
)
