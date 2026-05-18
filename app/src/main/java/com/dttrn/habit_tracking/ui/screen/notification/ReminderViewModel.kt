package com.dttrn.habit_tracking.ui.screen.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.repository.HabitRepository
import com.dttrn.habit_tracking.domain.model.Habit
import com.dttrn.habit_tracking.utils.LocalNotificationManager
import com.dttrn.habit_tracking.utils.NotificationScheduler
import com.dttrn.habit_tracking.worker.WorkManagerReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(
    val habitsWithReminder: List<Habit> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val notificationManager: LocalNotificationManager,
    private val scheduler: NotificationScheduler,
    private val repository: HabitRepository,
    private val reminderScheduler: WorkManagerReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        loadHabitsWithReminder()
    }

    private fun loadHabitsWithReminder() {
        viewModelScope.launch {
            repository.getAllActiveHabits().collect { habits ->
                _uiState.update {
                    it.copy(
                        habitsWithReminder = habits.filter { h -> h.reminderTime != null },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun canScheduleExact() = scheduler.canScheduleExact()

    /** Mở màn hình Settings để user cấp quyền Exact Alarm */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }

    /** Test: Hiện notification ngay lập tức */
    @androidx.annotation.RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTestNotification() {
        notificationManager.showSimpleNotification(
            id = 1001,
            title = "🔔 Test Nhắc nhở",
            message = "Đây là thông báo thử nghiệm từ Habit Journey!"
        )
    }
}