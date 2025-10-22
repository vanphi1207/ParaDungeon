package me.ihqqq.paraDungeon.models;

import org.bukkit.entity.LivingEntity;
import java.util.*;

public class DungeonRoom {

    private final String roomId;
    private final Dungeon dungeon;
    private final Set<UUID> players;
    private final Map<UUID, Integer> playerLives;
    private final Map<UUID, Integer> playerScores;
    private final Map<UUID, Long> respawnCooldowns;
    private final Map<UUID, Long> safeModeExpiry;
    private final List<LivingEntity> activeEntities;

    private RoomStatus status;
    private int currentStage;
    private int currentWave;
    private long creationTime;
    private long startTime;
    private int countdownTask;

    public DungeonRoom(String roomId, Dungeon dungeon) {
        this.roomId = roomId;
        this.dungeon = dungeon;
        this.players = new HashSet<>();
        this.playerLives = new HashMap<>();
        this.playerScores = new HashMap<>();
        this.respawnCooldowns = new HashMap<>();
        this.safeModeExpiry = new HashMap<>();
        this.activeEntities = new ArrayList<>();
        this.status = RoomStatus.WAITING;
        this.currentStage = 1;
        this.currentWave = 1;
        this.creationTime = System.currentTimeMillis();
        this.countdownTask = -1;
    }

    public String getRoomId() {
        return roomId;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID playerId) {
        players.add(playerId);
        playerLives.put(playerId, dungeon.getRespawnLives());
        playerScores.put(playerId, 0);
    }

    public void removePlayer(UUID playerId) {
        players.remove(playerId);
        playerLives.remove(playerId);
        playerScores.remove(playerId);
        respawnCooldowns.remove(playerId);
        safeModeExpiry.remove(playerId);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isFull() {
        return players.size() >= dungeon.getMaxPlayers();
    }

    public boolean hasMinPlayers() {
        return players.size() >= dungeon.getMinPlayers();
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int stage) {
        this.currentStage = stage;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int wave) {
        this.currentWave = wave;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long time) {
        this.startTime = time;
    }

    public int getCountdownTask() {
        return countdownTask;
    }

    public void setCountdownTask(int taskId) {
        this.countdownTask = taskId;
    }

    public int getPlayerLives(UUID playerId) {
        return playerLives.getOrDefault(playerId, 0);
    }

    public void setPlayerLives(UUID playerId, int lives) {
        playerLives.put(playerId, lives);
    }

    public void decrementPlayerLives(UUID playerId) {
        int lives = getPlayerLives(playerId);
        setPlayerLives(playerId, Math.max(0, lives - 1));
    }

    public int getPlayerScore(UUID playerId) {
        return playerScores.getOrDefault(playerId, 0);
    }

    public void addScore(UUID playerId, int score) {
        playerScores.put(playerId, getPlayerScore(playerId) + score);
    }

    public Map<UUID, Integer> getAllScores() {
        return new HashMap<>(playerScores);
    }

    public void setRespawnCooldown(UUID playerId, long expiry) {
        respawnCooldowns.put(playerId, expiry);
    }

    public Long getRespawnCooldown(UUID playerId) {
        return respawnCooldowns.get(playerId);
    }

    public void removeRespawnCooldown(UUID playerId) {
        respawnCooldowns.remove(playerId);
    }

    public void setSafeMode(UUID playerId, long expiry) {
        safeModeExpiry.put(playerId, expiry);
    }

    public boolean isInSafeMode(UUID playerId) {
        Long expiry = safeModeExpiry.get(playerId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            safeModeExpiry.remove(playerId);
            return false;
        }
        return true;
    }

    public List<LivingEntity> getActiveEntities() {
        return activeEntities;
    }

    public void addEntity(LivingEntity entity) {
        activeEntities.add(entity);
    }

    public void clearEntities() {
        for (LivingEntity entity : activeEntities) {
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
        activeEntities.clear();
    }

    /**
     * Get elapsed time in seconds since dungeon started
     */
    public long getElapsedTime() {
        if (startTime == 0) return 0;
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * Format elapsed time as MM:SS
     */
    public String getFormattedElapsedTime() {
        long seconds = getElapsedTime();
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public enum RoomStatus {
        WAITING,
        COUNTDOWN,
        ACTIVE,
        COMPLETED,
        FAILED
    }
}