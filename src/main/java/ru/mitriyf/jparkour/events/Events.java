package ru.mitriyf.jparkour.events;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.events.block.BlockEvents;
import ru.mitriyf.jparkour.events.entity.EntityEvents;
import ru.mitriyf.jparkour.events.player.PlayerEvents;
import ru.mitriyf.jparkour.events.world.WorldEvents;

public class Events implements Listener {
    private final WorldEvents worldEvents;
    private final BlockEvents blockEvents;
    private final EntityEvents entityEvents;
    private final PlayerEvents playerEvents;
    private final JParkour plugin;

    public Events(JParkour plugin) {
        this.plugin = plugin;
        worldEvents = new WorldEvents(plugin);
        blockEvents = new BlockEvents(plugin);
        entityEvents = new EntityEvents(plugin);
        playerEvents = new PlayerEvents(plugin);
    }

    public void setup() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(worldEvents, plugin);
        pluginManager.registerEvents(blockEvents, plugin);
        pluginManager.registerEvents(entityEvents, plugin);
        pluginManager.registerEvents(playerEvents, plugin);
    }
}
