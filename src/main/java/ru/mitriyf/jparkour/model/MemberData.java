package ru.mitriyf.jparkour.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import ru.mitriyf.jparkour.task.GameTask;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MemberData {
    private final Map<Location, StandActiveData> stands = new HashMap<>();
    private final Map<Integer, Location> points = new HashMap<>();
    private int id, lefts, rights;
    private GameTask gameTask;
    private Location start, end;
    private String locale, status;
    private SchematicMemberData schematicMemberData;

    public MemberData(int id) {
        this.id = id;
    }
}
