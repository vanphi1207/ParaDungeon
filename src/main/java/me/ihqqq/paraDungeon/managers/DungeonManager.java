package me.ihqqq.paraDungeon.managers;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.logging.Level;

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
                plugin.getLogger().log(Level.SEVERE, "Failed to load dungeon " + dungeonId, e);
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
        if (section.getConfigurationSection("end-point") != null) // Đã thêm
            dungeon.setEndPoint(loadLocation(section.getConfigurationSection("end-point"))); // Đã thêm

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

        // Load rewards
        dungeon.setRewards(loadRewards(section));

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
        dungeonSection.set("entries-per-reset", dungeon.getEntriesPerReset());
        dungeonSection.set("respawn-lives", dungeon.getRespawnLives());

        if (dungeon.getSpawnPoint() != null)
            saveLocation(dungeonSection.createSection("spawn-point"), dungeon.getSpawnPoint());
        if (dungeon.getLobbyPoint() != null)
            saveLocation(dungeonSection.createSection("lobby-point"), dungeon.getLobbyPoint());
        if (dungeon.getEndPoint() != null) // Đã thêm
            saveLocation(dungeonSection.createSection("end-point"), dungeon.getEndPoint()); // Đã thêm

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

        // Save rewards
        saveRewards(dungeonSection, dungeon.getRewards());

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
        Number xNum = (Number) map.get("x");
        Number yNum = (Number) map.get("y");
        Number zNum = (Number) map.get("z");
        Object yawObj = map.get("yaw");
        Object pitchObj = map.get("pitch");
        Number yawNum = (yawObj instanceof Number) ? (Number) yawObj : Double.valueOf(0.0);
        Number pitchNum = (pitchObj instanceof Number) ? (Number) pitchObj : Double.valueOf(0.0);
        return new Location(
                world,
                xNum != null ? xNum.doubleValue() : 0.0,
                yNum != null ? yNum.doubleValue() : 0.0,
                zNum != null ? zNum.doubleValue() : 0.0,
                yawNum.floatValue(),
                pitchNum.floatValue()
        );
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

    /**
     * Save rewards with ItemStack support
     */
    private void saveRewards(ConfigurationSection dungeonSection, DungeonRewards rewards) {
        if (rewards == null) return;

        ConfigurationSection rewardsSection = dungeonSection.createSection("rewards");

        // Completion rewards
        ConfigurationSection completionSection = rewardsSection.createSection("completion");

        // Save ItemStacks as Base64
        if (!rewards.getCompletionRewardItems().isEmpty()) {
            List<String> encodedItems = new ArrayList<>();
            for (ItemStack item : rewards.getCompletionRewardItems()) {
                String encoded = itemStackToBase64(item);
                if (encoded != null) {
                    encodedItems.add(encoded);
                }
            }
            completionSection.set("items-encoded", encodedItems);
        }

        // Legacy string items
        if (!rewards.getCompletionItems().isEmpty()) {
            completionSection.set("items", rewards.getCompletionItems());
        }

        // Commands
        if (!rewards.getCompletionCommands().isEmpty()) {
            completionSection.set("commands", rewards.getCompletionCommands());
        }

        // Score-based rewards
        if (!rewards.getScoreBasedRewards().isEmpty()) {
            ConfigurationSection scoreSection = rewardsSection.createSection("score-based");
            for (Map.Entry<Integer, DungeonRewards.ScoreReward> entry : rewards.getScoreBasedRewards().entrySet()) {
                ConfigurationSection tierSection = scoreSection.createSection(String.valueOf(entry.getKey()));
                DungeonRewards.ScoreReward reward = entry.getValue();

                // Save ItemStacks
                if (!reward.getRewardItems().isEmpty()) {
                    List<String> encodedItems = new ArrayList<>();
                    for (ItemStack item : reward.getRewardItems()) {
                        String encoded = itemStackToBase64(item);
                        if (encoded != null) {
                            encodedItems.add(encoded);
                        }
                    }
                    tierSection.set("items-encoded", encodedItems);
                }

                // Legacy
                if (!reward.getItems().isEmpty()) {
                    tierSection.set("items", reward.getItems());
                }

                if (!reward.getCommands().isEmpty()) {
                    tierSection.set("commands", reward.getCommands());
                }
            }
        }
    }

    /**
     * Load rewards with ItemStack support
     */
    private DungeonRewards loadRewards(ConfigurationSection dungeonSection) {
        ConfigurationSection rewardsSection = dungeonSection.getConfigurationSection("rewards");
        if (rewardsSection == null) return null;

        DungeonRewards rewards = new DungeonRewards();

        // Completion rewards
        ConfigurationSection completionSection = rewardsSection.getConfigurationSection("completion");
        if (completionSection != null) {
            // Load ItemStacks
            if (completionSection.contains("items-encoded")) {
                List<ItemStack> items = new ArrayList<>();
                for (String encoded : completionSection.getStringList("items-encoded")) {
                    ItemStack item = itemStackFromBase64(encoded);
                    if (item != null) {
                        items.add(item);
                    }
                }
                rewards.setCompletionRewardItems(items);
            }

            // Legacy items
            if (completionSection.contains("items")) {
                rewards.setCompletionItems(completionSection.getStringList("items"));
            }

            // Commands
            if (completionSection.contains("commands")) {
                rewards.setCompletionCommands(completionSection.getStringList("commands"));
            }
        }

        // Score-based rewards
        ConfigurationSection scoreSection = rewardsSection.getConfigurationSection("score-based");
        if (scoreSection != null) {
            for (String scoreStr : scoreSection.getKeys(false)) {
                try {
                    int score = Integer.parseInt(scoreStr);
                    ConfigurationSection tierSection = scoreSection.getConfigurationSection(scoreStr);

                    DungeonRewards.ScoreReward scoreReward = new DungeonRewards.ScoreReward();

                    // Load ItemStacks
                    if (tierSection.contains("items-encoded")) {
                        List<ItemStack> items = new ArrayList<>();
                        for (String encoded : tierSection.getStringList("items-encoded")) {
                            ItemStack item = itemStackFromBase64(encoded);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                        scoreReward.setRewardItems(items);
                    }

                    // Legacy
                    if (tierSection.contains("items")) {
                        scoreReward.setItems(tierSection.getStringList("items"));
                    }

                    if (tierSection.contains("commands")) {
                        scoreReward.setCommands(tierSection.getStringList("commands"));
                    }

                    rewards.addScoreReward(score, scoreReward);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid score tier: " + scoreStr);
                }
            }
        }

        return rewards;
    }

    /**
     * Convert ItemStack to Base64 string
     */
    private String itemStackToBase64(ItemStack item) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to encode ItemStack: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert Base64 string to ItemStack
     */
    private ItemStack itemStackFromBase64(String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to decode ItemStack: " + e.getMessage());
            return null;
        }
    }
}