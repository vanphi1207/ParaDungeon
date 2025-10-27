package me.ihqqq.paraDungeon.gui;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.listeners.CommandInputListener;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRewards;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * GUI để quản lý command rewards
 */
public class CommandRewardGUI {

    private final ParaDungeon plugin;

    // GUI Titles
    public static final String COMPLETION_COMMANDS_TITLE = "§8▎ §6§lLệnh Hoàn Thành";
    public static final String SCORE_COMMANDS_TITLE = "§8▎ §6§lLệnh Theo Điểm";

    // Tracking pending input
    private final Map<UUID, PendingCommandInput> pendingInputs;

    public CommandRewardGUI(ParaDungeon plugin) {
        this.plugin = plugin;
        this.pendingInputs = new HashMap<>();
    }

    /**
     * Open completion commands editor
     */
    public void openCompletionCommands(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, COMPLETION_COMMANDS_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        // Info panel
        ItemStack info = createItem(
                Material.PAPER,
                "§6§lHướng Dẫn Lệnh",
                "§7Thêm các lệnh sẽ được thực thi",
                "§7khi người chơi hoàn thành phó bản",
                "",
                "§e✦ §7Placeholders:",
                "  §e{player} §7- Tên người chơi",
                "  §e{score} §7- Điểm số đạt được",
                "",
                "§7Ví dụ:",
                "  §eeco give {player} 1000",
                "  §ecrate givekey {player} diamond 1"
        );
        gui.setItem(4, info);

        // Add new command button
        ItemStack addCmd = createItemWithData(
                Material.WRITABLE_BOOK,
                "§a§l+ THÊM LỆNH",
                "add_cmd_completion_" + dungeon.getId(),
                "§7Click để thêm lệnh mới",
                "",
                "§e▶ Nhập lệnh trong chat!"
        );
        gui.setItem(8, addCmd);

        // Display existing commands
        List<String> commands = rewards.getCompletionCommands();
        int slot = 18;
        for (int i = 0; i < commands.size() && slot < 45; i++) {
            String cmd = commands.get(i);

            ItemStack cmdItem = createItemWithData(
                    Material.COMMAND_BLOCK,
                    "§6Lệnh #" + (i + 1),
                    "cmd_completion_" + dungeon.getId() + "_" + i,
                    "§e/" + cmd,
                    "",
                    "§c▶ Click phải để xóa"
            );
            gui.setItem(slot++, cmdItem);
        }

        // Back button
        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_reward_" + dungeon.getId(),
                "§7Về menu phần thưởng"
        );
        gui.setItem(45, back);

