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

public class BlockEvents implements Listener {
    private final Values values;
    private final Manager manager;

    public BlockEvents(JParkour plugin) {
        this.values = plugin.getValues();
        this.manager = plugin.getManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (startWithWorldAndOp(e.getBlock(), e.getPlayer())) {
            Game game = manager.getGame(e.getPlayer().getWorld().getName());
            if (game != null && !game.getInfo().isPlaceBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getPlayer().getWorld().getName());
            if (game != null) {
                if (!e.getPlayer().isOp() && !game.getInfo().isBreakBlock()) {
                    e.setCancelled(true);
                    return;
                }
                Location loc = e.getBlock().getLocation();
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
                    e.getPlayer().sendMessage("§c" + message + " " + type + " deleted.\nLocation: " + loc);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isBurnBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isIgniteBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isFromToBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isEntityChangeBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isPhysicsBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isFadeBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isLeavesDecay()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        if (startWithWorld(e.getBlock())) {
            Game game = manager.getGame(e.getBlock().getWorld().getName());
            if (game != null && !game.getInfo().isMultiPlaceBlock()) {
                e.setCancelled(true);
            }
        }
    }

    private boolean startWithWorld(Block b) {
        return b.getWorld().getName().startsWith(values.getWorldStart());
    }

    private boolean startWithWorldAndOp(Block b, Player p) {
        return startWithWorld(b) && !p.isOp();
    }
}
