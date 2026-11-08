package server.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import server.monitor.model.PlayerManager;
import server.monitor.service.Sender;
import server.monitor.service.Task;
import server.monitor.service.TaskManager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

class TaskManagerTest {
    private final TaskManager taskManager = new TaskManager(new PlayerManager());
    private final Sender sender = mock(Sender.class);
    private final UUID playerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private Task task(Task.Type type, Map<String, String> args) {
        return new Task(type, taskId, playerId, args);
    }

    @Test
    void kickOnlinePlayerReportsSuccess() {
        Player player = mock(Player.class);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(player);

            taskManager.handleCommand(sender,
                    List.of(new Task(Task.Type.KICK, taskId, playerId, Map.of())));

            verify(player).kick();
            verify(sender).sendCommandStatus(taskId, true);
        }
    }

    @Test
    void banOnlinePlayerReportsSuccess() {
        Player player = mock(Player.class);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(player);

            taskManager.handleCommand(sender,
                    List.of(new Task(Task.Type.BAN, taskId, playerId, Map.of())));

            verify(player).ban("You have banned", Duration.ofHours(2),"Admin");
            verify(sender).sendCommandStatus(taskId, true);
        }
    }
    @Test
    void kickOfflinePlayerReportsFailure() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(null);

            taskManager.handleCommand(sender,
                    List.of(new Task(Task.Type.KICK, taskId, playerId, Map.of())));

            verify(sender).sendCommandStatus(taskId, false);
        }
    }
    @Test
    void banOfflinePlayerReportsFailure() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(null);

            taskManager.handleCommand(sender,
                    List.of(new Task(Task.Type.BAN, taskId, playerId, Map.of())));

            verify(sender).sendCommandStatus(taskId, false);
        }
    }
    @Test
    void giveOfflinePlayerReportsFailure() {

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(null);

            taskManager.handleCommand(sender,
                    List.of(new Task(Task.Type.GIVE, taskId, playerId, Map.of("itemName","DIAMOND_SWORD","itemAmount","1"))));

            verify(sender).sendCommandStatus(taskId, false);
        }
    }
}
