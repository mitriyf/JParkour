package ru.mitriyf.jparkour.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.manager.Manager;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.utils.Utils;

import java.util.List;

public class CJParkour implements CommandExecutor {
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;
    private final Manager manager;
    public CJParkour(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
        this.manager = plugin.getManager();
    }
    private void sendMessage(CommandSender sender, String message) {
        utils.sendMessage(sender, message);
    }
    private void sendMessage(CommandSender sender, List<String> message) {
        for (String msg : message) utils.sendMessage(sender, msg);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0 || args.length >= 3 || args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("jparkour.help")) sendMessage(sender, values.getHelp());
            else sendMessage(sender, values.getNoperm());
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "join": {
                if (!(sender instanceof Player)) {
                    sendMessage(sender, "&cYou console!");
                    return false;
                }
                Player p = (Player) sender;
                if (args.length == 2) {
                    manager.join(p, args[1].toLowerCase());
                    return false;
                }
                manager.join(p, null);
                return false;
            }
            case "exit": {
                if (!(sender instanceof Player)) {
                    sendMessage(sender, "&cYou console!");
                    return false;
                }
                Player p = (Player) sender;
                if (manager.getTasks().get(p.getUniqueId()) != null) {
                    manager.getTasks().get(p.getUniqueId()).cancel();
                    manager.getTasks().remove(p.getUniqueId());
                    manager.getWaiters().remove(p.getUniqueId());
                    utils.sendMessage(p, values.getExit());
                } else if (values.getPlayers().get(p.getUniqueId()) != null) {
                    String game = values.getPlayers().get(p.getUniqueId()).getGame();
                    values.getRooms().get(game).close();
                } else {
                    utils.sendMessage(p, values.getNoExit());
                }
                return false;
            }
            case "status": {
                if (!sender.hasPermission("jparkour.status")) {
                    sendMessage(sender, values.getNoperm());
                    return false;
                }
                sendMessage(sender, "&aСтатус плагина:\n\n&fКомнат: &a" + values.getRooms().size() + "\nСостояние: &aАктивный\n");
                return false;
            }
            case "reload": {
                if (!sender.hasPermission("jparkour.reload")) {
                    sendMessage(sender, values.getNoperm());
                    return false;
                }
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                values.setup();
                sendMessage(sender, "Успешно!");
            }
        }
        return false;
    }
}
