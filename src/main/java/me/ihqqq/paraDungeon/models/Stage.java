package me.ihqqq.paraDungeon.models;

import java.util.*;

public class Stage {
    
    private final int stageNumber;
    private final StageType type;
    private final Map<Integer, Wave> waves;
    
    public Stage(int stageNumber, StageType type) {
        this.stageNumber = stageNumber;
        this.type = type;
        this.waves = new HashMap<>();
    }
    
    public int getStageNumber() {
        return stageNumber;
    }
    
    public StageType getType() {
        return type;
    }
    
    public Map<Integer, Wave> getWaves() {
        return waves;
    }
    
    public void addWave(int waveNum, Wave wave) {
        this.waves.put(waveNum, wave);
    }
    
    public Wave getWave(int waveNum) {
        return waves.get(waveNum);
    }
    
    public int getTotalWaves() {
        return waves.size();
    }
    
    public boolean isBoss() {
        return type == StageType.BOSS;
    }
    
    public enum StageType {
        NORMAL,
        BOSS
    }
}
