# Product Requirements Document (PRD)
## Ứng dụng Theo Dõi Thói Quen (Habit Journey Tracker)

---

**Phiên bản:** 1.0  
**Ngày:** 02/05/2026  
**Nền tảng:** Android (Jetpack Compose + Kotlin)  
**Lưu trữ:** Room Database (local)

---

## 1. Tổng quan sản phẩm

### 1.1 Mô tả

Habit Journey Tracker là ứng dụng Android giúp người dùng xây dựng và duy trì thói quen hằng ngày. Ứng dụng cho phép tạo thói quen tùy chỉnh, theo dõi tiến độ qua heat map trực quan, và cung cấp widget nhanh ngay ngoài màn hình chính (Home Screen) để check-in thuận tiện.

### 1.2 Mục tiêu

- Giúp người dùng hình thành thói quen tốt và duy trì chuỗi ngày liên tục (streak).
- Trực quan hóa hành trình bằng heat map theo tháng/năm.
- Giảm ma sát bằng widget ngoài màn hình chính — check-in không cần mở app.
- Hoạt động hoàn toàn offline, không cần tài khoản.

### 1.3 Đối tượng người dùng

- Người muốn xây dựng thói quen mới (tập thể dục, đọc sách, uống nước, thiền...)
- Người dùng ưa giao diện đơn giản, tập trung vào dữ liệu cá nhân

---

## 2. Kiến trúc kỹ thuật

### 2.1 Tech Stack

| Thành phần | Công nghệ |
|---|---|
| UI | Jetpack Compose |
| Ngôn ngữ | Kotlin |
| Database | Room Database |
| Architecture | MVVM + Repository Pattern |
| Navigation | Compose Navigation |
| Widget | Glance API (Jetpack Glance) |
| DI | Hilt |
| Coroutines | Kotlin Coroutines + Flow |
| Date/Time | `java.time` (API 26+) |

### 2.2 Cấu trúc thư mục (gợi ý)

```
app/
├── data/
│   ├── db/
│   │   ├── HabitDatabase.kt
│   │   ├── dao/
│   │   │   ├── HabitDao.kt
│   │   │   └── HabitLogDao.kt
│   │   └── entity/
│   │       ├── HabitEntity.kt
│   │       └── HabitLogEntity.kt
│   └── repository/
│       └── HabitRepository.kt
├── domain/
│   └── model/
│       ├── Habit.kt
│       └── HabitLog.kt
├── ui/
│   ├── screen/
│   │   ├── home/
│   │   ├── detail/
│   │   ├── add_edit/
│   │   └── statistics/
│   ├── components/
│   │   ├── HeatMapView.kt
│   │   ├── HabitCard.kt
│   │   └── StreakBadge.kt
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
├── widget/
│   ├── HabitWidget.kt
│   └── HabitWidgetReceiver.kt
└── di/
    └── AppModule.kt
```

---

## 3. Database Schema (Room)

### 3.1 Bảng `habits`

```sql
CREATE TABLE habits (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    description TEXT,
    icon_emoji  TEXT DEFAULT '✅',
    color_hex   TEXT DEFAULT '#4CAF50',
    frequency   TEXT NOT NULL DEFAULT 'DAILY',  -- DAILY | WEEKLY | CUSTOM
    target_days TEXT,                            -- JSON array cho CUSTOM, e.g. [1,3,5]
    reminder_time TEXT,                          -- HH:mm hoặc NULL nếu không bật
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at  INTEGER NOT NULL                 -- Unix timestamp
)
```

### 3.2 Bảng `habit_logs`

```sql
CREATE TABLE habit_logs (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id   INTEGER NOT NULL,
    logged_date TEXT NOT NULL,                   -- ISO 8601: yyyy-MM-dd
    note       TEXT,
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    UNIQUE (habit_id, logged_date)               -- Mỗi ngày chỉ log 1 lần
)
```

---

## 4. Màn hình & Tính năng

### 4.1 Màn hình Home (`HomeScreen`)

**Mô tả:** Màn hình chính hiển thị danh sách thói quen trong ngày hôm nay.

**Các thành phần UI:**

- Header: Ngày hôm nay + câu động viên ngắn
- Thanh tiến độ tổng: "X/Y thói quen hoàn thành hôm nay"
- Danh sách `HabitCard`:
  - Tên thói quen + icon emoji
  - Streak hiện tại (🔥 N ngày)
  - Nút check-in (toggle: chưa làm / đã làm)
  - Mini heat map 7 ngày gần nhất
- FAB (+): Thêm thói quen mới
- Bottom Navigation: Home | Thống kê | Cài đặt

