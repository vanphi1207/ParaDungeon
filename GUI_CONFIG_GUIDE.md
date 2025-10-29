# ParaDungeon - Hướng Dẫn Cấu Hình GUI

## Tổng Quan

File `gui.yml` cho phép bạn tùy chỉnh toàn bộ giao diện GUI của plugin ParaDungeon mà không cần chỉnh sửa mã nguồn. Tất cả các menu, vật phẩm, màu sắc, vị trí slot, và văn bản đều có thể cấu hình.

## Cấu Trúc File

File `gui.yml` được chia thành các phần chính sau:

1. **main-menu** - Menu chính
2. **dungeon-list** - Danh sách phó bản
3. **dungeon-info** - Thông tin chi tiết phó bản
4. **leaderboard** - Bảng xếp hạng
5. **player-stats** - Thống kê người chơi
6. **settings** - Cài đặt
7. **reward-editor** - Quản lý phần thưởng (Admin)
8. **command-rewards** - Quản lý lệnh thưởng (Admin)

---

## Cú Pháp Cơ Bản

### Màu Sắc

Sử dụng ký tự `&` để định nghĩa màu sắc Minecraft:

```yaml
name: "&6&lTên Vàng Đậm"
lore:
  - "&7Dòng màu xám"
  - "&aDòng màu xanh lá"
  - "&c&lDòng đỏ đậm"
```

**Bảng mã màu phổ biến:**
- `&0` - Đen
- `&1` - Xanh đậm
- `&2` - Xanh lá đậm
- `&3` - Xanh lơ đậm
- `&4` - Đỏ đậm
- `&5` - Tím
- `&6` - Vàng
- `&7` - Xám
- `&8` - Xám đậm
- `&9` - Xanh
- `&a` - Xanh lá
- `&b` - Xanh lơ
- `&c` - Đỏ
- `&d` - Hồng
- `&e` - Vàng nhạt
- `&f` - Trắng

**Định dạng văn bản:**
- `&l` - Đậm
- `&m` - Gạch ngang
- `&n` - Gạch dưới
- `&o` - Nghiêng
- `&r` - Reset format

### Vật Liệu (Material)

Sử dụng tên vật phẩm Minecraft chính xác (phân biệt chữ hoa/thường):

```yaml
material: DIAMOND_SWORD
material: CHEST
material: PLAYER_HEAD
material: GRAY_STAINED_GLASS_PANE
```

Xem danh sách đầy đủ tại: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html

### Vị Trí Slot

Slot được đánh số từ 0 đến (size - 1):

```yaml
# GUI 27 slots (3 hàng):
# Slot 0-8: Hàng 1
# Slot 9-17: Hàng 2
# Slot 18-26: Hàng 3

# GUI 54 slots (6 hàng):
# Slot 0-8: Hàng 1
# Slot 9-17: Hàng 2
# ... và tiếp tục
# Slot 45-53: Hàng 6
```

**Slot âm** được tính từ cuối GUI:
```yaml
slot: -5  # Tương đương với slot (size - 5)
```

### Placeholders

Sử dụng `{placeholder}` để thay thế giá trị động:

```yaml
name: "&6&l{player_name}"  # Sẽ hiển thị tên người chơi
lore:
  - "&7Điểm: &6{score}"
  - "&7Hạng: &e#{rank}"
```

**Danh sách Placeholders phổ biến:**
- `{player}`, `{player_name}` - Tên người chơi
- `{score}`, `{high_score}` - Điểm số
- `{rank}` - Xếp hạng
- `{entries}` - Số lượt còn lại
- `{entries_color}` - Màu sắc dựa trên số lượt
- `{dungeon_name}` - Tên phó bản
- `{min_players}`, `{max_players}` - Số người chơi
- `{total_stages}` - Số ải
- `{respawn_lives}` - Số mạng
- `{count}` - Số lượng (dùng trong nhiều ngữ cảnh)
- `{command}` - Lệnh
- `{index}` - Chỉ số

---

## Hướng Dẫn Chi Tiết Từng Phần

### 1. Main Menu (Menu Chính)

```yaml
main-menu:
  title: "&8▎ &6&lParaDungeon Menu"
  size: 45
  filler:
    enabled: true
    material: GRAY_STAINED_GLASS_PANE
  
  items:
    dungeon-list:
      slot: 11
      material: DIAMOND_SWORD
      name: "&6&lDanh Sách Phó Bản"
      lore:
        - "&7Xem tất cả các phó bản"
        - "&7có thể tham gia"
        - ""
        - "&e▶ Nhấp để xem!"
```

