package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener để xử lý nhập command trong chat cho reward system
 */
public class CommandInputListener implements Listener {

    private final ParaDungeon plugin;

    public CommandInputListener(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Kiểm tra xem player có đang chờ nhập command không
        if (!plugin.getGUIManager().getCommandRewardGUI().hasPendingInput(player.getUniqueId())) {
            return;
        }

        // Cancel chat event
        event.setCancelled(true);

        String message = event.getMessage();

        // Xử lý input trên main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getGUIManager().getCommandRewardGUI().handleChatInput(player, message);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup pending input khi player quit
        plugin.getGUIManager().getCommandRewardGUI().cancelPendingInput(event.getPlayer().getUniqueId());
    }
}