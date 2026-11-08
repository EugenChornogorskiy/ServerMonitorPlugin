package server.monitor.service;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import server.monitor.model.PlayerManager;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class TaskManager {
    private final PlayerManager playerManager;
    public TaskManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    public void handleCommand(Sender sender, List<Task> tasks) {
        for (Task task : tasks) {
                try {
                    switch (task.type()) {
                        case KICK -> kick(task.player_id());
                        case BAN  -> ban(task.player_id());
                        case GIVE -> give(task.player_id(), task.arguments().get("itemName"), Integer.parseInt(task.arguments().get("itemAmount")));
                    }
                    sender.sendCommandStatus(task.id(),true);
                } catch (NullPointerException e) {
                    sender.sendCommandStatus(task.id(),false);
                }
        }
    }
    private static void kick(UUID id) throws NullPointerException {
        Bukkit.getPlayer(id).kick();
    }
    private static void ban(UUID id) throws NullPointerException {
        Bukkit.getPlayer(id).ban("You have banned", Duration.ofHours(2),"Admin");
    }
    private static void give(UUID id,String name, int amount) throws NullPointerException {
        org.bukkit.entity.Player player = Bukkit.getPlayer(id);
        if (player == null) {
            throw new NullPointerException();
        }
        player.give( getItem(name,amount));
    }
    public static ItemStack getItem(String name, int amount) throws NullPointerException {
        Material material = Material.getMaterial(name);
        ItemStack itemStack = new ItemStack(material,amount);
        return itemStack;
    }
}
