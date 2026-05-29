# Tasks: Daily Journal

## Overview
Total: 5 tasks | Estimated: ~7.5 hours

## Task List

### Task 1: Database & Domain Layer Setup
- **Type**: `setup`
- **Priority**: `high`
- **Estimated**: 1.5h
- **Dependencies**: None
- **Description**: Tạo cấu trúc dữ liệu cơ bản cho tính năng Journal (Entity, Model, DAO) và đăng ký vào Database hiện tại.
- **Files**:
  - Create: `app/src/main/java/com/dttrn/habit_tracking/data/db/entity/JournalEntity.kt`
  - Create: `app/src/main/java/com/dttrn/habit_tracking/data/db/dao/JournalDao.kt`
  - Create: `app/src/main/java/com/dttrn/habit_tracking/domain/model/Journal.kt`
  - Modify: `app/src/main/java/com/dttrn/habit_tracking/data/db/HabitDatabase.kt`
- **Acceptance**:
  - [x] Schema Room Database được cập nhật (version update hoặc auto-migration nếu cần).
  - [x] `JournalDao` có đầy đủ các hàm Insert(Replace), Select by date, Select all dates, Delete.

---

### Task 2: Repository & DI Setup
- **Type**: `setup`
- **Priority**: `high`
- **Estimated**: 1h
- **Dependencies**: Task 1
- **Description**: Tạo Repository để tương tác với DAO, mapping giữa Entity và Domain Model. Đăng ký DI Hilt cho các thành phần mới.
- **Files**:
  - Create: `app/src/main/java/com/dttrn/habit_tracking/data/repository/JournalRepository.kt`
  - Modify: `app/src/main/java/com/dttrn/habit_tracking/di/AppModule.kt`
- **Acceptance**:
  - [x] `JournalRepository` mapping data thành công giữa `JournalEntity` và `Journal`.
  - [x] `JournalDao` và `JournalRepository` được provide thành công trong `AppModule`.

---

### Task 3: Navigation & Bottom Tab UI
- **Type**: `feature`
- **Priority**: `medium`
- **Estimated**: 1h
- **Dependencies**: None
- **Description**: Khai báo route mới và tích hợp Tab Journal vào Bottom Navigation Bar hiện tại của ứng dụng.
- **Files**:
  - Modify: `app/src/main/java/com/dttrn/habit_tracking/navigation/Screen.kt`
  - Modify: `app/src/main/java/com/dttrn/habit_tracking/navigation/NavGraph.kt`
  - Modify: File chứa Bottom Navigation (VD: `MainScaffold` hoặc `HomeScreen` tùy cấu trúc hiện tại)
- **Acceptance**:
  - [x] Bottom Navigation xuất hiện thêm tab "Nhật ký" / "Journal".
  - [x] Có thể click chuyển qua lại giữa các tab mượt mà.
  - [x] Khai báo đủ route cho màn hình Calendar và màn hình Detail (nhận tham số `date`).

---

### Task 4: Journal Screen (Calendar View)
- **Type**: `feature`
- **Priority**: `high`
- **Estimated**: 2h
- **Dependencies**: Task 2, Task 3
- **Description**: Xây dựng màn hình hiển thị Lịch. Load dữ liệu từ DB xem những ngày nào đã có nhật ký để highlight trên lịch.
- **Files**:
  - Create: `app/src/main/java/com/dttrn/habit_tracking/ui/screen/journal/JournalViewModel.kt`
  - Create: `app/src/main/java/com/dttrn/habit_tracking/ui/screen/journal/JournalScreen.kt`
- **Acceptance**:
  - [x] Hiển thị được UI Lịch (Calendar).
  - [x] Dữ liệu (chấm màu / highlight) tự động hiển thị ở các ngày đã có bài nhật ký nhờ Flow.
  - [x] Bấm vào một ngày trên lịch sẽ điều hướng sang `JournalDetailScreen` truyền theo ngày đó.

---

### Task 5: Journal Detail Screen (Text Editor)
- **Type**: `feature`
- **Priority**: `high`
- **Estimated**: 2h
- **Dependencies**: Task 4
- **Description**: Tạo màn hình soạn thảo văn bản cho ngày cụ thể. Xử lý logic lưu, load nội dung hiện có, xóa bài.
- **Files**:
  - Create: `app/src/main/java/com/dttrn/habit_tracking/ui/screen/journal/JournalDetailViewModel.kt`
  - Create: `app/src/main/java/com/dttrn/habit_tracking/ui/screen/journal/JournalDetailScreen.kt`
- **Acceptance**:
  - [x] Text field đa dòng, tự động load nội dung cũ của ngày đó (nếu có).
  - [x] Bấm nút "Lưu" -> Cập nhật thành công vào Database và hiển thị thông báo.
  - [x] Nút "Xóa" hoạt động chính xác (xóa trắng record của ngày đó).

## Implementation Order
1. **Task 1** (Database & Domain Layer Setup) - Bắt buộc làm nền tảng.
2. **Task 2** (Repository & DI Setup) - Kết nối DB với logic.
3. **Task 3** (Navigation & Bottom Tab UI) - Định hình khung giao diện chính.
4. **Task 4** (Journal Screen - Calendar View) - Giao diện Lịch.
5. **Task 5** (Journal Detail Screen - Text Editor) - Giao diện chi tiết (phụ thuộc vào navigation của Task 4).
