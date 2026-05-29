package com.dttrn.habit_tracking.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dttrn.habit_tracking.data.db.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateJournal(journal: JournalEntity)

    @Query("SELECT * FROM journals WHERE date = :date")
    suspend fun getJournalByDate(date: String): JournalEntity?

    @Query("SELECT date FROM journals")
    fun getAllJournalDates(): Flow<List<String>>

    @Query("DELETE FROM journals WHERE date = :date")
    suspend fun deleteJournalByDate(date: String)
}
