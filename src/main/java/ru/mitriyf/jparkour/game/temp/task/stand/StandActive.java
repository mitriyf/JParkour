package ru.mitriyf.jparkour.game.temp.task.stand;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.data.StandData;

import java.util.UUID;

public class StandActive {
    private final Location standLocation;
    private final UUID big, small;
    @Getter
    private final boolean bomb;
    private final Utils utils;
    private final World world;
    @Getter
    private final String type;

    public StandActive(Utils utils, Game game, World world, String type) {
        this.utils = utils;
        this.world = world;
        this.type = type;
        standLocation = world.getBlockAt(0, 1000, 0).getLocation();
        StandData data = game.getStands().get(type);
        bomb = data.isBomb();
        big = data.generateBigStand(standLocation);
        small = data.generateSmallStand(standLocation);
    }

    public void teleport(Location loc) {
        Entity bigStand = utils.getEntity(world, big);
        Entity smallStand = utils.getEntity(world, small);
        Location bigLocation = loc.clone().add(0, -0.75, 0);
        bigStand.teleport(bigLocation);
        smallStand.teleport(loc);
    }

    public void teleportToSpawn() {
        Entity bigStand = utils.getEntity(world, big);
        Entity smallStand = utils.getEntity(world, small);
        bigStand.teleport(standLocation);
        smallStand.teleport(standLocation);
    }

    public boolean contains(UUID uuid) {
        return big == uuid || small == uuid;
    }

    public void close() {
        utils.getEntity(world, big).remove();
        utils.getEntity(world, small).remove();
    }
}
