package me.ihqqq.paraDungeon.gui;

import me.ihqqq.paraDungeon.ParaDungeon;
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

public class CommandRewardGUI {

    private final ParaDungeon plugin;
    public static final String COMPLETION_COMMANDS_TITLE = "§8▎ §6§lLệnh Hoàn Thành";
    public static final String SCORE_COMMANDS_TITLE = "§8▎ §6§lLệnh Theo Điểm";
    private final Map<UUID, PendingCommandInput> pendingInputs;

    public CommandRewardGUI(ParaDungeon plugin) {
        this.plugin = plugin;
        this.pendingInputs = new HashMap<>();
    }

    public void openCompletionCommands(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, COMPLETION_COMMANDS_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

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

        ItemStack addCmd = createItemWithData(
                Material.WRITABLE_BOOK,
                "§a§l+ THÊM LỆNH",
                "add_cmd_completion_" + dungeon.getId(),
                "§7Click để thêm lệnh mới",
                "",
                "§e▶ Nhập lệnh trong chat!"
        );
        gui.setItem(8, addCmd);

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

        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_reward_" + dungeon.getId(),
                "§7Về menu phần thưởng"
        );
        gui.setItem(45, back);

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

    public void openScoreTierCommands(Player player, Dungeon dungeon, int score) {
        Inventory gui = Bukkit.createInventory(null, 54, SCORE_COMMANDS_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

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

        ItemStack addCmd = createItemWithData(
                Material.WRITABLE_BOOK,
                "§a§l+ THÊM LỆNH",
                "add_cmd_tier_" + dungeon.getId() + "_" + score,
                "§7Click để thêm lệnh mới",
                "",
                "§e▶ Nhập lệnh trong chat!"
        );
        gui.setItem(8, addCmd);

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

        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_tier_editor_" + dungeon.getId() + "_" + score,
                "§7Về menu chỉnh sửa phần thưởng"
        );
        gui.setItem(45, back);

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

    public void startAddingCommand(Player player, String dungeonId, Integer scoreRequirement) {
        player.closeInventory();
        PendingCommandInput input = new PendingCommandInput(dungeonId, scoreRequirement);
        pendingInputs.put(player.getUniqueId(), input);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.command-prompt"));
    }

    public boolean handleChatInput(Player player, String message) {
        PendingCommandInput input = pendingInputs.get(player.getUniqueId());
        if (input == null) return false;

        pendingInputs.remove(player.getUniqueId());

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getConfigManager().getMessage("admin.command-cancelled"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> reopenCommandGUI(player, input), 1L);
            return true;
        }

        String command = message.startsWith("/") ? message.substring(1) : message;

        // === ĐOẠN SỬA LỖI QUAN TRỌNG NHẤT ===
        // Lấy ID phó bản từ 'input' đã được lưu trữ trước đó, không phải từ message chat.
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(input.dungeonId);
        if (dungeon == null) {
            // Bây giờ nếu có lỗi, nó sẽ hiển thị đúng ID, ví dụ: "test"
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", input.dungeonId));
            return true;
        }

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        if (input.scoreRequirement == null) {
            rewards.getCompletionCommands().add(command);
        } else {
            DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().computeIfAbsent(input.scoreRequirement, k -> new DungeonRewards.ScoreReward());
            scoreReward.getCommands().add(command);
        }

        plugin.getDungeonManager().saveDungeon(dungeon);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.command-added", "command", command));

        Bukkit.getScheduler().runTaskLater(plugin, () -> reopenCommandGUI(player, input), 1L);

        return true;
    }

    public void cancelPendingInput(UUID playerId) {
        pendingInputs.remove(playerId);
    }

    public boolean hasPendingInput(UUID playerId) {
        return pendingInputs.containsKey(playerId);
    }

    private void reopenCommandGUI(Player player, PendingCommandInput input) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(input.dungeonId);
        if (dungeon == null) return;

        if (input.scoreRequirement == null) {
            openCompletionCommands(player, dungeon);
        } else {
            openScoreTierCommands(player, dungeon, input.scoreRequirement);
        }
    }

    public void removeCompletionCommand(Player player, Dungeon dungeon, int index) {
        DungeonRewards rewards = dungeon.getRewards();
        if (rewards != null) {
            List<String> commands = rewards.getCompletionCommands();
            if (index >= 0 && index < commands.size()) {
                String removed = commands.remove(index);
                plugin.getDungeonManager().saveDungeon(dungeon);
                player.sendMessage(plugin.getConfigManager().getMessage("admin.command-removed", "command", removed));
                openCompletionCommands(player, dungeon);
            }
        }
    }

    public void removeScoreTierCommand(Player player, Dungeon dungeon, int score, int index) {
        DungeonRewards rewards = dungeon.getRewards();
        if (rewards != null) {
            DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
            if (scoreReward != null) {
                List<String> commands = scoreReward.getCommands();
                if (index >= 0 && index < commands.size()) {
                    String removed = commands.remove(index);
                    plugin.getDungeonManager().saveDungeon(dungeon);
                    player.sendMessage(plugin.getConfigManager().getMessage("admin.command-removed", "command", removed));
                    openScoreTierCommands(player, dungeon, score);
                }
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(new ArrayList<>(Arrays.asList(lore)));
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

    private static class PendingCommandInput {
        final String dungeonId;
        final Integer scoreRequirement;

        PendingCommandInput(String dungeonId, Integer scoreRequirement) {
            this.dungeonId = dungeonId;
            this.scoreRequirement = scoreRequirement;
        }
    }
}