package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.PlayerData;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    
    private final ParaDungeon plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    
    public PlayerDataManager(ParaDungeon plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
    }
    
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, id -> {
            PlayerData data = plugin.getDatabaseManager().loadPlayerData(id);
            if (data == null) {
                data = new PlayerData(id);
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
    
    public void saveAllPlayerData() {
        for (PlayerData data : playerDataMap.values()) {
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }
    
    public void checkEntryReset() {
        String resetType = plugin.getConfig().getString("settings.entry-system.reset-type", "DAILY");
        
        for (PlayerData data : playerDataMap.values()) {
            boolean shouldReset = false;
            long currentTime = System.currentTimeMillis();
            long lastReset = data.getLastEntryReset();
            
            if (resetType.equalsIgnoreCase("DAILY")) {
                // Check if it's a new day
                Calendar lastCal = Calendar.getInstance();
                lastCal.setTimeInMillis(lastReset);
                
                Calendar currentCal = Calendar.getInstance();
                currentCal.setTimeInMillis(currentTime);
                
                if (currentCal.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR) ||
                    currentCal.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR)) {
                    shouldReset = true;
                }
            } else if (resetType.equalsIgnoreCase("HOURLY")) {
                // Check if enough hours have passed
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
        
        // Reset entries for all dungeons
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
    
    public boolean hasAvailableEntries(Player player, String dungeonId) {
        PlayerData data = getPlayerData(player.getUniqueId());
        int entries = data.getDungeonEntries(dungeonId);
        
        if (entries <= 0) {
            // Initialize if not set
            int defaultEntries = plugin.getConfig().getInt("settings.entry-system.default-entries", 3);
            data.setDungeonEntries(dungeonId, defaultEntries);
            return true;
        }
        
        return entries > 0;
    }
    
    public void consumeEntry(Player player, String dungeonId) {
        PlayerData data = getPlayerData(player.getUniqueId());
        data.decrementEntries(dungeonId);
        savePlayerData(player.getUniqueId());
    }
}
