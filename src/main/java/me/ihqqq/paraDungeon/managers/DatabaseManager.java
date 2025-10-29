package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.PlayerData;
import org.bukkit.Bukkit;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {
    
    private final ParaDungeon plugin;
    private Connection connection;
    
    public DatabaseManager(ParaDungeon plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        String dbType = plugin.getConfig().getString("database.type", "SQLITE");
        
        try {
            if (dbType.equalsIgnoreCase("SQLITE")) {
                initializeSQLite();
            } else if (dbType.equalsIgnoreCase("MYSQL")) {
                initializeMySQL();
            }
            
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }
    
    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File dbFile = new File(dataFolder, "database.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }
    
    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host");
        int port = plugin.getConfig().getInt("database.mysql.port");
        String database = plugin.getConfig().getString("database.mysql.database");
        String username = plugin.getConfig().getString("database.mysql.username");
        String password = plugin.getConfig().getString("database.mysql.password");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
        connection = DriverManager.getConnection(url, username, password);
    }
    
    private void createTables() throws SQLException {
        if (!hasValidConnection()) {
            plugin.getLogger().severe("Database connection is not available; cannot create tables.");
            return;
        }
        String createPlayerData = "CREATE TABLE IF NOT EXISTS player_data (" +
            "player_id VARCHAR(36) PRIMARY KEY," +
            "last_entry_reset BIGINT" +
            ")";
        
        String createDungeonEntries = "CREATE TABLE IF NOT EXISTS dungeon_entries (" +
            "player_id VARCHAR(36)," +
            "dungeon_id VARCHAR(50)," +
            "entries INT," +
            "PRIMARY KEY (player_id, dungeon_id)" +
            ")";
        
        String createDungeonScores = "CREATE TABLE IF NOT EXISTS dungeon_scores (" +
            "player_id VARCHAR(36)," +
            "player_name VARCHAR(50)," +
            "dungeon_id VARCHAR(50)," +
            "score INT," +
            "PRIMARY KEY (player_id, dungeon_id)" +
            ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayerData);
            stmt.execute(createDungeonEntries);
            stmt.execute(createDungeonScores);
        }
    }
    
    public PlayerData loadPlayerData(UUID playerId) {
        if (!hasValidConnection()) {
            plugin.getLogger().severe("Database connection is not available; cannot load player data for " + playerId);
            return null;
        }
        try {
            String query = "SELECT * FROM player_data WHERE player_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerId.toString());
                ResultSet rs = stmt.executeQuery();
                
                PlayerData data = new PlayerData(playerId);
                
                if (rs.next()) {
                    data.setLastEntryReset(rs.getLong("last_entry_reset"));
                }
                
                // Load entries
                loadPlayerEntries(data);
                
                // Load scores
                loadPlayerScores(data);
                
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
            return null;
        }
    }
    
    private void loadPlayerEntries(PlayerData data) throws SQLException {
        String query = "SELECT * FROM dungeon_entries WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, data.getPlayerId().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String dungeonId = rs.getString("dungeon_id");
                int entries = rs.getInt("entries");
                data.setDungeonEntries(dungeonId, entries);
            }
        }
    }
    
    private void loadPlayerScores(PlayerData data) throws SQLException {
        String query = "SELECT * FROM dungeon_scores WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, data.getPlayerId().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String dungeonId = rs.getString("dungeon_id");
                int score = rs.getInt("score");
                data.setDungeonScore(dungeonId, score);
            }
        }
    }
    
    public void savePlayerData(PlayerData data) {
        if (!hasValidConnection()) {
            plugin.getLogger().severe("Database connection is not available; cannot save player data for " + data.getPlayerId());
            return;
        }
        try {
            // Save basic data
            String query = "INSERT OR REPLACE INTO player_data (player_id, last_entry_reset) VALUES (?, ?)";
            if (connection.getMetaData().getDatabaseProductName().contains("MySQL")) {
                query = "INSERT INTO player_data (player_id, last_entry_reset) VALUES (?, ?) " +
                       "ON DUPLICATE KEY UPDATE last_entry_reset = ?";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, data.getPlayerId().toString());
                stmt.setLong(2, data.getLastEntryReset());
                if (connection.getMetaData().getDatabaseProductName().contains("MySQL")) {
                    stmt.setLong(3, data.getLastEntryReset());
                }
                stmt.executeUpdate();
            }
            
            // Save entries
            savePlayerEntries(data);
            
            // Save scores
            savePlayerScores(data);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }
    
    private void savePlayerEntries(PlayerData data) throws SQLException {
        if (!hasValidConnection()) {
            return;
        }
        // Clear old entries
        String delete = "DELETE FROM dungeon_entries WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {
            stmt.setString(1, data.getPlayerId().toString());
            stmt.executeUpdate();
        }
        
        // Insert new entries
        String insert = "INSERT INTO dungeon_entries (player_id, dungeon_id, entries) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            for (String dungeonId : plugin.getDungeonManager().getAllDungeons().stream()
                    .map(d -> d.getId()).toList()) {
                int entries = data.getDungeonEntries(dungeonId);
                stmt.setString(1, data.getPlayerId().toString());
                stmt.setString(2, dungeonId);
                stmt.setInt(3, entries);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    private void savePlayerScores(PlayerData data) throws SQLException {
        if (!hasValidConnection()) {
            return;
        }
        String playerName = Bukkit.getOfflinePlayer(data.getPlayerId()).getName();
        
        for (Map.Entry<String, Integer> entry : data.getAllScores().entrySet()) {
            String query = "INSERT OR REPLACE INTO dungeon_scores (player_id, player_name, dungeon_id, score) VALUES (?, ?, ?, ?)";
            if (connection.getMetaData().getDatabaseProductName().contains("MySQL")) {
                query = "INSERT INTO dungeon_scores (player_id, player_name, dungeon_id, score) VALUES (?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE score = ?, player_name = ?";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, data.getPlayerId().toString());
                stmt.setString(2, playerName);
                stmt.setString(3, entry.getKey());
                stmt.setInt(4, entry.getValue());
                if (connection.getMetaData().getDatabaseProductName().contains("MySQL")) {
                    stmt.setInt(5, entry.getValue());
                    stmt.setString(6, playerName);
                }
                stmt.executeUpdate();
            }
        }
    }
    
    public List<LeaderboardManager.LeaderboardEntry> getTopScores(String dungeonId, int limit) {
        List<LeaderboardManager.LeaderboardEntry> entries = new ArrayList<>();
        
        if (!hasValidConnection()) {
            plugin.getLogger().severe("Database connection is not available; cannot load leaderboard for " + dungeonId);
            return entries;
        }

        try {
            String query = "SELECT player_id, player_name, score FROM dungeon_scores " +
                          "WHERE dungeon_id = ? ORDER BY score DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, dungeonId);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    String playerName = rs.getString("player_name");
                    int score = rs.getInt("score");
                    
                    entries.add(new LeaderboardManager.LeaderboardEntry(playerId, playerName, score));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load leaderboard: " + e.getMessage());
        }
        
        return entries;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }

    private boolean hasValidConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
