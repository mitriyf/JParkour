package ru.mitriyf.jparkour.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.editor.GameEditor;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.model.*;
import ru.mitriyf.jparkour.task.GameTask;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game {
    private final int size;
    private final String map;
    private final boolean dev;
    private final Utils utils;
    private final String name;
    private final Values values;
    private final Player leader;
    private final JParkour plugin;
    private final Set<Player> players;
    private final GameManager gameManager;
    private final ThreadLocalRandom random;
    private final BukkitScheduler scheduler;
    private final Map<String, StandData> stands;
    private final String[] searchGame = {"%game%"};
    private final Set<String> actives = new HashSet<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Map<UUID, MemberData> memberDataMap = new HashMap<>();
    private final String[] search = {"%game%", "%accuracy%", "%star_win%", "%star_loss%"};
    private boolean fullSlots, started, restartActive, restartCooldown, infoExists = true;
    private int health = 20, foodLevel = 10, exitTime, finished = 0;
    @Setter
    private boolean triggerEnabled = true;
    private LocationsData locations;
    private GameEditor gameEditor;
    @Setter
    private SchematicData info;
    @Setter
    private Material trigger;
    private String mapName;

    public Game(JParkour plugin, SchematicData schematicData, CountDownLatch latch, GameData gameData, String mapId, String name, boolean dev) {
        this.dev = dev;
        this.name = name;
        this.plugin = plugin;
        info = schematicData;
        utils = plugin.getUtils();
        values = plugin.getValues();
        random = plugin.getRandom();
        leader = gameData.getLeader();
        players = gameData.getMembers();
        gameManager = plugin.getGameManager();
        scheduler = plugin.getServer().getScheduler();
        stands = values.getStands();
        size = players.size();
        map = mapId;
        if (!dev) {
            setupSchematic(latch, false);
        } else {
            if (mapId == null || !values.getSchematics().containsKey(mapId)) {
                infoExists = false;
                locations = new LocationsData(plugin, this, latch, true);
            } else {
                setupSchematic(latch, true);
            }
            gameEditor = new GameEditor(this);
        }
        readyMap();
    }

    private void setupSchematic(CountDownLatch latch, boolean dev) {
        fullSlots = info.isFullSlots();
        mapName = info.getName();
        health = info.getHealth();
        foodLevel = info.getFoodLevel();
        exitTime = info.getExitTime();
        locations = new LocationsData(plugin, this, latch, dev);
    }

    private void readyMap() {
        List<Integer> list = new ArrayList<>();
        if (infoExists) {
            for (int i = 0; i < info.getSize(); i++) {
                list.add(i);
            }
        }
        scheduler.runTask(plugin, () -> {
            MemberData memberData = null;
            SchematicMemberData schematicMemberData = null;
            for (Player player : players) {
                String[] strings = new String[]{name};
                int memberId = 0;
                if (infoExists) {
                    sendMessage(player, values.getJoined(), info.getJoined(), searchGame, strings);
                    if (!dev) {
                        memberId = list.remove(random.nextInt(list.size()));
                    }
                } else {
                    utils.sendMessage(player, values.getJoined(), searchGame, strings);
                }
                String locale = utils.getLocale().player(player);
                UUID uuid = player.getUniqueId();
                memberData = new MemberData(memberId);
                memberData.setLocale(locale);
                memberData.setStatus(values.getSWait().getOrDefault(locale, values.getSWait().get("")));
                memberDataMap.put(uuid, memberData);
                gameManager.getWaiters().remove(uuid);
                gameManager.getPlayers().put(uuid, new PlayerData(plugin, player, name));
                if (!player.teleport(locations.getSpawn())) {
                    close(true, false);
                    return;
                }
                setDefault(player);
                if (infoExists) {
                    schematicMemberData = info.getSchematicMemberDataMap().get(memberId);
                    if (schematicMemberData != null) {
                        memberData.setSchematicMemberData(schematicMemberData);
                        memberData.setStart(locations.getLocation(schematicMemberData.getStart()));
                        memberData.setEnd(locations.getLocation(schematicMemberData.getEnd()));
                        memberData.setGameTask(new GameTask(this, player, memberData));
                        locations.generateStands(schematicMemberData, memberData);
                        for (Map.Entry<Integer, SchematicPointData> point : schematicMemberData.getPoints().entrySet()) {
                            memberData.getPoints().put(point.getKey(), locations.getLocation(point.getValue().getLocation()));
                        }
                    }
                } else {
                    memberData.setStart(locations.getDefaultLocation());
                    memberData.setEnd(locations.getDefaultLocation());
                }
            }
            if (dev) {
                gameEditor.setup(memberData, schematicMemberData);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setDefault(Player player) {
        player.setFlying(false);
        player.setMaxHealth(health);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        if (!dev) {
            player.setAllowFlight(false);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(info.getGameMode());
            utils.setSlots(player, values.getSlots());
            if (fullSlots) {
                utils.setSlots(player, info.getSlots());
            }
        } else {
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
            utils.setSlots(player, values.getEditorSlots());
        }
    }

    public void start() {
        clear();
        started = true;
        for (Player player : players) {
            MemberData memberData = memberDataMap.get(player.getUniqueId());
            player.teleport(memberData.getStart());
            memberData.setStatus(values.getSStart().getOrDefault(memberData.getLocale(), values.getSStart().get("")));
            if (!fullSlots) {
                utils.setSlots(player, info.getSlots());
            }
            memberData.getGameTask().startMove();
        }
    }

    public void finish(Player player) {
        MemberData memberData = memberDataMap.get(player.getUniqueId());
        memberData.setStatus(values.getSWin().getOrDefault(memberData.getLocale(), values.getSWin().get("")));
        player.setAllowFlight(true);
        player.setFlying(true);
        SchematicMemberData schematicMemberData = memberData.getSchematicMemberData();
        double accuracy = info.getAccuracy(memberData.getLefts(), memberData.getRights(), schematicMemberData.getMaxLefts(), schematicMemberData.getMaxRights());
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
        sendMessage(player, values.getWin(), info.getWin(), search, replace);
        info.sendMessage(player, stars);
        finished++;
        if (finished == size) {
            tasks.add(scheduler.runTaskLater(plugin, () -> close(false, false), exitTime));
        }
    }

    public void restart() {
        clear();
        started = false;
        for (Player player : players) {
            setDefault(player);
            MemberData memberData = memberDataMap.get(player.getUniqueId());
            memberData.setStatus(values.getSWait().getOrDefault(memberData.getLocale(), values.getSWait().get("")));
            player.teleport(locations.getSpawn());
            if (infoExists) {
                sendMessage(player, values.getRestarted(), info.getRestarted());
            }
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
            sendMessage(leader, values.getCooldownRestart(), info.getCooldownRestart());
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

    private void kickPlayers(boolean force, boolean isPluginStop) {
        for (Player player : players) {
            UUID uuid = player.getUniqueId();
            PlayerData data = gameManager.getPlayers().get(uuid);
            if (data != null) {
                data.apply();
            }
            gameManager.getPlayers().remove(uuid);
            if (!isPluginStop && infoExists) {
                if (force) {
                    sendMessage(player, values.getKicked(), info.getKicked(), searchGame, new String[]{name});
                } else {
                    sendMessage(player, values.getEnd(), info.getMEnd(), searchGame, new String[]{name});
                }
            }
        }
    }

    public void close(boolean force, boolean isPluginStop) {
        clear();
        removeStands();
        kickPlayers(force, isPluginStop);
        plugin.getServer().unloadWorld(name, false);
        gameManager.getConfirmation().remove(name);
        gameManager.getRooms().remove(name);
        if (values.isDeleteWhenClosing()) {
            values.deleteDirectory(new File(name));
        }
    }

    private void removeStands() {
        for (MemberData memberData : memberDataMap.values()) {
            Map<Location, StandActiveData> standActiveDataMap = memberData.getStands();
            for (Map.Entry<Location, StandActiveData> stand : standActiveDataMap.entrySet()) {
                stand.getValue().close();
            }
            standActiveDataMap.clear();
        }
    }

    public void clear() {
        for (MemberData memberData : memberDataMap.values()) {
            GameTask gameTask = memberData.getGameTask();
            if (gameTask != null && gameTask.getTask() != null) {
                gameTask.getTask().cancel();
                gameTask.getChicken().remove();
                for (StandActiveData stand : gameTask.getStands().values()) {
                    stand.teleportToSpawn();
                }
                gameTask.getStands().clear();
                for (BukkitTask task : gameTask.getBombs().values()) {
                    task.cancel();
                }
                gameTask.getBombs().clear();
                gameTask.setTask(null);
            }
            memberData.setLefts(0);
            memberData.setRights(0);
        }
        for (BukkitTask task : tasks) {
            utils.getTasks().remove(task.getTaskId());
            task.cancel();
        }
        finished = 0;
        tasks.clear();
        actives.clear();
    }

    public void sendMessage(Player player, Map<String, List<Action>> msg, List<Action> msgSchem, String[] s, String[] r) {
        tasks.add(utils.sendMessage(player, msg, s, r));
        tasks.add(utils.sendMessage(player, msgSchem, s, r));
    }

    public void sendMessage(Player player, Map<String, List<Action>> msg, List<Action> msgSchem) {
        tasks.add(utils.sendMessage(player, msg));
        tasks.add(utils.sendMessage(player, msgSchem));
    }
}
