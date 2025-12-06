package ru.mitriyf.jparkour.cmd.admin.editor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.cmd.admin.editor.subcommands.SaveGameSettings;
import ru.mitriyf.jparkour.cmd.admin.editor.subcommands.edit.GetGameSettings;
import ru.mitriyf.jparkour.cmd.admin.editor.subcommands.edit.RemoveGameSettings;
import ru.mitriyf.jparkour.cmd.admin.editor.subcommands.edit.SetGameSettings;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;

import java.io.File;

public class GameEditor {
    private final RemoveGameSettings removeGameSettings;
    private final SaveGameSettings saveGameSettings;
    private final GetGameSettings getGameSettings;
    private final SetGameSettings setGameSettings;
    private final JParkour plugin;
    private final Manager manager;
    private final Values values;
    private final Utils utils;

    public GameEditor(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
        manager = plugin.getManager();
        removeGameSettings = new RemoveGameSettings();
        saveGameSettings = new SaveGameSettings(plugin);
        getGameSettings = new GetGameSettings(this);
        setGameSettings = new SetGameSettings(this, values);
    }

    public void checkGameEditorCommand(CommandSender s, String[] args) {
        if (!s.hasPermission("jparkour.gameeditor")) {
            utils.sendMessage(s, values.getNoperm());
            return;
        }
        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "new": {
                    newGame(s, args);
                    return;
                }
                case "list": {
                    listGames(s);
                    return;
                }
                case "remove": {
                    removeGame(s, args);
                    return;
                }
                default: {
                    sendGameEditorHelp(s);
                    return;
                }
            }
        }
        sendGameEditorHelp(s);
    }

    private void newGame(CommandSender s, String[] args) {
        if (!(s instanceof Player)) {
            s.sendMessage("§cYou console!");
            return;
        }
        Player p = (Player) s;
        manager.join(p, args.length == 4 ? args[3] : null, true);
        s.sendMessage("§eWarning! You are in the game editor.");
        s.sendMessage("§eYou can't start the game here, but you can use the editor to create new maps and quickly adjust the locations of the stands.");
        s.sendMessage("§eFor help, use:\n/jparkour admin game\n§cExit the editor: /jparkour exit");
    }

    private void listGames(CommandSender s) {
        s.sendMessage("§aList games:\n" + values.getSchematics().keySet());
    }

    private void removeGame(CommandSender s, String[] args) {
        if (args.length == 4) {
            SchematicData data = values.getSchematics().get(args[3]);
            if (data == null) {
                s.sendMessage("§cThis schematic does not exist.");
                return;
            }
            String schemName = data.getSchematic();
            values.delete(new File(plugin.getDataFolder(), values.getSchematicsDir() + schemName));
            values.getSchematics().remove(args[3]);
            values.getMaps().remove(args[3]);
            s.sendMessage("§aSuccessfully!");
            return;
        }
        s.sendMessage("§c/jparkour admin gameeditor remove Name §f- Delete the game schematic.");
    }

    public void checkGameCommand(CommandSender s, String[] args) {
        if (!s.hasPermission("jparkour.game")) {
            utils.sendMessage(s, values.getNoperm());
            return;
        } else if (args.length < 3) {
            sendGameHelp(s);
            return;
        } else if (!(s instanceof Player)) {
            s.sendMessage("§cYou console!");
            return;
        }
        Player p = (Player) s;
        Game game = manager.getGame(p.getUniqueId());
        if (game == null || !game.isDev()) {
            s.sendMessage("§cYou are not in the gameeditor!");
            return;
        }
        checkAdminSubCommand(p, game.getEditor(), args);
    }

    private void checkAdminSubCommand(Player p, Editor editor, String[] args) {
        switch (args[2].toLowerCase()) {
            case "set": {
                setGameSettings.setGameSetting(p, editor, args);
                return;
            }
            case "get": {
                if (args.length >= 4) {
                    getGameSettings.getGameSetting(p, editor, args);
                    return;
                }
                break;
            }
            case "remove": {
                if (args.length == 5) {
                    removeGameSettings.removeGameSetting(p, editor, args);
                    return;
                }
                break;
            }
            case "save": {
                if (args.length == 4) {
                    saveGameSettings.saveGame(p, editor, args[3]);
                    return;
                }
                sendGameHelp(p);
                return;
            }
            default: {
                break;
            }
        }
        sendGameHelp(p);
    }

    private void sendGameEditorHelp(CommandSender s) {
        s.sendMessage("§aJParkour GameEditor Help:\n");
        s.sendMessage("§a/jparkour admin gameeditor new §f- Create a new game schematic.");
        s.sendMessage("§a/jparkour admin gameeditor new OtherGame §f- Create a new game schematic by copying another game schematic.");
        s.sendMessage("§a/jparkour admin gameeditor list §f- Get a list of game schematics.");
        s.sendMessage("§a/jparkour admin gameeditor remove Name §f- Delete the game schematic.");
    }

    private void sendGameHelp(CommandSender s) {
        s.sendMessage("§aCommands for configuring the game during creation (new):");
        sendSetGameHelp(s);
        sendGetGameHelp(s);
        sendRemoveGameHelp(s);
        s.sendMessage("§a/jparkour admin game save Name §f- Save the schematic with a name and add it to the game. Be sure to highlight the map's boundary points and enable it once it's ready.");
    }

    public void sendSetGameHelp(CommandSender s) {
        s.sendMessage("§a/jparkour admin game set pose 1/2/3/portal §f- Stand at the border of one of the points and select it.");
        s.sendMessage("§a/jparkour admin game set stand Type §f- Strike the block where stand should be with the axe in your hands.");
        s.sendMessage("§a/jparkour admin game set loc Type §f- Strike the block where loc should be with the axe in your hands.");
        s.sendMessage("§a/jparkour admin game set point Number(1to∞) RadiusStartPoint(0.5/?.?) Teleportation(true/false) addX addY addZ Yaw Pitch §f- Go to the location where the point should be and enter this command.");
        s.sendMessage("§fThe normal block values will be taken (there is an add for this), and if yaw and pitch are not specified, the values that the player has looked at will be taken");
    }

    public void sendRemoveGameHelp(CommandSender s) {
        s.sendMessage("§a/jparkour admin game remove point number(1to∞) §f- Delete a point.");
    }

    public void sendGetGameHelp(CommandSender s) {
        s.sendMessage("§a/jparkour admin game get pose 1/2/3/portal §f- Find out the coordinates of the boundaries of point 1 or 2.");
        s.sendMessage("§a/jparkour admin game get stand §f- Find out the type of block selected by the axe.");
        s.sendMessage("§a/jparkour admin game get loc §f- Find out the type of block selected by the axe.");
        s.sendMessage("§a/jparkour admin game get point number(1to∞) §f- Find out information about the point.");
        s.sendMessage("§a/jparkour admin game get locs §f- Get all locs..");
        s.sendMessage("§a/jparkour admin game get stands §f- Get all stands.");
    }
}
