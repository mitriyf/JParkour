package ru.mitriyf.jparkour.utils;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.mitriyf.jparkour.JParkour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private final JParkour plugin;
    private final ThreadLocalRandom rnd;
    public Utils(JParkour plugin) {
        this.plugin = plugin;
        this.rnd = plugin.getRnd();
    }
    // Minecraft
    private final Pattern PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public String hex(String message) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = PATTERN.matcher(message);
        if (plugin.getVersion_mode() >= 16) {
            while (matcher.find()) {
                String color = matcher.group(1);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : color.toCharArray()) replacement.append('§').append(c);
                matcher.appendReplacement(buffer, replacement.toString());
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString().replace('&', '§');
    }
    public void sendMessage(Player p, List<String> msg) {
        for (String s : msg) sendMessage(p, s);
    }
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(hex(message));
    }
    public void sendMessage(Player p, String text) {
        if (text == null) return;
        String formatted = text
                .replace("%player%", p.getName());
        if (formatted.contains("%rnd_player%")) {
            List<Player> playersList = new ArrayList<>(Bukkit.getOnlinePlayers());
            String name = playersList.get(rnd.nextInt(playersList.size())).getName();
            formatted = formatted.replace("%rnd_player%", name);
        }
        formatted = PlaceholderAPI.setPlaceholders(p, formatted);
        String lowerCase = formatted.toLowerCase();
        if (!lowerCase.startsWith("[") || lowerCase.startsWith("[message] ")) {
            text = hex(formatted.replace("[message] ", ""));
            p.sendMessage(text);
        }
        else if (lowerCase.startsWith("[player] ")) p.performCommand(hex(formatted.replace("[player] ", "")));
        else if (lowerCase.startsWith("[console] ")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hex(formatted.replace("[console] ", "")));
        else if (lowerCase.startsWith("[broadcast] ")) for (Player pls : Bukkit.getOnlinePlayers()) pls.sendMessage(hex(formatted.replace("[broadcast] ", "")));
        else if (lowerCase.startsWith("[sound] ")) playSound(p, formatted);
        else if (lowerCase.startsWith("[title] ")) {
            String[] title = formatted.replace("[title] ", "").split(";");
            if (title.length == 2) p.sendTitle(hex(title[0]), hex(title[1]));
            else plugin.getLogger().warning("An error occurred when calling title from the configuration. (title;title)");
        }
        else p.sendMessage(hex(text));
    }
    private void playSound(Player p, String formatted) {
        formatted = formatted.replace("[sound] ", "");
        Sound sound;
        int volume = 1;
        int pitch = 1;
        try {
            if (formatted.contains(";")) {
                String[] split = formatted.split(";");
                sound = Sound.valueOf(split[0]);
                volume = Integer.parseInt(split[1]);
                pitch = Integer.parseInt(split[2]);
            }
            else sound = Sound.valueOf(formatted);
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
        catch (Exception e) {
            plugin.getLogger().warning("An error occurred when calling sound from the configuration");
        }
    }
    public void paste(Location loc, String schematicId) {
        File schem = new File(plugin.getDataFolder(), schematicId);
        if (!schem.exists()) {
            plugin.getLogger().warning("File " + schematicId + " not found.");
            return;
        }
        try {
            Vector lc = Vector.toBlockPoint(loc.getX(), loc.getY(), loc.getZ());
            Schematic sch = ClipboardFormats.findByFile(schem).load(schem);
            sch.paste(new BukkitWorld(loc.getWorld()), lc, false, true, null);
        } catch (IOException error) {
            plugin.getLogger().warning("An error occurred while inserting the diagram. Please contact the administrator.\nError: " + error);
        }
    }
    public ItemStack generateItem(ConfigurationSection slot) {
        ItemStack item = new ItemStack(Material.valueOf(slot.getString("type")));
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (slot.getString("name") != null) meta.setDisplayName(hex(slot.getString("name")));
        if (slot.getString("lore") != null) {
            List<String> l = new ArrayList<>();
            for (String t : slot.getStringList("lore")) {
                l.add(hex(t));
            }
            meta.setLore(l);
        }
        item.setItemMeta(meta);
        return item;
    }
    public String repeat(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
