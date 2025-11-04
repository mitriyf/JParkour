package ru.mitriyf.jparkour.game.temp.editor;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.temp.data.LocationsData;
import ru.mitriyf.jparkour.game.temp.task.stand.StandActive;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class Editor {
    private final Map<Location, UUID> glowStands = new HashMap<>();
    private final Map<Location, String> stands = new HashMap<>();
    private final Material material = Material.COAL_ORE;
    private final Material glass = Material.GLASS;
    private final String standCustomName = "§c§l★   STAND   ★";
    private final String spawnCustomName = "§a§l❤   SPAWN   ❤";
    private final String portalCustomName = "§5§l⚓   PORTAL   ⚓";
    private final String startCustomName = "§e§l⚡   START   ⚡";
    private final String endCustomName = "§8§l⌛    END    ⌛";
    private final Game game;
    private final Utils utils;
    private final Values values;
    private final LocationsData locs;
    private Location spawn, portal, start, end;
    private Block selectedBlockAxe;
    private Location pose1, pose2, pose3;

    public Editor(Game game) {
        this.game = game;
        values = game.getValues();
        utils = game.getUtils();
        locs = game.getLocs();
        if (game.getInfo() != null) {
            pose3 = locs.getDefaultLocation();
            load();
            setBlockStands();
        }
    }

    private void load() {
        spawn = locs.getSpawn().getBlock().getLocation();
        portal = locs.getPortal().getBlock().getLocation();
        start = locs.getStart().getBlock().getLocation();
        end = locs.getEnd().getBlock().getLocation();
        game.getScheduler().runTask(game.getPlugin(), () -> {
            for (Map.Entry<Location, StandActive> stand : locs.getStands().entrySet()) {
                Block b = stand.getKey().getBlock();
                stands.put(b.getLocation(), stand.getValue().getType());
            }
        });
    }

    public void save(String name) {
        File fileConfig = new File(values.getDataFolder(), values.getSchematicsDir() + name + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(fileConfig);
        if (spawn != null) {
            ConfigurationSection locs = cfg.getConfigurationSection("locs");
            setLocs(locs);
        }
        ConfigurationSection standsSection = cfg.getConfigurationSection("stands");
        setStands(standsSection);
        try {
            cfg.save(fileConfig);
            values.setup(false);
        } catch (Exception e) {
            values.getLogger().warning("Error save " + name + ".yml.\nError: " + e);
        }
        setBlockStands();
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

    public void setBlockStand(Location loc, String typeStack) {
        stands.put(loc, typeStack);
        createArmorStand(loc, standCustomName);
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

    private void setLoc(Location loc, Location newLoc, String name, boolean setBlock) {
        if (loc != null) {
            if (setBlock && !loc.equals(newLoc)) {
                loc.getBlock().setType(Material.AIR);
            }
            removeBlockStand(loc);
        }
        if (newLoc != null) {
            createArmorStand(newLoc, name);
        }
    }

    public void removeBlockStand(Location loc) {
        stands.remove(loc);
        UUID uuid = glowStands.get(loc);
        if (uuid != null) {
            utils.getEntity(loc.getWorld(), uuid).remove();
        }
        glowStands.remove(loc);
    }

    private void setBlockStands() {
        game.getScheduler().runTask(game.getPlugin(), () -> {
            for (Location loc : stands.keySet()) {
                loc.getBlock().setType(material);
                createArmorStand(loc, standCustomName);
            }
            spawn.getBlock().setType(glass);
            createArmorStand(spawn, spawnCustomName);
            createArmorStand(portal, portalCustomName);
            start.getBlock().setType(glass);
            createArmorStand(start, startCustomName);
            end.getBlock().setType(glass);
            createArmorStand(end, endCustomName);
        });
    }

    private void createArmorStand(Location loc, String customName) {
        Location locStand = loc.clone().add(0.5, 0.1, 0.5);
        ArmorStand stand = (ArmorStand) locStand.getWorld().spawnEntity(locStand, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setArms(false);
        stand.setSmall(true);
        stand.setMarker(true);
        stand.setCustomName(customName);
        stand.setCustomNameVisible(true);
        glowStands.put(loc, stand.getUniqueId());
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
            utils.getEntity(loc.getWorld(), stand.getValue()).remove();
            glowStands.remove(loc);
        }
        for (Location loc : stands.keySet()) {
            loc.getBlock().setType(Material.AIR);
        }
        spawn.getBlock().setType(Material.AIR);
        start.getBlock().setType(Material.AIR);
        end.getBlock().setType(Material.AIR);
    }
}
