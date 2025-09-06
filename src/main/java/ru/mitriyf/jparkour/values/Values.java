package ru.mitriyf.jparkour.values;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.supports.Placeholder;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.utils.actions.ActionType;
import ru.mitriyf.jparkour.utils.colors.CHex;
import ru.mitriyf.jparkour.utils.colors.CMiniMessage;
import ru.mitriyf.jparkour.utils.colors.Colorizer;
import ru.mitriyf.jparkour.values.info.SchematicData;
import ru.mitriyf.jparkour.values.info.StandData;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Values {
    private final JParkour plugin;
    private final Logger logger;
    private final File dataFolder;
    private final File configSlots;
    private final Pattern action_pattern = Pattern.compile("\\[(\\w+)] ?(.*)");
    private final Map<String, SchematicData> schematics = new HashMap<>();
    private final String[] lcs = new String[]{"de_DE", "en_US", "ru_RU"};
    private final Map<String, List<Action>> damageHeart = new HashMap<>();
    private final Map<String, List<Action>> notfound = new HashMap<>();
    private final Map<String, List<Action>> started = new HashMap<>();
    private final Map<String, List<Action>> connect = new HashMap<>();
    private final Map<String, List<Action>> waiter = new HashMap<>();
    private final Map<String, List<Action>> noExit = new HashMap<>();
    private final Map<String, List<Action>> noperm = new HashMap<>();
    private final Map<String, List<Action>> restarted = new HashMap<>();
    private final Map<String, List<Action>> kicked = new HashMap<>();
    private final Map<String, List<Action>> inGame = new HashMap<>();
    private final Map<String, List<Action>> help = new HashMap<>();
    private final Map<String, List<Action>> exit = new HashMap<>();
    private final Map<String, List<Action>> end = new HashMap<>();
    private final Map<String, List<Action>> win = new HashMap<>();
    private final Map<Integer, ItemStack> slots = new HashMap<>();
    private final Map<String, StandData> stands = new HashMap<>();
    private final Map<String, List<Action>> map = new HashMap<>();
    private final Map<String, String> sStart = new HashMap<>();
    private final Map<String, String> sWin = new HashMap<>();
    private final Map<String, String> sWait = new HashMap<>();
    private final Map<String, String> right = new HashMap<>();
    private final Map<String, String> left = new HashMap<>();
    private final Map<String, Game> rooms = new HashMap<>();
    private final List<String> maps = new ArrayList<>();
    private FileConfiguration config;
    private boolean updaterEnabled, required, release, placeholderAPI, locale;
    @Setter
    private FileConfiguration itemSlots;
    private String world, worldStart;
    private Placeholder placeholder;
    private Colorizer colorizer;
    private Utils utils;
    private int amount;

    public Values(JParkour plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.configSlots = new File(plugin.getDataFolder(), "slots.yml");
        this.logger = plugin.getLogger();
    }

    public void setup() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        if (!configSlots.exists()) {
            plugin.saveResource("slots.yml", true);
        }
        itemSlots = YamlConfiguration.loadConfiguration(configSlots);
        utils = plugin.getUtils();
        clear();
        setupSettings();
        setupLocales();
        setupSchematics();
    }

    private void setupSchematics() {
        File dir = new File(dataFolder, "schematics");
        if (!dir.exists()) {
            logger.warning("No schematics were found. I'm starting an attempt to download schematics from the plugin...");
            if (dir.mkdir()) {
                logger.info("The folder has been created.");
            }
            try {
                exportSchematics();
                logger.info("The download has been completed successfully.");
            } catch (Exception e) {
                logger.warning("A critical error.");
                throw new RuntimeException(e);
            }
        }
        ConfigurationSection cfg = config.getConfigurationSection("settings");
        assert cfg != null;
        File[] files = dir.listFiles();
        if (files == null) {
            logger.warning("No files were found in the schematics folder.");
        } else {
            String sc = "schematics/";
            for (File schem : files) {
                if (schem.getName().contains(".schem")) {
                    String name = schem.getName().split("\\.")[0];
                    String l = name.toLowerCase();
                    File file = new File(dir, name + ".yml");
                    if (!file.exists()) {
                        plugin.saveResource(sc + "default.yml", true);
                        if (new File(dir, "default.yml").renameTo(file)) {
                            logger.info("The configuration file " + file.getName() + " has been created");
                        } else {
                            logger.warning("An error occurred while creating the " + file.getName() + " configuration file");
                            return;
                        }
                    }
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    maps.add(l);
                    schematics.put(l, new SchematicData(this, yaml, schem.getName(), name));
                }
            }
        }
    }

    private void exportSchematics() {
        String[] files = new String[]{"hello.txt"};
        String schematics = "schematics/";
        String path = dataFolder + "/" + schematics;
        try {
            for (String s : files) {
                String fullPath = path + s;
                if (!(new File(fullPath)).exists()) {
                    plugin.saveResource(schematics + s, true);
                }
            }
            InputStream in = new URL("https://github.com/mitriyf/JParkour/raw/refs/heads/main/downloads/nether.schematic").openStream();
            String fullPath = path + "nether.schematic";
            Path fp = Paths.get(fullPath);
            Files.copy(in, fp, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.warning("An error occurred when loading the schematics. Check your internet connection.");
            logger.warning("You can download the schematics and upload them to the server on the official page of the resource. (GitHub)");
        }
    }

    private void setupSettings() {
        ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings == null) {
            logger.warning("No section found in the configuration: settings");
            return;
        }
        String translate = settings.getString("translate").toLowerCase();
        if (translate.equals("minimessage")) {
            colorizer = new CMiniMessage();
        } else {
            colorizer = new CHex();
        }
        locale = settings.getBoolean("locales");
        world = settings.getString("world");
        amount = settings.getInt("amount");
        ConfigurationSection updater = settings.getConfigurationSection("updater");
        updaterEnabled = updater.getBoolean("enabled");
        ConfigurationSection updaterSettings = updater.getConfigurationSection("settings");
        required = updaterSettings.getBoolean("required");
        release = updaterSettings.getBoolean("release");
        worldStart = world.replace("XIDX", "");
        ConfigurationSection plugins = settings.getConfigurationSection("plugins");
        placeholderAPI = plugins.getBoolean("placeholderAPI");
        if (placeholderAPI && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.warning("The PlaceholderAPI was not detected. This feature will be disabled.");
            placeholderAPI = false;
        }
        if (placeholderAPI) {
            placeholder = new Placeholder(plugin);
            placeholder.register();
        }
        ConfigurationSection items = itemSlots.getConfigurationSection("default");
        for (String s : items.getKeys(false)) {
            ConfigurationSection slot = items.getConfigurationSection(s);
            slots.put(slot.getInt("slot"), utils.generateItem(slot));
        }
        ConfigurationSection armorStands = settings.getConfigurationSection("armor-stands");
        for (String s : armorStands.getKeys(false)) {
            ConfigurationSection id = armorStands.getConfigurationSection(s);
            stands.put(s, new StandData(id));
        }
        recovery();
    }

    private void setupLocales() {
        Map<String, FileConfiguration> locales = new HashMap<>();
        locales.put("", config);
        if (locale) {
            File file = new File(dataFolder, "locales");
            if (!file.exists()) {
                for (String s : lcs) {
                    plugin.saveResource("locales/" + s + ".yml", false);
                }
            }
            File[] dir = file.listFiles();
            if (dir == null) {
                logger.warning("Locales are empty.");
            } else {
                for (File f : dir) {
                    if (f.isFile()) {
                        String name = f.getName();
                        locales.put(name.substring(0, name.indexOf(".")).toLowerCase(), YamlConfiguration.loadConfiguration(f));
                    }
                }
            }
        }
        for (Map.Entry<String, FileConfiguration> entry : locales.entrySet()) {
            ConfigurationSection messages = entry.getValue().getConfigurationSection("messages");
            String name = entry.getKey();
            ConfigurationSection cmd = messages.getConfigurationSection("cmd");
            help.put(name, getActionList(cmd.getStringList("help")));
            noperm.put(name, getActionList(cmd.getStringList("noperm")));
            ConfigurationSection game = messages.getConfigurationSection("game");
            ConfigurationSection room = game.getConfigurationSection("room");
            notfound.put(name, getActionList(room.getStringList("notfound")));
            started.put(name, getActionList(room.getStringList("started")));
            connect.put(name, getActionList(room.getStringList("connect")));
            exit.put(name, getActionList(room.getStringList("exit")));
            waiter.put(name, getActionList(room.getStringList("waiter")));
            noExit.put(name, getActionList(room.getStringList("noExit")));
            ConfigurationSection status = game.getConfigurationSection("status");
            sWait.put(name, status.getString("wait"));
            sStart.put(name, status.getString("start"));
            sWin.put(name, status.getString("win"));
            left.put(name, status.getString("left"));
            right.put(name, status.getString("right"));
            ConfigurationSection actions = game.getConfigurationSection("actions");
            inGame.put(name, getActionList(actions.getStringList("ingame")));
            restarted.put(name, getActionList(actions.getStringList("restarted")));
            kicked.put(name, getActionList(actions.getStringList("kicked")));
            end.put(name, getActionList(actions.getStringList("end")));
            damageHeart.put(name, getActionList(actions.getStringList("damageHeart")));
            win.put(name, getActionList(actions.getStringList("win")));
        }
    }

    private void recovery() {
        File dir = plugin.getServer().getWorldContainer().getAbsoluteFile();
        File[] list = dir.listFiles();
        if (list == null) {
            return;
        }
        for (File file : list) {
            if (file.getName().startsWith(worldStart)) {
                FileUtils.deleteQuietly(new File(file.getName()));
            }
        }
    }

    private Action fromString(String str) {
        Matcher matcher = action_pattern.matcher(str);
        if (!matcher.matches()) {
            return new Action(ActionType.MESSAGE, str);
        }
        ActionType type;
        try {
            type = ActionType.valueOf(matcher.group(1).toUpperCase());
        } catch (IllegalArgumentException e) {
            type = ActionType.MESSAGE;
            return new Action(type, str);
        }
        return new Action(type, matcher.group(2).trim());
    }

    public List<Action> getActionList(List<String> actionStrings) {
        ImmutableList.Builder<Action> actionListBuilder = ImmutableList.builder();
        for (String actionString : actionStrings) {
            actionListBuilder.add(fromString(actionString));
        }
        return actionListBuilder.build();
    }

    private void clear() {
        schematics.clear();
        map.clear();
        maps.clear();
        sWait.clear();
        sStart.clear();
        sWin.clear();
        left.clear();
        right.clear();
        slots.clear();
        stands.clear();
        for (Map<String, List<Action>> map : Arrays.asList(help, noperm, notfound, started, damageHeart, connect, exit, waiter, noExit, inGame, kicked, end, win)) {
            map.clear();
        }
    }
}
