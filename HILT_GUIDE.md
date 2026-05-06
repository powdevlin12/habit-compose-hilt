# 💉 Hướng dẫn Hilt Dependency Injection

> Tài liệu này được viết riêng cho project **Habit Tracking**, dùng để giải thích Hilt từ đầu và áp dụng thực tế vào codebase hiện có.

---

## 1. Dependency Injection là gì? (Hiểu nôm na)

Hãy tưởng tượng bạn muốn pha cà phê. Bạn cần:
- Máy pha cà phê
- Cốc
- Cà phê

### ❌ Không dùng DI (tự tạo mọi thứ bên trong)

```kotlin
class HomeViewModel {
    // Tự khởi tạo → cứng nhắc, khó test, lặp code
    private val db = Room.databaseBuilder(...).build()
    private val habitDao = db.habitDao()
    private val habitLogDao = db.habitLogDao()
    private val repository = HabitRepository(habitDao, habitLogDao)
}
```

**Vấn đề**: Mỗi ViewModel lại tạo một database riêng → tốn RAM, dữ liệu không đồng bộ.

### ✅ Dùng DI (Hilt cung cấp sẵn)

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository  // Hilt tự đưa vào!
) : ViewModel()
```

**Hilt** đóng vai trò như "người phục vụ" — tự biết tạo ra `HabitRepository` như thế nào và **đưa nó vào** ViewModel cho bạn.

---

## 2. Cách Hilt hoạt động trong project này

```
@HiltAndroidApp          ← Bật Hilt cho toàn app
HabitTrackingApp
       │
       ▼
@Module AppModule        ← Dạy Hilt cách tạo Database, DAO
       │
       ├─ provides HabitDatabase
       ├─ provides HabitDao
       └─ provides HabitLogDao
                │
                ▼
@Singleton HabitRepository  ← @Inject constructor nhận DAO từ Hilt
                │
                ▼
@HiltViewModel HomeViewModel  ← @Inject constructor nhận Repository từ Hilt
                │
                ▼
hiltViewModel()  ← Compose lấy ViewModel từ Hilt
HomeScreen
```

---

## 3. Các Annotation quan trọng (giải thích từng cái)

### 3.1 `@HiltAndroidApp` — Bắt buộc, đặt ở Application class

```kotlin
// HabitTrackingApp.kt
@HiltAndroidApp                          // ← Kích hoạt Hilt cho toàn bộ app
class HabitTrackingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
```

> **Tác dụng**: Tạo "kho chứa" (container) để Hilt quản lý tất cả dependencies. Không có annotation này, Hilt không chạy được.

---

### 3.2 `@AndroidEntryPoint` — Dùng cho Activity, Fragment

```kotlin
// MainActivity.kt
@AndroidEntryPoint                       // ← Cho phép Hilt inject vào Activity này
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HabitApp() }
    }
}
```

> **Tác dụng**: Hilt sẽ tự inject các dependency được khai báo trong Activity/Fragment.
> Trong project này `MainActivity` không inject gì trực tiếp, nhưng **bắt buộc phải có** để `hiltViewModel()` trong Compose hoạt động.

---

### 3.3 `@Inject constructor` — Khai báo "tôi cần được inject"

```kotlin
// HabitRepository.kt
@Singleton                               // ← Chỉ tạo 1 instance duy nhất trong toàn app
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,      // ← Hilt tự đưa HabitDao vào đây
    private val habitLogDao: HabitLogDao // ← Hilt tự đưa HabitLogDao vào đây
) {
    // business logic...
}
```

> **Tác dụng**: Hilt biết rằng khi ai đó cần `HabitRepository`, nó cần tạo một instance với `HabitDao` và `HabitLogDao`.
>
> **`@Singleton`**: Đảm bảo chỉ có **1 instance** `HabitRepository` tồn tại suốt vòng đời app — tất cả ViewModels dùng chung 1 repository.

---

### 3.4 `@HiltViewModel` — Dùng cho ViewModel

```kotlin
// HomeViewModel.kt
@HiltViewModel                           // ← Cho Hilt biết đây là ViewModel cần inject
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository  // ← Hilt tự đưa repository vào
) : ViewModel() {
    // ...
}
```

> **Tác dụng**: Hilt tạo ViewModel thông qua `hiltViewModel()` trong Compose, đồng thời tự xử lý lifecycle (không bị recreate khi xoay màn hình).

---

### 3.5 `@Module` + `@InstallIn` — Dạy Hilt cách tạo những thứ phức tạp

Một số class **không thể** dùng `@Inject constructor` được (ví dụ: Room Database, interface). Ta dùng **Module** để "dạy" Hilt cách tạo chúng.

```kotlin
// di/AppModule.kt
@Module                                  // ← Đây là module chứa các "công thức" tạo dependencies
@InstallIn(SingletonComponent::class)    // ← Module này sống suốt vòng đời app
object AppModule {

