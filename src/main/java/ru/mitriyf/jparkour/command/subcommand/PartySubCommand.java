package ru.mitriyf.jparkour.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.manager.GameManager;
import ru.mitriyf.jparkour.manager.PartyManager;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.UUID;

public class PartySubCommand {
    private final PartyManager partyManager;
    private final GameManager gameManager;
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;

    public PartySubCommand(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
        partyManager = plugin.getPartyManager();
    }

    public void checkPartySubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jparkour.party")) {
            utils.sendMessage(sender, values.getNoperm());
            return;
        } else if (!values.isPartyEnabled()) {
            utils.sendMessage(sender, values.getBigSizeParty());
            return;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou console!");
            return;
        }
        Player leader = (Player) sender;
        UUID uuid = leader.getUniqueId();
        if (gameManager.getPlayers().containsKey(uuid) || gameManager.getWaiters().contains(uuid)) {
            utils.sendMessage(leader, values.getInGame());
            return;
        }
        if (args.length > 1 && args.length < 13) {
            switch (args[1].toLowerCase()) {
                case "info": {
                    if (args.length == 2) {
                        partyManager.getInfoParty(leader);
                        return;
                    }
                    break;
                }
                case "create": {
                    if (args.length == 3) {
                        partyManager.createParty(leader, args[2]);
                        return;
                    }
                    break;
                }
                case "setleader": {
                    if (args.length == 3) {
                        Player player = plugin.getServer().getPlayer(args[2]);
                        if (player == null) {
                            utils.sendMessage(leader, values.getPlayerNotFound());
                            return;
                        }
                        partyManager.setLeader(leader, player);
                        return;
                    }
                    break;
                }
                case "disband": {
                    partyManager.disbandParty(leader);
                    return;
                }
                case "invite": {
                    if (args.length == 3) {
                        Player player = plugin.getServer().getPlayer(args[2]);
                        if (player == null) {
                            utils.sendMessage(leader, values.getPlayerNotFound());
                            return;
                        }
                        partyManager.addPlayerParty(leader, player);
                        return;
                    }
                    break;
                }
                case "kick": {
                    if (args.length == 3) {
                        Player player = plugin.getServer().getPlayer(args[2]);
                        if (player == null) {
                            utils.sendMessage(leader, values.getPlayerNotFound());
                            return;
                        }
                        partyManager.kickPlayerParty(leader, player);
                        return;
                    }
                    break;
                }
                case "leave": {
                    partyManager.leavePlayer(leader, leader.getUniqueId());
                    return;
                }
                case "accept": {
                    if (args.length == 3) {
                        partyManager.acceptRequest(leader, args[2]);
                        return;
                    }
                    break;
                }
                case "deny": {
                    if (args.length == 3) {
                        partyManager.denyRequest(leader, args[2]);
                        return;
                    }
                    break;
                }
            }
        }
        utils.sendMessage(sender, values.getPartyHelp());
    }
}
