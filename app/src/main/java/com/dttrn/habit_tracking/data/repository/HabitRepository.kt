package com.dttrn.habit_tracking.data.repository

import com.dttrn.habit_tracking.data.db.dao.HabitDao
import com.dttrn.habit_tracking.data.db.dao.HabitLogDao
import com.dttrn.habit_tracking.data.db.entity.HabitEntity
import com.dttrn.habit_tracking.data.db.entity.HabitLogEntity
import com.dttrn.habit_tracking.domain.model.Habit
import com.dttrn.habit_tracking.domain.model.HabitFrequency
import com.dttrn.habit_tracking.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) {
    fun getAllActiveHabits(): Flow<List<Habit>> =
        habitDao.getAllActiveHabits().map { list -> list.map { it.toDomain() } }

    fun getHabitById(id: Int): Flow<Habit?> =
        habitDao.getHabitById(id).map { it?.toDomain() }

    suspend fun getHabitByIdOnce(id: Int): Habit? =
        habitDao.getHabitByIdOnce(id)?.toDomain()

    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>> =
        habitLogDao.getLogsForHabit(habitId).map { list -> list.map { it.toDomain() } }

    suspend fun getLogsForHabitOnce(habitId: Int): List<HabitLog> =
        habitLogDao.getLogsForHabitOnce(habitId).map { it.toDomain() }

    suspend fun insertHabit(habit: Habit): Long =
        habitDao.insertHabit(habit.toEntity())

    suspend fun updateHabit(habit: Habit) =
        habitDao.updateHabit(habit.toEntity())

    suspend fun deleteHabit(habitId: Int) =
        habitDao.deleteHabitById(habitId)

    suspend fun archiveHabit(habitId: Int, archived: Boolean) =
        habitDao.setArchived(habitId, archived)

    suspend fun toggleLog(habitId: Int, date: LocalDate): Boolean {
        val dateStr = date.toString()
        val existing = habitLogDao.getLogForHabitAndDate(habitId, dateStr)
        return if (existing != null) {
            habitLogDao.deleteLog(existing)
            false
        } else {
            habitLogDao.insertLog(HabitLogEntity(habitId = habitId, loggedDate = dateStr))
            true
        }
    }

    suspend fun isLoggedForDate(habitId: Int, date: LocalDate): Boolean =
        habitLogDao.getLogForHabitAndDate(habitId, date.toString()) != null

    suspend fun getLoggedDates(habitId: Int): List<LocalDate> =
        habitLogDao.getLoggedDates(habitId)
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }

    suspend fun calculateCurrentStreak(habitId: Int): Int {
        val loggedSet = getLoggedDates(habitId).toHashSet()
        if (loggedSet.isEmpty()) return 0

        var streak = 0
        var checkDate = LocalDate.now()

        // Nếu hôm nay chưa log, bắt đầu từ hôm qua
        if (!loggedSet.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (loggedSet.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    suspend fun calculateLongestStreak(habitId: Int): Int {
        val loggedDates = getLoggedDates(habitId).sorted()
        if (loggedDates.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until loggedDates.size) {
            currentStreak = if (loggedDates[i] == loggedDates[i - 1].plusDays(1)) {
                currentStreak + 1
            } else {
                1
            }
            if (currentStreak > maxStreak) maxStreak = currentStreak
        }
        return maxStreak
    }

    suspend fun getCompletionRateLastDays(habitId: Int, days: Int): Float {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong() - 1)
        val logCount = habitLogDao.getLogCountInRange(habitId, startDate.toString(), endDate.toString())
        return logCount.toFloat() / days
    }

    suspend fun isDuplicateName(name: String, excludeId: Int = 0): Boolean =
        habitDao.countByName(name.trim(), excludeId) > 0

    // Mapper functions
    private fun HabitEntity.toDomain(): Habit {
        val targetDaysList = targetDays?.let {
            try { Json.decodeFromString<List<Int>>(it) } catch (e: Exception) { emptyList() }
        } ?: emptyList()

        return Habit(
            id = id,
            name = name,
            description = description,
            iconEmoji = iconEmoji,
            colorHex = colorHex,
            frequency = try { HabitFrequency.valueOf(frequency) } catch (e: Exception) { HabitFrequency.DAILY },
            targetDays = targetDaysList,
            reminderTime = reminderTime,
            isArchived = isArchived,
            createdAt = createdAt
        )
    }

    private fun Habit.toEntity(): HabitEntity {
        val targetDaysJson = if (targetDays.isNotEmpty()) {
            Json.encodeToString(targetDays)
        } else null

        return HabitEntity(
            id = id,
            name = name,
            description = description,
            iconEmoji = iconEmoji,
            colorHex = colorHex,
            frequency = frequency.name,
            targetDays = targetDaysJson,
            reminderTime = reminderTime,
            isArchived = isArchived,
            createdAt = createdAt
        )
    }

    private fun HabitLogEntity.toDomain(): HabitLog =
        HabitLog(id = id, habitId = habitId, loggedDate = loggedDate, note = note)
}
