package ru.mitriyf.jparkour.utils.common;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.model.PartyData;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class CommonUtils {
    private final BukkitScheduler scheduler;
    private final JParkour plugin;
    private final Values values;
    private final Logger logger;
    private final Utils utils;

    public CommonUtils(Utils utils, JParkour plugin) {
        this.utils = utils;
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        scheduler = plugin.getServer().getScheduler();
    }

    public void sendRoom(Player player, String message) {
        String text = utils.formatString(message);
        UUID uuid = player.getUniqueId();
        Game game = plugin.getGameManager().getGame(uuid);
        if (game == null) {
            player.sendMessage(text);
            return;
        }
        for (Player member : game.getPlayers()) {
            member.sendMessage(text);
        }
    }

    public void sendParty(Player player, String message) {
        String text = utils.formatString(message);
        for (PartyData partyData : plugin.getPartyManager().getPartyDataMap().values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(player.getUniqueId())) {
                for (UUID member : members) {
                    plugin.getServer().getPlayer(member).sendMessage(text);
                }
                return;
            }
        }
        player.sendMessage(text);
    }

    public void broadcast(String message) {
        String text = utils.formatString(message);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(text);
        }
    }

    public void dispatchConsole(String cmd) {
        scheduler.runTaskLater(plugin, () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd), 0);
    }

    public ItemStack generateItem(ConfigurationSection slot) {
        try {
            String type = slot.getString("type");
            if (type != null && type.equalsIgnoreCase("itemstack")) {
                return slot.getItemStack("item");
            }
            slot = slot.getConfigurationSection("item");
            ItemStack item = new ItemStack(Material.valueOf(slot.getString("type")));
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            String name = slot.getString("name");
            if (name != null) {
                meta.setDisplayName(utils.formatString(name));
            }
            List<String> lore = slot.getStringList("lore");
            if (lore != null) {
                List<String> l = new ArrayList<>();
                for (String t : lore) {
                    l.add(utils.formatString(t));
                }
                meta.setLore(l);
            }
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            logger.warning("Error generate item " + slot.toString() + ": " + e);
            return new ItemStack(Material.AIR);
        }
    }

    @SuppressWarnings("deprecation")
    public void saveItem(Player p, String sPath, int slot, String itemName, Material material) {
        String item = sPath + "." + itemName;
        FileConfiguration slots = values.getItemSlots();
        slots.set(item + ".slot", slot);
        if (p != null) {
            slots.set(item + ".type", "itemstack");
            slots.set(item + ".item", p.getItemInHand());
        } else {
            slots.set(item + ".type", "default");
            slots.set(item + ".item.type", material.toString());
        }
        try {
            slots.save(values.getSlotsFile());
            if (p != null) {
                p.sendMessage("§aThe " + itemName + " item was created successfully!\nUse: /jparkour reload - Apply the new settings.");
            }
        } catch (Exception e) {
            logger.warning("Error item " + itemName + " save slots.yml: " + e);
        }
    }
}
