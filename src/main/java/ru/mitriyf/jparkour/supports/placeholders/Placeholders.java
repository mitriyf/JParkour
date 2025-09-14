package ru.mitriyf.jparkour.supports.placeholders;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.supports.tops.Tops;
import ru.mitriyf.jparkour.supports.tops.schematic.player.SPlayerData;
import ru.mitriyf.jparkour.values.Values;

public class Placeholders extends PlaceholderExpansion {
    private final Manager manager;
    private final Values values;
    private final Tops tops;

    public Placeholders(JParkour plugin) {
        manager = plugin.getManager();
        values = plugin.getValues();
        tops = plugin.getSupports().getTops();
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        String[] args = ind.split("_");
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("tops")) {
                return tops(args);
            }
            return player(p, args);
        }
        return null;
    }

    private String tops(String[] args) {
        if (args.length == 4) {
            int i = Integer.parseInt(args[2]);
            if (tops.getSchematic().get(args[1]) == null) {
                return "Schematic not found.";
            }
            SPlayerData playerData = tops.getSchematic().get(args[1]).getTop().get(i);
            if (playerData == null) {
                return "Not claimed.";
            }
            switch (args[3].toLowerCase()) {
                case "name": {
                    return playerData.getName();
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
        String tru = PlaceholderAPIPlugin.booleanTrue();
        String fals = PlaceholderAPIPlugin.booleanFalse();
        Game game = null;
        String gameId;
        if (manager.getPlayers().containsKey(p.getUniqueId())) {
            gameId = manager.getPlayers().get(p.getUniqueId()).getGame();
            game = values.getRooms().get(gameId);
        }
        if (args[0].equalsIgnoreCase("active")) {
            return game != null ? tru : fals;
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
        return "1.0";
    }
}
