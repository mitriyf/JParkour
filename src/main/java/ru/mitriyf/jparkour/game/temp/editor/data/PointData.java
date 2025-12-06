package ru.mitriyf.jparkour.game.temp.editor.data;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class PointData {
    private final Location location;
    private final double radiusStartPoint;
    private final boolean teleportEnabled;

    public PointData(Location location, double radiusStartPoint, boolean teleportEnabled) {
        this.location = location;
        this.teleportEnabled = teleportEnabled;
        this.radiusStartPoint = radiusStartPoint;
    }
}
