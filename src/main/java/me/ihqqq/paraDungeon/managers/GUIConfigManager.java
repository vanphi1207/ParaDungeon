package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GUIConfigManager {

    private final ParaDungeon plugin;
    private FileConfiguration guiConfig;
    private File guiFile;

    public GUIConfigManager(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    public void load() {
        createDefault();
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void reload() {
        load();
    }

    private void createDefault() {
        guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public String getColoredString(String path, String def) {
        String value = guiConfig.getString(path, def);
        return color(value);
    }

    public java.util.List<String> getColoredStringList(String path) {
        java.util.List<String> list = guiConfig.getStringList(path);
        java.util.List<String> colored = new java.util.ArrayList<>();
        for (String s : list) colored.add(color(s));
        return colored;
    }

    public int getInt(String path, int def) {
        return guiConfig.getInt(path, def);
    }

    public Material getMaterial(String path, String def) {
        String name = guiConfig.getString(path, def);
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Material.valueOf(def.toUpperCase());
        }
    }

    public String color(String input) {
        return input == null ? null : input.replace("&", "§");
    }

    // Titles helpers
    public String titleMainMenu() { return getColoredString("titles.main_menu", "§8▎ §6§lParaDungeon Menu"); }
    public String titleDungeonList() { return getColoredString("titles.dungeon_list", "§8▎ §6§lChọn Phó Bản"); }
    public String titleDungeonInfo() { return getColoredString("titles.dungeon_info", "§8▎ §6§lThông Tin Phó Bản"); }
    public String titleLeaderboard() { return getColoredString("titles.leaderboard", "§8▎ §6§lBảng Xếp Hạng"); }
    public String titleStats() { return getColoredString("titles.player_stats", "§8▎ §6§lThống Kê Của Bạn"); }
    public String titleSettings() { return getColoredString("titles.settings", "§8▎ §6§lCài Đặt"); }
    public String titleRewardMenu() { return getColoredString("titles.reward_menu", "§8▎ §6§lQuản Lý Phần Thưởng"); }
    public String titleCompletionRewards() { return getColoredString("titles.completion_rewards", "§8▎ §6§lPhần Thưởng Hoàn Thành"); }
    public String titleScoreRewards() { return getColoredString("titles.score_rewards", "§8▎ §6§lPhần Thưởng Theo Điểm"); }
    public String titleEditScoreReward() { return getColoredString("titles.edit_score_reward", "§8▎ §6§lChỉnh Sửa Phần Thưởng"); }
    public String titleCompletionCommands() { return getColoredString("titles.completion_commands", "§8▎ §6§lLệnh Hoàn Thành"); }
    public String titleScoreCommands() { return getColoredString("titles.score_commands", "§8▎ §6§lLệnh Theo Điểm"); }

    public boolean isAnyKnownTitle(String title) {
        return title.equals(titleMainMenu()) ||
                title.equals(titleDungeonList()) ||
                title.equals(titleDungeonInfo()) ||
                title.equals(titleLeaderboard()) ||
                title.equals(titleStats()) ||
                title.equals(titleSettings());
    }
}
