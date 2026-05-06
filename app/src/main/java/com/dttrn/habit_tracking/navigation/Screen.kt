package com.dttrn.habit_tracking.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddHabit : Screen("add_habit")
    data object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit/$habitId"
    }
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Int) = "habit_detail/$habitId"
    }
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")

    data object Reminder : Screen("reminder_screen")
}
