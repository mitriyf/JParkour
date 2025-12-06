package ru.mitriyf.jparkour.values.data.schematic.point;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ru.mitriyf.jparkour.values.data.schematic.SchematicData;

@Getter
public class SchematicPoint {
    private final int yaw, pitch;
    private final boolean teleport;
    private final double[] location;
    private final double radiusStartPoint;

    public SchematicPoint(SchematicData info, ConfigurationSection pointSection) {
        ConfigurationSection locationSection = pointSection.getConfigurationSection("location");
        location = info.toDouble(locationSection.getString("start"));
        ConfigurationSection teleportSection = locationSection.getConfigurationSection("teleport");
        teleport = teleportSection.getBoolean("enabled");
        yaw = teleportSection.getInt("yaw");
        pitch = teleportSection.getInt("pitch");
        radiusStartPoint = pointSection.getDouble("radiusStartPoint");
    }
}
