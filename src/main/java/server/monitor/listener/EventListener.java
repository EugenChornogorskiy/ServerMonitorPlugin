package server.monitor.listener;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import server.monitor.model.PlayerManager;
import server.monitor.service.TaskManager;

public class EventListener implements org.bukkit.event.Listener {
    private final PlayerManager playerManager;
    private final TaskManager taskManager;
    public EventListener(PlayerManager playerManager, TaskManager taskManager) {
        this.playerManager = playerManager;
        this.taskManager = taskManager;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerManager.addPlayer(player.getUniqueId(),
                player.getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getInventory()
        );
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        playerManager.updatePosition(player.getUniqueId(),player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        playerManager.playerDeath(event.getPlayer().getUniqueId());
        playerManager.playerKill(event.getPlayer());
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        playerManager.updateInventory(event.getPlayer().getUniqueId(),event.getPlayer().getInventory());
    }
    @EventHandler
    public void onPlayerPickItemEvent(PlayerPickItemEvent event) {
        playerManager.updateInventory(event.getPlayer().getUniqueId(),event.getPlayer().getInventory());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerManager.removePlayer(event.getPlayer().getUniqueId());
    }
}
