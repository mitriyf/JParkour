package ru.mitriyf.jparkour.utils;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.utils.actions.ActionType;
import ru.mitriyf.jparkour.utils.actions.ActionUtils;
import ru.mitriyf.jparkour.utils.actions.titles.Title;
import ru.mitriyf.jparkour.utils.actions.titles.impl.Title10;
import ru.mitriyf.jparkour.utils.actions.titles.impl.Title11;
import ru.mitriyf.jparkour.utils.common.CommonUtils;
import ru.mitriyf.jparkour.utils.locales.Locale;
import ru.mitriyf.jparkour.utils.locales.impl.Locale12;
import ru.mitriyf.jparkour.utils.locales.impl.Locale13;
import ru.mitriyf.jparkour.utils.schematic.Paste;
import ru.mitriyf.jparkour.utils.schematic.impl.Paste12;
import ru.mitriyf.jparkour.utils.schematic.impl.Paste13;
import ru.mitriyf.jparkour.utils.worlds.WorldGenerator;
import ru.mitriyf.jparkour.utils.worlds.impl.Generator12;
import ru.mitriyf.jparkour.utils.worlds.impl.Generator13;
import ru.mitriyf.jparkour.values.Values;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class Utils {
    private final Values values;
    private final Logger logger;
    private final JParkour plugin;
    private final CountDownLatch latch;
    private final CommonUtils commonUtils;
    private final ActionUtils actionUtils;
    private final Set<Integer> tasks = new HashSet<>();
    private boolean actionBar = false, bar = false, tit = false;
    private WorldGenerator worldGenerator;
    private Material gSword;
    private Paste schematic;
    private Locale locale;
    private Title title;

    public Utils(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        latch = new CountDownLatch(1);
        actionUtils = new ActionUtils(this, plugin);
        commonUtils = new CommonUtils(this, plugin);
    }

    public void setup() {
        int version = plugin.getVersion();
        if (version < 13) {
            if (version < 8) {
                tit = true;
            }
            if (version < 9) {
                bar = true;
            }
            values.setSchematicUrl("nether.schematic");
            locale = new Locale12();
            schematic = new Paste12(plugin);
            worldGenerator = new Generator12();
            gSword = Material.valueOf("GOLD_SWORD");
        } else {
            values.setSchematicUrl("nether.schem");
            values.setDefaultId("13");
            locale = new Locale13();
            schematic = new Paste13(plugin);
            worldGenerator = new Generator13();
            gSword = Material.GOLDEN_SWORD;
        }
        if (version < 11) {
            actionBar = true;
            if (!tit) {
                title = new Title10();
            }
        } else {
            title = new Title11();
        }
    }

    public BukkitTask sendMessage(Player p, List<Action> actions) {
        return sendMessage(p, actions, null, null);
    }

    public BukkitTask sendMessage(Player p, List<Action> actions, String[] search, String[] replace) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                tasks.add(getTaskId());
                for (Action action : actions) {
                    if (!tasks.contains(getTaskId())) {
                        return;
                    }
                    sendPlayer(p, action, search, replace);
                }
            }
        }.runTaskAsynchronously(plugin);
    }


    public BukkitTask sendMessage(CommandSender sender, Map<String, List<Action>> actions) {
        return sendMessage(sender, actions, null, null);
    }

    public BukkitTask sendMessage(CommandSender sender, Map<String, List<Action>> actions, String[] search, String[] replace) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                tasks.add(getTaskId());
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    for (Action action : actions.getOrDefault(locale.player(p), actions.get(""))) {
                        if (!tasks.contains(getTaskId())) {
                            return;
                        }
                        sendPlayer(p, action, search, replace);
                    }
                    return;
                }
                for (Action action : actions.get("")) {
                    if (!tasks.contains(getTaskId())) {
                        return;
                    }
                    sendSender(sender, action, search, replace);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void sendPlayer(Player p, Action action, String[] search, String[] replace) {
        ActionType type = action.getType();
        String context = replaceEach(action.getContext().replace("%player%", p.getName()).replace("%world%", p.getWorld().getName()), search, replace);
        if (values.isPlaceholderAPI()) {
            context = PlaceholderAPI.setPlaceholders(p, context);
        }
        switch (type) {
            case PLAYER:
                actionUtils.dispatchPlayer(p, context);
                break;
            case TELEPORT:
                actionUtils.teleportPlayer(p, context);
                break;
            case CONSOLE:
                commonUtils.dispatchConsole(context);
                break;
            case ACTIONBAR:
                actionUtils.sendActionBar(p, context);
                break;
            case BOSSBAR:
                actionUtils.sendBossbar(p, context);
                break;
            case BROADCAST:
                commonUtils.broadcast(context);
                break;
            case TITLE:
                actionUtils.sendTitle(p, context);
                break;
            case SOUND:
                actionUtils.playSound(p, context);
                break;
            case EFFECT:
                actionUtils.giveEffect(p, context);
                break;
            case EXPLOSION:
                actionUtils.createExplosion(p, context);
                break;
            case LOG:
                log(context);
                break;
            case DELAY:
                try {
                    if (latch.await(Integer.parseInt(context) * 50L, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (Exception ignored) {
                }
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
                commonUtils.dispatchConsole(context);
                break;
            case BROADCAST:
                commonUtils.broadcast(context);
                break;
            case LOG:
                log(context);
                break;
            case DELAY:
                try {
                    if (latch.await(Integer.parseInt(context) * 50L, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (Exception ignored) {
                }
                break;
            case PLAYER:
            case TITLE:
            case ACTIONBAR:
            case BOSSBAR:
            case EFFECT:
            case TELEPORT:
            case SOUND:
            case EXPLOSION:
                break;
            default:
                sendMessage(sender, context);
                break;
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

    public Entity getEntity(World world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUniqueId().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    public void paste(Location loc, String schematicId, boolean pasteAir) {
        try {
            schematic.paste(loc, schematicId, pasteAir);
        } catch (Exception error) {
            logger.warning("An error occurred while inserting the diagram. Please contact the administrator.\nError: " + error);
        }
    }

    public void saveItem(Player p, String sPath, int slot, String itemName, Material material) {
        commonUtils.saveItem(p, sPath, slot, itemName, material);
    }

    public ItemStack generateItem(ConfigurationSection slot) {
        return commonUtils.generateItem(slot);
    }

    private void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(formatString(text));
    }

    private void sendMessage(Player player, String text) {
        player.sendMessage(formatString(text));
    }

    public String formatString(String s) {
        return values.getColorizer().colorize(s);
    }

    public Float formatFloat(String s) {
        return Float.parseFloat(s);
    }

    public int formatInt(String s) {
        return Integer.parseInt(s);
    }

    public double formatDouble(String s) {
        return Double.parseDouble(s);
    }

    public boolean formatBoolean(String s) {
        return Boolean.parseBoolean(s);
    }

    private void log(String log) {
        logger.info(log);
    }
}
