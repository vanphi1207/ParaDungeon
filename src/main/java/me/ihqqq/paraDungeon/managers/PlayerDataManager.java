package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.PlayerData;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data including entries, scores, and statistics
 */
public class PlayerDataManager {

    private final ParaDungeon plugin;
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(ParaDungeon plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
    }

    /**
     * Get player data for a specific player, loading from database if not cached
     * 
     * @param playerId UUID of the player
     * @return PlayerData object for the player
     */
    public PlayerData getPlayerData(UUID playerId) {
        if (playerId == null) {
            plugin.getLogger().warning("Attempted to get player data with null UUID");
            return null;
        }
        return playerDataMap.computeIfAbsent(playerId, id -> {
            PlayerData data = plugin.getDatabaseManager().loadPlayerData(id);
            if (data == null) {
                data = new PlayerData(id);
                plugin.getLogger().info("Created new player data for " + id);
            }
            return data;
        });
    }

    public void savePlayerData(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        if (data != null) {
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    /**
     * Save all currently loaded player data to database
     */
    public void saveAllPlayerData() {
        int count = 0;
        for (PlayerData data : playerDataMap.values()) {
            try {
                plugin.getDatabaseManager().savePlayerData(data);
                count++;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save player data for " + data.getPlayerId() + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("Saved data for " + count + " player(s)");
    }

    public void checkEntryReset() {
        String resetType = plugin.getConfig().getString("settings.entry-system.reset-type", "DAILY");

        for (PlayerData data : playerDataMap.values()) {
            boolean shouldReset = false;
            long currentTime = System.currentTimeMillis();
            long lastReset = data.getLastEntryReset();

            if (resetType.equalsIgnoreCase("DAILY")) {
                Calendar lastCal = Calendar.getInstance();
                lastCal.setTimeInMillis(lastReset);

                Calendar currentCal = Calendar.getInstance();
                currentCal.setTimeInMillis(currentTime);

                if (currentCal.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR) ||
                        currentCal.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR)) {
                    shouldReset = true;
                }
            } else if (resetType.equalsIgnoreCase("HOURLY")) {
                int resetHours = plugin.getConfig().getInt("settings.entry-system.reset-hours", 24);
                long resetMillis = resetHours * 60 * 60 * 1000L;

                if (currentTime - lastReset >= resetMillis) {
                    shouldReset = true;
                }
            }

            if (shouldReset) {
                resetPlayerEntries(data);
            }
        }
    }

    private void resetPlayerEntries(PlayerData data) {
        int defaultEntries = plugin.getConfig().getInt("settings.entry-system.default-entries", 3);

        for (String dungeonId : plugin.getDungeonManager().getAllDungeons().stream()
                .map(d -> d.getId()).toList()) {
            data.setDungeonEntries(dungeonId, defaultEntries);
        }

        data.setLastEntryReset(System.currentTimeMillis());
        savePlayerData(data.getPlayerId());
    }

    public void unloadPlayerData(UUID playerId) {
        savePlayerData(playerId);
        playerDataMap.remove(playerId);
    }

    /**
     * Sửa lỗi logic:
     * - Logic cũ: Nếu entries <= 0, nó sẽ reset lượt và trả về true, khiến người chơi có thể vào lại ngay.
     * - Logic mới:
     * 1. Kiểm tra xem người chơi đã có dữ liệu lượt đi cho phó bản này chưa.
     * 2. Nếu chưa, đây là lần đầu họ chơi, cấp cho họ số lượt mặc định.
     * 3. Nếu có rồi, chỉ cần kiểm tra xem số lượt có > 0 hay không.
     */
    public boolean hasAvailableEntries(Player player, String dungeonId) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (!data.getDungeonEntriesMap().containsKey(dungeonId)) {
            int defaultEntries = plugin.getConfig().getInt("settings.entry-system.default-entries", 3);
            data.setDungeonEntries(dungeonId, defaultEntries);
            return true;
        }

        return data.getDungeonEntries(dungeonId) > 0;
    }

    public void consumeEntry(Player player, String dungeonId) {
        PlayerData data = getPlayerData(player.getUniqueId());
        data.decrementEntries(dungeonId);
        savePlayerData(player.getUniqueId());
    }
}