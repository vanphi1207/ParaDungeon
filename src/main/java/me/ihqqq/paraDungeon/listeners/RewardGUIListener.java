package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.gui.RewardEditorGUI;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRewards;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardGUIListener implements Listener {

    private final ParaDungeon plugin;
    private final Map<String, String> editingSessions;
    private final Map<String, Integer> editingScores;

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

        if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {

            Inventory topInventory = event.getView().getTopInventory();

            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);

                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }

                int playerInventorySlot = event.getRawSlot();

                if (playerInventorySlot >= topInventory.getSize()) {
                    int firstEmpty = -1;
                    for (int i = 18; i <= 44; i++) {
                        if (topInventory.getItem(i) == null || topInventory.getItem(i).getType().isAir()) {
                            firstEmpty = i;
                            break;
                        }
                    }

                    if (firstEmpty != -1) {
                        topInventory.setItem(firstEmpty, clickedItem.clone());
                        event.setCurrentItem(null);
                    } else {
                        player.sendMessage(plugin.getConfigManager().getMessage("gui.rewards-no-space"));
                    }
                }
                return;
            }

            int slot = event.getRawSlot();

            if (slot >= 18 && slot <= 44) {
                return;
            }
            if (slot >= topInventory.getSize()) {
                return;
            }

            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();

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

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

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
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {

            for (int slot : event.getRawSlots()) {
                if (slot >= 18 && slot <= 44) {
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getPlayer();

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
                plugin.getGUIManager().openDungeonInfo(player, dungeon);
            }
        } else if (data.startsWith("preview_")) {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.rewards-preview-coming-soon"));
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
            player.sendMessage(plugin.getConfigManager().getMessage("gui.rewards-score-tier-coming-soon"));
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

        scoreReward.setRewardItems(rewardItems);
        plugin.getDungeonManager().saveDungeon(dungeon);

        plugin.getLogger().info("Saved " + rewardItems.size() + " reward items for score tier " + score + " in dungeon " + dungeonId);
    }
}