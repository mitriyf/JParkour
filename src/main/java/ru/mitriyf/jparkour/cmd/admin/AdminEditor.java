package ru.mitriyf.jparkour.cmd.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.cmd.admin.editor.GameEditor;
import ru.mitriyf.jparkour.cmd.admin.item.ItemEditor;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

public class AdminEditor {
    private final GameEditor gameEditor;
    private final ItemEditor itemEditor;
    private final JParkour plugin;
    private final Manager manager;
    private final Values values;
    private final Utils utils;

    public AdminEditor(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
        manager = plugin.getManager();
        itemEditor = new ItemEditor(plugin);
        gameEditor = new GameEditor(plugin);
    }

    public void checkAdminCommand(CommandSender s, String[] args) {
        if (!s.hasPermission("jparkour.admin")) {
            utils.sendMessage(s, values.getNoperm());
            return;
        }
        if (args.length > 1 && args.length < 13) {
            switch (args[1].toLowerCase()) {
                case "add": {
                    addGame(s, args);
                    return;
                }
                case "item": {
                    itemEditor.checkItemCommand(s, args);
                    return;
                }
                case "gameeditor": {
                    gameEditor.checkGameEditorCommand(s, args);
                    return;
                }
                case "game": {
                    gameEditor.checkGameCommand(s, args);
                    return;
                }
                case "restart": {
                    restartGame(s, args);
                    return;
                }
                case "updatetops": {
                    updateTops(s);
                    return;
                }
                case "kick": {
                    closeGame(s, args);
                    return;
                }
            }
        }
        sendAdminHelp(s);
    }

    private void addGame(CommandSender s, String[] args) {
        if (args.length < 3 || plugin.getServer().getPlayer(args[2]) == null) {
            s.sendMessage("§cThis player is not found.\n§c/jparkour admin add playerName Map");
            return;
        }
        Player p = plugin.getServer().getPlayer(args[2]);
        manager.join(p, args.length == 4 ? args[3].toLowerCase() : null, false);
        s.sendMessage("§aConnection attempt has been sent.");
    }

    private void restartGame(CommandSender s, String[] args) {
        Game game = getGame(s, args);
        if (game != null) {
            game.restart();
            s.sendMessage("§aSuccessfully!");
        }
    }

    private void updateTops(CommandSender s) {
        plugin.getSupports().getTops().startTimer();
        s.sendMessage("§aSuccessfully!");
    }

    private void closeGame(CommandSender s, String[] args) {
        Game game = getGame(s, args);
        if (game != null) {
            game.close(true, false);
            s.sendMessage("§aSuccessfully!");
        }
    }

    private Game getGame(CommandSender s, String[] args) {
        if (args.length < 3 || plugin.getServer().getPlayer(args[2]) == null) {
            s.sendMessage("§cThis player is not found/The command was executed incorrectly.");
            return null;
        }
        Player p = plugin.getServer().getPlayer(args[2]);
        Game game = manager.getGame(p.getUniqueId());
        if (game == null) {
            s.sendMessage("§cThe player is not in the game.");
            return null;
        }
        return game;
    }

    private void sendAdminHelp(CommandSender s) {
        s.sendMessage("§aJParkour Admin Help:\n");
        s.sendMessage("§a/jparkour admin add playerName §f- Add a player to a random game.");
        s.sendMessage("§a/jparkour admin add playerName Map §f- Add a player to a specific game.");
        s.sendMessage("§a/jparkour admin item §f- Get a Item Help.");
        s.sendMessage("§a/jparkour admin gameeditor §f- Get a GameEditor Help.");
        s.sendMessage("§a/jparkour admin game §f- Set a GameEditor Settings.");
        s.sendMessage("§a/jparkour admin restart playerName §f- Restart the player's game.");
        s.sendMessage("§a/jparkour admin updatetops §f- Update the tops.");
        s.sendMessage("§a/jparkour admin kick playerName §f- Kick the player out of the game.");
    }
}