**Hành vi:**
- Tap vào card → mở `DetailScreen`
- Long press → hiện menu nhanh: Sửa / Xoá / Lưu trữ
- Pull-to-refresh không cần (dữ liệu local, reactive qua Flow)

---

### 4.2 Màn hình Thêm / Sửa thói quen (`AddEditScreen`)

**Các trường nhập:**

| Trường | Loại | Bắt buộc |
|---|---|---|
| Tên thói quen | Text input | ✅ |
| Mô tả | Text input (multiline) | ❌ |
| Icon (emoji picker) | Grid chọn emoji | ❌ |
| Màu sắc | Color picker (6–8 màu preset) | ✅ |
| Tần suất | Dropdown: Hằng ngày / Hằng tuần / Tùy chỉnh | ✅ |
| Ngày trong tuần (nếu Tùy chỉnh) | Multi-select chip: T2–CN | Conditional |
| Nhắc nhở | Time Picker + toggle bật/tắt | ❌ |

**Validation:**
- Tên không được để trống
- Tên không được trùng với thói quen đang hoạt động

---

### 4.3 Màn hình Chi tiết (`DetailScreen`)

**Nội dung:**

- Header: Tên + emoji + màu sắc thói quen
- Thông tin streak:
  - 🔥 Streak hiện tại: N ngày
  - 🏆 Streak dài nhất từ trước đến nay
  - 📅 Ngày bắt đầu
- **Heat Map (trọng tâm)**:
  - Hiển thị 365 ngày gần nhất (dạng GitHub contribution graph)
  - Mỗi ô = 1 ngày, màu sắc theo cường độ:
    - Không có log → màu nền nhạt (xám/trắng)
    - Có log → màu thói quen (gradient nhạt → đậm nếu có note)
  - Tap vào ô → hiện popup: ngày + note (nếu có)
  - Hỗ trợ scroll ngang (12 tháng)
  - Chú thích: "Ít hơn ←→ Nhiều hơn" với dải màu
- Nút: Sửa thói quen | Lưu trữ | Xoá

---

### 4.4 Màn hình Thống kê (`StatisticsScreen`)

**Nội dung:**

- Tổng quan tất cả thói quen:
  - Số thói quen đang hoạt động
  - Tỷ lệ hoàn thành trung bình 30 ngày qua (%)
  - Thói quen có streak dài nhất
- Biểu đồ thanh (Bar Chart): Tổng số check-in theo tuần (8 tuần gần nhất)
- Danh sách top thói quen theo streak

---

### 4.5 Màn hình Cài đặt (`SettingsScreen`)

- Chủ đề: Sáng / Tối / Theo hệ thống
- Ngôn ngữ (tiếng Việt / tiếng Anh)
- Nhắc nhở mặc định (giờ gợi ý mặc định cho thói quen mới)
- Xuất dữ liệu: Export sang CSV (tùy chọn nâng cao)
- Xoá toàn bộ dữ liệu (có confirm dialog)

---

## 5. Heat Map Component

### 5.1 Yêu cầu kỹ thuật

```kotlin
// HeatMapView.kt — Custom Composable
@Composable
fun HabitHeatMap(
    logs: List<LocalDate>,          // Danh sách ngày đã check-in
    habitColor: Color,              // Màu chủ đạo của thói quen
    startDate: LocalDate,           // Ngày bắt đầu hiển thị
    endDate: LocalDate = LocalDate.now(),
    onDayClick: (LocalDate) -> Unit = {}
)
```

### 5.2 Quy tắc hiển thị màu

| Trạng thái | Màu |
|---|---|
| Không có log | `Color.LightGray.copy(alpha = 0.3f)` |
| Có log (không note) | `habitColor.copy(alpha = 0.6f)` |
| Có log + có note | `habitColor` (full opacity) |
| Ngày trong tương lai | Không hiển thị / ẩn |
| Hôm nay (chưa log) | Viền nét đứt màu `habitColor` |

### 5.3 Layout

- Grid 7 hàng × N cột (7 ngày/tuần × số tuần)
- Mỗi ô: `8.dp × 8.dp`, khoảng cách `2.dp`
- Header cột: tháng (hiện khi bắt đầu tháng mới)
- Header hàng: T2, T4, T6 (thứ trong tuần)
- Toàn bộ wrap trong `LazyRow` để scroll ngang

---

## 6. Widget (Glance API)

### 6.1 Mô tả

Widget nhỏ gọn hiển thị trên Home Screen, cho phép người dùng check-in nhanh mà không cần mở app.

### 6.2 Kích thước hỗ trợ

