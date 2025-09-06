package ru.mitriyf.jparkour.game.actions.task;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.actions.data.Locations;
import ru.mitriyf.jparkour.values.info.SchematicData;
import ru.mitriyf.jparkour.values.info.StandData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Run {
    @Getter
    private final Map<Location, Set<ArmorStand>> stands = new HashMap<>();
    @Getter
    private final Map<Location, BukkitTask> bombs = new HashMap<>();
    private final BukkitScheduler scheduler;
    private final double radiusFinish;
    private final double radiusStands;
    private final SchematicData info;
    private final EntityType type;
    private final int damageBomb;
    private final Location start;
    private final JParkour plugin;
    private final Location end;
    private final double speed;
    private final int timer;
    private final Player p;
    private final Locations locs;
    private final Game game;
    @Getter
    private LivingEntity chicken;
    private Location oldLoc;
    @Getter
    private BukkitTask task;

    public Run(Game game) {
        this.plugin = game.getPlugin();
        this.game = game;
        this.locs = game.getLocs();
        this.p = game.getPlayer();
        info = game.getInfo();
        start = locs.getStart();
        end = locs.getEnd();
        speed = info.getSpeed();
        type = info.getEntity();
        timer = info.getTimer();
        damageBomb = info.getDamageBomb();
        radiusStands = info.getRadiusStands();
        radiusFinish = info.getRadiusFinish();
        scheduler = plugin.getServer().getScheduler();
    }

    @SuppressWarnings("deprecation")
    public void startMove() {
        chicken = (LivingEntity) start.getWorld().spawnEntity(start, type);
        chicken.setPassenger(p);
        task = scheduler.runTaskTimer(plugin, () -> {
            Location loc = chicken.getLocation();
            if (chicken.getPassenger() == null) {
                game.restart();
                return;
            }
            if (loc.distance(end) <= radiusFinish) {
                game.finish();
                task.cancel();
                return;
            }
            Vector direction = end.toVector().subtract(loc.toVector()).normalize();
            Location checkForward = loc.clone().add(direction.clone().multiply(1.5));
            Location checkUp = checkForward.clone().add(0, 1, 0);
            for (Map.Entry<double[], String> co : info.getStands().entrySet()) {
                Location l = locs.getStand(co.getKey());
                if (!stands.containsKey(l) && l.distance(loc) <= radiusStands) {
                    StandData data = game.getStands().get(co.getValue());
                    stands.put(l, new HashSet<>());
                    ArmorStand big = data.generateBigStand(l);
                    ArmorStand small = data.generateSmallStand(l);
                    if (big.getChestplate().getType() == Material.TNT) {
                        bombs.put(l, scheduler.runTaskLater(game.getPlugin(), () -> {
                            big.remove();
                            small.remove();
                            p.getWorld().createExplosion(l, 1);
                            p.setHealth(p.getHealth() - damageBomb);
                            game.sendMessage(game.getValues().getDamageHeart(), info.getDamageHeart());
                        }, timer));
                    }
                    stands.get(l).add(big);
                    stands.get(l).add(small);
                }
            }
            boolean obstacleAhead = checkForward.getBlock().getType().isSolid() || checkUp.getBlock().getType().isSolid() || loc.getBlock().getRelative(BlockFace.UP).getType().isSolid() || oldLoc != null && oldLoc.distance(loc) < 0.05;
            boolean canDescend = !obstacleAhead && end.getY() <= loc.getY() && !loc.clone().subtract(0, 0.1, 0).getBlock().getType().isSolid() && !loc.clone().add(direction).subtract(0, 0.1, 0).getBlock().getType().isSolid();
            oldLoc = loc.clone();
            if (obstacleAhead) {
                direction.add(new Vector(0, 0.5, 0)).normalize();
            } else if (canDescend) {
                direction.add(new Vector(0, -0.8, 0)).normalize();
            }
            chicken.setVelocity(direction.multiply(speed));
        }, 0L, 1L);
    }
}
