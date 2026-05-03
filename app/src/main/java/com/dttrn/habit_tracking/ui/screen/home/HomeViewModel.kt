package com.dttrn.habit_tracking.ui.screen.home

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

data class HabitWithStatus(
    val habit: Habit,
    val isLoggedToday: Boolean,
    val currentStreak: Int,
    val loggedDates: List<LocalDate>
)

data class HomeUiState(
    val habitsWithStatus: List<HabitWithStatus> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHabits()
    }

    fun observeHabits() {
        viewModelScope.launch {
            repository.getAllActiveHabits().collect { habits ->
                refreshHabitsState(habits)
            }
        }
    }

    private suspend fun refreshHabitsState(habits: List<Habit>) {
        val today = LocalDate.now()
        val habitsWithStatus = habits.map { habit ->
            val isLoggedToday = repository.isLoggedForDate(habit.id, today)
            val streak = repository.calculateCurrentStreak(habit.id)
            val loggedDates = repository.getLoggedDates(habit.id)
            HabitWithStatus(habit, isLoggedToday, streak, loggedDates)
        }
        _uiState.update {
            it.copy(
                habitsWithStatus = habitsWithStatus,
                completedCount = habitsWithStatus.count { h -> h.isLoggedToday },
                totalCount = habitsWithStatus.size,
                isLoading = false,
                today = today
            )
        }
    }

    fun toggleCheckIn(habitId: Int) {
        viewModelScope.launch {
            repository.toggleLog(habitId, LocalDate.now())
            // Flow sẽ tự emit lại, nhưng cần update status ngay lập tức
            val current = _uiState.value
            val updated = current.habitsWithStatus.map { h ->
                if (h.habit.id == habitId) {
                    val newLogged = !h.isLoggedToday
                    val newStreak = repository.calculateCurrentStreak(habitId)
                    val newDates = repository.getLoggedDates(habitId)
                    h.copy(isLoggedToday = newLogged, currentStreak = newStreak, loggedDates = newDates)
                } else h
            }
            _uiState.update {
                it.copy(
                    habitsWithStatus = updated,
                    completedCount = updated.count { h -> h.isLoggedToday }
                )
            }
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun archiveHabit(habitId: Int) {
        viewModelScope.launch {
            repository.archiveHabit(habitId, true)
        }
    }
}
