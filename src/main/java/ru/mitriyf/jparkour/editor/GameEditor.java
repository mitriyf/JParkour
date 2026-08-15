package ru.mitriyf.jparkour.editor;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.model.*;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Getter
public class GameEditor {
    private final Map<Location, List<Location>> defaultLocations = new HashMap<>();
    private final Material material = Material.COAL_ORE, glass = Material.GLASS;
    private final Map<Location, StandLocationData> stands = new HashMap<>();
    private final Map<Location, Set<UUID>> glowStands = new HashMap<>();
    private final Map<Location, Set<UUID>> poseStands = new HashMap<>();
    private final Map<Integer, PointData> points = new HashMap<>();
    private final String poseCustomName = "§6§l⌂ POSE ";
    private final String pointCustomName = "§6§l⇄ POINT ";
    private final String standCustomName = "§c§l★   STAND   ★";
    private final String spawnCustomName = "§a§l❤   SPAWN   ❤";
    private final String portalCustomName = "§5§l⚓   PORTAL   ⚓";
    private final String startCustomName = "§e§l⚡   START   ⚡";
    private final String endCustomName = "§8§l⌛    END    ⌛";
    private final Game game;
    private final World world;
    private final Utils utils;
    private final Values values;
    private final JParkour plugin;
    private final LocationsData locations;
    private final BukkitScheduler scheduler;
    @Setter
    private Block selectedBlockAxe;
    private Location pose1, pose2, pose3, spawn, start, end, portal;
    private SchematicMemberData schematicMemberData;
    private boolean process = false, processChange = false;
    private MemberData memberData;
    private SchematicData info;

    public GameEditor(Game game) {
        this.game = game;
        plugin = game.getPlugin();
        info = game.getInfo();
        locations = game.getLocations();
        utils = game.getUtils();
        values = game.getValues();
        scheduler = game.getScheduler();
        world = locations.getDefaultLocation().getWorld();
    }

    public void changePlayer(Player player, int id) {
        if (processChange) {
            clear(true);
            memberData = new MemberData(id);
            if (info != null) {
                setup(memberData, info.getSchematicMemberDataMap().get(id));
            } else {
                setup(memberData, schematicMemberData);
            }
            player.sendMessage("§aYou have successfully set player " + id);
        } else {
            processChange = true;
            player.sendMessage("§eAre you sure? §cIf you haven't saved the player's settings, you will lose them.\n§aIf you have, enter the command again. §eYou have 30 seconds to decide.");
            scheduler.runTaskLater(plugin, () -> processChange = false, 600);
        }
    }

    public void setup(MemberData memberData, SchematicMemberData schematicMemberData) {
        this.memberData = memberData;
        if (schematicMemberData != null) {
            this.schematicMemberData = schematicMemberData;
        } else {
            double[] doubles = new double[]{0, 0, 0, 0, 0};
            this.schematicMemberData = new SchematicMemberData(doubles, doubles);
        }
        setDefaultMemberData();
        loadData();
        setInfoData();
    }

    private void setDefaultMemberData() {
        memberData.setSchematicMemberData(schematicMemberData);
        memberData.setStart(locations.getLocation(schematicMemberData.getStart()));
        memberData.setEnd(locations.getLocation(schematicMemberData.getEnd()));
        locations.generateStands(schematicMemberData, memberData);
        for (Map.Entry<Integer, SchematicPointData> point : schematicMemberData.getPoints().entrySet()) {
            memberData.getPoints().put(point.getKey(), locations.getLocation(point.getValue().getLocation()));
        }
    }

