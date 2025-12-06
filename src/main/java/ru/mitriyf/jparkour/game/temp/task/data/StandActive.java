package ru.mitriyf.jparkour.game.temp.task.data;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.data.StandData;

import java.util.UUID;

public class StandActive {
    private final UUID big, small;
    @Getter
    private final boolean bomb;
    private final Utils utils;
    private final Location standLocation;
    @Getter
    private final ItemStack trigger;
    private final World world;
    @Getter
    private final String type;

    public StandActive(Utils utils, Game game, Location standLocation, String type) {
        this.utils = utils;
        this.standLocation = standLocation;
        this.type = type;
        world = standLocation.getWorld();
        StandData data = game.getStands().get(type);
        bomb = data.isBomb();
        trigger = data.getItem();
        small = data.generateSmallStand(standLocation);
        big = data.generateBigStand(standLocation);
    }

    public void teleport(Location loc) {
        Entity bigStand = getEntity(big);
        Entity smallStand = getEntity(small);
        Location bigLocation = loc.clone().add(0, -0.75, 0);
        bigStand.teleport(bigLocation);
        smallStand.teleport(loc);
    }

    public void teleportToSpawn() {
        Entity bigStand = getEntity(big);
        Entity smallStand = getEntity(small);
        bigStand.teleport(standLocation);
        smallStand.teleport(standLocation);
    }

    public ArmorStand get(UUID uuid) {
        return uuid == big || uuid == small ? (ArmorStand) utils.getEntity(world, big) : null;
    }

    private Entity getEntity(UUID uuid) {
        return utils.getEntity(world, uuid);
    }

    public void close() {
        getEntity(big).remove();
        getEntity(small).remove();
    }
}
