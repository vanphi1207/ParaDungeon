# Tính năng Auto-Renew Time cho từng Phó Bản

## Mô tả
Đã thêm khả năng cấu hình thời gian auto-renew (tự động làm mới phòng) riêng biệt cho từng phó bản khác nhau.

## Các thay đổi

### 1. Model - Dungeon.java
- **Thêm field mới**: `autoRenewTime` (int) - Thời gian auto-renew tính bằng phút
  - Giá trị `-1`: Sử dụng cài đặt global trong config.yml
  - Giá trị `> 0`: Sử dụng thời gian riêng cho phó bản này
- **Thêm getter/setter**: `getAutoRenewTime()` và `setAutoRenewTime(int)`
- **Giá trị mặc định**: -1 (dùng global setting)

### 2. DungeonManager.java
#### Phương thức loadDungeon():
```java
dungeon.setAutoRenewTime(section.getInt("auto-renew-time", -1));
```
- Load giá trị `auto-renew-time` từ config dungeons.yml
- Mặc định là -1 nếu không có trong config

#### Phương thức saveDungeon():
```java
if (dungeon.getAutoRenewTime() != -1) {
    dungeonSection.set("auto-renew-time", dungeon.getAutoRenewTime());
}
```
- Lưu giá trị `auto-renew-time` vào config (chỉ khi khác -1)

### 3. RoomManager.java
#### Phương thức checkAutoRenew():
- **Cải tiến logic**: Kiểm tra xem dungeon có setting riêng không
- **Ưu tiên**: Dungeon-specific > Global setting
- **Chi tiết**:
  ```java
  int dungeonAutoRenew = room.getDungeon().getAutoRenewTime();
  if (dungeonAutoRenew == -1) {
      autoRenewTime = globalAutoRenewTime;  // Dùng global
  } else {
      autoRenewTime = dungeonAutoRenew * 60000L;  // Dùng dungeon-specific
  }
  ```

### 4. Config - dungeons.yml
Thêm tùy chọn mới trong cấu hình phó bản:
```yaml
dungeons:
  example_dungeon:
    # ... các config khác ...
    
    # Thời gian tự động làm mới phòng (phút)
    # Nếu không set hoặc set -1, sẽ dùng giá trị global trong config.yml
    # Nếu set giá trị > 0, phòng sẽ tự động đóng sau X phút nếu chưa bắt đầu
    auto-renew-time: 15
```

## Cách sử dụng

### 1. Dùng cài đặt global (cho tất cả phó bản)
Trong `config.yml`:
```yaml
settings:
  auto-renew-time: 10  # 10 phút cho tất cả
```

Trong `dungeons.yml` - **KHÔNG** thêm `auto-renew-time` hoặc set `-1`:
```yaml
dungeons:
  dungeon1:
    # auto-renew-time: -1  # hoặc không có dòng này
```

### 2. Dùng cài đặt riêng cho từng phó bản
Trong `dungeons.yml`:
```yaml
dungeons:
  dungeon_easy:
    auto-renew-time: 5    # Phòng tự đóng sau 5 phút chờ
    
  dungeon_hard:
    auto-renew-time: 20   # Phòng tự đóng sau 20 phút chờ
    
  dungeon_normal:
    # Không set, sẽ dùng giá trị global (10 phút)
```

## Ưu điểm
- ✅ Linh hoạt cấu hình theo từng loại phó bản
- ✅ Tương thích ngược (backward compatible)
- ✅ Tự động fallback về global setting
- ✅ Dễ dàng quản lý và bảo trì

## Ví dụ thực tế
```yaml
dungeons:
  # Phó bản dễ - tự đóng nhanh (5 phút)
  beginner_dungeon:
    display-name: "&aPhó Bản Mới Bắt Đầu"
    auto-renew-time: 5
    
  # Phó bản thường - thời gian trung bình (10 phút - global)
  normal_dungeon:
    display-name: "&ePhó Bản Thường"
    # Không set, dùng global 10 phút
    
  # Phó bản khó - cho nhiều thời gian hơn (30 phút)
  hard_dungeon:
    display-name: "&cPhó Bản Khó"
    auto-renew-time: 30
    
  # Phó bản raid - thời gian dài (60 phút)
  raid_dungeon:
    display-name: "&4Phó Bản Raid"
    auto-renew-time: 60
```

## Lưu ý
- Thời gian tính bằng **phút** trong config
- Giá trị `-1` hoặc không set = dùng global setting
- Giá trị `> 0` = dùng setting riêng cho dungeon đó
- Chỉ áp dụng cho phòng đang ở trạng thái `WAITING` (chưa bắt đầu)