        // Save button
        ItemStack save = createItemWithData(
                Material.LIME_WOOL,
                "§a§l✔ LƯU VÀ ĐÓNG",
                "save_close_commands_" + dungeon.getId(),
                "§7Lưu và đóng menu"
        );
        gui.setItem(49, save);

        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    /**
     * Open score tier commands editor
     */
    public void openScoreTierCommands(Player player, Dungeon dungeon, int score) {
        Inventory gui = Bukkit.createInventory(null, 54, SCORE_COMMANDS_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

        // Info panel
        ItemStack info = createItem(
                Material.PAPER,
                "§6§lLệnh Cho " + score + " Điểm",
                "§7Các lệnh sẽ được thực thi",
                "§7khi đạt " + score + " điểm",
                "",
                "§e✦ §7Placeholders:",
                "  §e{player} §7- Tên người chơi",
                "  §e{score} §7- Điểm số đạt được"
        );
        gui.setItem(4, info);

        // Add new command button
        ItemStack addCmd = createItemWithData(
                Material.WRITABLE_BOOK,
                "§a§l+ THÊM LỆNH",
                "add_cmd_tier_" + dungeon.getId() + "_" + score,
                "§7Click để thêm lệnh mới",
                "",
                "§e▶ Nhập lệnh trong chat!"
        );
        gui.setItem(8, addCmd);

        // Display existing commands
        List<String> commands = scoreReward.getCommands();
        int slot = 18;
        for (int i = 0; i < commands.size() && slot < 45; i++) {
            String cmd = commands.get(i);

            ItemStack cmdItem = createItemWithData(
                    Material.COMMAND_BLOCK,
                    "§6Lệnh #" + (i + 1),
                    "cmd_tier_" + dungeon.getId() + "_" + score + "_" + i,
                    "§e/" + cmd,
                    "",
                    "§c▶ Click phải để xóa"
            );
            gui.setItem(slot++, cmdItem);
        }

        // Back button
        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_tier_editor_" + dungeon.getId() + "_" + score,
                "§7Về menu chỉnh sửa phần thưởng"
        );
        gui.setItem(45, back);

        // Save button
        ItemStack save = createItemWithData(
                Material.LIME_WOOL,
                "§a§l✔ LƯU VÀ ĐÓNG",
                "save_close_tier_commands_" + dungeon.getId() + "_" + score,
                "§7Lưu và đóng menu"
        );
        gui.setItem(49, save);

        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    /**
     * Handle adding command for completion rewards
     */
    public void startAddingCommand(Player player, String dungeonId, Integer scoreRequirement) {
        player.closeInventory();

        PendingCommandInput input = new PendingCommandInput(dungeonId, scoreRequirement);
        pendingInputs.put(player.getUniqueId(), input);

        player.sendMessage(plugin.getConfigManager().getMessage("admin.command-prompt"));
        player.sendMessage("§7Nhập lệnh §e(không cần dấu /) §7hoặc §ccancel §7để hủy:");
        player.sendMessage("§7Ví dụ: §eeco give {player} 1000");
    }

    /**
     * Handle chat input for commands
     */
    public boolean handleChatInput(Player player, String message) {
        PendingCommandInput input = pendingInputs.get(player.getUniqueId());
        if (input == null) return false;

        pendingInputs.remove(player.getUniqueId());

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getConfigManager().getMessage("admin.command-cancelled"));

            // Reopen GUI
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                reopenCommandGUI(player, input);
            }, 1L);
            return true;
        }

        // Remove leading slash if present
        String command = message.startsWith("/") ? message.substring(1) : message;

        // Add command to appropriate reward
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(input.dungeonId);
        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", input.dungeonId));
            return true;
        }

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        if (input.scoreRequirement == null) {
            // Completion command
            rewards.getCompletionCommands().add(command);
        } else {
            // Score tier command
            DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(input.scoreRequirement);
            if (scoreReward == null) {
                scoreReward = new DungeonRewards.ScoreReward();
                rewards.addScoreReward(input.scoreRequirement, scoreReward);
            }
            scoreReward.getCommands().add(command);
        }

        plugin.getDungeonManager().saveDungeon(dungeon);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.command-added", "command", command));

        // Reopen GUI
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reopenCommandGUI(player, input);
        }, 1L);

        return true;
    }

    /**
     * Cancel pending input
     */
    public void cancelPendingInput(UUID playerId) {
        pendingInputs.remove(playerId);
    }

    /**
     * Check if player has pending input
     */
    public boolean hasPendingInput(UUID playerId) {
        return pendingInputs.containsKey(playerId);
    }

    /**
     * Reopen appropriate command GUI after input
     */
    private void reopenCommandGUI(Player player, PendingCommandInput input) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(input.dungeonId);
        if (dungeon == null) return;

        if (input.scoreRequirement == null) {
            openCompletionCommands(player, dungeon);
        } else {
            openScoreTierCommands(player, dungeon, input.scoreRequirement);
        }
    }

    /**
     * Remove command from completion rewards
     */
    public void removeCompletionCommand(Player player, Dungeon dungeon, int index) {
        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        List<String> commands = rewards.getCompletionCommands();
        if (index >= 0 && index < commands.size()) {
            String removed = commands.remove(index);
            plugin.getDungeonManager().saveDungeon(dungeon);
            player.sendMessage(plugin.getConfigManager().getMessage("admin.command-removed", "command", removed));

            // Reopen GUI
            Bukkit.getScheduler().runTask(plugin, () -> openCompletionCommands(player, dungeon));
        }
    }

    /**
     * Remove command from score tier
     */
    public void removeScoreTierCommand(Player player, Dungeon dungeon, int score, int index) {
        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) return;

        List<String> commands = scoreReward.getCommands();
        if (index >= 0 && index < commands.size()) {
            String removed = commands.remove(index);
            plugin.getDungeonManager().saveDungeon(dungeon);
            player.sendMessage(plugin.getConfigManager().getMessage("admin.command-removed", "command", removed));

            // Reopen GUI
            Bukkit.getScheduler().runTask(plugin, () -> openScoreTierCommands(player, dungeon, score));
        }
    }

    // ==================== UTILITY METHODS ====================

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
            meta.getPersistentDataContainer().set(
                    plugin.getDungeonKey(),
                    PersistentDataType.STRING,
                    data
            );
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

    /**
     * Helper class to track pending command input
     */
    private static class PendingCommandInput {
        final String dungeonId;
        final Integer scoreRequirement;

        PendingCommandInput(String dungeonId, Integer scoreRequirement) {
            this.dungeonId = dungeonId;
            this.scoreRequirement = scoreRequirement;
        }
    }
}