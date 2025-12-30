package com.sleeplife.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplife.app.data.entities.PomodoroSession
import com.sleeplife.app.data.entities.SessionType
import com.sleeplife.app.data.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    val todaySessions = pomodoroRepository.getTodaySessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        loadTodayStats()
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            val todayFocusTime = pomodoroRepository.getTodayFocusTime()
            _uiState.update { it.copy(
                todayFocusMinutes = todayFocusTime
            ) }
        }
    }

    fun startSession(taskName: String, duration: Int) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val startTime = kotlinx.datetime.LocalDateTime.parse(
                now.toString(),
                kotlinx.datetime.format.DateTimeFormat.ISO_LOCAL_DATE_TIME
            )

            val session = PomodoroSession(
                taskName = taskName,
                duration = duration,
                actualDuration = 0,
                sessionType = SessionType.WORK,
                completed = false,
                startTime = startTime
            )

            val sessionId = pomodoroRepository.insertSession(session)

            _uiState.update { it.copy(
                isRunning = true,
                currentSessionId = sessionId,
                currentTaskName = taskName,
                sessionType = SessionType.WORK,
                remainingSeconds = duration * 60,
                totalSeconds = duration * 60
            ) }

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && _uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(
                    remainingSeconds = it.remainingSeconds - 1
                ) }
            }

            if (_uiState.value.remainingSeconds <= 0) {
                completeSession()
            }
        }
    }

    fun pauseSession() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resumeSession() {
        _uiState.update { it.copy(isRunning = true) }
        startTimer()
    }

    fun stopSession() {
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value.currentSessionId?.let { id ->
                val session = pomodoroRepository.getSessionById(id)
                session?.let {
                    val elapsedSeconds = _uiState.value.totalSeconds - _uiState.value.remainingSeconds
                    val updatedSession = it.copy(
                        actualDuration = elapsedSeconds / 60,
                        interrupted = true
                    )
                    pomodoroRepository.updateSession(updatedSession)
                }
            }
        }

        _uiState.update { it.copy(
            isRunning = false,
            currentSessionId = null,
            currentTaskName = "",
            remainingSeconds = 0,
            totalSeconds = 0
        ) }
    }

    private fun completeSession() {
        timerJob?.cancel()
        viewModelScope.launch {
            val endTime = kotlinx.datetime.LocalDateTime.parse(
                Clock.System.now().toString(),
                kotlinx.datetime.format.DateTimeFormat.ISO_LOCAL_DATE_TIME
            )

            _uiState.value.currentSessionId?.let { id ->
                val session = pomodoroRepository.getSessionById(id)
                session?.let {
                    val elapsedSeconds = _uiState.value.totalSeconds
                    val updatedSession = it.copy(
                        actualDuration = elapsedSeconds / 60,
                        completed = true,
                        endTime = endTime
                    )
                    pomodoroRepository.updateSession(updatedSession)
                }
            }

            _uiState.update { it.copy(
                isRunning = false,
                currentSessionId = null,
                currentTaskName = "",
                remainingSeconds = 0,
                totalSeconds = 0,
                sessionCompleted = true
            ) }

            loadTodayStats()

            delay(3000)
            _uiState.update { it.copy(sessionCompleted = false) }
        }
    }

    fun showStartDialog() {
        _uiState.update { it.copy(showStartDialog = true) }
    }

    fun hideStartDialog() {
        _uiState.update { it.copy(showStartDialog = false) }
    }

    fun onSessionTypeChange(type: SessionType) {
        _uiState.update { it.copy(sessionType = type) }
    }
}

data class PomodoroUiState(
    val isRunning: Boolean = false,
    val currentSessionId: Long? = null,
    val currentTaskName: String = "",
    val sessionType: SessionType = SessionType.WORK,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val showStartDialog: Boolean = false,
    val sessionCompleted: Boolean = false,
    val todayFocusMinutes: Int = 0
)
