package server.monitor;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import server.monitor.command.CommandManager;
import server.monitor.listener.EventListener;
import server.monitor.logging.Logger;
import server.monitor.model.PlayerManager;
import server.monitor.service.Sender;
import server.monitor.service.Task;
import server.monitor.service.TaskManager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

public class EventListenerTest {
    private final PlayerManager playerManager = mock(PlayerManager.class);
    private final TaskManager taskManager = mock(TaskManager.class);
    private final CommandManager commandManager = mock(CommandManager.class);
    private final EventListener eventListener = new EventListener(playerManager, taskManager,commandManager);
    private final UUID playerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private Task task(Task.Type type, Map<String, String> args) {
        return new Task(type, taskId, playerId, args);
    }

    @Test
    void eventPlayerJoin() {
        PlayerJoinEvent pje = mock(PlayerJoinEvent.class);
        Player player = mock(Player.class);
        PlayerInventory playerInventory = mock(PlayerInventory.class);
        when(pje.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Riven");
        Location playerLocation = mock(Location.class);
        when(player.getLocation()).thenReturn(playerLocation);
        when(playerLocation.getX()).thenReturn(0d);
        when(playerLocation.getY()).thenReturn(0d);
        when(playerLocation.getZ()).thenReturn(0d);
        when(player.getInventory()).thenReturn(playerInventory);
        eventListener.onPlayerJoin(pje);
        verify(playerManager).addPlayer(playerId, "Riven",0d,0d,0d,playerInventory);

    }
    @Test
    void eventPlayerQuit() {
        PlayerQuitEvent pqe = mock(PlayerQuitEvent.class);
        Player player = mock(Player.class);
        when(pqe.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        eventListener.onPlayerQuit(pqe);
        verify(playerManager).removePlayer(playerId);
    }
    @Test
    void playerPickItemEvent() {
        PlayerPickItemEvent ppie = mock(PlayerPickItemEvent.class);
        Player player = mock(Player.class);
        PlayerInventory playerInventory = mock(PlayerInventory.class);
        when(ppie.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getInventory()).thenReturn(playerInventory);
        eventListener.onPlayerPickItemEvent(ppie);
        verify(playerManager).updateInventory(playerId,playerInventory);
    }
    @Test
    void playerDropItemEvent() {
        PlayerDropItemEvent pdie = mock(PlayerDropItemEvent.class);
        Player player = mock(Player.class);
        PlayerInventory playerInventory = mock(PlayerInventory.class);
        when(pdie.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getInventory()).thenReturn(playerInventory);
        eventListener.onPlayerDropItem(pdie);
        verify(playerManager).updateInventory(playerId,playerInventory);
    }
    @Test
    void playerDeathEvent() {
        PlayerDeathEvent pde = mock(PlayerDeathEvent.class);
        Player player = mock(Player.class);
        when(pde.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        eventListener.onPlayerDeath(pde);
        verify(playerManager).playerDeath(playerId);
    }
    @Test
    void playerMoveEvent() {
        PlayerMoveEvent pme = mock(PlayerMoveEvent.class);
        Player player = mock(Player.class);
        when(pme.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        Location playerLocation = mock(Location.class);
        when(player.getLocation()).thenReturn(playerLocation);
        when(playerLocation.getX()).thenReturn(0d);
        when(playerLocation.getY()).thenReturn(0d);
        when(playerLocation.getZ()).thenReturn(0d);
        eventListener.onPlayerMove(pme);
        verify(playerManager).updatePosition(playerId,0d,0d,0d);
    }
    @Test
    void playerCommandPreprocess() {
        PlayerCommandPreprocessEvent pcpe = mock(PlayerCommandPreprocessEvent.class);
        Player player = mock(Player.class);
        when(pcpe.getPlayer()).thenReturn(player);
        when(pcpe.getMessage()).thenReturn("/authorize 1111");
        eventListener.onPlayerCommandPreprocess(pcpe);
        verify(commandManager).handleCommand("/authorize 1111",player);
    }
}
