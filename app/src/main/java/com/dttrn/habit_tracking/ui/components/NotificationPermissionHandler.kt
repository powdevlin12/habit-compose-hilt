package com.dttrn.habit_tracking.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun NotificationPermissionHandler(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted() else onDenied()
    }

    LaunchedEffect(Unit) {
        when {
            // Android 12 trở xuống không cần xin
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                onGranted()
            }

            // Đã có permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }

            // Chưa có → xin
            else -> {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}