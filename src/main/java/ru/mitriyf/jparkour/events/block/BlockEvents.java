package ru.mitriyf.jparkour.events.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import ru.mitriyf.jparkour.values.Values;

public class BlockEvents implements Listener {
    private final Values values;

    public BlockEvents(Values values) {
        this.values = values;
    }

    @EventHandler
    public void onBlock(BlockPlaceEvent e) {
        e.setCancelled(startWithWorldAndOp(e.getBlock(), e.getPlayer()));
    }

    @EventHandler
    public void onBlock(BlockBreakEvent e) {
        e.setCancelled(startWithWorldAndOp(e.getBlock(), e.getPlayer()));
    }

    @EventHandler
    public void onBlock(BlockBurnEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void waterLava(BlockFromToEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void block(EntityChangeBlockEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void block(BlockPhysicsEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void block(BlockFadeEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void block(LeavesDecayEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    @EventHandler
    public void block(BlockMultiPlaceEvent e) {
        e.setCancelled(startWithWorld(e.getBlock()));
    }

    private boolean startWithWorld(Block b) {
        return b.getWorld().getName().startsWith(values.getWorldStart());
    }

    private boolean startWithWorldAndOp(Block b, Player p) {
        return startWithWorld(b) && !p.isOp();
    }
}
