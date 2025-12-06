package ru.mitriyf.jparkour.utils.worlds.impl;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import ru.mitriyf.jparkour.utils.worlds.WorldGenerator;
import ru.mitriyf.jparkour.utils.worlds.impl.world13.EmptyGenerator;

public class Generator13 implements WorldGenerator {
    @Override
    public World generateWorld(String name) {
        return new WorldCreator(name).generator(new EmptyGenerator()).createWorld();
    }
}
