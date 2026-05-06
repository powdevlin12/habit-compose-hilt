## Local Notification trong Android

---

## Bước 1: Thêm Permission vào Manifest

```xml
<manifest ...>
    <!-- Bắt buộc cho Android 13+ -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    
    <!-- Nếu dùng exact alarm (nhắc nhở đúng giờ) -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>
    
    <application ...>
        <!-- Khai báo BroadcastReceiver để nhận alarm -->
        <receiver
            android:name=".notification.AlarmReceiver"
            android:exported="false"/>
    </application>
</manifest>
```

---

## Bước 2: Tạo Notification Helper

```kotlin
object NotificationHelper {

    // Định nghĩa các channel
    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_ALERT    = "channel_alert"
    const val CHANNEL_SILENT   = "channel_silent"

    fun createChannels(context: Context) {
        val channels = listOf(
            // Channel quan trọng - có âm thanh + popup
            NotificationChannel(
                CHANNEL_REMINDER,
                "Nhắc nhở",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            },

            // Channel bình thường - có âm thanh
            NotificationChannel(
                CHANNEL_ALERT,
                "Cảnh báo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo cảnh báo"
            },

            // Channel im lặng - không âm thanh
            NotificationChannel(
                CHANNEL_SILENT,
                "Im lặng",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo im lặng"
            }
        )

        val manager = context.getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
```

**Gọi trong Application class:**
```kotlin
@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this) // ← tạo channel 1 lần duy nhất
    }
}
```

---

## Bước 3: Xin Permission Runtime

```kotlin
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
```

---

## Bước 4: Tạo NotificationManager

```kotlin
class LocalNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    // ① Notification đơn giản
    fun showSimpleNotification(
        id: Int,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // tự xóa khi bấm vào
            .build()

        notificationManager.notify(id, notification)
    }

    // ② Notification với text dài
    fun showBigTextNotification(
        id: Int,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)       // nội dung dài khi mở rộng
                    .setBigContentTitle(title)
                    .setSummaryText("App của bạn")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    // ③ Notification có nút action
    fun showActionNotification(
        id: Int,
        title: String,
        message: String
    ) {
        // Intent khi bấm "Xem ngay"
        val viewIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_id", id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent khi bấm "Bỏ qua"
        val dismissIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = "ACTION_DISMISS"
                putExtra("notification_id", id)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .addAction(R.drawable.ic_check, "Xem ngay", viewIntent)
            .addAction(R.drawable.ic_close, "Bỏ qua", dismissIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    // ④ Notification có progress bar
    fun showProgressNotification(id: Int, title: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("$progress%")
            .setProgress(100, progress, false) // (max, current, indeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // không thể vuốt xóa khi đang chạy
            .build()

        notificationManager.notify(id, notification)
    }

    // Xóa notification
    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    // Xóa tất cả
    fun cancelAll() {
        notificationManager.cancelAll()
    }
}
```

---

## Bước 5: Tạo BroadcastReceiver xử lý Action

```kotlin
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", -1)

        when (intent.action) {
            "ACTION_DISMISS" -> {
                // Xóa notification
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
            "ACTION_ACCEPT" -> {
                // Xử lý logic
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }
    }
}
```

---

## Bước 6: Đặt lịch Notification (AlarmManager)

```kotlin
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    // Đặt lịch thông báo 1 lần
    fun scheduleOnce(
        notificationId: Int,
        title: String,
        message: String,
        triggerAtMillis: Long  // thời điểm muốn nhận notification
    ) {
        val intent = PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra("notification_id", notificationId)
                putExtra("title", title)
                putExtra("message", message)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            intent
        )
    }

    // Hủy lịch
    fun cancelScheduled(notificationId: Int) {
        val intent = PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(intent)
    }
}
```

**AlarmReceiver - Nhận alarm và hiển thị notification:**
```kotlin
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id      = intent.getIntExtra("notification_id", 0)
        val title   = intent.getStringExtra("title") ?: "Nhắc nhở"
        val message = intent.getStringExtra("message") ?: ""

        // Hiển thị notification
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
```

---

## Bước 7: Inject và dùng trong ViewModel

```kotlin
// Khai báo trong Hilt Module
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideLocalNotificationManager(
        @ApplicationContext context: Context
    ): LocalNotificationManager = LocalNotificationManager(context)

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler = NotificationScheduler(context)
}
```

```kotlin
@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val notificationManager: LocalNotificationManager,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    // Hiện notification ngay lập tức
    fun showNow() {
        notificationManager.showSimpleNotification(
            id = 1001,
            title = "Nhắc nhở",
            message = "Đã đến giờ uống nước!"
        )
    }

    // Đặt lịch sau 1 tiếng
    fun scheduleAfter1Hour() {
        val triggerTime = System.currentTimeMillis() + (60 * 60 * 1000)
        scheduler.scheduleOnce(
            notificationId = 2001,
            title = "Nhắc nhở",
            message = "Đã 1 tiếng rồi, nghỉ ngơi đi!",
            triggerAtMillis = triggerTime
        )
    }
}
```

---

## Bước 8: Dùng trong Composable

```kotlin
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    // Xin permission khi vào màn hình
    NotificationPermissionHandler(
        onGranted = { /* có thể show notification */ },
        onDenied  = { /* hiện dialog giải thích */ }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { viewModel.showNow() }) {
            Text("Thông báo ngay")
        }

        Button(onClick = { viewModel.scheduleAfter1Hour() }) {
            Text("Nhắc sau 1 tiếng")
        }
    }
}
```

---

## Toàn bộ Flow

```
User bấm nút
    └── ViewModel gọi NotificationManager / Scheduler
            └── Show ngay → NotificationManagerCompat.notify()
            └── Đặt lịch  → AlarmManager.setExactAndAllowWhileIdle()
                                └── Đúng giờ → AlarmReceiver.onReceive()
                                        └── Hiển thị notification
                                                └── User bấm vào → MainActivity
```

---

## Checklist

```
✅ POST_NOTIFICATIONS trong Manifest
✅ SCHEDULE_EXACT_ALARM nếu dùng alarm
✅ Khai báo AlarmReceiver trong Manifest
✅ Tạo Notification Channel trong Application.onCreate()
✅ Xin runtime permission (Android 13+)
✅ Inject LocalNotificationManager qua Hilt
✅ Dùng AlarmManager cho thông báo có lịch
```

Bạn muốn dùng notification cho mục đích gì (nhắc nhở, đếm ngược, hàng ngày...)? Mình có thể tùy chỉnh chi tiết hơn!