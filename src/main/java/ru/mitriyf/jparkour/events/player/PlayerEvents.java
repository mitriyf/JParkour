package ru.mitriyf.jparkour.events.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.Map;
import java.util.Set;

public class PlayerEvents implements Listener {
    private final EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.VOID;
    private final Utils utils;
    private final Values values;
    private final Manager manager;
    private final JParkour plugin;
    private final BukkitScheduler scheduler;

    public PlayerEvents(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        manager = plugin.getManager();
        scheduler = plugin.getServer().getScheduler();
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        if (startWithWorld(e.getPlayer().getWorld())) {
            Player p = e.getPlayer();
            if (manager.getPlayers().get(p.getUniqueId()) == null) {
                p.teleport(new Location(Bukkit.getWorld("world"), 0, 80, 0));
                return;
            }
            String id = manager.getPlayers().get(p.getUniqueId()).getGame();
            Game game = values.getRooms().get(id);
            Material m = game.getTrigger();
            if (!game.isStarted() && p.getLocation().getBlock().getType() == m) {
                game.start();
            }
        }
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (startWithWorld(p.getWorld())) {
            String id = manager.getPlayers().get(p.getUniqueId()).getGame();
            Game game = values.getRooms().get(id);
            e.setRespawnLocation(game.getLocs().getSpawn());
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageEvent e) {
        if (startWithWorld(e.getEntity().getWorld())) {
            if (e.getCause() == cause && manager.getPlayers().containsKey(e.getEntity().getUniqueId()) && !e.getEntity().leaveVehicle()) {
                String id = manager.getPlayers().get(e.getEntity().getUniqueId()).getGame();
                Game game = values.getRooms().get(id);
                game.restart();
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || e.getMessage().equalsIgnoreCase("/jparkour exit")) {
            return;
        }
        if (manager.getPlayers().containsKey(p.getUniqueId()) || manager.getWaiters().contains(p.getUniqueId())) {
            utils.sendMessage(p, values.getInGame());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent e) {
        if (startWithWorld(e.getEntity().getWorld())) {
            String id = manager.getPlayers().get(e.getEntity().getUniqueId()).getGame();
            Game game = values.getRooms().get(id);
            game.getRun().getTask().cancel();
            scheduler.runTaskLater(plugin, () -> {
                e.getEntity().spigot().respawn();
                game.restart();
            }, 1);
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void playerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (manager.getPlayers().containsKey(p.getUniqueId())) {
            if (p.getItemInHand().getType() == Material.BARRIER) {
                String game = manager.getPlayers().get(p.getUniqueId()).getGame();
                values.getRooms().get(game).close(true, false);
            }
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        manager.getWaiters().remove(e.getPlayer().getUniqueId());
        if (startWithWorld(e.getPlayer().getWorld())) {
            String id = manager.getPlayers().get(e.getPlayer().getUniqueId()).getGame();
            Game game = values.getRooms().get(id);
            game.close(true, false);
        }
    }

    @EventHandler
    public void entityDamageByEntity(EntityDamageByEntityEvent e) {
        if (startWithWorld(e.getEntity().getWorld()) && manager.getPlayers().containsKey(e.getDamager().getUniqueId()) && e.getDamager().getType() == EntityType.PLAYER) {
            removeStands(e.getEntity(), (Player) e.getDamager(), "LEFT_CLICK");
        }
    }

    @EventHandler
    public void playerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (startWithWorld(e.getPlayer().getWorld()) && manager.getPlayers().containsKey(e.getPlayer().getUniqueId())) {
            removeStands(e.getRightClicked(), e.getPlayer(), "RIGHT_CLICK");
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent e) {
        if (startWithWorld(e.getPlayer().getWorld()) && manager.getPlayers().containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void foodLevelChange(FoodLevelChangeEvent e) {
        if (startWithWorld(e.getEntity().getWorld()) && e.getFoodLevel() != 10) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void playerPickupItem(PlayerPickupItemEvent e) {
        e.setCancelled(startWithWorld(e.getPlayer().getWorld()));
    }

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent e) {
        e.setCancelled(startWithWorld(e.getPlayer().getWorld()));
    }

    @SuppressWarnings("deprecation")
    private void removeStands(Entity e, Player p, String click) {
        if (e.getType() == EntityType.ARMOR_STAND) {
            ArmorStand entity = (ArmorStand) e;
            String id = manager.getPlayers().get(p.getUniqueId()).getGame();
            Game game = values.getRooms().get(id);
            for (Map.Entry<Location, Set<ArmorStand>> a : game.getRun().getStands().entrySet()) {
                if (a.getValue().contains(entity) && entity.getCustomName().equalsIgnoreCase(click)) {
                    Map<Location, BukkitTask> bombs = game.getRun().getBombs();
                    if (p.getItemInHand().getType() == entity.getBoots().getType()) {
                        a.getValue().forEach(ArmorStand::remove);
                        if (game.getRun().getBombs().get(a.getKey()) != null) {
                            bombs.get(a.getKey()).cancel();
                            bombs.remove(a.getKey());
                            game.setRights(game.getRights() + 1);
                        } else {
                            game.setLefts(game.getLefts() + 1);
                        }
                    } else if (bombs.get(a.getKey()) != null) {
                        bombs.remove(a.getKey());
                        a.getValue().forEach(ArmorStand::remove);
                    }
                }
            }
        }
    }

    private boolean startWithWorld(World w) {
        return w.getName().startsWith(values.getWorldStart());
    }
}
