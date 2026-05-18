package com.dttrn.habit_tracking.domain.model

import java.time.LocalDate

data class Challenge(
    val id: Int = 0,
    val habitId: Int,
    val title: String,
    val description: String? = null,
    val durationDays: Int = 30,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val profileId: Int = 1
)
