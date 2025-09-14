package ru.mitriyf.jparkour.events.world;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.values.Values;

import java.util.ArrayList;
import java.util.List;

public class WorldEvents implements Listener {
    private final List<String> unloaded = new ArrayList<>();
    private final JParkour plugin;
    private final Values values;

    public WorldEvents(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
    }

    @EventHandler
    public void stopWorldSaving(WorldUnloadEvent e) {
        World w = e.getWorld();
        if (startWithWorld(w) && !unloaded.contains(w.getName())) {
            for (Chunk chunk : w.getLoadedChunks()) {
                chunk.unload(false);
            }
            w.setAutoSave(false);
            w.setKeepSpawnInMemory(false);
            unloaded.add(w.getName());
            e.setCancelled(true);
            plugin.getServer().unloadWorld(w, false);
            return;
        }
        unloaded.remove(w.getName());
    }

    @EventHandler
    public void stopAutoSave(WorldLoadEvent e) {
        setWorldSettings(e.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void noLagOnLoad(WorldInitEvent e) {
        setWorldSettings(e.getWorld());
    }

    @EventHandler
    public void stopSaving(WorldSaveEvent e) {
        setSaveSettings(e.getWorld());
    }

    private void setWorldSettings(World w) {
        if (startWithWorld(w)) {
            w.setKeepSpawnInMemory(false);
            w.setAutoSave(false);
        }
    }

    private void setSaveSettings(World w) {
        if (startWithWorld(w)) {
            w.setAutoSave(false);
        }
    }

    private boolean startWithWorld(World w) {
        return w.getName().startsWith(values.getWorldStart());
    }
}
