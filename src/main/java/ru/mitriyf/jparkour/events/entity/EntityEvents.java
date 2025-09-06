package ru.mitriyf.jparkour.events.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import ru.mitriyf.jparkour.values.Values;

public class EntityEvents implements Listener {
    private final CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private final Values values;

    public EntityEvents(Values values) {
        this.values = values;
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent e) {
        if (startWithWorld(e.getEntity()) && e.getSpawnReason() != reason) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void entityCreatePortal(EntityCreatePortalEvent e) {
        e.setCancelled(startWithWorld(e.getEntity()));
    }

    @EventHandler
    public void playerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(startWithWorld(e.getPlayer()));
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent e) {
        e.setCancelled(startWithWorld(e.getEntity()));
    }

    @EventHandler
    public void entityTarget(EntityTargetEvent e) {
        e.setCancelled(startWithWorld(e.getEntity()));
    }

    @EventHandler
    public void itemSpawn(ItemSpawnEvent e) {
        e.setCancelled(startWithWorld(e.getEntity()));
    }

    private boolean startWithWorld(Entity e) {
        return e.getWorld().getName().startsWith(values.getWorldStart());
    }
}
