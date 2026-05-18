package com.dttrn.habit_tracking.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val iconEmoji: String = "✅",
    val colorHex: String = "#4CAF50",
    val frequency: String = "DAILY",
    val targetDays: String? = null,
    val reminderTime: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    /** ID của profile sở hữu thói quen này. Default = 1 (profile đầu tiên). */
    val profileId: Int = 1
)
