package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.gui.CommandRewardGUI;
import me.ihqqq.paraDungeon.gui.RewardEditorGUI;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRewards;
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
        ItemStack clickedItem = event.getCurrentItem();

        // ✅ FIX: Cho phép tương tác với reward slots (18-44)
        if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {

            int slot = event.getRawSlot();

            // Cho phép đặt/lấy item ở slots 18-44
            if (slot >= 18 && slot <= 44) {
                // Không cancel, cho phép tương tác bình thường
                return;
            }

            // Cho phép shift-click từ player inventory
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (event.getClickedInventory() != event.getView().getTopInventory()) {
                    event.setCancelled(true);

                    if (clickedItem != null && !clickedItem.getType().isAir()) {
                        // Tìm slot trống trong khoảng 18-44
                        Inventory topInv = event.getView().getTopInventory();
                        for (int i = 18; i <= 44; i++) {
                            ItemStack slotItem = topInv.getItem(i);
                            if (slotItem == null || slotItem.getType().isAir()) {
                                topInv.setItem(i, clickedItem.clone());
                                event.setCurrentItem(null);
                                return;
                            }
                        }
                        player.sendMessage(plugin.getConfigManager().getMessage("gui.rewards-no-space"));
                    }
                    return;
                }
            }

            // Cho phép tương tác với player inventory
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }
        }

        // Cancel các click khác
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

        // Phân luồng xử lý
        if (title.equals(RewardEditorGUI.REWARD_MENU_TITLE) ||
                title.equals(RewardEditorGUI.SCORE_REWARDS_TITLE)) {
            handleRewardMenuClick(player, data, meta, event.isLeftClick());
        } else if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {
            handleRewardEditorClick(player, data, event.getInventory());
        } else if (title.equals(CommandRewardGUI.COMPLETION_COMMANDS_TITLE) ||
                title.equals(CommandRewardGUI.SCORE_COMMANDS_TITLE)) {
            handleCommandRewardGUIClick(player, data, event.isRightClick());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(RewardEditorGUI.COMPLETION_REWARDS_TITLE) ||
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE)) {

            // Chỉ cho phép drag trong slots 18-44
            for (int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize() &&
                        (slot < 18 || slot > 44)) {
                    event.setCancelled(true);
                    return;
                }
            }
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
                title.equals(RewardEditorGUI.EDIT_SCORE_REWARD_TITLE) ||
                title.equals(CommandRewardGUI.COMPLETION_COMMANDS_TITLE) ||
                title.equals(CommandRewardGUI.SCORE_COMMANDS_TITLE);
    }

    private void handleRewardMenuClick(Player player, String data, ItemMeta meta, boolean isLeftClick) {
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

        } else if (data.startsWith("back_dungeon_info_")) {
            String dungeonId = data.substring(18);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().openDungeonInfo(player, dungeon);
            }

        } else if (data.startsWith("preview_")) {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.rewards-preview-coming-soon"));

        } else if (data.startsWith("edit_tier_")) {
            if (!isLeftClick) {
                // Right click - delete
                String deleteData = meta.getPersistentDataContainer().get(
                        plugin.getDeleteKey(),
                        PersistentDataType.STRING
                );
                if (deleteData != null && deleteData.startsWith("delete_tier_")) {
                    String[] parts = deleteData.substring(12).split("_", 2);
                    if (parts.length >= 2) {
                        String dungeonId = parts[0];
                        try {
                            int score = Integer.parseInt(parts[1]);
                            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                            if (dungeon != null) {
                                DungeonRewards rewards = dungeon.getRewards();
                                if (rewards != null) {
                                    rewards.getScoreBasedRewards().remove(score);
                                    plugin.getDungeonManager().saveDungeon(dungeon);
                                    player.sendMessage(plugin.getConfigManager().getMessage("admin.score-tier-removed", "score", String.valueOf(score)));
                                    plugin.getGUIManager().getRewardEditorGUI().openScoreRewardsEditor(player, dungeon);
                                }
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", parts[1]));
                        }
                    }
                }
                return;
            }

            // Left click - edit
            String[] parts = data.substring(10).split("_", 2);
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        editingSessions.put(player.getName(), dungeonId);
                        editingScores.put(player.getName(), score);
                        plugin.getGUIManager().getRewardEditorGUI().openScoreTierEditor(player, dungeon, score);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", parts[1]));
                }
            }

        } else if (data.startsWith("add_score_tier_")) {
            String dungeonId = data.substring(15);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().getCommandRewardGUI().startAddingScoreTier(player, dungeonId);
            }
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
                plugin.getGUIManager().getRewardEditorGUI().openRewardMenu(player, dungeon);
            }

            // ✅ FIX: Đổi prefix thành "add_cmd_completion_"
        } else if (data.startsWith("add_cmd_completion_")) {
            String dungeonId = data.substring(19);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                plugin.getGUIManager().getCommandRewardGUI().openCompletionCommands(player, dungeon);
            }

            // ✅ FIX: Đổi prefix thành "add_cmd_tier_"
        } else if (data.startsWith("add_cmd_tier_")) {
            String[] parts = data.substring(13).split("_", 2);
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        plugin.getGUIManager().getCommandRewardGUI().openScoreTierCommands(player, dungeon, score);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", parts[1]));
                }
            }

        } else if (data.startsWith("save_tier_")) {
            String[] parts = data.substring(10).split("_", 2);
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
                plugin.getGUIManager().getRewardEditorGUI().openScoreRewardsEditor(player, dungeon);
            }
        }
    }

    private void handleCommandRewardGUIClick(Player player, String data, boolean isRightClick) {
        CommandRewardGUI cmdGUI = plugin.getGUIManager().getCommandRewardGUI();
        RewardEditorGUI rewardGUI = plugin.getGUIManager().getRewardEditorGUI();

        if (data.startsWith("add_cmd_completion_")) {
            String dungeonId = data.substring(19);
            cmdGUI.startAddingCommand(player, dungeonId, null);

        } else if (data.startsWith("add_command_tier_")) {
            String[] parts = data.substring(17).split("_", 2);
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    cmdGUI.startAddingCommand(player, dungeonId, score);
                } catch (NumberFormatException ignored) {}
            }

        } else if (data.startsWith("cmd_completion_")) {
            if (isRightClick) {
                String[] parts = data.substring(15).split("_", 2);
                if (parts.length >= 2) {
                    String dungeonId = parts[0];
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        try {
                            int index = Integer.parseInt(parts[1]);
                            cmdGUI.removeCompletionCommand(player, dungeon, index);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } else if (data.startsWith("cmd_tier_")) {
            if (isRightClick) {
                String[] parts = data.substring(9).split("_", 3);
                if (parts.length >= 3) {
                    String dungeonId = parts[0];
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        try {
                            int score = Integer.parseInt(parts[1]);
                            int index = Integer.parseInt(parts[2]);
                            cmdGUI.removeScoreTierCommand(player, dungeon, score, index);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } else if (data.startsWith("back_reward_")) {
            String dungeonId = data.substring(12);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                rewardGUI.openRewardMenu(player, dungeon);
            }
        } else if (data.startsWith("back_tier_editor_")) {
            String[] parts = data.substring(17).split("_", 2);
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        rewardGUI.openScoreTierEditor(player, dungeon, score);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } else if (data.startsWith("save_close_commands_")) {
            String dungeonId = data.substring(20);
            Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon != null) {
                rewardGUI.openCompletionRewardsEditor(player, dungeon);
            }
        } else if (data.startsWith("save_close_tier_commands_")) {
            String[] parts = data.substring(25).split("_", 2);
            if (parts.length >= 2) {
                String dungeonId = parts[0];
                try {
                    int score = Integer.parseInt(parts[1]);
                    Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (dungeon != null) {
                        rewardGUI.openScoreTierEditor(player, dungeon, score);
                    }
                } catch (NumberFormatException ignored) {}
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
                rewardItems.add(item.clone());
            }
        }

        scoreReward.setRewardItems(rewardItems);
        plugin.getDungeonManager().saveDungeon(dungeon);

        plugin.getLogger().info("Saved " + rewardItems.size() + " reward items for score tier " + score + " in dungeon " + dungeonId);
    }
}