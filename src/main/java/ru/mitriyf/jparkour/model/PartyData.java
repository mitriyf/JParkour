package ru.mitriyf.jparkour.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class PartyData {
    private final Set<UUID> members;
    private final String partyName;
    private UUID leader;

    public PartyData(String partyName, UUID leader, Set<UUID> members) {
        this.leader = leader;
        this.members = members;
        this.partyName = partyName;
    }
}
