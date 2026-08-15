package ru.mitriyf.jparkour.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.command.subcommand.admin.GameEditorSubCommand;
import ru.mitriyf.jparkour.command.subcommand.admin.ItemEditorSubCommand;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

public class AdminEditorSubCommand {
    private final GameEditorSubCommand gameEditorSubCommand;
    private final ItemEditorSubCommand itemEditorSubCommand;
    private final GameManager gameManager;
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;

    public AdminEditorSubCommand(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
        gameManager = plugin.getGameManager();
        itemEditorSubCommand = new ItemEditorSubCommand(plugin);
        gameEditorSubCommand = new GameEditorSubCommand(plugin);
    }

    public void checkAdminSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jparkour.admin")) {
            utils.sendMessage(sender, values.getNoperm());
            return;
        }
        if (args.length > 1 && args.length < 13) {
            switch (args[1].toLowerCase()) {
                case "add": {
                    addGame(sender, args);
                    return;
                }
                case "item": {
                    itemEditorSubCommand.checkItemSubCommand(sender, args);
                    return;
                }
                case "game": {
                    gameEditorSubCommand.checkGameSubCommand(sender, args);
                    return;
                }
                case "gameeditor": {
                    gameEditorSubCommand.checkGameEditorSubCommand(sender, args);
                    return;
                }
                case "restart": {
                    restartGame(sender, args);
                    return;
                }
                case "updatetops": {
                    updateTops(sender);
                    return;
                }
                case "kick": {
                    closeGame(sender, args);
                    return;
                }
                case "locale": {
                    getLocale(sender);
                    return;
                }
            }
        }
        sendAdminHelp(sender);
    }

    private void addGame(CommandSender sender, String[] args) {
        if (args.length < 3 || plugin.getServer().getPlayer(args[2]) == null) {
            sender.sendMessage("§cThis player is not found.\n§c/jparkour admin add playerName Map");
            return;
        }
        Player p = plugin.getServer().getPlayer(args[2]);
        gameManager.join(p, args.length == 4 ? args[3].toLowerCase() : null, false);
        sender.sendMessage("§aConnection attempt has been sent.");
    }

    private void restartGame(CommandSender sender, String[] args) {
        Game game = getGame(sender, args);
        if (game != null) {
            game.restart();
            sender.sendMessage("§aSuccessfully!");
        }
    }

    private void getLocale(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou console!");
            return;
        }
        sender.sendMessage("§aYour language code:\n" + utils.getLocale().player((Player) sender));
    }

    private void updateTops(CommandSender sender) {
        plugin.getSupports().getTops().startTimer();
        sender.sendMessage("§aSuccessfully!");
    }

    private void closeGame(CommandSender sender, String[] args) {
        Game game = getGame(sender, args);
        if (game != null) {
            game.close(true, false);
            sender.sendMessage("§aSuccessfully!");
        }
    }

    private Game getGame(CommandSender sender, String[] args) {
        if (args.length < 3 || plugin.getServer().getPlayer(args[2]) == null) {
            sender.sendMessage("§cThis player is not found/The command was executed incorrectly.");
            return null;
        }
        Player player = plugin.getServer().getPlayer(args[2]);
        Game game = gameManager.getGame(player.getUniqueId());
        if (game == null) {
            sender.sendMessage("§cThe player is not in the game.");
            return null;
        }
        return game;
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage("§aJParkour Admin Help:\n");
        sender.sendMessage("§a/jparkour admin add playerName Map §f- Add a player to a specific game.");
        sender.sendMessage("§a/jparkour admin add playerName §f- Add a player to a random game.");
        sender.sendMessage("§a/jparkour admin item §f- Get a Item Help.");
        sender.sendMessage("§a/jparkour admin gameeditor §f- Get a GameEditor Help.");
        sender.sendMessage("§a/jparkour admin game §f- Get/Set a GameEditor Settings.");
        sender.sendMessage("§a/jparkour admin restart playerName §f- Restart the player's game.");
        sender.sendMessage("§a/jparkour admin updatetops §f- Update the tops.");
        sender.sendMessage("§a/jparkour admin kick playerName §f- Kick the player out of the game.");
        sender.sendMessage("§a/jparkour admin locale §f- Get the client's language code.");
    }
}
