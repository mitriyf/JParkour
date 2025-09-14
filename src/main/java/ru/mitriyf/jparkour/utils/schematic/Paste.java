package ru.mitriyf.jparkour.utils.schematic;

import org.bukkit.Location;

import java.io.File;

public interface Paste {
    void paste(Location loc, String schem) throws Exception;

    void generate(String s, File f) throws Exception;
}
