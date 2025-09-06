package ru.mitriyf.jparkour.utils.schematic;

import org.bukkit.Location;

import java.io.File;

public interface Paste {
    void paste(Location loc, File schem) throws Exception;
}
