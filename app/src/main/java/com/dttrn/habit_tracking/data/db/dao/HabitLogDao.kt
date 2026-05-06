package com.dttrn.habit_tracking.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dttrn.habit_tracking.data.db.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY loggedDate DESC")
    fun getLogsForHabit(habitId: Int): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY loggedDate DESC")
    suspend fun getLogsForHabitOnce(habitId: Int): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE loggedDate = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND loggedDate = :date LIMIT 1")
    suspend fun getLogForHabitAndDate(habitId: Int, date: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND loggedDate >= :startDate ORDER BY loggedDate ASC")
    suspend fun getLogsFromDate(habitId: Int, startDate: String): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Delete
    suspend fun deleteLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND loggedDate = :date")
    suspend fun deleteLogForDate(habitId: Int, date: String)

    @Query("SELECT loggedDate FROM habit_logs WHERE habitId = :habitId ORDER BY loggedDate DESC")
    suspend fun getLoggedDates(habitId: Int): List<String>

    @Query("SELECT loggedDate FROM habit_logs WHERE habitId = :habitId AND loggedDate >= :startDate ORDER BY loggedDate ASC")
    suspend fun getLoggedDatesFrom(habitId: Int, startDate: String): List<String>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE loggedDate = :date")
    suspend fun getLogCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND loggedDate >= :startDate AND loggedDate <= :endDate")
    suspend fun getLogCountInRange(habitId: Int, startDate: String, endDate: String): Int

    @Query("SELECT loggedDate, COUNT(*) as cnt FROM habit_logs WHERE loggedDate >= :startDate GROUP BY loggedDate ORDER BY loggedDate ASC")
    suspend fun getDailyLogCountsFrom(startDate: String): List<DailyCount>

    @Query("SELECT * FROM habit_logs ORDER BY loggedDate DESC")
    suspend fun getAllLogsOnce(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()
}

data class DailyCount(val loggedDate: String, val cnt: Int)
