package me.ihqqq.paraDungeon.models;

import org.bukkit.inventory.ItemStack;
import java.util.*;

public class DungeonRewards {

    private List<String> completionItems; // Legacy support
    private List<ItemStack> completionRewardItems; // New: actual ItemStacks
    private List<String> completionCommands;
    private Map<Integer, ScoreReward> scoreBasedRewards;

    public DungeonRewards() {
        this.completionItems = new ArrayList<>();
        this.completionRewardItems = new ArrayList<>();
        this.completionCommands = new ArrayList<>();
        this.scoreBasedRewards = new TreeMap<>(Collections.reverseOrder());
    }

    // Legacy methods
    public List<String> getCompletionItems() {
        return completionItems;
    }

    public void setCompletionItems(List<String> items) {
        this.completionItems = items;
    }

    // New ItemStack methods
    public List<ItemStack> getCompletionRewardItems() {
        return completionRewardItems;
    }

    public void setCompletionRewardItems(List<ItemStack> items) {
        this.completionRewardItems = items;
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
        private List<String> items; // Legacy
        private List<ItemStack> rewardItems; // New
        private List<String> commands;

        public ScoreReward() {
            this.items = new ArrayList<>();
            this.rewardItems = new ArrayList<>();
            this.commands = new ArrayList<>();
        }

        // Legacy
        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }

        // New ItemStack methods
        public List<ItemStack> getRewardItems() {
            return rewardItems;
        }

        public void setRewardItems(List<ItemStack> items) {
            this.rewardItems = items;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }
    }
}