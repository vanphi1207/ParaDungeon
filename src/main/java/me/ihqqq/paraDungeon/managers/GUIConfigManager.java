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

    // Titles helpers - Updated to match new gui.yml structure
    public String titleMainMenu() { return getColoredString("main-menu.title", "§8[ §6§lPHÓ BẢN §8] §7Menu Chính"); }
    public String titleDungeonList() { return getColoredString("dungeon-list.title", "§8[ §6§lPHÓ BẢN §8] §7Chọn Phó Bản"); }
    public String titleDungeonInfo() { return getColoredString("dungeon-info.title", "§8[ §6§lPHÓ BẢN §8] §7Thông Tin"); }
    public String titleLeaderboard() { return getColoredString("leaderboard.title", "§8[ §6§lPHÓ BẢN §8] §7Bảng Xếp Hạng"); }
    public String titleStats() { return getColoredString("player-stats.title", "§8[ §6§lPHÓ BẢN §8] §7Thống Kê"); }
    public String titleSettings() { return getColoredString("settings.title", "§8[ §6§lPHÓ BẢN §8] §7Cài Đặt"); }
    public String titleRewardMenu() { return getColoredString("reward-editor.main.title", "§8[ §6§lPHÓ BẢN §8] §cQuản Lý Thưởng"); }
    public String titleCompletionRewards() { return getColoredString("reward-editor.completion-rewards.title", "§8[ §6§lPHÓ BẢN §8] §aThưởng Hoàn Thành"); }
    public String titleScoreRewards() { return getColoredString("reward-editor.score-rewards.title", "§8[ §6§lPHÓ BẢN §8] §eThưởng Theo Điểm"); }
    public String titleEditScoreReward() { return getColoredString("reward-editor.score-tier-editor.title", "§8[ §6§lPHÓ BẢN §8] §eChỉnh Sửa Mức"); }
    public String titleCompletionCommands() { return getColoredString("command-rewards.completion.title", "§8[ §6§lPHÓ BẢN §8] §aLệnh Hoàn Thành"); }
    public String titleScoreCommands() { return getColoredString("command-rewards.score-tier.title", "§8[ §6§lPHÓ BẢN §8] §eLệnh Theo Điểm"); }

    public boolean isAnyKnownTitle(String title) {
        return title.equals(titleMainMenu()) ||
                title.equals(titleDungeonList()) ||
                title.equals(titleDungeonInfo()) ||
                title.equals(titleLeaderboard()) ||
                title.equals(titleStats()) ||
                title.equals(titleSettings());
    }
}