**Giải thích:**
- `title`: Tiêu đề menu hiển thị ở đầu
- `size`: Kích thước GUI (9, 18, 27, 36, 45, 54)
- `filler.enabled`: Bật/tắt ô trống tự động
- `filler.material`: Vật liệu dùng để điền ô trống
- `items.dungeon-list.slot`: Vị trí của nút
- `lore`: Danh sách mô tả khi rê chuột

### 2. Dungeon List (Danh Sách Phó Bản)

```yaml
dungeon-list:
  title: "&8▎ &6&lChọn Phó Bản"
  min-size: 27
  
  icons:
    has-entries: DIAMOND_SWORD      # Còn lượt chơi
    has-boss: NETHERITE_SWORD        # Phó bản có boss
    no-entries: RED_WOOL             # Hết lượt chơi
  
  entries-colors:
    high: "&a"      # >= 3 lượt
    medium: "&e"    # 2 lượt
    low: "&c"       # 1 lượt
    none: "&4"      # 0 lượt
```

**Giải thích:**
- `icons`: Icon hiển thị dựa trên trạng thái phó bản
- `entries-colors`: Màu sắc hiển thị số lượt còn lại
- Plugin tự động chọn icon và màu phù hợp

### 3. Leaderboard (Bảng Xếp Hạng)

```yaml
leaderboard:
  rank-display:
    start-slot: 18
    
    rank-1:
      material: GOLD_BLOCK
      color: "&6&l"
      special: "&e⭐ Top 1!"
    rank-2:
      material: IRON_BLOCK
      color: "&7&l"
      special: "&e⭐ Top 2!"
    rank-3:
      material: COPPER_BLOCK
      color: "&c&l"
      special: "&e⭐ Top 3!"
    default:
      material: COAL_BLOCK
      color: "&f"
```

**Giải thích:**
- `rank-1`, `rank-2`, `rank-3`: Cấu hình riêng cho top 3
- `default`: Cấu hình cho các hạng còn lại
- `special`: Dòng đặc biệt chỉ hiển thị cho top 3

### 4. Reward Editor (Quản Lý Phần Thưởng)

```yaml
reward-editor:
  main:
    title: "&8▎ &6&lQuản Lý Phần Thưởng"
    size: 27
    items:
      completion-rewards:
        slot: 11
        material: CHEST
        name: "&6&lPhần Thưởng Hoàn Thành"
        lore:
          - "&7Phần thưởng khi hoàn thành"
          - "&7phó bản (không phụ thuộc điểm)"
          - ""
          - "&e▶ Click để chỉnh sửa!"
```

**Sub-menus:**
- `reward-editor.main` - Menu chính
- `reward-editor.completion-rewards` - Phần thưởng hoàn thành
- `reward-editor.score-rewards` - Phần thưởng theo điểm
- `reward-editor.score-tier-editor` - Chỉnh sửa mức điểm

### 5. Command Rewards (Quản Lý Lệnh)

```yaml
command-rewards:
  completion:
    items:
      info:
        slot: 4
        material: PAPER
        name: "&6&lHướng Dẫn Lệnh"
        lore:
          - "&7Thêm các lệnh sẽ được thực thi"
          - "&7khi người chơi hoàn thành phó bản"
          - ""
          - "&e✦ &7Placeholders:"
          - "  &e{player} &7- Tên người chơi"
          - "  &e{score} &7- Điểm số đạt được"
```

**Lưu ý:**
- `{player}` và `{score}` sẽ được thay thế tự động khi lệnh thực thi
- Lệnh không cần dấu `/` ở đầu (plugin tự thêm)

---

## Tùy Chỉnh Nâng Cao

### Filler với Phạm Vi Tùy Chỉnh

```yaml
completion-rewards:
  filler:
    enabled: true
    material: BLACK_STAINED_GLASS_PANE
    start-slot: 0
    end-slot: 17
```

**Giải thích:**
- Chỉ điền ô trống từ slot 0 đến 17
- Các slot khác (18-53) để trống cho người chơi đặt vật phẩm

### Slot Động với Giá Trị Âm

```yaml
back-button:
  slot: -5  # Tự động tính từ cuối GUI
```

**Ví dụ:**
- GUI 27 slots: `-5` = slot 22
- GUI 54 slots: `-5` = slot 49

### Lore Template với Điều Kiện

