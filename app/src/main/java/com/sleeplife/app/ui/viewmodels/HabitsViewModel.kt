package com.sleeplife.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplife.app.data.entities.Habit
import com.sleeplife.app.data.entities.HabitCheckIn
import com.sleeplife.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    val allHabits = habitRepository.getAllActiveHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedHabit = MutableStateFlow<Habit?>(null)
    val selectedHabit: StateFlow<Habit?> = _selectedHabit.asStateFlow()

    val habitCheckIns = _selectedHabit.flatMapLatest { habit ->
        habit?.let { habitRepository.getHabitCheckIns(it.id) } ?: flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadHabitsWithCheckIns()
    }

    private fun loadHabitsWithCheckIns() {
        viewModelScope.launch {
            val habits = habitRepository.getAllActiveHabitsSync()
            val habitsWithProgress = habits.map { habit ->
                val checkInCount = habitRepository.getCheckInCount(habit.id)
                val todayCheckIn = habitRepository.getTodayCheckIn(habit.id)
                HabitWithProgress(
                    habit = habit,
                    checkInCount = checkInCount,
                    todayCheckedIn = todayCheckIn != null,
                    progress = (checkInCount.toFloat() / habit.targetDays.toFloat()).coerceAtMost(1f)
                )
            }
            _uiState.update { it.copy(habitsWithProgress = habitsWithProgress) }
        }
    }

    fun selectHabit(habit: Habit) {
        _selectedHabit.value = habit
    }

    fun clearSelectedHabit() {
        _selectedHabit.value = null
    }

    fun showAddHabitDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddHabitDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addHabit(name: String, description: String, icon: String, targetDays: Int, color: Long) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                icon = icon,
                targetDays = targetDays,
                color = color
            )
            habitRepository.insertHabit(habit)
            hideAddHabitDialog()
            loadHabitsWithCheckIns()
        }
    }

    fun checkInHabit(habitId: Long, note: String = "") {
        viewModelScope.launch {
            val now = Clock.System.now()
            val checkIn = HabitCheckIn(
                habitId = habitId,
                checkInDate = LocalDateTime.parse(
                    now.toString(),
                    kotlinx.datetime.format.DateTimeFormat.ISO_LOCAL_DATE_TIME
                ),
                note = note
            )
            habitRepository.insertCheckIn(checkIn)
            loadHabitsWithCheckIns()
        }
    }

    fun undoCheckIn(habitId: Long) {
        viewModelScope.launch {
            val todayCheckIn = habitRepository.getTodayCheckIn(habitId)
            todayCheckIn?.let {
                habitRepository.deleteCheckInById(it.id)
                loadHabitsWithCheckIns()
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
            loadHabitsWithCheckIns()
        }
    }
}

data class HabitsUiState(
    val habitsWithProgress: List<HabitWithProgress> = emptyList(),
    val showAddDialog: Boolean = false
)

data class HabitWithProgress(
    val habit: Habit,
    val checkInCount: Int,
    val todayCheckedIn: Boolean,
    val progress: Float
)
