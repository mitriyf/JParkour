package ru.mitriyf.jparkour.command.subcommand.admin;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.editor.GameEditor;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.model.PointData;
import ru.mitriyf.jparkour.model.SchematicData;
import ru.mitriyf.jparkour.model.StandLocationData;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameEditorSubCommand {
    private final Set<UUID> confirmation = new HashSet<>();
    private final BukkitScheduler scheduler;
    private final GameManager gameManager;
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;

    public GameEditorSubCommand(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
        scheduler = plugin.getServer().getScheduler();
    }

    public void checkGameSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jparkour.game")) {
            utils.sendMessage(sender, values.getNoperm());
            return;
        } else if (args.length < 3) {
            sendGameHelp(sender);
            return;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou console!");
            return;
        }
        Player p = (Player) sender;
        Game game = gameManager.getGame(p.getUniqueId());
        if (game == null || !game.isDev()) {
            sender.sendMessage("§cYou are not in the gameeditor!");
            return;
        }
        checkGameSubCommand(p, game, game.getGameEditor(), args);
    }

    private void checkGameSubCommand(Player player, Game game, GameEditor gameEditor, String[] args) {
        switch (args[2].toLowerCase()) {
            case "set": {
                setGameSetting(player, gameEditor, args);
                return;
            }
            case "get": {
                if (args.length >= 4) {
                    getGameSetting(player, game, gameEditor, args);
                    return;
                }
                break;
            }
            case "remove": {
                if (args.length == 5) {
                    removeGameSetting(player, gameEditor, args);
                    return;
                }
                break;
            }
            case "save": {
                if (args.length == 4) {
                    saveGame(player, gameEditor, args[3]);
                    return;
                }
                sendGameHelp(player);
                return;
            }
            default: {
                break;
            }
        }
        sendGameHelp(player);
    }

    public void checkGameEditorSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jparkour.gameeditor")) {
            utils.sendMessage(sender, values.getNoperm());
            return;
        }
        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "new": {
                    newGame(sender, args);
                    return;
                }
                case "list": {
                    listGames(sender);
                    return;
                }
                case "remove": {
                    removeGame(sender, args);
                    return;
                }
                default: {
                    sendGameEditorHelp(sender);
                    return;
                }
            }
        }
        sendGameEditorHelp(sender);
    }

    private void newGame(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou console!");
            return;
        }
        Player p = (Player) sender;
        gameManager.join(p, args.length == 4 ? args[3].toLowerCase() : null, true);
        sender.sendMessage("§eWarning! You are in the game editor.");
        sender.sendMessage("§eYou can't start the game here, but you can use the editor to create new maps and quickly adjust the locations of the stands.");
        sender.sendMessage("§eFor help, use:\n/jparkour admin game\n§cExit the editor: /jparkour exit");
    }

    private void listGames(CommandSender sender) {
        sender.sendMessage("§aList games:\n" + values.getSchematics().keySet());
    }

    private void removeGame(CommandSender sender, String[] args) {
        if (args.length == 4) {
            SchematicData data = values.getSchematics().get(args[3].toLowerCase());
            if (data == null) {
                sender.sendMessage("§cThis schematic does not exist.");
                return;
            }
            String schemName = data.getSchematic();
            values.delete(new File(plugin.getDataFolder(), values.getSchematicsDir() + schemName));
            values.getSchematics().remove(args[3]);
            values.getMaps().remove(args[3]);
            sender.sendMessage("§aSuccessfully!");
            return;
        }
        sender.sendMessage("§c/jparkour admin gameeditor remove Name §f- Delete the game schematic.");
    }

    private void setGameSetting(Player player, GameEditor gameEditor, String[] args) {
        if (args.length >= 5) {
            switch (args[3].toLowerCase()) {
                case "player": {
                    setGamePlayer(player, gameEditor, args[4]);
                    return;
                }
                case "pose": {
                    setGamePose(player, gameEditor, args[4]);
                    return;
                }
                case "point": {
                    setGamePoint(player, gameEditor, args);
                    return;
                }
                case "stand": {
                    setGameStand(player, gameEditor, args);
                    return;
                }
                case "loc": {
                    setGameLoc(player, gameEditor, args);
                    return;
                }
            }
        }
        sendSetGameHelp(player);
    }

    private void setGamePlayer(Player player, GameEditor gameEditor, String id) {
        int playerId;
        try {
            playerId = utils.formatInt(id);
        } catch (Exception e) {
            player.sendMessage("§cYou didn't enter a number.");
            return;
        }
        gameEditor.changePlayer(player, playerId);
    }

    private void setGamePose(Player player, GameEditor gameEditor, String type) {
        Location loc = player.getLocation().getBlock().getLocation();
        switch (type.toLowerCase()) {
            case "portal": {
                gameEditor.setPortal(loc);
                break;
            }
            case "3": {
                gameEditor.setPose3(loc);
                break;
            }
            case "2": {
                gameEditor.setPose2(loc);
                break;
            }
            default: {
                type = "1";
                gameEditor.setPose1(loc);
                break;
            }
        }
        player.sendMessage("§aYou have successfully set pose " + type + " (" + loc + ")");
    }

    private void setGamePoint(Player player, GameEditor gameEditor, String[] args) {
        if (args.length < 5 || args.length > 12) {
            sendSetGameHelp(player);
            return;
        }
        int point;
        double radiusStartPoint;
        boolean teleportation;
        Location loc = player.getLocation().getBlock().getLocation();
        try {
            point = Integer.parseInt(args[4]);
            if (point == 0 || gameEditor.getPoints().get(point) != null) {
                player.sendMessage("§cThis point already exists! Remove it:");
                sendRemoveGameHelp(player);
                return;
            }
            int length = args.length;
            radiusStartPoint = length >= 6 ? Double.parseDouble(args[5]) : 0.5;
            teleportation = length >= 7 && Boolean.parseBoolean(args[6]);
            double addX = length >= 8 ? utils.formatDouble(args[7]) : 0;
            double addY = length >= 9 ? utils.formatDouble(args[8]) : 0;
            double addZ = length >= 10 ? utils.formatDouble(args[9]) : 0;
            loc.add(addX + 0.5, addY, addZ + 0.5);
            loc.setYaw(length >= 11 ? utils.formatFloat(args[10]) : loc.getYaw());
            loc.setPitch(length == 12 ? utils.formatFloat(args[11]) : loc.getPitch());
        } catch (Exception e) {
            player.sendMessage("§cInsert a number, not a string [point]. Insert a boolean (true/false), not a string [teleportation]");
            player.sendMessage("§cInsert a double(0.5/?.?)/float, not a string. [radiusStartPoint, addX, addY, addZ (double), yaw, pitch (float)]");
            return;
        }
        gameEditor.getPoints().put(point, new PointData(loc, radiusStartPoint, teleportation));
        gameEditor.setPoint(point, radiusStartPoint, teleportation, loc);
        player.sendMessage("§aSuccessfully!");
    }

    public void setGameStand(Player player, GameEditor gameEditor, String[] args) {
        Block block = gameEditor.getSelectedBlockAxe();
        String type = args[4];
        if (block == null) {
            player.sendMessage("§cThe location block is not selected, place it with a axe.");
            return;
        } else if (!values.getStands().containsKey(type)) {
            player.sendMessage("§cThis type does not exist. Use the following types:\n" + values.getStands().keySet());
            return;
        }
        Location loc = block.getLocation();
        if (setLocationFromArgs(player, args, loc)) {
            return;
        }
        String id = gameEditor.contains(loc);
        if (id != null) {
            player.sendMessage("§cThis block is already occupied by " + id + ". Remove the block and add a new one using the command. (" + loc + ")");
            return;
        }
        gameEditor.setBlockStand(loc, type);
        player.sendMessage("§aThe Stand has been successfully placed, and if you break the block stand will be deleted!");
    }

    private void setGameLoc(Player player, GameEditor gameEditor, String[] args) {
        if (gameEditor.getSelectedBlockAxe() == null) {
            player.sendMessage("§cThe location block is not selected, place it with a axe.");
            return;
        }
        Location loc = gameEditor.getSelectedBlockAxe().getLocation();
        String id = gameEditor.contains(loc);
        if (id != null) {
            player.sendMessage("§cThis block is already occupied by " + id + ". Remove the block and add a new one using the command. (" + loc + ")");
            return;
        }
        String type = args[4];
        if (setLocationFromArgs(player, args, loc)) {
            return;
        }
        switch (type.toLowerCase()) {
            case "spawn": {
                gameEditor.setSpawn(loc);
                player.sendMessage("§aThe Loc spawn has been successfully placed, and if you break the block stand will be deleted!");
                return;
            }
            case "start": {
                gameEditor.setStart(loc);
                player.sendMessage("§aThe Loc start has been successfully placed, and if you break the block stand will be deleted!");
                return;
            }
            case "end": {
                gameEditor.setEnd(loc);
                player.sendMessage("§aThe Loc end has been successfully placed, and if you break the block stand will be deleted!");
                return;
            }
            default: {
                player.sendMessage("§cThis type does not exist. Use the following types:\n[spawn, start, end]");
            }
        }
    }

    private boolean setLocationFromArgs(Player player, String[] args, Location loc) {
        int length = args.length;
        try {
            double addX = length >= 6 ? utils.formatDouble(args[5]) : 0;
            double addY = length >= 7 ? utils.formatDouble(args[6]) : 0;
            double addZ = length >= 8 ? utils.formatDouble(args[7]) : 0;
            loc.add(addX + 0.5, addY, addZ + 0.5);
            loc.setYaw(length >= 9 ? utils.formatFloat(args[8]) : loc.getYaw());
            loc.setPitch(length == 10 ? utils.formatFloat(args[9]) : loc.getPitch());
        } catch (Exception e) {
            player.sendMessage("§cCheck the correctness of the numbers.");
            return true;
        }
        return false;
    }

    private void getGameSetting(Player player, Game game, GameEditor gameEditor, String[] args) {
        switch (args[3].toLowerCase()) {
            case "player": {
                player.sendMessage("§aYour player's ID: " + gameEditor.getMemberData().getId());
                return;
            }
            case "pose": {
                if (args.length == 5) {
                    getGamePose(player, gameEditor, args[4]);
                    return;
                }
                break;
            }
            case "stand": {
                getGameStand(player, gameEditor);
                return;
            }
            case "point": {
                if (args.length == 5) {
                    getGamePoint(player, gameEditor, args[4]);
                    return;
                }
                break;
            }
            case "loc": {
                getGameLoc(player, gameEditor);
                return;
            }
            case "locs": {
                getGameLocs(player, gameEditor);
                return;
            }
            case "stands": {
                getGameStands(player, gameEditor);
                return;
            }
            case "items": {
                getGameItems(player, game);
                return;
            }
            default: {
                break;
            }
        }
        sendGetGameHelp(player);
    }

    private void getGamePose(Player player, GameEditor gameEditor, String type) {
        Location loc;
        switch (type.toLowerCase()) {
            case "portal": {
                loc = gameEditor.getPortal();
                break;
            }
            case "3": {
                loc = gameEditor.getPose3();
                break;
            }
            case "2": {
                loc = gameEditor.getPose2();
                break;
            }
            default: {
                type = "1";
                loc = gameEditor.getPose1();
                break;
            }
        }
        if (loc == null) {
            player.sendMessage("§cThe points are not set.");
            return;
        }
        player.sendMessage("§aPoint " + type + " is located in the following location:\n" + loc);
    }

    private void getGameStand(Player player, GameEditor gameEditor) {
        Block block = gameEditor.getSelectedBlockAxe();
        if (block == null) {
            player.sendMessage("§cFirst, select a block using the axe you were given.");
            return;
        }
        Location bLocation = block.getLocation();
        StandLocationData standLocationData = gameEditor.getStands().get(bLocation);
        if (standLocationData == null) {
            player.sendMessage("§cThis stand was not found.");
            return;
        }
        player.sendMessage("Stand info:\nType: " + standLocationData.getType() + "\nLocation: " + standLocationData.getLocation());
    }

    private void getGamePoint(Player player, GameEditor gameEditor, String type) {
        int point;
        try {
            point = Integer.parseInt(type);
        } catch (Exception e) {
            player.sendMessage("§cInsert a number, not a string.");
            return;
        }
        PointData pointData = gameEditor.getPoints().get(point);
        if (pointData == null) {
            player.sendMessage("§cThe points are not set.");
            return;
        }
        player.sendMessage("§aPoint " + point + " is located in the following location:\n" + pointData.getLocation());
        player.sendMessage("§aTeleportation: " + pointData.isTeleportEnabled() + "\nRadiusStartPoint: " + pointData.getRadiusStartPoint());
    }

    private void getGameLoc(Player player, GameEditor gameEditor) {
        Block block = gameEditor.getSelectedBlockAxe();
        if (block == null) {
            player.sendMessage("§cFirst, select a block using the axe you were given.");
            return;
        }
        Location bLocation = block.getLocation();
        StringBuilder type = new StringBuilder();
        List<Location> locations = gameEditor.getDefaultLocations().get(bLocation);
        if (locations != null) {
            for (Location location : locations) {
                if (type.length() > 0) {
                    type.append(", ");
                }
                if (location.equals(gameEditor.getSpawn())) {
                    type.append("spawn");
                } else if (location.equals(gameEditor.getStart())) {
                    type.append("start");
                } else if (location.equals(gameEditor.getEnd())) {
                    type.append("end");
                } else if (location.equals(gameEditor.getPortal())) {
                    type.append("portal");
                }
            }
        }
        String finalType = type.toString();
        if (finalType.isEmpty()) {
            player.sendMessage("§cThis loc was not found.");
            return;
        }
        player.sendMessage("Loc info:\nType: " + finalType + "\nLocation: " + locations);
    }

    private void getGameItems(Player player, Game game) {
        utils.setSlots(player, game.getValues().getEditorSlots());
        player.sendMessage("§aSuccessfully!");
    }

    private void getGameStands(Player player, GameEditor gameEditor) {
        player.sendMessage("§aStands Editor:\n" + gameEditor.getStands());
    }

    private void getGameLocs(Player player, GameEditor gameEditor) {
        player.sendMessage("§aLocs:\nspawn: " + gameEditor.getSpawn() + "\nstart: " + gameEditor.getStart() + "\nend: " + gameEditor.getEnd() + "\nportal: " + gameEditor.getPortal());
    }

    private void removeGameSetting(Player player, GameEditor gameEditor, String[] args) {
        int point;
        try {
            point = Integer.parseInt(args[4]);
        } catch (Exception e) {
            player.sendMessage("§cInsert a number, not a string.");
            return;
        }
        PointData pointData = gameEditor.getPoints().get(point);
        if (pointData == null) {
            player.sendMessage("§cThe point does not exist!");
            return;
        }
        Location loc = pointData.getLocation();
        gameEditor.removeStand(gameEditor.getGlowStands(), loc);
        gameEditor.getPoints().remove(point);
        player.sendMessage("§aSuccessfully!");
    }

    private void saveGame(Player player, GameEditor gameEditor, String name) {
        if (gameEditor.getPose1() == null || gameEditor.getPose2() == null || gameEditor.getPose3() == null) {
            player.sendMessage("§cNo positions are selected. Cancel...");
            return;
        } else if (gameEditor.getSpawn() == null || gameEditor.getPortal() == null || gameEditor.getStart() == null || gameEditor.getEnd() == null) {
            player.sendMessage("§cNo locs are selected. Cancel...");
            return;
        }
        UUID uuid = player.getUniqueId();
        if (values.getSchematics().containsKey(name.toLowerCase()) && !confirmation.contains(uuid)) {
            player.sendMessage("§cSuch schematics already exist. §aWrite the command again if you are sure that you want to overwrite the schematic.\nYou have 30 seconds to confirm.");
            confirmation.add(uuid);
            scheduler.runTaskLater(plugin, () -> confirmation.remove(uuid), 600);
            return;
        } else if (gameEditor.isProcess()) {
            player.sendMessage("§cSaving is already in progress! Please wait...");
            return;
        }
        gameEditor.processSave(player, name);
    }

    private void sendGameEditorHelp(CommandSender sender) {
        sender.sendMessage("§aJParkour GameEditor Help:\n");
        sender.sendMessage("§a/jparkour admin gameeditor new §f- Create a new game schematic.");
        sender.sendMessage("§a/jparkour admin gameeditor new OtherGame §f- Create a new game schematic by copying another game schematic.");
        sender.sendMessage("§a/jparkour admin gameeditor list §f- Get a list of game schematics.");
        sender.sendMessage("§a/jparkour admin gameeditor remove Name §f- Delete the game schematic.");
    }

    private void sendGameHelp(CommandSender sender) {
        sender.sendMessage("§aCommands for configuring the game during creation (new):");
        sendSetGameHelp(sender);
        sendGetGameHelp(sender);
        sendRemoveGameHelp(sender);
        sender.sendMessage("§a/jparkour admin game save Name §f- Save the schematic with a name and add it to the game. Be sure to highlight the map's boundary points and enable it once it's ready.");
    }

    private void sendSetGameHelp(CommandSender sender) {
        sender.sendMessage("§a/jparkour admin game set player 0/1/2/... §f- Change the player. The initial value is 0.");
        sender.sendMessage("§a/jparkour admin game set pose 1/2/3/portal §f- Stand at the border of one of the points and select it.");
        sender.sendMessage("§a/jparkour admin game set stand Type addX addY addZ Yaw Pitch §f- Strike the block where stand should be with the axe in your hands.");
        sender.sendMessage("§a/jparkour admin game set loc Type addX addY addZ Yaw Pitch §f- Strike the block where loc should be with the axe in your hands.");
        sender.sendMessage("§a/jparkour admin game set point Number(1to∞) RadiusStartPoint(0.5/?.?) Teleportation(true/false) addX addY addZ Yaw Pitch §f- Go to the location where the point should be and enter this command.");
        sender.sendMessage("§fThe normal block values will be taken (there is an add for this), and if yaw and pitch are not specified, the values that the player has looked at will be taken");
    }

    private void sendRemoveGameHelp(CommandSender sender) {
        sender.sendMessage("§a/jparkour admin game remove point number(1to∞) §f- Delete a point.");
    }

    private void sendGetGameHelp(CommandSender sender) {
        sender.sendMessage("§a/jparkour admin game get player §f- Get the player. The initial value is 0.");
        sender.sendMessage("§a/jparkour admin game get pose 1/2/3/portal §f- Find out the coordinates of the boundaries of point 1 or 2.");
        sender.sendMessage("§a/jparkour admin game get stand §f- Find out the type of block selected by the axe.");
        sender.sendMessage("§a/jparkour admin game get loc §f- Find out the type of block selected by the axe.");
        sender.sendMessage("§a/jparkour admin game get point number(1to∞) §f- Find out information about the point.");
        sender.sendMessage("§a/jparkour admin game get locs §f- Get all locs.");
        sender.sendMessage("§a/jparkour admin game get stands §f- Get all stands.");
        sender.sendMessage("§a/jparkour admin game get items §f- Get all Items.");
    }
}
