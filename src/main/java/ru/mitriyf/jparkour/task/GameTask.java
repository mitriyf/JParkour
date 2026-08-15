package ru.mitriyf.jparkour.task;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.model.*;
import ru.mitriyf.jparkour.values.Values;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class GameTask {
    private final double radiusFinish, radiusSquared, speedDown, speedUp, checkDown, speed;
    @Getter
    private final Map<Location, StandActiveData> stands = new HashMap<>();
    @Getter
    private final Map<Location, BukkitTask> bombs = new HashMap<>();
    private final Map<Integer, Location> points = new HashMap<>();
    private final boolean checkUpBlock, resetVectorFinish;
    private final SchematicMemberData schematicMemberData;
    private final int damageBomb, forward, timer;
    private final BukkitScheduler scheduler;
    private final MemberData memberData;
    private final Location start, end;
    private final SchematicData info;
    private final long everyTicks;
    private final JParkour plugin;
    private final EntityType type;
    private final Player player;
    private final Values values;
    private final Game game;
    @Getter
    private LivingEntity chicken;
    @Getter
    @Setter
    private BukkitTask task;

    public GameTask(Game game, Player player, MemberData memberData) {
        this.game = game;
        this.player = player;
        this.memberData = memberData;
        start = memberData.getStart();
        end = memberData.getEnd();
        plugin = game.getPlugin();
        values = plugin.getValues();
        info = game.getInfo();
        speed = info.getSpeed();
        type = info.getEntity();
        timer = info.getTimer();
        speedUp = info.getSpeedUp();
        forward = info.getForward();
        checkDown = info.getCheckDown();
        speedDown = info.getSpeedDown();
        damageBomb = info.getDamageBomb();
        checkUpBlock = info.isCheckUpBlock();
        radiusFinish = info.getRadiusFinish();
        double radiusStands = info.getRadiusStands();
        radiusSquared = radiusStands * radiusStands;
        everyTicks = info.getEveryTicks();
        scheduler = plugin.getServer().getScheduler();
        resetVectorFinish = info.isResetVectorFinish();
        schematicMemberData = memberData.getSchematicMemberData();
    }

    public void startMove() {
        chicken = (LivingEntity) start.getWorld().spawnEntity(start, type);
        chicken.setPassenger(player);
        game.sendMessage(player, values.getMStarted(), info.getStarted(), game.getSearchGame(), new String[]{game.getName()});
        move(0);
    }

    private void move(int i) {
        if (i == 0) {
            points.putAll(memberData.getPoints());
        }
        int point = i + 1;
        Location location = points.get(point);
        if (location == null) {
            moveToLocation(i, point, end, radiusFinish);
            points.remove(i);
            return;
        }
        moveToLocation(i, point, location, schematicMemberData.getPoints().get(point).getRadiusStartPoint());
    }

    private void moveToLocation(int i, int pointInt, Location end, double radiusFinish) {
        task = scheduler.runTaskTimer(plugin, () -> {
            if (chicken.getPassenger() == null) {
                task.cancel();
                game.restart();
                return;
            }
            Location loc = chicken.getLocation();
            if (loc.distance(end) <= radiusFinish) {
                task.cancel();
                if (resetVectorFinish) {
                    chicken.setVelocity(new Vector(0, 0, 0));
                }
                if (points.isEmpty()) {
                    game.finish(player);
                } else {
                    goToPoint(i, pointInt);
                }
                return;
            }
            searchStands(loc);
            Vector direction = end.toVector().subtract(loc.toVector()).normalize().setY(0);
            if (checkForward(direction, loc)) {
                direction.add(new Vector(0, speedUp, 0));
            } else if (!loc.clone().add(0, checkDown, 0).getBlock().getType().isSolid()) {
                direction.add(new Vector(0, speedDown, 0));
            }
            chicken.setVelocity(direction.multiply(speed));
        }, 0L, everyTicks);
    }

    private void goToPoint(int i, int pointInt) {
        SchematicPointData point = schematicMemberData.getPoints().get(pointInt);
        if (point != null) {
            Location location = points.get(pointInt);
            points.remove(i);
            if (point.isTeleport()) {
                player.teleport(location);
                chicken.teleport(location);
                scheduler.runTaskLater(plugin, () -> {
                    if (chicken.getPassenger() == null) {
                        chicken.setPassenger(player);
                    }
                    move(pointInt);
                }, 2);
                return;
            }
            move(pointInt);
        }
    }

    private boolean checkForward(Vector direction, Location loc) {
        for (int i = 1; i <= forward; i++) {
            Location forward = loc.clone().add(direction.clone().multiply(i));
            boolean up = checkUpBlock && isSolid(forward.clone().add(0, 1, 0));
            if (isSolid(forward) || up) {
                return true;
            }
        }
        return false;
    }

    private void searchStands(Location loc) {
        for (Location standLoc : memberData.getStands().keySet()) {
            if (!stands.containsKey(standLoc) && standLoc.distanceSquared(loc) <= radiusSquared) {
                StandActiveData stand = memberData.getStands().get(standLoc);
                stand.teleport(standLoc);
                stands.put(standLoc, stand);
                if (stand.isBomb()) {
                    startBombTask(standLoc, stand);
                }
            }
        }
    }

    private void startBombTask(Location standLoc, StandActiveData stand) {
        bombs.put(standLoc, scheduler.runTaskLater(game.getPlugin(), () -> {
            game.sendMessage(player, game.getValues().getDamageHeart(), info.getDamageHeart());
            stand.teleportToSpawn();
            double health = player.getHealth() - damageBomb;
            if (health == 0) {
                scheduler.runTask(plugin, game::restart);
            } else {
                player.setHealth(health);
            }
            bombs.remove(standLoc);
        }, timer));
    }

    private boolean isSolid(Location loc) {
        return loc.getBlock().getType().isSolid();
    }
}
