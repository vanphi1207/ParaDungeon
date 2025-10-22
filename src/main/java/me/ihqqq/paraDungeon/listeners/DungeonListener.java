package me.ihqqq.paraDungeon.listeners;

import me.ihqqq.paraDungeon.ParaDungeon;
import me.ihqqq.paraDungeon.models.DungeonRoom;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.Set;

public class DungeonListener implements Listener {

    private final ParaDungeon plugin;

    public DungeonListener(ParaDungeon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Vòng lặp trực tiếp qua các phòng dungeon đang có để kiểm tra.
        // Cách này hiệu quả và an toàn hơn nhiều so với việc lặp qua tất cả người chơi.
        for (DungeonRoom room : plugin.getRoomManager().getAllRooms()) {
            // Chỉ xử lý nếu phòng đang hoạt động và thực thể chết thuộc về phòng đó.
            if (room.getStatus() == DungeonRoom.RoomStatus.ACTIVE && room.getActiveEntities().contains(entity)) {
                plugin.getRoomManager().onMobKilled(room, entity.getKiller());
                // Khi đã tìm thấy phòng chứa thực thể, thoát khỏi vòng lặp để tối ưu.
                break;
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            DungeonRoom room = plugin.getRoomManager().getPlayerRoom(player.getUniqueId());

            if (room != null && room.isInSafeMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}