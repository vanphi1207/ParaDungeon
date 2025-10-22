package me.ihqqq.paraDungeon.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    
    private final UUID playerId;
    private final Map<String, Integer> dungeonEntries;
    private final Map<String, Integer> dungeonScores;
    private long lastEntryReset;
    private String currentDungeonRoom;
    
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.dungeonEntries = new HashMap<>();
        this.dungeonScores = new HashMap<>();
        this.lastEntryReset = System.currentTimeMillis();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public int getDungeonEntries(String dungeonId) {
        return dungeonEntries.getOrDefault(dungeonId, 0);
    }
    
    public void setDungeonEntries(String dungeonId, int entries) {
        dungeonEntries.put(dungeonId, entries);
    }
    
    public void decrementEntries(String dungeonId) {
        int entries = getDungeonEntries(dungeonId);
        setDungeonEntries(dungeonId, Math.max(0, entries - 1));
    }
    
    public int getDungeonScore(String dungeonId) {
        return dungeonScores.getOrDefault(dungeonId, 0);
    }
    
    public void setDungeonScore(String dungeonId, int score) {
        if (score > getDungeonScore(dungeonId)) {
            dungeonScores.put(dungeonId, score);
        }
    }
    
    public Map<String, Integer> getAllScores() {
        return new HashMap<>(dungeonScores);
    }
    
    public long getLastEntryReset() {
        return lastEntryReset;
    }
    
    public void setLastEntryReset(long time) {
        this.lastEntryReset = time;
    }
    
    public String getCurrentDungeonRoom() {
        return currentDungeonRoom;
    }
    
    public void setCurrentDungeonRoom(String roomId) {
        this.currentDungeonRoom = roomId;
    }
    
    public boolean isInDungeon() {
        return currentDungeonRoom != null;
    }
}
