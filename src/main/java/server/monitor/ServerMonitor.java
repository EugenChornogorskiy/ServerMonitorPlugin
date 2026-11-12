package server.monitor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.command.CommandManager;
import server.monitor.listener.EventListener;
import server.monitor.logging.Logger;
import server.monitor.model.PlayerManager;
import server.monitor.service.TaskManager;
import server.monitor.service.Sender;

public class ServerMonitor extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        PlayerManager playerManager = new PlayerManager();
        Logger logger = new Logger(getConfig().getString("log-file"),this);
        TaskManager taskManager = new TaskManager(playerManager);
        Sender sender = new Sender(getConfig().getString("url"),getConfig().getString("api-key"), taskManager, this,logger);
        CommandManager commandManager = new CommandManager(sender,this);
        getServer().getPluginManager().registerEvents(new EventListener(playerManager,taskManager,commandManager), this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            sender.send("/api/set/players", playerManager.getPlayers());
        }, 200L, 200L);
    }
}
