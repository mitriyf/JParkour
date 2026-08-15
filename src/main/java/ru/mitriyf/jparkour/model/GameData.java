package ru.mitriyf.jparkour.model;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

@Getter
public class GameData {
    private final Set<Player> members;
    private final Player leader;

    public GameData(Player leader, Set<Player> members) {
        this.leader = leader;
        this.members = members;
    }
}
