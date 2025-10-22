package me.ihqqq.paraDungeon.models;

import java.util.*;

public class DungeonRewards {
    
    private List<String> completionItems;
    private List<String> completionCommands;
    private Map<Integer, ScoreReward> scoreBasedRewards;
    
    public DungeonRewards() {
        this.completionItems = new ArrayList<>();
        this.completionCommands = new ArrayList<>();
        this.scoreBasedRewards = new TreeMap<>(Collections.reverseOrder());
    }
    
    public List<String> getCompletionItems() {
        return completionItems;
    }
    
    public void setCompletionItems(List<String> items) {
        this.completionItems = items;
    }
    
    public List<String> getCompletionCommands() {
        return completionCommands;
    }
    
    public void setCompletionCommands(List<String> commands) {
        this.completionCommands = commands;
    }
    
    public Map<Integer, ScoreReward> getScoreBasedRewards() {
        return scoreBasedRewards;
    }
    
    public void addScoreReward(int score, ScoreReward reward) {
        this.scoreBasedRewards.put(score, reward);
    }
    
    public ScoreReward getRewardForScore(int score) {
        for (Map.Entry<Integer, ScoreReward> entry : scoreBasedRewards.entrySet()) {
            if (score >= entry.getKey()) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public static class ScoreReward {
        private List<String> items;
        private List<String> commands;
        
        public ScoreReward() {
            this.items = new ArrayList<>();
            this.commands = new ArrayList<>();
        }
        
        public List<String> getItems() {
            return items;
        }
        
        public void setItems(List<String> items) {
            this.items = items;
        }
        
        public List<String> getCommands() {
            return commands;
        }
        
        public void setCommands(List<String> commands) {
            this.commands = commands;
        }
    }
}
