package server.monitor.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();

    public static void addPlayer(UUID id, String name, double x, double y, double z,
                                 PlayerInventory playerInventory) {
        addPlayer(id, new Player(id,name, new Player.Position(x, y, z), inventory(playerInventory)));
    }
    public static void removePlayer(UUID id) {
        players.remove(id);
    }
    public static void updateInventory(UUID id,PlayerInventory playerInventory) {
        if (isPlayerOnMap(id)) {
            players.get(id).setInventory(inventory(playerInventory));
        }
    }
    public static void updatePosition(UUID id,double x, double y, double z) {
        if (isPlayerOnMap(id)) {
        players.get(id).setPosition(new Player.Position(x,y,z));
        }
    }
    public static void playerKill(org.bukkit.entity.Player player) {
        if (player.getKiller() != null){
            if (isPlayerOnMap(player.getKiller().getUniqueId())) {
                players.get(player.getKiller().getUniqueId()).kill();
            }
        }
    }
    public static void playerDeath(UUID id) {
        if (isPlayerOnMap(id)) {
            players.get(id).death();
        }
    }
    public static void kickPlayer(UUID id,String reason) {
        if (isPlayerOnMap(id)) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(id);
            if (player != null){
                player.kick(Component.text(reason));
            }
        }
    }
    public static void banPlayer(UUID id, String reason, Duration time) {
        if (isPlayerOnMap(id)) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(id);
            if (player != null){
                player.ban(reason,time,"Admin");
            }

        }
    }
    public static void banIpPlayer(UUID id, String reason, Duration time) {
        if (isPlayerOnMap(id)) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.banIp(reason, time, "Admin", true);
            }
        }
    }
    public static Player getPlayer(UUID id) {
        return players.get(id);
    }
    public static Map<UUID, Player> getPlayers() {
        return Map.copyOf(players);
    }
    public static List<Player.PlayerItem> inventory(PlayerInventory playerInventory) {
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

    public static boolean isPlayerOnMap(UUID id) {
        return players.get(id) != null;
    }
    public static void addPlayer(UUID id, Player stats) {
        players.put(id, stats);
    }
    public static void clear() {
        players.clear();
    }

}
