package me.ihqqq.paraDungeon;

import me.ihqqq.paraDungeon.commands.DungeonCommand;
import me.ihqqq.paraDungeon.commands.DungeonAdminCommand;
import me.ihqqq.paraDungeon.gui.GUIManager;
import me.ihqqq.paraDungeon.managers.*;
import me.ihqqq.paraDungeon.listeners.*;
import me.ihqqq.paraDungeon.integrations.PlaceholderAPIIntegration;
import me.ihqqq.paraDungeon.tasks.ActionBarTask;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParaDungeon extends JavaPlugin {

    private static ParaDungeon instance;

    private ConfigManager configManager;
    private DungeonManager dungeonManager;
    private RoomManager roomManager;
    private PlayerDataManager playerDataManager;
    private LeaderboardManager leaderboardManager;
    private RewardManager rewardManager;
    private DatabaseManager databaseManager;
    private GUIConfigManager guiConfigManager;

    private GUIManager guiManager;
    private NamespacedKey dungeonKey;
    private NamespacedKey deleteKey;

    private ActionBarTask actionBarTask;

    @Override
    public void onEnable() {
        instance = this;

        dungeonKey = new NamespacedKey(this, "dungeon_id");
        deleteKey = new NamespacedKey(this, "delete_action");

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        guiConfigManager = new GUIConfigManager(this);
        guiConfigManager.load();

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        playerDataManager = new PlayerDataManager(this);
        dungeonManager = new DungeonManager(this);
        roomManager = new RoomManager(this);
        leaderboardManager = new LeaderboardManager(this);
        rewardManager = new RewardManager(this);

        guiManager = new GUIManager(this);

        dungeonManager.loadDungeons();

        registerCommands();
        registerListeners();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIIntegration(this).register();
            getLogger().info("PlaceholderAPI hooked successfully!");
        }

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            getLogger().warning("MythicMobs not found! Plugin will not work properly.");
        } else {
            getLogger().info("MythicMobs hooked successfully!");
        }

        startTasks();

        getLogger().info("ParaDungeon has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ParaDungeon...");
        
        // Cancel scheduled tasks
        if (actionBarTask != null) {
            try {
                actionBarTask.cancel();
                getLogger().info("ActionBar task cancelled");
            } catch (Exception e) {
                getLogger().warning("Error cancelling ActionBar task: " + e.getMessage());
            }
        }
        
        // Cancel all Bukkit tasks
        try {
            Bukkit.getScheduler().cancelTasks(this);
            getLogger().info("All scheduled tasks cancelled");
        } catch (Exception e) {
            getLogger().warning("Error cancelling scheduled tasks: " + e.getMessage());
        }

        // Save all player data
        if (playerDataManager != null) {
            try {
                playerDataManager.saveAllPlayerData();
                getLogger().info("All player data saved");
            } catch (Exception e) {
                getLogger().severe("Error saving player data: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Clear and cleanup all active dungeon rooms
        if (roomManager != null) {
            try {
                roomManager.clearAllRooms();
                getLogger().info("All dungeon rooms cleared");
            } catch (Exception e) {
                getLogger().severe("Error clearing rooms: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Close database connection
        if (databaseManager != null) {
            try {
                databaseManager.close();
                getLogger().info("Database connection closed");
            } catch (Exception e) {
                getLogger().severe("Error closing database: " + e.getMessage());
                e.printStackTrace();
            }
        }

        getLogger().info("ParaDungeon has been disabled!");
    }

    private void registerCommands() {
        getCommand("dungeon").setExecutor(new DungeonCommand(this));
        getCommand("dungeonadmin").setExecutor(new DungeonAdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new DungeonListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new RewardGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandInputListener(this), this);
    }

    private void startTasks() {
        // Auto-renew task
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            roomManager.checkAutoRenew();
        }, 0L, 20L * 60L);

        // Leaderboard update task
        int updateInterval = getConfig().getInt("settings.leaderboard.update-interval", 5);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            leaderboardManager.updateLeaderboards();
        }, 0L, 20L * 60L * updateInterval);

        // Entry reset task
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            playerDataManager.checkEntryReset();
        }, 0L, 20L * 60L);

        // ActionBar update task (runs every 10 ticks = 0.5 seconds)
        actionBarTask = new ActionBarTask(this);
        actionBarTask.runTaskTimer(this, 0L, 10L);
    }

    public void reload() {
        configManager.loadConfigs();
        guiConfigManager.reload();
        dungeonManager.loadDungeons();
        leaderboardManager.updateLeaderboards();
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public NamespacedKey getDungeonKey() {
        return dungeonKey;
    }

    public NamespacedKey getDeleteKey() {
        return deleteKey;
    }

    public static ParaDungeon getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GUIConfigManager getGUIConfigManager() {
        return guiConfigManager;
    }
}