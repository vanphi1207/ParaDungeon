package me.ihqqq.paraDungeon.commands;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.managers.LeaderboardManager;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import me.ihqqq.paraDungeon.models.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Collectors;

public class DungeonCommand implements CommandExecutor, TabCompleter {

    private final ParaDungeon plugin;

    public DungeonCommand(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open main menu GUI instead of help
            plugin.getGUIManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
            case "menu":
                plugin.getGUIManager().openMainMenu(player);
                break;
            case "list":
                plugin.getGUIManager().openDungeonList(player);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cSử dụng: /dungeon join <tên>");
                    return true;
                }
                joinDungeon(player, args[1]);
                break;
            case "leave":
                leaveDungeon(player);
                break;
            case "top":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cSử dụng: /dungeon top <tên>");
                    return true;
                }
                plugin.getGUIManager().openLeaderboard(player, args[1]);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cSử dụng: /dungeon info <tên>");
                    return true;
                }
                Dungeon dungeon = plugin.getDungeonManager().getDungeon(args[1]);
                if (dungeon == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", args[1]));
                    return true;
                }
                plugin.getGUIManager().openDungeonInfo(player, dungeon);
                break;
            case "stats":
                plugin.getGUIManager().openPlayerStats(player);
                break;
            case "settings":
                plugin.getGUIManager().openSettings(player);
                break;
            case "help":
                sendHelp(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        List<String> help = plugin.getConfigManager().getMessagesConfig().getStringList("help.player");
        for (String line : help) {
            player.sendMessage(line.replace("&", "§"));
        }
    }

    private void joinDungeon(Player player, String dungeonId) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);

        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found",
                    "dungeon", dungeonId));
            return;
        }

        // Check if already in dungeon
        if (plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).isInDungeon()) {
            player.sendMessage(plugin.getConfigManager().getMessage("lobby.already-in-dungeon"));
            return;
        }

        // Check entries
        if (!plugin.getPlayerDataManager().hasAvailableEntries(player, dungeonId)) {
            player.sendMessage(plugin.getConfigManager().getMessage("lobby.no-entries"));
            return;
        }

        // Find or create room
        Collection<DungeonRoom> availableRooms = plugin.getRoomManager().getAvailableRooms(dungeon);
        DungeonRoom room;

        if (availableRooms.isEmpty()) {
            room = plugin.getRoomManager().createRoom(dungeon);
        } else {
            room = availableRooms.iterator().next();
        }

        if (room.isFull()) {
            player.sendMessage(plugin.getConfigManager().getMessage("lobby.full"));
            return;
        }

        // Consume entry
        plugin.getPlayerDataManager().consumeEntry(player, dungeonId);

        // Join room
        plugin.getRoomManager().joinRoom(player, room);
    }

    private void leaveDungeon(Player player) {
        if (!plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).isInDungeon()) {
            player.sendMessage(plugin.getConfigManager().getMessage("lobby.not-in-lobby"));
            return;
        }

        plugin.getRoomManager().leaveRoom(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("gui", "menu", "list", "join", "leave", "top", "info", "stats", "settings", "help")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("join") ||
                args[0].equalsIgnoreCase("top") ||
                args[0].equalsIgnoreCase("info"))) {
            return plugin.getDungeonManager().getAllDungeons().stream()
                    .map(Dungeon::getId)
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}