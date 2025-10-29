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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardEditorGUI {

    private final ParaDungeon plugin;

    public RewardEditorGUI(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    public void openRewardMenu(Player player, Dungeon dungeon) {
        String title = plugin.getConfigManager().getGUITitle("reward-editor.main.title");
        int size = plugin.getConfigManager().getGUISize("reward-editor.main.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        ItemStack completionRewards = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.main.items.completion-rewards.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.main.items.completion-rewards.name"),
                "completion_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.main.items.completion-rewards.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.main.items.completion-rewards.slot"), completionRewards);

        List<String> scoreLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("reward-editor.main.items.score-rewards.lore"));
        String scoreInfo = getScoreRewardInfo(dungeon);
        scoreLore.replaceAll(line -> line.replace("{score_info}", scoreInfo));
        ItemStack scoreRewards = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.main.items.score-rewards.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.main.items.score-rewards.name"),
                "score_" + dungeon.getId(),
                scoreLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.main.items.score-rewards.slot"), scoreRewards);

        ItemStack preview = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.main.items.preview.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.main.items.preview.name"),
                "preview_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.main.items.preview.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.main.items.preview.slot"), preview);

        ItemStack back = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.main.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.main.items.back-button.name"),
                "back_dungeon_info_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.main.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.main.items.back-button.slot"), back);

        if (plugin.getConfigManager().isGUIFillerEnabled("reward-editor.main.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("reward-editor.main.filler")));
        }
        player.openInventory(gui);
    }

    public void openCompletionRewardsEditor(Player player, Dungeon dungeon) {
        String title = plugin.getConfigManager().getGUITitle("reward-editor.completion-rewards.title");
        int size = plugin.getConfigManager().getGUISize("reward-editor.completion-rewards.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        // Info panel
        ItemStack info = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.completion-rewards.items.info.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.completion-rewards.items.info.name"),
                plugin.getConfigManager().getGUIItemLore("reward-editor.completion-rewards.items.info.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.completion-rewards.items.info.slot"), info);

        List<String> cmdLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("reward-editor.completion-rewards.items.command-info.lore"));
        cmdLore.replaceAll(line -> line.replace("{count}", String.valueOf(rewards.getCompletionCommands().size())));
        ItemStack cmdInfo = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.completion-rewards.items.command-info.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.completion-rewards.items.command-info.name"),
                "add_cmd_completion_" + dungeon.getId(),
                cmdLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.completion-rewards.items.command-info.slot"), cmdInfo);

        // Load existing rewards
        int startSlot = plugin.getConfigManager().getGUIInt("reward-editor.completion-rewards.items.reward-slots.start", 18);
        int endSlot = plugin.getConfigManager().getGUIInt("reward-editor.completion-rewards.items.reward-slots.end", 44);
        List<ItemStack> currentRewardItems = rewards.getCompletionRewardItems();
        for (int i = 0; i < currentRewardItems.size() && startSlot + i <= endSlot; i++) {
            gui.setItem(startSlot + i, currentRewardItems.get(i));
        }

        // Save button
        ItemStack save = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.completion-rewards.items.save-button.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.completion-rewards.items.save-button.name"),
                "save_completion_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.completion-rewards.items.save-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.completion-rewards.items.save-button.slot"), save);

        // Back button
        ItemStack back = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.completion-rewards.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.completion-rewards.items.back-button.name"),
                "back_reward_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.completion-rewards.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.completion-rewards.items.back-button.slot"), back);

        if (plugin.getConfigManager().isGUIFillerEnabled("reward-editor.completion-rewards.filler")) {
            int fillerStart = plugin.getConfigManager().getGUIInt("reward-editor.completion-rewards.filler.start-slot", 0);
            int fillerEnd = plugin.getConfigManager().getGUIInt("reward-editor.completion-rewards.filler.end-slot", 17);
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("reward-editor.completion-rewards.filler")), fillerStart, fillerEnd);
        }
        player.openInventory(gui);
    }

    public void openScoreRewardsEditor(Player player, Dungeon dungeon) {
        String title = plugin.getConfigManager().getGUITitle("reward-editor.score-rewards.title");
        int size = plugin.getConfigManager().getGUISize("reward-editor.score-rewards.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        ItemStack info = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-rewards.items.info.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.score-rewards.items.info.name"),
                plugin.getConfigManager().getGUIItemLore("reward-editor.score-rewards.items.info.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-rewards.items.info.slot"), info);

        ItemStack addTier = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-rewards.items.add-tier.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.score-rewards.items.add-tier.name"),
                "add_score_tier_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.score-rewards.items.add-tier.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-rewards.items.add-tier.slot"), addTier);

        Map<Integer, DungeonRewards.ScoreReward> scoreRewards = rewards.getScoreBasedRewards();
        int slot = plugin.getConfigManager().getGUIInt("reward-editor.score-rewards.items.tier-display.start-slot", 18);
        int endSlot = plugin.getConfigManager().getGUIInt("reward-editor.score-rewards.items.tier-display.end-slot", 44);
        for (Map.Entry<Integer, DungeonRewards.ScoreReward> entry : scoreRewards.entrySet()) {
            int score = entry.getKey();
            DungeonRewards.ScoreReward reward = entry.getValue();

            List<String> tierLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("reward-editor.score-rewards.items.tier-display.lore"));
            tierLore.replaceAll(line -> line
                    .replace("{score}", String.valueOf(score))
                    .replace("{items}", String.valueOf(reward.getRewardItems().size()))
                    .replace("{commands}", String.valueOf(reward.getCommands().size())));
            
            String tierName = plugin.getConfigManager().getGUIItemName("reward-editor.score-rewards.items.tier-display.name")
                    .replace("{score}", String.valueOf(score));
            
            ItemStack tierItem = createItemWithData(
                    Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-rewards.items.tier-display.material")),
                    tierName,
                    "edit_tier_" + dungeon.getId() + "_" + score,
                    tierLore.toArray(new String[0])
            );

            ItemMeta tierMeta = tierItem.getItemMeta();
            if (tierMeta != null) {
                tierMeta.getPersistentDataContainer().set(
                        plugin.getDeleteKey(),
                        PersistentDataType.STRING,
                        "delete_tier_" + dungeon.getId() + "_" + score
                );
                tierItem.setItemMeta(tierMeta);
            }
            gui.setItem(slot++, tierItem);

            if (slot >= endSlot) break;
        }

        ItemStack back = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-rewards.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.score-rewards.items.back-button.name"),
                "back_reward_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.score-rewards.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-rewards.items.back-button.slot"), back);

        if (plugin.getConfigManager().isGUIFillerEnabled("reward-editor.score-rewards.filler")) {
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("reward-editor.score-rewards.filler")));
        }
        player.openInventory(gui);
    }

    public void openScoreTierEditor(Player player, Dungeon dungeon, int score) {
        String title = plugin.getConfigManager().getGUITitle("reward-editor.score-tier-editor.title");
        int size = plugin.getConfigManager().getGUISize("reward-editor.score-tier-editor.size");
        Inventory gui = Bukkit.createInventory(null, size, title);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

        String infoName = plugin.getConfigManager().getGUIItemName("reward-editor.score-tier-editor.items.info.name")
                .replace("{score}", String.valueOf(score));
        ItemStack info = createItem(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-tier-editor.items.info.material")),
                infoName,
                plugin.getConfigManager().getGUIItemLore("reward-editor.score-tier-editor.items.info.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-tier-editor.items.info.slot"), info);

        List<String> cmdLore = new ArrayList<>(plugin.getConfigManager().getGUIItemLore("reward-editor.score-tier-editor.items.command-info.lore"));
        cmdLore.replaceAll(line -> line.replace("{count}", String.valueOf(scoreReward.getCommands().size())));
        ItemStack cmdInfo = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-tier-editor.items.command-info.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.score-tier-editor.items.command-info.name"),
                "add_cmd_tier_" + dungeon.getId() + "_" + score,
                cmdLore.toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-tier-editor.items.command-info.slot"), cmdInfo);

        int startSlot = plugin.getConfigManager().getGUIInt("reward-editor.score-tier-editor.items.reward-slots.start", 18);
        int endSlot = plugin.getConfigManager().getGUIInt("reward-editor.score-tier-editor.items.reward-slots.end", 44);
        List<ItemStack> currentRewardItems = scoreReward.getRewardItems();
        for (int i = 0; i < currentRewardItems.size() && startSlot + i <= endSlot; i++) {
            gui.setItem(startSlot + i, currentRewardItems.get(i));
        }

        ItemStack save = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-tier-editor.items.save-button.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.score-tier-editor.items.save-button.name"),
                "save_tier_" + dungeon.getId() + "_" + score,
                plugin.getConfigManager().getGUIItemLore("reward-editor.score-tier-editor.items.save-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-tier-editor.items.save-button.slot"), save);

        ItemStack back = createItemWithData(
                Material.valueOf(plugin.getConfigManager().getGUIItemMaterial("reward-editor.score-tier-editor.items.back-button.material")),
                plugin.getConfigManager().getGUIItemName("reward-editor.score-tier-editor.items.back-button.name"),
                "back_score_rewards_" + dungeon.getId(),
                plugin.getConfigManager().getGUIItemLore("reward-editor.score-tier-editor.items.back-button.lore").toArray(new String[0])
        );
        gui.setItem(plugin.getConfigManager().getGUIItemSlot("reward-editor.score-tier-editor.items.back-button.slot"), back);

        if (plugin.getConfigManager().isGUIFillerEnabled("reward-editor.score-tier-editor.filler")) {
            int fillerStart = plugin.getConfigManager().getGUIInt("reward-editor.score-tier-editor.filler.start-slot", 0);
            int fillerEnd = plugin.getConfigManager().getGUIInt("reward-editor.score-tier-editor.filler.end-slot", 17);
            fillEmptySlots(gui, Material.valueOf(plugin.getConfigManager().getGUIFillerMaterial("reward-editor.score-tier-editor.filler")), fillerStart, fillerEnd);
        }
        player.openInventory(gui);
    }

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
        fillEmptySlots(gui, material, 0, gui.getSize() - 1);
    }

    private void fillEmptySlots(Inventory gui, Material material, int start, int end) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int i = start; i <= end && i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    private String getScoreRewardInfo(Dungeon dungeon) {
        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null || rewards.getScoreBasedRewards().isEmpty()) {
            return plugin.getConfigManager().getGUIString("reward-editor.main.items.score-rewards.score-info-empty", "§7Chưa có mức điểm nào");
        }
        String template = plugin.getConfigManager().getGUIString("reward-editor.main.items.score-rewards.score-info-has", "§eSố mức: §a{count}");
        return template.replace("{count}", String.valueOf(rewards.getScoreBasedRewards().size()));
    }
}