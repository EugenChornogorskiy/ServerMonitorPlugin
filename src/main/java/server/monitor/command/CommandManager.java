package server.monitor.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.service.Sender;


public class CommandManager {
    private final Sender sender;
    private final JavaPlugin plugin;
    public CommandManager(Sender sender, JavaPlugin plugin) {
        this.sender = sender;
        this.plugin = plugin;
    }

    public void handleCommand(String command, Player player) {
        String[] words = command.split(" ");
        if(words.length != 2) {
            player.sendMessage("Invalid arguments amount");
        }
        if(words[0].equals("/authorize")) {
            authorize(player,words[1]);
        }

    }
    public void authorize(Player player, String code) {
        sender.authorizeAccount(player.getUniqueId(), code).thenAccept(ok ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (ok) player.sendMessage(Component.text("Account authorized", NamedTextColor.GREEN));
                    else player.sendMessage(Component.text("Failed to authorize your account", NamedTextColor.RED));
                })
        );
    }
    public void getPurchase(Player player, String purchase) {
        sender.getPurchase(player.getUniqueId(), purchase).thenAccept(ok ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (ok) player.sendMessage(Component.text("Thank you for buying", NamedTextColor.GREEN));
                    else player.sendMessage(Component.text("You don't have " + purchase + " in your purchases", NamedTextColor.RED));
                })
        );
    }
}
