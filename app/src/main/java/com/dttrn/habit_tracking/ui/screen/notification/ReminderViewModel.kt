package com.dttrn.habit_tracking.ui.screen.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.dttrn.habit_tracking.utils.LocalNotificationManager
import com.dttrn.habit_tracking.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val notificationManager: LocalNotificationManager,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    fun canScheduleExact() = scheduler.canScheduleExact()

    /** Mở màn hình Settings để user cấp quyền Exact Alarm */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }

    // Hiện notification ngay lập tức
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNow() {
        notificationManager.showSimpleNotification(
            id = 1001,
            title = "Nhắc nhở",
            message = "Đã đến giờ uống nước!"
        )
    }

    // Đặt lịch sau 1 tiếng
    fun scheduleAfter1Hour() {
        val triggerTime = System.currentTimeMillis() + (60 * 60 * 1000)
        scheduler.scheduleOnce(
            notificationId = 2001,
            title = "Nhắc nhở",
            message = "Đã 1 tiếng rồi, nghỉ ngơi đi!",
            triggerAtMillis = triggerTime
        )
    }
}