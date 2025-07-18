package ru.mitriyf.jparkour.supports;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.game.Game;

public class Placeholder extends PlaceholderExpansion {
    private final Values values;

    public Placeholder(JParkour plugin) {
        values = plugin.getValues();
    }
    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        String[] args = ind.split("_");
        if (args.length >= 1) {
            String tru = PlaceholderAPIPlugin.booleanTrue();
            String fals = PlaceholderAPIPlugin.booleanFalse();
            String gameId;
            Game game = null;
            if (values.getPlayers().containsKey(p.getUniqueId())) {
                gameId = values.getPlayers().get(p.getUniqueId()).getGame();
                game = values.getRooms().get(gameId);
            }
            if (args[0].equalsIgnoreCase("active")) {
                return game != null ? tru : fals;
            } else if (game != null) {
                switch (args[0].toLowerCase()) {
                    case "status":
                        return game.getStatus();
                    case "id":
                        return game.getMap();
                    case "map":
                        return game.getMapName();
                    case "lefts":
                        return game.getLefts() + "";
                    case "rights":
                        return game.getRights() + "";
                    case "maxlefts":
                        return game.getMaxLefts() + "";
                    case "maxrights":
                        return game.getMaxRights() + "";
                    default:
                        return "status, id, map, lefts, rights, maxLefts, maxRights";
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
