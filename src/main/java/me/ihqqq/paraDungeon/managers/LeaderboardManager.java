package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dungeon leaderboards and player rankings
 */
public class LeaderboardManager {
    
    private final ParaDungeon plugin;
    private final Map<String, List<LeaderboardEntry>> leaderboards;
    
    public LeaderboardManager(ParaDungeon plugin) {
        this.plugin = plugin;
        this.leaderboards = new ConcurrentHashMap<>();
    }
    
    /**
     * Update all dungeon leaderboards from database
     */
    public void updateLeaderboards() {
        int count = 0;
        for (String dungeonId : plugin.getDungeonManager().getAllDungeons().stream()
                .map(d -> d.getId()).toList()) {
            try {
                updateLeaderboard(dungeonId);
                count++;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update leaderboard for " + dungeonId + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("Updated " + count + " leaderboard(s)");
    }
    
    /**
     * Update leaderboard for a specific dungeon
     * 
     * @param dungeonId ID of the dungeon
     */
    public void updateLeaderboard(String dungeonId) {
        if (dungeonId == null || dungeonId.isEmpty()) {
            plugin.getLogger().warning("Cannot update leaderboard: dungeonId is null or empty");
            return;
        }
        
        try {
            List<LeaderboardEntry> entries = plugin.getDatabaseManager().getTopScores(dungeonId, 
                plugin.getConfig().getInt("settings.leaderboard.top-players", 10));
            leaderboards.put(dungeonId, entries);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update leaderboard for " + dungeonId + ": " + e.getMessage());
            leaderboards.put(dungeonId, new ArrayList<>());
        }
    }
    
    public List<LeaderboardEntry> getLeaderboard(String dungeonId) {
        return leaderboards.getOrDefault(dungeonId, new ArrayList<>());
    }
    
    public int getPlayerRank(String dungeonId, UUID playerId) {
        List<LeaderboardEntry> entries = getLeaderboard(dungeonId);
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPlayerId().equals(playerId)) {
                return i + 1;
            }
        }
        return -1;
    }
    
    public static class LeaderboardEntry {
        private final UUID playerId;
        private final String playerName;
        private final int score;
        
        public LeaderboardEntry(UUID playerId, String playerName, int score) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
        }
        
        public UUID getPlayerId() {
            return playerId;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public int getScore() {
            return score;
        }
    }
}
