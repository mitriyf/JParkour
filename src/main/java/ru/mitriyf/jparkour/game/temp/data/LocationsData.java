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
import ru.mitriyf.jparkour.game.temp.task.data.StandActive;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;
import ru.mitriyf.jparkour.values.data.schematic.point.SchematicPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LocationsData {
    @Getter
    private final Map<Location, StandActive> stands = new HashMap<>();
    @Getter
    private final Map<Integer, Location> points = new HashMap<>();
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
        this.latch = latch;
        this.game = game;
        this.dev = dev;
        scheduler = game.getScheduler();
        utils = plugin.getUtils();
        info = game.getInfo();
        name = game.getName();
        generateLocation();
    }

    @SuppressWarnings("deprecation")
    private void generateLocation() {
        scheduler.runTask(plugin, () -> {
            World w = plugin.getServer().getWorld(name);
            if (w == null) {
                w = utils.getWorldGenerator().generateWorld(name);
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
                defaultLocation.setPitch(info.getPitch());
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
            utils.paste(defaultLocation, info.getSchematic(), info.isPasteAir());
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
            for (Map.Entry<Integer, SchematicPoint> point : info.getPoints().entrySet()) {
                SchematicPoint schematicPoint = point.getValue();
                Location location = getLocation(schematicPoint.getLocation());
                if (schematicPoint.isTeleport()) {
                    location.setYaw(schematicPoint.getYaw());
                    location.setPitch(schematicPoint.getPitch());
                }
                points.put(point.getKey(), location);
            }
        } else {
            start = defaultLocation;
            spawn = defaultLocation;
            portal = defaultLocation;
            end = defaultLocation;
        }
    }

    public void generateStands() {
        Location loc = spawn.clone().add(0, 100400, 0);
        for (Map.Entry<double[], String> stand : info.getStands().entrySet()) {
            stands.put(getLocation(stand.getKey()), new StandActive(utils, game, loc, stand.getValue()));
        }
    }

    public Location getLocation(double[] coords) {
        return defaultLocation.clone().add(coords[0], coords[1], coords[2]);
    }
}
