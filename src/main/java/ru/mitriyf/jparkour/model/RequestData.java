package ru.mitriyf.jparkour.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Getter
public class RequestData {
    private final String partyName;
    private final UUID uuid;
    @Setter
    private BukkitTask task;

    public RequestData(UUID uuid, String partyName) {
        this.uuid = uuid;
        this.partyName = partyName;
    }
}
