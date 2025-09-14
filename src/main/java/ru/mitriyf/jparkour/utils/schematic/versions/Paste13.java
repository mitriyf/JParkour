package ru.mitriyf.jparkour.utils.schematic.versions;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import ru.mitriyf.jparkour.utils.schematic.Paste;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Paste13 implements Paste {
    private final Map<String, Clipboard> clipboards = new HashMap<>();

    @Override
    public void paste(Location loc, String schem) {
        World w = FaweAPI.getWorld(loc.getWorld().getName());
        clipboards.get(schem).paste(w, BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), false, false, null);
    }

    @Override
    public void generate(String s, File f) throws IOException {
        Clipboard clipboard = FaweAPI.load(f);
        clipboards.put(s, clipboard);
    }
}
