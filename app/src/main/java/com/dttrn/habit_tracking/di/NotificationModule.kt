package com.dttrn.habit_tracking.di

import android.content.Context
import com.dttrn.habit_tracking.utils.LocalNotificationManager
import com.dttrn.habit_tracking.utils.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Khai báo trong Hilt Module
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideLocalNotificationManager(
        @ApplicationContext context: Context
    ): LocalNotificationManager = LocalNotificationManager(context)

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler = NotificationScheduler(context)
}