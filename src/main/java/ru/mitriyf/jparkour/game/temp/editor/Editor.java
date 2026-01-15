package ru.mitriyf.jparkour.game.temp.editor;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.temp.data.LocationsData;
import ru.mitriyf.jparkour.game.temp.editor.data.PointData;
import ru.mitriyf.jparkour.game.temp.task.data.StandActive;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;
import ru.mitriyf.jparkour.values.data.schematic.point.SchematicPoint;

import java.io.File;
import java.util.*;

@Getter
public class Editor {
    private final Map<Location, UUID> glowStands = new HashMap<>();
    private final Map<Location, UUID> poseStands = new HashMap<>();
    private final Map<Integer, PointData> points = new HashMap<>();
    private final Map<Location, String> stands = new HashMap<>();
    private final Material material = Material.COAL_ORE;
    private final Material glass = Material.GLASS;
    private final String poseCustomName = "§6§l⌂ POSE ";
    private final String pointCustomName = "§6§l⇄ POINT ";
    private final String standCustomName = "§c§l★   STAND   ★";
    private final String spawnCustomName = "§a§l❤   SPAWN   ❤";
    private final String portalCustomName = "§5§l⚓   PORTAL   ⚓";
    private final String startCustomName = "§e§l⚡   START   ⚡";
    private final String endCustomName = "§8§l⌛    END    ⌛";
    private final World world;
    private final Utils utils;
    private final Values values;
    private final JParkour plugin;
    private final LocationsData locs;
    private final SchematicData info;
    private final BukkitScheduler scheduler;
    private Location spawn, portal, start, end;
    @Setter
    private Block selectedBlockAxe;
    private Location pose1, pose2, pose3;
    @Setter
    private boolean isProcess = false;

    public Editor(Game game) {
        plugin = game.getPlugin();
        info = game.getInfo();
        locs = game.getLocs();
        utils = game.getUtils();
        values = game.getValues();
        scheduler = game.getScheduler();
        world = locs.getDefaultLocation().getWorld();
        if (game.isInfoExists()) {
            game.getScheduler().runTask(game.getPlugin(), () -> setPose3(locs.getDefaultLocation()));
        }
    }

    public void setup() {
        loadData();
        setInfoData();
    }

    private void loadData() {
        spawn = locs.getSpawn().getBlock().getLocation();
        portal = locs.getPortal().getBlock().getLocation();
        start = locs.getStart().getBlock().getLocation();
        end = locs.getEnd().getBlock().getLocation();
        scheduler.runTask(plugin, () -> {
            for (Map.Entry<Location, StandActive> stand : locs.getStands().entrySet()) {
                Block b = stand.getKey().getBlock();
                stands.put(b.getLocation(), stand.getValue().getType());
            }
        });
        for (Map.Entry<Integer, Location> point : locs.getPoints().entrySet()) {
            int i = point.getKey();
            Location loc = point.getValue();
            SchematicPoint schematicPoint = info.getPoints().get(i);
            double radiusStartPoint = schematicPoint.getRadiusStartPoint();
            boolean teleportEnabled = schematicPoint.isTeleport();
            points.put(i, new PointData(loc, radiusStartPoint, teleportEnabled));
        }
    }

    public String contains(Location bLoc) {
        if (bLoc.equals(spawn) || bLoc.equals(start) || bLoc.equals(end) || bLoc.equals(portal)) {
            return "loc";
        }
        return stands.get(bLoc);
    }

    private void setInfoData() {
        scheduler.runTask(plugin, () -> {
            for (Location loc : stands.keySet()) {
                loc.getBlock().setType(material);
                createGlowStand(loc, standCustomName, false);
            }
            spawn.getBlock().setType(glass);
            createGlowStand(spawn, spawnCustomName, false);
            createGlowStand(portal, portalCustomName, false);
            start.getBlock().setType(glass);
            createGlowStand(start, startCustomName, false);
            end.getBlock().setType(glass);
            createGlowStand(end, endCustomName, false);
            for (Map.Entry<Integer, PointData> point : points.entrySet()) {
                PointData pointData = point.getValue();
                setPoint(point.getKey(), pointData.getRadiusStartPoint(), pointData.isTeleportEnabled(), pointData.getLocation());
            }
        });
    }

    private void setLocs(ConfigurationSection locs) {
        Location spawnDistance = getDistanceWithDefault(spawn, pose3).clone().add(0.5, 0, 0.5);
        Location portalDistance = getDistanceWithDefault(portal, pose3).clone().add(0.5, 0, 0.5);
        Location startDistance = getDistanceWithDefault(start, pose3).clone().add(0.5, 0, 0.5);
        Location endDistance = getDistanceWithDefault(end, pose3).clone().add(0.5, 0, 0.5);
        locs.set("spawn", spawnDistance.getX() + ";" + spawnDistance.getY() + ";" + spawnDistance.getZ());
        locs.set("portal", portalDistance.getX() + ";" + portalDistance.getY() + ";" + portalDistance.getZ());
        locs.set("start", startDistance.getX() + ";" + startDistance.getY() + ";" + startDistance.getZ());
        locs.set("end", endDistance.getX() + ";" + endDistance.getY() + ";" + endDistance.getZ());
    }

    private void setStands(ConfigurationSection standsSection) {
        List<String> standsLocs = new ArrayList<>();
        for (Map.Entry<Location, String> stand : stands.entrySet()) {
            Location loc = getDistanceWithDefault(stand.getKey(), pose3).getBlock().getLocation().clone().add(0.5, 0, 0.5);
            standsLocs.add(stand.getValue() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ());
        }
        standsSection.set("locs", standsLocs);
    }

