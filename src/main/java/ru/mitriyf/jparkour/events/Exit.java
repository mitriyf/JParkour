package ru.mitriyf.jparkour.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.manager.Manager;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.utils.Utils;

import java.util.Map;
import java.util.Set;

public class Exit implements Listener {
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;
    private final Manager manager;

    public Exit(JParkour plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
        this.manager = plugin.getManager();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || e.getMessage().equalsIgnoreCase("/jparkour exit")) return;
        if (values.getPlayers().containsKey(p.getUniqueId()) || manager.getWaiters().contains(p.getUniqueId())) {
            utils.sendMessage(p, values.getInGame());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void interactExit(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (values.getPlayers().containsKey(p.getUniqueId())) {
            if (p.getItemInHand().getType() == Material.BARRIER) {
                String game = values.getPlayers().get(p.getUniqueId()).getGame();
                values.getRooms().get(game).close();
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart())) return;
        Player p = e.getPlayer();
        if (values.getPlayers().get(p.getUniqueId()) == null) {
            p.teleport(new Location(Bukkit.getWorld("world"), 0, 80, 0));
            return;
        }
        String id = values.getPlayers().get(p.getUniqueId()).getGame();
        Game game = values.getRooms().get(id);
        Material m = game.getTrigger();
        if (!game.isStarted()) {
            if (p.getLocation().getBlock().getType() == m) {
                game.start();
            }
        }
    }

    @EventHandler
    public void food(FoodLevelChangeEvent e) {
        if (e.getEntity().getWorld().getName().startsWith(values.getWorldStart()))
            if (e.getFoodLevel() != 10) e.setCancelled(true);
    }

    @EventHandler
    public void onBlock(BlockPlaceEvent e) {
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart())) return;
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }

    @EventHandler
    public void stopSaving(WorldSaveEvent e) {
        World w = e.getWorld();
        if (!w.getName().startsWith(values.getWorldStart())) return;
        w.setAutoSave(false);
    }

    @EventHandler
    public void stopWorldSaving(WorldUnloadEvent event) {
        World w = event.getWorld();
        if (manager.getUnloaded().contains(w.getName()) || !w.getName().startsWith(values.getWorldStart())) return;
        for (Chunk chunk : w.getLoadedChunks()) chunk.unload(false);
        w.setAutoSave(false);
        w.setKeepSpawnInMemory(false);
        manager.getUnloaded().add(w.getName());
        event.setCancelled(true);
        Bukkit.unloadWorld(w, false);
    }
    @EventHandler
    public void stopAutoSave(WorldLoadEvent event) {
        World w = event.getWorld();
        if (!w.getName().startsWith(values.getWorldStart())) return;
        w.setAutoSave(false);
        w.setKeepSpawnInMemory(false);
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void noLagOnLoad(WorldInitEvent event) {
        World w = event.getWorld();
        if (!w.getName().startsWith(values.getWorldStart())) return;
        w.setKeepSpawnInMemory(false);
    }
    @EventHandler
    public void onBlock(BlockBreakEvent e) {
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart())) return;
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }
    @EventHandler
    public void onBlock(BlockBurnEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void onBlock(PlayerDeathEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart())) return;
        String id = values.getPlayers().get(e.getEntity().getUniqueId()).getGame();
        Game game = values.getRooms().get(id);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            e.getEntity().spigot().respawn();
            game.restart();
        }, 1);
    }
    @EventHandler
    public void onBlock(PlayerRespawnEvent e) {
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart())) return;
        String id = values.getPlayers().get(e.getPlayer().getUniqueId()).getGame();
        Game game = values.getRooms().get(id);
        e.setRespawnLocation(game.getSpawn());
    }
    @EventHandler
    public void interact(PlayerInteractAtEntityEvent e) {
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart()) && !manager.getWaiters().contains(e.getPlayer().getUniqueId())) return;
        removeStands(e.getRightClicked(), e.getPlayer(), "RIGHT_CLICK");
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart()) && !manager.getWaiters().contains(e.getEntity().getUniqueId()) || e.getDamager().getType() != EntityType.PLAYER) return;
        removeStands(e.getEntity(), (Player) e.getDamager(), "LEFT_CLICK");
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart()) && !manager.getWaiters().contains(e.getEntity().getUniqueId())) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                String id = values.getPlayers().get(e.getEntity().getUniqueId()).getGame();
                Game game = values.getRooms().get(id);
                game.restart();
            } else return;
        }
        e.setCancelled(true);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.getWaiters().remove(e.getPlayer().getUniqueId());
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart())) return;
        String id = values.getPlayers().get(e.getPlayer().getUniqueId()).getGame();
        Game game = values.getRooms().get(id);
        game.close();
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void waterLava(BlockFromToEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(EntityChangeBlockEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(BlockPhysicsEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(BlockFadeEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(LeavesDecayEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(BlockMultiPlaceEvent e) {
        if (!e.getBlock().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void mobSpawn(PlayerDropItemEvent e) {
        if (!e.getPlayer().getLocation().getWorld().getName().startsWith(values.getWorldStart())) return;
        if (values.getSlots().containsValue(e.getItemDrop().getItemStack())) e.setCancelled(true);
    }
    @EventHandler
    public void mobExplode(EntityExplodeEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void mobSpawn(ItemSpawnEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void mobSpawn(CreatureSpawnEvent e) {
        if (!e.getLocation().getWorld().getName().startsWith(values.getWorldStart())) return;
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) e.setCancelled(true);
    }
    @EventHandler
    public void mobTarget(EntityTargetEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        if (!e.getPlayer().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void onEntityCreatePortalEvent(EntityCreatePortalEvent e) {
        if (!e.getEntity().getWorld().getName().startsWith(values.getWorldStart())) return;
        e.setCancelled(true);
    }
    private void removeStands(Entity e, Player p, String click) {
        if (e.getType() == EntityType.ARMOR_STAND) {
            ArmorStand entity = (ArmorStand) e;
            String id = values.getPlayers().get(p.getUniqueId()).getGame();
            Game game = values.getRooms().get(id);
            for (Map.Entry<Location, Set<ArmorStand>> a : game.getStands().entrySet()) {
                if (a.getValue().contains(entity) && entity.getCustomName().equalsIgnoreCase(click)) {
                    if (p.getItemInHand().isSimilar(entity.getBoots())) {
                        a.getValue().forEach(ArmorStand::remove);
                        if (game.getBombs().get(a.getKey()) != null) {
                            game.getBombs().get(a.getKey()).cancel();
                            game.getBombs().remove(a.getKey());
                            game.setRights(game.getRights() + 1);
                        } else game.setLefts(game.getLefts() + 1);
                    }
                    else if (game.getBombs().get(a.getKey()) != null) {
                        game.getBombs().remove(a.getKey());
                        a.getValue().forEach(ArmorStand::remove);
                    }
                }
            }
        }
    }
}
