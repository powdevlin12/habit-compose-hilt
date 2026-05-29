package com.dttrn.habit_tracking.ui.screen.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.repository.JournalRepository
import com.dttrn.habit_tracking.domain.model.Journal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalDetailUiState(
    val date: LocalDate = LocalDate.now(),
    val content: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class JournalDetailViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalDetailUiState())
    val uiState: StateFlow<JournalDetailUiState> = _uiState.asStateFlow()

    init {
        val dateString = savedStateHandle.get<String>("date")
        if (dateString != null) {
            val date = LocalDate.parse(dateString)
            _uiState.update { it.copy(date = date) }
            loadJournal(date)
        }
    }

    private fun loadJournal(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val journal = journalRepository.getJournalByDate(date)
            _uiState.update {
                it.copy(
                    content = journal?.content ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun updateContent(newContent: String) {
        _uiState.update { it.copy(content = newContent, saveSuccess = false) }
    }

    fun saveJournal() {
        val currentState = _uiState.value
        if (currentState.content.isBlank()) {
            // Nếu nội dung trống, coi như xóa nhật ký
            deleteJournal()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val journal = Journal(
                date = currentState.date,
                content = currentState.content,
                updatedAt = System.currentTimeMillis()
            )
            journalRepository.saveJournal(journal)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun deleteJournal() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            journalRepository.deleteJournal(currentState.date)
            _uiState.update { it.copy(content = "", isSaving = false, saveSuccess = true) }
        }
    }
    
    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
