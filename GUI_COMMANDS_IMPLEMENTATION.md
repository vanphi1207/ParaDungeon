# Tách Các GUI Liên Quan Đến Cài Đặt Dungeon Ra Thành Các Lệnh Riêng

## Tổng Quan

Đã thêm 3 lệnh mới vào `/dgadmin` để mở trực tiếp các GUI cài đặt dungeon, giúp quản trị viên truy cập nhanh hơn vào các chức năng chỉnh sửa mà không cần phải đi qua nhiều menu.

## Các Lệnh Mới Được Thêm

### 1. `/dgadmin edit <dungeon>`
- **Chức năng**: Mở menu chính để chỉnh sửa phần thưởng của dungeon
- **GUI được mở**: Reward Editor Main Menu (openRewardMenu)
- **Nội dung menu**:
  - Phần thưởng hoàn thành (Completion Rewards)
  - Phần thưởng theo điểm (Score-based Rewards)
  - Xem trước phần thưởng (Preview)

### 2. `/dgadmin addreward <dungeon>`
- **Chức năng**: Mở trực tiếp GUI để thêm/chỉnh sửa phần thưởng hoàn thành dungeon
- **GUI được mở**: Completion Rewards Editor (openCompletionRewardsEditor)
- **Nội dung**:
  - Cho phép đặt vật phẩm phần thưởng (slots 18-44)
  - Thêm lệnh phần thưởng (commands)
  - Lưu và quay lại

### 3. `/dgadmin editscore <dungeon>`
- **Chức năng**: Mở trực tiếp GUI để chỉnh sửa phần thưởng theo điểm
- **GUI được mở**: Score Rewards Editor (openScoreRewardsEditor)
- **Nội dung**:
  - Xem danh sách các mốc điểm hiện có
  - Thêm mốc điểm mới
  - Chỉnh sửa/xóa mốc điểm
  - Click trái để chỉnh sửa, click phải để xóa

## Các File Đã Sửa Đổi

### 1. `DungeonAdminCommand.java`
#### Thêm các case mới trong switch statement:
```java
case "edit":
    // Kiểm tra player only
    // Kiểm tra đủ arguments
    // Gọi openRewardEditor()

case "addreward":
    // Kiểm tra player only
    // Kiểm tra đủ arguments
    // Gọi openAddReward()

case "editscore":
    // Kiểm tra player only
    // Kiểm tra đủ arguments
    // Gọi openEditScore()
```

#### Thêm 3 methods mới:
```java
private void openRewardEditor(Player player, String dungeonId)
private void openAddReward(Player player, String dungeonId)
private void openEditScore(Player player, String dungeonId)
```

#### Cập nhật tab completion:
- Thêm "edit", "addreward", "editscore" vào danh sách suggestions

### 2. `messages.yml`
#### Thêm usage messages:
```yaml
admin:
  usage:
    edit: "&cSử dụng: /dgadmin edit <dungeon>"
    addreward: "&cSử dụng: /dgadmin addreward <dungeon>"
    editscore: "&cSử dụng: /dgadmin editscore <dungeon>"
```

#### Cập nhật help messages:
```yaml
help:
  admin:
    - "&c/dgadmin edit <tên> &7- Mở menu chỉnh sửa phần thưởng."
    - "&c/dgadmin addreward <tên> &7- Thêm phần thưởng hoàn thành phó bản."
    - "&c/dgadmin editscore <tên> &7- Chỉnh sửa phần thưởng theo điểm."
```

## Cách Sử Dụng

### Ví Dụ Thực Tế:

1. **Chỉnh sửa phần thưởng tổng quát**:
   ```
   /dgadmin edit mydungeon
   ```
   → Mở menu chính để chọn loại phần thưởng cần chỉnh sửa

2. **Thêm phần thưởng hoàn thành nhanh**:
   ```
   /dgadmin addreward mydungeon
   ```
   → Mở ngay GUI để đặt vật phẩm và lệnh phần thưởng hoàn thành

3. **Chỉnh sửa phần thưởng theo điểm**:
   ```
   /dgadmin editscore mydungeon
   ```
   → Mở GUI để quản lý các mốc điểm và phần thưởng tương ứng

## Lợi Ích

1. **Truy cập nhanh hơn**: Không cần phải đi qua menu `/dg info <dungeon>` → click vào rewards item
2. **Tiện lợi cho quản trị viên**: Có thể sử dụng lệnh trực tiếp từ console hoặc command blocks
3. **Tổ chức tốt hơn**: Các lệnh rõ ràng cho từng chức năng cụ thể
4. **Tab completion**: Hỗ trợ gợi ý lệnh và tên dungeon

## Kiểm Tra Lỗi

Tất cả các lệnh đều có:
- ✅ Kiểm tra quyền admin (paradungeon.admin)
- ✅ Kiểm tra player only (không cho console nếu cần mở GUI)
- ✅ Kiểm tra đủ arguments
- ✅ Kiểm tra dungeon tồn tại
- ✅ Thông báo lỗi rõ ràng bằng tiếng Việt

## Tương Thích

- ✅ Không ảnh hưởng đến các lệnh hiện có
- ✅ Sử dụng cùng hệ thống GUI đã có
- ✅ Tương thích với hệ thống messages.yml
- ✅ Tab completion hoạt động đầy đủ

## Ghi Chú

- Các GUI được mở vẫn giống như khi truy cập từ menu info
- Tất cả chức năng chỉnh sửa, lưu, xóa đều hoạt động như trước
- Listener hiện có (`RewardGUIListener`) xử lý tất cả các tương tác GUI
