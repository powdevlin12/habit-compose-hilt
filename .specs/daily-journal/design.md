# Design: Daily Journal

## Architecture Overview
Luồng hoạt động sẽ đi từ màn hình chính qua Bottom Navigation, sau đó tương tác với CSDL cục bộ qua Repository và DAO.

```
[Bottom Navigation (Tab Mới)] → [JournalScreen (Calendar)]
            ↓ (Chọn Ngày)
[JournalDetailScreen] → [JournalViewModel]
            ↓
[JournalRepository] → [JournalDao] → [Room Database]
```

## Components / Modules

### New Components
| File | Type | Purpose |
|------|------|---------|
| `data/db/entity/JournalEntity.kt` | Entity | Bảng lưu dữ liệu nhật ký trong Room DB |
| `data/db/dao/JournalDao.kt` | DAO | Interface query CSDL cho các thao tác Journal |
| `domain/model/Journal.kt` | Model | Data class mô tả Journal ở Domain Layer |
| `data/repository/JournalRepository.kt` | Repository | Quản lý data operations riêng cho Journal |
| `ui/screen/journal/JournalScreen.kt` | Component | Màn hình chính chứa Calendar view |
| `ui/screen/journal/JournalViewModel.kt` | ViewModel | Quản lý trạng thái và Flow dữ liệu cho JournalScreen |
| `ui/screen/journal/JournalDetailScreen.kt` | Component | Màn hình/Dialog nhập và xem chi tiết văn bản nhật ký |
| `ui/screen/journal/JournalDetailViewModel.kt`| ViewModel | Xử lý logic lưu/xóa/hiển thị text của một ngày cụ thể |

### Modified Components
| File | Change | Reason |
|------|--------|--------|
| `data/db/HabitDatabase.kt` | Thêm `JournalEntity` và `JournalDao` | Đăng ký entity và schema mới |
| `di/AppModule.kt` | Cung cấp (Provide) `JournalDao` và `JournalRepository` | Hỗ trợ Dependency Injection |
| `navigation/Screen.kt` | Thêm route `Journal` và `JournalDetail` | Phục vụ điều hướng |
| `navigation/NavGraph.kt` | Thêm NavHost destinations cho Journal | Render UI khi điều hướng |
| Màn hình chứa BottomTab | Thêm Tab "Nhật ký" vào cấu trúc Bottom Navigation | Để người dùng truy cập từ màn hình chính |

## Data Flow
1. User nhấn Tab "Nhật ký" → `JournalScreen` được render.
2. `JournalViewModel` fetch `getAllJournalDates()` (chỉ lấy List các ngày) từ Database và highlight những ngày đã có nhật ký trên Calendar.
3. User nhấn vào 1 ngày trên Calendar → Điều hướng sang `JournalDetailScreen(date)`.
4. `JournalDetailViewModel` load nội dung nhật ký hiện tại của `date` đó (nếu có).
5. User nhập text và bấm "Lưu" → Gọi action lưu xuống `JournalRepository` (nếu đã có thì Update, nếu chưa thì Insert) → Hiển thị thông báo (Snackbar).
6. Quay lại `JournalScreen` → Lịch tự động update highlight (Reactive Flow).

## API / Interface Changes

### Data Models
```kotlin
// Domain Model (domain/model/Journal.kt)
data class Journal(
    val date: LocalDate,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

// Entity (data/db/entity/JournalEntity.kt)
@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey val date: String, // Chuỗi định dạng YYYY-MM-DD để dễ query & đảm bảo unique/ngày
    val content: String,
    val updatedAt: Long
)
```

### New Database Queries (JournalDao)
```kotlin
@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateJournal(journal: JournalEntity)

    @Query("SELECT * FROM journals WHERE date = :date")
    suspend fun getJournalByDate(date: String): JournalEntity?

    @Query("SELECT date FROM journals")
    fun getAllJournalDates(): Flow<List<String>> // Trả về dạng Flow để UI auto-update

    @Query("DELETE FROM journals WHERE date = :date")
    suspend fun deleteJournalByDate(date: String)
}
```

## State Management
```kotlin
// Trạng thái cho màn hình Calendar
data class JournalUiState(
    val activeDates: Set<LocalDate> = emptySet(), // Set chứa các ngày đã có nhật ký
    val isLoading: Boolean = true
)

// Trạng thái cho màn hình viết/sửa
data class JournalDetailUiState(
    val date: LocalDate = LocalDate.now(),
    val content: String = "",
    val isSaving: Boolean = false
)
```

## Performance Considerations
- Trong màn hình Calendar, query `getAllJournalDates` chỉ SELECT đúng cột `date` (trả về `List<String>`), không query toàn bộ `content` để giảm thiểu áp lực Memory khi Database lớn lên qua các năm.

## Error Handling
- Bắt exception khi thao tác CSDL gặp lỗi.
- Thông báo Toast / Snackbar ("Đã lưu nhật ký", "Xóa thành công").
