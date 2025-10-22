package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DungeonManager {

    private final ParaDungeon plugin;
    private final Map<String, Dungeon> dungeons;

    public DungeonManager(ParaDungeon plugin) {
        this.plugin = plugin;
        this.dungeons = new HashMap<>();
    }

    public void loadDungeons() {
        dungeons.clear();
        ConfigurationSection dungeonsSection = plugin.getConfigManager().getDungeonsConfig().getConfigurationSection("dungeons");
        if (dungeonsSection == null) {
            plugin.getLogger().warning("No dungeons configured!");
            return;
        }
        for (String dungeonId : dungeonsSection.getKeys(false)) {
            try {
                Dungeon dungeon = loadDungeon(dungeonId, dungeonsSection.getConfigurationSection(dungeonId));
                if (dungeon != null) {
                    dungeons.put(dungeonId, dungeon);
                    plugin.getLogger().info("Loaded dungeon: " + dungeonId);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load dungeon " + dungeonId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("Loaded " + dungeons.size() + " dungeon(s)");
    }

    private Dungeon loadDungeon(String id, ConfigurationSection section) {
        if (section == null) return null;
        Dungeon dungeon = new Dungeon(id);
        dungeon.setDisplayName(section.getString("display-name", id));
        dungeon.setDescription(section.getStringList("description"));
        dungeon.setMinPlayers(section.getInt("min-players", 1));
        dungeon.setMaxPlayers(section.getInt("max-players", 5));
        dungeon.setEntriesPerReset(section.getInt("entries-per-reset", 3));
        dungeon.setRespawnLives(section.getInt("respawn-lives", 3));

        if (section.getConfigurationSection("spawn-point") != null)
            dungeon.setSpawnPoint(loadLocation(section.getConfigurationSection("spawn-point")));
        if (section.getConfigurationSection("lobby-point") != null)
            dungeon.setLobbyPoint(loadLocation(section.getConfigurationSection("lobby-point")));

        ConfigurationSection stagesSection = section.getConfigurationSection("stages");
        if (stagesSection != null) {
            for (String stageKey : stagesSection.getKeys(false)) {
                ConfigurationSection stageSection = stagesSection.getConfigurationSection(stageKey);
                Stage stage = new Stage(Integer.parseInt(stageKey), Stage.StageType.valueOf(stageSection.getString("type", "NORMAL")));
                ConfigurationSection wavesSection = stageSection.getConfigurationSection("waves");
                if (wavesSection != null) {
                    for (String waveKey : wavesSection.getKeys(false)) {
                        ConfigurationSection waveSection = wavesSection.getConfigurationSection(waveKey);
                        Wave wave = new Wave(Integer.parseInt(waveKey));
                        for (Map<?, ?> mobMap : waveSection.getMapList("mobs")) {
                            wave.addMob((String) mobMap.get("mob"), (int) mobMap.get("amount"));
                        }
                        if (waveSection.isSet("spawn-locations")) {
                            for (Map<?, ?> locMap : waveSection.getMapList("spawn-locations")) {
                                Location loc = mapToLocation(locMap);
                                if (loc != null) wave.addSpawnLocation(loc);
                            }
                        }
                        stage.addWave(wave.getWaveNumber(), wave);
                    }
                }
                dungeon.addStage(stage.getStageNumber(), stage);
            }
        }
        // ... load rewards logic ...
        return dungeon;
    }

    public void saveDungeon(Dungeon dungeon) {
        ConfigurationSection dungeonsSection = plugin.getConfigManager().getDungeonsConfig().getConfigurationSection("dungeons");
        if (dungeonsSection == null) dungeonsSection = plugin.getConfigManager().getDungeonsConfig().createSection("dungeons");

        ConfigurationSection dungeonSection = dungeonsSection.createSection(dungeon.getId());
        dungeonSection.set("display-name", dungeon.getDisplayName());
        dungeonSection.set("description", dungeon.getDescription());
        dungeonSection.set("min-players", dungeon.getMinPlayers());
        dungeonSection.set("max-players", dungeon.getMaxPlayers());
        // ... set other properties ...

        if (dungeon.getSpawnPoint() != null)
            saveLocation(dungeonSection.createSection("spawn-point"), dungeon.getSpawnPoint());
        if (dungeon.getLobbyPoint() != null)
            saveLocation(dungeonSection.createSection("lobby-point"), dungeon.getLobbyPoint());

        ConfigurationSection stagesSection = dungeonSection.createSection("stages");
        for (Stage stage : dungeon.getStages().values()) {
            ConfigurationSection stageSection = stagesSection.createSection(String.valueOf(stage.getStageNumber()));
            stageSection.set("type", stage.getType().name());
            ConfigurationSection wavesSection = stageSection.createSection("waves");
            for (Wave wave : stage.getWaves().values()) {
                ConfigurationSection waveSection = wavesSection.createSection(String.valueOf(wave.getWaveNumber()));
                List<Map<String, Object>> mobsToSave = new ArrayList<>();
                wave.getMobs().forEach(m -> mobsToSave.add(Map.of("mob", m.getMobId(), "amount", m.getAmount())));
                waveSection.set("mobs", mobsToSave);

                if (!wave.getSpawnLocations().isEmpty()) {
                    List<Map<String, Object>> locationsToSave = new ArrayList<>();
                    wave.getSpawnLocations().forEach(loc -> locationsToSave.add(locationToMap(loc)));
                    waveSection.set("spawn-locations", locationsToSave);
                }
            }
        }
        plugin.getConfigManager().saveDungeonsConfig();
        dungeons.put(dungeon.getId(), dungeon);
    }

    private void saveLocation(ConfigurationSection section, Location loc) {
        section.set("world", loc.getWorld().getName());
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
        section.set("yaw", loc.getYaw());
        section.set("pitch", loc.getPitch());
    }

    private Location loadLocation(ConfigurationSection section) {
        World world = Bukkit.getWorld(section.getString("world"));
        if (world == null) return null;
        return new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
    }

    private Map<String, Object> locationToMap(Location loc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", loc.getWorld().getName());
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("yaw", (double) loc.getYaw());
        map.put("pitch", (double) loc.getPitch());
        return map;
    }

    private Location mapToLocation(Map<?, ?> map) {
        World world = Bukkit.getWorld((String) map.get("world"));
        if (world == null) return null;
        return new Location(world, (Double) map.get("x"), (Double) map.get("y"), (Double) map.get("z"), ((Number) map.get("yaw")).floatValue(), ((Number) map.get("pitch")).floatValue());
    }

    public Dungeon getDungeon(String id) {
        return dungeons.get(id);
    }

    public Collection<Dungeon> getAllDungeons() {
        return dungeons.values();
    }

    public void deleteDungeon(String id) {
        dungeons.remove(id);
        plugin.getConfigManager().getDungeonsConfig().set("dungeons." + id, null);
        plugin.getConfigManager().saveDungeonsConfig();
    }
}