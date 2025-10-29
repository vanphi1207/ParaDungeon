# ParaDungeon

## Cấu hình GUI (gui.yml)

Từ phiên bản này, toàn bộ giao diện (GUI) của ParaDungeon đã được tách khỏi source và có thể cấu hình 100% qua file `gui.yml` trong thư mục plugin.

- Hỗ trợ đổi tiêu đề mọi GUI
- Tùy chỉnh kích thước (bội số của 9), icon, tên, lore, vị trí các nút
- Tùy chỉnh vật liệu nền (filler)
- Hỗ trợ placeholder động trong lore: `{entries}`, `{highScore}`, `{rank}`, `{score}`, `{resetTime}`, `{dungeonDisplayName}`, v.v.
- Màu sử dụng ký tự `&` (tự động chuyển sang §)
- Reload nhanh không cần restart server: `/dgadmin reload`

### Vị trí file
- Mặc định được tạo tại: `plugins/ParaDungeon/gui.yml`
- Mẫu cài sẵn cũng nằm trong `src/main/resources/gui.yml` (dành cho developer)

### Danh sách mục chính trong gui.yml
- `titles.*`: Tiêu đề các cửa sổ GUI
- `main_menu.*`: Menu chính (kích thước, filler, các nút và vị trí)
- `dungeon_list.*`: Danh sách phó bản (filler, nhãn thống kê, icon theo trạng thái)
- `dungeon_info.*`: Thông tin phó bản (các ô info, top, phần thưởng, tham gia/quay lại)
- `leaderboard.*`: Bảng xếp hạng (vật liệu/ màu theo hạng, nút quay lại)
- `player_stats.*`: Thống kê cá nhân (filler, nút quay lại)
- `settings.*`: Menu cài đặt (demo, có thể mở rộng)
- `reward_editor.*`: Toàn bộ GUI chỉnh sửa phần thưởng (menu chính, theo completion, theo điểm, chỉnh từng mốc)
- `command_rewards.*`: Toàn bộ GUI quản lý lệnh thưởng (completion và theo mốc điểm)

Mỗi phần đều có ví dụ chi tiết và chú thích ngay trong `gui.yml` (đã thêm comment đầy đủ).

### Placeholder phổ biến trong lore
- `{entries}`: Lượt còn lại của người chơi
- `{highScore}`: Điểm cao nhất của người chơi
- `{rank}` / `{rankLine}`: Xếp hạng hoặc dòng “chưa có xếp hạng”
- `{score}`: Mốc điểm hiện tại (ở GUI theo điểm)
- `{resetTime}`: Chuỗi thời gian reset lượt (theo cấu hình `settings.entry-system`)
- `{dungeonDisplayName}`: Tên hiển thị của phó bản

### Ví dụ nhanh: đổi icon và tên nút “Bảng Xếp Hạng” ở menu chính
```yml
main_menu:
  items:
    leaderboard:
      slot: 15
      material: NETHER_STAR
      name: "&b&lBXH Máy Chủ"
      lore:
        - "&7Top người chơi xuất sắc"
        - "&7nhất server"
        - ""
        - "&e▶ Nhấp để xem!"
```

### Reload cấu hình
- Thực hiện lệnh: `/dgadmin reload`
- Lệnh sẽ tải lại `config.yml`, `messages.yml`, `dungeons.yml` và `gui.yml`

### Ghi chú triển khai
- Toàn bộ GUI không còn hardcode trong source, đều đọc từ `gui.yml`
- Hệ thống lắng nghe (listeners) nhận diện GUI theo tiêu đề cấu hình
- Các nút hành động dùng PersistentDataContainer để đảm bảo không phụ thuộc tên hiển thị

Nếu bạn gặp lỗi khi chỉnh sửa YAML, hãy kiểm tra khoảng trắng (không dùng tab) và giá trị `Material` có hợp lệ với phiên bản server.