    @Provides                            // ← "Công thức" tạo HabitDatabase
    @Singleton                           // ← Chỉ tạo 1 lần duy nhất
    fun provideHabitDatabase(
        @ApplicationContext context: Context  // ← Hilt tự cung cấp Application Context
    ): HabitDatabase =
        Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_journey.db"
        ).build()

    @Provides                            // ← "Công thức" tạo HabitDao
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()
    //                  ↑ Hilt biết cách lấy HabitDatabase từ hàm trên!

    @Provides
    fun provideHabitLogDao(db: HabitDatabase): HabitLogDao = db.habitLogDao()
}
```

> **Tại sao cần Module?**
> - `HabitDatabase` cần `Context` và chuỗi tên file — Hilt không thể tự đoán được
> - `HabitDao` được tạo từ `HabitDatabase` — không có constructor public
>
> **`@InstallIn(SingletonComponent::class)`**: Module này gắn với scope App (singleton). Các scope khác:
> | Scope | InstallIn | Lifetime |
> |---|---|---|
> | App-wide | `SingletonComponent` | Suốt vòng đời app |
> | Activity | `ActivityComponent` | Theo Activity |
> | ViewModel | `ViewModelComponent` | Theo ViewModel |

---

### 3.6 `@Singleton` vs không có annotation

```kotlin
// Trong AppModule
@Provides
@Singleton          // Chỉ tạo 1 lần → tất cả dùng chung 1 database instance
fun provideHabitDatabase(...): HabitDatabase = ...

@Provides
// Không có @Singleton → mỗi lần ai hỏi sẽ tạo mới (nhưng HabitDao rất nhẹ nên OK)
fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()
```

---

## 4. Lấy ViewModel trong Compose

Trong Compose, **không** dùng `ViewModelProvider` thủ công. Dùng `hiltViewModel()`:

```kotlin
// HomeScreen.kt
@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit,
    // ...
    viewModel: HomeViewModel = hiltViewModel()  // ← Hilt tự tạo và inject
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

> **Tại sao `= hiltViewModel()` có giá trị default?**
> Khi preview Compose trong Android Studio, bạn có thể truyền mock ViewModel vào. Khi chạy thật, Hilt tự điền.

---

## 5. Scope và Lifetime — Hiểu để dùng đúng

```
App (SingletonComponent)
│
├── HabitDatabase          @Singleton → 1 instance cho toàn app
├── HabitRepository        @Singleton → 1 instance cho toàn app
├── LocalNotificationManager  @Singleton
└── NotificationScheduler     @Singleton
        │
        ▼
Activity (ActivityComponent)
│
└── ViewModel (ViewModelComponent)
    │
    ├── HomeViewModel      @HiltViewModel → 1 per screen instance
    ├── AddEditViewModel   @HiltViewModel
    └── DetailViewModel    @HiltViewModel
```

**Quy tắc**: Dependency ở scope thấp hơn KHÔNG được inject vào scope cao hơn.
- ✅ `HabitRepository` (`@Singleton`) → inject vào `HomeViewModel`
- ❌ `HomeViewModel` → inject vào `HabitRepository` (không hợp lý về lifecycle)

---

## 6. Ví dụ thực tế: Thêm feature mới với Hilt

### Tình huống: Tạo màn hình `AchievementScreen` hiển thị thành tích

#### Bước 1: Tạo service cần inject

```kotlin
// utils/AchievementService.kt
class AchievementService @Inject constructor(
    private val repository: HabitRepository  // Hilt đưa repository vào đây
) {
    suspend fun calculateBadges(): List<String> {
        // logic tính thành tích...
        return listOf("🔥 7-day streak", "✅ 30 completions")
    }
}
```

> Không cần thêm gì vào Module! Vì `AchievementService` dùng `@Inject constructor`, Hilt tự biết cách tạo nó.

#### Bước 2: Tạo ViewModel

```kotlin
// ui/screen/achievement/AchievementViewModel.kt
data class AchievementUiState(
    val badges: List<String> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementService: AchievementService  // Hilt inject tự động
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            val badges = achievementService.calculateBadges()
            _uiState.update { it.copy(badges = badges, isLoading = false) }
        }
    }
}
```

#### Bước 3: Tạo Screen (Hilt lo phần còn lại)

