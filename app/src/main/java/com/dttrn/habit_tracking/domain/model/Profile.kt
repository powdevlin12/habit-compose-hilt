package com.dttrn.habit_tracking.domain.model

data class Profile(
    val id: Int = 0,
    val name: String,
    val avatarEmoji: String = "🙂",
    val createdAt: Long = System.currentTimeMillis()
)
