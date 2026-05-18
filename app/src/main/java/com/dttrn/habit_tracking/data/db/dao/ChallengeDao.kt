package com.dttrn.habit_tracking.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dttrn.habit_tracking.data.db.entity.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getChallengesForProfile(profileId: Int): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE habitId = :habitId ORDER BY createdAt DESC")
    fun getChallengesForHabit(habitId: Int): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: Int): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity): Long

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteChallenge(id: Int)

    @Query("UPDATE challenges SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Int)
}
