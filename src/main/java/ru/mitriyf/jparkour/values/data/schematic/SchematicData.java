package ru.mitriyf.jparkour.values.data.schematic;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.schematic.point.SchematicPoint;

import java.util.*;

@Getter
public class SchematicData {
    private final Map<Integer, SchematicPoint> points = new HashMap<>();
    private final Map<Integer, ItemStack> slots = new HashMap<>();
    private final Map<double[], String> stands = new HashMap<>();
    private final Set<ItemStack> restartItems = new HashSet<>();
    private final Set<ItemStack> exitItems = new HashSet<>();
    private final String schematic;
    private final Values values;
    private final String mapId;
    private final Utils utils;
    private long everyTicks;
    private String name, star;
    private EntityType entity;
    private GameMode gameMode;
    private List<String> gameRules;
    private double[] spawn, portal, start, end;
    private double five, four, three, two, one;
    private List<Action> win = new ArrayList<>();
    private List<Action> mEnd = new ArrayList<>();
    private List<Action> mOne = new ArrayList<>();
    private List<Action> mTwo = new ArrayList<>();
    private List<Action> mFour = new ArrayList<>();
    private List<Action> mFive = new ArrayList<>();
    private List<Action> mZero = new ArrayList<>();
    private List<Action> joined = new ArrayList<>();
    private List<Action> inGame = new ArrayList<>();
    private List<Action> kicked = new ArrayList<>();
    private List<Action> mThree = new ArrayList<>();
    private boolean resetVectorFinish, checkUpBlock;
    private List<Action> started = new ArrayList<>();
    private List<Action> restarted = new ArrayList<>();
    private List<Action> damageHeart = new ArrayList<>();
    private List<Action> cooldownRestart = new ArrayList<>();
    private double speed, radiusFinish, radiusStands, speedUp, checkDown, speedDown;
    private boolean fullSlots, pasteAir, foodLevelChange, pickupItem, consumeItem, entityDamage, creatureSpawn;
    private int health, foodLevel, damageBomb, exitTime, timer, yaw, pitch, x, y, z, maxLefts, maxRights, forward;
    private boolean dropItem, entityExplode, entityTarget, itemSpawn, placeBlock, breakBlock, igniteBlock, fromToBlock;
    private boolean fadeBlock, burnBlock, entityChangeBlock, physicsBlock, leavesDecay, multiPlaceBlock, failedDefuseBomb;

    public SchematicData(Values values, YamlConfiguration cfg, String schematicId, String mapId) {
        this.values = values;
        this.mapId = mapId;
        utils = values.getUtils();
        schematic = schematicId;
        setupSettings(cfg);
        ConfigurationSection messages = cfg.getConfigurationSection("messages");
        setupMessages(messages);
    }

    private void setupSettings(YamlConfiguration cfg) {
        ConfigurationSection schematicSection = cfg.getConfigurationSection("schematic");
        name = schematicSection.getString("name");
        star = schematicSection.getString("star");
        pasteAir = schematicSection.getBoolean("pasteAir");
        gameRules = schematicSection.getStringList("gameRules");
        ConfigurationSection rulesSection = cfg.getConfigurationSection("rules");
        setupRules(rulesSection);
        ConfigurationSection entitySection = cfg.getConfigurationSection("entity");
        setupEntity(entitySection);
        ConfigurationSection playerSection = cfg.getConfigurationSection("player");
        health = playerSection.getInt("health");
        foodLevel = playerSection.getInt("foodLevel");
        gameMode = GameMode.valueOf(playerSection.getString("gameMode"));
        damageBomb = playerSection.getInt("damageBomb");
        failedDefuseBomb = playerSection.getBoolean("failedDefuseBomb");
        fullSlots = playerSection.getBoolean("fullSlots");
        radiusFinish = playerSection.getDouble("radiusFinish");
        exitTime = playerSection.getInt("exitTime");
        ConfigurationSection coords = cfg.getConfigurationSection("coords");
        x = coords.getInt("x");
        y = coords.getInt("y");
        z = coords.getInt("z");
        ConfigurationSection locationSection = cfg.getConfigurationSection("location");
        yaw = locationSection.getInt("yaw");
        pitch = locationSection.getInt("pitch");
        ConfigurationSection locs = cfg.getConfigurationSection("locs");
        generateLocs(locs);
        generateItems(values.getItemSlots());
        ConfigurationSection standsSection = cfg.getConfigurationSection("stands");
        generateStands(standsSection);
        ConfigurationSection stars = cfg.getConfigurationSection("stars");
        setStars(stars);
    }

