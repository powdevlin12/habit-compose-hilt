package com.dttrn.habit_tracking.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", -1)

        when (intent.action) {
            "ACTION_DISMISS" -> {
                // Xóa notification
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
            "ACTION_ACCEPT" -> {
                // Xử lý logic
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }
    }
}