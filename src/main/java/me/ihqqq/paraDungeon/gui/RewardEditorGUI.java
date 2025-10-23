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

/**
 * GUI để quản lý phần thưởng của dungeon
 */
public class RewardEditorGUI {

    private final ParaDungeon plugin;

    // GUI Titles
    public static final String REWARD_MENU_TITLE = "§8▎ §6§lQuản Lý Phần Thưởng";
    public static final String COMPLETION_REWARDS_TITLE = "§8▎ §6§lPhần Thưởng Hoàn Thành";
    public static final String SCORE_REWARDS_TITLE = "§8▎ §6§lPhần Thưởng Theo Điểm";
    public static final String EDIT_SCORE_REWARD_TITLE = "§8▎ §6§lChỉnh Sửa Phần Thưởng";
    public static final String ADD_SCORE_REWARD_TITLE = "§8▎ §6§lThêm Điểm Yêu Cầu";

    public RewardEditorGUI(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    /**
     * Open main reward management menu
     */
    public void openRewardMenu(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 27, REWARD_MENU_TITLE);

        // Completion Rewards
        ItemStack completionRewards = createItemWithData(
                Material.CHEST,
                "§6§lPhần Thưởng Hoàn Thành",
                "completion_" + dungeon.getId(),
                "§7Phần thưởng khi hoàn thành",
                "§7phó bản (không phụ thuộc điểm)",
                "",
                "§e▶ Click để chỉnh sửa!"
        );
        gui.setItem(11, completionRewards);

        // Score-based Rewards
        ItemStack scoreRewards = createItemWithData(
                Material.NETHER_STAR,
                "§6§lPhần Thưởng Theo Điểm",
                "score_" + dungeon.getId(),
                "§7Phần thưởng dựa trên",
                "§7điểm số đạt được",
                "",
                getScoreRewardInfo(dungeon),
                "",
                "§e▶ Click để chỉnh sửa!"
        );
        gui.setItem(13, scoreRewards);

        // Preview Rewards
        ItemStack preview = createItemWithData(
                Material.BOOK,
                "§6§lXem Trước Phần Thưởng",
                "preview_" + dungeon.getId(),
                "§7Xem tất cả phần thưởng",
                "§7của phó bản này",
                "",
                "§e▶ Click để xem!"
        );
        gui.setItem(15, preview);

        // Back button
        ItemStack back = createItem(
                Material.ARROW,
                "§c§lQuay Lại",
                "§7Về thông tin phó bản"
        );
        gui.setItem(22, back);

        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    /**
     * Open completion rewards editor
     */
    public void openCompletionRewardsEditor(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, COMPLETION_REWARDS_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        // Info panel
        ItemStack info = createItem(
                Material.PAPER,
                "§6§lHướng Dẫn",
                "§7Đặt các item phần thưởng vào",
                "§7các ô trống phía dưới",
                "",
                "§e✦ §7Chấp nhận mọi loại item:",
                "  §7- Vanilla items",
                "  §7- MMOItems",
                "  §7- Custom items từ plugin khác",
                "",
                "§aPhần thưởng sẽ tự động lưu!"
        );
        gui.setItem(4, info);

        // Command rewards info
        ItemStack cmdInfo = createItemWithData(
                Material.COMMAND_BLOCK,
                "§6§lCommand Rewards",
                "add_command_" + dungeon.getId(),
                "§7Thêm lệnh thực thi khi",
                "§7người chơi hoàn thành",
                "",
                "§eSố lượng: §a" + rewards.getCompletionCommands().size(),
                "",
                "§e▶ Click để quản lý!"
        );
        gui.setItem(8, cmdInfo);

        // Load existing rewards (slots 18-44 for items)
        List<ItemStack> currentRewardItems = rewards.getCompletionRewardItems();
        for (int i = 0; i < currentRewardItems.size() && i < 27; i++) {
            gui.setItem(18 + i, currentRewardItems.get(i));
        }

        // Save button
        ItemStack save = createItemWithData(
                Material.LIME_WOOL,
                "§a§l✔ LƯU",
                "save_completion_" + dungeon.getId(),
                "§7Lưu các phần thưởng",
                "",
                "§e▶ Click để lưu!"
        );
        gui.setItem(49, save);

        // Back button
        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_reward_" + dungeon.getId(),
                "§7Về menu phần thưởng"
        );
        gui.setItem(45, back);

        fillEmptySlots(gui, Material.BLACK_STAINED_GLASS_PANE, 0, 17); // Only top rows
        player.openInventory(gui);
    }

