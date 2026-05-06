## Xử lý Notification trên Android

---

## Bước 1: Thêm Permission

**`AndroidManifest.xml`**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

<application ...>
    <!-- Khai báo FCM Service -->
    <service
        android:name=".notification.MyFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT"/>
        </intent-filter>
    </service>
</application>
```

---

## Bước 2: Setup Dependencies

**`build.gradle (app)`**
```kotlin
dependencies {
    // Firebase Cloud Messaging
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Coroutines (nếu chưa có)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}
```

---

## Bước 3: Tạo Notification Channel (Android 8.0+)

```kotlin
object NotificationHelper {

    const val CHANNEL_ID_MESSAGE = "channel_message"
    const val CHANNEL_ID_PROMO   = "channel_promo"
    const val CHANNEL_ID_SYSTEM  = "channel_system"

    fun createNotificationChannels(context: Context) {
        // Chỉ cần tạo 1 lần khi app khởi động
        val channels = listOf(
            NotificationChannel(
                CHANNEL_ID_MESSAGE,
                "Tin nhắn",
                NotificationManager.IMPORTANCE_HIGH  // hiện popup + âm thanh
            ).apply {
                description = "Thông báo tin nhắn mới"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            },

            NotificationChannel(
                CHANNEL_ID_PROMO,
                "Khuyến mãi",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo khuyến mãi"
            },

            NotificationChannel(
                CHANNEL_ID_SYSTEM,
                "Hệ thống",
                NotificationManager.IMPORTANCE_LOW  // im lặng
            ).apply {
                description = "Thông báo hệ thống"
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
        NotificationHelper.createNotificationChannels(this)  // ← tạo channel
    }
}
```

---

## Bước 4: Xin Permission Runtime (Android 13+)

```kotlin
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    
    // Launcher xin permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Permission", "Đã cho phép notification")
        } else {
            Log.d("Permission", "Từ chối notification")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // Đã có permission rồi
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> { /* OK */ }

                // Chưa có → xin
                else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
```

---

## Bước 5: Tạo và hiển thị Notification

```kotlin
object NotificationManager {

    fun showMessageNotification(
        context: Context,
        title: String,
        message: String,
        data: Map<String, String> = emptyMap()
    ) {
        // Intent khi bấm vào notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_MESSAGE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // text dài
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)        // tự xóa khi bấm vào
            .setContentIntent(pendingIntent)
            .build()

        // Hiển thị
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    // Notification có action button
    fun showNotificationWithActions(context: Context, title: String, message: String) {
        val acceptIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = "ACTION_ACCEPT"
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_MESSAGE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .addAction(R.drawable.ic_check, "Chấp nhận", acceptIntent)  // ← action button
            .addAction(R.drawable.ic_close, "Từ chối", null)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(1001, notification)
    }
}
```

---

## Bước 6: FCM Service - Nhận notification từ server

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // App đang mở (foreground) → tự xử lý hiển thị
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Thông báo"
        val body  = remoteMessage.notification?.body  ?: ""
        val data  = remoteMessage.data  // data từ server

        // Phân loại theo type
        when (data["type"]) {
            "message" -> {
                NotificationManager.showMessageNotification(
                    context = this,
                    title = title,
                    message = body,
                    data = data
                )
            }
            "promo" -> { /* xử lý khác */ }
            else -> {
                NotificationManager.showMessageNotification(this, title, body)
            }
        }
    }

    // Token mới → gửi lên server để server lưu lại
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token mới: $token")
        
        // Gửi token lên server của bạn
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        // Dùng coroutine hoặc WorkManager để gửi
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // apiService.updateFcmToken(token)
            } catch (e: Exception) {
                Log.e("FCM", "Gửi token thất bại: ${e.message}")
            }
        }
    }
}
```

---

## Bước 7: Xử lý khi bấm vào Notification

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Xử lý intent khi bấm notification
        handleNotificationIntent(intent)

        setContent { MyApp() }
    }

    // Khi app đang chạy và nhận notification mới
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            val type    = it.getStringExtra("type")
            val postId  = it.getStringExtra("post_id")
            val userId  = it.getStringExtra("user_id")

            when (type) {
                "message" -> {
                    // Navigate đến màn hình chat
                }
                "post" -> {
                    // Navigate đến bài post
                }
            }
        }
    }
}
```

---

## Bước 8: Lấy FCM Token

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                viewModelScope.launch {
                    repository.updateFcmToken(token)
                }
            }
            .addOnFailureListener {
                Log.e("FCM", "Lấy token thất bại")
            }
    }
}
```

---

## Toàn bộ Flow

```
Server gửi FCM
    └── App background → Android tự hiển thị notification
    └── App foreground → onMessageReceived() → tự build & show
            └── User bấm vào notification
                    └── MainActivity.handleNotificationIntent()
                            └── Navigate đến màn hình tương ứng
```

---

## Checklist

```
✅ Thêm POST_NOTIFICATIONS permission trong Manifest
✅ Khai báo FirebaseMessagingService trong Manifest  
✅ Tạo Notification Channel trong Application.onCreate()
✅ Xin runtime permission (Android 13+)
✅ Implement onMessageReceived() cho foreground
✅ Implement onNewToken() để gửi token lên server
✅ Xử lý intent trong MainActivity khi bấm notification
```

Bạn đang dùng notification cho mục đích gì (chat, social, e-commerce...)? Mình có thể hướng dẫn chi tiết hơn cho use case của bạn!