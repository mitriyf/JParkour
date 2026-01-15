package ru.mitriyf.jparkour.events.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.Game;
import ru.mitriyf.jparkour.game.manager.Manager;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.values.Values;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;

public class BlockEvents implements Listener {
    private final Values values;
    private final Manager manager;

    public BlockEvents(JParkour plugin) {
        this.values = plugin.getValues();
        this.manager = plugin.getManager();
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
            Game game = manager.getGame(block.getWorld().getName());
            if (game != null) {
                SchematicData info = game.getInfo();
                if (!player.hasPermission("jparkour.admin") && info != null && !info.isBreakBlock()) {
                    e.setCancelled(true);
                    return;
                }
                Location loc = block.getLocation();
                Editor editor = game.getEditor();
                if (editor != null) {
                    String message = "Loc";
                    String type = editor.getStands().get(loc);
                    if (type != null) {
                        message = "Stand";
                        editor.removeBlockStand(loc);
                    } else if (loc.equals(editor.getSpawn())) {
                        type = "spawn";
                        editor.setSpawn(null);
                    } else if (loc.equals(editor.getStart())) {
                        type = "start";
                        editor.setStart(null);
                    } else if (loc.equals(editor.getEnd())) {
                        type = "end";
                        editor.setEnd(null);
                    } else {
                        return;
                    }
                    player.sendMessage("§c" + message + " " + type + " deleted.\nLocation: " + loc);
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
        Game game = manager.getGame(block.getWorld().getName());
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
