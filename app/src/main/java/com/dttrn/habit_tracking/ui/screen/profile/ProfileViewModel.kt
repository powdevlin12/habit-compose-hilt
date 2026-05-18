package com.dttrn.habit_tracking.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.repository.ProfileRepository
import com.dttrn.habit_tracking.domain.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: Int = 1,
    val isLoading: Boolean = true,
    val showCreateSheet: Boolean = false,
    val editingProfile: Profile? = null,
    val inputName: String = "",
    val inputEmoji: String = "🙂",
    val nameError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            combine(
                profileRepository.getAllProfiles(),
                profileRepository.activeProfileId
            ) { profiles, activeId ->
                Pair(profiles, activeId)
            }.collect { (profiles, activeId) ->
                _uiState.update {
                    it.copy(
                        profiles = profiles,
                        activeProfileId = activeId,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun switchProfile(profileId: Int) {
        profileRepository.setActiveProfile(profileId)
    }

    fun onShowCreateSheet(editProfile: Profile? = null) {
        _uiState.update {
            it.copy(
                showCreateSheet = true,
                editingProfile = editProfile,
                inputName = editProfile?.name ?: "",
                inputEmoji = editProfile?.avatarEmoji ?: "🙂",
                nameError = null
            )
        }
    }

    fun onDismissSheet() = _uiState.update { it.copy(showCreateSheet = false, editingProfile = null) }

    fun onNameChange(name: String) = _uiState.update { it.copy(inputName = name, nameError = null) }
    fun onEmojiChange(emoji: String) = _uiState.update { it.copy(inputEmoji = emoji) }

    fun saveProfile() {
        val state = _uiState.value
        if (state.inputName.isBlank()) {
            _uiState.update { it.copy(nameError = "Tên profile không được để trống") }
            return
        }
        viewModelScope.launch {
            val profile = Profile(
                id = state.editingProfile?.id ?: 0,
                name = state.inputName.trim(),
                avatarEmoji = state.inputEmoji
            )
            if (state.editingProfile == null) {
                val newId = profileRepository.insertProfile(profile).toInt()
                profileRepository.setActiveProfile(newId)
            } else {
                profileRepository.updateProfile(profile)
            }
            _uiState.update { it.copy(showCreateSheet = false, editingProfile = null) }
        }
    }

    fun deleteProfile(profileId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            // Không cho xóa profile đang active hoặc profile cuối cùng
            if (state.profiles.size <= 1 || profileId == state.activeProfileId) return@launch
            profileRepository.deleteProfile(profileId)
            // Nếu không còn profile active hợp lệ → chuyển sang profile đầu
            val remaining = state.profiles.filter { it.id != profileId }
            if (remaining.isNotEmpty()) {
                profileRepository.setActiveProfile(remaining.first().id)
            }
        }
    }
}
