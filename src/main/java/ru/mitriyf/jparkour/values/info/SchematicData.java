package ru.mitriyf.jparkour.values.info;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.values.Values;

import java.util.*;

@Getter
public class SchematicData {
    private final Utils utils;
    private final Values values;
    private final String mapId;
    private final Map<double[], String> stands = new HashMap<>();
    private final Map<Integer, ItemStack> slots = new HashMap<>();
    private final String schematic;
    private String name, star;
    private EntityType entity;
    private long everyTicks;
    private boolean fullSlots;
    private double speed, radiusFinish, radiusStands;
    private double[] spawn, portal, start, end;
    private int health, damageBomb, exitTime, timer, yaw, north, x, y, z, maxLefts, maxRights;
    private double five, four, three, two, one;
    private List<Action> damageHeart = new ArrayList<>();
    private List<Action> joined = new ArrayList<>();
    private List<Action> inGame = new ArrayList<>();
    private List<Action> started = new ArrayList<>();
    private List<Action> kicked = new ArrayList<>();
    private List<Action> restarted = new ArrayList<>();
    private List<Action> mEnd = new ArrayList<>();
    private List<Action> win = new ArrayList<>();
    private List<Action> mZero = new ArrayList<>();
    private List<Action> mOne = new ArrayList<>();
    private List<Action> mTwo = new ArrayList<>();
    private List<Action> mThree = new ArrayList<>();
    private List<Action> mFour = new ArrayList<>();
    private List<Action> mFive = new ArrayList<>();

    public SchematicData(Values values, YamlConfiguration cfg, String schematicId, String mapId) {
        this.values = values;
        this.utils = values.getUtils();
        this.mapId = mapId;
        schematic = schematicId;
        setupSettings(cfg);
        ConfigurationSection messages = cfg.getConfigurationSection("messages");
        setupMessages(messages);
    }

    private void setupSettings(YamlConfiguration cfg) {
        name = cfg.getString("name");
        star = cfg.getString("star");
        ConfigurationSection e = cfg.getConfigurationSection("entity");
        entity = EntityType.valueOf(e.getString("type"));
        speed = e.getDouble("speed");
        everyTicks = e.getLong("everyTicks");
        ConfigurationSection p = cfg.getConfigurationSection("player");
        health = p.getInt("health");
        damageBomb = p.getInt("damageBomb");
        fullSlots = p.getBoolean("fullSlots");
        radiusFinish = p.getDouble("radiusFinish");
        exitTime = p.getInt("exitTime");
        ConfigurationSection coords = cfg.getConfigurationSection("coords");
        x = coords.getInt("x");
        y = coords.getInt("y");
        z = coords.getInt("z");
        ConfigurationSection lc = cfg.getConfigurationSection("location");
        yaw = lc.getInt("yaw");
        north = lc.getInt("north");
        ConfigurationSection locs = cfg.getConfigurationSection("locs");
        spawn = toDouble(locs.getString("spawn"));
        portal = toDouble(locs.getString("portal"));
        start = toDouble(locs.getString("start"));
        end = toDouble(locs.getString("end"));
        ConfigurationSection items = values.getItemSlots().getConfigurationSection("schematics." + mapId);
        items.getKeys(false).forEach(s -> slots.put(items.getConfigurationSection(s).getInt("slot"), utils.generateItem(items.getConfigurationSection(s))));
        ConfigurationSection st = cfg.getConfigurationSection("stands");
        radiusStands = st.getDouble("radiusStands");
        timer = st.getInt("timer");
        st.getStringList("locs").forEach(s -> {
            String[] n = s.split(";");
            stands.put(toDouble(s.replace(n[0] + ";", "")), n[0]);
        });
        maxLefts = (int) stands.values().stream().filter(stand -> values.getStands().get(stand).getType().equalsIgnoreCase("use")).count();
        maxRights = stands.size() - maxLefts;
        ConfigurationSection stars = cfg.getConfigurationSection("stars");
        five = stars.getDouble("5");
        four = stars.getDouble("4");
        three = stars.getDouble("3");
        two = stars.getDouble("2");
        one = stars.getDouble("1");
    }

    private void setupMessages(ConfigurationSection msg) {
        ConfigurationSection actions = msg.getConfigurationSection("actions");
        joined = getActionList(actions.getStringList("joined"));
        started = getActionList(actions.getStringList("started"));
        inGame = getActionList(actions.getStringList("ingame"));
        mEnd = getActionList(actions.getStringList("end"));
        restarted = getActionList(actions.getStringList("restarted"));
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

    private List<Action> getActionList(List<String> s) {
        return values.getActionList(s);
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

    private double[] toDouble(String id) {
        return Arrays.stream(id.split(";")).mapToDouble(Double::parseDouble).toArray();
    }
}
