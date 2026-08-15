package ru.mitriyf.jparkour.model;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class SchematicPointData {
    private final boolean teleport;
    private final double[] location;
    private final double radiusStartPoint;

    public SchematicPointData(SchematicData info, ConfigurationSection pointSection) {
        ConfigurationSection locationSection = pointSection.getConfigurationSection("location");
        location = info.toDouble(locationSection.getString("start"));
        teleport = locationSection.getBoolean("teleport");
        radiusStartPoint = pointSection.getDouble("radiusStartPoint");
    }

    public SchematicPointData() {
        teleport = false;
        location = new double[]{0, 0, 0, 0, 0};
        radiusStartPoint = 0.5;
    }
}
