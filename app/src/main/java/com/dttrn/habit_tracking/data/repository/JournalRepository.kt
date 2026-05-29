package com.dttrn.habit_tracking.data.repository

import com.dttrn.habit_tracking.data.db.dao.JournalDao
import com.dttrn.habit_tracking.data.db.entity.JournalEntity
import com.dttrn.habit_tracking.domain.model.Journal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAllJournalDates(): Flow<List<LocalDate>> {
        return journalDao.getAllJournalDates().map { dates ->
            dates.map { LocalDate.parse(it, formatter) }
        }
    }

    suspend fun getJournalByDate(date: LocalDate): Journal? {
        val dateString = date.format(formatter)
        val entity = journalDao.getJournalByDate(dateString)
        return entity?.toDomain()
    }

    suspend fun saveJournal(journal: Journal) {
        journalDao.insertOrUpdateJournal(journal.toEntity())
    }

    suspend fun deleteJournal(date: LocalDate) {
        journalDao.deleteJournalByDate(date.format(formatter))
    }

    private fun JournalEntity.toDomain(): Journal {
        return Journal(
            date = LocalDate.parse(this.date, formatter),
            content = this.content,
            updatedAt = this.updatedAt
        )
    }

    private fun Journal.toEntity(): JournalEntity {
        return JournalEntity(
            date = this.date.format(formatter),
            content = this.content,
            updatedAt = this.updatedAt
        )
    }
}
