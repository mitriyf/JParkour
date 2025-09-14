package ru.mitriyf.jparkour.cmd.item;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.Map;
import java.util.Set;

public class ItemEditor {
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;

    public ItemEditor(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
    }

    public void checkItemCommand(CommandSender s, String[] args) {
        if (!s.hasPermission("jparkour.item")) {
            utils.sendMessage(s, values.getNoperm());
            return;
        }
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "add": {
                    if (!(s instanceof Player)) {
                        s.sendMessage("§cYou console!");
                        return;
                    }
                    Player p = (Player) s;
                    addItem(p, args);
                    return;
                }
                case "list": {
                    listItems(s, args);
                    return;
                }
                case "info": {
                    infoItem(s, args);
                    return;
                }
                case "remove": {
                    removeItem(s, args);
                    return;
                }
                default: {
                    sendItemHelp(s);
                    return;
                }
            }
        }
        sendItemHelp(s);
    }

    @SuppressWarnings("deprecation")
    private void addItem(Player p, String[] args) {
        if (args.length == 5) {
            if (p.getItemInHand().getType() == Material.AIR) {
                p.sendMessage("§cYour hand is empty. The item is accepted.");
            }
            if (!args[2].equals("default") && !values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[2])) {
                p.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
                return;
            }
            String sPath = args[2].equals("default") ? "default." : "schematics." + args[2];
            int slot = Integer.parseInt(args[3]);
            Set<String> items = values.getItemSlots().getConfigurationSection(sPath).getKeys(false);
            if (items.contains(args[4])) {
                p.sendMessage("§cThe item name is already taken. Change the name or delete that item.");
                return;
            }
            for (String i : items) {
                if (slot == values.getItemSlots().getInt(sPath + "." + i + ".slot")) {
                    p.sendMessage("§cThe item slot is already taken. Change the slot or delete item " + i);
                    return;
                }
            }
            String pItem = sPath + "." + args[4];
            FileConfiguration slots = values.getItemSlots();
            slots.set(pItem + ".slot", slot);
            slots.set(pItem + ".type", "itemstack");
            slots.set(pItem + ".item", p.getItemInHand());
            try {
                slots.save(values.getSlotsFile());
                p.sendMessage("§aThe " + args[4] + " item was created successfully!\nUse: /jparkour reload - Apply the new settings.");
            } catch (Exception e) {
                plugin.getLogger().warning("Error item " + args[3] + " save slots.yml: " + e);
            }
        } else {
            p.sendMessage("§aUse: /jparkour item add default/schematicName slot itemName §f- Add the item in your hand to the selected schematic.");
        }
    }

    private void listItems(CommandSender s, String[] args) {
        if (args.length == 3) {
            if (args[2].equals("default") || values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[2])) {
                String id = args[2].equals("default") ? values.getItemSlots().getConfigurationSection("default").getKeys(false).toString() : values.getItemSlots().getConfigurationSection("schematics." + args[2]).getKeys(false).toString();
                s.sendMessage("§aList items " + args[2] + " schematic:\n§f" + id);
                return;
            }
        }
        s.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
    }

    private void infoItem(CommandSender s, String[] args) {
        if (args.length == 4) {
            if (args[2].equals("default") || values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[2])) {
                ConfigurationSection schematics = args[2].equals("default") ? values.getItemSlots().getConfigurationSection("default") : values.getItemSlots().getConfigurationSection("schematics." + args[2]);
                ConfigurationSection item = schematics.getConfigurationSection(args[3]);
                if (item != null) {
                    s.sendMessage("§aItem " + args[3] + ":\n");
                    for (Map.Entry<String, Object> section : item.getValues(true).entrySet()) {
                        s.sendMessage("§f" + section.getKey() + ": " + section.getValue());
                    }
                    return;
                }
                s.sendMessage("§aList items " + args[2] + " schematic:\n§f" + schematics.getKeys(false).toString());
                return;
            }
            s.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
            return;
        }
        s.sendMessage("§a/jparkour item info default/schematicName itemName §f- Get the item from the selected schematic.");
    }

    private void removeItem(CommandSender s, String[] args) {
        if (args.length == 4) {
            if (args[2].equals("default") || values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[2])) {
                ConfigurationSection schematics = args[2].equals("default") ? values.getItemSlots().getConfigurationSection("default") : values.getItemSlots().getConfigurationSection("schematics." + args[2]);
                ConfigurationSection item = schematics.getConfigurationSection(args[3]);
                if (item != null) {
                    FileConfiguration slots = values.getItemSlots();
                    slots.set(item.getCurrentPath(), null);
                    try {
                        slots.save(values.getSlotsFile());
                        s.sendMessage("§aThe item " + args[3] + " has been successfully deleted.\nUse: /jparkour reload - Apply the new settings.");
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error item " + args[3] + " save slots.yml: " + e);
                    }
                    return;
                }
                s.sendMessage("§aList items " + args[2] + " schematic:\n§f" + schematics.getKeys(false).toString());
                return;
            }
            s.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
            return;
        }
        s.sendMessage("§a/jparkour item remove default/schematicName itemName §f- Remove an item from the selected schematic.");
    }

    private void sendItemHelp(CommandSender s) {
        s.sendMessage("§aJParkour Item Help:\n");
        s.sendMessage("§a/jparkour item add default/schematicName slot itemName §f- Add the item in your hand to the selected schematic.");
        s.sendMessage("§a/jparkour item list §f- Get a list of schematics.");
        s.sendMessage("§a/jparkour item list default/schematicName §f- Get a list of items in the selected schematic.");
        s.sendMessage("§a/jparkour item info default/schematicName itemName §f- Get the item from the selected schematic.");
        s.sendMessage("§a/jparkour item remove default/schematicName itemName §f- Remove an item from the selected schematic.");
    }
}
