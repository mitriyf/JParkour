package ru.mitriyf.jparkour.game.manager;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.info.PlayerData;

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
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    @Getter
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public Manager(JParkour plugin) {
        this.plugin = plugin;
        this.rnd = plugin.getRnd();
        this.scheduler = plugin.getServer().getScheduler();
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
    }

    public void join(Player p, String mapId) {
        if (players.containsKey(p.getUniqueId())) {
            utils.sendMessage(p, values.getInGame());
            return;
        } else if (mapId != null && !values.getSchematics().containsKey(mapId)) {
            utils.sendMessage(p, values.getNotfound());
            return;
        }
        generateRoom(p, mapId);
    }

    private void generateRoom(Player p, String mapId) {
        UUID uuid = p.getUniqueId();
        String name = values.getWorldStart() + rnd.nextInt(values.getAmount());
        if (!values.getRooms().containsKey(name)) {
            waiters.remove(uuid);
            tasks.remove(uuid);
            String[] replace = {name};
            utils.sendMessage(p, values.getConnect(), search, replace);
            CountDownLatch latch = new CountDownLatch(1);
            values.getRooms().put(name, null);
            scheduler.runTaskAsynchronously(plugin, () -> values.getRooms().put(name, new Game(plugin, latch, p, mapId, name)));
        } else {
            tasks.put(uuid, scheduler.runTaskLater(plugin, () -> {
                if (!waiters.contains(uuid)) {
                    waiters.add(uuid);
                    utils.sendMessage(p, values.getWaiter());
                }
                generateRoom(p, mapId);
            }, 2));
        }
    }
}
