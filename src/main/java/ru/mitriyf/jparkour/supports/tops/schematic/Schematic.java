package ru.mitriyf.jparkour.supports.tops.schematic;

import lombok.Getter;
import ru.mitriyf.jparkour.supports.tops.schematic.player.PlayerDataSchematic;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
public class Schematic {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");
    private final Map<Integer, PlayerDataSchematic> top = new HashMap<>();
    private final Map<String, Integer> topName = new HashMap<>();

    public Schematic(File schematics) {
        File[] files = schematics.listFiles();
        if (files != null) {
            List<PlayerDataSchematic> playersData = new ArrayList<>();
            for (File file : files) {
                playersData.add(new PlayerDataSchematic(file));
            }
            updateTop(playersData);
        }
    }

    private void updateTop(List<PlayerDataSchematic> playersData) {
        top.clear();
        playersData.sort((p1, p2) -> {
            int dCompare = Double.compare(p2.getAccuracy(), p1.getAccuracy());
            if (dCompare != 0) {
                return dCompare;
            }
            try {
                return getDate(p1).compareTo(getDate(p2));
            } catch (Exception e) {
                return 0;
            }
        });
        for (int i = 0; i < playersData.size(); i++) {
            PlayerDataSchematic playerData = playersData.get(i);
            int topPlace = i + 1;
            top.put(topPlace, playerData);
            topName.put(playerData.getName(), topPlace);
        }
    }

    private Date getDate(PlayerDataSchematic data) {
        return Timestamp.valueOf(LocalDateTime.parse(data.getTime(), formatter));
    }
}