    private void setupEntity(ConfigurationSection entitySection) {
        entity = EntityType.valueOf(entitySection.getString("type"));
        speed = entitySection.getDouble("speed");
        everyTicks = entitySection.getLong("everyTicks");
        ConfigurationSection runSection = entitySection.getConfigurationSection("run");
        resetVectorFinish = runSection.getBoolean("resetVectorFinish");
        ConfigurationSection directionSection = runSection.getConfigurationSection("directions");
        ConfigurationSection upSection = directionSection.getConfigurationSection("up");
        speedUp = upSection.getDouble("speed");
        ConfigurationSection blockChecksSection = upSection.getConfigurationSection("blockChecks");
        forward = blockChecksSection.getInt("forward");
        checkUpBlock = blockChecksSection.getBoolean("upBlock");
        ConfigurationSection downSection = directionSection.getConfigurationSection("down");
        checkDown = downSection.getDouble("check");
        speedDown = downSection.getDouble("speed");
    }

    private void setupRules(ConfigurationSection rules) {
        foodLevelChange = rules.getBoolean("foodLevelChange");
        pickupItem = rules.getBoolean("pickupItem");
        consumeItem = rules.getBoolean("consumeItem");
        dropItem = rules.getBoolean("dropItem");
        entityDamage = rules.getBoolean("entityDamage");
        creatureSpawn = rules.getBoolean("creatureSpawn");
        entityExplode = rules.getBoolean("entityExplode");
        entityTarget = rules.getBoolean("entityTarget");
        itemSpawn = rules.getBoolean("itemSpawn");
        placeBlock = rules.getBoolean("placeBlock");
        breakBlock = rules.getBoolean("breakBlock");
        burnBlock = rules.getBoolean("burnBlock");
        igniteBlock = rules.getBoolean("igniteBlock");
        fromToBlock = rules.getBoolean("fromToBlock");
        entityChangeBlock = rules.getBoolean("entityChangeBlock");
        physicsBlock = rules.getBoolean("physicsBlock");
        fadeBlock = rules.getBoolean("fadeBlock");
        leavesDecay = rules.getBoolean("leavesDecay");
        multiPlaceBlock = rules.getBoolean("multiPlaceBlock");
    }

    private void generateLocs(ConfigurationSection locs) {
        spawn = toDouble(locs.getString("spawn"));
        portal = toDouble(locs.getString("portal"));
        start = toDouble(locs.getString("start"));
        end = toDouble(locs.getString("end"));
        ConfigurationSection pointsSection = locs.getConfigurationSection("points");
        for (int i = 1; i <= pointsSection.getKeys(false).size(); i++) {
            ConfigurationSection pointSection = pointsSection.getConfigurationSection(String.valueOf(i));
            if (pointSection == null) {
                return;
            }
            points.put(i, new SchematicPoint(this, pointSection));
        }
    }

    private void generateItems(FileConfiguration slotsConfig) {
        ConfigurationSection items = slotsConfig.getConfigurationSection("schematics." + mapId);
        if (items == null) {
            items = slotsConfig.createSection("schematics." + mapId);
            try {
                String path = items.getCurrentPath();
                utils.saveItem(null, path, 0, "diamondSword", Material.DIAMOND_SWORD);
                utils.saveItem(null, path, 1, "gSword", utils.getGSword());
            } catch (Exception e) {
                values.getLogger().warning("Error save slots.yml. Error: " + e);
            }
        }
        for (String s : items.getKeys(false)) {
            ConfigurationSection slot = items.getConfigurationSection(s);
            ItemStack item = values.setItemData(slot, slots);
            if (slot.getBoolean("exit")) {
                exitItems.add(item);
            } else if (slot.getBoolean("restart")) {
                restartItems.add(item);
            }
        }
    }

