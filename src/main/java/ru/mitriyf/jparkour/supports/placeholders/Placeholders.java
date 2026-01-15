package ru.mitriyf.jparkour.supports.placeholders;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.supports.tops.Tops;
import ru.mitriyf.jparkour.supports.tops.schematic.Schematic;
import ru.mitriyf.jparkour.supports.tops.schematic.player.PlayerDataSchematic;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

public class Placeholders extends PlaceholderExpansion {
    private final String falseString = PlaceholderAPIPlugin.booleanFalse();
    private final String trueString = PlaceholderAPIPlugin.booleanTrue();
    private final Manager manager;
    private final Values values;
    private final Utils utils;
    private final Tops tops;

    public Placeholders(JParkour plugin) {
        manager = plugin.getManager();
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
            Schematic schematic = tops.getSchematic().get(args[1]);
            if (schematic == null) {
                return "Schematic not found.";
            }
            int topPlace;
            try {
                topPlace = Integer.parseInt(args[2]);
            } catch (Exception e) {
                String name = args[2].replace("name=", "");
                topPlace = schematic.getTopName().getOrDefault(name, -404);
                if (topPlace == -404) {
                    return falseString;
                }
            }
            PlayerDataSchematic playerData = schematic.getTop().get(topPlace);
            if (playerData == null) {
                if (args[3].equalsIgnoreCase("name")) {
                    String text = values.getNotClaimed().getOrDefault(utils.getLocale().player(p), values.getNotClaimed().get(""));
                    return values.getColorizer().colorize(text);
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

    private String player(Player p, String[] args) {
        Game game = manager.getGame(p.getUniqueId());
        if (args[0].equalsIgnoreCase("active")) {
            return game != null ? trueString : falseString;
        } else if (game != null) {
            switch (args[0].toLowerCase()) {
                case "status": {
                    return game.getStatus();
                }
                case "id": {
                    return game.getMap();
                }
                case "map": {
                    return game.getMapName();
                }
                case "lefts": {
                    return game.getLefts() + "";
                }
                case "rights": {
                    return game.getRights() + "";
                }
                case "maxlefts": {
                    return game.getMaxLefts() + "";
                }
                case "maxrights": {
                    return game.getMaxRights() + "";
                }
                default: {
                    return "status, id, map, tops, lefts, rights, maxLefts, maxRights";
                }
            }
        }
        return null;
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
        return "1.6";
    }
}
