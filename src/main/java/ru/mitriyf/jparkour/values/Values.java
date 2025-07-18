package ru.mitriyf.jparkour.values;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.info.PlayerData;
import ru.mitriyf.jparkour.values.info.SchematicData;
import ru.mitriyf.jparkour.values.info.StandData;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Getter
@Setter
public class Values {
    private final JParkour plugin;
    private Utils utils;
    private final Map<String, Game> rooms = new HashMap<>();
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Map<String, SchematicData> schematics = new HashMap<>();
    private final Map<Integer, ItemStack> slots = new HashMap<>();
    private final Map<String, StandData> stands = new HashMap<>();
    private final List<String> maps = new ArrayList<>();
    private final List<String> map = new ArrayList<>();
    private final List<String> inGame = new ArrayList<>();
    private final List<String> end = new ArrayList<>();
    private final List<String> help = new ArrayList<>();
    private final List<String> noperm = new ArrayList<>();
    private final List<String> kicked = new ArrayList<>();
    private final List<String> win = new ArrayList<>();
    private String world, worldStart, notfound, started, connect, waiter, exit, noExit, stopped, wait, start, sWait, sStart, sWin, left, right;
    private int amount;
    public Values(JParkour plugin) {
        this.plugin = plugin;
    }
    public void setup() {
        utils = plugin.getUtils();
        clear();
        setupSettings();
        setupMessages();
        setupSchematics();
    }
    private void setupSchematics() {
        File dir = new File(plugin.getDataFolder(), "schematics");
        if (!dir.exists()) {
            plugin.getLogger().warning("No schematics were found. I'm starting an attempt to download schematics from the plugin...");
            if (dir.mkdir()) plugin.getLogger().info("The folder has been created.");
            try {
                exportSchematics();
                plugin.getLogger().info("The download has been completed successfully.");
            } catch (Exception e) {
                plugin.getLogger().warning("A critical error.");
                throw new RuntimeException(e);
            }
        }
        ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("settings");
        assert cfg != null;
        File[] files = dir.listFiles();
        if (files == null) {
            plugin.getLogger().warning("No files were found in the schematics folder.");
        } else {
            String sc = "schematics/";
            for (File schem : files) {
                if (schem.getName().contains(".schem")) {
                    String name = FilenameUtils.removeExtension(schem.getName());
                    String l = name.toLowerCase();
                    File file = new File(dir, name + ".yml");
                    if (!file.exists()) {
                        plugin.saveResource(sc + "default.yml", true);
                        if (new File(dir, "default.yml").renameTo(file)) {
                            plugin.getLogger().info("The configuration file " + file.getName() + " has been created");
                        } else {
                            plugin.getLogger().warning("An error occurred while creating the " + file.getName() + " configuration file");
                            return;
                        }
                    }
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    maps.add(l);
                    schematics.put(l, new SchematicData(stands, utils, yaml, sc + schem.getName()));
                }
            }
        }
    }
    private void exportSchematics() {
        String[] files = new String[] {"hello.txt"};
        String schematics = "schematics/";
        String path = plugin.getDataFolder() + "/" + schematics;
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
            plugin.getLogger().warning("An error occurred when loading the schematics. Check your internet connection.");
            plugin.getLogger().warning("You can download the schematics and upload them to the server on the official page of the resource. (GitHub)");
        }
    }
    private void setupSettings() {
        ConfigurationSection settings = plugin.getConfig().getConfigurationSection("settings");
        if (settings == null) {
            plugin.getLogger().warning("No section found in the configuration: settings");
            return;
        }
        world = settings.getString("world");
        amount = settings.getInt("amount");
        worldStart = world.replace("XIDX", "");
        ConfigurationSection game = settings.getConfigurationSection("game");
        if (game == null) {
            plugin.getLogger().warning("No section found in the configuration: settings.game");
            return;
        }
        ConfigurationSection items = game.getConfigurationSection("items");
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
    private void setupMessages() {
        ConfigurationSection messages = plugin.getConfig().getConfigurationSection("messages");
        if (messages == null) {
            plugin.getLogger().warning("No section found in the configuration: messages");
            return;
        }
        ConfigurationSection cmd = messages.getConfigurationSection("cmd");
        if (cmd == null) {
            plugin.getLogger().warning("No section found in the configuration: messages.cmd");
            return;
        }
        help.addAll(cmd.getStringList("help"));
        noperm.addAll(cmd.getStringList("noperm"));
        ConfigurationSection game = messages.getConfigurationSection("game");
        if (game == null) {
            plugin.getLogger().warning("No section found in the configuration: messages.game");
            return;
        }
        ConfigurationSection room = game.getConfigurationSection("room");
        if (room == null) {
            plugin.getLogger().warning("No section found in the configuration: messages.game.room");
            return;
        }
        notfound = room.getString("notfound");
        started = room.getString("started");
        connect = room.getString("connect");
        exit = room.getString("exit");
        waiter = room.getString("waiter");
        noExit = room.getString("noExit");
        stopped = game.getString("status.stopped");
        wait = game.getString("status.wait");
        start = game.getString("status.start");
        ConfigurationSection status = game.getConfigurationSection("status");
        sWait = status.getString("wait");
        sStart = status.getString("start");
        sWin = status.getString("win");
        left = status.getString("left");
        right = status.getString("right");
        ConfigurationSection actions = game.getConfigurationSection("actions");
        if (actions == null) {
            plugin.getLogger().warning("No section found in the configuration: messages.game.actions");
            return;
        }
        inGame.addAll(actions.getStringList("ingame"));
        kicked.addAll(actions.getStringList("kicked"));
        end.addAll(actions.getStringList("end"));
        win.addAll(actions.getStringList("win"));
    }
    private void recovery() {
        File dir = plugin.getServer().getWorldContainer().getAbsoluteFile();
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File file : list) if (file.getName().startsWith(worldStart)) FileUtils.deleteQuietly(new File(file.getName()));
    }
    private void clear() {
        schematics.clear();
        map.clear();
        maps.clear();
        for (List<String> strings : Arrays.asList(help, noperm, inGame, end, kicked, win)) {
            strings.clear();
        }
    }
}
