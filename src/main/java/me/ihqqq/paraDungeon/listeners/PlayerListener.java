package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
    
    private final ParaDungeon plugin;
    
    public PlayerListener(ParaDungeon plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player data
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        
        // Remove from dungeon room if in one
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());
        if (room != null) {
            plugin.getRoomManager().leaveRoom(player);
        }
        
        // Unload player data
        plugin.getPlayerDataManager().unloadPlayerData(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());
        
        if (room == null || room.getStatus() != DungeonRoom.RoomStatus.ACTIVE) {
            return;
        }
        
        // Decrement lives
        room.decrementPlayerLives(player.getUniqueId());
        int livesLeft = room.getPlayerLives(player.getUniqueId());
        
        if (livesLeft <= 0) {
            // No more lives
            player.sendMessage(plugin.getConfigManager().getMessage("dungeon.no-lives"));
            
            // Check if all players are dead
            boolean allDead = true;
            for (java.util.UUID playerId : room.getPlayers()) {
                if (room.getPlayerLives(playerId) > 0) {
                    allDead = false;
                    break;
                }
            }
            
            if (allDead) {
                plugin.getRoomManager().failDungeon(room);
            }
        } else {
            // Set respawn cooldown
            int cooldown = plugin.getConfig().getInt("settings.respawn-system.respawn-cooldown", 5);
            room.setRespawnCooldown(player.getUniqueId(), 
                System.currentTimeMillis() + (cooldown * 1000L));
            
            player.sendMessage(plugin.getConfigManager().getMessage("dungeon.lives-remaining",
                "lives", String.valueOf(livesLeft)));
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());
        
        if (room == null || room.getStatus() != DungeonRoom.RoomStatus.ACTIVE) {
            return;
        }
        
        // Teleport to spawn point
        if (plugin.getConfig().getBoolean("settings.respawn-system.teleport-to-spawn", true)) {
            if (room.getDungeon().getSpawnPoint() != null) {
                event.setRespawnLocation(room.getDungeon().getSpawnPoint());
            }
        }
        
        // Set safe mode
        int safeTime = plugin.getConfig().getInt("settings.respawn-system.safe-time", 5);
        room.setSafeMode(player.getUniqueId(), System.currentTimeMillis() + (safeTime * 1000L));
        
        // Schedule message
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(plugin.getConfigManager().getMessage("dungeon.respawned",
                "time", String.valueOf(safeTime)));
        }, 5L);
    }
}
