package ru.mitriyf.jparkour.manager;

import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.model.GameData;
import ru.mitriyf.jparkour.model.PartyData;
import ru.mitriyf.jparkour.model.PlayerData;
import ru.mitriyf.jparkour.model.SchematicData;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {
    private final Utils utils;
    private final Values values;
    private final Server server;
    private final JParkour plugin;
    private final ThreadLocalRandom random;
    private final PartyManager partyManager;
    private final BukkitScheduler scheduler;
    private final String[] search = {"%room%"};
    @Getter
    private final Set<UUID> waiters = new HashSet<>();
    @Getter
    private final Map<String, Game> rooms = new HashMap<>();
    @Getter
    private final Set<String> confirmation = new HashSet<>();
    @Getter
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    @Getter
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public GameManager(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        random = plugin.getRandom();
        values = plugin.getValues();
        server = plugin.getServer();
        scheduler = server.getScheduler();
        partyManager = plugin.getPartyManager();
    }

    public void join(Player player, String mapId, boolean dev) {
        UUID uuid = player.getUniqueId();
        if (players.containsKey(uuid) || waiters.contains(uuid)) {
            utils.sendMessage(player, values.getInGame());
            return;
        } else if (mapId != null && (!dev && !values.getSchematics().containsKey(mapId))) {
            utils.sendMessage(player, values.getNotfound());
            return;
        }
        String playerName = player.getName();
        GameData gameData = null;
        String partyName = null;
        for (PartyData partyData : partyManager.getPartyDataMap().values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(uuid)) {
                partyName = partyData.getPartyName();
                if (!partyData.getLeader().equals(uuid)) {
                    utils.sendMessage(player, values.getNotLeader(), partyManager.getSearch(), new String[]{server.getPlayer(partyData.getLeader()).getName(), playerName, partyName});
                    return;
                }
                gameData = new GameData(player, convertToPlayers(members));
                break;
            }
        }
        if (gameData == null) {
            gameData = new GameData(player, convertToPlayers(uuid));
        }
        generateRoom(gameData, uuid, partyName, mapId, dev);
    }

    private void generateRoom(GameData gameData, UUID uuid, String partyName, String mapId, boolean dev) {
        String readyMapId;
        if (!dev) {
            readyMapId = setMap(mapId);
        } else {
            readyMapId = mapId;
        }
        int amount = values.getAmount();
        String name = values.getWorldStart() + (dev ? "E" : "") + (amount < 1 ? 1 : random.nextInt(amount));
        Set<Player> players = gameData.getMembers();
        if (amount > 0 && !rooms.containsKey(name)) {
            tasks.remove(uuid);
            Player leader = gameData.getLeader();
            utils.sendMessage(leader, values.getConnect(), search, new String[]{name});
            CountDownLatch latch = new CountDownLatch(1);
            SchematicData schematicData = values.getSchematics().get(readyMapId);
            int size = players.size();
            if (!dev && schematicData.getSize() < size) {
                if (mapId != null || values.getMaxSize() < size) {
                    String leaderName = leader.getName();
                    utils.sendMessage(leader, values.getBigSizeParty(), partyManager.getSearch(), new String[]{leaderName, leaderName, partyName});
                    waiters.remove(uuid);
                } else {
                    regenerateRoom(gameData, uuid, partyName, null, false);
                }
                return;
            }
            waiters.add(uuid);
            rooms.put(name, null);
            scheduler.runTaskAsynchronously(plugin, () -> rooms.put(name, new Game(plugin, schematicData, latch, gameData, readyMapId, name, dev)));
        } else {
            regenerateRoom(gameData, uuid, partyName, mapId, dev);
        }
    }

    private void regenerateRoom(GameData gameData, UUID uuid, String partyName, String mapId, boolean dev) {
        tasks.put(uuid, scheduler.runTaskLater(plugin, () -> {
            if (!waiters.contains(uuid)) {
                waiters.add(uuid);
                utils.sendMessage(gameData.getLeader(), values.getWaiter());
            }
            generateRoom(gameData, uuid, partyName, mapId, dev);
        }, 10));
    }

    public Game getGame(UUID uuid) {
        PlayerData data = players.get(uuid);
        if (data != null) {
            String id = data.getGame();
            return rooms.get(id);
        }
        return null;
    }

    private Set<Player> convertToPlayers(Set<UUID> uuids) {
        Set<Player> players = new HashSet<>();
        for (UUID uuid : uuids) {
            players.add(server.getPlayer(uuid));
        }
        return players;
    }

    private Set<Player> convertToPlayers(UUID uuid) {
        Set<Player> players = new HashSet<>();
        players.add(server.getPlayer(uuid));
        return players;
    }

    private String setMap(String mapId) {
        return mapId != null ? mapId : values.getMaps().get(random.nextInt(values.getMaps().size()));
    }

    public Game getGame(String world) {
        return rooms.get(world);
    }
}
