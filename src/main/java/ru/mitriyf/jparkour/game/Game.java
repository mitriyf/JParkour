package ru.mitriyf.jparkour.game;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.manager.Manager;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.info.PlayerData;
import ru.mitriyf.jparkour.values.info.SchematicData;
import ru.mitriyf.jparkour.values.info.StandData;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game {
    private final JParkour plugin;
    private final Utils utils;
    private final Values values;
    private final Manager manager;
    private final Player p;
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Map<Location, Set<ArmorStand>> stands = new HashMap<>();
    private final Set<String> actives = new HashSet<>();
    private final Map<Location, BukkitTask> bombs = new HashMap<>();
    private final String name;
    private final String map;
    private final String mapName;
    private final SchematicData info;
    private final EntityType type;
    private final double speed;
    private final double radiusFinish;
    private final double radiusStands;
    private final double loss;
    private final int health;
    private final int damageBomb;
    private final int timer;
    private BukkitTask run;
    private Location default_loc, oldLoc;
    private BukkitWorld world;
    private Material trigger;
    private LivingEntity chicken;
    private boolean started;
    private String status;
    @Setter private int lefts, rights;
    private final int maxLefts, maxRights;
    public Game(JParkour plugin, Player p, String mapId, String name) {
        this.plugin = plugin;
        this.utils = plugin.getUtils();
        this.p = p;
        this.values = plugin.getValues();
        this.manager = plugin.getManager();
        this.name = name;
        map = setMap(mapId);
        info = values.getSchematics().get(map);
        mapName = info.getName();
        type = info.getEntity();
        speed = info.getSpeed();
        radiusFinish = info.getRadiusFinish();
        radiusStands = info.getRadiusStands();
        loss = info.getLoss();
        health = info.getHealth();
        damageBomb = info.getDamageBomb();
        timer = info.getTimer();
        status = values.getSWait();
        maxLefts = info.getMaxLefts();
        maxRights = info.getMaxRights();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(new File(name))));
        generateLocation();
        addPlayer(p);
    }
    public void addPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        values.getPlayers().put(uuid, new PlayerData(plugin, p, name));
        if (!p.teleport(getSpawn())) {
            values.getPlayers().remove(uuid);
            close();
            return;
        }
        setDefault();
    }
    public void kickPlayer() {
        UUID uuid = p.getUniqueId();
        values.getPlayers().get(uuid).apply();
        values.getPlayers().remove(uuid);
        for (String msg : values.getKicked()) sendMessage(p, msg);
    }
    public void start() {
        clear();
        for (Map.Entry<Integer, ItemStack> d : info.getSlots().entrySet()) p.getInventory().setItem(d.getKey(), d.getValue());
        started = true;
        Location st = getStart();
        st.setYaw(info.getYaw());
        st.setPitch(info.getNorth());
        p.teleport(st);
        chicken = (LivingEntity) st.getWorld().spawnEntity(st, type);
        chicken.setPassenger(p);
        status = values.getSStart();
        startMove();
    }
    private void win() {
        status = values.getSWin();
        p.setAllowFlight(true);
        p.setFlying(true);
        List<String> win = new ArrayList<>();
        int stars = info.getStars(lefts, rights);
        if (info.getStar().isEmpty()) {
            for (String s : values.getWin()) {
                win.add(s.replace("%star_win%", stars + "")
                        .replace("%star_loss%", "5"));
            }
        } else {
            String star = info.getStar();
            String fill = utils.repeat(star, stars);
            String empty = utils.repeat(star, 5 - stars);
            for (String s : values.getWin()) {
                win.add(s.replace("%star_win%", fill)
                        .replace("%star_loss%", empty));
            }
        }
        sendMessage(p, win);
        tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, this::close, 100));
    }
    private void sendMessage(Player p, String msg) {
        utils.sendMessage(p, msg);
    }
    private void sendMessage(Player p, List<String> msg) {
        utils.sendMessage(p, msg);
    }
    private void generateLocation() {
        manager.getUnloaded().remove(name);
        World w = Bukkit.getWorld(name);
        if (w == null) {
            w = new WorldCreator(name)
                    .type(WorldType.FLAT)
                    .generatorSettings("2;0;1;")
                    .createWorld();
        }
        w.setGameRuleValue("randomTickSpeed", "0");
        world = new BukkitWorld(w);
        p.getWorld().getEntities().stream()
                .filter(e -> !(e instanceof Player) && !(e instanceof ItemFrame))
                .forEach(Entity::remove);
        Block b = w.getBlockAt(info.getX(), info.getY(), info.getZ());
        default_loc = b.getLocation();
        default_loc.setYaw(info.getYaw());
        default_loc.setPitch(info.getNorth());
        utils.paste(default_loc, info.getSchematic());
        trigger = getPortal().getBlock().getType();
    }
    public void restart() {
        clear();
        setDefault();
        started = false;
        status = values.getSWait();
        p.teleport(getSpawn());
    }
    public void close() {
        clear();
        kickPlayer();
        Bukkit.unloadWorld(name, false);
        values.getRooms().remove(name);
    }
    private void startMove() {
        Location end = getEnd();
        run = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Location loc = chicken.getLocation();
            if (chicken.getPassenger() == null) {
                restart();
                return;
            }
            if (loc.distance(end) <= radiusFinish) {
                win();
                run.cancel();
                return;
            }
            Vector direction = end.toVector().subtract(loc.toVector()).normalize();
            Location checkForward = loc.clone().add(direction.clone().multiply(1.5));
            Location checkUp = checkForward.clone().add(0, 1, 0);
            for (Map.Entry<double[], String> co : info.getStands().entrySet()) {
                Location l = getStand(co.getKey());
                if (!stands.containsKey(l) && l.distance(loc) <= radiusStands) {
                    StandData data = values.getStands().get(co.getValue());
                    stands.put(l, new HashSet<>());
                    ArmorStand big = data.generateBigStand(l);
                    ArmorStand small = data.generateSmallStand(l);
                    if (big.getChestplate().getType() == Material.TNT) bombs.put(l, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        big.remove();
                        small.remove();
                        p.getWorld().createExplosion(l, 1);
                        p.setHealth(p.getHealth() - damageBomb);
                        p.playSound(p.getLocation(), Sound.HURT_FLESH, 1, 1);
                    }, timer));
                    stands.get(l).add(big);
                    stands.get(l).add(small);
                }
            }
            boolean obstacleAhead = checkForward.getBlock().getType().isSolid() ||
                    checkUp.getBlock().getType().isSolid() ||
                    loc.getBlock().getRelative(BlockFace.UP).getType().isSolid() ||
                    oldLoc != null && oldLoc.distance(loc) < 0.05;
            boolean canDescend = !obstacleAhead &&
                    end.getY() <= loc.getY() &&
                    !loc.clone().subtract(0, 0.1, 0).getBlock().getType().isSolid() &&
                    !loc.clone().add(direction).subtract(0, 0.1, 0).getBlock().getType().isSolid();
            oldLoc = loc.clone();
            if (obstacleAhead) direction.add(new Vector(0, 0.5, 0)).normalize();
            else if (canDescend) direction.add(new Vector(0, -0.8, 0)).normalize();
            chicken.setVelocity(direction.multiply(speed));
        }, 0L, 1L);
    }
    private Location getLocation(double[] coords, boolean centerXZ) {
        double x = coords[0] + (centerXZ ? 0.5 : 0);
        double z = coords[2] + (centerXZ ? 0.5 : 0);
        return default_loc.clone().add(x, coords[1], z);
    }
    private Location getStart() {
        return getLocation(info.getStart(), true);
    }
    public Location getSpawn() {
        return getLocation(info.getSpawn(), false);
    }
    private Location getPortal() {
        return getLocation(info.getPortal(), false);
    }
    private Location getEnd() {
        return getLocation(info.getEnd(), true);
    }
    private Location getStand(double[] coords) {
        return getLocation(coords, true);
    }
    private String setMap(String mapId) {
        return mapId != null ? mapId :
                values.getMaps().get(rnd.nextInt(values.getMaps().size()));
    }
    private void setDefault() {
        p.setGameMode(GameMode.ADVENTURE);
        p.setMaxHealth(health);
        p.setHealthScale(health);
        p.setHealth(health);
        p.setFoodLevel(10);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        values.getSlots().forEach((slot, item) -> p.getInventory().setItem(slot, item));
    }
    private void clear() {
        if (run != null) {
            run.cancel();
            chicken.remove();
        }
        lefts = 0;
        rights = 0;
        stands.values().forEach(set -> set.forEach(ArmorStand::remove));
        bombs.values().forEach(BukkitTask::cancel);
        tasks.forEach(BukkitTask::cancel);
        stands.clear();
        bombs.clear();
        tasks.clear();
        actives.clear();
    }
}
