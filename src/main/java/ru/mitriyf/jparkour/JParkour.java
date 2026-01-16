package ru.mitriyf.jparkour;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mitriyf.jparkour.cmd.JParkourCommand;
import ru.mitriyf.jparkour.events.Events;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.supports.Supports;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class JParkour extends JavaPlugin {
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private final String configsVersion = "1.6";
    private int version = 13;
    private Values values;
    private Utils utils;
    private Events events;
    private Manager manager;
    private Supports supports;

    @Override
    public void onEnable() {
        getLogger().info("Support: https://vk.com/jdevs");
        getVer();
        values = new Values(this);
        utils = new Utils(this);
        manager = new Manager(this);
        supports = new Supports(this);
        utils.setup();
        values.setup(true);
        getCommand("jparkour").setExecutor(new JParkourCommand(this));
        events = new Events(this);
        events.setup();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        if (supports.getPlaceholders() != null) {
            supports.getPlaceholders().unregister();
        }
        for (Game game : new HashMap<>(values.getRooms()).values()) {
            game.close(true, true);
        }
        if (supports != null) {
            supports.unregister();
        }
    }

    private void getVer() {
        String ver = getServer().getBukkitVersion().split("-")[0].split("\\.")[1];
        if (ver.length() >= 2) {
            version = Integer.parseInt(ver.substring(0, 2));
        } else {
            version = Integer.parseInt(ver);
        }
    }
}