# 📁 Habit Tracking — Project Structure

> Ứng dụng Android theo dõi thói quen hàng ngày, xây dựng bằng **Kotlin + Jetpack Compose**, áp dụng kiến trúc **MVVM + Repository Pattern** kết hợp **Hilt DI**.

---

## 1. Thông tin chung

| Mục | Giá trị |
|---|---|
| **Package** | `com.dttrn.habit_tracking` |
| **Min SDK** | 26 (Android 8.0) |
| **Target / Compile SDK** | 36 |
| **Kotlin** | 2.2.10 |
| **Compose BOM** | 2024.12.01 |
| **Build System** | Gradle KTS + Version Catalog (`libs.versions.toml`) |
| **Database** | Room 2.7.1 (`habit_journey.db`) |
| **DI** | Hilt 2.56 + KSP 2.2.10-2.0.2 |
| **Navigation** | Navigation Compose 2.8.9 |
| **Serialization** | Kotlinx Serialization JSON 1.7.3 |

---

## 2. Kiến trúc tổng quan

```
┌──────────────────────────────────────────────────────┐
│                    UI Layer                          │
│  (Screens, Components, Theme, Navigation)            │
│  Jetpack Compose + Material 3                        │
├──────────────────────────────────────────────────────┤
│                 ViewModel Layer                      │
│  @HiltViewModel + StateFlow (UiState pattern)        │
├──────────────────────────────────────────────────────┤
│               Domain Layer (Model)                   │
│  Data classes: Habit, HabitLog, HabitFrequency       │
├──────────────────────────────────────────────────────┤
│                  Data Layer                           │
│  Repository → DAO → Room Database                    │
│  Entity ↔ Domain mapping trong Repository            │
├──────────────────────────────────────────────────────┤
│                  DI Layer                             │
│  Hilt Modules: AppModule, NotificationModule         │
├──────────────────────────────────────────────────────┤
│               Utils / Services                       │
│  Notification, Alarm, Widget                         │
└──────────────────────────────────────────────────────┘
```

### Luồng dữ liệu (Data Flow)

```
User Action → Screen (Compose) → ViewModel → Repository → DAO → Room DB
                  ↑                              │
                  └──── StateFlow / Flow ←────────┘
```

---

## 3. Cây thư mục chi tiết

