package ru.mitriyf.jparkour.utils.colors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CMiniMessage implements Colorizer {
    @Override
    public String colorize(String message) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
