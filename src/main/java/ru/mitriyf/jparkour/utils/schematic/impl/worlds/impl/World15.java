package ru.mitriyf.jparkour.utils.schematic.impl.worlds.impl;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import ru.mitriyf.jparkour.utils.schematic.impl.worlds.WorldAPI;

import java.io.File;

public class World15 implements WorldAPI {
    @Override
    public EditSession getSession(World world) {
        return WorldEdit.getInstance().newEditSession(world);
    }

    @Override
    public World getWorld(org.bukkit.World world) {
        return FaweAPI.getWorld(world.getName());
    }

    @Override
    public Clipboard getClipboard(File file) throws Exception {
        return FaweAPI.load(file);
    }
}
