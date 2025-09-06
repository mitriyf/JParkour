package ru.mitriyf.jparkour.utils.locales.versions;

import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.utils.locales.Locale;

public class Locale13 implements Locale {
    @Override
    public String player(Player p) {
        return p.getLocale().toLowerCase();
    }
}
