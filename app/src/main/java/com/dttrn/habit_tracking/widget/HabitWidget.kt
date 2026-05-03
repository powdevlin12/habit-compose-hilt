package com.dttrn.habit_tracking.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.dttrn.habit_tracking.MainActivity
import com.dttrn.habit_tracking.data.db.HabitDatabase
import com.dttrn.habit_tracking.data.db.entity.HabitLogEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val WidgetHabitIdKey = ActionParameters.Key<Int>("habit_id")

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = WidgetDbHelper.getOrCreate(context)
        val habits = db.habitDao().getAllActiveHabits().first().take(4)
        val today = LocalDate.now()
        val todayStr = today.toString()

        val habitsWithLog = habits.map { habit ->
            val isLogged = db.habitLogDao().getLogForHabitAndDate(habit.id, todayStr) != null
            habit to isLogged
        }

        val completedCount = habitsWithLog.count { it.second }
        val totalCount = habitsWithLog.size
        val dateStr = today.format(
            DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi-VN"))
        ).replaceFirstChar { it.uppercase() }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFFF8FDF5)))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📅 $dateStr",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF1B8C4E))
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ColorProvider(Color(0xFFE0EADA)))
                    ) {}

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    habitsWithLog.forEach { (habit, isLogged) ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLogged) "✅" else "⬜",
                                style = TextStyle(fontSize = 14.sp)
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = "${habit.iconEmoji} ${habit.name}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF1A1C19))
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Box(
                                modifier = GlanceModifier
                                    .width(32.dp)
                                    .height(24.dp)
                                    .background(
                                        ColorProvider(
                                            if (isLogged) Color(0xFF1B8C4E) else Color(0xFFE0EADA)
                                        )
                                    )
                                    .clickable(
                                        actionRunCallback<WidgetCheckInAction>(
                                            actionParametersOf(WidgetHabitIdKey to habit.id)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isLogged) "✓" else "+",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(
                                            if (isLogged) Color.White else Color(0xFF4A5240)
                                        )
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ColorProvider(Color(0xFFE0EADA)))
                    ) {}

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedCount/$totalCount hoàn thành",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = ColorProvider(Color(0xFF7C8572))
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Box(
                            modifier = GlanceModifier
                                .width(60.dp)
                                .height(22.dp)
                                .background(ColorProvider(Color(0xFF1B8C4E)))
                                .clickable(
                                    actionStartActivity(
                                        ComponentName(context, MainActivity::class.java)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mở App",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color.White)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class WidgetCheckInAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[WidgetHabitIdKey] ?: return
        val db = WidgetDbHelper.getOrCreate(context)
        val today = LocalDate.now().toString()
        val existing = db.habitLogDao().getLogForHabitAndDate(habitId, today)
        if (existing != null) {
            db.habitLogDao().deleteLog(existing)
        } else {
            db.habitLogDao().insertLog(
                HabitLogEntity(habitId = habitId, loggedDate = today)
            )
        }
        HabitWidgetReceiver.updateAll(context)
    }
}

object WidgetDbHelper {
    @Volatile private var instance: HabitDatabase? = null

    fun getOrCreate(context: Context): HabitDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                HabitDatabase::class.java,
                "habit_journey.db"
            ).build().also { instance = it }
        }
    }
}
