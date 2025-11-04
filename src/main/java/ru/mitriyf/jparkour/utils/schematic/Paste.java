package ru.mitriyf.jparkour.utils.schematic;

import org.bukkit.Location;

import java.io.File;

public interface Paste {
    void paste(Location loc, String schem, boolean pasteAir) throws Exception;

    void save(String name, Location pose1, Location pose2, Location pose3) throws Exception;

    void generate(String s, File f) throws Exception;
}
