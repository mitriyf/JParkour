package ru.mitriyf.jparkour.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SchematicMemberData {
    private final Map<Integer, SchematicPointData> points = new HashMap<>();
    private final Map<double[], String> stands = new HashMap<>();
    private final double[] start, end;
    @Setter
    private int maxRights, maxLefts;

    public SchematicMemberData(double[] start, double[] end) {
        this.start = start;
        this.end = end;
    }
}
