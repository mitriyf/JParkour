package ru.mitriyf.jparkour.game.temp.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.values.info.SchematicData;

import java.util.concurrent.CountDownLatch;

public class Locations {
    private final CountDownLatch latch;
    private final SchematicData info;
    private final String name;
    private final Game game;
    @Getter
    private Location start, spawn, portal, end;
    private Location default_loc;

    public Locations(Game game, CountDownLatch latch) {
        this.game = game;
        this.latch = latch;
        info = game.getInfo();
        name = game.getName();
        generateLocation();
    }

    @SuppressWarnings("deprecation")
    public void generateLocation() {
        game.getScheduler().runTask(game.getPlugin(), () -> {
            World w = Bukkit.getWorld(name);
            if (w == null) {
                w = game.generateWorld(name);
            }
            w.setGameRuleValue("randomTickSpeed", "0");
            w.getEntities().stream().filter(e -> !(e instanceof Player) && !(e instanceof ItemFrame)).forEach(Entity::remove);
            Block b = w.getBlockAt(info.getX(), info.getY(), info.getZ());
            default_loc = b.getLocation();
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        default_loc.setYaw(info.getYaw());
        default_loc.setPitch(info.getNorth());
        setLocations();
        game.paste(default_loc, info.getSchematic());
        game.setTrigger(portal.getBlock().getType());
    }

    private void setLocations() {
        start = getLocation(info.getStart(), true);
        spawn = getLocation(info.getSpawn(), false);
        portal = getLocation(info.getPortal(), false);
        end = getLocation(info.getEnd(), true);
    }

    public Location getStand(double[] coords) {
        return getLocation(coords, true);
    }

    private Location getLocation(double[] coords, boolean centerXZ) {
        double x = coords[0] + (centerXZ ? 0.5 : 0);
        double z = coords[2] + (centerXZ ? 0.5 : 0);
        return default_loc.clone().add(x, coords[1], z);
    }
}