| Kích thước | Mô tả |
|---|---|
| Small (2×1) | 1 thói quen + nút check-in |
| Medium (4×2) | 3–4 thói quen + nút check-in từng cái |
| Large (4×4) | Toàn bộ danh sách hôm nay + mini heat map 7 ngày |

### 6.3 Nội dung widget (Medium — mặc định)

```
┌─────────────────────────────────────┐
│  📅 Hôm nay — Thứ Sáu, 02/05       │
│  ─────────────────────────────────  │
│  ✅ [🏋️ Tập gym       ] [  ✓  ]    │
│  ✅ [📖 Đọc sách       ] [  ✓  ]    │
│  ⬜ [💧 Uống nước      ] [  +  ]    │
│  ─────────────────────────────────  │
│  2/3 hoàn thành          [Mở App]   │
└─────────────────────────────────────┘
```

### 6.4 Hành vi widget

- Check-in từ widget → ghi log vào Room DB, cập nhật UI widget ngay lập tức
- Tap vào tên thói quen → deep link mở `DetailScreen` của thói quen đó
- Tap "Mở App" → mở `HomeScreen`
- Widget tự refresh mỗi 15 phút hoặc khi có thay đổi dữ liệu (dùng `GlanceAppWidgetManager.updateIf`)
- Không yêu cầu internet — hoàn toàn offline

### 6.5 Implementation notes (Glance)

```kotlin
class HabitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            HabitWidgetContent()
        }
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = HabitWidget()
}
```

Khai báo trong `AndroidManifest.xml`:
```xml
<receiver android:name=".widget.HabitWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/habit_widget_info" />
</receiver>
```

---

## 7. Notification / Reminder

- Dùng `WorkManager` để schedule nhắc nhở theo giờ đã cài
- Khi thói quen có `reminder_time` được bật:
  - Tạo `PeriodicWorkRequest` hoặc `OneTimeWorkRequest` lặp lại hằng ngày
  - Notification hiển thị: `"Đã đến giờ [Tên thói quen] rồi! 💪"`
  - Tap notification → mở `HomeScreen`, highlight thói quen tương ứng
- Hủy notification nếu thói quen đã được check-in trong ngày

---

## 8. Luồng người dùng (User Flow)

```
[Lần đầu mở app]
       │
       ▼
[HomeScreen — trống]
       │
       ▼  Tap FAB (+)
[AddEditScreen]
       │
       ▼  Lưu
[HomeScreen — hiện thói quen]
       │
       ├──► Tap check-in → Log vào DB → UI cập nhật ngay
       │
       ├──► Tap card → DetailScreen → xem heat map + streak
       │
       └──► Widget ngoài màn hình → check-in nhanh
```

---

## 9. Yêu cầu phi chức năng

| Tiêu chí | Yêu cầu |
|---|---|
| Hiệu năng | Danh sách 50+ thói quen không giật lag |
| Khởi động lạnh | < 1.5 giây |
| Database | Tất cả query chạy trên background thread (Room + Flow) |
| Offline | 100% hoạt động không cần mạng |
| Android version | Min SDK 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Dark mode | Hỗ trợ đầy đủ |
| Accessibility | Content description cho icon, contrast tối thiểu AA |
| Kích thước APK | < 20 MB |

---

## 10. Roadmap & Ưu tiên

### v1.0 — MVP

- [x] CRUD thói quen (thêm / sửa / xoá / lưu trữ)
- [x] Check-in hằng ngày
- [x] Heat map 1 năm
- [x] Streak counter
- [x] Widget Medium (4×2)
- [x] Room Database local

### v1.1 — Nâng cao

- [x] Thống kê chi tiết + biểu đồ
- [x] Reminder / Notification (WorkManager)
- [x] Dark mode hoàn chỉnh
- [x] Xuất CSV

### v1.2 — Tương lai

- [ ] Backup lên Google Drive *(bỏ qua theo yêu cầu)*
- [x] Chia sẻ streak (screenshot đẹp)
- [x] Thách thức (30-day challenge mode)
- [x] Nhiều profile

---

## 11. Rủi ro & Giải pháp

| Rủi ro | Giải pháp |
|---|---|
| Widget không cập nhật kịp thời | Dùng `GlanceAppWidgetManager` update sau mỗi action |
| Heat map lag với 365 ngày | Dùng `Canvas` API tự vẽ thay vì `LazyGrid` nhiều item |
| Mất dữ liệu khi xoá app | Cảnh báo người dùng; v1.1 thêm export CSV |
| Thói quen lặp (Weekly) tính streak sai | Logic tính streak riêng cho từng `frequency` |

---

*Tài liệu này là bản PRD nội bộ phục vụ cho quá trình phát triển ứng dụng. Cập nhật khi có thay đổi yêu cầu.*