```kotlin
// ui/screen/achievement/AchievementScreen.kt
@Composable
fun AchievementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementViewModel = hiltViewModel()  // Hilt inject!
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // UI...
}
```

---

## 7. `@ApplicationContext` — Lấy Context an toàn

Khi class cần `Context`, **không bao giờ** inject `Activity` context (gây memory leak). Dùng `@ApplicationContext`:

```kotlin
// Ví dụ từ NotificationModule.kt
@Provides
@Singleton
fun provideLocalNotificationManager(
    @ApplicationContext context: Context   // ← Context của Application, không leak
): LocalNotificationManager = LocalNotificationManager(context)
```

---

## 8. Lỗi thường gặp & Cách fix

### ❌ Lỗi: "Cannot provide without @Inject or @Provides"

```
[Hilt] Error: com.dttrn.habit_tracking.data.repository.HabitRepository
cannot be provided without an @Inject constructor or an @Provides-annotated method.
```

**Nguyên nhân**: Hilt không biết cách tạo class.

**Fix**: Thêm `@Inject constructor` vào class, HOẶC thêm `@Provides` function vào Module.

---

### ❌ Lỗi: "@HiltViewModel không có @Inject"

```
[Hilt] Error: HomeViewModel must have exactly one @Inject annotated constructor
```

**Fix**:
```kotlin
// ❌ Sai
@HiltViewModel
class HomeViewModel(private val repo: HabitRepository) : ViewModel()

// ✅ Đúng
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: HabitRepository
) : ViewModel()
```

---

### ❌ Lỗi: "Scoping error — injecting @Singleton into shorter-lived scope"

Thường xảy ra khi inject Activity-scoped dep vào Singleton.

**Fix**: Kiểm tra lại `@InstallIn` và `@Singleton` scope cho đúng.

---

### ❌ Lỗi: Quên `@AndroidEntryPoint` trên Activity/Fragment

```
Cannot create an instance of class HomeViewModel
```

**Fix**: Đảm bảo `MainActivity` có `@AndroidEntryPoint`.

---

## 9. Map toàn bộ Hilt trong project

| File | Annotation | Ý nghĩa |
|---|---|---|
| `HabitTrackingApp.kt` | `@HiltAndroidApp` | Bật Hilt cho app |
| `MainActivity.kt` | `@AndroidEntryPoint` | Cho phép Hilt inject vào Activity |
| `di/AppModule.kt` | `@Module @InstallIn(Singleton)` | Cung cấp Database, DAOs |
| `di/NotificationModule.kt` | `@Module @InstallIn(Singleton)` | Cung cấp Notification services |
| `HabitRepository.kt` | `@Singleton @Inject constructor` | Singleton, nhận DAOs từ Hilt |
| `HomeViewModel.kt` | `@HiltViewModel @Inject constructor` | Nhận Repository từ Hilt |
| `AddEditViewModel.kt` | `@HiltViewModel @Inject constructor` | Nhận Repository từ Hilt |
| `DetailViewModel.kt` | `@HiltViewModel @Inject constructor` | Nhận Repository từ Hilt |
| `StatisticsViewModel.kt` | `@HiltViewModel @Inject constructor` | Nhận Repository từ Hilt |
| `SettingsViewModel.kt` | `@HiltViewModel @Inject constructor` | Nhận Repository + services từ Hilt |
| `ReminderViewModel.kt` | `@HiltViewModel @Inject constructor` | Nhận Notification services từ Hilt |
| `HomeScreen.kt` | `hiltViewModel()` | Lấy ViewModel từ Hilt |

---

## 10. Cheatsheet nhanh

```
Cần inject class tự viết?     → Thêm @Inject constructor
Cần inject vào ViewModel?      → @HiltViewModel + @Inject constructor
Cần inject interface/3rd lib?  → Tạo @Module với @Provides
Cần dùng trong Activity?       → @AndroidEntryPoint
Khởi động app?                 → @HiltAndroidApp trên Application class
Cần Context?                   → @ApplicationContext Context
Singleton (dùng chung)?        → @Singleton
Lấy ViewModel trong Compose?   → hiltViewModel()
```

---

## 11. Khi nào cần chạm vào Hilt?

| Task | Việc cần làm |
|---|---|
| Tạo screen mới | Chỉ cần `@HiltViewModel @Inject constructor` + `hiltViewModel()` |
| Tạo service/helper mới cần inject | Thêm `@Inject constructor` vào class đó |
| Tạo table Room mới | Thêm `@Provides fun provideXxxDao(db)` vào `AppModule` |
| Class cần Context | Dùng `@ApplicationContext` trong constructor |
| Muốn 1 instance dùng chung | Thêm `@Singleton` |
