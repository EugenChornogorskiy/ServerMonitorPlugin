package server.monitor.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.logging.Logger;
import server.monitor.model.PlayerManager;
import server.monitor.service.Sender;

import java.util.ArrayList;
import java.util.List;


public class CommandManager {
    private final Sender sender;
    private final JavaPlugin plugin;
    private final Logger logger;
    private final PlayerManager playerManager;
    public CommandManager(Sender sender, JavaPlugin plugin,Logger logger,PlayerManager playerManager) {
        this.sender = sender;
        this.plugin = plugin;
        this.logger = logger;
        this.playerManager = playerManager;
    }

    public void handleCommand(String command, Player player) {
        String[] words = command.split(" ");
        if(words.length != 2) {
            player.sendMessage("Invalid arguments amount");
        }
        if(words[0].equals("/authorize")) {
            authorize(player,words[1]);
        }
        if(words[0].equals("/get")) {
            getPurchase(player,words[1]);
        }
    }
    public void authorize(Player player, String code) {
        sender.authorizeAccount(player.getUniqueId(), code).thenAccept(response ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    switch (response.status()){
                        case OK -> player.sendMessage(Component.text("Account authorized", NamedTextColor.GREEN));
                        case NOT_FOUND -> player.sendMessage(Component.text("Player not found", NamedTextColor.RED));
                        case CONFLICT -> player.sendMessage(Component.text("Player already authorized", NamedTextColor.RED));
                        case UNAVAILABLE -> player.sendMessage(Component.text("Server unavailable", NamedTextColor.RED));
                        case SERVER_ERROR -> player.sendMessage(Component.text("Server error", NamedTextColor.RED));
                    }
                })
        );
    }
    public void getPurchase(Player player, String purchase) {
        sender.getPurchase(player.getUniqueId(), purchase).thenAccept(response ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    List<Sender.PurchaseItem> purchaseItems= response.data().items();
                    Long id = response.data().id();
                    switch (response.status()){
                        case OK -> {
                            List<ItemStack> items = new ArrayList<>();
                                try{
                                    for (Sender.PurchaseItem item : purchaseItems) {
                                        ItemStack itemStack = new ItemStack(Material.valueOf(item.material()), item.amount());
                                        items.add(itemStack);
                                    }
                                    for(ItemStack itemStack : items) {
                                        player.getInventory().addItem(itemStack);
                                        logger.log("INFO", "CommandManager.getPuchase", "Give " + itemStack.getType() + " to " + player.getName());
                                    }
                                    playerManager.updateInventory(player.getUniqueId(), player.getInventory());
                                    player.sendMessage(Component.text("Thank you for buying", NamedTextColor.GREEN));
                                    sender.closePurchase(id, Sender.ClosePurchase.Status.SUCCESS);
                                } catch (IllegalArgumentException e) {
                                    player.sendMessage(Component.text("Error with: " + purchase + " contact admin", NamedTextColor.RED));
                                    logger.log("ERROR", "CommandManager.getPuchase","Unknown item material " + purchaseItems);
                                    sender.closePurchase(id, Sender.ClosePurchase.Status.FAILURE);
                                }
                        }
                        case NOT_FOUND -> {
                            player.sendMessage(Component.text("You don't have " + purchase + " in your purchases", NamedTextColor.RED));
                            sender.closePurchase(id, Sender.ClosePurchase.Status.FAILURE);
                        }
                        case CONFLICT -> {
                            player.sendMessage(Component.text(purchase + " is already in progress", NamedTextColor.RED));
                            sender.closePurchase(id, Sender.ClosePurchase.Status.FAILURE);
                        }
                        case UNAVAILABLE -> {
                            player.sendMessage(Component.text("Server unavailable", NamedTextColor.RED));
                            sender.closePurchase(id, Sender.ClosePurchase.Status.FAILURE);
                        }
                        case SERVER_ERROR -> {
                            player.sendMessage(Component.text("Server error", NamedTextColor.RED));
                            sender.closePurchase(id, Sender.ClosePurchase.Status.FAILURE);
                        }
                    }
                })
        );
    }


}
