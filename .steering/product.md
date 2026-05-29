# Product: Habit Tracking

## Vision
Habit Journey Tracker là ứng dụng Android giúp người dùng xây dựng và duy trì thói quen hằng ngày. Ứng dụng cung cấp các công cụ trực quan như heat map theo năm và widget tiện lợi ở màn hình chính, nhằm giảm thiểu ma sát trong quá trình check-in và giúp người dùng dễ dàng theo dõi hành trình phát triển bản thân.

## Target Users
- Những người muốn xây dựng và duy trì thói quen mới (tập thể dục, đọc sách, uống nước, thiền...).
- Những người dùng yêu thích sự tối giản, tập trung vào cá nhân hóa và tính riêng tư của dữ liệu (ứng dụng hoạt động hoàn toàn offline).

## Key Features
- **Quản lý thói quen (CRUD)**: Thêm, sửa, xóa, và lưu trữ thói quen với màu sắc và biểu tượng tùy chỉnh.
- **Theo dõi chuỗi (Streak)**: Theo dõi chuỗi ngày liên tục hiện tại và chuỗi ngày dài nhất.
- **Heat Map 365 ngày**: Trực quan hóa dữ liệu qua biểu đồ nhiệt dạng GitHub, cung cấp cái nhìn toàn cảnh về cường độ thực hiện thói quen.
- **Widget màn hình chính (Glance)**: Check-in nhanh không cần mở ứng dụng.
- **Nhắc nhở (Local Notification)**: Nhắc nhở người dùng theo giờ đã cài đặt.
- **Thống kê tổng quan**: Biểu đồ hiển thị tỷ lệ hoàn thành, tổng số check-in theo tuần, top thói quen.
- **Chế độ sáng/tối** và hỗ trợ xuất dữ liệu ra tệp CSV.

## Success Metrics
- Người dùng có thể theo dõi mượt mà 50+ thói quen mà không gặp hiện tượng giật lag.
- Thời gian khởi động ứng dụng (Cold start) < 1.5 giây.
- Ứng dụng duy trì trải nghiệm liền mạch, đảm bảo dữ liệu ghi nhận tức thì (qua Flow).

## Non-Goals
- Không yêu cầu tài khoản người dùng hoặc kết nối internet (100% offline).
- Không có tính năng đồng bộ hóa hay backup lên đám mây (ví dụ: Google Drive) trong phiên bản hiện tại.
