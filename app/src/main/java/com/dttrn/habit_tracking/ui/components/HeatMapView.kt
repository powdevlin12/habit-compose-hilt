package com.dttrn.habit_tracking.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun HabitHeatMap(
    logs: List<LocalDate>,
    habitColor: Color,
    modifier: Modifier = Modifier,
    startDate: LocalDate = LocalDate.now().minusYears(1),
    endDate: LocalDate = LocalDate.now(),
    onDayClick: (LocalDate) -> Unit = {}
) {
    val logSet = remember(logs) { logs.toHashSet() }
    val today = LocalDate.now()
    val density = LocalDensity.current

    val cellSizeDp = 10.dp
    val spacingDp = 2.dp
    val dayLabelWidthDp = 22.dp
    val monthLabelHeightDp = 18.dp

    val cellSizePx = with(density) { cellSizeDp.toPx() }
    val spacingPx = with(density) { spacingDp.toPx() }
    val stepPx = cellSizePx + spacingPx

    // Build weeks matrix
    val weeks = remember(startDate, endDate) {
        val adjustedStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val result = mutableListOf<List<LocalDate?>>()
        var current = adjustedStart
        while (!current.isAfter(endDate)) {
            result.add((0..6).map { offset ->
                val d = current.plusDays(offset.toLong())
                if (!d.isBefore(startDate) && !d.isAfter(endDate)) d else null
            })
            current = current.plusWeeks(1)
        }
        result
    }

    val weekCount = weeks.size
    val totalWidthDp = dayLabelWidthDp + weekCount * (cellSizeDp + spacingDp)
    val totalHeightDp = monthLabelHeightDp + 7 * (cellSizeDp + spacingDp)

    val emptyColor = Color.LightGray.copy(alpha = 0.3f)
    val filledColor = habitColor.copy(alpha = 0.65f)
    val filledWithNoteColor = habitColor

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val dayLabelWidthPx = with(density) { dayLabelWidthDp.toPx() }
    val monthLabelHeightPx = with(density) { monthLabelHeightDp.toPx() }

    val scrollState = rememberScrollState(Int.MAX_VALUE)

    Column(modifier = modifier) {
        // Day-of-week labels + heatmap canvas
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            // Day labels column
            Column(modifier = Modifier.width(dayLabelWidthDp)) {
                Spacer(modifier = Modifier.height(monthLabelHeightDp))
                listOf("", "T2", "", "T4", "", "T6", "").forEach { label ->
                    Box(
                        modifier = Modifier
                            .height(cellSizeDp + spacingDp)
                            .width(dayLabelWidthDp)
                    ) {
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Canvas for the heatmap
            Canvas(
                modifier = Modifier
                    .width(totalWidthDp - dayLabelWidthDp)
                    .height(totalHeightDp)
                    .pointerInput(weeks, logSet) {
                        detectTapGestures { tapOffset ->
                            val col = ((tapOffset.x) / stepPx).toInt()
                            val row = ((tapOffset.y - monthLabelHeightPx) / stepPx).toInt()
                            if (col in weeks.indices && row in 0..6) {
                                val date = weeks[col][row]
                                if (date != null && !date.isAfter(today)) {
                                    selectedDate = if (selectedDate == date) null else date
                                    onDayClick(date)
                                }
                            }
                        }
                    }
            ) {
                var prevMonth = -1

                weeks.forEachIndexed { colIndex, week ->
                    val x = colIndex * stepPx

                    // Month label at start of new month
                    val firstNonNull = week.firstOrNull { it != null }
                    if (firstNonNull != null && firstNonNull.monthValue != prevMonth) {
                        prevMonth = firstNonNull.monthValue
                        // Month label drawn via Text composable below
                    }

                    week.forEachIndexed { rowIndex, date ->
                        val y = monthLabelHeightPx + rowIndex * stepPx

                        when {
                            date == null -> { /* empty cell */ }
                            date.isAfter(today) -> { /* future, skip */ }
                            date == today && !logSet.contains(date) -> {
                                // Today not yet logged — dashed border
                                drawRoundRect(
                                    color = habitColor.copy(alpha = 0.5f),
                                    topLeft = Offset(x, y),
                                    size = Size(cellSizePx, cellSizePx),
                                    cornerRadius = CornerRadius(2f, 2f),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                            logSet.contains(date) -> {
                                drawRoundRect(
                                    color = filledColor,
                                    topLeft = Offset(x, y),
                                    size = Size(cellSizePx, cellSizePx),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                            else -> {
                                drawRoundRect(
                                    color = emptyColor,
                                    topLeft = Offset(x, y),
                                    size = Size(cellSizePx, cellSizePx),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }

                        // Highlight selected
                        if (date == selectedDate) {
                            drawRoundRect(
                                color = habitColor,
                                topLeft = Offset(x, y),
                                size = Size(cellSizePx, cellSizePx),
                                cornerRadius = CornerRadius(2f, 2f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }
        }

        // Month labels row (rendered after canvas for proper layout)
        Row(
            modifier = Modifier
                .padding(start = dayLabelWidthDp)
                .horizontalScroll(rememberScrollState())
        ) {
            var prevMonth = -1
            weeks.forEach { week ->
                val firstDay = week.firstOrNull { it != null }
                val label = if (firstDay != null && firstDay.monthValue != prevMonth) {
                    prevMonth = firstDay.monthValue
                    firstDay.month.getDisplayName(TextStyle.SHORT, Locale("vi"))
                } else ""

                Box(modifier = Modifier.width(cellSizeDp + spacingDp)) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Legend
        HeatMapLegend(
            habitColor = habitColor,
            modifier = Modifier
                .padding(top = 4.dp, start = dayLabelWidthDp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun HeatMapLegend(
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(
            text = "Ít hơn",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        listOf(0.15f, 0.3f, 0.5f, 0.7f, 1f).forEach { alpha ->
            Canvas(modifier = Modifier.width(10.dp).height(10.dp).padding(1.dp)) {
                drawRoundRect(
                    color = if (alpha < 0.2f) Color.LightGray.copy(alpha = 0.3f) else habitColor.copy(alpha = alpha),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Nhiều hơn",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MiniHeatMap(
    logs: List<LocalDate>,
    habitColor: Color,
    modifier: Modifier = Modifier,
    days: Int = 7
) {
    val today = LocalDate.now()
    val recentDays = (days - 1 downTo 0).map { i -> today.minusDays(i.toLong()) }
    val logSet = logs.toHashSet()
    val cellSize = 8.dp
    val spacing = 2.dp

    Row(modifier = modifier) {
        recentDays.forEach { date ->
            val isLogged = logSet.contains(date)
            Canvas(modifier = Modifier.width(cellSize).height(cellSize)) {
                drawRoundRect(
                    color = if (isLogged) habitColor.copy(alpha = 0.7f) else Color.LightGray.copy(alpha = 0.3f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
            Spacer(modifier = Modifier.width(spacing))
        }
    }
}
