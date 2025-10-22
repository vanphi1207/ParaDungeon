# ParaDungeon Plugin

Plugin Minecraft Dungeon (Phó Bản) hoàn chỉnh với tích hợp MythicMobs và PlaceholderAPI.

## Tính Năng

### ✅ Đã Hoàn Thành Tất Cả Yêu Cầu

1. **Khởi tạo và thiết lập phó bản ngay trong Game**
   - Tạo phó bản mới với lệnh `/dgadmin create <tên>`
   - Cấu hình hoàn toàn trong game
   - Không cần restart server

2. **Quái và BOSS phó bản sử dụng từ MythicMobs**
   - Hoàn toàn tích hợp MythicMobs
   - Spawn quái từ MythicMobs ID
   - Hỗ trợ mọi mob của MythicMobs

3. **Bảng xếp hạng điểm phó bản của người chơi**
   - Leaderboard tự động cập nhật
   - Lưu điểm cao nhất
   - Xem ranking với `/dungeon top <tên>`

4. **10 ải đánh quái và 01 ải đánh BOSS**
   - Hỗ trợ tối đa 11 ải (10 normal + 1 boss)
   - Mỗi ải có thể có nhiều đợt quái (waves)
   - Custom được số lượng và loại quái mỗi đợt

5. **Hệ thống phần thưởng**
   - Phần thưởng hoàn thành dungeon
   - Phần thưởng theo điểm số
   - Hỗ trợ items và commands

6. **Lượt tham gia tự động cấp**
   - Reset theo ngày hoặc giờ (tùy chỉnh)
   - Số lượt mặc định có thể config
   - Tự động reset vào thời gian đã định

7. **Hệ thống hồi sinh**
   - Cooldown hồi sinh (tùy chỉnh)
   - Trạng thái an toàn sau khi hồi sinh
   - Giới hạn số lượt hồi sinh
   - Thua khi hết lượt hồi sinh

8. **Command Aliases**
   - Tùy chỉnh aliases trong config.yml
   - Mặc định: `/dg`, `/phobans`, `/pb`
   - Admin: `/dgadmin`, `/phobanadmin`, `/pbadmin`

9. **PlaceholderAPI đa dạng**
   - Hơn 20 placeholders
   - Hỗ trợ GUI và Scoreboard
   - Real-time updates

10. **Auto Renew**
    - Tự động kick người chơi sau thời gian chờ
    - Mặc định 10 phút (có thể tùy chỉnh)
    - Giải phóng phòng tự động

## Cài Đặt

1. Tải file JAR
2. Đặt vào thư mục `plugins/`
3. Cài đặt dependencies:
   - **MythicMobs** (bắt buộc)
   - **PlaceholderAPI** (tùy chọn)
4. Khởi động server
5. Cấu hình trong `plugins/ParaDungeon/`

## Commands

### Người Chơi
```
/dungeon list - Xem danh sách phó bản
/dungeon join <tên> - Tham gia phó bản
/dungeon leave - Rời phó bản
/dungeon top [tên] - Xem bảng xếp hạng
/dungeon info <tên> - Thông tin phó bản
```

### Admin
```
/dgadmin create <tên> - Tạo phó bản mới
/dgadmin delete <tên> - Xóa phó bản
/dgadmin addstage <dungeon> <số> [NORMAL|BOSS] - Thêm ải
/dgadmin addwave <dungeon> <ải> <đợt> <mob> <số lượng> - Thêm đợt quái
/dgadmin setspawn <dungeon> - Đặt điểm spawn
/dgadmin setlobby <dungeon> - Đặt điểm lobby
/dgadmin reload - Tải lại config
```

## Permissions

```yaml
paradungeon.admin - Quyền admin (mặc định: op)
```

## PlaceholderAPI

### Placeholders Cơ Bản
```
%paradungeon_in_dungeon% - Có trong dungeon không (true/false)
%paradungeon_current_dungeon% - Tên dungeon hiện tại
%paradungeon_current_stage% - Ải hiện tại
%paradungeon_current_wave% - Đợt hiện tại
%paradungeon_lives% - Số mạng còn lại
%paradungeon_score% - Điểm hiện tại
%paradungeon_safe_mode% - Đang trong safe mode (true/false)
```

### Placeholders Phòng
```
%paradungeon_room_status% - Trạng thái phòng
%paradungeon_room_players% - Số người trong phòng
%paradungeon_room_max_players% - Số người tối đa
```

### Placeholders Theo Dungeon
```
%paradungeon_entries_<dungeon>% - Lượt chơi còn lại
%paradungeon_highscore_<dungeon>% - Điểm cao nhất
%paradungeon_rank_<dungeon>% - Xếp hạng
```

### Placeholders Leaderboard
```
%paradungeon_top_<dungeon>_<vị trí>_player% - Tên người chơi
%paradungeon_top_<dungeon>_<vị trí>_score% - Điểm số
```

Ví dụ:
```
%paradungeon_top_example_dungeon_1_player% - Tên top 1
%paradungeon_top_example_dungeon_1_score% - Điểm top 1
```

