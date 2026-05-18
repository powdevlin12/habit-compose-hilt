package com.dttrn.habit_tracking.data.repository

import com.dttrn.habit_tracking.data.db.dao.ChallengeDao
import com.dttrn.habit_tracking.data.db.dao.HabitLogDao
import com.dttrn.habit_tracking.data.db.entity.ChallengeEntity
import com.dttrn.habit_tracking.domain.model.Challenge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepository @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val habitLogDao: HabitLogDao
) {
    fun getChallengesForProfile(profileId: Int): Flow<List<Challenge>> =
        challengeDao.getChallengesForProfile(profileId)
            .map { list -> list.map { it.toDomain() } }

    fun getChallengesForHabit(habitId: Int): Flow<List<Challenge>> =
        challengeDao.getChallengesForHabit(habitId)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insertChallenge(challenge: Challenge): Long =
        challengeDao.insertChallenge(challenge.toEntity())

    suspend fun deleteChallenge(id: Int) = challengeDao.deleteChallenge(id)

    /**
     * Tính số ngày đã hoàn thành trong challenge (dựa trên habit_logs trong khoảng thời gian).
     */
    suspend fun getCompletedDaysCount(challenge: Challenge): Int {
        val loggedDates = habitLogDao.getLoggedDates(challenge.habitId)
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()

        var count = 0
        var date = challenge.startDate
        while (!date.isAfter(challenge.endDate) && !date.isAfter(LocalDate.now())) {
            if (loggedDates.contains(date)) count++
            date = date.plusDays(1)
        }
        return count
    }

    /**
     * Kiểm tra và đánh dấu challenge là hoàn thành nếu đủ ngày.
     * Trả về true nếu vừa được đánh dấu completed.
     */
    suspend fun checkAndMarkCompleted(challenge: Challenge): Boolean {
        if (challenge.isCompleted) return false
        val today = LocalDate.now()
        if (today.isBefore(challenge.endDate)) return false

        val completedDays = getCompletedDaysCount(challenge)
        val threshold = (challenge.durationDays * 0.8).toInt() // 80% là đạt
        return if (completedDays >= threshold) {
            challengeDao.markCompleted(challenge.id)
            true
        } else false
    }

    private fun ChallengeEntity.toDomain() = Challenge(
        id = id,
        habitId = habitId,
        title = title,
        description = description,
        durationDays = durationDays,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        isCompleted = isCompleted,
        createdAt = createdAt,
        profileId = profileId
    )

    private fun Challenge.toEntity() = ChallengeEntity(
        id = id,
        habitId = habitId,
        title = title,
        description = description,
        durationDays = durationDays,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        isCompleted = isCompleted,
        createdAt = createdAt,
        profileId = profileId
    )
}
