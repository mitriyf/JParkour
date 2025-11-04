package ru.mitriyf.jparkour.values.data;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Logger;

@Getter
public class StandData {
    private final ItemStack block;
    private final ItemStack in_block;
    private final ItemStack item;
    private final String click_type;
    private final Logger logger;
    private final String type;
    private boolean bomb;

    public StandData(ConfigurationSection id, Logger logger) {
        this.logger = logger;
        type = id.getString("type");
        click_type = id.getString("click_type");
        item = getStack(id.getString("item"), 0);
        block = getStack(id.getString("block"), id.getInt("blockData"));
        in_block = getStack(id.getString("in_block"), id.getInt("in_blockData"));
        if (!type.equalsIgnoreCase("use")) {
            bomb = true;
        }
    }

    public UUID generateBigStand(Location loc) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setHelmet(block);
        setSettings(stand);
        return stand.getUniqueId();
    }

    public UUID generateSmallStand(Location loc) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setSmall(true);
        stand.setHelmet(in_block);
        setSettings(stand);
        return stand.getUniqueId();
    }

    private void setSettings(ArmorStand stand) {
        stand.setCustomName(click_type);
        stand.setBoots(item);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setArms(false);
    }

    @SuppressWarnings("deprecation")
    private ItemStack getStack(String m, int data) {
        try {
            ItemStack stack = new ItemStack(Material.valueOf(m));
            stack.setDurability((short) data);
            return stack;
        } catch (Exception e) {
            logger.warning("One of the stands failed to load ItemStack. Error: " + e);
            return new ItemStack(Material.BARRIER);
        }
    }
}
