package com.dttrn.habit_tracking.ui.screen.home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dttrn.habit_tracking.ui.components.HabitCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit,
    onNavigateToHabitDetail: (Int) -> Unit,
    onNavigateToEditHabit: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChallenge: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNav by rememberSaveable { mutableStateOf(0) }
    var habitToDelete by remember { mutableStateOf<Int?>(null) }

    val today = uiState.today
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale("vi"))
    val dateString = today.format(formatter)

    val motivationalMessages = listOf(
        "Hôm nay là cơ hội tuyệt vời! 💪",
        "Mỗi ngày một bước tiến nhỏ! 🚀",
        "Kiên trì tạo nên thói quen! ⭐",
        "Bạn đang làm rất tốt! 🌟",
        "Tiếp tục phát huy nhé! 🔥"
    )
    val motivationalMsg = remember(today) {
        motivationalMessages[today.dayOfYear % motivationalMessages.size]
    }

    // handle refresh when back home
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Sự kiện xảy ra khi màn hình này được hiển thị lại (bao gồm cả khi back về)
                Log.d("Dat Test","User back to this screen!")
                viewModel.observeHabits()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = dateString.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = motivationalMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddHabit,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm thói quen",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") },
                    selected = selectedNav == 0,
                    onClick = { selectedNav = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Book, contentDescription = "Nhật ký") },
                    label = { Text("Nhật ký") },
                    selected = selectedNav == 1,
                    onClick = {
                        selectedNav = 1
                        onNavigateToJournal()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Cài đặt") },
                    label = { Text("Cài đặt") },
                    selected = selectedNav == 2,
                    onClick = {
                        selectedNav = 2
                        onNavigateToSettings()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Thách thức") },
                    label = { Text("Thách thức") },
                    selected = selectedNav == 3,
                    onClick = {
                        selectedNav = 3
                        onNavigateToChallenge()
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress bar
            if (uiState.totalCount > 0) {
                ProgressHeader(
                    completed = uiState.completedCount,
                    total = uiState.totalCount,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Đang tải...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                uiState.habitsWithStatus.isEmpty() -> {
                    EmptyHabitsView(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.habitsWithStatus,
                            key = { _, h -> h.habit.id }
                        ) { index, habitWithStatus ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                            ) {
                                HabitCard(
                                    name = habitWithStatus.habit.name,
                                    iconEmoji = habitWithStatus.habit.iconEmoji,
                                    colorHex = habitWithStatus.habit.colorHex,
                                    streak = habitWithStatus.currentStreak,
                                    isLoggedToday = habitWithStatus.isLoggedToday,
                                    recentDays = emptyList(),
                                    loggedDates = habitWithStatus.loggedDates,
                                    onClick = { onNavigateToHabitDetail(habitWithStatus.habit.id) },
                                    onCheckIn = { viewModel.toggleCheckIn(habitWithStatus.habit.id) },
                                    onEdit = { onNavigateToEditHabit(habitWithStatus.habit.id) },
                                    onDelete = { habitToDelete = habitWithStatus.habit.id },
                                    onArchive = { viewModel.archiveHabit(habitWithStatus.habit.id) }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    habitToDelete?.let { habitId ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Xoá thói quen") },
            text = { Text("Bạn có chắc muốn xoá thói quen này? Tất cả dữ liệu check-in sẽ bị mất.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(habitId)
                        habitToDelete = null
                    }
                ) {
                    Text("Xoá", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Huỷ")
                }
            }
        )
    }
}

@Composable
private fun ProgressHeader(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hôm nay",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$completed/$total hoàn thành",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) completed.toFloat() / total else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun EmptyHabitsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🌱", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có thói quen nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nhấn + để thêm thói quen đầu tiên\nvà bắt đầu hành trình của bạn!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
