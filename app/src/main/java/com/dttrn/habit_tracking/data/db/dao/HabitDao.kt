package com.dttrn.habit_tracking.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dttrn.habit_tracking.data.db.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isArchived = 0 AND profileId = :profileId ORDER BY createdAt ASC")
    fun getAllActiveHabits(profileId: Int = 1): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 1 AND profileId = :profileId ORDER BY createdAt DESC")
    fun getArchivedHabits(profileId: Int = 1): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: Int): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitByIdOnce(id: Int): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Int)

    @Query("UPDATE habits SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("SELECT COUNT(*) FROM habits WHERE name = :name AND isArchived = 0 AND id != :excludeId AND profileId = :profileId")
    suspend fun countByName(name: String, excludeId: Int = 0, profileId: Int = 1): Int

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0 AND profileId = :profileId")
    fun getActiveHabitCount(profileId: Int = 1): Flow<Int>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    suspend fun getAllHabitsOnce(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
