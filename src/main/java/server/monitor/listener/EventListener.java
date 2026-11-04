package server.monitor.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import server.monitor.model.PlayerManager;

public class EventListener implements org.bukkit.event.Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager.addPlayer(player.getUniqueId(),
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
        PlayerManager.updatePosition(player.getUniqueId(),player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerManager.playerDeath(event.getPlayer().getUniqueId());
        PlayerManager.playerKill(event.getPlayer());
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        PlayerManager.updateInventory(event.getPlayer().getUniqueId(),event.getPlayer().getInventory());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerManager.removePlayer(event.getPlayer().getUniqueId());
    }
}
