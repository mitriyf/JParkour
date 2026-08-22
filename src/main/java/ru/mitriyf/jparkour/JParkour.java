package ru.mitriyf.jparkour;

import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mitriyf.jparkour.command.JParkourCommand;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.hook.Supports;
import ru.mitriyf.jparkour.listener.BlockListener;
import ru.mitriyf.jparkour.listener.EntityListener;
import ru.mitriyf.jparkour.listener.PlayerListener;
import ru.mitriyf.jparkour.listener.WorldListener;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.manager.PartyManager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class JParkour extends JavaPlugin {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final String configsVersion = "1.8";
    private PartyManager partyManager;
    private GameManager gameManager;
    private Supports supports;
    private int version = 13;
    private Values values;
    private Utils utils;

    @Override
    public void onEnable() {
        getLogger().info("Support: https://vk.com/jdevs");
        tryGetServerVersion();
        values = new Values(this);
        utils = new Utils(this);
        partyManager = new PartyManager(this);
        gameManager = new GameManager(this);
        supports = new Supports(this);
        utils.setup();
        values.setup(true);
        getCommand("jparkour").setExecutor(new JParkourCommand(this));
        setupListeners();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void setupListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new WorldListener(this), this);
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        for (Game game : new HashMap<>(gameManager.getRooms()).values()) {
            if (game != null) {
                game.close(true, true);
            }
        }
        if (supports != null) {
            supports.unregister();
        }
    }

    private void tryGetServerVersion() {
        try {
            String[] serverVersion = getServer().getBukkitVersion().split("-")[0].split("\\.");
            String subVersion = serverVersion[1];
            if (Integer.parseInt(serverVersion[0]) > 1) {
                version = 26;
            } else if (subVersion.length() >= 2) {
                version = Integer.parseInt(subVersion.substring(0, 2));
            } else {
                version = Integer.parseInt(subVersion);
            }
        } catch (Exception e) {
            getLogger().info("Version check failed. Default set version 26. Error: " + e);
            version = 26;
        }
    }
}