# Tech Stack: Habit Tracking

## Core Technologies
| Technology | Version | Purpose |
|-----------|---------|---------|
| Android SDK | Min 26, Target 36 | Nền tảng ứng dụng |
| Kotlin | 2.2.10 | Ngôn ngữ lập trình chính |
| Jetpack Compose | BOM 2024.12.01 | Xây dựng giao diện người dùng (UI) |
| Room Database | 2.7.1 | Lưu trữ dữ liệu cục bộ (Local Database) |
| Dagger Hilt | 2.56 | Dependency Injection (DI) |
| Jetpack Glance | - | Xây dựng App Widget |
| Navigation Compose| 2.8.9 | Điều hướng giữa các màn hình |

## Architecture Pattern
- **MVVM + Repository Pattern**: Phân tách rõ ràng giữa UI (Compose), Presentation Logic (ViewModel), Business Logic (Repository), và Data (Room DB).
- **Single Source of Truth**: Repository chịu trách nhiệm làm nguồn dữ liệu duy nhất và thực hiện chuyển đổi (mapping) giữa Entity và Domain model.

## Development Practices
- **Reactive UI**: Sử dụng `StateFlow` kết hợp với pattern `UiState` cho quản lý trạng thái, nói không với `LiveData`.
- **Background Processing**: Sử dụng `WorkManager` và AlarmManager (Local NotificationManager) cho các tác vụ nhắc nhở.
- **Build System**: Gradle KTS kết hợp với Version Catalog (`libs.versions.toml`) để quản lý dependencies.
- **KSP**: Sử dụng KSP thay thế cho KAPT để cải thiện tốc độ biên dịch (Room, Hilt).

## Key Constraints
- Hoạt động 100% offline (local database).
- Hiệu suất cao, tất cả truy vấn DB đều được thực thi trên background thread thông qua Coroutines và Flow.
- Hỗ trợ đầy đủ Dark mode và kích thước APK tối ưu (< 20 MB).
