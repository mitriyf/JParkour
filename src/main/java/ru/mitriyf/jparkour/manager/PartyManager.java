package ru.mitriyf.jparkour.manager;

import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.model.PartyData;
import ru.mitriyf.jparkour.model.RequestData;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class PartyManager {
    private final String[] infoSearch = new String[]{"%members%", "%leader%", "%player%", "%party%"};
    private final Map<UUID, Set<RequestData>> setRequestDataMap = new HashMap<>();
    private final Map<String, PartyData> partyDataMap = new HashMap<>();
    private final String[] search = {"%leader%", "%player%", "%party%"};
    private final ThreadLocalRandom random;
    private final JParkour plugin;
    private final Server server;
    private final Values values;
    private final Utils utils;

    public PartyManager(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        server = plugin.getServer();
        values = plugin.getValues();
        random = plugin.getRandom();
    }

    public void createParty(Player player, String partyName) {
        UUID leaderUuid = player.getUniqueId();
        String playerName = player.getName();
        for (PartyData partyData : partyDataMap.values()) {
            if (partyData.getMembers().contains(leaderUuid)) {
                utils.sendMessage(player, values.getHaveCreated(), search, new String[]{playerName, playerName, partyData.getPartyName()});
                return;
            }
        }
        if (partyDataMap.containsKey(partyName)) {
            utils.sendMessage(player, values.getAlreadyCreated(), search, new String[]{playerName, playerName, null});
            return;
        }
        UUID uuid = player.getUniqueId();
        Set<UUID> uuidSet = new HashSet<>();
        uuidSet.add(uuid);
        partyDataMap.put(partyName, new PartyData(partyName, uuid, uuidSet));
        utils.sendMessage(player, values.getPartyCreated(), search, new String[]{playerName, playerName, partyName});
    }

    public void addPlayerParty(Player leader, Player player) {
        UUID leaderUuid = leader.getUniqueId();
        for (PartyData partyData : partyDataMap.values()) {
            if (partyData.getMembers().contains(leaderUuid)) {
                String partyName = partyData.getPartyName();
                if (partyData.getLeader().equals(leaderUuid)) {
                    sendRequest(partyData.getPartyName(), leader, player);
                } else {
                    utils.sendMessage(leader, values.getNotLeader(), search, new String[]{server.getPlayer(partyData.getLeader()).getName(), player.getName(), partyName});
                }
                return;
            }
        }
        utils.sendMessage(leader, values.getNotInParty(), search, new String[]{leader.getName(), player.getName(), null});
    }

    public void setLeader(Player leader, Player player) {
        UUID leaderUuid = leader.getUniqueId();
        String playerName = player.getName();
        for (PartyData partyData : partyDataMap.values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(leaderUuid)) {
                String partyName = partyData.getPartyName();
                UUID partyLeader = partyData.getLeader();
                if (partyLeader.equals(leaderUuid)) {
                    UUID playerUuid = player.getUniqueId();
                    if (partyLeader.equals(playerUuid)) {
                        utils.sendMessage(leader, values.getAlreadyLeader(), search, new String[]{playerName, playerName, partyName});
                        return;
                    }
                    partyData.setLeader(playerUuid);
                    utils.sendMessage(leader, values.getNewLeader(), search, new String[]{playerName, playerName, partyName});
                } else {
                    utils.sendMessage(leader, values.getNotLeader(), search, new String[]{server.getPlayer(partyData.getLeader()).getName(), playerName, partyName});
                }
                return;
            }
        }
        utils.sendMessage(leader, values.getNotInParty(), search, new String[]{playerName, playerName, null});
    }

    public void disbandParty(Player leader) {
        UUID leaderUuid = leader.getUniqueId();
        String playerName = leader.getName();
        for (PartyData partyData : partyDataMap.values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(leaderUuid)) {
                String partyName = partyData.getPartyName();
                if (partyData.getLeader().equals(leaderUuid)) {
                    utils.sendMessage(leader, values.getPartyBroken(), search, new String[]{playerName, playerName, partyName});
                    members.clear();
                    partyDataMap.remove(partyData.getPartyName());
                } else {
                    utils.sendMessage(leader, values.getNotLeader(), search, new String[]{server.getPlayer(partyData.getLeader()).getName(), playerName, partyName});
                }
                return;
            }
        }
        utils.sendMessage(leader, values.getNotInParty(), search, new String[]{playerName, playerName, null});
    }

    public void getInfoParty(Player leader) {
        UUID leaderUuid = leader.getUniqueId();
        String playerName = leader.getName();
        for (PartyData partyData : partyDataMap.values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(leaderUuid)) {
                String partyName = partyData.getPartyName();
                StringBuilder membersString = new StringBuilder();
                for (UUID member : members) {
                    Player memberPlayer = server.getPlayer(member);
                    if (membersString.length() > 0) {
                        membersString.append(", ");
                    }
                    membersString.append(memberPlayer.getName());
                }
                utils.sendMessage(leader, values.getPartyInfo(), infoSearch, new String[]{membersString.toString(), getServer().getPlayer(partyData.getLeader()).getName(), playerName, partyName});
                return;
            }
        }
        utils.sendMessage(leader, values.getNotInParty(), search, new String[]{playerName, playerName, null});
    }

    public void kickPlayerParty(Player leader, Player player) {
        UUID leaderUuid = leader.getUniqueId();
        String playerName = leader.getName();
        for (PartyData partyData : partyDataMap.values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(leaderUuid)) {
                String partyName = partyData.getPartyName();
                UUID partyLeader = partyData.getLeader();
                if (partyLeader.equals(leaderUuid)) {
                    UUID playerUuid = player.getUniqueId();
                    if (members.remove(playerUuid)) {
                        String[] strings = new String[]{playerName, playerName, partyName};
                        utils.sendMessage(leader, values.getKickedFromParty(), search, strings);
                        utils.sendMessage(player, values.getKickedFromParty(), search, strings);
                        if (members.isEmpty()) {
                            partyDataMap.remove(partyData.getPartyName());
                            return;
                        }
                        if (partyLeader.equals(playerUuid)) {
                            setNewLeader(partyData, members, playerName, partyName);
                        }
                    } else {
                        utils.sendMessage(leader, values.getPlayerNotInParty(), search, new String[]{server.getPlayer(partyData.getLeader()).getName(), playerName, partyName});
                    }
                } else {
                    utils.sendMessage(leader, values.getNotLeader(), search, new String[]{server.getPlayer(partyData.getLeader()).getName(), playerName, partyName});
                }
                return;
            }
        }
        utils.sendMessage(leader, values.getNotInParty(), search, new String[]{playerName, playerName, null});
    }

    public void leavePlayer(Player player, UUID uuid) {
        String playerName = player.getName();
        for (PartyData partyData : partyDataMap.values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(uuid)) {
                String partyName = partyData.getPartyName();
                UUID leader = partyData.getLeader();
                members.remove(uuid);
                String leaderName = server.getPlayer(leader).getName();
                String[] strings = new String[]{leaderName, playerName, partyName};
                utils.sendMessage(player, values.getPlayerLeaved(), search, strings);
                if (members.isEmpty()) {
                    partyDataMap.remove(partyData.getPartyName());
                    return;
                }
                if (leader.equals(uuid)) {
                    setNewLeader(partyData, members, playerName, partyName);
                }
                utils.sendMessage(server.getPlayer(partyData.getLeader()), values.getPlayerLeaved(), search, strings);
                return;
            }
        }
        utils.sendMessage(player, values.getNotInParty(), search, new String[]{playerName, playerName, null});
    }

    private void setNewLeader(PartyData partyData, Set<UUID> members, String playerName, String partyName) {
        List<UUID> memberList = new ArrayList<>(members);
        UUID newLeader = memberList.get(random.nextInt(memberList.size()));
        partyData.setLeader(newLeader);
        utils.sendMessage(server.getPlayer(newLeader), values.getNewLeader(), search, new String[]{server.getPlayer(partyData.getLeader()).getName(), playerName, partyName});
    }

    private void sendRequest(String partyName, Player leader, Player player) {
        UUID uuid = player.getUniqueId();
        String[] strings = new String[]{leader.getName(), player.getName(), partyName};
        for (PartyData partyData : partyDataMap.values()) {
            Set<UUID> members = partyData.getMembers();
            if (members.contains(uuid)) {
                utils.sendMessage(leader, values.getInParty(), search, strings);
                return;
            }
        }
        Set<RequestData> requestDataList = setRequestDataMap.computeIfAbsent(uuid, k -> new HashSet<>());
        for (RequestData requestData : requestDataList) {
            if (requestData.getPartyName().equals(partyName)) {
                utils.sendMessage(leader, values.getAlreadySendRequest(), search, strings);
                return;
            }
        }
        RequestData requestData = new RequestData(uuid, partyName);
        requestDataList.add(requestData);
        requestData.setTask(server.getScheduler().runTaskLater(plugin, () -> {
            requestDataList.remove(requestData);
            PartyData partyData = partyDataMap.get(partyName);
            if (partyData != null) {
                utils.sendMessage(player, values.getPlayerTimeLeaved(), search, strings);
                utils.sendMessage(leader, values.getLeaderTimeLeaved(), search, strings);
            }
        }, values.getTimeForJoin()));
        utils.sendMessage(player, values.getPlayerGetRequest(), search, strings);
        utils.sendMessage(leader, values.getLeaderSendRequest(), search, strings);
    }

    public void acceptRequest(Player player, String partyName) {
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();
        String[] strings = new String[]{playerName, playerName, null};
        Set<RequestData> requestDataList = setRequestDataMap.get(uuid);
        if (requestDataList == null) {
            utils.sendMessage(player, values.getNoHaveInvites(), search, strings);
            return;
        }
        for (RequestData requestData : requestDataList) {
            if (requestData.getPartyName().equals(partyName)) {
                PartyData partyData = partyDataMap.get(partyName);
                if (partyData == null) {
                    utils.sendMessage(player, values.getPartyBroken(), search, new String[]{playerName, playerName, partyName});
                    return;
                }
                setRequestDataMap.remove(uuid);
                cancelTasks(requestDataList);
                partyData.getMembers().add(uuid);
                Player leader = server.getPlayer(partyData.getLeader());
                utils.sendMessage(leader, values.getJoinedToParty(), search, new String[]{leader.getName(), playerName, partyName});
                return;
            }
        }
        utils.sendMessage(player, values.getNotInParty(), search, strings);
    }

    public void denyRequest(Player player, String partyName) {
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();
        Set<RequestData> requestDataList = setRequestDataMap.get(uuid);
        String[] strings = new String[]{playerName, playerName, null};
        if (requestDataList == null) {
            utils.sendMessage(player, values.getNoHaveInvites(), search, strings);
            return;
        }
        for (RequestData requestData : new HashSet<>(requestDataList)) {
            if (requestData.getPartyName().equals(partyName)) {
                requestData.getTask().cancel();
                requestDataList.remove(requestData);
                utils.sendMessage(player, values.getPlayerDenyInvite(), search, new String[]{playerName, playerName, partyName});
                PartyData partyData = partyDataMap.get(partyName);
                if (partyData != null) {
                    Player leader = server.getPlayer(partyData.getLeader());
                    utils.sendMessage(leader, values.getDeniedInviteParty(), search, new String[]{leader.getName(), playerName, partyName});
                }
                return;
            }
        }
        utils.sendMessage(player, values.getDontHaveInvite(), search, strings);
    }

    private void cancelTasks(Set<RequestData> requestDataList) {
        for (RequestData requestData : requestDataList) {
            requestData.getTask().cancel();
        }
    }
}
