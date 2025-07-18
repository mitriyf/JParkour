package ru.mitriyf.jparkour.values.info;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;

@Getter
public class PlayerData {
    private final JParkour plugin;
    private final Values values;
    private final Player p;
    private final String game;
    private final Location loc;
    private final ItemStack[] contents;
    private final File file;
    private final boolean allowFly;
    private final int foodLevel;
    private final double healthMax;
    private final double healthLevel;
    private final double health;
    private final GameMode gamemode;
    private final float exp;
    public PlayerData(JParkour plugin, Player p, String game) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.p = p;
        this.game = game;
        this.file = new File(plugin.getDataFolder(), "playerData/" + p.getUniqueId().toString() + ".yml");
        loc = p.getLocation();
        contents = p.getInventory().getContents();
        allowFly = p.getAllowFlight();
        foodLevel = p.getFoodLevel();
        healthMax = p.getMaxHealth();
        healthLevel = p.getHealthScale();
        health = p.getHealth();
        gamemode = p.getGameMode();
        exp = p.getExp();
    }
    public void apply() {
        p.getInventory().clear();
        p.spigot().respawn();
        p.teleport(loc);
        p.getInventory().setContents(contents);
        p.setAllowFlight(allowFly);
        p.setFoodLevel(foodLevel);
        p.setMaxHealth(healthMax);
        p.setHealthScale(healthLevel);
        p.setHealth(health);
        p.setGameMode(gamemode);
        p.setExp(exp);
        p.updateInventory();
        if (!file.delete()) file.deleteOnExit();
    }
}
