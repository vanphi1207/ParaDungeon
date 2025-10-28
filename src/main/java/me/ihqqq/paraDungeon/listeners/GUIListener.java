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
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openMainMenu(player);
            return;
        }
        String dungeonId = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), PersistentDataType.STRING);
        if (dungeonId == null) return;
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        if (isLeftClick) {
            player.closeInventory();
            player.performCommand("dungeon join " + dungeonId);
        } else {
            plugin.getGUIManager().openDungeonInfo(player, dungeon);
        }
    }

    private void handleDungeonInfo(Player player, ItemStack item, ItemMeta meta) {
        String displayName = meta.getDisplayName();
        if (item.getType() == Material.ARROW) {
            plugin.getGUIManager().openDungeonList(player);
            return;
        }
        String data = meta.getPersistentDataContainer().get(plugin.getDungeonKey(), PersistentDataType.STRING);
        if (data == null) return;

        if (displayName.contains("THAM GIA")) {
            if (data.startsWith("join_")) {
                String dungeonId = data.substring(5);
                player.closeInventory();
                player.performCommand("dungeon join " + dungeonId);
            }
            return;
        }

        if (displayName.contains("Top Người Chơi")) {
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
                    player.sendMessage(plugin.getConfigManager().getMessage("gui.rewards-preview-coming-soon"));
                }
            }
        }
    }

    private void handleLeaderboard(Player player, ItemStack item, ItemMeta meta) {
        if (item.getType() == Material.ARROW) {
            player.closeInventory();
            plugin.getGUIManager().openDungeonList(player);
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