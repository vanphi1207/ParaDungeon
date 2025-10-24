package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class PlayerListener implements Listener {

    private final ParaDungeon plugin;

    public PlayerListener(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());
        if (room != null) {
            plugin.getRoomManager().leaveRoom(player);
        }
        plugin.getPlayerDataManager().unloadPlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());

        if (room == null || room.getStatus() != DungeonRoom.RoomStatus.ACTIVE) {
            return;
        }

        room.decrementPlayerLives(player.getUniqueId());
        int livesLeft = room.getPlayerLives(player.getUniqueId());

        if (livesLeft <= 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("dungeon.no-lives"));
            boolean allDead = room.getPlayers().stream().allMatch(p -> room.getPlayerLives(p) <= 0);
            if (allDead) {
                plugin.getRoomManager().failDungeon(room);
            }
        } else {
            int cooldown = plugin.getConfig().getInt("settings.respawn-system.respawn-cooldown", 5);
            room.setRespawnCooldown(player.getUniqueId(), System.currentTimeMillis() + (cooldown * 1000L));
            player.sendMessage(plugin.getConfigManager().getMessage("dungeon.lives-remaining", "lives", String.valueOf(livesLeft)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());

        if (room == null || room.getStatus() != DungeonRoom.RoomStatus.ACTIVE) {
            return;
        }

        if (plugin.getConfig().getBoolean("settings.respawn-system.teleport-to-spawn", true)) {
            Location spawnPoint = room.getDungeon().getSpawnPoint();
            if (spawnPoint != null) {
                event.setRespawnLocation(spawnPoint);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.teleport(spawnPoint);
                    }
                }, 1L);
            }
        }

        int safeTime = plugin.getConfig().getInt("settings.respawn-system.safe-time", 5);
        room.setSafeMode(player.getUniqueId(), System.currentTimeMillis() + (safeTime * 1000L));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(plugin.getConfigManager().getMessage("dungeon.respawned", "time", String.valueOf(safeTime)));
            }
        }, 5L);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());
        if (room != null && room.getStatus() == DungeonRoom.RoomStatus.ACTIVE) {
            if (!plugin.getConfig().getBoolean("settings.dungeon-restrictions.enabled", true)) {
                return;
            }
            String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();
            List<String> blockedCmds = plugin.getConfig().getStringList("settings.dungeon-restrictions.blocked-cmds");
            if (blockedCmds.stream().anyMatch(blockedCmd -> command.equalsIgnoreCase(blockedCmd))) {
                event.setCancelled(true);
                // SỬA LỖI: Gọi trực tiếp message, không ghép prefix
                player.sendMessage(plugin.getConfigManager().getMessage("dungeon.command-blocked"));
            }
        }
    }
}