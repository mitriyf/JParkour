package ru.mitriyf.jparkour.game.temp.task;

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
import ru.mitriyf.jparkour.game.temp.data.LocationsData;
import ru.mitriyf.jparkour.game.temp.task.data.StandActive;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;
import ru.mitriyf.jparkour.values.data.schematic.point.SchematicPoint;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class Run {
    private final double radiusFinish, radiusSquared, speedDown, speedUp, checkDown, speed;
    @Getter
    private final Map<Location, StandActive> stands = new HashMap<>();
    @Getter
    private final Map<Location, BukkitTask> bombs = new HashMap<>();
    private final Map<Integer, Location> points = new HashMap<>();
    private final boolean checkUpBlock, resetVectorFinish;
    private final int damageBomb, forward, timer;
    private final BukkitScheduler scheduler;
    private final Location start, end;
    private final SchematicData info;
    private final LocationsData locs;
    private final long everyTicks;
    private final JParkour plugin;
    private final EntityType type;
    private final Values values;
    private final Game game;
    private final Player p;
    @Getter
    private LivingEntity chicken;
    @Getter
    @Setter
    private BukkitTask task;

    public Run(Game game) {
        this.plugin = game.getPlugin();
        this.game = game;
        this.values = plugin.getValues();
        this.locs = game.getLocs();
        this.p = game.getPlayer();
        info = game.getInfo();
        start = locs.getStart();
        end = locs.getEnd();
        speed = info.getSpeed();
        type = info.getEntity();
        timer = info.getTimer();
        speedUp = info.getSpeedUp();
        forward = info.getForward();
        checkUpBlock = info.isCheckUpBlock();
        checkDown = info.getCheckDown();
        speedDown = info.getSpeedDown();
        damageBomb = info.getDamageBomb();
        radiusFinish = info.getRadiusFinish();
        double radiusStands = info.getRadiusStands();
        radiusSquared = radiusStands * radiusStands;
        everyTicks = info.getEveryTicks();
        scheduler = plugin.getServer().getScheduler();
        resetVectorFinish = info.isResetVectorFinish();
    }

    public void startMove() {
        chicken = (LivingEntity) start.getWorld().spawnEntity(start, type);
        chicken.setPassenger(p);
        game.sendMessage(values.getMStarted(), info.getStarted(), game.getSearchGame(), new String[]{game.getName()});
        move(0);
    }

    private void move(int i) {
        if (i == 0) {
            points.putAll(locs.getPoints());
        }
        int point = i + 1;
        Location location = points.get(point);
        if (location == null) {
            moveToLocation(i, point, end, radiusFinish);
            points.remove(i);
            return;
        }
        moveToLocation(i, point, location, info.getPoints().get(point).getRadiusStartPoint());
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
                    game.finish();
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
        SchematicPoint point = info.getPoints().get(pointInt);
        if (point != null) {
            Location location = points.get(pointInt);
            points.remove(i);
            if (point.isTeleport()) {
                p.teleport(location);
                chicken.teleport(location);
                scheduler.runTaskLater(plugin, () -> {
                    if (chicken.getPassenger() == null) {
                        chicken.setPassenger(p);
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
        for (Location standLoc : locs.getStands().keySet()) {
            if (!stands.containsKey(standLoc) && standLoc.distanceSquared(loc) <= radiusSquared) {
                StandActive stand = locs.getStands().get(standLoc);
                stand.teleport(standLoc);
                stands.put(standLoc, stand);
                if (stand.isBomb()) {
                    startBombTask(standLoc, stand);
                }
            }
        }
    }

    private void startBombTask(Location standLoc, StandActive stand) {
        bombs.put(standLoc, scheduler.runTaskLater(game.getPlugin(), () -> {
            game.sendMessage(game.getValues().getDamageHeart(), info.getDamageHeart());
            stand.teleportToSpawn();
            double health = p.getHealth() - damageBomb;
            if (health == 0) {
                scheduler.runTask(plugin, game::restart);
            } else {
                p.setHealth(health);
            }
            bombs.remove(standLoc);
        }, timer));
    }

    private boolean isSolid(Location loc) {
        return loc.getBlock().getType().isSolid();
    }
}
