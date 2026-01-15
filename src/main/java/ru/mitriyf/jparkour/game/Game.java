package ru.mitriyf.jparkour.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.game.temp.data.LocationsData;
import ru.mitriyf.jparkour.game.temp.data.PlayerData;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.game.temp.task.Run;
import ru.mitriyf.jparkour.game.temp.task.data.StandActive;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.StandData;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game {
    private final UUID uuid;
    private final String map;
    private final boolean dev;
    private final Utils utils;
    private final String name;
    private final Values values;
    private final Player player;
    private final String locale;
    private final Location start;
    private final JParkour plugin;
    private final Manager manager;
    private final ThreadLocalRandom rnd;
    private final BukkitScheduler scheduler;
    private final Map<String, StandData> stands;
    private final String[] searchGame = {"%game%"};
    private final Set<String> actives = new HashSet<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final String[] search = {"%game%", "%accuracy%", "%star_win%", "%star_loss%"};
    private boolean fullSlots, started, restartActive, restartCooldown, infoExists = true;
    private int maxLefts, maxRights, health = 20, foodLevel = 10, exitTime;
    @Setter
    private boolean triggerEnabled = true;
    private SchematicData info;
    private LocationsData locs;
    @Setter
    private int lefts, rights;
    @Setter
    private Material trigger;
    private String mapName;
    private Editor editor;
    private String status;
    private Run run;

    public Game(JParkour plugin, CountDownLatch latch, Player p, String mapId, String name, boolean dev) {
        this.plugin = plugin;
        this.name = name;
        this.dev = dev;
        utils = plugin.getUtils();
        player = p;
        uuid = p.getUniqueId();
        values = plugin.getValues();
        manager = plugin.getManager();
        scheduler = plugin.getServer().getScheduler();
        rnd = plugin.getRnd();
        stands = values.getStands();
        if (!dev) {
            map = setMap(mapId);
            setupSchematic(latch, false);
        } else {
            map = mapId;
            if (mapId == null) {
                infoExists = false;
                locs = new LocationsData(plugin, this, latch, true);
            } else {
                setupSchematic(latch, true);
            }
            editor = new Editor(this);
        }
        locale = utils.getLocale().player(p);
        status = values.getSWait().getOrDefault(locale, values.getSWait().get(""));
        start = locs.getStart();
        addPlayer(p);
    }

    private void setupSchematic(CountDownLatch latch, boolean dev) {
        info = values.getSchematics().get(map);
        fullSlots = info.isFullSlots();
        mapName = info.getName();
        health = info.getHealth();
        foodLevel = info.getFoodLevel();
        exitTime = info.getExitTime();
        maxLefts = info.getMaxLefts();
        maxRights = info.getMaxRights();
        locs = new LocationsData(plugin, this, latch, dev);
        run = new Run(this);
    }

    public void addPlayer(Player p) {
        manager.getWaiters().remove(uuid);
        manager.getPlayers().put(uuid, new PlayerData(plugin, p, name));
        scheduler.runTask(plugin, () -> {
            if (!p.teleport(locs.getSpawn())) {
                manager.getPlayers().remove(uuid);
                close(true, false);
            } else {
                if (infoExists) {
                    locs.generateStands();
                    sendMessage(values.getJoined(), info.getJoined(), searchGame, new String[]{name});
                    if (dev) {
                        editor.setup();
                    }
                }
                setDefault();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setDefault() {
        player.setFlying(false);
        player.setMaxHealth(health);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        if (!dev) {
            player.setAllowFlight(false);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(info.getGameMode());
            setSlots(values.getSlots());
            if (fullSlots) {
                setSlots(info.getSlots());
            }
        } else {
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
            setSlots(values.getEditorSlots());
        }
    }

    public void start() {
        clear();
        started = true;
        start.setYaw(info.getYaw());
        start.setPitch(info.getPitch());
        player.teleport(start);
        status = values.getSStart().getOrDefault(locale, values.getSStart().get(""));
        if (!fullSlots) {
            setSlots(info.getSlots());
        }
        run.startMove();
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
        tasks.add(scheduler.runTaskLater(plugin, () -> close(false, false), exitTime));
    }

    public void restart() {
        clear();
        setDefault();
        started = false;
        status = values.getSWait().getOrDefault(locale, values.getSWait().get(""));
        player.teleport(locs.getSpawn());
        if (infoExists) {
            sendMessage(values.getRestarted(), info.getRestarted());
        }
    }

    public void restartActive() {
        if (active()) {
            return;
        }
        restart();
    }

    public void playerRestart() {
        if (active()) {
            return;
        } else if (restartCooldown) {
            sendMessage(values.getCooldownRestart(), info.getCooldownRestart());
            return;
        }
        restartCooldown = true;
        restart();
        scheduler.runTaskLater(plugin, () -> restartCooldown = false, values.getRestartCooldown());
    }

    private boolean active() {
        if (restartActive) {
            return true;
        }
        restartActive = true;
        scheduler.runTaskLater(plugin, () -> restartActive = false, 2);
        return false;
    }

    private void kickPlayer(boolean force, boolean isPluginStop) {
        PlayerData data = manager.getPlayers().get(uuid);
        if (data != null) {
            data.apply();
        }
        manager.getPlayers().remove(uuid);
        if (!isPluginStop && infoExists) {
            if (force) {
                sendMessage(values.getKicked(), info.getKicked(), searchGame, new String[]{name});
            } else {
                sendMessage(values.getEnd(), info.getMEnd(), searchGame, new String[]{name});
            }
        }
    }

    public void close(boolean force, boolean isPluginStop) {
        clear();
        removeStands();
        kickPlayer(force, isPluginStop);
        plugin.getServer().unloadWorld(name, false);
        manager.getConfirmation().remove(name);
        values.getRooms().remove(name);
        if (values.isDeleteWhenClosing()) {
            values.deleteDirectory(new File(name));
        }
    }

    private void removeStands() {
        for (Map.Entry<Location, StandActive> stand : locs.getStands().entrySet()) {
            stand.getValue().close();
        }
        locs.getStands().clear();
    }

    public void clear() {
        if (run != null && run.getTask() != null) {
            run.getTask().cancel();
            run.getChicken().remove();
            for (StandActive stand : run.getStands().values()) {
                stand.teleportToSpawn();
            }
            run.getStands().clear();
            for (BukkitTask task : run.getBombs().values()) {
                task.cancel();
            }
            run.getBombs().clear();
            run.setTask(null);
        }
        lefts = 0;
        rights = 0;
        for (BukkitTask task : tasks) {
            utils.getTasks().remove(task.getTaskId());
            task.cancel();
        }
        tasks.clear();
        actives.clear();
    }

    private String setMap(String mapId) {
        return mapId != null ? mapId : values.getMaps().get(rnd.nextInt(values.getMaps().size()));
    }

    public void setSlots(Map<Integer, ItemStack> slots) {
        for (Map.Entry<Integer, ItemStack> s : slots.entrySet()) {
            player.getInventory().setItem(s.getKey(), s.getValue());
        }
    }

    public void sendMessage(Map<String, List<Action>> msg, List<Action> msgSchem, String[] s, String[] r) {
        tasks.add(utils.sendMessage(player, msg, s, r));
        tasks.add(utils.sendMessage(player, msgSchem, s, r));
    }

    public void sendMessage(Map<String, List<Action>> msg, List<Action> msgSchem) {
        tasks.add(utils.sendMessage(player, msg));
        tasks.add(utils.sendMessage(player, msgSchem));
    }
}
