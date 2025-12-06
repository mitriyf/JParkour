package ru.mitriyf.jparkour.game.temp.data;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import ru.mitriyf.jparkour.JParkour;

import java.util.Collection;

@Getter
@SuppressWarnings("deprecation")
public class PlayerData {
    private final JParkour plugin;
    private final Player p;
    private final String game;
    private final Location loc;
    private final ItemStack[] contents;
    private final ItemStack[] armorContents;
    private final boolean allowFly, flying;
    private final int foodLevel, level;
    private final double healthMax;
    private final double health;
    private final GameMode gamemode;
    private final float exp, flySpeed, walkSpeed;
    private final Collection<PotionEffect> potionEffects;

    public PlayerData(JParkour plugin, Player p, String game) {
        this.plugin = plugin;
        this.p = p;
        this.game = game;
        loc = p.getLocation();
        contents = p.getInventory().getContents();
        armorContents = p.getInventory().getArmorContents();
        allowFly = p.getAllowFlight();
        flying = p.isFlying();
        foodLevel = p.getFoodLevel();
        healthMax = p.getMaxHealth();
        health = p.getHealth();
        gamemode = p.getGameMode();
        exp = p.getExp();
        potionEffects = p.getActivePotionEffects();
        level = p.getLevel();
        flySpeed = p.getFlySpeed();
        walkSpeed = p.getWalkSpeed();
        removeEffects();
    }

    public void apply() {
        removeEffects();
        p.getInventory().clear();
        p.setFallDistance(0);
        p.spigot().respawn();
        p.teleport(loc);
        p.setGameMode(gamemode);
        p.getInventory().setContents(contents);
        p.getInventory().setArmorContents(armorContents);
        p.updateInventory();
        p.setAllowFlight(allowFly);
        p.setFlying(flying);
        p.setFoodLevel(foodLevel);
        p.setMaxHealth(healthMax);
        p.setHealth(health);
        p.setExp(exp);
        p.setLevel(level);
        p.setWalkSpeed(walkSpeed);
        p.setFlySpeed(flySpeed);
        for (PotionEffect effect : potionEffects) {
            p.addPotionEffect(effect);
        }
    }

    private void removeEffects() {
        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
    }
}
