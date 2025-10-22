package me.ihqqq.paraDungeon.commands;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.Dungeon;
import me.ihqqq.paraDungeon.models.Stage;
import me.ihqqq.paraDungeon.models.Wave;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonAdminCommand implements CommandExecutor, TabCompleter {

    private final ParaDungeon plugin;

    public DungeonAdminCommand(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("paradungeon.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /dgadmin create <name>");
                    return true;
                }
                createDungeon(sender, args[1]);
                break;
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /dgadmin delete <dungeon>");
                    return true;
                }
                deleteDungeon(sender, args[1]);
                break;
            case "addstage":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /dgadmin addstage <dungeon> <stage_number> [NORMAL|BOSS]");
                    return true;
                }
                addStage(sender, args[1], args[2], args.length > 3 ? args[3] : "NORMAL");
                break;
            case "addwave":
                if (args.length < 6) {
                    sender.sendMessage("§cUsage: /dgadmin addwave <dungeon> <stage> <wave> <mob_id> <amount>");
                    return true;
                }
                addWave(sender, args[1], args[2], args[3], args[4], args[5]);
                break;
            case "setspawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /dgadmin setspawn <dungeon>");
                    return true;
                }
                setSpawn((Player) sender, args[1]);
                break;
            case "setlobby":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /dgadmin setlobby <dungeon>");
                    return true;
                }
                setLobby((Player) sender, args[1]);
                break;
            case "setwavespawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /dgadmin setwavespawn <dungeon> <stage> <wave>");
                    return true;
                }
                setWaveSpawn((Player) sender, args[1], args[2], args[3]);
                break;
            case "reload":
                plugin.reload();
                sender.sendMessage(plugin.getConfigManager().getMessage("general.reload-success"));
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> help = plugin.getConfigManager().getMessagesConfig().getStringList("help.admin");
        for (String line : help) {
            sender.sendMessage(line.replace("&", "§"));
        }
    }

    private void createDungeon(CommandSender sender, String dungeonId) {
        if (plugin.getDungeonManager().getDungeon(dungeonId) != null) {
            sender.sendMessage("§cDungeon already exists!");
            return;
        }
        Dungeon dungeon = new Dungeon(dungeonId);
        dungeon.setDisplayName("§6" + dungeonId);
        dungeon.setMinPlayers(1);
        dungeon.setMaxPlayers(5);
        dungeon.setEntriesPerReset(3);
        dungeon.setRespawnLives(3);
        plugin.getDungeonManager().saveDungeon(dungeon);
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.dungeon-created", "dungeon", dungeonId));
    }

    private void deleteDungeon(CommandSender sender, String dungeonId) {
        if (plugin.getDungeonManager().getDungeon(dungeonId) == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }
        plugin.getDungeonManager().deleteDungeon(dungeonId);
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.dungeon-deleted", "dungeon", dungeonId));
    }

    private void addStage(CommandSender sender, String dungeonId, String stageNumStr, String typeStr) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }
        try {
            int stageNum = Integer.parseInt(stageNumStr);
            Stage.StageType type = Stage.StageType.valueOf(typeStr.toUpperCase());
            dungeon.addStage(stageNum, new Stage(stageNum, type));
            plugin.getDungeonManager().saveDungeon(dungeon);
            sender.sendMessage(plugin.getConfigManager().getMessage("admin.stage-added", "stage", stageNumStr, "dungeon", dungeonId));
        } catch (Exception e) {
            sender.sendMessage("§cInvalid arguments.");
        }
    }

    private void addWave(CommandSender sender, String dungeonId, String stageNumStr, String waveNumStr, String mobId, String amountStr) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }
        try {
            int stageNum = Integer.parseInt(stageNumStr);
            int waveNum = Integer.parseInt(waveNumStr);
            int amount = Integer.parseInt(amountStr);
            Stage stage = dungeon.getStage(stageNum);
            if (stage == null) {
                sender.sendMessage("§cStage does not exist.");
                return;
            }
            Wave wave = stage.getWave(waveNum);
            if (wave == null) {
                wave = new Wave(waveNum);
                stage.addWave(waveNum, wave);
            }
            wave.addMob(mobId, amount);
            plugin.getDungeonManager().saveDungeon(dungeon);
            sender.sendMessage(plugin.getConfigManager().getMessage("admin.wave-added", "stage", stageNumStr));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number.");
        }
    }

    private void setSpawn(Player player, String dungeonId) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }
        dungeon.setSpawnPoint(player.getLocation());
        plugin.getDungeonManager().saveDungeon(dungeon);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.setspawn", "dungeon", dungeonId));
    }

    private void setLobby(Player player, String dungeonId) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }
        dungeon.setLobbyPoint(player.getLocation());
        plugin.getDungeonManager().saveDungeon(dungeon);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.setlobby", "dungeon", dungeonId));
    }

    private void setWaveSpawn(Player player, String dungeonId, String stageNumStr, String waveNumStr) {
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.dungeon-not-found", "dungeon", dungeonId));
            return;
        }
        try {
            int stageNum = Integer.parseInt(stageNumStr);
            int waveNum = Integer.parseInt(waveNumStr);
            Stage stage = dungeon.getStage(stageNum);
            if (stage == null) {
                player.sendMessage("§cStage " + stageNum + " does not exist!");
                return;
            }
            Wave wave = stage.getWave(waveNum);
            if (wave == null) {
                player.sendMessage("§cWave " + waveNum + " does not exist in stage " + stageNum + "!");
                return;
            }
            wave.addSpawnLocation(player.getLocation());
            plugin.getDungeonManager().saveDungeon(dungeon);
            player.sendMessage(plugin.getConfigManager().getMessage("admin.wave-spawn-set", "wave", waveNumStr, "stage", stageNumStr, "dungeon", dungeonId));
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", "stage/wave number"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "addstage", "addwave", "setspawn", "setlobby", "setwavespawn", "reload")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("reload")) {
            return plugin.getDungeonManager().getAllDungeons().stream()
                    .map(Dungeon::getId).filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return null;
    }
}