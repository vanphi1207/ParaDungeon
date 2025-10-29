package me.ihqqq.paraDungeon.managers;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import me.ihqqq.paraDungeon.models.Stage;
import me.ihqqq.paraDungeon.models.Wave;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import io.lumine.mythic.core.mobs.ActiveMob;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    // Constants
    private static final int DEFAULT_SCORE_PER_KILL = 10;
    private static final int DEFAULT_PROGRESS_BAR_LENGTH = 15;
    private static final int DEFAULT_COUNTDOWN_BAR_LENGTH = 20;
    private static final int MOB_SPAWN_DELAY_TICKS = 5;
    private static final int PARTICLE_ANIMATION_TICKS = 20;
    private static final int PARTICLE_CIRCLE_SEGMENTS = 4;
    private static final double PARTICLE_RADIUS = 1.0;
    private static final int COMPLETION_DELAY_TICKS = 200;
    private static final int FAILURE_DELAY_TICKS = 100;
    private static final float DEFAULT_SOUND_VOLUME = 1.0f;
    private static final float DEFAULT_SOUND_PITCH = 1.0f;

    private final ParaDungeon plugin;
    private final Map<String, DungeonRoom> rooms;
    private final Map<UUID, String> playerRooms;
    private final Random random = new Random();

    public RoomManager(ParaDungeon plugin) {
        this.plugin = plugin;
        this.rooms = new ConcurrentHashMap<>();
        this.playerRooms = new ConcurrentHashMap<>();
    }

    public DungeonRoom createRoom(Dungeon dungeon) {
        if (dungeon == null) {
            plugin.getLogger().severe("Cannot create room: dungeon is null");
            return null;
        }
        String roomId = UUID.randomUUID().toString();
        DungeonRoom room = new DungeonRoom(roomId, dungeon);
        rooms.put(roomId, room);
        plugin.getLogger().info("Created room " + roomId + " for dungeon " + dungeon.getId());
        return room;
    }

    public void joinRoom(Player player, DungeonRoom room) {
        if (player == null || room == null) {
            plugin.getLogger().warning("Cannot join room: player or room is null");
            return;
        }
        if (!player.isOnline()) {
            plugin.getLogger().warning("Cannot join room: player is not online");
            return;
        }
        
        room.addPlayer(player.getUniqueId());
        playerRooms.put(player.getUniqueId(), room.getRoomId());
        
        Location lobbyPoint = room.getDungeon().getLobbyPoint();
        if (lobbyPoint != null) {
            player.teleport(lobbyPoint);
        } else {
            plugin.getLogger().warning("Lobby point is not set for dungeon " + room.getDungeon().getId());
        }
        
        player.sendMessage(plugin.getConfigManager().getMessage("lobby.joined", "dungeon", room.getDungeon().getDisplayName()));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, DEFAULT_SOUND_VOLUME, 1.5f);

        if (room.hasMinPlayers() && room.getStatus() == DungeonRoom.RoomStatus.WAITING) {
            startCountdown(room);
        }
    }

    public void leaveRoom(Player player) {
        DungeonRoom room = getPlayerRoom(player.getUniqueId());
        if (room == null) return;

        // Dịch chuyển người chơi ra ngoài trước khi xử lý logic phòng
        teleportPlayerOut(player, room.getDungeon());

        // Xóa người chơi khỏi phòng
        room.removePlayer(player.getUniqueId());
        playerRooms.remove(player.getUniqueId());
        player.sendMessage(plugin.getConfigManager().getMessage("lobby.left"));

        // Kiểm tra và xử lý phòng sau khi người chơi rời đi
        if (room.getPlayerCount() == 0) {
            deleteRoom(room);
        } else if (!room.hasMinPlayers() && room.getStatus() == DungeonRoom.RoomStatus.COUNTDOWN) {
            cancelCountdown(room);
        }
    }

    private void startCountdown(DungeonRoom room) {
        room.setStatus(DungeonRoom.RoomStatus.COUNTDOWN);
        int countdownTime = plugin.getConfig().getInt("settings.lobby.countdown-time", 10);
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int timeLeft = countdownTime;
            @Override
            public void run() {
                if (timeLeft <= 0 || !room.hasMinPlayers()) {
                    Bukkit.getScheduler().cancelTask(room.getCountdownTask());
                    if (!room.hasMinPlayers()) cancelCountdown(room);
                    else startDungeon(room);
                    return;
                }

                for (UUID uuid : room.getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendTitle(
                                ChatColor.GOLD + "" + ChatColor.BOLD + timeLeft,
                                ChatColor.YELLOW + "Chuẩn bị bắt đầu...",
                                5, 20, 5
                        );

                        String progressBar = createProgressBar(countdownTime - timeLeft, countdownTime, DEFAULT_COUNTDOWN_BAR_LENGTH);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(ChatColor.YELLOW + "Đếm ngược: " + progressBar + ChatColor.GOLD + " " + timeLeft + "s"));

                        if (timeLeft <= 5) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f + (timeLeft * 0.1f));
                        } else if (timeLeft % 5 == 0) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.0f);
                        }
                    }
                }

                if (timeLeft <= 5 || timeLeft % 10 == 0) {
                    broadcastToRoom(room, plugin.getConfigManager().getMessage("lobby.countdown-start", "time", String.valueOf(timeLeft)));
                }
                timeLeft--;
            }
        }, 0L, 20L).getTaskId();
        room.setCountdownTask(taskId);
    }

    private void cancelCountdown(DungeonRoom room) {
        if (room.getCountdownTask() != -1) {
            Bukkit.getScheduler().cancelTask(room.getCountdownTask());
            room.setCountdownTask(-1);
        }
        room.setStatus(DungeonRoom.RoomStatus.WAITING);
        broadcastToRoom(room, plugin.getConfigManager().getMessage("lobby.countdown-cancel"));

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle("", "", 0, 0, 0);
            }
        }
    }

    public void startDungeon(DungeonRoom room) {
        if (room == null) {
            plugin.getLogger().severe("Cannot start dungeon: room is null");
            return;
        }
        if (room.getDungeon() == null) {
            plugin.getLogger().severe("Cannot start dungeon: dungeon is null");
            return;
        }
        
        room.setStatus(DungeonRoom.RoomStatus.ACTIVE);
        room.setStartTime(System.currentTimeMillis());
        broadcastToRoom(room, plugin.getConfigManager().getMessage("lobby.starting"));

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(
                        ChatColor.GREEN + "" + ChatColor.BOLD + "BẮT ĐẦU!",
                        ChatColor.YELLOW + room.getDungeon().getDisplayName(),
                        10, 40, 10
                );
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }

        if (room.getDungeon().getSpawnPoint() != null) {
            room.getPlayers().forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.teleport(room.getDungeon().getSpawnPoint());
            });
        }

        startStage(room, 1);
    }

    public void startStage(DungeonRoom room, int stageNum) {
        if (room == null || room.getDungeon() == null) {
            plugin.getLogger().severe("Cannot start stage: room or dungeon is null");
            return;
        }
        
        Stage stage = room.getDungeon().getStage(stageNum);
        if (stage == null) {
            plugin.getLogger().info("No more stages for dungeon " + room.getDungeon().getId() + ". Completing dungeon.");
            completeDungeon(room);
            return;
        }
        room.setCurrentStage(stageNum);

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                if (stage.isBoss()) {
                    p.sendTitle(
                            ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚔ BOSS ⚔",
                            ChatColor.RED + "Cẩn thận!",
                            10, 40, 10
                    );
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                } else {
                    p.sendTitle(
                            ChatColor.AQUA + "" + ChatColor.BOLD + "ẢI " + stageNum,
                            ChatColor.YELLOW + "Bắt đầu!",
                            10, 30, 10
                    );
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.2f);
                }
            }
        }

        broadcastToRoom(room, plugin.getConfigManager().getMessage(
                stage.isBoss() ? "dungeon.boss-spawn" : "dungeon.stage-start",
                "stage", String.valueOf(stageNum)
        ));

        startWave(room, stageNum, 1);
    }

    public void startWave(DungeonRoom room, int stageNum, int waveNum) {
        if (room == null || room.getDungeon() == null) {
            plugin.getLogger().severe("Cannot start wave: room or dungeon is null");
            return;
        }
        
        Stage stage = room.getDungeon().getStage(stageNum);
        if (stage == null) {
            plugin.getLogger().warning("Stage " + stageNum + " not found for dungeon " + room.getDungeon().getId());
            startStage(room, stageNum + 1);
            return;
        }
        
        Wave wave = stage.getWave(waveNum);
        if (wave == null) {
            plugin.getLogger().info("No more waves for stage " + stageNum + ". Moving to next stage.");
            startStage(room, stageNum + 1);
            return;
        }
        room.setCurrentWave(waveNum);

        int totalWaves = stage.getTotalWaves();

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(
                        "",
                        ChatColor.YELLOW + "Đợt " + waveNum + "/" + totalWaves,
                        5, 25, 5
                );
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
        }

        broadcastToRoom(room, plugin.getConfigManager().getMessage(
                "dungeon.wave-start",
                "wave", String.valueOf(waveNum),
                "total", String.valueOf(totalWaves)
        ));

        spawnWaveMobs(room, wave);
    }

    private void spawnWaveMobs(DungeonRoom room, Wave wave) {
        List<Location> waveSpawnPoints = wave.getSpawnLocations();
        Location defaultSpawnPoint = room.getDungeon().getSpawnPoint();
        boolean useWaveSpawns = !waveSpawnPoints.isEmpty();

        if (!useWaveSpawns && defaultSpawnPoint == null) {
            plugin.getLogger().severe("No spawn points for dungeon " + room.getDungeon().getId());
            return;
        }

        boolean enableGlow = plugin.getConfig().getBoolean("settings.mob-glow.enabled", true);
        ChatColor glowColor = getGlowColor(plugin.getConfig().getString("settings.mob-glow.color", "RED"));

        for (Wave.MobSpawn mobSpawn : wave.getMobs()) {
            for (int i = 0; i < mobSpawn.getAmount(); i++) {
                Location spawnPoint = useWaveSpawns ?
                        waveSpawnPoints.get(random.nextInt(waveSpawnPoints.size())) :
                        defaultSpawnPoint;

                spawnPreSpawnParticles(spawnPoint);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ActiveMob mob = MythicBukkit.inst().getMobManager().spawnMob(
                            mobSpawn.getMobId(),
                            spawnPoint
                    );

                    if (mob != null && mob.getEntity().getBukkitEntity() instanceof LivingEntity) {
                        LivingEntity entity = (LivingEntity) mob.getEntity().getBukkitEntity();
                        room.addEntity(entity);

                        if (enableGlow) {
                            applyGlowEffect(entity, glowColor);
                        }

                        spawnPostSpawnParticles(entity.getLocation());

                        entity.getWorld().playSound(
                                entity.getLocation(),
                                Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
                                0.8f,
                                0.8f
                        );
                    } else {
                        plugin.getLogger().warning("Invalid mob type or failed to spawn: " + mobSpawn.getMobId());
                    }
                }, i * MOB_SPAWN_DELAY_TICKS);
            }
        }
    }

    private void applyGlowEffect(LivingEntity entity, ChatColor color) {
        entity.setGlowing(true);

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "dungeon_mob_" + color.name().toLowerCase();
            org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);

            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                team.setColor(color);
            }

            team.addEntry(entity.getUniqueId().toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set glow color: " + e.getMessage());
        }
    }

    private ChatColor getGlowColor(String colorName) {
        try {
            return ChatColor.valueOf(colorName.toUpperCase());
        } catch (Exception e) {
            return ChatColor.RED;
        }
    }

    private void spawnPreSpawnParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= PARTICLE_ANIMATION_TICKS) {
                    Bukkit.getScheduler().cancelTask(this.hashCode());
                    return;
                }

                double radius = PARTICLE_RADIUS;
                double y = ticks * 0.1;
                for (int i = 0; i < PARTICLE_CIRCLE_SEGMENTS; i++) {
                    double angle = (ticks * 18 + i * 90) * Math.PI / 180;
                    double x = location.getX() + radius * Math.cos(angle);
                    double z = location.getZ() + radius * Math.sin(angle);

                    world.spawnParticle(
                            Particle.PORTAL,
                            x, location.getY() + y, z,
                            5, 0.1, 0.1, 0.1, 0.05
                    );
                }
                ticks++;
            }
        }, 0L, 1L);
    }

    private void spawnPostSpawnParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(
                Particle.EXPLOSION,
                location.clone().add(0, 1, 0),
                3, 0.5, 0.5, 0.5, 0
        );

        world.spawnParticle(
                Particle.SMOKE,
                location.clone().add(0, 0.5, 0),
                20, 0.5, 0.5, 0.5, 0.05
        );
    }

    public void onMobKilled(DungeonRoom room, @Nullable Player killer) {
        if (killer != null && room.getPlayers().contains(killer.getUniqueId())) {
            room.addScore(killer.getUniqueId(), DEFAULT_SCORE_PER_KILL);

            int score = room.getPlayerScore(killer.getUniqueId());
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.GOLD + "+" + ChatColor.YELLOW + DEFAULT_SCORE_PER_KILL + " " +
                            ChatColor.GRAY + "| " + ChatColor.GOLD + "Tổng: " + ChatColor.YELLOW + score));

            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            room.getActiveEntities().removeIf(e -> e == null || e.isDead());
            updateProgressActionBar(room);

            if (room.getActiveEntities().isEmpty()) {
                startWave(room, room.getCurrentStage(), room.getCurrentWave() + 1);
            }
        }, 1L);
    }

    private void updateProgressActionBar(DungeonRoom room) {
        Stage stage = room.getDungeon().getStage(room.getCurrentStage());
        if (stage == null) return;

        int totalWaves = stage.getTotalWaves();
        int currentWave = room.getCurrentWave();
        int totalMobs = stage.getWave(currentWave).getTotalMobs();
        int remainingMobs = room.getActiveEntities().size();
        int killedMobs = totalMobs - remainingMobs;

        String progressBar = createProgressBar(killedMobs, totalMobs, DEFAULT_PROGRESS_BAR_LENGTH);

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                String message = ChatColor.GOLD + "Ải " + room.getCurrentStage() +
                        ChatColor.GRAY + " | " + ChatColor.YELLOW + "Đợt " + currentWave + "/" + totalWaves +
                        ChatColor.GRAY + " | " + progressBar + ChatColor.GRAY + " " + killedMobs + "/" + totalMobs;

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        }
    }

    private String createProgressBar(int current, int max, int barLength) {
        if (max == 0) return ChatColor.GRAY + "[]";

        int filled = (int) ((double) current / max * barLength);
        StringBuilder bar = new StringBuilder(ChatColor.GREEN + "[");

        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append(ChatColor.GRAY).append("█");
            }
        }
        bar.append(ChatColor.GREEN).append("]");

        return bar.toString();
    }

    public void completeDungeon(DungeonRoom room) {
        room.setStatus(DungeonRoom.RoomStatus.COMPLETED);

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(
                        ChatColor.GOLD + "" + ChatColor.BOLD + "CHIẾN THẮNG!",
                        ChatColor.YELLOW + "Hoàn thành phó bản",
                        10, 60, 20
                );

                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 1, 2, 1, 0.1);
                p.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.5, 1, 0.5, 0.1);
            }
        }

        broadcastToRoom(room, plugin.getConfigManager().getMessage("dungeon.victory"));

        room.getPlayers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.getPlayerDataManager().getPlayerData(uuid)
                        .setDungeonScore(room.getDungeon().getId(), room.getPlayerScore(uuid));
                plugin.getRewardManager().giveRewards(p, room.getDungeon(), room.getPlayerScore(uuid));
            }
        });

        plugin.getLeaderboardManager().updateLeaderboard(room.getDungeon().getId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> deleteRoom(room), COMPLETION_DELAY_TICKS);
    }

    public void failDungeon(DungeonRoom room) {
        room.setStatus(DungeonRoom.RoomStatus.FAILED);

        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(
                        ChatColor.RED + "" + ChatColor.BOLD + "THẤT BẠI",
                        ChatColor.GRAY + "Hãy thử lại!",
                        10, 60, 20
                );

                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 0.5f);
            }
        }

        broadcastToRoom(room, plugin.getConfigManager().getMessage("dungeon.defeat"));
        Bukkit.getScheduler().runTaskLater(plugin, () -> deleteRoom(room), FAILURE_DELAY_TICKS);
    }

    private void deleteRoom(DungeonRoom room) {
        room.clearEntities();
        for (UUID playerId : new HashSet<>(room.getPlayers())) {
            playerRooms.remove(playerId);
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                teleportPlayerOut(p, room.getDungeon());
                p.sendTitle("", "", 0, 0, 0);
            }
        }
        if (room.getCountdownTask() != -1) Bukkit.getScheduler().cancelTask(room.getCountdownTask());
        rooms.remove(room.getRoomId());
    }

    private void teleportPlayerOut(Player player, Dungeon dungeon) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Location endPoint = dungeon.getEndPoint();
        if (endPoint != null) {
            player.teleport(endPoint);
        } else {
            Location spawn = player.getRespawnLocation();
            if (spawn == null) {
                spawn = player.getWorld().getSpawnLocation();
            }
            player.teleport(spawn);
        }
    }

    public void checkAutoRenew() {
        long autoRenewTime = plugin.getConfig().getLong("settings.auto-renew-time", 10) * 60000;
        rooms.values().stream()
                .filter(r -> r.getStatus() == DungeonRoom.RoomStatus.WAITING &&
                        System.currentTimeMillis() - r.getCreationTime() > autoRenewTime)
                .forEach(room -> {
                    broadcastToRoom(room, plugin.getConfigManager().getMessage(
                            "lobby.auto-renew",
                            "time", String.valueOf(autoRenewTime / 60000)
                    ));
                    new HashSet<>(room.getPlayers()).forEach(uuid -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if(p != null) leaveRoom(p);
                    });
                });
    }

    public void clearAllRooms() {
        new ArrayList<>(rooms.values()).forEach(this::deleteRoom);
    }

    public DungeonRoom getPlayerRoom(UUID playerId) {
        String roomId = playerRooms.get(playerId);
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    public Collection<DungeonRoom> getAvailableRooms(Dungeon dungeon) {
        return rooms.values().stream()
                .filter(r -> r.getDungeon().getId().equals(dungeon.getId()) &&
                        !r.isFull() && r.getStatus() == DungeonRoom.RoomStatus.WAITING)
                .toList();
    }

    private void broadcastToRoom(DungeonRoom room, String message) {
        if (room == null || message == null) {
            plugin.getLogger().warning("Cannot broadcast to room: room or message is null");
            return;
        }
        room.getPlayers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
            }
        });
    }

    public Collection<DungeonRoom> getAllRooms() {
        return rooms.values();
    }
}