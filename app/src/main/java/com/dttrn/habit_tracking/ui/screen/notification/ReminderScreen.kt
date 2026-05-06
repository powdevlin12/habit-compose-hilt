package com.dttrn.habit_tracking.ui.screen.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dttrn.habit_tracking.ui.components.NotificationPermissionHandler

@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Re-check mỗi khi user quay lại từ Settings
    var canExact by remember { mutableStateOf(viewModel.canScheduleExact()) }
    LifecycleResumeEffect(Unit) {
        canExact = viewModel.canScheduleExact()
        onPauseOrDispose {}
    }

    // Xin permission POST_NOTIFICATIONS khi vào màn hình
    NotificationPermissionHandler(
        onGranted = { },
        onDenied  = { }
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Banner cảnh báo nếu chưa có quyền exact alarm
            if (!canExact) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cần quyền đặt lịch chính xác",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Vào Cài đặt → Ứng dụng → Báo thức & nhắc nhở để bật quyền.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(onClick = { viewModel.openExactAlarmSettings(context) }) {
                            Text("Cấp quyền")
                        }
                    }
                }
            }

            Button(
                onClick = @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
                    viewModel.showNow()
                }
            ) {
                Text("Thông báo ngay")
            }

            Button(onClick = { viewModel.scheduleAfter1Hour() }) {
                Text("Nhắc sau 1 tiếng")
            }
        }
    }
}