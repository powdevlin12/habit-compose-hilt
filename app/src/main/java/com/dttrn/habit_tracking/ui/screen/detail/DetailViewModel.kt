package com.dttrn.habit_tracking.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.repository.HabitRepository
import com.dttrn.habit_tracking.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DetailUiState(
    val habit: Habit? = null,
    val loggedDates: List<LocalDate> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalLogs: Int = 0,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            repository.getHabitById(habitId).collect { habit ->
                if (habit != null) {
                    val loggedDates = repository.getLoggedDates(habitId)
                    val currentStreak = repository.calculateCurrentStreak(habitId)
                    val longestStreak = repository.calculateLongestStreak(habitId)

                    _uiState.update {
                        it.copy(
                            habit = habit,
                            loggedDates = loggedDates,
                            currentStreak = currentStreak,
                            longestStreak = longestStreak,
                            totalLogs = loggedDates.size,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(habit = null, isLoading = false)
                    }
                }
            }
        }
    }

    fun deleteHabit() {
        val habitId = _uiState.value.habit?.id ?: return
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun archiveHabit() {
        val habitId = _uiState.value.habit?.id ?: return
        viewModelScope.launch {
            repository.archiveHabit(habitId, true)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun toggleLogForDate(date: LocalDate) {
        val habitId = _uiState.value.habit?.id ?: return
        viewModelScope.launch {
            repository.toggleLog(habitId, date)
            val updatedDates = repository.getLoggedDates(habitId)
            val updatedStreak = repository.calculateCurrentStreak(habitId)
            val updatedLongest = repository.calculateLongestStreak(habitId)
            _uiState.update {
                it.copy(
                    loggedDates = updatedDates,
                    currentStreak = updatedStreak,
                    longestStreak = updatedLongest,
                    totalLogs = updatedDates.size
                )
            }
        }
    }
}
