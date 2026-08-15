package ru.mitriyf.jparkour.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LocationsData {
    private final BukkitScheduler scheduler;
    private final boolean dev, infoExists;
    private final CountDownLatch latch;
    private final SchematicData info;
    private final JParkour plugin;
    private final String name;
    private final Utils utils;
    private final Game game;
    @Getter
    private Location spawn, portal;
    @Getter
    @Setter
    private Location defaultLocation;

    public LocationsData(JParkour plugin, Game game, CountDownLatch latch, boolean dev) {
        this.plugin = plugin;
        this.latch = latch;
        this.game = game;
        this.dev = dev;
        scheduler = game.getScheduler();
        utils = plugin.getUtils();
        info = game.getInfo();
        infoExists = game.isInfoExists();
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
            if (infoExists) {
                for (String s : info.getGameRules()) {
                    String[] gameRule = s.split(":");
                    w.setGameRuleValue(gameRule[0], gameRule[1]);
                }
                defaultLocation = w.getBlockAt(info.getX(), info.getY(), info.getZ()).getLocation();
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
        if (infoExists) {
            utils.paste(defaultLocation, info.getSchematic(), info.isPasteAir());
            scheduler.runTask(plugin, () -> game.setTrigger(!dev ? portal.getBlock().getType() : null));
        } else {
            scheduler.runTask(plugin, () -> defaultLocation.getBlock().setType(Material.STONE));
        }
    }

    private void setLocations() {
        if (infoExists) {
            spawn = getLocation(info.getSpawn());
            portal = getLocation(info.getPortal());
        } else {
            spawn = defaultLocation;
            portal = defaultLocation;
        }
    }

    public void generateStands(SchematicMemberData schematicMemberData, MemberData memberData) {
        Location loc = spawn.clone().add(0, Integer.MAX_VALUE, 0);
        for (Map.Entry<double[], String> stand : schematicMemberData.getStands().entrySet()) {
            memberData.getStands().put(getLocation(stand.getKey()), new StandActiveData(utils, game, loc, stand.getValue()));
        }
    }

    public Location getLocation(double[] coords) {
        Location location = null;
        try {
            location = defaultLocation.clone().add(coords[0], coords[1], coords[2]);
            location.setYaw((float) coords[3]);
            location.setPitch((float) coords[4]);
        } catch (Exception e) {
            plugin.getLogger().warning("An error occurred while receiving the location. Make sure everything is in this format: X;Y;Z;YAW;PITCH");
            if (location == null) {
                location = defaultLocation.clone();
            }
        }
        return location;
    }
}
