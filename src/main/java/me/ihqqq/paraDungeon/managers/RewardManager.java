package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRewards;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class RewardManager {

    private final ParaDungeon plugin;

    public RewardManager(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    public void giveRewards(Player player, Dungeon dungeon, int score) {
        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        List<String> receivedRewards = new ArrayList<>();

        // Give completion rewards - ItemStack version (priority)
        if (!rewards.getCompletionRewardItems().isEmpty()) {
            for (ItemStack item : rewards.getCompletionRewardItems()) {
                giveItemStack(player, item.clone());
                receivedRewards.add(getItemDisplayName(item));
            }
        } else {
            // Fallback to legacy string format
            for (String itemStr : rewards.getCompletionItems()) {
                giveItem(player, itemStr);
                receivedRewards.add(itemStr);
            }
        }

        // Give completion commands
        for (String command : rewards.getCompletionCommands()) {
            executeCommand(player, command);
        }

        // Give score-based rewards
        DungeonRewards.ScoreReward scoreReward = rewards.getRewardForScore(score);
        if (scoreReward != null) {
            // ItemStack version (priority)
            if (!scoreReward.getRewardItems().isEmpty()) {
                for (ItemStack item : scoreReward.getRewardItems()) {
                    giveItemStack(player, item.clone());
                    receivedRewards.add(getItemDisplayName(item));
                }
            } else {
                // Fallback to legacy
                for (String itemStr : scoreReward.getItems()) {
                    giveItem(player, itemStr);
                    receivedRewards.add(itemStr);
                }
            }

            for (String command : scoreReward.getCommands()) {
                executeCommand(player, command);
            }
        }

        // Send reward message
        if (!receivedRewards.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("rewards.received",
                    "rewards", String.join(", ", receivedRewards)));
        }

        player.sendMessage(plugin.getConfigManager().getMessage("rewards.score",
                "score", String.valueOf(score)));
    }

    private void giveItemStack(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            // Inventory full, drop item
            player.getWorld().dropItem(player.getLocation(), item);
            player.sendMessage(plugin.getConfigManager().getMessage("rewards.dropped"));
        } else {
            player.getInventory().addItem(item);
        }
    }

    private void giveItem(Player player, String itemStr) {
        try {
            String[] parts = itemStr.split(" ");
            Material material = Material.valueOf(parts[0]);
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

            ItemStack item = new ItemStack(material, amount);
            giveItemStack(player, item);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid item format: " + itemStr);
        }
    }

    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }

    private void executeCommand(Player player, String command) {
        String processedCommand = command.replace("{player}", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
    }
}