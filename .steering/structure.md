# Project Structure: Habit Tracking

## Directory Layout
```
app/src/main/java/com/dttrn/habit_tracking/
├── data/
│   ├── db/              # Room Database, DAO, Entity
│   └── repository/      # Implement Repository mapping Entity <-> Domain
├── di/                  # Hilt Modules (AppModule, NotificationModule)
├── domain/
│   └── model/           # Data classes thuần (Habit, HabitLog)
├── navigation/          # NavGraph, Route declarations (Sealed classes)
├── ui/
│   ├── components/      # Reusable composables (HabitCard, HeatMap, etc.)
│   ├── screen/          # Tính năng màn hình (Home, Detail, AddEdit, Settings...)
│   └── theme/           # Color, Typography, Material3 Theme
├── utils/               # Alarm, Notification Receivers & Helpers
└── widget/              # Glance App Widget
```

## Naming Conventions
| Element | Convention | Example |
|---------|-----------|---------|
| Component | PascalCase | `HabitCard.kt` |
| ViewModels | PascalCase, hậu tố ViewModel | `HomeViewModel.kt` |
| Packages | lowercase, snake_case | `add_edit`, `habit_tracking` |
| Routes | snake_case | `edit_habit`, `habit_detail` |

## Code Organization Principles
1. **Unidirectional Data Flow (UDF)**: User Action -> ViewModel -> Repository -> Room DB -> Flow updates -> UiState -> Compose UI.
2. **ViewModel Constraints**: ViewModel KHÔNG bao giờ giữ reference tới `NavController`. Các thao tác chuyển hướng (navigation) phải được truyền qua các lambda callbacks từ UI.
3. **Repository Responsibility**: Chỉ Repository mới xử lý mapping giữa Entity (Data Layer) và Domain Model (Domain Layer). UI Layer chỉ làm việc với Domain Model.
4. **Data Streams**: DAO cung cấp dữ liệu qua Kotlin `Flow` để đảm bảo UI phản ứng linh hoạt với các thay đổi dưới cơ sở dữ liệu. Dùng `suspend` cho các tác vụ một lần (one-shot queries).
5. **Dependency Injection**: Luôn ưu tiên Constructor Injection (`@Inject constructor`) qua Dagger Hilt.
6. **State Management**: Sử dụng `StateFlow` và đóng gói tất cả các biến trạng thái giao diện vào trong duy nhất một `UiState` data class (VD: `HomeUiState`).
