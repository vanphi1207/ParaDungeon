package me.ihqqq.paraDungeon.models;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class Wave {

    private final int waveNumber;
    private final List<MobSpawn> mobs;
    private final List<Location> spawnLocations;

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.mobs = new ArrayList<>();
        this.spawnLocations = new ArrayList<>();
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public List<MobSpawn> getMobs() {
        return mobs;
    }

    public void addMob(String mobId, int amount) {
        mobs.add(new MobSpawn(mobId, amount));
    }

    public int getTotalMobs() {
        return mobs.stream().mapToInt(MobSpawn::getAmount).sum();
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }

    public void addSpawnLocation(Location location) {
        this.spawnLocations.add(location);
    }

    public static class MobSpawn {
        private final String mobId;
        private final int amount;

        public MobSpawn(String mobId, int amount) {
            this.mobId = mobId;
            this.amount = amount;
        }

        public String getMobId() {
            return mobId;
        }

        public int getAmount() {
            return amount;
        }
    }
}