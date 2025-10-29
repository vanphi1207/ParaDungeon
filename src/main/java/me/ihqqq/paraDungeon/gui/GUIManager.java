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
        String title = plugin.getConfigManager().getGUITitle("main-menu.title");
        int size = plugin.getConfigManager().getGUISize("main-menu.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        ItemStack dungeonList = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("main-menu.items.dungeon-list.material")),
                plugin.getConfigManager().getGUIItemName("main-menu.items.dungeon-list.name"),
                plugin.getConfigManager().getGUIItemLore("main-menu.items.dungeon-list.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("main-menu.items.dungeon-list.slot"), dungeonList);

        ItemStack stats = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("main-menu.items.my-stats.material")),
                plugin.getConfigManager().getGUIItemName("main-menu.items.my-stats.name"),
                plugin.getConfigManager().getGUIItemLore("main-menu.items.my-stats.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("main-menu.items.my-stats.slot"), stats);

        ItemStack leaderboard = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("main-menu.items.leaderboard.material")),
                plugin.getConfigManager().getGUIItemName("main-menu.items.leaderboard.name"),
                plugin.getConfigManager().getGUIItemLore("main-menu.items.leaderboard.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("main-menu.items.leaderboard.slot"), leaderboard);

        ItemStack settings = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("main-menu.items.settings.material")),
                plugin.getConfigManager().getGUIItemName("main-menu.items.settings.name"),
                plugin.getConfigManager().getGUIItemLore("main-menu.items.settings.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("main-menu.items.settings.slot"), settings);

        if (plugin.getConfigManager().isGUIFillerEnabled("main-menu.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("main-menu.filler")));
        }
        player.openInventory(gui);
    }

    public void openDungeonList(Player player) {
        List<Dungeon> dungeons = new ArrayList<>(plugin.getDungeonManager().getAllDungeons());
        int minSize = plugin.getConfigManager().getGUISize("dungeon-list.min-size");
        int size = Math.min(54, ((dungeons.size() + 8) / 9) * 9);
        if (size < minSize) size = minSize;

        String title = plugin.getConfigManager().getGUITitle("dungeon-list.title");
        Inventory gui = Bukkit.createInventory(null, size, title);
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
            ItemStack back = createItem(
                    Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.back-button.material")),
                    plugin.getConfigManager().getGUIItemName("dungeon-list.back-button.name"),
                    plugin.getConfigManager().getGUIItemLore("dungeon-list.back-button.lore").toArray(new String[0])
            );
            int backSlot = plugin.getConfigManager().getGUIItemSlot("dungeon-list.back-button.slot");
            gui.setItem(backSlot < 0 ? size + backSlot : backSlot, back);
        }
        if (plugin.getConfigManager().isGUIFillerEnabled("dungeon-list.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("dungeon-list.filler")));
        }
        player.openInventory(gui);
    }

    public void openDungeonInfo(Player player, Dungeon dungeon) {
        String title = plugin.getConfigManager().getGUITitle("dungeon-info.title");
        int size = plugin.getConfigManager().getGUISize("dungeon-info.size");
        Inventory gui = Bukkit.createInventory(null, size, title);
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        ItemStack dungeonIcon = createItem(getDungeonIcon(dungeon, data), dungeon.getDisplayName().replace("&", "§"),
                dungeon.getDescription().stream().map(s -> "§7" + s.replace("&", "§")).toArray(String[]::new));
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.dungeon-icon.slot"), dungeonIcon);

        List<String> generalInfoLore = plugin.getConfigManager().getGUIItemLore("dungeon-info.items.general-info.lore");
        generalInfoLore.replaceAll(line -> line
                .replace("{min_players}", String.valueOf(dungeon.getMinPlayers()))
                .replace("{max_players}", String.valueOf(dungeon.getMaxPlayers()))
                .replace("{total_stages}", String.valueOf(dungeon.getTotalStages()))
                .replace("{respawn_lives}", String.valueOf(dungeon.getRespawnLives()))
                .replace("{entries_per_reset}", String.valueOf(dungeon.getEntriesPerReset())));
        ItemStack generalInfo = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.general-info.material")),
                plugin.getConfigManager().getGUIItemName("dungeon-info.items.general-info.name"),
                generalInfoLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.general-info.slot"), generalInfo);

        int entries = data.getDungeonEntries(dungeon.getId());
        int highScore = data.getDungeonScore(dungeon.getId());
        int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());

        List<String> yourStatsLore = plugin.getConfigManager().getGUIItemLore("dungeon-info.items.your-stats.lore");
        yourStatsLore.replaceAll(line -> line
                .replace("{entries_color}", getEntriesColor(entries))
                .replace("{entries}", String.valueOf(entries))
                .replace("{high_score}", String.valueOf(highScore))
                .replace("{rank_line}", rank != -1 ? "§7Xếp hạng: §e#" + rank : "§7Chưa có xếp hạng"));
        ItemStack yourStats = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.your-stats.material")),
                plugin.getConfigManager().getGUIItemName("dungeon-info.items.your-stats.name"),
                yourStatsLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.your-stats.slot"), yourStats);

        ItemStack topPlayers = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.top-players.material")),
                plugin.getConfigManager().getGUIItemName("dungeon-info.items.top-players.name"),
                plugin.getConfigManager().getGUIItemLore("dungeon-info.items.top-players.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.top-players.slot"), topPlayers);

        List<String> rewardsLore = player.hasPermission("paradungeon.admin") ?
                plugin.getConfigManager().getGUIItemLore("dungeon-info.items.rewards.lore-admin") :
                plugin.getConfigManager().getGUIItemLore("dungeon-info.items.rewards.lore-player");
        ItemStack rewards = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.rewards.material")),
                plugin.getConfigManager().getGUIItemName("dungeon-info.items.rewards.name"),
                "rewards_" + dungeon.getId(),
                rewardsLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.rewards.slot"), rewards);

        if (entries > 0) {
            List<String> joinLore = plugin.getConfigManager().getGUIItemLore("dungeon-info.items.join-button.lore");
            joinLore.replaceAll(line -> line.replace("{entries}", String.valueOf(entries)));
            ItemStack joinBtn = createItemWithData(
                    Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.join-button.material")),
                    plugin.getConfigManager().getGUIItemName("dungeon-info.items.join-button.name"),
                    "join_" + dungeon.getId(),
                    joinLore.toArray(new String[0])
            );
            gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.join-button.slot"), joinBtn);
        } else {
            List<String> noEntriesLore = plugin.getConfigManager().getGUIItemLore("dungeon-info.items.no-entries-button.lore");
            noEntriesLore.replaceAll(line -> line.replace("{reset_time}", getResetTimeString()));
            ItemStack noEntries = createItem(
                    Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.no-entries-button.material")),
                    plugin.getConfigManager().getGUIItemName("dungeon-info.items.no-entries-button.name"),
                    noEntriesLore.toArray(new String[0])
            );
            gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.no-entries-button.slot"), noEntries);
        }

        ItemStack back = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-info.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("dungeon-info.items.back-button.name"),
                plugin.getConfigManager().getGUIItemLore("dungeon-info.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("dungeon-info.items.back-button.slot"), back);
        
        if (plugin.getConfigManager().isGUIFillerEnabled("dungeon-info.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("dungeon-info.filler")));
        }
        player.openInventory(gui);
    }

    public void openLeaderboard(Player player, String dungeonId) {
        String title = plugin.getConfigManager().getGUITitle("leaderboard.title");
        int size = plugin.getConfigManager().getGUISize("leaderboard.size");
        Inventory gui = Bukkit.createInventory(null, size, title);
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        var leaderboard = plugin.getLeaderboardManager().getLeaderboard(dungeonId);
        List<String> titleLore = plugin.getConfigManager().getGUIItemLore("leaderboard.items.title.lore");
        titleLore.replaceAll(line -> line
                .replace("{dungeon_name}", dungeon.getDisplayName().replace("&", "§"))
                .replace("{total}", String.valueOf(leaderboard.size())));
        ItemStack titleItem = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.items.title.material")),
                plugin.getConfigManager().getGUIItemName("leaderboard.items.title.name"),
                titleLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("leaderboard.items.title.slot"), titleItem);

        int slot = plugin.getConfigManager().getGUIInt("leaderboard.rank-display.start-slot", 18);
        for (int i = 0; i < Math.min(leaderboard.size(), 21); i++) {
            var entry = leaderboard.get(i);
            int rank = i + 1;
            Material material = getRankMaterial(rank);
            String rankColor = getRankColor(rank);
            String specialLine = rank <= 3 ? plugin.getConfigManager().getGUIString("leaderboard.rank-display.rank-" + rank + ".special", "") : "";

            List<String> lore = plugin.getConfigManager().getGUIItemLore("leaderboard.rank-display.lore-template");
            lore.replaceAll(line -> line
                    .replace("{score}", String.valueOf(entry.getScore()))
                    .replace("{special_line}", specialLine));
            
            ItemStack item = createItem(material, rankColor + "#" + rank + " §f" + entry.getPlayerName(),
                    lore.toArray(new String[0]));
            gui.setItem(slot++, item);
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        int playerRank = plugin.getLeaderboardManager().getPlayerRank(dungeonId, player.getUniqueId());
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        List<String> yourRankLore = playerRank != -1 ?
                plugin.getConfigManager().getGUIItemLore("leaderboard.items.your-rank.lore-ranked") :
                plugin.getConfigManager().getGUIItemLore("leaderboard.items.your-rank.lore-unranked");
        yourRankLore.replaceAll(line -> line
                .replace("{rank}", String.valueOf(playerRank))
                .replace("{score}", String.valueOf(data.getDungeonScore(dungeonId))));
        ItemStack yourRank = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.items.your-rank.material")),
                plugin.getConfigManager().getGUIItemName("leaderboard.items.your-rank.name"),
                yourRankLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("leaderboard.items.your-rank.slot"), yourRank);

        ItemStack back = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("leaderboard.items.back-button.name"),
                plugin.getConfigManager().getGUIItemLore("leaderboard.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("leaderboard.items.back-button.slot"), back);
        
        if (plugin.getConfigManager().isGUIFillerEnabled("leaderboard.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("leaderboard.filler")));
        }
        player.openInventory(gui);
    }

    public void openPlayerStats(Player player) {
        String title = plugin.getConfigManager().getGUITitle("player-stats.title");
        int size = plugin.getConfigManager().getGUISize("player-stats.size");
        Inventory gui = Bukkit.createInventory(null, size, title);
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            String headName = plugin.getConfigManager().getGUIItemName("player-stats.items.player-head.name");
            skullMeta.setDisplayName(headName.replace("{player_name}", player.getName()));
            skullMeta.setLore(plugin.getConfigManager().getGUIItemLore("player-stats.items.player-head.lore"));
            head.setItemMeta(skullMeta);
        }
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("player-stats.items.player-head.slot"), head);

        int slot = plugin.getConfigManager().getGUIInt("player-stats.items.dungeon-stats.start-slot", 18);
        int maxSlot = plugin.getConfigManager().getGUIInt("player-stats.items.dungeon-stats.max-slot", 44);
        for (Dungeon dungeon : plugin.getDungeonManager().getAllDungeons()) {
            int entries = data.getDungeonEntries(dungeon.getId());
            int score = data.getDungeonScore(dungeon.getId());
            int rank = plugin.getLeaderboardManager().getPlayerRank(dungeon.getId(), player.getUniqueId());
            
            List<String> statsLore = plugin.getConfigManager().getGUIItemLore("player-stats.items.dungeon-stats.lore");
            statsLore.replaceAll(line -> line
                    .replace("{entries_color}", getEntriesColor(entries))
                    .replace("{entries}", String.valueOf(entries))
                    .replace("{score}", String.valueOf(score))
                    .replace("{rank_line}", rank != -1 ? "§7Xếp hạng: §e#" + rank : "§7Chưa có xếp hạng"));
            
            ItemStack item = createItem(getDungeonIcon(dungeon, data), dungeon.getDisplayName().replace("&", "§"),
                    statsLore.toArray(new String[0]));

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(plugin.getDungeonKey(), PersistentDataType.STRING, dungeon.getId());
                item.setItemMeta(meta);
            }
            gui.setItem(slot++, item);
            if (slot >= maxSlot) break;
        }

        ItemStack back = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("player-stats.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("player-stats.items.back-button.name"),
                plugin.getConfigManager().getGUIItemLore("player-stats.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("player-stats.items.back-button.slot"), back);
        
        if (plugin.getConfigManager().isGUIFillerEnabled("player-stats.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("player-stats.filler")));
        }
        player.openInventory(gui);
    }

    public void openSettings(Player player) {
        String title = plugin.getConfigManager().getGUITitle("settings.title");
        int size = plugin.getConfigManager().getGUISize("settings.size");
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        ItemStack particles = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("settings.items.particles.material")),
                plugin.getConfigManager().getGUIItemName("settings.items.particles.name"),
                plugin.getConfigManager().getGUIItemLore("settings.items.particles.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("settings.items.particles.slot"), particles);

        ItemStack sounds = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("settings.items.sounds.material")),
                plugin.getConfigManager().getGUIItemName("settings.items.sounds.name"),
                plugin.getConfigManager().getGUIItemLore("settings.items.sounds.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("settings.items.sounds.slot"), sounds);

        ItemStack notifications = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("settings.items.notifications.material")),
                plugin.getConfigManager().getGUIItemName("settings.items.notifications.name"),
                plugin.getConfigManager().getGUIItemLore("settings.items.notifications.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("settings.items.notifications.slot"), notifications);

        ItemStack back = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("settings.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("settings.items.back-button.name"),
                plugin.getConfigManager().getGUIItemLore("settings.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("settings.items.back-button.slot"), back);
        
        if (plugin.getConfigManager().isGUIFillerEnabled("settings.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("settings.filler")));
        }
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
        if (entries <= 0) {
            return Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.icons.no-entries"));
        }
        boolean hasBoss = dungeon.getStages().values().stream().anyMatch(Stage::isBoss);
        if (hasBoss) {
            return Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.icons.has-boss"));
        }
        return Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.icons.has-entries"));
    }

    private String getEntriesColor(int entries) {
        if (entries >= 3) return plugin.getConfigManager().getGUIString("dungeon-list.entries-colors.high", "§a");
        if (entries == 2) return plugin.getConfigManager().getGUIString("dungeon-list.entries-colors.medium", "§e");
        if (entries == 1) return plugin.getConfigManager().getGUIString("dungeon-list.entries-colors.low", "§c");
        return plugin.getConfigManager().getGUIString("dungeon-list.entries-colors.none", "§4");
    }

    private Material getRankMaterial(int rank) {
        return switch (rank) {
            case 1 -> Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.rank-display.rank-1.material"));
            case 2 -> Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.rank-display.rank-2.material"));
            case 3 -> Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.rank-display.rank-3.material"));
            default -> Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("leaderboard.rank-display.default.material"));
        };
    }

    private String getRankColor(int rank) {
        return switch (rank) {
            case 1 -> plugin.getConfigManager().getGUIString("leaderboard.rank-display.rank-1.color", "§6§l");
            case 2 -> plugin.getConfigManager().getGUIString("leaderboard.rank-display.rank-2.color", "§7§l");
            case 3 -> plugin.getConfigManager().getGUIString("leaderboard.rank-display.rank-3.color", "§c§l");
            default -> plugin.getConfigManager().getGUIString("leaderboard.rank-display.default.color", "§f");
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

    /**
     * Get the appropriate icon material for a dungeon based on player data
     */
    private Material getDungeonIcon(Dungeon dungeon, PlayerData data) {
        int entries = data.getDungeonEntries(dungeon.getId());
        if (entries <= 0) {
            return Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.icons.no-entries"));
        }
        boolean hasBoss = dungeon.getStages().values().stream().anyMatch(Stage::isBoss);
        if (hasBoss) {
            return Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.icons.has-boss"));
        }
        return Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("dungeon-list.icons.has-entries"));
    }
}