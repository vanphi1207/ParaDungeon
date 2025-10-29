package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.gui.GUIManager;
import me.ihqqq.paraDungeon.models.Dungeon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {

    private final ParaDungeon plugin;

    public GUIListener(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!isPluginGUI(title)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String mainTitle = plugin.getGUIConfigManager().titleMainMenu();
        String listTitle = plugin.getGUIConfigManager().titleDungeonList();
        String infoTitle = plugin.getGUIConfigManager().titleDungeonInfo();
        String lbTitle = plugin.getGUIConfigManager().titleLeaderboard();
        String statsTitle = plugin.getGUIConfigManager().titleStats();
        String settingsTitle = plugin.getGUIConfigManager().titleSettings();

        if (title.equals(mainTitle)) {
            handleMainMenu(player, clickedItem, meta);
        } else if (title.equals(listTitle)) {
            handleDungeonList(player, clickedItem, meta, event.isLeftClick());
        } else if (title.equals(infoTitle)) {
            handleDungeonInfo(player, clickedItem, meta);
        } else if (title.equals(lbTitle)) {
            handleLeaderboard(player, clickedItem, meta);
        } else if (title.equals(statsTitle)) {
            handleStats(player, clickedItem, meta);
        } else if (title.equals(settingsTitle)) {
            handleSettings(player, clickedItem, meta);
        }
    }

    private boolean isPluginGUI(String title) {
        return plugin.getGUIConfigManager().isAnyKnownTitle(title);
    }

    private void handleMainMenu(Player player, ItemStack item, ItemMeta meta) {
        String data = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), org.bukkit.persistence.PersistentDataType.STRING);
        if (data == null || !data.startsWith("action:")) return;
        switch (data) {
            case "action:open_dungeon_list":
                plugin.getGUIManager().openDungeonList(player);
                break;
            case "action:open_player_stats":
                plugin.getGUIManager().openPlayerStats(player);
                break;
            case "action:open_leaderboard":
                var dungeons = plugin.getDungeonManager().getAllDungeons();
                if (!dungeons.isEmpty()) {
                    String firstDungeonId = dungeons.iterator().next().getId();
                    plugin.getGUIManager().openLeaderboard(player, firstDungeonId);
                }
                break;
            case "action:open_settings":
                plugin.getGUIManager().openSettings(player);
                break;
        }
    }

    private void handleDungeonList(Player player, ItemStack item, ItemMeta meta, boolean isLeftClick) {
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }
        String dungeonId = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), PersistentDataType.STRING);
        if (dungeonId == null) return;
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        if (isLeftClick) {
            // SỬA LỖI: Thêm kiểm tra lượt chơi trước khi thực hiện lệnh
            if (!plugin.getPlayerDataManager().hasAvailableEntries(player, dungeonId)) {
                player.sendMessage(plugin.getConfigManager().getMessage("lobby.no-entries"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                // Cập nhật lại GUI để hiển thị đúng trạng thái (nút đỏ)
                plugin.getGUIManager().openDungeonList(player);
                return;
            }
            player.closeInventory();
            player.performCommand("dungeon join " + dungeonId);
        } else {
            plugin.getGUIManager().openDungeonInfo(player, dungeon);
        }
    }

    private void handleDungeonInfo(Player player, ItemStack item, ItemMeta meta) {
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openDungeonList(player);
            return;
        }
        String data = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), PersistentDataType.STRING);
        if (data == null) return;

        if (data.startsWith("join_")) {
            String dungeonId = data.substring(5);
            player.closeInventory();
            player.performCommand("dungeon join " + dungeonId);
            return;
        }

        // Open leaderboard for this dungeon when clicking Top Players item
        if (data.startsWith("leaderboard_")) {
            String dungeonId = data.substring("leaderboard_".length());
            plugin.getGUIManager().openLeaderboard(player, dungeonId);
            return;
        }

        if ("hint_top_players".equals(data)) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    plugin.getConfigManager().getMessageRaw("gui.top-players-hint"));
            return;
        }

        if (data.startsWith("rewards_")) {
            String dungeonId = data.substring(8);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                if (player.hasPermission("paradungeon.admin")) {
                    plugin.getGUIManager().getRewardEditorGUI().openRewardMenu(player, dungeon);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
                }
            }
        }
    }

    private void handleLeaderboard(Player player, ItemStack item, ItemMeta meta) {
        if (item.getType() == Material.ARROW) {
            String dungeonId = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), PersistentDataType.STRING);
            if(dungeonId != null){
                Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                if (dungeon != null) {
                    plugin.getGUIManager().openDungeonInfo(player, dungeon);
                }
            } else {
                plugin.getGUIManager().openMainMenu(player);
            }

        }
    }

    private void handleStats(Player player, ItemStack item, ItemMeta meta) {
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }
        String dungeonId = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), PersistentDataType.STRING);
        if (dungeonId != null) {
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().openDungeonInfo(player, dungeon);
            }
        }
    }

    private void handleSettings(Player player, ItemStack item, ItemMeta meta) {
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }
        player.sendMessage(plugin.getConfigManager().getMessage("gui.settings-coming-soon"));
    }
}