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
import ru.mitriyf.jparkour.game.temp.task.stand.StandActive;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.SchematicData;

import java.util.HashMap;
import java.util.Map;

public class Run {
    private final double radiusFinish, radiusStands, speedDown, speedUp, checkDown;
    @Getter
    private final Map<Location, StandActive> stands = new HashMap<>();
    @Getter
    private final Map<Location, BukkitTask> bombs = new HashMap<>();
    private final Map<Integer, Location> points = new HashMap<>();
    private final boolean checkUpBlock, resetVectorFinish;
    private final BukkitScheduler scheduler;
    private final int damageBomb, forward;
    private final SchematicData info;
    private final EntityType type;
    private final long everyTicks;
    private final Location start;
    private final JParkour plugin;
    private final Location end;
    private final double speed;
    private final int timer;
    private final Player p;
    private final LocationsData locs;
    private final Values values;
    private final Game game;
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
        radiusStands = info.getRadiusStands();
        radiusFinish = info.getRadiusFinish();
        everyTicks = info.getEveryTicks();
        scheduler = plugin.getServer().getScheduler();
        resetVectorFinish = info.isResetVectorFinish();
    }

    @SuppressWarnings("deprecation")
    public void startMove() {
        chicken = (LivingEntity) start.getWorld().spawnEntity(start, type);
        chicken.setPassenger(p);
        game.sendMessage(values.getMStarted(), info.getStarted(), game.getSearchGame(), new String[]{game.getName()});
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
                game.finish();
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

    private boolean isSolid(Location loc) {
        return loc.getBlock().getType().isSolid();
    }

    private void searchStands(Location loc) {
        for (Location standLoc : locs.getStands().keySet()) {
            if (!stands.containsKey(standLoc) && standLoc.distance(loc) <= radiusStands) {
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
}
