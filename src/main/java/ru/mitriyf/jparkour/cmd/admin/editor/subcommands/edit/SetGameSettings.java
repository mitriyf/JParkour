package ru.mitriyf.jparkour.cmd.admin.editor.subcommands.edit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.cmd.admin.editor.GameEditor;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.game.temp.editor.data.PointData;
import ru.mitriyf.jparkour.values.Values;

public class SetGameSettings {
    private final GameEditor gameEditor;
    private final Values values;

    public SetGameSettings(GameEditor gameEditor, Values values) {
        this.gameEditor = gameEditor;
        this.values = values;
    }

    public void setGameSetting(Player p, Editor editor, String[] args) {
        if (args.length >= 5) {
            switch (args[3].toLowerCase()) {
                case "pose": {
                    setGamePose(p, editor, args[4]);
                    return;
                }
                case "point": {
                    setGamePoint(p, editor, args);
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
        gameEditor.sendSetGameHelp(p);
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
        p.sendMessage("§aYou have successfully set pose " + type + " (" + loc + ")");
    }

    private void setGamePoint(Player p, Editor editor, String[] args) {
        if (args.length < 5 || args.length > 12) {
            gameEditor.sendSetGameHelp(p);
            return;
        }
        int point;
        double radiusStartPoint, addX, addY, addZ;
        float yaw = 0, pitch = 0;
        boolean teleportation;
        Location loc = p.getLocation().getBlock().getLocation();
        try {
            point = Integer.parseInt(args[4]);
            if (point == 0 || editor.getPoints().get(point) != null) {
                p.sendMessage("§cThis point already exists! Remove it:");
                gameEditor.sendRemoveGameHelp(p);
                return;
            }
            radiusStartPoint = args.length >= 6 ? Double.parseDouble(args[5]) : 0.5;
            teleportation = args.length >= 7 && Boolean.parseBoolean(args[6]);
            addX = args.length >= 8 ? Double.parseDouble(args[7]) : 0;
            addY = args.length >= 9 ? Double.parseDouble(args[8]) : 0;
            addZ = args.length >= 10 ? Double.parseDouble(args[9]) : 0;
            loc.add(addX + 0.5, addY, addZ + 0.5);
            if (!teleportation) {
                yaw = args.length >= 11 ? Float.parseFloat(args[10]) : loc.getYaw();
                pitch = args.length == 12 ? Float.parseFloat(args[11]) : loc.getPitch();
            }
            loc.setYaw(yaw);
            loc.setPitch(pitch);
        } catch (Exception e) {
            p.sendMessage("§cInsert a number, not a string [point]. Insert a boolean (true/false), not a string [teleportation]");
            p.sendMessage("§cInsert a double(0.5/?.?)/float, not a string. [radiusStartPoint, addX, addY, addZ (double), yaw, pitch (float)]");
            return;
        }
        editor.getPoints().put(point, new PointData(loc, radiusStartPoint, teleportation));
        editor.setPoint(point, radiusStartPoint, teleportation, loc);
        p.sendMessage("§aSuccessfully!");
    }

    private void setGameStand(Player p, Editor editor, String type) {
        Block block = editor.getSelectedBlockAxe();
        if (block == null) {
            p.sendMessage("§cThe location block is not selected, place it with a axe.");
            return;
        } else if (!values.getStands().containsKey(type)) {
            p.sendMessage("§cThis type does not exist. Use the following types:\n" + values.getStands().keySet());
            return;
        }
        Location loc = block.getLocation();
        String id = editor.contains(loc);
        if (id != null) {
            p.sendMessage("§cThis block is already occupied by " + id + ". Remove the block and add a new one using the command. (" + loc + ")");
            return;
        }
        editor.setBlockStand(loc, type);
        p.sendMessage("§aThe Stand has been successfully placed, and if you break the block stand will be deleted!");
    }

    private void setGameLoc(Player p, Editor editor, String type) {
        if (editor.getSelectedBlockAxe() == null) {
            p.sendMessage("§cThe location block is not selected, place it with a axe.");
            return;
        }
        Location loc = editor.getSelectedBlockAxe().getLocation();
        String id = editor.contains(loc);
        if (id != null) {
            p.sendMessage("§cThis block is already occupied by " + id + ". Remove the block and add a new one using the command. (" + loc + ")");
            return;
        }
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
}
