package com.dttrn.habit_tracking.di

import android.content.Context
import androidx.room.Room
import com.dttrn.habit_tracking.data.db.HabitDatabase
import com.dttrn.habit_tracking.data.db.dao.ChallengeDao
import com.dttrn.habit_tracking.data.db.dao.HabitDao
import com.dttrn.habit_tracking.data.db.dao.HabitLogDao
import com.dttrn.habit_tracking.data.db.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_journey.db"
        )
            .addMigrations(HabitDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitLogDao(db: HabitDatabase): HabitLogDao = db.habitLogDao()

    @Provides
    fun provideProfileDao(db: HabitDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideChallengeDao(db: HabitDatabase): ChallengeDao = db.challengeDao()
}
