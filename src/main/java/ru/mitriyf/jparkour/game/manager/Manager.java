package ru.mitriyf.jparkour.game.manager;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.temp.data.PlayerData;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Manager {
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;
    private final ThreadLocalRandom rnd;
    private final BukkitScheduler scheduler;
    private final String[] search = {"%room%"};
    @Getter
    private final Set<UUID> waiters = new HashSet<>();
    @Getter
    private final Set<String> confirmation = new HashSet<>();
    @Getter
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    @Getter
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public Manager(JParkour plugin) {
        this.plugin = plugin;
        rnd = plugin.getRnd();
        utils = plugin.getUtils();
        values = plugin.getValues();
        scheduler = plugin.getServer().getScheduler();
    }

    public void join(Player p, String mapId, boolean dev) {
        if (players.containsKey(p.getUniqueId()) || waiters.contains(p.getUniqueId())) {
            utils.sendMessage(p, values.getInGame());
            return;
        } else if (mapId != null && !dev) {
            if (values.getSchematics().isEmpty() || !values.getSchematics().containsKey(mapId)) {
                utils.sendMessage(p, values.getNotfound());
                return;
            }
        }
        generateRoom(p, mapId, dev);
    }

    private void generateRoom(Player p, String mapId, boolean dev) {
        UUID uuid = p.getUniqueId();
        int amount = values.getAmount();
        String name = values.getWorldStart() + (dev ? "E" : "") + (amount < 1 ? 1 : rnd.nextInt(amount));
        if (amount > 0 && !values.getRooms().containsKey(name)) {
            tasks.remove(uuid);
            String[] replace = {name};
            utils.sendMessage(p, values.getConnect(), search, replace);
            CountDownLatch latch = new CountDownLatch(1);
            values.getRooms().put(name, null);
            waiters.add(uuid);
            scheduler.runTaskAsynchronously(plugin, () -> values.getRooms().put(name, new Game(plugin, latch, p, mapId, name, dev)));
        } else {
            tasks.put(uuid, scheduler.runTaskLater(plugin, () -> {
                if (!waiters.contains(uuid)) {
                    waiters.add(uuid);
                    utils.sendMessage(p, values.getWaiter());
                }
                generateRoom(p, mapId, dev);
            }, 10));
        }
    }

    public Game getGame(UUID uuid) {
        PlayerData data = players.get(uuid);
        if (data != null) {
            String id = data.getGame();
            return values.getRooms().get(id);
        }
        return null;
    }

    public Game getGame(String world) {
        return values.getRooms().get(world);
    }
}
