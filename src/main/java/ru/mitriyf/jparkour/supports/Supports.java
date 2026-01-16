package ru.mitriyf.jparkour.supports;

import lombok.Getter;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.supports.placeholders.Placeholders;
import ru.mitriyf.jparkour.supports.tops.Tops;
import ru.mitriyf.jparkour.values.Values;

public class Supports {
    private final JParkour plugin;
    private final Values values;
    @Getter
    private Placeholders placeholders;
    @Getter
    private Tops tops;

    public Supports(JParkour plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }

    public void register() {
        if (values.isTopsEnabled()) {
            tops = new Tops(plugin);
            tops.startTimer();
        }
        if (values.isPlaceholderAPI()) {
            placeholders = new Placeholders(plugin);
            placeholders.register();
        }
    }

    public void unregister() {
        if (placeholders != null) {
            placeholders.unregister();
            placeholders = null;
        }
        if (tops != null && tops.getTask() != null) {
            tops.stopTimer();
            tops.getSchematic().clear();
        }
    }
}
