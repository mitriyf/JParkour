package ru.mitriyf.jparkour.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.cmd.admin.AdminEditor;
import ru.mitriyf.jparkour.cmd.item.ItemEditor;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;

import java.util.List;
import java.util.Map;

public class CJParkour implements CommandExecutor {
    private final AdminEditor adminEditor;
    private final ItemEditor itemEditor;
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;
    private final Manager manager;

    public CJParkour(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
        this.manager = plugin.getManager();
        this.itemEditor = new ItemEditor(plugin);
        this.adminEditor = new AdminEditor(plugin);
    }

    private void sendMessage(CommandSender sender, Map<String, List<Action>> message) {
        utils.sendMessage(sender, message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("jparkour.help") || args.length == 0 || args.length >= 6 || args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("jparkour.help")) {
                sendMessage(sender, values.getHelp());
            } else {
                sendMessage(sender, values.getNoperm());
            }
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "join": {
                join(sender, args);
                return false;
            }
            case "exit": {
                exit(sender);
                return false;
            }
            case "item": {
                itemEditor.checkItemCommand(sender, args);
                return false;
            }
            case "admin": {
                adminEditor.checkAdminCommand(sender, args);
                return false;
            }
            case "status": {
                status(sender);
                return false;
            }
            case "reload": {
                reload(sender);
                return false;
            }
            default: {
                sendMessage(sender, values.getHelp());
            }
        }
        return false;
    }

    private void join(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("jparkour.join")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        Player p = (Player) sender;
        if (args.length == 2) {
            manager.join(p, args[1].toLowerCase());
            return;
        }
        manager.join(p, null);
    }

    private void exit(CommandSender sender) {
        if (!(sender instanceof Player) || !sender.hasPermission("jparkour.join")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        Player p = (Player) sender;
        if (manager.getTasks().get(p.getUniqueId()) != null) {
            manager.getTasks().get(p.getUniqueId()).cancel();
            manager.getTasks().remove(p.getUniqueId());
            manager.getWaiters().remove(p.getUniqueId());
            utils.sendMessage(p, values.getExit());
        } else if (manager.getPlayers().get(p.getUniqueId()) != null) {
            String game = manager.getPlayers().get(p.getUniqueId()).getGame();
            values.getRooms().get(game).close(true, false);
        } else {
            utils.sendMessage(p, values.getNoExit());
        }
    }

    private void status(CommandSender sender) {
        if (!sender.hasPermission("jparkour.status")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        sender.sendMessage("§aPlugin status:\n\n");
        sender.sendMessage("§fRooms: §a" + values.getRooms().size());
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("jparkour.reload")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, values::setup);
        sender.sendMessage("§aSuccessfully!");
    }
}
