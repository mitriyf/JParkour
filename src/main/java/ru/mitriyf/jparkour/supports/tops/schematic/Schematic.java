package ru.mitriyf.jparkour.supports.tops.schematic;

import lombok.Getter;
import ru.mitriyf.jparkour.supports.tops.schematic.player.SPlayerData;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Schematic {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");
    private final Map<Integer, SPlayerData> top = new HashMap<>();

    public Schematic(File schematics) {
        File[] files = schematics.listFiles();
        if (files != null) {
            List<SPlayerData> playerDataList = new ArrayList<>();
            for (File playerFile : files) {
                playerDataList.add(new SPlayerData(playerFile));
            }
            updateTop(playerDataList);
        }
    }

    private void updateTop(List<SPlayerData> allPlayersData) {
        top.clear();
        List<SPlayerData> sortedPlayers = allPlayersData.stream().sorted((p1, p2) -> {
            int dCompare = Double.compare(p2.getAccuracy(), p1.getAccuracy());
            if (dCompare != 0) {
                return dCompare;
            }
            try {
                Date date1 = Timestamp.valueOf(LocalDateTime.parse(p1.getTime(), formatter));
                Date date2 = Timestamp.valueOf(LocalDateTime.parse(p2.getTime(), formatter));
                return date1.compareTo(date2);
            } catch (Exception e) {
                return 0;
            }
        }).collect(Collectors.toList());
        for (int i = 0; i < sortedPlayers.size(); i++) {
            top.put(i + 1, sortedPlayers.get(i));
        }
    }
}
