package ru.mitriyf.jparkour.values.info;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@Getter
public class StandData {
    private final ItemStack block;
    private final ItemStack in_block;
    private final String type;
    private final ItemStack item;
    private final String click_type;

    public StandData(ConfigurationSection id) {
        type = id.getString("type");
        click_type = id.getString("click_type");
        item = getStack(id.getString("item"), 0);
        block = getStack(id.getString("block"), id.getInt("blockData"));
        in_block = getStack(id.getString("in_block"), id.getInt("in_blockData"));
    }

    public ArmorStand generateBigStand(Location loc) {
        loc = loc.clone().add(0, -0.75, 0);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setHelmet(block);
        setSettings(stand);
        return stand;
    }

    public ArmorStand generateSmallStand(Location loc) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setSmall(true);
        stand.setHelmet(in_block);
        setSettings(stand);
        return stand;
    }

    private void setSettings(ArmorStand stand) {
        stand.setCustomName(click_type);
        if (!type.equalsIgnoreCase("use")) stand.setChestplate(getStack("TNT", 0));
        stand.setBoots(item);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setArms(false);
    }

    @SuppressWarnings("deprecation")
    private ItemStack getStack(String m, int data) {
        ItemStack stack = new ItemStack(Material.valueOf(m));
        stack.setDurability((short) data);
        return stack;
    }
}
