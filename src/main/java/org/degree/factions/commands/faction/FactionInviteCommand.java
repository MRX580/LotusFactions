package org.degree.factions.commands.faction;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FactionInviteCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionInviteCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localization.getMessage("messages.only_players_can_use"));
            return;
        }

        Player inviter = (Player) sender;

        String factionName;
        try {
            factionName = factionDatabase.getFactionNameForPlayer(inviter.getUniqueId().toString());
            if (factionName == null) {
                localization.sendMessageToPlayer(inviter, "messages.not_in_faction");
                return;
            }
        } catch (SQLException e) {
            localization.sendMessageToPlayer(inviter, "messages.error_fetching_faction");
            e.printStackTrace();
            return;
        }

        if (args.length < 1) {
            localization.sendMessageToPlayer(inviter, "messages.usage_faction_invite");
            return;
        }

        Player invitee = Bukkit.getPlayer(args[0]);
        if (invitee == null || !invitee.isOnline()) {
            localization.sendMessageToPlayer(inviter,
                    "messages.player_not_online",
                    Map.of("playerName", args[0])
            );
            return;
        }

        try {
            factionDatabase.addInvite(
                    factionName,
                    inviter.getUniqueId().toString(),
                    invitee.getUniqueId().toString()
            );

            localization.sendMessageToPlayer(invitee,
                    "messages.invited_to_faction",
                    Map.of("inviterName", inviter.getName())
            );

            localization.sendMessageToPlayer(inviter,
                    "messages.invitation_sent",
                    Map.of("inviteeName", invitee.getName())
            );

        } catch (SQLException e) {
            localization.sendMessageToPlayer(inviter, "messages.error_sending_invite");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender)) {
                    suggestions.add(player.getName());
                }
            }
            return suggestions;
        }
        return Collections.emptyList();
    }
}
