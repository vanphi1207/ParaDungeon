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

    // Titles are now driven by gui.yml via GUIConfigManager

    public RewardEditorGUI(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    public void openRewardMenu(Player player, Dungeon dungeon) {
        int size = plugin.getGUIConfigManager().getInt("reward_editor.reward_menu.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, plugin.getGUIConfigManager().titleRewardMenu());

        // Completion rewards
        {
            String path = "reward_editor.reward_menu.items.completion";
            ItemStack item = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "CHEST"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lPhần Thưởng Hoàn Thành"),
                    "completion_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            int slot = plugin.getGUIConfigManager().getInt(path + ".slot", 11);
            gui.setItem(slot, item);
        }

        // Score rewards
        {
            String path = "reward_editor.reward_menu.items.score";
            List<String> lore = plugin.getGUIConfigManager().getColoredStringList(path + ".lore");
            int tierCount = dungeon.getRewards() == null ? 0 : dungeon.getRewards().getScoreBasedRewards().size();
            List<String> replaced = new ArrayList<>();
            for (String line : lore) replaced.add(line.replace("{scoreTierCount}", String.valueOf(tierCount)));
            ItemStack item = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "NETHER_STAR"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lPhần Thưởng Theo Điểm"),
                    "score_" + dungeon.getId(),
                    replaced.toArray(new String[0])
            );
            int slot = plugin.getGUIConfigManager().getInt(path + ".slot", 13);
            gui.setItem(slot, item);
        }

        // Preview (placeholder action)
        {
            String path = "reward_editor.reward_menu.items.preview";
            ItemStack item = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "BOOK"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lXem Trước Phần Thưởng"),
                    "preview_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            int slot = plugin.getGUIConfigManager().getInt(path + ".slot", 15);
            gui.setItem(slot, item);
        }

        // Back
        {
            String path = "reward_editor.reward_menu.items.back";
            ItemStack item = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "ARROW"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&c&lQuay Lại"),
                    "back_dungeon_info_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            int slot = plugin.getGUIConfigManager().getInt(path + ".slot", 22);
            gui.setItem(slot, item);
        }

        Material filler = plugin.getGUIConfigManager().getMaterial("reward_editor.filler.default", "GRAY_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openCompletionRewardsEditor(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.getGUIConfigManager().titleCompletionRewards());

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        // Info panel
        {
            String path = "reward_editor.completion_rewards.info";
            ItemStack info = createItem(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "PAPER"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lHướng Dẫn"),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 4), info);
        }

        // ✅ FIX: Đổi data key thành "add_cmd_completion_"
        {
            String path = "reward_editor.completion_rewards.add_commands";
            List<String> lore = plugin.getGUIConfigManager().getColoredStringList(path + ".lore");
            List<String> replaced = new ArrayList<>();
            for (String line : lore) replaced.add(line.replace("{count}", String.valueOf(rewards.getCompletionCommands().size())));
            ItemStack cmdInfo = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "COMMAND_BLOCK"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lCommand Rewards"),
                    "add_cmd_completion_" + dungeon.getId(),
                    replaced.toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 8), cmdInfo);
        }

        // Load existing rewards (slots 18-44 for items)
        List<ItemStack> currentRewardItems = rewards.getCompletionRewardItems();
        for (int i = 0; i < currentRewardItems.size() && i < 27; i++) {
            gui.setItem(18 + i, currentRewardItems.get(i));
        }

        // Save button
        {
            String path = "reward_editor.completion_rewards.save";
            ItemStack save = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "LIME_WOOL"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&a&l✔ LƯU"),
                    "save_completion_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 49), save);
        }

        // Back button
        {
            String path = "reward_editor.completion_rewards.back";
            ItemStack back = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "ARROW"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&c&lQuay Lại"),
                    "back_reward_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 45), back);
        }

        Material black = plugin.getGUIConfigManager().getMaterial("reward_editor.filler.black", "BLACK_STAINED_GLASS_PANE");
        fillEmptySlots(gui, black, 0, 17);
        player.openInventory(gui);
    }

    public void openScoreRewardsEditor(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.getGUIConfigManager().titleScoreRewards());

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        {
            String path = "reward_editor.score_rewards.info";
            ItemStack info = createItem(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "PAPER"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lHướng Dẫn"),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 4), info);
        }

        {
            String path = "reward_editor.score_rewards.add_tier";
            ItemStack addTier = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "EMERALD_BLOCK"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&a&l+ THÊM MỨC ĐIỂM"),
                    "add_score_tier_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 8), addTier);
        }

        Map<Integer, DungeonRewards.ScoreReward> scoreRewards = rewards.getScoreBasedRewards();
        int slot = 18;
        for (Map.Entry<Integer, DungeonRewards.ScoreReward> entry : scoreRewards.entrySet()) {
            int score = entry.getKey();
            DungeonRewards.ScoreReward reward = entry.getValue();

            ItemStack tierItem = createItemWithData(
                    Material.DIAMOND,
                    "§6§l" + score + " Điểm",
                    "edit_tier_" + dungeon.getId() + "_" + score,
                    "§7Phần thưởng cho " + score + " điểm",
                    "",
                    "§eVật phẩm: §a" + reward.getRewardItems().size(),
                    "§eCommands: §a" + reward.getCommands().size(),
                    "",
                    "§a▶ Click trái: Chỉnh sửa",
                    "§c▶ Click phải: Xóa"
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

            if (slot >= 44) break;
        }

        {
            String path = "reward_editor.score_rewards.back";
            ItemStack back = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "ARROW"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&c&lQuay Lại"),
                    "back_reward_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 45), back);
        }

        Material filler = plugin.getGUIConfigManager().getMaterial("reward_editor.filler.default", "GRAY_STAINED_GLASS_PANE");
        fillEmptySlots(gui, filler);
        player.openInventory(gui);
    }

    public void openScoreTierEditor(Player player, Dungeon dungeon, int score) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.getGUIConfigManager().titleEditScoreReward());

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

        {
            String path = "reward_editor.edit_score_tier.info";
            List<String> lore = plugin.getGUIConfigManager().getColoredStringList(path + ".lore");
            String name = plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lPhần Thưởng {score} Điểm").replace("{score}", String.valueOf(score));
            List<String> replacedLore = new ArrayList<>();
            for (String line : lore) replacedLore.add(line.replace("{score}", String.valueOf(score)));
            ItemStack info = createItem(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "PAPER"),
                    name,
                    replacedLore.toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 4), info);
        }

        // ✅ FIX: Đổi data key thành "add_cmd_tier_"
        {
            String path = "reward_editor.edit_score_tier.add_commands";
            List<String> lore = plugin.getGUIConfigManager().getColoredStringList(path + ".lore");
            List<String> replaced = new ArrayList<>();
            for (String line : lore) replaced.add(line.replace("{count}", String.valueOf(scoreReward.getCommands().size())));
            ItemStack cmdInfo = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "COMMAND_BLOCK"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&6&lCommand Rewards"),
                    "add_cmd_tier_" + dungeon.getId() + "_" + score,
                    replaced.toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 8), cmdInfo);
        }

        List<ItemStack> currentRewardItems = scoreReward.getRewardItems();
        for (int i = 0; i < currentRewardItems.size() && i < 27; i++) {
            gui.setItem(18 + i, currentRewardItems.get(i));
        }

        {
            String path = "reward_editor.edit_score_tier.save";
            ItemStack save = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "LIME_WOOL"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&a&l✔ LƯU"),
                    "save_tier_" + dungeon.getId() + "_" + score,
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 49), save);
        }

        {
            String path = "reward_editor.edit_score_tier.back";
            ItemStack back = createItemWithData(
                    plugin.getGUIConfigManager().getMaterial(path + ".material", "ARROW"),
                    plugin.getGUIConfigManager().getColoredString(path + ".name", "&c&lQuay Lại"),
                    "back_score_rewards_" + dungeon.getId(),
                    plugin.getGUIConfigManager().getColoredStringList(path + ".lore").toArray(new String[0])
            );
            gui.setItem(plugin.getGUIConfigManager().getInt(path + ".slot", 45), back);
        }

        Material black = plugin.getGUIConfigManager().getMaterial("reward_editor.filler.black", "BLACK_STAINED_GLASS_PANE");
        fillEmptySlots(gui, black, 0, 17);
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
            return "§7Chưa có mức điểm nào";
        }
        return "§eSố mức: §a" + rewards.getScoreBasedRewards().size();
    }
}