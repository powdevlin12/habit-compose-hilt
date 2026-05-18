package com.dttrn.habit_tracking.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu các thách thức 30 ngày.
 * Mỗi challenge gắn với 1 thói quen và có start/end date.
 */
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val habitId: Int,
    val title: String,
    val description: String? = null,
    val durationDays: Int = 30,
    val startDate: String,         // yyyy-MM-dd
    val endDate: String,           // yyyy-MM-dd (startDate + durationDays - 1)
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val profileId: Int = 1
)
