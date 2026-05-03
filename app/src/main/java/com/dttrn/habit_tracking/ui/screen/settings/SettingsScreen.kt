package com.dttrn.habit_tracking.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
    LIGHT("Sáng"),
    DARK("Tối"),
    SYSTEM("Theo hệ thống")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTheme by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.SemiBold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSectionHeader("Giao diện")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    iconTint = Color(0xFF5C6BC0),
                    title = "Chủ đề",
                    subtitle = selectedTheme.label,
                    onClick = { showThemeDialog = true }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Language,
                    iconTint = Color(0xFF26A69A),
                    title = "Ngôn ngữ",
                    subtitle = "Tiếng Việt",
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("Thông báo")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    iconTint = Color(0xFFFF7043),
                    title = "Giờ nhắc nhở mặc định",
                    subtitle = "08:00",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("Dữ liệu")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    iconTint = Color(0xFF42A5F5),
                    title = "Xuất dữ liệu CSV",
                    subtitle = "Xuất tất cả check-in ra file CSV",
                    onClick = {}
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = "Xoá toàn bộ dữ liệu",
                    subtitle = "Xoá tất cả thói quen và lịch sử",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteDataDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("Ứng dụng")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFF78909C),
                    title = "Phiên bản",
                    subtitle = "1.0.0 (MVP)",
                    showChevron = false,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Chọn chủ đề") },
            text = {
                Column {
                    ThemeMode.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = theme
                                    showThemeDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = {
                                    selectedTheme = theme
                                    showThemeDialog = false
                                }
                            )
                            Text(theme.label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Language dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Ngôn ngữ") },
            text = { Text("Tính năng đa ngôn ngữ sẽ có trong phiên bản v1.1.") },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("OK") }
            }
        )
    }

    // Delete data dialog
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text("Xoá toàn bộ dữ liệu") },
            text = {
                Text("Hành động này sẽ xoá TOÀN BỘ thói quen và lịch sử check-in. Dữ liệu không thể khôi phục.")
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text("Xoá tất cả", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
