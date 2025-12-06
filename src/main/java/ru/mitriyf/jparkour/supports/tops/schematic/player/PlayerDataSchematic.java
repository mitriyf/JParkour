package ru.mitriyf.jparkour.supports.tops.schematic.player;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@Getter
public class PlayerDataSchematic {
    private final String name;
    private final String time;
    private final int accuracy;

    public PlayerDataSchematic(File data) {
        YamlConfiguration playerData = YamlConfiguration.loadConfiguration(data);
        name = playerData.getString("name");
        time = playerData.getString("time");
        accuracy = playerData.getInt("accuracy");
    }
}
