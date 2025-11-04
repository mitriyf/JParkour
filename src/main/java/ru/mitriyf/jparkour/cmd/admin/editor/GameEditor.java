package ru.mitriyf.jparkour.cmd.admin.editor;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.game.temp.data.PlayerData;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class GameEditor {
    private final Set<UUID> confirmation = new HashSet<>();
    private final BukkitScheduler scheduler;
    private final JParkour plugin;
    private final Manager manager;
    private final Values values;
    private final Utils utils;

    public GameEditor(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
        manager = plugin.getManager();
        scheduler = plugin.getServer().getScheduler();
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
            if (!values.getSchematics().containsKey(args[3])) {
                s.sendMessage("§cThis schematic does not exist.");
                return;
            }
            String schemName = values.getSchematics().get(args[3]).getSchematic();
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
        }
        if (args.length >= 3) {
            if (!(s instanceof Player)) {
                s.sendMessage("§cYou console!");
                return;
            }
            Player p = (Player) s;
            Game game = manager.getGame(p.getUniqueId());
            if (game == null || !game.isDev()) {
                s.sendMessage("§cYou are not in the gameeditor!");
                return;
            }
            Editor editor = game.getEditor();
            switch (args[2].toLowerCase()) {
                case "set": {
                    setGameSetting(p, editor, args);
                    return;
                }
                case "get": {
                    if (args.length >= 4) {
                        getGameSetting(p, editor, args);
                        return;
                    }
                    sendGameHelp(s);
                    return;
                }
                case "save": {
                    if (args.length == 4) {
                        saveGame(p, editor, args[3]);
                        return;
                    }
                    sendGameHelp(s);
                    return;
                }
                default: {
                    sendGameHelp(s);
                    return;
                }
            }
        }
        sendGameHelp(s);
    }

    public void setGameSetting(Player p, Editor editor, String[] args) {
        if (args.length == 5) {
            switch (args[3].toLowerCase()) {
                case "pose": {
                    setGamePose(p, editor, args[4]);
                    return;
                }
                case "stand": {
                    setGameStand(p, editor, args[4]);
                    return;
                }
                case "loc": {
                    setGameLoc(p, editor, args[4]);
                    return;
                }
            }
        }
        sendSetGameHelp(p);
    }

    private void setGamePose(Player p, Editor editor, String type) {
        Location loc = p.getLocation().getBlock().getLocation();
        switch (type.toLowerCase()) {
            case "portal": {
                editor.setPortal(loc);
                break;
            }
            case "3": {
                editor.setPose3(loc);
                break;
            }
            case "2": {
                editor.setPose2(loc);
                break;
            }
            default: {
                type = "1";
                editor.setPose1(loc);
                break;
            }
        }
        p.sendMessage("§aYou have successfully set point " + type + " (" + loc + ")");
    }

    private void setGameStand(Player p, Editor editor, String type) {
        if (editor.getSelectedBlockAxe() == null) {
            p.sendMessage("§cThe location block is not selected, place it with a axe.");
            return;
        } else if (!values.getStands().containsKey(type)) {
            p.sendMessage("§cThis type does not exist. Use the following types:\n" + values.getStands().keySet());
            return;
        }
        Location loc = editor.getSelectedBlockAxe().getLocation();
        editor.setBlockStand(loc, type);
        p.sendMessage("§aThe Stand has been successfully placed, and if you break the block stand will be deleted!");
    }

    private void setGameLoc(Player p, Editor editor, String type) {
        if (editor.getSelectedBlockAxe() == null) {
            p.sendMessage("§cThe location block is not selected, place it with a axe.");
            return;
        }
        Location loc = editor.getSelectedBlockAxe().getLocation();
        switch (type.toLowerCase()) {
            case "spawn": {
                editor.setSpawn(loc);
                p.sendMessage("§aThe Loc spawn has been successfully placed, and if you break the block stand will be deleted!");
                return;
            }
            case "start": {
                editor.setStart(loc);
                p.sendMessage("§aThe Loc start has been successfully placed, and if you break the block stand will be deleted!");
                return;
            }
            case "end": {
                editor.setEnd(loc);
                p.sendMessage("§aThe Loc end has been successfully placed, and if you break the block stand will be deleted!");
                return;
            }
            default: {
                p.sendMessage("§cThis type does not exist. Use the following types:\n[spawn, start, end]");
            }
        }
    }

    private void saveGame(Player p, Editor editor, String name) {
        if (editor.getPose1() == null || editor.getPose2() == null || editor.getPose3() == null) {
            p.sendMessage("§cNo positions are selected. Cancel...");
            return;
        } else if (editor.getSpawn() == null || editor.getPortal() == null || editor.getStart() == null || editor.getEnd() == null) {
            p.sendMessage("§cNo locs are selected. Cancel...");
            return;
        } else if (values.getSchematics().containsKey(name.toLowerCase()) && !confirmation.contains(p.getUniqueId())) {
            p.sendMessage("§cSuch schematics already exist. §aWrite the command again if you are sure that you want to overwrite the schematic.\nYou have 30 seconds to confirm.");
            confirmation.add(p.getUniqueId());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmation.remove(p.getUniqueId()), 600);
            return;
        }
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                CountDownLatch latch = new CountDownLatch(1);
                scheduler.runTask(plugin, () -> {
                    editor.clear();
                    scheduler.runTaskLater(plugin, latch::countDown, 2);
                });
                latch.await();
                utils.getSchematic().save(name, editor.getPose1(), editor.getPose2(), editor.getPose3());
                values.setup(false);
                scheduler.runTask(plugin, () -> {
                    editor.save(name);
                    scheduler.runTaskLater(plugin, latch::countDown, 2L);
                });
                latch.await();
                p.sendMessage("§aSuccessfully!\n§eSettings accepted.\nExit the editor: /jparkour exit");
            } catch (Exception e) {
                plugin.getLogger().warning("Couldn't save schem. Error: " + e);
                p.sendMessage("§cCouldn't save schem.");
            }
        });
    }

    public void getGameSetting(Player p, Editor editor, String[] args) {
        switch (args[3].toLowerCase()) {
            case "pose": {
                if (args.length == 5) {
                    getGamePose(p, editor, args[4]);
                } else {
                    sendGetGameHelp(p);
                }
                return;
            }
            case "stand": {
                getGameStand(p, editor);
                return;
            }
            case "loc": {
                getGameLoc(p, editor);
                return;
            }
            case "stands": {
                getGameStands(p, editor);
                return;
            }
            default: {
                sendGetGameHelp(p);
            }
        }
    }

    private void getGamePose(Player p, Editor editor, String type) {
        Location loc;
        switch (type.toLowerCase()) {
            case "portal": {
                loc = editor.getPortal();
                break;
            }
            case "3": {
                loc = editor.getPose3();
                break;
            }
            case "2": {
                loc = editor.getPose2();
                break;
            }
            default: {
                type = "1";
                loc = editor.getPose1();
                break;
            }
        }
        if (loc == null) {
            p.sendMessage("§cThe points are not set.");
            return;
        }
        p.sendMessage("§aPoint " + type + " is located in the following location:\n" + loc);
    }

    private void getGameStand(Player p, Editor editor) {
        if (editor.getSelectedBlockAxe() == null) {
            p.sendMessage("§cFirst, select a block using the axe you were given.");
            return;
        }
        Location bLocation = editor.getSelectedBlockAxe().getLocation();
        if (!editor.getStands().containsKey(bLocation)) {
            p.sendMessage("§cThis stand was not found.");
            return;
        }
        String type = editor.getStands().get(bLocation);
        p.sendMessage("Stand info:\nType: " + type + "\nLocation: " + bLocation);
    }

    private void getGameLoc(Player p, Editor editor) {
        if (editor.getSelectedBlockAxe() == null) {
            p.sendMessage("§cFirst, select a block using the axe you were given.");
            return;
        }
        Location bLocation = editor.getSelectedBlockAxe().getLocation();
        String type;
        if (editor.getSpawn().equals(bLocation)) {
            type = "spawn";
        } else if (editor.getStart().equals(bLocation)) {
            type = "start";
        } else if (editor.getEnd().equals(bLocation)) {
            type = "end";
        } else {
            p.sendMessage("§cThis loc was not found.");
            return;
        }
        p.sendMessage("Loc info:\nType: " + type + "\nLocation: " + bLocation);
    }

    private void getGameStands(Player p, Editor editor) {
        p.sendMessage("§aStands Editor:\n" + editor.getStands());
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
        s.sendMessage("§a/jparkour admin game save Name §f- Save the schematic with a name and add it to the game. Be sure to highlight the map's boundary points and enable it once it's ready.");
    }

    private void sendSetGameHelp(CommandSender s) {
        s.sendMessage("§a/jparkour admin game set pose 1/2/3/portal §f- Stand at the border of one of the points and select it.");
        s.sendMessage("§a/jparkour admin game set stand Type §f- Strike the block where stand should be with the axe in your hands.");
        s.sendMessage("§a/jparkour admin game set loc Type §f- Strike the block where loc should be with the axe in your hands.");
    }

    private void sendGetGameHelp(CommandSender s) {
        s.sendMessage("§a/jparkour admin game get pose 1/2/3/portal §f- Find out the coordinates of the boundaries of point 1 or 2.");
        s.sendMessage("§a/jparkour admin game get stand §f- Find out the type of block selected by the axe.");
        s.sendMessage("§a/jparkour admin game get loc §f- Find out the type of block selected by the axe.");
        s.sendMessage("§a/jparkour admin game get stands §f- Get all stands.");
    }
}
