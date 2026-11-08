package server.monitor.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();

    public void addPlayer(UUID id, String name, double x, double y, double z,
                                 PlayerInventory playerInventory) {
        addPlayer(id, new Player(id,name, new Player.Position(x, y, z), inventory(playerInventory)));
    }
    public void removePlayer(UUID id) {
        players.remove(id);
    }
    public void updateInventory(UUID id,PlayerInventory playerInventory) {
        if (isPlayerOnMap(id)) {
            players.get(id).setInventory(inventory(playerInventory));
        }
    }
    public void updatePosition(UUID id,double x, double y, double z) {
        if (isPlayerOnMap(id)) {
            players.get(id).setPosition(new Player.Position(x,y,z));
        }
    }
    public void playerKill(org.bukkit.entity.Player player) {
        if (player.getKiller() != null){
            if (isPlayerOnMap(player.getKiller().getUniqueId())) {
                players.get(player.getKiller().getUniqueId()).kill();
            }
        }
    }
    public void playerDeath(UUID id) {
        if (isPlayerOnMap(id)) {
            players.get(id).death();
        }
    }
    public Player getPlayer(UUID id) throws NullPointerException {
            return players.get(id);
    }
    public Map<UUID, Player> getPlayers() {
        return Map.copyOf(players);
    }
    public List<Player.PlayerItem> inventory(PlayerInventory playerInventory) {
        ItemStack[] items = playerInventory.getContents();
        List<Player.PlayerItem> playerItems = Collections.synchronizedList(new ArrayList<>());
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack item = items[slot];
            if (item != null && !item.getType().isAir()) {
                playerItems.add(new Player.PlayerItem(slot,item.getType().name(),item.getAmount()));
            }
        }
        return playerItems;
    }

    public boolean isPlayerOnMap(UUID id) {
        return players.get(id) != null;
    }
    public void addPlayer(UUID id, Player stats) {
        players.put(id, stats);
    }
    public void clear() {
        players.clear();
    }

}
