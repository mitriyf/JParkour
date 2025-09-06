package ru.mitriyf.jparkour.values.updater;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.updater.config.ConfigUpdater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Updater {
    private final JParkour plugin;
    private final Values values;
    private final File dataFolder;
    private final Logger logger;

    public Updater(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.dataFolder = plugin.getDataFolder();
        this.logger = plugin.getLogger();
    }

    public void checkUpdates() {
        if (values.isUpdaterEnabled()) {
            checkVersions();
        }
        checkLocales();
        checkSchematics();
    }

    public void checkVersions() {
        try {
            InputStream in = new URL("https://github.com/mitriyf/JParkour/raw/refs/heads/main/updater/versions.yml").openStream();
            File cfg = new File(plugin.getDataFolder(), "temp/versions.yml");
            FileUtils.copyInputStreamToFile(in, cfg);
            YamlConfiguration versions = YamlConfiguration.loadConfiguration(cfg);
            FileUtils.deleteQuietly(cfg.getParentFile());
            for (String ver : versions.getStringList("versions")) {
                String[] info = ver.split(";");
                String pluginVersion = plugin.getDescription().getVersion();
                if (pluginVersion.equals(info[0])) {
                    logger.info("This is the correct version of the plugin, as reviewed by Updater.");
                    return;
                } else if (values.isRequired() & info[1].equals("REQUIRED") || values.isRelease()) {
                    boolean devMode = true;
                    for (String searchVer : versions.getStringList("versions")) {
                        String[] searchInfo = searchVer.split(";");
                        if (pluginVersion.equals(searchInfo[0])) {
                            devMode = false;
                            break;
                        }
                    }
                    if (devMode) {
                        logger.warning("No version was found. This is a developer version.");
                        return;
                    }
                    logger.warning("Download the new plugin update: " + info[2]);
                    return;
                }
            }
        } catch (Exception e) {
            logger.warning("An error occurred while checking for updates. Please check your network connection.");
            logger.warning("Error: " + e);
        }
    }

    private void checkLocales() {
        File folderLocales = new File(dataFolder, "locales");
        String ver = plugin.getConfigVersion();
        File cfg = new File(dataFolder, "config.yml");
        Set<File> locales = new HashSet<>();
        File[] lFiles = folderLocales.listFiles();
        locales.add(cfg);
        if (lFiles != null) {
            locales.addAll(Arrays.asList(lFiles));
        }
        for (File locale : locales) {
            String parentPath, updatePath;
            parentPath = "locales/";
            if (locale.getParentFile().getName().equals(plugin.getName())) {
                parentPath = "";
            }
            updatePath = parentPath + locale.getName();
            if (plugin.getResource(updatePath) == null) {
                updatePath = parentPath + "en_US.yml";
            }
            updateConfig(locale, ver, parentPath, updatePath);
        }
    }

    private void checkSchematics() {
        File sch = new File(dataFolder, "schematics");
        String schemVer = plugin.getSchematicVersion();
        File[] files = sch.listFiles();
        if (files == null) {
            plugin.getLogger().warning("No files were found in the schematics folder.");
        } else {
            String sc = "schematics/";
            for (File schem : files) {
                updateConfig(schem, schemVer, sc, sc + "default.yml");
            }
        }
    }

    private void updateConfig(File cfg, String ver, String parentPath, String updatePath) {
        if (cfg.getName().contains(".yml")) {
            try {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(cfg);
                String oldVersion = yml.getString("version") == null ? "1.0" : yml.getString("version");
                if (!oldVersion.equals(ver)) {
                    backupConfig(parentPath, cfg, oldVersion);
                    yml.set("version", ver);
                    yml.save(cfg);
                    new ConfigUpdater(plugin).update(updatePath, cfg);
                    plugin.getLogger().info("The " + cfg.getName() + " has been successfully updated to version " + ver);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Update " + cfg.getName() + " is failed! Error: " + e);
            }
        }
    }

    private void backupConfig(String parentPath, File file, String oldVersion) throws IOException {
        File copied = new File(dataFolder, parentPath + "backups/" + file.getName() + "-" + oldVersion + ".backup");
        Path copiedPath = copied.toPath();
        Files.createDirectories(copied.getParentFile().toPath());
        Files.deleteIfExists(copiedPath);
        Files.copy(file.toPath(), copiedPath);
    }
}
