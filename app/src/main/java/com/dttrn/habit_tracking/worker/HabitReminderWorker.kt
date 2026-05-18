package com.dttrn.habit_tracking.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dttrn.habit_tracking.MainActivity
import com.dttrn.habit_tracking.R
import com.dttrn.habit_tracking.data.repository.HabitRepository
import com.dttrn.habit_tracking.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * WorkManager Worker gửi notification nhắc nhở thói quen.
 * Được schedule với tag = "reminder_<habitId>" để có thể cancel khi cần.
 */
@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getInt(KEY_HABIT_ID, -1)
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: return Result.failure()
        val habitEmoji = inputData.getString(KEY_HABIT_EMOJI) ?: "✅"

        if (habitId == -1) return Result.failure()

        // Kiểm tra xem thói quen đã check-in hôm nay chưa
        val isAlreadyLoggedToday = repository.isLoggedForDate(habitId, LocalDate.now())
        if (isAlreadyLoggedToday) {
            // Đã check-in rồi, không cần nhắc
            return Result.success()
        }

        sendNotification(habitId, habitName, habitEmoji)
        return Result.success()
    }

    private fun sendNotification(habitId: Int, habitName: String, habitEmoji: String) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val tapIntent = PendingIntent.getActivity(
            context,
            habitId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("habit_id", habitId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("$habitEmoji Đã đến giờ $habitName rồi! 💪")
            .setContentText("Tap để check-in ngay trong app.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_BASE_ID + habitId, notification)
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_HABIT_EMOJI = "habit_emoji"
        const val NOTIFICATION_BASE_ID = 10000
        fun workTag(habitId: Int) = "habit_reminder_$habitId"
    }
}
