package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.PlayerData;
import org.bukkit.Bukkit;
import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    
    private static final String SQLITE_PREFIX = "jdbc:sqlite:";
    private static final String MYSQL_PREFIX = "jdbc:mysql://";
    private static final String DB_TYPE_SQLITE = "SQLITE";
    private static final String DB_TYPE_MYSQL = "MYSQL";
    
    private final ParaDungeon plugin;
    private Connection connection;
    
    public DatabaseManager(ParaDungeon plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        String dbType = plugin.getConfig().getString("database.type", DB_TYPE_SQLITE);
        
        try {
            if (dbType.equalsIgnoreCase(DB_TYPE_SQLITE)) {
                initializeSQLite();
            } else if (dbType.equalsIgnoreCase(DB_TYPE_MYSQL)) {
                initializeMySQL();
            } else {
                plugin.getLogger().warning("Unknown database type: " + dbType + ". Defaulting to SQLite.");
                initializeSQLite();
            }
            
            createTables();
            plugin.getLogger().info("Database initialized successfully using " + dbType);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new SQLException("Failed to create plugin data folder");
            }
        }
        
        File dbFile = new File(dataFolder, "database.db");
        String url = SQLITE_PREFIX + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
        plugin.getLogger().info("SQLite database file: " + dbFile.getAbsolutePath());
    }
    
    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database");
        String username = plugin.getConfig().getString("database.mysql.username");
        String password = plugin.getConfig().getString("database.mysql.password");
        
        if (host == null || database == null || username == null) {
            throw new SQLException("MySQL configuration is incomplete");
        }
        
        String url = MYSQL_PREFIX + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
        connection = DriverManager.getConnection(url, username, password);
        plugin.getLogger().info("Connected to MySQL database at " + host + ":" + port);
    }
    
    private void createTables() throws SQLException {
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
        if (playerId == null) {
            plugin.getLogger().warning("Attempted to load player data with null UUID");
            return new PlayerData(playerId);
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().severe("Database connection is closed. Cannot load player data.");
                return new PlayerData(playerId);
            }
            
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
            plugin.getLogger().severe("Failed to load player data for " + playerId + ": " + e.getMessage());
            e.printStackTrace();
            return new PlayerData(playerId);
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
        if (data == null) {
            plugin.getLogger().warning("Attempted to save null player data");
            return;
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().severe("Database connection is closed. Cannot save player data.");
                return;
            }
            
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
            plugin.getLogger().severe("Failed to save player data for " + data.getPlayerId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void savePlayerEntries(PlayerData data) throws SQLException {
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
        String playerName = Bukkit.getOfflinePlayer(data.getPlayerId()).getName();
        if (playerName == null) {
            playerName = "Unknown";
        }
        
        // Use batch operations for better performance
        boolean isMySQL = connection.getMetaData().getDatabaseProductName().contains("MySQL");
        String query = isMySQL ?
                "INSERT INTO dungeon_scores (player_id, player_name, dungeon_id, score) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = ?, player_name = ?" :
                "INSERT OR REPLACE INTO dungeon_scores (player_id, player_name, dungeon_id, score) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (Map.Entry<String, Integer> entry : data.getAllScores().entrySet()) {
                stmt.setString(1, data.getPlayerId().toString());
                stmt.setString(2, playerName);
                stmt.setString(3, entry.getKey());
                stmt.setInt(4, entry.getValue());
                if (isMySQL) {
                    stmt.setInt(5, entry.getValue());
                    stmt.setString(6, playerName);
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    public List<LeaderboardManager.LeaderboardEntry> getTopScores(String dungeonId, int limit) {
        List<LeaderboardManager.LeaderboardEntry> entries = new ArrayList<>();
        
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
                plugin.getLogger().info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }
    
    /**
     * Check if database connection is active
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
