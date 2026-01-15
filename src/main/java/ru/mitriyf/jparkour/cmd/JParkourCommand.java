package ru.mitriyf.jparkour.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.cmd.admin.AdminEditor;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JParkourCommand implements CommandExecutor {
    private final BukkitScheduler scheduler;
    private final AdminEditor adminEditor;
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;
    private final Manager manager;

    public JParkourCommand(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
        manager = plugin.getManager();
        adminEditor = new AdminEditor(plugin);
        scheduler = plugin.getServer().getScheduler();
    }

    private void sendMessage(CommandSender sender, Map<String, List<Action>> message) {
        utils.sendMessage(sender, message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        boolean permission = sender.hasPermission("jparkour.help");
        if (!permission || args.length == 0 || args.length >= 13 || args[0].equalsIgnoreCase("help")) {
            if (permission) {
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
            manager.join(p, args[1].toLowerCase(), false);
            return;
        }
        manager.join(p, null, false);
    }

    private void exit(CommandSender sender) {
        if (!(sender instanceof Player) || !sender.hasPermission("jparkour.join")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();
        BukkitTask task = manager.getTasks().get(uuid);
        if (task != null) {
            task.cancel();
            manager.getTasks().remove(uuid);
            manager.getWaiters().remove(uuid);
            utils.sendMessage(p, values.getExit());
            return;
        }
        Game game = manager.getGame(p.getUniqueId());
        if (game != null) {
            String gameName = game.getName();
            if (gameName.startsWith(values.getWorldStart() + "E")) {
                if (!manager.getConfirmation().contains(gameName)) {
                    p.sendMessage("§eConfirm the exit from the editor by typing §a/jparkour exit§e again");
                    p.sendMessage("§eYou have 30 seconds to §aconfirm§e. §aMake sure you save everything you need.");
                    manager.getConfirmation().add(gameName);
                    scheduler.runTaskLater(plugin, () -> manager.getConfirmation().remove(gameName), 600);
                    return;
                }
            }
            game.close(true, false);
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
        values.setup(false);
        sender.sendMessage("§aSuccessfully!");
    }
}
