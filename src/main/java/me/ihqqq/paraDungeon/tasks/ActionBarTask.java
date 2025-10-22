package me.ihqqq.paraDungeon.tasks;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import me.ihqqq.paraDungeon.models.Stage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Task to continuously update ActionBar for players in dungeons
 */
public class ActionBarTask extends BukkitRunnable {

    private final ParaDungeon plugin;

    public ActionBarTask(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null || !player.isOnline()) continue;

                DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());
                if (room == null || room.getStatus() != DungeonRoom.RoomStatus.ACTIVE) {
                    continue;
                }
                String message = buildActionBarMessage(room, player.getUniqueId());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        } catch (Exception e) {
            // Silently catch to prevent console spam
        }
    }

    private String buildActionBarMessage(DungeonRoom room, UUID playerId) {
        StringBuilder message = new StringBuilder();

        // Stage info
        message.append(ChatColor.GOLD).append("Ải ").append(room.getCurrentStage());
        message.append(ChatColor.DARK_GRAY).append(" | ");

        // Wave info
        Stage stage = room.getDungeon().getStage(room.getCurrentStage());
        if (stage != null) {
            int totalWaves = stage.getTotalWaves();
            message.append(ChatColor.YELLOW).append("Đợt ")
                    .append(room.getCurrentWave()).append("/").append(totalWaves);
            message.append(ChatColor.DARK_GRAY).append(" | ");

            // Progress bar
            int totalMobs = stage.getWave(room.getCurrentWave()) != null ?
                    stage.getWave(room.getCurrentWave()).getTotalMobs() : 0;
            int remainingMobs = room.getActiveEntities().size();
            int killedMobs = totalMobs - remainingMobs;

            String progressBar = createProgressBar(killedMobs, totalMobs, 10);
            message.append(progressBar).append(ChatColor.GRAY)
                    .append(" ").append(killedMobs).append("/").append(totalMobs);
            message.append(ChatColor.DARK_GRAY).append(" | ");
        }

        // Lives
        int lives = room.getPlayerLives(playerId);
        message.append(getLivesColor(lives)).append("❤ ").append(lives);
        message.append(ChatColor.DARK_GRAY).append(" | ");

        // Score
        int score = room.getPlayerScore(playerId);
        message.append(ChatColor.GOLD).append("⭐ ").append(ChatColor.YELLOW).append(score);
        message.append(ChatColor.DARK_GRAY).append(" | ");

        // Time
        message.append(ChatColor.AQUA).append("⌚ ").append(room.getFormattedElapsedTime());

        return message.toString();
    }

    private String createProgressBar(int current, int max, int barLength) {
        if (max == 0) return ChatColor.GRAY + "[]";

        int filled = Math.min(barLength, (int) ((double) current / max * barLength));
        StringBuilder bar = new StringBuilder();

        bar.append(ChatColor.GREEN).append("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.GRAY).append("█");
            }
        }
        bar.append(ChatColor.GREEN).append("]");

        return bar.toString();
    }

    private ChatColor getLivesColor(int lives) {
        if (lives >= 3) return ChatColor.GREEN;
        if (lives == 2) return ChatColor.YELLOW;
        if (lives == 1) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }
}