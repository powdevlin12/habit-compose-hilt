package com.dttrn.habit_tracking.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** True nếu app được phép đặt exact alarm */
    fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Dưới Android 12 không cần xin thêm
        }
    }

    /**
     * Đặt lịch thông báo 1 lần.
     * - Nếu có quyền exact alarm → dùng setExactAndAllowWhileIdle (chính xác)
     * - Nếu chưa được cấp → dùng setAndAllowWhileIdle (không chính xác nhưng không crash)
     */
    fun scheduleOnce(
        notificationId: Int,
        title: String,
        message: String,
        triggerAtMillis: Long
    ) {
        val intent = PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra("notification_id", notificationId)
                putExtra("title", title)
                putExtra("message", message)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                intent
            )
        } else {
            // Fallback: inexact alarm, không crash, nhưng hệ thống có thể trễ một chút
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                intent
            )
        }
    }

    // Hủy lịch
    fun cancelScheduled(notificationId: Int) {
        val intent = PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(intent)
    }
}