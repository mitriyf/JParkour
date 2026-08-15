package ru.mitriyf.jparkour.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
import ru.mitriyf.jparkour.editor.GameEditor;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.manager.PartyManager;
import ru.mitriyf.jparkour.model.MemberData;
import ru.mitriyf.jparkour.model.SchematicData;
import ru.mitriyf.jparkour.model.StandActiveData;
import ru.mitriyf.jparkour.task.GameTask;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Utils utils;
    private final Values values;
    private final JParkour plugin;
    private final GameManager gameManager;
    private final PartyManager partyManager;
    private final BukkitScheduler scheduler;
    private final EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.VOID;

    public PlayerListener(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
        partyManager = plugin.getPartyManager();
        scheduler = plugin.getServer().getScheduler();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (startWithWorld(player.getWorld())) {
            UUID uuid = player.getUniqueId();
            Game game = getGame(uuid);
            if (game == null) {
                return;
            }
            Material material = game.getTrigger();
            if (!game.isStarted() && game.isTriggerEnabled() && player.getLocation().getBlock().getType() == material && game.getLeader().getUniqueId().equals(uuid)) {
                game.start();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (startWithWorld(p.getWorld())) {
            Game game = getGame(p.getUniqueId());
            if (game != null) {
                e.setRespawnLocation(game.getLocations().getSpawn());
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (startWithWorld(e.getEntity().getWorld())) {
            Game game = gameManager.getGame(e.getEntity().getWorld().getName());
            if (game != null) {
                if (e.getCause() == cause && !e.getEntity().leaveVehicle()) {
                    game.restartActive();
                }
                SchematicData info = game.getInfo();
                if (info != null && !info.isEntityDamage()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("jparkour.admin") || e.getMessage().equalsIgnoreCase("/jparkour exit")) {
            return;
        }
        UUID uuid = p.getUniqueId();
        if (gameManager.getPlayers().containsKey(uuid) || gameManager.getWaiters().contains(uuid)) {
            utils.sendMessage(p, values.getInGame());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (startWithWorld(player.getWorld())) {
            UUID uuid = player.getUniqueId();
            Game game = gameManager.getGame(uuid);
            if (game != null) {
                MemberData memberData = game.getMemberDataMap().get(uuid);
                if (memberData != null) {
                    GameTask gameTask = memberData.getGameTask();
                    if (gameTask != null) {
                        BukkitTask task = gameTask.getTask();
                        if (task != null) {
                            task.cancel();
                        }
                    }
                }
                scheduler.runTaskLater(plugin, () -> {
                    player.spigot().respawn();
                    game.restart();
                }, 1);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR && action != Action.LEFT_CLICK_AIR) {
            return;
        }
        Player player = e.getPlayer();
        Game game = getGame(player.getUniqueId());
        if (game != null) {
            ItemStack stack = player.getItemInHand();
            if (checkInfoInteract(game, stack, player, e)) {
                return;
            }
            Block block = e.getClickedBlock();
            if (block != null && game.isDev() && (utils.isBar() || e.getHand() == EquipmentSlot.HAND)) {
                String typeStack = values.getStandsItems().get(stack);
                if (typeStack == null) {
                    return;
                }
                e.setCancelled(true);
                GameEditor gameEditor = game.getGameEditor();
                if (typeStack.equalsIgnoreCase("default")) {
                    gameEditor.setSelectedBlockAxe(block);
                    player.sendMessage("§aYou have successfully selected a block.");
                } else {
                    Location bLoc = block.getLocation();
                    String id = gameEditor.contains(bLoc);
                    if (id == null) {
                        gameEditor.setBlockStand(bLoc.add(0.5, 0, 0.5), typeStack);
                        player.sendMessage("§aYou have successfully set a stand: §e" + typeStack);
                    } else {
                        player.sendMessage("§cThis block is already linked to " + id);
                    }
                }
            }
        }
    }

    private boolean checkInfoInteract(Game game, ItemStack stack, Player player, Cancellable e) {
        SchematicData info = game.getInfo();
        if (info != null) {
            boolean defaultExits = values.getExitItems().contains(stack);
            boolean infoExits = info.getExitItems().contains(stack);
            if (defaultExits || infoExits) {
                e.setCancelled(true);
                game.close(true, false);
                return true;
            }
            boolean defaultRestarts = values.getRestartItems().contains(stack);
            boolean infoRestarts = info.getRestartItems().contains(stack);
            if (defaultRestarts || infoRestarts) {
                if (!game.getLeader().getUniqueId().equals(player.getUniqueId())) {
                    e.setCancelled(true);
                    return true;
                }
                e.setCancelled(true);
                game.playerRestart();
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        gameManager.getWaiters().remove(uuid);
        BukkitTask task = gameManager.getTasks().get(uuid);
        if (task != null) {
            task.cancel();
        }
        if (startWithWorld(player.getWorld())) {
            Game game = getGame(uuid);
            if (game != null) {
                game.close(true, false);
            }
        }
        partyManager.leavePlayer(player, uuid);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        Entity damage = e.getDamager();
        if (startWithWorld(entity.getWorld()) && damage.getType() == EntityType.PLAYER) {
            removeStands(entity, (Player) damage, "LEFT_CLICK");
        } else if (!values.isDamageWaiters()) {
            if ((damage instanceof Player && gameManager.getWaiters().contains(damage.getUniqueId())) || (entity instanceof Player && gameManager.getWaiters().contains(entity.getUniqueId()))) {
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
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            SchematicData info = getInfo(world);
            if (info != null && !info.isDropItem()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        World world = e.getEntity().getWorld();
        if (startWithWorld(world)) {
            Game game = getGame(world);
            if (game != null && e.getFoodLevel() != game.getFoodLevel()) {
                SchematicData info = game.getInfo();
                if (info != null && !info.isFoodLevelChange()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            SchematicData info = getInfo(world);
            if (info != null && !info.isPickupItem()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            SchematicData info = getInfo(world);
            if (info != null && !info.isConsumeItem()) {
                e.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void removeStands(Entity entity, Player player, String click) {
        if (entity.getType() == EntityType.ARMOR_STAND) {
            Game game = getGame(player.getUniqueId());
            if (game == null || game.isDev()) {
                return;
            }
            UUID uuid = player.getUniqueId();
            MemberData memberData = game.getMemberDataMap().get(uuid);
            GameTask gameTask = memberData.getGameTask();
            for (Map.Entry<Location, StandActiveData> stands : gameTask.getStands().entrySet()) {
                StandActiveData standActiveData = stands.getValue();
                ArmorStand armorStand = standActiveData.get(entity.getUniqueId());
                if (armorStand != null && armorStand.getCustomName().equals(click)) {
                    Map<Location, BukkitTask> bombs = gameTask.getBombs();
                    Location loc = stands.getKey();
                    if (player.getItemInHand().getType() == standActiveData.getTrigger().getType()) {
                        standActiveData.teleportToSpawn();
                        BukkitTask task = bombs.get(loc);
                        if (task != null) {
                            task.cancel();
                            bombs.remove(loc);
                            memberData.setRights(memberData.getRights() + 1);
                        } else {
                            memberData.setLefts(memberData.getLefts() + 1);
                        }
                    } else if (game.getInfo().isFailedDefuseBomb() && bombs.containsKey(loc)) {
                        bombs.remove(loc);
                        standActiveData.teleportToSpawn();
                    }
                }
            }
        }
    }

    private SchematicData getInfo(World world) {
        Game game = gameManager.getGame(world.getName());
        if (game == null) {
            return null;
        }
        return game.getInfo();
    }

    private boolean startWithWorld(World world) {
        return world.getName().startsWith(values.getWorldStart());
    }

    private Game getGame(World world) {
        return gameManager.getGame(world.getName());
    }

    private Game getGame(UUID uuid) {
        return gameManager.getGame(uuid);
    }
}
