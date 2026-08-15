package ru.mitriyf.jparkour.compat.impl.v1_13;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import ru.mitriyf.jparkour.compat.abstraction.WorldGenerator;
import ru.mitriyf.jparkour.generator.EmptyGenerator;

public class WorldGeneratorV13 implements WorldGenerator {
    @Override
    public World generateWorld(String name) {
        return new WorldCreator(name).generator(new EmptyGenerator()).createWorld();
    }
}
