package ru.mitriyf.jparkour.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.game.temp.data.Locations;
import ru.mitriyf.jparkour.game.temp.task.Run;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.info.PlayerData;
import ru.mitriyf.jparkour.values.info.SchematicData;
import ru.mitriyf.jparkour.values.info.StandData;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game {
    private final JParkour plugin;
    private final Utils utils;
    private final Values values;
    private final Manager manager;
    private final Player player;
    private final UUID uuid;
    private final Run run;
    private final Locations locs;
    private final ThreadLocalRandom rnd;
    private final String[] searchGame = {"%game%"};
    private final String[] search = {"%game%", "%accuracy%", "%star_win%", "%star_loss%"};
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Set<String> actives = new HashSet<>();
    private final Map<String, StandData> stands;
    private final BukkitScheduler scheduler;
    private final int maxLefts, maxRights;
    private final boolean fullSlots;
    private final Location start;
    private final String locale;
    private final String mapName;
    private final String name;
    private final String map;
    private final SchematicData info;
    private final int health, exitTime;
    private boolean started;
    private String status;
    @Setter
    private Material trigger;
    @Setter
    private int lefts, rights;

    public Game(JParkour plugin, CountDownLatch latch, Player p, String mapId, String name) {
        this.plugin = plugin;
        this.utils = plugin.getUtils();
        this.player = p;
        this.uuid = p.getUniqueId();
        this.values = plugin.getValues();
        this.manager = plugin.getManager();
        this.scheduler = plugin.getServer().getScheduler();
        this.name = name;
        this.rnd = plugin.getRnd();
        this.map = setMap(mapId);
        this.info = values.getSchematics().get(map);
        this.locs = new Locations(this, latch);
        this.run = new Run(this);
        fullSlots = info.isFullSlots();
        stands = values.getStands();
        mapName = info.getName();
        health = info.getHealth();
        exitTime = info.getExitTime();
        locale = utils.getLocale().player(p);
        status = values.getSWait().getOrDefault(locale, values.getSWait().get(""));
        maxLefts = info.getMaxLefts();
        maxRights = info.getMaxRights();
        start = locs.getStart();
        addPlayer(p);
    }

    public void addPlayer(Player p) {
        manager.getPlayers().put(uuid, new PlayerData(plugin, p, name));
        scheduler.runTask(plugin, () -> {
            if (!p.teleport(locs.getSpawn())) {
                manager.getPlayers().remove(uuid);
                close(true, false);
            } else {
                setDefault();
                sendMessage(values.getJoined(), info.getJoined(), searchGame, new String[]{name});
            }
        });
    }

    public World generateWorld(String name) {
        return utils.getWorldGenerator().generateWorld(name);
    }

    public void kickPlayer(boolean force, boolean isPluginStop) {
        if (manager.getPlayers().get(uuid) != null) {
            manager.getPlayers().get(uuid).apply();
        }
        manager.getPlayers().remove(uuid);
        if (!isPluginStop) {
            if (force) {
                sendMessage(values.getKicked(), info.getKicked(), searchGame, new String[]{name});
            } else {
                sendMessage(values.getEnd(), info.getMEnd(), searchGame, new String[]{name});
            }
        }
    }

    public void finish() {
        status = values.getSWin().getOrDefault(locale, values.getSWin().get(""));
        player.setAllowFlight(true);
        player.setFlying(true);
        double accuracy = info.getAccuracy(lefts, rights);
        int accuracyFull = (int) Math.round(accuracy * 100);
        int stars = info.getStars(accuracy);
        String[] replace;
        if (info.getStar().isEmpty()) {
            replace = new String[]{name, String.valueOf(accuracyFull), String.valueOf(stars), "5"};
        } else {
            String star = info.getStar();
            String fill = utils.repeat(star, stars);
            String empty = utils.repeat(star, 5 - stars);
            replace = new String[]{name, String.valueOf(accuracyFull), fill, empty};
        }
        plugin.getSupports().getTops().setData(player, map, accuracyFull);
        sendMessage(values.getWin(), info.getWin(), search, replace);
        info.sendMessage(player, stars);
        tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> close(false, false), exitTime));
    }

    public void start() {
        clear();
        started = true;
        start.setYaw(info.getYaw());
        start.setPitch(info.getNorth());
        player.teleport(start);
        status = values.getSStart().getOrDefault(locale, values.getSStart().get(""));
        if (!fullSlots) {
            info.getSlots().forEach((slot, item) -> player.getInventory().setItem(slot, item));
        }
        run.startMove();
    }

    public void sendMessage(Map<String, List<Action>> msg, List<Action> msgSchem, String[] s, String[] r) {
        utils.sendMessage(player, msg, s, r);
        utils.sendMessage(player, msgSchem, s, r);
    }

    public void sendMessage(Map<String, List<Action>> msg, List<Action> msgSchem) {
        utils.sendMessage(player, msg);
        utils.sendMessage(player, msgSchem);
    }

    public void restart() {
        clear();
        setDefault();
        started = false;
        status = values.getSWait().getOrDefault(locale, values.getSWait().get(""));
        player.teleport(locs.getSpawn());
        sendMessage(values.getRestarted(), info.getRestarted());
    }

    public void close(boolean force, boolean isPluginStop) {
        clear();
        kickPlayer(force, isPluginStop);
        Bukkit.unloadWorld(name, false);
        values.getRooms().remove(name);
        if (values.isDeleteWhenClosing()) {
            values.deleteDirectory(new File(name));
        }
    }

    private String setMap(String mapId) {
        return mapId != null ? mapId : values.getMaps().get(rnd.nextInt(values.getMaps().size()));
    }

    @SuppressWarnings("deprecation")
    private void setDefault() {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setMaxHealth(health);
        player.setHealthScale(health);
        player.setHealth(health);
        player.setFoodLevel(10);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        values.getSlots().forEach((slot, item) -> player.getInventory().setItem(slot, item));
        if (fullSlots) {
            info.getSlots().forEach((slot, item) -> player.getInventory().setItem(slot, item));
        }
    }

    public void paste(Location loc, String schematic) {
        utils.paste(loc, schematic);
    }

    private void clear() {
        if (run.getTask() != null) {
            run.getTask().cancel();
            run.getChicken().remove();
        }
        lefts = 0;
        rights = 0;
        run.getStands().values().forEach(set -> set.forEach(ArmorStand::remove));
        run.getStands().clear();
        run.getBombs().values().forEach(BukkitTask::cancel);
        tasks.forEach(BukkitTask::cancel);
        run.getBombs().clear();
        tasks.clear();
        actives.clear();
    }
}
