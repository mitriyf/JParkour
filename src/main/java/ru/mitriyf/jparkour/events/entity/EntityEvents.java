package ru.mitriyf.jparkour.events.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;

public class EntityEvents implements Listener {
    private final CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private final Manager manager;
    private final Values values;

    public EntityEvents(JParkour plugin) {
        values = plugin.getValues();
        manager = plugin.getManager();
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            SchematicData info = getInfo(entity);
            if (info != null && e.getSpawnReason() != reason && !info.isCreatureSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(startWithWorld(e.getPlayer()));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            SchematicData info = getInfo(entity);
            if (info != null && !info.isEntityExplode()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            SchematicData info = getInfo(entity);
            if (info != null && !info.isEntityTarget()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            SchematicData info = getInfo(entity);
            if (info != null && !info.isItemSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    private SchematicData getInfo(Entity entity) {
        Game game = manager.getGame(entity.getWorld().getName());
        if (game == null) {
            return null;
        }
        return game.getInfo();
    }

    private boolean startWithWorld(Entity e) {
        return e.getWorld().getName().startsWith(values.getWorldStart());
    }
}
