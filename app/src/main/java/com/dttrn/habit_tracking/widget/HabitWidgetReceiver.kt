package com.dttrn.habit_tracking.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()

    companion object {
        fun updateAll(context: Context) {
            MainScope().launch {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(HabitWidget::class.java)
                glanceIds.forEach { glanceId ->
                    HabitWidget().update(context, glanceId)
                }
            }
        }
    }
}
