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
    private final Map<UUID, PendingCommandInput> pendingInputs;
    private final Map<UUID, PendingScoreTierInput> pendingScoreTierInputs;

    public CommandRewardGUI(ParaDungeon plugin) {
        this.plugin = plugin;
        this.pendingInputs = new HashMap<>();
        this.pendingScoreTierInputs = new HashMap<>();
    }

    public void openCompletionCommands(Player player, Dungeon dungeon) {
        String title = plugin.getConfigManager().getGUITitle("command-rewards.completion.title");
        int size = plugin.getConfigManager().getGUISize("command-rewards.completion.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        ItemStack info = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.completion.items.info.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.completion.items.info.name"),
                plugin.getConfigManager().getGUIItemLore("command-rewards.completion.items.info.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.completion.items.info.slot"), info);

        ItemStack addCmd = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.completion.items.add-command.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.completion.items.add-command.name"),
                "add_cmd_completion_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("command-rewards.completion.items.add-command.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.completion.items.add-command.slot"), addCmd);

        List<String> commands = rewards.getCompletionCommands();
        int slot = plugin.getConfigManager().getGUIInt("command-rewards.completion.items.command-display.start-slot", 18);
        int endSlot = plugin.getConfigManager().getGUIInt("command-rewards.completion.items.command-display.end-slot", 44);
        for (int i = 0; i < commands.size() && slot <= endSlot; i++) {
            String cmd = commands.get(i);
            List<String> cmdLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("command-rewards.completion.items.command-display.lore"));
            cmdLore.replaceAll(line -> line.replace("{command}", cmd));
            String cmdName = plugin.getConfigManager().getGUIItemName("command-rewards.completion.items.command-display.name")
                    .replace("{index}", String.valueOf(i + 1));
            ItemStack cmdItem = createItemWithData(
                    Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.completion.items.command-display.material")),
                    cmdName,
                    "cmd_completion_" + dungeon.getId() + "_" + i,
                    cmdLore.toArray(new String[0])
            );
            gui.setItem(slot++, cmdItem);
        }

        ItemStack back = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.completion.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.completion.items.back-button.name"),
                "back_reward_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("command-rewards.completion.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.completion.items.back-button.slot"), back);

        ItemStack save = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.completion.items.save-close-button.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.completion.items.save-close-button.name"),
                "save_close_commands_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("command-rewards.completion.items.save-close-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.completion.items.save-close-button.slot"), save);

        if (plugin.getConfigManager().isGUIFillerEnabled("command-rewards.completion.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("command-rewards.completion.filler")));
        }
        player.openInventory(gui);
    }

    public void openScoreTierCommands(Player player, Dungeon dungeon, int score) {
        String title = plugin.getConfigManager().getGUITitle("command-rewards.score-tier.title");
        int size = plugin.getConfigManager().getGUISize("command-rewards.score-tier.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

        List<String> infoLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("command-rewards.score-tier.items.info.lore"));
        infoLore.replaceAll(line -> line.replace("{score}", String.valueOf(score)));
        String infoName = plugin.getConfigManager().getGUIItemName("command-rewards.score-tier.items.info.name")
                .replace("{score}", String.valueOf(score));
        ItemStack info = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.score-tier.items.info.material")),
                infoName,
                infoLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.score-tier.items.info.slot"), info);

        ItemStack addCmd = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.score-tier.items.add-command.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.score-tier.items.add-command.name"),
                "add_command_tier_" + dungeon.getId() + "_" + score,
                plugin.getConfigManager().getGUIItemLore("command-rewards.score-tier.items.add-command.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.score-tier.items.add-command.slot"), addCmd);

        List<String> commands = scoreReward.getCommands();
        int slot = plugin.getConfigManager().getGUIInt("command-rewards.score-tier.items.command-display.start-slot", 18);
        int endSlot = plugin.getConfigManager().getGUIInt("command-rewards.score-tier.items.command-display.end-slot", 44);
        for (int i = 0; i < commands.size() && slot <= endSlot; i++) {
            String cmd = commands.get(i);
            List<String> cmdLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("command-rewards.score-tier.items.command-display.lore"));
            cmdLore.replaceAll(line -> line.replace("{command}", cmd));
            String cmdName = plugin.getConfigManager().getGUIItemName("command-rewards.score-tier.items.command-display.name")
                    .replace("{index}", String.valueOf(i + 1));
            ItemStack cmdItem = createItemWithData(
                    Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.score-tier.items.command-display.material")),
                    cmdName,
                    "cmd_tier_" + dungeon.getId() + "_" + score + "_" + i,
                    cmdLore.toArray(new String[0])
            );
            gui.setItem(slot++, cmdItem);
        }

        ItemStack back = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.score-tier.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.score-tier.items.back-button.name"),
                "back_tier_editor_" + dungeon.getId() + "_" + score,
                plugin.getConfigManager().getGUIItemLore("command-rewards.score-tier.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.score-tier.items.back-button.slot"), back);

        ItemStack save = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("command-rewards.score-tier.items.save-close-button.material")),
                plugin.getConfigManager().getGUIItemName("command-rewards.score-tier.items.save-close-button.name"),
                "save_close_tier_commands_" + dungeon.getId() + "_" + score,
                plugin.getConfigManager().getGUIItemLore("command-rewards.score-tier.items.save-close-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("command-rewards.score-tier.items.save-close-button.slot"), save);

        if (plugin.getConfigManager().isGUIFillerEnabled("command-rewards.score-tier.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("command-rewards.score-tier.filler")));
        }
        player.openInventory(gui);
    }

    public void startAddingCommand(Player player, String dungeonId, Integer scoreRequirement) {
        player.closeInventory();

        // ✅ FIX: Kiểm tra dungeon tồn tại TRƯỚC KHI lưu input
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }

        PendingCommandInput input = new PendingCommandInput(dungeonId, scoreRequirement);
        pendingInputs.put(player.getUniqueId(), input);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.command-prompt"));
    }

    public void startAddingScoreTier(Player player, String dungeonId) {
        player.closeInventory();

        // ✅ FIX: Kiểm tra dungeon
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }

        PendingScoreTierInput input = new PendingScoreTierInput(dungeonId);
        pendingScoreTierInputs.put(player.getUniqueId(), input);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.score-tier-prompt"));
    }

    public boolean handleChatInput(Player player, String message) {
        // Check for score tier input first
        PendingScoreTierInput scoreTierInput = pendingScoreTierInputs.get(player.getUniqueId());
        if (scoreTierInput != null) {
            pendingScoreTierInputs.remove(player.getUniqueId());

            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(plugin.getConfigManager().getMessage("admin.command-cancelled"));
                Dungeon dungeon = plugin.getDungeonManager().getDungeon(scoreTierInput.dungeonId);
                if (dungeon != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            plugin.getGUIManager().getRewardEditorGUI().openScoreRewardsEditor(player, dungeon), 1L);
                }
                return true;
            }

            try {
                int score = Integer.parseInt(message);
                if (score <= 0) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", message));
                    return true;
                }

                Dungeon dungeon = plugin.getDungeonManager().getDungeon(scoreTierInput.dungeonId);
                if (dungeon == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", scoreTierInput.dungeonId));
                    return true;
                }

                DungeonRewards rewards = dungeon.getRewards();
                if (rewards == null) {
                    rewards = new DungeonRewards();
                    dungeon.setRewards(rewards);
                }

                if (rewards.getScoreBasedRewards().containsKey(score)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("admin.score-tier-exists", "score", String.valueOf(score)));
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            plugin.getGUIManager().getRewardEditorGUI().openScoreRewardsEditor(player, dungeon), 1L);
                    return true;
                }

                rewards.addScoreReward(score, new DungeonRewards.ScoreReward());
                plugin.getDungeonManager().saveDungeon(dungeon);
                player.sendMessage(plugin.getConfigManager().getMessage("admin.score-tier-added", "score", String.valueOf(score)));

                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        plugin.getGUIManager().getRewardEditorGUI().openScoreTierEditor(player, dungeon, score), 1L);

                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", message));
                return true;
            }
        }

        // Handle command input
        PendingCommandInput input = pendingInputs.get(player.getUniqueId());
        if (input == null) return false;

        pendingInputs.remove(player.getUniqueId());

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getConfigManager().getMessage("admin.command-cancelled"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> reopenCommandGUI(player, input), 1L);
            return true;
        }

        String command = message.startsWith("/") ? message.substring(1) : message;

        // ✅ FIX: Kiểm tra dungeon lại lần nữa
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
            rewards.getCompletionCommands().add(command);
        } else {
            DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().computeIfAbsent(
                    input.scoreRequirement,
                    k -> new DungeonRewards.ScoreReward()
            );
            scoreReward.getCommands().add(command);
        }

        plugin.getDungeonManager().saveDungeon(dungeon);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.command-added", "command", command));

        Bukkit.getScheduler().runTaskLater(plugin, () -> reopenCommandGUI(player, input), 1L);

        return true;
    }

    public void cancelPendingInput(UUID playerId) {
        pendingInputs.remove(playerId);
        pendingScoreTierInputs.remove(playerId);
    }

    public boolean hasPendingInput(UUID playerId) {
        return pendingInputs.containsKey(playerId) || pendingScoreTierInputs.containsKey(playerId);
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

    private static class PendingScoreTierInput {
        final String dungeonId;

        PendingScoreTierInput(String dungeonId) {
            this.dungeonId = dungeonId;
        }
    }
}