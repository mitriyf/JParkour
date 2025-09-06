package ru.mitriyf.jparkour.game;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.actions.data.Locations;
import ru.mitriyf.jparkour.game.actions.task.Run;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;
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
    private final Player player;
    private final UUID uuid;
    private final Run run;
    private final Locations locs;
    private final ThreadLocalRandom rnd;
    private final String[] search = {"%star_win%", "%star_loss%"};
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Set<String> actives = new HashSet<>();
    private final Map<String, StandData> stands;
    private final BukkitScheduler scheduler;
    private final int maxLefts, maxRights;
    private final Location start;
    private final String locale;
    private final String mapName;
    private final String name;
    private final String map;
    private final SchematicData info;
    private final int health;
    private boolean started;
    private String status;
    @Setter
    private Material trigger;
    @Setter
    private int lefts, rights;

    public Game(JParkour plugin, Player p, String mapId, String name) {
        this.plugin = plugin;
        this.utils = plugin.getUtils();
        this.player = p;
        this.uuid = p.getUniqueId();
        this.values = plugin.getValues();
        this.manager = plugin.getManager();
        this.name = name;
        this.rnd = plugin.getRnd();
        this.map = setMap(mapId);
        this.info = values.getSchematics().get(map);
        this.locs = new Locations(this);
        this.run = new Run(this);
        stands = values.getStands();
        mapName = info.getName();
        health = info.getHealth();
        scheduler = plugin.getServer().getScheduler();
        locale = utils.getLocale().player(p);
        status = values.getSWait().getOrDefault(locale, values.getSWait().get(""));
        maxLefts = info.getMaxLefts();
        maxRights = info.getMaxRights();
        start = locs.getStart();
        addPlayer(p);
    }

    public void addPlayer(Player p) {
        manager.getPlayers().put(uuid, new PlayerData(plugin, p, name));
        if (!p.teleport(locs.getSpawn())) {
            manager.getPlayers().remove(uuid);
            close();
            return;
        }
        setDefault();
    }

    public void kickPlayer() {
        manager.getPlayers().get(uuid).apply();
        manager.getPlayers().remove(uuid);
        sendMessage(values.getKicked(), info.getKicked());
    }

    public void finish() {
        status = values.getSWin().getOrDefault(locale, values.getSWin().get(""));
        player.setAllowFlight(true);
        player.setFlying(true);
        int stars = info.getStars(lefts, rights);
        String[] replace;
        if (info.getStar().isEmpty()) {
            replace = new String[]{stars + "", "5"};
        } else {
            String star = info.getStar();
            String fill = utils.repeat(star, stars);
            String empty = utils.repeat(star, 5 - stars);
            replace = new String[]{fill, empty};
        }
        sendMessage(values.getWin(), info.getWin(), search, replace);
        info.sendMessage(player, stars);
        tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, this::close, 100));
    }

    public void start() {
        clear();
        for (Map.Entry<Integer, ItemStack> d : info.getSlots().entrySet()) {
            player.getInventory().setItem(d.getKey(), d.getValue());
        }
        started = true;
        start.setYaw(info.getYaw());
        start.setPitch(info.getNorth());
        player.teleport(start);
        status = values.getSStart().getOrDefault(locale, values.getSStart().get(""));
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

    public void close() {
        clear();
        kickPlayer();
        Bukkit.unloadWorld(name, false);
        FileUtils.deleteQuietly(new File(name));
        values.getRooms().remove(name);
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
