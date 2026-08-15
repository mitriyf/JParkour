package ru.mitriyf.jparkour.model;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class StandLocationData {
    private final String type;
    private final Location location;

    public StandLocationData(Location location, String type) {
        this.location = location;
        this.type = type;
    }
}
