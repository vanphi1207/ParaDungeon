package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final ParaDungeon plugin;
    private FileConfiguration dungeonsConfig;
    private FileConfiguration messagesConfig;
    private File dungeonsFile;
    private File messagesFile;

    public ConfigManager(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        createDefaultConfigs();
        dungeonsConfig = YamlConfiguration.loadConfiguration(dungeonsFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void createDefaultConfigs() {
        dungeonsFile = new File(plugin.getDataFolder(), "dungeons.yml");
        if (!dungeonsFile.exists()) {
            plugin.saveResource("dungeons.yml", false);
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public void saveDungeonsConfig() {
        try {
            dungeonsConfig.save(dungeonsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save dungeons.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getDungeonsConfig() {
        return dungeonsConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * Lấy và định dạng message từ messages.yml (không có placeholder)
     * Đã sửa lỗi không hiển thị màu cho prefix.
     */
    public String getMessage(String path) {
        String prefix = messagesConfig.getString("prefix", "");
        String message = messagesConfig.getString(path, ""); // Dùng giá trị mặc định để tránh null
        return (prefix + message).replace("&", "§");
    }


    public String getMessage(String path, String... replacements) {
        String prefix = messagesConfig.getString("prefix", "");
        String messageTemplate = messagesConfig.getString(path, "");

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                messageTemplate = messageTemplate.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }

        return (prefix + messageTemplate).replace("&", "§");
    }
}