    /**
     * Open score-based rewards editor
     */
    public void openScoreRewardsEditor(Player player, Dungeon dungeon) {
        Inventory gui = Bukkit.createInventory(null, 54, SCORE_REWARDS_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) {
            rewards = new DungeonRewards();
            dungeon.setRewards(rewards);
        }

        // Info
        ItemStack info = createItem(
                Material.PAPER,
                "§6§lHướng Dẫn",
                "§7Phần thưởng dựa trên điểm số",
                "§7người chơi đạt được",
                "",
                "§7Ví dụ:",
                "  §e1000 điểm: §710 kim cương",
                "  §e2000 điểm: §720 kim cương",
                "",
                "§7Người chơi nhận thưởng cao nhất",
                "§7mà họ đạt đủ điểm"
        );
        gui.setItem(4, info);

        // Add new score tier
        ItemStack addTier = createItemWithData(
                Material.EMERALD_BLOCK,
                "§a§l+ THÊM MỨC ĐIỂM",
                "add_score_tier_" + dungeon.getId(),
                "§7Thêm mức điểm mới",
                "",
                "§e▶ Click để thêm!"
        );
        gui.setItem(8, addTier);

        // Display existing score tiers
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
            gui.setItem(slot++, tierItem);

            if (slot >= 44) break;
        }

        // Back button
        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_reward_" + dungeon.getId(),
                "§7Về menu phần thưởng"
        );
        gui.setItem(45, back);

        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    /**
     * Open editor for specific score tier
     */
    public void openScoreTierEditor(Player player, Dungeon dungeon, int score) {
        Inventory gui = Bukkit.createInventory(null, 54, EDIT_SCORE_REWARD_TITLE);

        DungeonRewards rewards = dungeon.getRewards();
        if (rewards == null) return;

        DungeonRewards.ScoreReward scoreReward = rewards.getScoreBasedRewards().get(score);
        if (scoreReward == null) {
            scoreReward = new DungeonRewards.ScoreReward();
            rewards.addScoreReward(score, scoreReward);
        }

        // Info
        ItemStack info = createItem(
                Material.PAPER,
                "§6§lPhần Thưởng " + score + " Điểm",
                "§7Đặt các item phần thưởng vào",
                "§7các ô trống phía dưới",
                "",
                "§aPhần thưởng sẽ tự động lưu!"
        );
        gui.setItem(4, info);

        // Command rewards
        ItemStack cmdInfo = createItemWithData(
                Material.COMMAND_BLOCK,
                "§6§lCommand Rewards",
                "add_command_tier_" + dungeon.getId() + "_" + score,
                "§7Thêm lệnh thực thi",
                "",
                "§eSố lượng: §a" + scoreReward.getCommands().size(),
                "",
                "§e▶ Click để quản lý!"
        );
        gui.setItem(8, cmdInfo);

        // Load existing rewards
        List<ItemStack> currentRewardItems = scoreReward.getRewardItems();
        for (int i = 0; i < currentRewardItems.size() && i < 27; i++) {
            gui.setItem(18 + i, currentRewardItems.get(i));
        }

        // Save button
        ItemStack save = createItemWithData(
                Material.LIME_WOOL,
                "§a§l✔ LƯU",
                "save_tier_" + dungeon.getId() + "_" + score,
                "§7Lưu phần thưởng",
                "",
                "§e▶ Click để lưu!"
        );
        gui.setItem(49, save);

        // Back button
        ItemStack back = createItemWithData(
                Material.ARROW,
                "§c§lQuay Lại",
                "back_score_rewards_" + dungeon.getId(),
                "§7Về danh sách mức điểm"
        );
        gui.setItem(45, back);

        fillEmptySlots(gui, Material.BLACK_STAINED_GLASS_PANE, 0, 17);
        player.openInventory(gui);
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