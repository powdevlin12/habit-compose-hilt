package com.dttrn.habit_tracking.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey
    val date: String, // format YYYY-MM-DD
    val content: String,
    val updatedAt: Long
)
