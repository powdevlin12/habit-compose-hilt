package com.dttrn.habit_tracking.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dttrn.habit_tracking.data.db.dao.ChallengeDao
import com.dttrn.habit_tracking.data.db.dao.HabitDao
import com.dttrn.habit_tracking.data.db.dao.HabitLogDao
import com.dttrn.habit_tracking.data.db.dao.ProfileDao
import com.dttrn.habit_tracking.data.db.entity.ChallengeEntity
import com.dttrn.habit_tracking.data.db.entity.HabitEntity
import com.dttrn.habit_tracking.data.db.entity.HabitLogEntity
import com.dttrn.habit_tracking.data.db.entity.ProfileEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        ProfileEntity::class,
        ChallengeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun profileDao(): ProfileDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        /**
         * Migration 1→2:
         * - Thêm bảng profiles
         * - Thêm cột profileId vào habits (default 1)
         * - Thêm bảng challenges
         * - Insert profile mặc định "Tôi" (id=1)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Tạo bảng profiles
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS profiles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        avatarEmoji TEXT NOT NULL DEFAULT '🙂',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                // 2. Insert profile mặc định id=1
                db.execSQL(
                    "INSERT INTO profiles (id, name, avatarEmoji, createdAt) VALUES (1, 'Tôi', '🙂', ${System.currentTimeMillis()})"
                )

                // 3. Thêm cột profileId vào habits
                db.execSQL("ALTER TABLE habits ADD COLUMN profileId INTEGER NOT NULL DEFAULT 1")

                // 4. Tạo bảng challenges
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS challenges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        durationDays INTEGER NOT NULL DEFAULT 30,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        profileId INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
