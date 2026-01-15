package ru.mitriyf.jparkour.cmd.admin.editor.subcommands.edit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.cmd.admin.editor.GameEditor;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.game.temp.editor.data.PointData;

public class GetGameSettings {
    private final GameEditor gameEditor;

    public GetGameSettings(GameEditor gameEditor) {
        this.gameEditor = gameEditor;
    }

    public void getGameSetting(Player p, Game game, Editor editor, String[] args) {
        switch (args[3].toLowerCase()) {
            case "pose": {
                if (args.length == 5) {
                    getGamePose(p, editor, args[4]);
                    return;
                }
                break;
            }
            case "stand": {
                getGameStand(p, editor);
                return;
            }
            case "point": {
                if (args.length == 5) {
                    getGamePoint(p, editor, args[4]);
                    return;
                }
                break;
            }
            case "loc": {
                getGameLoc(p, editor);
                return;
            }
            case "locs": {
                getGameLocs(p, editor);
                return;
            }
            case "stands": {
                getGameStands(p, editor);
                return;
            }
            case "items": {
                getGameItems(p, game);
                return;
            }
            default: {
                break;
            }
        }
        gameEditor.sendGetGameHelp(p);
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
        Block block = editor.getSelectedBlockAxe();
        if (block == null) {
            p.sendMessage("§cFirst, select a block using the axe you were given.");
            return;
        }
        Location bLocation = block.getLocation();
        if (!editor.getStands().containsKey(bLocation)) {
            p.sendMessage("§cThis stand was not found.");
            return;
        }
        String type = editor.getStands().get(bLocation);
        p.sendMessage("Stand info:\nType: " + type + "\nLocation: " + bLocation);
    }

    private void getGamePoint(Player p, Editor editor, String type) {
        int point;
        try {
            point = Integer.parseInt(type);
        } catch (Exception e) {
            p.sendMessage("§cInsert a number, not a string.");
            return;
        }
        PointData pointData = editor.getPoints().get(point);
        if (pointData == null) {
            p.sendMessage("§cThe points are not set.");
            return;
        }
        p.sendMessage("§aPoint " + point + " is located in the following location:\n" + pointData.getLocation());
        p.sendMessage("§aTeleportation: " + pointData.isTeleportEnabled() + "\nRadiusStartPoint: " + pointData.getRadiusStartPoint());
    }

    private void getGameLoc(Player p, Editor editor) {
        Block block = editor.getSelectedBlockAxe();
        if (block == null) {
            p.sendMessage("§cFirst, select a block using the axe you were given.");
            return;
        }
        Location bLocation = block.getLocation();
        String type;
        if (bLocation.equals(editor.getSpawn())) {
            type = "spawn";
        } else if (bLocation.equals(editor.getStart())) {
            type = "start";
        } else if (bLocation.equals(editor.getEnd())) {
            type = "end";
        } else {
            p.sendMessage("§cThis loc was not found.");
            return;
        }
        p.sendMessage("Loc info:\nType: " + type + "\nLocation: " + bLocation);
    }

    private void getGameItems(Player p, Game game) {
        game.setSlots(game.getValues().getEditorSlots());
        p.sendMessage("§aSuccessfully!");
    }

    private void getGameStands(Player p, Editor editor) {
        p.sendMessage("§aStands Editor:\n" + editor.getStands());
    }

    private void getGameLocs(Player p, Editor editor) {
        p.sendMessage("§aLocs:\nspawn: " + editor.getSpawn() + "\nstart: " + editor.getStart() + "\nend: " + editor.getEnd() + "\nportal: " + editor.getPortal());
    }
}
