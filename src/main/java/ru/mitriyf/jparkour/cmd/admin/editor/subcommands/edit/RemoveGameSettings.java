package ru.mitriyf.jparkour.cmd.admin.editor.subcommands.edit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.game.temp.editor.data.PointData;

public class RemoveGameSettings {
    public void removeGameSetting(Player p, Editor editor, String[] args) {
        int point;
        try {
            point = Integer.parseInt(args[4]);
        } catch (Exception e) {
            p.sendMessage("§cInsert a number, not a string.");
            return;
        }
        PointData pointData = editor.getPoints().get(point);
        if (pointData == null) {
            p.sendMessage("§cThe point does not exist!");
            return;
        }
        Location loc = pointData.getLocation();
        editor.removeGlowStand(loc);
        editor.getPoints().remove(point);
        p.sendMessage("§aSuccessfully!");
    }
}