    private void setPoints(ConfigurationSection locs) {
        ConfigurationSection pointsSection = locs.createSection("points");
        for (Map.Entry<Integer, PointData> point : points.entrySet()) {
            PointData pointData = point.getValue();
            Location loc = getDistanceWithDefault(pointData.getLocation(), pose3);
            String locString = loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
            ConfigurationSection pointSection = pointsSection.createSection(String.valueOf(point.getKey()));
            pointSection.set("radiusStartPoint", pointData.getRadiusStartPoint());
            ConfigurationSection locationSection = pointSection.createSection("location");
            locationSection.set("start", locString);
            ConfigurationSection teleportSection = locationSection.createSection("teleport");
            teleportSection.set("enabled", pointData.isTeleportEnabled());
            teleportSection.set("yaw", loc.getYaw());
            teleportSection.set("pitch", loc.getPitch());
        }
    }

    public void setBlockStand(Location loc, String typeStack) {
        stands.put(loc, typeStack);
        createGlowStand(loc, standCustomName, false);
    }

    public void setPoint(int id, double radiusStartPoint, boolean teleport, Location loc) {
        String name = pointCustomName + id + " ★ " + radiusStartPoint + " ★ " + teleport + " ★ " + loc.getYaw() + " ★ " + loc.getPitch();
        createGlowStand(loc, name, true);
    }

    public void setSpawn(Location loc) {
        setLoc(spawn, loc, spawnCustomName, true);
        spawn = loc;
    }

    public void setPortal(Location loc) {
        setLoc(portal, loc, portalCustomName, false);
        portal = loc;
    }

    public void setStart(Location loc) {
        setLoc(start, loc, startCustomName, true);
        start = loc;
    }

    public void setEnd(Location loc) {
        setLoc(end, loc, endCustomName, true);
        end = loc;
    }

    public void setPose1(Location loc) {
        removePoseStand(pose1);
        pose1 = loc;
        createPoseStand(loc, poseCustomName + 1);
    }

    public void setPose2(Location loc) {
        removePoseStand(pose2);
        pose2 = loc;
        createPoseStand(loc, poseCustomName + 2);
    }

    public void setPose3(Location loc) {
        removePoseStand(pose3);
        pose3 = loc;
        createPoseStand(loc, poseCustomName + 3);
    }

    private void setLoc(Location loc, Location newLoc, String name, boolean setBlock) {
        if (loc != null) {
            if (setBlock && !loc.equals(newLoc)) {
                loc.getBlock().setType(Material.AIR);
            }
            removeBlockStand(loc);
        }
        if (newLoc != null) {
            createGlowStand(newLoc, name, false);
        }
    }

    private void createGlowStand(Location loc, String customName, boolean point) {
        ArmorStand stand = createArmorStand(loc, customName, point);
        glowStands.put(loc, stand.getUniqueId());
    }

    private void createPoseStand(Location loc, String customName) {
        ArmorStand stand = createArmorStand(loc, customName, false);
        poseStands.put(loc, stand.getUniqueId());
    }

    private ArmorStand createArmorStand(Location loc, String customName, boolean point) {
        ItemStack stack = null;
        Location locStand;
        if (point) {
            stack = new ItemStack(Material.TORCH);
            locStand = loc;
        } else {
            locStand = loc.clone().add(0.5, 0.1, 0.5);
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
        removeGlowStand(loc);
    }

    public void removeGlowStand(Location loc) {
        UUID uuid = glowStands.get(loc);
        if (uuid != null) {
            utils.getEntity(loc.getWorld(), uuid).remove();
        }
        glowStands.remove(loc);
    }

    private void removePoseStand(Location loc) {
        UUID uuid = poseStands.get(loc);
        if (uuid != null) {
            utils.getEntity(loc.getWorld(), uuid).remove();
        }
        poseStands.remove(loc);
    }

    public void save(String name) {
        File fileConfig = new File(values.getDataFolder(), values.getSchematicsDir() + name + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(fileConfig);
        cfg.set("version", "editorBackup");
        ConfigurationSection locs = cfg.getConfigurationSection("locs");
        setLocs(locs);
        setPoints(locs);
        ConfigurationSection standsSection = cfg.getConfigurationSection("stands");
        setStands(standsSection);
        try {
            cfg.save(fileConfig);
            values.setup(false);
        } catch (Exception e) {
            values.getLogger().warning("Error save " + name + ".yml.\nError: " + e);
        }
        setInfoData();
        setPose1(pose1);
        setPose2(pose2);
        setPose3(pose3);
    }

    private Location getDistanceWithDefault(Location loc, Location pose3) {
        double x = loc.getX() - pose3.getX();
        double y = loc.getY() - pose3.getY();
        double z = loc.getZ() - pose3.getZ();
        return new Location(pose3.getWorld(), x, y, z);
    }

    public void clear() {
        for (Map.Entry<Location, UUID> stand : new HashSet<>(glowStands.entrySet())) {
            Location loc = stand.getKey();
            removeEntity(stand, loc);
            glowStands.remove(loc);
        }
        for (Map.Entry<Location, UUID> stand : new HashSet<>(poseStands.entrySet())) {
            Location loc = stand.getKey();
            removeEntity(stand, loc);
            poseStands.remove(loc);
        }
        for (Location loc : new HashSet<>(stands.keySet())) {
            loc.getBlock().setType(Material.AIR);
        }
        spawn.getBlock().setType(Material.AIR);
        start.getBlock().setType(Material.AIR);
        end.getBlock().setType(Material.AIR);
    }

    private void removeEntity(Map.Entry<Location, UUID> stand, Location loc) {
        Entity entity = utils.getEntity(loc.getWorld(), stand.getValue());
        if (entity != null) {
            entity.remove();
        }
    }
}
