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

public class EntityEvents implements Listener {
    private final CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private final Manager manager;
    private final Values values;

    public EntityEvents(JParkour plugin) {
        values = plugin.getValues();
        manager = plugin.getManager();
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent e) {
        if (startWithWorld(e.getEntity())) {
            Game game = manager.getGame(e.getEntity().getWorld().getName());
            if (game != null && e.getSpawnReason() != reason && !game.getInfo().isCreatureSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(startWithWorld(e.getPlayer()));
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent e) {
        if (startWithWorld(e.getEntity())) {
            Game game = manager.getGame(e.getEntity().getWorld().getName());
            if (game != null && !game.getInfo().isEntityExplode()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void entityTarget(EntityTargetEvent e) {
        if (startWithWorld(e.getEntity())) {
            Game game = manager.getGame(e.getEntity().getWorld().getName());
            if (game != null && !game.getInfo().isEntityTarget()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void itemSpawn(ItemSpawnEvent e) {
        if (startWithWorld(e.getEntity())) {
            Game game = manager.getGame(e.getEntity().getWorld().getName());
            if (game != null && !game.getInfo().isItemSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    private boolean startWithWorld(Entity e) {
        return e.getWorld().getName().startsWith(values.getWorldStart());
    }
}
