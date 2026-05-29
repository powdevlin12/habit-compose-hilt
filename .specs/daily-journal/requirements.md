# Requirements: Daily Journal (Nhật ký hằng ngày)

## Overview
Tính năng "Daily Journal" cho phép người dùng ghi chép lại nhật ký, cảm xúc hoặc những suy nghĩ hằng ngày của mình. Tính năng này hoạt động như một cuốn sổ tay cá nhân tích hợp bên trong ứng dụng theo dõi thói quen, giúp người dùng nhìn lại ngày của mình một cách tổng quan thay vì chỉ check-in các thói quen cụ thể.

## Problem Statement
Bên cạnh việc theo dõi các thói quen (đã làm/chưa làm) và ghi chú (note) cho từng thói quen riêng lẻ, người dùng thường có nhu cầu ghi lại cảm xúc, những thành tựu chung hoặc suy ngẫm trong một ngày. Hiện tại ứng dụng chưa có không gian độc lập để người dùng trút bầu tâm sự hoặc lưu giữ những kỷ niệm cá nhân gắn với từng ngày.

## User Stories
- As a user, I want to write a general text entry for a specific day so that I can record my thoughts and feelings.
- As a user, I want to view a history of my past journal entries so that I can reflect on my personal journey.
- As a user, I want to edit or delete my journal entries so that I can keep my records accurate and relevant.

## Acceptance Criteria

### AC-1: Giao diện Viết/Xem nhật ký (Journal Screen)
- [ ] Cung cấp giao diện để người dùng soạn thảo nội dung văn bản (không giới hạn dòng) cho ngày được chọn.
- [ ] Hiển thị rõ ràng ngày (Date) của bài viết.
- [ ] Có nút Lưu (Save) để ghi nhận thay đổi vào cơ sở dữ liệu và hiển thị thông báo thành công.

### AC-2: Database & Data layer
- [ ] Thêm Entity mới (VD: `JournalEntity`) vào Room Database chứa thông tin: ID, ngày (LocalDate/chuỗi YYYY-MM-DD), và nội dung văn bản.
- [ ] Ràng buộc (Constraint): Mỗi ngày chỉ có tối đa một bản ghi nhật ký. Thao tác lưu mới vào ngày đã tồn tại sẽ ghi đè (Update) nội dung cũ.

### AC-3: Quản lý danh sách (Journal History)
- [ ] Hiển thị danh sách các bài nhật ký đã viết, sắp xếp theo thứ tự thời gian giảm dần (mới nhất xếp trên).
- [ ] Cho phép nhấn vào một thẻ (card) nhật ký trong quá khứ để xem chi tiết và chỉnh sửa.
- [ ] Hỗ trợ tính năng Xóa (Delete) bài nhật ký đã viết.

## Out of Scope
- Trình soạn thảo văn bản đa dạng (Rich text editor) như in đậm, in nghiêng, chèn hình ảnh, chèn âm thanh.
- Tìm kiếm (Search) nội dung nhật ký bằng từ khóa (sẽ cân nhắc trong tương lai).
- Sao lưu tự động (Auto cloud backup) cho các ghi chú này.

## Open Questions
- [ ] **Vị trí UI:** Chức năng "Journal" này sẽ được truy cập từ đâu? (Thêm một Tab mới vào Bottom Navigation, hay một nút trên màn hình Home, hoặc trong màn hình Thống kê?)
- [ ] **Định dạng hiển thị lịch sử:** Bạn muốn hiển thị lịch sử nhật ký dưới dạng danh sách cuộn dọc đơn thuần hay dưới dạng lịch (Calendar view) để người dùng bấm vào xem?
