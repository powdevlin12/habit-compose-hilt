package com.dttrn.habit_tracking.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {

    // Định nghĩa các channel
    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_ALERT    = "channel_alert"
    const val CHANNEL_SILENT   = "channel_silent"

    fun createChannels(context: Context) {
        val channels = listOf(
            // Channel quan trọng - có âm thanh + popup
            NotificationChannel(
                CHANNEL_REMINDER,
                "Nhắc nhở",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            },

            // Channel bình thường - có âm thanh
            NotificationChannel(
                CHANNEL_ALERT,
                "Cảnh báo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo cảnh báo"
            },

            // Channel im lặng - không âm thanh
            NotificationChannel(
                CHANNEL_SILENT,
                "Im lặng",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo im lặng"
            }
        )

        val manager = context.getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}