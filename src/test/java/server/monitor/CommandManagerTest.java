package server.monitor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import server.monitor.command.CommandManager;
import server.monitor.model.PlayerManager;
import server.monitor.service.Sender;
import server.monitor.service.Task;
import server.monitor.service.TaskManager;

import javax.annotation.processing.Completion;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class CommandManagerTest {
    private final JavaPlugin plugin = mock(JavaPlugin.class);
    private final Sender sender = mock(Sender.class);
    private final CommandManager commandManager = new CommandManager(sender,plugin);
    private final UUID playerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    @Test
    void authorizeCommandHandle() {
        String message = "/authorize 1111";
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(sender.authorizeAccount(playerId, "1111"))
                .thenReturn(CompletableFuture.completedFuture(true));

        BukkitScheduler scheduler = mock(BukkitScheduler.class);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            commandManager.handleCommand(message, player);

            ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).runTask(eq(plugin), captor.capture());

            captor.getValue().run();

            verify(player).sendMessage(Component.text("Account authorized", NamedTextColor.GREEN));
        }
    }
    @Test
    void authorizeCommandHandleFailure() {
        String message = "/authorize 1111";
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(sender.authorizeAccount(playerId, "1111"))
                .thenReturn(CompletableFuture.completedFuture(false));

        BukkitScheduler scheduler = mock(BukkitScheduler.class);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            commandManager.handleCommand(message, player);

            ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).runTask(eq(plugin), captor.capture());

            captor.getValue().run();

            verify(player).sendMessage(Component.text("Failed to authorize your account", NamedTextColor.RED));
        }
    }

    @Test
    void authorizeCommandHandleInvalidAmountArguments() {
        String message = "/authorize 1111 1313";
        Player player = mock (Player.class);
        commandManager.handleCommand(message,player);
        verify(player).sendMessage("Invalid arguments amount");
    }
}
