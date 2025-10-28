package me.ihqqq.paraDungeon.gui;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.PlayerData;
import me.ihqqq.paraDungeon.models.Stage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final ParaDungeon plugin;
    private final RewardEditorGUI rewardEditorGUI;
    private final CommandRewardGUI commandRewardGUI;

    public static final String MAIN_MENU_TITLE = "§8▎ §6§lParaDungeon Menu";
    public static final String DUNGEON_LIST_TITLE = "§8▎ §6§lChọn Phó Bản";
    public static final String DUNGEON_INFO_TITLE = "§8▎ §6§lThông Tin Phó Bản";
    public static final String LEADERBOARD_TITLE = "§8▎ §6§lBảng Xếp Hạng";
    public static final String STATS_TITLE = "§8▎ §6§lThống Kê Của Bạn";
    public static final String SETTINGS_TITLE = "§8▎ §6§lCài Đặt";

    public GUIManager(ParaDungeon plugin) {
        this.plugin = plugin;
        this.rewardEditorGUI = new RewardEditorGUI(plugin);
        this.commandRewardGUI = new CommandRewardGUI(plugin);
    }

    public RewardEditorGUI getRewardEditorGUI() {
        return rewardEditorGUI;
    }

    public CommandRewardGUI getCommandRewardGUI() {
        return commandRewardGUI;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, MAIN_MENU_TITLE);

        ItemStack dungeonList = createItem(
                Material.DIAMOND_SWORD,
                "§6§lDanh Sách Phó Bản",
                "§7Xem tất cả các phó bản",
                "§7có thể tham gia",
                "",
                "§e▶ Nhấp để xem!"
        );
        gui.setItem(11, dungeonList);

        ItemStack stats = createItem(
                Material.BOOK,
                "§6§lThống Kê Của Tôi",
                "§7Xem điểm số và xếp hạng",
                "§7của bạn",
                "",
                "§e▶ Nhấp để xem!"
        );
        gui.setItem(13, stats);

        ItemStack leaderboard = createItem(
                Material.GOLDEN_HELMET,
                "§6§lBảng Xếp Hạng",
                "§7Top người chơi xuất sắc",
                "§7nhất server",
                "",
                "§e▶ Nhấp để xem!"
        );
        gui.setItem(15, leaderboard);

        ItemStack settings = createItem(
                Material.REDSTONE,
                "§6§lCài Đặt",
                "§7Tùy chỉnh trải nghiệm",
                "§7của bạn",
                "",
                "§e▶ Nhấp để xem!"
        );
        gui.setItem(31, settings);

        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openDungeonList(Player player) {
        List<Dungeon> dungeons = new ArrayList<>(plugin.getDungeonManager().getAllDungeons());
        int size = Math.min(54, ((dungeons.size() + 8) / 9) * 9);
        if (size < 27) size = 27;

        Inventory gui = Bukkit.createInventory(null, size, DUNGEON_LIST_TITLE);
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        for (int i = 0; i < dungeons.size() && i < 45; i++) {
            Dungeon dungeon = dungeons.get(i);
            Material icon = getDungeonIcon(dungeon, data);
            List<String> lore = new ArrayList<>();
            dungeon.getDescription().forEach(line -> lore.add("§7" + line.replace("&", "§")));
            lore.add("");
            lore.add("§8▎ §7Người chơi: §e" + dungeon.getMinPlayers() + "-" + dungeon.getMaxPlayers());
            lore.add("§8▎ §7Số ải: §e" + dungeon.getTotalStages());
            lore.add("§8▎ §7Số mạng: §e" + dungeon.getRespawnLives());
            lore.add("");

            int entries = data.getDungeonEntries(dungeon.getId());
            int highScore = data.getDungeonScore(dungeon.getId());
            int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());

            lore.add("§8▎ §6§lThống Kê Của Bạn:");
            lore.add("  §7Lượt còn lại: " + getEntriesColor(entries) + entries);
            lore.add("  §7Điểm cao nhất: §6" + highScore);
            if (rank != -1) {
                lore.add("  §7Xếp hạng: §e#" + rank);
            }
            lore.add("");

            if (entries > 0) {
                lore.add("§a§l▶ Click trái: §aTham gia");
            } else {
                lore.add("§c§l✖ Hết lượt chơi!");
            }
            lore.add("§e§l▶ Click phải: §eXem chi tiết");

            ItemStack item = createItemWithData(
                    icon,
                    dungeon.getDisplayName().replace("&", "§"),
                    dungeon.getId(),
                    lore.toArray(new String[0])
            );
            gui.setItem(i, item);
        }

        if (size > 27) {
            ItemStack back = createItem(Material.ARROW, "§c§lQuay Lại", "§7Về menu chính");
            gui.setItem(size - 5, back);
        }
        fillEmptySlots(gui, Material.BLACK_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openDungeonInfo(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, DUNGEON_INFO_TITLE);
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        ItemStack dungeonIcon = createItem(getDungeonIcon(dungeon, data), dungeon.getDisplayName().replace("&", "§"),
                dungeon.getDescription().stream().map(s -> "§7" + s.replace("&", "§")).toArray(String[]::new));
        gui.setItem(4, dungeonIcon);

        ItemStack generalInfo = createItem(Material.BOOK, "§6§lThông Tin Chung",
                "§7Người chơi: §e" + dungeon.getMinPlayers() + "-" + dungeon.getMaxPlayers(),
                "§7Số ải: §e" + dungeon.getTotalStages(), "§7Số mạng: §e" + dungeon.getRespawnLives(),
                "§7Lượt/ngày: §e" + dungeon.getEntriesPerReset());
        gui.setItem(19, generalInfo);

        int entries = data.getDungeonEntries(dungeon.getId());
        int highScore = data.getDungeonScore(dungeon.getId());
        int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());

        ItemStack yourStats = createItem(Material.PLAYER_HEAD, "§6§lThống Kê Của Bạn",
                "§7Lượt còn lại: " + getEntriesColor(entries) + entries, "§7Điểm cao nhất: §6" + highScore,
                rank != -1 ? "§7Xếp hạng: §e#" + rank : "§7Chưa có xếp hạng");
        gui.setItem(21, yourStats);

        ItemStack topPlayers = createItem(Material.GOLDEN_HELMET, "§6§lTop Người Chơi", "§7Xem bảng xếp hạng", "", "§e▶ Click để xem!");
        gui.setItem(23, topPlayers);

        ItemStack rewards = createItemWithData(Material.CHEST, "§6§lPhần Thưởng", "rewards_" + dungeon.getId(),
                "§7Hoàn thành phó bản để", "§7nhận phần thưởng hấp dẫn!", "", "§e⭐ Điểm càng cao,",
                "§e⭐ thưởng càng nhiều!", "", player.hasPermission("paradungeon.admin") ? "§c▶ Click để quản lý!" : "§e▶ Click để xem!");
        gui.setItem(25, rewards);

        if (entries > 0) {
            ItemStack joinBtn = createItemWithData(Material.LIME_WOOL, "§a§l✔ THAM GIA", "join_" + dungeon.getId(),
                    "§7Click để tham gia phó bản!", "", "§aLượt còn lại: §e" + entries);
            gui.setItem(49, joinBtn);
        } else {
            ItemStack noEntries = createItem(Material.RED_WOOL, "§c§l✖ HẾT LƯỢT", "§7Bạn đã hết lượt chơi!",
                    "§7Lượt chơi sẽ reset vào:", "§e" + getResetTimeString());
            gui.setItem(49, noEntries);
        }

        ItemStack back = createItem(Material.ARROW, "§c§lQuay Lại", "§7Về danh sách phó bản");
        gui.setItem(45, back);
        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openLeaderboard(Player player, String dungeonId) {
        Inventory gui = Bukkit.createInventory(null, 54, LEADERBOARD_TITLE);
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        var leaderboard = plugin.getLeaderboardManager().getLeaderboard(dungeonId);
        ItemStack title = createItem(Material.GOLDEN_HELMET, "§6§lBảng Xếp Hạng",
                "§e" + dungeon.getDisplayName().replace("&", "§"), "", "§7Top " + leaderboard.size() + " người chơi");
        gui.setItem(4, title);

        int slot = 18;
        for (int i = 0; i < Math.min(leaderboard.size(), 21); i++) {
            var entry = leaderboard.get(i);
            int rank = i + 1;
            Material material = getRankMaterial(rank);
            String rankColor = getRankColor(rank);

            ItemStack item = createItem(material, rankColor + "#" + rank + " §f" + entry.getPlayerName(),
                    "§7Điểm số: §6" + entry.getScore(), "", rank <= 3 ? "§e⭐ Top " + rank + "!" : "");
            gui.setItem(slot++, item);
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        int playerRank = plugin.getLeaderboardManager().getPlayerRank(dungeonId, player.getUniqueId());
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        ItemStack yourRank = createItem(Material.PLAYER_HEAD, "§6§lXếp Hạng Của Bạn",
                playerRank != -1 ? "§7Hạng: §e#" + playerRank : "§7Chưa có xếp hạng",
                "§7Điểm: §6" + data.getDungeonScore(dungeonId));
        gui.setItem(49, yourRank);

        ItemStack back = createItem(Material.ARROW, "§c§lQuay Lại", "§7Về thông tin phó bản");
        gui.setItem(45, back);
        fillEmptySlots(gui, Material.BLACK_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openPlayerStats(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, STATS_TITLE);
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName("§6§l" + player.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Thống kê tổng quan");
            skullMeta.setLore(lore);
            head.setItemMeta(skullMeta);
        }
        gui.setItem(4, head);

        int slot = 18;
        for (Dungeon dungeon : plugin.getDungeonManager().getAllDungeons()) {
            int entries = data.getDungeonEntries(dungeon.getId());
            int score = data.getDungeonScore(dungeon.getId());
            int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());
            ItemStack item = createItem(getDungeonIcon(dungeon, data), dungeon.getDisplayName().replace("&", "§"),
                    "§7Lượt còn lại: " + getEntriesColor(entries) + entries, "§7Điểm cao nhất: §6" + score,
                    rank != -1 ? "§7Xếp hạng: §e#" + rank : "§7Chưa có xếp hạng", "", "§e▶ Click để xem chi tiết!");

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(plugin.getDungeonKey(), PersistentDataType.STRING, dungeon.getId());
                item.setItemMeta(meta);
            }
            gui.setItem(slot++, item);
            if (slot >= 44) break;
        }

        ItemStack back = createItem(Material.ARROW, "§c§lQuay Lại", "§7Về menu chính");
        gui.setItem(49, back);
        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openSettings(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, SETTINGS_TITLE);
        // Implement settings toggles here based on PlayerData
        ItemStack particles = createItem(Material.BLAZE_POWDER, "§6§lHiệu Ứng Particles", "§7Bật/tắt hiệu ứng hạt", "", "§eComing soon...");
        gui.setItem(11, particles);

        ItemStack sounds = createItem(Material.NOTE_BLOCK, "§6§lÂm Thanh", "§7Bật/tắt âm thanh", "", "§eComing soon...");
        gui.setItem(13, sounds);

        ItemStack notifications = createItem(Material.BELL, "§6§lThông Báo", "§7Tùy chỉnh thông báo", "", "§eComing soon...");
        gui.setItem(15, notifications);

        ItemStack back = createItem(Material.ARROW, "§c§lQuay Lại", "§7Về menu chính");
        gui.setItem(22, back);
        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    if (!line.isEmpty()) loreList.add(line);
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItemWithData(Material material, String name, String data, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.getDungeonKey(), PersistentDataType.STRING, data);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillEmptySlots(Inventory gui, Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    private Material getDungeonIcon(Dungeon dungeon, PlayerData data) {
        int entries = data.getDungeonEntries(dungeon.getId());
        if (entries <= 0) return Material.RED_WOOL;
        boolean hasBoss = dungeon.getStages().values().stream().anyMatch(Stage::isBoss);
        if (hasBoss) return Material.NETHERITE_SWORD;
        return Material.DIAMOND_SWORD;
    }

    private String getEntriesColor(int entries) {
        if (entries >= 3) return "§a";
        if (entries == 2) return "§e";
        if (entries == 1) return "§c";
        return "§4";
    }

    private Material getRankMaterial(int rank) {
        return switch (rank) {
            case 1 -> Material.GOLD_BLOCK;
            case 2 -> Material.IRON_BLOCK;
            case 3 -> Material.COPPER_BLOCK;
            default -> Material.COAL_BLOCK;
        };
    }

    private String getRankColor(int rank) {
        return switch (rank) {
            case 1 -> "§6§l";
            case 2 -> "§7§l";
            case 3 -> "§c§l";
            default -> "§f";
        };
    }

    private String getResetTimeString() {
        String resetType = plugin.getConfig().getString("settings.entry-system.reset-type", "DAILY");
        if (resetType.equalsIgnoreCase("DAILY")) {
            return "Ngày mai (00:00)";
        } else {
            int hours = plugin.getConfig().getInt("settings.entry-system.reset-hours", 24);
            return hours + " giờ nữa";
        }
    }
}