    private void generateStands(ConfigurationSection st) {
        radiusStands = st.getDouble("radiusStands");
        timer = st.getInt("timer");
        for (String s : st.getStringList("locs")) {
            String[] n = s.split(";");
            stands.put(toDouble(s.replace(n[0] + ";", "")), n[0]);
        }
        try {
            for (String stand : stands.values()) {
                if (values.getStands().get(stand).getType().equalsIgnoreCase("use")) {
                    maxLefts++;
                }
            }
        } catch (Exception e) {
            values.getLogger().warning("An error was detected in the " + schematic + " (yml) config. Get Stand type error: " + e);
        }
        maxRights = stands.size() - maxLefts;
    }

    private void setupMessages(ConfigurationSection msg) {
        ConfigurationSection actions = msg.getConfigurationSection("actions");
        joined = getActionList(actions.getStringList("joined"));
        started = getActionList(actions.getStringList("started"));
        inGame = getActionList(actions.getStringList("ingame"));
        mEnd = getActionList(actions.getStringList("end"));
        restarted = getActionList(actions.getStringList("restarted"));
        cooldownRestart = getActionList(actions.getStringList("cooldownRestart"));
        kicked = getActionList(actions.getStringList("kicked"));
        damageHeart = getActionList(actions.getStringList("damageHeart"));
        win = getActionList(actions.getStringList("win"));
        ConfigurationSection stars = msg.getConfigurationSection("stars");
        mFive = getActionList(stars.getStringList("5"));
        mFour = getActionList(stars.getStringList("4"));
        mThree = getActionList(stars.getStringList("3"));
        mTwo = getActionList(stars.getStringList("2"));
        mOne = getActionList(stars.getStringList("1"));
        mZero = getActionList(stars.getStringList("0"));
    }

    public void sendMessage(Player p, int s) {
        if (s >= 5) {
            utils.sendMessage(p, mFive);
        } else if (s == 4) {
            utils.sendMessage(p, mFour);
        } else if (s == 3) {
            utils.sendMessage(p, mThree);
        } else if (s == 2) {
            utils.sendMessage(p, mTwo);
        } else if (s == 1) {
            utils.sendMessage(p, mOne);
        } else {
            utils.sendMessage(p, mZero);
        }
    }

    public double getAccuracy(int lefts, int rights) {
        double rig = (double) rights / maxRights;
        double lef = (double) lefts / maxLefts;
        double full = (rig + lef) / 2;
        return (double) Math.round(full * 100) / 100;
    }

    public int getStars(double accuracy) {
        if (accuracy >= five) {
            return 5;
        } else if (accuracy >= four) {
            return 4;
        } else if (accuracy >= three) {
            return 3;
        } else if (accuracy >= two) {
            return 2;
        } else if (accuracy >= one) {
            return 1;
        } else {
            return 0;
        }
    }

    public double[] toDouble(String id) {
        String[] strings = id.split(";");
        double[] doubles = new double[strings.length];
        for (int i = 0; i < strings.length; i++) {
            try {
                doubles[i] = Double.parseDouble(strings[i]);
            } catch (Exception e) {
                values.getLogger().warning("Error parsing double. Error string: " + id + ". Error: " + e);
                doubles[i] = 0.0;
            }
        }
        return doubles;
    }

    private void setStars(ConfigurationSection stars) {
        five = stars.getDouble("5");
        four = stars.getDouble("4");
        three = stars.getDouble("3");
        two = stars.getDouble("2");
        one = stars.getDouble("1");
    }

    private List<Action> getActionList(List<String> s) {
        return values.getActionList(s);
    }
}
