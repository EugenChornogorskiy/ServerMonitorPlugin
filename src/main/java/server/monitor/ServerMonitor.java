package server.monitor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.listener.EventListener;
import server.monitor.model.PlayerManager;
import server.monitor.service.Sender;

public class ServerMonitor extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Sender sender = new Sender("http://127.0.0.1:5000","mykey");
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            sender.send("/api", PlayerManager.getPlayers());
        }, 200L, 200L);
    }
}
