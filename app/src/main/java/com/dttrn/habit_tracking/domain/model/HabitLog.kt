package com.dttrn.habit_tracking.domain.model

data class HabitLog(
    val id: Int = 0,
    val habitId: Int,
    val loggedDate: String,
    val note: String? = null
)