## Cấu Hình

### config.yml

Cấu hình chính của plugin:
- Auto-renew time
- Entry system (DAILY/HOURLY)
- Respawn system
- Lobby settings
- Leaderboard settings
- Database settings (SQLITE/MYSQL)

### dungeons.yml

Định nghĩa các phó bản:
- Display name và description
- Min/max players
- Entries per reset
- Respawn lives
- Spawn và lobby points
- Stages và waves
- Rewards

### messages.yml

Tất cả messages của plugin (tiếng Việt)

## Hướng Dẫn Tạo Dungeon

### Bước 1: Tạo Dungeon
```
/dgadmin create my_dungeon
```

### Bước 2: Đặt Spawn và Lobby
```
/dgadmin setspawn my_dungeon
/dgadmin setlobby my_dungeon
```

### Bước 3: Thêm Ải (tối thiểu 3 ải)
```
/dgadmin addstage my_dungeon 1 NORMAL
/dgadmin addstage my_dungeon 2 NORMAL
/dgadmin addstage my_dungeon 3 NORMAL
```

### Bước 4: Thêm Đợt Quái
```
/dgadmin addwave my_dungeon 1 1 SkeletonKing 5
/dgadmin addwave my_dungeon 1 2 SkeletonKing 10
```

### Bước 5: Thêm Boss (tùy chọn)
```
/dgadmin addstage my_dungeon 11 BOSS
/dgadmin addwave my_dungeon 11 1 FinalBoss 1
```

### Bước 6: Cấu Hình Phần Thưởng

Chỉnh sửa trong `dungeons.yml`:
```yaml
dungeons:
  my_dungeon:
    rewards:
      completion:
        items:
          - "DIAMOND 5"
          - "EMERALD 10"
        commands:
          - "eco give {player} 1000"
      score-based:
        1000:
          items:
            - "DIAMOND 10"
```

## Hệ Thống Điểm

- Mỗi quái tiêu diệt: +10 điểm
- Có thể custom thêm trong code
- Điểm cao nhất được lưu vào database
- Hiển thị trên leaderboard

## Database

### SQLite (Mặc định)
- Tự động tạo file `database.db`
- Không cần cấu hình thêm
- Phù hợp server nhỏ/vừa

### MySQL (Tùy chọn)
Cấu hình trong `config.yml`:
```yaml
database:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: paradungeon
    username: root
    password: password
```

## Ví Dụ Sử Dụng với DeluxeMenus

```yaml
# Menu chọn dungeon
example_menu:
  item:
    material: DIAMOND_SWORD
    display_name: '%paradungeon_entries_example_dungeon% lượt'
    lore:
      - '&7Điểm cao nhất: &e%paradungeon_highscore_example_dungeon%'
      - '&7Xếp hạng: &e%paradungeon_rank_example_dungeon%'
    left_click_commands:
      - '[player] dungeon join example_dungeon'
```

## Ví Dụ Scoreboard

```yaml
# ScoreboardAPI/FeatherBoard
lines:
  - '&6&lDungeon Info'
  - '&7Dungeon: &e%paradungeon_current_dungeon%'
  - '&7Stage: &e%paradungeon_current_stage%'
  - '&7Wave: &e%paradungeon_current_wave%'
  - '&7Lives: &c%paradungeon_lives%'
  - '&7Score: &a%paradungeon_score%'
```

## API cho Developers

```java
// Get plugin instance
ParaDungeon plugin = ParaDungeon.getInstance();

// Get player data
PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

// Get player's room
DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());

// Check if in dungeon
boolean inDungeon = data.isInDungeon();

// Get score
int score = data.getDungeonScore("dungeon_id");

// Get leaderboard
List<LeaderboardEntry> leaderboard = plugin.getLeaderboardManager().getLeaderboard("dungeon_id");
```

## Troubleshooting

### MythicMobs không spawn quái
- Kiểm tra MythicMobs đã cài đúng chưa
- Kiểm tra tên mob trong config có đúng không
- Xem console có lỗi không

### Không vào được dungeon
- Kiểm tra đã set spawn và lobby chưa
- Kiểm tra có đủ 3 ải không
- Xem lại lượt chơi còn không

### Database lỗi
- Kiểm tra quyền ghi file (SQLite)
- Kiểm tra kết nối MySQL
- Xem log trong console

## Yêu Cầu

- **Minecraft**: 1.21+
- **Java**: 21+
- **MythicMobs**: 5.6.1+
- **PlaceholderAPI**: 2.11.5+ (tùy chọn)

## Build

```bash
mvn clean package
```

File JAR sẽ được tạo trong `target/ParaDungeon-1.0.0.jar`

## License

MIT License

## Author

ihqqq

## Hỗ Trợ

Nếu có lỗi hoặc câu hỏi, vui lòng tạo issue trên GitHub.

---

**Chúc bạn sử dụng plugin vui vẻ! 🎮**
#   P a r a D u n g e o n  
 