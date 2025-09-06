package ru.mitriyf.jparkour.utils.schematic.versions;

import org.bukkit.Location;
import ru.mitriyf.jparkour.utils.schematic.Paste;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@SuppressWarnings("all")
public class Paste12 implements Paste {
    @Override
    public void paste(Location loc, File schem) throws Exception {
        Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
        Method toBlockPointMethod = vectorClass.getMethod("toBlockPoint", double.class, double.class, double.class);
        Object lc = toBlockPointMethod.invoke(null, loc.getX(), loc.getY(), loc.getZ());
        Class<?> clipboardFormatsClass = Class.forName("com.sk89q.worldedit.extent.clipboard.ClipboardFormats");
        Method findFileMethod = clipboardFormatsClass.getMethod("findByFile", File.class);
        Object format = findFileMethod.invoke(null, schem);
        Method loadMethod = format.getClass().getMethod("load", File.class);
        Object sch = loadMethod.invoke(format, schem);
        Class<?> wClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
        Constructor<?> wConstructor = wClass.getConstructor(org.bukkit.World.class);
        Object w = wConstructor.newInstance(loc.getWorld());
        Method paste = sch.getClass().getMethod("paste",
                Class.forName("com.sk89q.worldedit.world.World"),
                Class.forName("com.sk89q.worldedit.Vector"),
                boolean.class,
                boolean.class,
                Class.forName("com.sk89q.worldedit.math.transform.Transform"));
        paste.invoke(sch, w, lc, false, true, null);
    }
}
