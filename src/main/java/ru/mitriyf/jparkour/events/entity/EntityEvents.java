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
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            Game game = getGame(entity);
            if (game != null && e.getSpawnReason() != reason && !game.getInfo().isCreatureSpawn()) {
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
            Game game = getGame(entity);
            if (game != null && !game.getInfo().isEntityExplode()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            Game game = getGame(entity);
            if (game != null && !game.getInfo().isEntityTarget()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            Game game = getGame(entity);
            if (game != null && !game.getInfo().isItemSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    private Game getGame(Entity entity) {
        return manager.getGame(entity.getWorld().getName());
    }

    private boolean startWithWorld(Entity e) {
        return e.getWorld().getName().startsWith(values.getWorldStart());
    }
}