```
habit_tracking/
├── app/
│   ├── build.gradle.kts              # Cấu hình module app (dependencies, SDK, plugins)
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml    # Khai báo Activity, Receiver, Permissions
│       │   ├── java/com/dttrn/habit_tracking/
│       │   │   ├── HabitTrackingApp.kt          # Application class (@HiltAndroidApp)
│       │   │   ├── MainActivity.kt              # Entry point (@AndroidEntryPoint)
│       │   │   │
│       │   │   ├── data/                        # ═══ DATA LAYER ═══
│       │   │   │   ├── db/
│       │   │   │   │   ├── HabitDatabase.kt     # @Database (entities, version)
│       │   │   │   │   ├── dao/
│       │   │   │   │   │   ├── HabitDao.kt      # @Dao - CRUD habits
│       │   │   │   │   │   └── HabitLogDao.kt   # @Dao - CRUD habit logs
│       │   │   │   │   └── entity/
│       │   │   │   │       ├── HabitEntity.kt    # @Entity "habits"
│       │   │   │   │       └── HabitLogEntity.kt # @Entity "habit_logs" (FK → habits)
│       │   │   │   └── repository/
│       │   │   │       └── HabitRepository.kt   # Trung tâm logic: mapper, streak, CRUD
│       │   │   │
│       │   │   ├── di/                          # ═══ DEPENDENCY INJECTION ═══
│       │   │   │   ├── AppModule.kt             # Provides: Database, DAO
│       │   │   │   └── NotificationModule.kt    # Provides: NotificationManager, Scheduler
│       │   │   │
│       │   │   ├── domain/                      # ═══ DOMAIN LAYER ═══
│       │   │   │   └── model/
│       │   │   │       ├── Habit.kt             # Domain model + HabitFrequency enum
│       │   │   │       └── HabitLog.kt          # Domain model cho log
│       │   │   │
│       │   │   ├── navigation/                  # ═══ NAVIGATION ═══
│       │   │   │   ├── Screen.kt                # Sealed class định nghĩa routes
│       │   │   │   └── NavGraph.kt              # NavHost + composable destinations
│       │   │   │
│       │   │   ├── ui/                          # ═══ UI LAYER ═══
│       │   │   │   ├── components/              # Reusable Composables
│       │   │   │   │   ├── HabitCard.kt         # Card hiển thị habit
│       │   │   │   │   ├── HeatMapView.kt       # Lưới heatmap kiểu GitHub
│       │   │   │   │   ├── NotificationPermissionHandler.kt
│       │   │   │   │   └── StreakBadge.kt       # Badge hiển thị streak
│       │   │   │   ├── screen/                  # Các màn hình (Screen + ViewModel)
│       │   │   │   │   ├── home/
│       │   │   │   │   │   ├── HomeScreen.kt
│       │   │   │   │   │   └── HomeViewModel.kt
│       │   │   │   │   ├── add_edit/
│       │   │   │   │   │   ├── AddEditScreen.kt
│       │   │   │   │   │   └── AddEditViewModel.kt
│       │   │   │   │   ├── detail/
│       │   │   │   │   │   ├── DetailScreen.kt
│       │   │   │   │   │   └── DetailViewModel.kt
│       │   │   │   │   ├── statistics/
│       │   │   │   │   │   ├── StatisticsScreen.kt
│       │   │   │   │   │   └── StatisticsViewModel.kt
│       │   │   │   │   ├── settings/
│       │   │   │   │   │   ├── SettingsScreen.kt
│       │   │   │   │   │   └── SettingsViewModel.kt
│       │   │   │   │   └── notification/
│       │   │   │   │       ├── ReminderScreen.kt
│       │   │   │   │       └── ReminderViewModel.kt
│       │   │   │   └── theme/                   # Design System
│       │   │   │       ├── Color.kt             # Color palette (Green, Teal, Neutral)
│       │   │   │       ├── Theme.kt             # Material3 Theme (Light/Dark)
│       │   │   │       └── Type.kt              # Typography
│       │   │   │
│       │   │   ├── utils/                       # ═══ UTILITIES ═══
│       │   │   │   ├── AlarmReceiver.kt         # BroadcastReceiver cho alarm
│       │   │   │   ├── NotificationActionReceiver.kt
│       │   │   │   ├── NotificationHelper.kt    # Tạo notification channels
│       │   │   │   ├── NotificationManager.kt   # Quản lý local notifications
│       │   │   │   └── NotificationScheduler.kt # Lên lịch notifications
│       │   │   │
│       │   │   └── widget/                      # ═══ APP WIDGET ═══
│       │   │       ├── HabitWidget.kt           # Glance AppWidget UI
│       │   │       └── HabitWidgetReceiver.kt   # Widget receiver
│       │   │
│       │   └── res/                             # Resources
│       │       ├── drawable/
│       │       ├── mipmap-*/                    # App icons
│       │       ├── values/                      # Strings, themes, colors
│       │       └── xml/                         # Widget info, backup rules
│       │
│       ├── androidTest/                         # Instrumentation tests
│       └── test/                                # Unit tests
│
├── build.gradle.kts                  # Root build (plugins declaration)
├── settings.gradle.kts               # Project settings, module includes
├── gradle/
│   ├── libs.versions.toml            # Version Catalog (tất cả dependencies)
│   └── wrapper/
├── gradle.properties
├── gradlew / gradlew.bat
│
├── FLOW_ARCHITECTURE.md              # Tài liệu kiến trúc luồng
├── NOTIFICATION_FLOW.md              # Tài liệu luồng notification
└── NOTI_LOCAL.md                     # Tài liệu notification local
```

---

## 4. Chi tiết các Layer

### 4.1 Data Layer

#### Database Schema

```
┌─────────────────────┐       ┌──────────────────────────┐
│      habits         │       │       habit_logs          │
├─────────────────────┤       ├──────────────────────────┤
│ id (PK, auto)       │◄──FK──│ id (PK, auto)            │
│ name                │       │ habitId                  │
│ description?        │       │ loggedDate (unique w/ FK) │
│ iconEmoji           │       │ note?                    │
│ colorHex            │       └──────────────────────────┘
│ frequency           │       
│ targetDays? (JSON)  │       ON DELETE CASCADE
│ reminderTime?       │
│ isArchived          │
│ createdAt           │
└─────────────────────┘
```

#### Entity → Domain Mapping
- **Entity** (`data/db/entity/`): Room annotations, raw types (String cho frequency, JSON string cho targetDays)
- **Domain** (`domain/model/`): Clean Kotlin data classes, enum cho `HabitFrequency`, `List<Int>` cho targetDays
- **Mapping**: Thực hiện trong `HabitRepository` qua các extension functions `toDomain()` / `toEntity()`

