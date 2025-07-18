package ru.mitriyf.jparkour.manager;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Manager {
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;
    private final ThreadLocalRandom rnd;
    private final Set<UUID> waiters = new HashSet<>();
    private final List<String> unloaded = new ArrayList<>();
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    public Manager(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
        this.rnd = plugin.getRnd();
    }
    public void join(Player p, String mapId) {
        if (values.getPlayers().containsKey(p.getUniqueId())) {
            utils.sendMessage(p, values.getInGame());
            return;
        } else if (mapId != null && !values.getSchematics().containsKey(mapId)) {
            utils.sendMessage(p, values.getNotfound());
            return;
        }
        generateRoom(p, mapId);
    }
    public void generateRoom(Player p, String mapId) {
        UUID uuid = p.getUniqueId();
        String name = values.getWorldStart() + rnd.nextInt(values.getAmount());
        if (!values.getRooms().containsKey(name)) {
            waiters.remove(uuid);
            tasks.remove(uuid);
            utils.sendMessage(p, values.getConnect().replace("%room%", name));
            values.getRooms().put(name, new Game(plugin, p, mapId, name));
        }
        else {
            tasks.put(uuid, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!waiters.contains(uuid)) {
                    waiters.add(uuid);
                    utils.sendMessage(p, values.getWaiter());
                }
                generateRoom(p, mapId);
            }, 2));
        }
    }
}
