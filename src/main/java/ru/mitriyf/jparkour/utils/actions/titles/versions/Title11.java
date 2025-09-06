package ru.mitriyf.jparkour.utils.actions.titles.versions;

import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.utils.actions.titles.Title;

public class Title11 implements Title {
    @Override
    public void send(Player p, String title, String subtitle, int i, int d, int k) {
        p.sendTitle(title, subtitle, i, d, k);
    }
}
