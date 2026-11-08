package server.monitor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.listener.EventListener;
import server.monitor.model.PlayerManager;
import server.monitor.service.TaskManager;
import server.monitor.service.Sender;

public class ServerMonitor extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        PlayerManager playerManager = new PlayerManager();
        TaskManager taskManager = new TaskManager(playerManager);
        Sender sender = new Sender("http://localhost:3001","mykey", taskManager, this);
        getServer().getPluginManager().registerEvents(new EventListener(playerManager,taskManager), this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            sender.send("/api/set/players", playerManager.getPlayers());
        }, 200L, 200L);
    }
}
