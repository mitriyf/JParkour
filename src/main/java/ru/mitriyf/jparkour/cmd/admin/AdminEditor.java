package ru.mitriyf.jparkour.cmd.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

public class AdminEditor {
    private final JParkour plugin;
    private final Manager manager;
    private final Values values;
    private final Utils utils;

    public AdminEditor(JParkour plugin) {
        this.plugin = plugin;
        manager = plugin.getManager();
        values = plugin.getValues();
        utils = plugin.getUtils();
    }

    public void checkAdminCommand(CommandSender s, String[] args) {
        if (!s.hasPermission("jparkour.admin")) {
            utils.sendMessage(s, values.getNoperm());
            return;
        }
        if (args.length == 3 || args.length == 4) {
            if (plugin.getServer().getPlayer(args[2]) == null) {
                s.sendMessage("§cThis player is not found.");
                return;
            }
            Player p = plugin.getServer().getPlayer(args[2]);
            switch (args[1].toLowerCase()) {
                case "add": {
                    addGame(s, p, args);
                    return;
                }
                case "restart": {
                    restartGame(s, p);
                    return;
                }
                case "kick": {
                    closeGame(s, p);
                    return;
                }
                default: {
                    sendAdminHelp(s);
                    return;
                }
            }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("updatetops")) {
            updateTops();
            s.sendMessage("§aSuccessfully!");
            return;
        }
        sendAdminHelp(s);
    }

    private void addGame(CommandSender s, Player p, String[] args) {
        manager.join(p, args.length == 4 ? args[3] : null);
        s.sendMessage("§aConnection attempt has been sent.");
    }

    private void restartGame(CommandSender s, Player p) {
        if (manager.getPlayers().get(p.getUniqueId()) == null) {
            s.sendMessage("§cThe player is not in the game.");
            return;
        }
        String id = manager.getPlayers().get(p.getUniqueId()).getGame();
        Game game = values.getRooms().get(id);
        game.restart();
    }

    private void updateTops() {
        plugin.getSupports().getTops().getTask().cancel();
        plugin.getSupports().getTops().startTimer();
    }

    private void closeGame(CommandSender s, Player p) {
        if (manager.getPlayers().get(p.getUniqueId()) == null) {
            s.sendMessage("§cThe player is not in the game.");
            return;
        }
        String id = manager.getPlayers().get(p.getUniqueId()).getGame();
        Game game = values.getRooms().get(id);
        game.close(true, false);
    }

    private void sendAdminHelp(CommandSender s) {
        s.sendMessage("§aJParkour Admin Help:\n");
        s.sendMessage("§a/jparkour admin add playerName §f- Add a player to a random game.");
        s.sendMessage("§a/jparkour admin add playerName Map §f- Add a player to a specific game.");
        s.sendMessage("§a/jparkour admin restart playerName §f- Restart the player's game.");
        s.sendMessage("§a/jparkour admin updatetops §f- Update the tops.");
        s.sendMessage("§a/jparkour admin kick playerName §f- Kick the player out of the game.");
    }
}
