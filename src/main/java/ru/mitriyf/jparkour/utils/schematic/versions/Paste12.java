package ru.mitriyf.jparkour.utils.schematic.versions;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.utils.schematic.Paste;
import ru.mitriyf.jparkour.utils.schematic.versions.worlds.WorldAPI;
import ru.mitriyf.jparkour.utils.schematic.versions.worlds.versions.World13;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class Paste12 implements Paste {
    private final Map<String, Object> clipboards = new HashMap<>();
    private final WorldAPI worldAPI;
    private final File schematicsDir;
    private Class<?> vClass, worldData;
    private Method toBlockPoint, findFile, setSourceMaskMethod;
    private Constructor<?> createClipboard, createRegion, copyForward, maskConstructor;
    private Object schematicFormat;

    public Paste12(JParkour plugin) {
        schematicsDir = new File(plugin.getValues().getDataFolder(), plugin.getValues().getSchematicsDir());
        worldAPI = new World13();
        try {
            Class<?> clipboardFormats = Class.forName("com.sk89q.worldedit.extent.clipboard.ClipboardFormats");
            worldData = Class.forName("com.sk89q.worldedit.world.registry.WorldData");
            maskConstructor = ExistingBlockMask.class.getConstructor(Extent.class);
            setSourceMaskMethod = ForwardExtentCopy.class.getMethod("setSourceMask", Mask.class);
            vClass = Class.forName("com.sk89q.worldedit.Vector");
            copyForward = ForwardExtentCopy.class.getConstructor(Extent.class, Region.class, Extent.class, vClass);
            toBlockPoint = vClass.getMethod("toBlockPoint", double.class, double.class, double.class);
            findFile = clipboardFormats.getMethod("findByFile", File.class);
            createRegion = CuboidRegion.class.getConstructor(World.class, vClass, vClass);
            createClipboard = BlockArrayClipboard.class.getConstructor(Region.class);
            schematicFormat = ClipboardFormat.class.getField("SCHEMATIC").get(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Error retrieving classes for schematics. Error: " + e);
        }
    }

    @Override
    public void paste(Location loc, String schem, boolean pasteAir) throws Exception {
        Object vector = toBlockPoint.invoke(null, loc.getX(), loc.getY(), loc.getZ());
        Object world = worldAPI.getWorld(loc.getWorld());
        Object sch = clipboards.get(schem);
        Method paste = sch.getClass().getMethod("paste", World.class, vClass, boolean.class, boolean.class, Transform.class);
        paste.invoke(sch, world, vector, false, pasteAir, null);
    }

    @Override
    public void save(String name, Location pose1, Location pose2, Location pose3) throws Exception {
        Object pose3Vector = toBlockPoint.invoke(null, pose3.getX(), pose3.getY(), pose3.getZ());
        World world = worldAPI.getWorld(pose1.getWorld());
        Object region = createRegion(world, pose1, pose2);
        Object clipboard = createClipboard.newInstance(region);
        Method setOrigin = clipboard.getClass().getMethod("setOrigin", vClass);
        setOrigin.invoke(clipboard, pose3Vector);
        File file = new File(schematicsDir, name + ".schematic");
        EditSession editSession = worldAPI.getSession(world);
        Method getMinimumPoint = clipboard.getClass().getMethod("getMinimumPoint");
        Object copy = copyForward.newInstance(editSession, region, clipboard, getMinimumPoint.invoke(clipboard));
        Object sourceMask = maskConstructor.newInstance(editSession);
        setSourceMaskMethod.invoke(copy, sourceMask);
        Operations.completeLegacy((Operation) copy);
        Method getWriterMethod = schematicFormat.getClass().getMethod("getWriter", OutputStream.class);
        Object writer = getWriterMethod.invoke(schematicFormat, Files.newOutputStream(file.toPath()));
        Method writeMethod = writer.getClass().getMethod("write", Clipboard.class, worldData);
        Method getWorldDataMethod = world.getClass().getMethod("getWorldData");
        Object worldData = getWorldDataMethod.invoke(world);
        writeMethod.invoke(writer, clipboard, worldData);
        editSession.flushQueue();
    }

    private Object createRegion(World world, Location pose1, Location pose2) throws Exception {
        Location min;
        Location max;
        if (pose1.getY() < pose2.getY()) {
            min = pose1;
            max = pose2;
        } else {
            min = pose2;
            max = pose1;
        }
        Object pose1Vector = toBlockPoint.invoke(null, min.getX(), min.getY(), min.getZ());
        Object pose2Vector = toBlockPoint.invoke(null, max.getX(), max.getY(), max.getZ());
        return createRegion.newInstance(world, pose1Vector, pose2Vector);
    }

    @Override
    public void generate(String s, File f) throws Exception {
        Object format = findFile.invoke(null, f);
        Method load = format.getClass().getMethod("load", File.class);
        Object sch = load.invoke(format, f);
        clipboards.put(s, sch);
    }
}
