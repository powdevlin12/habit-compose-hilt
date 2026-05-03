package com.dttrn.habit_tracking.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dttrn.habit_tracking.data.db.dao.HabitDao
import com.dttrn.habit_tracking.data.db.dao.HabitLogDao
import com.dttrn.habit_tracking.data.db.entity.HabitEntity
import com.dttrn.habit_tracking.data.db.entity.HabitLogEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
}
