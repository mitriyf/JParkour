package ru.mitriyf.jparkour.utils.worlds.versions;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import ru.mitriyf.jparkour.utils.worlds.WorldGenerator;

public class Generator12 implements WorldGenerator {
    @Override
    public World generateWorld(String name) {
        return new WorldCreator(name).type(WorldType.FLAT).generatorSettings("2;0;1;").createWorld();
    }
}
