package ru.mitriyf.jparkour;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mitriyf.jparkour.cmd.CJParkour;
import ru.mitriyf.jparkour.events.Events;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.supports.Supports;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.updater.Updater;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class JParkour extends JavaPlugin {
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private final String configVersion = "1.4";
    private final String schematicVersion = "1.4";
    private int version = 13;
    private Updater updater;
    private Values values;
    private Utils utils;
    private Events events;
    private Manager manager;
    private Supports supports;

    @Override
    public void onEnable() {
        getLogger().info("Support: https://vk.com/jdevs");
        version = getVer();
        values = new Values(this);
        utils = new Utils(this);
        manager = new Manager(this);
        supports = new Supports(this);
        utils.setup();
        updater = new Updater(this);
        values.setup();
        updater.checkUpdates();
        getCommand("jparkour").setExecutor(new CJParkour(this));
        events = new Events(this);
        events.setup();
    }

    @Override
    public void onDisable() {
        if (supports.getPlaceholders() != null) {
            supports.getPlaceholders().unregister();
        }
        for (Game game : new HashMap<>(values.getRooms()).values()) {
            game.close(true, true);
        }
    }

    private int getVer() {
        String ver = getServer().getBukkitVersion().split("-")[0].split("\\.")[1];
        if (ver.length() >= 2) {
            return Integer.parseInt(ver.substring(0, 2));
        } else {
            return Integer.parseInt(ver);
        }
    }
}