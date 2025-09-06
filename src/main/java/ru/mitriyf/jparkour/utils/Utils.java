package ru.mitriyf.jparkour.utils;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.utils.actions.ActionType;
import ru.mitriyf.jparkour.utils.actions.titles.Title;
import ru.mitriyf.jparkour.utils.actions.titles.versions.Title10;
import ru.mitriyf.jparkour.utils.actions.titles.versions.Title11;
import ru.mitriyf.jparkour.utils.locales.Locale;
import ru.mitriyf.jparkour.utils.locales.versions.Locale12;
import ru.mitriyf.jparkour.utils.locales.versions.Locale13;
import ru.mitriyf.jparkour.utils.schematic.Paste;
import ru.mitriyf.jparkour.utils.schematic.versions.Paste12;
import ru.mitriyf.jparkour.utils.schematic.versions.Paste13;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
    private final BukkitScheduler scheduler;
    private final JParkour plugin;
    private final Values values;
    private boolean aBar = false, bar = false, tit = false;
    @Getter
    private Locale locale;
    private Paste schematic;
    private Title title;

    public Utils(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.scheduler = plugin.getServer().getScheduler();
    }

    public void setup() {
        int version = plugin.getVersion();
        if (version < 8) {
            tit = true;
        }
        if (version < 9) {
            bar = true;
        }
        if (version < 11) {
            aBar = true;
            title = new Title10();
        } else {
            title = new Title11();
        }
        if (version < 13) {
            locale = new Locale12();
            schematic = new Paste12();
        } else {
            locale = new Locale13();
            schematic = new Paste13();
        }
    }

    public void sendMessage(Player p, List<Action> actions) {
        for (Action action : actions) {
            sendPlayer(p, action, null, null);
        }
    }

    public void sendMessage(Player p, List<Action> actions, String[] search, String[] replace) {
        for (Action action : actions) {
            sendPlayer(p, action, search, replace);
        }
    }


    public void sendMessage(CommandSender sender, Map<String, List<Action>> actions) {
        sendMessage(sender, actions, null, null);
    }

    public void sendMessage(CommandSender sender, Map<String, List<Action>> actions, String[] search, String[] replace) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            for (Action action : actions.getOrDefault(locale.player(p), actions.get(""))) {
                sendPlayer(p, action, search, replace);
            }
            return;
        }
        for (Action action : actions.get("")) {
            sendSender(sender, action, search, replace);
        }
    }

    private void sendPlayer(Player p, Action action, String[] search, String[] replace) {
        ActionType type = action.getType();
        String context = replaceEach(action.getContext().replace("%player%", p.getName()), search, replace);
        if (values.isPlaceholderAPI()) {
            context = PlaceholderAPI.setPlaceholders(p, context);
        }
        switch (type) {
            case PLAYER:
                dispatchPlayer(p, context);
                break;
            case TELEPORT:
                teleport(p, context);
                break;
            case CONSOLE:
                dispatchConsole(context);
                break;
            case ACTIONBAR:
                sendActionBar(p, context);
                break;
            case BOSSBAR:
                sendBossbar(p, context);
                break;
            case BROADCAST:
                broadcast(context);
                break;
            case TITLE:
                title(p, context);
                break;
            case SOUND:
                playSound(p, context);
                break;
            case EFFECT:
                giveEffect(p, context);
                break;
            case LOG:
                log(context);
                break;
            default:
                sendMessage(p, context);
                break;
        }
    }

    private void sendSender(CommandSender sender, Action action, String[] search, String[] replace) {
        ActionType type = action.getType();
        String context = replaceEach(action.getContext(), search, replace);
        switch (type) {
            case CONSOLE:
                dispatchConsole(context);
                break;
            case BROADCAST:
                broadcast(context);
                break;
            case LOG:
                log(context);
                break;
            case PLAYER:
            case TITLE:
            case ACTIONBAR:
            case BOSSBAR:
            case EFFECT:
            case TELEPORT:
            case SOUND:
                break;
            default:
                sendMessage(sender, context);
                break;
        }
    }

    private void playSound(Player p, String s) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String[] spl = s.split(";");
                if (spl.length == 0 || spl.length > 4) {
                    plugin.getLogger().warning("Invalid sound. [sound;volume;pitch;delay], error: " + s);
                    return;
                }
                int later = spl.length == 4 ? fInt(spl[3]) : 0;
                Sound sound = Sound.valueOf(spl[0]);
                float volume = spl.length >= 2 ? fFloat(spl[1]) : 1.0F;
                float pitch = spl.length >= 3 ? fFloat(spl[2]) : 1.0F;
                scheduler.runTaskLaterAsynchronously(plugin, () -> p.playSound(p.getLocation(), sound, volume, pitch), later);
            } catch (Exception e) {
                plugin.getLogger().warning("Sound error: " + e);
            }
        });
    }

    private void giveEffect(Player p, String s) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String[] spl = s.split(";");
                if (spl.length == 0 || spl.length > 4) {
                    plugin.getLogger().warning("Invalid effect. [type;duration;amplifier;delay], error: " + s);
                    return;
                }
                int later = spl.length == 4 ? fInt(spl[3]) : 0;
                PotionEffectType type = PotionEffectType.getByName(spl[0]);
                int duration = spl.length >= 2 ? fInt(spl[1]) : 1;
                int amplifier = spl.length >= 3 ? fInt(spl[2]) : 1;
                PotionEffect effect = new PotionEffect(type, duration, amplifier);
                scheduler.runTaskLater(plugin, () -> p.addPotionEffect(effect), later);
            } catch (Exception e) {
                plugin.getLogger().warning("Effect error: " + e);
            }
        });
    }

    private void teleport(Player p, String s) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String[] spl = s.split(";");
                if (spl.length == 0 || spl.length > 7) {
                    plugin.getLogger().warning("Invalid location. [world;x;y;z;yaw;pitch;delay], error: " + s);
                    return;
                }
                int later = spl.length == 7 ? fInt(spl[6]) : 0;
                World w = Bukkit.getWorld(spl[0]);
                if (w == null) {
                    w = Bukkit.getWorld("world");
                }
                double x = spl.length >= 2 ? fDouble(spl[1]) : 0;
                double y = spl.length >= 3 ? fDouble(spl[2]) : 80;
                double z = spl.length >= 4 ? fDouble(spl[3]) : 0;
                float yaw = spl.length >= 5 ? fFloat(spl[4]) : 180;
                float pitch = spl.length >= 6 ? fFloat(spl[5]) : 0;
                Location loc = new Location(w, x, y, z, yaw, pitch);
                scheduler.runTaskLater(plugin, () -> p.teleport(loc), later);
            } catch (Exception e) {
                plugin.getLogger().warning("Teleport error: " + e);
            }
        });
    }

    private void dispatchConsole(String cmd) {
        scheduler.runTaskLater(plugin, () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd), 0);
    }

    private void dispatchPlayer(Player p, String cmd) {
        scheduler.runTaskLater(plugin, () -> p.performCommand(cmd), 0);
    }

    @SuppressWarnings("deprecation")
    private void sendActionBar(Player p, String bar) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                if (aBar) {
                    plugin.getLogger().warning("Invalid actionbar. [message]. For 1.11+, error: " + bar);
                    return;
                }
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(fStr(bar)));
            } catch (Exception e) {
                plugin.getLogger().warning("ActionBar error: " + e);
            }
        });
    }

    private void broadcast(String message) {
        String formatted = fStr(message);
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.sendMessage(formatted);
        }
    }

    private void title(Player p, String titleS) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String[] t = titleS.split(";");
                if (t.length == 0 || t.length > 5 || tit) {
                    plugin.getLogger().warning("Invalid title. [title;subtitle;int;int1;int2]. For 1.8+, error: " + title);
                    return;
                }
                String titleF = fStr(t[0]);
                String subtitle = t.length >= 2 ? fStr(t[1]) : "";
                int fadeIn = t.length >= 3 ? fInt(t[2]) : 10;
                int stay = t.length >= 4 ? fInt(t[3]) : 60;
                int fadeOut = t.length == 5 ? fInt(t[4]) : 20;
                title.send(p, titleF, subtitle, fadeIn, stay, fadeOut);
            } catch (Exception e) {
                plugin.getLogger().warning("Title error: " + e);
            }
        });

    }

    private void sendBossbar(Player p, String bossbar) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String[] b = bossbar.split(";");
                if (b.length == 0 || b.length > 6 || bar) {
                    plugin.getLogger().warning("Invalid bossbar. [message;color;type;time;style;flag]. For 1.9+, error: " + bossbar);
                    return;
                }
                BarColor color = b.length >= 2 ? BarColor.valueOf(b[1].toUpperCase()) : BarColor.WHITE;
                String type = b.length >= 3 ? b[2].toLowerCase() : "time";
                long time = b.length >= 4 ? fInt(b[3]) : 5;
                long ticks = time * 20L;
                BarStyle style = b.length >= 5 ? BarStyle.valueOf(b[4].toUpperCase()) : BarStyle.SEGMENTED_6;
                BarFlag flag = b.length == 6 ? BarFlag.valueOf(b[5].toUpperCase()) : null;
                boolean update = b[0].contains("%time%");
                String text = fStr(b[0].replace("%time%", time + ""));
                BossBar bossBar = flag == null ? plugin.getServer().createBossBar(text, color, style) : plugin.getServer().createBossBar(text, color, style, flag);
                bossBar.addPlayer(p);
                if (type.equals("stop")) {
                    scheduler.runTaskLaterAsynchronously(plugin, () -> {
                        bossBar.removeAll();
                        bossBar.setVisible(false);
                    }, ticks);
                } else {
                    new BukkitRunnable() {
                        private int t = 0;

                        @Override
                        public void run() {
                            t++;
                            if (t == ticks) {
                                bossBar.removeAll();
                                bossBar.setVisible(false);
                                cancel();
                                return;
                            }
                            int left = (int) (time - Math.floor((double) t / 20));
                            if (update) {
                                bossBar.setTitle(fStr(b[0].replace("%time%", left + "")));
                            }
                            bossBar.setProgress(1 - ((double) t / ticks));
                        }
                    }.runTaskTimerAsynchronously(plugin, 1, 1);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Bossbar error: " + e);
            }
        });
    }

    private void sendMessage(CommandSender s, String text) {
        s.sendMessage(fStr(text));
    }

    private void log(String log) {
        plugin.getLogger().info(log);
    }

    private String fStr(String s) {
        return values.getColorizer().colorize(s);
    }

    private Float fFloat(String s) {
        return Float.parseFloat(s);
    }

    private int fInt(String s) {
        return Integer.parseInt(s);
    }

    private double fDouble(String s) {
        return Double.parseDouble(s);
    }

    @SuppressWarnings("all")
    public void paste(Location loc, String schematicId) {
        File schem = new File(plugin.getDataFolder(), "schematics/" + schematicId);
        if (!schem.exists()) {
            plugin.getLogger().warning("File " + schematicId + " not found.");
            return;
        }
        try {
            schematic.paste(loc, schem);
        } catch (Exception error) {
            plugin.getLogger().warning("An error occurred while inserting the diagram. Please contact the administrator.\nError: " + error);
        }
    }

    public ItemStack generateItem(ConfigurationSection slot) {
        try {
            String type = slot.getString("type");
            if (type.equalsIgnoreCase("itemstack")) {
                return slot.getItemStack("item");
            }
            slot = slot.getConfigurationSection("item");
            ItemStack item = new ItemStack(Material.valueOf(slot.getString("type")));
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (slot.getString("name") != null) {
                meta.setDisplayName(values.getColorizer().colorize(slot.getString("name")));
            }
            if (slot.getString("lore") != null) {
                List<String> l = new ArrayList<>();
                for (String t : slot.getStringList("lore")) {
                    l.add(values.getColorizer().colorize(t));
                }
                meta.setLore(l);
            }
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Error generate item " + slot.toString() + ": " + e);
            return new ItemStack(Material.AIR);
        }
    }

    private String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text.isEmpty() || searchList == null || replacementList == null) {
            return text;
        }
        final StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < searchList.length; i++) {
            final String search = searchList[i];
            final String replacement = replacementList[i];
            int start = 0;
            while ((start = result.indexOf(search, start)) != -1) {
                result.replace(start, start + search.length(), replacement);
                start += replacement.length();
            }
        }

        return result.toString();
    }

    public String repeat(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
