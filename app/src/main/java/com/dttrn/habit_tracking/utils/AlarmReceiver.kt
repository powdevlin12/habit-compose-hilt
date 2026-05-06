package com.dttrn.habit_tracking.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dttrn.habit_tracking.R

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val id      = intent.getIntExtra("notification_id", 0)
        val title   = intent.getStringExtra("title") ?: "Nhắc nhở"
        val message = intent.getStringExtra("message") ?: ""

        // Hiển thị notification
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}