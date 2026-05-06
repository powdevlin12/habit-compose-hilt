package com.dttrn.habit_tracking.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dttrn.habit_tracking.MainActivity
import com.dttrn.habit_tracking.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    // ① Notification đơn giản
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSimpleNotification(
        id: Int,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // tự xóa khi bấm vào
            .build()

        notificationManager.notify(id, notification)
    }

    // ② Notification với text dài
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showBigTextNotification(
        id: Int,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)       // nội dung dài khi mở rộng
                    .setBigContentTitle(title)
                    .setSummaryText("App của bạn")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    // ③ Notification có nút action
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showActionNotification(
        id: Int,
        title: String,
        message: String
    ) {
        // Intent khi bấm "Xem ngay"
        val viewIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_id", id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent khi bấm "Bỏ qua"
        val dismissIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = "ACTION_DISMISS"
                putExtra("notification_id", id)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .addAction(R.drawable.ic_launcher_background, "Xem ngay", viewIntent)
            .addAction(R.drawable.ic_launcher_background, "Bỏ qua", dismissIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    // ④ Notification có progress bar
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showProgressNotification(id: Int, title: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText("$progress%")
            .setProgress(100, progress, false) // (max, current, indeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // không thể vuốt xóa khi đang chạy
            .build()

        notificationManager.notify(id, notification)
    }

    // Xóa notification
    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    // Xóa tất cả
    fun cancelAll() {
        notificationManager.cancelAll()
    }
}