package ru.mitriyf.jparkour.values;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.utils.actions.Action;
import ru.mitriyf.jparkour.utils.actions.ActionType;
import ru.mitriyf.jparkour.utils.colors.CHex;
import ru.mitriyf.jparkour.utils.colors.CMiniMessage;
import ru.mitriyf.jparkour.utils.colors.Colorizer;
import ru.mitriyf.jparkour.values.info.SchematicData;
import ru.mitriyf.jparkour.values.info.StandData;

import java.io.File;
import java.io.IOException;
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
    private final File slotsFile;
    private final File configFile;
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
    private final Map<String, List<Action>> mStarted = new HashMap<>();
    private final Map<String, List<Action>> restarted = new HashMap<>();
    private final Map<String, List<Action>> kicked = new HashMap<>();
    private final Map<String, List<Action>> joined = new HashMap<>();
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
    private boolean deleteWhenClosing, updaterEnabled, topsEnabled, required, release, placeholderAPI, locale;
    private FileConfiguration config;
    @Setter
    private FileConfiguration itemSlots;
    @Setter
    private String defaultId = "";
    @Setter
    private String schematicUrl;
    private String world, worldStart;
    private Colorizer colorizer;
    private Utils utils;
    private int topsInterval, amount;

    public Values(JParkour plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.slotsFile = new File(plugin.getDataFolder(), "slots.yml");
        this.logger = plugin.getLogger();
    }

    public void setup() {
        saveConfigs();
        config = YamlConfiguration.loadConfiguration(configFile);
        itemSlots = YamlConfiguration.loadConfiguration(slotsFile);
        utils = plugin.getUtils();
        clear();
        setupSettings();
        setupLocales();
        setupSchematics();
        plugin.getSupports().register();
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
                logger.warning("A critical error. Error: " + e);
            }
        }
        ConfigurationSection cfg = config.getConfigurationSection("settings");
        assert cfg != null;
        File[] files = dir.listFiles();
        if (files == null) {
            logger.warning("No files were found in the schematics folder.");
        } else {
            for (File schem : files) {
                if (schem.getName().contains(".schem")) {
                    String name = schem.getName().split("\\.")[0];
                    String l = name.toLowerCase();
                    File file = new File(dir, name + ".yml");
                    if (!file.exists()) {
                        plugin.saveResource("schematics/default.yml", true);
                        if (new File(dir, "default.yml").renameTo(file)) {
                            logger.info("The configuration file " + file.getName() + " has been created");
                        } else {
                            logger.warning("An error occurred while creating the " + file.getName() + " configuration file");
                            return;
                        }
                    }
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    maps.add(l);
                    String schemName = schem.getName();
                    schematics.put(l, new SchematicData(this, yaml, schemName, name));
                    try {
                        utils.getSchematic().generate(schemName, schem);
                    } catch (Exception e) {
                        logger.warning("Error loading schematic. Error: " + e);
                    }
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
            InputStream in = new URL("https://github.com/mitriyf/JParkour/raw/refs/heads/main/downloads/" + schematicUrl).openStream();
            String fullPath = path + schematicUrl;
            Path fp = Paths.get(fullPath);
            Files.copy(in, fp, StandardCopyOption.REPLACE_EXISTING);
            in.close();
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
        ConfigurationSection games = settings.getConfigurationSection("games");
        world = games.getString("world");
        amount = games.getInt("amount");
        deleteWhenClosing = games.getBoolean("deleteWhenClosing");
        ConfigurationSection updater = settings.getConfigurationSection("updater");
        updaterEnabled = updater.getBoolean("enabled");
        ConfigurationSection updaterSettings = updater.getConfigurationSection("settings");
        required = updaterSettings.getBoolean("required");
        release = updaterSettings.getBoolean("release");
        worldStart = world.replace("XIDX", "");
        ConfigurationSection supports = settings.getConfigurationSection("supports");
        placeholderAPI = supports.getBoolean("placeholderAPI");
        if (placeholderAPI && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.warning("The PlaceholderAPI was not detected. This feature will be disabled.");
            placeholderAPI = false;
        }
        ConfigurationSection tops = supports.getConfigurationSection("tops");
        topsEnabled = tops.getBoolean("enabled");
        topsInterval = tops.getInt("updateInterval") * 20;
        ConfigurationSection items = itemSlots.getConfigurationSection("default");
        for (String s : items.getKeys(false)) {
            ConfigurationSection slot = items.getConfigurationSection(s);
            slots.put(slot.getInt("slot"), utils.generateItem(slot));
        }
        ConfigurationSection armorStands = settings.getConfigurationSection("armor-stands");
        for (String s : armorStands.getKeys(false)) {
            ConfigurationSection id = armorStands.getConfigurationSection(s);
            stands.put(s, new StandData(id, logger));
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
            joined.put(name, getActionList(actions.getStringList("joined")));
            inGame.put(name, getActionList(actions.getStringList("ingame")));
            mStarted.put(name, getActionList(actions.getStringList("started")));
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
                deleteDirectory(new File(file.getName()));
            }
        }
    }

    public void deleteDirectory(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    delete(file);
                }
            }
            delete(f);
        }
    }

    private void delete(File f) {
        try {
            Files.delete(f.toPath());
        } catch (IOException ignored) {
        }
    }

    private void saveConfigs() {
        saveConfig("config", configFile);
        saveConfig("slots", slotsFile);
    }

    private void saveConfig(String configName, File file) {
        if (file.exists()) {
            return;
        }
        String resource = configName + defaultId + ".yml";
        try {
            plugin.saveResource(resource, true);
            if (!defaultId.isEmpty()) {
                Path oldCfg = new File(plugin.getDataFolder(), resource).toPath();
                Files.move(oldCfg, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            logger.warning("Error save configurations. Error: " + e);
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
        plugin.getSupports().unregister();
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
        for (Map<String, List<Action>> map : Arrays.asList(help, noperm, notfound, started, mStarted, joined, damageHeart, connect, exit, waiter, noExit, inGame, kicked, end, win)) {
            map.clear();
        }
    }
}
