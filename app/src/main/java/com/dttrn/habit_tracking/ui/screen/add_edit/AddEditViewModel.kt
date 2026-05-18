package com.dttrn.habit_tracking.ui.screen.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.preferences.ProfilePreferences
import com.dttrn.habit_tracking.data.repository.HabitRepository
import com.dttrn.habit_tracking.domain.model.Habit
import com.dttrn.habit_tracking.domain.model.HabitFrequency
import com.dttrn.habit_tracking.worker.WorkManagerReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val habitId: Int? = null,
    val name: String = "",
    val description: String = "",
    val iconEmoji: String = "✅",
    val colorHex: String = "#4CAF50",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDays: List<Int> = emptyList(),
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:00",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderScheduler: WorkManagerReminderScheduler,
    private val profilePreferences: ProfilePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val habit = repository.getHabitByIdOnce(habitId)
            if (habit != null) {
                _uiState.update {
                    it.copy(
                        habitId = habit.id,
                        name = habit.name,
                        description = habit.description ?: "",
                        iconEmoji = habit.iconEmoji,
                        colorHex = habit.colorHex,
                        frequency = habit.frequency,
                        targetDays = habit.targetDays,
                        reminderEnabled = habit.reminderTime != null,
                        reminderTime = habit.reminderTime ?: "08:00",
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value) }

    fun onIconChange(emoji: String) =
        _uiState.update { it.copy(iconEmoji = emoji) }

    fun onColorChange(hex: String) =
        _uiState.update { it.copy(colorHex = hex) }

    fun onFrequencyChange(freq: HabitFrequency) =
        _uiState.update { it.copy(frequency = freq, targetDays = emptyList()) }

    fun onTargetDayToggle(day: Int) {
        val current = _uiState.value.targetDays.toMutableList()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _uiState.update { it.copy(targetDays = current.sorted()) }
    }

    fun onReminderToggle(enabled: Boolean) =
        _uiState.update { it.copy(reminderEnabled = enabled) }

    fun onReminderTimeChange(time: String) =
        _uiState.update { it.copy(reminderTime = time) }

    fun saveHabit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Tên thói quen không được để trống") }
            return
        }

        viewModelScope.launch {
            val isDuplicate = repository.isDuplicateName(state.name.trim(), state.habitId ?: 0)
            if (isDuplicate) {
                _uiState.update { it.copy(nameError = "Tên thói quen đã tồn tại") }
                return@launch
            }

            val habit = Habit(
                id = state.habitId ?: 0,
                name = state.name.trim(),
                description = state.description.trim().ifBlank { null },
                iconEmoji = state.iconEmoji,
                colorHex = state.colorHex,
                frequency = state.frequency,
                targetDays = if (state.frequency == HabitFrequency.CUSTOM) state.targetDays else emptyList(),
                reminderTime = if (state.reminderEnabled) state.reminderTime else null,
                profileId = profilePreferences.activeProfileId.value
            )

            val savedId = if (state.habitId == null) {
                repository.insertHabit(habit).toInt()
            } else {
                repository.updateHabit(habit)
                state.habitId
            }

            // Schedule hoặc cancel WorkManager reminder
            if (state.reminderEnabled) {
                reminderScheduler.scheduleReminder(
                    habitId = savedId,
                    habitName = habit.name,
                    habitEmoji = habit.iconEmoji,
                    reminderTime = state.reminderTime
                )
            } else {
                reminderScheduler.cancelReminder(savedId)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
