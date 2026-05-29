package com.dttrn.habit_tracking.ui.screen.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val journalDates by viewModel.journalDates.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhật ký", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            CalendarHeader(
                currentMonth = currentMonth,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CalendarGrid(
                currentMonth = currentMonth,
                journalDates = journalDates,
                onDateClick = { date ->
                    onNavigateToDetail(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            )
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYearText = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
        }
        Text(
            text = monthYearText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    journalDates: List<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
    
    // T2 is 1, so offset is firstDayOfWeek - 1
    val offset = firstDayOfWeek - 1
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Ngày trong tuần
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grid ngày
        val totalCells = offset + daysInMonth
        val rows = kotlin.math.ceil(totalCells / 7.0).toInt()
        
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - offset + 1
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            val isToday = date == LocalDate.now()
                            val hasJournal = journalDates.contains(date)
                            
                            DayCell(
                                date = date,
                                isToday = isToday,
                                hasJournal = hasJournal,
                                onClick = { onDateClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    hasJournal: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        if (hasJournal) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Spacer(modifier = Modifier.size(6.dp))
        }
    }
}