    public void processSave(Player player, String name) {
        process = true;
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                CountDownLatch clearLatch = new CountDownLatch(1);
                scheduler.runTask(plugin, () -> {
                    clear(false);
                    scheduler.runTaskLater(plugin, clearLatch::countDown, 5L);
                });
                clearLatch.await();
                utils.getSchematic().save(name, pose1, pose2, pose3);
                values.setup(false);
                CountDownLatch saveLatch = new CountDownLatch(1);
                scheduler.runTask(plugin, () -> {
                    save(name);
                    scheduler.runTaskLater(plugin, saveLatch::countDown, 5L);
                });
                saveLatch.await();
                process = false;
                player.sendMessage("§aSuccessfully!\n§eSettings accepted.\nExit the editor: /jparkour exit");
            } catch (Exception e) {
                plugin.getLogger().warning("Couldn't save schem. Error: " + e);
                player.sendMessage("§cCouldn't save schem.");
            }
        });
    }

    private void loadData() {
        if (info == null) {
            spawn = locations.getSpawn();
            portal = locations.getPortal();
        } else {
            spawn = locations.getLocation(info.getSpawn());
            portal = locations.getLocation(info.getPortal());
        }
        setupLocation(spawn);
        setupLocation(portal);
        start = memberData.getStart();
        setupLocation(start);
        end = memberData.getEnd();
        setupLocation(end);
        for (Map.Entry<Integer, Location> point : memberData.getPoints().entrySet()) {
            int i = point.getKey();
            Location loc = point.getValue();
            SchematicPointData schematicPointData = schematicMemberData.getPoints().computeIfAbsent(i, k -> new SchematicPointData());
            double radiusStartPoint = schematicPointData.getRadiusStartPoint();
            boolean teleportEnabled = schematicPointData.isTeleport();
            points.put(i, new PointData(loc, radiusStartPoint, teleportEnabled));
        }
    }

    private void setupLocation(Location location) {
        defaultLocations.computeIfAbsent(location.getBlock().getLocation(), k -> new ArrayList<>()).add(location);
    }

    public String contains(Location blockLocation) {
        if (defaultLocations.containsKey(blockLocation)) {
            return "loc";
        }
        StandLocationData standLocationData = stands.get(blockLocation);
        if (standLocationData == null) {
            return null;
        }
        return standLocationData.getType();
    }

    private void setInfoData() {
        scheduler.runTask(plugin, () -> {
            for (Map.Entry<Location, StandActiveData> stand : memberData.getStands().entrySet()) {
                Location key = stand.getKey();
                Block block = key.getBlock();
                stands.put(block.getLocation(), new StandLocationData(key, stand.getValue().getType()));
                block.setType(material);
                createGlowStand(key, standCustomName, false);
            }
            spawn.getBlock().setType(glass);
            start.getBlock().setType(glass);
            end.getBlock().setType(glass);
            createGlowStand(center(portal), portalCustomName, false);
            createGlowWithInfo();
            for (Map.Entry<Integer, PointData> point : points.entrySet()) {
                PointData pointData = point.getValue();
                setPoint(point.getKey(), pointData.getRadiusStartPoint(), pointData.isTeleportEnabled(), pointData.getLocation());
            }
            if (game.isInfoExists()) {
                setPose3(locations.getDefaultLocation());
            }
        });
    }

    private void createGlowWithInfo() {
        if (info != null) {
            createGlowStand(spawn, spawnCustomName, false);
            createGlowStand(start, startCustomName, false);
            createGlowStand(end, endCustomName, false);
        } else {
            createGlowStand(center(spawn), spawnCustomName, false);
            createGlowStand(center(start), startCustomName, false);
            createGlowStand(center(end), endCustomName, false);
        }
    }

    private void setDefaultLocations(ConfigurationSection locationsSection) {
        Location spawnDistance = getDistanceWithDefault(spawn, pose3).clone();
        Location portalDistance = getDistanceWithDefault(portal, pose3).clone();
        locationsSection.set("spawn", spawnDistance.getX() + ";" + spawnDistance.getY() + ";" + spawnDistance.getZ() + ";" + spawn.getYaw() + ";" + spawn.getPitch());
        locationsSection.set("portal", portalDistance.getX() + ";" + portalDistance.getY() + ";" + portalDistance.getZ() + ";" + portal.getYaw() + ";" + portal.getPitch());
    }

    private void setLocations(ConfigurationSection playerSection) {
        Location startDistance = getDistanceWithDefault(start, pose3).clone();
        Location endDistance = getDistanceWithDefault(end, pose3).clone();
        playerSection.set("start", startDistance.getX() + ";" + startDistance.getY() + ";" + startDistance.getZ() + ";" + start.getYaw() + ";" + start.getPitch());
        playerSection.set("end", endDistance.getX() + ";" + endDistance.getY() + ";" + endDistance.getZ() + ";" + end.getYaw() + ";" + end.getPitch());
    }

    private void setStands(ConfigurationSection playerSection) {
        List<String> standsLocs = new ArrayList<>();
        for (StandLocationData stand : stands.values()) {
            Location standLocationData = stand.getLocation();
            Location loc = getDistanceWithDefault(standLocationData, pose3).clone();
            standsLocs.add(stand.getType() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + standLocationData.getYaw() + ";" + standLocationData.getPitch());
        }
        if (standsLocs.isEmpty()) {
            playerSection.set("stands", null);
        } else {
            playerSection.set("stands", standsLocs);
        }
    }

    private void setPoints(ConfigurationSection locs) {
        ConfigurationSection pointsSection = locs.createSection("points");
        for (Map.Entry<Integer, PointData> point : points.entrySet()) {
            PointData pointData = point.getValue();
            Location loc = getDistanceWithDefault(pointData.getLocation(), pose3);
            String locString = loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
            ConfigurationSection pointSection = pointsSection.createSection(String.valueOf(point.getKey()));
            pointSection.set("radiusStartPoint", pointData.getRadiusStartPoint());
            ConfigurationSection locationSection = pointSection.createSection("location");
            locationSection.set("start", locString);
            locationSection.set("teleport", pointData.isTeleportEnabled());
        }
    }

    public void setBlockStand(Location loc, String typeStack) {
        stands.put(loc.getBlock().getLocation(), new StandLocationData(loc, typeStack));
        createGlowStand(loc, standCustomName, false);
    }

    public void setPoint(int id, double radiusStartPoint, boolean teleport, Location loc) {
        String name = pointCustomName + id + " ★ " + radiusStartPoint + " ★ " + teleport + " ★ " + loc.getYaw() + " ★ " + loc.getPitch();
        createGlowStand(loc, name, true);
    }

    public void setSpawn(Location loc) {
        setLocation(spawn, loc, spawnCustomName, true, false);
        spawn = loc;
    }

    public void setPortal(Location loc) {
        setLocation(portal, loc, portalCustomName, false, true);
        portal = loc;
    }

    public void setStart(Location loc) {
        setLocation(start, loc, startCustomName, true, false);
        start = loc;
    }

    public void setEnd(Location loc) {
        setLocation(end, loc, endCustomName, true, false);
        end = loc;
    }

    public void setPose1(Location loc) {
        removeStand(poseStands, pose1);
        pose1 = loc;
        createPoseStand(loc, poseCustomName + 1);
    }

    public void setPose2(Location loc) {
        removeStand(poseStands, pose2);
        pose2 = loc;
        createPoseStand(loc, poseCustomName + 2);
    }

    public void setPose3(Location loc) {
        removeStand(poseStands, pose3);
        pose3 = loc;
        createPoseStand(loc, poseCustomName + 3);
        locations.setDefaultLocation(pose3);
    }

    private void setLocation(Location loc, Location newLoc, String name, boolean setBlock, boolean center) {
        if (loc != null) {
            Block block = loc.getBlock();
            if (setBlock && !loc.equals(newLoc)) {
                block.setType(Material.AIR);
            }
            removeBlockStand(loc);
            defaultLocations.remove(block.getLocation());
        }
        if (newLoc != null) {
            if (center) {
                createGlowStand(center(newLoc), name, false);
            } else {
                createGlowStand(newLoc, name, false);
            }
            setupLocation(newLoc);
        }
    }

    private void createGlowStand(Location loc, String customName, boolean point) {
        ArmorStand stand = createArmorStand(loc, customName, point, false);
        put(glowStands, loc, stand.getUniqueId());
    }

    private void createPoseStand(Location loc, String customName) {
        ArmorStand stand = createArmorStand(loc, customName, false, true);
        put(poseStands, loc, stand.getUniqueId());
    }

    private ArmorStand createArmorStand(Location loc, String customName, boolean point, boolean center) {
        ItemStack stack = null;
        Location locStand;
        if (point) {
            stack = new ItemStack(Material.TORCH);
            locStand = loc;
        } else {
            double add = center ? 0.5 : 0;
            locStand = loc.clone().add(add, 0.1, add);
        }
        ArmorStand stand = (ArmorStand) locStand.getWorld().spawnEntity(locStand, EntityType.ARMOR_STAND);
        stand.setHelmet(stack);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setArms(false);
        stand.setSmall(true);
        stand.setMarker(true);
        stand.setCustomName(customName);
        stand.setCustomNameVisible(true);
        return stand;
    }

    public void removeBlockStand(Location loc) {
        stands.remove(loc);
        removeStand(glowStands, loc);
    }

    public void removeStand(Map<Location, Set<UUID>> stands, Location loc) {
        if (loc == null) {
            return;
        }
        Set<UUID> uuids = stands.get(loc.getBlock().getLocation());
        if (uuids != null) {
            World world = loc.getWorld();
            for (UUID uuid : uuids) {
                removeEntity(world, uuid);
            }
        }
        stands.remove(loc);
    }

    private void save(String name) {
        File fileConfig = new File(values.getDataFolder(), values.getSchematicsDir() + name + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(fileConfig);
        cfg.set("version", "editorBackup");
        ConfigurationSection coordsSection = cfg.getConfigurationSection("coords");
        if (coordsSection == null) {
            coordsSection = cfg.createSection("coords");
        }
        savePose3(coordsSection);
        ConfigurationSection locationsSection = cfg.getConfigurationSection("locations");
        if (locationsSection == null) {
            locationsSection = cfg.createSection("locations");
        }
        setDefaultLocations(locationsSection);
        String playerId = String.valueOf(memberData.getId());
        ConfigurationSection playersSection = locationsSection.getConfigurationSection("players");
        if (playersSection == null) {
            playersSection = locationsSection.createSection("players");
        }
        ConfigurationSection playerSection = playersSection.getConfigurationSection(playerId);
        if (playerSection == null) {
            playerSection = playersSection.createSection(playerId);
        }
        setLocations(playerSection);
        setPoints(playerSection);
        setStands(playerSection);
        try {
            cfg.save(fileConfig);
            values.setup(false);
        } catch (Exception e) {
            values.getLogger().warning("Error save " + name + ".yml.\nError: " + e);
        }
        stands.clear();
        points.clear();
        int id = memberData.getId();
        info = values.getSchematics().get(name.toLowerCase());
        game.setInfo(info);
        setup(new MemberData(id), info.getSchematicMemberDataMap().get(id));
        setPose1(pose1);
        setPose2(pose2);
        setPose3(pose3);
    }

    private void savePose3(ConfigurationSection coordsSection) {
        coordsSection.set("x", pose3.getBlockX());
        coordsSection.set("y", pose3.getBlockY());
        coordsSection.set("z", pose3.getBlockZ());
    }

    private Location getDistanceWithDefault(Location loc, Location pose3) {
        double x = loc.getX() - pose3.getX();
        double y = loc.getY() - pose3.getY();
        double z = loc.getZ() - pose3.getZ();
        return new Location(pose3.getWorld(), x, y, z);
    }

    private void clear(boolean force) {
        removeStands(glowStands);
        removeStands(poseStands);
        for (Location loc : new HashSet<>(stands.keySet())) {
            loc.getBlock().setType(Material.AIR);
        }
        spawn.getBlock().setType(Material.AIR);
        start.getBlock().setType(Material.AIR);
        end.getBlock().setType(Material.AIR);
        if (force) {
            stands.clear();
            points.clear();
            defaultLocations.clear();
        }
    }

    private void removeStands(Map<Location, Set<UUID>> mapStand) {
        for (Map.Entry<Location, Set<UUID>> stand : new HashSet<>(mapStand.entrySet())) {
            Location loc = stand.getKey();
            World world = loc.getWorld();
            for (UUID uuid : stand.getValue()) {
                removeEntity(world, uuid);
            }
            mapStand.remove(loc);
        }
    }

    private void removeEntity(World world, UUID uuid) {
        Entity entity = utils.getEntity(world, uuid);
        if (entity != null) {
            entity.remove();
        }
    }

    private Location center(Location location) {
        return location.clone().add(0.5, 0, 0.5);
    }

    private void put(Map<Location, Set<UUID>> stand, Location location, UUID uuid) {
        stand.computeIfAbsent(location.getBlock().getLocation(), k -> new HashSet<>()).add(uuid);
    }
}
