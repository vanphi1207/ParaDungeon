package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.gui.RewardEditorGUI;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRewards;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener for Reward Editor GUI
 */
public class RewardGUIListener implements Listener {

    private final ParaDungeon plugin;
    private final Map<String, String> editingSessions; // player name -> dungeon id
    private final Map<String, Integer> editingScores; // player name -> score tier

    public RewardGUIListener(ParaDungeon plugin) {
        this.plugin = plugin;
        this.editingSessions = new HashMap<>();
        this.editingScores = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (!isRewardGUI(title)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // Handle reward editor GUIs differently - allow item placement
        if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {

            int slot = event.getRawSlot();

            // Allow item placement in slots 18-44 (reward slots)
            if (slot >= 18 && slot <= 44) {
                // Don't cancel - allow item placement
                return;
            }

            // Cancel clicks on control items
            event.setCancelled(true);

            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null) return;

            String data = meta.getPersistentDataContainer().get(
                    plugin.getDungeonKey(),
                    PersistentDataType.STRING
            );

            if (data == null) return;

            handleRewardEditorClick(player, data, event.getInventory());
            return;
        }

        // For other reward GUIs, cancel all clicks
        event.setCancelled(true);

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String data = meta.getPersistentDataContainer().get(
                plugin.getDungeonKey(),
                PersistentDataType.STRING
        );

        if (data == null) return;

        handleRewardMenuClick(player, data, event.isLeftClick());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getPlayer();

        // Auto-save when closing reward editor
        if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE)) {
            String dungeonId = editingSessions.get(player.getName());
            if (dungeonId != null) {
                saveCompletionRewards(player, event.getInventory(), dungeonId);
                editingSessions.remove(player.getName());
            }
        } else if (title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {
            String dungeonId = editingSessions.get(player.getName());
            Integer score = editingScores.get(player.getName());
            if (dungeonId != null && score != null) {
                saveScoreTierRewards(player, event.getInventory(), dungeonId, score);
                editingSessions.remove(player.getName());
                editingScores.remove(player.getName());
            }
        }
    }

    private boolean isRewardGUI(String title) {
        return title.equals(RewardEditorGUI.REWARD_MENU_TITLE) ||
                title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.SCORE_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE);
    }

    private void handleRewardMenuClick(Player player, String data, boolean isLeftClick) {
        if (data.startsWith("completion_")) {
            String dungeonId = data.substring(11);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                editingSessions.put(player.getName(), dungeonId);
                plugin.getGUIManager().getRewardEditorGUI().openCompletionRewardsEditor(player, dungeon);
            }
        } else if (data.startsWith("score_")) {
            String dungeonId = data.substring(6);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().getRewardEditorGUI().openScoreRewardsEditor(player, dungeon);
            }
        } else if (data.startsWith("back_reward_")) {
            String dungeonId = data.substring(12);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().getRewardEditorGUI().openRewardMenu(player, dungeon);
            }
        } else if (data.startsWith("preview_")) {
            String dungeonId = data.substring(8);
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§7Tính năng xem trước phần thưởng đang được phát triển!");
        }
    }

    private void handleRewardEditorClick(Player player, String data, Inventory inv) {
        if (data.startsWith("save_completion_")) {
            String dungeonId = data.substring(16);
            saveCompletionRewards(player, inv, dungeonId);
            player.sendMessage(plugin.getConfigManager().getMessage("admin.rewards-saved"));
        } else if (data.startsWith("back_reward_")) {
            String dungeonId = data.substring(12);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                player.closeInventory();
                plugin.getGUIManager().getRewardEditorGUI().openRewardMenu(player, dungeon);
            }
        } else if (data.startsWith("add_score_tier_")) {
            String dungeonId = data.substring(15);
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("admin.type-score"));
            // TODO: Implement chat listener for score input
            // For now, just inform the user
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§eTính năng này đang được hoàn thiện. Vui lòng sử dụng config file để thêm score tier mới.");
        } else if (data.startsWith("edit_tier_")) {
            String[] parts = data.substring(10).split("_");
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        editingSessions.put(player.getName(), dungeonId);
                        editingScores.put(player.getName(), score);
                        player.closeInventory();
                        plugin.getGUIManager().getRewardEditorGUI().openScoreTierEditor(player, dungeon, score);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", parts[1]));
                }
            }
        } else if (data.startsWith("save_tier_")) {
            String[] parts = data.substring(10).split("_");
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    saveScoreTierRewards(player, inv, dungeonId, score);
                    player.sendMessage(plugin.getConfigManager().getMessage("admin.rewards-saved"));
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", parts[1]));
                }
            }
        } else if (data.startsWith("back_score_rewards_")) {
            String dungeonId = data.substring(19);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                player.closeInventory();
                plugin.getGUIManager().getRewardEditorGUI().openScoreRewardsEditor(player, dungeon);
            }
        }
    }

    private void saveCompletionRewards(Player player, Inventory inv, String dungeonId) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        // Collect items from slots 18-44
        List<ItemStack> rewardItems = new ArrayList<>();
        for (int i = 18; i <= 44; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) {
                // Check if it's not a control item
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(plugin.getDungeonKey(), PersistentDataType.STRING)) {
                    continue; // Skip control items
                }
                rewardItems.add(item.clone());
            }
        }

        // Save items to dungeon config
        rewards.setCompletionRewardItems(rewardItems);
        plugin.getDungeonManager().saveDungeon(dungeon);

        plugin.getLogger().info("Saved " + rewardItems.size() + " completion reward items for dungeon " + dungeonId);
    }

    private void saveScoreTierRewards(Player player, Inventory inv, String dungeonId, int score) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) return;

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

        // Collect items from slots 18-44
        List<ItemStack> rewardItems = new ArrayList<>();
        for (int i = 18; i <= 44; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(plugin.getDungeonKey(), PersistentDataType.STRING)) {
                    continue;
                }
                rewardItems.add(item.clone());
            }
        }

        // Save items
        scoreReward.setRewardItems(rewardItems);
        plugin.getDungeonManager().saveDungeon(dungeon);

        plugin.getLogger().info("Saved " + rewardItems.size() + " reward items for score tier " + score + " in dungeon " + dungeonId);
    }
}