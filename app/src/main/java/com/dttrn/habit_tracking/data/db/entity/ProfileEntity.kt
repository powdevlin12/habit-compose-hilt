package com.dttrn.habit_tracking.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu các profile người dùng.
 * Mỗi profile có tên, avatar emoji riêng.
 */
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val avatarEmoji: String = "🙂",
    val createdAt: Long = System.currentTimeMillis()
)
