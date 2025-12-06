package ru.mitriyf.jparkour.events.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.game.temp.task.data.StandActive;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.Map;
import java.util.UUID;

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
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (startWithWorld(p.getWorld())) {
            Game game = manager.getGame(p.getUniqueId());
            if (game == null) {
                if (!p.isOp()) {
                    p.teleport(new Location(plugin.getServer().getWorld("world"), 0, 80, 0));
                }
                return;
            }
            Material m = game.getTrigger();
            if (!game.isStarted() && game.isTriggerEnabled() && p.getLocation().getBlock().getType() == m) {
                game.start();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (startWithWorld(p.getWorld())) {
            Game game = manager.getGame(p.getUniqueId());
            if (game != null) {
                e.setRespawnLocation(game.getLocs().getSpawn());
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (startWithWorld(e.getEntity().getWorld())) {
            Game game = manager.getGame(e.getEntity().getWorld().getName());
            if (game != null) {
                if (e.getCause() == cause && !e.getEntity().leaveVehicle()) {
                    game.restartActive();
                }
                if (!game.getInfo().isEntityDamage()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || e.getMessage().equalsIgnoreCase("/jparkour exit")) {
            return;
        }
        UUID uuid = p.getUniqueId();
        if (manager.getPlayers().containsKey(uuid) || manager.getWaiters().contains(uuid)) {
            utils.sendMessage(p, values.getInGame());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (startWithWorld(e.getEntity().getWorld())) {
            Game game = manager.getGame(e.getEntity().getUniqueId());
            if (game != null) {
                game.getRun().getTask().cancel();
                scheduler.runTaskLater(plugin, () -> {
                    e.getEntity().spigot().respawn();
                    game.restart();
                }, 1);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Game game = manager.getGame(p.getUniqueId());
        if (game != null) {
            ItemStack stack = p.getItemInHand();
            boolean defaultExits = values.getExitItems().contains(stack);
            boolean infoExits = game.getInfo().getExitItems().contains(stack);
            if (defaultExits || infoExits) {
                e.setCancelled(true);
                game.close(true, false);
                return;
            }
            boolean defaultRestarts = values.getRestartItems().contains(stack);
            boolean infoRestarts = game.getInfo().getRestartItems().contains(stack);
            if (defaultRestarts || infoRestarts) {
                e.setCancelled(true);
                game.playerRestart();
                return;
            }
            Block b = e.getClickedBlock();
            if (e.getClickedBlock() != null && game.isDev() && (utils.isBar() || e.getHand() == EquipmentSlot.HAND)) {
                String typeStack = values.getStandsItems().get(stack);
                if (typeStack != null) {
                    e.setCancelled(true);
                    Editor editor = game.getEditor();
                    if (typeStack.equalsIgnoreCase("default")) {
                        editor.setSelectedBlockAxe(b);
                        p.sendMessage("§aYou have successfully selected a block.");
                    } else {
                        Location bLoc = b.getLocation();
                        String id = editor.contains(bLoc);
                        if (id == null) {
                            editor.setBlockStand(bLoc, typeStack);
                            p.sendMessage("§aYou have successfully set a stand: §e" + typeStack);
                        } else {
                            p.sendMessage("§cThis block is already linked to " + id);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        manager.getWaiters().remove(uuid);
        BukkitTask task = manager.getTasks().get(uuid);
        if (task != null) {
            task.cancel();
        }
        if (startWithWorld(p.getWorld())) {
            Game game = manager.getGame(uuid);
            if (game != null) {
                game.close(true, false);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        Entity damage = e.getDamager();
        if (startWithWorld(entity.getWorld()) && damage.getType() == EntityType.PLAYER) {
            removeStands(entity, (Player) damage, "LEFT_CLICK");
        } else if (!values.isDamageWaiters()) {
            if ((damage instanceof Player && manager.getWaiters().contains(damage.getUniqueId())) || (entity instanceof Player && manager.getWaiters().contains(entity.getUniqueId()))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (startWithWorld(p.getWorld())) {
            removeStands(e.getRightClicked(), p, "RIGHT_CLICK");
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (startWithWorld(p.getWorld())) {
            Game game = manager.getGame(p.getWorld().getName());
            if (game != null && !game.getInfo().isDropItem()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        World world = e.getEntity().getWorld();
        if (startWithWorld(world)) {
            Game game = manager.getGame(world.getName());
            if (game != null && e.getFoodLevel() != game.getFoodLevel() && !game.getInfo().isFoodLevelChange()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            Game game = manager.getGame(world.getName());
            if (game != null && !game.getInfo().isPickupItem()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            Game game = manager.getGame(world.getName());
            if (game != null && !game.getInfo().isConsumeItem()) {
                e.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void removeStands(Entity e, Player p, String click) {
        if (e.getType() == EntityType.ARMOR_STAND) {
            Game game = manager.getGame(p.getUniqueId());
            if (game == null) {
                return;
            }
            for (Map.Entry<Location, StandActive> stands : game.getRun().getStands().entrySet()) {
                StandActive standActive = stands.getValue();
                ArmorStand entity = standActive.get(e.getUniqueId());
                if (entity != null && entity.getCustomName().equals(click)) {
                    Map<Location, BukkitTask> bombs = game.getRun().getBombs();
                    Location loc = stands.getKey();
                    if (p.getItemInHand().getType() == standActive.getTrigger().getType()) {
                        standActive.teleportToSpawn();
                        BukkitTask task = bombs.get(loc);
                        if (task != null) {
                            task.cancel();
                            bombs.remove(loc);
                            game.setRights(game.getRights() + 1);
                        } else {
                            game.setLefts(game.getLefts() + 1);
                        }
                    } else if (game.getInfo().isFailedDefuseBomb() && bombs.containsKey(loc)) {
                        bombs.remove(loc);
                        standActive.teleportToSpawn();
                    }
                }
            }
        }
    }

    private boolean startWithWorld(World w) {
        return w.getName().startsWith(values.getWorldStart());
    }
}
