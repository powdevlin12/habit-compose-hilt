package com.dttrn.habit_tracking.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service để schedule/cancel WorkManager periodic reminders cho từng thói quen.
 *
 * Chiến lược:
 * - Mỗi habit có 1 PeriodicWorkRequest chạy mỗi 24h, tag = "habit_reminder_<habitId>"
 * - initialDelay tính dựa trên reminderTime (HH:mm) để fire đúng giờ lần đầu
 * - Khi habit xóa reminder → cancel work tag
 */
@Singleton
class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule (hoặc update) reminder cho 1 thói quen.
     * @param habitId ID thói quen
     * @param habitName Tên thói quen
     * @param habitEmoji Emoji thói quen
     * @param reminderTime Giờ nhắc nhở dạng "HH:mm"
     */
    fun scheduleReminder(
        habitId: Int,
        habitName: String,
        habitEmoji: String,
        reminderTime: String
    ) {
        val delayMillis = calculateInitialDelay(reminderTime)

        val inputData = Data.Builder()
            .putInt(HabitReminderWorker.KEY_HABIT_ID, habitId)
            .putString(HabitReminderWorker.KEY_HABIT_NAME, habitName)
            .putString(HabitReminderWorker.KEY_HABIT_EMOJI, habitEmoji)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(HabitReminderWorker.workTag(habitId))
            .build()

        workManager.enqueueUniquePeriodicWork(
            HabitReminderWorker.workTag(habitId),
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // update nếu đã có
            workRequest
        )
    }

    /**
     * Hủy reminder cho 1 thói quen.
     */
    fun cancelReminder(habitId: Int) {
        workManager.cancelUniqueWork(HabitReminderWorker.workTag(habitId))
    }

    /**
     * Tính initial delay (ms) để WorkRequest fire vào đúng reminderTime hôm nay hoặc ngày mai.
     */
    private fun calculateInitialDelay(reminderTime: String): Long {
        return try {
            val parts = reminderTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val now = LocalTime.now()
            val target = LocalTime.of(hour, minute)

            val secondsUntilTarget = if (target.isAfter(now)) {
                now.until(target, java.time.temporal.ChronoUnit.SECONDS)
            } else {
                // Đã qua giờ đó → đợi đến ngày mai
                now.until(target.plusHours(24), java.time.temporal.ChronoUnit.SECONDS)
            }

            secondsUntilTarget * 1000L
        } catch (e: Exception) {
            // Fallback: delay 1 tiếng nếu parse lỗi
            TimeUnit.HOURS.toMillis(1)
        }
    }
}
