package com.dttrn.habit_tracking.domain.model

import java.time.LocalDate

data class Journal(
    val date: LocalDate,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
