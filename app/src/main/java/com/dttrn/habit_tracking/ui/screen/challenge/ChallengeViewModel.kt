package com.dttrn.habit_tracking.ui.screen.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.preferences.ProfilePreferences
import com.dttrn.habit_tracking.data.repository.ChallengeRepository
import com.dttrn.habit_tracking.data.repository.HabitRepository
import com.dttrn.habit_tracking.domain.model.Challenge
import com.dttrn.habit_tracking.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ChallengeWithProgress(
    val challenge: Challenge,
    val habit: Habit?,
    val completedDays: Int,
    val progressPercent: Float,
    val daysLeft: Int
)

data class ChallengeUiState(
    val challenges: List<ChallengeWithProgress> = emptyList(),
    val activeHabits: List<Habit> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateSheet: Boolean = false,
    val selectedHabitId: Int? = null,
    val challengeTitle: String = "",
    val challengeDescription: String = "",
    val durationDays: Int = 30,
    val titleError: String? = null
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val habitRepository: HabitRepository,
    private val profilePreferences: ProfilePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profileId = profilePreferences.activeProfileId.value
            combine(
                challengeRepository.getChallengesForProfile(profileId),
                habitRepository.getAllActiveHabits(profileId)
            ) { challenges, habits ->
                val habitMap = habits.associateBy { it.id }
                val withProgress = challenges.map { challenge ->
                    val completed = challengeRepository.getCompletedDaysCount(challenge)
                    val progress = completed.toFloat() / challenge.durationDays
                    val today = LocalDate.now()
                    val daysLeft = maxOf(0, today.until(challenge.endDate, java.time.temporal.ChronoUnit.DAYS).toInt())
                    ChallengeWithProgress(
                        challenge = challenge,
                        habit = habitMap[challenge.habitId],
                        completedDays = completed,
                        progressPercent = progress.coerceIn(0f, 1f),
                        daysLeft = daysLeft
                    )
                }
                Pair(withProgress, habits)
            }.collect { (withProgress, habits) ->
                _uiState.update {
                    it.copy(
                        challenges = withProgress,
                        activeHabits = habits,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onShowCreateSheet() = _uiState.update {
        it.copy(
            showCreateSheet = true,
            selectedHabitId = it.activeHabits.firstOrNull()?.id,
            challengeTitle = "",
            challengeDescription = "",
            durationDays = 30,
            titleError = null
        )
    }

    fun onDismissCreateSheet() = _uiState.update { it.copy(showCreateSheet = false) }

    fun onTitleChange(title: String) = _uiState.update { it.copy(challengeTitle = title, titleError = null) }
    fun onDescriptionChange(desc: String) = _uiState.update { it.copy(challengeDescription = desc) }
    fun onHabitSelected(habitId: Int) = _uiState.update { it.copy(selectedHabitId = habitId) }
    fun onDurationChange(days: Int) = _uiState.update { it.copy(durationDays = days) }

    fun createChallenge() {
        val state = _uiState.value
        if (state.challengeTitle.isBlank()) {
            _uiState.update { it.copy(titleError = "Tên thách thức không được để trống") }
            return
        }
        val habitId = state.selectedHabitId ?: return
        val profileId = profilePreferences.activeProfileId.value

        viewModelScope.launch {
            val startDate = LocalDate.now()
            val endDate = startDate.plusDays(state.durationDays.toLong() - 1)
            challengeRepository.insertChallenge(
                Challenge(
                    habitId = habitId,
                    title = state.challengeTitle.trim(),
                    description = state.challengeDescription.trim().ifBlank { null },
                    durationDays = state.durationDays,
                    startDate = startDate,
                    endDate = endDate,
                    profileId = profileId
                )
            )
            _uiState.update { it.copy(showCreateSheet = false) }
        }
    }

    fun deleteChallenge(challengeId: Int) {
        viewModelScope.launch { challengeRepository.deleteChallenge(challengeId) }
    }
}
