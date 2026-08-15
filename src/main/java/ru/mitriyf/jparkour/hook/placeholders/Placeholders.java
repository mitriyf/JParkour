package ru.mitriyf.jparkour.hook.placeholders;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.hook.tops.Tops;
import ru.mitriyf.jparkour.hook.tops.schematic.SchematicTop;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.model.MemberData;
import ru.mitriyf.jparkour.model.PlayerDataSchematic;
import ru.mitriyf.jparkour.model.SchematicMemberData;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {
    private final String falseString = PlaceholderAPIPlugin.booleanFalse();
    private final String trueString = PlaceholderAPIPlugin.booleanTrue();
    private final GameManager gameManager;
    private final Values values;
    private final Utils utils;
    private final Tops tops;

    public Placeholders(JParkour plugin) {
        gameManager = plugin.getGameManager();
        values = plugin.getValues();
        utils = plugin.getUtils();
        tops = plugin.getSupports().getTops();
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        String[] args = ind.split("_");
        if (p != null && args.length >= 1) {
            if (args[0].equalsIgnoreCase("tops")) {
                return tops(p, args);
            }
            return player(p, args);
        }
        return null;
    }

    private String tops(Player p, String[] args) {
        if (args.length == 4) {
            SchematicTop schematicTop = tops.getSchematic().get(args[1]);
            if (schematicTop == null) {
                if (values.getSchematics().containsKey(args[1])) {
                    if (args[3].equalsIgnoreCase("name")) {
                        return notClaimed(p);
                    } else {
                        return "";
                    }
                }
                return "Schematic not found.";
            }
            int topPlace;
            try {
                topPlace = Integer.parseInt(args[2]);
            } catch (Exception e) {
                String name = args[2];
                if (name.equalsIgnoreCase("player")) {
                    name = p.getName();
                } else {
                    name = args[2].replace("name=", "");
                }
                topPlace = schematicTop.getTopName().getOrDefault(name, -404);
                if (topPlace == -404) {
                    return falseString;
                }
            }
            PlayerDataSchematic playerData = schematicTop.getTop().get(topPlace);
            if (playerData == null) {
                if (args[3].equalsIgnoreCase("name")) {
                    return notClaimed(p);
                }
                return "";
            }
            switch (args[3].toLowerCase()) {
                case "name": {
                    return playerData.getName();
                }
                case "top": {
                    return "" + topPlace;
                }
                case "accuracy": {
                    return "" + playerData.getAccuracy();
                }
                case "time": {
                    return playerData.getTime();
                }
            }
        }
        return null;
    }

    private String player(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        Game game = gameManager.getGame(uuid);
        if (args[0].equalsIgnoreCase("active")) {
            return game != null ? trueString : falseString;
        } else if (game != null) {
            MemberData memberData = game.getMemberDataMap().get(uuid);
            SchematicMemberData schematicMemberData = memberData.getSchematicMemberData();
            switch (args[0].toLowerCase()) {
                case "status": {
                    return memberData.getStatus();
                }
                case "id": {
                    return game.getMap();
                }
                case "map": {
                    return game.getMapName();
                }
                case "lefts": {
                    return memberData.getLefts() + "";
                }
                case "rights": {
                    return memberData.getRights() + "";
                }
                case "maxlefts": {
                    return schematicMemberData.getMaxLefts() + "";
                }
                case "maxrights": {
                    return schematicMemberData.getMaxRights() + "";
                }
                default: {
                    return "status, id, map, tops, lefts, rights, maxLefts, maxRights";
                }
            }
        }
        return null;
    }

    private String notClaimed(Player p) {
        String text = values.getNotClaimed().getOrDefault(utils.getLocale().player(p), values.getNotClaimed().get(""));
        return values.getColorizer().colorize(text);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "JParkour";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mitriyf";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.7";
    }
}