```yaml
lore-player:
  - "&7Dành cho người chơi"
  - "&e▶ Click để xem!"

lore-admin:
  - "&7Dành cho admin"
  - "&c▶ Click để quản lý!"
```

**Giải thích:**
- Plugin tự động chọn lore phù hợp dựa trên quyền của người chơi

---

## Các Lỗi Thường Gặp

### 1. GUI Không Mở

**Nguyên nhân:** Material không hợp lệ hoặc không tồn tại
```yaml
# SAI:
material: WRONG_MATERIAL_NAME

# ĐÚNG:
material: DIAMOND_SWORD
```

### 2. Màu Không Hiển Thị

**Nguyên nhân:** Dùng `§` thay vì `&`
```yaml
# SAI:
name: "§6§lTên"

# ĐÚNG:
name: "&6&lTên"
```

### 3. Slot Bị Trùng

**Nguyên nhân:** Nhiều item cùng slot
```yaml
# SAI:
item1:
  slot: 10
item2:
  slot: 10  # Trùng!

# ĐÚNG:
item1:
  slot: 10
item2:
  slot: 11
```

### 4. Placeholder Không Được Thay Thế

**Nguyên nhân:** Sai tên placeholder
```yaml
# SAI:
lore:
  - "&7Điểm: {diem}"  # Placeholder không tồn tại

# ĐÚNG:
lore:
  - "&7Điểm: {score}"
```

---

## Mẹo Tối Ưu

### 1. Sử Dụng Template Lore

Tái sử dụng lore cho nhiều menu:
```yaml
# Định nghĩa một lần
common-lore: &common
  - ""
  - "&e▶ Click để xem chi tiết!"

# Sử dụng nhiều lần
item1:
  lore:
    - "&7Mô tả item 1"
    <<: *common

item2:
  lore:
    - "&7Mô tả item 2"
    <<: *common
```

### 2. Tổ Chức Màu Sắc Nhất Quán

Tạo quy tắc màu cho toàn bộ plugin:
- `&6` - Tiêu đề chính
- `&e` - Thông tin quan trọng
- `&7` - Mô tả thường
- `&a` - Hành động tích cực
- `&c` - Hành động tiêu cực/cảnh báo

### 3. Backup Trước Khi Chỉnh Sửa

```bash
cp gui.yml gui.yml.backup
```

### 4. Reload Plugin Sau Khi Sửa

```
/paradungeon reload
```
hoặc
```
/reload confirm
```

---

## Ví Dụ Cấu Hình Hoàn Chỉnh

### Menu Tùy Chỉnh Hoàn Toàn

```yaml
main-menu:
  title: "&8[&6&lPARADUNGEON&8]"
  size: 45
  filler:
    enabled: true
    material: BLACK_STAINED_GLASS_PANE
  
  items:
    dungeon-list:
      slot: 20
      material: NETHERITE_SWORD
      name: "&6&l⚔ PHÓNG BẢN"
      lore:
        - "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        - ""
        - "&7Khám phá các phó bản"
        - "&7đầy thử thách!"
        - ""
        - "&e✦ Click để bắt đầu!"
        - ""
        - "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
    
    my-stats:
      slot: 22
      material: ENCHANTED_BOOK
      name: "&6&l📊 THỐNG KÊ"
      lore:
        - "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        - ""
        - "&7Xem điểm số và"
        - "&7thành tích của bạn"
        - ""
        - "&e✦ Click để xem!"
        - ""
        - "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
    
    leaderboard:
      slot: 24
      material: GOLDEN_HELMET
      name: "&6&l🏆 XẾP HẠNG"
      lore:
        - "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        - ""
        - "&7Top người chơi"
        - "&7xuất sắc nhất!"
        - ""
        - "&e✦ Click để xem!"
        - ""
        - "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
```

---

## Hỗ Trợ

Nếu gặp vấn đề với cấu hình:

1. Kiểm tra syntax YAML: https://www.yamllint.com/
2. Xem console log khi reload plugin
3. So sánh với file `gui.yml` mặc định
4. Liên hệ developer hoặc cộng đồng

---

## Changelog

### v1.0.0
- Phát hành hệ thống cấu hình GUI đầu tiên
- Hỗ trợ tùy chỉnh toàn bộ 8 loại menu
- Hỗ trợ placeholders động
- Hỗ trợ filler tùy chỉnh

---

**Lưu ý:** Plugin sẽ tự động tạo file `gui.yml` với cấu hình mặc định nếu file không tồn tại. Bạn có thể xóa file và reload plugin để reset về mặc định.
