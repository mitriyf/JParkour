package ru.mitriyf.jparkour;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.mitriyf.jparkour.cmd.CJParkour;
import ru.mitriyf.jparkour.events.Exit;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.manager.Manager;
import ru.mitriyf.jparkour.supports.Placeholder;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class JParkour extends JavaPlugin {
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private int version_mode = 13;
    private Values values;
    private Utils utils;
    private Exit exit;
    private Manager manager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        setVersion();
        getLogger().info("Support: https://vk.com/jdevs");
        values = new Values(this);
        utils = new Utils(this);
        values.setup();
        manager = new Manager(this);
        getCommand("jparkour").setExecutor(new CJParkour(this));
        exit = new Exit(this);
        Bukkit.getPluginManager().registerEvents(exit, this);
        placeholderAPI = new Placeholder(this);
        connectPlaceholderAPI();
    }
    Placeholder placeholderAPI = null;
    public void connectPlaceholderAPI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    placeholderAPI.register();
                    getLogger().info("Connection to PlaceholderAPI was successful!");
                    cancel();
                }
            }
        }.runTaskTimer(this, 20, 20);
    }
    @Override
    public void onDisable() {
        if (placeholderAPI != null) placeholderAPI.unregister();
        for (Game game : new HashMap<>(values.getRooms()).values()) game.close();
    }
    private void setVersion() {
        String[] ver = getServer().getBukkitVersion().split("\\.");
        if (!ver[0].endsWith("1")) {
            getLogger().warning("THIS PLUGIN DOES NOT SUPPORT MINECRAFT VERSION >=2. Contact the developer to update the plugin.\n" +
                    "ДАННЫЙ ПЛАГИН НЕ ПОДДЕРЖИВАЕТ MINECRAFT ВЕРСИЮ >=2. Обратитесь к разработчику для обновления плагина.");
            return;
        }
        if (ver[1].length() >= 2) version_mode = Integer.parseInt(ver[1].substring(0, 2));
        else version_mode = Integer.parseInt(ver[1]);
        if (version_mode <= 7) {
            getLogger().info("Version mode: <=1.7.10");
            if (version_mode <= 6) getLogger().info("Version mode: <=?6?");
        } else if (version_mode <= 12) getLogger().info("Version mode: <=1.12.2");
        else getLogger().warning("The plugin cannot work correctly on 1.13 and higher.");
    }
}

// Торт - это ложь.