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

    private static final String ACTION_OPEN_DUNGEON_LIST = "action:open_dungeon_list";
    private static final String ACTION_OPEN_STATS = "action:open_player_stats";
    private static final String ACTION_OPEN_LEADERBOARD = "action:open_leaderboard";
    private static final String ACTION_OPEN_SETTINGS = "action:open_settings";

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
        int size = plugin.getGUIConfigManager().getInt("main_menu.size", 45);
        Inventory gui = Bukkit.createInventory(null, size, plugin.getGUIConfigManager().titleMainMenu());

        // Dungeon list item
        setConfiguredMenuItem(gui, "main_menu.items.dungeon_list", ACTION_OPEN_DUNGEON_LIST, 11);
        // Stats item
        setConfiguredMenuItem(gui, "main_menu.items.stats", ACTION_OPEN_STATS, 13);
        // Leaderboard item
        setConfiguredMenuItem(gui, "main_menu.items.leaderboard", ACTION_OPEN_LEADERBOARD, 15);
        // Settings item
        setConfiguredMenuItem(gui, "main_menu.items.settings", ACTION_OPEN_SETTINGS, 31);

        Material filler = plugin.getGUIConfigManager().getMaterial("main_menu.filler", "GRAY_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openDungeonList(Player player) {
        List<Dungeon> dungeons = new ArrayList<>(plugin.getDungeonManager().getAllDungeons());
        int size = Math.min(54, ((dungeons.size() + 8) / 9) * 9);
        if (size < 27) size = 27;

        Inventory gui = Bukkit.createInventory(null, size, plugin.getGUIConfigManager().titleDungeonList());
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        for (int i = 0; i < dungeons.size() && i < 45; i++) {
            Dungeon dungeon = dungeons.get(i);
            Material icon = getConfiguredDungeonIcon(dungeon, data);
            List<String> lore = new ArrayList<>();
            String prefix = plugin.getGUIConfigManager().getColoredString("dungeon_list.item.lore_prefix_desc", "§7");
            dungeon.getDescription().forEach(line -> lore.add(prefix + line.replace("&", "§")));
            lore.add("");
            lore.add("§8▎ §7Người chơi: §e" + dungeon.getMinPlayers() + "-" + dungeon.getMaxPlayers());
            lore.add("§8▎ §7Số ải: §e" + dungeon.getTotalStages());
            lore.add("§8▎ §7Số mạng: §e" + dungeon.getRespawnLives());
            lore.add("");

            int entries = data.getDungeonEntries(dungeon.getId());
            int highScore = data.getDungeonScore(dungeon.getId());
            int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());

            lore.add(plugin.getGUIConfigManager().color("&8▎ &6&lThống Kê Của Bạn:"));
            lore.add(plugin.getGUIConfigManager().getColoredString("dungeon_list.labels.entries", "&7Lượt còn lại: {color}{entries}")
                    .replace("{color}", getEntriesColor(entries))
                    .replace("{entries}", String.valueOf(entries)));
            lore.add(plugin.getGUIConfigManager().getColoredString("dungeon_list.labels.high_score", "&7Điểm cao nhất: &6{highScore}")
                    .replace("{highScore}", String.valueOf(highScore)));
            if (rank != -1) {
                lore.add(plugin.getGUIConfigManager().getColoredString("dungeon_list.labels.rank", "&7Xếp hạng: &e#{rank}")
                        .replace("{rank}", String.valueOf(rank)));
            }
            lore.add("");

            if (entries > 0) {
                lore.add(plugin.getGUIConfigManager().getColoredString("dungeon_list.labels.join_left", "&a&l▶ Click trái: &aTham gia"));
            } else {
                lore.add(plugin.getGUIConfigManager().getColoredString("dungeon_list.labels.no_entries", "&c&l✖ Hết lượt chơi!"));
            }
            lore.add(plugin.getGUIConfigManager().getColoredString("dungeon_list.labels.detail_right", "&e&l▶ Click phải: &eXem chi tiết"));

            String nameFormat = plugin.getGUIConfigManager().getColoredString("dungeon_list.item.name_format", "{displayName}");
            String name = nameFormat.replace("{displayName}", dungeon.getDisplayName().replace("&", "§"));
            ItemStack item = createItemWithData(icon, name, dungeon.getId(), lore.toArray(new String[0]));
            gui.setItem(i, item);
        }

        if (size > 27) {
            ItemStack back = createConfiguredSimpleItem("dungeon_list.back", Material.ARROW, "§c§lQuay Lại", new String[]{"§7Về menu chính"});
            gui.setItem(size - 5, back);
        }
        Material filler = plugin.getGUIConfigManager().getMaterial("dungeon_list.filler", "BLACK_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openDungeonInfo(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.getGUIConfigManager().titleDungeonInfo());
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        ItemStack dungeonIcon = createItem(getConfiguredDungeonIcon(dungeon, data), dungeon.getDisplayName().replace("&", "§"),
                dungeon.getDescription().stream().map(s -> plugin.getGUIConfigManager().color("&7" + s)).toArray(String[]::new));
        gui.setItem(4, dungeonIcon);

        ItemStack generalInfo = createConfiguredItem(
                "dungeon_info.items.general_info",
                "{minPlayers}", String.valueOf(dungeon.getMinPlayers()),
                "{maxPlayers}", String.valueOf(dungeon.getMaxPlayers()),
                "{totalStages}", String.valueOf(dungeon.getTotalStages()),
                "{respawnLives}", String.valueOf(dungeon.getRespawnLives()),
                "{entriesPerReset}", String.valueOf(dungeon.getEntriesPerReset())
        );
        gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.general_info.slot", 19), generalInfo);

        int entries = data.getDungeonEntries(dungeon.getId());
        int highScore = data.getDungeonScore(dungeon.getId());
        int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());

        String rankLine = rank != -1
                ? plugin.getGUIConfigManager().getColoredString("dungeon_info.items.your_stats.rank_line.has_rank", "&7Xếp hạng: &e#{rank}").replace("{rank}", String.valueOf(rank))
                : plugin.getGUIConfigManager().getColoredString("dungeon_info.items.your_stats.rank_line.no_rank", "&7Chưa có xếp hạng");
        ItemStack yourStats = createConfiguredItem(
                "dungeon_info.items.your_stats",
                "{color}", getEntriesColor(entries),
                "{entries}", String.valueOf(entries),
                "{highScore}", String.valueOf(highScore),
                "{rankLine}", rankLine
        );
        gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.your_stats.slot", 21), yourStats);

        ItemStack topPlayers = createConfiguredItem("dungeon_info.items.top_players");
        ItemMeta tpMeta = topPlayers.getItemMeta();
        if (tpMeta != null) {
            tpMeta.getPersistentDataContainer().set(plugin.getDungeonKey(), PersistentDataType.STRING, "hint_top_players");
            topPlayers.setItemMeta(tpMeta);
        }
        gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.top_players.slot", 23), topPlayers);

        String adminLine = player.hasPermission("paradungeon.admin")
                ? plugin.getGUIConfigManager().getColoredString("dungeon_info.items.rewards.admin_line.admin", "&c▶ Click để quản lý!")
                : plugin.getGUIConfigManager().getColoredString("dungeon_info.items.rewards.admin_line.user", "&e▶ Click để xem!");
        ItemStack rewards = createItemWithData(
                plugin.getGUIConfigManager().getMaterial("dungeon_info.items.rewards.material", "CHEST"),
                plugin.getGUIConfigManager().getColoredString("dungeon_info.items.rewards.name", "&6&lPhần Thưởng"),
                "rewards_" + dungeon.getId(),
                plugin.getGUIConfigManager().getColoredStringList("dungeon_info.items.rewards.lore")
                        .stream().map(s -> s.replace("{adminLine}", adminLine)).toArray(String[]::new)
        );
        gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.rewards.slot", 25), rewards);

        if (entries > 0) {
            ItemStack joinBtn = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial("dungeon_info.items.join_button.material", "LIME_WOOL"),
                    plugin.getGUIConfigManager().getColoredString("dungeon_info.items.join_button.name", "&a&l✔ THAM GIA"),
                    "join_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList("dungeon_info.items.join_button.lore")
                            .stream().map(s -> s.replace("{entries}", String.valueOf(entries))).toArray(String[]::new)
            );
            gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.join_button.slot", 49), joinBtn);
        } else {
            ItemStack noEntries = createConfiguredItem(
                    "dungeon_info.items.no_entries",
                    "{resetTime}", getResetTimeString()
            );
            gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.no_entries.slot", 49), noEntries);
        }

        ItemStack back = createConfiguredSimpleItem("dungeon_info.items.back", Material.ARROW, "§c§lQuay Lại", new String[]{"§7Về danh sách phó bản"});
        gui.setItem(plugin.getGUIConfigManager().getInt("dungeon_info.items.back.slot", 45), back);
        Material filler = plugin.getGUIConfigManager().getMaterial("dungeon_info.filler", "GRAY_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openLeaderboard(Player player, String dungeonId) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.getGUIConfigManager().titleLeaderboard());
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        var leaderboard = plugin.getLeaderboardManager().getLeaderboard(dungeonId);
        ItemStack title = createConfiguredItem(
                "leaderboard.items.title",
                "{dungeonDisplayName}", dungeon.getDisplayName().replace("&", "§"),
                "{count}", String.valueOf(leaderboard.size())
        );
        gui.setItem(plugin.getGUIConfigManager().getInt("leaderboard.items.title.slot", 4), title);

        int slot = 18;
        for (int i = 0; i < Math.min(leaderboard.size(), 21); i++) {
            var entry = leaderboard.get(i);
            int rank = i + 1;
            Material material = getConfiguredRankMaterial(rank);
            String rankColor = getConfiguredRankColor(rank);

            ItemStack item = createItem(material, rankColor + "#" + rank + " §f" + entry.getPlayerName(),
                    "§7Điểm số: §6" + entry.getScore(), "", rank <= 3 ? "§e⭐ Top " + rank + "!" : "");
            gui.setItem(slot++, item);
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        int playerRank = plugin.getLeaderboardManager().getPlayerRank(dungeonId, player.getUniqueId());
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        String rankLine = playerRank != -1
                ? plugin.getGUIConfigManager().getColoredString("leaderboard.items.your_rank.rank_line.has_rank", "&7Hạng: &e#{rank}").replace("{rank}", String.valueOf(playerRank))
                : plugin.getGUIConfigManager().getColoredString("leaderboard.items.your_rank.rank_line.no_rank", "&7Chưa có xếp hạng");
        ItemStack yourRank = createConfiguredItem(
                "leaderboard.items.your_rank",
                "{rankLine}", rankLine,
                "{score}", String.valueOf(data.getDungeonScore(dungeonId))
        );
        gui.setItem(plugin.getGUIConfigManager().getInt("leaderboard.items.your_rank.slot", 49), yourRank);

        ItemStack back = createConfiguredSimpleItem("leaderboard.items.back", Material.ARROW, "§c§lQuay Lại", new String[]{"§7Về thông tin phó bản"});
        gui.setItem(plugin.getGUIConfigManager().getInt("leaderboard.items.back.slot", 45), back);
        Material filler = plugin.getGUIConfigManager().getMaterial("leaderboard.filler", "BLACK_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openPlayerStats(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.getGUIConfigManager().titleStats());
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

        ItemStack back = createConfiguredSimpleItem("player_stats.back", Material.ARROW, "§c§lQuay Lại", new String[]{"§7Về menu chính"});
        gui.setItem(plugin.getGUIConfigManager().getInt("player_stats.back.slot", 49), back);
        Material filler = plugin.getGUIConfigManager().getMaterial("player_stats.filler", "GRAY_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openSettings(Player player) {
        int size = plugin.getGUIConfigManager().getInt("settings.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, plugin.getGUIConfigManager().titleSettings());
        // Particles
        setConfiguredSimpleItem(gui, "settings.items.particles", 11);
        // Sounds
        setConfiguredSimpleItem(gui, "settings.items.sounds", 13);
        // Notifications
        setConfiguredSimpleItem(gui, "settings.items.notifications", 15);
        // Back
        setConfiguredSimpleItem(gui, "settings.items.back", 22);

        Material filler = plugin.getGUIConfigManager().getMaterial("settings.filler", "GRAY_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
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

    private Material getConfiguredDungeonIcon(Dungeon dungeon, PlayerData data) {
        int entries = data.getDungeonEntries(dungeon.getId());
        if (entries <= 0) return plugin.getGUIConfigManager().getMaterial("dungeon_list.icon.when_no_entries", "RED_WOOL");
        boolean hasBoss = dungeon.getStages().values().stream().anyMatch(Stage::isBoss);
        if (hasBoss) return plugin.getGUIConfigManager().getMaterial("dungeon_list.icon.when_has_boss", "NETHERITE_SWORD");
        return plugin.getGUIConfigManager().getMaterial("dungeon_list.icon.default", "DIAMOND_SWORD");
    }

    private String getEntriesColor(int entries) {
        if (entries >= 3) return "§a";
        if (entries == 2) return "§e";
        if (entries == 1) return "§c";
        return "§4";
    }

    private Material getConfiguredRankMaterial(int rank) {
        String base = "leaderboard.rank_materials." + rank;
        if (rank > 3) base = "leaderboard.rank_materials.default";
        return plugin.getGUIConfigManager().getMaterial(base, rank == 1 ? "GOLD_BLOCK" : rank == 2 ? "IRON_BLOCK" : rank == 3 ? "COPPER_BLOCK" : "COAL_BLOCK");
    }

    private String getConfiguredRankColor(int rank) {
        String key = rank > 3 ? "leaderboard.rank_colors.default" : "leaderboard.rank_colors." + rank;
        return plugin.getGUIConfigManager().getColoredString(key, rank == 1 ? "&6&l" : rank == 2 ? "&7&l" : rank == 3 ? "&c&l" : "&f");
    }

    private void setConfiguredMenuItem(Inventory gui, String path, String actionData, int defaultSlot) {
        int slot = plugin.getGUIConfigManager().getInt(path + ".slot", defaultSlot);
        ItemStack item = createItemWithData(
                plugin.getGUIConfigManager().getMaterial(path + ".material", "PAPER"),
                plugin.getGUIConfigManager().getColoredString(path + ".name", ""),
                actionData,
                plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
        );
        gui.setItem(slot, item);
    }

    private void setConfiguredSimpleItem(Inventory gui, String path, int defaultSlot) {
        int slot = plugin.getGUIConfigManager().getInt(path + ".slot", defaultSlot);
        ItemStack item = createConfiguredItem(path);
        gui.setItem(slot, item);
    }

    private ItemStack createConfiguredItem(String basePath, String... replacements) {
        ItemStack item = new ItemStack(plugin.getGUIConfigManager().getMaterial(basePath + ".material", "PAPER"));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getGUIConfigManager().getColoredString(basePath + ".name", "");
            if (name != null) meta.setDisplayName(applyReplacements(name, replacements));
            List<String> loreList = plugin.getGUIConfigManager().getColoredStringList(basePath + ".lore");
            if (!loreList.isEmpty()) {
                List<String> out = new ArrayList<>();
                for (String line : loreList) {
                    if (line == null) continue;
                    String replaced = applyReplacements(line, replacements);
                    if (!replaced.isEmpty()) out.add(replaced);
                }
                meta.setLore(out);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createConfiguredSimpleItem(String basePath, Material defMat, String defName, String[] defLore) {
        ItemStack item = new ItemStack(plugin.getGUIConfigManager().getMaterial(basePath + ".material", defMat.name()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getGUIConfigManager().getColoredString(basePath + ".name", defName);
            if (name != null) meta.setDisplayName(name);
            List<String> loreList = plugin.getGUIConfigManager().getColoredStringList(basePath + ".lore");
            if (loreList.isEmpty() && defLore != null) {
                loreList = new ArrayList<>();
                for (String s : defLore) loreList.add(s);
            }
            if (!loreList.isEmpty()) meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String applyReplacements(String input, String... replacements) {
        if (input == null || replacements == null) return input;
        String out = input;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            out = out.replace(replacements[i], replacements[i + 1]);
        }
        return out;
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