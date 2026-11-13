package server.monitor.service;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import server.monitor.model.PlayerManager;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class TaskManager {
    private final PlayerManager playerManager;
    public TaskManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    public void handleCommand(Sender sender, Map<UUID,Task> tasks) {
        for (Map.Entry<UUID,Task> task : tasks.entrySet()) {
            Task value = task.getValue();
                try {
                    switch (task.getValue().type()) {
                        case KICK -> kick(value.player_id());
                        case BAN  -> ban(value.player_id());
                        case GIVE -> give(value.player_id(), value.arguments().get("itemName"), Integer.parseInt(value.arguments().get("itemAmount")));
                    }
                    sender.sendCommandStatus(value.id(),true);
                } catch (IllegalStateException e) {
                    sender.sendCommandStatus(value.id(),false);
                }
        }
    }
    private void kick(UUID id) throws IllegalStateException {
        org.bukkit.entity.Player player = Bukkit.getPlayer(id);
        if (player == null) {
            throw new IllegalStateException("Player is offline: " + id);
        }
        player.kick();
    }
    private void ban(UUID id) throws IllegalStateException {
        org.bukkit.entity.Player player = Bukkit.getPlayer(id);
        if (player == null) {
            throw new IllegalStateException("Player is offline: " + id);
        }
        player.ban("You have banned", Duration.ofHours(2),"Admin");
    }
    private void give(UUID id,String name, int amount) throws IllegalStateException {
        org.bukkit.entity.Player player = Bukkit.getPlayer(id);
        if (player == null) {
            throw new IllegalStateException("Player is offline: " + id);
        }
        player.getInventory().addItem( getItem(name,amount));
    }
    public ItemStack getItem(String name, int amount) throws NullPointerException {
        Material material = Material.getMaterial(name);
        return new ItemStack(material,amount);
    }
}
