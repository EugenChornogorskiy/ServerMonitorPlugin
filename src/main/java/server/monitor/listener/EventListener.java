package server.monitor.listener;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import server.monitor.command.CommandManager;
import server.monitor.logging.Logger;
import server.monitor.model.PlayerManager;
import server.monitor.service.TaskManager;

import java.util.Set;

public class EventListener implements org.bukkit.event.Listener {
    private final PlayerManager playerManager;
    private final TaskManager taskManager;
    private final CommandManager commandManager;
    public EventListener(PlayerManager playerManager, TaskManager taskManager,CommandManager commandManager) {
        this.playerManager = playerManager;
        this.taskManager = taskManager;
        this.commandManager = commandManager;
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
        Player player = event.getPlayer();
        playerManager.playerDeath(player.getUniqueId());
        playerManager.playerKill(player);
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
    @EventHandler
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String command = message.substring(1).split(" ")[0].toLowerCase();
        Set<String> WATCHED = Set.of("authorize","give");
        if (WATCHED.contains(command)) {
            commandManager.handleCommand(message,event.getPlayer());
        }
    }
}
