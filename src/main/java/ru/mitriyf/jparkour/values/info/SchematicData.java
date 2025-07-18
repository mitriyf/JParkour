package ru.mitriyf.jparkour.values.info;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class SchematicData {
    private final String name;
    private final String star;
    private final Map<double[], String> stands = new HashMap<>();
    private final Map<Integer, ItemStack> slots = new HashMap<>();
    private final EntityType entity;
    private final double speed;
    private final double radiusFinish;
    private final double radiusStands;
    private final double loss;
    private final String schematic;
    private final double[] spawn;
    private final double[] portal;
    private final double[] start;
    private final double[] end;
    private final int health;
    private final int damageBomb;
    private final int timer;
    private final int yaw;
    private final int north;
    private final int x;
    private final int y;
    private final int z;
    private final int maxLefts, maxRights;
    private final double five, four, three, two, one;
    public SchematicData(Map<String, StandData> standMap, Utils utils, YamlConfiguration cfg, String schematicId) {
        name = cfg.getString("name");
        star = cfg.getString("star");
        ConfigurationSection e = cfg.getConfigurationSection("entity");
        entity = EntityType.valueOf(e.getString("type"));
        speed = e.getDouble("speed");
        ConfigurationSection p = cfg.getConfigurationSection("player");
        health = p.getInt("health");
        damageBomb = p.getInt("damageBomb");
        radiusFinish = p.getDouble("radiusFinish");
        ConfigurationSection coords = cfg.getConfigurationSection("coords");
        x = coords.getInt("x");
        y = coords.getInt("y");
        z = coords.getInt("z");
        schematic = schematicId;
        ConfigurationSection lc = cfg.getConfigurationSection("location");
        yaw = lc.getInt("yaw");
        north = lc.getInt("north");
        loss = lc.getDouble("loss");
        ConfigurationSection locs = cfg.getConfigurationSection("locs");
        spawn = toDouble(locs.getString("spawn"));
        portal = toDouble(locs.getString("portal"));
        start = toDouble(locs.getString("start"));
        end = toDouble(locs.getString("end"));
        ConfigurationSection items = cfg.getConfigurationSection("items");
        items.getKeys(false).forEach(s -> slots.put(items.getConfigurationSection(s).getInt("slot"),
                utils.generateItem(items.getConfigurationSection(s))));
        ConfigurationSection st = cfg.getConfigurationSection("stands");
        radiusStands = st.getDouble("radiusStands");
        timer = st.getInt("timer");
        st.getStringList("locs").forEach(s -> {
            String[] n = s.split(";");
            stands.put(toDouble(s.replace(n[0] + ";", "")), n[0]);
        });
        maxLefts = (int) stands.values().stream().filter(stand ->
                standMap.get(stand).getType().equalsIgnoreCase("use")).count();
        maxRights = stands.size() - maxLefts;
        ConfigurationSection stars = cfg.getConfigurationSection("stars");
        five = stars.getDouble("5");
        four = stars.getDouble("4");
        three = stars.getDouble("3");
        two = stars.getDouble("2");
        one = stars.getDouble("1");
    }
    public int getStars(int lefts, int rights) {
        double rig = (double) rights / maxRights;
        double lef = (double) lefts / maxLefts;
        double all = (rig + lef) / 2;
        if (all >= five) return 5;
        else if (all >= four) return 4;
        else if (all >= three) return 3;
        else if (all >= two) return 2;
        else if (all >= one) return 1;
        else return 0;
    }
    private double[] toDouble(String id) {
        return Arrays.stream(id.split(";"))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
