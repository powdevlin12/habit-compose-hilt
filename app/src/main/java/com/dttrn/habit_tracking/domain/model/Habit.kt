package com.dttrn.habit_tracking.domain.model

data class Habit(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val iconEmoji: String = "✅",
    val colorHex: String = "#4CAF50",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDays: List<Int> = emptyList(),
    val reminderTime: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val profileId: Int = 1
)

enum class HabitFrequency(val label: String) {
    DAILY("Hằng ngày"),
    WEEKLY("Hằng tuần"),
    CUSTOM("Tùy chỉnh")
}
