package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Manages plugin configuration files including messages and GUI configs
 */
public class ConfigManager {

    private final ParaDungeon plugin;
    private FileConfiguration dungeonsConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration guiConfig;
    private File dungeonsFile;
    private File messagesFile;
    private File guiFile;

    public ConfigManager(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        createDefaultConfigs();
        dungeonsConfig = YamlConfiguration.loadConfiguration(dungeonsFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
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

        guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
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

    public FileConfiguration getGUIConfig() {
        return guiConfig;
    }

    /**
     * Lấy message với prefix tự động
     */
    public String getMessage(String path) {
        String prefix = messagesConfig.getString("prefix", "");
        String message = messagesConfig.getString(path, "");
        return (prefix + message).replace("&", "§");
    }

    /**
     * Lấy message với prefix tự động và thay thế placeholder
     */
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

    /**
     * Lấy message KHÔNG có prefix (dùng cho các trường hợp đặc biệt)
     */
    public String getMessageRaw(String path) {
        String message = messagesConfig.getString(path, "");
        return message.replace("&", "§");
    }

    /**
     * Lấy message KHÔNG có prefix với placeholder
     */
    public String getMessageRaw(String path, String... replacements) {
        String messageTemplate = messagesConfig.getString(path, "");

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                messageTemplate = messageTemplate.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }

        return messageTemplate.replace("&", "§");
    }

    /**
     * Lấy prefix
     */
    public String getPrefix() {
        return messagesConfig.getString("prefix", "").replace("&", "§");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GUI CONFIGURATION METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lấy tiêu đề GUI với màu sắc đã được xử lý
     */
    public String getGUITitle(String path) {
        return guiConfig.getString(path, "").replace("&", "§");
    }

    /**
     * Lấy kích thước GUI
     */
    public int getGUISize(String path) {
        return guiConfig.getInt(path, 27);
    }

    /**
     * Lấy tên item với màu sắc đã được xử lý
     */
    public String getGUIItemName(String path) {
        return guiConfig.getString(path, "").replace("&", "§");
    }

    /**
     * Lấy danh sách lore với màu sắc đã được xử lý
     */
    public List<String> getGUIItemLore(String path) {
        List<String> lore = guiConfig.getStringList(path);
        lore.replaceAll(line -> line.replace("&", "§"));
        return lore;
    }

    /**
     * Lấy vật liệu item
     */
    public String getGUIItemMaterial(String path) {
        return guiConfig.getString(path, "STONE");
    }

    /**
     * Lấy slot của item
     */
    public int getGUIItemSlot(String path) {
        return guiConfig.getInt(path, 0);
    }

    /**
     * Kiểm tra filler có được bật không
     */
    public boolean isGUIFillerEnabled(String path) {
        return guiConfig.getBoolean(path + ".enabled", true);
    }

    /**
     * Lấy vật liệu filler
     */
    public String getGUIFillerMaterial(String path) {
        return guiConfig.getString(path + ".material", "GRAY_STAINED_GLASS_PANE");
    }

    /**
     * Lấy giá trị string từ GUI config
     */
    public String getGUIString(String path, String def) {
        return guiConfig.getString(path, def).replace("&", "§");
    }

    /**
     * Lấy giá trị int từ GUI config
     */
    public int getGUIInt(String path, int def) {
        return guiConfig.getInt(path, def);
    }

    /**
     * Kiểm tra key có tồn tại không
     */
    public boolean hasGUIKey(String path) {
        return guiConfig.contains(path);
    }
}