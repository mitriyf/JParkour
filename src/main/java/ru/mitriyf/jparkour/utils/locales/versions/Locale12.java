package ru.mitriyf.jparkour.utils.locales.versions;

import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.utils.locales.Locale;

@SuppressWarnings("deprecation")
public class Locale12 implements Locale {
    @Override
    public String player(Player p) {
        return p.spigot().getLocale().toLowerCase();
    }
}
