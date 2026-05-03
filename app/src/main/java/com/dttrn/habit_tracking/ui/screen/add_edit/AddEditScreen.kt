package com.dttrn.habit_tracking.ui.screen.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dttrn.habit_tracking.domain.model.HabitFrequency

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    habitId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(habitId) {
        if (habitId != null) viewModel.loadHabit(habitId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    val isEditing = habitId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Sửa thói quen" else "Thêm thói quen mới",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icon + Name
            Row(verticalAlignment = Alignment.Top) {
                EmojiPickerButton(
                    selectedEmoji = uiState.iconEmoji,
                    onEmojiSelected = viewModel::onIconChange
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Tên thói quen *") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Mô tả (tuỳ chọn)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            SectionLabel("Màu sắc")
            ColorPicker(
                selectedColorHex = uiState.colorHex,
                onColorSelected = viewModel::onColorChange
            )

            SectionLabel("Tần suất")
            FrequencyPicker(
                selected = uiState.frequency,
                onSelected = viewModel::onFrequencyChange
            )

            if (uiState.frequency == HabitFrequency.CUSTOM) {
                SectionLabel("Ngày trong tuần")
                WeekDayPicker(
                    selectedDays = uiState.targetDays,
                    onDayToggle = viewModel::onTargetDayToggle
                )
            }

            SectionLabel("Nhắc nhở")
            ReminderSection(
                enabled = uiState.reminderEnabled,
                time = uiState.reminderTime,
                onToggle = viewModel::onReminderToggle,
                onTimeChange = viewModel::onReminderTimeChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::saveHabit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = if (isEditing) "Cập nhật" else "Tạo thói quen",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiPickerButton(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val commonEmojis = listOf(
        "✅", "🏋️", "📖", "💧", "🧘", "🚴", "🎯", "⭐",
        "🌱", "💊", "🎵", "🧹", "✍️", "🍎", "😴", "🧠"
    )
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showPicker = true },
        contentAlignment = Alignment.Center
    ) {
        Text(text = selectedEmoji, fontSize = 28.sp)
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Chọn biểu tượng") },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == selectedEmoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    onEmojiSelected(emoji)
                                    showPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    val colorHexList = listOf(
        "#4CAF50", "#2196F3", "#FF9800", "#9C27B0",
        "#F44336", "#009688", "#E91E63", "#3F51B5"
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        colorHexList.forEach { hex ->
            val color = try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) { Color.Gray }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (hex == selectedColorHex)
                            Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(hex) }
            )
        }
    }
}

@Composable
private fun FrequencyPicker(
    selected: HabitFrequency,
    onSelected: (HabitFrequency) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HabitFrequency.entries.forEach { freq ->
            FilterChip(
                selected = selected == freq,
                onClick = { onSelected(freq) },
                label = { Text(freq.label) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekDayPicker(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit
) {
    // ISO: 1=Sun, 2=Mon..., 7=Sat  (hoặc custom mapping)
    val days = listOf(
        "T2" to 2, "T3" to 3, "T4" to 4, "T5" to 5,
        "T6" to 6, "T7" to 7, "CN" to 1
    )

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { (label, dayNum) ->
            FilterChip(
                selected = selectedDays.contains(dayNum),
                onClick = { onDayToggle(dayNum) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun ReminderSection(
    enabled: Boolean,
    time: String,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bật nhắc nhở", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }

        if (enabled) {
            OutlinedTextField(
                value = time,
                onValueChange = onTimeChange,
                label = { Text("Giờ nhắc nhở (HH:mm)") },
                placeholder = { Text("08:00") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.5f),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
