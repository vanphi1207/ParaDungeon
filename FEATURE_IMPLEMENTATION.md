# Tính Năng Mới - ParaDungeon

## Tổng Quan
Đã triển khai các tính năng mới cho hệ thống phòng chơi (dungeon rooms):

### 1. Yêu Cầu Số Người Chơi Tối Thiểu ✅
- **Mô tả**: Phòng chơi chỉ bắt đầu khi đủ số người chơi tối thiểu
- **Cách hoạt động**: 
  - Khi người chơi join phòng, hệ thống kiểm tra số lượng người chơi
  - Nếu đủ số người tối thiểu (`min-players` trong config), countdown sẽ tự động bắt đầu
  - Nếu người chơi rời khỏi phòng và số lượng giảm xuống dưới mức tối thiểu, countdown sẽ bị hủy
- **Vị trí code**: `RoomManager.java` - methods `joinRoom()` và `leaveRoom()`

### 2. Hệ Thống Auto-Renewal ✅
- **Mô tả**: Tự động kick toàn bộ người chơi ra khỏi phòng nếu phòng không bắt đầu sau một khoảng thời gian nhất định
- **Cách hoạt động**:
  - Khi phòng được tạo, thời gian tạo (`creationTime`) được lưu lại
  - Task tự động chạy mỗi phút để kiểm tra các phòng đang ở trạng thái WAITING
  - Nếu phòng không bắt đầu sau thời gian cấu hình (mặc định 10 phút), tất cả người chơi sẽ bị kick
  - Thời gian có thể tùy chỉnh trong `config.yml` qua tham số `settings.auto-renew-time` (đơn vị: phút)
- **Vị trí code**: 
  - `RoomManager.java` - method `checkAutoRenew()`
  - `ParaDungeon.java` - task scheduler (dòng 114-116)
  - `config.yml` - cấu hình `settings.auto-renew-time: 10`

### 3. Lệnh Force Start Cho Admin ✅
- **Mô tả**: Admin có thể bắt đầu cưỡng chế phòng chơi mà không cần đủ số người chơi tối thiểu
- **Cú pháp**: `/dgadmin forcestart <tên_phó_bản>`
- **Quyền hạn**: Yêu cầu permission `paradungeon.admin`
- **Cách hoạt động**:
  - Tìm phòng đang ở trạng thái WAITING hoặc COUNTDOWN của dungeon được chỉ định
  - Bỏ qua kiểm tra số người chơi tối thiểu
  - Hủy countdown (nếu đang chạy) và bắt đầu dungeon ngay lập tức
  - Thông báo kết quả cho admin
- **Vị trí code**: 
  - `DungeonAdminCommand.java` - method `forceStart()`
  - `RoomManager.java` - method `forceStartRoom()`

## Cấu Hình

### config.yml
```yaml
settings:
  # Thời gian tự động làm mới phòng khi chưa start (phút)
  auto-renew-time: 10
  
  lobby:
    # Số người chơi tối thiểu để bắt đầu
    min-players: 1
    # Số người chơi tối đa
    max-players: 5
    # Thời gian đếm ngược trước khi bắt đầu (giây)
    countdown-time: 10
```

### Tin Nhắn (messages.yml)
Đã thêm các tin nhắn mới:
- `lobby.auto-renew`: Thông báo khi phòng bị tự động làm mới
- `admin.force-start-success`: Thông báo khi force start thành công
- `admin.force-start-failed`: Thông báo khi force start thất bại
- `admin.usage.forcestart`: Hướng dẫn sử dụng lệnh forcestart

## Luồng Hoạt Động

### Kịch Bản 1: Người chơi join và đủ số lượng
1. Người chơi A join phòng (min_players = 2)
2. Người chơi B join phòng → Đủ 2 người
3. Countdown tự động bắt đầu (10 giây mặc định)
4. Dungeon bắt đầu sau khi countdown kết thúc

### Kịch Bản 2: Người chơi rời đi trong countdown
1. Phòng có 2 người, countdown đang chạy
2. Người chơi A rời phòng → Còn 1 người (< min_players)
3. Countdown bị hủy
4. Phòng quay về trạng thái WAITING

### Kịch Bản 3: Auto-renewal
1. Người chơi A join phòng nhưng không đủ người
2. Phòng ở trạng thái WAITING
3. Sau 10 phút (hoặc thời gian cấu hình), hệ thống tự động:
   - Gửi thông báo cho tất cả người chơi trong phòng
   - Kick tất cả người chơi ra khỏi phòng
   - Teleport người chơi về end point hoặc spawn

### Kịch Bản 4: Admin force start
1. Phòng có 1 người (< min_players = 2)
2. Admin dùng lệnh: `/dgadmin forcestart <dungeon_id>`
3. Dungeon bắt đầu ngay lập tức mà không cần đợi thêm người

## Lệnh Admin Mới

### /dgadmin forcestart <dungeon>
- **Mô tả**: Bắt đầu cưỡng chế phó bản (bỏ qua yêu cầu số người)
- **Permission**: `paradungeon.admin`
- **Ví dụ**: `/dgadmin forcestart easy_dungeon`
- **Tab completion**: Có hỗ trợ auto-complete danh sách dungeons

## Files Đã Chỉnh Sửa

1. **RoomManager.java**
   - Thêm method `forceStartRoom(String dungeonId)`
   - Cập nhật method `startDungeon()` để hủy countdown nếu đang chạy
   - Method `checkAutoRenew()` đã có sẵn và hoạt động đúng

2. **DungeonAdminCommand.java**
   - Thêm case `forcestart` trong switch statement
   - Thêm method `forceStart(CommandSender, String)`
   - Cập nhật tab completion để bao gồm `forcestart`

3. **messages.yml**
   - Thêm tin nhắn cho force start
   - Cập nhật help messages cho admin

4. **config.yml**
   - Đã có cấu hình `auto-renew-time` (mặc định 10 phút)

5. **ParaDungeon.java**
   - Task scheduler cho auto-renewal đã được triển khai (chạy mỗi phút)

## Kiểm Tra

### Để test các tính năng:

1. **Test yêu cầu số người tối thiểu**:
   ```
   - Join phòng với 1 người (min = 2) → Không có countdown
   - Người thứ 2 join → Countdown tự động bắt đầu
   - Người thứ 2 leave → Countdown bị hủy
   ```

2. **Test auto-renewal**:
   ```
   - Đặt auto-renew-time = 1 trong config.yml
   - Join phòng với 1 người (< min_players)
   - Đợi 1 phút → Người chơi bị kick tự động
   ```

3. **Test force start**:
   ```
   - Join phòng với 1 người (< min_players)
   - Dùng lệnh: /dgadmin forcestart <dungeon_id>
   - Dungeon nên bắt đầu ngay lập tức
   ```

## Lưu Ý

- Auto-renewal chỉ áp dụng cho phòng đang ở trạng thái WAITING (chưa bắt đầu countdown)
- Force start yêu cầu ít nhất 1 người trong phòng
- Thời gian auto-renewal được tính bằng phút và có thể tùy chỉnh trong config
- Tất cả các tính năng đều có thông báo rõ ràng cho người chơi và admin
