package com.dttrn.habit_tracking.ui.screen.statistics

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

data class HabitStats(
    val habit: Habit,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate30Days: Float
)

data class WeeklyBarData(
    val weekLabel: String,
    val count: Int
)

data class StatisticsUiState(
    val activeHabitCount: Int = 0,
    val avgCompletionRate30Days: Float = 0f,
    val topStreakHabit: Habit? = null,
    val topStreakValue: Int = 0,
    val habitStatsList: List<HabitStats> = emptyList(),
    val weeklyData: List<WeeklyBarData> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            repository.getAllActiveHabits().collect { habits ->
                if (habits.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                val today = LocalDate.now()
                val statsPerHabit = habits.map { habit ->
                    val streak = repository.calculateCurrentStreak(habit.id)
                    val longest = repository.calculateLongestStreak(habit.id)
                    val rate = repository.getCompletionRateLastDays(habit.id, 30)
                    HabitStats(habit, streak, longest, rate)
                }

                val avgRate = if (statsPerHabit.isEmpty()) 0f
                else statsPerHabit.sumOf { it.completionRate30Days.toDouble() }.toFloat() / statsPerHabit.size

                val topStreakEntry = statsPerHabit.maxByOrNull { it.currentStreak }

                // Weekly bar chart data (8 weeks)
                val weeklyData = (7 downTo 0).map { weeksBack ->
                    val weekStart = today.minusWeeks(weeksBack.toLong())
                        .with(java.time.DayOfWeek.MONDAY)
                    val weekEnd = weekStart.plusDays(6)
                    var totalLogs = 0
                    habits.forEach { habit ->
                        val logs = repository.getLoggedDates(habit.id)
                        totalLogs += logs.count { !it.isBefore(weekStart) && !it.isAfter(weekEnd) }
                    }
                    val label = if (weeksBack == 0) "Tuần này"
                    else if (weeksBack == 1) "Tuần trước"
                    else "T-$weeksBack"
                    WeeklyBarData(label, totalLogs)
                }

                _uiState.update {
                    it.copy(
                        activeHabitCount = habits.size,
                        avgCompletionRate30Days = avgRate,
                        topStreakHabit = topStreakEntry?.habit,
                        topStreakValue = topStreakEntry?.currentStreak ?: 0,
                        habitStatsList = statsPerHabit.sortedByDescending { s -> s.currentStreak },
                        weeklyData = weeklyData,
                        isLoading = false
                    )
                }
            }
        }
    }
}
