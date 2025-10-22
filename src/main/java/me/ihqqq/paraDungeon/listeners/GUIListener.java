package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.gui.GUIManager;
import me.ihqqq.paraDungeon.models.Dungeon;
import org.bukkit.Material;
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

        // Check if it's one of our GUIs
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

        // Handle different GUIs
        switch (title) {
            case GUIManager.MAIN_MENU_TITLE:
                handleMainMenu(player, clickedItem, meta);
                break;
            case GUIManager.DUNGEON_LIST_TITLE:
                handleDungeonList(player, clickedItem, meta, event.isLeftClick());
                break;
            case GUIManager.DUNGEON_INFO_TITLE:
                handleDungeonInfo(player, clickedItem, meta);
                break;
            case GUIManager.LEADERBOARD_TITLE:
                handleLeaderboard(player, clickedItem, meta);
                break;
            case GUIManager.STATS_TITLE:
                handleStats(player, clickedItem, meta);
                break;
            case GUIManager.SETTINGS_TITLE:
                handleSettings(player, clickedItem, meta);
                break;
        }
    }

    private boolean isPluginGUI(String title) {
        return title.equals(GUIManager.MAIN_MENU_TITLE) ||
                title.equals(GUIManager.DUNGEON_LIST_TITLE) ||
                title.equals(GUIManager.DUNGEON_INFO_TITLE) ||
                title.equals(GUIManager.LEADERBOARD_TITLE) ||
                title.equals(GUIManager.STATS_TITLE) ||
                title.equals(GUIManager.SETTINGS_TITLE);
    }

    private void handleMainMenu(Player player, ItemStack item, ItemMeta meta) {
        String displayName = meta.getDisplayName();

        if (displayName.contains("Danh Sách Phó Bản")) {
            plugin.getGUIManager().openDungeonList(player);
        } else if (displayName.contains("Thống Kê Của Tôi")) {
            plugin.getGUIManager().openPlayerStats(player);
        } else if (displayName.contains("Bảng Xếp Hạng")) {
            // Open leaderboard for first dungeon or show dungeon selector
            var dungeons = plugin.getDungeonManager().getAllDungeons();
            if (!dungeons.isEmpty()) {
                String firstDungeonId = dungeons.iterator().next().getId();
                plugin.getGUIManager().openLeaderboard(player, firstDungeonId);
            }
        } else if (displayName.contains("Cài Đặt")) {
            plugin.getGUIManager().openSettings(player);
        }
    }

    private void handleDungeonList(Player player, ItemStack item, ItemMeta meta, boolean isLeftClick) {
        // Back button
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }

        // Get dungeon ID from item
        String dungeonId = meta.getPersistentDataContainer().get(
                plugin.getDungeonKey(),
                PersistentDataType.STRING
        );

        if (dungeonId == null) return;

        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        if (isLeftClick) {
            // Join dungeon
            player.closeInventory();
            player.performCommand("dungeon join " + dungeonId);
        } else {
            // Show info
            plugin.getGUIManager().openDungeonInfo(player, dungeon);
        }
    }

    private void handleDungeonInfo(Player player, ItemStack item, ItemMeta meta) {
        String displayName = meta.getDisplayName();

        // Back button
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openDungeonList(player);
            return;
        }

        // Join button
        if (displayName.contains("THAM GIA")) {
            String data = meta.getPersistentDataContainer().get(
                    plugin.getDungeonKey(),
                    PersistentDataType.STRING
            );
            if (data != null && data.startsWith("join_")) {
                String dungeonId = data.substring(5);
                player.closeInventory();
                player.performCommand("dungeon join " + dungeonId);
            }
            return;
        }

        // Top players button
        if (displayName.contains("Top Người Chơi")) {
            // Get current dungeon ID from title or context
            // For now, we'll need to track it differently
            // This is a placeholder
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§7Sử dụng §e/dungeon top <tên> §7để xem bảng xếp hạng");
        }
    }

    private void handleLeaderboard(Player player, ItemStack item, ItemMeta meta) {
        // Back button
        if (item.getType() == Material.ARROW) {
            player.closeInventory();
            plugin.getGUIManager().openDungeonList(player);
        }
    }

    private void handleStats(Player player, ItemStack item, ItemMeta meta) {
        // Back button
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }

        // Click on dungeon to see info
        String dungeonId = meta.getPersistentDataContainer().get(
                plugin.getDungeonKey(),
                PersistentDataType.STRING
        );

        if (dungeonId != null) {
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().openDungeonInfo(player, dungeon);
            }
        }
    }

    private void handleSettings(Player player, ItemStack item, ItemMeta meta) {
        // Back button
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }

        // Settings options - currently placeholders
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                "§eTính năng này đang được phát triển!");
    }
}