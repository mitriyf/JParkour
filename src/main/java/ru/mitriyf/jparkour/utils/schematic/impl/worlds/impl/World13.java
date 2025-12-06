package ru.mitriyf.jparkour.utils.schematic.impl.worlds.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.World;
import ru.mitriyf.jparkour.utils.schematic.impl.worlds.WorldAPI;

import java.io.File;
import java.nio.file.Files;

public class World13 implements WorldAPI {
    @Override
    @SuppressWarnings("deprecation")
    public EditSession getSession(World world) {
        return WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
    }

    @Override
    public World getWorld(org.bukkit.World world) {
        return new BukkitWorld(world);
    }

    @Override
    public Clipboard getClipboard(File file) throws Exception {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format != null) {
            try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
                return reader.read();
            }
        }
        return null;
    }
}