#### Repository Pattern
`HabitRepository` là **single source of truth**, cung cấp:
- CRUD operations (qua DAO)
- Business logic: `toggleLog()`, `calculateCurrentStreak()`, `calculateLongestStreak()`
- Export/Import: `getAllHabitsForExport()`, `restoreFromBackup()`
- Validation: `isDuplicateName()`

### 4.2 DI Layer (Hilt)

| Module | Scope | Provides |
|---|---|---|
| `AppModule` | `@SingletonComponent` | `HabitDatabase`, `HabitDao`, `HabitLogDao` |
| `NotificationModule` | `@SingletonComponent` | `LocalNotificationManager`, `NotificationScheduler` |

### 4.3 UI Layer

#### Quy ước Screen + ViewModel
Mỗi màn hình có cặp file:
- `<Feature>Screen.kt` — `@Composable` function, nhận navigation callbacks qua lambda
- `<Feature>ViewModel.kt` — `@HiltViewModel`, sử dụng `StateFlow<UiState>` pattern

#### UiState Pattern
```kotlin
// Ví dụ: HomeViewModel
data class HomeUiState(
    val habitsWithStatus: List<HabitWithStatus> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now()
)

// Trong ViewModel
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
```

#### Navigation
- **Route Definition**: `sealed class Screen` với `data object` cho mỗi destination
- **Argument Passing**: Dùng URL-style paths (`edit_habit/{habitId}`)
- **NavGraph**: Tập trung trong `NavGraph.kt`, callbacks qua lambda (không inject NavController vào ViewModel)

### 4.4 Theme / Design System

- **Color Scheme**: Green-based palette (Green10→Green90, Teal, NeutralVariant)
- **Light / Dark**: Cả hai đều được define, auto-switch theo system
- **Dynamic Color**: Hỗ trợ (Android 12+) nhưng mặc định tắt
- **Typography**: Custom `HabitTypography`

---

## 5. Tính năng chính

| # | Tính năng | Files liên quan |
|---|---|---|
| 1 | 📋 Quản lý habit (CRUD) | `HomeScreen`, `AddEditScreen`, `HabitRepository` |
| 2 | ✅ Check-in hàng ngày | `HomeViewModel.toggleCheckIn()`, `HabitRepository.toggleLog()` |
| 3 | 🔥 Streak tracking | `HabitRepository.calculateCurrentStreak/LongestStreak()` |
| 4 | 📊 Thống kê / Heatmap | `StatisticsScreen`, `HeatMapView`, `HabitRepository.getCompletionRate()` |
| 5 | 🔔 Nhắc nhở (Local Notification) | `utils/Notification*.kt`, `ReminderScreen` |
| 6 | 📱 App Widget (Glance) | `widget/HabitWidget.kt`, `HabitWidgetReceiver.kt` |
| 7 | ⚙️ Settings / Export-Import | `SettingsScreen`, `SettingsViewModel` |
| 8 | 🗄️ Lưu trữ (Archive) | `HabitRepository.archiveHabit()`, `HabitDao.setArchived()` |

---

## 6. Quy ước & Convention

### Naming
- **Package**: lowercase, underscore (`add_edit`, `habit_tracking`)
- **Files**: PascalCase cho class (`HomeScreen.kt`), file name = class name
- **Routes**: snake_case (`edit_habit`, `habit_detail`)

### Architecture Rules
1. **ViewModel** KHÔNG giữ reference tới `NavController` — navigation qua lambda callbacks
2. **Repository** là layer duy nhất mapping Entity ↔ Domain
3. **DAO** trả về `Flow` cho reactive data, `suspend` cho one-shot queries
4. **DI** qua constructor injection (`@Inject constructor`)
5. **State management** dùng `StateFlow` + `UiState` data class (KHÔNG dùng `LiveData`)

### Dependencies
- Tất cả dependencies quản lý qua **Version Catalog** (`gradle/libs.versions.toml`)
- Sử dụng **KSP** thay cho kapt (cho Room compiler và Hilt compiler)

---

## 7. Permissions

| Permission | Mục đích |
|---|---|
| `RECEIVE_BOOT_COMPLETED` | Khôi phục alarm sau khi reboot |
| `POST_NOTIFICATIONS` | Gửi local notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Đặt alarm chính xác cho reminder |

---

## 8. Deep Link

- **Scheme**: `habittracker://`
- Khai báo trong `AndroidManifest.xml` trên `MainActivity`
