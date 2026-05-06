package com.dttrn.habit_tracking

import android.app.Application
import com.dttrn.habit_tracking.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabitTrackingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this) // ← tạo channel 1 lần duy nhất
    }
}
