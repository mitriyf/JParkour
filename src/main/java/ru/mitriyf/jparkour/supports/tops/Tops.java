package ru.mitriyf.jparkour.supports.tops;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.supports.tops.schematic.Schematic;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Tops {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");
    @Getter
    private final Map<String, Schematic> schematic = new HashMap<>();
    private final BukkitScheduler scheduler;
    private final JParkour plugin;
    private final Values values;
    private final Logger logger;
    private final File playerData;
    @Getter
    private BukkitTask task;

    public Tops(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        scheduler = plugin.getServer().getScheduler();
        logger = plugin.getLogger();
        playerData = new File(plugin.getDataFolder(), "playerData");
    }

    public void startTimer() {
        if (task != null) {
            task.cancel();
        }
        task = scheduler.runTaskTimerAsynchronously(plugin, () -> {
            schematic.clear();
            File[] schematics = playerData.listFiles();
            if (schematics != null) {
                for (File f : schematics) {
                    schematic.put(f.getName(), new Schematic(f));
                }
            }
        }, 0, values.getTopsInterval());
    }

    public void stopTimer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void setData(Player p, String mapId, int accuracy) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            File infoPlayerFile = new File(playerData, mapId + "/" + p.getUniqueId() + ".yml");
            try {
                Files.createDirectories(infoPlayerFile.getParentFile().toPath());
            } catch (Exception e) {
                logger.warning("Error creating directories for tops. Error: " + e);
            }
            YamlConfiguration infoPlayer = YamlConfiguration.loadConfiguration(infoPlayerFile);
            if (infoPlayer.getInt("accuracy") >= accuracy) {
                return;
            }
            infoPlayer.set("name", p.getName());
            infoPlayer.set("accuracy", accuracy);
            infoPlayer.set("time", OffsetDateTime.now().format(formatter));
            try {
                infoPlayer.save(infoPlayerFile);
            } catch (Exception e) {
                logger.warning("Error save playerData file. Error: " + e);
            }
        });
    }
}
