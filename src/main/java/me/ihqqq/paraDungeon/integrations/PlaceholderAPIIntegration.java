package me.ihqqq.paraDungeon.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.managers.LeaderboardManager;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import me.ihqqq.paraDungeon.models.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIIntegration extends PlaceholderExpansion {

    private final ParaDungeon plugin;

    public PlaceholderAPIIntegration(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "paradungeon";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ihqqq";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());

        switch (params) {
            case "in_dungeon":
                return data.isInDungeon() ? "true" : "false";
            case "current_dungeon":
                return room != null ? room.getDungeon().getDisplayName() : "None";
            case "current_stage":
                return room != null ? String.valueOf(room.getCurrentStage()) : "0";
            case "current_wave":
                return room != null ? String.valueOf(room.getCurrentWave()) : "0";
            case "lives":
                return room != null ? String.valueOf(room.getPlayerLives(player.getUniqueId())) : "0";
            case "score":
                return room != null ? String.valueOf(room.getPlayerScore(player.getUniqueId())) : "0";
            case "room_status":
                return room != null ? room.getStatus().name() : "NONE";
            case "room_players":
                return room != null ? String.valueOf(room.getPlayerCount()) : "0";
            case "room_max_players":
                return room != null ? String.valueOf(room.getDungeon().getMaxPlayers()) : "0";
            case "safe_mode":
                return room != null && room.isInSafeMode(player.getUniqueId()) ? "true" : "false";
        }

        // Placeholders với tham số động
        if (params.startsWith("entries_")) {
            String dungeonId = params.substring(8);
            return String.valueOf(data.getDungeonEntries(dungeonId));
        }

        if (params.startsWith("highscore_")) {
            String dungeonId = params.substring(10);
            return String.valueOf(data.getDungeonScore(dungeonId));
        }

        if (params.startsWith("rank_")) {
            String dungeonId = params.substring(5);
            int rank = plugin.getLeaderboardManager().getPlayerRank(dungeonId, player.getUniqueId());
            return rank != -1 ? String.valueOf(rank) : "N/A";
        }

        // Sửa lỗi xử lý placeholder của leaderboard
        if (params.startsWith("top_")) {
            return handleLeaderboardPlaceholder(params);
        }

        return null;
    }

    private String handleLeaderboardPlaceholder(String params) {
        String[] parts;
        String type;

        if (params.endsWith("_player")) {
            parts = params.substring(4, params.length() - 7).split("_");
            type = "player";
        } else if (params.endsWith("_score")) {
            parts = params.substring(4, params.length() - 6).split("_");
            type = "score";
        } else {
            return "Invalid Placeholder";
        }

        if (parts.length < 2) {
            return "N/A";
        }

        try {
            // Lấy vị trí từ phần tử cuối cùng
            int position = Integer.parseInt(parts[parts.length - 1]);

            // Ghép các phần còn lại để tạo thành dungeonId
            StringBuilder dungeonIdBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                dungeonIdBuilder.append(parts[i]);
                if (i < parts.length - 2) {
                    dungeonIdBuilder.append("_");
                }
            }
            String dungeonId = dungeonIdBuilder.toString();

            var leaderboard = plugin.getLeaderboardManager().getLeaderboard(dungeonId);
            if (position > 0 && position <= leaderboard.size()) {
                LeaderboardManager.LeaderboardEntry entry = leaderboard.get(position - 1);
                return type.equals("player") ? entry.getPlayerName() : String.valueOf(entry.getScore());
            }

        } catch (NumberFormatException e) {
            return "Invalid Position";
        }

        return type.equals("player") ? "N/A" : "0";
    }
}