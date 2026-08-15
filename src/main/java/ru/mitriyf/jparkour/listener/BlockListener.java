package ru.mitriyf.jparkour.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.editor.GameEditor;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.model.SchematicData;
import ru.mitriyf.jparkour.model.StandLocationData;
import ru.mitriyf.jparkour.values.Values;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockListener implements Listener {
    private final Values values;
    private final GameManager gameManager;

    public BlockListener(JParkour plugin) {
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if (startWithWorldAndOp(block, player)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isPlaceBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            Player player = e.getPlayer();
            Game game = gameManager.getGame(block.getWorld().getName());
            if (game != null) {
                SchematicData info = game.getInfo();
                if (!player.hasPermission("jparkour.admin") && info != null && !info.isBreakBlock()) {
                    e.setCancelled(true);
                    return;
                }
                Set<Location> locationSet = new HashSet<>();
                Location loc = block.getLocation();
                GameEditor gameEditor = game.getGameEditor();
                if (gameEditor != null) {
                    String message = "Loc";
                    StandLocationData standLocationData = gameEditor.getStands().get(loc);
                    StringBuilder type = new StringBuilder();
                    if (standLocationData != null) {
                        message = "Stand";
                        type.append(standLocationData.getType());
                        gameEditor.removeBlockStand(loc);
                        locationSet.add(standLocationData.getLocation());
                    } else {
                        List<Location> locations = gameEditor.getDefaultLocations().get(loc.getBlock().getLocation());
                        if (locations == null) {
                            return;
                        }
                        locationSet.addAll(new HashSet<>(locations));
                        for (Location location : locations) {
                            if (type.length() > 0) {
                                type.append(", ");
                            }
                            if (location.equals(gameEditor.getSpawn())) {
                                type.append("spawn");
                                gameEditor.setSpawn(null);
                            } else if (location.equals(gameEditor.getStart())) {
                                type.append("start");
                                gameEditor.setStart(null);
                            } else if (location.equals(gameEditor.getEnd())) {
                                type.append("end");
                                gameEditor.setEnd(null);
                            } else if (location.equals(gameEditor.getPortal())) {
                                type.append("portal");
                                gameEditor.setPortal(null);
                            }
                        }
                    }
                    player.sendMessage("§c" + message + " " + type + " deleted.\nLocation: " + locationSet);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isBurnBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isIgniteBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isFromToBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isEntityChangeBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isPhysicsBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isFadeBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isLeavesDecay()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            SchematicData info = getInfo(block);
            if (info != null && !info.isMultiPlaceBlock()) {
                e.setCancelled(true);
            }
        }
    }

    private SchematicData getInfo(Block block) {
        Game game = gameManager.getGame(block.getWorld().getName());
        if (game == null) {
            return null;
        }
        return game.getInfo();
    }

    private boolean startWithWorld(Block b) {
        return b.getWorld().getName().startsWith(values.getWorldStart());
    }

    private boolean startWithWorldAndOp(Block b, Player p) {
        return startWithWorld(b) && !p.isOp();
    }
}
