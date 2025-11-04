package ru.mitriyf.jparkour.game.temp.data;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.temp.task.stand.StandActive;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.data.SchematicData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LocationsData {
    @Getter
    private final Map<Location, StandActive> stands = new HashMap<>();
    private final BukkitScheduler scheduler;
    private final CountDownLatch latch;
    private final SchematicData info;
    private final JParkour plugin;
    private final boolean dev;
    private final String name;
    private final Utils utils;
    private final Game game;
    @Getter
    private Location defaultLocation, start, spawn, portal, end;

    public LocationsData(JParkour plugin, Game game, CountDownLatch latch, boolean dev) {
        this.plugin = plugin;
        this.utils = plugin.getUtils();
        this.game = game;
        this.latch = latch;
        this.dev = dev;
        scheduler = plugin.getServer().getScheduler();
        info = game.getInfo();
        name = game.getName();
        generateLocation();
    }

    @SuppressWarnings("deprecation")
    private void generateLocation() {
        game.getScheduler().runTask(plugin, () -> {
            World w = plugin.getServer().getWorld(name);
            if (w == null) {
                w = game.generateWorld(name);
            }
            for (Entity e : w.getEntities()) {
                if (!(e instanceof Player) && !(e instanceof ItemFrame)) {
                    e.remove();
                }
            }
            if (info != null) {
                for (String s : info.getGameRules()) {
                    String[] gameRule = s.split(":");
                    w.setGameRuleValue(gameRule[0], gameRule[1]);
                }
                defaultLocation = w.getBlockAt(info.getX(), info.getY(), info.getZ()).getLocation();
                defaultLocation.setYaw(info.getYaw());
                defaultLocation.setPitch(info.getNorth());
            } else {
                defaultLocation = w.getBlockAt(0, 100, 0).getLocation();
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (Exception ignored) {
        }
        setLocations();
        if (info != null) {
            game.paste(defaultLocation, info.getSchematic());
            scheduler.runTask(plugin, () -> game.setTrigger(!dev ? portal.getBlock().getType() : null));
        } else {
            scheduler.runTask(plugin, () -> defaultLocation.getBlock().setType(Material.STONE));
        }
    }

    private void setLocations() {
        if (info != null) {
            start = getLocation(info.getStart());
            spawn = getLocation(info.getSpawn());
            portal = getLocation(info.getPortal());
            end = getLocation(info.getEnd());
            World world = defaultLocation.getWorld();
            scheduler.runTask(plugin, () -> {
                for (Map.Entry<double[], String> stand : info.getStands().entrySet()) {
                    stands.put(getLocation(stand.getKey()), new StandActive(utils, game, world, stand.getValue()));
                }
            });
        } else {
            start = defaultLocation;
            spawn = defaultLocation;
            portal = defaultLocation;
            end = defaultLocation;
        }
    }

    public Location getLocation(double[] coords) {
        return defaultLocation.clone().add(coords[0], coords[1], coords[2]);
    }
}
