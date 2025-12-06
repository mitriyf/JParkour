package ru.mitriyf.jparkour.utils.schematic.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.utils.schematic.Paste;
import ru.mitriyf.jparkour.utils.schematic.impl.worlds.WorldAPI;
import ru.mitriyf.jparkour.utils.schematic.impl.worlds.impl.World13;
import ru.mitriyf.jparkour.utils.schematic.impl.worlds.impl.World15;
import ru.mitriyf.jparkour.values.Values;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class Paste13 implements Paste {
    private final Map<String, Clipboard> clipboards = new HashMap<>();
    private final WorldAPI worldAPI;
    private final Values values;

    public Paste13(JParkour plugin) {
        values = plugin.getValues();
        if (plugin.getVersion() < 15) {
            worldAPI = new World13();
        } else {
            worldAPI = new World15();
        }
    }

    @Override
    public void paste(Location loc, String schem, boolean pasteAir) {
        World world = worldAPI.getWorld(loc.getWorld());
        EditSession editSession = worldAPI.getSession(world);
        Clipboard clipboard = clipboards.get(schem);
        clipboard.paste(editSession, BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), pasteAir, null);
        Operations.complete(editSession.commit());
        editSession.close();
    }

    @Override
    public void save(String name, Location pose1, Location pose2, Location pose3) throws Exception {
        World world = worldAPI.getWorld(pose1.getWorld());
        CuboidRegion region = getCuboidRegion(pose1, pose2, world);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        BlockVector3 origin = BlockVector3.at(pose3.getX(), pose3.getY(), pose3.getZ());
        clipboard.setOrigin(origin);
        EditSession editSession = worldAPI.getSession(world);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        Operations.complete(forwardExtentCopy);
        File file = new File(values.getDataFolder(), values.getSchematicsDir() + name + ".schem");
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(Files.newOutputStream(file.toPath()))) {
            writer.write(clipboard);
        }
        editSession.close();
    }

    private CuboidRegion getCuboidRegion(Location pose1, Location pose2, World world) {
        BlockVector3 loc1 = BlockVector3.at(pose1.getX(), pose1.getY(), pose1.getZ());
        BlockVector3 loc2 = BlockVector3.at(pose2.getX(), pose2.getY(), pose2.getZ());
        BlockVector3 min;
        BlockVector3 max;
        if (loc1.getY() < loc2.getY()) {
            min = loc1;
            max = loc2;
        } else {
            min = loc2;
            max = loc1;
        }
        return new CuboidRegion(world, min, max);
    }

    @Override
    public void generate(String schematicName, File file) throws Exception {
        Clipboard clipboard = worldAPI.getClipboard(file);
        clipboards.put(schematicName, clipboard);
    }
}
