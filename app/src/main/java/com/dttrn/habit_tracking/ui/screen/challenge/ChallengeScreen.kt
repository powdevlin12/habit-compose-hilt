package com.dttrn.habit_tracking.ui.screen.challenge

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialogFor by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Thách thức 30 ngày", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${uiState.challenges.size} thách thức",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.activeHabits.isNotEmpty()) {
                FloatingActionButton(
                    onClick = viewModel::onShowCreateSheet,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tạo thách thức")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (uiState.challenges.isEmpty()) {
            EmptyChallenge(
                hasHabits = uiState.activeHabits.isNotEmpty(),
                onCreateClick = viewModel::onShowCreateSheet,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.challenges, key = { it.challenge.id }) { item ->
                ChallengeCard(
                    item = item,
                    onDelete = { showDeleteDialogFor = item.challenge.id }
                )
            }
        }
    }

    // Create challenge bottom sheet
    if (uiState.showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissCreateSheet,
            sheetState = sheetState
        ) {
            CreateChallengeSheet(
                uiState = uiState,
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onHabitSelected = viewModel::onHabitSelected,
                onDurationChange = viewModel::onDurationChange,
                onConfirm = viewModel::createChallenge,
                onDismiss = viewModel::onDismissCreateSheet
            )
        }
    }

    // Delete confirm dialog
    showDeleteDialogFor?.let { challengeId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialogFor = null },
            title = { Text("Xoá thách thức?") },
            text = { Text("Thách thức sẽ bị xoá vĩnh viễn.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChallenge(challengeId)
                    showDeleteDialogFor = null
                }) { Text("Xoá", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogFor = null }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun EmptyChallenge(
    hasHabits: Boolean,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Chưa có thách thức nào",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (hasHabits) "Tạo thách thức 30 ngày cho thói quen của bạn!"
                else "Thêm thói quen trước, rồi tạo thách thức nhé!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            if (hasHabits) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tạo thách thức")
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    item: ChallengeWithProgress,
    onDelete: () -> Unit
) {
    val habitColor = remember(item.habit?.colorHex) {
        try { Color(android.graphics.Color.parseColor(item.habit?.colorHex ?: "#4CAF50")) }
        catch (e: Exception) { Color(0xFF4CAF50) }
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Habit emoji
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.habit?.iconEmoji ?: "🎯", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.challenge.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.challenge.isCompleted) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Hoàn thành",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        item.habit?.name ?: "Thói quen đã xoá",
                        style = MaterialTheme.typography.labelSmall,
                        color = habitColor
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xoá",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { item.progressPercent },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = if (item.challenge.isCompleted) Color(0xFF4CAF50) else habitColor,
                trackColor = habitColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${item.completedDays}/${item.challenge.durationDays} ngày",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    when {
                        item.challenge.isCompleted -> "🏆 Hoàn thành!"
                        item.daysLeft == 0 -> "Kết thúc hôm nay"
                        else -> "Còn ${item.daysLeft} ngày"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        item.challenge.isCompleted -> Color(0xFF4CAF50)
                        item.daysLeft == 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (item.challenge.isCompleted) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            // Date range
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.challenge.startDate.format(dateFormatter)} → ${item.challenge.endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateChallengeSheet(
    uiState: ChallengeUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onHabitSelected: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedHabit = uiState.activeHabits.firstOrNull { it.id == uiState.selectedHabitId }
    val durationOptions = listOf(7, 14, 21, 30, 60, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Tạo thách thức mới",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.challengeTitle,
            onValueChange = onTitleChange,
            label = { Text("Tên thách thức *") },
            isError = uiState.titleError != null,
            supportingText = uiState.titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = uiState.challengeDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Mô tả (tuỳ chọn)") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Habit dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedHabit?.let { "${it.iconEmoji} ${it.name}" } ?: "Chọn thói quen",
                onValueChange = {},
                readOnly = true,
                label = { Text("Thói quen") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                uiState.activeHabits.forEach { habit ->
                    DropdownMenuItem(
                        text = { Text("${habit.iconEmoji} ${habit.name}") },
                        onClick = {
                            onHabitSelected(habit.id)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Duration picker
        Column {
            Text("Thời gian", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durationOptions.forEach { days ->
                    FilterChip(
                        selected = uiState.durationDays == days,
                        onClick = { onDurationChange(days) },
                        label = { Text("${days}d") }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Huỷ") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Tạo thách thức") }
        }

        Spacer(Modifier.height(8.dp))
    }
}
