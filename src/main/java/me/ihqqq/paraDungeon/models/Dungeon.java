package me.ihqqq.paraDungeon.models;

import org.bukkit.Location;
import java.util.*;

public class Dungeon {

    private final String id;
    private String displayName;
    private List<String> description;
    private int minPlayers;
    private int maxPlayers;
    private int entriesPerReset;
    private int respawnLives;
    private long autoRenewMinutes;
    private Location spawnPoint;
    private Location lobbyPoint;
    private Location endPoint; // Đã thêm
    private Map<Integer, Stage> stages;
    private DungeonRewards rewards;

    public Dungeon(String id) {
        this.id = id;
        this.stages = new HashMap<>();
        this.description = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getEntriesPerReset() {
        return entriesPerReset;
    }

    public void setEntriesPerReset(int entriesPerReset) {
        this.entriesPerReset = entriesPerReset;
    }

    public int getRespawnLives() {
        return respawnLives;
    }

    public void setRespawnLives(int respawnLives) {
        this.respawnLives = respawnLives;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Location getLobbyPoint() {
        return lobbyPoint;
    }

    public void setLobbyPoint(Location lobbyPoint) {
        this.lobbyPoint = lobbyPoint;
    }

    public Location getEndPoint() { // Đã thêm
        return endPoint;
    }

    public void setEndPoint(Location endPoint) { // Đã thêm
        this.endPoint = endPoint;
    }

    public Map<Integer, Stage> getStages() {
        return stages;
    }

    public void addStage(int stageNum, Stage stage) {
        this.stages.put(stageNum, stage);
    }

    public Stage getStage(int stageNum) {
        return stages.get(stageNum);
    }

    public int getTotalStages() {
        return stages.size();
    }

    public boolean hasMinimumStages() {
        // Cần ít nhất 3 ải để khởi tạo
        return stages.size() >= 3;
    }

    public DungeonRewards getRewards() {
        return rewards;
    }

    public void setRewards(DungeonRewards rewards) {
        this.rewards = rewards;
    }

    public long getAutoRenewMinutes() {
        return autoRenewMinutes;
    }

    public void setAutoRenewMinutes(long autoRenewMinutes) {
        this.autoRenewMinutes = autoRenewMinutes;
    }
}