package ru.mitriyf.jparkour.utils.schematic.versions;

import org.bukkit.Location;
import ru.mitriyf.jparkour.utils.schematic.Paste;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class Paste12 implements Paste {
    private final Map<String, Object> clipboards = new HashMap<>();
    private Method toBlockPoint;
    private Constructor<?> wGet;

    public Paste12(Logger logger) {
        try {
            Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
            toBlockPoint = vectorClass.getMethod("toBlockPoint", double.class, double.class, double.class);
            Class<?> wClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
            wGet = wClass.getConstructor(org.bukkit.World.class);
        } catch (Exception e) {
            logger.warning("Error retrieving classes for schematics. Error: " + e);
        }
    }

    @Override
    public void paste(Location loc, String schem) throws Exception {
        Object lc = toBlockPoint.invoke(null, loc.getX(), loc.getY(), loc.getZ());
        Object w = wGet.newInstance(loc.getWorld());
        Object sch = clipboards.get(schem);
        Method paste = sch.getClass().getMethod("paste",
                Class.forName("com.sk89q.worldedit.world.World"),
                Class.forName("com.sk89q.worldedit.Vector"),
                boolean.class,
                boolean.class,
                Class.forName("com.sk89q.worldedit.math.transform.Transform"));
        paste.invoke(sch, w, lc, false, true, null);
    }

    @Override
    public void generate(String s, File f) throws Exception {
        Class<?> clipboardFormatsClass = Class.forName("com.sk89q.worldedit.extent.clipboard.ClipboardFormats");
        Method findFileMethod = clipboardFormatsClass.getMethod("findByFile", File.class);
        Object format = findFileMethod.invoke(null, f);
        Method loadMethod = format.getClass().getMethod("load", File.class);
        Object sch = loadMethod.invoke(format, f);
        clipboards.put(s, sch);
    }
}
